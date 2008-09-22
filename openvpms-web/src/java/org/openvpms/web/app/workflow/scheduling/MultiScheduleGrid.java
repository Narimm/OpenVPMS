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
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class MultiScheduleGrid extends AbstractAppointmentGrid {

    private List<Schedule> columns;

    private final AppointmentRules rules;


    public MultiScheduleGrid(Date date,
                             Map<Party, List<ObjectSet>> appointments) {
        super(date, -1, -1);
        rules = new AppointmentRules();
        columns = new ArrayList<Schedule>();
        setAppointments(appointments);
    }

    public List<Schedule> getSchedules() {
        return columns;
    }

    public ObjectSet getAppointment(Schedule schedule, int slot) {
        Date time = getStartTime(slot);
        ObjectSet result = schedule.getAppointment(time, getSlotSize());
        if (result == null && slot == 0) {
            result = schedule.getIntersectingAppointment(time);
        }
        return result;
    }

    public int getFirstSlot(int mins) {
        if (mins < getStartMins() || mins > getEndMins()) {
            return -1;
        }
        return (mins - getStartMins()) / getSlotSize();
    }

    public int getLastSlot(int mins) {
        return getFirstSlot(mins);
    }


    private void setAppointments(Map<Party, List<ObjectSet>> appointments) {
        int startMins = -1;
        int endMins = -1;
        setSlotSize(-1);
        for (Party schedule : appointments.keySet()) {
            Schedule column = new Schedule(schedule, rules);
            columns.add(column);
            int start = column.getStartMins();
            if (startMins == -1 || start < startMins) {
                startMins = start;
            }
            int end = column.getEndMins();
            if (end > endMins) {
                endMins = end;
            }
            if (column.getSlotSize() < getSlotSize()) {
                setSlotSize(column.getSlotSize());
            }
        }
        if (startMins == -1) {
            startMins = DEFAULT_START;
        }
        if (endMins == -1) {
            endMins = DEFAULT_END;
        }
        setStartMins(startMins);
        setEndMins(endMins);

        for (Map.Entry<Party, List<ObjectSet>> entry
                : appointments.entrySet()) {
            Party schedule = entry.getKey();
            List<ObjectSet> sets = entry.getValue();

            for (ObjectSet set : sets) {
                addAppointment(schedule, set);
            }
        }
        if (getSlotSize() == -1) {
            setSlotSize(DEFAULT_SLOT_SIZE);
        }
    }

    private void addAppointment(Party schedule, ObjectSet set) {
        Date startTime = set.getDate(Appointment.ACT_START_TIME);
        Date endTime = set.getDate(Appointment.ACT_END_TIME);
        int index = -1;
        boolean found = false;
        Schedule column = null;
        Schedule match = null;
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
            column = new Schedule(match);
            columns.add(index + 1, column);
        }
        column.addAppointment(set);
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
