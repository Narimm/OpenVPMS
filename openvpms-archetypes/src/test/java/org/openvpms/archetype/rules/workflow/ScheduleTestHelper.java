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

import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * Scheduling test helper.
 *
 * @author Tim Anderson
 */
public class ScheduleTestHelper extends TestHelper {

    /**
     * Helper to a add schedule view to a practice location.
     *
     * @param location the practice location
     * @param view     the schedule view
     */
    public static void addScheduleView(Party location, Entity view) {
        addScheduleView(location, view, false);
    }

    /**
     * Helper to a add schedule view to a practice location.
     *
     * @param location    the practice location
     * @param view        the schedule view
     * @param defaultView determines if the view is the default for the location
     */
    public static void addScheduleView(Party location, Entity view, boolean defaultView) {
        IMObjectBean bean = new IMObjectBean(location);
        EntityRelationship relationship = (EntityRelationship) bean.addTarget("scheduleViews", view);
        view.addEntityRelationship(relationship);
        IMObjectBean relBean = new IMObjectBean(relationship);
        relBean.setValue("default", defaultView);
        save(location, view);
    }

    /**
     * Helper to a add a work list view to a practice location.
     *
     * @param location    the practice location
     * @param view        the work list view
     * @param defaultView determines if the view is the default for the location
     */
    public static void addWorkListView(Party location, Entity view, boolean defaultView) {
        IMObjectBean bean = new IMObjectBean(location);
        EntityRelationship relationship = (EntityRelationship) bean.addTarget("workListViews", view);
        view.addEntityRelationship(relationship);
        IMObjectBean relBean = new IMObjectBean(relationship);
        relBean.setValue("default", defaultView);
        save(location, view);
    }

    /**
     * Helper to create and save an <em>entity.organisationScheduleView</em>,
     * with associated <em>party.organisationSchedule</em>s.
     *
     * @param schedules the schedules
     * @return a new schedule view
     */
    public static Entity createScheduleView(Entity... schedules) {
        Entity view = (Entity) create("entity.organisationScheduleView");
        view.setName("XScheduleView");
        addSchedules(view, schedules);
        return view;
    }

