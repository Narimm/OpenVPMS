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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.echo.tabpane;

import echopointng.TabbedPane;
import echopointng.tabbedpane.TabModel;
import org.openvpms.web.echo.keyboard.KeyStrokeHandler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TabPane extends TabbedPane implements KeyStrokeHandler {

    /**
     * Constructs a new <tt>TabPane</tt>.
     */
    public TabPane() {
    }

    /**
     * Constructs a new <tt>TabPane</tt>.
     *
     * @param model the model
     */
    public TabPane(TabModel model) {
        super(model);
        // register a listener to re-register keystroke listeners when
        // the pane changes as Firefox seems to lose the registration.
        addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                reregisterKeyStrokeListeners();
            }
        });
    }

    /**
     * Re-registers keystroke listeners.
     */
    public void reregisterKeyStrokeListeners() {
        TabModel model = getModel();
        if (model instanceof TabPaneModel) {
            ((TabPaneModel) model).reregisterKeyStrokeListeners();
        }
    }
}
