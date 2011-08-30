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

package org.openvpms.web.component.button;

import org.openvpms.web.resource.util.Messages;


/**
 * Shortcut helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ShortcutHelper {

    /**
     * Returns the shortcut from a string.
     *
     * @param text the string
     * @return the first shortcut found in <code>text</code>, or
     *         <code>null</code> if none is found
     */
    public static String getShortcut(String text) {
        int index = 0;
        if (text == null) {
            return null;
        }
        String result = null;
        while ((index = text.indexOf("&", index)) != -1) {
            int keyIndex = index + 1;
            if (keyIndex < text.length()) {
                if (text.charAt(keyIndex) != '&') {
                    result = Character.toString(text.charAt(keyIndex));
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Returns localised text minus any shortcut indicators (single ampersands).
     *
     * @param key the key of the text to be returned
     * @return the text
     */
    public static String getLocalisedText(String key) {
        return getText(Messages.get(key));
    }

    /**
     * Returns a string minus any shortcut indicators (single ampersands).
     * Multiple ampersands are replaced with a single one.
     *
     * @param text the string
     * @return the text
     */
    public static String getText(String text) {
        int lastIndex = 0;
        int index;
        StringBuffer buf = new StringBuffer();
        while ((index = text.indexOf("&", lastIndex)) != -1) {
            int keyIndex = index + 1;
            if (keyIndex < text.length()) {
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
     * @param text the string containing the shortcut
     * @return an XHTML string with any shortcut underlined
     */
    public static String getHTML(String text) {
        int lastIndex = 0;
        int index;
        StringBuffer buf = new StringBuffer();
        while ((index = text.indexOf("&", lastIndex)) != -1) {
            int keyIndex = index + 1;
            if (keyIndex < text.length()) {
                buf.append(text.substring(lastIndex, index));
                buf.append("<span xmlns=\"http://www.w3.org/1999/xhtml\" ");
                buf.append("style=\"text-decoration:underline\">");
                buf.append(text.substring(keyIndex, keyIndex + 1));
                buf.append("</span>");
                index = keyIndex;
            }
            lastIndex = index + 1;
        }
        buf.append(text.substring(lastIndex));
        return buf.toString();

    }
}
