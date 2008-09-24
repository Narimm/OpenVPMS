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

import org.openvpms.archetype.rules.workflow.Appointment;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ObjectSet;

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
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class MultiScheduleGrid extends AbstractAppointmentGrid {

    /**
     * The schedules.
     */
    private List<Schedule> columns;


    /**
     * Creates a new <tt>MultiScheduleGrid</tt>.
     *
     * @param date         the appointment date
     * @param appointments the appointments
     */
    public MultiScheduleGrid(Date date,
                             Map<Party, List<ObjectSet>> appointments) {
        super(date, -1, -1);
        rules = new AppointmentRules();
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
     * @return the corresponding appointment, or <tt>null</tt> if none is found
     */
    public ObjectSet getAppointment(Schedule schedule, int slot) {
        Date time = getStartTime(slot);
        ObjectSet result = schedule.getAppointment(time, getSlotSize());
        if (result == null && slot == 0) {
            result = schedule.getIntersectingAppointment(time);
        }
        return result;
    }

    /**
     * Returns the first slot that has a start time and end time intersecting
     * the specified minutes.
     *
     * @param minutes the minutes
     * @return the first slot that minutes intersects, or <tt>-1</tt> if no
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
     * @return the last slot that minutes intersects, or <tt>-1</tt> if no
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
    private void setAppointments(Map<Party, List<ObjectSet>> appointments) {
        int startMins = -1;
        int endMins = -1;
        int slotSize = -1;
        setSlotSize(-1);

        // Determine the startMins, endMins and slotSize. The:
        // . startMins is the minimum startMins of all schedules
        // . endMins is the minimum endMins of all schedules
        // . slotSize is the minimum slotSize of all schedules
        for (Party schedule : appointments.keySet()) {
            Schedule column = createSchedule(schedule);
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
        for (Map.Entry<Party, List<ObjectSet>> entry
                : appointments.entrySet()) {
            Party schedule = entry.getKey();
            List<ObjectSet> sets = entry.getValue();

            for (ObjectSet set : sets) {
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
    private void addAppointment(Party schedule, ObjectSet set) {
        Date startTime = set.getDate(Appointment.ACT_START_TIME);
        Date endTime = set.getDate(Appointment.ACT_END_TIME);
        int index = -1;
        boolean found = false;
        Schedule column = null;
        Schedule match = null;

        // try and find a corresponding Schedule that has no appointment that
        // intersects the supplied one
        for (int i = 0; i < columns.size(); ++i) {
            column = columns.get(i);
            if (column.getSchedule().equals(schedule)) {
                if (column.hasAppointment(set)) {
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
        column.addAppointment(set);

        // adjust the grid start and end times, if required
        int slotStart = getSlotMinutes(startTime, false);
        int slotEnd = getSlotMinutes(endTime, true);
        if (getStartMins() > slotStart) {
            setStartMins(slotStart);
        }
        if (getEndMins() < slotEnd) {
            setEndMins(slotEnd);
        }
    }

}
