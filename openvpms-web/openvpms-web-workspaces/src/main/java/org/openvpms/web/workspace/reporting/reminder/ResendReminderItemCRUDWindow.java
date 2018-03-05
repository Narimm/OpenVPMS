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

package org.openvpms.web.workspace.reporting.reminder;

import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.help.HelpContext;

/**
 * Resend reminder item CRUD window.
 *
 * @author Tim Anderson
 * @see ReminderWorkspace
 */
class ResendReminderItemCRUDWindow extends ReminderItemCRUDWindow {

    /**
     * Constructs a {@link ResendReminderItemCRUDWindow}.
     *
     * @param browser the browser
     * @param context the context
     * @param help    the help context
     */
    public ResendReminderItemCRUDWindow(ReminderItemBrowser browser, Context context, HelpContext help) {
        super(browser, true, context, help);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button set
     */
    protected void layoutButtons(ButtonSet buttons) {
        buttons.add(createEditButton());
        buttons.add(createSendButton());
        buttons.add(createSendAllButton());
        buttons.add(createPreviewButton());
    }

}
