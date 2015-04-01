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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment;

import echopointng.layout.TableLayoutDataEx;
import echopointng.xhtml.XhtmlFragment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Table;
import org.openvpms.web.echo.table.TableHelper;
import org.openvpms.web.workspace.workflow.scheduling.Schedule;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleEventGrid;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleTableCellRenderer;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleTableModel;

import static org.openvpms.web.workspace.workflow.scheduling.ScheduleEventGrid.Availability.UNAVAILABLE;


/**
 * TableCellRender for {@link MultiDayTableModel}.
 *
 * @author Tim Anderson
 */
public class MultiDayTableCellRenderer extends ScheduleTableCellRenderer {

    /**
     * Default constructor.
     */
    public MultiDayTableCellRenderer() {
        super("entity.appointmentType");
    }

    /**
     * Returns a component for a value.
     *
     * @param table  the <tt>Table</tt> for which the rendering is occurring
     * @param value  the value retrieved from the <tt>TableModel</tt> for the specified coordinate
     * @param column the column
     * @param row    the row
     * @return a component representation of the value. May be {@code null}
     */
    @Override
    protected Component getComponent(Table table, Object value, int column, int row) {
        Component result;
        MultiDayTableModel model = (MultiDayTableModel) table.getModel();
        if (value instanceof Component && model.isScheduleColumn(column)) {
            String style = getFreeStyle(model, row);
            TableLayoutDataEx layout = TableHelper.getTableLayoutDataEx(style);
            result = (Component) value;
            result.setLayoutData(layout);
        } else {
            result = super.getComponent(table, value, column, row);
        }
        return result;
    }

    /**
     * Returns a {@code XhtmlFragment} that will be displayed as the content at the specified co-ordinate in the table.
     *
     * @param table  the {@code Table} for which the rendering is occurring
     * @param value  the value retrieved from the {@code TableModel} for the specified coordinate
     * @param column the column index to render
     * @param row    the row index to render
     * @return a {@code XhtmlFragment} representation of the value
     */
    public XhtmlFragment getTableCellRendererContent(Table table, Object value, int column, int row) {
        XhtmlFragment result = TableHelper.createFragment(value);
        ScheduleTableModel model = (ScheduleTableModel) table.getModel();

        ScheduleEventGrid.Availability avail = model.getAvailability(column, row);
        String style = getStyle(avail, model, row);
        TableLayoutDataEx layout = TableHelper.getTableLayoutDataEx(style);

        if (layout != null && avail == UNAVAILABLE) {
            Schedule schedule = model.getSchedule(column, row);
            int span = model.getGrid().getUnavailableSlots(schedule, row);
            layout.setRowSpan(span);
        }
        result.setLayoutData(layout);
        return result;
    }

    /**
     * This method allows you to "restrict" the cells (within a row) that will
     * cause selection of the row to occur. By default any cell will cause
     * selection of a row. If this methods returns false then only certain cells
     * within the row will cause selection when clicked on.
     *
     * @param table  the table
     * @param column the column
     * @param row    the row
     * @return {@code true} if the cell causes selection
     */
    @Override
    public boolean isSelectionCausingCell(Table table, int column, int row) {
        MultiDayTableModel model = (MultiDayTableModel) table.getModel();
        return !model.isScheduleColumn(column) && super.isSelectionCausingCell(table, column, row);
    }

    /**
     * This method is called to determine which cells within a row can cause an
     * action to be raised on the server when clicked.
     * <p/>
     * By default if a Table has attached actionListeners then any click on any
     * cell within a row will cause the action to fire.
     * <p/>
     * This method allows this to be overrriden and only certain cells within a
     * row can cause an action event to be raise.
     *
     * @param table  the Table in question
     * @param column the column in question
     * @param row    the row in quesiton
     * @return true means that the cell can cause actions while false means the cells can not cause action events.
     */
    @Override
    public boolean isActionCausingCell(Table table, int column, int row) {
        MultiDayTableModel model = (MultiDayTableModel) table.getModel();
        return !model.isScheduleColumn(column) && super.isActionCausingCell(table, column, row);
    }

    /**
     * Determines if the cell can be highlighted.
     *
     * @param table  the table
     * @param column the column
     * @param row    the row
     * @return {@code true} if the cell can be highlighted
     */
    @Override
    protected boolean canHighlightCell(Table table, int column, int row) {
        MultiDayTableModel model = (MultiDayTableModel) table.getModel();
        return !model.isScheduleColumn(column) && super.canHighlightCell(table, column, row);
    }


}
