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

package org.openvpms.component.business.dao.hibernate.im.lookup;

import org.openvpms.component.business.domain.archetype.ArchetypeId;

/**
 * {@link LookupDOImpl} helper methods for test purposes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LookupDOHelper {

    /**
     * Helper to create a new lookup, with a unique code.
     *
     * @param shortName the lookup short name
     * @param code      the lookup code
     */
    public static LookupDO createLookup(String shortName, String code) {
        ArchetypeId id = new ArchetypeId(shortName + ".1.0");
        code = code + "-" + System.currentTimeMillis();
        LookupDO result = new LookupDOImpl();
        result.setArchetypeId(id);
        result.setCode(code);
        return result;
    }

    /**
     * Helper to create a new lookup, with a unique code.
     *
     * @param shortName the lookup short name
     * @param code      the lookup code
     * @param name      the lookup name. May be <tt>null</tt>
     */
    public static LookupDO createLookup(String shortName, String code,
                                        String name) {
        LookupDO result = createLookup(shortName, code);
        result.setName(name);
        return result;
    }

}
