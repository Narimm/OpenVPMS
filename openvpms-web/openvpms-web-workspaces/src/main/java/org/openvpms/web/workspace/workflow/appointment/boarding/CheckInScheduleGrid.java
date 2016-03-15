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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment.boarding;

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.workspace.workflow.appointment.AbstractMultiDayScheduleGrid;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A grid that shows all schedules with appointments starting on the date.
 *
 * @author Tim Anderson
 */
public class CheckInScheduleGrid extends AbstractMultiDayScheduleGrid {

    /**
     * Constructs an {@link CheckInScheduleGrid}.
     *
     * @param scheduleView the schedule view
     * @param date         the date
     * @param days         the number of days to display
     * @param appointments the appointments
     */
    public CheckInScheduleGrid(Entity scheduleView, Date date, int days, Map<Entity, List<PropertySet>> appointments) {
        super(scheduleView, date, days, filterAppointments(appointments, date));
    }

    /**
     * Filters appointments so that they only include those checking in on or after the specified date.
     * <p>
     * If a schedule has no appointments checking in on the date, then all of its appointments are excluded.
     *
     * @param appointments the appointments
     * @param date         the date
     * @return the filtered appointments
     */
    private static Map<Entity, List<PropertySet>> filterAppointments(Map<Entity, List<PropertySet>> appointments,
                                                                     Date date) {
        Map<Entity, List<PropertySet>> map = new LinkedHashMap<>();
        for (Map.Entry<Entity, List<PropertySet>> appointmentsBySchedule : appointments.entrySet()) {
            if (!appointmentsBySchedule.getValue().isEmpty()) {
                List<PropertySet> onDate = new ArrayList<>();    // check-ins on date
                List<PropertySet> afterDate = new ArrayList<>(); // check-ins after date
                for (PropertySet set : appointmentsBySchedule.getValue()) {
                    Date startDate = DateRules.getDate(set.getDate(ScheduleEvent.ACT_START_TIME));
                    if (startDate.compareTo(date) == 0) {
                        onDate.add(set);
                    } else if (startDate.compareTo(date) > 0) {
                        afterDate.add(set);
                    }
                }
                if (!onDate.isEmpty()) {
                    onDate.addAll(afterDate);                   // add all check-ins after the date to provide context
                    map.put(appointmentsBySchedule.getKey(), onDate);
                }
            }
        }
        return map;
    }
}
