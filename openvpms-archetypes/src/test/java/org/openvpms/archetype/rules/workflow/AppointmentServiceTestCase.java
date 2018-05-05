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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.workflow;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.util.PropertySet;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.test.TestHelper.createPatient;
import static org.openvpms.archetype.test.TestHelper.getDate;
import static org.openvpms.archetype.test.TestHelper.getDatetime;


/**
 * Tests the {@link AppointmentService}.
 *
 * @author Tim Anderson
 */
public class AppointmentServiceTestCase extends AbstractScheduleServiceTest {

    /**
     * The appointment service.
     */
    private AppointmentService service;

    /**
     * The schedule.
     */
    private Party schedule;

    /**
     * The practice location.
     */
    private Party location;


    /**
     * Tests addition of an appointment.
     */
    @Test
    public void testAddEvent() {
        Date date1 = getDate("2008-01-01");
        Date date2 = getDate("2008-01-02");
        Date date3 = getDate("2008-01-03");

        // retrieve the appointments for date1 and date2 and verify they are
        // empty.
        // This caches the appointments for each date.
        service = createScheduleService(30);
        List<PropertySet> results = service.getEvents(schedule, date1);
        assertEquals(0, results.size());

        results = service.getEvents(schedule, date2);
        assertEquals(0, results.size());

        // create and save appointment for date1
        Act appointment = createAppointment(date1);

        results = service.getEvents(schedule, date1);
        assertEquals(1, results.size());
        PropertySet set = results.get(0);
        checkAppointment(appointment, set);

        results = service.getEvents(schedule, date2);
        assertEquals(0, results.size());

        results = service.getEvents(schedule, date3);
        assertEquals(0, results.size());
    }

    /**
     * Tests removal of an event.
     */
    @Test
    public void testRemoveEvent() {
        Date date1 = getDate("2008-01-01");
        Date date2 = getDate("2008-01-02");
        Date date3 = getDate("2008-01-03");

        // retrieve the appointments for date1 and date2 and verify they are
        // empty.
        // This caches the appointments for each date.
        service = createScheduleService(30);
        List<PropertySet> results = service.getEvents(schedule, date1);
        assertEquals(0, results.size());

        results = service.getEvents(schedule, date2);
        assertEquals(0, results.size());

        // create and save appointment for date1
        Act appointment = createAppointment(date1);

        results = service.getEvents(schedule, date1);
        assertEquals(1, results.size());

        results = service.getEvents(schedule, date2);
        assertEquals(0, results.size());

        // now remove it
        getArchetypeService().remove(appointment);

        // verify it has been removed
        assertEquals(0, service.getEvents(schedule, date1).size());
        assertEquals(0, service.getEvents(schedule, date2).size());
        assertEquals(0, service.getEvents(schedule, date3).size());
    }

    /**
     * Tests the {@link AppointmentService#getEvents(Entity, Date)} method.
     */
    @Test
    public void testGetEvents() {
        final int count = 10;
        Party schedule = ScheduleTestHelper.createSchedule(location);
        Act[] appointments = new Act[count];
        Date date = getDate("2007-01-01");
        for (int i = 0; i < count; ++i) {
            Date startTime = DateRules.getDate(date, 15 * count, DateUnits.MINUTES);
            Date endTime = DateRules.getDate(startTime, 15, DateUnits.MINUTES);
            Date arrivalTime = (i % 2 == 0) ? new Date() : null;
            Party customer = TestHelper.createCustomer();
            Party patient = TestHelper.createPatient();
            User clinician = TestHelper.createClinician();
            Act appointment = ScheduleTestHelper.createAppointment(
                    startTime, endTime, schedule, customer, patient);
            ActBean bean = new ActBean(appointment);
            bean.addParticipation("participation.clinician", clinician);
            bean.setValue("arrivalTime", arrivalTime);
            appointments[i] = appointment;
            bean.save();
        }

        service = createScheduleService(30);
        List<PropertySet> results = service.getEvents(schedule, date);
        assertEquals(count, results.size());
        for (int i = 0; i < results.size(); ++i) {
            PropertySet set = results.get(i);
            checkAppointment(appointments[i], set);
        }
    }

