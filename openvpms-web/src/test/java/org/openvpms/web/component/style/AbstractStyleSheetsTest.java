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
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;

import java.awt.Dimension;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * Base class for {@link StyleSheets} test cases.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractStyleSheetsTest {

    /**
     * The style sheets.
     */
    private StyleSheets styleSheets;

    /**
     * Tests the {@link org.openvpms.web.component.style.StyleSheets#getStyleSheet(int, int)} method.
     */
    @Test
    public void testGetStyleSheet() {
        StyleSheet styleSheet = styleSheets.getStyleSheet(640, 480);
        Style style = styleSheet.getStyle(Button.class, "button");
        Color background = (Color) style.getProperty("background");
        Color foreground = (Color) style.getProperty("foreground");
        Font font = (Font) style.getProperty("font");
        assertEquals(new Color(0x99, 0xcc, 0x66), background);
        assertEquals(new Color(0x00, 0x00, 0x00), foreground);
        assertEquals(new Extent(12, Extent.PX), font.getSize());
        assertEquals("arial,sans-serif", font.getTypeface().getName());
    }

    /**
     * Tests the {@link StyleSheets#getDefaultProperties()} method.
     */
    @Test
    public void testGetDefaultProperties() {
        Map<String, String> properties = styleSheets.getDefaultProperties();
        Map<String, String> expected = getDefaultProperties(true);
        assertEquals(expected, properties);
    }

    /**
     * Tests the {@link org.openvpms.web.component.style.StyleSheets#getProperties(int, int)} method.
     */
    @Test
    public void testGetProperties() {
        Map<String, String> properties = styleSheets.getProperties(640, 480);
        Map<String, String> expected = getDefaultProperties(false);
        expected.put("dialog.width", "320");
        expected.put("dialog.height", "240");
        expected.put("font.size", "12");
        assertEquals(expected, properties);
    }

    /**
     * Tests the {@link org.openvpms.web.component.style.StyleSheets#getProperties(int, int, boolean)} method where properties aren't evaluated.
     */
    @Test
    public void testGetPropertiesNoEval() {
        Map<String, String> properties = styleSheets.getProperties(640, 480, false);
        Map<String, String> expected = getDefaultProperties(true);
        expected.put("font.size", "12");
        assertEquals(expected, properties);
    }

    /**
     * Verifies that properties not specified for a particular resolution are inherited from the default.
     */
    @Test
    public void testPropertyInheritance() {
        // font.size not specified for 1024x768
        Map<String, String> properties = styleSheets.getProperties(1024, 768);
        assertEquals("15", properties.get("font.size"));
    }

    /**
     * Tests the {@link StyleSheets#getResolutions()} method.
     */
    @Test
    public void testGetResolutions() {
        Dimension[] sizes = styleSheets.getResolutions();
        assertEquals(2, sizes.length);
        assertEquals(new Dimension(640, 480), sizes[0]);
        assertEquals(new Dimension(1024, 768), sizes[1]);
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Before
    public void setUp() throws Exception {
        styleSheets = createStyleSheets();
    }

    /**
     * Returns the default properties. These should be exactly the same as those as specified in
     * <tt>valid.properties</tt>.
     *
     * @param quote if <tt>true</tt>, quote string values
     * @return the default properties
     */
    protected Map<String, String> getDefaultProperties(boolean quote) {
        Map<String, String> result = new HashMap<String, String>();
        result.put("background", quote("#99cc66", quote));
        result.put("foreground", quote("#000000", quote));
        result.put("border", quote("7b68ee", quote));
        result.put("font.typeface", quote("arial,sans-serif", quote));
        result.put("font.size", "15");
        result.put("dialog.width", "$width * 0.50");
        result.put("dialog.height", "$height div 2");
        return result;
    }

    /**
     * Helper to check the font size for a style named "button".
     *
     * @param styleSheet the style sheet
     * @param size       the expected size, in pixels
     */
    protected void checkButtonFontSize(StyleSheet styleSheet, int size) {
        Style style = styleSheet.getStyle(Button.class, "button");
        assertNotNull(style);
        Font font = (Font) style.getProperty("font");
        assertNotNull(font);
        assertEquals(new Extent(size, Extent.PX), font.getSize());
    }

    /**
     * Creates the style sheets.
     *
     * @return the style sheets
     * @throws java.io.IOException for any I/O error
     */
    protected abstract StyleSheets createStyleSheets() throws IOException;

    /**
     * Returns the style sheets.
     *
     * @return the style sheets.
     */
    protected StyleSheets getStyleSheets() {
        return styleSheets;
    }

    /**
     * Helper to conditionally quotes a string.
     *
     * @param value the value
     * @param quote if <tt>true<//tt> quote the value.
     * @return the value, quoted if necessary
     */
    private String quote(String value, boolean quote) {
        return quote ? "\"" + value + "\"" : value;
    }
}
