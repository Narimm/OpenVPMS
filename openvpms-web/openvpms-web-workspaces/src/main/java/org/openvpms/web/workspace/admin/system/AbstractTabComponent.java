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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.system;

import org.openvpms.web.component.workspace.TabComponent;
import org.openvpms.web.echo.button.ButtonRow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.help.HelpContext;

/**
 * Abstract implementation of the {@link TabComponent} interface.
 *
 * @author Tim Anderson
 */
abstract class AbstractTabComponent implements TabComponent {

    /**
     * The help context.
     */
    private final HelpContext help;

    /**
     * The buttons.
     */
    private ButtonRow buttons;

    /**
     * The focus group.
     */
    private final FocusGroup focusGroup = new FocusGroup(getClass().getName());


    /**
     * Constructs an {@link AbstractTabComponent}.
     *
     * @param help the help context for the tab
     */
    public AbstractTabComponent(HelpContext help) {
        this.help = help;
        buttons = new ButtonRow(new FocusGroup(getClass().getName() + ".buttons"), "ControlRow",
                                ButtonRow.BUTTON_STYLE);
    }

    /**
     * Returns the help context for the tab.
     *
     * @return the help context
     */
    @Override
    public HelpContext getHelpContext() {
        return help;
    }

    /**
     * Returns the button row.
     *
     * @return the button row
     */
    protected ButtonRow getButtons() {
        return buttons;
    }

    /**
     * Returns the buttons.
     *
     * @return the buttons
     */
    protected ButtonSet getButtonSet() {
        return buttons.getButtons();
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group
     */
    protected FocusGroup getFocusGroup() {
        return focusGroup;
    }
}
