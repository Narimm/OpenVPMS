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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.query;

import org.openvpms.component.system.common.query.IPage;

import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;


/**
 * Iterator over an {@link ResultSet}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ResultSetIterator<T> implements ListIterator<T> {

    /**
     * The result set.
     */
    private final ResultSet<T> set;

    /**
     * Iterator over the current page.
     */
    private ListIterator<T> pageIterator;

    /**
     * Indicates to start iteration at the specified offset in first page, or <tt>-1</tt> to start at the beginning
     */
    private int initialOffset;

    /**
     * The page offset into the results, use to help iterate through the result set.
     */
    private int firstResult = -1;

    /**
     * The no. of elements in the last-read page.
     */
    private int count;

    /**
     * The index of the last returned element.
     */
    private int last = -1;


    /**
     * Constructs a <tt>ResultSetIterator</tt>.
     *
     * @param set the result set
     */
    public ResultSetIterator(ResultSet<T> set) {
        this(set, -1);
    }

    /**
     * Constructs a <tt>ResultSetIterator</tt>.
     *
     * @param set    the result set
     * @param offset start iteration at the specified offset in first page, or <tt>-1</tt> to start at the beginning
     */
    public ResultSetIterator(ResultSet<T> set, int offset) {
        this.set = set;
        this.initialOffset = offset;
        if (initialOffset != -1) {
            moveNext();
        }
    }

    /**
     * Constructs a <tt>ResultSetIterator</tt> that attempts to start iteration from the supplied object.
     * <p/>
     * NOTE: the object must be in the last-retrieved page, for the iteration to begin at it. It will be
     * returned by the first call to <tt>next()</tt>.
     *
     * @param set   the result set
     * @param first the first object to return
     */
    public ResultSetIterator(ResultSet<T> set, T first) {
        this.set = set;
        int lastPage = set.lastIndex();
        if (lastPage != -1) {
            IPage<T> page = set.getPage(lastPage);
            if (page != null) {
                initialOffset = page.getResults().indexOf(first);
                if (initialOffset != -1) {
                    moveNext();
                }
            }
        }
    }

    /**
     * Returns <tt>true</tt> if the iteration has more elements. (In other
     * words, returns <tt>true</tt> if <tt>next</tt> would return an element
     * rather than throwing an exception.)
     *
     * @return <tt>true</tt> if the iterator has more elements.
     */
    public boolean hasNext() {
        if (pageIterator == null || !pageIterator.hasNext()) {
            moveNext();
        }
        return pageIterator != null && pageIterator.hasNext();
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
        if (pageIterator == null || !pageIterator.hasPrevious()) {
            movePrevious();
        }
        return pageIterator != null && pageIterator.hasPrevious();
    }

    /**
     * Returns the next element in the iteration.  Calling this method
     * repeatedly until the {@link #hasNext()} method returns false will
     * return each element in the underlying collection exactly once.
     *
     * @return the next element in the iteration.
     * @throws NoSuchElementException iteration has no more elements.
     */
    public T next() {
        if (pageIterator == null || !pageIterator.hasNext()) {
            if (!moveNext()) {
                throw new NoSuchElementException();
            }
        }
        int lastIndex = nextIndex();
        T result = pageIterator.next();
        last = lastIndex;
        return result;
    }

    /**
     * Returns the previous element in the list.  This method may be called
     * repeatedly to iterate through the list backwards, or intermixed with
     * calls to <tt>next</tt> to go back and forth.  (Note that alternating
     * calls to <tt>next</tt> and <tt>previous</tt> will return the same
     * element repeatedly.)
     *
     * @return the previous element in the list.
     * @throws java.util.NoSuchElementException
     *          if the iteration has no previous element
     */
    public T previous() {
        if (pageIterator == null || !pageIterator.hasPrevious()) {
            if (!movePrevious()) {
                throw new NoSuchElementException();
            }
        }
        int lastIndex = previousIndex();
        T result = pageIterator.previous();
        last = lastIndex;
        return result;
    }

    /**
     * Returns the index of the element that would be returned by a subsequent
     * call to <tt>next</tt>. (Returns list size if the list iterator is at the
     * end of the list.)
     *
     * @return the index of the element that would be returned by a subsequent
     *         call to <tt>next</tt>, or list size if list iterator is at end
     *         of list.
     */
    public int nextIndex() {
        int offset = (pageIterator == null) ? 0 : pageIterator.nextIndex();
        int page = set.previousIndex();
        if (page < 0) {
            page = 0;
        }
        return set.getPageSize() * page + offset;
    }

    /**
     * Returns the index of the element that would be returned by a subsequent
     * call to <tt>previous</tt>. (Returns -1 if the list iterator is at the
     * beginning of the list.)
     *
     * @return the index of the element that would be returned by a subsequent
     *         call to <tt>previous</tt>, or -1 if list iterator is at
     *         beginning of list.
     */
    public int previousIndex() {
        int page;
        int offset;
        if (pageIterator == null || !pageIterator.hasPrevious()) {
            page = set.previousIndex();
            if (set.hasNext()) {
                offset = set.getPageSize() - 1;
            } else {
                offset = count - 1;
            }
        } else {
            page = set.nextIndex();
            offset = pageIterator.previousIndex();
        }
        return (page >= 0) ? set.getPageSize() * page + offset : -1;
    }

    /**
     * Returns the index of the last returned element.
     *
     * @return the index of the last returned element, or <tt>-1</tt> if no element has been returned
     */
    public int lastIndex() {
        return last;
    }

    /**
     * Not supported.
     *
     * @param t the element with which to replace the last element returned by
     *          <tt>next</tt> or <tt>previous</tt>.
     * @throws UnsupportedOperationException if invoked
     */
    public void set(T t) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported.
     *
     * @param e the element to insert.
     * @throws UnsupportedOperationException if invoked
     */
    public void add(T e) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported.
     *
     * @throws UnsupportedOperationException if invoked
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Attempts to move to the next page.
     *
     * @return <tt>true</tt> if the move was successful.
     */
    private boolean moveNext() {
        boolean result = false;
        IPage<T> page = null;
        if (set.hasNext()) {
            page = set.next();
            if (firstResult == page.getFirstResult()) {
                // have just done a movePrevious()/moveNext() which returns the same page.
                // Discard it, and try and get the next one
                if (set.hasNext()) {
                    page = set.next();
                } else {
                    page = null;
                }
            }
        }
        if (page != null) {
            pageIterator = page.getResults().listIterator();
            count = page.getResults().size();
            if (initialOffset != -1) {
                // skip forward to the specified index
                while (pageIterator.hasNext() && pageIterator.nextIndex() != initialOffset) {
                    pageIterator.next();
                }
                initialOffset = -1;
            }
            firstResult = page.getFirstResult();
            result = true;
        } else {
            firstResult += set.getPageSize();
            pageIterator = Collections.<T>emptyList().listIterator();
        }
        return result;
    }

    /**
     * Attempts to move to the previous page.
     *
     * @return <tt>true</tt> if the move was successful.
     */
    private boolean movePrevious() {
        boolean result = false;
        IPage<T> page = null;
        if (set.hasPrevious()) {
            page = set.previous();
            if (firstResult == page.getFirstResult()) {
                // have just done a moveNext()/movePrevious() which returns the same page.
                // Discard it, and try and get the prior one
                if (set.hasPrevious()) {
                    page = set.previous();
                } else {
                    page = null;
                }
            }
        }
        if (page != null) {
            List<T> list = page.getResults();
            pageIterator = list.listIterator(list.size());
            firstResult = page.getFirstResult();
            result = true;
        } else {
            firstResult -= set.getPageSize();
            pageIterator = Collections.<T>emptyList().listIterator();
        }
        return result;
    }

}