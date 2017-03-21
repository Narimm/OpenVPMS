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

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.patient.reminder.ReminderType.GroupBy;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes.REMINDER_ITEMS;
import static org.openvpms.archetype.rules.patient.reminder.ReminderGroupingPolicy.ALL;
import static org.openvpms.archetype.rules.patient.reminder.ReminderGroupingPolicy.NONE;
import static org.openvpms.archetype.rules.patient.reminder.ReminderItemStatus.PENDING;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createEmailReminder;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createListReminder;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createPrintReminder;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createReminder;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createSMSReminder;
import static org.openvpms.archetype.rules.patient.reminder.ReminderType.GroupBy.CUSTOMER;
import static org.openvpms.archetype.rules.patient.reminder.ReminderType.GroupBy.PATIENT;

/**
 * Tests the {@link GroupingReminderIterator}.
 *
 * @author Tim Anderson
 */
public class GroupingReminderIteratorTestCase extends ArchetypeServiceTest {

    /**
     * Patient A email reminder 1.
     */
    private Act emailReminderA1;

    /**
     * Patient A SMS reminder 1.
     */
    private Act smsReminderA1;

    /**
     * Patient A email reminder 2.
     */
    private Act emailReminderA2;

    /**
     * Patient A SMS reminder 2.
     */
    private Act smsReminderA2;

    /**
     * Patient A print reminder 3.
     */
    private Act printReminderA3;

    /**
     * Patient B email reminder 1.
     */
    private Act emailReminderB1;

    /**
     * Patient B SMS reminder 1.
     */
    private Act smsReminderB1;

    /**
     * Patient B print reminder 2.
     */
    private Act printReminderB2;

    /**
     * Patient C email reminder 1.
     */
    private Act emailReminderC1;

    /**
     * Patient C email reminder 2.
     */
    private Act emailReminderC2;

    /**
     * Patient C list reminder 3.
     */
    private Act listReminderC3;

    /**
     * Patient C list reminder 4.
     */
    private Act listReminderC4;

    /**
     * Patient C list reminder 5.
     */
    private Act listReminderC5;

    /**
     * Patient C list reminder 6.
     */
    private Act listReminderC6;

    /**
     * The query factory.
     */
    private ReminderItemQueryFactory factory;

    /**
     * The reminder types.
     */
    private ReminderTypes reminderTypes;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        Date today = new Date();
        Date tomorrow = DateRules.getNextDate(today);
        Entity reminderType1 = createReminderType("PATIENT");
        Entity reminderType2 = createReminderType("CUSTOMER");
        Entity reminderType3 = createReminderType("PATIENT");
        Entity reminderType4 = createReminderType(null);
        Entity reminderType5 = createReminderType(null);
        Party customer1 = TestHelper.createCustomer("B", "B", true);
        Party customer2 = TestHelper.createCustomer("A", "A", true);
        Party patientA = TestHelper.createPatient("Spot", customer1, true);
        Party patientB = TestHelper.createPatient("Fido", customer1, true);
        Party patientC = TestHelper.createPatient("Rukus", customer2, true);

        emailReminderA1 = createEmailReminder(today, tomorrow, PENDING, 0);
        smsReminderA1 = createSMSReminder(today, tomorrow, PENDING, 0);
        createReminder(tomorrow, patientA, reminderType1, emailReminderA1, smsReminderA1);

        emailReminderA2 = createEmailReminder(today, tomorrow, PENDING, 0);
        smsReminderA2 = createSMSReminder(today, tomorrow, PENDING, 0);
        createReminder(tomorrow, patientA, reminderType1, emailReminderA2, smsReminderA2);

        printReminderA3 = createPrintReminder(today, tomorrow, PENDING, 0);
        createReminder(tomorrow, patientA, reminderType2, printReminderA3);

        emailReminderB1 = createEmailReminder(today, tomorrow, PENDING, 0);
        smsReminderB1 = createSMSReminder(today, tomorrow, PENDING, 0);
        createReminder(tomorrow, patientB, reminderType1, emailReminderB1, smsReminderB1);

        printReminderB2 = createPrintReminder(today, tomorrow, PENDING, 0);
        createReminder(tomorrow, patientB, reminderType2, printReminderB2);

        emailReminderC1 = createEmailReminder(today, tomorrow, PENDING, 0);
        createReminder(tomorrow, patientC, reminderType1, emailReminderC1);

        emailReminderC2 = createEmailReminder(today, tomorrow, PENDING, 0);
        createReminder(tomorrow, patientC, reminderType3, emailReminderC2);

        listReminderC3 = createListReminder(today, tomorrow, PENDING, 0);
        createReminder(tomorrow, patientC, reminderType2, listReminderC3);

        listReminderC4 = createListReminder(today, tomorrow, PENDING, 0);
        createReminder(tomorrow, patientC, reminderType4, listReminderC4);

        listReminderC5 = createListReminder(today, tomorrow, PENDING, 0);
        createReminder(tomorrow, patientC, reminderType5, listReminderC5);

