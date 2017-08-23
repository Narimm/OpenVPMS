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
import org.mockito.Mockito;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderConfiguration;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderItemStatus;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper;
import org.openvpms.archetype.rules.patient.reminder.ReminderType;
import org.openvpms.archetype.rules.patient.reminder.ReminderTypes;
import org.openvpms.archetype.rules.practice.PracticeArchetypes;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.im.report.ReporterFactory;
import org.openvpms.web.component.mail.EmailTemplateEvaluator;
import org.openvpms.web.component.mail.MailerFactory;
import org.openvpms.web.workspace.customer.communication.CommunicationLogger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link ReminderEmailProcessor}.
 *
 * @author Tim Anderson
 */
public class ReminderEmailProcessorTestCase extends ArchetypeServiceTest {

    /**
     * Reminder rules.
     */
    @Autowired
    private ReminderRules reminderRules;

    /**
     * Patient rules.
     */
    @Autowired
    private PatientRules patientRules;

    /**
     * Practice rules.
     */
    @Autowired
    private PracticeRules practiceRules;

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
        customer = TestHelper.createCustomer();
        patient = TestHelper.createPatient(customer);
    }

    /**
     * Verifies that if a reminder item was not sent in time, it is cancelled.
     */
    @Test
    public void testOutOfDate() {
        checkCancelItem(DateRules.getDate(new Date(), -1, DateUnits.MONTHS), "Reminder not processed in time");
    }

    /**
     * Verifies that if a patient associated with a reminder item is deceased, the reminder item is cancelled.
     */
    @Test
    public void testDeceasedPatient() {
        patientRules.setDeceased(patient);
        checkCancelItem(DateRules.getToday(), "Patient is deceased");
    }

    /**
     * Verifies that if a patient associated with a reminder item is inactive, the reminder item is cancelled.
     */
    @Test
    public void testInactivePatient() {
        patient.setActive(false);
        checkCancelItem(DateRules.getToday(), "Patient is inactive");
    }

    /**
     * Verifies that if a customer associated with a reminder item is inactive, the reminder item is cancelled.
     */
    @Test
    public void testInactiveCustomer() {
        customer.setActive(false);
        checkCancelItem(DateRules.getToday(), "Customer is inactive");
    }

    /**
     * Verifies that a reminder item is cancelled.
     *
     * @param send    the item send date
     * @param message the expected message
     */
    protected void checkCancelItem(Date send, String message) {
        IArchetypeService service = getArchetypeService();
        ReminderConfiguration config = new ReminderConfiguration(create(ReminderArchetypes.CONFIGURATION), service);
        Party practice = (Party) create(PracticeArchetypes.PRACTICE);
        practice.setName("Test Practice");
        practice.addContact(TestHelper.createEmailContact("foo@bar.com"));
        Entity reminderType = ReminderTestHelper.createReminderType(1, DateUnits.MONTHS, 1, DateUnits.DAYS);

        MailerFactory factory = Mockito.mock(MailerFactory.class);
        EmailTemplateEvaluator evaluator = Mockito.mock(EmailTemplateEvaluator.class);
        ReminderTypes reminderTypes = new ReminderTypes(service);
        CommunicationLogger logger = Mockito.mock(CommunicationLogger.class);
        ReporterFactory reporterFactory = Mockito.mock(ReporterFactory.class);
        ReminderEmailProcessor processor = new ReminderEmailProcessor(
                factory, evaluator, reporterFactory, reminderTypes, practice, reminderRules, patientRules,
                practiceRules, service, config, logger);

        Date tomorrow = DateRules.getTomorrow();
        Act item = ReminderTestHelper.createEmailReminder(send, tomorrow, ReminderItemStatus.PENDING, 0);
        Act reminder = ReminderTestHelper.createReminder(tomorrow, patient, reminderType, item);

        ReminderEvent event = new ReminderEvent(reminder, item, patient, customer);
        PatientReminders reminders = processor.prepare(Collections.singletonList(event), ReminderType.GroupBy.CUSTOMER,
                                                       new Date(), false);
        assertEquals(1, reminders.getCancelled().size());
        assertEquals(ActStatus.IN_PROGRESS, reminder.getStatus());
        assertEquals(ReminderItemStatus.CANCELLED, item.getStatus());
        assertEquals(message, new ActBean(item).getString("error"));
    }

}
