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
import org.apache.commons.jxpath.Variables;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.system.common.jxpath.JXPathHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;


/**
 * Macro evaluator. Evaluates macros embedded in text.
 * Macros are xpath expressions, loaded from one or more property files named
 * <em>Macros.properties</em> located in the classpath. E.g:
 * <code>
 * pm = 'in the afternoon'
 * am = 'in the morning'
 * </code>
 * Macros can refer to other macros. E.g:
 * <code>
 * sellingUnits = openvpms:get(., 'product.entity.sellingUnits',"")
 * oid = concat('Take ', $number, ' ', $sellingUnits, ' Once Daily')
 * </code>
 * The <em>$number</em> variable is a special variable set when a macro is
 * prefixed with a number. E.g, given the macro:
 * <code>tid = concat('Take ', $number, ' tablets twice daily')</code>
 * The evaluation of the macro '<em>3tid'</em> would evaluate to:
 * <em>Take 3 tablets twice daily</em>
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
        JXPathContext ctx = null;
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            String newToken = token;
            String number = "";
            // If token starts with numbers strip numbers and create number
            // variable. If any left pass token to test for macro.
            int index = 0;
            while (index < token.length()
                    && Character.isDigit(token.charAt(index))) {
                ++index;
            }
            if (index != 0) {
                number = token.substring(0, index);
                newToken = token.substring(index);
            }
            String macro = macros.get(newToken);
            if (macro != null) {
                try {
                    if (ctx == null) {
                        ctx = JXPathHelper.newContext(context);
                        ctx.setVariables(new MacroVariables(context));
                    }
                    ctx.getVariables().declareVariable("number", number);
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

    /**
     * Variables implementation that evaluates macros.
     */
    private static class MacroVariables implements Variables {

        /**
         * The context to evaluate macro based variables with.
         */
        private Object context;

        /**
         * The variables.
         */
        private Map<String, Object> variables;


        public MacroVariables(Object context) {
            this.context = context;
            variables = new HashMap<String, Object>();
        }

        public void declareVariable(String name, Object value) {
            variables.put(name, value);
        }

        public Object getVariable(String name) {
            Object result;
            try {
                JXPathContext ctx = JXPathHelper.newContext(context);
                String macro = macros.get(name);
                if (macro != null) {
                    result = ctx.getValue(macro);
                } else {
                    result = variables.get(name);
                }

            } catch (Throwable exception) {
                result = null;
                log.debug(exception);
            }

            return result;
        }

        public boolean isDeclaredVariable(String name) {
            if (macros.containsKey(name)) {
                return true;
            }
            return (variables.containsKey(name));
        }

        public void undeclareVariable(String name) {
            variables.remove(name);
        }

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
