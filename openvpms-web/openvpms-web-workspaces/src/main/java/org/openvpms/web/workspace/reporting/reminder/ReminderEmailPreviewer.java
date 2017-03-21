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
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.mail.MailDialog;
import org.openvpms.web.component.mail.MailDialogFactory;
import org.openvpms.web.component.mail.MailEditor;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.CustomerMailContext;

/**
 * Previews email reminders.
 *
 * @author Tim Anderson
 */
public class ReminderEmailPreviewer extends AbstractPatientReminderPreviewer {

    /**
     * Constructs a {@link ReminderEmailPreviewer}.
     *
     * @param processor the processor to use to prepare reminders
     * @param help      the help context
     */
    public ReminderEmailPreviewer(ReminderEmailProcessor processor, HelpContext help) {
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
        EmailReminders state = (EmailReminders) reminders;
        Contact contact = state.getContact();
        Context context = state.createContext(processor.getPractice());
        MailContext mailContext = new CustomerMailContext(context, help);
        MailDialogFactory factory = ServiceHelper.getBean(MailDialogFactory.class);
        MailDialog dialog = factory.create(mailContext, contact, new DefaultLayoutContext(context, help));
        MailEditor editor = dialog.getMailEditor();
        editor.setSubject(state.getSubject(context));
        editor.setMessage(state.getMessage(context));
        if (processor.getConfig().getEmailAttachments()) {
            editor.addAttachment(state.createAttachment(context));
        }
        dialog.show();
    }
}
