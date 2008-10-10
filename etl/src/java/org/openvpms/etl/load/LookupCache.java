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

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Lookup cache.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class LookupCache {

    /**
     * A map of lookup relationships short names to the corresponding
     * relationship instances.
     */
    private Map<String, List<LookupRelationship>> relationshipsByArchetype
            = new HashMap<String, List<LookupRelationship>>();

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookupService;

    /**
     * The logger.
     */
    private final Log log = LogFactory.getLog(LookupCache.class);


    /**
     * Constructs a new <tt>LookupCache</tt>.
     *
     * @param service the archetype service
     */
    @SuppressWarnings("HardCodedStringLiteral")
    public LookupCache(IArchetypeService service,
                       ILookupService lookupService) {
        this.service = service;
        this.lookupService = lookupService;
        if (!(lookupService instanceof CachingLookupService)) {
            log.warn(CachingLookupService.class.getName()
                    + " is not configured. The lookup cache may "
                    + "not perform optimally");
        }
    }

    /**
     * Returns the lookup for a specified lookup archetype short name and code.
     *
     * @param archetype the lookup archetype short name
     * @param code      the lookup code
     * @return the corresponding lookup, or <tt>null</tt> if none exists
     * @throws ArchetypeServiceException for any error
     */
    public Lookup get(String archetype, String code) {
        return lookupService.getLookup(archetype, code);
    }

    /**
     * Adds a lookup to the cache.
     *
     * @param lookup the lookup to add
     * @throws ArchetypeServiceException for any error
     */
    public void add(Lookup lookup) {
        if (lookupService instanceof CachingLookupService) {
            ((CachingLookupService) lookupService).add(lookup);
        }
    }

    /**
     * Determines if a lookup exists.
     *
     * @param archetype the lookup archetype short name
     * @param code      the lookup code
     * @return <tt>true</tt> if the lookup exists
     * @throws ArchetypeServiceException for any error
     */
    public boolean exists(String archetype, String code) {
        return get(archetype, code) != null;
    }

    /**
     * Adds a lookup relationship to the cache.
     *
     * @param relationship the relationship to add
     * @throws ArchetypeServiceException for any error
     */
    public void add(LookupRelationship relationship) {
        String archetype = relationship.getArchetypeId().getShortName();
        List<LookupRelationship> relationships = getRelationships(archetype);
        relationships.add(relationship);
        if (lookupService instanceof CachingLookupService) {
            // need to update the cached lookups with the relationship.
            CachingLookupService cache = (CachingLookupService) lookupService;
            cache.add(relationship);
        }
    }

    /**
     * Determines if a lookup relationship exists.
     *
     * @param archetype the lookup relationship archetype short name
     * @param source    the source lookup reference
     * @param target    the target lookup reference
     * @return <tt>true</tt> if the relationship exists
     * @throws ArchetypeServiceException for any error
     */
    public boolean exists(String archetype, IMObjectReference source,
                          IMObjectReference target) {
        for (LookupRelationship relationship : getRelationships(archetype)) {
            if (ObjectUtils.equals(relationship.getSource(), source)
                    && ObjectUtils.equals(relationship.getTarget(), target)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns all instances of the given lookup relationship archetype.
     *
     * @param archetype the lookup relationship archetype short name
     * @return a list of relationships
     * @throws ArchetypeServiceException for any error
     */
    private List<LookupRelationship> getRelationships(String archetype) {
        List<LookupRelationship> relationships
                = relationshipsByArchetype.get(archetype);
        if (relationships == null) {
            relationships = new ArrayList<LookupRelationship>();
            ArchetypeQuery query = new ArchetypeQuery(archetype, false, true);
            query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
            IPage<IMObject> results = service.get(query);
            for (IMObject object : results.getResults()) {
                relationships.add((LookupRelationship) object);
            }
            relationshipsByArchetype.put(archetype, relationships);
        }
        return relationships;
    }
}
