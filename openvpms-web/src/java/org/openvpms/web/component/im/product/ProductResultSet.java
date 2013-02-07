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
 *  Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.component.im.product;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.EntityResultSet;

import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.idEq;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.leftJoin;
import static org.openvpms.component.system.common.query.Constraints.notExists;
import static org.openvpms.component.system.common.query.Constraints.or;
import static org.openvpms.component.system.common.query.Constraints.subQuery;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public class ProductResultSet extends EntityResultSet<Product> {

    private final String species;

    private final Party stockLocation;

    /**
     * Constructs a {@code EntityResultSet}.
     *
     * @param archetypes       the archetypes to query
     * @param value            the value to query on. May be {@code null}
     * @param searchIdentities if {@code true} search on identity name
     * @param sort             the sort criteria. May be {@code null}
     * @param rows             the maximum no. of rows per page
     */
    public ProductResultSet(ShortNameConstraint archetypes, String value, boolean searchIdentities,
                            String species, Party stockLocation, SortConstraint[] sort, int rows) {
        super(archetypes, value, searchIdentities, null, sort, rows, true);
        this.species = species;
        this.stockLocation = stockLocation;
    }

    /**
     * Creates a new archetype query.
     *
     * @return a new archetype query
     */
    @Override
    protected ArchetypeQuery createQuery() {
        getArchetypes().setAlias("p");
        String[] shortNames = getArchetypes().getShortNames();
        ArchetypeQuery query = super.createQuery();
        if (species != null) {
            query.add(leftJoin("species", "s"));
            query.add(or(eq("s.code", species),
                         notExists(
                                 subQuery(shortNames, "p2").add(join("species", "s2").add(idEq("p", "p2"))))));
        }
        if (stockLocation != null) {
            query.add(leftJoin("stockLocations", "l"));
            query.add(or(eq("l.target", stockLocation.getObjectReference()),
                         notExists(
                                 subQuery(shortNames, "p3").add(join("stockLocations", "l2").add(idEq("p", "p3"))))));
        }
        return query;
    }
}
