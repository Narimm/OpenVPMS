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

package org.openvpms.web.echo.factory;

import nextapp.echo2.app.Component;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;


/**
 * Factory for {@link Component}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ComponentFactory {

    /**
     * Helper to set the default style for a component.
     *
     * @param component the component to populate
     */
    public static void setDefaultStyle(Component component) {
        setStyle(component, Styles.DEFAULT);
    }

    /**
     * Sets the style name, adjusting it to the resolution of the client
     * browser, where a style for that resolution exists.
     *
     * @param component the component
     * @param style     the style name
     */
    public static void setStyle(Component component, String style) {
        component.setStyleName(style);
    }

    /**
     * Helper to return localised text for a component.
     *
     * @param type      the component type
     * @param name      the component instance name
     * @param allowNull if <code>true</code> return <code>null</code> if there
     *                  is no text for the give <code>type</code> and
     *                  <code>name</code>
     * @return the localised string corresponding to <code>key</code>
     */
    protected static String getString(String type, String name,
                                      boolean allowNull) {
        String result = Messages.get(type + "." + name, true);
        if (result == null) {
            result = Messages.get(name, allowNull);
        }
        return result;
    }

    /**
     * Helper to add a set of components to a container.
     *
     * @param container  the container
     * @param components the components to add
     */
    protected static void add(Component container, Component... components) {
        for (Component component : components) {
            container.add(component);
        }
    }
}
