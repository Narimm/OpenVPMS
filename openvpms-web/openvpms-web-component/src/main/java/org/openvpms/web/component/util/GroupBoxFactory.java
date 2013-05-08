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

package org.openvpms.web.component.util;

import echopointng.GroupBox;
import nextapp.echo2.app.Component;
import org.openvpms.web.echo.factory.ComponentFactory;


/**
 * Factory for {@link GroupBox}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class GroupBoxFactory extends ComponentFactory {

    /**
     * Create a new group box.
     *
     * @return a new group box
     */
    public static GroupBox create() {
        GroupBox box = new GroupBox();
        setDefaultStyle(box);
        return box;
    }

    /**
     * Create a new group box with localised title.
     *
     * @param key the resource bundle key. May be <code>null</code>
     * @return a new group box
     */
    public static GroupBox create(String key) {
        GroupBox box = create();
        if (key != null) {
            box.setTitle(getString("label", key, false));
        }
        return box;
    }

    /**
     * Create a new group box with localised title and style.
     *
     * @param key   the resource bundle key. May be <code>null</code>
     * @param style the style name
     * @return a new group box
     */
    public static GroupBox create(String key, String style) {
        GroupBox box = create(key);
        box.setStyleName(style);
        return box;
    }

    /**
     * Create a new group box, containing a set of components.
     *
     * @param components the components to add
     * @return a new group box
     */
    public static GroupBox create(Component... components) {
        GroupBox box = create();
        add(box, components);
        return box;
    }

    /**
     * Create a new group box with a localised title, and containing a set of
     * components.
     *
     * @param key        the resource bundle key. May be <code>null</code>
     * @param components the components to add
     * @return a new group box
     */
    public static GroupBox create(String key, Component... components) {
        GroupBox box = create(key);
        add(box, components);
        return box;
    }

    /**
     * Create a new group box with localised title and style.
     *
     * @param key        the resource bundle key. May be <code>null</code>
     * @param style      the style name
     * @param components the components to add
     * @return a new group box
     */
    public static GroupBox create(String key, String style,
                                  Component... components) {
        GroupBox box = create(key, style);
        add(box, components);
        return box;
    }
}
