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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.workflow;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.util.PropertySet;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.openvpms.archetype.test.TestHelper.getDate;


/**
 * Tests the {@link TaskService}.
 *
 * @author Tim Anderson
 */
public class TaskServiceTestCase extends ArchetypeServiceTest {

    /**
     * The task service.
     */
    private ScheduleService service;

    /**
     * The work list.
     */
    private Party workList;


    /**
     * Tests addition of a task.
     */
    @Test
    public void testAddEvent() {
        Date date1 = getDate("2008-01-01");
        Date date2 = getDate("2008-01-02");
        Date date3 = getDate("2008-01-03");

        // retrieve the tasks for date1 and date2 and verify they are empty.
        // This caches the tasks for each date.
        List<PropertySet> results = service.getEvents(workList, date1);
        assertEquals(0, results.size());

        results = service.getEvents(workList, date2);
        assertEquals(0, results.size());

        // create and save task for date1. As it has no end time, it should
        // appear for the 3 dates.
        Act task = createTask(date1);

        results = service.getEvents(workList, date1);
        assertEquals(1, results.size());
        PropertySet set = results.get(0);
        checkTask(task, set);

        results = service.getEvents(workList, date2);
        assertEquals(1, results.size());
        set = results.get(0);
        checkTask(task, set);

        results = service.getEvents(workList, date3);
        assertEquals(1, results.size());
        set = results.get(0);
        checkTask(task, set);
    }

    /**
     * Tests removal of an event.
     */
    @Test
    public void testRemoveEvent() {
        Date date1 = getDate("2008-01-01");
        Date date2 = getDate("2008-01-02");
        Date date3 = getDate("2008-01-03");

        List<PropertySet> results = service.getEvents(workList, date1);
        assertEquals(0, results.size());

        results = service.getEvents(workList, date2);
        assertEquals(0, results.size());

        Act task = createTask(date1);

        results = service.getEvents(workList, date1);
        assertEquals(1, results.size());

        results = service.getEvents(workList, date2);
        assertEquals(1, results.size());

        getArchetypeService().remove(task);

        assertEquals(0, service.getEvents(workList, date1).size());
        assertEquals(0, service.getEvents(workList, date2).size());
        assertEquals(0, service.getEvents(workList, date3).size());
    }

    /**
     * Tests moving of an event from one date to another.
     */
    @Test
    public void testChangeEventDate() {
        Date date1 = getDate("2008-01-01");
        Date date2 = getDate("2008-03-01");

        service.getEvents(workList, date1);
        assertEquals(0, service.getEvents(workList, date1).size());
        assertEquals(0, service.getEvents(workList, date2).size());

        Act task = createTask(date1);

        assertEquals(1, service.getEvents(workList, date1).size());
        assertEquals(1, service.getEvents(workList, date2).size());

        task.setActivityStartTime(date2); // move it to date2
        getArchetypeService().save(task);

        assertEquals(0, service.getEvents(workList, date1).size());
        assertEquals(1, service.getEvents(workList, date2).size());
    }

    /**
     * Tests moving of an event from one worklist to another.
     */
    @Test
    public void testChangeEventWorkList() {
        Date date = getDate("2008-01-01");

        service.getEvents(workList, date);
        assertEquals(0, service.getEvents(workList, date).size());

        Act task = createTask(date);
        assertEquals(1, service.getEvents(workList, date).size());

        Party workList2 = ScheduleTestHelper.createWorkList();
        ActBean bean = new ActBean(task);
        bean.setParticipant(ScheduleArchetypes.WORKLIST_PARTICIPATION, workList2);

        getArchetypeService().save(task);

        assertEquals(0, service.getEvents(workList, date).size());
        assertEquals(1, service.getEvents(workList2, date).size());
    }

