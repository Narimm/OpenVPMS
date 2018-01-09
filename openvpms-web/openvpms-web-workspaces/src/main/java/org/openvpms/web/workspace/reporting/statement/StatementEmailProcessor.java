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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.statement;

import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.doc.TemplateHelper;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.statement.Statement;
import org.openvpms.archetype.rules.finance.statement.StatementProcessorException;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.report.DocFormats;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.report.ReportContextFactory;
import org.openvpms.web.component.im.report.Reporter;
import org.openvpms.web.component.im.report.ReporterFactory;
import org.openvpms.web.component.im.report.TemplatedReporter;
import org.openvpms.web.component.mail.EmailAddress;
import org.openvpms.web.component.mail.EmailTemplateEvaluator;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.reporting.email.PracticeEmailAddresses;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.List;

import static org.openvpms.archetype.rules.finance.statement.StatementProcessorException.ErrorCode.FailedToProcessStatement;
import static org.openvpms.archetype.rules.finance.statement.StatementProcessorException.ErrorCode.InvalidConfiguration;
import static org.openvpms.archetype.rules.finance.statement.StatementProcessorException.ErrorCode.NoContact;


/**
 * Sends statement emails.
 *
 * @author Tim Anderson
 */
public class StatementEmailProcessor extends AbstractStatementProcessorListener {

    /**
     * The mail sender.
     */
    private final JavaMailSender sender;

    /**
     * The email template evaluator.
     */
    private final EmailTemplateEvaluator evaluator;

    /**
     * The reporter factory.
     */
    private final ReporterFactory factory;

    /**
     * The practice email addresses.
     */
    private final PracticeEmailAddresses addresses;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * The statement document template.
     */
    private DocumentTemplate statementTemplate;

    /**
     * The statement email template.
     */
    private Entity emailTemplate;

    /**
     * Report context.
     */
    private final Context context;


    /**
     * Constructs a {@link StatementEmailProcessor}.
     *
     * @param sender    the mail sender
     * @param evaluator the email template evaluator
     * @param factory   the reporter factory
     * @param practice  the practice
     * @param context   the context
     * @throws ArchetypeServiceException   for any archetype service error
     * @throws StatementProcessorException for any statement processor error
     */
    public StatementEmailProcessor(JavaMailSender sender, EmailTemplateEvaluator evaluator, ReporterFactory factory,
                                   Party practice, Context context) {
        super(practice);
        this.sender = sender;
        this.evaluator = evaluator;
        this.factory = factory;
        this.context = context;
        addresses = new PracticeEmailAddresses(practice, "BILLING");
        handlers = ServiceHelper.getDocumentHandlers();
        TemplateHelper helper = new TemplateHelper(ServiceHelper.getArchetypeService());
        Entity entity = helper.getTemplateForArchetype(CustomerAccountArchetypes.OPENING_BALANCE);
        if (entity == null) {
            throw new StatementProcessorException(InvalidConfiguration, "No document template configured");
        }
        statementTemplate = new DocumentTemplate(entity, ServiceHelper.getArchetypeService());
        emailTemplate = statementTemplate.getEmailTemplate();
        if (emailTemplate == null) {
            throw new StatementProcessorException(InvalidConfiguration, "No email document template configured");
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
                throw new StatementProcessorException(NoContact, statement.getCustomer());
            }
            Contact contact = contacts.get(0);
            IMObjectBean bean = new IMObjectBean(contact);
            if (!bean.isA(ContactArchetypes.EMAIL)) {
                throw new StatementProcessorException(NoContact, statement.getCustomer());
            }
            String to = bean.getString("emailAddress");
            EmailAddress email = addresses.getAddress(statement.getCustomer());
            helper.setFrom(email.getAddress(), email.getName());
            helper.setTo(to);
            Context local = new LocalContext();
            local.setCustomer(statement.getCustomer());
            local.setLocation(context.getLocation());
            local.setPractice(context.getPractice());
            local.setUser(context.getUser());
            String subject = evaluator.getSubject(emailTemplate, statement.getCustomer(), context);
            String text = evaluator.getMessage(emailTemplate, statement.getCustomer(), local);
            helper.setSubject(subject);
            helper.setText(text, true);
            Iterable<IMObject> objects = getActs(statement);
            Reporter reporter = factory.create(objects, statementTemplate, TemplatedReporter.class);
            reporter.setParameters(getParameters(statement));
            reporter.setFields(ReportContextFactory.create(local));
            final Document doc = reporter.getDocument(DocFormats.PDF_TYPE, true);

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
        } catch (ArchetypeServiceException | StatementProcessorException exception) {
            throw exception;
        } catch (Throwable exception) {
            throw new StatementProcessorException(exception, FailedToProcessStatement, exception.getMessage());
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
