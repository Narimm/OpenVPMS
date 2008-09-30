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
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


/**
 * Event schedule.
 */
public class Schedule {

    /**
     * The schedule.
     */
    private final Entity schedule;

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
     * The events.
     */
    private List<ObjectSet> events = new ArrayList<ObjectSet>();


    /**
     * Creates a new <tt>Schedule</tt>.
     *
     * @param schedule the event schedule
     */
    public Schedule(Entity schedule) {
        this(schedule, -1, -1, 0);
    }

    /**
     * Creates a new <tt>Schedule</tt>.
     *
     * @param schedule  the event schedule
     * @param startMins the schedule start time, as minutes since midnight
     * @param endMins   the schedule end time, as minutes since midnight
     */
    public Schedule(Entity schedule, int startMins, int endMins, int slotSize) {
        this.schedule = schedule;
        this.startMins = startMins;
        this.endMins = endMins;
        this.slotSize = slotSize;
    }

    /**
     * Creates a schedule from an existing schedule.
     * <p/>
     * The events are not copied.
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
    public Entity getSchedule() {
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
     * Adds an event.
     *
     * @param set an object set representing the event
     */
    public void addEvent(ObjectSet set) {
        events.add(set);
    }

    /**
     * Returns the events.
     *
     * @return the events
     */
    public List<ObjectSet> getEvents() {
        return events;
    }

    /**
     * Determines if the schedule has an event that intersects the specified
     * event.
     *
     * @param event the event
     * @return <tt>true</tt> if the schedule has an intersecting event
     */
    public boolean hasEvent(ObjectSet event) {
        return Collections.binarySearch(events, event,
                                        IntersectComparator.INSTANCE) >= 0;
    }

    /**
     * Returns the event starting at the specified time.
     *
     * @param time     the time
     * @param slotSize the slot size
     * @return the corresponding event, or <tt>null</tt> if none is found
     */
    public ObjectSet getEvent(Date time, int slotSize) {
        ObjectSet set = new ObjectSet();
        set.set(ScheduleEvent.ACT_START_TIME, time);
        set.set(ScheduleEvent.ACT_END_TIME, time);
        int index = Collections.binarySearch(events, set,
                                             new StartTimeComparator(slotSize));
        return (index < 0) ? null : events.get(index);
    }

    /**
     * Returns the event intersecting the specified time.
     *
     * @param time the time
     * @return the corresponding event, or <tt>null</tt> if none is found
     */
    public ObjectSet getIntersectingEvent(Date time) {
        ObjectSet set = new ObjectSet();
        set.set(ScheduleEvent.ACT_START_TIME, time);
        set.set(ScheduleEvent.ACT_END_TIME, time);
        int index = Collections.binarySearch(events, set,
                                             IntersectComparator.INSTANCE);
        return (index < 0) ? null : events.get(index);
    }

    /**
     * Comparator used to locate events starting at particular time.
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
                    o1.getDate(ScheduleEvent.ACT_START_TIME), slotSize, false);
            int start2 = SchedulingHelper.getSlotMinutes(
                    o2.getDate(ScheduleEvent.ACT_START_TIME), slotSize, false);
            return start1 - start2;
        }
    }

    /**
     * Comparator used to locate intersecting events.
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
            Date start1 = o1.getDate(ScheduleEvent.ACT_START_TIME);
            Date end1 = o1.getDate(ScheduleEvent.ACT_END_TIME);
            Date start2 = o2.getDate(ScheduleEvent.ACT_START_TIME);
            Date end2 = o2.getDate(ScheduleEvent.ACT_END_TIME);
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
