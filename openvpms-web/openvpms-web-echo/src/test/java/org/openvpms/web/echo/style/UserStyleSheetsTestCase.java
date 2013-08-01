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

package org.openvpms.web.echo.style;

import nextapp.echo2.app.StyleSheet;
import org.junit.Test;

import java.awt.Dimension;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;


/**
 * Tests {@link UserStyleSheets}.
 *
 * @author Tim Anderson
 */
public class UserStyleSheetsTestCase extends AbstractStyleSheetsTest {

    /**
     * Checks the behaviour of overriding the default properties via {@link UserStyleSheets#setDefaultProperties}.
     */
    @Test
    public void testOverrideDefaults() {
        UserStyleSheets styleSheets = getStyleSheets();
        StyleSheet styleSheet = styleSheets.getStyleSheet(1024, 768);
        checkButtonFontSize(styleSheet, 15);

        Map<String, String> expected = getDefaultProperties(true);
        expected.put("font.size", "14");
        styleSheets.setDefaultProperties(expected);

        // verify the font size has updated
        StyleSheet styleSheet2 = styleSheets.getStyleSheet(1024, 768);
        checkButtonFontSize(styleSheet2, 14);

        // now revert the changes, and verify they have reverted
        styleSheets.reset();
        StyleSheet styleSheet3 = styleSheets.getStyleSheet(1024, 768);
        checkButtonFontSize(styleSheet3, 15);
    }

    /**
     * Tests adding custom properties for a resolution.
     */
    @Test
    public void testAddResolution() {
        UserStyleSheets styleSheets = getStyleSheets();

        // check the font size for 480x320. Should be 12px as that is what is configured for 640x480
        StyleSheet styleSheet1 = styleSheets.getStyleSheet(480, 320);
        checkButtonFontSize(styleSheet1, 12);

        // now add override properties for 480x320
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("font.size", "9");
        styleSheets.setProperties(properties, 480, 320);

        // check the new font size for 480x320
        StyleSheet styleSheet2 = styleSheets.getStyleSheet(480, 320);
        checkButtonFontSize(styleSheet2, 9);

        // check the available custom resolutions
        Dimension[] sizes = styleSheets.getResolutions();
        assertEquals(3, sizes.length);
        assertEquals(new Dimension(480, 320), sizes[0]);
        assertEquals(new Dimension(640, 480), sizes[1]);
        assertEquals(new Dimension(1024, 768), sizes[2]);

        // now revert the changes, and verify they have reverted
        styleSheets.reset();
        StyleSheet styleSheet3 = styleSheets.getStyleSheet(480, 320);
        checkButtonFontSize(styleSheet3, 12);

        Dimension[] sizes2 = styleSheets.getResolutions();
        assertEquals(2, sizes2.length);
        assertEquals(new Dimension(640, 480), sizes2[0]);
        assertEquals(new Dimension(1024, 768), sizes2[1]);
    }

    /**
     * Creates the style sheets.
     *
     * @return the style sheets
     * @throws java.io.IOException for any I/O error
     */
    protected StyleSheets createStyleSheets() throws IOException {
        StyleSheetCache cache = new StyleSheetCache("org/openvpms/web/echo/style/valid");
        return new UserStyleSheets(cache);
    }

    /**
     * Returns the style sheets.
     *
     * @return the style sheets.
     */
    @Override
    protected UserStyleSheets getStyleSheets() {
        return (UserStyleSheets) super.getStyleSheets();
    }
}