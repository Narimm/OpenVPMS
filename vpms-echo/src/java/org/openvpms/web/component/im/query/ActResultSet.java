package org.openvpms.web.component.im.query;

import java.util.Date;

import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.search.IPage;
import org.openvpms.component.system.common.search.PagingCriteria;
import org.openvpms.component.system.common.search.SortCriteria;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.spring.ServiceHelper;


/**
 * Result set for {@link Act}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class ActResultSet extends AbstractArchetypeServiceResultSet<Act> {

    /**
     * The id of the entity to search for.
     */
    private IMObjectReference _entityId;

    /**
     * The archetype entity name.
     */
    private final String _entityName;

    /**
     * The archetype conceptName.
     */
    private final String _conceptName;

    /**
     * The start-from date.
     */
    private final Date _startFrom;

    /**
     * The start-to date.
     */
    private final Date _startTo;

    /**
     * The act status.
     */
    private final String _status;


    /**
     * Construct a new <code>ActResultSet</code>.
     *
     * @param entityId    the id of the entity to search for
     * @param entityName  the act entity name
     * @param conceptName the act concept name
     * @param rows        the maximum no. of rows per page
     * @param order       the sort criteria. May be <code>null</code>
     */
    public ActResultSet(IMObjectReference entityId, String entityName,
                        String conceptName, Date from, Date to, String status,
                        int rows, SortCriteria order) {
        super(rows, order);
        _entityId = entityId;
        _entityName = entityName;
        _conceptName = conceptName;
        _startFrom = from;
        _startTo = to;
        _status = status;
    }

    /**
     * Returns the specified page.
     *
     * @param criteria the paging criteria
     * @return the page corresponding to <code>page</code>, or <code>null</code>
     *         if none exists
     */
    protected IPage<Act> getPage(PagingCriteria criteria) {
        IPage<Act> result = null;
        try {
            IArchetypeService service = ServiceHelper.getArchetypeService();
            result = service.getActs(_entityId, null, _entityName, _conceptName,
                                     _startFrom, _startTo, null, null,
                                     _status, true, criteria, getSortCriteria());
        } catch (ArchetypeServiceException exception) {
            ErrorDialog.show(exception);
        }
        return result;
    }

}
