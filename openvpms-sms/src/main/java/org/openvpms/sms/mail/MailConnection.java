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

import org.openvpms.sms.Connection;
import org.openvpms.sms.SMSException;
import org.openvpms.sms.i18n.SMSMessages;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;


/**
 * A {@link Connection} that sends SMS messages via an email-to-SMS gateway.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class MailConnection implements Connection {

    /**
     * The mail sender.
     */
    private JavaMailSender sender;

    /**
     * The message factory.
     */
    private MailMessageFactory factory;


    /**
     * Constructs a <tt>MailConnection</tt>.
     *
     * @param sender  the mail sender
     * @param factory the message factory
     */
    public MailConnection(JavaMailSender sender, MailMessageFactory factory) {
        this.sender = sender;
        this.factory = factory;
    }

    /**
     * Sends an SMS.
     *
     * @param phone the phone number to send the SMS to
     * @param text  the SMS text
     * @throws SMSException          if the send fails
     * @throws IllegalStateException if the connection is closed
     */
    public synchronized void send(String phone, String text) {
        if (factory == null) {
            throw new IllegalStateException("Connection is closed");
        }
        MailMessage message = factory.createMessage(phone, text);
        MimeMessage mime = sender.createMimeMessage();
        try {
            message.copyTo(mime);
        } catch (MessagingException exception) {
            throw new SMSException(SMSMessages.failedToCreateEmail(exception.getLocalizedMessage()), exception);
        }
        sender.send(mime);
    }

    /**
     * Closes the connection, freeing resources.
     * <p/>
     * May be invoked multiple times.
     */
    public synchronized void close() {
        factory = null;
        sender = null;
    }
}
