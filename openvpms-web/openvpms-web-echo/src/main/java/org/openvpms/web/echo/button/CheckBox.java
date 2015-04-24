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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.echo.button;

import org.openvpms.web.echo.style.Styles;

/**
 * A check box that uses a different styles when the check box is enabled/disabled.
 *
 * @author Tim Anderson
 */
public class CheckBox extends nextapp.echo2.app.CheckBox {

    private boolean edit = false;

    /**
     * The disabled style name.
     */
    private static final String DISABLED_STYLE = "disabled";

    /**
     * Sets the name of the style to use from the
     * {@code ApplicationInstance}-defined {@code StyleSheet}.
     * Setting the style name will have no impact on the local stylistic
     * properties of the {@code Component}.
     *
     * @param newValue the new style name
     * @see #getStyleName
     */
    @Override
    public void setStyleName(String newValue) {
        edit = Styles.EDIT.equals(newValue);
        if (edit) {
            if (isEnabled()) {
                super.setStyleName(Styles.DEFAULT);
            } else {
                super.setStyleName(DISABLED_STYLE);
            }
        } else {
            super.setStyleName(newValue);
        }
    }

    /**
     * Sets the enabled state of the {@code Component}.
     *
     * @param newValue the new state
     * @see #isEnabled
     */
    @Override
    public void setEnabled(boolean newValue) {
        super.setEnabled(newValue);
        if (edit) {
            if (newValue) {
                super.setStyleName(Styles.DEFAULT);
            } else {
                setStyleName(DISABLED_STYLE);
            }
        }
    }
}
