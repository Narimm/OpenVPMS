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

import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.util.Date;


/**
 * Tests the {@link ReminderRules} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReminderRulesTestCase extends ArchetypeServiceTest {

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
        // group1 lookup.reminderGroup. Verify it has not changed
        // reminder0
        Act reminder1 = createReminder(patient1, group1);
        checkReminder(reminder1, ReminderStatus.IN_PROGRESS);
        checkReminder(reminder0, ReminderStatus.IN_PROGRESS, true);

        // create a reminder for patient2, with an entity.reminderType with
        // group2 lookup.reminderGroup. Verify it has not changed
        // reminder1
        Act reminder2 = createReminder(patient2, group2);
        checkReminder(reminder2, ReminderStatus.IN_PROGRESS);
        checkReminder(reminder1, ReminderStatus.IN_PROGRESS, true);

        // create a reminder for patient1, with an entity.reminderType with
        // group1 lookup.reminderGroup. Verify it marks reminder1
        // COMPLETED.
        Act reminder3 = createReminder(patient1, group1);
        checkReminder(reminder3, ReminderStatus.IN_PROGRESS);
        checkReminder(reminder1, ReminderStatus.COMPLETED, true);

        // create a reminder for patient2, with an entity.reminderType with
        // both group1 and group2 lookup.reminderGroup. Verify it marks
        // reminder2 COMPLETED.
        Act reminder4 = createReminder(patient2, group1, group2);
        checkReminder(reminder4, ReminderStatus.IN_PROGRESS);
        checkReminder(reminder2, ReminderStatus.COMPLETED, true);
    }

    /**
     * Tests the {@link ReminderRules#getContact(Party)} method.
     */
    public void testGetContact() {
        // create a patient, and owner. Remove default contacts from owner
        ReminderRules rules = new ReminderRules();
        Party owner = TestHelper.createCustomer();
        Party patient = TestHelper.createPatient(owner);
        for (Contact contact : owner.getContacts().toArray(new Contact[0])) {
            owner.removeContact(contact);
        }

        // add an email contact to the owner, and verify it is returned
        owner.addContact(createEmail());
        save(owner);
        Contact email = rules.getContact(patient);
        assertTrue(TypeHelper.isA(email, "contact.email"));

        // add an preferred phone contact to the owner, and verify it is
        // returned instead of the email contact
        owner.addContact(createPhone(true));
        save(owner);
        Contact contact = rules.getContact(patient);
        assertTrue(TypeHelper.isA(contact, "contact.phoneNumber"));

        // add a location contact to the owner, and verify the preferred phone
        // contact is still returned
        owner.addContact(createLocation(false));
        save(owner);
        contact = rules.getContact(patient);
        assertTrue(TypeHelper.isA(contact, "contact.phoneNumber"));

        // add a preferred location contact to the owner, and verify it is
        // returned instead of the phone contact
        owner.addContact(createLocation(true));
        save(owner);
        Contact location = rules.getContact(patient);
        assertTrue(TypeHelper.isA(location, "contact.location"));

        // add a REMINDER classification to the email contact and verify it is
        // returned instead of the preferred location contact
        email = (Contact) get(email);
        Lookup reminder = TestHelper.getClassification("lookup.contactPurpose",
                                                       "REMINDER");
        email.addClassification(reminder);
        save(email);
        contact = rules.getContact(patient);
        assertTrue(TypeHelper.isA(contact, "contact.email"));

        // add a REMINDER classification to the location contact and verify it
        // is returned instead of the email contact
        location = (Contact) get(location);
        location.addClassification(reminder);
        save(location);
        contact = rules.getContact(patient);
        assertTrue(TypeHelper.isA(contact, "contact.location"));
    }

    /**
     * Helper to create a reminder.
     *
     * @param patient the patient
     * @param groups  the reminder group classifications
     * @return a new reminder
     */
    private Act createReminder(Party patient, Lookup ... groups) {
        Entity reminderType = ReminderTestHelper.createReminderType(groups);
        return ReminderTestHelper.createReminder(patient, reminderType,
                                                 new Date());
    }

    /**
     * Verifies a reminder has the expected state.
     * For COMPLETED status, checks that the 'completedDate' node is non-null.
     *
     * @param reminder the reminder
     * @param status   the expected reminder status
     */
    private void checkReminder(Act reminder, String status) {
        checkReminder(reminder, status, false);
    }

    /**
     * Verifies a reminder has the expected state.
     * For COMPLETED status, checks that the 'completedDate' node is non-null.
     *
     * @param reminder the reminder
     * @param status   the expected reminder status
     * @param reload   if <code>true</code>, reload the act before checking
     */
    private void checkReminder(Act reminder, String status,
                               boolean reload) {
        if (reload) {
            reminder = (Act) get(reminder);
            assertNotNull(reminder);
        }
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
     * Helper to create an email contact.
     *
     * @return a new email contact
     */
    private Contact createEmail() {
        Contact contact = (Contact) create("contact.email");
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
        Contact contact = (Contact) create("contact.phoneNumber");
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
        Contact contact = (Contact) create("contact.location");
        IMObjectBean bean = new IMObjectBean(contact);
        bean.setValue("preferred", preferred);
        save(contact);
        return contact;
    }

}
