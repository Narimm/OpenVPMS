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

package org.openvpms.web.workspace.customer.communication;

import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.mail.DefaultMailer;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.mail.Mailer;
import org.openvpms.web.component.service.MailService;
import org.openvpms.web.workspace.customer.CustomerMailContext;

import java.util.List;

/**
 * An {@link Mailer} the creates <em>act.customerCommunication</em> acts for emails involving customers.
 *
 * @author Tim Anderson
 */
public class LoggingMailer implements Mailer {

    /**
     * The mailer.
     */
    private final Mailer mailer;

    /**
     * The communication logger.
     */
    private final CommunicationLogger logger;


    /**
     * Constructs a {@link LoggingMailer}.
     *
     * @param context  the mail context
     * @param service  the mail service
     * @param handlers the document handlers
     * @param logger   the communication logger
     */
    public LoggingMailer(MailContext context, MailService service, DocumentHandlers handlers,
                         CommunicationLogger logger) {
        mailer = new DefaultMailer(context, service, handlers);
        this.logger = logger;
    }

    /**
     * Returns the mail context.
     *
     * @return the mail context
     */
    @Override
    public MailContext getContext() {
        return mailer.getContext();
    }

    /**
     * Sets the from address.
     *
     * @param from the from address
     */
    @Override
    public void setFrom(String from) {
        mailer.setFrom(from);
    }

    /**
     * Returns the from address.
     *
     * @return the from address
     */
    @Override
    public String getFrom() {
        return mailer.getFrom();
    }

    /**
     * Sets the to address.
     *
     * @param to the to addresses. May be {@code null}
     */
    @Override
    public void setTo(String[] to) {
        mailer.setTo(to);
    }

    /**
     * Returns the to addresses.
     *
     * @return the to addresses. May be {@code null}
     */
    @Override
    public String[] getTo() {
        return mailer.getTo();
    }

    /**
     * Sets the CC addresses.
     *
     * @param cc the CC addresses. May be {@code null}
     */
    @Override
    public void setCc(String[] cc) {
        mailer.setCc(cc);
    }

    /**
     * Returns the CC addresses.
     *
     * @return the CC addresses. May be {@code null}
     */
    @Override
    public String[] getCc() {
        return mailer.getCc();
    }

    /**
     * Sets the BCC addresses.
     *
     * @param bcc the BCC addresses. May be {@code null}
     */
    @Override
    public void setBcc(String[] bcc) {
        mailer.setBcc(bcc);
    }

    /**
     * Returns the BCC addresses.
     *
     * @return the BCC addresses. May be {@code null}
     */
    @Override
    public String[] getBcc() {
        return mailer.getBcc();
    }

    /**
     * Sets the subject.
     *
     * @param subject the subject
     */
    @Override
    public void setSubject(String subject) {
        mailer.setSubject(subject);
    }

    /**
     * Returns the subject.
     *
     * @return the subject
     */
    @Override
    public String getSubject() {
        return mailer.getSubject();
    }

    /**
     * Sets the body.
     *
     * @param body the body
     */
    @Override
    public void setBody(String body) {
        mailer.setBody(body);
    }

    /**
     * Returns the body.
     *
     * @return the body
     */
    @Override
    public String getBody() {
        return mailer.getBody();
    }

    /**
     * Adds an attachment.
     *
     * @param document the document to attach
     */
    @Override
    public void addAttachment(Document document) {
        mailer.addAttachment(document);
    }

    /**
     * Returns the attachments.
     *
     * @return the attachments
     */
    @Override
    public List<Document> getAttachments() {
        return mailer.getAttachments();
    }

    /**
     * Sends the mail.
     *
     * @throws OpenVPMSException for any error
     */
    @Override
    public void send() {
        mailer.send();
        if (getContext() instanceof CustomerMailContext) {
            CustomerMailContext context = (CustomerMailContext) getContext();
            log(context, mailer, logger);
        }
    }

    /**
     * Returns the mailer.
     *
     * @return the mailer
     */
    protected Mailer getMailer() {
        return mailer;
    }

    /**
     * Returns the logger.
     *
     * @return the logger
     */
    protected CommunicationLogger getLogger() {
        return logger;
    }

    /**
     * Logs an email to a customer.
     *
     * @param context the mail context
     * @param mailer  the mailer
     * @param logger  the logger
     */
    protected void log(CustomerMailContext context, Mailer mailer, CommunicationLogger logger) {
        Party customer = context.getCustomer();
        Party location = context.getLocation();
        if (customer != null) {
            String attachments = CommunicationHelper.getAttachments(mailer.getAttachments());
            String reason = (mailer.getAttachments().isEmpty()) ? "AD_HOC_EMAIL" : "FORWARDED_DOCUMENT";

            logger.logEmail(customer, context.getPatient(), mailer.getFrom(), mailer.getTo(), mailer.getCc(),
                            mailer.getBcc(), mailer.getSubject(), reason, mailer.getBody(), null, attachments,
                            location);
        }
    }

}
