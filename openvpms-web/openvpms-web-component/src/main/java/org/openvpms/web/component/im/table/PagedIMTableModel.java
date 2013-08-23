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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.table;

import nextapp.echo2.app.table.TableColumn;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.echo.table.PageableTableModel;
import org.openvpms.web.echo.table.SortableTableModel;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * Paged table model.
 *
 * @author Tim Anderson
 */
public class PagedIMTableModel<T, K> extends DelegatingIMTableModel<T, K>
        implements PageableTableModel, SortableTableModel {

    /**
     * The result set.
     */
    private ResultSet<T> set;

    /**
     * The current page.
     */
    private int page;

    /**
     * The sort column.
     */
    private int sortColumn;

    /**
     * The default sort column, or <tt>-1</tt> if no column is sortable.
     */
    private int defaultSortColumn;


    /**
     * Creates a new <tt>PagedIMTableModel</tt>.
     *
     * @param model the underlying model
     */
    public PagedIMTableModel(IMTableModel<K> model) {
        super(model);
        Iterator iter = model.getColumnModel().getColumns();
        while (iter.hasNext()) {
            TableColumn column = (TableColumn) iter.next();
            if (isSortable(column.getModelIndex())) {
                defaultSortColumn = column.getModelIndex();
                break;
            }
        }
    }

    /**
     * Sets the result set.
     *
     * @param set the result set
     */
    public void setResultSet(ResultSet<T> set) {
        this.set = set;
        sortColumn = -1;
        SortConstraint[] sorted = set.getSortConstraints();
        Iterator iter = getModel().getColumnModel().getColumns();
        if (sorted.length != 0) {
            while (iter.hasNext()) {
                TableColumn column = (TableColumn) iter.next();
                SortConstraint[] columnConstraint = getModel().getSortConstraints(column.getModelIndex(),
                                                                                  set.isSortedAscending());
                boolean match = true;
                if (columnConstraint != null && sorted.length == columnConstraint.length) {
                    for (int i = 0; i < sorted.length; ++i) {
                        if (!sorted[i].equals(columnConstraint[i])) {
                            match = false;
                            break;
                        }
                    }
                } else {
                    match = false;
                }
                if (match) {
                    sortColumn = column.getModelIndex();
                    break;
                }
            }
        }
        if (!setPage(0)) {
            // no pages.
            page = 0;
            List<T> objects = Collections.emptyList();
            setPage(objects);
        }
    }

    /**
     * Returns the result set.
     *
     * @return the result set, or <tt>null</tt> if it hasn't been set.
     */
    public ResultSet<T> getResultSet() {
        return set;
    }

    /**
     * Determines if a page exists.
     *
     * @param page the page
     * @return <tt>true</tt> if the page exists, otherwise <tt>false</tt>
     */
    public boolean hasPage(int page) {
        return set.getPage(page) != null;
    }

    /**
     * Attempts to set the current page.
     *
     * @param page the page to set
     * @return <tt>true</tt> if the page was set, or <tt>false</tt> if there
     *         is no such page
     */
    public boolean setPage(int page) {
        IPage<T> result = set.getPage(page);
        if (result != null) {
            this.page = page;
            setPage(result.getResults());
            return true;
        }
        return false;
    }

    /**
     * Returns the current page.
     *
     * @return the current page
     */
    public int getPage() {
        return page;
    }

    /**
     * Returns the total number of pages.
     * For complex queries, this operation can be expensive. If an exact
     * count is not required, use {@link #getEstimatedPages()}.
     *
     * @return the total no. of pages.
     */
    public int getPages() {
        return set.getPages();
    }

    /**
     * Returns an estimation of the total no. of pages.
     *
     * @return an estimation of the total no. of pages
     */
    public int getEstimatedPages() {
        return set.getEstimatedPages();
    }

    /**
     * Determines if the estimated no. of results is the actual total, i.e
     * if {@link #getEstimatedPages()} would return the same as
     * {@link #getPages()}.
     *
     * @return <tt>true</tt> if the estimated pages equals the actual no.
     *         of pages
     */
    public boolean isEstimatedActual() {
        return set.isEstimatedActual();
    }

    /**
     * Returns the number of rows per page.
     *
     * @return the number. of rows per page
     */
    public int getRowsPerPage() {
        return set.getPageSize();
    }

    /**
     * Returns the total number of rows. <em>NOTE: </em> the {@link
     * #getRowCount} method returns the number of visible rows.
     *
     * @return the total number of rows
     */
    public int getResults() {
        return set.getResults();
    }

    /**
     * Returns the total number of results matching the query criteria.
     *
     * @param force if <tt>true</tt>, force a calculation of the total no. of
     *              results
     * @return the total no. of results, or <tt>-1</tt> if the no. isn't known
     */
    public int getResults(boolean force) {
        return set.getEstimatedResults();
    }

    /**
     * Sets the objects to display.
     *
     * @param objects the objects to display
     */
    public void setObjects(List<T> objects) {
        throw new UnsupportedOperationException();
    }

    /**
     * Sort the table rows.
     *
     * @param column    the column to sort on
     * @param ascending if <tt>true</tt> sort the column ascending order;
     *                  otherwise sort it in <tt>descending</tt> order
     */
    public void sort(int column, boolean ascending) {
        SortConstraint[] criteria = getSortConstraints(column, ascending);
        sortColumn = column;
        set.sort(criteria);
        setPage(0);
    }

    /**
     * Returns the sort column.
     *
     * @return the sort column
     */
    public int getSortColumn() {
        return sortColumn;
    }

    /**
     * Returns the default sort column.
     *
     * @return the default sort column, or <tt>-1</tt> if no column is sortable.
     */
    public int getDefaultSortColumn() {
        return defaultSortColumn;
    }

    /**
     * Determines if a column is sortable.
     *
     * @param column the column
     * @return <tt>true</tt> if the column is sortable; otherwise
     *         <tt>false</tt>
     */
    public boolean isSortable(int column) {
        SortConstraint[] sort = getModel().getSortConstraints(column, true);
        return (sort != null && sort.length != 0);
    }

    /**
     * Determines if the table is sorted.
     *
     * @return <tt>true</tt> if the table is sorted, otherwise false
     */
    public boolean isSorted() {
        return set.getSortConstraints().length != 0;
    }

    /**
     * Determines if the sort column is sorted ascending or descending.
     *
     * @return <tt>true</tt> if the column is sorted ascending;
     *         <tt>false</tt> if it is sorted descending
     */
    public boolean isSortedAscending() {
        return set.isSortedAscending();
    }

    /**
     * Sets the objects for the current page.
     *
     * @param objects the objects to set
     */
    protected void setPage(List<T> objects) {
        getModel().setObjects(convertTo(objects));
    }
}
