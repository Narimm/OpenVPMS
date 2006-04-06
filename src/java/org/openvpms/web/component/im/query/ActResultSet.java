package org.openvpms.web.component.im.query;

import java.util.Date;

import org.apache.commons.lang.StringUtils;

import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
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
                        int rows, SortOrder order) {
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
     * @param firstRow the first row of the page to retrieve
     * @param maxRows  the maximun no of rows in the page
     * @return the page corresponding to <code>firstRow</code>, or
     *         <code>null</code> if none exists
     */
    protected IPage<Act> getPage(int firstRow, int maxRows) {
        IPage<Act> result = null;
        try {

            IArchetypeService service = ServiceHelper.getArchetypeService();
            ArchetypeQuery query = new ArchetypeQuery(null, _entityName,
                                                      _conceptName,
                                                      true, true);
            query.setFirstRow(firstRow);
            query.setNumOfRows(maxRows);

            if (!StringUtils.isEmpty(_status)) {
                query.add(new NodeConstraint("status", RelationalOp.EQ, _status));
            }

            if (_startFrom != null && _startTo != null) {
                query.add(new NodeConstraint("startTime", RelationalOp.BTW,
                                             new Object[]{_startFrom, _startTo}));
            }

            CollectionNodeConstraint participations = new CollectionNodeConstraint(
                    "participants", "participation.customer", true, true)
                    .setJoinType(CollectionNodeConstraint.JoinType.LeftOuterJoin)
                    .add(new ObjectRefNodeConstraint("entity", _entityId));
            query.add(participations);

/*
            result = (IPage<Act>) ArchetypeQueryHelper.getActs(
                    service, _entityId, "participation.customer", _entityName,
                    _conceptName,
                    _startFrom, _startTo, null, null,
                    _status, true, firstRow, maxRows);
*/
            IPage<IMObject> page = service.get(query);
            result = convert(page);
        } catch (ArchetypeServiceException exception) {
            ErrorDialog.show(exception);
        }
        return result;
    }

}
