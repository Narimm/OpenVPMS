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

package org.openvpms.component.business.service.lookup;

import junit.framework.Assert;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;

import java.util.Arrays;
import java.util.List;


/**
 * Lookup helper methods for test purposes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LookupUtil extends Assert {

    /**
     * Helper to create a new lookup, with a unique code.
     *
     * @param shortName the lookup short name
     * @param code      the lookup code
     */
    public static Lookup createLookup(String shortName, String code) {
        ArchetypeId id = new ArchetypeId(shortName + ".1.0");
        code = code + "-" + System.currentTimeMillis();
        return new Lookup(id, code);
    }

    /**
     * Helper to create a new lookup, with a unique code.
     *
     * @param shortName the lookup short name
     * @param code      the lookup code
     * @param name      the lookup name. May be <tt>null</tt>
     */
    public static Lookup createLookup(String shortName, String code,
                                      String name) {
        ArchetypeId id = new ArchetypeId(shortName + ".1.0");
        code = code + "-" + System.currentTimeMillis();
        return new Lookup(id, code, name);
    }

    /**
     * Helper to create a new lookup via the archetype service, with a unique
     * code.
     *
     * @param service   the archetype service
     * @param shortName the lookup short name
     * @param code      the lookup code
     * @return a new lookup
     */
    public static Lookup createLookup(IArchetypeService service,
                                      String shortName, String code) {
        Lookup lookup = (Lookup) service.create(shortName);
        assertNotNull(lookup);
        code = code + "-" + System.currentTimeMillis();
        lookup.setCode(code);
        return lookup;
    }

    /**
     * Helper to create a new lookup via the archetype service, with a unique
     * code.
     *
     * @param service   the archetype service
     * @param shortName the lookup short name
     * @param code      the lookup code
     * @param name      the lookup name
     * @return a new lookup
     */
    public static Lookup createLookup(IArchetypeService service,
                                      String shortName, String code,
                                      String name) {
        Lookup lookup = createLookup(service, shortName, code);
        lookup.setName(name);
        return lookup;
    }

    /**
     * Retuurns the lookup with the specified code, creating and saving it if
     * it doesn't exist.
     *
     * @param service   the archetype service
     * @param shortName the lookup short name
     * @param code      the lookup code
     */
    public static Lookup getLookup(IArchetypeService service, String shortName,
                                   String code) {
        Lookup lookup;
        ArchetypeQuery query = new ArchetypeQuery(shortName, false, false);
        List<IMObject> lookups = service.get(query).getResults();
        if (lookups.isEmpty()) {
            lookup = createLookup(service, shortName, code);
            lookup.setCode(code); // use the supplied code
            service.save(lookup);
        } else {
            lookup = (Lookup) lookups.get(0);
        }
        return lookup;
    }

    /**
     * Returns a lookup that is the target in a lookup relationship, creating
     * and saving it if it doesn't exist.
     *
     * @param service               the archetype service
     * @param shortName             the target lookup short name
     * @param code                  the lookup code
     * @param source                the source lookup
     * @param relationshipShortName the lookup relationship short name
     */
    public static Lookup getLookup(IArchetypeService service, String shortName,
                                   String code, Lookup source,
                                   String relationshipShortName) {
        Lookup target = getLookup(service, shortName, code);
        for (LookupRelationship relationship
                : source.getLookupRelationships()) {
            if (relationship.getTarget().equals(target.getObjectReference())) {
                return target;
            }
        }
        LookupRelationship relationship
                = (LookupRelationship) service.create(relationshipShortName);
        relationship.setSource(source.getObjectReference());
        relationship.setTarget(target.getObjectReference());
        source.addLookupRelationship(relationship);
        target.addLookupRelationship(relationship);
        service.save(Arrays.asList(source, target));
        return target;
    }


    /**
     * Helper to create and add a relationship between two lookups.
     *
     * @param service   the archetype service
     * @param shortName the lookup relationship short name
     * @param source    the source lookup
     * @param target    the target lookup
     * @return a new lookup relationship
     */
    public static LookupRelationship addRelationship(
            IArchetypeService service, String shortName, Lookup source,
            Lookup target) {
        LookupRelationship relationship
                = (LookupRelationship) service.create(shortName);
        assertNotNull(relationship);
        relationship.setSource(source.getObjectReference());
        relationship.setTarget(target.getObjectReference());
        source.addLookupRelationship(relationship);
        target.addLookupRelationship(relationship);
        return relationship;
    }

    /**
     * Removes all lookups with the specified short name.
     *
     * @param service   the archetype service
     * @param shortName the short name
     */
    public static void removeAll(IArchetypeService service, String shortName) {
        ArchetypeQuery query = new ArchetypeQuery(shortName, false, false);
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        List<IMObject> lookups = service.get(query).getResults();
        for (IMObject lookup : lookups) {
            service.remove(lookup);
        }
    }

}
