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

package org.openvpms.archetype.function.lookup;

import org.openvpms.component.business.domain.im.lookup.Lookup;

/**
 * JXPath extension functions that operate on {@link Lookup} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */

public class LookupFunctions {

    /**
     * Returns a string if the lookup is the default.
     *
     * @param context the expression context. Expected to reference a party.
     * @return the string or
     *         <code>null</code>
     */
    public static String isDefault(Boolean defaultLookup) {
        StringBuffer result = new StringBuffer();
        if (defaultLookup) {
            result.append(" (Default)");
        } else {
            result.append("");
        }
        return result.toString();
    }
}
