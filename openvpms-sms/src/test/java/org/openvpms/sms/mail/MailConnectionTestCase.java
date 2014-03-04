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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.openvpms.sms.Connection;
import org.openvpms.sms.ConnectionFactory;
import org.openvpms.sms.SMSException;
import org.openvpms.sms.mail.template.MailTemplate;
import org.openvpms.sms.mail.template.TemplatedMailMessageFactory;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.net.ConnectException;
import java.util.List;


/**
 * Tests the {@link MailConnection}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class MailConnectionTestCase {

    /**
     * Creates a connection and verifies an SMS can be sent.
     *
     * @throws Exception for any error
     */
    @Test
    public void testCreateConnection() throws Exception {
        String from = "test@openvpms.com";
        MailTemplate template = new MailTemplate();
        template.setCountryPrefix("61");
        template.setAreaPrefix("0");
        template.setFrom(from);
        template.setToExpression("concat($phone, '@sms.com')");
        template.setSubject("subject");
        template.setTextExpression("$message");
        MailMessageFactory messageFactory = new TemplatedMailMessageFactory(template);
        MailSender sender = new MailSender();
        ConnectionFactory factory = new MailConnectionFactory(sender, messageFactory);
        Connection connection = factory.createConnection();
        assertNotNull(connection);
        connection.send("0411234567", "test");

        List<MimeMessage> sent = sender.getSent();
        assertEquals(1, sent.size());
        MimeMessage message = sent.get(0);
        assertEquals(from, getFrom(message));
        assertEquals("61411234567@sms.com", getTo(message));
        assertEquals("subject", message.getSubject());
        assertEquals("test", getContent(message));

        connection.close();
    }

    /**
     * Verifies that the connection throws IllegalStateException if an attempt is made to use it after it has been
     * closed.
     */
    @Test
    public void testIllegalStateException() {
        MailMessageFactory messageFactory = new TemplatedMailMessageFactory(new MailTemplate());
        MailSender sender = new MailSender();
        ConnectionFactory factory = new MailConnectionFactory(sender, messageFactory);
        Connection connection = factory.createConnection();
        connection.close();
        try {
            connection.send("12345", "text");
            fail("Expected IllegalStateException to be thrown");
        } catch (IllegalStateException expected) {
            // do nothing
        }
    }

    /**
     * Verifies an {@link SMSException} is thrown if the email cannot be created.
     */
    @Test
    public void testFailedToCreateEmail() {
        MailMessageFactory messageFactory = createMailMessageFactory("foo", "foo");
        MailSender sender = new MailSender();
        ConnectionFactory factory = new MailConnectionFactory(sender, messageFactory);
        Connection connection = factory.createConnection();
        try {
            connection.send("0411234567", "test");
            fail("Expected SMSException to be thrown");
        } catch (SMSException expected) {
            assertEquals("SMS-0200: Failed to create email: Missing final '@domain'", expected.getLocalizedMessage());
        }
    }

    /**
     * Verifies an {@link SMSException} is thrown if there is a mail authentication exception.
     */
    @Test
    public void testMailAuthenticationException() {
        MailMessageFactory messageFactory = createMailMessageFactory("foo@test", "foo@test");
        MailSender sender = new MailSender() {
            @Override
            public void send(MimeMessage mimeMessage) throws MailException {
                throw new MailAuthenticationException("foo");
            }
        };
        ConnectionFactory factory = new MailConnectionFactory(sender, messageFactory);
        Connection connection = factory.createConnection();
        try {
            connection.send("0411234567", "test");
            fail("Expected SMSException to be thrown");
        } catch (SMSException expected) {
            assertEquals("SMS-0201: Mail server authentication failed: foo", expected.getLocalizedMessage());
        }
    }


    /**
     * Verifies an {@link SMSException} is thrown if there is a mail connection exception.
     */
    @Test
    @SuppressWarnings("ThrowableInstanceNeverThrown")
    public void testMailConnectionFailed() {
        MailMessageFactory messageFactory = createMailMessageFactory("foo@test", "foo@test");
        MailSender sender = new MailSender() {
            @Override
            public void send(MimeMessage mimeMessage) throws MailException {
                throw new MailSendException("foo", new MessagingException("bar", new ConnectException()));
            }
        };
        ConnectionFactory factory = new MailConnectionFactory(sender, messageFactory);
        Connection connection = factory.createConnection();
        try {
            connection.send("0411234567", "test");
            fail("Expected SMSException to be thrown");
        } catch (SMSException expected) {
            assertEquals("SMS-0202: Mail server connection failed: bar", expected.getLocalizedMessage());
        }
    }

    /**
     * Verifies an {@link SMSException} is thrown if there is a mail send exception.
     */
    @Test
    public void testMailSendFailed() {
        MailMessageFactory messageFactory = createMailMessageFactory("foo@test", "foo@test");
        MailSender sender = new MailSender() {
            @Override
            public void send(MimeMessage mimeMessage) throws MailException {
                throw new MailSendException("foo");
            }
        };
        ConnectionFactory factory = new MailConnectionFactory(sender, messageFactory);
        Connection connection = factory.createConnection();
        try {
            connection.send("0411234567", "test");
            fail("Expected SMSException to be thrown");
        } catch (SMSException expected) {
            assertEquals("SMS-0203: Failed to send email: foo", expected.getLocalizedMessage());
        }
    }

    /**
     * Helper to create a {@link MailMessageFactory} that returns messages with the specified from and to addresses.
     *
     * @param from the from address. May be <tt>null</tt>
     * @param to   the to address. May be <tt>null</tt>
     * @return a new factory
     */
    private MailMessageFactory createMailMessageFactory(final String from, final String to) {
        return new MailMessageFactory() {
            public MailMessage createMessage(String phone, String text) {
                MailMessage message = new MailMessage();
                message.setTo(to);
                message.setFrom(from);
                message.setText(text);
                return message;
            }
        };
    }

    /**
     * Returns the message content.
     *
     * @param message the message
     * @return the message content
     * @throws Exception for any error
     */
    private String getContent(MimeMessage message) throws Exception {
        Object content = message.getContent();
        assertNotNull(content);
        return content.toString();
    }

    /**
     * Returns the to address.
     *
     * @param message the message
     * @return the to address
     * @throws MessagingException for any error
     */
    private String getTo(MimeMessage message) throws MessagingException {
        Address[] addresses = message.getRecipients(MimeMessage.RecipientType.TO);
        assertEquals(1, addresses.length);
        return addresses[0].toString();
    }

    /**
     * Returns the from address.
     *
     * @param message the message
     * @return the from address
     * @throws MessagingException for any error
     */
    private String getFrom(MimeMessage message) throws MessagingException {
        Address[] addresses = message.getFrom();
        assertEquals(1, addresses.length);
        return addresses[0].toString();
    }

}
