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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.reporting.reminder;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderProcessorException;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.report.DocFormats;
import org.openvpms.report.IMReport;
import org.openvpms.web.app.reporting.ReportingException;
import static org.openvpms.web.app.reporting.ReportingException.ErrorCode.FailedToProcessReminder;
import static org.openvpms.web.app.reporting.ReportingException.ErrorCode.TemplateMissingEmailText;
import org.openvpms.web.component.im.report.ObjectSetReporter;
import org.openvpms.web.component.im.report.IMObjectReporter;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.AddressException;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;


/**
 * Sends reminder emails.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class ReminderEmailProcessor extends ReminderProgressBarProcessor {

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
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ReminderEmailProcessor.class);

    
    /**
     * Constructs a new <tt>ReminderEmailProcessor</tt>.
     *
     * @param reminders     the reminders
     * @param sender        the mail sender
     * @param emailAddress  the email address
     * @param emailName     the email name
     * @param groupTemplate the grouped reminder document template
     * @param statistics    the statistics
     */
    public ReminderEmailProcessor(List<List<ReminderEvent>> reminders, JavaMailSender sender, String emailAddress,
                                  String emailName, Entity groupTemplate, Statistics statistics) {
        super(reminders, groupTemplate, statistics, Messages.get("reporting.reminder.run.email"));
        this.sender = sender;
        this.emailAddress = emailAddress;
        this.emailName = emailName;
        handlers = ServiceHelper.getDocumentHandlers();
    }

    @Override
    protected void process(List<ReminderEvent> events, String shortName, Entity documentTemplate) {
        ReminderEvent event = events.get(0);
        Contact contact = event.getContact();
        String to = null;

        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setValidateAddresses(true);
            IMObjectBean bean = new IMObjectBean(contact);
            to = bean.getString("emailAddress");
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
            processCompleted(events);
        } catch (AddressException exception) {
            processCompleted(events);
            Party party = contact.getParty();
            String customer = null;
            if (party != null) {
                customer = "id=" + party.getId() + ", name=" + party.getName();
            }

            log.warn("Invalid email address for customer " + customer + ": " + to, exception);
        }
        catch (ArchetypeServiceException exception) {
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
