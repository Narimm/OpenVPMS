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

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
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
        template.setCountry("61");
        template.setTrunkPrefix("0");
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
        MailMessageFactory messageFactory = new MailMessageFactory() {
            public MailMessage createMessage(String phone, String text) {
                MailMessage message = new MailMessage();
                message.setTo("foo");
                message.setFrom("foo");
                return message;
            }
        };
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
