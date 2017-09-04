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
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderConfiguration;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderItemStatus;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper;
import org.openvpms.archetype.rules.patient.reminder.ReminderType;
import org.openvpms.archetype.rules.practice.PracticeArchetypes;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Base class for {@link PatientReminderProcessor} tests.
 *
 * @author Tim Anderson
 */
public abstract class AbstractPatientReminderProcessorTest extends ArchetypeServiceTest {

    /**
     * The test customer.
     */
    protected Party customer;

    /**
     * The test patient.
     */
    protected Party patient;

    /**
     * The reminder type.
     */
    protected Entity reminderType;

    /**
     * The practice.
     */
    protected Party practice;

    /**
     * The patient rules.
     */
    @Autowired
    protected PatientRules patientRules;

    /**
     * Reminder rules.
     */
    @Autowired
    protected ReminderRules reminderRules;

    /**
     * The contact archetype.
     */
    private final String archetype;

    /**
     * Constructs a {@link AbstractPatientReminderProcessorTest}.
     *
     * @param archetype the contact archetype
     */
    public AbstractPatientReminderProcessorTest(String archetype) {
        this.archetype = archetype;
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        customer = TestHelper.createCustomer("J", "Bloggs", false, true);
        patient = TestHelper.createPatient(customer);

        practice = (Party) create(PracticeArchetypes.PRACTICE);
        practice.setName("Test Practice");
        practice.addContact(TestHelper.createEmailContact("foo@bar.com"));

        reminderType = ReminderTestHelper.createReminderType(1, DateUnits.MONTHS, 1, DateUnits.DAYS);
    }

    /**
     * Verifies that the reminder item status is set to ERROR, when the customer has no relevant contact.
     */
    @Test
    public void testNoContact() {
        checkNoContact();
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
     * Verifies that the reminder item status is set to ERROR, when the customer has no relevant contact.
     */
    protected void checkNoContact() {
        Date tomorrow = DateRules.getTomorrow();
        Act item = createReminderItem(DateRules.getToday(), tomorrow);
        Act reminder = ReminderTestHelper.createReminder(tomorrow, patient, reminderType, item);
        PatientReminders reminders = prepare(item, reminder, null);
        assertEquals(1, reminders.getErrors().size());
        assertEquals(ActStatus.IN_PROGRESS, reminder.getStatus());
        checkItem(item, ReminderItemStatus.ERROR, "Customer has no " + DescriptorHelper.getDisplayName(archetype));
    }

    /**
     * Returns the reminder processor.
     *
     * @return the reminder processor
     */
    protected abstract PatientReminderProcessor getProcessor();

    /**
     * Creates a PENDING reminder item for reminder count 0.
     *
     * @param send    the send date
     * @param dueDate the due date
     * @return a new reminder item
     */
    protected abstract Act createReminderItem(Date send, Date dueDate);


    /**
     * Verifies that a reminder item is cancelled.
     *
     * @param send    the item send date
     * @param message the expected message
     */
    protected void checkCancelItem(Date send, String message) {
        Date tomorrow = DateRules.getTomorrow();
        Act item = createReminderItem(send, tomorrow);
        Act reminder = ReminderTestHelper.createReminder(tomorrow, patient, reminderType, item);

        ReminderEvent event = new ReminderEvent(reminder, item, patient, customer);
        PatientReminderProcessor processor = getProcessor();
        PatientReminders reminders = processor.prepare(Collections.singletonList(event), ReminderType.GroupBy.CUSTOMER,
                                                       new Date(), false);
        assertEquals(1, reminders.getCancelled().size());
        assertEquals(ActStatus.IN_PROGRESS, reminder.getStatus());
        checkItem(item, ReminderItemStatus.CANCELLED, message);

        assertTrue(processor.complete(reminders));
    }

    /**
     * Verifies an item matches that expected.
     *
     * @param item    the item
     * @param status  the expect status
     * @param message the expected error message
     */
    protected void checkItem(Act item, String status, String message) {
        assertEquals(status, item.getStatus());
        assertEquals(message, new ActBean(item).getString("error"));
    }

    /**
     * Prepares a reminder for send.
     *
     * @param item     the reminder item
     * @param reminder the reminder
     * @param contact  the contact to use. May be {@code null}
     * @return the reminders
     */
    protected PatientReminders prepare(Act item, Act reminder, Contact contact) {
        ReminderEvent event = new ReminderEvent(reminder, item, patient, customer, contact);
        return getProcessor().prepare(Collections.singletonList(event), ReminderType.GroupBy.CUSTOMER,
                                      new Date(), false);
    }

    /**
     * Creates a new reminder configuration.
     *
     * @return a new configuration
     */
    protected ReminderConfiguration createConfiguration() {
        IArchetypeService service = getArchetypeService();
        IMObject config = create(ReminderArchetypes.CONFIGURATION);
        IMObjectBean bean = new IMObjectBean(config);
        bean.setValue("emailAttachments", false);
        return new ReminderConfiguration(config, service);
    }

    /**
     * Creates a new <em>contact.location</em>
     *
     * @param address   the address
     * @param preferred if {@code true}, flags the contact as preferred
     * @param purpose   the contact purpose. May be {@code null}
     * @return a new location contact
     */
    protected Contact createLocation(String address, boolean preferred, String purpose) {
        Contact contact = TestHelper.createLocationContact(address, "THORNBURY", "VIC", "3071");
        IMObjectBean bean = new IMObjectBean(contact);
        bean.setValue("preferred", preferred);
        if (purpose != null) {
            contact.addClassification(TestHelper.getLookup(ContactArchetypes.PURPOSE, purpose));
        }
        return contact;
    }

}
