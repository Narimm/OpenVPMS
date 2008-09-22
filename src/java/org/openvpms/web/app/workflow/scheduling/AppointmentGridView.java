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
 * Provides a time-range view of a set of appointments.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class AppointmentGridView extends AbstractAppointmentGrid {

    /**
     * The grid to filter appointments from.
     */
    private final AppointmentGrid grid;

    private final int startSlot;

    private int slots;

    /**
     * Creates a new <tt>AppointmentGridView</tt>.
     *
     * @param grid
     * @param startMins
     * @param endMins
     */
    public AppointmentGridView(AppointmentGrid grid, int startMins,
                               int endMins) {
        super(grid.getDate(), startMins, endMins);
        this.grid = grid;
        startSlot = grid.getFirstSlot(startMins);
        int endSlot;
        if (grid.getEndMins() == endMins) {
            endSlot = grid.getLastSlot(endMins - getSlotSize());
        } else {
            endSlot = grid.getLastSlot(endMins);
        }
        slots = (endSlot - startSlot) + 1;
    }

    public List<Schedule> getSchedules() {
        return grid.getSchedules();
    }

    public ObjectSet getAppointment(Schedule schedule, int slot) {
        return grid.getAppointment(schedule, slot);
    }

    @Override
    public int getSlots() {
        return slots;
    }

    @Override
    public Date getStartTime(int slot) {
        return grid.getStartTime(startSlot + slot);
    }

    @Override
    public int getHour(int slot) {
        return grid.getHour(startSlot + slot);
    }

    @Override
    public int getSlots(ObjectSet appointment, int slot) {
        return grid.getSlots(appointment, slot);
    }

    public int getFirstSlot(int mins) {
        return grid.getFirstSlot(mins);
    }

    public int getLastSlot(int mins) {
        return grid.getLastSlot(mins);
    }
}
