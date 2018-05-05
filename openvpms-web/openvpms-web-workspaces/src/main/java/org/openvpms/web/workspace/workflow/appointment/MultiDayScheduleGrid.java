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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment;

import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.system.common.util.PropertySet;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Multiple-day schedule grid.
 *
 * @author Tim Anderson
 */
public class MultiDayScheduleGrid extends AbstractMultiDayScheduleGrid {

    /**
     * Constructs an {@link MultiDayScheduleGrid}.
     *
     * @param scheduleView the schedule view
     * @param date         the date
     * @param days         the number of days to display
     * @param events       the events
     * @param rules        the appointment rules
     */
    public MultiDayScheduleGrid(Entity scheduleView, Date date, int days, Map<Entity, List<PropertySet>> events,
                                AppointmentRules rules) {
        super(scheduleView, date, days, events, rules);
    }

}
