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

package org.openvpms.sms.mail.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.openvpms.sms.mail.MailMessage;
import org.openvpms.sms.mail.MailMessageFactory;
import org.openvpms.sms.SMSException;

/**
 * Tests the {@link TemplatedMailMessageFactory}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class TemplatedMailMessageFactoryTestCase {

    /**
     * Tests generation of the 'from' address.
     */
    @Test
    public void testFrom() {
        MailTemplate template1 = createMailTemplate();
        template1.setFrom("from1@test.com");
        assertEquals("from1@test.com", generate(template1).getFrom());

        MailTemplate template2 = createMailTemplate();
        template2.setFromExpression("'from2@test.com'");
        assertEquals("from2@test.com", generate(template2).getFrom());

        MailTemplate template3 = createMailTemplate();
        template3.setFrom("from3");
        template3.setFromExpression("concat($from, '@test.com')");
        assertEquals("from3@test.com", generate(template3).getFrom());
    }

    /**
     * Tests generation of the 'to' address.
     */
    @Test
    public void testTo() {
        MailTemplate template1 = createMailTemplate();
        template1.setTo("to1@test.com");
        assertEquals("to1@test.com", generate(template1).getTo());

        MailTemplate template2 = createMailTemplate();
        template2.setToExpression("'to2@test.com'");
        assertEquals("to2@test.com", generate(template2).getTo());

        MailTemplate template3 = createMailTemplate();
        template3.setTo("to3");
        template3.setToExpression("concat($to, '@test.com')");
        assertEquals("to3@test.com", generate(template3).getTo());
    }

    /**
     * Tests generation of the 'reply-to' address.
     */
    @Test
    public void testReplyTo() {
        MailTemplate template1 = createMailTemplate();
        template1.setReplyTo("reply1@test.com");
        assertEquals("reply1@test.com", generate(template1).getReplyTo());

        MailTemplate template2 = createMailTemplate();
        template2.setReplyToExpression("'reply2@test.com'");
        assertEquals("reply2@test.com", generate(template2).getReplyTo());

        MailTemplate template3 = createMailTemplate();
        template3.setReplyTo("reply3");
        template3.setReplyToExpression("concat($replyTo, '@test.com')");
        assertEquals("reply3@test.com", generate(template3).getReplyTo());
    }

    /**
     * Tests generation of the subject.
     */
    @Test
    public void testSubject() {
        MailTemplate template1 = createMailTemplate();
        template1.setSubject("subject1");
        assertEquals("subject1", generate(template1).getSubject());

        MailTemplate template2 = createMailTemplate();
        template2.setSubjectExpression("'subject2'");
        assertEquals("subject2", generate(template2).getSubject());

        MailTemplate template3 = createMailTemplate();
        template3.setSubject("subject");
        template3.setSubjectExpression("concat($subject, '3')");
        assertEquals("subject3", generate(template3).getSubject());
    }

    /**
     * Tests generation of the text.
     */
    @Test
    public void testText() {
        MailTemplate template1 = createMailTemplate();
        template1.setText("text1");
        assertEquals("text1", generate(template1).getText());

        MailTemplate template2 = createMailTemplate();
        template2.setTextExpression("'text2'");
        assertEquals("text2", generate(template2).getText());

        MailTemplate template3 = createMailTemplate();
        template3.setText("text");
        template3.setTextExpression("concat($text, '3')");
        assertEquals("text3", generate(template3).getText());
    }


    /**
     * Tests generation of the 'to' address when the template has no country prefix.
     */
    @Test
    public void testToWithNoPrefix() {
        MailTemplate template = createMailTemplate();
        template.setTrunkPrefix("0");
        template.setToExpression("concat($phone, '@test.com')");

        String phone1 = "0411123456";
        String phone2 = "+61411123456";
        String phone3 = "1234 567 890";

        assertEquals(phone1 + "@test.com", generate(phone1, null, template).getTo());
        assertEquals("61411123456@test.com", generate(phone2, null, template).getTo());
        assertEquals("1234567890@test.com", generate(phone3, null, template).getTo());
    }

    /**
     * Tests generation of the 'to' address when the template has a country prefix.
     */
    @Test
    public void testToWithPrefix() {
        MailTemplate template = createMailTemplate();
        template.setCountry("61");
        template.setTrunkPrefix("0");
        template.setToExpression("concat($phone, '@test.com')");

        String phone1 = "0411123456";
        String phone2 = "+61411123456";
        String phone3 = "1234 567 890";

        assertEquals("61411123456@test.com", generate(phone1, null, template).getTo());
        assertEquals("61411123456@test.com", generate(phone2, null, template).getTo());
        assertEquals("611234567890@test.com", generate(phone3, null, template).getTo());
    }

    /**
     * Verifies that an exception is thrown if an expression is invalid.
     */
    @Test
    public void testInvalidExpression() {
        MailTemplate template = createMailTemplate();
        template.setFromExpression("foo");
        try {
            generate("123456", null, template);
            fail("Expected generation to fail");
        } catch (SMSException exception) {
            assertEquals("SMS-0300: Failed to evaluate expression: foo", exception.getMessage());
        }
    }

    /**
     * Verifies that an exception is thrown if the 'From' address is invalid.
     */
    @Test
    public void testInvalidFromAddress() {
        MailTemplate template = createMailTemplate();
        template.setFrom("foo");
        try {
            generate("123456", null, template);
            fail("Expected generation to fail");
        } catch (SMSException exception) {
            assertEquals("SMS-0301: Invalid 'From' email address: foo", exception.getMessage());
        }
    }

    /**
     * Verifies that an exception is thrown if the 'To' address is invalid.
     */
    @Test
    public void testInvalidToAddress() {
        MailTemplate template = createMailTemplate();
        template.setTo("foo");
        try {
            generate("123456", null, template);
            fail("Expected generation to fail");
        } catch (SMSException exception) {
            assertEquals("SMS-0302: Invalid 'To' email address: foo", exception.getMessage());
        }
    }

    /**
     * Verifies that an exception is thrown if the 'ReplyTo' address is invalid.
     */
    @Test
    public void testInvalidReplyToAddress() {
        MailTemplate template = createMailTemplate();
        template.setReplyTo("foo");
        try {
            generate("123456", null, template);
            fail("Expected generation to fail");
        } catch (SMSException exception) {
            assertEquals("SMS-0303: Invalid 'Reply To' email address: foo", exception.getMessage());
        }
    }

    /**
     * Creates a new template with valid from and to addresses.
     *
     * @return a new template
     */
    private MailTemplate createMailTemplate() {
        MailTemplate result = new MailTemplate();
        result.setFrom("from@test.com");
        result.setTo("to@test.com");
        return result;
    }

    /**
     * Generates a message using the specified template.
     *
     * @param template the template
     * @return the generated message
     */
    private MailMessage generate(MailTemplate template) {
        return generate("123456", "message", template);
    }

    /**
     * Generates a mail message using the specified phone number, message, and template.
     *
     * @param phone    the phone numner
     * @param message  the message
     * @param template the template
     * @return the generated message
     */
    private MailMessage generate(String phone, String message, MailTemplate template) {
        MailMessageFactory factory = new TemplatedMailMessageFactory(template);
        return factory.createMessage(phone, message);
    }

}
