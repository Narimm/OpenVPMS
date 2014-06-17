/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.supplier.vet;

import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.EntityObjectSetResultSet;
import org.openvpms.web.component.im.query.ResultSet;

import java.util.Date;
import java.util.List;

import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.gte;
import static org.openvpms.component.system.common.query.Constraints.isNull;
import static org.openvpms.component.system.common.query.Constraints.leftJoin;
import static org.openvpms.component.system.common.query.Constraints.lte;
import static org.openvpms.component.system.common.query.Constraints.or;

/**
 * A {@link ResultSet} implementation that queries entities.
 * <p/>
 * The returned {@link ObjectSet ObjectSet}s contain the following:
 * <ul>
 * <li><em>entity</em> - the vet's object reference</li>
 * <li><em>entity.name</em> - the vet's name</li>
 * <li><em>entity.description</em> - the vet's description</li>
 * <li><em>entity.active</em> - the vet's active state</li>
 * <li><em>practice.name</em> - the vet's practice</li>
 * </ul>
 *
 * @author Tim Anderson
 */
public class VetResultSet extends EntityObjectSetResultSet {

    /**
     * Constructs an {@link VetResultSet}.
     *
     * @param archetypes       the archetypes to query
     * @param value            the value to query on. May be {@code null}
     * @param searchIdentities if {@code true} search on identity name
     * @param sort             the sort criteria. May be {@code null}
     * @param rows             the maximum no. of rows per page
     * @param distinct         if {@code true} filter duplicate rows
     */

    public VetResultSet(ShortNameConstraint archetypes, String value, boolean searchIdentities,
                        SortConstraint[] sort, int rows, boolean distinct) {
        super(archetypes, value, searchIdentities, sort, rows, distinct);
    }

    /**
     * Creates a new archetype query.
     *
     * @return a new archetype query
     */
    @Override
    protected ArchetypeQuery createQuery() {
        ArchetypeQuery query = new ArchetypeQuery(getArchetypes());
        Date now = new Date();
        query.add(leftJoin("practices")
                          .add(lte("activeStartTime", now))
                          .add(or(gte("activeEndTime", now), isNull("activeEndTime")))
                          .add(leftJoin("source", "practice")));
        if (isSearchingIdentities()) {
            addIdentityConstraints(query);
        } else {
            addValueConstraints(query);
        }
        addSelectConstraints(query);
        query.add(new NodeSelectConstraint("practice.name"));
        return query;
    }

    /**
     * Creates constraints to query nodes on a value.
     *
     * @param value the value to query
     * @param nodes the nodes to query
     * @return the constraints
     */
    @Override
    protected List<IConstraint> createValueConstraints(String value, List<String> nodes) {
        List<IConstraint> result = super.createValueConstraints(value, nodes);
        result.add(eq("practice.name", value));
        return result;
    }
}
