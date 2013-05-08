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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.test;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.web.echo.dialog.PopupDialog;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Helper routines for Echo Web framework tests.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class EchoTestHelper {

    /**
     * Helper to click a button on a dialog.
     *
     * @param dialog   the dialog
     * @param buttonId the button identifier
     */
    public static void fireDialogButton(PopupDialog dialog, String buttonId) {
        Button button = dialog.getButtons().getButton(buttonId);
        assertNotNull(button);
        assertTrue(button.isEnabled());
        button.fireActionPerformed(new ActionEvent(button, button.getActionCommand()));
    }

    /**
     * Helper to find a component of the specified type.
     *
     * @param component the compenent to begin the search from
     * @param type      the type of the component to find
     * @return the first matching component, or <tt>null</tt> if none is found
     */
    public static <T extends Component> T findComponent(Component component, Class<T> type) {
        Component result = (type.isAssignableFrom(component.getClass())) ? component : null;
        if (result == null) {
            for (Component child : component.getComponents()) {
                result = findComponent(child, type);
                if (result != null) {
                    break;
                }
            }
        }
        return type.cast(result);
    }

}
