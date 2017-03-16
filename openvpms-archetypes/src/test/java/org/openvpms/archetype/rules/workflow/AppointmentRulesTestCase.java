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

package org.openvpms.archetype.rules.workflow;

import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.Period;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.rules.util.DateUnits.HOURS;
import static org.openvpms.archetype.rules.util.DateUnits.MONTHS;
import static org.openvpms.archetype.rules.util.DateUnits.YEARS;
import static org.openvpms.archetype.test.TestHelper.getDatetime;


/**
 * Tests the {@link AppointmentRules} class.
 *
 * @author Tim Anderson
 */
public class AppointmentRulesTestCase extends ArchetypeServiceTest {

    /**
     * The appointment rules.
     */
    private AppointmentRules rules;

    /**
     * Tests the {@link AppointmentRules#getSlotSize(Entity)} method.
     */
    @Test
    public void testGetSlotSize() {
        Entity appointmentType = createAppointmentType();
        Party schedule = createSchedule(15, "MINUTES", 2, appointmentType);
        assertEquals(15, rules.getSlotSize(schedule));
    }

    /**
     * Tests the {@link AppointmentRules#getDefaultAppointmentType} method.
     */
    @Test
    public void testGetDefaultAppointmentType() {
        Entity appointmentType1 = createAppointmentType();
        Entity appointmentType2 = createAppointmentType();
        Party schedule = createSchedule(15, "MINUTES", 2, null);
        assertNull(rules.getDefaultAppointmentType(schedule));

        ScheduleTestHelper.addAppointmentType(schedule, appointmentType1, 2, false);

        // no default appointment type
        assertNull(rules.getDefaultAppointmentType(schedule));

        ScheduleTestHelper.addAppointmentType(schedule, appointmentType2, 2, true);
        assertEquals(rules.getDefaultAppointmentType(schedule), appointmentType2);
    }

    /**
     * Tests the behaviour of {@link AppointmentRules#calculateEndTime} when
     * the schedule units are in minutes .
     */
    @Test
    public void testCalculateEndTimeForMinsUnits() {
        Entity appointmentType = createAppointmentType();
        save(appointmentType);
        Party schedule = createSchedule(15, "MINUTES", 2, appointmentType);
        Date start = createTime(9, 0);
        Date end = rules.calculateEndTime(start, schedule, appointmentType);
        Date expected = createTime(9, 30);
        assertEquals(expected, end);
    }

    /**
     * Tests the behaviour of {@link AppointmentRules#calculateEndTime} when
     * the schedule units are in hours.
     */
    @Test
    public void testCalculateEndTimeForHoursUnits() {
        Entity appointmentType = createAppointmentType();
        save(appointmentType);
        Party schedule = createSchedule(1, "HOURS", 3, appointmentType);
        Date start = createTime(9, 0);
        Date end = rules.calculateEndTime(start, schedule, appointmentType);
        Date expected = createTime(12, 0);
        assertEquals(expected, end);
    }

    /**
     * Verifies that the status of a task associated with an appointment
     * is updated when the appointment is saved.
     * <p/>
     * Note that this requires the
     * <em>archetypeService.save.act.customerAppointment.afer</em> rule.
     */
    @Test
    public void testUpdateTaskStatus() {
        Date start = createTime(9, 0);
        Date end = createTime(9, 15);
        Entity appointmentType = createAppointmentType();
        Party schedule = createSchedule(15, "MINUTES", 2, appointmentType);
        Act appointment = createAppointment(start, end, schedule);

        Act task = createTask();
        save(task);

        ActBean bean = new ActBean(appointment);
        bean.addRelationship("actRelationship.customerAppointmentTask", task);
        save(appointment);

        task = get(task); // need to reload as relationship has been added
        assertEquals(ActStatus.IN_PROGRESS, task.getStatus());

        checkStatus(appointment, WorkflowStatus.PENDING, task,
                    WorkflowStatus.IN_PROGRESS); // no change
        checkStatus(appointment, AppointmentStatus.CHECKED_IN, task,
                    WorkflowStatus.IN_PROGRESS);
        checkStatus(appointment, WorkflowStatus.IN_PROGRESS, task,
                    WorkflowStatus.IN_PROGRESS);
        checkStatus(appointment, WorkflowStatus.BILLED, task,
                    WorkflowStatus.BILLED);
        checkStatus(appointment, WorkflowStatus.COMPLETED, task,
                    WorkflowStatus.COMPLETED);
        checkStatus(appointment, WorkflowStatus.CANCELLED, task,
                    WorkflowStatus.CANCELLED);
    }

