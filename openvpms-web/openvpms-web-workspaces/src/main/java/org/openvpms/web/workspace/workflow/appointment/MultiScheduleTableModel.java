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

import nextapp.echo2.app.Extent;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.workflow.scheduling.Schedule;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleColours;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleEventGrid;

import java.util.List;


/**
 * Appointment table model for multiple schedules.
 *
 * @author Tim Anderson
 */
class MultiScheduleTableModel extends AppointmentTableModel {

    /**
     * The column index of the right start time column.
     */
    private int rightStartTimeIndex;

    /**
     * Constructs a {@link MultiScheduleTableModel}.
     *
     * @param grid    the appointment grid
     * @param context the context
     * @param colours the colour cache
     */
    public MultiScheduleTableModel(AppointmentGrid grid, Context context, ScheduleColours colours) {
        super(grid, context, colours);
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
        Column c = getColumn(column);
        int index = c.getModelIndex();
        if (index == START_TIME_INDEX || index == rightStartTimeIndex) {
            result = getGrid().getStartTime(row);
        } else {
            result = getEvent(column, row);
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
        List<Schedule> schedules = grid.getSchedules();
        int index = START_TIME_INDEX;
        String startTime = Messages.get("workflow.scheduling.table.time");
        TimeColumnCellRenderer timeRenderer = new TimeColumnCellRenderer();

        // first column is the start time slots
        TableColumn leftStartCol = createTimeColumn(index, startTime, timeRenderer);
        result.addColumn(leftStartCol);

        ++index;
        int percent = (!schedules.isEmpty()) ? 100 / schedules.size() : 0;

        // add a column for each schedule
        MultiScheduleTableCellRenderer renderer = new MultiScheduleTableCellRenderer(this);
        for (Schedule schedule : schedules) {
            ScheduleColumn column = new ScheduleColumn(index++, schedule);
            column.setCellRenderer(renderer);
            column.setHeaderRenderer(AppointmentTableHeaderRenderer.INSTANCE);
            if (percent != 0) {
                column.setWidth(new Extent(percent, Extent.PERCENT));
            }
            result.addColumn(column);
        }

        // add a time column on the far right
        rightStartTimeIndex = index;
        TableColumn rightStartCol = createTimeColumn(rightStartTimeIndex, startTime, timeRenderer);
        result.addColumn(rightStartCol);
        return result;
    }

    /**
     * Creates a new column to display the appointment time slots.
     *
     * @param modelIndex the column model index
     * @param header     the header name
     * @param renderer   the column renderer
     * @return a new column
     */
    private TableColumn createTimeColumn(int modelIndex, String header, TimeColumnCellRenderer renderer) {
        ScheduleColumn column = new ScheduleColumn(modelIndex, header);
        column.setHeaderRenderer(AppointmentTableHeaderRenderer.INSTANCE);
        column.setCellRenderer(renderer);
        column.setWidth(new Extent(100));
        return column;
    }
}
