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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;


/**
 * Tests the {@link StylePropertyEvaluator} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class StylePropertyEvaluatorTestCase {

    /**
     * Verifies that that expressions are evaluated.
     */
    @Test
    public void testEvaluate() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("dialog.width", "$width * 0.50");
        properties.put("dialog.height", "$height * 0.50");
        properties.put("font.size", "15");
        properties.put("padding.large", "($font.size div 3) * 4");
        properties.put("separator.position", "$font.size + $padding.large");
        StylePropertyEvaluator template = new StylePropertyEvaluator(properties);
        Map<String, String> eval = template.getProperties(1024, 768);
        assertEquals("512", eval.get("dialog.width"));
        assertEquals("384", eval.get("dialog.height"));
        assertEquals("20", eval.get("padding.large"));
        assertEquals("15", eval.get("font.size"));
        assertEquals("35", eval.get("separator.position"));
    }

    /**
     * Verifies that numeric expressions are rounded to the nearest integer.
     */
    @Test
    public void testPrecision() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("dialog.width", "$width * (2 div 3)");
        StylePropertyEvaluator template = new StylePropertyEvaluator(properties);
        Map<String, String> expanded = template.getProperties(1024, 768);
        assertEquals("683", expanded.get("dialog.width"));
    }

    /**
     * Verifies that a {@link StyleSheetException} is thrown if an expression is invalid.
     */
    @Test
    public void testInvalidExpression() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("dialog.width", "$width / 2");
        StylePropertyEvaluator template = new StylePropertyEvaluator(properties);
        try {
            template.getProperties(1024, 768);
            fail("Expected StyleSheetException to be thrown");
        } catch (StyleSheetException exception) {
            assertEquals(StyleSheetException.ErrorCode.InvalidExpression, exception.getErrorCode());
        }
    }

}