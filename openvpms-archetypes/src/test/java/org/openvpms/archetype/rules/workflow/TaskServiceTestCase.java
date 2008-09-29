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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.workflow;

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.Date;
import java.util.List;

/**
 * Tests the {@link TaskService}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TaskServiceTestCase extends ArchetypeServiceTest {

    /**
     * Tests the {@link TaskService#getEvents(Entity, Date)} method.
     */
    public void test() {
        final int count = 10;
        Party schedule = ScheduleTestHelper.createWorkList();
        Act[] tasks = new Act[count];
        Date[] startTimes = new Date[count];
        Date[] endTimes = new Date[count];
        Party[] customers = new Party[count];
        Party[] patients = new Party[count];
        User[] clinicians = new User[count];
        Date date = java.sql.Date.valueOf("2007-1-1");
        Entity[] taskTypes = new Entity[count];
        for (int i = 0; i < count; ++i) {
            Date startTime = DateRules.getDate(date, 15 * count,
                                               DateUnits.MINUTES);
            Date endTime = DateRules.getDate(startTime, 15, DateUnits.MINUTES);
            Party customer = TestHelper.createCustomer();
            Party patient = TestHelper.createPatient();
            User clinician = TestHelper.createClinician();
            Act task = ScheduleTestHelper.createTask(
                    startTime, endTime, schedule, customer, patient);
            ActBean bean = new ActBean(task);
            bean.addParticipation("participation.clinician", clinician);
            tasks[i] = task;
            startTimes[i] = startTime;
            endTimes[i] = endTime;
            taskTypes[i] = bean.getNodeParticipant("taskType");
            customers[i] = customer;
            patients[i] = patient;
            clinicians[i] = clinician;
            bean.save();
        }

        TaskService service = (TaskService) applicationContext.getBean(
                "taskService");
        List<ObjectSet> results = service.getEvents(schedule, date);
        assertEquals(count, results.size());
        for (int i = 0; i < results.size(); ++i) {
            ObjectSet set = results.get(i);
            assertEquals(tasks[i].getObjectReference(),
                         set.get(ScheduleEvent.ACT_REFERENCE));
            assertEquals(startTimes[i],
                         set.get(ScheduleEvent.ACT_START_TIME));
            assertEquals(endTimes[i], set.get(ScheduleEvent.ACT_END_TIME));
            assertEquals(tasks[i].getStatus(),
                         set.get(ScheduleEvent.ACT_STATUS));
            assertEquals(tasks[i].getReason(),
                         set.get(ScheduleEvent.ACT_REASON));
            assertEquals(tasks[i].getDescription(),
                         set.get(ScheduleEvent.ACT_DESCRIPTION));
            assertEquals(customers[i].getObjectReference(),
                         set.get(ScheduleEvent.CUSTOMER_REFERENCE));
            assertEquals(customers[i].getName(),
                         set.get(ScheduleEvent.CUSTOMER_NAME));
            assertEquals(patients[i].getObjectReference(),
                         set.get(ScheduleEvent.PATIENT_REFERENCE));
            assertEquals(patients[i].getName(),
                         set.get(ScheduleEvent.PATIENT_NAME));
            assertEquals(clinicians[i].getObjectReference(),
                         set.get(ScheduleEvent.CLINICIAN_REFERENCE));
            assertEquals(clinicians[i].getName(),
                         set.get(ScheduleEvent.CLINICIAN_NAME));
            assertEquals(taskTypes[i].getObjectReference(),
                         set.get(ScheduleEvent.SCHEDULE_TYPE_REFERENCE));
            assertEquals(taskTypes[i].getName(),
                         set.get(ScheduleEvent.SCHEDULE_TYPE_NAME));
        }
    }

}