    /**
     * Tests the {@link TaskService#getEvents(Entity, Date)} method.
     */
    @Test
    public void testGetEvents() {
        final int count = 10;
        Party schedule = ScheduleTestHelper.createWorkList();
        Act[] tasks = new Act[count];
        Date date = getDate("2007-01-01");
        for (int i = 0; i < count; ++i) {
            Date startTime = DateRules.getDate(date, 15 * count, DateUnits.MINUTES);
            Date endTime = DateRules.getDate(startTime, 15, DateUnits.MINUTES);
            Date consultStartTime = (i % 2 == 0) ? new Date() : null;

            tasks[i] = createTask(startTime, endTime, schedule, consultStartTime);
        }

        List<PropertySet> results = service.getEvents(schedule, date);
        assertEquals(count, results.size());
        for (int i = 0; i < results.size(); ++i) {
            checkTask(tasks[i], results.get(i));
        }
    }

    /**
     * Creates and saves a new task.
     *
     * @param date the date to create the task on
     * @return a new task
     */
    private Act createTask(Date date) {
        return createTask(date, workList);
    }

    /**
     * Creates and saves a new task.
     *
     * @param date     the date to create the task on
     * @param workList the work list
     * @return a new task
     */
    private Act createTask(Date date, Party workList) {
        Date startTime = DateRules.getDate(date, 15, DateUnits.MINUTES);
        return createTask(startTime, null, workList, null);
    }

    /**
     * Creates and saves a new task.
     *
     * @param startTime        the start time
     * @param endTime          the end time
     * @param workList         the work list
     * @param consultStartTime the consult start time. May be {@code null}
     * @return a new task
     */
    private Act createTask(Date startTime, Date endTime, Party workList, Date consultStartTime) {
        Party customer = TestHelper.createCustomer();
        Party patient = TestHelper.createPatient();
        User clinician = TestHelper.createClinician();
        User author = TestHelper.createClinician();
        Act task = ScheduleTestHelper.createTask(startTime, endTime, workList, customer, patient, clinician, author);
        IMObjectBean bean = new IMObjectBean(task);
        bean.setValue("consultStartTime", consultStartTime);
        save(task);
        return task;
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        service = (ScheduleService) applicationContext.getBean("taskService");
        workList = ScheduleTestHelper.createWorkList();
    }

    /**
     * Verifies that a task matches the {@link PropertySet} representing it.
     *
     * @param task the task
     * @param set  the set
     */
    private void checkTask(Act task, PropertySet set) {
        ActBean bean = new ActBean(task);
        assertEquals(task.getObjectReference(), set.get(ScheduleEvent.ACT_REFERENCE));
        assertEquals(task.getActivityStartTime(), set.get(ScheduleEvent.ACT_START_TIME));
        assertEquals(task.getActivityEndTime(), set.get(ScheduleEvent.ACT_END_TIME));
        assertEquals(task.getStatus(), set.get(ScheduleEvent.ACT_STATUS));
        assertEquals(task.getReason(), set.get(ScheduleEvent.ACT_REASON));
        assertEquals(task.getDescription(), set.get(ScheduleEvent.ACT_DESCRIPTION));
        assertEquals(bean.getNodeParticipantRef("customer"), set.get(ScheduleEvent.CUSTOMER_REFERENCE));
        assertEquals(bean.getNodeParticipant("customer").getName(), set.get(ScheduleEvent.CUSTOMER_NAME));
        assertEquals(bean.getNodeParticipantRef("patient"), set.get(ScheduleEvent.PATIENT_REFERENCE));
        assertEquals(bean.getNodeParticipant("patient").getName(), set.get(ScheduleEvent.PATIENT_NAME));
        assertEquals(bean.getNodeParticipantRef("clinician"), set.get(ScheduleEvent.CLINICIAN_REFERENCE));
        assertEquals(bean.getNodeParticipant("clinician").getName(), set.get(ScheduleEvent.CLINICIAN_NAME));
        assertEquals(bean.getNodeParticipantRef("worklist"), set.get(ScheduleEvent.SCHEDULE_REFERENCE));
        assertEquals(bean.getNodeParticipant("worklist").getName(), set.get(ScheduleEvent.SCHEDULE_NAME));
        assertEquals(bean.getNodeParticipantRef("taskType"), set.get(ScheduleEvent.SCHEDULE_TYPE_REFERENCE));
        assertEquals(bean.getNodeParticipant("taskType").getName(), set.get(ScheduleEvent.SCHEDULE_TYPE_NAME));
        assertEquals(bean.getDate("consultStartTime"), set.get(ScheduleEvent.CONSULT_START_TIME));
    }

}
