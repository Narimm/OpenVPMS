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

package org.openvpms.web.component.dialog;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Window;
import nextapp.echo2.app.WindowPane;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.web.component.button.KeyStrokeHandler;
import org.openvpms.web.component.focus.FocusGroup;


/**
 * Dialog manager.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DialogManager {

    /**
     * Shows a dialog.
     * The dialog's focus group will be reindexed if it overlaps existing
     * dialogs.
     *
     * @param dialog the dialog to show
     */
    public static void show(PopupWindow dialog) {
        int lastIndex = -1;
        Window root = ApplicationInstance.getActive().getDefaultWindow();
        int zIndex = 0;
        for (Component component : root.getContent().getComponents()) {
            if (component instanceof WindowPane) {
                WindowPane pane = (WindowPane) component;
                if (pane.getZIndex() > zIndex) {
                    zIndex = pane.getZIndex();
                }
                if (component instanceof PopupWindow) {
                    FocusGroup group
                            = ((PopupWindow) component).getFocusGroup();
                    if (group.getLast() > lastIndex) {
                        lastIndex = group.getLast();
                    }
                }
            }
        }
        dialog.setZIndex(zIndex + 1);
        FocusGroup group = dialog.getFocusGroup();
        if (group.getFirst() <= lastIndex) {
            group.reindex(lastIndex + 1000);
            // give the parent dialog room to grow.
        }

        dialog.addWindowPaneListener(new WindowPaneListener() {
            /**
             * Invoked when a user attempts to close a <code>WindowPane</code>.
             *
             * @param e the <code>WindowPaneEvent</code> describing the change
             */
            public void windowPaneClosing(WindowPaneEvent e) {
                reregisterKeyStrokeListeners();
            }
        });

        root.getContent().add(dialog);
    }

    /**
     * Reregisters any keystroke listeners.
     */
    private static void reregisterKeyStrokeListeners() {
        Window root = ApplicationInstance.getActive().getDefaultWindow();
        reregisterKeyStrokeListeners(root);
    }

    /**
     * Reregisters any keystroke listeners by traversing the component heiarchy
     * looking for components that inplement the {@link KeyStrokeHandler}
     * interface.
     */
    private static void reregisterKeyStrokeListeners(Component component) {
        if (component instanceof KeyStrokeHandler) {
            ((KeyStrokeHandler) component).reregisterKeyStrokeListeners();
        }
        for (Component child : component.getComponents()) {
            reregisterKeyStrokeListeners(child);
        }
    }


}
