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

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.system.common.jxpath.JXPathHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;


/**
 * Macro evaluator. Evaluates macros embedded in text.
 * Macros are xpath expressions,  loaded from one or more property files named
 * <em>Macros.properties</em> located in the classpath. E.g:
 * <code>@macro1 = 'simple text'</code>
 * <code>@bid = concat(openvpms:get(., 'product.entity.sellingUnits'), ' twice a day')</code>
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class MacroEvaluator {

    /**
     * The macros.
     */
    private static Map<String, String> macros;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(MacroEvaluator.class);


    /**
     * Evaluates any macros in the supplied text.
     *
     * @param text    the text
     * @param context the macro context
     * @return the text with macros evaluated
     */
    public static String evaluate(String text, Object context) {
        StringTokenizer tokens = new StringTokenizer(text, " \t\n\r", true);
        StringBuffer result = new StringBuffer();
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            String macro = macros.get(token);
            if (macro != null) {
                try {
                    JXPathContext ctx = JXPathHelper.newContext(context);
                    Object value = ctx.getValue(macro);
                    if (value != null) {
                        result.append(value);
                    }
                } catch (Throwable exception) {
                    result.append(token);
                    log.debug(exception);
                }
            } else {
                result.append(token);
            }
        }
        return result.toString();
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
