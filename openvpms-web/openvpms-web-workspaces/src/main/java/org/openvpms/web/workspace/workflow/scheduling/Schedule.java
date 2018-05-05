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

package org.openvpms.web.workspace.workflow.scheduling;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.util.PropertySet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


/**
 * Event schedule.
 * <p/>
 * This supports non-blocking and block events, with the following restrictions:
 * <ul>
 * <li>events must be ordered on ascending start time</li>
 * <li>non-blocking events may overlap blocking events</li>
 * <li>blocking events may not overlap other blocking events</li>
 * <li>non-blocking events may not overlap other non-blocking events</li>
 * </ul>
 */
public class Schedule {

    /**
     * The schedule.
     */
    private final Entity schedule;

    /**
     * The cage type.
     */
    private final Entity cageType;

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
     * Determines if the even or odd rendering style should be used.
     */
    private boolean renderEven = true;

    /**
     * All events.
     */
    private final List<PropertySet> events = new ArrayList<>();

    /**
     * The non-blocking events.
     */
    private final List<PropertySet> nonBlockingEvents = new ArrayList<>();

    /**
     * The blocking events.
     */
    private final List<PropertySet> blockingEvents;

    /**
     * The appointment rules.
     */
    private final AppointmentRules rules;

    /**
     * The comparator to detect intersecting events.
     */
    private final Comparator<PropertySet> intersectComparator;

    /**
     * Constructs a {@link Schedule}.
     *
     * @param schedule the event schedule
     * @param cageType the cage type. May be {@code null}
     * @param rules    the appointment rules
     */
    public Schedule(Entity schedule, Entity cageType, AppointmentRules rules) {
        this(schedule, cageType, -1, -1, 0, rules);
    }

    /**
     * Constructs an {@link Schedule}.
     *
     * @param schedule  the event schedule
     * @param cageType  the cage type. May be {@code null}
     * @param startMins the schedule start time, as minutes since midnight
     * @param endMins   the schedule end time, as minutes since midnight
     * @param slotSize  the schedule slot size, in minutes
     * @param rules     the appointment rules
     */
    public Schedule(Entity schedule, Entity cageType, int startMins, int endMins, int slotSize,
                    AppointmentRules rules) {
        this.schedule = schedule;
        this.cageType = cageType;
        this.startMins = startMins;
        this.endMins = endMins;
        this.slotSize = slotSize;
        this.rules = rules;
        this.blockingEvents = new ArrayList<>();
        intersectComparator = new IntersectComparator(slotSize, rules);
    }

    /**
     * Constructs an {@link Schedule}.
     *
     * @param schedule       the event schedule
     * @param cageType       the cage type. May be {@code null}
     * @param startMins      the schedule start time, as minutes since midnight
     * @param endMins        the schedule end time, as minutes since midnight
     * @param slotSize       the schedule slot size, in minutes
     * @param blockingEvents the blocking events
     * @param rules          the appointment rules
     */
    public Schedule(Entity schedule, Entity cageType, int startMins, int endMins, int slotSize,
                    List<PropertySet> blockingEvents, AppointmentRules rules) {
        this.schedule = schedule;
        this.cageType = cageType;
        this.startMins = startMins;
        this.endMins = endMins;
        this.slotSize = slotSize;
        this.rules = rules;
        this.blockingEvents = blockingEvents;
        intersectComparator = new IntersectComparator(slotSize, rules);
    }

