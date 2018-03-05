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

package org.openvpms.archetype.rules.patient.reminder;

import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.practice.Location;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createEmailReminder;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createExportReminder;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createListReminder;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createPrintReminder;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createReminder;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createReminderType;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createSMSReminder;
import static org.openvpms.archetype.test.TestHelper.createPatient;

/**
 * Tests the {@link ReminderItemQueryFactory}.
 *
 * @author Tim Anderson
 */
public class ReminderItemQueryFactoryTestCase extends ArchetypeServiceTest {

    /**
     * Verifies that the expected items are returned by the query.
     */
    @Test
    public void testQuery() {
        Entity reminderType = createReminderType(1, DateUnits.YEARS);
        Party customer = TestHelper.createCustomer();
        Party patient = createPatient(customer);
        Date date = DateRules.getToday();

        Act email1 = createEmailReminder(date, date, ReminderItemStatus.PENDING, 0);
        Act sms1 = createSMSReminder(date, date, ReminderItemStatus.PENDING, 0);
        Act print1 = createPrintReminder(date, date, ReminderItemStatus.PENDING, 0);
        Act export1 = createExportReminder(date, date, ReminderItemStatus.PENDING, 0);
        Act list1 = createListReminder(date, date, ReminderItemStatus.PENDING, 0);
        Act reminder1 = createReminder(date, patient, reminderType, email1, sms1, print1, export1, list1);

        Act email2 = createPrintReminder(date, date, ReminderItemStatus.PENDING, 0);
        Act print2 = createPrintReminder(date, date, ReminderItemStatus.PENDING, 0);
        Act reminder2 = createReminder(date, patient, reminderType, email2, print2);
        reminder2.setStatus(ActStatus.COMPLETED);
        save(reminder2);

        Act email3 = createPrintReminder(date, date, ReminderItemStatus.PENDING, 0);
        Act list3 = createListReminder(date, date, ReminderItemStatus.PENDING, 0);
        Act reminder3 = createReminder(date, patient, reminderType, email3, list3);
        reminder3.setStatus(ActStatus.CANCELLED);
        save(reminder3);

        ReminderItemQueryFactory query = new ReminderItemQueryFactory();
        checkExists(query, reminder1, patient, customer, email1, sms1, print1, export1, list1);
        checkNotExists(query, reminder2, patient, customer, email2, print2);
        checkNotExists(query, reminder3, patient, customer, email3, list3);

        // change query to only return email items
        query.setArchetype(ReminderArchetypes.EMAIL_REMINDER);
        checkExists(query, reminder1, patient, customer, email1);
        checkNotExists(query, reminder1, patient, customer, sms1, print1, export1, list1);
        checkNotExists(query, reminder2, patient, customer, email2, print2);
        checkNotExists(query, reminder3, patient, customer, email3, list3);

        // now query by status
        email1.setStatus(ReminderItemStatus.ERROR);
        save(email1);

        query.setArchetype(ReminderArchetypes.REMINDER_ITEMS);
        query.setStatus(ReminderItemStatus.PENDING);
        checkExists(query, reminder1, patient, customer, sms1, print1, export1, list1);
        checkNotExists(query, reminder1, patient, customer, email1);
        checkNotExists(query, reminder2, patient, customer, email2, print2);
        checkNotExists(query, reminder3, patient, customer, email3, list3);

        query.setStatus(ReminderItemStatus.ERROR);
        checkExists(query, reminder1, patient, customer, email1);
        checkNotExists(query, reminder1, patient, customer, sms1, print1, export1, list1);
        checkNotExists(query, reminder2, patient, customer, email2, print2);
        checkNotExists(query, reminder3, patient, customer, email3, list3);
    }

    /**
     * Verifies that reminder items can be restricted to a particular customer.
     */
    @Test
    public void testQueryByCustomer() {
        Entity reminderType = createReminderType(1, DateUnits.YEARS);
        Party customer1 = TestHelper.createCustomer();
        Party patient1 = createPatient(customer1);
        Party customer2 = TestHelper.createCustomer();
        Party patient2 = createPatient(customer2);

        Date date = DateRules.getToday();

        Act email1 = createEmailReminder(date, date, ReminderItemStatus.PENDING, 0);
        Act sms1 = createSMSReminder(date, date, ReminderItemStatus.PENDING, 0);
        Act print1 = createPrintReminder(date, date, ReminderItemStatus.PENDING, 0);
        Act export1 = createExportReminder(date, date, ReminderItemStatus.PENDING, 0);
        Act list1 = createListReminder(date, date, ReminderItemStatus.PENDING, 0);
        Act reminder1 = createReminder(date, patient1, reminderType, email1, sms1, print1, export1, list1);

        Act email2 = createPrintReminder(date, date, ReminderItemStatus.PENDING, 0);
        Act print2 = createPrintReminder(date, date, ReminderItemStatus.PENDING, 0);
        Act reminder2 = createReminder(date, patient2, reminderType, email2, print2);

        ReminderItemQueryFactory query = new ReminderItemQueryFactory();
        query.setCustomer(customer1);
        checkExists(query, reminder1, patient1, customer1, email1, sms1, print1, export1, list1);
        checkNotExists(query, reminder2, patient2, customer2, email2, print2);

        query.setCustomer(customer2);
        checkNotExists(query, reminder1, patient1, customer1, email1, sms1, print1, export1, list1);
        checkExists(query, reminder2, patient2, customer2, email2, print2);
    }

