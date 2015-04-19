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

package org.openvpms.web.workspace.product.stock;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ObjectSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.AbstractEntityResultSet;
import org.openvpms.web.component.im.query.ObjectSetQueryExecutor;

import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.shortName;

/**
 * Stock export result set.
 * <p/>
 * This returns {@link ObjectSet}s with the following attributes:
 * <ul>
 * <li>product - the product</li>
 * <li>relationship - the entityRelationship.productStockLocation</li>
 * </ul>
 *
 * @author Tim Anderson
 */

public class StockExportResultSet extends AbstractEntityResultSet<ObjectSet> {

    /**
     * The stock location.
     */
    private final Entity stockLocation;

    /**
     * The product type. May be {@code null}
     */
    private final Entity productType;

    /**
     * The product income type code. May be {@code null}.
     */
    private final String incomeType;

    /**
     * The product group code. May be {@code null}.
     */
    private final String productGroup;

    /**
     * Constructs a {@link StockExportResultSet}.
     *
     * @param archetypes       the archetypes to query
     * @param value            the value to query on. May be {@code null}
     * @param searchIdentities if {@code true} search on identity name
     * @param sort             the sort criteria. May be {@code null}
     * @param rows             the maximum no. of rows per page
     */
    public StockExportResultSet(ShortNameConstraint archetypes, String value, boolean searchIdentities,
                                Entity stockLocation, Entity productType, String incomeType,
                                String productGroup, SortConstraint[] sort, int rows) {
        super(archetypes, value, searchIdentities, null, sort, rows, true, new ObjectSetQueryExecutor());
        archetypes.setAlias("product");
        this.stockLocation = stockLocation;
        this.productType = productType;
        this.incomeType = incomeType;
        this.productGroup = productGroup;
    }

    /**
     * Creates a new archetype query.
     *
     * @return a new archetype query
     */
    @Override
    protected ArchetypeQuery createQuery() {
        ArchetypeQuery query = super.createQuery();
        query.add(join("stockLocations", "relationship").add(eq("relationship.target",
                                                                stockLocation.getObjectReference())));

        if (productType != null) {
            query.add(join("type").add(eq("source", productType.getObjectReference())));
        }
        if (incomeType != null) {
            query.add(join("classifications", shortName("incomeType", "lookup.productIncomeType"))
                              .add(eq("code", incomeType)));
        }
        if (productGroup != null) {
            query.add(join("classifications", shortName("productGroup", "lookup.productGroup"))
                              .add(eq("code", productGroup)));
        }
        query.add(new ObjectSelectConstraint("product"));
        query.add(new ObjectSelectConstraint("relationship"));
        return query;
    }

}
