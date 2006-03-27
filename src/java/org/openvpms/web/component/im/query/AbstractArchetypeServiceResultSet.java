package org.openvpms.web.component.im.query;

import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.search.SortCriteria;


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
     * The sort criteria. May be <code>null</code>.
     */
    private SortCriteria _order;

    /**
     * Construct a new <code>AbstractArchetypeServiceResultSet</code>.
     *
     * @param rows  the maximum no. of rows per page
     * @param order the sort criteria. May be <code>null</code>
     */
    public AbstractArchetypeServiceResultSet(int rows, SortCriteria order) {
        super(rows);
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
        _order = new SortCriteria(node, ascending);
        reset();
    }

    /**
     * Returns the node the set was sorted on.
     *
     * @return the sort node, or <code>null</code> if the set is unsorted
     */
    public String getSortNode() {
        return (_order != null) ? _order.getSortNode() : null;
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
    public SortCriteria getSortCriteria() {
        return _order;
    }

    /**
     * Sets the sort criteria.
     *
     * @param order the sort criteria
     */
    protected void setSortCriteria(SortCriteria order) {
        _order = order;
    }

}
