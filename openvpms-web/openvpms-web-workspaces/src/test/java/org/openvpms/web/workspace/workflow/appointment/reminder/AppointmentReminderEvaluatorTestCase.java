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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment.reminder;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.macro.Macros;
import org.openvpms.macro.impl.MacroTestHelper;
import org.openvpms.web.component.im.sms.SMSTemplateEvaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests the {@link AppointmentReminderEvaluator}.
 *
 * @author Tim Anderson
 */
public class AppointmentReminderEvaluatorTestCase extends ArchetypeServiceTest {

    /**
     * The expression evaluator.
     */
    private AppointmentReminderEvaluator evaluator;

    /**
     * The test practice.
     */
    private Party practice;

    /**
     * The test location.
     */
    private Party location;

    /**
     * The test appointment.
     */
    private Act act;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        SMSTemplateEvaluator smsEvaluator = new SMSTemplateEvaluator(getArchetypeService(), getLookupService(),
                                                                     applicationContext.getBean(Macros.class));
        evaluator = new AppointmentReminderEvaluator(getArchetypeService(), smsEvaluator);
        practice = TestHelper.getPractice();
        practice.setName("Vets R Us");
        location = TestHelper.createLocation();
        location.setName("Vets R Us - Cowes");
        Party customer = TestHelper.createCustomer("Pippi", "Smith", true);
        Party patient = TestHelper.createPatient(customer);
        patient.setName("Fido");
        save(patient);
        act = ScheduleTestHelper.createAppointment(TestHelper.getDatetime("2015-11-28 09:00:00"),
                                                   TestHelper.getDatetime("2015-11-28 09:30:00"),
                                                   ScheduleTestHelper.createSchedule(location), customer, patient);
    }

    /**
     * Tests xpath expression evaluation.
     */
    @Test
    public void testXPathExpression() {
        String expression = "concat('Reminder: ', $patient.name, ' has an appointment at ', $location.name, ' on ', "
                            + "date:formatDate($appointment.startTime, 'short'))";
        Entity template = createTemplate("XPATH", expression);
        String value = evaluator.evaluate(template, act, location, practice);
        assertEquals("Reminder: Fido has an appointment at Vets R Us - Cowes on 28/11/15", value);
    }

    /**
     * Verifies that the $patient, $customer, $location, and $practice variables are defined.
     */
    @Test
    public void testXPathVariables() {
        checkXPathExpression("$patient.name", "Fido");
        checkXPathExpression("$location.name", "Vets R Us - Cowes");
        checkXPathExpression("$customer.firstName", "Pippi");
        checkXPathExpression("$practice.name", "Vets R Us");
    }

    /**
     * Verifies that an invalid XPath expression generates an {@link AppointmentReminderException}.
     */
    @Test
    public void testXPathException() {
        try {
            Entity template = createTemplate("XPATH", "$badexpression");
            evaluator.evaluate(template, act, location, practice);
            fail("Expected evaluation to throw an exception");
        } catch (AppointmentReminderException expected) {
            assertEquals("Failed to evaluate the expression in XPATH template", expected.getMessage());
        }
    }

    /**
     * Tests macro expression evaluation.
     */
    @Test
    public void testMacroExpression() {
        MacroTestHelper.createMacro("@patient", "$patient.name");
        MacroTestHelper.createMacro("@location", "$location.name");
        MacroTestHelper.createMacro("@startDate", "date:formatDate($appointment.startTime, 'short')");
        String expression = "Reminder: @patient has an appointment at @location on @startDate";

        Entity template = createTemplate("MACRO", expression);
        String value = evaluator.evaluate(template, act, location, practice);
        assertEquals("Reminder: Fido has an appointment at Vets R Us - Cowes on 28/11/15", value);
    }

    /**
     * Verifies that an invalid macro expression generates an {@link AppointmentReminderException}.
     */
    @Test
    public void testMacroException() {
        MacroTestHelper.createMacro("@badexpression", "concat(");
        try {
            Entity template = createTemplate("MACRO", "@badexpression");
            evaluator.evaluate(template, act, location, practice);
            fail("Expected evaluation to throw an exception");
        } catch (AppointmentReminderException expected) {
            assertEquals("Failed to evaluate the expression in MACRO template", expected.getMessage());
        }
    }

    /**
     * Verifies the result of an XPath expression matches that expected.
     *
     * @param expression the expression
     * @param expected   the expected value
     */
    private void checkXPathExpression(String expression, String expected) {
        Entity template = createTemplate("XPATH", expression);
        String value = evaluator.evaluate(template, act, location, practice);
        assertEquals(expected, value);
    }

    /**
     * Helper to create an appointment reminder SMS template.
     *
     * @param type       the expression type
     * @param expression the expression
     * @return a new template
     */
    private Entity createTemplate(String type, String expression) {
        Entity template = (Entity) create(DocumentArchetypes.APPOINTMENT_SMS_TEMPLATE);
        IMObjectBean bean = new IMObjectBean(template);
        bean.setValue("name", type + " template");
        bean.setValue("contentType", type);
        bean.setValue("content", expression);
        return template;
    }


}

