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

package org.openvpms.archetype.util;

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
 * Macros are represented by the <em>lookup.macro</em> archetype,
 * with the expression node being an xpath expression.
 * E.g:
 * <code>
 * code="pm" name="Afternoon" expression="'in the afternoon'"
 * code="am" name="Morning" expression="'in the morning'"
 * </code>
 * Macros can refer to other macros. E.g:
 * <code>
 * code="sellingUnits" name="Selling units" expression="openvpms:get(., 'product.entity.sellingUnits','')"
 * code="oid" name="Once daily" expression="concat('Take ', $number, ' ', $sellingUnits, ' Once Daily')"
 * </code>
 * The <em>$number</em> variable is a special variable set when a macro is
 * prefixed with a number. E.g, given the macro:
 * <code>code="tid" name="Take twice daily" expression="concat('Take ', $number, ' tablets twice daily')"</code>
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
    private final MacroCache cache;

    /**
     * The variables.
     */
    private Map<String, Object> variables;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(MacroEvaluator.class);


    /**
     * Constructs a new <tt>MacroEvaluator</tt>.
     *
     * @param cache the macro cache
     */
    public MacroEvaluator(MacroCache cache) {
        this.cache = cache;
        variables = new HashMap<String, Object>();
    }

    /**
     * Declares a variable.
     *
     * @param name  the variable name
     * @param value the variable value
     */
    public void declareVariable(String name, Object value) {
        variables.put(name, value);
    }

    /**
     * Evaluates any macros in the supplied text.
     *
     * @param text    the text
     * @param context the macro context
     * @return the text with macros evaluated
     */
    public String evaluate(String text, Object context) {
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
            while (index < token.length() && isNumeric(token.charAt(index))) {
                ++index;
            }
            if (index != 0) {
                number = token.substring(0, index);
                newToken = token.substring(index);
            }
            String macro = cache.getExpression(newToken);
            if (macro != null) {
                try {
                    if (ctx == null) {
                        ctx = JXPathHelper.newContext(context);
                        ctx.setVariables(new MacroVariables(context, variables));
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
     * Determines if a character is numeric. This supports no.s in decimal
     * and fraction format.
     *
     * @param ch the character
     * @return <tt>true</tt> if <tt>ch</tt> is one of '0'..'9','.' or '/'
     */
    private boolean isNumeric(char ch) {
        return Character.isDigit(ch) || ch == '.' || ch == '/';
    }

    /**
     * Variables implementation that evaluates macros.
     */
    private class MacroVariables implements Variables {

        /**
         * The context to evaluate macro based variables with.
         */
        private Object context;

        /**
         * The variables.
         */
        private Map<String, Object> variables;


        public MacroVariables(Object context, Map<String, Object> variables) {
            this.context = context;
            this.variables = new HashMap<String, Object>(variables);
        }

        public void declareVariable(String name, Object value) {
            variables.put(name, value);
        }

        public Object getVariable(String name) {
            Object result;
            try {
                JXPathContext ctx = JXPathHelper.newContext(context);
                String macro = cache.getExpression(name);
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
            return cache.getExpression(name) != null || variables.containsKey(name);
        }

        public void undeclareVariable(String name) {
            variables.remove(name);
        }
    }

}
