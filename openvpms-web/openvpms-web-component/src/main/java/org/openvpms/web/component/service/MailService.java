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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.service;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.practice.MailServer;
import org.openvpms.web.component.app.Context;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessagePreparator;

import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.Properties;


/**
 * Mail service that configures the SMTP details from <em>party.organisationLocation</em> from
 * {@link Context#getLocation()}, if available.
 * <p/>
 *
 * @author Tim Anderson
 */
public abstract class MailService implements JavaMailSender {

    /**
     * The sender to delegate to.
     */
    private JavaMailSender sender;

    /**
     * The settings used to configure the sender.
     */
    private MailServer settings;

    /**
     * Used to turn on JavaMail debugging.
     */
    private boolean debug = false;

    /**
     * Used to turn on protocol authentication commands in the reference implementation of JavaMail.
     */
    private boolean debugAuth = false;

    /**
     * The connection timeout, or {@code 0} to use the default timeout.
     */
    private long connectionTimout = 0;

    /**
     * The timeout, or {@code 0} to use the default timeout.
     */
    private long timeout = 0;

    /**
     * Property name for STARTTLS flag.
     */
    private static final String MAIL_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";

    /**
     * Property name for the connection timeout.
     */
    private static final String NAIL_SMTP_CONNECTION_TIMEOUT = "mail.smtp.connectiontimeout";

    /**
     * Property name for the timeout.
     */
    private static final String NAIL_SMTP_TIMEOUT = "mail.smtp.timeout";

    /**
     * Property name for authentication flag.
     */
    private static final String MAIL_SMTP_AUTH = "mail.smtp.auth";

    /**
     * Property name for the debug flag.
     */
    private static final String MAIL_DEBUG = "mail.debug";

    /**
     * Property name for the authentication debug flag.
     */
    private static final String MAIL_DEBUG_AUTH = "mail.debug.auth";

    /**
     * Create a new JavaMail MimeMessage for the underlying JavaMail Session
     * of this sender. Needs to be called to create MimeMessage instances
     * that can be prepared by the client and passed to send(MimeMessage).
     *
     * @return the new MimeMessage instance
     * @see #send(MimeMessage)
     * @see #send(MimeMessage[])
     */
    @Override
    public MimeMessage createMimeMessage() {
        return getSender().createMimeMessage();
    }

