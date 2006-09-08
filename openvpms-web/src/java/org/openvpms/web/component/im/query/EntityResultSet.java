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
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.BaseArchetypeConstraint;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import static org.openvpms.component.system.common.query.CollectionNodeConstraint.JoinType.LeftOuterJoin;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.OrConstraint;
import org.openvpms.component.system.common.query.SortConstraint;

import java.util.List;


/**
 * Result set for {@link Entity} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-08-22 05:39:25Z $
 */
public class EntityResultSet extends NameResultSet<Entity> {

    /**
     * Determines if queries on name should include the 'identities' node.
     * True if all archetypes have a 'identities' node.
     */
    private final boolean _hasIdentities;


    /**
     * Construct a new <code>EntityResultSet</code>.
     *
     * @param archetypes   the archetypes to query
     * @param instanceName the instance name
     * @param constraints  additional query constraints. May be
     *                     <code<null</code>
     * @param sort         the sort criteria. May be <code>null</code>
     * @param rows         the maximum no. of rows per page
     * @param distinct     if <code>true</code> filter duplicate rows
     */
    public EntityResultSet(BaseArchetypeConstraint archetypes,
                           String instanceName,
                           IConstraint constraints, SortConstraint[] sort,
                           int rows,
                           boolean distinct) {
        super(archetypes, instanceName, constraints, sort, rows, distinct);
        List<ArchetypeDescriptor> matches = getArchetypes(archetypes);
        if (!StringUtils.isEmpty(instanceName)) {
            boolean identities = true;
            for (ArchetypeDescriptor archetype : matches) {
                if (archetype.getNodeDescriptor("identities") == null) {
                    identities = false;
                    break;
                }
            }
            if (identities) {
                setDistinct(true);
            }
            _hasIdentities = identities;
        } else {
            _hasIdentities = false;
        }
    }

    /**
     * Returns the query.
     *
     * @param archetypes the archetype constraint
     * @param name       the name. May be <code>null</code>
     * @return the query
     */
    @Override
    protected ArchetypeQuery getQuery(BaseArchetypeConstraint archetypes,
                                      String name) {
        ArchetypeQuery query;
        if (_hasIdentities && name.matches(".*\\d+.*")) {
            query = new ArchetypeQuery(archetypes);
            CollectionNodeConstraint constraint
                    = new CollectionNodeConstraint("identities");
            constraint.setJoinType(LeftOuterJoin);
            constraint.add(new NodeConstraint("name", name));
            query.add(constraint);

            IConstraint constraints = getConstraints();
            if (constraints != null) {
                query.add(constraints);
            }
        } else {
            query = super.getQuery(archetypes, name);
        }
        return query;
    }

}
