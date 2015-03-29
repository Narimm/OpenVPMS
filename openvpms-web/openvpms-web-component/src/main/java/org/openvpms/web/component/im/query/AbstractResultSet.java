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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.query;

import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;

import java.util.NoSuchElementException;


/**
 * Abstract implementation of the {@link ResultSet} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractResultSet<T> implements ResultSet<T> {

    /**
     * The no. of results per page.
     */
    private final int pageSize;

    /**
     * The last retrieved page.
     */
    private IPage<T> currentPage;

    /**
     * The page cursor.
     */
    private int cursor;

    /**
     * The nodes to query. If {@code null} indicates to query all nodes.
     */
    private String[] nodes;


    /**
     * Construct an {@link AbstractResultSet}.
     *
     * @param pageSize the maximum no. of results per page
     */
    public AbstractResultSet(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * Reset the iterator.
     */
    public void reset() {
        currentPage = null;
        cursor = 0;
    }

    /**
     * Determines if a page exists.
     *
     * @param page the page no.
     * @return {@code true} if the page exists
     */
    public boolean hasPage(int page) {
        return get(page) != null;
    }

    /**
     * Returns the specified page.
     *
     * @param page the page no.
     * @return the page corresponding to {@code page}. May be {@code null}
     */
    public IPage<T> getPage(int page) {
        int row = page * pageSize;
        if (currentPage == null || currentPage.getFirstResult() != row) {
            currentPage = get(page);
            if (currentPage != null && currentPage.getResults().isEmpty()) {
                currentPage = null;
            }
        }
        cursor = page;
        return currentPage;
    }

    /**
     * Returns the total number of pages.
     *
     * @return the total no. of pages.
     */
    public int getPages() {
        return getPages(true);
    }

    /**
     * Returns an estimation of the total no. of pages.
     *
     * @return an estimation of the total no. of pages
     */
    public int getEstimatedPages() {
        if (pageSize == ArchetypeQuery.ALL_RESULTS) {
            return 1;
        }
        int results = getEstimatedResults();
        int pages;
        if (results != -1) {
            pages = (results / pageSize);
            if (results % pageSize > 0) {
                ++pages;
            }
        } else {
            pages = -1;
        }
        return pages;
    }

    /**
     * Returns the number of results returned per page.
     *
     * @return the maximum no. of results returned in each page, or {@link ArchetypeQuery#ALL_RESULTS} for all results.
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Sets the nodes to query. If {@code null} or empty, indicates
     * to query all nodes.
     *
     * @param nodes the nodes to query. May be {@code null}
     */
    public void setNodes(String[] nodes) {
        this.nodes = nodes;
    }

    /**
     * Returns the nodes to query.
     *
     * @return the nodes to query/ If {@code null} indicates to query
     *         all nodes
     */
    public String[] getNodes() {
        return nodes;
    }

    /**
     * Returns {@code true} if this list iterator has more elements when
     * traversing the list in the forward direction. (In other words, returns
     * {@code true} if {@code next} would return an element rather than
     * throwing an exception.)
     *
     * @return {@code true} if the list iterator has more elements when
     *         traversing the list in the forward direction.
     */
    public boolean hasNext() {
        return get(cursor) != null;
    }

    /**
     * Returns {@code true} if this list iterator has more elements when
     * traversing the list in the reverse direction.  (In other words, returns
     * {@code true} if {@code previous} would return an element rather than
     * throwing an exception.)
     *
     * @return {@code true} if the list iterator has more elements when
     *         traversing the list in the reverse direction.
     */
    public boolean hasPrevious() {
        return (cursor != 0);
    }

    /**
     * Returns the next element in the list.  This method may be called
     * repeatedly to iterate through the list, or intermixed with calls to
     * {@code previous} to go back and forth.  (Note that alternating calls to
     * {@code next} and {@code previous} will return the same element
     * repeatedly.)
     *
     * @return the next element in the list.
     * @throws NoSuchElementException if the iteration has no next element.
     */
    public IPage<T> next() {
        IPage<T> page = get(cursor);
        if (page == null) {
            throw new NoSuchElementException();
        }
        currentPage = page;
        ++cursor;
        return page;
    }

    /**
     * Returns the previous element in the list.  This method may be called
     * repeatedly to iterate through the list backwards, or intermixed with
     * calls to {@code next} to go back and forth.  (Note that alternating
     * calls to {@code next} and {@code previous} will return the same element
     * repeatedly.)
     *
     * @return the previous element in the list.
     * @throws NoSuchElementException if the iteration has no previous element.
     */
    public IPage<T> previous() {
        IPage<T> page = get(cursor - 1);
        if (page == null) {
            throw new NoSuchElementException();
        }
        currentPage = page;
        --cursor;
        return page;
    }

    /**
     * Returns the index of the element that would be returned by a subsequent
     * call to {@code next}. (Returns list size if the list iterator is at the
     * end of the list.)
     *
     * @return the index of the element that would be returned by a subsequent
     *         call to {@code next}, or list size if list iterator is at end of
     *         list.
     */
    public int nextIndex() {
        return cursor;
    }

    /**
     * Returns the index of the element that would be returned by a subsequent
     * call to {@code previous}. (Returns -1 if the list iterator is at the
     * beginning of the list.)
     *
     * @return the index of the element that would be returned by a subsequent
     *         call to {@code previous}, or -1 if list iterator is at beginning
     *         of list.
     */
    public int previousIndex() {
        return cursor - 1;
    }

    /**
     * Removes from the list the last element that was returned by {@code next}
     * or {@code previous} (optional operation).
     *
     * @throws UnsupportedOperationException if invoked
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Replaces the last element returned by {@code next} or {@code previous}
     * with the specified element (optional operation).
     *
     * @param object the element with which to replace the last element returned
     *               by {@code next} or {@code previous}.
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
     * @return the index of the last returned page, or {@code -1} if no page has been returned
     */
    public int lastIndex() {
        int page = -1;
        if (currentPage != null) {
            if (pageSize != ArchetypeQuery.ALL_RESULTS) {
                page = (currentPage.getFirstResult() / pageSize);
            } else {
                page = 0;
            }
        }
        return page;
    }

    /**
     * Clones this result set.
     * <p/>
     * This copies the state of iterators.
     *
     * @return a clone of this
     * @throws CloneNotSupportedException if the instance cannot be cloned
     */
    @SuppressWarnings("unchecked")
    public ResultSet<T> clone() throws CloneNotSupportedException {
        return (ResultSet<T>) super.clone();
    }

    /**
     * Returns the no. of pages.
     *
     * @param force if {@code true} force a calculation of the no. of pages
     * @return the total no. of pages
     */
    protected int getPages(boolean force) {
        if (pageSize == ArchetypeQuery.ALL_RESULTS) {
            return 1;
        }
        int results = (force) ? getResults() : getEstimatedResults();
        int pages;
        if (results != -1) {
            pages = (results / pageSize);
            if (results % pageSize > 0) {
                ++pages;
            }
        } else {
            pages = 1;
        }
        return pages;
    }

    /**
     * Returns the specified page.
     *
     * @param page the page no.
     * @return the page, or {@code null} if there is no such page
     */
    protected abstract IPage<T> get(int page);

    /**
     * Returns the current page.
     *
     * @return the current page or {@code null} if there is no current page
     */
    protected IPage<T> getPage() {
        return currentPage;
    }

    /**
     * Calculates the first row of a page.
     *
     * @param page the page
     * @return the first row
     */
    protected int getFirstResult(int page) {
        return (pageSize == ArchetypeQuery.ALL_RESULTS) ? 0 : page * pageSize;
    }

}
