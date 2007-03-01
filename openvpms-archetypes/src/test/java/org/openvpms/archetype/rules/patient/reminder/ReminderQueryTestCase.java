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
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.RelationalOp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;


/**
 * Tests the {@link ReminderQuery} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReminderQueryTestCase extends ArchetypeServiceTest {

    /**
     * Verifies that a query with no constraints returns all IN_PROGRESS
     * reminders.
     */
    public void testQuery() {
        int initialCount = countReminders(null, null);
        ReminderQuery query = new ReminderQuery();
        List<ObjectSet> reminders = getReminders(query);
        assertEquals(initialCount, reminders.size());
        for (ObjectSet set : reminders) {
            IMObjectReference actRef =
                    (IMObjectReference) set.get(ReminderQuery.ACT_REFERENCE);
            String status = (String) set.get(ReminderQuery.ACT_STATUS);
            assertTrue(TypeHelper.isA(actRef, "act.patientReminder"));
            assertEquals(ActStatus.IN_PROGRESS, status);
        }
    }

    /**
     * Verifies that IN_PROGRESS reminders can be queried for a particular
     * reminder type.
     */
    public void testQueryReminderType() {
        final int count = 10;
        Entity reminderType = ReminderTestHelper.createReminderType();
        Party customer = TestHelper.createCustomer();
        Party patient = TestHelper.createPatient(customer);
        Date dueDate = new Date();

        for (int i = 0; i < count; ++i) {
            ReminderTestHelper.createReminder(patient, reminderType, dueDate);
        }

        ReminderQuery query = new ReminderQuery();
        query.setReminderType(reminderType);
        List<ObjectSet> reminders = getReminders(query);
        assertEquals(count, reminders.size());
    }

    /**
     * Verifies that IN_PROGRESS reminders can be queried for a date range.
     */
    public void testQueryDateRange() {
        final int count = 10;

        Entity reminderType = ReminderTestHelper.createReminderType();
        Party customer = TestHelper.createCustomer();
        Party patient = TestHelper.createPatient(customer);

        Calendar calendar = new GregorianCalendar();
        calendar.set(1980, 0, 1);
        Date dueFrom = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, count);
        Date dueTo = calendar.getTime();

        // add some data
        for (int i = 0; i < count; ++i) {
            ReminderTestHelper.createReminder(patient, reminderType, dueFrom);
        }

        // now determine the no. of acts with a due date in the date range
        int rangeCount = countReminders(dueFrom, dueTo);

        // now verify that the query gives the same count
        ReminderQuery query = new ReminderQuery();
        query.setDueDateRange(dueFrom, dueTo);
        List<ObjectSet> reminders = getReminders(query);
        assertEquals(rangeCount, reminders.size());
    }

    /**
     * Verifies that IN_PROGRESS reminders can be queried for a reminder type
     * and date range.
     */
    public void testQueryReminderTypeDateRange() {
        final int count = 10;
        Entity reminderType = ReminderTestHelper.createReminderType();
        Party customer = TestHelper.createCustomer();
        Party patient = TestHelper.createPatient(customer);

        Calendar calendar = new GregorianCalendar();
        calendar.set(1980, 0, 1);
        Date dueFrom = calendar.getTime();

        for (int i = 0; i < count; ++i) {
            calendar.set(Calendar.DAY_OF_MONTH, i + 1);
            Date dueDate = calendar.getTime();
            ReminderTestHelper.createReminder(patient, reminderType, dueDate);
        }
        // query a subset of the dates just added
        calendar.set(Calendar.DAY_OF_MONTH, 5);
        Date dueTo = calendar.getTime();

        ReminderQuery query = new ReminderQuery();
        query.setReminderType(reminderType);
        query.setDueDateRange(dueFrom, dueTo);
        List<ObjectSet> reminders = getReminders(query);
        assertEquals(5, reminders.size());
    }

    /**
     * Returns all reminders matching a query.
     *
     * @param query the query
     */
    private List<ObjectSet> getReminders(ReminderQuery query) {
        List<ObjectSet> result = new ArrayList<ObjectSet>();
        Iterator<ObjectSet> iterator = query.query();
        while (iterator.hasNext()) {
            result.add(iterator.next());
        }
        return result;
    }

    /**
     * Counts IN_PROGRESS reminders for patients with patient-owner relationships.
     *
     * @param dueFrom the start due date. May be <code>null</code>
     * @param dueTo   to end due date. May be <code>null</code>
     * @return a count of reminders in the specified date range
     */
    private int countReminders(Date dueFrom, Date dueTo) {
        PatientRules rules = new PatientRules();
        int result = 0;
        ArchetypeQuery query = new ArchetypeQuery("act.patientReminder", false,
                                                  true);
        query.add(new NodeConstraint("status", ActStatus.IN_PROGRESS));
        if (dueFrom != null && dueTo != null) {
            query.add(new NodeConstraint("endTime", RelationalOp.BTW, dueFrom,
                                         dueTo));
        }
        int pageIndex = 0;
        int rowsPerPage = 100;
        query.setFirstResult(0);
        query.setMaxResults(rowsPerPage);
        IPage<IMObject> page = getArchetypeService().get(query);
        while (!page.getResults().isEmpty()) {
            for (IMObject object : page.getResults()) {
                ActBean bean = new ActBean((Act) object);
                Party patient = (Party) bean.getParticipant(
                        "participation.patient");
                if (patient != null) {
                    if (rules.getOwner(patient) != null) {
                        result++;
                    }
                }
            }
            pageIndex++;
            query.setFirstResult(pageIndex * rowsPerPage);
            page = getArchetypeService().get(query);
        }
        return result;
    }

}
