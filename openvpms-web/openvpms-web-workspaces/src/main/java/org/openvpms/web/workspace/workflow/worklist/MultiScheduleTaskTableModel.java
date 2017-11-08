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

package org.openvpms.web.workspace.workflow.worklist;

import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.table.DefaultTableHeaderRenderer;
import org.openvpms.web.workspace.workflow.scheduling.Schedule;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleColours;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleEventGrid;

import java.util.List;


/**
 * Table model to display <em>act.customerTask<em>s for multiple schedules.
 *
 * @author Tim Anderson
 */
public class MultiScheduleTaskTableModel extends TaskTableModel {

    /**
     * Constructs a {@link MultiScheduleTaskTableModel}.
     *
     * @param grid    the task grid
     * @param context the context
     * @param colours the colour cache
     */
    public MultiScheduleTaskTableModel(TaskGrid grid, Context context, ScheduleColours colours) {
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
        return getEvent(column, row);
    }

    /**
     * Creates a column model to display a list of schedules.
     *
     * @param grid the appointment grid
     * @return a new column model
     */
    protected TableColumnModel createColumnModel(ScheduleEventGrid grid) {
        DefaultTableColumnModel result = new DefaultTableColumnModel();
        List<Schedule> schedules = grid.getSchedules();
        int i = 0;
        MultiScheduleTaskTableCellRenderer renderer = new MultiScheduleTaskTableCellRenderer(this);
        for (Schedule schedule : schedules) {
            ScheduleColumn column = new ScheduleColumn(i++, schedule);
            column.setCellRenderer(renderer);
            column.setHeaderRenderer(DefaultTableHeaderRenderer.DEFAULT);
            result.addColumn(column);
        }
        return result;
    }

}
