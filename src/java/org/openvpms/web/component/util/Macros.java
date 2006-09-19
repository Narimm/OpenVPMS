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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.util;

import java.util.HashMap;
import java.util.Map;


/**
 * Manages a set of macros, loaded from a properties file.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class Macros {

    /**
     * The macros.
     */
    private static Map<String, String> macros;


    /**
     * Returns the named macro.
     *
     * @param name the macro name
     * @return the named macro, or <code>null</code> if it doesn't exist
     */
    public static String getMacro(String name) {
        return macros.get(name);
    }


    private static class Parser extends PropertiesParser {

        /**
         * Parse a property file entry.
         *
         * @param key   the property key
         * @param value the property value
         */
        protected void parse(String key, String value) {
            macros.put(key, value);
        }
    }

    static {
        macros = new HashMap<String, String>();
        Parser parser = new Parser();
        parser.parse("Macros.properties");
    }
}
