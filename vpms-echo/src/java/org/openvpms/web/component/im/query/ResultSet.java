package org.openvpms.web.component.im.query;

import java.util.ListIterator;

import org.openvpms.component.system.common.query.IPage;


/**
 * Paged query result set.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public interface ResultSet<T> extends ListIterator<IPage<T>> {

    /**
     * Reset the iterator.
     */
    void reset();

    /**
     * Sorts the set. This resets the iterator.
     *
     * @param node      the node to sort on
     * @param ascending if <code>true</code> sort the set in ascending order;
     *                  otherwise sort it in <code>descebding</code> order
     */
    void sort(String node, boolean ascending);

    /**
     * Returns the specified page.
     *
     * @param page the page no.
     * @return the page corresponding to <code>page</code>
     */
    IPage<T> getPage(int page);

    /**
     * Returns the total number of pages.
     *
     * @return the total no. of pages.
     */
    int getPages();

    /**
     * Returns the number of rows returned per page.
     *
     * @return the maximum no. of rows returned in each page, or {@link
     *         PagingCriteria#ALL_ROWS} for all rows.
     */
    int getRowsPerPage();

    /**
     * Returns the number of rows.
     *
     * @return the number of rows
     */
    int getRows();

    /**
     * Returns the node the set was sorted on.
     *
     * @return the sort node, or <code>null</code> if the set is unsorted
     */
    String getSortNode();

    /**
     * Determines if the node is sorted ascending or descending.
     *
     * @return <code>true</code> if the node is sorted ascending;
     *         <code>false</code> if it is sorted descending
     */
    boolean isSortedAscending();


}
