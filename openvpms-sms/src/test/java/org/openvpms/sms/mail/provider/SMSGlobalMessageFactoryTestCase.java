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

package org.openvpms.sms.mail.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.sms.mail.MailMessage;
import org.openvpms.sms.mail.MailMessageFactory;
import org.openvpms.sms.mail.template.MailTemplateFactory;
import org.openvpms.sms.mail.template.TemplatedMailMessageFactory;


/**
 * Tests the {@link TemplatedMailMessageFactory} when configured with an <em>entity.SMSConfigEmailSMSGlobal</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class SMSGlobalMessageFactoryTestCase extends ArchetypeServiceTest {

    /**
     * Checks the default values of the <em>entity.SMSConfigEmailSMSGlobal</em>
     */
    @Test
    public void testDefaults() {
        Entity config = (Entity) create("entity.SMSConfigEmailSMSGlobal");
        IMObjectBean bean = new IMObjectBean(config);
        assertEquals("SMS Global Email2SMS Configuration", bean.getString("name"));
        assertNull(bean.getString("description"));
        assertEquals("http://www.smsglobal.com", bean.getString("website"));
        assertTrue(bean.getBoolean("active"));
        assertNull(bean.getString("countryPrefix"));
        assertNull(bean.getString("areaPrefix"));

        assertNull(bean.getString("from"));
        assertFalse(bean.hasNode("fromExpression"));
        assertFalse(bean.hasNode("to"));
        assertEquals("concat($phone, \"@email.smsglobal.com\")", bean.getString("toExpression"));
        assertFalse(bean.hasNode("replyTo"));
        assertFalse(bean.hasNode("replyToExpression"));
        assertFalse(bean.hasNode("subject"));
        assertFalse(bean.hasNode("subjectExpression"));
        assertFalse(bean.hasNode("text"));
        assertEquals("$message", bean.getString("textExpression"));
    }

    /**
     * Verifies that messages created using an <em>entity.SMSConfigEmailSMSGlobal</em> are populated
     * correctly.
     */
    @Test
    public void testCreateMessage() {
        String from = "test@openvpms";
        final Entity entity = createConfig(from);

        MailTemplateFactory templateFactory = new MailTemplateFactory(getArchetypeService());
        MailMessageFactory factory = new TemplatedMailMessageFactory(templateFactory.getTemplate(entity));

        String phone = "0411234567";
        String text = "text";
        MailMessage message = factory.createMessage(phone, text);
        assertEquals(from, message.getFrom());
        assertEquals(phone + "@email.smsglobal.com", message.getTo());
        assertEquals(null, message.getSubject());
        assertEquals(text, message.getText());
    }

    /**
     * Helper to create an <em>entity.SMSConfigEmailSMSGlobal</em>
     *
     * @param from the from address
     * @return a new configuration
     */
    private Entity createConfig(String from) {
        Entity entity = (Entity) create("entity.SMSConfigEmailSMSGlobal");
        IMObjectBean bean = new IMObjectBean(entity);
        bean.setValue("from", from);
        return entity;
    }
}
