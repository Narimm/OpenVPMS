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

package org.openvpms.web.echo.style;

import nextapp.echo2.app.StyleSheet;

import java.awt.Dimension;
import java.util.Map;


/**
 * Encapsulates a {@code Stylesheet} and the properties used to generate it.
 *
 * @author Tim Anderson
 */
public class Style {

    /**
     * The stylesheet.
     */
    private final StyleSheet stylesheet;

    /**
     * The screen dimensions.
     */
    private final Dimension size;

    /**
     * The properties used in the stylesheet.
     */
    private Map<String, String> properties;

    /**
     * Constructs a {@link Style}.
     *
     * @param stylesheet the stylesheet
     * @param size       the screen dimensions
     * @param properties the properties used to populate the stylesheet
     */
    public Style(StyleSheet stylesheet, Dimension size, Map<String, String> properties) {
        this.stylesheet = stylesheet;
        this.size = size;
        this.properties = properties;
    }

    /**
     * Returns the stylesheet.
     *
     * @return the stylesheet
     */
    public StyleSheet getStylesheet() {
        return stylesheet;
    }

    /**
     * Returns the screen dimensions.
     *
     * @return the screen dimensions
     */
    public Dimension getSize() {
        return size;
    }

    /**
     * Returns the properties used to populate the stylesheet.
     *
     * @return the properties
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * Returns the named property.
     *
     * @param name the property name
     * @return the property value. May be {@code null}
     */
    public String getProperty(String name) {
        return properties.get(name);
    }

    /**
     * Returns the named property, as an int.
     *
     * @param name         the property name
     * @param defaultValue the default value, if the property doesn't exist or is invalid
     * @return the property value, or {@code defaultValue} if the property doesn't exist or is invalid
     */
    public int getProperty(String name, int defaultValue) {
        int result = defaultValue;
        String value = properties.get(name);
        if (value != null) {
            try {
                result = Integer.valueOf(value);
            } catch (NumberFormatException ignore) {
                // do nothing
            }
        }
        return result;
    }
}
