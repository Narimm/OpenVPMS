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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.mail;

import org.openvpms.component.system.common.util.Variables;
import org.openvpms.macro.Macros;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.property.StringPropertyTransformer;
import org.openvpms.web.resource.i18n.Messages;

/**
 * Mail helper methods.
 *
 * @author Tim Anderson
 */
class MailHelper {

    /**
     * Helper to create a mandatory property.
     *
     * @param name the property name
     * @param key  the message resource bundle key
     * @param trim if {@code true} trim the string of leading and trailing spaces, new lines
     * @return a new property
     */
    static SimpleProperty createProperty(String name, String key, boolean trim) {
        return createProperty(name, key, trim, null, null);
    }

    /**
     * Helper to create a mandatory property.
     *
     * @param name      the property name
     * @param key       the message resource bundle key
     * @param trim      if {@code true} trim the string of leading and trailing spaces, new lines
     * @param macros    if non-null enable macro expansion
     * @param variables variables to be used in macro expansion. May be {@code null}
     * @return a new property
     */
    static SimpleProperty createProperty(String name, String key, boolean trim, Macros macros,
                                         Variables variables) {
        SimpleProperty result = new SimpleProperty(name, String.class);
        result.setDisplayName(Messages.get(key));
        result.setRequired(true);
        StringPropertyTransformer transformer = new StringPropertyTransformer(result, trim, macros, null, variables);
        result.setTransformer(transformer);
        return result;
    }
}
