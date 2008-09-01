/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.service.lookup;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


/**
 * Implementation of {@link ILookupService} that caches objects.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CachingLookupService extends AbstractLookupService {

    /**
     * The cache.
     */
    private final Cache cache;


    /**
     * Creates a new <tt>CachingLookupService</tt>.
     *
     * @param service the archetype service
     * @param cache   the cache
     */
    public CachingLookupService(IArchetypeService service, Cache cache) {
        super(service);
        this.cache = cache;
        service.addListener(
                "lookup.*", new IArchetypeServiceListener() {
            public void saved(IMObject object) {
                addLookup((Lookup) object);
            }

            public void removed(IMObject object) {
                removeLookup((Lookup) object);
            }
        });
    }

    /**
     * Loads lookups into the cache.
     *
     * @param shortNames the lookup archetype short names. May contain wildcards
     */
    public void setCached(String ... shortNames) {
        for (String wildcard : shortNames) {
            String[] expanded = DescriptorHelper.getShortNames(wildcard, false,
                                                               getService());
            for (String shortName : expanded) {
                getLookups(shortName);
            }
        }
    }

    /**
     * Returns the lookup with the specified lookup archetype short name and
     * code.
     *
     * @param shortName the lookup archetype short name
     * @param code      the lookup code
     * @return the corresponding lookup or <tt>null</tt> if none is found
     */
    public Lookup getLookup(String shortName, String code) {
        Lookup result;
        Element element = cache.get(new Key(shortName, code));
        if (element != null) {
            result = (Lookup) element.getObjectValue();
        } else {
            result = super.getLookup(shortName, code);
            if (result != null) {
                addLookup(result);
            }
        }
        return result;
    }

    /**
     * Returns all lookups with the specified lookup archetype short name.
     *
     * @param shortName the lookup archetype short name
     * @return a collection of lookups with the specified short name
     */
    public Collection<Lookup> getLookups(String shortName) {
        Collection<Lookup> result;
        Key key = new Key(shortName);
        Element element = cache.get(key);
        if (element != null) {
            result = new ArrayList<Lookup>();
            Set<IMObjectReference> references
                    = (Set<IMObjectReference>) element.getObjectValue();
            for (IMObjectReference reference : references) {
                Lookup lookup = getLookup(reference);
                if (lookup != null) {
                    result.add(lookup);
                }
            }
        } else {
            result = query(shortName);
            Set<IMObjectReference> references
                    = new HashSet<IMObjectReference>();
            for (Lookup lookup : result) {
                references.add(lookup.getObjectReference());
                addLookup(lookup);
            }
            cache.put(new Element(key, references));
        }
        return result;
    }

    /**
     * Returns the default lookup for the specified lookup archetype short name.
     *
     * @param shortName the lookup archetype short name
     * @return the default lookup, or <Tt>null</tt> if none is found
     */
    public Lookup getDefaultLookup(String shortName) {
        for (Lookup lookup : getLookups(shortName)) {
            if (lookup.isDefaultLookup()) {
                return lookup;
            }
        }
        return null;
    }

    /**
     * Retrieves a lookup by reference.
     *
     * @param reference the lookup reference. May be <tt>null</tt>
     * @return the corresponding lookup, or <tt>null</tt> if none is found
     */
    @Override
    protected Lookup getLookup(IMObjectReference reference) {
        Lookup result = null;
        if (reference != null) {
            Element element = cache.get(new Key(reference));
            if (element != null) {
                result = (Lookup) element.getObjectValue();
            } else {
                result = super.getLookup(reference);
                if (result != null) {
                    addLookup(result);
                }
            }
        }
        return result;
    }

    /**
     * Adds a lookup to the cache.
     *
     * @param lookup the lookup to add
     */
    private void addLookup(Lookup lookup) {
        String shortName = lookup.getArchetypeId().getShortName();

        Key refKey = new Key(lookup.getObjectReference());
        Key codeKey = new Key(shortName, lookup.getCode());
        cache.put(new Element(refKey, lookup));
        cache.put(new Element(codeKey, lookup));

        Key collectionKey = new Key(shortName);
        Element element = cache.get(collectionKey);
        if (element != null) {
            synchronized (element) {
                Set<IMObjectReference> lookups
                        = (Set<IMObjectReference>) element.getObjectValue();
                lookups.add(lookup.getObjectReference());
            }
        }
    }

    /**
     * Remove a lookup from the cache.
     *
     * @param lookup the lookup to remove
     */
    private void removeLookup(Lookup lookup) {
        String shortName = lookup.getArchetypeId().getShortName();

        Key refKey = new Key(lookup.getObjectReference());
        Key codeKey = new Key(shortName, lookup.getCode());
        cache.remove(refKey);
        cache.remove(codeKey);

        Key collectionKey = new Key(shortName);
        Element element = cache.get(collectionKey);
        if (element != null) {
            synchronized (element) {
                Set<IMObjectReference> lookups
                        = (Set<IMObjectReference>) element.getObjectValue();
                lookups.remove(lookup.getObjectReference());
            }
        }
    }

    private static class Key implements Serializable {

        private String key;

        private IMObjectReference ref;

        private int hashCode;

        public Key(String shortName) {
            key = shortName;
            hashCode = key.hashCode();
        }

        public Key(String shortName, String code) {
            key = shortName + "-" + code;
            hashCode = key.hashCode();
        }

        public Key(IMObjectReference ref) {
            this.ref = ref;
            hashCode = ref.hashCode();
        }

        /**
         * Indicates whether some other object is "equal to" this one.
         *
         * @param obj the reference object with which to compare.
         * @return <code>true</code> if this object is the same as the obj
         *         argument; <code>false</code> otherwise.
         */
        @Override
        public boolean equals(Object obj) {
            Key other = (Key) obj;
            return (ObjectUtils.equals(key, other.key)
                    || ObjectUtils.equals(ref, other.ref));
        }

        /**
         * Returns a hash code value for the object.
         *
         * @return a hash code value for this object.
         */
        @Override
        public int hashCode() {
            return hashCode;
        }
    }

}
