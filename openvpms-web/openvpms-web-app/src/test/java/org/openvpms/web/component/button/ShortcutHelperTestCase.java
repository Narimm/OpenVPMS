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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.button;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


/**
 * Tests the {@link ShortcutHelper} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ShortcutHelperTestCase {

    /**
     * Tests the {@link ShortcutHelper#getShortcut(String)} method.
     */
    @Test
    public void testGetShortcut() {
        assertNull(ShortcutHelper.getShortcut(null));
        assertNull(ShortcutHelper.getShortcut(""));
        assertNull(ShortcutHelper.getShortcut("&"));
        assertNull(ShortcutHelper.getShortcut("&&"));
        assertEquals("A", ShortcutHelper.getShortcut("&A"));
        assertEquals("A", ShortcutHelper.getShortcut("&A &B"));
        assertEquals("B", ShortcutHelper.getShortcut("&& &B"));
    }

    /**
     * Tests the {@link ShortcutHelper#getText(String)} method.
     */
    @Test
    public void testGetText() {
        assertEquals("", ShortcutHelper.getText(null));
        assertEquals("", ShortcutHelper.getText(""));
        assertEquals("", ShortcutHelper.getText("&"));
        assertEquals("&", ShortcutHelper.getText("&&"));
        assertEquals("A", ShortcutHelper.getText("&A"));
        assertEquals("A B", ShortcutHelper.getText("&A &B"));
        assertEquals("& B", ShortcutHelper.getText("&& &B"));
    }

    /**
     * Tests the {@link ShortcutHelper#getHTML(String)} method.
     */
    @Test
    public void testGetHTML() {
        checkHtml(null, "");
        checkHtml("a simple string", "a simple string");
        checkHtml("<", "&lt;");
        checkHtml(">", "&gt;");
        checkHtml("&&", "&amp;");
        checkHtml("&A", ShortcutHelper.UNDERLINE_OPEN + "A"
                        + ShortcutHelper.UNDERLINE_CLOSE);
        checkHtml("&A &B",
                  ShortcutHelper.UNDERLINE_OPEN + "A"
                  + ShortcutHelper.UNDERLINE_CLOSE + " B");
        checkHtml("&& &B",
                  "&amp; " + ShortcutHelper.UNDERLINE_OPEN + "B" +
                  "" + ShortcutHelper.UNDERLINE_CLOSE);
    }

    /**
     * Verifies that {@link ShortcutHelper#getHTML(String)} returns the
     * expected result.
     *
     * @param text     the text
     * @param expected the expected result
     */
    private void checkHtml(String text, String expected) {
        assertEquals(expected, ShortcutHelper.getHTML(text));
    }
}
