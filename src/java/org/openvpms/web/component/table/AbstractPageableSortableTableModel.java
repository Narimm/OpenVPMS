package org.openvpms.web.component.table;


/**
 * Abstract implementation of a pageable, sortable table model.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractPageableSortableTableModel
        extends AbstractSortableTableModel implements PageableTableModel {

    /**
     * The current page.
     */
    private int _page;

    /**
     * The no. of rows per page.
     */
    private int _rowsPerPage = 25;


    /**
     * Construct a new <code>AbstractPageableSortableTableModel</code>.
     */
    public AbstractPageableSortableTableModel() {
    }

    /**
     * Returns the number of rows in the table.
     *
     * @return the row count
     */
    public int getRowCount() {
        int available = getRows() - (_page * _rowsPerPage);
        return (available > _rowsPerPage) ? _rowsPerPage : available;
    }

    /**
     * Sets the current page.
     *
     * @param page the page to set
     */
    public void setPage(int page) {
        if (page * getRowsPerPage() > getRows()) {
            throw new IllegalArgumentException("Invalid page: " + page);
        }
        _page = page;
        fireTableDataChanged();
    }

    /**
     * Returns the current page.
     *
     * @return the current page
     */
    public int getPage() {
        return _page;
    }

    /**
     * Returns the total number of pages.
     *
     * @return the total number of pages
     */
    public int getPages() {
        return getRows() / _rowsPerPage;
    }

    /**
     * Sets the number of rows per page.
     *
     * @param rows the rows per page
     */
    public void setRowsPerPage(int rows) {
        _rowsPerPage = rows;
        fireTableDataChanged();
    }

    /**
     * Returns the number of rows per page.
     *
     * @return the number. of rows per page
     */
    public int getRowsPerPage() {
        return _rowsPerPage;
    }

    /**
     * Returns the absolute row, given a page-relative one.
     *
     * @param row the row relative to the current page
     * @return the absolute row
     */
    public int getAbsRow(int row) {
        return (_page * _rowsPerPage) + row;
    }

    /**
     * Returns the value found at the given coordinate within the table.
     * <p/>This implementation calculates the absolute row, and delegates to
     * {@link #getValue}.
     *
     * @param column the column index (0-based)
     * @param row    the row index (0-based)
     * @return the value at the given coordinate.
     */
    public Object getValueAt(int column, int row) {
        return getValue(column, getAbsRow(row));
    }

    /**
     * Returns the value at a given column and absolute row.
     *
     * @param column the column
     * @param row    the row
     * @return the value at the given coordinate.
     */
    @Override
    protected abstract Object getValue(int column, int row);

}
