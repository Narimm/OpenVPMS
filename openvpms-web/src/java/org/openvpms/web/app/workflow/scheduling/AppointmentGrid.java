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
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface AppointmentGrid {

    /**
     * Returns the appointment date.
     * <p/>
     * All appointments in the grid start or end on this date.
     *
     * @return the date, excluding any time
     */
    Date getDate();

    /**
     * Returns the time for
     *
     * @param slot
     * @return the start time of the specified slot
     */
    Date getStartTime(int slot);

    int getStartMins(int slot);

    int getHour(int slot);

    int getStartMins();

    int getEndMins();

    int getSlots();

    List<Schedule> getSchedules();

    int getSlotSize();

    ObjectSet getAppointment(Schedule schedule, int slot);

    int getSlots(ObjectSet appointment, int slot);

    int getFirstSlot(int mins);

    int getLastSlot(int mins);

    Availability getAvailability(Schedule schedule, int row);

    int getUnavailableSlots(Schedule schedule, int row);

    /**
     * Slot availability.
     */
    enum Availability {
        FREE, BUSY, UNAVAILABLE
    }
}
