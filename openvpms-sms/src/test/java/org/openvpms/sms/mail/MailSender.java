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

import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 * Mock implementation of the <tt>JavaMailSender</tt> interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class MailSender implements JavaMailSender {

    /**
     * The sent messages.
     */
    private List<MimeMessage> sent = new ArrayList<MimeMessage>();

    /**
     * Returns the sent messages.
     *
     * @return the sent messages
     */
    public List<MimeMessage> getSent() {
        return sent;
    }

    /**
     * Create a new JavaMail MimeMessage for the underlying JavaMail Session
     * of this sender. Needs to be called to create MimeMessage instances
     * that can be prepared by the client and passed to send(MimeMessage).
     *
     * @return the new MimeMessage instance
     * @see #send(MimeMessage)
     * @see #send(MimeMessage[])
     */
    public MimeMessage createMimeMessage() {
        return new MimeMessage(Session.getInstance(new Properties()));
    }

    /**
     * Create a new JavaMail MimeMessage for the underlying JavaMail Session
     * of this sender, using the given input stream as the message source.
     *
     * @param contentStream the raw MIME input stream for the message
     * @return the new MimeMessage instance
     * @throws MailParseException in case of message creation failure
     */
    public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
        throw new IllegalStateException("Not implemented");
    }

    /**
     * Send the given JavaMail MIME message.
     * The message needs to have been created with {@link #createMimeMessage()}.
     *
     * @param mimeMessage message to send
     * @throws MailAuthenticationException in case of authentication failure
     * @throws MailSendException           in case of failure when sending the message
     * @see #createMimeMessage
     */
    public void send(MimeMessage mimeMessage) throws MailException {
        sent.add(mimeMessage);
    }

    /**
     * Send the given array of JavaMail MIME messages in batch.
     * The messages need to have been created with {@link #createMimeMessage()}.
     *
     * @param mimeMessages messages to send
     * @throws MailAuthenticationException in case of authentication failure
     * @throws MailSendException           in case of failure when sending a message
     * @see #createMimeMessage
     */
    public void send(MimeMessage[] mimeMessages) throws MailException {
        throw new IllegalStateException("Not implemented");
    }

    /**
     * Send the JavaMail MIME message prepared by the given MimeMessagePreparator.
     * <p>Alternative way to prepare MimeMessage instances, instead of
     * {@link #createMimeMessage()} and {@link #send(MimeMessage)} calls.
     * Takes care of proper exception conversion.
     *
     * @param mimeMessagePreparator the preparator to use
     * @throws MailPreparationException    in case of failure when preparing the message
     * @throws MailParseException          in case of failure when parsing the message
     * @throws MailAuthenticationException in case of authentication failure
     * @throws MailSendException           in case of failure when sending the message
     */
    public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {
        throw new IllegalStateException("Not implemented");
    }

    /**
     * Send the JavaMail MIME messages prepared by the given MimeMessagePreparators.
     * <p>Alternative way to prepare MimeMessage instances, instead of
     * {@link #createMimeMessage()} and {@link #send(MimeMessage[])} calls.
     * Takes care of proper exception conversion.
     *
     * @param mimeMessagePreparators the preparator to use
     * @throws MailPreparationException    in case of failure when preparing a message
     * @throws MailParseException          in case of failure when parsing a message
     * @throws MailAuthenticationException in case of authentication failure
     * @throws MailSendException           in case of failure when sending a message
     */
    public void send(MimeMessagePreparator[] mimeMessagePreparators) throws MailException {
        throw new IllegalStateException("Not implemented");
    }

    /**
     * Send the given simple mail message.
     *
     * @param simpleMessage the message to send
     * @throws MailParseException          in case of failure when parsing the message
     * @throws MailAuthenticationException in case of authentication failure
     * @throws MailSendException           in case of failure when sending the message
     */
    public void send(SimpleMailMessage simpleMessage) throws MailException {
        throw new IllegalStateException("Not implemented");
    }

    /**
     * Send the given array of simple mail messages in batch.
     *
     * @param simpleMessages the messages to send
     * @throws MailParseException          in case of failure when parsing a message
     * @throws MailAuthenticationException in case of authentication failure
     * @throws MailSendException           in case of failure when sending a message
     */
    public void send(SimpleMailMessage[] simpleMessages) throws MailException {
        throw new IllegalStateException("Not implemented");
    }
}
