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

package org.openvpms.web.component.util;

import echopointng.tabbedpane.DefaultTabModel;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.ImageReference;
import org.openvpms.web.component.button.KeyStrokeHandler;
import org.openvpms.web.component.button.ShortcutButton;
import org.openvpms.web.component.button.ShortcutButtons;
import org.openvpms.web.component.button.ShortcutHelper;


/**
 * A <tt>TabModel</tt> that supports shortcut keys on its buttons.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TabPaneModel extends DefaultTabModel implements KeyStrokeHandler {

    /**
     * The shortcut buttons. May be <tt>null</tt>
     */
    private final ShortcutButtons buttons;

    /**
     * Constructs a new <tt>TabPaneModel</tt> that doesn't provide shortcut
     * support.
     */
    public TabPaneModel() {
        this(null);
    }

    /**
     * Constructs a new <tt>TabPaneModel</tt> that supports shortcut support
     * if a container is specified.
     *
     * @param container the container. May be <tt>null</tt>
     */
    public TabPaneModel(Component container) {
        buttons = (container != null) ? new ShortcutButtons(
            container) : null;
    }

    /**
     * Re-registers keystroke listeners.
     */
    public void reregisterKeyStrokeListeners() {
        if (buttons != null) {
            buttons.reregisterKeyStrokeListeners();
        }
    }

    /**
     * This method is called to create a Tab component with the specified text
     * and icon. The default behaviour creates ButtonEx instances. Subclasses
     * can overrride this method to modify what components are returned.
     *
     * @param tabTitle -
     *                 the title of the tab
     * @param tabIcon  -
     *                 the icon for the tab
     * @return a component that will be used as the Tab. This will most likely
     *         be a Button.
     */
    @Override
    protected Component createTabComponent(String tabTitle,
                                           ImageReference tabIcon) {
        ShortcutButton result = new ShortcutButton();
        result.setActionCommand(ShortcutHelper.getShortcut(tabTitle));
        result.setText(tabTitle);
        result.setIcon(tabIcon);
        //result.setStyle(DEFAULT_TOP_ALIGNED_STYLE);
        ComponentFactory.setDefaultStyle(result);
        result.setFocusTraversalParticipant(false);
        if (buttons != null) {
            buttons.add(result);
        }
        return result;
    }
}
