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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.echo.table;

import nextapp.echo2.app.table.TableModel;


/**
 * Table model that can be sorted on a column.
 *
 * @author Tim Anderson
 */
public interface SortableTableModel extends TableModel {

    /**
     * Sort the table rows.
     *
     * @param column    the column to sort on
     * @param ascending if {@code true} sort the column ascending order; otherwise sort it in {@code descending} order
     */
    void sort(int column, boolean ascending);

    /**
     * Returns the sort column.
     *
     * @return the sort column, or {@code -1} if no column is sorted.
     */
    int getSortColumn();

    /**
     * Returns the default sort column.
     *
     * @return the default sort column, or {@code -1} if there is no default.
     */
    int getDefaultSortColumn();

    /**
     * Determines if a column is sortable.
     *
     * @param column the column
     * @return {@code true} if the column is sortable; otherwise {@code false}
     */
    boolean isSortable(int column);

    /**
     * Determines if the table is sorted.
     *
     * @return {@code true} if the table is sorted, otherwise false
     */
    boolean isSorted();

    /**
     * Determines if the sort column is sorted ascending or descending.
     *
     * @return {@code true} if the column is sorted ascending; {@code false} if it is sorted descending
     */
    boolean isSortedAscending();

}
