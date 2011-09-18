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
import static org.junit.Assert.assertNotNull;
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
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
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
        config = new PracticeMailTemplateConfig(getArchetypeService(), new PracticeRules());
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
            assertEquals("SMS-0001: the SMS provider is not configured for practice " + practice.getName(),
                         expected.getLocalizedMessage());
        }
    }

    /**
     * Verifies that the returned template changes when the practice or SMS configuration are updated.
     */
    @Test
    public void testUpdate() {
        String from1 = "test@openvpms";
        String to1 = "concat($phone, '@test.com')";
        Entity configA = createConfig(from1, to1);
        EntityBean bean = new EntityBean(practice);
        EntityRelationship rel = bean.addRelationship(SMSArchetypes.PRACTICE_SMS_CONFIGURATION, configA);
        save(practice, configA);

        check(config.getTemplate(), from1, to1, null, "$message");

        String to2 = "concat($phone, '@new.com')";
        configA.getDetails().put("to", to2);
        save(configA);

        check(config.getTemplate(), from1, to2, null, "$message");

        practice.removeEntityRelationship(rel);
        save(practice);

        try {
            config.getTemplate();
            fail("Expected getTemplate() to fail");
        } catch (SMSException expected) {
            assertEquals("SMS-0001: the SMS provider is not configured for practice " + practice.getName(),
                         expected.getLocalizedMessage());
        }

        String from2 = "foo@openvpms";
        Entity configB = createConfig(from2, to1);
        bean.addRelationship(SMSArchetypes.PRACTICE_SMS_CONFIGURATION, configB);
        save(practice, configB);
        check(config.getTemplate(), from2, to1, null, "$message");

    }

    /**
     * Verifies a template matches that expected.
     *
     * @param template the template
     * @param from     the expected from address
     * @param to       the expected to address
     * @param subject  the expected subject
     * @param text     the expected test
     */
    private void check(MailTemplate template, String from, String to, String subject, String text) {
        assertNotNull(template);
        assertEquals(from, template.getFrom());
        assertEquals(to, template.getTo());
        assertEquals(subject, template.getSubject());
        assertEquals(text, template.getText());
    }

}
