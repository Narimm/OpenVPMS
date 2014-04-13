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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.archetype.rules.patient.reminder;

import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.practice.Location;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.addTemplate;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.assertEquals;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.assertTrue;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createReminder;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createReminderType;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createReminders;
import static org.openvpms.archetype.test.TestHelper.getDate;


/**
 * Tests the {@link DueReminderQuery} class.
 *
 * @author Tim Anderson
 */
public class DueReminderQueryTestCase extends ArchetypeServiceTest {

    /**
     * The patient rules.
     */
    @Autowired
    private PatientRules patientRules;


    /**
     * Tests the query, where no properties are constrained. This should return exactly the same acts as
     * {@link ReminderQuery}.
     */
    @Test
    public void testUnconstrainedQuery() {
        Entity reminderType = createReminderType();
        createReminders(10, reminderType); // create some reminders

        ReminderQuery query = new ReminderQuery(getArchetypeService());
        DueReminderQuery dueQuery = new DueReminderQuery(getArchetypeService());
        Set<Act> expected = getReminders(query.query());
        Set<Act> actual = getReminders(dueQuery.query());
        checkReminders(expected, actual);
    }

    /**
     * Checks querying by reminder type and due date.
     */
    @Test
    public void testQueryByReminderTypeAndDate() {
        // create a reminder type with 3 months interval
        Entity reminderType = createReminderType(3, DateUnits.MONTHS);

        // create some patients
        Party customer = TestHelper.createCustomer();
        Party patient1 = TestHelper.createPatient(customer);
        Party patient2 = TestHelper.createPatient(customer);
        Party patient3 = TestHelper.createPatient(customer);

        // create reminders with the specified start dates
        Date startDate1 = getDate("2009-10-16");
        Date startDate2 = getDate("2009-11-16");
        Date startDate3 = getDate("2009-12-16");
        Act reminder1 = createReminder(patient1, reminderType, startDate1);
        Act reminder2 = createReminder(patient2, reminderType, startDate2);
        Act reminder3 = createReminder(patient3, reminderType, startDate3);

        // verify the due dates have been calculated correctly
        Date dueDate1 = getDate("2010-01-16");
        Date dueDate2 = getDate("2010-02-16");
        Date dueDate3 = getDate("2010-03-16");
        assertEquals(dueDate1.getTime(), reminder1.getActivityEndTime().getTime());
        assertEquals(dueDate2.getTime(), reminder2.getActivityEndTime().getTime());
        assertEquals(dueDate3.getTime(), reminder3.getActivityEndTime().getTime());

        // check query constrained only by reminderType. All 3 reminders should be returned
        DueReminderQuery query = new DueReminderQuery(getArchetypeService());
        query.setReminderType(reminderType);
        checkReminders(query, reminder1, reminder2, reminder3);

        // exclude reminders after 1/3/10
        query.setTo(getDate("2010-03-01"));
        checkReminders(query, reminder1, reminder2);

        // exclude reminders before 1/2/10
        query.setFrom(getDate("2010-02-01"));
        checkReminders(query, reminder2);

        // now set the cancel date. All reminders due prior to this will now be returned, so they can be cancelled
        query.setCancelDate(getDate("2010-02-01"));
        checkReminders(query, reminder1, reminder2);
    }

