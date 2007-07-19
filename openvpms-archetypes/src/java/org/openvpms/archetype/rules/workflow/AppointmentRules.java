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

import org.apache.commons.lang.time.DateUtils;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.NamedQuery;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.Date;
import java.util.List;


/**
 * Appointment rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AppointmentRules {

    /**
     * The archetype service.
     */
    private IArchetypeService service;


    /**
     * Creates a new <tt>AppointmentRules</tt>.
     */
    public AppointmentRules() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Creates a new <tt>AppointmentRules</tt>.
     *
     * @param service the archetype service
     */
    public AppointmentRules(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Returns the schedule slot size in minutes.
     *
     * @param schedule the schedule
     * @return the schedule slot size in minutes
     * @throws OpenVPMSException for any error
     */
    public int getSlotSize(Party schedule) {
        EntityBean bean = new EntityBean(schedule, service);
        return getSlotSize(bean);
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
    public Date calculateEndTime(Date startTime, Party schedule,
                                 Entity appointmentType) {
        EntityBean schedBean = new EntityBean(schedule, service);
        int noSlots = getSlots(schedBean, appointmentType);
        int minutes = getSlotSize(schedBean) * noSlots;
        int millis = minutes * DateUtils.MILLIS_IN_MINUTE;
        return new Date(startTime.getTime() + millis);
    }

    /**
     * Determines if there are acts that overlap with an appointment.
     *
     * @param appointment the appointment
     * @return a list of acts that overlap with the appointment
     * @throws OpenVPMSException for any error
     */
    public boolean hasOverlappingAppointments(Act appointment) {
        long uid = appointment.getUid();
        ActBean bean = new ActBean(appointment, service);
        IMObjectReference schedule
                = bean.getParticipantRef("participation.schedule");
        Date startTime = appointment.getActivityStartTime();
        Date endTime = appointment.getActivityEndTime();

        if (startTime != null && endTime != null && schedule != null) {
            return hasOverlappingAppointments(uid, startTime, endTime,
                                              schedule);
        }
        return false;
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
        List<Act> tasks = bean.getActsForNode("tasks");
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
     * @param act
     * @throws OpenVPMSException for any error
     */
    public void updateAppointment(Act act) {
        ActBean bean = new ActBean(act, service);
        List<Act> appointments = bean.getActsForNode("appointments");
        if (!appointments.isEmpty()) {
            Act appointment = appointments.get(0);
            updateStatus(act, appointment);
        }
    }

    /**
     * Determines if there are acts that overlap with an appointment.
     *
     * @param uid       the appointment id
     * @param startTime the appointment start time
     * @param endTime   the appointment end time
     * @param schedule  the schedule
     * @return a list of acts that overlap with the appointment
     * @throws OpenVPMSException for any error
     */
    private boolean hasOverlappingAppointments(long uid, Date startTime,
                                               Date endTime,
                                               IMObjectReference schedule) {
        NamedQuery query = new NamedQuery("act.customerAppointment-overlap");
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        query.setParameter("scheduleId", schedule.getLinkId());
        query.setParameter("actId", uid);
        List<ObjectSet> overlaps = service.getObjects(query).getResults();
        return !overlaps.isEmpty();
/*
        // Commented out as under the 1.0-rc1 schema, mysql doesn't select the
        // fastest index. Re-implemented as a named query in order to avoid
        // specifying participation.schedule in the where clause which
        // causes mysql to perform a slow index-merge

        ShortNameConstraint shortName
                = new ShortNameConstraint("act", APPOINTMENT, true);
        ArchetypeQuery query = new ArchetypeQuery(shortName);
        query.setFirstResult(0);
        query.setMaxResults(1);

        // Create the query:
        //   act.uid != uid
        //   && act.schedule = schedule
        //   && ((act.startTime < startTime && act.endTime > startTime)
        //   || (act.startTime < endTime && act.endTime > endTime)
        //   || (act.startTime >= startTime && act.endTime <= endTime))
        query.add(new NodeConstraint("uid", RelationalOp.NE, uid));
        CollectionNodeConstraint participations = new CollectionNodeConstraint(
                "schedule", "participation.schedule", false, true)
                .add(new ObjectRefNodeConstraint("entity", schedule))
                .add(new ObjectRefNodeConstraint("act", new ArchetypeId(
                        APPOINTMENT)));
        // re-specify the act short name. to force utilisation of the
        // (faster) participation index. Ideally would only need to specify
        // the act short name on participations, but this isn't supported
        // by ArchetypeQuery.
        query.add(participations);
        OrConstraint or = new OrConstraint();
        IConstraint overlapStart = createOverlapConstraint(startTime);
        IConstraint overlapEnd = createOverlapConstraint(endTime);
        AndConstraint overlapIn = new AndConstraint();
        overlapIn.add(new NodeConstraint("startTime", RelationalOp.GTE,
                                         startTime));
        overlapIn.add(new NodeConstraint("endTime", RelationalOp.LTE, endTime));

        or.add(overlapStart);
        or.add(overlapEnd);
        or.add(overlapIn);
        query.add(or);

        query.add(new NodeSelectConstraint("act.uid"));
        List<ObjectSet> overlaps = service.getObjects(query).getResults();
        return !overlaps.isEmpty();
*/
    }

    /**
     * Helper to create a constraint of the form:
     * <code>act.startTime < time && act.endTime > time</code>
     *
     * @param time the time
     * @return a new constraint
     */
    /*   private IConstraint createOverlapConstraint(Date time) {
            AndConstraint and = new AndConstraint();
            and.add(new NodeConstraint("startTime", RelationalOp.LT, time));
            and.add(new NodeConstraint("endTime", RelationalOp.GT, time));
            return and;
        }
    */

    /**
     * Returns the schedule slot size in minutes.
     *
     * @param schedule the schedule
     * @return the schedule slot size in minutes
     * @throws OpenVPMSException for any error
     */
    private int getSlotSize(EntityBean schedule) {
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
     * @return the no. of slots, or <code>0</code> if unknown
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
        if (WorkflowStatus.PENDING.equals(status)
                || WorkflowStatus.IN_PROGRESS.equals(status)
                || WorkflowStatus.COMPLETED.equals(status)
                || WorkflowStatus.CANCELLED.equals(status)) {
            if (!status.equals(linked.getStatus())) {
                linked.setStatus(status);
                if (WorkflowStatus.COMPLETED.equals(status)) {
                    linked.setActivityEndTime(act.getActivityEndTime());
                }
                service.save(linked);
            }
        }
    }

}
