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
 *  Copyright 2007-2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.product;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.AbstractEntityQuery;
import org.openvpms.web.component.im.query.ResultSet;


/**
 * Query implementation that queries {@link Product} instances on short name,
 * instance name, active/inactive status, species and location.
 *
 * @author Tim Anderson
 */
public class ProductQuery extends AbstractEntityQuery<Product> {

    /**
     * The species to constrain the query to. May be {@code null}.
     */
    private String species;

    /**
     * The stock location to constrain the query to. May be {@code null}.
     */
    private Party location;


    /**
     * Construct a new {@code ProductQuery} that queries products with the
     * specified short names.
     *
     * @param shortNames the short names
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    public ProductQuery(String[] shortNames) {
        super(shortNames, Product.class);
    }

    /**
     * Sets the species to constrain the query to.
     *
     * @param species the species classification
     */
    public void setSpecies(String species) {
        this.species = species;
    }

    /**
     * Sets the stock location to constrain the query to.
     *
     * @param location the stock location
     */
    public void setStockLocation(Party location) {
        this.location = location;
    }

    /**
     * Creates the result set.
     *
     * @param sort the sort criteria. May be {@code null}
     * @return a new result set
     */
    protected ResultSet<Product> createResultSet(SortConstraint[] sort) {
        return new ProductResultSet(getArchetypeConstraint(), getValue(), isIdentitySearch(), species, location,
                                    sort, getMaxResults());
    }

}