    /**
     * Tests querying where are reminder's reminderCount is incremented and therefore due date changes.
     */
    @Test
    public void testQueryWithReminderCount() {
        ReminderRules rules = new ReminderRules(getArchetypeService(), patientRules);

        Date startDate = getDate("2009-10-16");
        Date expected1stDueDate = getDate("2010-01-16");  // 3 months after start

        // create a new reminder type with two templates:
        // . reminderCount=0, 0 week overdue interval
        // . reminderCount=1, 6 week overdue interval 
        Entity reminderType = createReminderType(3, DateUnits.MONTHS, 12, DateUnits.MONTHS);
        addTemplate(reminderType, 0, 0, DateUnits.WEEKS);
        addTemplate(reminderType, 1, 6, DateUnits.WEEKS);

        // create a new reminder starting on startDate
        Party customer = TestHelper.createCustomer();
        Party patient = TestHelper.createPatient(customer);
        Act reminder = createReminder(patient, reminderType, startDate);

        // verify the due date matches that expected
        Date dueDate = reminder.getActivityEndTime();
        assertEquals(expected1stDueDate.getTime(), dueDate.getTime());

        // query reminders with reminderType. As there is no date range specified, it should pick up all reminders
        DueReminderQuery query = new DueReminderQuery(getArchetypeService());
        query.setReminderType(reminderType);

        checkReminders(query, reminder);

        // now specify the date range, and verify it still picks up the reminder
        query.setFrom(getDate("2010-01-01"));
        query.setTo(getDate("2010-01-31"));
        checkReminders(query, reminder);

        rules.updateReminder(reminder, new Date()); // update the reminder, incrementing the reminderCount
        Date expected2ndDueDate = getDate("2010-02-27");
        dueDate = rules.getNextDueDate(reminder);
        assertEquals(expected2ndDueDate.getTime(), dueDate.getTime());

        // no reminder should be found, as it should now be due on 27/2/10
        checkReminders(query);

        // change the date range to include the expected 2nd due date
        query.setFrom(getDate("2010-02-01"));
        query.setTo(getDate("2010-03-01"));
        checkReminders(query, reminder); // verify the reminder is found
    }

    /**
     * Tests the {@link DueReminderQuery#setLocation(Location)} methods.
     */
    @Test
    public void testQueryByLocation() {
        Date startDate = getDate("2009-10-16");

        // create a new reminder type with two templates:
        // . reminderCount=0, 0 week overdue interval
        // . reminderCount=1, 6 week overdue interval
        Entity reminderType = createReminderType(3, DateUnits.MONTHS, 12, DateUnits.MONTHS);
        addTemplate(reminderType, 0, 0, DateUnits.WEEKS);
        addTemplate(reminderType, 1, 6, DateUnits.WEEKS);

        // create a new reminder starting on startDate
        Party customer1 = TestHelper.createCustomer();
        Party customer2 = TestHelper.createCustomer();
        Party customer3 = TestHelper.createCustomer();
        Party patient1 = TestHelper.createPatient(customer1);
        Party patient2 = TestHelper.createPatient(customer2);
        Party patient3 = TestHelper.createPatient(customer3);
        Act reminder1 = createReminder(patient1, reminderType, startDate);
        Act reminder2 = createReminder(patient2, reminderType, startDate);
        Act reminder3 = createReminder(patient3, reminderType, startDate);

        // query reminders with reminderType. As there is no date range specified, it should pick up all reminders
        DueReminderQuery query = new DueReminderQuery(getArchetypeService());
        query.setReminderType(reminderType);
        query.setLocation(Location.NONE);

        checkReminders(query, reminder1, reminder2, reminder3);

        Party location1 = TestHelper.createLocation();
        EntityBean bean = new EntityBean(customer1);
        bean.addNodeTarget("practice", location1);
        bean.save();

        query.setLocation(new Location(location1));
        checkReminders(query, reminder1);
    }

    /**
     * Helper to get all reminders from an <tt>Iterable</tt>.
     *
     * @param iterable the iterable
     * @return a set of reminders
     */
    private Set<Act> getReminders(Iterable<Act> iterable) {
        List<Act> list = new ArrayList<Act>();
        for (Act act : iterable) {
            assertTrue(TypeHelper.isA(act, ReminderArchetypes.REMINDER));
            assertEquals(ActStatus.IN_PROGRESS, act.getStatus());
            list.add(act);
        }
        Set<Act> result = new HashSet<Act>(list);
        assertEquals(list.size(), result.size()); // should be no duplicates
        return result;
    }

    /**
     * Checks that the reminders returned by a query match the expected result.
     *
     * @param query    the query
     * @param expected the expected results
     */
    private void checkReminders(DueReminderQuery query, Act... expected) {
        Set<Act> results = new HashSet<Act>(Arrays.asList(expected));
        assertEquals(expected.length, results.size());
        checkReminders(results, getReminders(query.query()));
    }

    /**
     * Checks that the set of expected reminders match the actual results.
     *
     * @param expected the expected reminders
     * @param actual   the actual reminders
     */
    private void checkReminders(Set<Act> expected, Set<Act> actual) {
        assertEquals(expected.size(), actual.size());
        assertEquals(expected, actual);
    }
}
