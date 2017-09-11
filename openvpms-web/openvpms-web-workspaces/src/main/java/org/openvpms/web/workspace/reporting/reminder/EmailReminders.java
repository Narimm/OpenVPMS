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
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderType;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.report.DocFormats;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.report.ReportContextFactory;
import org.openvpms.web.component.im.report.Reporter;
import org.openvpms.web.component.im.report.ReporterFactory;
import org.openvpms.web.component.mail.EmailTemplateEvaluator;
import org.openvpms.web.workspace.reporting.ReportingException;

import java.util.ArrayList;
import java.util.List;

import static org.openvpms.web.workspace.reporting.ReportingException.ErrorCode.TemplateMissingEmailText;

/**
 * Reminders to be emailed to a customer.
 *
 * @author Tim Anderson
 */
public class EmailReminders extends GroupedReminders {

    /**
     * The email template.
     */
    private final Entity emailTemplate;

    /**
     * The email template evaluator.
     */
    private final EmailTemplateEvaluator evaluator;

    /**
     * The reporter factory.
     */
    private final ReporterFactory factory;

    /**
     * Constructs a {@link EmailReminders}.
     *
     * @param reminders     the reminders to send
     * @param groupBy       the reminder grouping policy. This is used to determine which document template, if any, is
     *                      selected to process reminders.
     * @param cancelled     reminders that have been cancelled
     * @param errors        reminders that are in error
     * @param updated       reminders/reminder items that have been updated
     * @param resend        determines if reminders are being resent
     * @param customer      the customer the reminders are for. May be {@code null} if there are no reminders to send
     * @param contact       the contact to use. May be {@code null} if there are no reminders to send
     * @param location      the practice location. May be {@code null} if there are no reminders to send
     * @param template      the document template to use. May be {@code null} if there are no reminders to send
     * @param emailTemplate the email template to use. May be {@code null} if there are no reminders to send
     * @param evaluator     the template evaluator
     * @param factory       the reporter factory
     */
    public EmailReminders(List<ReminderEvent> reminders, ReminderType.GroupBy groupBy, List<ReminderEvent> cancelled,
                          List<ReminderEvent> errors, List<Act> updated, boolean resend, Party customer,
                          Contact contact, Party location, DocumentTemplate template, Entity emailTemplate,
                          EmailTemplateEvaluator evaluator, ReporterFactory factory) {
        super(reminders, groupBy, cancelled, errors, updated, resend, customer, contact, location, template);
        this.emailTemplate = emailTemplate;
        this.evaluator = evaluator;
        this.factory = factory;
    }

    /**
     * Returns the email address.
     *
     * @return the email address
     */
    public String getEmailAddress() {
        IMObjectBean bean = new IMObjectBean(getContact());
        return bean.getString("emailAddress");
    }

    /**
     * Returns the email subject.
     *
     * @param context the context
     * @return the email subject
     */
    public String getSubject(Context context) {
        List<ReminderEvent> reminders = getReminders();
        Act reminder = reminders.get(0).getReminder();
        String subject = evaluator.getSubject(emailTemplate, reminder, context);
        if (StringUtils.isEmpty(subject)) {
            subject = getTemplate().getName();
        }
        return subject;
    }

    /**
     * Returns the email message.
     *
     * @param context the context
     * @return the email message
     */
    public String getMessage(Context context) {
        String body = null;
        List<ReminderEvent> reminders = getReminders();
        if (reminders.size() == 1) {
            Act reminder = reminders.get(0).getReminder();
            Reporter<IMObject> reporter = evaluator.getMessageReporter(emailTemplate, reminder, context);
            if (reporter != null) {
                body = evaluator.getMessage(reporter);
                if (StringUtils.isEmpty(body)) {
                    throw new ReportingException(TemplateMissingEmailText, getTemplate().getName());
                }
            }
        } else {
            List<ObjectSet> sets = getObjectSets(reminders);
            Reporter<ObjectSet> reporter = evaluator.getMessageReporter(emailTemplate, sets, context);
            if (reporter != null) {
                body = evaluator.getMessage(reporter);
                if (StringUtils.isEmpty(body)) {
                    throw new ReportingException(TemplateMissingEmailText, getTemplate().getName());
                }
            }
        }
        if (body == null) {
            body = evaluator.getMessage(emailTemplate, getCustomer(), context);
        }
        if (StringUtils.isEmpty(body)) {
            throw new ReportingException(TemplateMissingEmailText, getTemplate().getName());
        }
        return body;
    }

    /**
     * Creates an email attachment containing the reminders.
     *
     * @param context the reporting context
     * @return a new report
     */
    public Document createAttachment(Context context) {
        Document result;
        List<ReminderEvent> reminders = getReminders();
        DocumentTemplate template = getTemplate();
        if (reminders.size() > 1) {
            result = getDocument(factory.createObjectSetReporter(getObjectSets(reminders), template), context);
        } else {
            List<Act> acts = new ArrayList<>();
            for (ReminderEvent event : reminders) {
                acts.add(event.getReminder());
            }
            result = getDocument(factory.createIMObjectReporter(acts, template), context);
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
