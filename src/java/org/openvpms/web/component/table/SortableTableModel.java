package org.openvpms.web.component.table;

import nextapp.echo2.app.table.TableModel;


/**
 * Table model that can be sorted on a column.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public interface SortableTableModel extends TableModel {

    /**
     * Sort the table rows.
     *
     * @param column    the column to sort on
     * @param ascending if <code>true</code> sort the column ascending order;
     *                  otherwise sort it in <code>descebding</code> order
     */
    void sort(int column, boolean ascending);

    /**
     * Returns the sort column.
     *
     * @return the sort column, or <code>-1</code> if no column is sorted.
     */
    int getSortColumn();

    /**
     * Determines if the sort column is sorted ascending or descending.
     *
     * @return <code>true</code> if the column is sorted ascending;
     * <code>false</code> if it is sorted descending
     */
    boolean isSortedAscending();

}