    /**
     * Tests moving an event from one date to another.
     */
    @Test
    public void testChangeEventDate() {
        Date date1 = getDate("2008-01-01");
        Date date2 = getDate("2008-03-01");

        service = createScheduleService(30);
        service.getEvents(schedule, date1);
        assertEquals(0, service.getEvents(schedule, date1).size());
        assertEquals(0, service.getEvents(schedule, date2).size());

        Act act = createAppointment(date1);

        assertEquals(1, service.getEvents(schedule, date1).size());
        assertEquals(0, service.getEvents(schedule, date2).size());

        act.setActivityStartTime(date2); // move it to date2
        act.setActivityEndTime(DateRules.getDate(date2, 15, DateUnits.MINUTES));
        getArchetypeService().save(act);

        assertEquals(0, service.getEvents(schedule, date1).size());
        assertEquals(1, service.getEvents(schedule, date2).size());
    }

    /**
     * Verifies that a new to lookup.visitReason appears in new appointments.
     */
    @Test
    public void testAddReason() {
        Date date1 = getDate("2008-01-01");
        Date date2 = getDate("2008-01-02");
        Party patient = TestHelper.createPatient();

        // create and save appointment for date1
        Act appointment1 = createAppointment(date1);
        service = createScheduleService(30);
        List<PropertySet> results = service.getEvents(schedule, date1);
        assertEquals(1, results.size());
        PropertySet set = results.get(0);
        checkAppointment(appointment1, set);

        String code = "XREASON" + System.currentTimeMillis();
        String name = "Added reason";
        TestHelper.getLookup(ScheduleArchetypes.VISIT_REASON, code, name, true);

        Act appointment2 = createAppointment(date2, schedule, patient, false);
        appointment2.setReason(code);
        save(appointment2);

        // verify the reason code and name appears in the new event
        results = service.getEvents(schedule, date2);
        assertEquals(1, results.size());
        set = results.get(0);
        assertEquals(code, set.getString(ScheduleEvent.ACT_REASON));
        assertEquals(name, set.getString(ScheduleEvent.ACT_REASON_NAME));
    }

    /**
     * Verifies that changes to lookup.visitReason get reflected in the cached appointments.
     */
    @Test
    public void testUpdateReason() {
        Date date = getDate("2008-01-01");
        service = createScheduleService(30);

        // create and save appointment for date
        Act appointment = createAppointment(date);
        List<PropertySet> results = service.getEvents(schedule, date);
        assertEquals(1, results.size());
        PropertySet set = results.get(0);
        checkAppointment(appointment, set);

        Lookup reason = getLookupService().getLookup(appointment, "reason");
        assertNotNull(reason);
        String name = "New reason: " + System.currentTimeMillis();
        reason.setName(name);
        save(reason);

        results = service.getEvents(schedule, date);
        assertEquals(1, results.size());
        set = results.get(0);
        assertEquals(reason.getCode(), set.getString(ScheduleEvent.ACT_REASON));
        assertEquals(name, set.getString(ScheduleEvent.ACT_REASON_NAME));
    }

    /**
     * Verifies that if a lookup.visitReason is removed, the cache is updated.
     * <p/>
     * Strictly speaking, the application shouldn't remove a lookup in use, but if it occurs,
     * this implementation will return null for the reason name.
     */
    @Test
    public void testRemoveReason() {
        Date date = getDate("2008-01-01");
        Party patient = TestHelper.createPatient();

        service = createScheduleService(30);
        // create and save appointment for date
        Act appointment = createAppointment(date, schedule, patient, false);
        String code = "XREASON" + System.currentTimeMillis();
        String name = "Reason to remove";
        Lookup reason = TestHelper.getLookup(ScheduleArchetypes.VISIT_REASON, code, name, true);
        appointment.setReason(code);
        save(appointment);

        List<PropertySet> results = service.getEvents(schedule, date);
        assertEquals(1, results.size());
        PropertySet set = results.get(0);

        assertEquals(code, set.getString(ScheduleEvent.ACT_REASON));
        assertEquals(name, set.getString(ScheduleEvent.ACT_REASON_NAME));

        // now remove the appointment and the reason lookup
        remove(appointment);
        remove(reason);

        appointment = createAppointment(date, schedule, patient, false);
        appointment.setReason(code);
        save(appointment);

        results = service.getEvents(schedule, date);
        assertEquals(1, results.size());
        set = results.get(0);
        assertEquals(code, set.getString(ScheduleEvent.ACT_REASON));
        assertNull(set.getString(ScheduleEvent.ACT_REASON_NAME));
    }

