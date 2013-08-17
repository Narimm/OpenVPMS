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

package org.openvpms.web.component.im.view;

import nextapp.echo2.app.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods to walk a {@code Component} hierarchy, determining what the user is currently viewing.
 * <p/>
 * This is used to display the same layout when switching between viewing an object, and editing it.
 *
 * @author Tim Anderson
 */
public class SelectionHelper {

    /**
     * Walks a component hierarchy, returning the first visible {@link IMObjectComponent} from the root component.
     *
     * @param root the root component
     * @return the first visible {@link IMObjectComponent}, or {@code null} if none is found
     */
    public static IMObjectComponent getComponent(Component root) {
        IMObjectComponent result = null;
        if (root.isVisible() && root instanceof IMObjectComponent) {
            result = (IMObjectComponent) root;
        } else {
            for (Component child : root.getComponents()) {
                result = getComponent(child);
                if (result != null) {
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Walks a component hierarchy, returning the selection of the first visible {@link IMObjectComponent} from the root
     * component.
     *
     * @param root the root component
     * @return the selection of the first visible {@link IMObjectComponent}, or {@code null} if none is found
     */
    public static IMObjectComponent getSelected(Component root) {
        IMObjectComponent result = getComponent(root);
        return (result != null) ? result.getSelected() : null;
    }

    /**
     * Returns the selection path in a component hierarchy.
     * <p/>
     * This is the list of {@link Selection}s that the user has made, drilling down through the component hierarchy.
     *
     * @param root the root component
     * @return the selection path
     */
    public static List<Selection> getSelectionPath(Component root) {
        return getSelection(getSelected(root), new ArrayList<Selection>());
    }

    /**
     * Returns the selection path in a component hierarchy.
     * <p/>
     * This is the list of {@link Selection}s that the user has made, drilling down through the component hierarchy.
     *
     * @param parent the parent component. May be {@code null}
     * @param path   the selection path to add to
     * @return the selection path
     */
    private static List<Selection> getSelection(IMObjectComponent parent, List<Selection> path) {
        if (parent != null) {
            path.add(new Selection(parent.getNode(), parent.getObject()));
            getSelection(parent.getSelected(), path);
        }
        return path;
    }
}
