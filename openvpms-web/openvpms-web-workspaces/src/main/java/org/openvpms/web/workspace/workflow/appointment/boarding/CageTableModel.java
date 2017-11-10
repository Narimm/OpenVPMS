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

import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableCellRenderer;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.table.StyleTableCellRenderer;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.workflow.appointment.AbstractMultiDayTableModel;
import org.openvpms.web.workspace.workflow.scheduling.Schedule;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleColours;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleEventGrid;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Appointment table model for appointments that span multiple days where the appointments are grouped by cage type.
 *
 * @author Tim Anderson
 */
public abstract class CageTableModel extends AbstractMultiDayTableModel {

    /**
     * Constructs a {@link CageTableModel}.
     *
     * @param grid    the appointment grid
     * @param context the context
     * @param colours the colour cache
     */
    public CageTableModel(ScheduleEventGrid grid, Context context, ScheduleColours colours) {
        super(grid, context, colours);
    }

    /**
     * Returns the number of rows in the table.
     *
     * @return the row count
     */
    @Override
    public int getRowCount() {
        return getGrid().getRows().size();
    }

    /**
     * Returns the grid.
     *
     * @return the grid
     */
    @Override
    public CageScheduleGrid getGrid() {
        return (CageScheduleGrid) super.getGrid();
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param column the column
     * @param row    the row
     * @return the cell value
     */
    @Override
    public Object getValueAt(int column, int row) {
        Object result;
        CageRow cageRow = getGrid().getCageSchedule(row);
        if (column == SCHEDULE_INDEX || cageRow.isSummary()) {
            result = cageRow;
        } else {
            result = getEvent(column, row);
        }
        return result;
    }

    /**
     * Returns the event at the specified column and row.
     *
     * @param column the column
     * @param row    the row
     * @return the event, or {@code null} if none is found
     */
    @Override
    public PropertySet getEvent(int column, int row) {
        if (column == SCHEDULE_INDEX) {
            return null;
        }
        Schedule schedule = getSchedule(column, row);
        return (schedule != null) ? getGrid().getEvent(schedule, column - 1) : null;
    }

    /**
     * Returns the number of rows spanned by a cell.
     *
     * @param column the cell column
     * @param row    the cell row
     * @return the number of spanned rows
     */
    @Override
    public int getRows(int column, int row) {
        int rowSpan = 1;
        CageScheduleGrid grid = getGrid();
        List<CageRow> rows = grid.getRows();
        CageRow cageRow = rows.get(row);
        if (cageRow.getSchedule() != null) {
            Entity entity = cageRow.getSchedule().getSchedule();
            for (int i = row + 1; i < rows.size() && rows.get(i).isSchedule(entity); ++i) {
                if (getEvent(column, i) == null) {
                    rowSpan++;
                } else {
                    break;
                }
            }
        }
        return rowSpan;
    }

    /**
     * Returns the schedule at the given column and row.
     *
     * @param column the column
     * @param row    the row
     * @return the schedule, or {@code null} if there is no schedule associated with the column and row
     */
    @Override
    public Schedule getSchedule(int column, int row) {
        CageRow cageRow = getCageRow(row);
        return cageRow != null ? cageRow.getSchedule() : null;
    }

    /**
     * Returns the cage/schedule at the specified row.
     *
     * @param row the row
     * @return cage/schedule at the specified row, or {@code null} if the row doesn't exist
     */
    public CageRow getCageRow(int row) {
        return getGrid().getCageSchedule(row);
    }

    /**
     * Expands or contracts the group at the specified row.
     *
     * @param row the row
     */
    public void toggle(int row) {
        CageRow cageRow = getCageRow(row);
        if (cageRow != null && cageRow.getGroup() != null) {
            CageScheduleGroup group = cageRow.getGroup();
            getGrid().expand(group, !group.isExpanded());
            fireTableDataChanged();
        }
    }

    /**
     * Returns the state of the model.
     *
     * @return the state of the model
     */
    @Override
    public State getState() {
        return new CageState(this);
    }

    /**
     * Sets the state of the model.
     *
     * @param state the state
     */
    @Override
    public void setState(State state) {
        super.setState(state);
        if (state instanceof CageState) {
            boolean changed = false;
            CageScheduleGrid grid = getGrid();
            CageState cageState = (CageState) state;
            for (CageState.Group groupState : cageState.groups) {
                CageScheduleGroup group = grid.getGroup(groupState.cageType);
                if (group != null) {
                    changed |= grid.expand(group, groupState.expanded);
                }
            }
            if (changed) {
                fireTableDataChanged();
            }
        }
    }

    /**
     * Determines if a row represents a cage type.
     *
     * @param row the row
     * @return {@code true} if the row represents a cage type
     */
    public boolean isCageType(int row) {
        return getCageRow(row).isSummary();
    }

    /**
     * Returns all rows that a schedule appears in.
     *
     * @param scheduleRef the schedule reference
     * @return the rows
     */
    protected List<ScheduleRow> getRows(IMObjectReference scheduleRef) {
        List<ScheduleRow> result = new ArrayList<>();
        int index = 0;
        for (CageRow row : getGrid().getRows()) {
            if (row.isSchedule(scheduleRef)) {
                result.add(new ScheduleRow(row.getSchedule(), index));
            } else if (!result.isEmpty()) {
                // duplicate schedules appear consecutively, so can break out
                break;
            }
            index++;
        }
        return result;
    }

    /**
     * Creates a column model to display a list of schedules.
     *
     * @param grid the appointment grid
     * @return a new column model
     */
    @Override
    protected TableColumnModel createColumnModel(ScheduleEventGrid grid) {
        DefaultTableColumnModel result = new DefaultTableColumnModel();
        Date start = grid.getStartDate();
        int modelIndex = SCHEDULE_INDEX;
        Column cageSchedule = new Column(modelIndex++, Messages.get("workflow.scheduling.appointment.cage"));
        cageSchedule.setHeaderRenderer(new StyleTableCellRenderer("Table.Header"));
        cageSchedule.setCellRenderer(new CageScheduleCellRenderer());
        result.addColumn(cageSchedule);

        // add a column for each date
        TableCellRenderer renderer = createEventRenderer();
        for (int i = 0; i < grid.getSlots(); ++i) {
            DateColumn column = new DateColumn(modelIndex++, DateRules.getDate(start, i, DateUnits.DAYS));
            column.setCellRenderer(renderer);
            result.addColumn(column);
        }
        return result;
    }

    /**
     * Returns a renderer to render event cells.
     *
     * @return a new renderer
     */
    protected abstract TableCellRenderer createEventRenderer();

    protected static class CageState extends State {

        private List<Group> groups = new ArrayList<>();

        public CageState(CageTableModel model) {
            super(model);
            for (CageScheduleGroup group : model.getGrid().getGroups()) {
                groups.add(new Group(group));
            }
        }

        private static class Group {
            private final IMObjectReference cageType;

            private final boolean expanded;

            public Group(CageScheduleGroup group) {
                cageType = (group.getCageType() != null) ? group.getCageType().getObjectReference() : null;
                expanded = group.isExpanded();
            }
        }
    }
}
