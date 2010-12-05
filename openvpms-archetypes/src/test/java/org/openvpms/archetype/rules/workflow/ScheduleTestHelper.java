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

import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.sql.Time;
import java.util.Date;


/**
 * Scheduling test helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ScheduleTestHelper extends TestHelper {

    /**
     * Helper to create and save an <em>entity.organisationScheduleView</em>,
     * with associated <em>party.organisationSchedule</em>s.
     *
     * @param schedules the schedules
     * @return a new schedule view
     */
    public static Entity createScheduleView(Party... schedules) {
        Entity view = (Entity) create("entity.organisationScheduleView");
        view.setName("XScheduleView");
        EntityBean bean = new EntityBean(view);
        for (Party schedule : schedules) {
            bean.addRelationship("entityRelationship.locationView", schedule);
        }
        bean.save();
        return view;
    }

    /**
     * Helper to create and save a new <em>entity.appointmentType</em>.
     *
     * @return a new appointment type
     */
    public static Entity createAppointmentType() {
        return createAppointmentType("XAppointmentType", true);
    }

    /**
     * Helper to create a new <em>entity.appointmentType</em>.
     *
     * @param name the appointment type name
     * @param save if <tt>true</tt> save the appointment type
     * @return a new appointment type
     */
    public static Entity createAppointmentType(String name, boolean save) {
        Entity appointmentType = (Entity) create("entity.appointmentType");
        appointmentType.setName(name);
        if (save) {
            save(appointmentType);
        }
        return appointmentType;
    }

    /**
     * Helper to create and save a <tt>party.organisationSchedule</em>.
     *
     * @return a new schedule
     */
    public static Party createSchedule() {
        return createSchedule(15, "MINUTES", 2, createAppointmentType());
    }

    /**
     * Helper to create and save new <tt>party.organisationSchedule</em>.
     *
     * @param slotSize        the schedule slot size
     * @param slotUnits       the schedule slot units
     * @param noSlots         the appointment no. of slots
     * @param appointmentType the appointment type. May be <tt>null</tt>
     * @return a new schedule
     */
    public static Party createSchedule(int slotSize, String slotUnits,
                                       int noSlots, Entity appointmentType) {
        Party schedule = (Party) create("party.organisationSchedule");
        EntityBean bean = new EntityBean(schedule);
        bean.setValue("name", "XSchedule");
        bean.setValue("slotSize", slotSize);
        bean.setValue("slotUnits", slotUnits);
        bean.setValue("startTime", Time.valueOf("09:00:00"));
        bean.setValue("endTime", Time.valueOf("18:00:00"));
        if (appointmentType != null) {
            addAppointmentType(schedule, appointmentType, noSlots, true);
        }
        bean.save();
        return schedule;
    }

    /**
     * Helper to add a appointment type to a schedule.
     *
     * @param schedule        the schedule
     * @param appointmentType the appointment type
     * @param noSlots         the appointment no. of slots
     * @param isDefault       determines if the appointment type is the default
     * @return the new <em>entityRelationship.scheduleAppointmentType</em>
     */
    public static EntityRelationship addAppointmentType(Party schedule,
                                                        Entity appointmentType,
                                                        int noSlots,
                                                        boolean isDefault) {
        EntityBean bean = new EntityBean(schedule);
        EntityRelationship relationship = bean.addRelationship(
                "entityRelationship.scheduleAppointmentType",
                appointmentType);
        IMObjectBean relBean = new IMObjectBean(relationship);
        relBean.setValue("noSlots", noSlots);
        if (isDefault) {
            relBean.setValue("default", true);
        }
        return relationship;
    }

    /**
     * Helper to create an <em>act.customerAppointment</em>.
     *
     * @param startTime the act start time
     * @param endTime   the act end time
     * @param schedule  the schedule
     * @return a new act
     */
    public static Act createAppointment(Date startTime, Date endTime,
                                        Party schedule) {
        Party customer = TestHelper.createCustomer();
        Party patient = TestHelper.createPatient();
        return createAppointment(startTime, endTime, schedule, customer,
                                 patient);
    }

    /**
     * Helper to create an <em>act.customerAppointment</em>.
     *
     * @param startTime the act start time
     * @param endTime   the act end time
     * @param schedule  the schedule
     * @param customer  the customer
     * @param patient   the patient. May be <tt>null</tt>
     * @return a new act
     */
    public static Act createAppointment(Date startTime, Date endTime,
                                        Party schedule, Party customer,
                                        Party patient) {
        return createAppointment(startTime, endTime, schedule, customer,
                                 patient, null);
    }

    /**
     * Helper to create an <em>act.customerAppointment</em>.
     *
     * @param startTime the act start time
     * @param endTime   the act end time
     * @param schedule  the schedule
     * @param customer  the customer
     * @param patient   the patient. May be <tt>null</tt>
     * @param clinician the clinician. May be <tt>null</tt>
     * @return a new act
     */
    public static Act createAppointment(Date startTime, Date endTime,
                                        Party schedule, Party customer,
                                        Party patient, User clinician) {
        Act act = (Act) create(ScheduleArchetypes.APPOINTMENT);
        Lookup reason = TestHelper.getLookup("lookup.appointmentReason", "XREASON", "Reason X", true);

        ActBean bean = new ActBean(act);
        bean.setValue("startTime", startTime);
        bean.setValue("endTime", endTime);
        bean.setValue("reason", reason.getCode());
        bean.setValue("status", AppointmentStatus.IN_PROGRESS);
        Entity appointmentType = createAppointmentType();
        appointmentType.setName("XAppointmentType");
        save(appointmentType);
        bean.setParticipant("participation.customer", customer);
        if (patient != null) {
            bean.setParticipant("participation.patient", patient);
        }
        bean.setParticipant("participation.schedule", schedule);
        bean.setParticipant("participation.appointmentType", appointmentType);
        if (clinician != null) {
            bean.setParticipant("participation.clinician", clinician);
        }
        return act;
    }

    /**
     * Helper to create and save an <em>entity.organisationScheduleView</em>,
     * with associated <em>party.organisationWorkList</em>s.
     *
     * @param workLists the work lists
     * @return a new work list view
     */
    public static Entity createWorkListView(Party... workLists) {
        Entity view = (Entity) create("entity.organisationWorkListView");
        view.setName("XWorkListView");
        EntityBean bean = new EntityBean(view);
        for (Party workList : workLists) {
            bean.addRelationship("entityRelationship.locationWorkListView",
                                 workList);
        }
        bean.save();
        return view;
    }

    /**
     * Helper to create and save a new <em>entity.taskType</em>.
     *
     * @return a new task type
     */
    public static Entity createTaskType() {
        Entity taskType = (Entity) create("entity.taskType");
        taskType.setName("XTaskType");
        save(taskType);
        return taskType;
    }

    /**
     * Helper to create a new <em>entity.taskType</em>.
     *
     * @param name the task type name
     * @param save if <tt>true</tt> save the task type
     * @return a new task type
     */
    public static Entity createTaskType(String name, boolean save) {
        Entity taskType = (Entity) create("entity.taskType");
        taskType.setName(name);
        if (save) {
            save(taskType);
        }
        return taskType;
    }


    /**
     * Helper to create a new <em>party.organisationWorkList</em>.
     *
     * @return a new work list
     */
    public static Party createWorkList() {
        Party workList = (Party) create("party.organisationWorkList");
        workList.setName("XWorkList");
        save(workList);
        return workList;
    }

    /**
     * Helper to create an <em>act.customerTask</em>.
     *
     * @param startTime the act start time
     * @param endTime   the act end time
     * @param workList  the work list
     * @return a new act
     */
    public static Act createTask(Date startTime, Date endTime,
                                 Party workList) {
        Party customer = TestHelper.createCustomer();
        Party patient = TestHelper.createPatient();
        return createTask(startTime, endTime, workList, customer, patient);
    }

    /**
     * Helper to create an <em>act.customerTask</em>.
     *
     * @param startTime the act start time
     * @param endTime   the act end time
     * @param schedule  the schedule
     * @param customer  the customer
     * @param patient   the patient. May be <tt>null</tt>
     * @return a new act
     */
    public static Act createTask(Date startTime, Date endTime, Party schedule,
                                 Party customer, Party patient) {
        return createTask(startTime, endTime, schedule, customer, patient,
                          null);
    }

    /**
     * Helper to create an <em>act.customerTask</em>.
     *
     * @param startTime the act start time
     * @param endTime   the act end time
     * @param schedule  the schedule
     * @param customer  the customer
     * @param patient   the patient. May be <tt>null</tt>
     * @param clinician the clinician. May be <tt>null</tt>
     * @return a new act
     */
    public static Act createTask(Date startTime, Date endTime,
                                 Party schedule, Party customer,
                                 Party patient, User clinician) {
        Act act = (Act) create(ScheduleArchetypes.TASK);

        ActBean bean = new ActBean(act);
        bean.setValue("startTime", startTime);
        bean.setValue("endTime", endTime);
        bean.setValue("status", TaskStatus.IN_PROGRESS);
        Entity taskType = createTaskType();
        taskType.setName("XTaskType");
        save(taskType);
        bean.setParticipant("participation.customer", customer);
        if (patient != null) {
            bean.setParticipant("participation.patient", patient);
        }
        bean.setParticipant("participation.worklist", schedule);
        bean.setParticipant("participation.taskType", taskType);
        if (clinician != null) {
            bean.setParticipant("participation.clinician", clinician);
        }
        return act;
    }

}