    /**
     * Verifies that the status of an appointment associated with an task
     * is updated when the task is saved.
     * <p/>
     * Note that this requires the
     * <em>archetypeService.save.act.customerTask.afer</em> rule.
     */
    @Test
    public void testUpdateAppointmentStatus() {
        Date start = createTime(9, 0);
        Date end = createTime(9, 15);
        Entity appointmentType = createAppointmentType();
        Party schedule = createSchedule(15, "MINUTES", 2, appointmentType);
        Act appointment = createAppointment(start, end, schedule);

        Act task = createTask();
        save(task);

        ActBean bean = new ActBean(appointment);
        bean.addRelationship("actRelationship.customerAppointmentTask", task);
        save(appointment);

        task = get(task); // need to reload as relationship has been added

        checkStatus(task, WorkflowStatus.PENDING, appointment,
                    WorkflowStatus.IN_PROGRESS);
        checkStatus(task, WorkflowStatus.IN_PROGRESS, appointment,
                    WorkflowStatus.IN_PROGRESS);
        checkStatus(task, WorkflowStatus.BILLED, appointment,
                    WorkflowStatus.BILLED);
        checkStatus(task, WorkflowStatus.COMPLETED, appointment,
                    WorkflowStatus.COMPLETED);
        checkStatus(task, WorkflowStatus.CANCELLED, appointment,
                    WorkflowStatus.CANCELLED);
    }

    /**
     * Tests the {@link AppointmentRules#copy(Act)} method.
     */
    @Test
    public void testCopy() {
        // create an appointment
        Date arrival = createTime(8, 55);
        Date start = createTime(9, 0);
        Date end = createTime(9, 15);
        Entity appointmentType = createAppointmentType();
        Party schedule = createSchedule(15, "MINUTES", 2, appointmentType);
        Party customer = TestHelper.createCustomer();
        Party patient = TestHelper.createPatient();
        User clinician = TestHelper.createClinician();
        User author = TestHelper.createUser();
        Act appointment = ScheduleTestHelper.createAppointment(start, end, schedule, appointmentType, customer, patient,
                                                               clinician, author);
        appointment.setStatus(AppointmentStatus.IN_PROGRESS);

        ActBean bean = new ActBean(appointment);
        bean.setValue("arrivalTime", arrival);
        bean.setValue("description", "some notes");

        // link it to a task
        Act task = createTask();
        save(task);
        bean.addRelationship("actRelationship.customerAppointmentTask", task);
        save(appointment);

        // now copy it, and verify the task relationship isn't copied
        Act copy = rules.copy(appointment);
        assertTrue(TypeHelper.isA(copy, ScheduleArchetypes.APPOINTMENT));
        assertTrue(copy.getActRelationships().isEmpty());

        assertTrue(copy.isNew());        // shouldn't be saved
        ActBean copyBean = new ActBean(copy);
        assertEquals(0, DateRules.compareTo(start, copy.getActivityStartTime()));
        assertEquals(0, DateRules.compareTo(end, copy.getActivityEndTime()));
        assertEquals(schedule, copyBean.getNodeParticipant("schedule"));
        assertEquals(customer, copyBean.getNodeParticipant("customer"));
        assertEquals(patient, copyBean.getNodeParticipant("patient"));
        assertEquals(clinician, copyBean.getNodeParticipant("clinician"));
        assertEquals(author, copyBean.getNodeParticipant("author"));
        assertEquals(appointmentType, copyBean.getNodeParticipant("appointmentType"));
        assertEquals(AppointmentStatus.IN_PROGRESS, copy.getStatus());
        assertEquals(0, DateRules.compareTo(arrival, copyBean.getDate("arrivalTime")));
        assertEquals("some notes", copyBean.getString("description"));
        assertEquals(appointment.getReason(), copyBean.getString("reason"));
    }

