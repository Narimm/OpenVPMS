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

package org.openvpms.web.workspace.admin.calendar;

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.workspace.workflow.scheduling.Schedule;

import java.util.Date;
import java.util.List;

/**
 * Represents the calendar as a grid of days and slots.
 * <p>
 * NOTE: this does not support overlapping events.
 *
 * @author Tim Anderson
 */
public class DefaultCalendarGrid implements CalendarGrid {

    /**
     * The schedule.
     */
    private final Schedule schedule;

    /**
     * The date the grid starts on.
     */
    private final Date startDate;

    /**
     * The number of days in the grid.
     */
    private final int days;

    /**
     * The slot size, in minutes.
     */
    private final int slotSize = 15;

    /**
     * Constructs a {@link DefaultCalendarGrid}.
     *
     * @param date   the starting date
     * @param days   the number of days to display
     * @param events the events
     * @param rules  the appointment rules
     */
    public DefaultCalendarGrid(Entity calendar, Date date, int days, List<PropertySet> events, AppointmentRules rules) {
        this.startDate = date;
        this.days = days;
        schedule = new Schedule(calendar, null, 0, 24 * 60, slotSize, rules);
        for (PropertySet event : events) {
            Reference eventRef = event.getReference(ScheduleEvent.ACT_REFERENCE);
            if (schedule.indexOf(eventRef) == -1) {
                // multi-day events are duplicated on subsequent days, so skip it if it has already been added
                schedule.addEvent(event);
            }
        }
    }

    /**
     * Returns the no. of minutes from midnight that the grid starts at.
     *
     * @return the minutes from midnight that the grid starts at
     */
    @Override
    public int getStartMins() {
        return 0;
    }

    /**
     * Returns the no. of minutes from midnight that the grid ends at.
     *
     * @return the minutes from midnight that the grid ends at
     */
    @Override
    public int getEndMins() {
        return 24 * 60;
    }

    /**
     * Returns the first slot that has a start time and end time intersecting
     * the specified minutes.
     *
     * @param minutes the minutes
     * @return the first slot that minutes intersects, or {@code -1} if no slots intersect
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
     * @return the last slot that minutes intersects, or {@code -1} if no slots intersect
     */
    public int getLastSlot(int minutes) {
        return getFirstSlot(minutes);
    }

    /**
     * Returns the no. of slots in the grid.
     *
     * @return the no. of slots
     */
    @Override
    public int getSlots() {
        return 24 * 60 / slotSize;
    }

    /**
     * Returns the starting date.
     *
     * @return the starting date
     */
    @Override
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Returns the number of days to display.
     *
     * @return the number of days
     */
    @Override
    public int getDays() {
        return days;
    }

    /**
     * Returns the time that the specified slot starts at.
     *
     * @param slot the slot
     * @return the start time of the specified slot
     */
    @Override
    public Date getStartTime(int slot) {
        return DateRules.getDate(startDate, slot * slotSize, DateUnits.MINUTES);
    }

    /**
     * Returns the date/time of the specified slot.
     *
     * @param offset the day offset form {@link #getStartDate()}
     * @param slot   the slot
     * @return the date/time of the slot
     */
    @Override
    public Date getDatetime(int offset, int slot) {
        Date date = DateRules.getDate(startDate, offset, DateUnits.DAYS);
        return DateRules.getDate(date, slot * slotSize, DateUnits.MINUTES);
    }

    /**
     * Returns the event at or intersecting the specified slot.
     *
     * @param offset the day offset form {@link #getStartDate()}
     * @param slot   the slot
     * @return the corresponding event, or {@code null} if none is found
     */
    @Override
    public PropertySet getEvent(int offset, int slot) {
        Date datetime = getDatetime(offset, slot);
        PropertySet result = schedule.getEvent(datetime, slotSize, true);
        if (result == null) {
            result = schedule.getIntersectingEvent(datetime, true);
        }
        return result;
    }

    /**
     * Returns the hour that a slot falls in.
     *
     * @param slot the slot
     * @return the hour
     */
    @Override
    public int getHour(int slot) {
        return (slot * slotSize) / 60;
    }

    /**
     * Returns the slot size.
     *
     * @return the slot size, in minutes
     */
    @Override
    public int getSlotSize() {
        return slotSize;
    }

}
