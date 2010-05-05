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

import nextapp.echo2.app.event.TableModelEvent;
import nextapp.echo2.app.event.TableModelListener;
import nextapp.echo2.app.table.AbstractTableModel;
import nextapp.echo2.app.table.TableColumnModel;
import nextapp.echo2.app.table.TableModel;
import org.openvpms.component.system.common.query.SortConstraint;

import java.util.List;


/**
 * An <tt>IMTableModel</tt> that delegates to another.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DelegatingIMTableModel<T, K> extends AbstractTableModel
        implements IMTableModel<T> {

    /**
     * The model to delegate to.
     */
    private IMTableModel<K> model;

    /**
     * Constructs a new <tt>DelegatingIMTableModel</tt>.
     */
    public DelegatingIMTableModel() {
        this(null);
    }

    /**
     * Constructs a new <tt>DelegatingIMTableModel</tt>.
     *
     * @param model the model
     */
    public DelegatingIMTableModel(IMTableModel<K> model) {
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
     * Returns the most-specific class of objects found in a given table
     * column.  Every object in the specified column must be an instance
     * of the returned class.
     *
     * @see TableModel#getColumnClass(int)
     */
    @Override
    public Class getColumnClass(int column) {
        return model.getColumnClass(column);
    }

    /**
     * Returns the name of the specified column number.
     *
     * @param column the column index (0-based)
     * @return the column name
     */
    @Override
    public String getColumnName(int column) {
        return model.getColumnName(column);
    }

    /**
     * Returns the number of rows in the table.
     *
     * @return the row count
     */
    public int getRowCount() {
        return model.getRowCount();
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
    public Object getValueAt(int column, int row) {
        return model.getValueAt(column, row);
    }

    /**
     * @see TableModel#addTableModelListener(TableModelListener)
     */
    public void addTableModelListener(TableModelListener l) {
        model.addTableModelListener(l);
    }

    /**
     * @see TableModel#removeTableModelListener(TableModelListener)
     */
    public void removeTableModelListener(TableModelListener l) {
        model.removeTableModelListener(l);
    }

    /**
     * Returns the objects being displayed.
     *
     * @return the objects being displayed
     */
    public List<T> getObjects() {
        return convertFrom(model.getObjects());
    }

    /**
     * Sets the objects to display.
     *
     * @param objects the objects to display
     */
    public void setObjects(List<T> objects) {
        model.setObjects(convertTo(objects));
    }

    /**
     * Returns the column model.
     *
     * @return the column model
     */
    public TableColumnModel getColumnModel() {
        return model.getColumnModel();
    }

    /**
     * Returns the sort criteria.
     *
     * @param column    the primary sort column
     * @param ascending if <tt>true</tt> sort in ascending order; otherwise
     *                  sort in <tt>descending</tt> order
     * @return the sort criteria, or <tt>null</tt> if the column isn't
     *         sortable
     */
    public SortConstraint[] getSortConstraints(int column, boolean ascending) {
        return model.getSortConstraints(column, ascending);
    }

    /**
     * Determines if selection should be enabled.
     *
     * @return <tt>true</tt> if selection should be enabled; otherwise
     *         <tt>false</tt>
     */
    public boolean getEnableSelection() {
        return model.getEnableSelection();
    }

    /**
     * Determines if selection should be enabled.
     *
     * @param enable if <tt>true</tt> selection should be enabled; otherwise
     *               it should be disabled
     */
    public void setEnableSelection(boolean enable) {
        model.setEnableSelection(enable);
    }

    /**
     * Notfies the table to refresh.
     * <p/>
     * This can be used to refresh the table if properties of objects held by the model have changed.
     */
    public void refresh() {
        model.refresh();
    }

    /**
     * Sets the model to delegate to.
     *
     * @param model the model to delegate to
     */
    protected void setModel(IMTableModel<K> model) {
        this.model = model;
        this.model.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent event) {
                notifyListeners(event);
            }
        });
    }

    /**
     * Returns the model to delegate to.
     *
     * @return the model
     */
    protected IMTableModel<K> getModel() {
        return model;
    }

    /**
     * Notify listeners of an update to the underlying table.
     *
     * @param event the event
     */
    protected void notifyListeners(TableModelEvent event) {
        if (event.getType() == TableModelEvent.STRUCTURE_CHANGED) {
            fireTableStructureChanged();
        } else {
            fireTableDataChanged();
        }
    }

    /**
     * Converts to the delegate type. This implementation does a simple cast.
     *
     * @param list the list to convert
     * @return the converted list
     */
    @SuppressWarnings("unchecked")
    protected List<K> convertTo(List<T> list) {
        return (List<K>) list;
    }

    /**
     * Converts from the delegate type. This implementation does a simple cast.
     *
     * @param list the list to convert
     * @return the converted list
     */
    @SuppressWarnings("unchecked")
    protected List<T> convertFrom(List<K> list) {
        return (List<T>) list;
    }
}
