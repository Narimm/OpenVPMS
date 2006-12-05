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

package org.openvpms.archetype.rules.workflow;

import org.apache.commons.lang.time.DateUtils;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * Tests the {@link AppointmentQuery} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AppointmentQueryTestCase extends ArchetypeServiceTest {

    /**
     * Tests the {@link AppointmentQuery#query()} method, when a
     * schedule and date range have been specified.
     */
    public void testQuery() {
        final int count = 10;
        Party schedule = AppointmentTestHelper.createSchedule();
        Date from = new Date();
        Date[] startTimes = new Date[count];
        Date[] endTimes = new Date[count];
        Party[] customers = new Party[count];
        Party[] patients = new Party[count];
        for (int i = 0; i < count; ++i) {
            Date startTime = new Date();
            Date endTime = new Date();
            Party customer = TestHelper.createCustomer();
            Party patient = TestHelper.createPatient();
            Act appointment = AppointmentTestHelper.createAppointment(
                    startTime, endTime, schedule, customer, patient);
            startTimes[i] = getTimestamp(startTime);
            endTimes[i] = getTimestamp(endTime);
            customers[i] = customer;
            patients[i] = patient;
            save(appointment);
        }
        Date to = new Date();

        AppointmentQuery query = new AppointmentQuery();
        query.setSchedule(schedule);
        query.setDateRange(from, to);
        IPage<ObjectSet> page = query.query();
        assertNotNull(page);
        List<ObjectSet> results = page.getResults();
        assertEquals(count, results.size());
        for (int i = 0; i < results.size(); ++i) {
            ObjectSet set = results.get(i);
            assertEquals(startTimes[i],
                         set.get(AppointmentQuery.ACT_START_TIME));
            assertEquals(endTimes[i], set.get(AppointmentQuery.ACT_END_TIME));
            assertEquals(customers[i].getObjectReference(),
                         set.get(AppointmentQuery.CUSTOMER_REFERENCE));
            assertEquals(customers[i].getName(),
                         set.get(AppointmentQuery.CUSTOMER_NAME));
            assertEquals(patients[i].getObjectReference(),
                         set.get(AppointmentQuery.PATIENT_REFERENCE));
            assertEquals(patients[i].getName(),
                         set.get(AppointmentQuery.PATIENT_NAME));
        }
    }

    /**
     * Tests the {@link AppointmentQuery#query()} method, when a
     * schedule, date range and clinician have been specified.
     */
    public void testClinicianQuery() {
        final int count = 10;
        Party schedule = AppointmentTestHelper.createSchedule();
        Date from = new Date();
        Party customer = TestHelper.createCustomer();
        Party patient = TestHelper.createPatient();
        User clinician = TestHelper.createClinician();
        for (int i = 0; i < count; ++i) {
            Date startTime = new Date();
            Date endTime = new Date();
            User vet = (i % 2 == 0) ? clinician : null;
            Act appointment = AppointmentTestHelper.createAppointment(
                    startTime, endTime, schedule, customer, patient, vet);
            save(appointment);
        }
        Date to = new Date();

        AppointmentQuery query = new AppointmentQuery();
        query.setSchedule(schedule);
        query.setDateRange(from, to);
        query.setClinician(clinician);
        IPage<ObjectSet> page = query.query();
        assertNotNull(page);
        List<ObjectSet> results = page.getResults();
        assertEquals(count / 2, results.size());
    }

    /**
     * Verifies that no results are returned if the schedule, or date range
     * is not specified.
     */
    public void testEmptyQuery() {
        final int count = 10;
        Party schedule = AppointmentTestHelper.createSchedule();
        Date from = new Date();
        for (int i = 0; i < count; ++i) {
            Date startTime = new Date();
            Date endTime = new Date();
            Act appointment = AppointmentTestHelper.createAppointment(
                    startTime, endTime, schedule);
            save(appointment);
        }
        Date to = new Date();
        AppointmentQuery query = new AppointmentQuery();
        IPage<ObjectSet> page = query.query();
        assertTrue(page.getResults().isEmpty());

        query.setSchedule(schedule);
        page = query.query();
        assertTrue(page.getResults().isEmpty());

        query.setDateRange(from, null);
        page = query.query();
        assertTrue(page.getResults().isEmpty());

        query.setDateRange(from, to);
        page = query.query();
        assertEquals(count, page.getResults().size());
    }

    /**
     * Helper to remove any seconds from a time, as the database may not
     * store them.
     *
     * @param timestamp the timestamp
     * @return the timestamp with seconds and milliseconds removed
     */
    private Date getTimestamp(Date timestamp) {
        return DateUtils.truncate(timestamp, Calendar.SECOND);
    }
}
