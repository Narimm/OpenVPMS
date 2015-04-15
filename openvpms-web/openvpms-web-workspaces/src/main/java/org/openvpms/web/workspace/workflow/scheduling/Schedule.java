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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.scheduling;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.util.PropertySet;

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
    private List<PropertySet> events = new ArrayList<PropertySet>();

    /**
     * The comparator to detect intersecting events.
     */
    private final Comparator<PropertySet> intersectComparator;


    /**
     * Constructs a {@link Schedule}.
     *
     * @param schedule the event schedule
     */
    public Schedule(Entity schedule) {
        this(schedule, -1, -1, 0);
    }

    /**
     * Constructs an {@link Schedule}.
     *
     * @param schedule  the event schedule
     * @param startMins the schedule start time, as minutes since midnight
     * @param endMins   the schedule end time, as minutes since midnight
     * @param slotSize  the schedule slot size, in minutes
     */
    public Schedule(Entity schedule, int startMins, int endMins, int slotSize) {
        this.schedule = schedule;
        this.startMins = startMins;
        this.endMins = endMins;
        this.slotSize = slotSize;
        intersectComparator = new IntersectComparator(slotSize);
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
    public void addEvent(PropertySet set) {
        events.add(set);
    }

    /**
     * Returns the events.
     *
     * @return the events
     */
    public List<PropertySet> getEvents() {
        return events;
    }

    /**
     * Returns the event given its reference.
     *
     * @param event the event reference
     * @return the event, or {@code null} if it is not found
     */
    public PropertySet getEvent(IMObjectReference event) {
        int index = indexOf(event);
        return (index != -1) ? events.get(index) : null;
    }


    /**
     * Returns the index of an event, given its reference.
     *
     * @param event the event reference
     * @return the index, or {@code -1} if the event is not found
     */
    public int indexOf(IMObjectReference event) {
        for (int i = 0; i < events.size(); ++i) {
            PropertySet set = events.get(i);
            if (ObjectUtils.equals(event, set.getReference(ScheduleEvent.ACT_REFERENCE))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Determines if the schedule has an event that intersects the specified
     * event.
     *
     * @param event the event
     * @return {@code true} if the schedule has an intersecting event
     */
    public boolean hasIntersectingEvent(PropertySet event) {
        return Collections.binarySearch(events, event, intersectComparator) >= 0;
    }

    /**
     * Returns the event starting at the specified time.
     *
     * @param time     the time
     * @param slotSize the slot size
     * @return the corresponding event, or {@code null} if none is found
     */
    public PropertySet getEvent(Date time, int slotSize) {
        PropertySet set = new ObjectSet();
        set.set(ScheduleEvent.ACT_START_TIME, time);
        set.set(ScheduleEvent.ACT_END_TIME, time);
        int index = Collections.binarySearch(events, set, new StartTimeComparator(slotSize));
        return (index < 0) ? null : events.get(index);
    }

    /**
     * Returns the event intersecting the specified time.
     *
     * @param time the time
     * @return the corresponding event, or {@code null} if none is found
     */
    public PropertySet getIntersectingEvent(Date time) {
        PropertySet set = new ObjectSet();
        set.set(ScheduleEvent.ACT_START_TIME, time);
        set.set(ScheduleEvent.ACT_END_TIME, time);
        int index = Collections.binarySearch(events, set, intersectComparator);
        return (index < 0) ? null : events.get(index);
    }

    /**
     * Comparator used to locate events starting at particular time.
     */
    private class StartTimeComparator implements Comparator<PropertySet> {

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
         * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or
         *         greater than the second.
         */
        public int compare(PropertySet o1, PropertySet o2) {
            Date startTime1 = o1.getDate(ScheduleEvent.ACT_START_TIME);
            Date startTime2 = o2.getDate(ScheduleEvent.ACT_START_TIME);
            int result = DateRules.getDate(startTime1).compareTo(DateRules.getDate(startTime2));
            if (result == 0) {
                int start1 = SchedulingHelper.getSlotMinutes(startTime1, slotSize, false);
                int start2 = SchedulingHelper.getSlotMinutes(startTime2, slotSize, false);
                result = start1 - start2;
            }
            return result;
        }
    }

}
