/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.service.lookup;

import org.apache.commons.lang.ObjectUtils;
import org.ehcache.Cache;
import org.openvpms.component.business.dao.im.common.IMObjectDAO;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.AbstractArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.business.service.cache.EhCacheable;
import org.openvpms.component.business.service.cache.EhcacheManager;
import org.openvpms.component.model.lookup.Lookup;
import org.openvpms.component.model.lookup.LookupRelationship;
import org.openvpms.component.model.object.Reference;
import org.springframework.beans.factory.DisposableBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


/**
 * Implementation of {@link ILookupService} that caches objects.
 * <br/>
 * This uses the supplied {@link EhcacheManager} to create a cache named "lookupCache".
 * <br/>
 * Note that race conditions can occur if a lookup is updated while a collection of the same lookups are
 * being loaded - TODO.
 *
 * @author Tim Anderson
 */
public class CachingLookupService extends AbstractLookupService implements DisposableBean, EhCacheable {

    /**
     * The cache.
     */
    private final Cache<Key, Object> cache;

    /**
     * The listener for updates.
     */
    private final IArchetypeServiceListener listener;

    /**
     * The archetypes to monitor updates for.
     */
    private static final String ARCHETYPES = "lookup.*";

    /**
     * Constructs a {@link CachingLookupService}.
     *
     * @param service      the archetype service
     * @param dao          the data access object
     * @param cacheManager the cache manager
     */
    public CachingLookupService(IArchetypeService service, IMObjectDAO dao, EhcacheManager cacheManager) {
        super(service, dao);
        this.cache = cacheManager.create("lookupCache", Key.class, Object.class);
        listener = new AbstractArchetypeServiceListener() {

            @Override
            public void saved(IMObject object) {
                addLookup((Lookup) object, true);
            }

            @Override
            public void removed(IMObject object) {
                removeLookup((Lookup) object, true);
            }
        };
        service.addListener(ARCHETYPES, listener);
    }

    /**
     * Invoked by a BeanFactory on destruction of a singleton.
     */
    @Override
    public void destroy() {
        getService().removeListener(ARCHETYPES, listener);
        cache.clear();
    }

    /**
     * Loads lookups into the cache.
     *
     * @param shortNames the lookup archetype short names. May contain wildcards
     */
    public void setCached(String... shortNames) {
        for (String wildcard : shortNames) {
            String[] expanded = DescriptorHelper.getShortNames(wildcard, false, getService());
            for (String shortName : expanded) {
                getLookups(shortName);
            }
        }
    }

    /**
     * Returns the lookup with the specified lookup archetype short name and code.
     *
     * @param shortName   the lookup archetype short name. May contain wildcards
     * @param code        the lookup code
     * @param activeOnly, if {@code true}, the lookup must be active, otherwise it must be active/inactive
     * @return the corresponding lookup or {@code null} if none is found
     */
    @Override
    public Lookup getLookup(String shortName, String code, boolean activeOnly) {
        Lookup result = (Lookup) cache.get(new Key(shortName, code));
        if (result != null) {
            if (activeOnly && !result.isActive()) {
                result = null;
            }
        } else {
            result = super.getLookup(shortName, code, activeOnly);
            if (result != null) {
                addLookup(result, false);
            }
        }
        return result;
    }

    /**
     * Returns all active lookups with the specified lookup archetype short name.
     *
     * @param shortName the lookup archetype short name
     * @return a collection of lookups with the specified short name
     */
    @SuppressWarnings("unchecked")
    public Collection<Lookup> getLookups(String shortName) {
        Collection<Lookup> result;
        Key key = new Key(shortName);
        Set<Reference> references = (Set<Reference>) cache.get(key);
        if (references != null) {
            result = new ArrayList<>();
            synchronized (references) {
                for (Reference reference : references) {
                    Lookup lookup = getLookup(reference);
                    if (lookup != null && lookup.isActive()) {
                        result.add(lookup);
                    }
                }
            }
        } else {
            result = query(shortName);
            references = new HashSet<>();
            for (Lookup lookup : result) {
                references.add(lookup.getObjectReference());
                addLookup(lookup, false);
            }
            cache.put(key, references);
        }
        return result;
    }

    /**
     * Returns the default lookup for the specified lookup archetype short name.
     *
     * @param shortName the lookup archetype short name
     * @return the default lookup, or {@code null} if none is found
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
     * Clears the cache.
     */
    @Override
    public void clear() {
        cache.clear();
    }

    /**
     * Returns the underlying cache.
     *
     * @return the underlying cache
     */
    @Override
    public Cache getCache() {
        return cache;
    }

    /**
     * Retrieves a lookup by reference.
     *
     * @param reference the lookup reference. May be {@code null}
     * @return the corresponding lookup, or {@code null} if none is found. The lookup may be inactive
     */
    @Override
    protected Lookup getLookup(Reference reference) {
        Lookup result = null;
        if (reference != null) {
            result = getCached(new Key(reference));
            if (result == null) {
                result = super.getLookup(reference);
                if (result != null) {
                    addLookup(result, false);
                }
            }
        }
        return result;
    }

