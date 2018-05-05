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

import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderConfiguration;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderProcessorException;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderType;
import org.openvpms.archetype.rules.patient.reminder.ReminderTypes;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.report.ReporterFactory;
import org.openvpms.web.component.mail.EmailAddress;
import org.openvpms.web.component.mail.EmailTemplateEvaluator;
import org.openvpms.web.component.mail.Mailer;
import org.openvpms.web.component.mail.MailerFactory;
import org.openvpms.web.workspace.customer.CustomerMailContext;
import org.openvpms.web.workspace.customer.communication.CommunicationHelper;
import org.openvpms.web.workspace.customer.communication.CommunicationLogger;
import org.openvpms.web.workspace.reporting.ReportingException;
import org.openvpms.web.workspace.reporting.email.PracticeEmailAddresses;

import java.util.List;

import static org.openvpms.web.workspace.reporting.ReportingException.ErrorCode.FailedToProcessReminder;
import static org.openvpms.web.workspace.reporting.ReportingException.ErrorCode.TemplateMissingEmailText;


/**
 * Sends reminder emails.
 *
 * @author Tim Anderson
 */
public class ReminderEmailProcessor extends GroupedReminderProcessor {

    /**
     * The mailer factory.
     */
    private final MailerFactory factory;

    /**
     * The practice email addresses.
     */
    private final PracticeEmailAddresses addresses;

    /**
     * The email template evaluator.
     */
    private final EmailTemplateEvaluator evaluator;

    /**
     * The reporter factory.
     */
    private final ReporterFactory reporterFactory;

    /**
     * Constructs a {@link ReminderEmailProcessor}.
     *
     * @param factory         the mailer factory
     * @param evaluator       the email template evaluator
     * @param reporterFactory the reporter factory
     * @param reminderTypes   the reminder types
     * @param practice        the practice
     * @param reminderRules   the reminder rules
     * @param patientRules    the patient rules
     * @param practiceRules   the practice rules
     * @param service         the archetype service
     * @param config          the reminder configuration
     * @param logger          the communication logger. May be {@code null}
     */
    public ReminderEmailProcessor(MailerFactory factory, EmailTemplateEvaluator evaluator,
                                  ReporterFactory reporterFactory, ReminderTypes reminderTypes,
                                  Party practice, ReminderRules reminderRules, PatientRules patientRules,
                                  PracticeRules practiceRules, IArchetypeService service, ReminderConfiguration config,
                                  CommunicationLogger logger) {
        super(reminderTypes, reminderRules, patientRules, practice, service, config, logger);
        this.factory = factory;
        this.evaluator = evaluator;
        this.reporterFactory = reporterFactory;
        addresses = new PracticeEmailAddresses(practice, "REMINDER", practiceRules, service);
    }

    /**
     * Returns the reminder item archetype that this processes.
     *
     * @return the archetype
     */
    @Override
    public String getArchetype() {
        return ReminderArchetypes.EMAIL_REMINDER;
    }

    /**
     * Determines if reminder processing is performed asynchronously.
     *
     * @return {@code true} if reminder processing is performed asynchronously
     */
    @Override
    public boolean isAsynchronous() {
        return false;
    }

    /**
     * Processes reminders.
     *
     * @param reminders the reminder state
     */
    @Override
    public void process(PatientReminders reminders) {
        EmailReminders reminderState = (EmailReminders) reminders;
        Mailer mailer;

        try {
            mailer = send(reminderState);
        } catch (ArchetypeServiceException | ReportingException | ReminderProcessorException exception) {
            throw exception;
        } catch (Throwable exception) {
            throw new ReportingException(FailedToProcessReminder, exception, exception.getMessage());
        }
        if (getLogger() != null) {
            String from = mailer.getFrom();
            String[] to = mailer.getTo();
            String[] cc = mailer.getCc();
            String[] bcc = mailer.getBcc();
            String subject = mailer.getSubject();
            String body = mailer.getBody();
            String attachments = CommunicationHelper.getAttachments(mailer.getAttachments());
            for (ReminderEvent reminder : reminderState.getReminders()) {
                reminder.set("from", from);
                reminder.set("to", to);
                reminder.set("cc", cc);
                reminder.set("bcc", bcc);
                reminder.set("subject", subject);
                reminder.set("body", body);
                reminder.set("attachments", attachments);
            }
        }
    }

    /**
     * Prepares reminders for processing.
     *
     * @param reminders the reminders
     * @param groupBy   the reminder grouping policy. This determines which document template is selected
     * @param cancelled reminder items that will be cancelled
     * @param errors    reminders that can't be processed due to error
     * @param updated   acts that need to be saved on completion
     * @param resend    if {@code true}, reminders are being resent
     * @param customer  the customer, or {@code null} if there are no reminders to send
     * @param contact   the contact,  or {@code null} if there are no reminders to send
     * @param location  the practice location, or {@code null} if there are no reminders to send
     * @param template  the document template, or {@code null} if there are no reminders to send
     * @return the reminders to process
     * @throws ReportingException if the reminders cannot be prepared
     */
    @Override
    protected EmailReminders prepare(List<ReminderEvent> reminders, ReminderType.GroupBy groupBy,
                                     List<ReminderEvent> cancelled, List<ReminderEvent> errors, List<Act> updated,
                                     boolean resend, Party customer, Contact contact, Party location,
                                     DocumentTemplate template) {

        Entity emailTemplate = null;
        if (template != null && !reminders.isEmpty()) {
            emailTemplate = template.getEmailTemplate();
            if (emailTemplate == null) {
                throw new ReportingException(TemplateMissingEmailText, template.getName());
            }
        }
        return new EmailReminders(reminders, groupBy, cancelled, errors, updated, resend, customer, contact, location,
                                  template, emailTemplate, evaluator, reporterFactory);
    }

    /**
     * Returns the contact archetype.
     *
     * @return the contact archetype
     */
    @Override
    protected String getContactArchetype() {
        return ContactArchetypes.EMAIL;
    }

    /**
     * Logs reminder communications.
     *
     * @param state  the reminder state
     * @param logger the communication logger
     */
    @Override
    protected void log(PatientReminders state, CommunicationLogger logger) {
        Party location = ((EmailReminders) state).getLocation();
        for (ReminderEvent event : state.getReminders()) {
            String notes = getNote(event);
            String from = event.getString("from");
            String[] to = (String[]) event.get("to");
            String[] cc = (String[]) event.get("cc");
            String[] bcc = (String[]) event.get("bcc");
            String subject = event.getString("subject");
            String body = event.getString("body");
            String attachments = event.getString("attachments");

            logger.logEmail(event.getCustomer(), event.getPatient(), from, to, cc, bcc, subject, COMMUNICATION_REASON,
                            body, notes, attachments, location);
        }
    }

    /**
     * Emails reminders.
     *
     * @param reminders the reminders to email
     */
    protected Mailer send(EmailReminders reminders) {
        ReminderEvent event = reminders.getReminders().get(0);
        Context context = createContext(event, reminders.getLocation());
        Mailer mailer = factory.create(new CustomerMailContext(context));
        String body = reminders.getMessage(context);
        String to = reminders.getEmailAddress();

        EmailAddress from = addresses.getAddress(reminders.getCustomer());
        mailer.setFrom(from.toString(true));
        mailer.setTo(new String[]{to});

        String subject = reminders.getSubject(context);
        mailer.setSubject(subject);
        mailer.setBody(body);

        ReminderConfiguration config = getConfig();
        if (config.getEmailAttachments()) {
            Document document = reminders.createAttachment(context);
            mailer.addAttachment(document);
        }

        mailer.send();
        return mailer;
    }

}
