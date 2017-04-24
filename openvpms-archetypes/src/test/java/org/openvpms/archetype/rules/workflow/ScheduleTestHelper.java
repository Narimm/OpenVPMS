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

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.user.UserArchetypes;
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
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.springframework.cache.ehcache.EhCacheFactoryBean;

import java.io.IOException;
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
        EntityBean bean = new EntityBean(location);
        EntityRelationship relationship = bean.addNodeRelationship("scheduleViews", view);
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
        EntityBean bean = new EntityBean(location);
        EntityRelationship relationship = bean.addNodeRelationship("workListViews", view);
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
        EntityBean bean = new EntityBean(view);
        for (Entity schedule : schedules) {
            bean.addNodeRelationship("schedules", schedule);
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
    public static Party createSchedule(Party location) {
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
    public static Party createSchedule(int slotSize, String slotUnits,
                                       int noSlots, Entity appointmentType, Party location) {
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
        bean.addNodeTarget("location", location);
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
    public static EntityRelationship addAppointmentType(Entity schedule, Entity appointmentType, int noSlots,
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
    public static Act createAppointment(Date startTime, Date endTime, Entity schedule) {
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
    public static Act createAppointment(Date startTime, Date endTime, Entity schedule, Party customer, Party patient) {
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
    public static Act createAppointment(Date startTime, Date endTime, Entity schedule, Entity appointmentType,
                                        Party customer, Party patient, User clinician, User author) {
        Act act = (Act) create(ScheduleArchetypes.APPOINTMENT);
        Lookup reason = TestHelper.getLookup(ScheduleArchetypes.VISIT_REASON, "XREASON", "Reason X", true);

        ActBean bean = new ActBean(act);
        bean.setValue("startTime", startTime);
        bean.setValue("endTime", endTime);
        bean.setValue("reason", reason.getCode());
        bean.setValue("status", AppointmentStatus.IN_PROGRESS);
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
    public static Act createTask(Date startTime, Date endTime, Entity worklist, Party customer, Party patient,
                                 User clinician, User author) {
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
    public static Act createTask(Date startTime, Date endTime, Entity worklist, Party customer, Party patient,
                                 Entity taskType, User clinician, User author) {
        Act act = (Act) create(ScheduleArchetypes.TASK);

        ActBean bean = new ActBean(act);
        bean.setValue("startTime", startTime);
        bean.setValue("endTime", endTime);
        bean.setValue("status", TaskStatus.IN_PROGRESS);
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
     * @param maxElementsInMemory specifies the maximum number of cached objects in memory
     * @return a new cache
     */
    public static Ehcache createCache(int maxElementsInMemory) {
        EhCacheFactoryBean bean = new EhCacheFactoryBean();
        bean.setCacheName("foo" + System.nanoTime());
        bean.setMaxElementsInMemory(maxElementsInMemory);
        bean.setEternal(true);
        bean.setOverflowToDisk(false);
        try {
            bean.afterPropertiesSet();
        } catch (IOException exception) {
            throw new CacheException(exception);
        }
        return bean.getObject();
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
        ActBean bean = new ActBean(act);
        bean.setValue("startTime", startTime);
        bean.setValue("endTime", endTime);
        bean.addNodeParticipation("schedule", schedule);
        bean.addNodeParticipation("type", blockType);
        if (author != null) {
            bean.addNodeParticipation("author", author);
        }
        return act;
    }

}
