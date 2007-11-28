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

import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


/**
 * Tests the {@link AppointmentRules} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AppointmentRulesTestCase extends ArchetypeServiceTest {

    /**
     * The appointment rules.
     */
    private AppointmentRules rules;


    /**
     * Tests the {@link AppointmentRules#getSlotSize(Party)} method.
     */
    public void testGetSlotSize() {
        Entity appointmentType = createAppointmentType();
        Party schedule = createSchedule(15, "MINUTES", 2, appointmentType);
        assertEquals(15, rules.getSlotSize(schedule));
    }

    /**
     * Tests the {@link AppointmentRules#getDefaultAppointmentType(Party)}
     * method.
     */
    public void testGetDefaultAppointmentType() {
        Entity appointmentType1 = createAppointmentType();
        Entity appointmentType2 = createAppointmentType();
        Party schedule = createSchedule(15, "MINUTES", 2, null);
        assertNull(rules.getDefaultAppointmentType(schedule));

        AppointmentTestHelper.addAppointmentType(schedule, appointmentType1, 2,
                                                 false);

        // no default appointment type, so should pick the first available
        assertEquals(rules.getDefaultAppointmentType(schedule),
                     appointmentType1);

        AppointmentTestHelper.addAppointmentType(schedule, appointmentType2, 2,
                                                 true);
        assertEquals(rules.getDefaultAppointmentType(schedule),
                     appointmentType2);
    }

    /**
     * Tests the behaviour of {@link AppointmentRules#calculateEndTime} when
     * the schedule units are in minutes .
     */
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
     * Tests the behaviour of
     * {@link AppointmentRules#hasOverlappingAppointments}.
     */
    public void testHasOverlappingAppointments() {
        Date start = createTime(9, 0);
        Date end = createTime(9, 15);

        Entity appointmentType = createAppointmentType();
        Party schedule1 = createSchedule(15, "MINUTES", 2, appointmentType);
        Party schedule2 = createSchedule(15, "MINUTES", 2, appointmentType);
        save(schedule1);
        save(schedule2);

        Act appointment = createAppointment(start, end, schedule1);
        assertFalse(rules.hasOverlappingAppointments(appointment));
        save(appointment);
        assertFalse(rules.hasOverlappingAppointments(appointment));

        Act exactOverlap = createAppointment(start, end, schedule1);
        assertTrue(rules.hasOverlappingAppointments(exactOverlap));

        Act overlap = createAppointment(createTime(9, 5), createTime(9, 10),
                                        schedule1);
        assertTrue(rules.hasOverlappingAppointments(overlap));

        Act after = createAppointment(createTime(9, 15), createTime(9, 30),
                                      schedule1);
        assertFalse(rules.hasOverlappingAppointments(after));

        Act before = createAppointment(createTime(8, 45), createTime(9, 0),
                                       schedule1);
        assertFalse(rules.hasOverlappingAppointments(before));

        // now verify there are no overlaps for the same time but different
        // schedule
        Act appointment2 = createAppointment(start, end, schedule2);
        assertFalse(rules.hasOverlappingAppointments(appointment2));
        save(appointment2);
        assertFalse(rules.hasOverlappingAppointments(appointment2));
    }

    /**
     * Tests the behaviour of {@link AppointmentRules#hasOverlappingAppointments}
     * for an unpopulated appointment.
     */
    public void testHasOverlappingAppointmentsForEmptyAct() {
        Date start = createTime(9, 0);
        Date end = createTime(9, 15);
        Entity appointmentType = createAppointmentType();
        Party schedule = createSchedule(15, "MINUTES", 2, appointmentType);
        Act appointment = createAppointment(start, end, schedule);
        save(appointment);

        Act empty = createAct("act.customerAppointment");
        empty.setActivityStartTime(null);
        empty.setActivityEndTime(null);

        assertFalse(rules.hasOverlappingAppointments(empty));
    }

    /**
     * Verifies that the status of a task associated with an appointment
     * is updated when the appointment is saved.
     * <p/>
     * Note that this requires the
     * <em>archetypeService.save.act.customerAppointment.afer</em> rule.
     */
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

        checkStatus(appointment, WorkflowStatus.PENDING, task,
                    WorkflowStatus.PENDING);
        checkStatus(appointment, AppointmentStatus.CHECKED_IN, task,
                    WorkflowStatus.PENDING);
        checkStatus(appointment, WorkflowStatus.IN_PROGRESS, task,
                    WorkflowStatus.IN_PROGRESS);
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
                    WorkflowStatus.PENDING);
        checkStatus(task, WorkflowStatus.IN_PROGRESS, appointment,
                    WorkflowStatus.IN_PROGRESS);
        checkStatus(task, TaskStatus.BILLED, appointment,
                    WorkflowStatus.IN_PROGRESS);
        checkStatus(task, WorkflowStatus.COMPLETED, appointment,
                    WorkflowStatus.COMPLETED);
        checkStatus(task, WorkflowStatus.CANCELLED, appointment,
                    WorkflowStatus.CANCELLED);
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        removeActs();
        rules = new AppointmentRules();
    }

    /**
     * Helper to create an <em>act.customerAppointment</em>.
     *
     * @param startTime the act start time
     * @param endTime   the act end time
     * @param schedule  the schedule
     * @return a new act
     */
    protected Act createAppointment(Date startTime, Date endTime,
                                    Party schedule) {
        return AppointmentTestHelper.createAppointment(startTime, endTime,
                                                       schedule);
    }

    /**
     * Helper to create a new <em>act.customerTask</em>.
     *
     * @return a new act
     */
    protected Act createTask() {
        Entity taskType = createTaskType();
        Party customer = TestHelper.createCustomer();
        Party workList = AppointmentTestHelper.createWorkList();
        Act act = createAct("act.customerTask");
        ActBean bean = new ActBean(act);
        bean.setStatus(WorkflowStatus.PENDING);
        bean.setValue("startTime", new Date());
        bean.setValue("endTime", new Date());
        bean.addParticipation("participation.taskType", taskType);
        bean.addParticipation("participation.customer", customer);
        bean.addParticipation("participation.worklist", workList);
        bean.save();
        return act;
    }

    /**
     * Helper to create a new act.
     *
     * @param shortName the act short name
     * @return a new act
     */
    protected Act createAct(String shortName) {
        return (Act) create(shortName);
    }

    /**
     * Helper to create a new <em>entity.appointmentType</em>.
     *
     * @return a new appointment type
     */
    protected Entity createAppointmentType() {
        return AppointmentTestHelper.createAppointmentType();
    }

    /**
     * Helper to create a new <em>entity.taskType</em>.
     *
     * @return a new task type
     */
    protected Entity createTaskType() {
        Entity taskType = (Entity) create("entity.taskType");
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
    protected Party createSchedule(int slotSize, String slotUnits,
                                   int noSlots, Entity appointmentType) {
        return AppointmentTestHelper.createSchedule(slotSize, slotUnits,
                                                    noSlots, appointmentType);
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
        save(source);

        // reload the linked act to get any new status
        linked = get(linked);
        assertNotNull(linked);
        assertEquals(expectedStatus, linked.getStatus());

        // for completed acts, expect the endTimes to be the same
        if (WorkflowStatus.COMPLETED.equals(expectedStatus)) {
            assertEquals(source.getActivityEndTime(),
                         linked.getActivityEndTime());
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
