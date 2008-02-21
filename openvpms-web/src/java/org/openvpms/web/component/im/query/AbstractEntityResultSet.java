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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.query;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.JoinConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Result set for {@link Entity} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-08-22 05:39:25Z $
 */
public abstract class AbstractEntityResultSet<T> extends NameResultSet<T> {

    /**
     * Identity short names.
     */
    private String[] identityShortNames;


    /**
     * Creates a new <tt>AbstractEntityResultSet</tt>.
     *
     * @param archetypes       the archetypes to query
     * @param instanceName     the instance name. May be <tt>null</tt>
     * @param searchIdentities if <tt>true</tt> search on identity name
     * @param constraints      additional query constraints. May be
     *                         <tt>null</tt>
     * @param sort             the sort criteria. May be <tt>null</tt>
     * @param rows             the maximum no. of rows per page
     * @param distinct         if <tt>true</tt> filter duplicate rows
     */
    public AbstractEntityResultSet(ShortNameConstraint archetypes,
                                   String instanceName,
                                   boolean searchIdentities,
                                   IConstraint constraints,
                                   SortConstraint[] sort,
                                   int rows, boolean distinct,
                                   QueryExecutor<T> executor) {
        super(archetypes, instanceName, constraints, sort, rows, distinct,
              executor);
        if (!StringUtils.isEmpty(instanceName) && searchIdentities) {
            Set<String> shortNames = new HashSet<String>();
            List<ArchetypeDescriptor> matches = getArchetypes(archetypes);
            boolean identities = true;
            for (ArchetypeDescriptor archetype : matches) {
                NodeDescriptor ident
                        = archetype.getNodeDescriptor("identities");
                if (ident == null) {
                    identities = false;
                    break;
                } else {
                    for (String name : DescriptorHelper.getShortNames(ident)) {
                        shortNames.add(name);
                    }
                }
            }
            if (identities) {
                identityShortNames = shortNames.toArray(new String[0]);
                setDistinct(true);
            }
        }
    }

    /**
     * Creates a new archetype query.
     *
     * @return a new archetype query
     */
    @Override
    protected ArchetypeQuery createQuery() {
        ArchetypeQuery query = new ArchetypeQuery(getArchetypes());
        String name = getInstanceName();
        if (!StringUtils.isEmpty(name)) {
            NodeConstraint nameConstraint = new NodeConstraint("name", name);
            if (identityShortNames != null) {
                // querying on identity
                CollectionNodeConstraint idConstraint
                        = new CollectionNodeConstraint("identities",
                                                       identityShortNames,
                                                       false, true);
                idConstraint.setJoinType(JoinConstraint.JoinType.LeftOuterJoin);
                idConstraint.add(nameConstraint);
                query.add(idConstraint);
            } else {
                query.add(nameConstraint);
            }
        }
        return query;
    }

    /**
     * Adds sort constraints.
     * This implementation adds all those returned by
     * {@link #getSortConstraints()}, and finally adds a sort on <em>uid</em>
     * to guarantee that subsequent queries will return results in the same
     * order.
     *
     * @param query the query to add the constraints to
     */
    @Override
    protected void addSortConstraints(ArchetypeQuery query) {
        super.addSortConstraints(query);
        String alias = getArchetypes().getAlias();
        query.add(new NodeSortConstraint(alias, "uid"));
    }

}
