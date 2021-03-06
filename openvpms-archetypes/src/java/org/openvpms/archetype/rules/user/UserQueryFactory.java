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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.user;

import org.openvpms.component.model.party.Party;
import org.openvpms.component.system.common.query.ArchetypeIdConstraint;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.BaseArchetypeConstraint;
import org.openvpms.component.system.common.query.Constraints;

import static org.openvpms.archetype.rules.user.UserArchetypes.USER;
import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.idEq;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.leftJoin;
import static org.openvpms.component.system.common.query.Constraints.notExists;
import static org.openvpms.component.system.common.query.Constraints.or;
import static org.openvpms.component.system.common.query.Constraints.subQuery;

/**
 * User query factory.
 *
 * @author Tim Anderson
 */
public class UserQueryFactory {

    /**
     * Creates a query to return users, sorted by id.
     *
     * @param location if non-null, return all users that have a link to the location or no link to any location
     * @param sort     sort constraints. If not specified, the query results will be unsorted
     * @return a new query
     */
    public static ArchetypeQuery createUserQuery(Party location, String... sort) {
        ArchetypeQuery query = new ArchetypeQuery(USER, true, true);
        addLocationConstraint(location, query);
        if (sort.length != 0) {
            addSort(query, sort);
        }
        return query;
    }

    /**
     * Creates a query to return clinicians.
     *
     * @param location if non-null, return all clinicians that have a link to the location or no link to any location
     * @param sort     sort constraints. If not specified, the query results will be unsorted
     * @return a new query
     */
    public static ArchetypeQuery createClinicianQuery(Party location, String... sort) {
        ArchetypeQuery query = new ArchetypeQuery(USER, true, true);
        addClinicianConstraint(query);
        addLocationConstraint(location, query);
        if (sort.length != 0) {
            addSort(query, sort);
        }
        return query;
    }

    /**
     * Adds a clinician constraint to a query.
     * <p>
     * The primary query must be on <em>security.user</em> archetypes.
     *
     * @param query the query
     * @return the query
     */
    public static ArchetypeQuery addClinicianConstraint(ArchetypeQuery query) {
        query.add(join("classifications", new ArchetypeIdConstraint("lookup.userType")).add(eq("code", "CLINICIAN")));
        return query;
    }

    /**
     * Adds a clinician location constraint to a query.
     * <p>
     * The primary query must be on <em>security.user</em> archetypes.
     *
     * @param location the practice location. May be {@code null}
     * @param query    the query
     * @return the query
     */
    public static ArchetypeQuery addLocationConstraint(Party location, ArchetypeQuery query) {
        if (location != null) {
            BaseArchetypeConstraint archetype = query.getArchetypeConstraint();
            String alias = archetype.getAlias();
            if (alias == null) {
                alias = "u";
                archetype.setAlias(alias);
            }
            query.add(leftJoin("locations", "l"));
            query.add(or(eq("l.target", location),
                         notExists(subQuery(USER, "u2").add(join("locations", "l2").add(idEq(alias, "u2"))))));
        }
        return query;
    }

    /**
     * Adds ascending sort constraints to a query.
     *
     * @param query the query
     * @param sort  the nodes to sort on
     */
    private static void addSort(ArchetypeQuery query, String[] sort) {
        for (String node : sort) {
            query.add(Constraints.sort(node));
        }
    }

}
