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
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


/**
 * Appointment schedule.
 */
class Schedule {

    /**
     * The <em>party.organisationSchedule</em>.
     */
    private final Party schedule;

    /**
     * The schedule start time, as minutes since midnight.
     */
    private int startMins;

    /**
     * The schedule end time, as minutes since midnight.
     */
    private int endMins;

    /**
     * The schedule slot size, in minutes.
     */
    private int slotSize;

    /**
     * The appointments.
     */
    private List<ObjectSet> appointments = new ArrayList<ObjectSet>();


    /**
     * Creates a new <tt>Schedule</tt>.
     *
     * @param schedule  the <em>party.organisationSchedule</em>
     * @param startMins the schedule start time, as minutes since midnight
     * @param endMins   the schedule end time, as minutes since midnight
     */
    public Schedule(Party schedule, int startMins, int endMins, int slotSize) {
        this.schedule = schedule;
        this.startMins = startMins;
        this.endMins = endMins;
        this.slotSize = slotSize;
    }

    /**
     * Creates a schedule from an existing schedule.
     * <p/>
     * The appointments are not copied.
     *
     * @param source the source schedule
     */
    public Schedule(Schedule source) {
        this(source.getSchedule(), source.getStartMins(), source.getEndMins(),
             source.getSlotSize());
    }

    /**
     * Returns the schedule.
     *
     * @return the schedule
     */
    public Party getSchedule() {
        return schedule;
    }

    /**
     * Returns the schedule name.
     *
     * @return the schedule name
     */
    public String getName() {
        return schedule.getName();
    }

    /**
     * Returns the no. of minutes from midnight that the schedule starts at.
     *
     * @return the minutes from midnight that the schedule starts at
     */
    public int getStartMins() {
        return startMins;
    }

    /**
     * Returns the no. of minutes from midnight that the schedule ends at.
     *
     * @return the minutes from midnight that the schedule ends at
     */
    public int getEndMins() {
        return endMins;
    }

    /**
     * Returns the slot size.
     *
     * @return the slot size, in minutes
     */
    public int getSlotSize() {
        return slotSize;
    }

    /**
     * Adds an appointment.
     *
     * @param set an object set representing the appointment
     */
    public void addAppointment(ObjectSet set) {
        appointments.add(set);
    }

    /**
     * Returns the appointments.
     *
     * @return the appointments
     */
    public List<ObjectSet> getAppointments() {
        return appointments;
    }

    /**
     * Determines if the schedule has an appointment that intersects the
     * specified appointment.
     *
     * @param appointment the appointment
     * @return <tt>true</tt> if the schedule has an intersecting appointment
     */
    public boolean hasAppointment(ObjectSet appointment) {
        return Collections.binarySearch(appointments, appointment,
                                        IntersectComparator.INSTANCE) >= 0;
    }

    /**
     * Returns the appointment starting at the specified time.
     *
     * @param time     the time
     * @param slotSize the slot size
     * @return the corresponding appintment, or <tt>null</tt> if none is found
     */
    public ObjectSet getAppointment(Date time, int slotSize) {
        ObjectSet set = new ObjectSet();
        set.set(Appointment.ACT_START_TIME, time);
        set.set(Appointment.ACT_END_TIME, time);
        int index = Collections.binarySearch(appointments, set,
                                             new StartTimeComparator(slotSize));
        return (index < 0) ? null : appointments.get(index);
    }

    /**
     * Returns the appointment intersecting the specified time.
     *
     * @param time the time
     * @return the corresponding appintment, or <tt>null</tt> if none is found
     */
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
