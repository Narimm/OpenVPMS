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

import nextapp.echo2.app.Extent;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.table.AbstractCellTableModel;
import org.openvpms.web.echo.table.Cell;
import org.openvpms.web.echo.table.StyleTableCellRenderer;
import org.openvpms.web.echo.table.TableColumnFactory;
import org.openvpms.web.workspace.admin.calendar.CalendarHeaderCellRenderer;

import java.util.Date;

/**
 * Roster table model.
 *
 * @author Tim Anderson
 */
abstract class RosterTableModel extends AbstractCellTableModel {

    /**
     * Name column index.
     */
    public static final int NAME_INDEX = 0;

    /**
     * The event grid.
     */
    private final RosterEventGrid grid;

    /**
     * The first date.
     */
    private Date date;

    /**
     * First day column index.
     */
    private static final int FIRST_DAY_INDEX = NAME_INDEX + 1;

    /**
     * Renderer for the name column header.
     */
    private static final StyleTableCellRenderer NAME_HEADER_RENDERER = new StyleTableCellRenderer(Styles.H3);

    /**
     * Renderer for the name column cells.
     */
    private static final StyleTableCellRenderer NAME_CELL_RENDERER = new StyleTableCellRenderer(Styles.BOLD);

    /**
     * Constructs a {@link RosterTableModel}.
     *
     * @param grid the event grid
     */
    RosterTableModel(RosterEventGrid grid, String nameKey, String entityKey, Context context) {
        this.grid = grid;
        setTableColumnModel(createColumnModel(nameKey, entityKey, context));
        setStartDate(grid.getStartDate());
    }

    /**
     * Returns the start date.
     *
     * @return the start date
     */
    public Date getStartDate() {
        return date;
    }

    /**
     * Returns the number of rows.
     *
     * @return the number of rows
     */
    @Override
    public int getRowCount() {
        return getGrid().getRows();
    }

    /**
     * Returns the value found at the given coordinate within the table. Column
     * and row values are 0-based. <strong>WARNING: Take note that the column is
     * the first parameter passed to this method, and the row is the second
     * parameter.</strong>
     *
     * @param column the column index (0-based)
     * @param row    the row index (0-based)
     */
    public Object getValueAt(int column, int row) {
        TableColumn col = getColumn(column);
        if (col == null) {
            throw new IllegalArgumentException("Illegal column=" + column);
        }
        return getValueAt(col, row);
    }

    /**
     * Returns the event at the specified cell.
     *
     * @param cell the cell
     * @return the corresponding event. May be {@code null}
     */
    public PropertySet getEvent(Cell cell) {
        PropertySet result = null;
        int column = cell.getColumn();
        if (column >= FIRST_DAY_INDEX) {
            int slot = column - FIRST_DAY_INDEX;
            result = grid.getEvent(slot, cell.getRow());
        }
        return result;
    }

    /**
     * Returns the cell that the specified event appears in.
     *
     * @param event the event
     * @return the corresponding cell. May be {@code null}
     */
    public abstract Cell getCell(PropertySet event);

    /**
     * Returns the selected entity.
     *
     * @return the selected entity. May be {@code null}
     */
    public Entity getSelectedEntity() {
        Cell cell = getSelected();
        return (cell != null && cell.getColumn() >= FIRST_DAY_INDEX) ? grid.getEntity(cell.getRow()) : null;
    }

    /**
     * Returns the selected date.
     *
     * @return the selected date. May be {@code null}
     */
    public Date getSelectedDate() {
        Cell cell = getSelected();
        return (cell != null && cell.getColumn() >= FIRST_DAY_INDEX)
               ? DateRules.getDate(date, cell.getColumn() - FIRST_DAY_INDEX, DateUnits.DAYS) : null;
    }

    /**
     * Returns the date at the specified column (0-based).
     *
     * @param column the column
     * @return the date
     */
    public Date getDate(int column) {
        int day = getColumn(column).getModelIndex() - FIRST_DAY_INDEX;
        return DateRules.getDate(date, day, DateUnits.DAYS);
    }

    /**
     * Creates a new table column model.
     *
     * @param nameKey   the localisation key for the name column
     * @param entityKey the key to extract entity names from events
     * @param context   the context
     * @return a new table model
     */
    protected TableColumnModel createColumnModel(String nameKey, String entityKey, Context context) {
        DefaultTableColumnModel model = new DefaultTableColumnModel();

        int days = grid.getColumns();
        TableColumn name = TableColumnFactory.createKey(NAME_INDEX, nameKey, NAME_HEADER_RENDERER, NAME_CELL_RENDERER);
        name.setWidth(new Extent(18, Extent.PERCENT));
        Extent width = new Extent(100 - 18 / days);
        model.addColumn(name);
        RosterTableCellRenderer renderer = new RosterTableCellRenderer(entityKey, this, context);

        for (int i = 0; i < days; ++i) {
            TableColumn column = TableColumnFactory.create(FIRST_DAY_INDEX + i, CalendarHeaderCellRenderer.INSTANCE,
                                                           renderer);
            column.setWidth(width);
            model.addColumn(column);
        }
        return model;
    }

    /**
     * Returns the cell of an event, given its entity reference and event reference.
     *
     * @param entity the entity reference
     * @param event  the event reference
     * @return the cell, or {@code null} if none is found
     */
    protected Cell getCell(Reference entity, Reference event) {
        Cell result = null;
        int row = grid.findEntity(entity);
        while (row != -1) {
            int column = grid.findEvent(row, event);
            if (column == -1) {
                row = grid.findEntity(row + 1, entity);
            } else {
                result = new Cell(column + FIRST_DAY_INDEX, row);
                break;
            }
        }
        return result;
    }

    /**
     * Returns the event grid.
     *
     * @return the event grid
     */
    protected RosterEventGrid getGrid() {
        return grid;
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param column the column
     * @param row    the row
     * @return the value at the given coordinate
     */
    protected Object getValueAt(TableColumn column, int row) {
        Object result;
        if (column.getModelIndex() == NAME_INDEX) {
            result = getEntity(row);
        } else {
            int slot = column.getModelIndex() - FIRST_DAY_INDEX;
            result = grid.getEvent(slot, row);
        }
        return result;
    }

    /**
     * Returns the name of the entity at the specified row.
     *
     * @param row the row
     * @return the entity name, or {@code null} if it would duplicate the previous row
     */
    protected String getEntity(int row) {
        String result = null;
        RosterEventGrid grid = getGrid();
        Entity entity = grid.getEntity(row);
        if (row == 0 || !entity.equals(grid.getEntity(row - 1))) {
            result = entity.getName();
        }
        return result;
    }

    /**
     * Sets the start date.
     *
     * @param date the start date
     */
    private void setStartDate(Date date) {
        if (date == null || !DateRules.dateEquals(date, this.date)) {
            this.date = date;
            DefaultTableColumnModel model = (DefaultTableColumnModel) getColumnModel();
            for (int i = 0; i < grid.getColumns(); ++i) {
                int index = FIRST_DAY_INDEX + i;
                TableColumn column = model.getColumn(index);
                column.setHeaderValue(getDate(index));
            }
            fireTableDataChanged();
        }
    }

}