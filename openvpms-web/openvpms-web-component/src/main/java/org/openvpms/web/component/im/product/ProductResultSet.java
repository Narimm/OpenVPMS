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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.product;

import org.openvpms.archetype.rules.product.ProductQueryFactory;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.EntityResultSet;

/**
 * Product result set that supports queries filtering on species and/or location or stock location..
 *
 * @author Tim Anderson
 */
public class ProductResultSet extends EntityResultSet<Product> {

    /**
     * The species lookup code. May be {@code null}
     */
    private final String species;

    /**
     * Determines if products should be restricted to those available at the location or stock location.
     */
    private final boolean useLocationProducts;

    /**
     * The location, used to filter service and template products. May be {@code null}
     * <br/>
     * Ignored if {@code useLocationProducts} is {@code false}. If {@code useLocationProducts} is {@code true}, an
     * product that has a relationship to the location is excluded. This is because products list locations for
     * exclusion rather than inclusion, for efficiency purposes.
     */
    private final Party location;

    /**
     * The stock location, used to filter medication and merchandise products. May be {@code null}.
     * <br/>
     * If set, and {@code useLocationProducts} is {@code false}, products must either have the stock location, or no
     * stock location. This is to support supplier orders and deliveries.
     * <br/>.
     * If set, and {@code useLocationProducts} is {@code true}, products must have the stock location. This is to
     * support customer and patient acts filtering products by stock location.
     */
    private final Party stockLocation;


    /**
     * Constructs a {@link ProductResultSet}.
     *
     * @param archetypes          the archetypes to query
     * @param value               the value to query on. May be {@code null}
     * @param searchIdentities    if {@code true} search on identity name
     * @param species             id {@code non-null}, only include those products for the species, or those that have
     *                            no species classifications
     * @param useLocationProducts if {@code true}, products should be restricted to those available at the location or
     *                            stock location
     * @param location            the location used to exclude service and template products. May be {@code null}.
     *                            Only relevant when {@code useLocationProducts == true}
     * @param stockLocation       if {@code useLocationProducts == false}, products must either have the stock location,
     *                            or no stock location. If {@code useLocationProducts == true}, products must have the
     *                            stock location
     * @param sort                the sort criteria. May be {@code null}
     * @param rows                the maximum no. of rows per page
     */
    public ProductResultSet(ShortNameConstraint archetypes, String value, boolean searchIdentities, String species,
                            boolean useLocationProducts, Party location, Party stockLocation, SortConstraint[] sort,
                            int rows) {
        super(archetypes, value, searchIdentities, null, sort, rows, true);
        this.species = species;
        this.location = location;
        this.stockLocation = stockLocation;
        this.useLocationProducts = useLocationProducts;
    }

    /**
     * Creates a new archetype query.
     *
     * @return a new archetype query
     */
    @Override
    protected ArchetypeQuery createQuery() {
        getArchetypes().setAlias("p");
        String[] archetypes = getArchetypes().getShortNames();
        ArchetypeQuery query = super.createQuery();
        if (species != null) {
            ProductQueryFactory.addSpeciesConstraint(query, archetypes, species);
        }
        if (useLocationProducts && location != null) {
            ProductQueryFactory.addLocationConstraint(query, archetypes, location);
        }
        if (stockLocation != null) {
            ProductQueryFactory.addStockLocationConstraint(query, archetypes, useLocationProducts, stockLocation);
        }
        return query;
    }

}
