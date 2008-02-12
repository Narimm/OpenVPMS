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
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.service.archetype.IArchetypeService;


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
     * @param service the archetype service
     * @param shortName the lookup short name
     * @param code    the lookup code
     * @param name    the lookup name
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
     * Helper to create and add a relationship between two lookups.
     *
     * @param service the archetype service
     * @param shortName the lookup relationship short name
     * @param source the source lookup
     * @param target the target lookup
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
}
