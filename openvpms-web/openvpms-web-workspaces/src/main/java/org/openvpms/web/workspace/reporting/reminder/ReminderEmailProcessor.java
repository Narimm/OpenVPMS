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

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderConfiguration;
import org.openvpms.archetype.rules.patient.reminder.ReminderProcessorException;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderTypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.report.DocFormats;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.report.IMObjectReporter;
import org.openvpms.web.component.im.report.ObjectSetReporter;
import org.openvpms.web.component.im.report.ReportContextFactory;
import org.openvpms.web.component.im.report.Reporter;
import org.openvpms.web.component.mail.EmailAddress;
import org.openvpms.web.component.mail.EmailTemplateEvaluator;
import org.openvpms.web.component.mail.Mailer;
import org.openvpms.web.component.mail.MailerFactory;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.CustomerMailContext;
import org.openvpms.web.workspace.customer.communication.CommunicationHelper;
import org.openvpms.web.workspace.customer.communication.CommunicationLogger;
import org.openvpms.web.workspace.reporting.ReportingException;
import org.openvpms.web.workspace.reporting.email.PracticeEmailAddresses;

import java.util.ArrayList;
import java.util.Date;
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
     * The context.
     */
    private final Context context;

    /**
     * Constructs a {@link ReminderEmailProcessor}.
     *
     * @param factory       the mailer factory
     * @param groupTemplate the grouped reminder template
     * @param reminderTypes the reminder types
     * @param rules         the reminder rules
     * @param practice      the practice
     * @param service       the archetype service
     * @param config        the reminder configuration
     * @param logger        the communication logger. May be {@code null}
     * @param context       the context
     */
    public ReminderEmailProcessor(MailerFactory factory, DocumentTemplate groupTemplate,
                                  ReminderTypes reminderTypes, ReminderRules rules, Party practice,
                                  IArchetypeService service, ReminderConfiguration config, CommunicationLogger logger,
                                  Context context) {
        super(groupTemplate, reminderTypes, rules, practice, service, config, logger);
        this.factory = factory;
        this.context = context;
        addresses = new PracticeEmailAddresses(practice, "REMINDER");
        evaluator = ServiceHelper.getBean(EmailTemplateEvaluator.class);
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
     * Returns the contact archetype.
     *
     * @return the contact archetype
     */
    @Override
    protected String getContactArchetype() {
        return ContactArchetypes.EMAIL;
    }

    /**
     * Returns the date from which a reminder item should be cancelled.
     *
     * @param startTime the item start time
     * @param config    the reminder configuration
     * @return the date when the item should be cancelled
     */
    @Override
    protected Date getCancelDate(Date startTime, ReminderConfiguration config) {
        return config.getEmailCancelDate(startTime);
    }

    /**
     * Processes a list of reminder events.
     *
     * @param contact   the contact to send to
     * @param reminders the reminders
     * @param template  the document template to use
     */
    protected void process(Contact contact, List<ObjectSet> reminders, DocumentTemplate template) {
        Entity emailTemplate = template.getEmailTemplate();
        if (emailTemplate == null) {
            throw new ReportingException(TemplateMissingEmailText, template.getName());
        }
        ObjectSet event = reminders.get(0);
        Context context = createContext(event);
        Party customer = getCustomer(event);
        Mailer mailer = factory.create(new CustomerMailContext(context));

        try {
            send(customer, contact, reminders, template, emailTemplate, mailer, context);
        } catch (ArchetypeServiceException | ReportingException | ReminderProcessorException exception) {
            throw exception;
        } catch (Throwable exception) {
            throw new ReportingException(FailedToProcessReminder, exception, exception.getMessage());
        }
        if (getLogger() != null) {
            String[] to = mailer.getTo();
            String[] cc = mailer.getCc();
            String[] bcc = mailer.getBcc();
            String subject = mailer.getSubject();
            String body = mailer.getBody();
            String attachments = CommunicationHelper.getAttachments(mailer.getAttachments());
            for (ObjectSet reminder : reminders) {
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
     * Logs reminder communications.
     *
     * @param state  the reminder state
     * @param logger the communication logger
     */
    @Override
    protected void log(State state, CommunicationLogger logger) {
        for (ObjectSet set : state.getReminders()) {
            String notes = getNote(set);
            String[] to = (String[]) set.get("to");
            String[] cc = (String[]) set.get("cc");
            String[] bcc = (String[]) set.get("bcc");
            String subject = set.getString("subject");
            String body = set.getString("body");
            String attachments = set.getString("attachments");

            logger.logEmail(getCustomer(set), getPatient(set), to, cc, bcc, subject, COMMUNICATION_REASON, body,
                            notes, attachments, context.getLocation());
        }
    }

    /**
     * Emails reminders.
     *
     * @param customer         the customer
     * @param contact          the email contact
     * @param events           the events
     * @param reminderTemplate the reminder document template
     * @param emailTemplate    the email template
     * @param mailer           the mailer the mailer to user
     * @param context          the reporting context
     */
    protected void send(Party customer, Contact contact, List<ObjectSet> events, DocumentTemplate reminderTemplate,
                        Entity emailTemplate, Mailer mailer, Context context) {
        String body = null;
        Reporter<ObjectSet> reporter = evaluator.getMessageReporter(emailTemplate, events, context);
        if (reporter != null) {
            body = evaluator.getMessage(reporter);
            if (StringUtils.isEmpty(body)) {
                throw new ReportingException(TemplateMissingEmailText, reminderTemplate.getName());
            }
        }
        if (body == null) {
            body = evaluator.getMessage(emailTemplate, customer, context);
        }
        if (StringUtils.isEmpty(body)) {
            throw new ReportingException(TemplateMissingEmailText, reminderTemplate.getName());
        }
        IMObjectBean bean = new IMObjectBean(contact);
        String to = bean.getString("emailAddress");

        EmailAddress from = addresses.getAddress(customer);
        mailer.setFrom(from.toString(true));
        mailer.setTo(new String[]{to});

        String subject = evaluator.getSubject(emailTemplate, customer, context);
        if (StringUtils.isEmpty(subject)) {
            subject = reminderTemplate.getName();
        }
        mailer.setSubject(subject);
        mailer.setBody(body);

        ReminderConfiguration config = getConfig();
        if (config.getEmailAttachments()) {
            Document document = createReport(events, reminderTemplate, context);
            mailer.addAttachment(document);
        }

        mailer.send();
    }

    /**
     * Creates a new report.
     *
     * @param sets             the reminder sets, used for grouped reminders. Empty otherwise
     * @param documentTemplate the document template
     * @param context          the reporting context
     * @return a new report
     */
    private Document createReport(List<ObjectSet> sets, DocumentTemplate documentTemplate, Context context) {
        Document result;
        if (!sets.isEmpty()) {
            result = getDocument(new ObjectSetReporter(sets, documentTemplate), context);
        } else {
            List<Act> acts = new ArrayList<>();
            for (ObjectSet set : sets) {
                acts.add((Act) set.get("reminder"));
            }
            result = getDocument(new IMObjectReporter<>(acts, documentTemplate), context);
        }
        return result;
    }

    /**
     * Generates the reminder document to email.
     *
     * @param reporter the document generator
     * @return the generated document
     */
    private Document getDocument(Reporter<?> reporter, Context context) {
        reporter.setFields(ReportContextFactory.create(context));
        return reporter.getDocument(DocFormats.PDF_TYPE, true);
    }
}
