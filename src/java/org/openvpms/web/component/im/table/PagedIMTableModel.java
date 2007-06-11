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

import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.table.PageableTableModel;
import org.openvpms.web.component.table.SortableTableModel;

import java.util.Collections;
import java.util.List;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PagedIMTableModel<T> extends DelegatingIMTableModel<T, T>
        implements PageableTableModel, SortableTableModel {
    /**
     * The result set.
     */
    private ResultSet<T> _set;
    /**
     * The current page.
     */
    private int _page;
    /**
     * The sort column.
     */
    private int _sortColumn;


    public PagedIMTableModel(IMTableModel<T> model) {
        super(model);
    }

    /**
     * Sets the result set.
     *
     * @param set the result set
     */
    public void setResultSet(ResultSet<T> set) {
        _set = set;
        _sortColumn = -1;
        setPage(0);
    }

    /**
     * Sets the current page.
     *
     * @param page the page to set
     */
    public void setPage(int page) {
        List<T> objects = Collections.emptyList();
        IPage<T> result = _set.getPage(page);
        if (result != null) {
            int rows = result.getTotalResults();
            if (rows > 0) {
                objects = result.getResults();
            }
        }
        _page = page;
        setPage(objects);
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
        return _set.getPageSize();
    }

    /**
     * Returns the total number of rows. <em>NOTE: </em> the {@link
     * #getRowCount} method returns the number of visible rows.
     *
     * @return the total number of rows
     */
    public int getResults() {
        if (_set.getPage(_page) != null) {
            return _set.getResults();
        }
        return 0;
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
        SortConstraint[] sort = getModel().getSortConstraints(column, true);
        return (sort != null && sort.length != 0);
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
     * Sets the objects for the current page.
     *
     * @param objects the objects to set
     */
    protected void setPage(List<T> objects) {
        getModel().setObjects(objects);
    }
}
