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

package org.openvpms.web.workspace.workflow.appointment;

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.archetype.rules.workflow.ScheduleEvents;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.workspace.workflow.scheduling.Schedule;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * An {@link AppointmentGrid} for multiple schedules.
 * <p>
 * This handles overlapping and double booked appointments by creating new {@link Schedule} instances to contain them.
 *
 * @author Tim Anderson
 */
class MultiScheduleGrid extends AbstractAppointmentGrid {

    /**
     * Constructs a {@link MultiScheduleGrid}.
     *
     * @param scheduleView the schedule view
     * @param date         the appointment date
     * @param events       the events
     * @param rules        the appointment rules
     */
    public MultiScheduleGrid(Entity scheduleView, Date date, Map<Entity, ScheduleEvents> events,
                             AppointmentRules rules) {
        super(scheduleView, date, -1, -1, rules);
        setEvents(events);
    }

    /**
     * Returns the event for the specified schedule and slot.
     *
     * @param schedule the schedule
     * @param slot     the slot
     * @return the corresponding event, or {@code null} if none is found
     */
    public PropertySet getEvent(Schedule schedule, int slot) {
        Date time = getStartTime(schedule, slot);
        PropertySet result = schedule.getEvent(time, getSlotSize());
        if (result == null) {
            result = schedule.getIntersectingEvent(time);
        }
        return result;
    }

    /**
     * Returns the first slot that has a start time and end time intersecting
     * the specified minutes.
     *
     * @param minutes the minutes
     * @return the first slot that minutes intersects, or {@code -1} if no
     * slots intersect
     */
    public int getFirstSlot(int minutes) {
        if (minutes < getStartMins() || minutes > getEndMins()) {
            return -1;
        }
        return (minutes - getStartMins()) / getSlotSize();
    }

    /**
     * Returns the last slot that has a start time and end time intersecting
     * the specified minutes.
     *
     * @param minutes the minutes
     * @return the last slot that minutes intersects, or {@code -1} if no
     * slots intersect
     */
    public int getLastSlot(int minutes) {
        return getFirstSlot(minutes);
    }

    /**
     * Adds an event.
     * <p>
     * If event is an appointment, and the corresponding Schedule already has an appointment that intersects it,
     * new Schedule will be created with the same start and end times, and the appointment added to that.
     *
     * @param schedule the schedule to add the appointment to
     * @param event    the event
     */
    @Override
    protected void addEvent(Entity schedule, PropertySet event) {
        super.addEvent(schedule, event);
        Date startTime = event.getDate(ScheduleEvent.ACT_START_TIME);
        Date endTime = event.getDate(ScheduleEvent.ACT_END_TIME);

        // adjust the grid start and end times, if required
        Date startDate = DateRules.getDate(startTime);
        Date endDate = DateRules.getDate(endTime);
        int slotStart = startDate.compareTo(getStartDate()) < 0 ? getStartMins() : getSlotMinutes(startTime, false);
        int slotEnd = endDate.compareTo(getStartDate()) > 0 ? getEndMins() : getSlotMinutes(endTime, true);
        if (getStartMins() > slotStart) {
            setStartMins(slotStart);
        }
        if (getEndMins() < slotEnd) {
            setEndMins(slotEnd);
        }
    }

    /**
     * Sets the events.
     *
     * @param events the events, keyed on schedule
     */
    private void setEvents(Map<Entity, ScheduleEvents> events) {
        int startMins = -1;
        int endMins = -1;
        int slotSize = -1;
        setSlotSize(-1);

        List<Schedule> schedules = new ArrayList<>();

        // Determine the startMins, endMins and slotSize. The:
        // . startMins is the minimum startMins of all schedules
        // . endMins is the minimum endMins of all schedules
        // . slotSize is the minimum slotSize of all schedules
        for (Entity schedule : events.keySet()) {
            Schedule column = createSchedule(schedule);
            schedules.add(column);
            int start = column.getStartMins();
            if (startMins == -1 || start < startMins) {
                startMins = start;
            }
            int end = column.getEndMins();
            if (end > endMins) {
                endMins = end;
            }
            if (slotSize == -1 || column.getSlotSize() < slotSize) {
                slotSize = column.getSlotSize();
            }
        }
        if (startMins == -1) {
            startMins = DEFAULT_START;
        }
        if (endMins == -1) {
            endMins = DEFAULT_END;
        }
        if (slotSize == -1) {
            slotSize = DEFAULT_SLOT_SIZE;
        }
        setSchedules(schedules);
        setStartMins(startMins);
        setEndMins(endMins);
        setSlotSize(slotSize);

        // add the events
        for (Map.Entry<Entity, ScheduleEvents> entry : events.entrySet()) {
            Entity schedule = entry.getKey();
            List<PropertySet> sets = entry.getValue().getEvents();

            for (PropertySet set : sets) {
                addEvent(schedule, set);
            }
        }
    }

}