        listReminderC6 = createListReminder(today, tomorrow, PENDING, 0);
        createReminder(tomorrow, patientC, reminderType2, listReminderC6);

        factory = new ReminderItemQueryFactory(REMINDER_ITEMS, new String[]{PENDING}, today, tomorrow);
        reminderTypes = new ReminderTypes(getArchetypeService());
    }

    /**
     * Tests the iterator.
     */
    @Test
    public void testIterator() {
        GroupingReminderIterator iterator = new GroupingReminderIterator(factory, reminderTypes, 10, ALL, ALL,
                                                                         getArchetypeService());
        check(iterator, PATIENT, emailReminderC1, emailReminderC2);      // customer=A, patient=Rukus, group by patient

        // NOTE: list reminders aren't grouped
        check(iterator, GroupBy.NONE, listReminderC3);                   // customer=A, patient=Rukus, group by customer
        check(iterator, GroupBy.NONE, listReminderC4);                   // customer=A, patient=Rukus, no group
        check(iterator, GroupBy.NONE, listReminderC5);                   // customer=A, patient=Rukus, no group
        check(iterator, GroupBy.NONE, listReminderC6);                   // customer=A, patient=Rukus, group by customer

        check(iterator, PATIENT, emailReminderB1);                       // customer=B, patient=Fido, group by patient
        check(iterator, PATIENT, emailReminderA1, emailReminderA2);      // customer=B, patient=Spot, group by patient
        check(iterator, CUSTOMER, printReminderB2, printReminderA3);     // customer=B, group by customer
        check(iterator, PATIENT, smsReminderB1);                         // customer=B, patient=Fido, group by patient
        check(iterator, PATIENT, smsReminderA1, smsReminderA2);          // customer=B, patient=Spot, group by patient
    }

    /**
     * Tests iteration when reminder types indicate to group by customer, but grouping by customer is disabled.
     */
    @Test
    public void testIteratorGroupByCustomerDisabled() {
        GroupingReminderIterator iterator = new GroupingReminderIterator(factory, reminderTypes, 10, NONE, ALL,
                                                                         getArchetypeService());
        check(iterator, PATIENT, emailReminderC1, emailReminderC2);      // customer=A, patient=Rukus, group by patient
        check(iterator, GroupBy.NONE, listReminderC3);                   // customer=A, patient=Rukus, group by customer
        check(iterator, GroupBy.NONE, listReminderC4);                   // customer=A, patient=Rukus, no group
        check(iterator, GroupBy.NONE, listReminderC5);                   // customer=A, patient=Rukus, no group
        check(iterator, GroupBy.NONE, listReminderC6);                   // customer=A, patient=Rukus, group by customer
        check(iterator, PATIENT, emailReminderB1);                       // customer=B, patient=Fido, group by patient
        check(iterator, PATIENT, emailReminderA1, emailReminderA2);      // customer=B, patient=Spot, group by patient
        check(iterator, GroupBy.NONE, printReminderB2);                  // customer=B, group by customer
        check(iterator, GroupBy.NONE, printReminderA3);                  // customer=B, group by customer
        check(iterator, PATIENT, smsReminderB1);                         // customer=B, patient=Fido, group by patient
        check(iterator, PATIENT, smsReminderA1, smsReminderA2);          // customer=B, patient=Spot, group by patient
    }

    /**
     * Tests iteration when reminder types indicate to group by patient, but grouping by patient is disabled.
     */
    @Test
    public void testIteratorGroupByPatientDisabled() {
        GroupingReminderIterator iterator = new GroupingReminderIterator(factory, reminderTypes, 10, ALL, NONE,
                                                                         getArchetypeService());
        check(iterator, GroupBy.NONE, emailReminderC1);                // customer=A, patient=Rukus, group by patient
        check(iterator, GroupBy.NONE, emailReminderC2);                // customer=A, patient=Rukus, group by patient

        // NOTE: list reminders aren't grouped
        check(iterator, GroupBy.NONE, listReminderC3);                 // customer=A, patient=Rukus, group by customer
        check(iterator, GroupBy.NONE, listReminderC4);                 // customer=A, patient=Rukus, no group
        check(iterator, GroupBy.NONE, listReminderC5);                 // customer=A, patient=Rukus, no group
        check(iterator, GroupBy.NONE, listReminderC6);                 // customer=A, patient=Rukus, group by customer

        check(iterator, GroupBy.NONE, emailReminderB1);                   // customer=B, patient=Fido, group by patient
        check(iterator, GroupBy.NONE, emailReminderA1);                   // customer=B, patient=Spot, group by patient
        check(iterator, GroupBy.NONE, emailReminderA2);                   // customer=B, patient=Spot, group by patient
        check(iterator, CUSTOMER, printReminderB2, printReminderA3);  // customer=B, group by customer
        check(iterator, GroupBy.NONE, smsReminderB1);                     // customer=B, patient=Fido, group by patient
        check(iterator, GroupBy.NONE, smsReminderA1);                     // customer=B, patient=Spot, group by patient
        check(iterator, GroupBy.NONE, smsReminderA2);                     // customer=B, patient=Spot, group by patient
    }

    /**
     * Tests iteration when reminder types indicate to group by customer and patient, but grouping by SMS is disabled.
     */
    @Test
    public void testIteratorGroupBySMSDisabled() {
        ReminderGroupingPolicy noSMS = ReminderGroupingPolicy.getPolicy(true, true, false);
        GroupingReminderIterator iterator = new GroupingReminderIterator(factory, reminderTypes, 10, noSMS, noSMS,
                                                                         getArchetypeService());
        check(iterator, PATIENT, emailReminderC1, emailReminderC2);  // customer=A, patient=Rukus

        // NOTE: list reminders aren't grouped, despite reminder type configuration
        check(iterator, GroupBy.NONE, listReminderC3);                   // customer=A, patient=Rukus, group by customer
        check(iterator, GroupBy.NONE, listReminderC4);                   // customer=A, patient=Rukus, no group
        check(iterator, GroupBy.NONE, listReminderC5);                   // customer=A, patient=Rukus, no group
        check(iterator, GroupBy.NONE, listReminderC6);                   // customer=A, patient=Rukus, group by customer

        check(iterator, PATIENT, emailReminderB1);                 // customer=B, patient=Fido, group by patient
        check(iterator, PATIENT, emailReminderA1, emailReminderA2);// customer=B, patient=Spot, group by patient
        check(iterator, CUSTOMER, printReminderB2, printReminderA3);// customer=B, group by customer
        check(iterator, GroupBy.NONE, smsReminderB1);                     // customer=B, patient=Fido, group by patient
        check(iterator, GroupBy.NONE, smsReminderA1);                     // customer=B, patient=Spot, group by patient
        check(iterator, GroupBy.NONE, smsReminderA2);                     // customer=B, patient=Spot, group by patient
    }

    /**
     * Verifies that the iterator returns the correct items when the acts change.
     */
    @Test
    public void testUpdate() {
        GroupingReminderIterator iterator = new GroupingReminderIterator(factory, reminderTypes, 2, ALL, ALL,
                                                                         getArchetypeService());
        complete(iterator, PATIENT, emailReminderC1, emailReminderC2);  // customer=A, patient=Rukus, group by patient

        // NOTE: list reminders aren't grouped
        complete(iterator, GroupBy.NONE, listReminderC3);               // customer=A, patient=Rukus, group by customer
        complete(iterator, GroupBy.NONE, listReminderC4);               // customer=A, patient=Rukus, no group
        complete(iterator, GroupBy.NONE, listReminderC5);               // customer=A, patient=Rukus, no group
        complete(iterator, GroupBy.NONE, listReminderC6);               // customer=A, patient=Rukus, group by customer

        complete(iterator, PATIENT, emailReminderB1);                   // customer=B, patient=Fido, group by patient
        complete(iterator, PATIENT, emailReminderA1, emailReminderA2);  // customer=B, patient=Spot, group by patient
        complete(iterator, CUSTOMER, printReminderB2, printReminderA3);  // customer=B, group by customer
        complete(iterator, PATIENT, smsReminderB1);                     // customer=B, patient=Fido, group by patient
        complete(iterator, PATIENT, smsReminderA1, smsReminderA2);      // customer=B, patient=Spot, group by patient
    }

    /**
     * Checks that the iterator matches that expected.
     *
     * @param iterator the iterator
     * @param groupBy  the expected reminder type group by specification
     * @param expected the expected acts
     */
    private void check(GroupingReminderIterator iterator, GroupBy groupBy, Act... expected) {
        List<Act> list = Arrays.asList(expected);
        boolean found = false;
        while (iterator.hasNext()) {
            Reminders sets = iterator.next();
            List<Act> next = getItems(sets.getReminders());
            if (next.equals(list)) {
                assertEquals(groupBy, sets.getGroupBy());
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    /**
     * Checks that the iterator returns the expected reminders, sets their status to {@code COMPLETED} and updates
     * the iterator.
     *
     * @param iterator the iterator
     * @param groupBy  the expected reminder type group by specification
     * @param expected the expected acts
     */
    private void complete(GroupingReminderIterator iterator, GroupBy groupBy, Act... expected) {
        check(iterator, groupBy, expected);
        for (Act act : expected) {
            act.setStatus(ReminderItemStatus.COMPLETED);
            save(act);
        }
        iterator.updated();
    }

    /**
     * Returns the items from a group of events.
     *
     * @param events the reminder events
     * @return the reminder items
     */
    private List<Act> getItems(List<ReminderEvent> events) {
        List<Act> result = new ArrayList<>();
        for (ReminderEvent event : events) {
            result.add(event.getItem());
        }
        return result;
    }

    /**
     * Creates a reminder type.
     *
     * @param groupBy the grouping strategy. May be {@code null}
     * @return a new reminder type
     */
    private Entity createReminderType(String groupBy) {
        Entity result = ReminderTestHelper.createReminderType(1, DateUnits.YEARS);
        IMObjectBean bean = new IMObjectBean(result);
        bean.setValue("groupBy", groupBy);
        bean.save();
        return result;
    }
}