    /**
     * Creates a schedule from an existing schedule.
     * <p/>
     * Only the blocking events are copied.
     *
     * @param source the source schedule
     * @param rules  the appointment rules
     */
    public Schedule(Schedule source, AppointmentRules rules) {
        this(source.getSchedule(), source.getCageType(), source.getStartMins(), source.getEndMins(),
             source.getSlotSize(), source.blockingEvents, rules);
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
     * Returns the cage type.
     *
     * @return the cage type. May be {@code null}
     */
    public Entity getCageType() {
        return cageType;
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
     * @param event the event
     */
    public void addEvent(PropertySet event) {
        if (isBlockingEvent(event)) {
            blockingEvents.add(event);
        } else {
            nonBlockingEvents.add(event);
        }
        events.add(event);
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
        return indexOf(event, events);
    }

    /**
     * Returns the first event starting after that specified.
     * <p/>
     * This excludes blocking events.
     *
     * @param event     the event
     * @param startTime the start time to compare against
     * @return the first event, or {@code null} if none is found
     */
    public PropertySet getEventAfter(PropertySet event, Date startTime) {
        PropertySet result = null;
        int index = indexOf(event.getReference(ScheduleEvent.ACT_REFERENCE), events);
        if (index != -1) {
            while (index < events.size()) {
                PropertySet next = events.get(index);
                if (!isBlockingEvent(next)
                    && DateRules.compareTo(startTime, next.getDate(ScheduleEvent.ACT_START_TIME)) < 0) {
                    result = next;
                    break;
                } else {
                    index++;
                }
            }
        }
        return result;
    }

    /**
     * Determines if the schedule has an event that intersects the specified event.
     * <p/>
     * This excludes blocking events.
     *
     * @param event the event
     * @return {@code true} if the schedule has an intersecting event
     */
    public boolean hasIntersectingEvent(PropertySet event) {
        return Collections.binarySearch(nonBlockingEvents, event, intersectComparator) >= 0;
    }

    /**
     * Returns the event starting at the specified time.
     * <p/>
     * This returns non-blocking events in preference to blocking ones.
     *
     * @param time     the time
     * @param slotSize the slot size
     * @return the corresponding event, or {@code null} if none is found
     */
    public PropertySet getEvent(Date time, int slotSize) {
        return getEvent(time, slotSize, true);
    }

    /**
     * Returns the event starting at the specified time.
     *
     * @param time                  the time
     * @param slotSize              the slot size
     * @param includeBlockingEvents if {@code true}, look for blocking events if there are no non-blocking events
     * @return the corresponding event, or {@code null} if none is found
     */
    public PropertySet getEvent(Date time, int slotSize, boolean includeBlockingEvents) {
        StartTimeComparator comparator = new StartTimeComparator(slotSize);
        return getEvent(time, comparator, includeBlockingEvents);
    }

    /**
     * Returns the event intersecting the specified time.
     *
     * @param time the time
     * @return the corresponding event, or {@code null} if none is found
     */
    public PropertySet getIntersectingEvent(Date time) {
        return getIntersectingEvent(time, true);
    }

    /**
     * Returns the event intersecting the specified time.
     *
     * @param time                  the time
     * @param includeBlockingEvents if {@code true}, look for blocking events if there are no non-blocking events
     * @return the corresponding event, or {@code null} if none is found
     */
    public PropertySet getIntersectingEvent(Date time, boolean includeBlockingEvents) {
        return getEvent(time, intersectComparator, includeBlockingEvents);
    }

    /**
     * Determines if the even or odd rendering style should be used.
     *
     * @param renderEven if {@code true} use the even rendering style, otherwise use the odd style
     */
    public void setRenderEven(boolean renderEven) {
        this.renderEven = renderEven;
    }

    /**
     * Determines if the even or odd rendering style should be used.
     *
     * @return {@code true} to use the even rendering style, {@code false} to use the odd style
     */
    public boolean getRenderEven() {
        return renderEven;
    }

    /**
     * Helper to determine if an event is a blocking event.
     *
     * @param event the event
     * @return {@code true} if the event is a blocking event
     */
    public static boolean isBlockingEvent(PropertySet event) {
        return TypeHelper.isA(event.getReference(ScheduleEvent.ACT_REFERENCE), ScheduleArchetypes.CALENDAR_BLOCK);
    }

    /**
     * Returns the index of an event, given its reference.
     *
     * @param event the event reference
     * @param list  the events to search
     * @return the index, or {@code -1} if the event is not found
     */
    protected int indexOf(IMObjectReference event, List<PropertySet> list) {
        for (int i = 0; i < list.size(); ++i) {
            PropertySet set = list.get(i);
            if (ObjectUtils.equals(event, set.getReference(ScheduleEvent.ACT_REFERENCE))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns an event.
     * <p/>
     * This returns non-blocking events in preference to blocking ones.
     *
     * @param time                  the time to search for
     * @param comparator            the comparator used to locate matches
     * @param includeBlockingEvents if {@code true}, return any blocking event if there are no non-blocking events
     * @return the event
     */
    protected PropertySet getEvent(Date time, Comparator<PropertySet> comparator, boolean includeBlockingEvents) {
        PropertySet result = null;
        PropertySet set = new ObjectSet();
        set.set(ScheduleEvent.ACT_START_TIME, time);
        set.set(ScheduleEvent.ACT_END_TIME, time);
        int index = Collections.binarySearch(nonBlockingEvents, set, comparator);
        if (index >= 0) {
            result = nonBlockingEvents.get(index);
        } else if (includeBlockingEvents) {
            index = Collections.binarySearch(blockingEvents, set, comparator);
            if (index >= 0) {
                result = blockingEvents.get(index);
            }
        }
        return result;
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
         * greater than the second.
         */
        public int compare(PropertySet o1, PropertySet o2) {
            Date startTime1 = o1.getDate(ScheduleEvent.ACT_START_TIME);
            Date startTime2 = o2.getDate(ScheduleEvent.ACT_START_TIME);
            int result = DateRules.getDate(startTime1).compareTo(DateRules.getDate(startTime2));
            if (result == 0) {
                int start1 = rules.getSlotMinutes(startTime1, slotSize, false);
                int start2 = rules.getSlotMinutes(startTime2, slotSize, false);
                result = start1 - start2;
            }
            return result;
        }
    }

}
