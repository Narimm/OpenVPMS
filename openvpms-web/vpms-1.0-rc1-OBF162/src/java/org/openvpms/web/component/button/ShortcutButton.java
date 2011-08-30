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
 * EchoPointNG's <code>KeyStrokeListener</code> class.
 * The {@link ButtonSet} class provides a convenient way of receiving
 * keystroke notification.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ShortcutButton extends ButtonEx {

    /**
     * The shortcut key.
     */
    String key;

    /**
     * Constructs a new <code>ShortcutButton</code>.
     */
    public ShortcutButton() {
    }

    /**
     * Constructs a new <code>ShortcutButton</code>.
     *
     * @param text the button text
     */
    public ShortcutButton(String text) {
        setText(text);
    }

    /**
     * Sets the button text. Any shortcut will be parsed from the text.
     * The shortcut must be prefixed with an '&';
     *
     * @param text the button text
     */
    @Override
    public void setText(String text) {
        key = ShortcutHelper.getShortcut(text);
        if (key != null) {
            XhtmlFragment fragment = new XhtmlFragment(
                    ShortcutHelper.getHTML(text));
            setText(fragment);
        } else {
            super.setText(text);
        }
    }

    /**
     * Returns the keycode for this button.
     *
     * @return the keycode for this button, or <code>-1</code> if none is
     *         present
     */
    public int getKeyCode() {
        if (!StringUtils.isEmpty(key)) {
            char code = key.toUpperCase().toCharArray()[0];
            return KeyStrokeHelper.getKeyCode(code);
        }
        return -1;
    }

}