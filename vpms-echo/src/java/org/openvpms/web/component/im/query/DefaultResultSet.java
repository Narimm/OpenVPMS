package org.openvpms.web.component.im.query;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.search.IPage;
import org.openvpms.component.system.common.search.PagingCriteria;
import org.openvpms.component.system.common.search.SortCriteria;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.spring.ServiceHelper;


/**
 * Result set where archetype names are used as the criteria.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class DefaultResultSet extends AbstractArchetypeServiceResultSet<IMObject> {

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
     * The logger.
     */
    private final Log _log = LogFactory.getLog(DefaultResultSet.class);


    /**
     * Construct a new <code>DefaultResultSet</code>.
     *
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     * @param instanceName the instance name
     * @param activeOnly   determines if active and/or inactive results should
     *                     be retrieved
     * @param order        the sort criteria. May be <code>null</code>
     * @param rows         the maximum no. of rows per page
     */
    public DefaultResultSet(String refModelName, String entityName,
                            String conceptName, String instanceName,
                            boolean activeOnly,
                            SortCriteria order,
                            int rows) {
        super(rows, order);
        _refModelName = refModelName;
        _entityName = entityName;
        _conceptName = conceptName;
        _instanceName = instanceName;
        _activeOnly = activeOnly;
        if (order != null && isValidSortNode(order.getSortNode())) {
            setSortCriteria(null);
        }

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
        if (isValidSortNode(node)) {
            super.sort(node, ascending);
        } else {
            setSortCriteria(null);
            reset();
        }
    }

    /**
     * Returns the specified page.
     *
     * @param criteria the paging criteria
     * @return the page corresponding to <code>page</code>, or <code>null</code>
     *         if none exists
     */
    protected IPage<IMObject> getPage(PagingCriteria criteria) {
        IPage<IMObject> result = null;
        try {
            IArchetypeService service = ServiceHelper.getArchetypeService();
            result = service.get(_refModelName, _entityName, _conceptName,
                                 _instanceName, true, _activeOnly, criteria,
                                 getSortCriteria());
        } catch (ArchetypeServiceException exception) {
            ErrorDialog.show(exception);
        }
        return result;
    }

    /**
     * Determines if a node can be sorted on.
     *
     * @param node the node
     * @return <code>true</code> if the node can be sorted on, otherwise
     *         <code>false</code>
     */
    private boolean isValidSortNode(String node) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        List<ArchetypeDescriptor> archetypes = getArchetypes(service);
        NodeDescriptor descriptor = null;

        for (ArchetypeDescriptor archetype : archetypes) {
            descriptor = archetype.getNodeDescriptor(node);
            if (descriptor == null) {
                _log.warn("Can't sort results on node=" + node
                          + ". Node not supported by archetype="
                          + archetype.getName());
                return false;
            }
        }
        return true;
    }

    /**
     * Returns all archetypes matching the reference model, entity and concept
     * names.
     *
     * @param service the archetype service
     * @return the archetypes matching the names
     */
    private List<ArchetypeDescriptor> getArchetypes(IArchetypeService service) {
        List<String> shortNames = service.getArchetypeShortNames(
                _refModelName, _entityName, _conceptName, true);
        List<ArchetypeDescriptor> archetypes
                = new ArrayList<ArchetypeDescriptor>(shortNames.size());
        for (String shortName : shortNames) {
            ArchetypeDescriptor archetype = service.getArchetypeDescriptor(shortName);
            if (archetype != null) {
                archetypes.add(archetype);
            }
        }

        return archetypes;
    }


}
