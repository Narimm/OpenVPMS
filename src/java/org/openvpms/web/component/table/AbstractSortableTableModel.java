package org.openvpms.web.component.table;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import nextapp.echo2.app.table.AbstractTableModel;
import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.comparators.TransformingComparator;


/**
 * Abstract implementation of a sortable table model.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractSortableTableModel
        extends AbstractTableModel
        implements SortableTableModel {

    /**
     * The current sort column.
     */
    private int _sortColumn = -1;

    /**
     * Determines if the column is sorted ascending or descending.
     */
    private boolean _sortAscending;


    /**
     * Consruct a new <code>AbstractSortableTableModel</code>.
     */
    public AbstractSortableTableModel() {
    }

    /**
     * Returns the identifier for a row.
     *
     * @param row the row
     * @return a unique identifier for the row
     */
    public Object getRowId(int row) {
        return getRowIds().get(row);
    }

    /**
     * Returns a row given its identifier.
     *
     * @param id the row id
     * @return the row, or <code>-1</code> if it doesn't exist
     */
    public int getRow(Object id) {
        return getRowIds().indexOf(id);
    }

    /**
     * Sort the table rows.
     *
     * @param column    the column to sort on
     * @param ascending if <code>true</code> sort the column ascending order;
     *                  otherwise sort it in <code>descebding</code> order
     */
    public void sort(int column, boolean ascending) {
        List<Object> ids = getRowIds();
        Transformer transformer = new IdToDataTransformer(column);
        Comparator comparator = getComparator(column, ascending);
        Comparator<Object> c
                = new TransformingComparator(transformer, comparator);
        Collections.sort(ids, c);
        setRowIds(ids);
        _sortColumn = column;
        _sortAscending = ascending;
        fireTableDataChanged();
    }

    /**
     * Returns the sort column.
     *
     * @return the sort column, or <code>-1</code> if no column is sorted.
     */
    public int getSortColumn() {
        return _sortColumn;
    }

    /**
     * Determines if the sort column is sorted ascending or descending.
     *
     * @return <code>true</code> if the column is sorted ascending;
     *         <code>false</code> if it is sorted descending
     */
    public boolean isSortedAscending() {
        return _sortAscending;
    }

    /**
     * Returns a comparator to sort a column. This implementation sorts on
     * natural ordering, handling nulls.
     *
     * @param column    the column to sort on
     * @param ascending if <code>true</code> sort the column ascending order;
     *                  otherwise sort it in <code>descebding</code> order
     * @return a comparator to sort the column
     */
    protected Comparator getComparator(int column, boolean ascending) {
        Comparator comparator = ComparatorUtils.naturalComparator();

        // handle nulls.
        comparator = ComparatorUtils.nullLowComparator(comparator);
        if (!ascending) {
            comparator = ComparatorUtils.reversedComparator(comparator);
        }
        return comparator;
    }

    /**
     * Returns the value at a given column and row. This implementation
     * delegates to {@link #getValueAt}.
     *
     * @param column the column
     * @param row    the row
     */
    protected Object getValue(int column, int row) {
        return getValueAt(column, row);
    }

    /**
     * Returns the row identifiers.
     *
     * @return the row identifiers.
     */
    protected abstract List<Object> getRowIds();

    /**
     * Sets the row identifiers.
     *
     * @param ids the row identifiers
     */
    protected abstract void setRowIds(List<Object> ids);


    /**
     * Transformer that takes a row identifier and column and returns the
     * corresponding data.
     */
    class IdToDataTransformer implements Transformer {

        /**
         * The colummn.
         */
        private final int _column;

        /**
         * Construct a new <code>IdToDataTransformer</code>.
         */
        public IdToDataTransformer(int column) {
            _column = column;
        }

        /**
         * Given a row identfier, returns the value at the corresponding row and
         * column.
         *
         * @param input the row identifier
         * @return the row corresponding to <code>input</code>
         */
        public Object transform(Object input) {
            int row = getRow(input);
            return getValue(_column, row);
        }
    }

}
