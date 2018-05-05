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

import echopointng.table.TableCellRendererEx;
import echopointng.xhtml.XhtmlFragment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.TableFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.table.StyleTableCellRenderer;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.workflow.appointment.AbstractMultiDayScheduleGrid;
import org.openvpms.web.workspace.workflow.appointment.MultiDayTableModel;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleColours;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleEventGrid;

import java.util.Date;

/**
 * A table model for displaying check-ins and check-outs for a particular date.
 * If there are none, displays a message to that effect.
 *
 * @author Tim Anderson
 */
public abstract class CheckInOutTableModel extends MultiDayTableModel {

    /**
     * Constructs a {@link CheckInOutTableModel}.
     *
     * @param grid    the appointment grid
     * @param context the context
     * @param colours the colour cache
     */
    public CheckInOutTableModel(AbstractMultiDayScheduleGrid grid, Context context, ScheduleColours colours) {
        super(grid, context, colours);
    }

    /**
     * Determines if the grid is empty.
     *
     * @return {@code true} if the grid is empty
     */
    public boolean isEmpty() {
        return getGrid().getSchedules().isEmpty();
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
        if (isEmpty()) {
            return null;
        }
        return super.getValueAt(column, row);
    }

    /**
     * Returns the number of rows in the table.
     *
     * @return the row count
     */
    @Override
    public int getRowCount() {
        return isEmpty() ? 1 : super.getRowCount();
    }

    /**
     * Creates a column model to display a list of schedules.
     *
     * @param grid the appointment grid
     * @return a new column model
     */
    @Override
    protected TableColumnModel createColumnModel(ScheduleEventGrid grid) {
        TableColumnModel result;
        if (isEmpty()) {
            // create a column model with the normal header, but use the renderer returned by createEmptyTableCellRenderer()
            // to render the content
            result = new DefaultTableColumnModel();
            Date start = grid.getStartDate();
            int modelIndex = 0;

            // add the schedule column
            Column scheduleColumn = new Column(modelIndex++, Messages.get("workflow.scheduling.type"));
            scheduleColumn.setHeaderRenderer(new StyleTableCellRenderer("Table.Header"));
            scheduleColumn.setCellRenderer(createEmptyTableCellRenderer());
            result.addColumn(scheduleColumn);

            // add a column for each day
            for (int i = 0; i < grid.getSlots(); ++i) {
                DateColumn column = new DateColumn(modelIndex++, DateRules.getDate(start, i, DateUnits.DAYS));
                result.addColumn(column);
            }

        } else {
            result = super.createColumnModel(grid);
        }
        return result;
    }

    /**
     * Creates a cell renderer to render the table when there are no check-ins or check-outs.
     *
     * @return the renderer
     */
    protected abstract EmptyTableRenderer createEmptyTableCellRenderer();


    protected class EmptyTableRenderer implements TableCellRendererEx {

        /**
         * The message resource bundle key.
         */
        private final String messageKey;

        /**
         * Constructs an {@link EmptyTableRenderer}.
         *
         * @param messageKey the message resource bundle key
         */
        public EmptyTableRenderer(String messageKey) {
            this.messageKey = messageKey;
        }

        /**
         * Returns a component that will be displayed at the specified coordinate in
         * the table.
         *
         * @param table  the {@code Table} for which the rendering is occurring
         * @param value  the value retrieved from the {@code TableModel} for the specified coordinate
         * @param column the column index to render
         * @param row    the row index to render
         * @return a component representation  of the value.
         */
        public Component getTableCellRendererComponent(Table table, Object value, int column, int row) {
            Label label = LabelFactory.create(null, Styles.BOLD);
            label.setText(Messages.format(messageKey, getGrid().getStartDate()));
            nextapp.echo2.app.Column result = ColumnFactory.create(Styles.LARGE_INSET, label);
            result.setLayoutData(TableFactory.columnSpan(table.getModel().getColumnCount()));

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
            return false;
        }

        /**
         * This method is called to determine which cells within a row can cause an
         * action to be raised on the server when clicked.
         * <p>
         * By default if a Table has attached actionListeners then any click on any
         * cell within a row will cause the action to fire.
         * <p>
         * This method allows this to be overridden and only certain cells within a
         * row can cause an action event to be raise.
         *
         * @param table  the Table in question
         * @param column the column in question
         * @param row    the row in question
         * @return true means that the cell can cause actions while false means the cells can not cause action events.
         */
        @Override
        public boolean isActionCausingCell(Table table, int column, int row) {
            return false;
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
            return null;
        }

    }
}
