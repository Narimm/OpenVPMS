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
    protected TableColumnModel model;

    /**
     * The objects.
     */
    private List<T> objects = new ArrayList<T>();


    /**
     * Returns the number of columns in the table.
     *
     * @return the column count
     */
    public int getColumnCount() {
        return model.getColumnCount();
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
     * @return the object at <code>row</code>
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
        return getValue(object, col, row);
    }

    /**
     * Determines if selection should be enabled.
     *
     * @return <code>true</code> if selection should be enabled; otherwise
     *         <code>false</code>
     */
    public boolean getEnableSelection() {
        return true;
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
     * @return the column offset, or <code>-1</code> if a column with the
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
     * @return the column offset, or <code>-1</code> if a column with the
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
}