    /**
     * Tests the {@link AppointmentRules#getScheduleView(Party, Entity)} method.
     */
    @Test
    public void testGetScheduleView() {
        Party location1 = TestHelper.createLocation();
        Party location2 = TestHelper.createLocation();

        Entity scheduleA = ScheduleTestHelper.createSchedule(location1);
        Entity scheduleB = ScheduleTestHelper.createSchedule(location1);
        Entity scheduleC = ScheduleTestHelper.createSchedule(location2);
        Entity scheduleD = ScheduleTestHelper.createSchedule(location2);

        Entity view1 = ScheduleTestHelper.createScheduleView(scheduleA, scheduleB);
        Entity view2 = ScheduleTestHelper.createScheduleView(scheduleC);

        ScheduleTestHelper.addScheduleView(location1, view1);
        ScheduleTestHelper.addScheduleView(location2, view2);

        assertEquals(view1, rules.getScheduleView(location1, scheduleA));
        assertEquals(view1, rules.getScheduleView(location1, scheduleB));
        assertEquals(view2, rules.getScheduleView(location2, scheduleC));

        assertNull(rules.getScheduleView(location2, scheduleA));
        assertNull(rules.getScheduleView(location2, scheduleB));
        assertNull(rules.getScheduleView(location2, scheduleA));
        assertNull(rules.getScheduleView(location1, scheduleD));
        assertNull(rules.getScheduleView(location2, scheduleD));
    }

    /**
     * Tests the {@link AppointmentRules#getLocation(Entity)} method.
     */
    @Test
    public void testGetLocation() {
        Party location1 = TestHelper.createLocation();
        Entity scheduleA = ScheduleTestHelper.createSchedule(location1);
        assertEquals(location1, rules.getLocation(scheduleA));
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        removeActs();
        rules = new AppointmentRules(getArchetypeService());
    }

    /**
     * Tests the {@link AppointmentRules#isRemindersEnabled(Entity)}.
     */
    @Test
    public void testIsRemindersEnabled() {
        // check schedule support
        Entity schedule = ScheduleTestHelper.createSchedule(TestHelper.createLocation());
        assertFalse(rules.isRemindersEnabled(schedule));

        IMObjectBean bean = new IMObjectBean(schedule);
        bean.setValue("sendReminders", true);

        assertTrue(rules.isRemindersEnabled(schedule));

        // check appointment type support
        Entity appointmentType = ScheduleTestHelper.createAppointmentType();
        assertFalse(rules.isRemindersEnabled(appointmentType));
        bean = new IMObjectBean(appointmentType);
        bean.setValue("sendReminders", true);
        assertTrue(rules.isRemindersEnabled(appointmentType));

        // check null
        assertFalse(rules.isRemindersEnabled(null));
    }

    /**
     * Tests the {@link AppointmentRules#getNoReminderPeriod()}.
     */
    @Test
    public void testGetNoReminderPeriod() {
        final Entity job = (Entity) create(AppointmentRules.APPOINTMENT_REMINDER_JOB);
        AppointmentRules rules = new AppointmentRules(getArchetypeService()) {
            @Override
            protected IMObject getAppointmentReminderJob() {
                return job;
            }
        };
        assertEquals(Period.days(2), rules.getNoReminderPeriod());
    }

    /**
     * Tests the  {@link AppointmentRules#isBoardingAppointment(Act)} method.
     */
    @Test
    public void testIsBoardingAppointment() {
        Date start = createTime(9, 0);
        Date end = createTime(9, 15);
        Entity appointmentType = createAppointmentType();
        Party schedule = createSchedule(15, "MINUTES", 2, appointmentType);
        Act appointment = createAppointment(start, end, schedule);

        assertFalse(rules.isBoardingAppointment(appointment));

        // now add a cage type
        Entity cageType = ScheduleTestHelper.createCageType("Z Cage Type");
        IMObjectBean bean = new IMObjectBean(schedule);
        bean.addNodeTarget("cageType", cageType);
        bean.save();

        assertTrue(rules.isBoardingAppointment(appointment));
    }