    /**
     * Verifies that appointments can span multiple days.
     */
    @Test
    public void testMultipleDayAppointment() {
        Date start = getDate("2008-01-01");
        Date end = getDate("2008-01-05");
        Party patient = TestHelper.createPatient();

        service = createScheduleService(30);

        // retrieve the appointments from start to end and verify they are empty.
        // This caches the appointments for each date.
        List<PropertySet> results = service.getEvents(schedule, start, end);
        assertTrue(results.isEmpty());

        // create and save a multiple day appointment
        Date startTime = DateRules.getDate(start, 15, DateUnits.MINUTES);
        Date endTime = DateRules.getDate(end, 15, DateUnits.MINUTES);
        Act appointment = createAppointment(startTime, endTime, schedule, patient, true);

        // verify the appointment is returned for each day
        checkAppointment(appointment, 5);

        // update the start time for the appointment, and verify its not present on the original days
        startTime = DateRules.getDate(startTime, 2, DateUnits.DAYS);
        appointment.setActivityStartTime(startTime);
        save(appointment);
        results = service.getEvents(schedule, start, DateRules.getDate(start, 1, DateUnits.DAYS));
        assertTrue(results.isEmpty());

        // verify the appointment exists for the subsequent days
        checkAppointment(appointment, 3);

        // reduce the end time for the appointment, and verify its not present on the original days
        Date newEndTime = DateRules.getDate(endTime, -1, DateUnits.DAYS);
        appointment.setActivityEndTime(newEndTime);
        save(appointment);
        checkAppointment(appointment, 2);

        results = service.getEvents(schedule, endTime);
        assertTrue(results.isEmpty());
    }

    /**
     * Tests repeatedly reading the same sets of events for a cache size smaller than the number of dates read.
     */
    @Test
    public void testRepeatedSequentialRead() {
        Date start = getDate("2008-01-01");
        Party patient = createPatient();

        Set[] days = new Set[10];

        // create some appointments
        for (int i = 0; i < 10; ++i) {
            Set<Long> appointments = new HashSet<>();
            Date date = DateRules.getDate(start, i, DateUnits.DAYS);
            for (int j = 0; j < 100; ++j) {
                Act appointment = createAppointment(date, schedule, patient, true);
                appointments.add(appointment.getId());
            }
            days[i] = appointments;
        }

        service = createScheduleService(6); // cache 6 days

        // repeatedly read 10 days worth of appointments. This will force the cache to shed and re-read data.
        for (int j = 0; j < 10; ++j) {
            for (int k = 0; k < 10; ++k) {
                Date date = DateRules.getDate(start, k, DateUnits.DAYS);
                Set<Long> ids = getIds(date, schedule, service);
                assertEquals(days[k], ids);
            }
        }
    }

    /**
     * Tests the behaviour of {@link AppointmentService#getOverlappingEvent(Act)} method.
     */
    @Test
    public void testGetOverlappingEvent() {
        service = createScheduleService(1);
        Date start = getDatetime("2015-05-14 09:00:00");
        Date end = getDatetime("2015-05-14 09:15:00");

        Entity appointmentType = ScheduleTestHelper.createAppointmentType();
        Party schedule1 = ScheduleTestHelper.createSchedule(15, "MINUTES", 2, appointmentType, location);
        Party schedule2 = ScheduleTestHelper.createSchedule(15, "MINUTES", 2, appointmentType, location);
        save(schedule1);
        save(schedule2);

        Act appointment = createAppointment(start, end, schedule1, false);
        assertNull(service.getOverlappingEvent(appointment));
        save(appointment);
        assertNull(service.getOverlappingEvent(appointment));

        Act exactOverlap = createAppointment(start, end, schedule1, false);
        Times expected = Times.create(appointment);
        assertEquals(expected, service.getOverlappingEvent(exactOverlap));

        Act overlap = createAppointment(getDatetime("2015-05-14 09:05:00"), getDatetime("2015-05-14 09:10:00"),
                                        schedule1, true);
        assertEquals(expected, service.getOverlappingEvent(overlap));

        Act after = createAppointment(getDatetime("2015-05-14 09:15:00"), getDatetime("2015-05-14 09:30:00"),
                                      schedule1, false);
        assertNull(service.getOverlappingEvent(after));

        Act before = createAppointment(getDatetime("2015-05-14 08:45:00"), getDatetime("2015-05-14 09:00:00"),
                                       schedule1, false);
        assertNull(service.getOverlappingEvent(before));

        // now verify there are no overlaps for the same time but different schedule
        Act appointment2 = createAppointment(start, end, schedule2, false);
        assertNull(service.getOverlappingEvent(appointment2));
        save(appointment2);
        assertNull(service.getOverlappingEvent(appointment2));

        // verify there are no overlaps for an unpopulated act
        Act appointment3 = (Act) create(ScheduleArchetypes.APPOINTMENT);
        assertNull(service.getOverlappingEvent(appointment3));
        appointment3.setActivityStartTime(null);
        appointment3.setActivityEndTime(null);
        assertNull(service.getOverlappingEvent(appointment3));
    }

