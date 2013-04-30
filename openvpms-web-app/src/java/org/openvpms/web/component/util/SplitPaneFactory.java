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
import nextapp.echo2.app.SplitPane;


/**
 * Factory for {@link SplitPane}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public final class SplitPaneFactory extends ComponentFactory {

    /**
     * Create a new split pane.
     *
     * @param orientation the orientation
     * @return a new split pane
     */
    public static SplitPane create(int orientation) {
        return new SplitPane(orientation);
    }

    /**
     * Create a split pane, with a specific style.
     *
     * @param style the style name
     * @return a new split pane
     */
    public static SplitPane create(String style) {
        SplitPane pane = new SplitPane();
        setStyle(pane, style);
        return pane;
    }

    /**
     * Create a split pane containing a set of components.
     *
     * @param orientation the orientation
     * @param components  the components to add
     */
    public static SplitPane create(int orientation, Component... components) {
        SplitPane pane = create(orientation);
        add(pane, components);
        return pane;
    }

    /**
     * Create a split pane, with a specific style and containing a set of
     * components
     *
     * @param orientation the orientation
     * @param style       the style name
     * @param components  the components to add
     * @return a new split pane
     */
    public static SplitPane create(int orientation, String style, Component... components) {
        SplitPane pane = create(orientation);
        setStyle(pane, style);
        add(pane, components);
        return pane;
    }

}
