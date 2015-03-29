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

package org.openvpms.web.workspace.workflow;

import org.openvpms.archetype.rules.workflow.AppointmentStatus;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.echo.dialog.PopupDialog;

import java.util.Arrays;
import java.util.Date;

import static org.openvpms.web.test.EchoTestHelper.fireDialogButton;


/**
 * Helper class for workflow tests.
 *
 * @author Tim Anderson
 */
public class WorkflowTestHelper extends TestHelper {

    /**
     * Helper to create an appointment.
     *
     * @param customer  the customer
     * @param patient   the patient. May be {@code null}
     * @param clinician the clinician. May be {@code null}
     * @return a new appointment
     */
    public static Act createAppointment(Party customer, Party patient, User clinician) {
        return createAppointment(new Date(), customer, patient, clinician);
    }

    /**
     * Helper to create an appointment.
     *
     * @param startTime the appointment start time
     * @param customer  the customer
     * @param patient   the patient. May be {@code null}
     * @param clinician the clinician. May be {@code null}
     * @return a new appointment
     */
    public static Act createAppointment(Date startTime, Party customer, Party patient, User clinician) {
        Party schedule = ScheduleTestHelper.createSchedule();
        Entity appointmentType = ScheduleTestHelper.createAppointmentType();

        Act act = ScheduleTestHelper.createAppointment(startTime, startTime, schedule, appointmentType, customer,
                                                       patient, clinician, null);
        act.setStatus(AppointmentStatus.PENDING);
        TestHelper.save(act);
        return act;
    }

    /**
     * Helper to create a task.
     *
     * @param customer  the customer
     * @param patient   the patient. May be {@code null}
     * @param clinician the clinician. May be {@code null}
     * @return a new task
     */
    public static Act createTask(Party customer, Party patient, User clinician) {
        Date startTime = new Date();
        return createTask(startTime, customer, patient, clinician);
    }

    /**
     * Helper to create a task.
     *
     * @param startTime the task start time
     * @param customer  the customer
     * @param patient   the patient. May be {@code null}
     * @param clinician the clinician. May be {@code null}
     * @return a new task
     */
    public static Act createTask(Date startTime, Party customer, Party patient, User clinician) {
        Party workList = ScheduleTestHelper.createWorkList();
        Act task = ScheduleTestHelper.createTask(startTime, null, workList, customer, patient, clinician, null);
        save(task);
        return task;
    }

    /**
     * Helper to create and save new {@code party.organisationWorkList}.
     *
     * @param taskType the task type. May be {@code null}
     * @param noSlots  the no. of slots the task type takes up
     * @return a new schedule
     */
    public static Party createWorkList(Entity taskType, int noSlots) {
        Party workList = (Party) create("party.organisationWorkList");
        EntityBean bean = new EntityBean(workList);
        bean.setValue("name", "XWorkList");
        if (taskType != null) {
            EntityRelationship relationship = bean.addNodeRelationship("taskTypes", taskType);
            IMObjectBean relBean = new IMObjectBean(relationship);
            relBean.setValue("noSlots", noSlots);
            relBean.setValue("default", true);
            save(Arrays.asList(workList, taskType));
        } else {
            bean.save();
        }
        return workList;
    }

    /**
     * Cancels a dialog.
     *
     * @param dialog    the dialog
     * @param userClose if {@code true} cancel via the 'user close' button, otherwise use the 'cancel' button
     */
    public static void cancelDialog(PopupDialog dialog, boolean userClose) {
        if (userClose) {
            dialog.userClose();
        } else {
            fireDialogButton(dialog, PopupDialog.CANCEL_ID);
        }
    }

}
