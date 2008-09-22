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

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.Appointment;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.Date;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractAppointmentGrid implements AppointmentGrid {

    private Date date;

    /**
     * The start time, as minutes since midnight.
     */
    private int startMins = DEFAULT_START;

    /**
     * The end time, as minutes since midnight.
     */
    private int endMins = DEFAULT_END;

    /**
     * The slot size, in minutes.
     */
    private int slotSize = DEFAULT_SLOT_SIZE;

    /**
     * The default slot size, in minutes.
     */
    protected static final int DEFAULT_SLOT_SIZE = 15;

    /**
     * The default start time, as minutes from midnight.
     */
    protected static final int DEFAULT_START = 8 * 60;

    /**
     * The default end time, as minutes from midnight.
     */
    protected static final int DEFAULT_END = 18 * 60;

    public AbstractAppointmentGrid(Date date, int startMins, int endMins) {
        this.date = DateRules.getDate(date);
        this.startMins = startMins;
        this.endMins = endMins;
    }

    public Date getDate() {
        return date;
    }


    public Date getStartTime(int slot) {
        return DateRules.getDate(date, getStartMins(slot),
                                 DateUnits.MINUTES);
    }

    public int getStartMins(int slot) {
        return startMins + (slot * slotSize);
    }


    public int getHour(int slot) {
        int slotSize = getSlotSize();
        return (startMins + (slot * slotSize)) / 60;
    }

    public int getSlots() {
        return (endMins - startMins) / slotSize;
    }

    public int getSlotSize() {
        return slotSize;
    }

    public int getSlots(ObjectSet appointment, int slot) {
        Date startTime = getStartTime(slot);
        Date endTime = appointment.getDate(Appointment.ACT_END_TIME);
        int startSlot = getSlot(startTime);
        int endSlot = getSlot(endTime);
        return endSlot - startSlot;
    }

    public int getStartMins() {
        return startMins;
    }

    public int getEndMins() {
        return endMins;
    }

    public Availability getAvailability(Schedule schedule, int slot) {
        int mins = getStartMins(slot);
        if (mins < schedule.getStartMins() || mins >= schedule.getEndMins()) {
            return Availability.UNAVAILABLE;
        }
        if (getAppointment(schedule, slot) != null) {
            return Availability.BUSY;
        }
        return Availability.FREE;
    }

    public int getUnavailableSlots(Schedule schedule, int slot) {
        int result = 0;
        int mins = getStartMins(slot);
        if (mins < schedule.getStartMins()) {
            result = getFirstSlot(schedule.getStartMins()) - slot;
        } else if (mins >= schedule.getEndMins()) {
            result = getSlots() - slot;
        }
        return result;
    }

    protected void setStartMins(int startMins) {
        this.startMins = startMins;
    }

    protected void setEndMins(int endMins) {
        this.endMins = endMins;
    }

    protected void setSlotSize(int slotSize) {
        this.slotSize = slotSize;
    }

    protected int getSlotMinutes(Date time, boolean roundUp) {
        return SchedulingHelper.getSlotMinutes(time, slotSize, roundUp);
    }

    private int getSlot(Date time) {
        int mins = SchedulingHelper.getMinutes(time);
        return (mins - startMins) / slotSize;
    }

}
