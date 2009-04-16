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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.patient.reminder;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.util.Date;
import java.util.Set;


/**
 * Tests the {@link ReminderRules} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReminderRulesTestCase extends ArchetypeServiceTest {

    /**
     * The reminder rules.
     */
    private ReminderRules rules;


    /**
     * Tests the {@link ReminderRules#markMatchingRemindersCompleted(Act)}
     * method, when invoked via the
     * <em>archetypeService.save.act.patientReminder.before</em> rule.
     */
    public void testMarkMatchingRemindersCompleted() {
        Lookup group1 = ReminderTestHelper.createReminderGroup();
        Lookup group2 = ReminderTestHelper.createReminderGroup();

        Party patient1 = TestHelper.createPatient();
        Party patient2 = TestHelper.createPatient();

        // create a reminder for patient1, with an entity.reminderType with
        // no lookup.reminderGroup
        Act reminder0 = createReminder(patient1);
        checkReminder(reminder0, ReminderStatus.IN_PROGRESS);

        // create a reminder for patient1, with an entity.reminderType with
        // group1 lookup.reminderGroup. Verify it has not changed reminder0
        Act reminder1 = createReminder(patient1, group1);
        checkReminder(reminder1, ReminderStatus.IN_PROGRESS);
        checkReminder(reminder0, ReminderStatus.IN_PROGRESS);

        // create a reminder for patient2, with an entity.reminderType with
        // group2 lookup.reminderGroup. Verify it has not changed reminder1
        Act reminder2 = createReminder(patient2, group2);
        checkReminder(reminder2, ReminderStatus.IN_PROGRESS);
        checkReminder(reminder1, ReminderStatus.IN_PROGRESS);

        // create a reminder for patient1, with an entity.reminderType with
        // group1 lookup.reminderGroup. Verify it marks reminder1 COMPLETED.
        Act reminder3 = createReminder(patient1, group1);
        checkReminder(reminder3, ReminderStatus.IN_PROGRESS);
        checkReminder(reminder1, ReminderStatus.COMPLETED);

        // create a reminder for patient2, with an entity.reminderType with
        // both group1 and group2 lookup.reminderGroup. Verify it marks
        // reminder2 COMPLETED.
        Act reminder4 = createReminder(patient2, group1, group2);
        checkReminder(reminder4, ReminderStatus.IN_PROGRESS);
        checkReminder(reminder2, ReminderStatus.COMPLETED);

        // create a reminder type with no group, and create 2 reminders using it.
        Entity reminderType = ReminderTestHelper.createReminderType();
        Act reminder5 = createReminder(patient1, reminderType);
        Act reminder6 = createReminder(patient2, reminderType);
        checkReminder(reminder5, ReminderStatus.IN_PROGRESS);
        checkReminder(reminder6, ReminderStatus.IN_PROGRESS);

        // now create a reminder for patient1. Verify it marks reminder5 COMPLETED
        Act reminder7 = createReminder(patient1, reminderType);
        checkReminder(reminder5, ReminderStatus.COMPLETED);
        checkReminder(reminder6, ReminderStatus.IN_PROGRESS);
        checkReminder(reminder7, ReminderStatus.IN_PROGRESS);
    }

    /**
     * Tests the {@link ReminderRules#calculateReminderDueDate(Date, Entity)}
     * method.
     */
    public void testCalculateReminderDueDate() {
        checkCalculateReminderDueDate(1, DateUnits.DAYS, "2007-01-01",
                                      "2007-01-02");
        checkCalculateReminderDueDate(2, DateUnits.WEEKS, "2007-01-01",
                                      "2007-01-15");
        checkCalculateReminderDueDate(2, DateUnits.MONTHS, "2007-01-01",
                                      "2007-03-01");
        checkCalculateReminderDueDate(5, DateUnits.YEARS, "2007-01-01",
                                      "2012-01-01");
    }

    /**
     * Tests the {@link ReminderRules#countReminders(Party)} method.
     * Requires <em>Reminder.hbm.xml</em>.
     */
    public void testCountReminders() {
        Party patient = TestHelper.createPatient();
        assertEquals(0, rules.countReminders(patient));
        int count = 5;
        Act[] reminders = new Act[count];
        for (int i = 0; i < count; ++i) {
            reminders[i] = createReminder(patient);
        }
        assertEquals(count, rules.countReminders(patient));

        Act reminder0 = reminders[0];
        reminder0.setStatus(ActStatus.COMPLETED);
        save(reminder0);
        assertEquals(count - 1, rules.countReminders(patient));

        Act reminder1 = reminders[1];
        reminder1.setStatus(ActStatus.CANCELLED);
        save(reminder1);
        assertEquals(count - 2, rules.countReminders(patient));
    }

    /**
     * Tests the {@link ReminderRules#countAlerts} method.
     * Requires <em>Reminder.hbm.xml</em>.
     */
    public void testCountAlerts() {
        Party patient = TestHelper.createPatient();
        Date date = new Date();
        assertEquals(0, rules.countAlerts(patient, date));
        int count = 5;
        Act[] alerts = new Act[count];
        for (int i = 0; i < count; ++i) {
            alerts[i] = createAlert(patient);
        }
        assertEquals(count, rules.countAlerts(patient, date));

        Act alert0 = alerts[0];
        alert0.setStatus(ActStatus.COMPLETED);
        save(alert0);
        assertEquals(count - 1, rules.countAlerts(patient, date));

        Act alert1 = alerts[1];
        alert1.setActivityEndTime(date);
        save(alert1);
        assertEquals(count - 2, rules.countAlerts(patient, date));
    }

    /**
     * Tests the {@link ReminderRules#isDue(Act, Date, Date)} method.
     */
    public void testIsDue() {
        Lookup group = ReminderTestHelper.createReminderGroup();
        Party patient = TestHelper.createPatient();
        Entity reminderType = ReminderTestHelper.createReminderType(
                1, DateUnits.MONTHS, group);
        Date start = java.sql.Date.valueOf("2007-01-01");
        Date due = rules.calculateReminderDueDate(start, reminderType);
        Act reminder = ReminderTestHelper.createReminder(patient, reminderType,
                                                         due);

        checkDue(reminder, null, null, true);
        checkDue(reminder, null, "2007-01-01", false);
        checkDue(reminder, "2007-01-01", null, true);
        checkDue(reminder, "2007-01-01", "2007-01-31", false);
        checkDue(reminder, "2007-01-01", "2007-02-01", true);

        // Now add a template to the reminderType, due 2 weeks after the current
        // due date.
        EntityRelationship reminderTypeTemplate = (EntityRelationship) create(
                ReminderArchetypes.REMINDER_TYPE_TEMPLATE);
        Entity template = (Entity) create("entity.documentTemplate");
        template.setName("XTestTemplate_" + System.currentTimeMillis());
        IMObjectBean bean = new IMObjectBean(reminderTypeTemplate);
        bean.setValue("reminderCount", 0);
        bean.setValue("interval", 2);
        bean.setValue("units", DateUnits.WEEKS);
        bean.setValue("source", reminderType.getObjectReference());
        bean.setValue("target", template.getObjectReference());
        save(reminderTypeTemplate, template);
        checkDue(reminder, "2007-01-01", "2007-02-14", false);
        checkDue(reminder, "2007-01-01", "2007-02-15", true);
    }

    /**
     * Tests the {@link ReminderRules#shouldCancel(Act, Date)} method.
     */
    public void testShouldCancel() {
        Lookup group = ReminderTestHelper.createReminderGroup();
        Party patient = TestHelper.createPatient();
        Entity reminderType = ReminderTestHelper.createReminderType(
                1, DateUnits.MONTHS, group);
        Date start = java.sql.Date.valueOf("2007-01-01");
        Act reminder = ReminderTestHelper.createReminder(patient, reminderType);
        reminder.setActivityStartTime(start);
        rules.calculateReminderDueDate(reminder);

        checkShouldCancel(reminder, "2007-01-01", false);
        checkShouldCancel(reminder, "2007-01-31", false);
        checkShouldCancel(reminder, "2007-02-01", true);

        // Now add a cancel interval to the reminderType, due 2 weeks after the
        // current due date.
        IMObjectBean bean = new IMObjectBean(reminderType);
        bean.setValue("cancelInterval", 2);
        bean.setValue("cancelUnits", DateUnits.WEEKS.toString());
        bean.save();

        checkShouldCancel(reminder, "2007-02-01", false);
        checkShouldCancel(reminder, "2007-02-14", false);
        checkShouldCancel(reminder, "2007-02-15", true);

        // Now set patient to deceased
        EntityBean patientBean = new EntityBean(patient);
        patientBean.setValue("deceased", true);
        patientBean.save();
        checkShouldCancel(reminder, "2007-02-01", true);

    }

    /**
     * Tests the {@link ReminderRules#getContact(Set<Contact>)} method.
     */
    public void testGetContact() {
        // create a patient, and owner. Remove default contacts from owner
        Party owner = TestHelper.createCustomer();
        Contact[] contacts = owner.getContacts().toArray(new Contact[owner.getContacts().size()]);
        for (Contact contact : contacts) {
            owner.removeContact(contact);
        }

        // add an email contact to the owner, and verify it is returned
        Contact email = createEmail();
        checkContact(owner, email, email);

        // add a location contact to the owner, and verify it is returned
        // instead of the email contact
        Contact location = createLocation(false);
        checkContact(owner, location, location);

        // add a preferred phone contact to the owner, and verify the location
        // contact is still returned
        Contact phone = createPhone(true);
        checkContact(owner, phone, location);

        // add a preferred location contact to the owner, and verify it is
        // returned instead of the non-preferred location contact
        Contact preferredLocation = createLocation(true);
        checkContact(owner, preferredLocation, preferredLocation);

        // add a REMINDER classification to the email contact and verify it is
        // returned instead of the preferred location contact
        Lookup reminder = TestHelper.getLookup("lookup.contactPurpose",
                                               "REMINDER");
        email.addClassification(reminder);
        checkContact(owner, email, email);

        // add a REMINDER classification to the location contact and verify it
        // is returned instead of the email contact
        preferredLocation.addClassification(reminder);
        checkContact(owner, preferredLocation, preferredLocation);
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        rules = new ReminderRules();
    }

    /**
     * Adds a contact to a customer and verifies the expected contact is
     * returned by {@link ReminderRules#getContact(Set<Contact>)}.
     *
     * @param customer the customer
     * @param contact  the contact to add
     * @param expected the expected contact
     */
    private void checkContact(Party customer, Contact contact,
                              Contact expected) {
        customer.addContact(contact);
        Contact c = rules.getContact(customer.getContacts());
        assertEquals(expected, c);
    }

    /**
     * Helper to create a reminder.
     *
     * @param patient the patient
     * @param groups  the reminder group classifications
     * @return a new reminder
     */
    private Act createReminder(Party patient, Lookup... groups) {
        Entity reminderType = ReminderTestHelper.createReminderType(groups);
        return createReminder(patient, reminderType);
    }

    /**
     * Helper to create a reminder.
     *
     * @param patient      the patient
     * @param reminderType the reminder type
     * @return a new reminder
     */
    private Act createReminder(Party patient, Entity reminderType) {
        return ReminderTestHelper.createReminder(patient, reminderType, new Date());
    }

    /**
     * Verifies a reminder has the expected state.
     * For COMPLETED status, checks that the 'completedDate' node is non-null.
     *
     * @param reminder the reminder
     * @param status   the expected reminder status
     */
    private void checkReminder(Act reminder, String status) {
        reminder = get(reminder);
        assertNotNull(reminder);
        assertEquals(status, reminder.getStatus());
        ActBean bean = new ActBean(reminder);
        Date date = bean.getDate("completedDate");
        if (ReminderStatus.COMPLETED.equals(status)) {
            assertNotNull(date);
        } else {
            assertNull(date);
        }
    }

    /**
     * Checks the {@link ReminderRules#calculateReminderDueDate(Date, Entity)}
     * method.
     *
     * @param defaultInterval the default reminder interval
     * @param defaultUnits    the interval units
     * @param startDate       the reminder start date
     * @param expectedDate    the expected due date
     */
    private void checkCalculateReminderDueDate(int defaultInterval,
                                               DateUnits defaultUnits,
                                               String startDate,
                                               String expectedDate) {
        Lookup group = ReminderTestHelper.createReminderGroup();
        Entity reminderType = ReminderTestHelper.createReminderType(
                defaultInterval, defaultUnits, group);
        Date start = java.sql.Date.valueOf(startDate);
        Date expected = java.sql.Date.valueOf(expectedDate);
        Date to = rules.calculateReminderDueDate(start, reminderType);
        assertEquals(expected, to);
    }

    /**
     * Checks if a reminder is due using
     * {@link ReminderRules#isDue(Act, Date, Date)}.
     *
     * @param reminder the reminder
     * @param fromDate the from date. May be <tt>null</tt>
     * @param toDate   the to date. May be <tt>null</tt>
     * @param expected the expected isDue result
     */
    private void checkDue(Act reminder, String fromDate, String toDate,
                          boolean expected) {
        Date from = (fromDate != null) ? java.sql.Date.valueOf(fromDate) : null;
        Date to = (toDate != null) ? java.sql.Date.valueOf(toDate) : null;
        assertEquals(expected, rules.isDue(reminder, from, to));
    }

    /**
     * Checks if a reminder should be cancelled using
     * {@link ReminderRules#shouldCancel(Act, Date)}.
     *
     * @param reminder the reminder
     * @param date     the date
     * @param expected the expected shouldCancel result
     */
    private void checkShouldCancel(Act reminder, String date,
                                   boolean expected) {
        assertEquals(expected, rules.shouldCancel(reminder,
                                                  java.sql.Date.valueOf(date)));
    }

    /**
     * Helper to create an email contact.
     *
     * @return a new email contact
     */
    private Contact createEmail() {
        Contact contact = (Contact) create(ContactArchetypes.EMAIL);
        IMObjectBean bean = new IMObjectBean(contact);
        bean.setValue("emailAddress", "foo@bar.com");
        bean.save();
        return contact;
    }

    /**
     * Helper to create a phone contact.
     *
     * @param preferred determines if it is the preferred contact
     * @return a new phone contact
     */
    private Contact createPhone(boolean preferred) {
        Contact contact = (Contact) create(ContactArchetypes.PHONE);
        IMObjectBean bean = new IMObjectBean(contact);
        bean.setValue("preferred", preferred);
        save(contact);
        return contact;
    }

    /**
     * Helper to create a location contact.
     *
     * @param preferred determines if it is the preferred contact
     * @return a new location contact
     */
    private Contact createLocation(boolean preferred) {
        Contact contact = (Contact) create(ContactArchetypes.LOCATION);
        IMObjectBean bean = new IMObjectBean(contact);
        bean.setValue("preferred", preferred);
        save(contact);
        return contact;
    }

    /**
     * Helper to create and save an <em>act.patientAlert</tt> for a patient.
     *
     * @param patient the patient
     * @return a new alert
     */
    private Act createAlert(Party patient) {
        Act act = (Act) create("act.patientAlert");
        ActBean bean = new ActBean(act);
        bean.addParticipation("participation.patient", patient);
        Lookup alertType
                = TestHelper.getLookup("lookup.alertType", "OTHER");
        bean.setValue("alertType", alertType.getCode());
        bean.save();
        return act;
    }

}
