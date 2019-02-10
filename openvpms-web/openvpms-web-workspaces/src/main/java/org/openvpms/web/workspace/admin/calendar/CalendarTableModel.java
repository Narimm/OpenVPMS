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

package org.openvpms.web.workspace.admin.calendar;

import echopointng.LabelEx;
import echopointng.layout.TableLayoutDataEx;
import echopointng.table.TableCellRendererEx;
import echopointng.xhtml.XhtmlFragment;
import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.table.AbstractCellTableModel;
import org.openvpms.web.echo.table.Cell;
import org.openvpms.web.echo.table.TableColumnFactory;
import org.openvpms.web.echo.table.TableHelper;
import org.openvpms.web.resource.i18n.format.DateFormatter;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Calendar table model.
 *
 * @author Tim Anderson
 */
public class CalendarTableModel extends AbstractCellTableModel {

    public enum Availability {
        FREE, BUSY, UNAVAILABLE
    }

    /**
     * The time column.
     */
    public static final int TIME_COLUMN = 0;

    /**
     * The event grid.
     */
    private final CalendarGrid grid;

    /**
     * Constructs a {@link CalendarTableModel}.
     *
     * @param grid the event grid
     */
    public CalendarTableModel(CalendarGrid grid) {
        this.grid = grid;
        setTableColumnModel(createColumnModel(grid));
    }

    /**
     * Returns the value found at the given coordinate within the table.
     * Column and row values are 0-based.
     * <strong>WARNING: Take note that the column is the first parameter
     * passed to this method, and the row is the second parameter.</strong>
     *
     * @param column the column index (0-based)
     * @param row    the row index (0-based)
     */
    @Override
    public Object getValueAt(int column, int row) {
        TableColumn col = getColumn(column);
        if (col == null) {
            throw new IllegalArgumentException("Illegal column=" + column);
        }
        return getValueAt(col, row);
    }

    /**
     * Returns the value at the specified column and row.
     *
     * @param column the column
     * @param row    the row
     * @return the value
     */
    public Object getValueAt(TableColumn column, int row) {
        Object result;
        int index = column.getModelIndex();
        if (index == TIME_COLUMN) {
            result = grid.getStartTime(row);
        } else {
            result = grid.getEvent(index - 1, row);
        }
        return result;
    }

    /**
     * Returns the number of columns in the table.
     *
     * @return the column count
     */
    @Override
    public int getColumnCount() {
        return grid.getDays();
    }

    /**
     * Returns the number of rows in the table.
     *
     * @return the row count
     */
    @Override
    public int getRowCount() {
        return grid.getSlots();
    }

    public Availability getAvailability(int column, int row) {
        Availability result = Availability.UNAVAILABLE;
        TableColumn col = getColumn(column);
        if (col.getModelIndex() != TIME_COLUMN) {
            PropertySet event = grid.getEvent(col.getModelIndex() - 1, row);
            result = (event != null) ? Availability.BUSY : Availability.FREE;
        }
        return result;
    }

    public PropertySet getEvent(Cell cell) {
        return getEvent(cell.getColumn(), cell.getRow());
    }

    public PropertySet getEvent(int column, int row) {
        return grid.getEvent(column - 1, row);
    }

    public Date getStartDate() {
        return grid.getStartDate();
    }

    /**
     * Returns the no. of columns that an event occupies, from the specified column.
     * <p>
     * If the event begins prior to the column, the remaining columns will be returned.
     *
     * @param event  the event
     * @param column the starting column
     * @return the no. of columns that the event occupies
     */
    public int getColumns(PropertySet event, int column) {
        DateTime endTime = new DateTime(event.getDate(ScheduleEvent.ACT_END_TIME));
        int last = Days.daysBetween(new DateTime(getStartDate()), endTime).getDays();
        return last - column;
    }

