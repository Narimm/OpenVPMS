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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.smartflow.event.impl;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;

/**
 * Smart Flow Sheet helper methods.
 *
 * @author Tim Anderson
 */
public class SmartFlowSheetHelper {

    /**
     * Returns an object given its identifier.
     *
     * @param identifier the identifier. May be {@code null}
     * @param archetype  the archetype
     * @param service    the archetype service
     * @return the corresponding object, or {@code null} if none is found
     */
    public static IMObject getObject(String archetype, String identifier, IArchetypeService service) {
        IMObject result = null;
        long id = getId(identifier);
        if (id != -1) {
            IMObjectReference reference = new IMObjectReference(archetype, Long.valueOf(identifier));
            result = service.get(reference);
        }
        return result;
    }

    /**
     * Helper to parse an id from a string.
     *
     * @param value the value to parse
     * @return the id, or {@code -1} if one doesn't exist or can't be parsed
     */
    public static long getId(String value) {
        long id = -1;
        if (!StringUtils.isEmpty(value)) {
            try {
                id = Long.valueOf(value);
            } catch (NumberFormatException ignore) {
                // do nothing
            }
        }
        return id;
    }

}
