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

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.user.UserArchetypes;
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
import org.springframework.cache.ehcache.EhCacheFactoryBean;

import java.io.IOException;
import java.sql.Time;
import java.util.Date;


/**
 * Scheduling test helper.
 *
 * @author Tim Anderson
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
            bean.addNodeRelationship("schedules", schedule);
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
        Entity appointmentType = (Entity) create(ScheduleArchetypes.APPOINTMENT_TYPE);
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
        Party schedule = (Party) create(ScheduleArchetypes.ORGANISATION_SCHEDULE);
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
                ScheduleArchetypes.SCHEDULE_APPOINTMENT_TYPE_RELATIONSHIP, appointmentType);
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
        Entity appointmentType = createAppointmentType();
        appointmentType.setName("XAppointmentType");
        return createAppointment(startTime, endTime, schedule, appointmentType, customer, patient, null, null);
    }

    /**
     * Helper to create an <em>act.customerAppointment</em>.
     *
     * @param startTime       the act start time
     * @param endTime         the act end time
     * @param schedule        the schedule
     * @param appointmentType the appointment type
     * @param customer        the customer
     * @param patient         the patient. May be <tt>null</tt>
     * @param clinician       the clinician. May be <tt>null</tt>
     * @param author          the author. May be <tt>null</tt>
     * @return a new act
     */
    public static Act createAppointment(Date startTime, Date endTime,
                                        Party schedule, Entity appointmentType, Party customer,
                                        Party patient, User clinician, User author) {
        Act act = (Act) create(ScheduleArchetypes.APPOINTMENT);
        Lookup reason = TestHelper.getLookup("lookup.appointmentReason", "XREASON", "Reason X", true);

        ActBean bean = new ActBean(act);
        bean.setValue("startTime", startTime);
        bean.setValue("endTime", endTime);
        bean.setValue("reason", reason.getCode());
        bean.setValue("status", AppointmentStatus.IN_PROGRESS);
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
        if (author != null) {
            bean.setParticipant("participation.author", author);
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
            bean.addNodeRelationship("workLists", workList);
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
        return createTaskType("XTaskType", true);
    }

    /**
     * Helper to create a new <em>entity.taskType</em>.
     *
     * @param name the task type name
     * @param save if <tt>true</tt> save the task type
     * @return a new task type
     */
    public static Entity createTaskType(String name, boolean save) {
        Entity taskType = (Entity) create(ScheduleArchetypes.TASK_TYPE);
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
        return createWorkList(2, null);
    }

    /**
     * Helper to create a new <em>party.organisationWorkList</em>, linked to a task type.
     *
     * @param taskType the task type. May be <tt>null</tt>
     * @param noSlots  the no. of slots
     * @return a new work list
     */
    public static Party createWorkList(int noSlots, Entity taskType) {
        Party workList = (Party) create(ScheduleArchetypes.ORGANISATION_WORKLIST);
        workList.setName("XWorkList");
        if (taskType != null) {
            addTaskType(workList, taskType, noSlots, true);
            save(workList, taskType);
        } else {
            save(workList);
        }
        return workList;
    }

    /**
     * Helper to add a appointment type to a schedule.
     *
     * @param workList  the work list
     * @param taskType  the appointment type
     * @param noSlots   the work list no. of slots
     * @param isDefault determines if the appointment type is the default
     * @return the new <em>entityRelationship.scheduleAppointmentType</em>
     */
    public static EntityRelationship addTaskType(Party workList, Entity taskType, int noSlots, boolean isDefault) {
        EntityBean bean = new EntityBean(workList);
        EntityRelationship relationship = bean.addRelationship(ScheduleArchetypes.WORKLIST_TASK_TYPE_RELATIONSHIP,
                                                               taskType);
        IMObjectBean relBean = new IMObjectBean(relationship);
        relBean.setValue("noSlots", noSlots);
        if (isDefault) {
            relBean.setValue("default", true);
        }
        return relationship;
    }

    /**
     * Helper to create an <em>act.customerTask</em>.
     *
     * @param startTime the act start time
     * @param endTime   the act end time
     * @param workList  the work list
     * @return a new act
     */
    public static Act createTask(Date startTime, Date endTime, Party workList) {
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
        return createTask(startTime, endTime, schedule, customer, patient, null, null);
    }

    /**
     * Helper to create an <em>act.customerTask</em>.
     *
     * @param startTime the act start time
     * @param endTime   the act end time
     * @param worklist  the work list
     * @param customer  the customer
     * @param patient   the patient. May be <tt>null</tt>
     * @param clinician the clinician. May be <tt>null</tt>
     * @param author    the author. May be <tt>null</tt>
     * @return a new act
     */
    public static Act createTask(Date startTime, Date endTime, Party worklist, Party customer, Party patient,
                                 User clinician, User author) {
        Act act = (Act) create(ScheduleArchetypes.TASK);

        ActBean bean = new ActBean(act);
        bean.setValue("startTime", startTime);
        bean.setValue("endTime", endTime);
        bean.setValue("status", TaskStatus.IN_PROGRESS);
        Entity taskType = createTaskType();
        bean.setParticipant(CustomerArchetypes.CUSTOMER_PARTICIPATION, customer);
        if (patient != null) {
            bean.setParticipant(PatientArchetypes.PATIENT_PARTICIPATION, patient);
        }
        bean.setParticipant(ScheduleArchetypes.WORKLIST_PARTICIPATION, worklist);
        bean.setParticipant(ScheduleArchetypes.TASK_TYPE_PARTICIPATION, taskType);
        if (clinician != null) {
            bean.setParticipant(UserArchetypes.CLINICIAN_PARTICIPATION, clinician);
        }
        if (author != null) {
            bean.setParticipant(UserArchetypes.AUTHOR_PARTICIPATION, author);
        }
        return act;
    }

    /**
     * Helper to create an in-memory cache.
     *
     * @return a new cache
     */
    public static Ehcache createCache() {
        EhCacheFactoryBean bean = new EhCacheFactoryBean();
        bean.setCacheName("foo" + System.nanoTime());
        bean.setMaxElementsInMemory(30);
        bean.setEternal(true);
        bean.setOverflowToDisk(false);
        try {
            bean.afterPropertiesSet();
        } catch (IOException exception) {
            throw new CacheException(exception);
        }
        return bean.getObject();
    }
}
