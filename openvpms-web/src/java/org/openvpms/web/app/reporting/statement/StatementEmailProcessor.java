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

package org.openvpms.web.app.reporting.statement;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.doc.TemplateHelper;
import org.openvpms.archetype.rules.finance.account.CustomerAccountActTypes;
import org.openvpms.archetype.rules.finance.statement.Statement;
import org.openvpms.archetype.rules.finance.statement.StatementProcessorException;
import static org.openvpms.archetype.rules.finance.statement.StatementProcessorException.ErrorCode.*;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.report.DocFormats;
import org.openvpms.report.IMReport;
import org.openvpms.report.ReportFactory;
import org.openvpms.web.system.ServiceHelper;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.List;


/**
 * Sends statement emails.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class StatementEmailProcessor
        extends AbstractStatementProcessorListener {

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
     * The email subject.
     */
    private final String emailSubject;

    /**
     * The email body.
     */
    private final String emailText;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * The statement document template.
     */
    private Document template;


    /**
     * Constructs a new <tt>StatementEmailProcessor</tt>.
     *
     * @param sender       the mail sender
     * @param emailAddress the email address
     * @param emailName    the email name
     * @throws ArchetypeServiceException   for any archetype service error
     * @throws StatementProcessorException for any statement processor error
     */
    public StatementEmailProcessor(JavaMailSender sender,
                                   String emailAddress, String emailName) {
        this.sender = sender;
        this.emailAddress = emailAddress;
        this.emailName = emailName;
        handlers = ServiceHelper.getDocumentHandlers();
        TemplateHelper helper = new TemplateHelper();
        Entity entity = helper.getTemplateForArchetype(
                CustomerAccountActTypes.OPENING_BALANCE);
        if (entity == null) {
            throw new StatementProcessorException(
                    InvalidConfiguration, "No document template configured");
        }
        template = helper.getDocumentFromTemplate(entity);
        if (template == null) {
            throw new StatementProcessorException(
                    InvalidConfiguration,
                    "No customer statement document template");
        }

        IMObjectBean bean = new IMObjectBean(entity);
        String subject = bean.getString("emailSubject");
        if (StringUtils.isEmpty(subject)) {
            subject = entity.getName();
        }
        emailSubject = subject;

        emailText = bean.getString("emailText");
        if (StringUtils.isEmpty(emailText)) {
            throw new StatementProcessorException(
                    InvalidConfiguration, "Template has no email text");
        }
    }

    /**
     * Processes a statement.
     *
     * @param statement the event to process
     * @throws OpenVPMSException for any error
     */
    public void process(Statement statement) {
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            List<Contact> contacts = statement.getContacts();
            if (contacts.isEmpty()) {
                throw new StatementProcessorException(NoContact,
                                                      statement.getCustomer());
            }
            Contact contact = contacts.get(0);
            IMObjectBean bean = new IMObjectBean(contact);
            if (!bean.isA("contact.email")) {
                throw new StatementProcessorException(NoContact,
                                                      statement.getCustomer());
            }
            String to = bean.getString("emailAddress");
            helper.setFrom(emailAddress, emailName);
            helper.setTo(to);
            helper.setSubject(emailSubject);
            helper.setText(emailText);
            IMReport<IMObject> report = ReportFactory.createIMObjectReport(
                    template, ArchetypeServiceHelper.getArchetypeService(),
                    handlers);
            Iterable<IMObject> objects = getActs(statement);
            final Document doc
                    = report.generate(objects.iterator(),
                                      getParameters(statement),
                                      new String[]{DocFormats.PDF_TYPE});

            final DocumentHandler handler = handlers.get(
                    doc.getName(),
                    doc.getArchetypeId().getShortName(),
                    doc.getMimeType());

            helper.addAttachment(
                    doc.getName(), new InputStreamSource() {
                public InputStream getInputStream() {
                    return handler.getContent(doc);
                }
            });
            sender.send(message);
            if (!statement.isPreview() && !statement.isPrinted()) {
                setPrinted(statement);
            }
        } catch (ArchetypeServiceException exception) {
            throw exception;
        } catch (StatementProcessorException exception) {
            throw exception;
        } catch (Throwable exception) {
            throw new StatementProcessorException(
                    FailedToProcessStatement, exception.getMessage());
        }
    }

    /**
     * Helper to return the statement acts as an Iterable<IMObject>.
     *
     * @param event the statement event
     * @return the statement acts
     */
    @SuppressWarnings("unchecked")
    private Iterable<IMObject> getActs(Statement event) {
        return (Iterable) event.getActs();
    }

}
