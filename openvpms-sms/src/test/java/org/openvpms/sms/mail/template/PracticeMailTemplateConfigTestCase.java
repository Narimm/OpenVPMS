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
 */

package org.openvpms.sms.mail.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.sms.SMSException;
import org.openvpms.sms.mail.AbstractSMSTest;
import org.openvpms.sms.mail.SMSArchetypes;


/**
 * Tests the {@link PracticeMailTemplateConfig} class.
 *
 * @author Tim Anderson
 */
public class PracticeMailTemplateConfigTestCase extends AbstractSMSTest {

    /**
     * The practice.
     */
    private Party practice;

    /**
     * The mail template configuration.
     */
    private MailTemplateConfig config;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        // remove any existing SMS configuration
        practice = TestHelper.getPractice();
        EntityBean bean = new EntityBean(practice);
        for (EntityRelationship relationship : bean.getRelationships(SMSArchetypes.PRACTICE_SMS_CONFIGURATION)) {
            bean.removeRelationship(relationship);
        }
        save(practice);
        config = new PracticeMailTemplateConfig(getArchetypeService(), new PracticeRules(getArchetypeService()));
    }

    /**
     * Verifies that an {@link SMSException} is thrown if SMS is not configured.
     */
    @Test
    public void testNoConfiguration() {
        try {
            config.getTemplate();
            fail("Expected getTemplate() to fail");
        } catch (SMSException expected) {
            assertEquals("SMS-0100: The SMS provider is not configured for practice " + practice.getName(),
                         expected.getLocalizedMessage());
        }
    }

    /**
     * Verifies that an {@link SMSException} is thrown if there is no practice.
     */
    @Test
    public void testNoPractice() {
        remove(practice);
        config = new PracticeMailTemplateConfig(getArchetypeService(), new PracticeRules(getArchetypeService()));
        try {
            config.getTemplate();
            fail("Expected getTemplate() to fail");
        } catch (SMSException expected) {
            assertEquals("SMS-0101: Practice not found", expected.getLocalizedMessage());
        }
    }

    /**
     * Verifies that the returned template changes when the practice or SMS configuration are updated.
     */
    @Test
    public void testUpdate() {
        String from1 = "test@openvpms";
        String to1 = "concat($phone, '@test.com')";
        Entity configA = createConfig(from1, null, null, to1, "$message");
        EntityBean bean = new EntityBean(practice);
        EntityRelationship rel = bean.addRelationship(SMSArchetypes.PRACTICE_SMS_CONFIGURATION, configA);
        save(practice, configA);

        MailTemplate template = config.getTemplate();
        assertEquals(from1, template.getFrom());
        assertEquals(to1, template.getToExpression());
        assertEquals("$message", template.getTextExpression());

        String to2 = "concat($phone, '@new.com')";
        configA.getDetails().put("toExpression", to2);
        save(configA);

        template = config.getTemplate();
        assertEquals(from1, template.getFrom());
        assertEquals(to2, template.getToExpression());
        assertEquals("$message", template.getTextExpression());

        practice.removeEntityRelationship(rel);
        save(practice);

        try {
            config.getTemplate();
            fail("Expected getTemplate() to fail");
        } catch (SMSException expected) {
            assertEquals("SMS-0100: The SMS provider is not configured for practice " + practice.getName(),
                         expected.getLocalizedMessage());
        }

        String from2 = "foo@openvpms";
        Entity configB = createConfig(from2, null, null, to1, "$message");
        bean.addRelationship(SMSArchetypes.PRACTICE_SMS_CONFIGURATION, configB);
        save(practice, configB);

        template = config.getTemplate();
        assertEquals(from2, template.getFrom());
        assertEquals(to1, template.getToExpression());
        assertEquals("$message", template.getTextExpression());
    }

}
