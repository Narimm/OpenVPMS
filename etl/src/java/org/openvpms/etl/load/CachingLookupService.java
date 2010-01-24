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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.etl.load;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.resources.Messages;
import org.openvpms.component.business.dao.im.common.IMObjectDAO;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.lookup.AbstractLookupService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * A lookup service that caches lookups.
 * <p/>
 * TODO - should be replaced by
 * {@link org.openvpms.component.business.service.lookup.CachingLookupService}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CachingLookupService extends AbstractLookupService {

    /**
     * A map of lookup short names to their corresponding codes and lookups.
     */
    private final Map<String, Map<String, Lookup>> lookupsByArchetype
            = new HashMap<String, Map<String, Lookup>>();

    /**
     * A map of lookups keyed on reference.
     */
    private final Map<IMObjectReference, Lookup> lookupsByRef
            = new HashMap<IMObjectReference, Lookup>();


    /**
     * A cache of target lookups, keyed on source lookup reference.
     */
    private final Map<IMObjectReference, RelatedLookups> targetLookups
            = new HashMap<IMObjectReference, RelatedLookups>();

    /**
     * The logger.
     */
    private final Log log = LogFactory.getLog(CachingLookupService.class);

    /**
     * Logging messages.
     */
    private static final Messages messages
            = Messages.getMessages("org.openvpms.etl.load.messages"); // NON-NLS


    /**
     * Constructs a new <tt>CachingLookupService</tt>.
     *
     * @param service the archetype service
     * @param dao     the data access object
     */
    public CachingLookupService(IArchetypeService service, IMObjectDAO dao) {
        super(service, dao);
    }

    /**
     * Returns the lookup with the specified lookup archetype short name and
     * code.
     *
     * @param shortName the lookup archetype short name. May contain wildcards
     * @param code      the lookup code
     * @return the corresponding lookup or <tt>null</tt> if none is found
     */
    public Lookup getLookup(String shortName, String code) {
        String[] shortNames = getShortNames(shortName);
        for (String name : shortNames) {
            Map<String, Lookup> lookups = get(name);
            Lookup lookup = lookups.get(code);
            if (lookup != null) {
                return lookup;
            }
        }
        return null;
    }

    /**
     * Returns all lookups with the specified lookup archetype short name.
     *
     * @param shortName the lookup archetype short name. May contain wildcards
     * @return a collection of lookups with the specified short name
     */
    @SuppressWarnings("unchecked")
    public Collection<Lookup> getLookups(String shortName) {
        String[] shortNames = getShortNames(shortName);
        List<Lookup> result = new ArrayList<Lookup>();
        for (String name : shortNames) {
            Map<String, Lookup> lookups = get(name);
            result.addAll(lookups.values());
        }
        return result;
    }

    /**
     * Returns the default lookup for the specified lookup archetype short name.
     *
     * @param shortName the lookup archetype short name. May contain wildcards
     * @return the default lookup, or <Tt>null</tt> if none is found
     */
    public Lookup getDefaultLookup(String shortName) {
        String[] shortNames = getShortNames(shortName);
        for (String name : shortNames) {
            for (Lookup lookup : getLookups(name)) {
                if (lookup.isDefaultLookup()) {
                    return lookup;
                }
            }
        }
        return null;
    }

    /**
     * Adds a lookup to the cache. If the lookup already exists, it will be
     * replaced.
     *
     * @param lookup the lookup to add
     */
    public synchronized void add(Lookup lookup) {
        lookupsByRef.put(lookup.getObjectReference(), lookup);
        String shortName = lookup.getArchetypeId().getShortName();
        Map<String, Lookup> lookups = get(shortName);
        lookups.put(lookup.getCode(), lookup);
    }

    /**
     * Adds a relationship to the cache.
     *
     * @param relationship the relationship to add
     */
    public synchronized void add(LookupRelationship relationship) {
        Lookup source = null;
        Lookup target = null;
        if (relationship.getSource() != null) {
            source = getLookup(relationship.getSource());
        }
        if (relationship.getTarget() != null) {
            target = getLookup(relationship.getTarget());
        }
        if (source != null && target != null) {
            source.addLookupRelationship(relationship);
            target.addLookupRelationship(relationship);
            RelatedLookups related = getTargets(source);
            related.add(relationship.getArchetypeId().getShortName(), target);
        }
    }

    /**
     * Returns a lookup given its reference.
     *
     * @param reference the lookup reference
     * @return the corresponding lookup, or <tt>null</tt> if it doesn't exist
     */
    public synchronized Lookup getLookup(IMObjectReference reference) {
        String shortName = reference.getArchetypeId().getShortName();
        get(shortName);
        return lookupsByRef.get(reference);
    }

    /**
     * Returns the lookups that are the target of any lookup relationship where
     * the supplied lookup is the source.
     * <p/>
     * This implementation caches target lookups.
     *
     * @param lookup the source lookup
     * @return a collection of target lookups
     */
    @Override
    public Collection<Lookup> getTargetLookups(Lookup lookup) {
        RelatedLookups related = getTargets(lookup);
        return related.getLookups();
    }

    /**
     * Returns the lookups that are the target of specific lookup relationships
     * where the supplied lookup is the source.
     * <p/>
     * This implementation caches target lookups.
     *
     * @param lookup                the source lookup
     * @param relationshipShortName the relationship short name. May contain
     *                              wildcards
     * @return a collection of target lookups
     */
    @Override
    public Collection<Lookup> getTargetLookups(
            Lookup lookup, String relationshipShortName) {
        RelatedLookups related = getTargets(lookup);
        Collection<Lookup> targets = related.getLookups(relationshipShortName);
        if (targets == null) {
            targets = super.getTargetLookups(lookup, relationshipShortName);
            related.setLookups(relationshipShortName, targets);
        }
        return targets;
    }

    /**
     * Returns all lookups for the specified short name, keyed on lookup code.
     *
     * @param shortName the lookup short name
     * @return the lookups, keyed on code.
     */
    private synchronized Map<String, Lookup> get(String shortName) {
        Map<String, Lookup> lookups = lookupsByArchetype.get(shortName);
        if (lookups == null) {
            lookups = new HashMap<String, Lookup>();
            Collection<Lookup> list = query(shortName);
            for (Lookup lookup : list) {
                String code = lookup.getCode();
                if (lookups.containsKey(code)) {
                    log.warn(messages.getMessage(
                            "DuplicateLookup", new Object[]{shortName, code}));
                } else {
                    lookups.put(code, lookup);
                    lookupsByRef.put(lookup.getObjectReference(), lookup);
                }
            }
            lookupsByArchetype.put(shortName, lookups);
        }
        return lookups;
    }

    /**
     * Returns the targets of the specified lookup.
     *
     * @param lookup the lookup
     * @return the targets of the lookup
     */
    private RelatedLookups getTargets(Lookup lookup) {
        IMObjectReference ref = lookup.getObjectReference();
        RelatedLookups related = targetLookups.get(ref);
        if (related == null) {
            Collection<Lookup> targets = super.getTargetLookups(lookup);
            related = new RelatedLookups(targets);
            targetLookups.put(ref, related);
        }
        return related;
    }

    /**
     * Expands a wildcarded short name.
     *
     * @param shortName the short name
     * @return all short names matching the wildcarded short name
     */
    private String[] getShortNames(String shortName) {
        if (shortName.contains("*")) {
            return DescriptorHelper.getShortNames(shortName, getService());
        }
        return new String[]{shortName};
    }

    /**
     * Cache of related lookups.
     */
    private static class RelatedLookups {

        /**
         * The related lookups.
         */
        private Set<Lookup> lookups;

        /**
         * The related lookups, keyed on relationship short name.
         */
        private Map<String, Collection<Lookup>> lookupsByRelationship
                = new HashMap<String, Collection<Lookup>>();


        /**
         * Creates a new <tt>RelatedLookups</tt>.
         *
         * @param lookups the related lookups
         */
        public RelatedLookups(Collection<Lookup> lookups) {
            this.lookups = new HashSet<Lookup>(lookups);
        }

        /**
         * Returns the related lookups.
         *
         * @return the related lookups
         */
        public Collection<Lookup> getLookups() {
            return lookups;
        }

        /**
         * Returns the related lookups for a lookup relationship short name.
         *
         * @param relationship the lookup relationship short name
         * @return the related lookups, or <tt>null</tt> if they aren't cached
         */
        public Collection<Lookup> getLookups(String relationship) {
            return lookupsByRelationship.get(relationship);
        }

        /**
         * Sets the related lookups for a lookup relationship short name.
         *
         * @param relationship the lookup relationship short name
         * @param lookups      the related lookups
         */
        public void setLookups(String relationship,
                               Collection<Lookup> lookups) {
            lookupsByRelationship.put(relationship,
                                      new ArrayList<Lookup>(lookups));
        }

        /**
         * Adds a related lookup.
         *
         * @param relationship the lookup relationship short name
         * @param lookup       the related lookup
         */
        public void add(String relationship, Lookup lookup) {
            lookups.add(lookup);

            Collection<Lookup> lookups
                    = lookupsByRelationship.get(relationship);
            if (lookups != null) {
                lookups.add(lookup);
            }
        }
    }

}
