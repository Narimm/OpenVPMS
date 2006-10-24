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
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.AndConstraint;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.OrConstraint;
import org.openvpms.component.system.common.query.RelationalOp;

import java.util.Date;
import java.util.List;


/**
 * Appoinment rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AppointmentRules {

    /**
     * Returns the schedule slot size in minutes.
     *
     * @param schedule the schedule
     * @return the schedule slot size in minutes
     * @throws OpenVPMSException for any error
     */
    public static int getSlotSize(Party schedule) {
        EntityBean bean = new EntityBean(schedule);
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
    public static Date calculateEndTime(Date startTime, Party schedule,
                                        Entity appointmentType) {
        EntityBean schedBean = new EntityBean(schedule);
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
    public static boolean hasOverlappingAppointments(Act appointment) {
        long uid = appointment.getUid();
        ActBean bean = new ActBean(appointment);
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
     * Determines if there are acts that overlap with an appointment.
     *
     * @param uid       the appointment id
     * @param startTime the appointment start time
     * @param endTime   the appointment end time
     * @param schedule  the schedule
     * @return a list of acts that overlap with the appointment
     * @throws OpenVPMSException for any error
     */
    private static boolean hasOverlappingAppointments(long uid, Date startTime,
                                                      Date endTime,
                                                      IMObjectReference schedule) {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        ArchetypeQuery query = new ArchetypeQuery(
                null, "act", "customerAppointment", false, true);
        query.setFirstRow(0);
        query.setNumOfRows(1);

        // Create the query:
        //   act.uid != uid
        //   && act.schedule = schedule
        //   && ((act.startTime < startTime && act.endTime > startTime)
        //   || (act.startTime < endTime && act.endTime > endTime)
        //   || (act.startTime >= startTime && act.endTime <= endTime))
        query.add(new NodeConstraint("uid", RelationalOp.NE, uid));
        CollectionNodeConstraint participations = new CollectionNodeConstraint(
                "schedule", "participation.schedule", false, true)
                .add(new ObjectRefNodeConstraint("entity", schedule));
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

        List<IMObject> overlaps = service.get(query).getRows();
        return !overlaps.isEmpty();
    }

    /**
     * Helper to create a constraint of the form:
     * <code>act.startTime < time && act.endTime > time</code>
     *
     * @param time the time
     * @return a new constraint
     */
    private static IConstraint createOverlapConstraint(Date time) {
        AndConstraint and = new AndConstraint();
        and.add(new NodeConstraint("startTime", RelationalOp.LT, time));
        and.add(new NodeConstraint("endTime", RelationalOp.GT, time));
        return and;
    }

    /**
     * Returns the schedule slot size in minutes.
     *
     * @param schedule the schedule
     * @return the schedule slot size in minutes
     * @throws OpenVPMSException for any error
     */
    private static int getSlotSize(EntityBean schedule) {
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
    private static int getSlots(EntityBean schedule, Entity appointmentType) {
        int noSlots = 0;
        EntityRelationship relationship
                = schedule.getRelationship(appointmentType);
        if (relationship != null) {
            IMObjectBean bean = new IMObjectBean(relationship);
            noSlots = bean.getInt("noSlots");
        }
        return noSlots;
    }

}
