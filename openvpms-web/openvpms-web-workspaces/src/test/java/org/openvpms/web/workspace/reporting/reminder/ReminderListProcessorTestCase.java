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
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderItemStatus;
import org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper;
import org.openvpms.archetype.rules.patient.reminder.ReminderTypes;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.im.print.IMPrinterFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.workspace.customer.communication.CommunicationLogger;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.addReminderCount;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createSMSRule;

/**
 * Tests the {@link ReminderListProcessor}.
 *
 * @author Tim Anderson
 */
public class ReminderListProcessorTestCase extends AbstractPatientReminderProcessorTest {

    /**
     * The reminder processor.
     */
    private ReminderListProcessor processor;

    /**
     * Constructs a {@link ReminderListProcessorTestCase}.
     */
    public ReminderListProcessorTestCase() {
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
        ReminderTypes reminderTypes = new ReminderTypes(service);
        CommunicationLogger logger = mock(CommunicationLogger.class);
        ReminderConfiguration config = createConfiguration();
        Party location = TestHelper.createLocation();
        IMPrinterFactory printerFactory = Mockito.mock(IMPrinterFactory.class);
        processor = new ReminderListProcessor(reminderTypes, reminderRules, patientRules, location, practice,
                                              service, config, printerFactory, logger, new HelpContext("foo", null)) {
            /**
             * Prints reminders.
             *
             * @param reminders the reminders to print
             */
            @Override
            protected void print(List<Act> reminders) {
                // no-op
            }
        };
    }

    /**
     * Verifies that list reminders are still processed, even if the customer has no relevant contact.
     */
    @Test
    @Override
    public void testNoContact() {
        Date tomorrow = DateRules.getTomorrow();
        Act item = createReminderItem(DateRules.getToday(), tomorrow);
        Act reminder = ReminderTestHelper.createReminder(tomorrow, patient, reminderType, item);
        PatientReminders reminders = prepare(item, reminder, null);
        List<ReminderEvent> events = reminders.getReminders();
        assertEquals(1, events.size());
        assertEquals(0, reminders.getErrors().size());
        checkItem(events.get(0).getItem(), ReminderItemStatus.PENDING, null);
    }

    /**
     * Verifies that reminders are sent to the REMINDER contact.
     */
    @Test
    public void testReminderContact() {
        Contact phone1 = TestHelper.createPhoneContact(null, "1", false, true, "REMINDER");
        Contact phone2 = TestHelper.createPhoneContact(null, "2", false, false, null);
        Contact phone3 = TestHelper.createPhoneContact(null, "3", false, false, null);
        customer.addContact(phone1);
        customer.addContact(phone2);
        customer.addContact(phone3);

        checkList(null, phone1);
    }

    /**
     * Verifies that emails can be sent to a contact different to the default.
     */
    @Test
    public void testOverrideContact() {
        Contact phone1 = TestHelper.createPhoneContact(null, "1", true, true, "REMINDER");
        Contact phone2 = TestHelper.createPhoneContact(null, "2", true, false, null);
        customer.addContact(phone1);
        customer.addContact(phone2);

        checkList(phone2, phone2);
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
        return ReminderTestHelper.createListReminder(send, dueDate, ReminderItemStatus.PENDING, 0);
    }

    /**
     * Lists a reminder.
     *
     * @param contact the contact to use. May be {@code null}
     * @param to      the expected contact
     */
    private void checkList(Contact contact, Contact to) {
        PatientReminders reminders = prepare(contact);
        processor.process(reminders);
        assertEquals(1, reminders.getProcessed());
        for (ReminderEvent event : reminders.getReminders()) {
            assertEquals(to, event.getContact());
        }
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
