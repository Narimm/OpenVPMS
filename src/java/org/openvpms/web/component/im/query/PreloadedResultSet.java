package org.openvpms.web.component.im.query;

import java.util.ArrayList;
import java.util.List;

import org.openvpms.component.business.dao.im.Page;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.search.IPage;
import org.openvpms.component.system.common.search.PagingCriteria;


/**
 * Paged query, where the result set is pre-loaded.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class PreloadedResultSet extends AbstractResultSet {

    /**
     * The query objects.
     */
    private final List<IMObject> _objects;


    /**
     * Construct a new <code>PreloadedResultSet</code>.
     *
     * @param objects the objects
     * @param rows    the maximum no. of rows per page
     */
    public PreloadedResultSet(List<IMObject> objects, int rows) {
        super(rows);
        _objects = objects;

        reset();
    }

    /**
     * No sorting is possible with this set. This implementation only resets the
     * iterator.
     *
     * @param node      the node to sort on
     * @param ascending if <code>true</code> sort the column ascending order;
     *                  otherwise sort it in <code>descebding</code> order
     */
    public void sort(String node, boolean ascending) {
        // no-op
        reset();
    }

    /**
     * Returns the node the set was sorted on.
     *
     * @return the sort node, or <code>null</code> if the set is unsorted
     */
    public String getSortNode() {
        return null;
    }

    /**
     * Determines if the node is sorted ascending or descending.
     *
     * @return <code>true</code> if the node is sorted ascending;
     *         <code>false</code> if it is sorted descending
     */
    public boolean isSortedAscending() {
        return true;
    }

    /**
     * Returns the specified page.
     *
     * @param criteria the paging criteria
     * @return the page corresponding to <code>page</code>, or <code>null</code>
     *         if none exists
     */
    protected IPage<IMObject> getPage(PagingCriteria criteria) {
        int from = criteria.getFirstRow();
        int count = criteria.getNumOfRows();
        int to;
        if (count == PagingCriteria.ALL_ROWS
            || ((from + count) >= _objects.size())) {
            to = _objects.size();
        } else {
            to = from + count;
        }
        List<IMObject> rows
                = new ArrayList<IMObject>(_objects.subList(from, to));
        return new Page<IMObject>(rows, criteria, _objects.size());
    }
}
