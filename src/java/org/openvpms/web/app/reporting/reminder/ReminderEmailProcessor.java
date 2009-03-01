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

import static org.openvpms.web.app.reporting.ReportingException.ErrorCode.FailedToProcessReminder;
import static org.openvpms.web.app.reporting.ReportingException.ErrorCode.TemplateMissingEmailText;

import java.io.InputStream;
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.MimeMessage;

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
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.report.DocFormats;
import org.openvpms.web.app.reporting.ReportingException;
import org.openvpms.web.component.im.doc.ReportGenerator;
import org.openvpms.web.component.processor.ProgressBarProcessor;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;


/**
 * Sends reminder emails.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class ReminderEmailProcessor extends ProgressBarProcessor<ReminderEvent> {

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
     *  The logfile handler
     */
    
    private static final Log log = LogFactory.getLog(ReminderEmailProcessor.class);

    /**
     * Constructs a new <tt>ReminderEmailProcessor</tt>.
     *
     * @param reminders    the reminders
     * @param sender       the mail sender
     * @param emailAddress the email address
     * @param emailName    the email name
     */
    public ReminderEmailProcessor(List<ReminderEvent> reminders,
                                  JavaMailSender sender,
                                  String emailAddress, String emailName) {
        super(reminders, Messages.get("reporting.reminder.run.email"));
        this.sender = sender;
        this.emailAddress = emailAddress;
        this.emailName = emailName;
        handlers = ServiceHelper.getDocumentHandlers();
    }

    /**
     * Invoked to process a reminder.
     *
     * @param event the event
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReminderProcessorException if the event cannot be processed
     * @throws ReportingException         for any other error
     */
    protected void process(ReminderEvent event) {
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setValidateAddresses(true);
            Contact contact = event.getContact();
            IMObjectBean bean = new IMObjectBean(contact);
            String to = bean.getString("emailAddress");
            helper.setFrom(emailAddress, emailName);
            helper.setTo(to);
            Entity documentTemplate = event.getDocumentTemplate();

            IMObjectBean templateBean
                    = new IMObjectBean(documentTemplate);
            String subject = templateBean.getString("emailSubject");
            if (StringUtils.isEmpty(subject)) {
                subject = documentTemplate.getName();
            }
            String body = templateBean.getString("emailText");
            if (StringUtils.isEmpty(body)) {
                throw new ReportingException(TemplateMissingEmailText,
                                             documentTemplate.getName());
            }
            helper.setText(body);
            ReportGenerator generator = new ReportGenerator(documentTemplate);
            final Document reminder = generator.generate(event.getReminder(),
                                                         DocFormats.PDF_TYPE);

            final DocumentHandler handler = handlers.get(
                    reminder.getName(),
                    reminder.getArchetypeId().getShortName(),
                    reminder.getMimeType());

            helper.setSubject(subject);
            helper.addAttachment(
                    reminder.getName(), new InputStreamSource() {
                public InputStream getInputStream() {
                    return handler.getContent(reminder);
                }
            });
            sender.send(message);
            processCompleted(event);
        } catch (AddressException exception) {
        	processCompleted(event);
        	log.warn("Invalid Email Address", exception);
        }
        catch (ArchetypeServiceException exception) {
            throw exception;
        } catch (ReminderProcessorException exception) {
            throw exception;
        } catch (ReportingException exception) {
            throw exception;
        } catch (Throwable exception) {
            throw new ReportingException(FailedToProcessReminder,
                                         exception, exception.getMessage());
        }
    }
}
