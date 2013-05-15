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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.echo.button;

import static org.apache.commons.lang.StringEscapeUtils.escapeXml;


/**
 * Shortcut helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ShortcutHelper {

    /**
     * The underline opening tag.
     */
    static final String UNDERLINE_OPEN
        = "<span xmlns=\"http://www.w3.org/1999/xhtml\" "
          + "style=\"text-decoration:underline\">";

    /**
     * The underline closing tag.
     */
    static final String UNDERLINE_CLOSE = "</span>";


    /**
     * Returns the shortcut from a string.
     *
     * @param text the string. May be <tt>null</tt>
     * @return the first shortcut found in <tt>text</tt>, or <tt>null</tt> if
     *         none is found
     */
    public static String getShortcut(String text) {
        if (text == null) {
            return null;
        }
        String result = null;
        int index = 0;
        while ((index = text.indexOf("&", index)) != -1) {
            int keyIndex = index + 1;
            if (keyIndex < text.length()) {
                if (text.charAt(keyIndex) != '&') {
                    result = Character.toString(text.charAt(keyIndex));
                    break;
                } else {
                    index++;
                }
            }
            index++;
        }
        return result;
    }

    /**
     * Returns a string minus any shortcut indicators (single ampersands).
     * Multiple ampersands are replaced with a single one.
     *
     * @param text the string. May be <tt>null</tt>
     * @return the text
     */
    public static String getText(String text) {
        if (text == null) {
            return "";
        }
        int lastIndex = 0;
        int index;
        StringBuffer buf = new StringBuffer();
        while ((index = text.indexOf("&", lastIndex)) != -1) {
            int keyIndex = index + 1;
            if (keyIndex < text.length()) {
                if (text.charAt(keyIndex) == '&') {
                    index = keyIndex;
                }
                buf.append(text.substring(lastIndex, index));
            }
            lastIndex = index + 1;
        }
        buf.append(text.substring(lastIndex));
        return buf.toString();
    }

    /**
     * Returns XHTML underlining the shortcut in a string.
     *
     * @param text the string containing the shortcut. May be <tt>null</tt>
     * @return an XHTML string with any shortcut underlined
     */
    public static String getHTML(String text) {
        if (text == null) {
            return "";
        }
        int lastIndex = 0;
        int index;
        StringBuffer buf = new StringBuffer();
        boolean first = true;
        while ((index = text.indexOf("&", lastIndex)) != -1) {
            int keyIndex = index + 1;
            if (keyIndex < text.length()) {
                if (text.charAt(keyIndex) == '&') {
                    // double ampersand
                    index = keyIndex;
                    buf.append(escapeXml(text.substring(lastIndex, index)));
                } else {
                    buf.append(escapeXml(text.substring(lastIndex, index)));
                    if (first) {
                        buf.append(UNDERLINE_OPEN);
                        buf.append(escapeXml(text.substring(keyIndex,
                                                            keyIndex + 1)));
                        buf.append(UNDERLINE_CLOSE);
                        first = false;
                    } else {
                        // only render one shortcut
                        buf.append(escapeXml(text.substring(keyIndex,
                                                            keyIndex + 1)));
                    }
                }
                index = keyIndex;
            }
            lastIndex = index + 1;
        }
        buf.append(escapeXml(text.substring(lastIndex)));
        return buf.toString();

    }
}
