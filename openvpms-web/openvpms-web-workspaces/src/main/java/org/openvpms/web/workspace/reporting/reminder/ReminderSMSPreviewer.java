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

import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.sms.SMSDialog;
import org.openvpms.web.echo.help.HelpContext;

/**
 * Previews SMS patient reminders.
 *
 * @author Tim Anderson
 */
public class ReminderSMSPreviewer extends AbstractPatientReminderPreviewer {

    /**
     * Constructs a {@link ReminderSMSPreviewer}.
     *
     * @param processor the processor to use to prepare reminders
     */
    public ReminderSMSPreviewer(ReminderSMSProcessor processor, HelpContext help) {
        super(processor, help);
    }

    /**
     * Previews reminders.
     *
     * @param reminders the reminders
     * @param processor the processor
     * @param help      the help context
     */
    @Override
    protected void preview(PatientReminders reminders, PatientReminderProcessor processor, HelpContext help) {
        SMSReminders state = (SMSReminders) reminders;
        Contact contact = state.getContact();
        String text = state.getText(processor.getPractice());
        Context context = state.createContext(processor.getPractice());
        SMSDialog dialog = new SMSDialog(contact, context, help);
        dialog.show();
        dialog.setMessage(text);
    }
}
