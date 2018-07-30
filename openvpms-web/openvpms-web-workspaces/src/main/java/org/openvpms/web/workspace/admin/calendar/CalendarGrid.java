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
 * Represents the calendar as a grid of days and slots.
 *
 * @author Tim Anderson
 */
public interface CalendarGrid {

    /**
     * Returns the no. of minutes from midnight that the grid starts at.
     *
     * @return the minutes from midnight that the grid starts at
     */
    int getStartMins();

    /**
     * Returns the no. of minutes from midnight that the grid ends at.
     *
     * @return the minutes from midnight that the grid ends at
     */
    int getEndMins();

    /**
     * Returns the size of each slot, in minutes.
     *
     * @return the slot size, in minutes
     */
    int getSlotSize();

    /**
     * Returns the first slot that has a start time and end time intersecting the specified minutes.
     *
     * @param minutes the minutes
     * @return the first slot that minutes intersects, or {@code -1} if no slots intersect
     */
    int getFirstSlot(int minutes);

    /**
     * Returns the last slot that has a start time and end time intersecting the specified minutes.
     *
     * @param minutes the minutes
     * @return the last slot that minutes intersects, or {@code -1} if no slots intersect
     */
    int getLastSlot(int minutes);

    /**
     * Returns the no. of slots in the grid.
     *
     * @return the no. of slots
     */
    int getSlots();

    /**
     * Returns the starting date.
     *
     * @return the starting date
     */
    Date getStartDate();

    /**
     * Returns the number of days to display.
     *
     * @return the number of days
     */
    int getDays();

    /**
     * Returns the time that the specified slot starts at.
     *
     * @param slot the slot
     * @return the start time of the specified slot
     */
    Date getStartTime(int slot);

    /**
     * Returns the date/time of the specified slot.
     *
     * @param offset the day offset form {@link #getStartDate()}
     * @param slot   the slot
     * @return the date/time of the slot
     */
    Date getDatetime(int offset, int slot);

    /**
     * Returns the event at or intersecting the specified slot.
     *
     * @param offset the day offset form {@link #getStartDate()}
     * @param slot   the slot
     * @return the corresponding event, or {@code null} if none is found
     */
    PropertySet getEvent(int offset, int slot);

    /**
     * Returns the hour that a slot falls in.
     *
     * @param slot the slot
     * @return the hour
     */
    int getHour(int slot);
}