    /**
     * Tests the {@link AppointmentService#getOverlappingEvent(List, Entity)} method.
     */
    @Test
    public void getOverlappingEventTimes() {
        service = createScheduleService(1);
        Date start1 = getDatetime("2015-05-14 09:00:00");
        Date end1 = getDatetime("2015-05-14 09:15:00");
        Date start2 = getDatetime("2015-05-15 09:00:00");
        Date end2 = getDatetime("2015-05-15 09:15:00");
        Date beforeStart = getDatetime("2015-05-15 08:45:00");
        Date beforeEnd = getDatetime("2015-05-15 09:00:00");
        Date afterStart = getDatetime("2015-05-15 09:30:00");
        Date afterEnd = getDatetime("2015-05-15 09:45:00");
        Date overlap1Start = getDatetime("2015-05-15 09:05:00");
        Date overlap1End = getDatetime("2015-05-15 09:20:00");
        Date overlap2Start = getDatetime("2015-05-15 09:10:00");
        Date overlap2End = getDatetime("2015-05-15 09:25:00");

        Times times1 = new Times(start1, end1);
        Times times2 = new Times(start2, end2);
        List<Times> list = Arrays.asList(times1, times2);
        assertNull(service.getOverlappingEvent(list, schedule));

        // overlaps time1 exactly
        Act appointment1 = createAppointment(start1, end1, schedule, true);
        assertEquals(Times.create(appointment1), service.getOverlappingEvent(list, schedule));
        remove(appointment1);

        // overlaps time2 exactly
        Act appointment2 = createAppointment(start2, end2, schedule, true);
        assertEquals(Times.create(appointment2), service.getOverlappingEvent(list, schedule));
        remove(appointment2);

        // before time2
        createAppointment(beforeStart, beforeEnd, schedule, true);
        assertNull(service.getOverlappingEvent(list, schedule));

        // after time2
        createAppointment(afterStart, afterEnd, schedule, true);
        assertNull(service.getOverlappingEvent(list, schedule));

        // intersects start of time2
        Act appointment5 = createAppointment(overlap1Start, overlap1End, schedule, true);
        assertEquals(Times.create(appointment5), service.getOverlappingEvent(list, schedule));
        remove(appointment5);

        // intersects end of time2
        Act appointment6 = createAppointment(overlap2Start, overlap2End, schedule, true);
        assertEquals(Times.create(appointment6), service.getOverlappingEvent(list, schedule));
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        location = TestHelper.createLocation();
        schedule = ScheduleTestHelper.createSchedule(location);
    }

