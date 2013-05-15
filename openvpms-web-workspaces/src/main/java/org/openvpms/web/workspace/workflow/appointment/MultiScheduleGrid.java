/*
 * Version: 1.0
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
 *  Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment;

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.workspace.workflow.scheduling.Schedule;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * An {@link AppointmentGrid} for a multiple schedule.
 * <p/>
 * This handles overlapping and double booked appointments by creating new
 * {@link Schedule} instances to contain them.
 *
 * @author Tim Anderson
 */
class MultiScheduleGrid extends AbstractAppointmentGrid {

    /**
     * The schedules.
     */
    private List<Schedule> columns;


    /**
     * Constructs a {@code MultiScheduleGrid}.
     *
     * @param scheduleView the schedule view
     * @param date         the appointment date
     * @param appointments the appointments
     */
    public MultiScheduleGrid(Entity scheduleView, Date date,
                             Map<Entity, List<PropertySet>> appointments) {
        super(scheduleView, date, -1, -1);
        columns = new ArrayList<Schedule>();
        setAppointments(appointments);
    }

    /**
     * Returns the schedules.
     *
     * @return the schedules
     */
    public List<Schedule> getSchedules() {
        return columns;
    }

    /**
     * Returns the appointment for the specified schedule and slot.
     *
     * @param schedule the schedule
     * @param slot     the slot
     * @return the corresponding appointment, or {@code null} if none is found
     */
    public PropertySet getEvent(Schedule schedule, int slot) {
        Date time = getStartTime(schedule, slot);
        PropertySet result = schedule.getEvent(time, getSlotSize());
        if (result == null && slot == 0) {
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
     *         slots intersect
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
     *         slots intersect
     */
    public int getLastSlot(int minutes) {
        return getFirstSlot(minutes);
    }

    /**
     * Sets the appointments.
     *
     * @param appointments the appointments, keyed on schedule
     */
    private void setAppointments(Map<Entity, List<PropertySet>> appointments) {
        int startMins = -1;
        int endMins = -1;
        int slotSize = -1;
        setSlotSize(-1);

        // Determine the startMins, endMins and slotSize. The:
        // . startMins is the minimum startMins of all schedules
        // . endMins is the minimum endMins of all schedules
        // . slotSize is the minimum slotSize of all schedules
        for (Entity schedule : appointments.keySet()) {
            Schedule column = createSchedule((Party) schedule);
            columns.add(column);
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
        setStartMins(startMins);
        setEndMins(endMins);
        setSlotSize(slotSize);

        // add the appointments
        for (Map.Entry<Entity, List<PropertySet>> entry
            : appointments.entrySet()) {
            Party schedule = (Party) entry.getKey();
            List<PropertySet> sets = entry.getValue();

            for (PropertySet set : sets) {
                addAppointment(schedule, set);
            }
        }
    }

    /**
     * Adds an appointment.
     * <p/>
     * If the corresponding Schedule already has an appointment that intersects
     * the appointment, a new Schedule will be created with the same start and
     * end times, and the appointment added to that.
     *
     * @param schedule the schedule to add the appointment to
     * @param set      the appointment
     */
    private void addAppointment(Party schedule, PropertySet set) {
        Date startTime = set.getDate(ScheduleEvent.ACT_START_TIME);
        Date endTime = set.getDate(ScheduleEvent.ACT_END_TIME);
        int index = -1;
        boolean found = false;
        Schedule column = null;
        Schedule match = null;

        // try and find a corresponding Schedule that has no appointment that
        // intersects the supplied one
        for (int i = 0; i < columns.size(); ++i) {
            column = columns.get(i);
            if (column.getSchedule().equals(schedule)) {
                if (column.hasIntersectingEvent(set)) {
                    match = column;
                    index = i;
                } else {
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            // appointment intersects an existing one, so create a new Schedule
            column = new Schedule(match);
            columns.add(index + 1, column);
        }
        column.addEvent(set);

        // adjust the grid start and end times, if required
        Date startDate = DateRules.getDate(startTime);
        Date endDate = DateRules.getDate(endTime);
        int slotStart = startDate.compareTo(getDate()) < 0 ? getStartMins() : getSlotMinutes(startTime, false);
        int slotEnd = endDate.compareTo(getDate()) > 0 ? getEndMins() : getSlotMinutes(endTime, true);
        if (getStartMins() > slotStart) {
            setStartMins(slotStart);
        }
        if (getEndMins() < slotEnd) {
            setEndMins(slotEnd);
        }
    }

}
