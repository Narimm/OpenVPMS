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

package org.openvpms.web.component.im.table;

import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.table.TableCellRenderer;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.echo.table.AbstractTableModel;
import org.openvpms.web.echo.table.TableColumnFactory;
import org.openvpms.web.echo.table.TableHelper;
import org.openvpms.web.echo.text.TextHelper;

import java.util.ArrayList;
import java.util.List;


/**
 * Abstract implementation of the {@link IMTableModel} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractIMTableModel<T> extends AbstractTableModel implements IMTableModel<T> {

    /**
     * ID column localisation key.
     */
    protected static final String ID = "table.imobject.id";

    /**
     * Archetype column localisation key.
     */
    protected static final String ARCHETYPE = "table.imobject.archetype";

    /**
     * Name column localisation key.
     */
    protected static final String NAME = "table.imobject.name";

    /**
     * Description column localisation key.
     */
    protected static final String DESCRIPTION = "table.imobject.description";

    /**
     * Active column localisation key.
     */
    protected static final String ACTIVE = "table.imobject.active";

    /**
     * The objects.
     */
    private List<T> objects = new ArrayList<>();

    /**
     * The default sort column, or {@code -1} if there is no default.
     */
    private int defaultSortColumn = -1;

    /**
     * Determines if the default sort column should sort ascending or descending.
     */
    private boolean defaultSortAscending = true;

    /**
     * Determines if selection should be enabled.
     */
    private boolean enableSelection = true;

    /**
     * Tracks row marking.
     */
    private ListMarkModel rowMarkModel;

    /**
     * Listener for row mark events.
     */
    private ListMarkModel.Listener rowMarkListener;

    /**
     * Constructs an {@link AbstractIMTableModel}.
     */
    public AbstractIMTableModel() {
    }

    /**
     * Constructs an {@link AbstractIMTableModel}.
     *
     * @param model the column model
     */
    public AbstractIMTableModel(TableColumnModel model) {
        super(model);
    }

    /**
     * Returns the number of rows in the table.
     *
     * @return the row count
     */
    public int getRowCount() {
        return objects.size();
    }

    /**
     * Sets the objects to display.
     *
     * @param objects the objects to display
     */
    public void setObjects(List<T> objects) {
        this.objects = objects;
        if (rowMarkModel != null) {
            rowMarkModel.clear();
        }
        fireTableDataChanged();
    }

    /**
     * Returns the objects being displayed.
     *
     * @return the objects being displayed
     */
    public List<T> getObjects() {
        return objects;
    }

    /**
     * Return the object at the given row.
     *
     * @param row the row
     * @return the object at {@code row}
     */
    public T getObject(int row) {
        return objects.get(row);
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
     * Returns the value at the specified column and row.
     *
     * @param column the column
     * @param row    the row
     * @return the value
     */
    public Object getValueAt(TableColumn column, int row) {
        T object = getObject(row);
        Object result = getValue(object, column, row);
        if (result instanceof String) {
            String str = (String) result;
            if (TextHelper.hasControlChars(str)) {
                // replace any control chars with spaces.
                str = TextHelper.replaceControlChars(str, " ");
            }
            result = str;
        }
        return result;
    }

    /**
     * Returns the default sort column.
     *
     * @return the default sort column, or {@code -1} if there is no default.
     */
    @Override
    public int getDefaultSortColumn() {
        return defaultSortColumn;
    }

    /**
     * Sets the default sort column.
     *
     * @param column the column, or {@code -1} if there is no default
     */
    public void setDefaultSortColumn(int column) {
        this.defaultSortColumn = column;
    }

    /**
     * Determines if the default sort column should sort ascending or descending.
     *
     * @return {@code true} if it should sort ascending, {@code false}
     */
    @Override
    public boolean getDefaultSortAscending() {
        return defaultSortAscending;
    }

    public void setDefaultSortAscending(boolean ascending) {
        defaultSortAscending = ascending;
    }

    /**
     * Determines if selection should be enabled.
     * <p>
     * This implementation defaults to {@code true}.
     *
     * @return {@code true} if selection should be enabled; otherwise {@code false}
     */
    public boolean getEnableSelection() {
        return enableSelection;
    }

    /**
     * Determines if selection should be enabled.
     *
     * @param enable if {@code true} selection should be enabled; otherwise
     *               it should be disabled
     */
    public void setEnableSelection(boolean enable) {
        enableSelection = enable;
    }

    /**
     * Sets the model to track row marking.
     *
     * @param model the model
     */
    @Override
    public void setRowMarkModel(ListMarkModel model) {
        if (rowMarkListener == null) {
            rowMarkListener = new ListMarkModel.Listener() {
                @Override
                public void changed(int index, boolean marked) {
                    markRow(index, marked);
                }

                @Override
                public void cleared() {
                    unmarkRows();
                }
            };
        } else if (rowMarkModel != null) {
            rowMarkModel.removeListener(rowMarkListener);
        }
        rowMarkModel = model;
        if (rowMarkModel != null) {
            rowMarkModel.addListener(rowMarkListener);
        }
    }

    /**
     * Returns the model to track row marking.
     *
     * @return the model, or {@code null} if none is registered
     */
    @Override
    public ListMarkModel getRowMarkModel() {
        return rowMarkModel;
    }

    /**
     * Returns the objects associated with the marked rows.
     *
     * @return the objects
     */
    @Override
    public List<T> getMarkedRows() {
        List<T> result = new ArrayList<>();
        if (rowMarkModel != null && !rowMarkModel.isEmpty()) {
            for (int i = 0; i < getRowCount(); ++i) {
                if (rowMarkModel.isMarked(i)) {
                    result.add(objects.get(i));
                }
            }
        }
        return result;
    }

    /**
     * Notifies the table to refresh.
     * <p>
     * This can be used to refresh the table if properties of objects held by the model have changed.
     */
    public void refresh() {
        fireTableDataChanged();
    }

    /**
     * Invoked prior to the table being rendered.
     */
    @Override
    public void preRender() {
    }

    /**
     * Invoked after the table has been rendered.
     */
    @Override
    public void postRender() {
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object
     * @param column the column
     * @param row    the row
     * @return the value at the given coordinate.
     */
    protected abstract Object getValue(T object, TableColumn column, int row);

    /**
     * Invoked when a row is marked or unmarked.
     * <p/>
     * This implementation is a no-op.
     *
     * @param row    the row
     * @param marked if {@code true}, row has been marked, otherwise it has been unmarked
     */
    protected void markRow(int row, boolean marked) {
    }

    /**
     * Invoked when all rows are unmarked.
     * <p/>
     * This implementation is a no-op.
     */
    protected void unmarkRows() {
    }

    /**
     * Returns a column offset given its model index.
     *
     * @param model  the model
     * @param column the column index
     * @return the column offset, or {@code -1} if a column with the
     * specified index doesn't exist
     */
    protected int getColumnOffset(TableColumnModel model, int column) {
        return TableHelper.getColumnOffset(model, column);
    }

    /**
     * Adds a column after the specified column.
     *
     * @param column the column to add
     * @param after  the column model index to add after
     * @param model  the model
     */
    protected void addColumnAfter(TableColumn column, int after, TableColumnModel model) {
        model.addColumn(column);
        int columnOffset = getColumnOffset(model, after);
        if (columnOffset != -1) {
            model.moveColumn(model.getColumnCount() - 1, columnOffset + 1);
        }
    }

    /**
     * Helper to determine the next available model index.
     *
     * @param columns the columns
     * @return the next available model index.
     */
    protected int getNextModelIndex(TableColumnModel columns) {
        return getNextModelIndex(columns, 0);
    }

    /**
     * Helper to determine the next available model index.
     *
     * @param columns the columns
     * @param from    the index to start searching from
     * @return the next available model index.
     */
    protected int getNextModelIndex(TableColumnModel columns, int from) {
        return TableHelper.getNextModelIndex(columns, from);
    }

    /**
     * Returns a checkbox indicating the active state of an object.
     *
     * @param object the object
     * @return a new checkbox
     */
    protected CheckBox getActive(IMObject object) {
        return getCheckBox(object.isActive());
    }

    /**
     * Helper to create a read-only checkbox
     *
     * @param selected if {@code true}, selects the check-box
     * @return the checkbox
     */
    protected CheckBox getCheckBox(boolean selected) {
        CheckBox result = new CheckBox();
        result.setEnabled(false);
        result.setSelected(selected);
        return result;
    }

    /**
     * Helper to create a table column.
     *
     * @param index     the column index
     * @param headerKey the header label resource key
     * @return a new table column
     */
    protected static TableColumn createTableColumn(int index, String headerKey) {
        return TableColumnFactory.createKey(index, headerKey);
    }

    /**
     * Helper to create a table column.
     *
     * @param index     the column index
     * @param headerKey the header label resource key
     * @param renderer  the cell renderer
     * @return a new table column
     */
    protected static TableColumn createTableColumn(int index, String headerKey, TableCellRenderer renderer) {
        return TableColumnFactory.createKey(index, headerKey, null, renderer);
    }

}
