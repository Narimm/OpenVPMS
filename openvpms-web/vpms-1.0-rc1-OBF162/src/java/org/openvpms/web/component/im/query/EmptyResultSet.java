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

import org.openvpms.component.business.dao.im.Page;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.SortConstraint;

import java.util.Collections;
import java.util.List;


/**
 * Empty result set.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-11-27 05:30:20Z $
 */
public class EmptyResultSet<T> extends AbstractResultSet<T> {

    /**
     * The sort criteria.
     */
    private SortConstraint[] sort = new SortConstraint[0];

    /**
     * Determines if the set is sorted ascending or descending.
     */
    private boolean sortAscending = true;

    /**
     * The results.
     */
    private static List RESULTS = Collections.emptyList();


    /**
     * Construct a new <code>EmptyResultSet</code>.
     *
     * @param pageSize the maximum no. of results per page
     */
    public EmptyResultSet(int pageSize) {
        super(pageSize);
        reset();
    }

    /**
     * Sorts the set. This resets the iterator.
     *
     * @param sort the sort criteria. May be <code>null</code>
     */
    public void sort(SortConstraint[] sort) {
        reset();
    }

    /**
     * Determines if the node is sorted ascending or descending.
     *
     * @return <code>true</code> if the node is sorted ascending or no sort
     *         constraint was specified; <code>false</code> if it is sorted
     *         descending
     */
    public boolean isSortedAscending() {
        return sortAscending;
    }

    /**
     * Returns the sort criteria.
     *
     * @return the sort criteria. Never null
     */
    public SortConstraint[] getSortConstraints() {
        return sort;
    }

    /**
     * Determines if duplicate results should be filtered.
     *
     * @param distinct if true, remove duplicate results
     */
    public void setDistinct(boolean distinct) {
        // no-op
    }

    /**
     * Determines if duplicate results should be filtered.
     *
     * @return <code>true</code> if duplicate results should be removed;
     *         otherwise <code>false</code>
     */
    public boolean isDistinct() {
        return false;
    }

    /**
     * Returns the specified page.
     *
     * @param firstResult the first result of the page to retrieve
     * @param maxResults  the maximun no of results in the page
     * @return the page corresponding to <code>firstResult</code>, or
     *         <code>null</code> if none exists
     */
    @SuppressWarnings("unchecked")
    protected IPage<T> getPage(int firstResult, int maxResults) {
        return new Page<T>(RESULTS, firstResult, maxResults, 0);
    }

}
