package org.openvpms.macro.impl;

import org.apache.commons.jxpath.JXPathContext;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.macro.MacroException;
import org.openvpms.macro.Variables;

import java.util.HashMap;
import java.util.Map;

/**
 * An {@link MacroRunner} that evaluates a JXPath expression.
 *
 * @author Tim Anderson
 */
public abstract class AbstractExpressionMacroRunner extends MacroRunner {

    /**
     * Object to evaluate macros against, if none is supplied.
     */
    private static final Object DUMMY = new Object();

    /**
     * The JXPath context used to evaluate expressions.
     */
    private final JXPathContext jxPathContext;

    /**
     * Constructs an {@link AbstractExpressionMacroRunner}.
     *
     * @param context the macro context
     */
    public AbstractExpressionMacroRunner(MacroContext context) {
        super(context);
        Object object = getObject();
        if (object == null) {
            object = DUMMY;
        }
        jxPathContext = JXPathHelper.newContext(object);
        jxPathContext.setVariables(new MacroVariables(context.getVariables()));
    }

    /**
     * Evaluates the macro expression.
     *
     * @param macro  the macro to evaluate
     * @param number a numeric expression, used to declare the <em>$number</em> variable. May be empty or {@code null}
     * @return the result of the expression. May be {@code null}
     * @throws MacroException for any macro error
     */
    protected Object evaluate(AbstractExpressionMacro macro, String number) {
        Object result;
        try {
            jxPathContext.getVariables().declareVariable("number", number);
            result = jxPathContext.getValue(macro.getExpression());
        } catch (MacroException exception) {
            throw exception;
        } catch (Throwable exception) {
            throw new MacroException("Failed to evaluate expression='" + macro.getExpression()
                                     + "' for macro=" + macro.getCode(), exception);
        }
        return result;
    }

    /**
     * Variables implementation that evaluates macros.
     */
    protected class MacroVariables implements org.apache.commons.jxpath.Variables {

        /**
         * User variables. May be {@code null}
         */
        private final Variables variables;

        /**
         * Internal variables.
         */
        private Map<String, Object> declared = new HashMap<String, Object>();

        /**
         * Constructs an {@link org.openvpms.macro.impl.ExpressionMacroRunner.MacroVariables}.
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
