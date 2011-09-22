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
 * Tests the {@link TemplatedMailMessageFactory} when configured with an <em>entity.SMSConfigEmailClickatell</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class ClickatellMessageFactoryTestCase extends ArchetypeServiceTest {

    /**
     * Checks the default values of the <em>entity.SMSConfigEmailClickatell</em>
     */
    @Test
    public void testDefaults() {
        Entity config = (Entity) create("entity.SMSConfigEmailClickatell");
        IMObjectBean bean = new IMObjectBean(config);
        assertEquals("Clickatell SMTP Connection Configuration", bean.getString("name"));
        assertNull(bean.getString("description"));
        assertEquals("http://www.clickatell.com", bean.getString("website"));
        assertTrue(bean.getBoolean("active"));
        assertNull(bean.getString("country"));
        assertNull(bean.getString("trunkPrefix"));
        assertNull(bean.getString("user"));
        assertNull(bean.getString("password"));
        assertNull(bean.getString("apiId"));

        assertNull(bean.getString("from"));
        assertFalse(bean.hasNode("fromExpression"));
        assertNull(bean.getString("replyTo"));
        assertEquals("sms@messaging.clickatell.com", bean.getString("to"));
        assertFalse(bean.hasNode("replyToExpression"));
        assertFalse(bean.hasNode("subject"));
        assertFalse(bean.hasNode("subjectExpression"));
        assertFalse(bean.hasNode("text"));
        assertEquals("concat(\"user:\", $user, \"\npassword:\", $password, \"\napi_id:\", $apiId, \"\nto:\", $phone, "
                     + "\"\nreply:\", $replyTo, \"\ntext:\", replace($message, \"\n\",\"\ntext:\"))",
                     bean.getString("textExpression"));
    }

    /**
     * Verifies that messages created using an <em>entity.SMSConfigEmailClickatell</em> are populated
     * correctly.
     */
    @Test
    public void testCreateMessage() {
        String from = "test@openvpms";
        String reply = "replies@openvpms";
        String user = "user";
        String password = "password";
        String apiId = "apiId";
        final Entity entity = createConfig(from, user, password, apiId, reply);

        MailTemplateFactory templateFactory = new MailTemplateFactory(getArchetypeService());
        MailMessageFactory factory = new TemplatedMailMessageFactory(templateFactory.getTemplate(entity));
        String phone = "0411234567";
        String text = "text\nover\nmultiple\nlines";
        MailMessage message = factory.createMessage(phone, text);
        assertEquals(from, message.getFrom());
        assertEquals("sms@messaging.clickatell.com", message.getTo());
        assertEquals(null, message.getSubject());
        String expectedText = "user:" + user + "\npassword:" + password + "\napi_id:" + apiId + "\nto:" + phone +
                              "\nreply:" + reply + "\ntext:text\ntext:over\ntext:multiple\ntext:lines";
        assertEquals(expectedText, message.getText());
    }

    /**
     * Helper to create an <em>entity.SMSConfigEmailClickatell</em>
     *
     * @param from     the from address                      \
     * @param user     the user
     * @param password the password
     * @param apiId    the api id
     * @param replyTo    the replyTo address
     * @return a new configuration
     */
    private Entity createConfig(String from, String user, String password, String apiId, String replyTo) {
        Entity entity = (Entity) create("entity.SMSConfigEmailClickatell");
        IMObjectBean bean = new IMObjectBean(entity);
        bean.setValue("from", from);
        bean.setValue("user", user);
        bean.setValue("password", password);
        bean.setValue("apiId", apiId);
        bean.setValue("replyTo", replyTo);
        return entity;
    }
}