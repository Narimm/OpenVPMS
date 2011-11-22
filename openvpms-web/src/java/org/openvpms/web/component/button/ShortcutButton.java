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

import echopointng.ButtonEx;
import echopointng.xhtml.XhtmlFragment;
import org.apache.commons.lang.StringUtils;
import org.openvpms.web.component.util.KeyStrokeHelper;


/**
 * A button that renders its access key with an underline.
 * Unlike {@link AccessKeyButton} this button does not use the standard
 * browser 'accesskey', but must instead be used in conjunction with
 * EchoPointNG's <tt>KeyStrokeListener</tt> class.
 * The {@link ButtonSet} class provides a convenient way of receiving
 * keystroke notification.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ShortcutButton extends ButtonEx {

    /**
     * The key code.
     */
    private int code = -1;


    /**
     * Constructs a <tt>ShortcutButton</tt>.
     */
    public ShortcutButton() {
    }

    /**
     * Constructs a <tt>ShortcutButton</tt>.
     *
     * @param text the button text
     */
    public ShortcutButton(String text) {
        setText(text);
    }

    /**
     * Sets the key code.
     * <p/>
     * This replaces any code that may have been specified by the button text.
     *
     * @param code the key code
     */
    public void setKeyCode(int code) {
        this.code = code;
    }

    /**
     * Returns the keycode for this button.
     *
     * @return the keycode for this button, or <tt>-1</tt> if none is present
     */
    public int getKeyCode() {
        return code;
    }

    /**
     * Sets the button text. Any shortcut will be parsed from the text.
     * The shortcut must be prefixed with an '&';
     *
     * @param text the button text
     */
    @Override
    public void setText(String text) {
        String key = ShortcutHelper.getShortcut(text);
        if (!StringUtils.isEmpty(key)) {
            XhtmlFragment fragment = new XhtmlFragment(ShortcutHelper.getHTML(text));
            setText(fragment);
            char ch = key.toUpperCase().toCharArray()[0];
            code = KeyStrokeHelper.getKeyCode(ch);
        } else {
            if (text != null) {
                text = text.replace("&&", "&");     // replace escaped ampersands
            }
            super.setText(text);
        }
    }

}