    /**
     * Tests the {@link AppointmentRules#getBoardingDays} methods.
     */
    @Test
    public void testGetBoardingDays() {
        checkGetDays(1, "2016-03-23 10:00:00", "2016-03-23 17:00:00"); // same day
        checkGetDays(1, "2016-03-23 10:00:00", "2016-03-24 00:00:00"); // ends at midnight, so considered the same day
        checkGetDays(2, "2016-03-23 10:00:00", "2016-03-24 09:00:00"); // less than 24 hours
        checkGetDays(2, "2016-03-23 10:00:00", "2016-03-24 10:00:00"); // 24 hours
        checkGetDays(2, "2016-03-23 10:00:00", "2016-03-24 17:00:00"); // more than 24 hours
        checkGetDays(3, "2016-03-23 10:00:00", "2016-03-25 09:00:00"); // less than 48 hours
        checkGetDays(3, "2016-03-23 10:00:00", "2016-03-25 10:00:00"); // 48 hours
        checkGetDays(3, "2016-03-23 10:00:00", "2016-03-25 17:00:00"); // more than 48 hours

        checkGetDays(0, "2016-03-23 10:00:00", "2016-03-20 17:00:00"); // future dated being checked out now?
    }

    /**
     * Tests the {@link AppointmentRules#getBoardingNights(Date, Date)} method.
     */
    @Test
    public void testGetBoardingNights() {
        checkGetNights(1, "2016-03-23 10:00:00", "2016-03-23 17:00:00"); // same day
        checkGetNights(1, "2016-03-23 10:00:00", "2016-03-24 00:00:00"); // ends at midnight, so considered the same day
        checkGetNights(1, "2016-03-23 10:00:00", "2016-03-24 09:00:00"); // less than 24 hours
        checkGetNights(1, "2016-03-23 10:00:00", "2016-03-24 10:00:00"); // 24 hours
        checkGetNights(1, "2016-03-23 10:00:00", "2016-03-24 17:00:00"); // more than 24 hours
        checkGetNights(2, "2016-03-23 10:00:00", "2016-03-25 09:00:00"); // less than 48 hours
        checkGetNights(2, "2016-03-23 10:00:00", "2016-03-25 10:00:00"); // 48 hours
        checkGetNights(2, "2016-03-23 10:00:00", "2016-03-25 17:00:00"); // more than 48 hours

        checkGetNights(0, "2016-03-23 10:00:00", "2016-03-20 17:00:00"); // future dated being checked out now?
    }

    /**
     * Test the {@link AppointmentRules#getEvent(Act)} method.
     */
    @Test
    public void testGetEvent() {
        Date start = createTime(9, 0);
        Date end = createTime(9, 15);
        Entity appointmentType = createAppointmentType();
        Party schedule = createSchedule(15, "MINUTES", 2, appointmentType);
        Act appointment = createAppointment(start, end, schedule);

        assertNull(rules.getEvent(appointment));
        ActBean bean = new ActBean(appointment);

        Act event = PatientTestHelper.createEvent((Party) bean.getNodeParticipant("patient"));
        ActBean appointmentBean = new ActBean(appointment);
        appointmentBean.addNodeTarget("event", event);
        save(appointment, event);

        assertEquals(event, rules.getEvent(appointment));
    }

    /**
     * Tests the {@link AppointmentRules#getCustomerAppointments(Party, int, DateUnits)} and
     * {@link AppointmentRules#getPatientAppointments(Party, int, DateUnits)} methods.
     */
    @Test
    public void testGetAppointments() {
        Party customer1 = TestHelper.createCustomer();
        Party patient1a = TestHelper.createPatient(customer1);
        Party patient1b = TestHelper.createPatient(customer1);
        Party customer2 = TestHelper.createCustomer();
        Party patient2 = TestHelper.createPatient();
        Party location = TestHelper.createLocation();
        Entity schedule = ScheduleTestHelper.createSchedule(location);
        Date now = new Date();
        Act act1a = createAppointment(schedule, customer1, patient1a, DateRules.getDate(now, -1, HOURS));
        Act act1b = createAppointment(schedule, customer1, patient1b, DateRules.getDate(now, 6, MONTHS));
        Act act1c = createAppointment(schedule, customer1, null, DateRules.getDate(now, 9, MONTHS));
        Act act1d = createAppointment(schedule, customer1, patient1a, DateRules.getDate(now, 2, YEARS));
        Act act2a = createAppointment(schedule, customer2, patient2, DateRules.getDate(now, -1, YEARS));
        Act act2b = createAppointment(schedule, customer2, patient2, DateRules.getDate(now, 1, MONTHS));
        Act act2c = createAppointment(schedule, customer2, patient2, DateRules.getDate(now, 6, MONTHS));
        act2b.setStatus(AppointmentStatus.CANCELLED);
        save(act1a, act1b, act1c, act1d, act2a, act2b, act2c);

        checkAppointments(rules.getCustomerAppointments(customer1, 1, YEARS), act1b, act1c);
        checkAppointments(rules.getPatientAppointments(patient1a, 3, YEARS), act1d);
        checkAppointments(rules.getCustomerAppointments(customer2, 1, YEARS), act2c);
    }

