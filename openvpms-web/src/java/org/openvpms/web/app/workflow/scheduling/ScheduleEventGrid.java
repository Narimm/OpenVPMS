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

import org.openvpms.component.system.common.query.ObjectSet;

import java.util.Date;
import java.util.List;


/**
 * Represents a grid of schedule events.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface ScheduleEventGrid {

    /**
     * Slot availability.
     */
    enum Availability {
        FREE, BUSY, UNAVAILABLE
    }

    /**
     * Returns the schedule date.
     * <p/>
     * All events in the grid start or end on this date.
     *
     * @return the date, excluding any time
     */
    Date getDate();

    /**
     * Returns the schedules.
     *
     * @return the schedules
     */
    List<Schedule> getSchedules();

    /**
     * Returns the no. of slots in the grid.
     *
     * @return the no. of slots
     */
    int getSlots();

    /**
     * Returns the event for the specified schedule and slot.
     *
     * @param schedule the schedule
     * @param slot     the slot
     * @return the corresponding event, or <tt>null</tt> if none is found
     */
    ObjectSet getEvent(Schedule schedule, int slot);

    /**
     * Returns the time that the specified slot starts at.
     *
     * @param schedule the schedule
     * @param slot     the slot
     * @return the start time of the specified slot. May be <tt>null</tt>
     */
    Date getStartTime(Schedule schedule, int slot);

    /**
     * Determines the availability of a slot for the specified schedule.
     *
     * @param schedule the schedule
     * @param slot     the slot
     * @return the availability of the schedule
     */
    Availability getAvailability(Schedule schedule, int slot);

    /**
     * Determines how many slots are unavailable from the specified slot, for
     * a schedule.
     *
     * @param schedule the schedule
     * @param slot     the starting slot
     * @return the no. of concurrent slots that are unavailable
     */
    int getUnavailableSlots(Schedule schedule, int slot);

}
