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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.util;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.helper.LookupHelper;
import org.openvpms.component.business.service.archetype.helper.LookupHelperException;
import org.openvpms.web.system.ServiceHelper;

import java.util.Map;


/**
 * Lookup name helper.
 *
 * @author Tim Anderson
 */
public class LookupNameHelper {

    /**
     * Returns a list of lookups for the specified archetype short name and node name.
     *
     * @param shortName the archetype short name
     * @param node      the node name
     * @return a map of lookup codes to lookup names
     * @throws ArchetypeServiceException for any archetype service error
     * @throws LookupHelperException     if the lookup is incorrectly specified
     */
    public static Map<String, String> getLookupNames(String shortName, String node) {
        return LookupHelper.getNames(ArchetypeServiceHelper.getArchetypeService(), shortName, node);
    }

    /**
     * Returns the name of a lookup given the node containing its code.
     *
     * @param object the object
     * @param node   the node name
     * @return the corresponding lookup name, or {@code null} if none is found
     */
    public static String getName(IMObject object, String node) {
        return ServiceHelper.getLookupService().getName(object, node);
    }

    /**
     * Returns the name of a lookup given the archetype short name and code.
     *
     * @param shortName    the lookup archetype short name
     * @param code         the lookup code
     * @param defaultValue the default value if the lookup doesn't exist or its name is {@code null}.
     *                     May be {@code null}
     * @return the corresponding lookup name, or {@code defaultValue} if none is found or is {@code null}
     */
    public static String getName(String shortName, String code, String defaultValue) {
        Lookup lookup = ServiceHelper.getLookupService().getLookup(shortName, code);
        String name = (lookup != null) ? lookup.getName() : defaultValue;
        return (name != null) ? name : defaultValue;
    }

}
