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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.sms.mail;

import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;


/**
 * Simple representation of an email message to be sent to an email-to-SMS provider.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class MailMessage {

    /**
     * The from address.
     */
    private String from;

    /**
     * The to address.
     */
    private String to;

    /**
     * The mail subject.
     */
    private String subject;

    /**
     * The mail text.
     */
    private String text;

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
     * Sets the mail subject.
     *
     * @param subject the mail subject
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Returns the mail subject.
     *
     * @return the mail subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Sets the mail text.
     *
     * @param text the mail text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Returns the mail text.
     *
     * @return the mail text
     */
    public String getText() {
        return text;
    }

    /**
     * Copies the mail data to a mime message.
     *
     * @param message the message to copy to
     * @throws MessagingException if the data is invalid
     */
    public void copyTo(MimeMessage message) throws MessagingException {
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text);
    }

}
