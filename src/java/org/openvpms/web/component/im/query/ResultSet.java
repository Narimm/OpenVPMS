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
public interface ResultSet<T> extends ListIterator<IPage<T>> {

    /**
     * Reset the iterator.
     */
    void reset();

    /**
     * Sorts the set. This resets the iterator.
     *
     * @param sort the sort criteria. May be <code>null</code>
     */
    void sort(SortConstraint[] sort);

    /**
     * Returns the specified page.
     *
     * @param page the page no.
     * @return the page corresponding to <code>page</code>.
     *         May be <code>null</code>
     */
    IPage<T> getPage(int page);

    /**
     * Returns the total number of pages.
     *
     * @return the total no. of pages.
     * @throws IllegalStateException if there is no current page
     */
    int getPages();

    /**
     * Returns the number of results returned per page.
     *
     * @return the maximum no. of results returned in each page, or {@link
     *         ArchetypeQuery#ALL_RESULTS} for all results.
     */
    int getPageSize();

    /**
     * Returns the total number of results matching the query criteria.
     *
     * @return the total number of results
     * @throws IllegalStateException if there is no current page
     */
    int getResults();

    /**
     * Determines if the node is sorted ascending or descending.
     *
     * @return <code>true</code> if the node is sorted ascending or no sort
     *         constraint was specified; <code>false</code> if it is sorted
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
     * @return <code>true</code> if duplicate results should be removed;
     *         otherwise <code>false</code>
     */
    boolean isDistinct();

    /**
     * Sets the nodes to query.
     *
     * @param nodes the nodes to query
     */
    void setNodes(String[] nodes);
}
