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
        Map<String, String> result = new HashMap<String, String>();
        int fontSize = getFontSize(properties);

        JXPathContext context = JXPathContext.newContext(new Object());
        Variables variables = context.getVariables();
        variables.declareVariable("width", width);
        variables.declareVariable("height", height);
        if (fontSize != -1) {
            variables.declareVariable(FONT_SIZE, fontSize);
        }
        if (properties != null) {
            evaluate(properties, result, context, true);
        }
        evaluate(defaults, result, context, false);
        return result;
    }

    /**
     * Evalute properties.
     *
     * @param properties the properties to evaluate
     * @param result     the map to store the results
     * @param context    the xpath expression context used for evaluation
     * @param replace    if <tt>true</tt>, replace any existing property in <tt>result</tt> with the same name
     */
    private void evaluate(Map<String, String> properties, Map<String, String> result, JXPathContext context,
                          boolean replace) {
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String name = entry.getKey();
            if (replace || result.get(name) == null) {
                String value = entry.getValue();
                if (value != null) {
                    try {
                        Object eval = context.getValue(value, Object.class);
                        if (eval instanceof Number) {
                            eval = String.valueOf(((Number) eval).longValue());
                        } else if (eval != null && !(eval instanceof String)) {
                            eval = eval.toString();
                        }
                        result.put(name, (String) eval);
                    } catch (Throwable exception) {
                        throw new RuntimeException("Failed to evaluate: " + value, exception);
                    }
                }
            }
        }
    }

    /**
     * Returns the font size from the properties.
     *
     * @param properties the properties
     * @return the font size, in pixels, or <tt>-1</tt> if it is not specified or is invalid
     */
    private int getFontSize(Map<String, String> properties) {
        int result = -1;
        String size = (properties != null) ? properties.get(FONT_SIZE) : null;
        if (size == null) {
            size = defaults.get(FONT_SIZE);
        }
        if (size != null) {
            try {
                result = Integer.valueOf(size);
            } catch (NumberFormatException ignore) {
                // do nothing
            }
        }
        return result;
    }
}
