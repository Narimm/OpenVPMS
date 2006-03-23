package org.openvpms.web.component.im.query;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.search.IPage;
import org.openvpms.component.system.common.search.PagingCriteria;
import org.openvpms.component.system.common.search.SortCriteria;
import org.openvpms.web.spring.ServiceHelper;


/**
 * Paged query.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class ResultSetImpl extends AbstractResultSet {

    /**
     * The archetype reference model name.
     */
    private final String _refModelName;

    /**
     * the archetype entity name.
     */
    private final String _entityName;

    /**
     * The archetype concept name.
     */
    private final String _conceptName;

    /**
     * The instance name.
     */
    private final String _instanceName;

    /**
     * Determines if only active records should be included.
     */
    private final boolean _activeOnly;

    /**
     * The sort criteria.
     */
    private SortCriteria _order;


    /**
     * Construct a new <code>ResultSetImpl</code>.
     *
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     * @param instanceName the instabce name
     * @param activeOnly   determines if active and/or inactive results should
     *                     be retrieved
     * @param order        the sort criteria. May be <code>null</code>
     * @param rows         the maximum no. of rows per page
     */
    public ResultSetImpl(String refModelName, String entityName,
                         String conceptName, String instanceName,
                         boolean activeOnly,
                         SortCriteria order,
                         int rows) {
        super(rows);
        _refModelName = refModelName;
        _entityName = entityName;
        _conceptName = conceptName;
        _instanceName = instanceName;
        _activeOnly = activeOnly;
        _order = order;

        reset();
    }

    /**
     * Sort the set. This resets the iterator.
     *
     * @param node      the node to sort on
     * @param ascending if <code>true</code> sort the column ascending order;
     *                  otherwise sort it in <code>descebding</code> order
     */
    public void sort(String node, boolean ascending) {
        SortCriteria.SortDirection direction = (ascending)
                                               ? SortCriteria.SortDirection.Ascending
                                               : SortCriteria.SortDirection.Descending;

        _order = new SortCriteria(node, direction);
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
        SortCriteria.SortDirection direction
                = (_order != null) ? _order.getSortDirection()
                  : SortCriteria.SortDirection.Ascending;

        return (direction == SortCriteria.SortDirection.Ascending);
    }

    /**
     * Returns the specified page.
     *
     * @param criteria the paging criteria
     * @return the page corresponding to <code>page</code>, or <code>null</code>
     *         if none exists
     */
    protected IPage<IMObject> getPage(PagingCriteria criteria) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        return service.get(_refModelName, _entityName, _conceptName,
                           _instanceName, true, _activeOnly, criteria, _order);
    }

}
