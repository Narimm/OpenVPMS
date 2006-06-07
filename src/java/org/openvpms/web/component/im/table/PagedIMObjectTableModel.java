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

import java.util.Collections;
import java.util.List;

import nextapp.echo2.app.event.TableModelEvent;
import nextapp.echo2.app.event.TableModelListener;
import nextapp.echo2.app.table.AbstractTableModel;
import nextapp.echo2.app.table.TableColumnModel;
import nextapp.echo2.app.table.TableModel;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.table.PageableTableModel;
import org.openvpms.web.component.table.SortableTableModel;


/**
 * Enter description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PagedIMObjectTableModel
        extends AbstractTableModel
        implements PageableTableModel, SortableTableModel, IMObjectTableModel {

    /**
     * The result set.
     */
    private ResultSet _set;

    /**
     * The model to delegate to.
     */
    private final IMObjectTableModel _model;

    /**
     * The current page.
     */
    private int _page;

    /**
     * The sort column.
     */
    private int _sortColumn;


    /**
     * Construct a new <code>PagedIMObjectTableModel</code>.
     *
     * @param model the underlying table model.
     */
    public PagedIMObjectTableModel(IMObjectTableModel model) {
        _model = model;
        _model.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent event) {
                notifyListeners(event);
            }
        });
    }

    /**
     * Sets the result set.
     *
     * @param set the result set
     */
    public void setResultSet(ResultSet set) {
        _set = set;
        _sortColumn = -1;
        setPage(0);
    }

    /**
     * Returns the number of columns in the table.
     *
     * @return the column count
     */
    public int getColumnCount() {
        return _model.getColumnCount();
    }

    /**
     * Returns <code>Object.class</code>
     *
     * @see TableModel#getColumnClass(int)
     */
    @Override
    public Class getColumnClass(int column) {
        return _model.getColumnClass(column);
    }

    /**
     * Returns the name of the specified column number.
     *
     * @param column the column index (0-based)
     * @return the column name
     */
    @Override
    public String getColumnName(int column) {
        return _model.getColumnName(column);
    }

    /**
     * Returns the number of rows in the table.
     *
     * @return the row count
     */
    public int getRowCount() {
        return _model.getRowCount();
    }

    /**
     * Sets the current page.
     *
     * @param page the page to set
     */
    public void setPage(int page) {
        List<IMObject> objects = Collections.emptyList();
        IPage<IMObject> result = _set.getPage(page);
        if (result != null) {
            int rows = result.getTotalNumOfRows();
            if (rows > 0) {
                objects = result.getRows();
            }
        }
        _page = page;
        _model.setObjects(objects);
    }

    /**
     * Returns the current page.
     *
     * @return the current page
     */
    public int getPage() {
        return _page;
    }

    /**
     * Returns the total number of pages.
     *
     * @return the total number of pages
     */
    public int getPages() {
        if (_set.getPage(_page) != null) {
            return _set.getPages();
        }
        return 0;
    }

    /**
     * Returns the number of rows per page.
     *
     * @return the number. of rows per page
     */
    public int getRowsPerPage() {
        return _set.getRowsPerPage();
    }

    /**
     * Returns the total number of rows. <em>NOTE: </em> the {@link
     * #getRowCount} method returns the number of visible rows.
     *
     * @return the total number of rows
     */
    public int getRows() {
        if (_set.getPage(_page) != null) {
            return _set.getRows();
        }
        return 0;
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param column the column index (0-based)
     * @param row    the row index (0-based)
     * @return the value at the given coordinate.
     */
    public Object getValueAt(int column, int row) {
        return _model.getValueAt(column, row);
    }

    /**
     * Returns the objects being displayed.
     *
     * @return the objects being displayed
     */
    public List<IMObject> getObjects() {
        return _model.getObjects();
    }

    /**
     * Sets the objects to display.
     *
     * @param objects the objects to display
     */
    public void setObjects(List<IMObject> objects) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the column model.
     *
     * @return the column model
     */
    public TableColumnModel getColumnModel() {
        return _model.getColumnModel();
    }

    /**
     * Sort the table rows.
     *
     * @param column    the column to sort on
     * @param ascending if <code>true</code> sort the column ascending order;
     *                  otherwise sort it in <code>descebding</code> order
     */
    public void sort(int column, boolean ascending) {
        SortConstraint[] criteria = getSortConstraints(column, ascending);
        _sortColumn = column;
        _set.sort(criteria);
        setPage(0);
    }

    /**
     * Returns the sort column.
     *
     * @return the sort column
     */
    public int getSortColumn() {
        return _sortColumn;
    }

    /**
     * Determines if a column is sortable.
     *
     * @param column the column
     * @return <code>true</code> if the column is sortable; otherwise
     *         <code>false</code>
     */
    public boolean isSortable(int column) {
        SortConstraint[] sort = _model.getSortConstraints(column, true);
        return (sort != null);
    }

    /**
     * Determines if the sort column is sorted ascending or descending.
     *
     * @return <code>true</code> if the column is sorted ascending;
     *         <code>false</code> if it is sorted descending
     */
    public boolean isSortedAscending() {
        return _set.isSortedAscending();
    }

    /**
     * Returns the sort criteria.
     *
     * @param column    the primary sort column
     * @param ascending if <code>true</code> sort in ascending order; otherwise
     *                  sort in <code>descending</code> order
     * @return the sort criteria
     */
    public SortConstraint[] getSortConstraints(int column, boolean ascending) {
        return _model.getSortConstraints(column, ascending);
    }

    /**
     * Determines if selection should be enabled.
     *
     * @return <code>true</code> if selection should be enabled; otherwise
     *         <code>false</code>
     */
    public boolean getEnableSelection() {
        return _model.getEnableSelection();
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

}
