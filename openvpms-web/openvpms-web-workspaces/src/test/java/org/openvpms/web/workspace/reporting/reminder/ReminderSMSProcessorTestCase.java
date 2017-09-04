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
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderConfiguration;
import org.openvpms.archetype.rules.patient.reminder.ReminderItemStatus;
import org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper;
import org.openvpms.archetype.rules.patient.reminder.ReminderTypes;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.sms.Connection;
import org.openvpms.sms.ConnectionFactory;
import org.openvpms.sms.SMSException;
import org.openvpms.sms.i18n.SMSMessages;
import org.openvpms.web.component.im.sms.SMSTemplateEvaluator;
import org.openvpms.web.workspace.customer.communication.CommunicationLogger;
import org.openvpms.web.workspace.reporting.ReportingException;

import java.util.Date;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.addReminderCount;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createSMSRule;

/**
 * Tests the {@link ReminderSMSProcessor}.
 *
 * @author Tim Anderson
 */
public class ReminderSMSProcessorTestCase extends AbstractPatientReminderProcessorTest {

    /**
     * The reminder processor.
     */
    private ReminderSMSProcessor processor;

    /**
     * The SMS connection.
     */
    private Connection connection;

    /**
     * Constructs a {@link ReminderSMSProcessorTestCase}.
     */
    public ReminderSMSProcessorTestCase() {
        super(ContactArchetypes.PHONE);
    }

    /**
     * Sets up the test case.
     */
    @Before
    @Override
    public void setUp() {
        super.setUp();
        Entity documentTemplate = ReminderTestHelper.createDocumentTemplate(false, true);
        addReminderCount(reminderType, 0, 0, DateUnits.WEEKS, documentTemplate, createSMSRule());

        IArchetypeService service = getArchetypeService();
        SMSTemplateEvaluator templateEvaluator = new SMSTemplateEvaluator(service, getLookupService(), null);
        ReminderSMSEvaluator evaluator = new ReminderSMSEvaluator(templateEvaluator);
        ReminderTypes reminderTypes = new ReminderTypes(service);
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        when(connectionFactory.getMaxParts()).thenReturn(1);
        connection = mock(Connection.class);
        when(connectionFactory.createConnection()).thenReturn(connection);
        CommunicationLogger logger = mock(CommunicationLogger.class);
        ReminderConfiguration config = createConfiguration();
        PracticeRules practiceRules = mock(PracticeRules.class);
        when(practiceRules.isSMSEnabled(practice)).thenReturn(true);
        processor = new ReminderSMSProcessor(connectionFactory, evaluator, reminderTypes, practice,
                                             reminderRules, patientRules, practiceRules, service, config, logger);
    }

    /**
     * Verifies that reminders are sent to the REMINDER contact.
     */
    @Test
    public void testReminderContact() {
        Contact sms1 = TestHelper.createPhoneContact(null, "1", true, true, "REMINDER");
        Contact sms2 = TestHelper.createPhoneContact(null, "2", true, false, null);
        Contact sms3 = TestHelper.createPhoneContact(null, "3", true, false, null);
        customer.addContact(sms1);
        customer.addContact(sms2);
        customer.addContact(sms3);

        checkSend(null, "1");
    }

    /**
     * Verifies that SMS can be sent to a contact different to the default.
     */
    @Test
    public void testOverrideContact() {
        Contact sms1 = TestHelper.createPhoneContact(null, "1", true, true, "REMINDER");
        Contact sms2 = TestHelper.createPhoneContact(null, "2", true, false, null);
        customer.addContact(sms1);
        customer.addContact(sms2);

        checkSend(sms2, "2");
    }

    /**
     * Verifies that the reminder item status is set to ERROR, when the customer no phone contact with sms enabled.
     */
    @Test
    public void testNoSMSContact() {
        Contact phone = TestHelper.createPhoneContact(null, "1", false, true, "REMINDER");
        customer.addContact(phone);
        checkNoContact();
    }

    /**
     * Verifies that the reminder item status is set to ERROR, when the phone contact is incomplete.
     */
    @Test
    public void testMissingPhoneNumber() {
        Contact phone = TestHelper.createPhoneContact(null, null, true, true, "REMINDER");
        customer.addContact(phone);
        checkNoContact();
    }

    /**
     * Verifies that {@link ReminderSMSProcessor#failed(PatientReminders, Throwable)} updates reminders with the
     * failure message.
     */
    @Test
    public void testFailed() {
        Contact sms = TestHelper.createPhoneContact(null, "1", true, true, "REMINDER");
        customer.addContact(sms);

        Date tomorrow = DateRules.getTomorrow();
        Act item = ReminderTestHelper.createSMSReminder(DateRules.getToday(), tomorrow, ReminderItemStatus.PENDING, 0);
        Act reminder = ReminderTestHelper.createReminder(tomorrow, patient, reminderType, item);

        doThrow(new SMSException(SMSMessages.noMessageText())).when(connection).send(anyString(), anyString());

        PatientReminders reminders = prepare(item, reminder, null);
        try {
            processor.process(reminders);
            fail("Expected exception to be thrown");
        } catch (ReportingException expected) {
            assertTrue(processor.failed(reminders, expected));
            checkItem(get(item), ReminderItemStatus.ERROR, "Failed to process reminder: SMS-0304: Message has no text");
        }
    }

    /**
     * Returns the reminder processor.
     *
     * @return the reminder processor
     */
    @Override
    protected PatientReminderProcessor getProcessor() {
        return processor;
    }

    /**
     * Creates a PENDING reminder item for reminder count 0.
     *
     * @param send    the send date
     * @param dueDate the due date
     * @return a new reminder item
     */
    @Override
    protected Act createReminderItem(Date send, Date dueDate) {
        return ReminderTestHelper.createSMSReminder(send, dueDate, ReminderItemStatus.PENDING, 0);
    }

    /**
     * Sends an SMS reminder.
     *
     * @param contact the contact to use. May be {@code null}
     * @param to      the expected to address
     */
    private void checkSend(Contact contact, String to) {
        PatientReminders reminders = prepare(contact);
        processor.process(reminders);
        Mockito.verify(connection).send(to, "some plain text");
        assertTrue(processor.complete(reminders));
    }

    /**
     * Prepares a reminder for send.
     *
     * @param contact the contact to use. May be {@code null}
     * @return the reminders
     */
    private PatientReminders prepare(Contact contact) {
        Date tomorrow = DateRules.getTomorrow();
        Act item = ReminderTestHelper.createSMSReminder(DateRules.getToday(), tomorrow, ReminderItemStatus.PENDING, 0);
        Act reminder = ReminderTestHelper.createReminder(tomorrow, patient, reminderType, item);

        return prepare(item, reminder, contact);
    }

}
