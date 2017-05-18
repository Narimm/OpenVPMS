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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.echo.dialog;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Window;
import nextapp.echo2.app.WindowPane;
import org.openvpms.web.echo.focus.FocusGroup;


/**
 * Dialog manager.
 *
 * @author Tim Anderson
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
        int lastIndex = 1000;
        // hack to workaround tabindexes being set on components on the root
        // window. Ideally, would have major components implement an interface
        // that returns a FocusGroup, to accurately determine tabindexes

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

        root.getContent().add(dialog);
    }

    /**
     * Determines if a component is hidden behind modal dialogs.
     * <p/>
     * The component must be a child of a {@link WindowPane}.
     *
     * @param component the component
     * @return {@code true} if the component is hidden, and therefore cannot be selected
     */
    public static boolean isHidden(Component component) {
        WindowPane parent = getWindowPane(component);
        if (parent != null) {
            ApplicationInstance active = ApplicationInstance.getActive();
            if (active != null) {
                Window root = active.getDefaultWindow();
                for (Component c : root.getContent().getComponents()) {
                    if (c instanceof WindowPane) {
                        WindowPane pane = (WindowPane) c;
                        if (pane.isModal() && pane.getZIndex() > parent.getZIndex()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns the {@code WindowPane} that a component belongs to.
     *
     * @param component the component
     * @return the {@code WindowPane} or {@code null} if none is found
     */
    protected static WindowPane getWindowPane(Component component) {
        while (component != null && !(component instanceof WindowPane)) {
            component = component.getParent();
        }
        return (WindowPane) component;
    }

}
