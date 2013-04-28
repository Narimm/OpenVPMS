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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.macro.impl;

import org.apache.commons.jxpath.JXPathContext;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.macro.Variables;

import java.util.HashMap;
import java.util.Map;


/**
 * Evaluates expression macros using JXPath.
 *
 * @author Tim Anderson
 */
class ExpressionMacroRunner extends MacroRunner {

    /**
     * The JXPath context used to evaluate expressions.
     */
    private JXPathContext jxPathContext;

    /**
     * Object to evaluate macros against, if none is supplied.
     */
    private static final Object DUMMY = new Object();


    /**
     * Constructs an {@link ExpressionMacroRunner}.
     *
     * @param context the macro context
     */
    public ExpressionMacroRunner(MacroContext context) {
        super(context);
        Object object = getObject();
        if (object == null) {
            object = DUMMY;
        }
        jxPathContext = JXPathHelper.newContext(object);
        jxPathContext.setVariables(new MacroVariables(context.getVariables()));
    }

    /**
     * Runs a macro.
     *
     * @param macro  the macro to run
     * @param number a numeric expression, used to declare the <em>$number</em> variable. May be empty or {@code null}
     * @return the result of the macro
     */
    @Override
    public String run(Macro macro, String number) {
        String result = null;
        ExpressionMacro m = (ExpressionMacro) macro;
        String expression = m.getExpression();
        if (expression != null) {
            jxPathContext.getVariables().declareVariable("number", number);
            Object value = jxPathContext.getValue(expression);
            result = (value != null) ? value.toString() : null;
        }
        return result;
    }

    /**
     * Variables implementation that evaluates macros.
     */
    private class MacroVariables implements org.apache.commons.jxpath.Variables {

        /**
         * User variables. May be {@code null}
         */
        private final Variables variables;

        /**
         * Internal variables.
         */
        private Map<String, Object> declared = new HashMap<String, Object>();

        /**
         * Constructs an {@link MacroVariables}.
         *
         * @param variables the user variables
         */
        public MacroVariables(Variables variables) {
            this.variables = variables;
        }

        /**
         * Declares a variable.
         * <p/>
         * These have lower precedence than user variables supplied at construction.
         *
         * @param name  the variable name
         * @param value the variable value
         */
        public void declareVariable(String name, Object value) {
            declared.put(name, value);
        }

        /**
         * Returns a variable value.
         *
         * @param name the variable name
         * @return the variable value. May be {@code null}
         */
        public Object getVariable(String name) {
            Object result;
            MacroContext context = getContext();
            if (variables != null && variables.exists(name)) {
                result = variables.get(name);
            } else if (context.exists(name)) {
                // the variable is macro. Evaluate it.
                result = context.run(name, "");
            } else {
                result = declared.get(name);
            }
            return result;
        }

        /**
         * Determines if a variable is declared.
         *
         * @param name the variable name
         * @return {@code true} if the variable is declared
         */
        public boolean isDeclaredVariable(String name) {
            return getContext().exists(name) || declared.containsKey(name)
                   || (variables != null && variables.exists(name));
        }

        /**
         * Undeclares a variable.
         * <p/>
         * This implementation is a no-op.
         *
         * @param name the variable name.
         */
        public void undeclareVariable(String name) {
            // no-op
        }
    }


}
