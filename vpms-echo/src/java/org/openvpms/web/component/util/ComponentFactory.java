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

import nextapp.echo2.app.Component;

import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.resource.util.Styles;


/**
 * Factory for {@link Component}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ComponentFactory {

    /**
     * Helper to set defaults for a component.
     *
     * @param component the component to populate
     */
    public static void setDefaults(Component component) {
        component.setStyleName(Styles.DEFAULT);
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
    protected static String getString(String type, String name, boolean allowNull) {
        return Messages.get(type + "." + name, allowNull);
    }

    /**
     * Helper to add a set of components to a container.
     *
     * @param container  the container
     * @param components the components to add
     */
    protected static void add(Component container, Component ... components) {
        for (Component component : components) {
            container.add(component);
        }
    }
}
