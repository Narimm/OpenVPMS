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

package org.openvpms.web.component.mail;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link HtmlFilter}.
 *
 * @author Tim Anderson
 */
public class HtmlFilterTestCase {

    /**
     * Verifies that the inner html of the body element is returned by the filter.
     */
    @Test
    public void testHtml() {
        String html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" " +
                      "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
                      "<html>\n" +
                      "<head>\n" +
                      "  <title></title>\n" +
                      "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n" +
                      "  <style type=\"text/css\">\n" +
                      "    a {text-decoration: none}\n" +
                      "  </style>\n" +
                      "</head>\n" +
                      "<body>Hello World!</body>" +
                      "</html>";

        String result = HtmlFilter.filter(html);
        assertEquals("Hello World!", result);
    }

    /**
     * Verifies that element attributes are handle correctly.
     */
    @Test
    public void testAttributes() {
        String html = "<IMG SRC=\"http://www.creaturecomforts.com.hk/images/hh1.jpg\" " +
                      "NAME=\"graphics1\" ALIGN=LEFT WIDTH=384 HEIGHT=224 BORDER=0><BR CLEAR=LEFT><BR>";

        String result = HtmlFilter.filter(html);
        String expected = "<img src=\"http://www.creaturecomforts.com.hk/images/hh1.jpg\" " +
                          "name=\"graphics1\" align=\"left\" width=\"384\" height=\"224\" border=\"0\"/>" +
                          "<br clear=\"left\"/><br/>";
        assertEquals(expected, result);
    }

    /**
     * Verifies that nested elements are handled correctly.
     */
    @Test
    public void testElements() {
        String html = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n" +
                      "<html>\n" +
                      "  <head>\n" +
                      "    <title>Sample</title>\n" +
                      "  </head>\n" +
                      "\n" +
                      "  <body>\n" +
                      "    <p>This should be normal</p>\n" +
                      "    <p><span style=\"font-size: 19pt; color: #CC0000\">" +
                      "This should be in big red letters.</span></p>\n" +
                      "  </body>\n" +
                      "</html>\n";
        String result = HtmlFilter.filter(html);
        String expected = "<p>This should be normal</p><p><span style=\"font-size: 19pt; color: #CC0000\">" +
                          "This should be in big red letters.</span></p>";
        assertEquals(expected, result);
    }
}
