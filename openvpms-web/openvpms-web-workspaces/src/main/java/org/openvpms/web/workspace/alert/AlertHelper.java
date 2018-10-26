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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.alert;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Color;
import org.openvpms.web.component.alert.Alert;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;

/**
 * Alert helper methods.
 *
 * @author Tim Anderson
 */
class AlertHelper {

    /**
     * Creates a button to display an alert.
     * <p>
     * This has a tooltip with the alert reason, if one is present
     *
     * @param alert    the alert
     * @param listener the listener to register
     * @return the button
     */
    public static Button createButton(Alert alert, ActionListener listener) {
        Button result = ButtonFactory.create(null, "small", listener);
        result.setText(alert.getName());
        String reason = alert.getReason();
        if (reason != null) {
            result.setToolTipText(reason);
        }
        Color colour = alert.getColour();
        if (colour != null) {
            result.setBackground(colour);
            result.setForeground(alert.getTextColour());
        }
        return result;
    }
}
