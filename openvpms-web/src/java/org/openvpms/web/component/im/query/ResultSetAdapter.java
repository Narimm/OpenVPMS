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

import java.util.NoSuchElementException;


/**
 * Adapts the returned values of a result set to a different type.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class ResultSetAdapter<A, T> implements ResultSet<T> {

    /**
     * The result set to adapt.
     */
    private ResultSet<A> set;


    /**
     * Creates a new <tt>ResultSetAdapter</tt>.
     *
     * @param set the result set to adapt
     */
    public ResultSetAdapter(ResultSet<A> set) {
        this.set = set;
    }

    /**
     * Reset the iterator.
     */
    public void reset() {
        set.reset();
    }

    /**
     * Sorts the set. This resets the iterator.
     *
     * @param sort the sort criteria. May be <tt>null</tt>
     */
    public void sort(SortConstraint[] sort) {
        set.sort(sort);
    }

    /**
     * Returns the specified page.
     *
     * @param page the page no.
     * @return the page corresponding to <tt>page</tt>. May be <tt>null</tt>
     */
    public IPage<T> getPage(int page) {
        IPage<A> set = this.set.getPage(page);
        return (set != null) ? convert(set) : null;
    }


    /**
     * Returns the total number of pages.
     * For complex queries, this operation can be expensive. If an exact
     * count is not required, use {@link #getEstimatedPages()}.
     *
     * @return the total no. of pages.
     */
    public int getPages() {
        return set.getPages();
    }

    /**
     * Returns an estimation of the total no. of pages.
     *
     * @return an estimation of the total no. of pages
     */
    public int getEstimatedPages() {
        return set.getEstimatedPages();
    }

    /**
     * Returns the number of results returned per page.
     *
     * @return the maximum no. of results returned in each page, or
     *         {@link org.openvpms.component.system.common.query.ArchetypeQuery#ALL_RESULTS} for all results.
     */
    public int getPageSize() {
        return set.getPageSize();
    }

    /**
     * Returns the total number of results matching the query criteria.
     * For complex queries, this operation can be expensive. If an exact
     * count is not required, use {@link #getEstimatedResults()}.
     *
     * @return the total number of results
     */
    public int getResults() {
        return set.getResults();
    }

    /**
     * Returns an estimation of the total no. of results matching the query
     * criteria.
     *
     * @return an estimation of the total no. of results
     */
    public int getEstimatedResults() {
        return set.getEstimatedResults();
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
        return set.isEstimatedActual();
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
     * Sets the nodes to query.
     *
     * @param nodes the nodes to query
     */
    public void setNodes(String[] nodes) {
        set.setNodes(nodes);
    }

    /**
     * Returns <tt>true</tt> if this list iterator has more elements when
     * traversing the list in the forward direction. (In other words, returns
     * <tt>true</tt> if <tt>next</tt> would return an element rather than
     * throwing an exception.)
     *
     * @return <tt>true</tt> if the list iterator has more elements when
     *         traversing the list in the forward direction.
     */
    public boolean hasNext() {
        return set.hasNext();
    }

    /**
     * Returns <tt>true</tt> if this list iterator has more elements when
     * traversing the list in the reverse direction.  (In other words, returns
     * <tt>true</tt> if <tt>previous</tt> would return an element rather than
     * throwing an exception.)
     *
     * @return <tt>true</tt> if the list iterator has more elements when
     *         traversing the list in the reverse direction.
     */
    public boolean hasPrevious() {
        return set.hasPrevious();
    }

    /**
     * Returns the next element in the list.  This method may be called
     * repeatedly to iterate through the list, or intermixed with calls to
     * <tt>previous</tt> to go back and forth.  (Note that alternating calls to
     * <tt>next</tt> and <tt>previous</tt> will return the same element
     * repeatedly.)
     *
     * @return the next element in the list.
     * @throws NoSuchElementException if the iteration has no next element.
     */
    public IPage<T> next() {
        IPage<A> page = set.next();
        return convert(page);
    }

    /**
     * Returns the previous element in the list.  This method may be called
     * repeatedly to iterate through the list backwards, or intermixed with
     * calls to <tt>next</tt> to go back and forth.  (Note that alternating
     * calls to <tt>next</tt> and <tt>previous</tt> will return the same element
     * repeatedly.)
     *
     * @return the previous element in the list.
     * @throws NoSuchElementException if the iteration has no previous element.
     */
    public IPage<T> previous() {
        IPage<A> page = set.previous();
        return convert(page);
    }

    /**
     * Returns the index of the element that would be returned by a subsequent
     * call to <tt>next</tt>. (Returns list size if the list iterator is at the
     * end of the list.)
     *
     * @return the index of the element that would be returned by a subsequent
     *         call to <tt>next</tt>, or list size if list iterator is at end of
     *         list.
     */
    public int nextIndex() {
        return set.nextIndex();
    }

    /**
     * Returns the index of the element that would be returned by a subsequent
     * call to <tt>previous</tt>. (Returns -1 if the list iterator is at the
     * beginning of the list.)
     *
     * @return the index of the element that would be returned by a subsequent
     *         call to <tt>previous</tt>, or -1 if list iterator is at beginning
     *         of list.
     */
    public int previousIndex() {
        return set.previousIndex();
    }

    /**
     * Removes from the list the last element that was returned by <tt>next</tt>
     * or <tt>previous</tt> (optional operation).
     *
     * @throws UnsupportedOperationException if invoked
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Replaces the last element returned by <tt>next</tt> or <tt>previous</tt>
     * with the specified element (optional operation).
     *
     * @param object the element with which to replace the last element returned
     *               by <tt>next</tt> or <tt>previous</tt>.
     * @throws UnsupportedOperationException if invoked
     */
    public void set(IPage<T> object) {
        throw new UnsupportedOperationException();
    }

    /**
     * Inserts the specified element into the list (optional operation).
     *
     * @param object the element to insert.
     * @throws UnsupportedOperationException if invoked
     */
    public void add(IPage<T> object) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the index of the last returned page.
     *
     * @return the index of the last returned page, or <tt>-1</tt> if no page has been returned
     */
    public int lastIndex() {
        return set.lastIndex();
    }

    /**
     * Clones this result set.
     * <p/>
     * This copies the state of iterators.
     *
     * @return a clone of this
     * @throws CloneNotSupportedException if the set cannot be cloned
     */
    @SuppressWarnings("unchecked")
    public ResultSet<T> clone() throws CloneNotSupportedException {
        ResultSetAdapter<A, T> result = (ResultSetAdapter<A, T>) super.clone();
        result.set = set.clone();
        return result;
    }

    /**
     * Converts a page.
     *
     * @param page the page to convert
     * @return the converted page
     */
    protected abstract IPage<T> convert(IPage<A> page);
}
