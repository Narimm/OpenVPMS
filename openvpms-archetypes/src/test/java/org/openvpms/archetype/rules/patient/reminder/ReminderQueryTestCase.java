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
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.practice.Location;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.RelationalOp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Tests the {@link ReminderQuery} class.
 *
 * @author Tim Anderson
 */
public class ReminderQueryTestCase extends ArchetypeServiceTest {

    /**
     * Verifies that a query with no constraints returns all IN_PROGRESS
     * reminders.
     */
    @Test
    public void testQuery() {
        Entity reminderType = ReminderTestHelper.createReminderType();
        ReminderTestHelper.createReminders(10, reminderType); // create some reminders

        int initialCount = countReminders(null, null, false);
        ReminderQuery query = new ReminderQuery(getArchetypeService());
        List<Act> reminders = getReminders(query);
        assertEquals(initialCount, reminders.size());
        for (Act act : reminders) {
            assertTrue(TypeHelper.isA(act, ReminderArchetypes.REMINDER));
            assertEquals(ActStatus.IN_PROGRESS, act.getStatus());
        }
    }

    /**
     * Verifies that IN_PROGRESS reminders can be queried for a particular
     * reminder type.
     */
    @Test
    public void testQueryReminderType() {
        final int count = 10;
        Entity reminderType = ReminderTestHelper.createReminderType();
        ReminderTestHelper.createReminders(count, reminderType);

        ReminderQuery query = new ReminderQuery(getArchetypeService());
        query.setReminderType(reminderType);
        List<Act> reminders = getReminders(query);
        assertEquals(count, reminders.size());
    }

    /**
     * Verifies that IN_PROGRESS reminders can be queried for a date range.
     */
    @Test
    public void testQueryDateRange() {
        final int count = 10;

        Entity reminderType = ReminderTestHelper.createReminderType();
        Party customer = TestHelper.createCustomer();
        Party patient = TestHelper.createPatient(customer);

        Calendar calendar = new GregorianCalendar();
        calendar.set(1980, Calendar.JANUARY, 1);
        Date dueFrom = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, count);
        Date dueTo = calendar.getTime();

        // add some data
        for (int i = 0; i < count; ++i) {
            ReminderTestHelper.createReminderWithDueDate(patient, reminderType, dueFrom);
        }

        // now determine the no. of acts with a due date in the date range
        int rangeCount = countReminders(dueFrom, dueTo, false);

