/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.reminder;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.macro.Macros;
import org.openvpms.macro.impl.MacroTestHelper;
import org.openvpms.web.component.im.sms.SMSTemplateEvaluator;
import org.openvpms.web.workspace.reporting.ReportingException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests the {@link ReminderSMSEvaluator}.
 *
 * @author Tim Anderson
 */
public class ReminderSMSEvaluatorTestCase extends ArchetypeServiceTest {

    /**
     * The template evaluator.
     */
    private ReminderSMSEvaluator evaluator;

    /**
     * The test practice.
     */
    private Party practice;

    /**
     * The test location.
     */
    private Party location;

    /**
     * The test reminder.
     */
    private Act act;

    /**
     * The test reminder type.
     */
    private Entity reminderType;

    /**
     * The test customer.
     */
    private Party customer;

    /**
     * The test patient.
     */
    private Party patient;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        SMSTemplateEvaluator smsEvaluator = new SMSTemplateEvaluator(getArchetypeService(), getLookupService(),
                                                                     applicationContext.getBean(Macros.class));
        evaluator = new ReminderSMSEvaluator(smsEvaluator);
        practice = TestHelper.getPractice();
        practice.setName("Emergency Vet");
        location = TestHelper.createLocation();
        location.setName("Emergency Vet - San Remo");
        location.addContact(TestHelper.createPhoneContact("", "12345678"));
        customer = TestHelper.createCustomer("Reuben", "Smith", true);
        patient = TestHelper.createPatient(customer);
        patient.setName("Milo");
        reminderType = ReminderTestHelper.createReminderType();
        save(patient);
        act = ReminderTestHelper.createReminder(patient, reminderType, TestHelper.getDatetime("2016-01-01 09:00:00"));
    }

    /**
     * Tests xpath expression evaluation.
     */
    @Test
    public void testXPathExpression() {
        String expression = "concat($patient.name, ' is due for a vaccination at ', \n" +
                            "       $location.name, '.', $nl, 'Please contact us on ',  \n" +
                            "       party:getTelephone($location), \n" +
                            "       ' to make an appointment')";
        String value = evaluate("XPATH", expression);
        assertEquals("Milo is due for a vaccination at Emergency Vet - San Remo.\n" +
                     "Please contact us on 12345678 to make an appointment", value);
    }

    /**
     * Verifies that the $patient, $customer, $location, and $practice variables are defined.
     */
    @Test
    public void testXPathVariables() {
        checkXPathExpression("$patient.name", "Milo");
        checkXPathExpression("$location.name", "Emergency Vet - San Remo");
        checkXPathExpression("$customer.firstName", "Reuben");
        checkXPathExpression("$practice.name", "Emergency Vet");
    }

    /**
     * Verifies that an invalid XPath expression generates an {@link ReportingException}.
     */
    @Test
    public void testXPathException() {
        try {
            evaluate("XPATH", "$badexpression");
            fail("Expected evaluation to throw an exception");
        } catch (ReportingException expected) {
            assertEquals("Failed to evaluate the SMS template XPATH template", expected.getMessage());
        }
    }

    /**
     * Tests macro expression evaluation.
     */
    @Test
    public void testMacroExpression() {
        MacroTestHelper.createMacro("@patient", "$patient.name");
        MacroTestHelper.createMacro("@location", "$location.name");
        String expression = "@patient is due for a vaccination at @location";

        String value = evaluate("MACRO", expression);
        assertEquals("Milo is due for a vaccination at Emergency Vet - San Remo", value);
    }

    /**
     * Verifies that an invalid macro expression generates an {@link ReportingException}.
     */
    @Test
    public void testMacroException() {
        MacroTestHelper.createMacro("@badexpression", "concat(");
        try {
            evaluate("MACRO", "@badexpression");
            fail("Expected evaluation to throw an exception");
        } catch (ReportingException expected) {
            assertEquals("Failed to evaluate the SMS template MACRO template", expected.getMessage());
        }
    }

    /**
     * Verifies the result of an XPath expression matches that expected.
     *
     * @param expression the expression
     * @param expected   the expected value
     */
    private void checkXPathExpression(String expression, String expected) {
        String value = evaluate("XPATH", expression);
        assertEquals(expected, value);
    }

    /**
     * Evaluates a template against the test data.
     *
     * @param contentType the content type
     * @param content     the content
     * @return the result of the evaluation
     */
    private String evaluate(String contentType, String content) {
        Entity template = ReminderTestHelper.createSMSTemplate(contentType, content);
        return evaluator.evaluate(template, act, customer, patient, location, practice);
    }

}
