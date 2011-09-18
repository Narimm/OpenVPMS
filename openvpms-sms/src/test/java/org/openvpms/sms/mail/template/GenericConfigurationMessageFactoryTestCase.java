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
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.sms.mail.template.MailTemplate;
import org.openvpms.sms.mail.template.MailTemplateConfig;
import org.openvpms.sms.mail.template.TemplatedMailMessageFactory;
import org.openvpms.sms.mail.template.AbstractMailTemplateConfig;
import org.openvpms.sms.mail.AbstractSMSTest;
import org.openvpms.sms.mail.MailMessageFactory;
import org.openvpms.sms.mail.MailMessage;

/**
 * Tests the {@link TemplatedMailMessageFactory} when configured with an <em>entity.SMSEmailGenericConfiguration</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class GenericConfigurationMessageFactoryTestCase extends AbstractSMSTest {

    @Test
    public void testCreateMessage() {
        String from = "test@openvpms";
        String to = "concat($phone, '@test.com')";
        final Entity entity = createConfig(from, to);
        MailTemplateConfig config = new AbstractMailTemplateConfig(getArchetypeService()) {
            public MailTemplate getTemplate() {
                return getTemplate(entity);
            }
        };
        MailMessageFactory factory = new TemplatedMailMessageFactory(config);
        String phone = "123456";
        String text = "text";
        MailMessage message = factory.createMessage(phone, text);
        assertEquals(from, message.getFrom());
        assertEquals(phone + "@test.com", message.getTo());
        assertEquals(null, message.getSubject());
        assertEquals(text, message.getText());
    }

}