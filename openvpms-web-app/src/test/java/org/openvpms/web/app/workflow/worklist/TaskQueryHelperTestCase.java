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
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.app.workflow.worklist;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.openvpms.archetype.test.TestHelper.getDate;


/**
 * Tests the {@link TaskQueryHelper} class.
 *
 * @author Tim Anderson
 */
public class TaskQueryHelperTestCase extends ArchetypeServiceTest {

    /**
     * The work list.
     */
    private Party workList;

    /**
     * Sets up the test case. This creates a work list with only one slot.
     */
    @Before
    public void setUp() {
        workList = ScheduleTestHelper.createWorkList();
        IMObjectBean bean = new IMObjectBean(workList);
        bean.setValue("maxSlots", 1);
        bean.save();
    }

    /**
     * Verifies that when all of the available slots are filled a new task cannot be saved until an existing task
     * is completed.
     */
    @Test
    public void testCompleteTask() {
        Date past = getDate("2012-08-01");
        Date startDate = getDate("2012-10-03");

        Act existingTask = createTask(past);
        checkTooManyTasks(false, existingTask);

        // can't save newTask as existing task has not been completed
        Act newTask = createTask(startDate);
        checkTooManyTasks(true, newTask);

        checkTooManyTasks(false, existingTask); // can save an act that has already been saved
        existingTask.setStatus(ActStatus.COMPLETED);
        save(existingTask);

        checkTooManyTasks(false, newTask);
    }

    /**
     * Verifies that when all of the available slots are filled a new task cannot be saved until an existing task
     * is cancelled.
     */
    @Test
    public void testCancelTask() {
        Date past = getDate("2012-08-01");
        Date startDate = getDate("2012-10-03");

        Act existingTask = createTask(past);
        checkTooManyTasks(false, existingTask);

        // can't save newTask as existing task has not been completed
        Act newTask = createTask(startDate);
        checkTooManyTasks(true, newTask);

        checkTooManyTasks(false, existingTask); // can save an act that has already been saved
        existingTask.setStatus(ActStatus.CANCELLED);
        save(existingTask);

        checkTooManyTasks(false, newTask);
    }

    /**
     * Verifies that when all of the available slots are filled a new task cannot be saved if it overlaps the start time
     * of an existing future dated task.
     */
    @Test
    public void testOverlapStartTime() {
        Date startDate = getDate("2012-08-01");
        Date existingStartDate = getDate("2012-10-01");

        Act existingTask = createTask(existingStartDate);
        checkTooManyTasks(false, existingTask);

        // verify can't save new task as it overlaps existing task
        Act newTask = createTask(startDate);
        checkTooManyTasks(true, newTask);

        // give the new task an end time that doesn't overlap existing task, and verify it can be saved
        newTask.setActivityEndTime(getDate("2012-09-01"));
        checkTooManyTasks(false, newTask);
    }

    /**
     * Verifies that when all of the available slots are filled a new task cannot be saved if it overlaps the end time
     * of an existing future dated task.
     */
    @Test
    public void testOverlapEndTime() {
        Date startDate = getDate("2012-10-03");
        Date existingStart = getDate("2012-10-01");

        Act existingTask = createTask(existingStart);
        checkTooManyTasks(false, existingTask);

        // verify can't save new task as it overlaps existing task
        Act newTask = createTask(startDate);
        checkTooManyTasks(true, newTask);

        // give the existing task an end time
        existingTask.setActivityEndTime(getDate("2012-10-02"));
        save(existingTask);

        // verify can save new task
        checkTooManyTasks(false, newTask);
    }

    /**
     * Creates a new task.
     *
     * @param startTime the task start time
     * @return a new task
     */
    private Act createTask(Date startTime) {
        return ScheduleTestHelper.createTask(startTime, null, workList);
    }

    /**
     * Deterines if there are too many tasks for a worklist associated with an act
     *
     * @param tooMany if {@code true} expect too many tasks
     * @param act     the act to save if there aren't too may tasks
     */
    private void checkTooManyTasks(boolean tooMany, Act act) {
        assertEquals(tooMany, TaskQueryHelper.tooManyTasks(act));
        if (!tooMany) {
            save(act);
        }
    }
}
