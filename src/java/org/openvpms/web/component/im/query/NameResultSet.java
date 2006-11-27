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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.BaseArchetypeConstraint;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.LongNameConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.spring.ServiceHelper;

import java.util.ArrayList;
import java.util.List;


/**
 * Result set that queries on archetype and instance name.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class NameResultSet<T extends IMObject>
        extends AbstractArchetypeServiceResultSet<T> {

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
     * Construct a new <code>NameResultSet</code>.
     *
     * @param archetypes   the archetypes to query
     * @param instanceName the instance name
     * @param constraints  additional query constraints. May be
     *                     <code<null</code>
     * @param sort         the sort criteria. May be <code>null</code>
     * @param rows         the maximum no. of rows per page
     * @param distinct     if <code>true</code> filter duplicate rows
     */
    public NameResultSet(BaseArchetypeConstraint archetypes,
                         String instanceName, IConstraint constraints,
                         SortConstraint[] sort, int rows, boolean distinct) {
        super(constraints, rows, sort);
        _archetypes = archetypes;
        _instanceName = instanceName;
        setDistinct(distinct);
    }

    /**
     * Returns the specified page.
     *
     * @param firstResult the first result of the page to retrieve
     * @param maxResults  the maximun no of results in the page
     * @return the page corresponding to <code>firstResult</code>, or
     *         <code>null</code> if none exists
     */
    protected IPage<T> getPage(int firstResult, int maxResults) {
        IPage<IMObject> result = null;
        try {
            IArchetypeService service = ServiceHelper.getArchetypeService();
            ArchetypeQuery query = getQuery(_archetypes, _instanceName);
            query.setFirstResult(firstResult);
            query.setMaxResults(maxResults);
            query.setDistinct(isDistinct());
            for (SortConstraint sort : getSortConstraints()) {
                query.add(sort);
            }
            result = service.get(query);
        } catch (OpenVPMSException exception) {
            _log.error(exception, exception);
        }
        return convert(result);
    }

    /**
     * Returns the query.
     *
     * @param archetypes the archetype constraint
     * @param name       the name. May be <code>null</code>
     * @return the query
     */
    protected ArchetypeQuery getQuery(BaseArchetypeConstraint archetypes,
                                      String name) {
        ArchetypeQuery query = new ArchetypeQuery(archetypes);
        query.setDistinct(isDistinct());
        query.setCountResults(true);
        if (!StringUtils.isEmpty(name)) {
            query.add(new NodeConstraint("name", name));
        }
        IConstraint constraints = getConstraints();
        if (constraints != null) {
            query.add(constraints);
        }
        return query;
    }

    /**
     * Returns all archetypes matching supplied constraint.
     *
     * @param archetypes the archetype constraint
     * @return the archetypes matching the constraint
     * @throws OpenVPMSException for any error
     */
    protected List<ArchetypeDescriptor> getArchetypes(
            BaseArchetypeConstraint archetypes) {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        String[] shortNames;
        if (archetypes instanceof LongNameConstraint) {
            LongNameConstraint constraint = (LongNameConstraint) archetypes;
            shortNames = DescriptorHelper.getShortNames(
                    constraint.getRmName(), constraint.getEntityName(),
                    constraint.getConceptName());
        } else if (archetypes instanceof ShortNameConstraint) {
            ShortNameConstraint constraint = (ShortNameConstraint) archetypes;
            shortNames = DescriptorHelper.getShortNames(
                    constraint.getShortNames());
        } else {
            shortNames = new String[0];
        }
        List<ArchetypeDescriptor> result
                = new ArrayList<ArchetypeDescriptor>(shortNames.length);
        for (String shortName : shortNames) {
            ArchetypeDescriptor archetype = service.getArchetypeDescriptor(
                    shortName);
            if (archetype != null) {
                result.add(archetype);
            }
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
        List<ArchetypeDescriptor> archetypes = getArchetypes(_archetypes);
        NodeDescriptor descriptor;

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

}
