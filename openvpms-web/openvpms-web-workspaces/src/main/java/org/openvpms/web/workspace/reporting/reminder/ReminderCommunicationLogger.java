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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.reminder;

import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.mail.Mailer;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.communication.CommunicationHelper;
import org.openvpms.web.workspace.customer.communication.CommunicationLogger;

import java.util.List;

/**
 * Logs reminder communications to {@link CommunicationLogger}.
 *
 * @author Tim Anderson
 */
public class ReminderCommunicationLogger {

    public static final String PATIENT_REMINDER = "PATIENT_REMINDER";

    /**
     * The communication logger.
     */
    private CommunicationLogger service;

    /**
     * Constructs a {@link ReminderCommunicationLogger}.
     *
     * @param service the logging service
     */
    public ReminderCommunicationLogger(CommunicationLogger service) {
        this.service = service;
    }

    /**
     * Logs email reminders.
     *
     * @param reminders the reminders
     * @param location  the practice location. May be {@code null}
     * @param mailer    the mailer
     */
    public void logEmail(List<ReminderEvent> reminders, Party location, Mailer mailer) {
        String attachments = CommunicationHelper.getAttachments(mailer.getAttachments());
        for (ReminderEvent reminder : reminders) {
            DocumentTemplate template = new DocumentTemplate(reminder.getDocumentTemplate(),
                                                             ServiceHelper.getArchetypeService());
            String notes = getNote(reminder);

            service.logEmail(reminder.getCustomer(), reminder.getPatient(), mailer.getTo(), mailer.getCc(),
                             mailer.getBcc(), template.getEmailSubject(), PATIENT_REMINDER, template.getEmailText(),
                             notes, attachments, location);
        }
    }

    /**
     * Logs mail (i.e. printed) reminders.
     *
     * @param reminders the reminders
     * @param location  the practice location. May be {@code null}
     */
    public void logMail(List<ReminderEvent> reminders, Party location) {
        for (ReminderEvent reminder : reminders) {
            DocumentTemplate template = new DocumentTemplate(reminder.getDocumentTemplate(),
                                                             ServiceHelper.getArchetypeService());
            String notes = getNote(reminder);

            service.logMail(reminder.getCustomer(), reminder.getPatient(), reminder.getContact().getDescription(),
                            template.getEmailSubject(), PATIENT_REMINDER, template.getEmailText(), notes, location);
        }
    }

    /**
     * Logs exported reminders.
     *
     * @param reminder the reminder
     * @param location the practice location. May be {@code null}
     */
    public void logExport(ReminderEvent reminder, Party location) {
        String notes = getNote(reminder);
        String subject = Messages.get("reminder.log.export.subject");
        service.logMail(reminder.getCustomer(), reminder.getPatient(), reminder.getContact().getDescription(),
                        subject, PATIENT_REMINDER, null, notes, location);
    }

    /**
     * Logs listed reminders.
     *
     * @param reminder the reminder
     * @param location the practice location. May be {@code null}
     */
    public void logList(ReminderEvent reminder, Party location) {
        String notes = getNote(reminder);
        String subject = Messages.get("reminder.log.list.subject");
        Party customer = reminder.getCustomer();
        String contact = (reminder.getContact() != null) ? reminder.getContact().getDescription() : "";
        if (customer != null) {
            service.logPhone(customer, reminder.getPatient(), contact, subject, PATIENT_REMINDER, null, notes,
                             location);
        }
    }

    /**
     * Logs SMS reminders.
     *
     * @param reminders the reminders
     * @param location  the practice location. May be {@code null}
     */
    public void logSMS(List<ReminderEvent> reminders, Party location) {
        for (ReminderEvent reminder : reminders) {
            DocumentTemplate template = new DocumentTemplate(reminder.getDocumentTemplate(),
                                                             ServiceHelper.getArchetypeService());
            String notes = getNote(reminder);

            service.logSMS(reminder.getCustomer(), reminder.getPatient(), reminder.getContact().getDescription(),
                           template.getEmailSubject(), PATIENT_REMINDER, template.getSMS(), notes, location);
        }
    }

    /**
     * Returns a formatted note containing the reminder type and count.
     *
     * @param reminder the reminder
     * @return the note
     */
    protected String getNote(ReminderEvent reminder) {
        ActBean bean = new ActBean(reminder.getReminder());
        int reminderCount = bean.getInt("reminderCount");
        return "Reminder Type: " + reminder.getReminderType().getName()
               + "\nReminder Count: " + reminderCount;
    }
}
