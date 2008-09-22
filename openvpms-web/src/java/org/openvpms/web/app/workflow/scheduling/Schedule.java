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

package org.openvpms.web.app.workflow.scheduling;

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.Appointment;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


/**
 * Schedule column.
 */
class Schedule {

    private final Party schedule;

    private int startMins = -1;

    private int endMins = -1;

    private int slotSize;

    private List<ObjectSet> appointments = new ArrayList<ObjectSet>();

    public Schedule(Party schedule, AppointmentRules rules) {
        this.schedule = schedule;
        EntityBean bean = new EntityBean(schedule);
        Date start = bean.getDate("startTime");
        if (start != null) {
            startMins = SchedulingHelper.getMinutes(start);
        } else {
            startMins = MultiScheduleGrid.DEFAULT_START;
        }

        Date end = bean.getDate("endTime");
        if (end != null) {
            endMins = SchedulingHelper.getMinutes(end);
        } else {
            endMins = MultiScheduleGrid.DEFAULT_END;
        }
        slotSize = rules.getSlotSize(schedule);
        if (slotSize <= 0) {
            slotSize = MultiScheduleGrid.DEFAULT_SLOT_SIZE;
        }
    }

    public Schedule(Schedule source) {
        this.schedule = source.schedule;
        startMins = source.startMins;
        endMins = source.endMins;
        slotSize = source.slotSize;
    }

    public Party getSchedule() {
        return schedule;
    }

    public String getName() {
        return schedule.getName();
    }

    public int getSlotSize() {
        return slotSize;
    }

    public int getStartMins() {
        return startMins;
    }

    public int getEndMins() {
        return endMins;
    }

    public void addAppointment(ObjectSet set) {
        appointments.add(set);
    }

    public List<ObjectSet> getAppointments() {
        return appointments;
    }

    public boolean hasAppointment(ObjectSet appointment) {
        return Collections.binarySearch(appointments, appointment,
                                        IntersectComparator.INSTANCE) >= 0;
    }

    public ObjectSet getAppointment(Date time, int slotSize) {
        ObjectSet set = new ObjectSet();
        set.set(Appointment.ACT_START_TIME, time);
        set.set(Appointment.ACT_END_TIME, time);
        int index = Collections.binarySearch(appointments, set,
                                             new StartTimeComparator(slotSize));
        return (index < 0) ? null : appointments.get(index);
    }

    public ObjectSet getIntersectingAppointment(Date time) {
        ObjectSet set = new ObjectSet();
        set.set(Appointment.ACT_START_TIME, time);
        set.set(Appointment.ACT_END_TIME, time);
        int index = Collections.binarySearch(appointments, set,
                                             IntersectComparator.INSTANCE);
        return (index < 0) ? null : appointments.get(index);
    }

    /**
     * Comparator used to locate appointments starting at particular time.
     */
    private class StartTimeComparator implements Comparator<ObjectSet> {

        private final int slotSize;

        public StartTimeComparator(int slotSize) {
            this.slotSize = slotSize;
        }

        /**
         * Compares its two arguments for order.  Returns a negative integer,
         * zero, or a positive integer as the first argument is less than, equal
         * to, or greater than the second.<p>
         *
         * @param o1 the first object to be compared.
         * @param o2 the second object to be compared.
         * @return a negative integer, zero, or a positive integer as the
         *         first argument is less than, equal to, or greater than the
         *         second.
         * @throws ClassCastException if the arguments' types prevent them from
         *                            being compared by this Comparator.
         */
        public int compare(ObjectSet o1, ObjectSet o2) {
            int start1 = SchedulingHelper.getSlotMinutes(
                    o1.getDate(Appointment.ACT_START_TIME), slotSize, false);
            int start2 = SchedulingHelper.getSlotMinutes(
                    o2.getDate(Appointment.ACT_START_TIME), slotSize, false);
            return start1 - start2;
        }
    }

    /**
     * Comparator used to locate intersecting appointments.
     */
    private static class IntersectComparator implements Comparator<ObjectSet> {

        public static IntersectComparator INSTANCE = new IntersectComparator();

        /**
         * Compares its two arguments for order.  Returns a negative integer,
         * zero, or a positive integer as the first argument is less than, equal
         * to, or greater than the second.<p>
         *
         * @param o1 the first object to be compared.
         * @param o2 the second object to be compared.
         * @return a negative integer, zero, or a positive integer as the
         *         first argument is less than, equal to, or greater than the
         *         second.
         * @throws ClassCastException if the arguments' types prevent them from
         *                            being compared by this Comparator.
         */
        public int compare(ObjectSet o1, ObjectSet o2) {
            Date start1 = o1.getDate(Appointment.ACT_START_TIME);
            Date end1 = o1.getDate(Appointment.ACT_END_TIME);
            Date start2 = o2.getDate(Appointment.ACT_START_TIME);
            Date end2 = o2.getDate(Appointment.ACT_END_TIME);
            if (DateRules.compareTo(start1, start2) < 0
                    && DateRules.compareTo(end1, start2) <= 0) {
                return -1;
            }
            if (DateRules.compareTo(start1, end2) >= 0
                    && DateRules.compareTo(end1, end2) > 0) {
                return 1;
            }
            return 0;
        }
    }
}
