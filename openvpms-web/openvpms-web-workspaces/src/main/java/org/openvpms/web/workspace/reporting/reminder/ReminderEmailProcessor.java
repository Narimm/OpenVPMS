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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.reminder;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderProcessorException;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.report.DocFormats;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.im.report.DocumentTemplateLocator;
import org.openvpms.web.component.im.report.IMObjectReporter;
import org.openvpms.web.component.im.report.ObjectSetReporter;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.reporting.ReportingException;
import org.openvpms.web.workspace.reporting.email.EmailAddress;
import org.openvpms.web.workspace.reporting.email.PracticeEmailAddresses;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.ArrayList;
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
     * The mail sender.
     */
    private final JavaMailSender sender;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * The practice email addresses.
     */
    private final PracticeEmailAddresses addresses;

    /**
     * Constructs a {@code ReminderEmailProcessor}.
     *
     * @param sender        the mail sender
     * @param practice      the practice
     * @param groupTemplate the template for grouped reminders
     * @param context       the context
     */
    public ReminderEmailProcessor(JavaMailSender sender, Party practice, DocumentTemplate groupTemplate,
                                  Context context) {
        super(groupTemplate, context);
        this.sender = sender;
        handlers = ServiceHelper.getDocumentHandlers();

        addresses = new PracticeEmailAddresses(practice, "REMINDER");
    }

    /**
     * Processes a list of reminder events.
     *
     * @param events           the events
     * @param shortName        the report archetype short name, used to select the document template if none specified
     * @param documentTemplate the document template to use. May be {@code null}
     */
    protected void process(List<ReminderEvent> events, String shortName, DocumentTemplate documentTemplate) {
        ReminderEvent event = events.get(0);
        Contact contact = event.getContact();
        DocumentTemplateLocator locator = new ContextDocumentTemplateLocator(documentTemplate, shortName, getContext());
        documentTemplate = locator.getTemplate();
        if (documentTemplate == null) {
            throw new ReportingException(ReminderMissingDocTemplate);
        }

        try {
            EmailAddress from = addresses.getAddress(event.getCustomer());
            IMObjectBean bean = new IMObjectBean(contact);
            String to = bean.getString("emailAddress");

            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setValidateAddresses(true);
            helper.setFrom(from.getAddress(), from.getName());
            helper.setTo(to);

            String subject = documentTemplate.getEmailSubject();
            if (StringUtils.isEmpty(subject)) {
                subject = documentTemplate.getName();
            }
            String body = documentTemplate.getEmailText();
            if (StringUtils.isEmpty(body)) {
                throw new ReportingException(TemplateMissingEmailText, documentTemplate.getName());
            }
            helper.setText(body);

            final Document reminder = createReport(events, documentTemplate);
            final DocumentHandler handler = handlers.get(reminder.getName(), reminder.getArchetypeId().getShortName(),
                                                         reminder.getMimeType());

            helper.setSubject(subject);
            helper.addAttachment(reminder.getName(), new InputStreamSource() {
                public InputStream getInputStream() {
                    return handler.getContent(reminder);
                }
            });
            sender.send(message);
        } catch (ArchetypeServiceException exception) {
            throw exception;
        } catch (ReminderProcessorException exception) {
            throw exception;
        } catch (ReportingException exception) {
            throw exception;
        } catch (Throwable exception) {
            throw new ReportingException(FailedToProcessReminder, exception, exception.getMessage());
        }
    }

    /**
     * Creates a new report.
     *
     * @param events           the reminder events
     * @param documentTemplate the document template
     * @return a new report
     */
    private Document createReport(List<ReminderEvent> events, DocumentTemplate documentTemplate) {
        Document result;
        if (events.size() > 1) {
            List<ObjectSet> sets = createObjectSets(events);
            ObjectSetReporter reporter = new ObjectSetReporter(sets, documentTemplate);
            result = reporter.getDocument(DocFormats.PDF_TYPE, true);
        } else {
            List<Act> acts = new ArrayList<Act>();
            for (ReminderEvent event : events) {
                acts.add(event.getReminder());
            }
            IMObjectReporter<Act> reporter = new IMObjectReporter<Act>(acts, documentTemplate);
            result = reporter.getDocument(DocFormats.PDF_TYPE, true);
        }
        return result;
    }


}
