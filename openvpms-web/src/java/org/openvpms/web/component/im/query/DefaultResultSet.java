/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.query;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeLongNameConstraint;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ArchetypeShortNameConstraint;
import org.openvpms.component.system.common.query.BaseArchetypeConstraint;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.spring.ServiceHelper;


/**
 * Result set where archetype names are used as the criteria.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class DefaultResultSet extends AbstractArchetypeServiceResultSet<IMObject> {

    /**
     * The archetypes to query.
     */
    private final BaseArchetypeConstraint _archetypes;

    /**
     * The instance name.
     */
    private final String _instanceName;

    /**
     * The logger.
     */
    private final Log _log = LogFactory.getLog(DefaultResultSet.class);


    /**
     * Construct a new <code>DefaultResultSet</code>.
     *
     * @param archetypes   the archetypes to query
     * @param instanceName the instance name
     * @param constraints  additional query constraints. May be
     *                     <code<null</code>
     * @param sort         the sort criteria. May be <code>null</code>
     * @param rows         the maximum no. of rows per page
     */
    public DefaultResultSet(BaseArchetypeConstraint archetypes,
                            String instanceName, IConstraint constraints,
                            SortConstraint[] sort, int rows) {
        super(constraints, rows, sort);
        _archetypes = archetypes;
        _instanceName = instanceName;
        reset();
    }

    /**
     * Returns the specified page.
     *
     * @param firstRow the first row of the page to retrieve
     * @param maxRows  the maximun no of rows in the page
     * @return the page corresponding to <code>firstRow</code>, or
     *         <code>null</code> if none exists
     */
    protected IPage<IMObject> getPage(int firstRow, int maxRows) {
        IPage<IMObject> result = null;
        try {
            IArchetypeService service = ServiceHelper.getArchetypeService();
            ArchetypeQuery query = new ArchetypeQuery(_archetypes);
            if (!StringUtils.isEmpty(_instanceName)) {
                query.add(new NodeConstraint("name", _instanceName));
            }
            IConstraint constraints = getConstraints();
            if (constraints != null) {
                query.add(constraints);
            }
            for (SortConstraint sort : getSortConstraints()) {
                query.add(sort);
            }
            query.setFirstRow(firstRow);
            query.setNumOfRows(maxRows);
            result = service.get(query);
        } catch (OpenVPMSException exception) {
            _log.error(exception, exception);
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
        String[] shortNames;
        if (_archetypes instanceof ArchetypeLongNameConstraint) {
            ArchetypeLongNameConstraint constraint
                    = (ArchetypeLongNameConstraint) _archetypes;
            shortNames = DescriptorHelper.getShortNames(
                    constraint.getRmName(), constraint.getEntityName(),
                    constraint.getConceptName());
        } else if (_archetypes instanceof ArchetypeShortNameConstraint) {
            ArchetypeShortNameConstraint constraint
                    = (ArchetypeShortNameConstraint) _archetypes;
            shortNames = DescriptorHelper.getShortNames(
                    constraint.getShortNames());
        } else {
            shortNames = new String[0];
        }
        List<ArchetypeDescriptor> archetypes
                = new ArrayList<ArchetypeDescriptor>(shortNames.length);
        for (String shortName : shortNames) {
            ArchetypeDescriptor archetype = service.getArchetypeDescriptor(
                    shortName);
            if (archetype != null) {
                archetypes.add(archetype);
            }
        }

        return archetypes;
    }


}
