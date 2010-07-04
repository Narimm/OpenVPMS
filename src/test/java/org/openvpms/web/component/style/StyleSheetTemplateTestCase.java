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

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Font;
import nextapp.echo2.app.Style;
import nextapp.echo2.app.StyleSheet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


/**
 * Tests the {@link StyleSheetTemplate}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class StyleSheetTemplateTestCase {

    /**
     * Background colour.
     */
    private static final String BACKGROUND = "#99cc66";

    /**
     * Foreground colour.
     */
    private static final String FOREGROUND = "#000000";

    /**
     * Border colour.
     */
    private static final String BORDER = "7b68ee";

    /**
     * Font typeface.
     */
    private static final String TYPEFACE = "arial,sans-serif";

    /**
     * Font size.
     */
    private static final String FONT_SIZE = "15";

    /**
     * Valid style sheet resource path.
     */
    private static final String VALID_STYLESHEET = "org/openvpms/web/component/style/valid.stylesheet";


    /**
     * Reads a stylesheet and verifies that the expected properties are evaluated.
     *
     * @throws Exception for any error
     */
    @Test
    public void testExpandProperties() throws Exception {
        Map<String, String> properties = getProperties();
        InputStream stream = getStyleSheet(VALID_STYLESHEET);
        StyleSheetTemplate template = new StyleSheetTemplate(stream);
        StyleSheet styleSheet = template.getStyleSheet(properties);
        Style style = styleSheet.getStyle(Button.class, "button");
        Color background = (Color) style.getProperty("background");
        Color foreground = (Color) style.getProperty("foreground");
        Font font = (Font) style.getProperty("font");
        assertEquals(new Color(0x99, 0xcc, 0x66), background);
        assertEquals(new Color(0x00, 0x00, 0x00), foreground);
        assertEquals(new Extent(15, Extent.PX), font.getSize());
        assertEquals(TYPEFACE, font.getTypeface().getName());
    }

    /**
     * Reads a stylesheet and verifies that an {@link StyleSheetException} is thrown if a property doesn't exist.
     *
     * @throws Exception for any error
     */
    @Test
    public void testUndefinedProperty() throws Exception {
        Map<String, String> properties = getProperties();
        properties.remove("font.size");
        try {
            InputStream stream = getStyleSheet(VALID_STYLESHEET);
            StyleSheetTemplate template = new StyleSheetTemplate(stream);
            template.getStyleSheet(properties);
            fail("Expected StyleSheetException to be thrown for missing property");
        } catch (StyleSheetException expected) {
            assertEquals(StyleSheetException.ErrorCode.UndefinedProperty, expected.getErrorCode());
        }
    }

    /**
     * Reads a stylesheet and verifies that an {@link StyleSheetException} is thrown if a property doesn't have a
     * closing "}"
     *
     * @throws Exception for any error
     */
    @Test
    public void testUnterminatedProperty() throws Exception {
        try {
            InputStream stream = getStyleSheet("org/openvpms/web/component/style/unterminatedproperty.stylesheet");
            StyleSheetTemplate template = new StyleSheetTemplate(stream);
            Map<String, String> properties = new HashMap<String, String>();
            template.getStyleSheet(properties);
            fail("Expected StyleSheetException to be thrown for unterminated property");
        } catch (StyleSheetException expected) {
            assertEquals(StyleSheetException.ErrorCode.UnterminatedProperty, expected.getErrorCode());
        }
    }

    /**
     * Returns properties expected by valid.stylesheet.
     *
     * @return the properties
     */
    private Map<String, String> getProperties() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("background", BACKGROUND);
        properties.put("foreground", FOREGROUND);
        properties.put("border", BORDER);
        properties.put("font.size", FONT_SIZE);
        properties.put("font.typeface", TYPEFACE);
        return properties;
    }

    /**
     * Returns the style sheet template as a stream.
     *
     * @param path the style sheet resource path
     * @return the style sheet stream
     */
    private InputStream getStyleSheet(String path) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    }

}
