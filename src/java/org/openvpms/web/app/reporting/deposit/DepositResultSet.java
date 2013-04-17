/*
 * Version: 1.0
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
 *  Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.reporting.deposit;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.AbstractEntityResultSet;
import org.openvpms.web.component.im.query.DefaultQueryExecutor;

import java.util.Date;


/**
 * A query for <em>party.organisationDeposit</em> objects that optionally constrains them to a specified
 * <em>party.organisationLocation</em>.
 *
 * @author Tim Anderson
 */
public class DepositResultSet extends AbstractEntityResultSet<Party> {

    /**
     * The practice location. May be {@code null}.
     */
    private final Party location;

    /**
     * Constructs a {@code DepositResultSet}.
     *
     * @param location         the practice location. May be {@code null}
     * @param archetypes       the archetypes to query
     * @param value            the value to query on. May be {@code null}
     * @param searchIdentities if {@code true} search on identity name
     * @param constraints      additional query constraints. May be {@code null}
     * @param sort             the sort criteria. May be {@code null}
     * @param rows             the maximum no. of rows per page
     * @param distinct         if {@code true} filter duplicate rows
     */
    public DepositResultSet(Party location, ShortNameConstraint archetypes, String value, boolean searchIdentities,
                            IConstraint constraints, SortConstraint[] sort, int rows, boolean distinct) {
        super(archetypes, value, searchIdentities, constraints, sort, rows, distinct,
              new DefaultQueryExecutor<Party>());
        this.location = location;
    }

    /**
     * Creates a new archetype query.
     *
     * @return a new archetype query
     */
    @Override
    protected ArchetypeQuery createQuery() {
        ArchetypeQuery query = super.createQuery();

        if (location != null) {
            Date now = new Date();
            query.add(Constraints.join("locations",
                                       Constraints.shortName("rel", "entityRelationship.locationDeposit")));
            query.add(Constraints.eq("rel.source", location.getObjectReference()));
            query.add(Constraints.lte("rel.activeStartTime", now));
        }
        return query;
    }

}
