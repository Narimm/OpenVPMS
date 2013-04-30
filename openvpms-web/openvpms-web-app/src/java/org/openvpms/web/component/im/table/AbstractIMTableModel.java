/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.table;

import nextapp.echo2.app.table.AbstractTableModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.web.component.util.TextHelper;
import org.openvpms.web.resource.util.Messages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Abstract implementation of the {@link IMTableModel} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractIMTableModel<T> extends AbstractTableModel
    implements IMTableModel<T> {

    /**
     * The column model.
     */
    private TableColumnModel model;

    /**
     * The objects.
     */
    private List<T> objects = new ArrayList<T>();

    /**
     * Determines if selection should be enabled.
     */
    private boolean enableSelection = true;

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
     * Constructs an <tt>AbstractIMTableModel</tt>.
     */
    public AbstractIMTableModel() {
    }

    /**
     * Creates a new <tt>AbstractIMTableModel</tt>.
     *
     * @param model the column model
     */
    public AbstractIMTableModel(TableColumnModel model) {
        this.model = model;
    }

    /**
     * Returns the number of columns in the table.
     *
     * @return the column count
     */
    public int getColumnCount() {
        return model.getColumnCount();
    }

    /**
     * Returns the name of the specified column number.
     *
     * @param column the column index (0-based)
     * @return the column name
     */
    public String getColumnName(int column) {
        TableColumn col = getColumn(column);
        Object value = col.getHeaderValue();
        return (value != null) ? value.toString() : null;
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
     * @return the object at <tt>row</tt>
     */
    public T getObject(int row) {
        return objects.get(row);
    }

    /**
     * Returns the column model.
     *
     * @return the column model
     */
    public TableColumnModel getColumnModel() {
        return model;
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
        T object = getObject(row);
        TableColumn col = getColumn(column);
        if (col == null) {
            throw new IllegalArgumentException("Illegal column=" + column);
        }
        Object result = getValue(object, col, row);
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
     * Determines if selection should be enabled.
     * <p/>
     * This implementation defaults to <tt>true</tt>.
     *
     * @return <tt>true</tt> if selection should be enabled; otherwise
     *         <tt>false</tt>
     */
    public boolean getEnableSelection() {
        return enableSelection;
    }

    /**
     * Determines if selection should be enabled.
     *
     * @param enable if <tt>true</tt> selection should be enabled; otherwise
     *               it should be disabled
     */
    public void setEnableSelection(boolean enable) {
        enableSelection = enable;
    }

    /**
     * Notfies the table to refresh.
     * <p/>
     * This can be used to refresh the table if properties of objects held by the model have changed.
     */
    public void refresh() {
        fireTableDataChanged();
    }

    /**
     * Sets the column model.
     *
     * @param model the column model
     */
    protected void setTableColumnModel(TableColumnModel model) {
        this.model = model;
        fireTableStructureChanged();
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
     * Returns a column given its model index.
     *
     * @param column the column index
     * @return the column
     */
    protected TableColumn getColumn(int column) {
        TableColumn result = null;
        Iterator iterator = model.getColumns();
        while (iterator.hasNext()) {
            TableColumn col = (TableColumn) iterator.next();
            if (col.getModelIndex() == column) {
                result = col;
                break;
            }
        }
        return result;
    }

    /**
     * Returns a column offset given its model index.
     *
     * @param column the columjn index
     * @return the column offset, or <tt>-1</tt> if a column with the
     *         specified index doesn't exist
     */
    protected int getColumnOffset(int column) {
        return getColumnOffset(model, column);
    }

    /**
     * Returns a column offset given its model index.
     *
     * @param model  the model
     * @param column the column index
     * @return the column offset, or <tt>-1</tt> if a column with the
     *         specified index doesn't exist
     */
    protected int getColumnOffset(TableColumnModel model, int column) {
        int result = -1;
        int offset = 0;
        Iterator iterator = model.getColumns();
        while (iterator.hasNext()) {
            TableColumn col = (TableColumn) iterator.next();
            if (col.getModelIndex() == column) {
                result = offset;
                break;
            }
            ++offset;
        }
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
        TableColumn column = new TableColumn(index);
        String label = Messages.get(headerKey);
        column.setHeaderValue(label);
        return column;
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
        int index = from + 1;
        Iterator iterator = columns.getColumns();
        while (iterator.hasNext()) {
            TableColumn col = (TableColumn) iterator.next();
            if (col.getModelIndex() >= index) {
                index = col.getModelIndex() + 1;
            }
        }
        return index;
    }

}
