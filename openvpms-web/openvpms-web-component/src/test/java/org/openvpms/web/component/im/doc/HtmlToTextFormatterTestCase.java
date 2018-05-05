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

package org.openvpms.web.component.im.doc;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link HtmlToTextFormatter}.
 *
 * @author Tim Anderson
 */
public class HtmlToTextFormatterTestCase {

    /**
     * Tests formatting.
     */
    @Test
    public void testFormat() {
        HtmlToTextFormatter formatter = new HtmlToTextFormatter();
        assertEquals("Test\none", formatter.format("Test<br/>one"));

        assertEquals("\n1. one\n2. two\n3. three",
                     formatter.format("<ol><li>one</li><li>two</li><li>three</li></ol>"));

        assertEquals("Preamble\n* one\n* two\n* three",
                     formatter.format("Preamble<ul><li>one</li><li>two</li><li>three</li></ul>"));

        assertEquals("\n* A\n  1. one\n  2. two\n  3. three\n* B\n* C",
                     formatter.format("<ul><li>A<ol><li>one</li><li>two</li><li>three</li></ol></li>"
                                      + "<li>B</li>"
                                      + "<li>C</li></ul>"));
        assertEquals("\nOne Two Three\n1 2 3\n",
                     formatter.format("<table><tr><th>One</th><th>Two</th><th>Three</th></tr>"
                                      + "<tr><td>1</td><td>2</td><td>3</td></tr></table>"));
    }

}
