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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.workflow;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Period;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.act.DefaultActCopyHandler;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.util.EntityRelationshipHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.functor.SequenceComparator;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopier;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopyHandler;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.IterableIMObjectQuery;
import org.openvpms.component.system.common.query.JoinConstraint;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectRefSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.component.system.common.query.OrConstraint;
import org.openvpms.component.system.common.query.ParticipationConstraint;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import static org.openvpms.component.system.common.query.Constraints.and;
import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.gt;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.lt;
import static org.openvpms.component.system.common.query.Constraints.sort;
import static org.openvpms.component.system.common.query.ParticipationConstraint.Field.ActShortName;


/**
 * Appointment rules.
 *
 * @author Tim Anderson
 */
public class AppointmentRules {

    /**
     * Appointment reminder job configuration.
     */
    protected static final String APPOINTMENT_REMINDER_JOB = "entity.jobAppointmentReminder";

    /**
     * The archetype service.
     */
    private IArchetypeService service;


    /**
     * Constructs an {@link AppointmentRules}.
     *
     * @param service the archetype service
     */
    public AppointmentRules(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Returns the first view that contain the specified schedule for a given practice location.
     *
     * @param location the practice location
     * @param schedule the schedule
     * @return the view, or {@code null} if none is found
     */
    public Entity getScheduleView(Party location, Entity schedule) {
        EntityBean bean = new EntityBean(location, service);
        for (Entity view : bean.getNodeTargetEntities("scheduleViews", SequenceComparator.INSTANCE)) {
            IMObjectBean viewBean = new IMObjectBean(view, service);
            if (viewBean.hasNodeTarget("schedules", schedule)) {
                return view;
            }
        }
        return null;
    }

    /**
     * Returns the practice location associated with a schedule.
     *
     * @param schedule the schedule
     * @return the location, or {@code null} if none is found
     */
    public Party getLocation(Entity schedule) {
        IMObjectBean bean = new IMObjectBean(schedule, service);
        return (Party) bean.getNodeTargetObject("location");
    }

    /**
     * Returns the schedule slot size in minutes.
     *
     * @param schedule the schedule
     * @return the schedule slot size in minutes
     * @throws OpenVPMSException for any error
     */
    public int getSlotSize(Entity schedule) {
        IMObjectBean bean = new IMObjectBean(schedule, service);
        return getSlotSize(bean);
    }

    /**
     * Returns the default appointment type associated with a schedule.
     *
     * @param schedule the schedule
     * @return the default appointment type, or {@code null} if there is no default
     * @throws OpenVPMSException for any error
     */
    public Entity getDefaultAppointmentType(Entity schedule) {
        return EntityRelationshipHelper.getDefaultTarget(schedule, "appointmentTypes", false, service);
    }

    /**
     * Calculates an appointment end time, given the start time, schedule and
     * appointment type.
     *
     * @param startTime       the start time
     * @param schedule        an instance of <em>party.organisationSchedule</em>
     * @param appointmentType an instance of <em>entity.appointmentType</em>
     * @return the appointment end time
     * @throws OpenVPMSException for any error
     */
    public Date calculateEndTime(Date startTime, Entity schedule, Entity appointmentType) {
        EntityBean schedBean = new EntityBean(schedule, service);
        int noSlots = getSlots(schedBean, appointmentType);
        int minutes = getSlotSize(schedBean) * noSlots;
        return DateRules.getDate(startTime, minutes, DateUnits.MINUTES);
    }

    /**
     * Updates any <em>act.customerTask</em> associated with an
     * <em>act.customerAppointment</em> to ensure that it has the same status
     * (where applicable).
     *
     * @param act the appointment
     * @throws OpenVPMSException for any error
     */
    public void updateTask(Act act) {
        ActBean bean = new ActBean(act, service);
        List<Act> tasks = bean.getNodeActs("tasks");
        if (!tasks.isEmpty()) {
            Act task = tasks.get(0);
            updateStatus(act, task);
        }
    }

    /**
     * Updates any <em>act.customerAppointment</em> associated with an
     * <em>act.customerTask</em> to ensure that it has the same status
     * (where applicable).
     *
     * @param act the task
     * @throws OpenVPMSException for any error
     */
    public void updateAppointment(Act act) {
        ActBean bean = new ActBean(act, service);
        List<Act> appointments = bean.getNodeActs("appointments");
        if (!appointments.isEmpty()) {
            Act appointment = appointments.get(0);
            updateStatus(act, appointment);
        }
    }

    /**
     * Determines if a patient has an active appointment.
     *
     * @param patient the patient
     * @return an active appointment, or {@code null} if none exists
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Act getActiveAppointment(Party patient) {
        ArchetypeQuery query = new ArchetypeQuery(ScheduleArchetypes.APPOINTMENT, true, true);
        query.add(Constraints.join("patient").add(Constraints.eq("entity", patient.getObjectReference())));
        query.add(Constraints.not(Constraints.in("status", AppointmentStatus.PENDING, AppointmentStatus.CANCELLED,
                                                 AppointmentStatus.POSTED)));
        query.add(new NodeSortConstraint("startTime", false));
        query.setMaxResults(1);
        IMObjectQueryIterator<Act> iter = new IMObjectQueryIterator<>(service, query);
        return (iter.hasNext()) ? iter.next() : null;
    }

    /**
     * Copies an appointment, excluding any act relationships it may have.
     *
     * @param appointment the appointment to copy
     * @return a copy of the appointment
     */
    public Act copy(Act appointment) {
        IMObjectCopyHandler handler = new DefaultActCopyHandler() {
            {
                setCopy(Act.class, Participation.class);
                setExclude(ActRelationship.class);
            }

            @Override
            protected boolean checkCopyable(ArchetypeDescriptor archetype, NodeDescriptor node) {
                return true;
            }
        };
        IMObjectCopier copier = new IMObjectCopier(handler, service);
        return (Act) copier.apply(appointment).get(0);
    }

    /**
     * Determines if automatic reminders are enabled for a schedule or appointment type.
     *
     * @param entity the schedule or appointment type. May be {@code null}
     * @return {@code true} if reminders are enabled
     */
    public boolean isRemindersEnabled(Entity entity) {
        if (entity != null) {
            IMObjectBean bean = new IMObjectBean(entity, service);
            return bean.getBoolean("sendReminders");
        }
        return false;
    }

    /**
     * Returns the period from the current time when no appointment reminder should be sent.
     *
     * @return the period, or {@code null} if appointment reminders haven't been configured
     */
    public Period getNoReminderPeriod() {
        Period result = null;
        IMObject object = getAppointmentReminderJob();
        if (object != null) {
            IMObjectBean bean = new IMObjectBean(object, service);
            int period = bean.getInt("noReminder");
            DateUnits units = DateUnits.fromString(bean.getString("noReminderUnits"));
            if (period > 0 && units != null) {
                result = units.toPeriod(period);
            }
        }
        return result;
    }

    /**
     * Determines if an appointment is a boarding appointment.
     *
     * @param appointment the appointment
     * @return {@code true} if the appointment is a boarding appointment
     */
    public boolean isBoardingAppointment(Act appointment) {
        boolean result = false;
        ActBean bean = new ActBean(appointment, service);
        Entity schedule = bean.getNodeParticipant("schedule");
        if (schedule != null) {
            IMObjectBean scheduleBean = new IMObjectBean(schedule, service);
            if (scheduleBean.getNodeTargetObjectRef("cageType") != null) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Returns the number of days to charge boarding for an appointment.
     *
     * @param appointment the appointment
     * @return the number of days
     */
    public int getBoardingDays(Act appointment) {
        return getBoardingDays(appointment.getActivityStartTime(), appointment.getActivityEndTime());
    }

    /**
     * Returns the number of days to charge boarding for.
     *
     * @param startTime the boarding start time
     * @param endTime   the boarding end time
     * @return the number of days
     */
    public int getBoardingDays(Date startTime, Date endTime) {
        DateMidnight end = new DateMidnight(endTime);
        int days = Days.daysBetween(new DateMidnight(startTime), end).getDays();
        if (days < 0) {
            days = 0;
        } else if (DateRules.compareTo(endTime, end.toDate()) != 0) {
            // any time after midnight is another day
            days++;
        }
        return days;
    }

    /**
     * Returns the number of nights to charge boarding for.
     *
     * @param startTime the boarding start time
     * @param endTime   the boarding end time
     * @return the number of nights
     */
    public int getBoardingNights(Date startTime, Date endTime) {
        int days = getBoardingDays(startTime, endTime);
        return days > 1 ? days - 1 : days;
    }

    /**
     * Returns the patient clinical event associated with an appointment.
     *
     * @param appointment the appointment
     * @return the event, or {@code null} if none exists
     */
    public Act getEvent(Act appointment) {
        ActBean bean = new ActBean(appointment, service);
        return (Act) bean.getNodeTargetObject("event");
    }

    /**
     * Returns pending appointments for a customer.
     *
     * @param customer the customer
     * @param interval the interval, relative to the current date/time
     * @param units    the interval units
     * @return the pending appointments for the customer
     */
    public Iterable<Act> getCustomerAppointments(Party customer, int interval, DateUnits units) {
        ArchetypeQuery query = createAppointmentQuery(customer, "customer", interval, units);
        return new IterableIMObjectQuery<>(service, query);
    }

    /**
     * Returns pending appointments for a patient.
     *
     * @param patient  the patient
     * @param interval the interval, relative to the current date/time
     * @param units    the interval units
     * @return the pending appointments for the customer
     */
    public Iterable<Act> getPatientAppointments(Party patient, int interval, DateUnits units) {
        ArchetypeQuery query = createAppointmentQuery(patient, "patient", interval, units);
        return new IterableIMObjectQuery<>(service, query);
    }

    /**
     * Returns the minutes from midnight for the specified time.
     *
     * @param time the time
     * @return the minutes from midnight for {@code time}
     */
    public int getMinutes(Date time) {
        return new DateTime(time).getMinuteOfDay();
    }

    /**
     * Returns the minutes from midnight for the specified time, rounded up or down to the nearest slot.
     *
     * @param time     the time
     * @param slotSize the slot size
     * @param roundUp  if {@code true} round up to the nearest slot, otherwise round down
     * @return the minutes from midnight for the specified time
     */
    public int getSlotMinutes(Date time, int slotSize, boolean roundUp) {
        int mins = getMinutes(time);
        int result = getNearestSlot(mins, slotSize);
        if (result != mins && roundUp) {
            result += slotSize;
        }
        return result;
    }

    /**
     * Returns the time of the slot closest to that of the specified time.
     *
     * @param time     the time
     * @param slotSize the size of the slot, in minutes
     * @param roundUp  if {@code true} round up to the nearest slot, otherwise round down
     * @return the nearest slot time to {@code time}
     */
    public Date getSlotTime(Date time, int slotSize, boolean roundUp) {
        Date result;
        int mins = getMinutes(time);
        int nearestSlot = getNearestSlot(mins, slotSize);
        if (nearestSlot != mins) {
            if (roundUp) {
                nearestSlot += slotSize;
            }
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(time);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.add(Calendar.MINUTE, nearestSlot);
            result = calendar.getTime();
        } else {
            result = time;
        }
        return result;
    }

    /**
     * Returns the first appointment that overlaps the specified date range.
     * <p>
     * This ignores cancelled appointments.
     *
     * @param startTime the start of the date range
     * @param endTime   the end of the date range
     * @param schedule  the schedule
     * @return the appointment times, or {@code null} if none overlaps
     */
    public Times getOverlap(Date startTime, Date endTime, Entity schedule) {
        Times result = null;
        ArchetypeQuery query = new ArchetypeQuery(ScheduleArchetypes.APPOINTMENT);
        query.getArchetypeConstraint().setAlias("act");
        query.add(new ObjectRefSelectConstraint("act"));
        query.add(new NodeSelectConstraint("startTime"));
        query.add(new NodeSelectConstraint("endTime"));
        JoinConstraint participation = join("schedule");
        participation.add(eq("entity", schedule));

        // to encourage mysql to use the correct index
        participation.add(new ParticipationConstraint(ActShortName, ScheduleArchetypes.APPOINTMENT));
        query.add(participation);
        OrConstraint or = new OrConstraint();
        or.add(and(lt("startTime", endTime), gt("endTime", startTime)));
        query.add(or);
        query.add(sort("startTime"));
        query.add(Constraints.ne("status", ActStatus.CANCELLED));
        query.setMaxResults(1);
        ObjectSetQueryIterator iterator = new ObjectSetQueryIterator(service, query);
        if (iterator.hasNext()) {
            ObjectSet set = iterator.next();
            result = new Times(set.getReference("act.reference"), set.getDate("act.startTime"),
                               set.getDate("act.endTime"));
        }
        return result;
    }

    /**
     * Returns the nearest slot.
     *
     * @param mins     the start minutes
     * @param slotSize the slot size
     * @return the minutes from midnight for the specified time
     */
    protected int getNearestSlot(int mins, int slotSize) {
        return (mins / slotSize) * slotSize;
    }

    /**
     * Creates a pending appointment query for a party.
     *
     * @param party    a customer or patient
     * @param node     the customer/patient node
     * @param interval the interval, relative to the current date/time
     * @param units    the interval units
     * @return a new query
     */
    protected ArchetypeQuery createAppointmentQuery(Party party, String node, int interval, DateUnits units) {
        ArchetypeQuery query = new ArchetypeQuery(ScheduleArchetypes.APPOINTMENT);
        query.add(Constraints.join(node).add(Constraints.eq("entity", party)));
        Date from = new Date();
        Date to = DateRules.getDate(from, interval, units);
        query.add(Constraints.gte("startTime", from));
        query.add(Constraints.lt("startTime", to));
        query.add(Constraints.eq("status", AppointmentStatus.PENDING));
        query.add(Constraints.sort("startTime"));
        query.add(Constraints.sort("id"));
        return query;
    }

    /**
     * Returns the appointment reminder job configuration, if one is present.
     *
     * @return the configuration, or {@code null} if none exists
     */
    protected IMObject getAppointmentReminderJob() {
        ArchetypeQuery query = new ArchetypeQuery(APPOINTMENT_REMINDER_JOB, true, true);
        query.setMaxResults(1);
        Iterator<IMObject> iterator = new IMObjectQueryIterator<>(service, query);
        return (iterator.hasNext()) ? iterator.next() : null;
    }

    /**
     * Returns the schedule slot size in minutes.
     *
     * @param schedule the schedule
     * @return the schedule slot size in minutes
     * @throws OpenVPMSException for any error
     */
    private int getSlotSize(IMObjectBean schedule) {
        int slotSize = schedule.getInt("slotSize");
        String slotUnits = schedule.getString("slotUnits");
        int result;
        if ("HOURS".equals(slotUnits)) {
            result = slotSize * 60;
        } else {
            result = slotSize;
        }
        return result;
    }

    /**
     * Helper to return the no. of slots for an appointment type.
     *
     * @param schedule        the schedule
     * @param appointmentType the appointment type
     * @return the no. of slots, or {@code 0} if unknown
     * @throws OpenVPMSException for any error
     */
    private int getSlots(EntityBean schedule, Entity appointmentType) {
        int noSlots = 0;
        EntityRelationship relationship
                = schedule.getRelationship(appointmentType);
        if (relationship != null) {
            IMObjectBean bean = new IMObjectBean(relationship, service);
            noSlots = bean.getInt("noSlots");
        }
        return noSlots;
    }

    /**
     * Updates the status of a linked act to that of the supplied act,
     * where the statuses are common.
     *
     * @param act    the act
     * @param linked the act to update
     */
    private void updateStatus(Act act, Act linked) {
        String status = act.getStatus();
        // Only update the linked act status if workflow status not pending.
        if (WorkflowStatus.IN_PROGRESS.equals(status)
            || WorkflowStatus.BILLED.equals(status)
            || WorkflowStatus.COMPLETED.equals(status)
            || WorkflowStatus.CANCELLED.equals(status)) {
            if (!status.equals(linked.getStatus())) {
                linked.setStatus(status);
                if (TypeHelper.isA(linked, ScheduleArchetypes.TASK)
                    && WorkflowStatus.COMPLETED.equals(status)) {
                    // update the task's end time to now
                    linked.setActivityEndTime(new Date());
                }
                service.save(linked);
            }
        }
    }

}