    /**
     * Returns a cached lookup,  given its key.
     *
     * @param key the lookup key
     * @return the cached lookup, or {@code null} if none is found
     */
    private Lookup getCached(Key key) {
        return (Lookup) cache.get(key);
    }

    /**
     * Adds a lookup to the cache.
     *
     * @param lookup       the lookup to add
     * @param evictRelated if {@code true}, evict related objects to force them to be reloaded on next access
     */
    private void addLookup(Lookup lookup, boolean evictRelated) {
        String archetype = lookup.getArchetype();
        Reference reference = lookup.getObjectReference();
        Lookup original = null;

        Key refKey = new Key(reference);
        Key codeKey = new Key(archetype, lookup.getCode());
        if (evictRelated) {
            original = getCached(refKey);
            if (original == null) {
                // may have expired. Try and get it via the code key.
                original = getCached(codeKey);
            }
        }
        cache.put(refKey, lookup);
        cache.put(codeKey, lookup);

        if (lookup.isActive()) {
            addToCollection(reference);
        } else {
            removeFromCollection(reference);
        }

        if (original != null) {
            // if relationships have been removed, this evicts any of those related lookups that have the relationships
            evictRelated(original, reference);
        }
        if (evictRelated) {
            // if relationships have been added, this evicts any of those cached lookups that don't have the
            // relationships
            evictRelated(lookup, reference);
        }
    }

    /**
     * Evicts related lookups.
     *
     * @param lookup    the lookup
     * @param reference the lookup reference
     */
    private void evictRelated(Lookup lookup, Reference reference) {
        for (LookupRelationship relationship : lookup.getLookupRelationships()) {
            Reference source = relationship.getSource();
            Lookup cached = null;
            if (source != null && !reference.equals(source)) {
                cached = getCached(new Key(source));
            } else {
                Reference target = relationship.getTarget();
                if (target != null && !reference.equals(target)) {
                    cached = getCached(new Key(target));
                }
            }
            if (cached != null) {
                removeLookup(cached, false);
            }
        }
    }

    /**
     * Adds a lookup to the collection of lookups for a particular archetype.
     *
     * @param reference the lookup reference
     */
    @SuppressWarnings("unchecked")
    private void addToCollection(Reference reference) {
        String archetype = reference.getArchetype();
        Set<Key> keys = getKeys();
        for (Key key : keys) {
            if (key.matches(archetype)) {
                Set<Reference> lookups = (Set<Reference>) cache.get(key);
                if (lookups != null) {
                    synchronized (lookups) {
                        lookups.add(reference);
                    }
                }
            }
        }
    }

    /**
     * Returns the cache keys.
     *
     * @return the cache keys
     */
    private Set<Key> getKeys() {
        Set<Key> keys = new HashSet<>();
        cache.forEach(entry -> keys.add(entry.getKey()));
        return keys;
    }

    /**
     * Remove a lookup from the cache.
     *
     * @param lookup               the lookup to remove
     * @param removeFromCollection if {@code true}, remove it from the collection cache as well
     */
    private void removeLookup(Lookup lookup, boolean removeFromCollection) {
        Reference reference = lookup.getObjectReference();
        Key refKey = new Key(reference);
        Key codeKey = new Key(lookup.getArchetype(), lookup.getCode());
        cache.remove(refKey);
        cache.remove(codeKey);

        evictRelated(lookup, reference);

        if (removeFromCollection) {
            removeFromCollection(reference);
        }
    }

    /**
     * Removes a lookup from the collection of lookups for a particular lookup archetype.
     *
     * @param reference the lookup reference
     */
    @SuppressWarnings("unchecked")
    private void removeFromCollection(Reference reference) {
        String archetype = reference.getArchetype();
        for (Key key : getKeys()) {
            if (key.matches(archetype)) {
                Set<Reference> lookups = (Set<Reference>) cache.get(key);
                if (lookups != null) {
                    synchronized (lookups) {
                        lookups.remove(reference);
                    }
                }
            }
        }
    }

    private static class Key implements Serializable {

        /**
         * Non-null if a short name or shortName + code is being used as the identifier.
         */
        private String key;

        /**
         * Non-null if a reference is being used as the identifier.
         */
        private Reference ref;

        /**
         * Determines if the key is a short name only.
         */
        private boolean isShortName = false;

        /**
         * Key hash code.
         */
        private int hashCode;

        public Key(String shortName) {
            key = shortName;
            isShortName = true;
            hashCode = key.hashCode();
        }

        public Key(String shortName, String code) {
            key = shortName + "-" + code;
            hashCode = key.hashCode();
        }

        public Key(Reference ref) {
            this.ref = ref;
            hashCode = ref.hashCode();
        }

        /**
         * Indicates whether some other object is "equal to" this one.
         *
         * @param obj the reference object with which to compare.
         * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
         */
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Key)) {
                return false;
            }
            Key other = (Key) obj;
            return (ObjectUtils.equals(key, other.key) || ObjectUtils.equals(ref, other.ref));
        }

        /**
         * Determines if the specified short name matches the key.
         *
         * @param shortName the short name
         * @return {@code true} if they match, otherwise {@code alse}
         */
        public boolean matches(String shortName) {
            return (isShortName) && TypeHelper.matches(shortName, key);
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
