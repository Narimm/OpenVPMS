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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.util;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Extent;
import org.openvpms.web.echo.spring.SpringApplicationInstance;
import org.openvpms.web.echo.style.Style;
import org.openvpms.web.echo.style.UserStyleSheets;
import org.openvpms.web.system.ServiceHelper;

/**
 * Stylesheet helper methods.
 *
 * @author Tim Anderson
 */
public class StyleSheetHelper {

    /**
     * Returns the named property.
     *
     * @param name the property name
     * @return the property value, or {@code null} if the property doesn't exist
     */
    public static String getProperty(String name) {
        return getProperty(name, null);
    }

    /**
     * Returns the named property.
     *
     * @param name         the property name
     * @param defaultValue the default value, if the property doesn't exist
     * @return the property value, or {@code defaultValue} if the property doesn't exist
     */
    public static String getProperty(String name, String defaultValue) {
        String result = defaultValue;
        Style style = getStyle();
        if (style != null) {
            result = style.getProperty(name, defaultValue);
        }
        return result;
    }

    /**
     * Returns the named property.
     *
     * @param name         the property name
     * @param defaultValue the default value, if the property doesn't exist
     * @return the property value, or {@code defaultValue} if the property doesn't exist
     */
    public static int getProperty(String name, int defaultValue) {
        int result = defaultValue;
        Style style = getStyle();
        if (style != null) {
            result = style.getProperty(name, defaultValue);
        }
        return result;
    }

    /**
     * Returns the value of an extent property for a given component style.
     *
     * @param component the component class
     * @param styleName the style name
     * @param name      the property name
     * @return the extent, or {@code null} if the style doesn't exist
     */
    public static Extent getExtent(Class component, String styleName, String name) {
        Extent result = null;
        Style style = getStyle();
        if (style != null) {
            nextapp.echo2.app.Style s = style.getStylesheet().getStyle(component, styleName);
            if (s != null) {
                result = (Extent) s.getProperty(name);
            }
        }
        return result;
    }

    /**
     * Returns the current style.
     *
     * @return the current style, or {@code null} if there is no {@link SpringApplicationInstance} registered
     */
    protected static Style getStyle() {
        if (ApplicationInstance.getActive() instanceof SpringApplicationInstance) {
            UserStyleSheets styleSheets = ServiceHelper.getBean(UserStyleSheets.class);
            return styleSheets.getStyle();
        }
        return null;
    }

}
