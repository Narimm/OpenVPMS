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

package org.openvpms.web.workspace.patient.mr;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.workspace.ActCRUDWindow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.help.HelpContext;


/**
 * CRUD Window for patient reminders.
 *
 * @author Tim Anderson
 */
public class ReminderCRUDWindow extends ActCRUDWindow<Act> {

    /**
     * Resend button identifier.
     */
    private static final String RESEND_ID = "resend";


    /**
     * Constructs a {@link ReminderCRUDWindow}.
     *
     * @param context the context
     * @param help    the help context
     */
    public ReminderCRUDWindow(Context context, HelpContext help) {
        super(Archetypes.create(ReminderArchetypes.REMINDER, Act.class), ReminderActions.getInstance(), context, help);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        Button resend = ButtonFactory.create(RESEND_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onResend();
            }
        });
        buttons.add(resend);
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        super.enableButtons(buttons, enable);
        boolean enableResend = false;
        if (enable) {
            enableResend = getActions().canResendReminder(getObject());
        }
        buttons.setEnabled(RESEND_ID, enableResend);
    }

    /**
     * Returns the actions that may be performed on the selected object.
     *
     * @return the actions
     */
    @Override
    protected ReminderActions getActions() {
        return (ReminderActions) super.getActions();
    }

    /**
     * Invoked to resend a reminder.
     */
    private void onResend() {
        try {
            HelpContext help = getHelpContext().subtopic("resend");
            ResendReminderDialog dialog = ResendReminderDialog.create(getObject(), getContext(), help);
            if (dialog != null) {
                dialog.addWindowPaneListener(new WindowPaneListener() {
                    public void onClose(WindowPaneEvent event) {
                        onRefresh(getObject());
                    }
                });
                dialog.show();
            }
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
    }

}
