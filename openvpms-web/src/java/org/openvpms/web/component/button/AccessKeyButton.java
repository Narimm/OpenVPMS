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

import echopointng.PushButton;


/**
 * A button that renders its access key with an underline.
 * Note that this button uses standard browser 'accesskey' support.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AccessKeyButton extends PushButton {

    /**
     * Creates a new button.
     */
    public AccessKeyButton() {
    }

    /**
     * Creates a button with text.
     *
     * @param text A text label to display in the button.
     */
    public AccessKeyButton(String text) {
        super(text);
    }

    /**
     * Sets the button text.
     *
     * @param text the button text
     */
    public void setText(String text) {
        if (text != null) {
            String key = ShortcutHelper.getShortcut(text);
            if (key != null) {
                setAccessKey(key);
                text = ShortcutHelper.getText(text);
            }
        }
        super.setText(text);
    }

}
