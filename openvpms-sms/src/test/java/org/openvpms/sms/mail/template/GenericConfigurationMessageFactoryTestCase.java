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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.sms.mail.AbstractSMSTest;
import org.openvpms.sms.mail.MailMessage;
import org.openvpms.sms.mail.MailMessageFactory;
import org.openvpms.sms.mail.SMSArchetypes;

/**
 * Tests the {@link TemplatedMailMessageFactory} when configured with an <em>entity.SMSConfigEmailGeneric</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class GenericConfigurationMessageFactoryTestCase extends AbstractSMSTest {

    /**
     * Checks the default values of the <em>entity.SMSConfigEmailGeneric</em>.
     */
    @Test
    public void testDefaults() {
        Entity config = (Entity) create(SMSArchetypes.GENERIC_SMS_EMAIL_CONFIG);
        IMObjectBean bean = new IMObjectBean(config);
        assertNull(bean.getString("name"));
        assertNull(bean.getString("description"));
        assertNull(bean.getString("website"));
        assertTrue(bean.getBoolean("active"));
        assertNull(bean.getString("country"));
        assertNull(bean.getString("trunkPrefix"));
        assertNull(bean.getString("from"));
        assertEquals("$from", bean.getString("fromExpression"));
        assertNull(bean.getString("to"));
        assertEquals("$phone", bean.getString("toExpression"));
        assertNull(bean.getString("replyTo"));
        assertEquals("$replyTo", bean.getString("replyToExpression"));
        assertNull(bean.getString("subject"));
        assertEquals("$subject", bean.getString("subjectExpression"));
        assertNull(bean.getString("text"));
        assertEquals("$message", bean.getString("textExpression"));
    }

    /**
     * Verifies that messages created using an <em>entity.SMSConfigEmailGeneric</em> are populated
     * correctly.
     */
    @Test
    public void testCreateMessage() {
        String from = "test@openvpms";
        final Entity entity = createConfig(from, null, null, "concat($phone, '@test.com')", "$message");
        MailTemplateFactory templateFactory = new MailTemplateFactory(getArchetypeService());
        MailMessageFactory factory = new TemplatedMailMessageFactory(templateFactory.getTemplate(entity));
        String phone = "123456";
        String text = "text";
        MailMessage message = factory.createMessage(phone, text);
        assertEquals(from, message.getFrom());
        assertEquals(phone + "@test.com", message.getTo());
        assertEquals(null, message.getSubject());
        assertEquals(text, message.getText());
    }

}