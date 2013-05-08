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

import echopointng.TabbedPane;
import echopointng.tabbedpane.TabModel;
import org.openvpms.web.echo.factory.ComponentFactory;


/**
 * Factory for <code>TabbedPane</code>s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-04 06:28:25Z $
 */
public final class TabbedPaneFactory extends ComponentFactory {

    /**
     * Create a new tabbed pane.
     *
     * @return a new tabbed pane
     */
    public static TabbedPane create() {
        TabPane tab = new TabPane();
        return init(tab);
    }

    /**
     * Creates a new tabbed pane.
     *
     * @param model the tab model
     * @return a new tabbed pane
     */
    public static TabbedPane create(TabModel model) {
        TabPane tab = new TabPane(model);
        return init(tab);
    }

    /**
     * Initialises a tabbed pane.
     *
     * @param tab the pane to initialise
     * @return the initialised pane
     */
    private static TabbedPane init(TabbedPane tab) {
        setDefaultStyle(tab);
        tab.setTabBorderStyle(TabbedPane.TAB_STRIP_ONLY);
        return tab;
    }

}
