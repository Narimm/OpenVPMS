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
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.sms.mail.AbstractSMSTest;

import java.util.Map;


/**
 * Tests the {@link MailTemplateFactory} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class MailTemplateFactoryTestCase extends AbstractSMSTest {

    /**
     * Tests the {@link MailTemplateFactory#getTemplate} method.
     */
    @Test
    public void testGetTemplate() {
        // populate a entity.SMSConfigEmailGeneric
        Entity config = createConfig("from", "$from", "to", "$to", "$text");
        IMObjectBean bean = new IMObjectBean(config);
        bean.setValue("country", "61");
        bean.setValue("trunkPrefix", "0");
        bean.setValue("replyTo", "replyTo");
        bean.setValue("replyToExpression", "$replyTo");
        bean.setValue("subject", "subject");
        bean.setValue("subjectExpression", "$subject");
        bean.setValue("text", "text");
        bean.setValue("textExpression", "$text");
        getArchetypeService().validateObject(config);

        // generate the template
        MailTemplateFactory factory = new MailTemplateFactory(getArchetypeService());
        MailTemplate template = factory.getTemplate(config);

        // verify the template has the expected details
        assertEquals("61", template.getCountry());
        assertEquals("0", template.getTrunkPrefix());
        assertEquals("from", template.getFrom());
        assertEquals("$from", template.getFromExpression());
        assertEquals("to", template.getTo());
        assertEquals("$to", template.getToExpression());
        assertEquals("replyTo", template.getReplyTo());
        assertEquals("$replyTo", template.getReplyToExpression());
        assertEquals("subject", template.getSubject());
        assertEquals("$subject", template.getSubjectExpression());
        assertEquals("text", template.getText());
        assertEquals("$text", template.getTextExpression());

        // verify that only country and trunkPrefix are declared as variables
        Map<String, String> variables = template.getVariables();
        assertEquals(2, variables.size());
        assertEquals("61", variables.get("country"));
        assertEquals("0", variables.get("trunkPrefix"));
    }

}