    /**
     * Tests the {@link AppointmentRules#getSlotTime} method.
     */
    @Test
    public void testGetSlotTime() {
        Date date1 = getDatetime("2015-03-05 09:00:00");
        assertEquals(date1, rules.getSlotTime(date1, 15, false));
        assertEquals(date1, rules.getSlotTime(date1, 15, true));

        Date date2 = getDatetime("2015-03-05 09:05:00");
        assertEquals(getDatetime("2015-03-05 09:00:00"), rules.getSlotTime(date2, 15, false));
        assertEquals(getDatetime("2015-03-05 09:15:00"), rules.getSlotTime(date2, 15, true));

        Date date3 = getDatetime("2015-03-05 12:15:00");
        assertEquals(getDatetime("2015-03-05 12:00:00"), rules.getSlotTime(date3, 30, false));
        assertEquals(getDatetime("2015-03-05 12:30:00"), rules.getSlotTime(date3, 30, true));
    }

    /**
     * Verifies that {@link AppointmentRules#getBoardingDays(Date, Date)} and
     * {@link AppointmentRules#getBoardingDays(Act)} return the expected no. of days.
     *
     * @param expected  the expected no. of days
     * @param startTime the boarding start time
     * @param endTime   the boarding end time
     */
    private void checkGetDays(int expected, String startTime, String endTime) {
        Date start = TestHelper.getDatetime(startTime);
        Date end = TestHelper.getDatetime(endTime);
        assertEquals(expected, rules.getBoardingDays(start, end));

        Act appointment = (Act) create(ScheduleArchetypes.APPOINTMENT);
        appointment.setActivityStartTime(start);
        appointment.setActivityEndTime(end);
        assertEquals(expected, rules.getBoardingDays(appointment));
    }

    /**
     * Verifies that {@link AppointmentRules#getBoardingNights(Date, Date)} method returns the expected no. of nights.
     *
     * @param expected  the expected no. of days
     * @param startTime the boarding start time
     * @param endTime   the boarding end time
     */
    private void checkGetNights(int expected, String startTime, String endTime) {
        Date start = TestHelper.getDatetime(startTime);
        Date end = TestHelper.getDatetime(endTime);
        assertEquals(expected, rules.getBoardingNights(start, end));
    }

