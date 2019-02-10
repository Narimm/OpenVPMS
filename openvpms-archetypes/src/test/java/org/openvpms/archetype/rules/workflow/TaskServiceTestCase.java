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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.workflow;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.service.cache.BasicEhcacheManager;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.model.act.Act;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.model.party.Party;
import org.openvpms.component.model.user.User;
import org.openvpms.component.system.common.util.PropertySet;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.openvpms.archetype.test.TestHelper.getDate;


/**
 * Tests the {@link TaskService}.
 *
 * @author Tim Anderson
 */
public class TaskServiceTestCase extends AbstractScheduleServiceTest {

    /**
     * The work list.
     */
    private Party workList;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        workList = ScheduleTestHelper.createWorkList();
    }

    /**
     * Tests addition of a task.
     */
    @Test
    public void testAddEvent() {
        Date date1 = getDate("2008-01-01");
        Date date2 = getDate("2008-01-02");
        Date date3 = getDate("2008-01-03");

        ScheduleService service = initScheduleService(30);

        // retrieve the tasks for date1 and date2 and verify they are empty.
        // This caches the tasks for each date.
        long hash1 = checkEvents(workList, date1, 0);
        long hash2 = checkEvents(workList, date2, 0);
        assertNotEquals(hash1, hash2);

        // create and save task for date1. As it has no end time, it should
        // appear for the 3 dates.
        Act task = createTask(date1);

        ScheduleEvents events1 = service.getScheduleEvents(workList, date1);
        assertEquals(1, events1.size());
        PropertySet set1 = events1.getEvents().get(0);
        checkTask(task, set1);
        assertNotEquals(hash1, events1.getModHash());

        ScheduleEvents events2 = service.getScheduleEvents(workList, date2);
        assertEquals(1, events2.size());
        PropertySet set2 = events2.getEvents().get(0);
        checkTask(task, set2);
        assertNotEquals(hash2, events2.getModHash());

        ScheduleEvents events3 = service.getScheduleEvents(workList, date3);
        assertEquals(1, events3.size());
        PropertySet set3 = events3.getEvents().get(0);
        checkTask(task, set3);
    }

    /**
     * Tests removal of an event.
     */
    @Test
    public void testRemoveEvent() {
        Date date1 = getDate("2008-01-01");
        Date date2 = getDate("2008-01-02");
        Date date3 = getDate("2008-01-03");

        initScheduleService(30);
        long hash1 = checkEvents(workList, date1, 0);
        long hash2 = checkEvents(workList, date2, 0);

        Act task = createTask(date1);

        long hash3 = checkEvents(workList, date1, 1);
        assertNotEquals(hash1, hash3);

        long hash4 = checkEvents(workList, date2, 1);
        assertNotEquals(hash2, hash4);

        remove(task);

        long hash5 = checkEvents(workList, date1, 0);
        assertNotEquals(hash3, hash5);

        long hash6 = checkEvents(workList, date2, 0);
        assertNotEquals(hash4, hash6);

        checkEvents(workList, date3, 0);
    }

    /**
     * Tests moving of an event from one date to another.
     */
    @Test
    public void testChangeEventDate() {
        Date date1 = getDate("2008-01-01");
        Date date2 = getDate("2008-03-01");

        initScheduleService(30);
        long hash1 = checkEvents(workList, date1, 0);
        checkEvents(workList, date1, 0, hash1);
        long hash2 = checkEvents(workList, date2, 0);
        checkEvents(workList, date2, 0, hash2);

        Act task = createTask(date1);

        // the task has no end date, so will appear in both dates
        long hash3 = checkEvents(workList, date1, 1);
        long hash4 = checkEvents(workList, date2, 1);
        assertNotEquals(hash1, hash3);
        assertNotEquals(hash2, hash4);

        task.setActivityStartTime(date2); // move it to date2
        save(task);

        long hash5 = checkEvents(workList, date1, 0);
        assertNotEquals(hash3, hash5);

        long hash6 = checkEvents(workList, date2, 1);
        assertNotEquals(hash4, hash6);
    }

    /**
     * Tests the {@link TaskService#getEvents} method.
     */
    @Test
    public void testGetEvents() {
        final int count = 10;
        ScheduleService service = initScheduleService(30);
        Party patient = TestHelper.createPatient();
        Act[] tasks = new Act[count];
        Date date = getDate("2007-01-01");
        for (int i = 0; i < count; ++i) {
            Date startTime = DateRules.getDate(date, 15 * count, DateUnits.MINUTES);
            Date endTime = DateRules.getDate(startTime, 15, DateUnits.MINUTES);
            Date consultStartTime = (i % 2 == 0) ? new Date() : null;

            tasks[i] = createTask(startTime, endTime, workList, patient, consultStartTime);
        }

        List<PropertySet> results = service.getEvents(workList, date);
        assertEquals(count, results.size());
        for (int i = 0; i < results.size(); ++i) {
            checkTask(tasks[i], results.get(i));
        }
    }

    /**
     * Reads a schedule for a date in one thread, while updating it in another.
     * <p/>
     * Verifies that the schedule contains the expected result on completion.
     *
     * @throws Exception for any error
     */
    @Test
    public void testConcurrentReadWrite() throws Exception {
        for (int i = 0; i < 100; i++) {
            System.out.println("Concurrent read/write run: " + i + " ");
            TaskService service = createScheduleService(30);
            try {
                checkConcurrentReadWrite(service);
                System.out.println("OK");
            } finally {
                service.destroy();
            }
        }
    }

    /**
     * Reads a schedule for two separate dates in two threads, while updating it in a third.
     * <p/>
     * Verifies that the schedule contains the expected result on completion.
     *
     * @throws Exception for any error
     */
    @Test
    public void testConcurrentReadWrite2() throws Exception {
        for (int i = 0; i < 100; i++) {
            System.out.println("Concurrent read/write run: " + i + " ");
            TaskService service = createScheduleService(30);
            try {
                checkConcurrentReadWrite2(service);
                System.out.println("OK");
            } finally {
                service.destroy();
            }
        }
    }

    /**
     * Tests repeatedly reading the same sets of events for a cache size smaller than the number of dates read.
     */
    @Test
    public void testRepeatedSequentialRead() {
        Date start = getDate("2008-01-01");

        // create some open tasks
        Set<Long> tasks = new HashSet<>();
        for (int i = 0; i < 100; ++i) {
            Act task = createTask(start);
            tasks.add(task.getId());
        }

        ScheduleService service = initScheduleService(6); // cache 6 days

        // repeatedly read 10 days worth of tasks. This will force the cache to shed and re-read data.
        for (int j = 0; j < 10; ++j) {
            // read 10 days worth of tasks.
            for (int k = 0; k < 10; ++k) {
                Date date = DateRules.getDate(start, k, DateUnits.DAYS);
                Set<Long> ids = getIds(date, workList, service);
                assertEquals(tasks, ids);
            }
        }
    }

    /**
     * Creates a new {@link ScheduleService}.
     *
     * @param scheduleCacheSize the maximum number of schedule days to cache
     * @return the new service
     */
    @Override
    protected TaskService createScheduleService(int scheduleCacheSize) {
        return new TaskService(getArchetypeService(), applicationContext.getBean(ILookupService.class),
                               new BasicEhcacheManager(scheduleCacheSize));
    }

    /**
     * Creates a new schedule.
     *
     * @return the new schedule
     */
    @Override
    protected Entity createSchedule() {
        return ScheduleTestHelper.createWorkList();
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
        return createTask(date, (Party) schedule, patient);
    }

    /**
     * Reads a schedule for a date in one thread, while updating it in another.
     * <p/>
     * Verifies that the schedule contains the expected result on completion.
     *
     * @param taskService the task service
     * @throws Exception for any error
     */
    private void checkConcurrentReadWrite(TaskService taskService) throws Exception {
        final Date startTime = getDate("2007-01-01");
        final Date endTime = DateRules.getDate(startTime, 15, DateUnits.MINUTES);
        final Party schedule = ScheduleTestHelper.createWorkList();
        Party patient = TestHelper.createPatient();
        final Act task = createTask(startTime, schedule, patient);

        Callable<PropertySet> read = () -> {
            System.err.println("read");
            List<PropertySet> events = taskService.getEvents(schedule, startTime);
            assertFalse(events.size() > 1);
            return events.isEmpty() ? null : events.get(0);
        };

        Callable<PropertySet> write = () -> {
            System.err.println("write");
            task.setActivityEndTime(endTime);
            save(task);
            return null;
        };

        runConcurrent(read, write);

        List<PropertySet> events = taskService.getEvents(schedule, startTime);
        assertEquals(1, events.size());
        assertEquals(endTime, events.get(0).getDate(ScheduleEvent.ACT_END_TIME));
    }

    /**
     * Reads a schedule for two separate dates in two threads, while updating it in a third.
     * <p/>
     * Verifies that the schedule contains the expected result on completion.
     *
     * @param taskService the task service
     * @throws Exception for any error
     */
    private void checkConcurrentReadWrite2(final TaskService taskService) throws Exception {
        Date startTime = getDate("2007-01-01");
        Date endTime = DateRules.getDate(startTime, 15, DateUnits.MINUTES);
        Date date2 = getDate("2007-01-02");
        Party schedule = ScheduleTestHelper.createWorkList();
        Party patient = TestHelper.createPatient();
        Act task = createTask(startTime, schedule, patient);

        Callable<PropertySet> readDate1 = () -> {
            System.err.println("Read date1 thread=" + Thread.currentThread().getName());
            List<PropertySet> events = taskService.getEvents(schedule, startTime);
            assertFalse(events.size() > 1);
            return events.isEmpty() ? null : events.get(0);
        };
        Callable<PropertySet> readDate2 = () -> {
            System.err.println("Read date2 thread=" + Thread.currentThread().getName());
            List<PropertySet> events = taskService.getEvents(schedule, date2);
            assertFalse(events.size() > 1);
            return events.isEmpty() ? null : events.get(0);
        };
        Callable<PropertySet> write = () -> {
            System.err.println("Writer thread=" + Thread.currentThread().getName());
            task.setActivityEndTime(endTime);
            save(task);
            return null;
        };

        runConcurrent(readDate1, readDate2, write);

        List<PropertySet> events = taskService.getEvents(schedule, startTime);
        assertEquals(1, events.size());
        events = taskService.getEvents(schedule, date2);
        assertEquals(0, events.size());
    }

    /**
     * Creates and saves a new task.
     *
     * @param date the date to create the task on
     * @return a new task
     */
    private Act createTask(Date date) {
        return createTask(date, workList, TestHelper.createPatient());
    }

    /**
     * Creates and saves a new task.
     *
     * @param date     the date to create the task on
     * @param workList the work list
     * @param patient  the patient. May be {@code null}
     * @return a new task
     */
    private Act createTask(Date date, Party workList, Party patient) {
        Date startTime = DateRules.getDate(date, 15, DateUnits.MINUTES);
        return createTask(startTime, null, workList, patient, null);
    }

    /**
     * Creates and saves a new task.
     *
     * @param startTime        the start time
     * @param endTime          the end time
     * @param workList         the work list
     * @param patient          the patient. May be {@code null}
     * @param consultStartTime the consult start time. May be {@code null}
     * @return a new task
     */
    private Act createTask(Date startTime, Date endTime, Party workList, Party patient, Date consultStartTime) {
        Party customer = TestHelper.createCustomer();
        User clinician = TestHelper.createClinician();
        User author = TestHelper.createClinician();
        Act task = ScheduleTestHelper.createTask(startTime, endTime, workList, customer, patient, clinician, author);
        IMObjectBean bean = getBean(task);
        bean.setValue("consultStartTime", consultStartTime);
        save(task);
        return task;
    }

    /**
     * Verifies that a task matches the {@link PropertySet} representing it.
     *
     * @param task the task
     * @param set  the set
     */
    private void checkTask(Act task, PropertySet set) {
        IMObjectBean bean = getBean(task);
        assertEquals(task.getObjectReference(), set.get(ScheduleEvent.ACT_REFERENCE));
        assertEquals(task.getActivityStartTime(), set.get(ScheduleEvent.ACT_START_TIME));
        assertEquals(task.getActivityEndTime(), set.get(ScheduleEvent.ACT_END_TIME));
        assertEquals(task.getStatus(), set.get(ScheduleEvent.ACT_STATUS));
        assertEquals(task.getReason(), set.get(ScheduleEvent.ACT_REASON));
        assertEquals(task.getDescription(), set.get(ScheduleEvent.ACT_DESCRIPTION));
        assertEquals(bean.getTargetRef("customer"), set.get(ScheduleEvent.CUSTOMER_REFERENCE));
        assertEquals(bean.getTarget("customer").getName(), set.get(ScheduleEvent.CUSTOMER_NAME));
        assertEquals(bean.getTargetRef("patient"), set.get(ScheduleEvent.PATIENT_REFERENCE));
        assertEquals(bean.getTarget("patient").getName(), set.get(ScheduleEvent.PATIENT_NAME));
        assertEquals(bean.getTargetRef("clinician"), set.get(ScheduleEvent.CLINICIAN_REFERENCE));
        assertEquals(bean.getTarget("clinician").getName(), set.get(ScheduleEvent.CLINICIAN_NAME));
        assertEquals(bean.getTargetRef("worklist"), set.get(ScheduleEvent.SCHEDULE_REFERENCE));
        assertEquals(bean.getTarget("worklist").getName(), set.get(ScheduleEvent.SCHEDULE_NAME));
        assertEquals(bean.getTargetRef("taskType"), set.get(ScheduleEvent.SCHEDULE_TYPE_REFERENCE));
        assertEquals(bean.getTarget("taskType").getName(), set.get(ScheduleEvent.SCHEDULE_TYPE_NAME));
        assertEquals(bean.getDate("consultStartTime"), set.get(ScheduleEvent.CONSULT_START_TIME));
    }

}
