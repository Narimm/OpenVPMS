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
import nextapp.echo2.app.ContentPane;
import org.openvpms.web.echo.factory.ComponentFactory;


/**
 * Factory for {@link ContentPane}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ContentPaneFactory extends ComponentFactory {

    /**
     * Creates a new content pane.
     *
     * @return a new content pane
     */
    public static ContentPane create() {
        return new ContentPane();
    }

    /**
     * Create a new content pane, with a specific style.
     *
     * @param style the style to use
     * @return a new content pane.
     */
    public static ContentPane create(String style) {
        ContentPane pane = create();
        pane.setStyleName(style);
        return pane;
    }

    /**
     * Create a new content pane, with a specific style and child component.
     *
     * @param style the style to use
     * @param child the child component
     * @return a new content pane.
     */
    public static ContentPane create(String style, Component child) {
        ContentPane pane = create(style);
        pane.add(child);
        return pane;
    }

    /**
     * Create a new content pane, containing a set of components.
     *
     * @param components the components to add
     * @return a new content pane
     */
    public static ContentPane create(Component... components) {
        ContentPane pane = create();
        add(pane, components);
        return pane;
    }

}