    /**
     * Cleans up after the test.
     *
     * @throws Exception for any error
     */
    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        if (service != null) {
            service.destroy();
        }
    }

    /**
     * Creates a new {@link ScheduleService}.
     *
     * @param scheduleCacheSize the maximum number of schedule days to cache
     * @return the new service
     */
    @Override
    protected AppointmentService createScheduleService(int scheduleCacheSize) {
        return new AppointmentService(getArchetypeService(), applicationContext.getBean(ILookupService.class),
                                      ScheduleTestHelper.createCache(scheduleCacheSize));
    }

    /**
     * Creates a new schedule.
     *
     * @return the new schedule
     */
    @Override
    protected Entity createSchedule() {
        return ScheduleTestHelper.createSchedule(location);
    }

    /**
     * Creates a new event for the specified schedule and date.
     *
     * @param schedule the schedule
     * @param date     the date
     * @param patient  the patient. May be {@code null}
     * @return the new event act
     */
    @Override
    protected Act createEvent(Entity schedule, Date date, Party patient) {
        return createAppointment(date, (Party) schedule, patient, true);
    }

    /**
     * Verifies the service returns the appointment for each day it spans.
     *
     * @param act          the appointment
     * @param expectedDays the expected no. of days that the appointment should appear on
     */
    private void checkAppointment(Act act, int expectedDays) {
        Date start = DateRules.getDate(act.getActivityStartTime());
        Date end = DateRules.getDate(act.getActivityEndTime());
        Date date = start;
        int count = 0;
        while (date.compareTo(end) <= 0) {
            List<PropertySet> results = service.getEvents(schedule, date);
            assertEquals(1, results.size());
            PropertySet set = results.get(0);
            checkAppointment(act, set);
            ++count;

            date = DateRules.getDate(date, 1, DateUnits.DAYS);
        }
        assertEquals(expectedDays, count);

    }

    /**
     * Verifies that an appointment matches the {@link PropertySet} representing it.
     *
     * @param act the appointment
     * @param set the set
     */
    private void checkAppointment(Act act, PropertySet set) {
        ActBean bean = new ActBean(act);
        assertEquals(act.getObjectReference(), set.get(ScheduleEvent.ACT_REFERENCE));
        assertEquals(act.getActivityStartTime(), set.get(ScheduleEvent.ACT_START_TIME));
        assertEquals(act.getActivityEndTime(), set.get(ScheduleEvent.ACT_END_TIME));
        assertEquals(act.getStatus(), set.get(ScheduleEvent.ACT_STATUS));
        assertEquals(TestHelper.getLookupName(act, "status"), set.get(ScheduleEvent.ACT_STATUS_NAME));
        assertEquals(act.getReason(), set.get(ScheduleEvent.ACT_REASON));
        assertEquals(TestHelper.getLookupName(act, "reason"), set.get(ScheduleEvent.ACT_REASON_NAME));
        assertEquals(act.getDescription(), set.get(ScheduleEvent.ACT_DESCRIPTION));
        assertEquals(bean.getNodeParticipantRef("customer"), set.get(ScheduleEvent.CUSTOMER_REFERENCE));
        assertEquals(bean.getNodeParticipant("customer").getName(), set.get(ScheduleEvent.CUSTOMER_NAME));
        assertEquals(bean.getNodeParticipantRef("patient"), set.get(ScheduleEvent.PATIENT_REFERENCE));
        assertEquals(bean.getNodeParticipant("patient").getName(), set.get(ScheduleEvent.PATIENT_NAME));
        assertEquals(bean.getNodeParticipantRef("clinician"), set.get(ScheduleEvent.CLINICIAN_REFERENCE));
        assertEquals(bean.getNodeParticipant("clinician").getName(), set.get(ScheduleEvent.CLINICIAN_NAME));
        assertEquals(bean.getNodeParticipantRef("appointmentType"), set.get(ScheduleEvent.SCHEDULE_TYPE_REFERENCE));
        assertEquals(bean.getNodeParticipantRef("schedule"), set.get(ScheduleEvent.SCHEDULE_REFERENCE));
        assertEquals(bean.getNodeParticipant("schedule").getName(), set.get(ScheduleEvent.SCHEDULE_NAME));
        assertEquals(bean.getNodeParticipant("appointmentType").getName(), set.get(ScheduleEvent.SCHEDULE_TYPE_NAME));
        assertEquals(bean.getDate("arrivalTime"), set.get(ScheduleEvent.ARRIVAL_TIME));
    }

    /**
     * Creates and saves a new appointment.
     *
     * @param date the date to create the appointment on
     * @return a new appointment
     */
    private Act createAppointment(Date date) {
        Party patient = TestHelper.createPatient();
        return createAppointment(date, schedule, patient, true);
    }

    /**
     * Creates a new appointment.
     *
     * @param date     the date to create the appointment on
     * @param schedule the schedule
     * @param patient  the patient. May be {@code null}
     * @param save     if {@code true} save the appointment
     * @return a new appointment
     */
    private Act createAppointment(Date date, Party schedule, Party patient, boolean save) {
        Date startTime = DateRules.getDate(date, 15, DateUnits.MINUTES);
        Date endTime = DateRules.getDate(startTime, 15, DateUnits.MINUTES);
        return createAppointment(startTime, endTime, schedule, patient, save);
    }

    /**
     * Creates a new appointment.
     *
     * @param startTime the start time
     * @param endTime   the end time
     * @param schedule  the schedule
     * @param save      if {@code true} save the appointment
     * @return a new appointment
     */
    private Act createAppointment(Date startTime, Date endTime, Party schedule, boolean save) {
        return createAppointment(startTime, endTime, schedule, null, save);
    }

    /**
     * Creates a new appointment.
     *
     * @param startTime the start time
     * @param endTime   the end time
     * @param schedule  the schedule
     * @param patient   the patient. May be {@code null}
     * @param save      if {@code true} save the appointment
     * @return a new appointment
     */
    private Act createAppointment(Date startTime, Date endTime, Party schedule, Party patient, boolean save) {
        Party customer = TestHelper.createCustomer();
        User clinician = TestHelper.createClinician();
        User author = TestHelper.createClinician();
        Entity appointmentType = ScheduleTestHelper.createAppointmentType();
        appointmentType.setName("XAppointmentType");
        Act appointment = ScheduleTestHelper.createAppointment(startTime, endTime, schedule, appointmentType, customer,
                                                               patient, clinician, author);
        if (save) {
            save(appointment);
        }
        return appointment;
    }

}
