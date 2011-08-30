/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.app.reporting.reminder;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderProcessorException;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.report.DocFormats;
import org.openvpms.report.IMReport;
import org.openvpms.web.app.reporting.ReportingException;
import static org.openvpms.web.app.reporting.ReportingException.ErrorCode.FailedToProcessReminder;
import static org.openvpms.web.app.reporting.ReportingException.ErrorCode.TemplateMissingEmailText;
import org.openvpms.web.component.im.report.IMObjectReporter;
import org.openvpms.web.component.im.report.ObjectSetReporter;
import org.openvpms.web.system.ServiceHelper;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * Sends reminder emails.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReminderEmailProcessor extends AbstractReminderProcessor {

    /**
     * The mail sender.
     */
    private final JavaMailSender sender;

    /**
     * The email address.
     */
    private final String emailAddress;

    /**
     * The email name.
     */
    private final String emailName;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;


    /**
     * Creates a new <tt>ReminderEmailProcessor</tt>.
     *
     * @param sender        the mail sender
     * @param practice      the practice
     * @param groupTemplate the template for grouped reminders
     */
    public ReminderEmailProcessor(JavaMailSender sender, Party practice, Entity groupTemplate) {
        super(groupTemplate);
        ReminderRules rules = new ReminderRules();
        Contact email = rules.getEmailContact(practice.getContacts());
        if (email == null) {
            throw new ReportingException(ReportingException.ErrorCode.NoReminderContact, practice.getName());
        }
        IMObjectBean bean = new IMObjectBean(email);
        emailAddress = bean.getString("emailAddress");
        if (StringUtils.isEmpty(emailAddress)) {
            throw new ReportingException(ReportingException.ErrorCode.InvalidEmailAddress, emailAddress,
                                         practice.getName());
        }

        emailName = practice.getName();
        this.sender = sender;
        handlers = ServiceHelper.getDocumentHandlers();
    }

    /**
     * Processes a list of reminder events.
     *
     * @param events           the events
     * @param shortName        the report archetype short name, used to select the document template if none specified
     * @param documentTemplate the document template to use. May be <tt>null</tt>
     */
    protected void process(List<ReminderEvent> events, String shortName, Entity documentTemplate) {
        ReminderEvent event = events.get(0);
        Contact contact = event.getContact();

        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setValidateAddresses(true);
            IMObjectBean bean = new IMObjectBean(contact);
            String to = bean.getString("emailAddress");
            helper.setFrom(emailAddress, emailName);
            helper.setTo(to);

            IMObjectBean templateBean = new IMObjectBean(documentTemplate);
            String subject = templateBean.getString("emailSubject");
            if (StringUtils.isEmpty(subject)) {
                subject = documentTemplate.getName();
            }
            String body = templateBean.getString("emailText");
            if (StringUtils.isEmpty(body)) {
                throw new ReportingException(TemplateMissingEmailText, documentTemplate.getName());
            }
            helper.setText(body);

            final Document reminder = createReport(events, shortName, documentTemplate);
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

    private Document createReport(List<ReminderEvent> events, String shortName, Entity documentTemplate) {
        Document result;
        if (events.size() > 1) {
            List<ObjectSet> sets = createObjectSets(events);
            ObjectSetReporter reporter = new ObjectSetReporter(sets, shortName, documentTemplate);
            IMReport<ObjectSet> report = reporter.getReport();
            result = report.generate(sets.iterator(), DocFormats.PDF_TYPE);
        } else {
            List<Act> acts = new ArrayList<Act>();
            for (ReminderEvent event : events) {
                acts.add(event.getReminder());
            }
            IMObjectReporter<Act> reporter = new IMObjectReporter<Act>(acts, shortName, documentTemplate);
            IMReport<Act> report = reporter.getReport();
            result = report.generate(acts.iterator(), DocFormats.PDF_TYPE);
        }
        return result;
    }
}
