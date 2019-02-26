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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.test;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Window;
import nextapp.echo2.app.WindowPane;
import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.lang.StringUtils;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.echo.dialog.PopupDialog;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Helper routines for Echo Web framework tests.
 *
 * @author Tim Anderson
 */
public class EchoTestHelper {

    /**
     * Finds the {@link BrowserDialog} with the highest z-index.
     *
     * @return the browser dialog, or {@code null} if none is found
     */
    public static BrowserDialog findBrowserDialog() {
        return findWindowPane(BrowserDialog.class);
    }

    /**
     * Finds the {@link EditDialog} with the highest z-index.
     *
     * @return the edit dialog, or {@code null} if none is found
     */
    public static EditDialog findEditDialog() {
        return findWindowPane(EditDialog.class);
    }

    /**
     * Finds the {@link WindowPane} with the highest z-index.
     * @param type the window pane type to find
     * @return the window pane of the specified type, or {@code null} if none is found
     */
    public static <T extends WindowPane> T findWindowPane(Class<T> type) {
        Window root = ApplicationInstance.getActive().getDefaultWindow();
        int top = 0;
        T result = null;
        for (Component component : root.getContent().getComponents()) {
            if (component.getClass().isAssignableFrom(type)) {
                T pane = type.cast(component);
                int zIndex = pane.getZIndex();
                if (result == null || zIndex > top) {
                    result = pane;
                }
            }
        }
        return result;
    }

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
     * Finds and fires a button with the specified id.
     *
     * @param component the parent component
     * @param buttonId the button identifier
     */
    public static void fireButton(Component component, String buttonId) {
        Button button = findButton(component, buttonId);
        assertNotNull(button);
        button.fireActionPerformed(new ActionEvent(button, button.getActionCommand()));
    }

    /**
     * Finds a button with the specified id.
     *
     * @param component the parent component
     * @param buttonId the button identifier
     * @return the button, or {@code null} if none is found
     */
    public static Button findButton(Component component, String buttonId) {
        Button result = null;
        if (Button.class.isAssignableFrom(component.getClass()) && StringUtils.equals(buttonId, component.getId())) {
            result = (Button) component;
        } else {
            for (Component child : component.getComponents()) {
                if ((result = findButton(child, buttonId)) != null) {
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Helper to find a component of the specified type.
     *
     * @param component the compenent to begin the search from
     * @param type      the type of the component to find
     * @return the first matching component, or <tt>null</tt> if none is found
     */
    public static <T extends Component > T findComponent(Component component, Class < T > type) {
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