    /**
     * Returns the number of rows spanned by an event, starting from the specified cell.
     *
     * @param event  the event
     * @param column the cell column
     * @param row    the cell row
     * @return the number of spanned rows
     */
    public int getRows(PropertySet event, int column, int row) {
        Date columnDate = grid.getDatetime(column - 1, row);
        Date end = event.getDate(ScheduleEvent.ACT_END_TIME);
        int lastSlot;
        if (DateRules.dateEquals(columnDate, end)) {
            DateTime endTime = new DateTime(end);
            int minutes = endTime.getMinuteOfDay();
            int slotSize = grid.getSlotSize();
            lastSlot = minutes / slotSize;
            if (minutes % slotSize != 0 && lastSlot < grid.getSlots()) {
                lastSlot++;
            }
        } else {
            lastSlot = grid.getSlots();
        }
        return lastSlot - row;
    }

    /**
     * Returns the number of rows spanned by a cell.
     *
     * @param column the cell column
     * @param row    the cell row
     * @return the number of spanned rows
     */
    public int getFreeRows(int column, int row) {
        int rowSpan = 1;
        int rows = grid.getSlots();
        int currentHour = grid.getHour(row);
        for (int i = row + 1; i < rows && grid.getHour(i) == currentHour; ++i) {
            if (getEvent(column, i) == null) {
                rowSpan++;
            } else {
                break;
            }
        }
        return rowSpan;
    }

    public Date getDatetime(Cell cell) {
        int slot = cell.getColumn() - 1;
        return grid.getDatetime(slot, cell.getRow());
    }

    /**
     * Creates a column model to display a list of schedules.
     *
     * @param grid the appointment grid
     * @return a new column model
     */
    protected TableColumnModel createColumnModel(CalendarGrid grid) {
        DefaultTableColumnModel result = new DefaultTableColumnModel();
        int index = TIME_COLUMN;

        // first column is the start time slots
        TableColumn leftStartCol = createTimeColumn(index);
        result.addColumn(leftStartCol);

        CalendarTableCellRenderer renderer = new CalendarTableCellRenderer(this);

        // add a column for each day
        Date startDate = grid.getStartDate();
        for (int i = 0; i < grid.getDays(); ++i) {
            TableColumn dateColumn = createDateColumn(++index, DateRules.getDate(startDate, i, DateUnits.DAYS));
            dateColumn.setCellRenderer(renderer);
            result.addColumn(dateColumn);
        }
        return result;
    }

    /**
     * Creates a new column to display the time slots.
     *
     * @param modelIndex the column model index
     * @return a new column
     */
    private TableColumn createTimeColumn(int modelIndex) {
        return TableColumnFactory.create(modelIndex, CalendarHeaderCellRenderer.INSTANCE, new TimeColumnCellRenderer());
    }

    /**
     * Creates a new column to display the date.
     *
     * @param modelIndex the column model index
     * @return a new column
     */
    private TableColumn createDateColumn(int modelIndex, Date date) {
        return TableColumnFactory.create(modelIndex, date, CalendarHeaderCellRenderer.INSTANCE, null);
    }

    private class TimeColumnCellRenderer implements TableCellRendererEx {

        /**
         * Returns a {@code XhtmlFragment} that will be displayed as the
         * content at the specified coordinate in the table.
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
         * This method allows this to be overrriden and only certain cells within a
         * row can cause an action event to be raise.
         *
         * @param table  the Table in question
         * @param column the column in question
         * @param row    the row in quesiton
         * @return true means that the cell can cause actions while false means the cells can not cause action events.
         */
        public boolean isActionCausingCell(Table table, int column, int row) {
            return false;
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
            Date date = (Date) value;
            OffsetDateTime time = OffsetDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
            int minute = time.getMinute();
            Label label;
            if (minute == 0) {
                label = LabelFactory.create(true);
                label.setText(DateFormatter.formatTime(date, false));
                ((LabelEx) label).setHeight(new Extent(50));
            } else {
                label = TableHelper.createSpacer();
            }
            TableLayoutDataEx data = new TableLayoutDataEx();
            data.setAlignment(new Alignment(Alignment.RIGHT, Alignment.TOP));
            data.setRowSpan(4);
            label.setLayoutData(data);
            return label;
        }
    }

}
