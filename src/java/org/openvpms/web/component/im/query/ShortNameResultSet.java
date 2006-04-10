package org.openvpms.web.component.im.query;

import org.apache.commons.lang.StringUtils;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.spring.ServiceHelper;


/**
 * Result set where short names are used as the criteria.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class ShortNameResultSet
        extends AbstractArchetypeServiceResultSet<IMObject> {

    /**
     * The instance name.
     */
    private final String _instanceName;

    /**
     * Short names to query on.
     */
    private final String[] _shortNames;

    /**
     * Determines if only active records should be included.
     */
    private final boolean _activeOnly;


    /**
     * Construct a new <code>ShortNameResultSet</code>.
     *
     * @param shortNames   the short names to query on.
     * @param instanceName the instance name
     * @param activeOnly   determines if active and/or inactive results should
     *                     be retrieved
     * @param constraints  additional query constraints. May be
     *                     <code<null</code>
     * @param order        the sort criteria. May be <code>null</code>
     * @param rows         the maximum no. of rows per page
     */
    public ShortNameResultSet(String[] shortNames, String instanceName,
                              boolean activeOnly, IConstraint constraints,
                              SortOrder order, int rows) {
        super(constraints, rows, order);
        _shortNames = shortNames;
        _instanceName = instanceName;
        _activeOnly = activeOnly;

        reset();
    }

    /**
     * Returns the specified page.
     *
     * @param firstRow
     * @param maxRows
     * @return the page corresponding to <code>page</code>, or <code>null</code>
     *         if none exists
     */
    protected IPage<IMObject> getPage(int firstRow, int maxRows) {
        IPage<IMObject> result = null;
        try {
            IArchetypeService service = ServiceHelper.getArchetypeService();
            ArchetypeQuery query = new ArchetypeQuery(_shortNames, true,
                                                      _activeOnly);
            if (!StringUtils.isEmpty(_instanceName)) {
                query.add(new NodeConstraint("name", _instanceName));
            }
            IConstraint constraints = getConstraints();
            if (constraints != null) {
                query.add(constraints);
            }
            query.setFirstRow(firstRow);
            query.setNumOfRows(maxRows);
            result = service.get(query);
        } catch (ArchetypeServiceException exception) {
            ErrorDialog.show(exception);
        }
        return result;
    }

}
