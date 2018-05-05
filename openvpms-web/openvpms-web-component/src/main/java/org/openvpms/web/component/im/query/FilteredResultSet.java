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

package org.openvpms.web.component.im.query;

import org.openvpms.component.business.dao.im.Page;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.SortConstraint;

import java.util.ArrayList;
import java.util.List;


/**
 * An {@link ResultSet} that filters objects returned by an underlying result set.
 * <p/>
 * Note that this should not be used for large result sets, as all retrieved pages are cached.
 *
 * @author Tim Anderson
 */
public abstract class FilteredResultSet<T> extends AbstractResultSet<T> {

    /**
     * The underlying result set.
     */
    private final ResultSet<T> set;

    /**
     * Iterator over the underlying result set.
     */
    private ResultSetIterator<T> iterator;

    /**
     * The filtered pages.
     */
    private List<IPage<T>> pages = new ArrayList<>();


    /**
     * Constructs an {@link FilteredResultSet}.
     *
     * @param set the result set to filter
     */
    public FilteredResultSet(ResultSet<T> set) {
        super(set.getPageSize());
        this.set = set;
    }

    /**
     * Reset the iterator.
     */
    @Override
    public void reset() {
        set.reset();
        initPages();
        super.reset();
    }

    /**
     * Sorts the set. This resets the iterator.
     *
     * @param sort the sort criteria. May be {@code null}
     */
    public void sort(SortConstraint[] sort) {
        set.sort(sort);
        super.reset();
        initPages();
    }

    /**
     * Returns the total number of results matching the query criteria.
     * For complex queries, this operation can be expensive. If an exact
     * count is not required, use {@link #getEstimatedResults()}.
     *
     * @return the total number of results
     */
    public int getResults() {
        return count();
    }

    /**
     * Returns an estimation of the total no. of results matching the query criteria.
     *
     * @return an estimation of the total no. of results
     */
    public int getEstimatedResults() {
        IPage<T> page = getLast();
        return page != null ? page.getFirstResult() + page.getResults().size() : 0;
    }

    /**
     * Determines if the estimated no. of results is the actual total, i.e
     * if {@link #getEstimatedResults()} would return the same as
     * {@link #getResults()}, and {@link #getEstimatedPages()} would return
     * the same as {@link #getPages()}.
     *
     * @return {@code true} if the estimated results equals the actual no.
     * of results
     */
    public boolean isEstimatedActual() {
        return (iterator != null && !iterator.hasNext());
    }

    /**
     * Determines if the node is sorted ascending or descending.
     *
     * @return {@code true} if the node is sorted ascending or no sort constraint was specified; {@code false} if it is
     * sorted descending
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
     * @return {@code true} if duplicate results should be removed;
     * otherwise {@code false}
     */
    public boolean isDistinct() {
        return set.isDistinct();
    }

    /**
     * Determines if an object should be included in the result set.
     * <p/>
     * The {@code results} parameter enables one or more included objects related to {@code object} to be added to the
     * result set.
     *
     * @param object  the object
     * @param results the result set to add included objects to
     */
    protected abstract void filter(T object, List<T> results);

    /**
     * Returns the specified page.
     *
     * @param page the page no.
     * @return the page, or {@code null} if there is no such page
     */
    protected IPage<T> get(int page) {
        fill(page);
        return pages.size() > page ? pages.get(page) : null;
    }

    /**
     * Fills up to and including the specified page.
     *
     * @param page the page to fill to, or {@code -1} to fill all available pages
     */
    private void fill(int page) {
        if (iterator == null) {
            iterator = new ResultSetIterator<>(set);
        }
        int pageSize = getPageSize();
        while ((page == -1 || pageSize == -1|| page >= pages.size() || pages.get(page).getResults().size() < pageSize)
               && iterator.hasNext()) {
            List<T> objects = new ArrayList<>();
            while (iterator.hasNext() && (pageSize == -1 || objects.size() < pageSize)) {
                T object = iterator.next();
                filter(object, objects);
            }
            if (!objects.isEmpty()) {
                addPages(objects);
            }
        }
    }

    /**
     * Adds objects to the pages.
     *
     * @param objects the objects to add
     */
    private void addPages(List<T> objects) {
        int pageSize = getPageSize();
        int index = 0;
        IPage<T> last = getLast();
        if (last != null && (pageSize == -1 || last.getResults().size() < pageSize)) {
            // the last page was partially filled. Need to fill it first.
            int end = pageSize - last.getResults().size();
            if (end > objects.size()) {
                end = objects.size();
            }
            last.getResults().addAll(objects.subList(0, end));
            index = end;
        }
        int first = pages.size() * pageSize;
        while (index < objects.size()) {
            int end;
            if (pageSize == -1 || index + pageSize > objects.size()) {
                end = objects.size();
            } else {
                end = index + pageSize;
            }
            if (index < end) {
                List<T> list = new ArrayList<>(objects.subList(index, end));
                IPage<T> page = new Page<>(list, first, pageSize, -1);
                pages.add(page);
                first += pageSize;
                index = end;
            }
        }
    }

    /**
     * Initialises the pages.
     */
    private void initPages() {
        iterator = null;
        pages.clear();
    }

    /**
     * Counts the elements in the result set.
     *
     * @return the no. of elements in the result set
     */
    private int count() {
        int result = 0;
        fill(-1);
        IPage<T> page = getLast();
        if (page != null) {
            result = page.getFirstResult() + page.getResults().size();
        }
        return result;
    }

    /**
     * Returns the last page.
     *
     * @return the last page, or {@code null} if there are no pages
     */
    private IPage<T> getLast() {
        return !pages.isEmpty() ? pages.get(pages.size() - 1) : null;
    }

}
