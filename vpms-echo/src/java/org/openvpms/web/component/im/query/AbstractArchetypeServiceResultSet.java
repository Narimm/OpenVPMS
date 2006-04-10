package org.openvpms.web.component.im.query;

import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.IPage;


/**
 * Abstract implementation of the {@link ResultSet} interface for result sets
 * that query the {@link IArchetypeService}.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractArchetypeServiceResultSet<T>
        extends AbstractResultSet<T> {

    /**
     * Additional constraints to associate with the query. May be
     * <code>null</code>
     */
    private final IConstraint _constraints;

    /**
     * The sort order. May be <code>null</code>.
     */
    private SortOrder _order;


    /**
     * Construct a new <code>AbstractArchetypeServiceResultSet</code>.
     *
     * @param rows  the maximum no. of rows per page
     * @param order the sort criteria. May be <code>null</code>
     */
    public AbstractArchetypeServiceResultSet(int rows, SortOrder order) {
        this(null, rows, order);
    }

    /**
     * Construct a new <code>AbstractArchetypeServiceResultSet</code>.
     *
     * @param constraints query constraints. May be <code>null</code>
     * @param rows        the maximum no. of rows per page
     * @param order       the sort criteria. May be <code>null</code>
     */
    public AbstractArchetypeServiceResultSet(IConstraint constraints,
                                             int rows, SortOrder order) {
        super(rows);
        _constraints = constraints;
        _order = order;
    }

    /**
     * Sort the set. This resets the iterator.
     *
     * @param node      the node to sort on
     * @param ascending if <code>true</code> sort the column ascending order;
     *                  otherwise sort it in <code>descebding</code> order
     */
    public void sort(String node, boolean ascending) {
        _order = new SortOrder(node, ascending);
        reset();
    }

    /**
     * Returns the node the set was sorted on.
     *
     * @return the sort node, or <code>null</code> if the set is unsorted
     */
    public String getSortNode() {
        return (_order != null) ? _order.getNode() : null;
    }

    /**
     * Determines if the node is sorted ascending or descending.
     *
     * @return <code>true</code> if the node is sorted ascending;
     *         <code>false</code> if it is sorted descending
     */
    public boolean isSortedAscending() {
        return (_order == null) || _order.isAscending();
    }

    /**
     * Returns the sort criteria.
     *
     * @return the sort criteria
     */
    public SortOrder getSortOrder() {
        return _order;
    }

    /**
     * Sets the sort criteria.
     *
     * @param order the sort criteria
     */
    protected void setSortOrder(SortOrder order) {
        _order = order;
    }

    /**
     * Returns the query constraints.
     *
     * @return the query constraints
     */
    protected IConstraint getConstraints() {
        return _constraints;
    }

    /**
     * Helper to convert a page from one type to another. <strong>Use with
     * caution</strong>: No attempt is made to verify the contents of the page.
     *
     * @param page the page to convert
     * @return the converted page
     */
    @SuppressWarnings("unchecked")
    protected final <K, T> IPage<K> convert(IPage<T> page) {
        return (IPage<K>) page;
    }
}
