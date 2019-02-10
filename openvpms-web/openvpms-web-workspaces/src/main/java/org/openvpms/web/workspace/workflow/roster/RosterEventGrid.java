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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.roster;

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.archetype.rules.workflow.ScheduleEvents;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.system.common.util.PropertySet;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Roster event grid.
 *
 * @author Tim Anderson
 */
public class RosterEventGrid {

    /**
     * The events.
     */
    private final Map<Entity, ScheduleEvents> events;

    /**
     * The start date.
     */
    private final Date startDate;

    /**
     * The end date.
     */
    private final Date endDate;

    /**
     * The number of days in the grid.
     */
    private final int days;

    /**
     * The rows in the grid.
     */
    private final List<Row> rows = new ArrayList<>();

    /**
     * Constructs a {@link RosterEventGrid}.
     *
     * @param startDate the start date
     * @param endDate   the end date
     * @param days      the number of days in the grid
     * @param events    the events
     */
    public RosterEventGrid(Date startDate, Date endDate, int days, Map<Entity, ScheduleEvents> events) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.days = days;
        this.events = events;

        for (Map.Entry<Entity, ScheduleEvents> entry : events.entrySet()) {
            Entity entity = entry.getKey();
            ScheduleEvents value = entry.getValue();
            EntityRoster<Entity> roster = new EntityRoster<>(entity, startDate, days, value.getEvents());
            for (List<PropertySet> list : roster.getEvents()) {
                rows.add(new Row(roster, list));
            }
        }
    }

    /**
     * Returns the events.
     *
     * @return the events
     */
    public Map<Entity, ScheduleEvents> getEvents() {
        return events;
    }

    /**
     * Returns the number of rows in the grid.
     *
     * @return the number of rows
     */
    public int getRows() {
        return rows.size();
    }

    /**
     * Returns the number of columns in the grid.
     * <p/>
     * Each column represents a day.
     *
     * @return the number of columns
     */
    public int getColumns() {
        return days;
    }

    /**
     * Returns the grid start date.
     *
     * @return the start date
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Returns the grid end date.
     *
     * @return the end date
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Returns the date of the specified column.
     *
     * @param column the column
     * @return the corresponding date
     */
    public Date getDate(int column) {
        return DateRules.getDate(startDate, column, DateUnits.DAYS);
    }

    /**
     * Returns the events at the specified row.
     *
     * @param row the row
     * @return the corresponding events
     */
    public List<PropertySet> getEvents(int row) {
        return rows.get(row).events;
    }

    /**
     * Returns the event at the specified cell.
     *
     * @param column the column
     * @param row    the row
     * @return the corresponding event. May be {@code null}
     */
    public PropertySet getEvent(int column, int row) {
        List<PropertySet> events = getEvents(row);
        return column < events.size() ? events.get(column) : null;
    }

    /**
     * Returns the entity at the specified row.
     *
     * @param row the row
     * @return the corresponding entity
     */
    public Entity getEntity(int row) {
        return rows.get(row).roster.getEntity();
    }

    /**
     * Returns the first row for the specified entity.
     *
     * @param entity the entity reference
     * @return the first row for the entity, or {@code -1} if none is found
     */
    public int findEntity(Reference entity) {
        return findEntity(0, entity);
    }

    /**
     * Returns the first row for the specified entity, starting at the specified index.
     *
     * @param index  the starting row
     * @param entity the entity reference
     * @return the first row for the entity, or {@code -1} if none is found
     */
    public int findEntity(int index, Reference entity) {
        int result = -1;
        for (int i = index; i < rows.size(); ++i) {
            if (getEntity(i).getObjectReference().equals(entity)) {
                result = i;
            }
        }
        return result;
    }

    /**
     * Finds an event in the specified row.
     *
     * @param row   the row
     * @param event the event reference
     * @return the first column of the event, or {@code -1} if it is not found
     */
    public int findEvent(int row, Reference event) {
        int result = -1;
        List<PropertySet> events = rows.get(row).events;
        for (int i = 0; i < events.size(); ++i) {
            if (events.get(i).getReference(ScheduleEvent.ACT_REFERENCE).equals(event)) {
                result = i;
                break;
            }
        }
        return result;
    }

    static class Row {

        private final EntityRoster roster;

        private final List<PropertySet> events;

        Row(EntityRoster roster, List<PropertySet> events) {
            this.roster = roster;
            this.events = events;
        }

    }
}
