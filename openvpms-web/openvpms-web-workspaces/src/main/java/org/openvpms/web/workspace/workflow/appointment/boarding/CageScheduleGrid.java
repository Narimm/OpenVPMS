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

package org.openvpms.web.workspace.workflow.appointment.boarding;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.workspace.workflow.appointment.AbstractMultiDayScheduleGrid;
import org.openvpms.web.workspace.workflow.scheduling.Schedule;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Multi-day grid that groups schedules by cage type.
 *
 * @author Tim Anderson
 */
public class CageScheduleGrid extends AbstractMultiDayScheduleGrid {

    /**
     * The groups.
     */
    private final Set<CageScheduleGroup> groups;

    /**
     * The grid rows.
     */
    private List<CageRow> rows = new ArrayList<>();

    /**
     * Constructs a {@link CageScheduleGrid}.
     *
     * @param scheduleView the schedule view
     * @param date         the date
     * @param days         the number of days to display
     * @param appointments the appointments
     * @param rules        the appointment rules
     */
    public CageScheduleGrid(Entity scheduleView, Date date, int days, Map<Entity, List<PropertySet>> appointments,
                            AppointmentRules rules) {
        super(scheduleView, date, days, appointments, rules);

        groups = new LinkedHashSet<>();
        CageScheduleGroup schedulesWithOutCageType = new CageScheduleGroup(null);
        Map<Entity, CageScheduleGroup> groupsByCageType = new HashMap<>();
        for (Schedule schedule : getSchedules()) {
            CageScheduleGroup group;
            if (schedule.getCageType() != null) {
                group = groupsByCageType.get(schedule.getCageType());
                if (group == null) {
                    group = new CageScheduleGroup(schedule.getCageType());
                    groupsByCageType.put(schedule.getCageType(), group);
                }
            } else {
                group = schedulesWithOutCageType;
            }
            group.add(schedule);
            groups.add(group);
        }
        layout();
    }

    /**
     * Returns the groups.
     *
     * @return the groups
     */
    public Set<CageScheduleGroup> getGroups() {
        return groups;
    }

    /**
     * Returns the count of unique schedules within the grid.
     *
     * @return the schedule count
     */
    public int getScheduleCount() {
        int result = 0;
        for (CageScheduleGroup group : groups) {
            result += group.getScheduleCount();
        }
        return result;
    }

    /**
     * Returns a group for a cage type.
     *
     * @param cageType the cage type reference
     * @return the corresponding group, or {@code null} if none was found
     */
    public CageScheduleGroup getGroup(IMObjectReference cageType) {
        for (CageScheduleGroup group : groups) {
            if (group.getCageType() != null && ObjectUtils.equals(group.getCageType().getObjectReference(), cageType)) {
                return group;
            }
        }
        return null;
    }

    /**
     * Returns the rows.
     *
     * @return the rows
     */
    public List<CageRow> getRows() {
        return rows;
    }

    /**
     * Returns the cage/schedule at the specified row.
     *
     * @param row the row
     * @return cage/schedule at the specified row, or {@code null} if the row doesn't exist
     */
    public CageRow getCageSchedule(int row) {
        return row < rows.size() ? rows.get(row) : null;
    }

    /**
     * Expands or collapses a group.
     *
     * @param group  the group
     * @param expand if {@code true}, expand it, otherwise collapse it
     * @return {@code true} if the group changed
     */
    public boolean expand(CageScheduleGroup group, boolean expand) {
        boolean changed = false;
        if (group.isExpanded() != expand) {
            group.setExpanded(expand);
            changed = true;
            layout();
        }
        return changed;
    }

    /**
     * Lays out the grid.
     */
    private void layout() {
        rows.clear();
        rows.add(new CageRow()); // totals row
        for (CageScheduleGroup group : groups) {
            rows.add(new CageRow(group, null));
            if (group.isExpanded()) {
                for (Schedule schedule : group.getSchedules()) {
                    rows.add(new CageRow(group, schedule));
                }
            }
        }
    }

}