        // now verify that the query gives the same count
        ReminderQuery query = new ReminderQuery(getArchetypeService());
        query.setFrom(dueFrom);
        query.setTo(dueTo);
        List<Act> reminders = getReminders(query);
        assertEquals(rangeCount, reminders.size());
    }

    /**
     * Verifies that IN_PROGRESS reminders can be queried for a reminder type
     * and date range.
     */
    @Test
    public void testQueryReminderTypeDateRange() {
        final int count = 10;
        Entity reminderType = ReminderTestHelper.createReminderType();

        Calendar calendar = new GregorianCalendar();
        calendar.set(1980, Calendar.JANUARY, 1);
        Date dueFrom = calendar.getTime();

        for (int i = 0; i < count; ++i) {
            calendar.set(Calendar.DAY_OF_MONTH, i + 1);
            Date dueDate = calendar.getTime();
            Party customer = TestHelper.createCustomer();
            Party patient = TestHelper.createPatient(customer);
            ReminderTestHelper.createReminderWithDueDate(patient, reminderType, dueDate);
        }
        // query a subset of the dates just added
        calendar.set(Calendar.DAY_OF_MONTH, 5);
        Date dueTo = calendar.getTime();

        ReminderQuery query = new ReminderQuery(getArchetypeService());
        query.setReminderType(reminderType);
        query.setFrom(dueFrom);
        query.setTo(dueTo);
        List<Act> reminders = getReminders(query);
        assertEquals(5, reminders.size());
    }


    /**
     * Verifies that IN_PROGRESS reminders can be queried for a customer.
     */
    @Test
    public void testQueryByCustomer() {
        final int count = 10;
        Entity reminderType = ReminderTestHelper.createReminderType();

        Calendar calendar = new GregorianCalendar();
        Party customer = TestHelper.createCustomer();

        for (int i = 0; i < count; ++i) {
            calendar.set(Calendar.DAY_OF_MONTH, i + 1);
            Date dueDate = calendar.getTime();
            Party patient = TestHelper.createPatient(customer);
            ReminderTestHelper.createReminderWithDueDate(patient, reminderType, dueDate);
        }
        ReminderQuery query = new ReminderQuery(getArchetypeService());
        query.setCustomer(customer);
        List<Act> reminders = getReminders(query);
        assertEquals(count, reminders.size());
    }

    /**
     * Verifies that reminders can be queried by customer location.
     */
    @Test
    public void testQueryByLocation() {
        Entity reminderType = ReminderTestHelper.createReminderType();
        Party location1 = TestHelper.createLocation();
        Party location2 = TestHelper.createLocation();

        Party customer1 = createCustomer(location1);
        Party customer2 = createCustomer(location2);

        Party patient1 = TestHelper.createPatient(customer1);
        Party patient2 = TestHelper.createPatient(customer2);

        Act act1 = ReminderTestHelper.createReminder(patient1, reminderType);
        Act act2 = ReminderTestHelper.createReminder(patient2, reminderType);
        Act act3 = ReminderTestHelper.createReminder(patient2, reminderType);
        save(act1, act2, act3);

        ReminderQuery query = new ReminderQuery(getArchetypeService());
        query.setLocation(new Location(location1));

        List<Act> reminders = getReminders(query);
        assertEquals(1, reminders.size());

        query.setLocation(new Location(location2));
        reminders = getReminders(query);
        assertEquals(2, reminders.size());
    }

    /**
     * Verifies that reminders can be queried for customers that have no location.
     */
    @Test
    public void testQueryByNoLocation() {
        Entity reminderType = ReminderTestHelper.createReminderType();
        Party location = TestHelper.createLocation();

        int initialCount = countReminders(null, null, true);

        Party customer1 = createCustomer(location);
        Party customer2 = TestHelper.createCustomer();

        Party patient1 = TestHelper.createPatient(customer1);
        Party patient2 = TestHelper.createPatient(customer2);

        Act act1 = ReminderTestHelper.createReminder(patient1, reminderType);
        Act act2 = ReminderTestHelper.createReminder(patient2, reminderType);
        Act act3 = ReminderTestHelper.createReminder(patient2, reminderType);
        save(act1, act2, act3);

        ReminderQuery query = new ReminderQuery(getArchetypeService());
        query.setLocation(Location.NONE);

        List<Act> reminders = getReminders(query);
        assertEquals(initialCount + 2, reminders.size());
    }

    /**
     * Returns all reminders matching a query.
     *
     * @param query the query
     * @return the reminders matching the query
     * @throws ArchetypeServiceException for any archetype service error
     */
    private List<Act> getReminders(ReminderQuery query) {
        List<Act> result = new ArrayList<Act>();
        for (Act set : query.query()) {
            result.add(set);
        }
        return result;
    }

    /**
     * Counts IN_PROGRESS reminders for patients with patient-owner
     * relationships.
     *
     * @param dueFrom    the start due date. May be {@code null}
     * @param dueTo      to end due date. May be {@code null}
     * @param noLocation if {@code true}, only count reminders for customers that have no location
     * @return a count of reminders in the specified date range
     */
    private int countReminders(Date dueFrom, Date dueTo, boolean noLocation) {
        int result = 0;
        ArchetypeQuery query = new ArchetypeQuery(ReminderArchetypes.REMINDER, false, true);
        query.add(new NodeConstraint("status", ActStatus.IN_PROGRESS));
        if (dueFrom != null && dueTo != null) {
            query.add(new NodeConstraint("endTime", RelationalOp.BTW, DateRules.getDate(dueFrom),
                                         DateRules.getDate(dueTo)));
        }
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        IPage<IMObject> page = getArchetypeService().get(query);
        for (IMObject object : page.getResults()) {
            ActBean bean = new ActBean((Act) object);
            Party patient = (Party) bean.getNodeParticipant("patient");
            if (patient != null) {
                EntityBean entityBean = new EntityBean(patient);
                Party customer = (Party) entityBean.getSourceEntity(PatientArchetypes.PATIENT_OWNER);
                if (customer != null) {
                    if (noLocation) {
                        IMObjectBean customerBean = new IMObjectBean(customer);
                        if (customerBean.getNodeTargetObject("practice") == null) {
                            result++;
                        }
                    } else {
                        result++;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Creates a customer linked to a location.
     *
     * @param location the location
     * @return the customer
     */
    private Party createCustomer(Party location) {
        Party customer = TestHelper.createCustomer();
        EntityBean bean = new EntityBean(customer);
        bean.addNodeTarget("practice", location);
        bean.save();
        return customer;
    }

}
