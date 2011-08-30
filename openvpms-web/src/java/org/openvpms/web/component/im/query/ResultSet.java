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

package org.openvpms.web.component.im.query;

import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.SortConstraint;

import java.util.ListIterator;


/**
 * Paged query result set.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public interface ResultSet<T> extends ListIterator<IPage<T>>, Cloneable {

    /**
     * Reset the iterator.
     */
    void reset();

    /**
     * Sorts the set. This resets the iterator.
     *
     * @param sort the sort criteria. May be <tt>null</tt>
     */
    void sort(SortConstraint[] sort);

    /**
     * Determines if a page exists.
     *
     * @param page the page no.
     * @return <tt>true</tt> if the page exists
     */
    boolean hasPage(int page);

    /**
     * Returns the specified page.
     * <p/>
     * This moves the cursor.
     *
     * @param page the page no.
     * @return the page corresponding to <tt>page</tt>. May be <tt>null</tt>
     */
    IPage<T> getPage(int page);

    /**
     * Returns the total number of pages.
     * For complex queries, this operation can be expensive. If an exact
     * count is not required, use {@link #getEstimatedPages()}.
     *
     * @return the total no. of pages.
     */
    int getPages();

    /**
     * Returns an estimation of the total no. of pages.
     *
     * @return an estimation of the total no. of pages
     */
    int getEstimatedPages();

    /**
     * Returns the number of results returned per page.
     *
     * @return the maximum no. of results returned in each page, or {@link
     *         org.openvpms.component.system.common.query.ArchetypeQuery#ALL_RESULTS} for all results.
     */
    int getPageSize();

    /**
     * Returns the total number of results matching the query criteria.
     * For complex queries, this operation can be expensive. If an exact
     * count is not required, use {@link #getEstimatedResults()}.
     *
     * @return the total number of results
     */
    int getResults();

    /**
     * Returns an estimation of the total no. of results matching the query
     * criteria.
     *
     * @return an estimation of the total no. of results
     */
    int getEstimatedResults();

    /**
     * Determines if the estimated no. of results is the actual total, i.e
     * if {@link #getEstimatedResults()} would return the same as
     * {@link #getResults()}, and {@link #getEstimatedPages()} would return
     * the same as {@link #getPages()}.
     *
     * @return <tt>true</tt> if the estimated results equals the actual no.
     *         of results
     */
    boolean isEstimatedActual();

    /**
     * Determines if the node is sorted ascending or descending.
     *
     * @return <tt>true</tt> if the node is sorted ascending or no sort
     *         constraint was specified; <tt>false</tt> if it is sorted
     *         descending
     */
    boolean isSortedAscending();

    /**
     * Returns the sort criteria.
     *
     * @return the sort criteria. Never null
     */
    SortConstraint[] getSortConstraints();

    /**
     * Determines if duplicate results should be filtered.
     *
     * @param distinct if true, remove duplicate results
     */
    void setDistinct(boolean distinct);

    /**
     * Determines if duplicate results should be filtered.
     *
     * @return <tt>true</tt> if duplicate results should be removed;
     *         otherwise <tt>false</tt>
     */
    boolean isDistinct();

    /**
     * Sets the nodes to query.
     *
     * @param nodes the nodes to query
     */
    void setNodes(String[] nodes);

    /**
     * Returns the index of the last returned page.
     *
     * @return the index of the last returned page, or <tt>-1</tt> if no page has been returned
     */
    int lastIndex();

    /**
     * Clones this result set.
     * <p/>
     * This copies the state of iterators.
     *
     * @return a clone of this
     * @throws CloneNotSupportedException if the set cannot be cloned
     */
    ResultSet<T> clone() throws CloneNotSupportedException;
}
