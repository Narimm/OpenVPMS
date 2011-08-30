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

package org.openvpms.web.component.im.mail;

import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.report.Reporter;
import org.openvpms.web.component.mail.Mailer;
import org.openvpms.web.system.ServiceHelper;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.MimeMessage;
import java.io.InputStream;


/**
 * Abstract implementation of the {@link Mailer} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractIMMailer<T> implements Mailer {

    /**
     * The mail sender.
     */
    private final JavaMailSender sender;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * The from address.
     */
    private String from;

    /**
     * The from name.
     */
    private String fromName;

    /**
     * The to-address.
     */
    private String to;

    /**
     * The email subject.
     */
    private String subject;

    /**
     * The email body.
     */
    private String body;

    /**
     * The reporter.
     */
    private final Reporter<T> reporter;

    /**
     * Constructs a new <tt>AbstractIMMailer</tt>.
     *
     * @param reporter reporter
     */
    public AbstractIMMailer(Reporter<T> reporter) {
        this.reporter = reporter;
        sender = ServiceHelper.getMailSender();
        handlers = ServiceHelper.getDocumentHandlers();
    }

    /**
     * Sets the from address.
     *
     * @param from the from address
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * Returns the from address.
     *
     * @return the from address
     */
    public String getFrom() {
        return from;
    }

    /**
     * The from name.
     *
     * @param name the from name
     */
    public void setFromName(String name) {
        fromName = name;
    }

    /**
     * Returns the from name.
     *
     * @return the from name
     */
    public String getFromName() {
        return fromName;
    }

    /**
     * Sets the to address.
     *
     * @param to the to address
     */
    public void setTo(String to) {
        this.to = to;
    }

    /**
     * Returns the to address.
     *
     * @return the to address
     */
    public String getTo() {
        return to;
    }

    /**
     * Sets the subject.
     *
     * @param subject the subject
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Returns the subject.
     *
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Sets the body.
     *
     * @param body the body
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Returns the body.
     *
     * @return the body
     */
    public String getBody() {
        return body;
    }

    public void send() {
        send(to);
    }

    public void send(String address) {
        MimeMessage message = sender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from, fromName);
            helper.setTo(address);
            helper.setSubject(subject);
            helper.setText(body);
            final Document doc = reporter.getDocument();
            final DocumentHandler handler = handlers.get(
                    doc.getName(), doc.getArchetypeId().getShortName(),
                    doc.getMimeType());
            helper.addAttachment(
                    doc.getName(), new InputStreamSource() {
                public InputStream getInputStream() {
                    return handler.getContent(doc);
                }
            });
            sender.send(message);
        } catch (OpenVPMSException exception) {
            throw exception;
        } catch (Throwable exception) {
            throw new MailException(MailException.ErrorCode.FailedToSend,
                                    to, exception.getMessage());
        }
    }

    protected Reporter<T> getReporter() {
        return reporter;
    }

}
