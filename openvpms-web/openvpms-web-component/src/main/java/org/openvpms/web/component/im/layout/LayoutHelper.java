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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.layout;

import echopointng.TabbedPane;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;

/**
 * Layout helper methods.
 *
 * @author Tim Anderson
 */
public class LayoutHelper {

    /**
     * Helper to determine if a component needs to be inset.
     *
     * @param component the component
     * @return {@code true} if the component is inset, otherwise {@code false}
     */
    public static boolean needsInset(Component component) {
        boolean result = true;
        if (hasTabbedPane(component)) {
            // TabbedPane looks terrible when inset
            result = false;
        } else if (component.getStyleName() != null && component.getStyleName().startsWith("Inset")) {
            result = false;
        } else if (component.getComponentCount() == 1) {
            Component child = component.getComponent(0);
            if (child instanceof Row || child instanceof Column) {
                result = needsInset(child);
            }
        }
        return result;
    }

    /**
     * Determines if a component has a top-level tabbed pane.
     *
     * @param component the component
     * @return {@code true} if the component has a top-level tabbed pane
     */
    private static boolean hasTabbedPane(Component component) {
        if (component instanceof TabbedPane) {
            return true;
        } else if (component instanceof Row || component instanceof Column) {
            for (Component child : component.getComponents()) {
                if (hasTabbedPane(child)) {
                    return true;
                }
            }
        }
        return false;
    }
}