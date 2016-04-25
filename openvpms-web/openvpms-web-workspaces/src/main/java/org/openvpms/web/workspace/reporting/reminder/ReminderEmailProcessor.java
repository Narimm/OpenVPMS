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

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderProcessorException;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.report.DocFormats;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.im.report.DocumentTemplateLocator;
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
import org.openvpms.web.workspace.reporting.ReportingException;
import org.openvpms.web.workspace.reporting.email.PracticeEmailAddresses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.openvpms.web.workspace.reporting.ReportingException.ErrorCode.FailedToProcessReminder;
import static org.openvpms.web.workspace.reporting.ReportingException.ErrorCode.ReminderMissingDocTemplate;
import static org.openvpms.web.workspace.reporting.ReportingException.ErrorCode.TemplateMissingEmailText;


/**
 * Sends reminder emails.
 *
 * @author Tim Anderson
 */
public class ReminderEmailProcessor extends AbstractReminderProcessor {

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
     * The communication logger. May be {@code null}
     */
    private final ReminderCommunicationLogger logger;

    /**
     * Constructs a {@link ReminderEmailProcessor}.
     *
     * @param factory       the mailer factory
     * @param practice      the practice
     * @param groupTemplate the template for grouped reminders
     * @param context       the context
     * @param logger        if specified, logs email reminders
     */
    public ReminderEmailProcessor(MailerFactory factory, Party practice, DocumentTemplate groupTemplate,
                                  Context context, ReminderCommunicationLogger logger) {
        super(groupTemplate, context);
        this.factory = factory;
        this.logger = logger;
        addresses = new PracticeEmailAddresses(practice, "REMINDER");
        evaluator = ServiceHelper.getBean(EmailTemplateEvaluator.class);
    }

    /**
     * Processes a list of reminder events.
     *
     * @param events           the events
     * @param shortName        the report archetype short name, used to select the document template if none specified
     * @param documentTemplate the document template to use. May be {@code null}
     */
    protected void process(List<ReminderEvent> events, String shortName, DocumentTemplate documentTemplate) {
        DocumentTemplateLocator locator = new ContextDocumentTemplateLocator(documentTemplate, shortName, getContext());
        documentTemplate = locator.getTemplate();
        if (documentTemplate == null) {
            throw new ReportingException(ReminderMissingDocTemplate);
        }
        Entity emailTemplate = documentTemplate.getEmailTemplate();
        if (emailTemplate == null) {
            throw new ReportingException(TemplateMissingEmailText, documentTemplate.getName());
        }
        ReminderEvent event = events.get(0);
        Party patient = event.getPatient();
        Party customer = event.getCustomer();
        Context context = new LocalContext(getContext());
        context.setPatient(patient);
        context.setCustomer(customer);
        Mailer mailer = factory.create(new CustomerMailContext(context));

        try {
            send(customer, event.getContact(), events, documentTemplate, emailTemplate, mailer, context);
        } catch (ArchetypeServiceException | ReportingException | ReminderProcessorException exception) {
            throw exception;
        } catch (Throwable exception) {
            throw new ReportingException(FailedToProcessReminder, exception, exception.getMessage());
        }
        if (logger != null) {
            logger.logEmail(events, getContext().getLocation(), mailer);
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
    protected void send(Party customer, Contact contact, List<ReminderEvent> events, DocumentTemplate reminderTemplate,
                        Entity emailTemplate, Mailer mailer, Context context) {
        String body = null;
        List<ObjectSet> sets = (events.size() > 1) ? createObjectSets(events) : Collections.<ObjectSet>emptyList();
        if (!sets.isEmpty()) {
            Reporter<ObjectSet> reporter = evaluator.getMessageReporter(emailTemplate, sets, context);
            if (reporter != null) {
                body = evaluator.getMessage(reporter);
                if (StringUtils.isEmpty(body)) {
                    throw new ReportingException(TemplateMissingEmailText, reminderTemplate.getName());
                }
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

        Document document = createReport(events, sets, reminderTemplate, context);
        mailer.addAttachment(document);

        mailer.send();
    }

    /**
     * Creates a new report.
     *
     * @param events           the reminder events
     * @param sets             the reminder sets, used for grouped reminders. Empty otherwise
     * @param documentTemplate the document template
     * @param context          the reporting context
     * @return a new report
     */
    private Document createReport(List<ReminderEvent> events, List<ObjectSet> sets, DocumentTemplate documentTemplate,
                                  Context context) {
        Document result;
        if (!sets.isEmpty()) {
            result = getDocument(new ObjectSetReporter(sets, documentTemplate), context);
        } else {
            List<Act> acts = new ArrayList<>();
            for (ReminderEvent event : events) {
                acts.add(event.getReminder());
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
