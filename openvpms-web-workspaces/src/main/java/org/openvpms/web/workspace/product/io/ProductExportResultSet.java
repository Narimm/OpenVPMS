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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.product.io;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.product.ProductResultSet;

import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.join;

/**
 * Product export result set.
 *
 * @author Tim Anderson
 */
public class ProductExportResultSet extends ProductResultSet {

    /**
     * The product type. May be {@code null}
     */
    private final Entity productType;

    /**
     * Constructs a {@link ProductExportResultSet}.
     *
     * @param archetypes       the archetypes to query
     * @param value            the value to query on. May be {@code null}
     * @param searchIdentities if {@code true} search on identity name
     * @param species          the species. May be {@code null}
     * @param productType      the product type. May be {@code null}
     * @param stockLocation    the stock location. May be {@code null}
     * @param sort             the sort criteria. May be {@code null}
     * @param rows             the maximum no. of rows per page
     */
    public ProductExportResultSet(ShortNameConstraint archetypes, String value, boolean searchIdentities,
                                  String species, Entity productType, Party stockLocation, SortConstraint[] sort,
                                  int rows) {
        super(archetypes, value, searchIdentities, species, stockLocation, sort, rows);
        this.productType = productType;
    }

    /**
     * Creates a new archetype query.
     *
     * @return a new archetype query
     */
    @Override
    protected ArchetypeQuery createQuery() {
        ArchetypeQuery query = super.createQuery();
        if (productType != null) {
            query.add(join("type").add(eq("source", productType.getObjectReference())));
        }
        return query;
    }
}
