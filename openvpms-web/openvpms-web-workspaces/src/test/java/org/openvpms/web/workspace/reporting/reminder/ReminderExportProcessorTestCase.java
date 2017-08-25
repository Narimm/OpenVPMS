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
import org.openvpms.web.workspace.customer.communication.CommunicationLogger;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.addReminderCount;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createExportRule;

/**
 * Tests the {@link ReminderExportProcessor}.
 *
 * @author Tim Anderson
 */
public class ReminderExportProcessorTestCase extends AbstractPatientReminderProcessorTest {

    /**
     * The reminder processor.
     */
    private ReminderExportProcessor processor;

    /**
     * Constructs a {@link ReminderExportProcessorTestCase}.
     */
    public ReminderExportProcessorTestCase() {
        super(ContactArchetypes.LOCATION);
    }

    /**
     * Sets up the test case.
     */
    @Before
    @Override
    public void setUp() {
        super.setUp();
        Entity documentTemplate = ReminderTestHelper.createDocumentTemplate(false, false);
        addReminderCount(reminderType, 0, 0, DateUnits.WEEKS, documentTemplate, createExportRule());

        IArchetypeService service = getArchetypeService();
        ReminderTypes reminderTypes = new ReminderTypes(service);
        CommunicationLogger logger = mock(CommunicationLogger.class);
        ReminderConfiguration config = createConfiguration();
        Party location = TestHelper.createLocation();
        processor = new ReminderExportProcessor(reminderTypes, reminderRules, patientRules, location, practice, service,
                                                config, logger) {
            /**
             * Exports reminders.
             *
             * @param reminders the reminders to export
             */
            @Override
            protected void export(List<ReminderEvent> reminders) {
                // no-op
            }
        };
    }

    /**
     * Verifies that reminders are sent to the REMINDER contact.
     */
    @Test
    public void testReminderContact() {
        Contact location1 = createLocation("1 St Georges Rd", true, "REMINDER");
        Contact location2 = createLocation("2 Keon St", false, null);
        Contact location3 = createLocation("3 Hutton St", false, null);
        customer.addContact(location1);
        customer.addContact(location2);
        customer.addContact(location3);

        checkExport(null, location1);
    }

    /**
     * Verifies that prints can be address to a contact different to the default.
     */
    @Test
    public void testOverrideContact() {
        Contact location1 = createLocation("1 St Georges Rd", true, "REMINDER");
        Contact location2 = createLocation("2 Keon St", false, null);
        customer.addContact(location1);
        customer.addContact(location2);

        checkExport(location2, location2);
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
        return ReminderTestHelper.createExportReminder(send, dueDate, ReminderItemStatus.PENDING, 0);
    }

    /**
     * Exports reminders.
     *
     * @param contact the contact to use. May be {@code null}
     * @param to      the expected to address
     */
    private void checkExport(Contact contact, Contact to) {
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