    /**
     * Adds schedules to a schedule view.
     *
     * @param view      the view
     * @param schedules the schedules to add
     */
    public static void addSchedules(Entity view, Entity... schedules) {
        IMObjectBean bean = new IMObjectBean(view);
        for (Entity schedule : schedules) {
            EntityRelationship relationship = (EntityRelationship) bean.addTarget("schedules", schedule);
            schedule.addEntityRelationship(relationship);
        }
        List<Entity> list = new ArrayList<>();
        list.add(view);
        list.addAll(Arrays.asList(schedules));
        save(list);
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
     * @param save if {@code true} save the appointment type
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
     * Helper to create and save a <em>party.organisationSchedule</em>.
     *
     * @param location the practice location
     * @return a new schedule
     */
    public static Party createSchedule(org.openvpms.component.model.party.Party location) {
        return createSchedule(15, "MINUTES", 2, createAppointmentType(), location);
    }

    /**
     * Helper to create and save new <em>party.organisationSchedule</em>.
     *
     * @param slotSize        the schedule slot size
     * @param slotUnits       the schedule slot units
     * @param noSlots         the appointment no. of slots
     * @param appointmentType the appointment type. May be {@code null}
     * @param location        the practice location
     * @return a new schedule
     */
    public static Party createSchedule(int slotSize, String slotUnits, int noSlots,
                                       org.openvpms.component.model.entity.Entity appointmentType,
                                       org.openvpms.component.model.party.Party location) {
        Party schedule = (Party) create(ScheduleArchetypes.ORGANISATION_SCHEDULE);
        IMObjectBean bean = new IMObjectBean(schedule);
        bean.setValue("name", "XSchedule");
        bean.setValue("slotSize", slotSize);
        bean.setValue("slotUnits", slotUnits);
        bean.setValue("startTime", Time.valueOf("09:00:00"));
        bean.setValue("endTime", Time.valueOf("18:00:00"));
        if (appointmentType != null) {
            addAppointmentType(schedule, appointmentType, noSlots, true);
        }
        bean.setTarget("location", location);
        bean.save();
        return schedule;
    }

    /**
     * Creates a boarding schedule.
     *
     * @param location the practice location
     * @param cageType the cage type
     * @return a new schedule
     */
    public static Party createSchedule(Party location, Entity cageType) {
        Party schedule = (Party) create(ScheduleArchetypes.ORGANISATION_SCHEDULE);
        IMObjectBean bean = new IMObjectBean(schedule);
        bean.setValue("name", "XSchedule");
        bean.addNodeTarget("location", location);
        bean.addNodeTarget("cageType", cageType);
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
    public static EntityRelationship addAppointmentType(Entity schedule,
                                                        org.openvpms.component.model.entity.Entity appointmentType,
                                                        int noSlots, boolean isDefault) {
        IMObjectBean bean = new IMObjectBean(schedule);
        EntityRelationship relationship = (EntityRelationship) bean.addTarget("appointmentTypes", appointmentType);
        appointmentType.addEntityRelationship(relationship);
        IMObjectBean relBean = new IMObjectBean(relationship);
        relBean.setValue("noSlots", noSlots);
        if (isDefault) {
            relBean.setValue("default", true);
        }
        return relationship;
    }

    /**
     * Adds work lists to a schedule.
     * <p/>
     * This sets the {@code useAllWorkLists} flog to {@code false}.
     *
     * @param schedule  the schedule
     * @param worklists the work lists
     */
    public static void addWorkLists(Entity schedule, Entity... worklists) {
        IMObjectBean bean = new IMObjectBean(schedule);
        bean.setValue("useAllWorkLists", false);
        for (Entity worklist : worklists) {
            EntityRelationship relationship = (EntityRelationship) bean.addTarget("workLists", worklist);
            worklist.addEntityRelationship(relationship);
        }
        bean.save(worklists);
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
                                        org.openvpms.component.model.entity.Entity schedule) {
        Party customer = TestHelper.createCustomer();
        Party patient = TestHelper.createPatient();
        return createAppointment(startTime, endTime, schedule, customer, patient);
    }

    /**
     * Helper to create an <em>act.customerAppointment</em>.
     *
     * @param startTime the act start time
     * @param endTime   the act end time
     * @param schedule  the schedule
     * @param customer  the customer
     * @param patient   the patient. May be {@code null}
     * @return a new act
     */
    public static Act createAppointment(Date startTime, Date endTime,
                                        org.openvpms.component.model.entity.Entity schedule,
                                        org.openvpms.component.model.party.Party customer,
                                        org.openvpms.component.model.party.Party patient) {
        Entity appointmentType = createAppointmentType();
        appointmentType.setName("XAppointmentType");
        save(appointmentType);
        return createAppointment(startTime, endTime, schedule, appointmentType, customer, patient, null, null);
    }

    /**
     * Helper to create a 15 minute appointment.
     *
     * @param startTime the appointment start time
     * @param schedule  the schedule
     * @param customer  the customer
     * @param patient   the patient. May be {@code null}
     * @param status    the appointment status
     * @return a new appointment
     */
    public static Act createAppointment(Date startTime, Entity schedule, Party customer, Party patient, String status) {
        Date endTime = DateRules.getDate(startTime, 15, DateUnits.MINUTES);
        Act appointment = createAppointment(startTime, endTime, schedule, customer, patient);
        appointment.setStatus(status);
        return appointment;
    }

    /**
     * Helper to create an <em>act.customerAppointment</em>.
     *
     * @param startTime       the act start time
     * @param endTime         the act end time
     * @param schedule        the schedule
     * @param appointmentType the appointment type
     * @param customer        the customer
     * @param patient         the patient. May be {@code null}
     * @param clinician       the clinician. May be {@code null}
     * @param author          the author. May be {@code null}
     * @return a new act
     */
    public static Act createAppointment(Date startTime, Date endTime,
                                        org.openvpms.component.model.entity.Entity schedule,
                                        org.openvpms.component.model.entity.Entity appointmentType,
                                        org.openvpms.component.model.party.Party customer,
                                        org.openvpms.component.model.party.Party patient,
                                        org.openvpms.component.model.user.User clinician,
                                        org.openvpms.component.model.user.User author) {
        Act act = (Act) create(ScheduleArchetypes.APPOINTMENT);
        Lookup reason = TestHelper.getLookup(ScheduleArchetypes.VISIT_REASON, "XREASON", "Reason X", true);

        IMObjectBean bean = new IMObjectBean(act);
        bean.setValue("startTime", startTime);
        bean.setValue("endTime", endTime);
        bean.setValue("reason", reason.getCode());
        bean.setValue("status", AppointmentStatus.IN_PROGRESS);
        bean.setTarget("customer", customer);
        if (patient != null) {
            bean.setTarget("patient", patient);
        }
        bean.setTarget("schedule", schedule);
        bean.setTarget("appointmentType", appointmentType);
        if (clinician != null) {
            bean.setTarget("clinician", clinician);
        }
        if (author != null) {
            bean.setTarget("author", author);
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
    public static Entity createWorkListView(Entity... workLists) {
        Entity view = (Entity) create("entity.organisationWorkListView");
        view.setName("XWorkListView");
        IMObjectBean bean = new IMObjectBean(view);
        for (Entity workList : workLists) {
            EntityRelationship relationship = (EntityRelationship) bean.addTarget("workLists", workList);
            workList.addEntityRelationship(relationship);
        }
        bean.save(workLists);
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
     * @param save if {@code true} save the task type
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
     * @param taskType the task type. May be {@code null}
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
        IMObjectBean bean = new IMObjectBean(workList);
        EntityRelationship relationship = (EntityRelationship) bean.addTarget("taskTypes", taskType);
        taskType.addEntityRelationship(relationship);
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
    public static Act createTask(Date startTime, Date endTime, Entity workList) {
        Party customer = TestHelper.createCustomer();
        Party patient = TestHelper.createPatient();
        return createTask(startTime, endTime, workList, customer, patient);
    }

    /**
     * Helper to create an <em>act.customerTask</em>.
     *
     * @param startTime the act start time
     * @param endTime   the act end time
     * @param workList  the work list
     * @param customer  the customer
     * @param patient   the patient. May be {@code null}
     * @return a new act
     */
    public static Act createTask(Date startTime, Date endTime, Entity workList, Party customer, Party patient) {
        return createTask(startTime, endTime, workList, customer, patient, null, null);
    }

    /**
     * Helper to create an <em>act.customerTask</em>.
     *
     * @param startTime the act start time
     * @param endTime   the act end time
     * @param worklist  the work list
     * @param customer  the customer
     * @param patient   the patient. May be {@code null}
     * @param clinician the clinician. May be {@code null}
     * @param author    the author. May be {@code null}
     * @return a new act
     */
    public static Act createTask(Date startTime, Date endTime, org.openvpms.component.model.entity.Entity worklist,
                                 org.openvpms.component.model.party.Party customer,
                                 org.openvpms.component.model.party.Party patient,
                                 org.openvpms.component.model.user.User clinician,
                                 org.openvpms.component.model.user.User author) {
        return createTask(startTime, endTime, worklist, customer, patient, createTaskType(), clinician, author);
    }

    /**
     * Helper to create an <em>act.customerTask</em>.
     *
     * @param startTime the act start time
     * @param endTime   the act end time
     * @param worklist  the work list
     * @param customer  the customer
     * @param patient   the patient. May be {@code null}
     * @param taskType  the task type
     * @param clinician the clinician. May be {@code null}
     * @param author    the author. May be {@code null}
     * @return a new act
     */
    public static Act createTask(Date startTime, Date endTime,
                                 org.openvpms.component.model.entity.Entity worklist,
                                 org.openvpms.component.model.party.Party customer,
                                 org.openvpms.component.model.party.Party patient,
                                 org.openvpms.component.model.entity.Entity taskType,
                                 org.openvpms.component.model.user.User clinician,
                                 org.openvpms.component.model.user.User author) {
        Act act = (Act) create(ScheduleArchetypes.TASK);

        IMObjectBean bean = new IMObjectBean(act);
        bean.setValue("startTime", startTime);
        bean.setValue("endTime", endTime);
        bean.setValue("status", TaskStatus.IN_PROGRESS);
        bean.setTarget("customer", customer);
        if (patient != null) {
            bean.setTarget("patient", patient);
        }
        bean.setTarget("worklist", worklist);
        bean.setTarget("taskType", taskType);
        if (clinician != null) {
            bean.setTarget("clinician", clinician);
        }
        if (author != null) {
            bean.setTarget("author", author);
        }
        return act;
    }

    /**
     * Creates a new cage type.
     *
     * @param name the cage type name
     * @return a new cage type
     */
    public static Entity createCageType(String name) {
        Product product = TestHelper.createProduct(ProductArchetypes.SERVICE, null);
        return createCageType(name, product, null, null, null);
    }

    /**
     * Creates a new cage type.
     *
     * @param name                  the cage type name
     * @param firstPetProductDay    the first pet product, day rate
     * @param firstPetProductNight  the first pet product, overnight rate. May be {@code null}
     * @param secondPetProductDay   the second pet product, day rate
     * @param secondPetProductNight the second pet product, overnight rate. May be {@code null}
     * @return a new cage type
     */
    public static Entity createCageType(String name, Product firstPetProductDay, Product firstPetProductNight,
                                        Product secondPetProductDay, Product secondPetProductNight) {
        return createCageType(name, firstPetProductDay, firstPetProductNight, secondPetProductDay,
                              secondPetProductNight, null, null);
    }

    /**
     * Creates a new cage type.
     *
     * @param name                  the cage type name
     * @param firstPetProductDay    the first pet product, day rate
     * @param firstPetProductNight  the first pet product, overnight rate. May be {@code null}
     * @param secondPetProductDay   the second pet product, day rate
     * @param secondPetProductNight the second pet product, overnight rate. May be {@code null}
     * @param lateCheckoutTime      the late checkout time. May be {@code null}
     * @param lateCheckoutProduct   the late checkout product. May be {@code null}
     * @return a new cage type
     */
    public static Entity createCageType(String name, Product firstPetProductDay, Product firstPetProductNight,
                                        Product secondPetProductDay, Product secondPetProductNight,
                                        Time lateCheckoutTime, Product lateCheckoutProduct) {
        Entity entity = (Entity) create(ScheduleArchetypes.CAGE_TYPE);
        entity.setName(name);
        IMObjectBean bean = new IMObjectBean(entity);
        bean.addNodeTarget("firstPetProductDay", firstPetProductDay);
        if (firstPetProductNight != null) {
            bean.addNodeTarget("firstPetProductNight", firstPetProductNight);
        }
        if (secondPetProductDay != null) {
            bean.addNodeTarget("secondPetProductDay", secondPetProductDay);
        }
        if (secondPetProductNight != null) {
            bean.addNodeTarget("secondPetProductNight", secondPetProductNight);
        }
        if (lateCheckoutTime != null) {
            bean.setValue("lateCheckoutTime", lateCheckoutTime);
        }
        if (lateCheckoutProduct != null) {
            bean.addNodeTarget("lateCheckoutProduct", lateCheckoutProduct);
        }
        save(entity);
        return entity;
    }

    /**
     * Helper to create and save a new <em>entity.calendarBlockType</em>.
     *
     * @return a new calendar block type
     */
    public static Entity createCalendarBlockType() {
        return createCalendarBlockType("XCalendarBlockType");
    }

    /**
     * Helper to create and save a new <em>entity.calendarBlockType</em>.
     *
     * @param name the block type name
     * @return a new block type
     */
    public static Entity createCalendarBlockType(String name) {
        Entity blockType = (Entity) create(ScheduleArchetypes.CALENDAR_BLOCK_TYPE);
        blockType.setName(name);
        save(blockType);
        return blockType;
    }

    /**
     * Helper to create an <em>act.calendarBlock</em>.
     *
     * @param startTime the act start time
     * @param endTime   the act end time
     * @param schedule  the schedule
     * @param blockType the calendar block type
     * @param author    the author. May be {@code null}
     * @return a new act
     */
    public static Act createCalendarBlock(Date startTime, Date endTime, Entity schedule, Entity blockType,
                                          User author) {
        Act act = (Act) create(ScheduleArchetypes.CALENDAR_BLOCK);
        IMObjectBean bean = new IMObjectBean(act);
        bean.setValue("startTime", startTime);
        bean.setValue("endTime", endTime);
        bean.setTarget("schedule", schedule);
        bean.setTarget("type", blockType);
        if (author != null) {
            bean.setTarget("author", author);
        }
        return act;
    }

    /**
     * Helper to create a new <em>act.calendarEvent</em>.
     *
     * @param startTime the act start time
     * @param endTime   the act end time
     * @param schedule  the schedule
     * @param location  the practice location. May be {@code null}
     * @return a new event
     */
    public static Act createCalendarEvent(Date startTime, Date endTime, Entity schedule, Party location) {
        Act act = (Act) create(ScheduleArchetypes.CALENDAR_EVENT);
        IMObjectBean bean = new IMObjectBean(act);
        bean.setValue("startTime", startTime);
        bean.setValue("endTime", endTime);
        bean.setTarget("schedule", schedule);
        if (location != null) {
            bean.setTarget("location", location);
        }
        return act;
    }

    /**
     * Creates a roster area.
     *
     * @param location  the practice location
     * @param schedules the schedules
     * @return a new roster area
     */
    public static Entity createRosterArea(Party location, Entity... schedules) {
        Entity area = (Entity) create(ScheduleArchetypes.ROSTER_AREA);
        IMObjectBean bean = new IMObjectBean(area);
        bean.setValue("name", "XArea");
        bean.setTarget("location", location);
        for (Entity schedule : schedules) {
            bean.addTarget("schedules", schedule);
        }
        bean.save();
        return area;
    }

    /**
     * Helper to create a new <em>act.rosterEvent</em>.
     *
     * @param startTime the act start time
     * @param endTime   the act end time
     * @param user      the rostered employee. May be {@code null}
     * @param area      the area
     * @param location  the location
     * @return a new event
     */
    public static Act createRosterEvent(Date startTime, Date endTime, User user, Entity area, Party location) {
        Act act = (Act) create(ScheduleArchetypes.ROSTER_EVENT);
        IMObjectBean bean = new IMObjectBean(act);
        bean.setValue("startTime", startTime);
        bean.setValue("endTime", endTime);
        bean.setTarget("user", user);
        bean.setTarget("schedule", area);
        bean.setTarget("location", location);
        return act;
    }

}
