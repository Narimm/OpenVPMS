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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.query;

import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.SortConstraint;

import java.util.ArrayList;
import java.util.List;


/**
 * An {@link ResultSet} that filters objects returned by an underlying
 * result set.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractFilteredResultSet<T>
    extends AbstractResultSet<T> {

    /**
     * The underlying result set.
     */
    private final ResultSet<T> set;

    /**
     * The filtered result set.
     */
    private FilteredSet<T> filtered;


    /**
     * Creates a new <tt>AbstractFilteredResultSet</tt>.
     *
     * @param set the result set to filter
     */
    public AbstractFilteredResultSet(ResultSet<T> set) {
        super(set.getPageSize());
        this.set = set;
    }

    /**
     * Reset the iterator.
     */
    @Override
    public void reset() {
        filtered = null;
        super.reset();
    }

    /**
     * Sorts the set. This resets the iterator.
     *
     * @param sort the sort criteria. May be <tt>null</tt>
     */
    public void sort(SortConstraint[] sort) {
        set.sort(sort);
        filtered = null;
    }

    /**
     * Returns the total number of results matching the query criteria.
     * For complex queries, this operation can be expensive. If an exact
     * count is not required, use {@link #getEstimatedResults()}.
     *
     * @return the total number of results
     */
    public int getResults() {
        return getFiltered().getResults();
    }

    /**
     * Returns an estimation of the total no. of results matching the query
     * criteria.
     *
     * @return an estimation of the total no. of results
     */
    public int getEstimatedResults() {
        return getFiltered().getEstimatedResults();
    }

    /**
     * Determines if the estimated no. of results is the actual total, i.e
     * if {@link #getEstimatedResults()} would return the same as
     * {@link #getResults()}, and {@link #getEstimatedPages()} would return
     * the same as {@link #getPages()}.
     *
     * @return <tt>true</tt> if the estimated results equals the actual no.
     *         of results
     */
    public boolean isEstimatedActual() {
        return getFiltered().isEstimatedActual();
    }

    /**
     * Determines if the node is sorted ascending or descending.
     *
     * @return <tt>true</tt> if the node is sorted ascending or no sort
     *         constraint was specified; <tt>false</tt> if it is sorted
     *         descending
     */
    public boolean isSortedAscending() {
        return set.isSortedAscending();
    }

    /**
     * Returns the sort criteria.
     *
     * @return the sort criteria. Never null
     */
    public SortConstraint[] getSortConstraints() {
        return set.getSortConstraints();
    }

    /**
     * Determines if duplicate results should be filtered.
     *
     * @param distinct if true, remove duplicate results
     */
    public void setDistinct(boolean distinct) {
        set.setDistinct(distinct);
    }

    /**
     * Determines if duplicate results should be filtered.
     *
     * @return <tt>true</tt> if duplicate results should be removed;
     *         otherwise <tt>false</tt>
     */
    public boolean isDistinct() {
        return set.isDistinct();
    }

    /**
     * Determines if an object should be included in the result set.
     * <p/>
     * The <tt>results</tt> parameter enables one or more included objects
     * related to <tt>object</tt> to be added to the result set.
     *
     * @param object  the object
     * @param results the result set to add included objects to
     */
    protected abstract void filter(T object, List<T> results);

    /**
     * Returns the specified page.
     *
     * @param page the page no.
     * @return the page, or <tt>null</tt> if there is no such page
     */
    protected IPage<T> get(int page) {
        return getFiltered().get(page);
    }

    /**
     * @return the filtered set
     */
    private FilteredSet<T> getFiltered() {
        if (filtered == null) {
            List<T> objects = new ArrayList<T>();
            ResultSetIterator<T> iter = new ResultSetIterator<T>(set);
            while (iter.hasNext()) {
                T object = iter.next();
                filter(object, objects);
            }
            filtered = new FilteredSet<T>(objects, getPageSize());
        }
        return filtered;
    }


    private class FilteredSet<T> extends ListResultSet<T> {

        /**
         * Constructs a new <tt>FilteredSet</tt>.
         *
         * @param objects  the objects
         * @param pageSize the maximum no. of results per page
         */
        public FilteredSet(List<T> objects, int pageSize) {
            super(objects, pageSize);
        }
    }

}
