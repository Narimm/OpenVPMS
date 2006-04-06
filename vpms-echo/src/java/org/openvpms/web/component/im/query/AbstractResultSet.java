package org.openvpms.web.component.im.query;

import java.util.NoSuchElementException;

import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;


/**
 * Abstract implementation of the {@link ResultSet} interface.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractResultSet<T> implements ResultSet<T> {

    /**
     * The no. of rows per page.
     */
    private final int _rowsPerPage;

    /**
     * The last retrieved page.
     */
    private IPage<T> _page;

    /**
     * The page cursor.
     */
    private int _cursor;

    /**
     * The total no. of pages.
     */
    private int _pages;

    /**
     * The total no. of rows.
     */
    private int _rows;


    /**
     * Construct a new <code>AbstractResultSet</code>.
     *
     * @param rows the maximum no. of rows per page
     */
    public AbstractResultSet(int rows) {
        _rowsPerPage = rows;
    }

    /**
     * Reset the iterator.
     */
    public void reset() {
        _page = null;
        _cursor = 0;
        _page = getPage(_cursor);
        _pages = (_page != null) ? getPages(_page) : 0;
        _rows = (_page != null) ? _page.getTotalNumOfRows() : 0;
    }

    /**
     * Returns the specified page.
     *
     * @param page the page no.
     * @return the page corresponding to <code>page</code>
     */
    public IPage<T> getPage(int page) {
        IPage<T> result = get(page);
        _cursor = page;
        return result;
    }

    /**
     * Returns the total number of pages.
     *
     * @return the total no. of pages.
     */
    public int getPages() {
        return _pages;
    }

    /**
     * Returns the number of rows returned per page.
     *
     * @return the maximum no. of rows returned in each page, or {@link
     *         PagingCriteria#ALL_ROWS} for all rows.
     */
    public int getRowsPerPage() {
        return _rowsPerPage;
    }

    /**
     * Returns the number of rows.
     *
     * @return the number of rows
     */
    public int getRows() {
        return _rows;
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
        return get(_cursor) != null;
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
        return (_cursor != 0);
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
        IPage<T> page = get(_cursor);
        if (page == null) {
            throw new NoSuchElementException();
        }
        ++_cursor;
        return page;
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
        IPage<T> page = get(_cursor - 1);
        if (page == null) {
            throw new NoSuchElementException();
        }
        --_cursor;
        return page;
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
        return _cursor;
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
        return _cursor - 1;
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
     * Inserts the specified element into the list (optional operation)
     *
     * @param object the element to insert.
     * @throws UnsupportedOperationException if invoked
     */
    public void add(IPage<T> object) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the specified page.
     *
     * @param firstRow the first row of the page to retrieve
     * @param maxRows  the maximun no of rows in the page
     * @return the page corresponding to <code>firstRow</code>, or
     *         <code>null</code> if none exists
     */
    protected abstract IPage<T> getPage(int firstRow, int maxRows);

    /**
     * Returns the current page.
     *
     * @return the current page or <code>null</code> if there is no current
     *         page
     */
    protected IPage<T> getPage() {
        return _page;
    }

    /**
     * Returns the index of the current page.
     *
     * @param page the current page
     * @return the index of the current page
     */
    protected int getIndex(IPage<T> page) {
        int first = getFirstRow(page);
        if (_rowsPerPage == ArchetypeQuery.ALL_ROWS) {
            return 0;
        }
        return page.getTotalNumOfRows() / first;
    }

    /**
     * Returns the total no. of pages.
     *
     * @param page the current page
     * @return the total no. of pages
     */
    protected int getPages(IPage<T> page) {
        if (_rowsPerPage == ArchetypeQuery.ALL_ROWS) {
            return 1;
        }
        int pages = (page.getTotalNumOfRows() / _rowsPerPage);
        if (page.getTotalNumOfRows() % _rowsPerPage > 0) {
            ++pages;
        }
        return pages;
    }

    /**
     * Helper to return the index of the first row of the current page.
     *
     * @param page the current page
     * @return the index of the first row
     */
    protected int getFirstRow(IPage<T> page) {
        return page.getPagingCriteria().getFirstRow();
    }

    /**
     * Helper to return the index of the first row of the next page.
     *
     * @param page the current page
     * @return the index of the first row in the next page. If
     *         <code>&gt;objects.size()</code> indicates at end of set
     */
    protected int getNextRow(IPage<T> page) {
        return getFirstRow(page) + page.getRows().size();
    }

    /**
     * Helper to return the index of the first row of the previous page.
     *
     * @return the index of the first row of the previous page. If
     *         <code>&lt;0</code> indicates at beginning of set
     */
    protected int getPreviousRow(IPage<T> page) {
        if (_rowsPerPage == ArchetypeQuery.ALL_ROWS) {
            return -1;
        }
        return getFirstRow(page) - _rowsPerPage;
    }

    /**
     * Returns the specified page.
     *
     * @param page the page no.
     * @return the page, or <code>null</code> if there is no such page
     */
    protected IPage<T> get(int page) {
        int row = page * _rowsPerPage;
        if (_page == null || _page.getPagingCriteria().getFirstRow() != row) {
            _page = getPage(row, _rowsPerPage);
            if (_page != null && _page.getRows().isEmpty()) {
                _page = null;
            }
        }
        return _page;
    }

}
