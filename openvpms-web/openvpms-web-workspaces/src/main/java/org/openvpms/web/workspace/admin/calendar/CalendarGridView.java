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

import org.openvpms.component.system.common.util.PropertySet;

import java.util.Date;

/**
 * Provides a time-range view of a set of calendar events.
 *
 * @author Tim Anderson
 */
public class CalendarGridView implements CalendarGrid {

    private final CalendarGrid grid;

    private final int startMins;

    private final int endMins;

    /**
     * The starting slot to view from.
     */
    private final int startSlot;

    /**
     * The no. of slots from startSlot.
     */
    private final int slots;

    /**
     * Constructs a {@link CalendarGridView}.
     *
     * @param grid      the grid to filter events from
     * @param startMins the start time of the view, as minutes from midnight
     * @param endMins   the end time of the view, as minutes from midnight
     */
    public CalendarGridView(CalendarGrid grid, int startMins, int endMins) {
        this.grid = grid;
        this.startMins = startMins;
        this.endMins = endMins;
        int start = grid.getFirstSlot(startMins);
        if (start == -1) {
            if (grid.getSlots() > 0) {
                start = grid.getSlots() - 1;
            } else {
                start = 0;
            }
        }
        startSlot = start;
        int endSlot;
        if (grid.getEndMins() == endMins) {
            endSlot = grid.getLastSlot(endMins - grid.getSlotSize());
        } else {
            endSlot = grid.getLastSlot(endMins);
        }
        slots = (endSlot - startSlot) + 1;
    }

    /**
     * Returns the no. of minutes from midnight that the grid starts at.
     *
     * @return the minutes from midnight that the grid starts at
     */
    @Override
    public int getStartMins() {
        return startMins;
    }

    /**
     * Returns the no. of minutes from midnight that the grid ends at.
     *
     * @return the minutes from midnight that the grid ends at
     */
    @Override
    public int getEndMins() {
        return endMins;
    }

    /**
     * Returns the first slot that has a start time and end time intersecting the specified minutes.
     *
     * @param minutes the minutes
     * @return the first slot that minutes intersects, or {@code -1} if no slots intersect
     */
    @Override
    public int getFirstSlot(int minutes) {
        int slot = grid.getFirstSlot(minutes);
        return slot >= 0 ? slot - startSlot : slot;
    }

    /**
     * Returns the last slot that has a start time and end time intersecting the specified minutes.
     *
     * @param minutes the minutes
     * @return the last slot that minutes intersects, or {@code -1} if no slots intersect
     */
    @Override
    public int getLastSlot(int minutes) {
        int slot = grid.getLastSlot(minutes);
        return slot >= 0 ? slot - startSlot : slot;
    }

    /**
     * Returns the no. of slots in the grid.
     *
     * @return the no. of slots
     */
    @Override
    public int getSlots() {
        return slots;
    }

    /**
     * Returns the starting date.
     *
     * @return the starting date
     */
    @Override
    public Date getStartDate() {
        return grid.getStartDate();
    }

    /**
     * Returns the number of days to display.
     *
     * @return the number of days
     */
    @Override
    public int getDays() {
        return grid.getDays();
    }

    /**
     * Returns the time that the specified slot starts at.
     *
     * @param slot the slot
     * @return the start time of the specified slot
     */
    @Override
    public Date getStartTime(int slot) {
        return grid.getStartTime(startSlot + slot);
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
        return grid.getDatetime(offset, startSlot + slot);
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
        return grid.getEvent(offset, startSlot + slot);
    }

    /**
     * Returns the hour that a slot falls in.
     *
     * @param slot the slot
     * @return the hour
     */
    @Override
    public int getHour(int slot) {
        return grid.getHour(startSlot + slot);
    }

    /**
     * Returns the slot size.
     *
     * @return the slot size, in minutes
     */
    @Override
    public int getSlotSize() {
        return grid.getSlotSize();
    }
}
