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
import org.junit.Test;
import org.openvpms.sms.mail.template.MailTemplateConfig;
import org.openvpms.sms.mail.template.StaticMailTemplateConfig;
import org.openvpms.sms.mail.template.TemplatedMailMessageFactory;
import org.openvpms.sms.mail.MailMessageFactory;
import org.openvpms.sms.mail.MailMessage;

/**
 * Tests the {@link TemplatedMailMessageFactory}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class TemplatedMailMessageFactoryTestCase {

    /**
     * Tests generation of messages when the template has no country prefix.
     */
    @Test
    public void testNoPrefix() {
        MailTemplateConfig config = new StaticMailTemplateConfig(null, "0", "from", "$phone", "'subject'", "$message");
        String phone1 = "0411123456";
        String phone2 = "+61411123456";
        String phone3 = "1234 567 890";
        String text = "test";
        check(phone1, text, config, "from", phone1, "subject", text);
        check(phone2, text, config, "from", "61411123456", "subject", text);
        check(phone3, text, config, "from", "1234567890", "subject", text);
    }

    /**
     * Tests generation of messages when the template has a country prefix.
     */
    @Test
    public void testPrefix() {
        MailTemplateConfig config = new StaticMailTemplateConfig("61", "0", "from", "$phone", "'subject'", "$message");
        String phone1 = "0411123456";
        String phone2 = "+61411123456";
        String phone3 = "1234 567 890";
        String text = "test";
        check(phone1, text, config, "from", "61411123456", "subject", text);
        check(phone2, text, config, "from", "61411123456", "subject", text);
        check(phone3, text, config, "from", "611234567890", "subject", text);
    }

    private void check(String phone, String message, MailTemplateConfig config, String expectedFrom, String expectedTo, String expectedSubject, String expectedText) {
        MailMessageFactory factory = new TemplatedMailMessageFactory(config);
        MailMessage mail = factory.createMessage(phone, message);
        assertEquals(expectedFrom, mail.getFrom());
        assertEquals(expectedTo, mail.getTo());
        assertEquals(expectedSubject, mail.getSubject());
        assertEquals(expectedText, mail.getText());
    }

}
