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
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
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
        Act[] appointments = new Act[count];
        Date[] startTimes = new Date[count];
        Date[] endTimes = new Date[count];
        Date[] arrivalTimes = new Date[count];
        Party[] customers = new Party[count];
        Party[] patients = new Party[count];
        User[] clinicians = new User[count];
        Entity[] appointmentTypes = new Entity[count];
        for (int i = 0; i < count; ++i) {
            Date startTime = new Date();
            Date arrivalTime = (i % 2 == 0) ? new Date() : null;
            Date endTime = new Date();
            Party customer = TestHelper.createCustomer();
            Party patient = TestHelper.createPatient();
            User clinician = TestHelper.createClinician();
            Act appointment = AppointmentTestHelper.createAppointment(
                    startTime, endTime, schedule, customer, patient);
            ActBean bean = new ActBean(appointment);
            bean.addParticipation("participation.clinician", clinician);
            bean.setValue("arrivalTime", arrivalTime);
            appointments[i] = appointment;
            startTimes[i] = getTimestamp(startTime);
            arrivalTimes[i] = arrivalTime;
            endTimes[i] = getTimestamp(endTime);
            appointmentTypes[i] = bean.getNodeParticipant("appointmentType");
            customers[i] = customer;
            patients[i] = patient;
            clinicians[i] = clinician;
            bean.save();
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
            assertEquals(appointments[i].getObjectReference(),
                         set.get(Appointment.ACT_REFERENCE));
            assertEquals(startTimes[i],
                         set.get(Appointment.ACT_START_TIME));
            assertEquals(endTimes[i], set.get(Appointment.ACT_END_TIME));
            assertEquals(appointments[i].getStatus() ,
                         set.get(Appointment.ACT_STATUS));
            assertEquals(appointments[i].getReason() ,
                         set.get(Appointment.ACT_REASON));
            assertEquals(appointments[i].getDescription() ,
                         set.get(Appointment.ACT_DESCRIPTION));
            assertEquals(customers[i].getObjectReference(),
                         set.get(Appointment.CUSTOMER_REFERENCE));
            assertEquals(customers[i].getName(),
                         set.get(Appointment.CUSTOMER_NAME));
            assertEquals(patients[i].getObjectReference(),
                         set.get(Appointment.PATIENT_REFERENCE));
            assertEquals(patients[i].getName(),
                         set.get(Appointment.PATIENT_NAME));
            assertEquals(clinicians[i].getObjectReference(),
                         set.get(Appointment.CLINICIAN_REFERENCE));
            assertEquals(clinicians[i].getName(),
                         set.get(Appointment.CLINICIAN_NAME));
            assertEquals(appointmentTypes[i].getObjectReference(),
                         set.get(Appointment.APPOINTMENT_TYPE_REFERENCE));
            assertEquals(appointmentTypes[i].getName(),
                         set.get(Appointment.APPOINTMENT_TYPE_NAME));
            assertEquals(arrivalTimes[i],
                         set.get(Appointment.ARRIVAL_TIME));
        }
    }

    /**
     * Tests the {@link AppointmentQuery#query()} method, when a schedule,
     * date range and status range is specified.
     */
    public void testStatusQuery() {
        Party schedule = AppointmentTestHelper.createSchedule();
        Date from = new Date();
        Party customer = TestHelper.createCustomer();
        Party patient = TestHelper.createPatient();
        createAppointment(schedule, customer, patient,
                          AppointmentStatus.PENDING);
        createAppointment(schedule, customer, patient,
                          AppointmentStatus.CHECKED_IN);
        createAppointment(schedule, customer, patient,
                          AppointmentStatus.IN_PROGRESS);
        createAppointment(schedule, customer, patient,
                          AppointmentStatus.CANCELLED);
        createAppointment(schedule, customer, patient,
                          AppointmentStatus.COMPLETED);
        Date to = new Date();

        AppointmentQuery query = new AppointmentQuery();
        query.setSchedule(schedule);
        query.setDateRange(from, to);
        checkQuery(query, 3, 2);
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
     * Tests the {@link AppointmentQuery#query()} method, when a schedule,
     * date range and status range is specified.
     */
    public void testClinicianStatusQuery() {
        Party schedule = AppointmentTestHelper.createSchedule();
        Date from = new Date();
        Party customer = TestHelper.createCustomer();
        Party patient = TestHelper.createPatient();
        User clinician = TestHelper.createClinician();
        createAppointment(schedule, customer, patient, clinician,
                          AppointmentStatus.PENDING);
        createAppointment(schedule, customer, patient,
                          AppointmentStatus.CHECKED_IN);
        createAppointment(schedule, customer, patient, clinician,
                          AppointmentStatus.IN_PROGRESS);
        createAppointment(schedule, customer, patient,
                          AppointmentStatus.CANCELLED);
        createAppointment(schedule, customer, patient, clinician,
                          AppointmentStatus.COMPLETED);
        Date to = new Date();

        AppointmentQuery query = new AppointmentQuery();
        query.setSchedule(schedule);
        query.setDateRange(from, to);
        query.setClinician(clinician);
        checkQuery(query, 2, 1);
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
     * Checks an appointment query for different status ranges.
     *
     * @param query              the query
     * @param expectedIncomplete the expected no. of acts returned for an
     *                           incomplete status range
     * @param expectedComplete   the expected no. of acts returned for a
     *                           complete status range
     */
    private void checkQuery(AppointmentQuery query,
                            int expectedIncomplete, int expectedComplete) {

        // expect 3 acts to be returned for the INCOMPLETE status range
        query.setStatusRange(WorkflowStatus.StatusRange.INCOMPLETE);
        IPage<ObjectSet> page = query.query();
        assertNotNull(page);
        List<ObjectSet> results = page.getResults();
        assertEquals(expectedIncomplete, results.size());

        // expect 2 acts to be returned for the COMPLETE status range
        query.setStatusRange(WorkflowStatus.StatusRange.COMPLETE);
        page = query.query();
        assertNotNull(page);
        results = page.getResults();
        assertEquals(expectedComplete, results.size());

        // expect 5 acts to be returned for the ALL status range
        query.setStatusRange(WorkflowStatus.StatusRange.ALL);
        page = query.query();
        assertNotNull(page);
        results = page.getResults();
        assertEquals(expectedIncomplete + expectedComplete, results.size());
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

    /**
     * Helper to create and save a new appointment.
     *
     * @param schedule the appointment schedule
     * @param customer the customer
     * @param patient  the patient
     * @param status   the appointment status
     */
    private void createAppointment(Party schedule, Party customer,
                                   Party patient, String status) {
        createAppointment(schedule, customer, patient, null, status);
    }

    /**
     * Helper to create and save a new appointment for a clinician.
     *
     * @param schedule  the appointment schedule
     * @param customer  the customer
     * @param patient   the patient
     * @param clinician the clinician. May be <tt>null</tt>
     * @param status    the appointment status
     */
    private void createAppointment(Party schedule, Party customer,
                                   Party patient, User clinician,
                                   String status) {
        Date startTime = new Date();
        Date endTime = new Date();
        Act appointment = AppointmentTestHelper.createAppointment(
                startTime, endTime, schedule, customer, patient, clinician);
        appointment.setStatus(status);
        save(appointment);
    }

}
