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

import echopointng.table.TableCellRendererEx;
import echopointng.xhtml.XhtmlFragment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.TableFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.table.StyleTableCellRenderer;
import org.openvpms.web.echo.table.TableHelper;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.workflow.scheduling.Schedule;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleColours;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleEventGrid;

import java.util.Date;
import java.util.List;

import static org.openvpms.web.workspace.workflow.scheduling.ScheduleTableCellRenderer.EVEN_ROW_STYLE;
import static org.openvpms.web.workspace.workflow.scheduling.ScheduleTableCellRenderer.ODD_ROW_STYLE;

/**
 * Appointment table model for appointments that span multiple days.
 *
 * @author Tim Anderson
 */
public class MultiDayTableModel extends AbstractMultiDayTableModel {

    /**
     * Constructs a {@link MultiDayTableModel}.
     * @param grid                 the appointment grid
     * @param context              the context
     * @param colours              the colour cache
     */
    public MultiDayTableModel(AbstractMultiDayScheduleGrid grid, Context context, ScheduleColours colours) {
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
        Schedule schedule = getSchedule(column, row);
        if (column == SCHEDULE_INDEX) {
            result = schedule;
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
        if (column == 0) { // first column is the schedule column
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
    public int getRows(int column, int row) {
        int rowSpan = 1;
        Entity entity = getSchedule(0, row).getSchedule();
        List<Schedule> schedules = getSchedules();
        for (int i = row + 1; i < schedules.size() && schedules.get(i).getSchedule().equals(entity); ++i) {
            if (getEvent(column, i) == null) {
                rowSpan++;
            } else {
                break;
            }
        }
        return rowSpan;
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
        int modelIndex = 0;

        // add the schedule column
        Column scheduleColumn = new Column(modelIndex++, Messages.get("workflow.scheduling.type"));
        scheduleColumn.setHeaderRenderer(new StyleTableCellRenderer("Table.Header"));
        scheduleColumn.setCellRenderer(new ScheduleColumnCellRenderer());
        result.addColumn(scheduleColumn);

        // add a column for each day
        MultiDayTableCellRenderer renderer = new MultiDayTableCellRenderer(this);
        for (int i = 0; i < grid.getSlots(); ++i) {
            DateColumn column = new DateColumn(modelIndex++, DateRules.getDate(start, i, DateUnits.DAYS));
            column.setCellRenderer(renderer);
            result.addColumn(column);
        }
        return result;
    }

    private static class ScheduleColumnCellRenderer implements TableCellRendererEx {
        /**
         * Returns a {@code XhtmlFragment} that will be displayed as the content at the specified co-ordinate in the table.
         *
         * @param table  the {@code Table} for which the rendering is occurring
         * @param value  the value retrieved from the {@code TableModel} for the specified coordinate
         * @param column the column index to render
         * @param row    the row index to render
         * @return a {@code XhtmlFragment} representation of the value
         */
        @Override
        public XhtmlFragment getTableCellRendererContent(Table table, Object value, int column, int row) {
            return null;
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
         * <p/>
         * By default if a Table has attached actionListeners then any click on any
         * cell within a row will cause the action to fire.
         * <p/>
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
         * Returns a component that will be displayed at the specified coordinate in
         * the table.
         *
         * @param table  the {@code Table} for which the rendering is occurring
         * @param value  the value retrieved from the {@code TableModel} for
         *               the specified coordinate
         * @param column the column index to render
         * @param row    the row index to render
         * @return a component representation  of the value.
         */
        @Override
        public Component getTableCellRendererComponent(Table table, Object value, int column, int row) {
            Component result;
            Schedule schedule = (Schedule) value;
            Entity entity = schedule.getSchedule();
            if (entity != null) {
                MultiDayTableModel model = (MultiDayTableModel) table.getModel();
                Label label = LabelFactory.create(null, Styles.BOLD);
                label.setText(entity.getName());
                row++;
                int span = 1;
                while (row < model.getRowCount()) {
                    Schedule next = model.getSchedule(0, row);
                    if (next != null && next.getSchedule().equals(entity)) {
                        span++;
                        row++;
                    } else {
                        break;
                    }
                }
                if (span > 1) {
                    label.setLayoutData(TableFactory.rowSpan(span));
                }
                result = label;
            } else {
                result = new Label();
            }
            String styleName = schedule.getRenderEven() ? EVEN_ROW_STYLE : ODD_ROW_STYLE;
            TableHelper.mergeStyle(result, styleName);
            return result;
        }
    }
}
