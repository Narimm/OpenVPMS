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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.component.style;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Variables;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * An evaluator of properties used by a <tt>StyleSheet</tt>.
 * <p/>
 * Properties values are specified as simple jxpath expressions which may reference the following variables:
 * <ul>
 * <li>$width - the screen width
 * <li>$height - the screen height
 * <li>$font.sise - the default font size, in pixels
 * </ul>
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class StylePropertyEvaluator {

    /**
     * The default property values.
     */
    private Map<String, String> defaults;

    /**
     * The font size property.
     */
    private static final String FONT_SIZE = "font.size";

    /**
     * The H4 font size property.
     */
    private static final String FONT_H4_SIZE = "font.h4.size";

    /**
     * Large padding property.
     */
    private static final String PADDING_LARGE = "padding.large";

    /**
     * Medium padding property.
     */
    private static final String PADDING_MEDIUM = "padding.medium";

    /**
     * Small padding property.
     */
    private static final String PADDING_SMALL = "padding.small";

    /**
     * Smaller padding property.
     */
    private static final String PADDING_SMALLER = "padding.smaller";

    /**
     * Tiny padding property.
     */
    private static final String PADDING_TINY = "padding.tiny";


    /**
     * Constructs a <tt>StylePropertyEvaluator</tt>.
     *
     * @param defaults the default property values
     */
    public StylePropertyEvaluator(Map<String, String> defaults) {
        setDefaultProperties(defaults);
    }

    /**
     * Returns the default properties.
     *
     * @return the default properties. This map is read-only
     */
    public Map<String, String> getDefaultProperties() {
        return defaults;
    }

    /**
     * Sets the default properties.
     *
     * @param properties the default properties
     */
    public void setDefaultProperties(Map<String, String> properties) {
        defaults = Collections.unmodifiableMap(properties);
    }

    /**
     * Returns properties for the specified screen resolution.
     *
     * @param width  the screen width
     * @param height the screen height
     * @return properties for the screen resolution
     */
    public Map<String, String> getProperties(int width, int height) {
        return getProperties(width, height, null);
    }

    /**
     * Returns properties for the specified screen resolution.
     * <p/>
     * The <tt>width</tt> and <tt>height</tt> are used to declare the <em>$width</em> and <em>$height</em> variables
     * respectively.
     * The <em>$font.size</em> variable is obtained from <tt>properties</tt> if set, or the default properties if not.
     *
     * @param width      the screen width
     * @param height     the screen height
     * @param properties properties to override the defaults. May be <tt>null</tt>
     * @return properties for the screen resolution
     */
    public Map<String, String> getProperties(int width, int height, Map<String, String> properties) {
        Map<String, String> result = new HashMap<String, String>(defaults);
        if (properties != null) {
            result.putAll(properties);
        }
        JXPathContext context = JXPathContext.newContext(new Object());
        Variables variables = context.getVariables();
        variables.declareVariable("width", width);
        variables.declareVariable("height", height);
        evaluateAndDeclare(FONT_SIZE, result, context);
        evaluateAndDeclare(FONT_H4_SIZE, result, context);
        evaluateAndDeclare(PADDING_LARGE, result, context);
        evaluateAndDeclare(PADDING_MEDIUM, result, context);
        evaluateAndDeclare(PADDING_SMALL, result, context);
        evaluateAndDeclare(PADDING_SMALLER, result, context);
        evaluateAndDeclare(PADDING_TINY, result, context);

        evaluate(result, context);
        return result;
    }

    /**
     * Evaluates properties.
     *
     * @param properties the properties to evaluate
     * @param context    the xpath expression context used for evaluation
     */
    private void evaluate(Map<String, String> properties, JXPathContext context) {
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String name = entry.getKey();
            String expression = entry.getValue();
            if (expression != null) {
                String value = evaluate(expression, context);
                properties.put(name, value);
            }
        }
    }

    /**
     * Evaluates an expression.
     *
     * @param context    the jxpath context
     * @param expression the xpath expression
     * @return the value of the expression
     */
    private String evaluate(String expression, JXPathContext context) {
        String result = null;
        try {
            Object value = context.getValue(expression, Object.class);
            if (value instanceof Number) {
                result = String.valueOf(round((Number) value));
            } else if (value != null) {
                result = value.toString();
            }
        } catch (Throwable exception) {
            throw new StyleSheetException(StyleSheetException.ErrorCode.InvalidExpression, exception, expression);
        }
        return result;
    }

    /**
     * Evaluates an expression, declaring the value as a variable in the context, and replacing the original expression
     * with it.
     *
     * @param name       the property name
     * @param properties the properties to get the property expression from, and replace its value with
     * @param context    the context to evaluate the expression
     */
    private void evaluateAndDeclare(String name, Map<String, String> properties, JXPathContext context) {
        String expression = properties.get(name);
        if (expression != null) {
            String value = evaluate(expression, context);
            properties.put(name, value);
            context.getVariables().declareVariable(name, value);
        }
    }

    /**
     * Rounds a numeric value to a long.
     *
     * @param value the value to round
     * @return the rounded value
     */
    private long round(Number value) {
        if (value instanceof Integer || value instanceof Long) {
            return value.longValue();
        }
        BigDecimal result = (value instanceof BigDecimal) ? (BigDecimal) value : new BigDecimal(value.doubleValue());
        return result.setScale(0, RoundingMode.HALF_EVEN).longValue();
    }

}