    /**
     * Create a new JavaMail MimeMessage for the underlying JavaMail Session
     * of this sender, using the given input stream as the message source.
     *
     * @param contentStream the raw MIME input stream for the message
     * @return the new MimeMessage instance
     * @throws MailParseException in case of message creation failure
     */
    @Override
    public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
        return getSender().createMimeMessage(contentStream);
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
    @Override
    public void send(MimeMessage mimeMessage) throws MailException {
        getSender().send(mimeMessage);
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
    @Override
    public void send(MimeMessage[] mimeMessages) throws MailException {
        getSender().send(mimeMessages);
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
    @Override
    public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {
        getSender().send(mimeMessagePreparator);
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
    @Override
    public void send(MimeMessagePreparator[] mimeMessagePreparators) throws MailException {
        getSender().send(mimeMessagePreparators);
    }

    /**
     * Send the given simple mail message.
     *
     * @param simpleMessage the message to send
     * @throws MailParseException          in case of failure when parsing the message
     * @throws MailAuthenticationException in case of authentication failure
     * @throws MailSendException           in case of failure when sending the message
     */
    @Override
    public void send(SimpleMailMessage simpleMessage) throws MailException {
        getSender().send(simpleMessage);
    }

    /**
     * Send the given array of simple mail messages in batch.
     *
     * @param simpleMessages the messages to send
     * @throws MailParseException          in case of failure when parsing a message
     * @throws MailAuthenticationException in case of authentication failure
     * @throws MailSendException           in case of failure when sending a message
     */
    @Override
    public void send(SimpleMailMessage[] simpleMessages) throws MailException {
        getSender().send(simpleMessages);
    }

    /**
     * Sets the timeout for establishing an SMTP connection.
     * <p/>
     * This corresponds to the <em>mail.smtp.connectiontimeout</em> property.
     *
     * @param timeout the timeout, in seconds. Use {@code <= 0} for the default timeout
     */
    public void setConnectionTimeout(long timeout) {
        this.connectionTimout = timeout * 1000;
    }

    /**
     * Sets the timeout for sending a message.
     * <p/>
     * This corresponds to the <em>mail.smtp.timeout</em> property.
     *
     * @param timeout the timeout, in seconds. Use {@code <= 0} for the default timeout
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout * 1000;
    }

    /**
     * Determines if JavaMail debugging output is enabled.
     * <p/>
     * Corresponds to the JavaMail mail.debug property.
     *
     * @param debug if {@code true} turn on debugging output
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * Determines if JavaMail protocol authentication commands (including usernames and passwords) are
     * included in the debug output.
     * <p/>
     * Corresponds to the JavaMail mail.debug.auth property.
     *
     * @param debug if {@code true} turn on debugging output
     */
    public void setDebugAuth(boolean debug) {
        this.debugAuth = debug;
    }

    /**
     * Returns a mail sender, creating it if none is present, or the settings have changed.
     *
     * @return a mail sender
     */
    protected JavaMailSender getSender() {
        JavaMailSender result;
        MailServer current;
        synchronized (this) {
            result = sender;
            current = settings;
        }
        MailServer newSettings = getMailServer();
        if (newSettings != null && !ObjectUtils.equals(newSettings, current)) {
            result = setSender(createMailSender(newSettings), newSettings);
        } else if (result == null) {
            result = setSender(createMailSender(), newSettings);
        }
        return result;
    }

    /**
     * Creates a new mail sender.
     *
     * @param settings the mail server settings
     * @return a new mail sender
     */
    protected JavaMailSender createMailSender(MailServer settings) {
        JavaMailSenderImpl result;
        result = createMailSender();
        Properties properties = result.getJavaMailProperties();
        if (properties == null) {
            properties = new Properties();
            result.setJavaMailProperties(properties);
        }
        result.setHost(settings.getHost());
        result.setPort(settings.getPort());
        String username = settings.getUsername();
        result.setUsername(username);
        result.setPassword(settings.getPassword());
        if (settings.getSecurity() == MailServer.Security.SSL_TLS) {
            result.setProtocol("smtps");
        } else if (settings.getSecurity() == MailServer.Security.STARTTLS) {
            properties.setProperty(MAIL_SMTP_AUTH, Boolean.toString(!StringUtils.isEmpty(username)));
            properties.setProperty(MAIL_SMTP_STARTTLS_ENABLE, Boolean.TRUE.toString());
        }
        properties.setProperty(MAIL_DEBUG, Boolean.toString(debug));
        properties.setProperty(MAIL_DEBUG_AUTH, Boolean.toString(debugAuth));
        if (connectionTimout > 0) {
            properties.setProperty(NAIL_SMTP_CONNECTION_TIMEOUT, Long.toString(connectionTimout));
        }
        if (timeout > 0) {
            properties.setProperty(NAIL_SMTP_TIMEOUT, Long.toString(timeout));
        }
        return result;
    }

    /**
     * Creates a new mail sender.
     * <p/>
     * This implementation sets the default encoding to UTF-8.
     *
     * @return a new mail sender
     */
    protected JavaMailSenderImpl createMailSender() {
        JavaMailSenderImpl result = new JavaMailSenderImpl();
        result.setDefaultEncoding("UTF-8");
        return result;
    }

    /**
     * Returns the mail server settings.
     *
     * @return the settings, or {@code null} if none is configured
     */
    protected abstract MailServer getMailServer();

    /**
     * Register the sender and settings used to configure it.
     *
     * @param sender   the sender
     * @param settings the settings. May be {@code null}
     * @return the sender
     */
    private synchronized JavaMailSender setSender(JavaMailSender sender, MailServer settings) {
        this.sender = sender;
        this.settings = settings;
        return sender;
    }
}