    /**
     * Verifies that reminder items can be excluded by customer location.
     */
    @Test
    public void testQueryByLocation() {
        Date date = DateRules.getToday();
        Entity reminderType = createReminderType(1, DateUnits.YEARS);

        // create a new reminder starting on startDate
        Party customer1 = TestHelper.createCustomer();
        Party customer2 = TestHelper.createCustomer();
        Party customer3 = TestHelper.createCustomer();
        Party patient1 = TestHelper.createPatient(customer1);
        Party patient2 = TestHelper.createPatient(customer2);
        Party patient3 = TestHelper.createPatient(customer3);

        Act email1 = createEmailReminder(date, date, ReminderItemStatus.PENDING, 0);
        Act reminder1 = createReminder(date, patient1, reminderType, email1);

        Act email2 = createEmailReminder(date, date, ReminderItemStatus.PENDING, 0);
        Act reminder2 = createReminder(date, patient2, reminderType, email2);

        Act email3 = createEmailReminder(date, date, ReminderItemStatus.PENDING, 0);
        Act reminder3 = createReminder(date, patient3, reminderType, email3);

        // query reminders with reminderType. It should pick up all reminders
        ReminderItemQueryFactory query = new ReminderItemQueryFactory();
        checkExists(query, reminder1, patient1, customer1, email1);
        checkExists(query, reminder2, patient2, customer2, email2);
        checkExists(query, reminder3, patient3, customer3, email3);

        // now query reminders for customers with no location. Should still pick up everything
        query.setLocation(Location.NONE);
        checkExists(query, reminder1, patient1, customer1, email1);
        checkExists(query, reminder2, patient2, customer2, email2);
        checkExists(query, reminder3, patient3, customer3, email3);

        // link location1 to customer1. Should now be excluded from NONE query
        Party location1 = TestHelper.createLocation();
        IMObjectBean bean = new IMObjectBean(customer1);
        bean.addNodeTarget("practice", location1);
        bean.save();

        checkNotExists(query, reminder1, patient1, customer1, email1);
        checkExists(query, reminder2, patient2, customer2, email2);
        checkExists(query, reminder3, patient3, customer3, email3);

        // now constrain on location1
        query.setLocation(new Location(location1));
        checkExists(query, reminder1, patient1, customer1, email1);
        checkNotExists(query, reminder2, patient2, customer2, email2);
        checkNotExists(query, reminder3, patient3, customer3, email3);
    }

    /**
     * Verifies that the expected items are returned by the query.
     *
     * @param factory  the query factory
     * @param reminder the reminder
     * @param patient  the patient
     * @param customer the customer
     * @param items    the expected items
     */
    private void checkExists(ReminderItemQueryFactory factory, Act reminder, Party patient, Party customer,
                             Act... items) {
        int matches = getMatches(factory, reminder, patient, customer, items);
        assertEquals(items.length, matches);
    }

    /**
     * Verifies that items are not returned by the query.
     *
     * @param factory  the query factory
     * @param reminder the reminder
     * @param patient  the patient
     * @param customer the customer
     * @param items    the items that shouldn't be returned
     */
    private void checkNotExists(ReminderItemQueryFactory factory, Act reminder, Party patient, Party customer,
                                Act... items) {
        int matches = getMatches(factory, reminder, patient, customer, items);
        assertEquals(0, matches);
    }

    /**
     * Counts matches of items returned by a query.
     *
     * @param factory  the query factory
     * @param reminder the reminder
     * @param patient  the patient
     * @param customer the customer
     * @param items    the items
     * @return the no. of matches
     */
    private int getMatches(ReminderItemQueryFactory factory, Act reminder, Party patient, Party customer, Act[] items) {
        int matches = 0;
        Set<Act> itemSet = new HashSet<>(Arrays.asList(items));

        ObjectSetQueryIterator iterator = new ObjectSetQueryIterator(factory.createQuery());
        while (iterator.hasNext()) {
            ObjectSet set = iterator.next();
            Act actualReminder = (Act) set.get("reminder");
            if (actualReminder.equals(reminder)) {
                Act actualItem = (Act) set.get("item");
                Party actualPatient = (Party) set.get("patient");
                Party actualCustomer = (Party) set.get("customer");
                if (itemSet.contains(actualItem) && actualPatient.equals(patient) && actualCustomer.equals(customer)) {
                    matches++;
                }
            }
        }
        return matches;
    }
}