    private void checkAppointments(Iterable<Act> iterable, Act... expected) {
        List<Act> result = new ArrayList<>();
        CollectionUtils.addAll(result, iterable);
        assertEquals(expected.length, result.size());
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result.get(i));
        }
    }

    /**
     * Helper to create an <em>act.customerAppointment</em>.
     *
     * @param startTime the act start time
     * @param endTime   the act end time
     * @param schedule  the schedule
     * @return a new act
     */
    private Act createAppointment(Date startTime, Date endTime, Party schedule) {
        return ScheduleTestHelper.createAppointment(startTime, endTime, schedule);
    }

    /**
     * Helper to create a pending 15 minute appointment.
     *
     * @param schedule  the schedule
     * @param customer  the customer
     * @param patient   the patient
     * @param startTime the appointment start time
     * @return a new appointment
     */
    private Act createAppointment(Entity schedule, Party customer, Party patient, Date startTime) {
        return ScheduleTestHelper.createAppointment(startTime, schedule, customer, patient, AppointmentStatus.PENDING);
    }

    /**
     * Helper to create a new <em>act.customerTask</em>.
     *
     * @return a new act
     */
    private Act createTask() {
        Entity taskType = createTaskType();
        Party customer = TestHelper.createCustomer();
        Party workList = ScheduleTestHelper.createWorkList();
        Act act = createAct(ScheduleArchetypes.TASK);
        ActBean bean = new ActBean(act);
        bean.setStatus(WorkflowStatus.PENDING);
        bean.setValue("startTime", new Date());
        bean.setValue("endTime", new Date());
        bean.addParticipation(ScheduleArchetypes.TASK_TYPE_PARTICIPATION, taskType);
        bean.addParticipation(CustomerArchetypes.CUSTOMER_PARTICIPATION, customer);
        bean.addParticipation(ScheduleArchetypes.WORKLIST_PARTICIPATION, workList);
        bean.save();
        return act;
    }

    /**
     * Helper to create a new act.
     *
     * @param shortName the act short name
     * @return a new act
     */
    private Act createAct(String shortName) {
        return (Act) create(shortName);
    }

    /**
     * Helper to create a new <em>entity.appointmentType</em>.
     *
     * @return a new appointment type
     */
    private Entity createAppointmentType() {
        return ScheduleTestHelper.createAppointmentType();
    }

    /**
     * Helper to create a new <em>entity.taskType</em>.
     *
     * @return a new task type
     */
    private Entity createTaskType() {
        Entity taskType = (Entity) create(ScheduleArchetypes.TASK_TYPE);
        taskType.setName("XTaskType");
        save(taskType);
        return taskType;
    }

    /**
     * Helper to create a new <code>party.organisationSchedule</em>.
     *
     * @param slotSize        the schedule slot size
     * @param slotUnits       the schedule slot units
     * @param noSlots         the appointment no. of slots
     * @param appointmentType the appointment type
     * @return a new schedule
     */
    private Party createSchedule(int slotSize, String slotUnits, int noSlots, Entity appointmentType) {
        return ScheduleTestHelper.createSchedule(slotSize, slotUnits, noSlots, appointmentType,
                                                 TestHelper.createLocation());
    }

    /**
     * Helper to create a time with a fixed date.
     *
     * @param hour    the hour
     * @param minutes the minutes
     * @return a new time
     */
    private Date createTime(int hour, int minutes) {
        Calendar calendar = new GregorianCalendar(2006, 8, 22, hour, minutes);
        return calendar.getTime();
    }

    /**
     * Checks the status of a linked act (an <em>act.customerAppointment</em>
     * or <em>act.customerTask</em> when its linker is saved.
     *
     * @param source         the source act
     * @param status         the status to set
     * @param linked         the linked act
     * @param expectedStatus the expected linked act status
     */
    private void checkStatus(Act source, String status, Act linked,
                             String expectedStatus) {
        source.setStatus(status);
        linked = get(linked);    // ensure using the latest version
        Date endTime = linked.getActivityEndTime();
        try {
            // force a sleep to ensure end times are different to check
            // correct updates
            Thread.sleep(1000);
        } catch (InterruptedException ignore) {
            // do nothing.
        }
        save(source);

        // reload the linked act to get any new status
        linked = get(linked);
        assertNotNull(linked);
        assertEquals(expectedStatus, linked.getStatus());

        // for completed acts where the linked act is a task, expect the
        // endTimes to be different
        if (TypeHelper.isA(linked, ScheduleArchetypes.TASK)) {
            if (WorkflowStatus.COMPLETED.equals(expectedStatus)) {
                // end time should be > than before
                assertTrue(linked.getActivityEndTime().compareTo(endTime) > 0);
            } else {
                assertEquals(endTime, linked.getActivityEndTime());
            }
        }
    }

    /**
     * Remove any existing appointment acts that will interfere with the tests.
     */
    private void removeActs() {
        Date startDay = createTime(0, 0);
        Date endDay = createTime(23, 59);
        List rows = ArchetypeQueryHelper.getActs(
                getArchetypeService(), "act", "customerAppointment",
                startDay, endDay, null, null, null, true, 0,
                ArchetypeQuery.ALL_RESULTS).getResults();
        for (Object object : rows) {
            Act act = (Act) object;
            remove(act);
        }
    }

}
