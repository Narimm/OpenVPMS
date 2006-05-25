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

import java.util.ListIterator;

import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.SortConstraint;


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
     * Determines if the node is sorted ascending or descending.
     *
     * @return <code>true</code> if the node is sorted ascending;
     *         <code>false</code> if it is sorted descending
     */
    boolean isSortedAscending();

    /**
     * Returns the sort criteria.
     *
     * @return the sort criteria. Never null
     */
    SortConstraint[] getSortConstraints();

}
