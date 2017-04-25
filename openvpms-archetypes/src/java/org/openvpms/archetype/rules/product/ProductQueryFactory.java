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

package org.openvpms.archetype.rules.product;

import org.apache.commons.lang.ArrayUtils;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.OrConstraint;

import static org.openvpms.archetype.rules.product.ProductArchetypes.MEDICATION;
import static org.openvpms.archetype.rules.product.ProductArchetypes.MERCHANDISE;
import static org.openvpms.archetype.rules.product.ProductArchetypes.SERVICE;
import static org.openvpms.archetype.rules.product.ProductArchetypes.TEMPLATE;
import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.idEq;
import static org.openvpms.component.system.common.query.Constraints.isA;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.leftJoin;
import static org.openvpms.component.system.common.query.Constraints.not;
import static org.openvpms.component.system.common.query.Constraints.notExists;
import static org.openvpms.component.system.common.query.Constraints.or;
import static org.openvpms.component.system.common.query.Constraints.subQuery;


/**
 * Product query factory.
 *
 * @author Tim Anderson
 */
public class ProductQueryFactory {

    /**
     * Creates a new query for active products.
     *
     * @param archetypes          the product archetypes to query
     * @param name                the name. May be {@code null}, or contain wildcards
     * @param species             the species lookup code. May be {@code null}
     * @param useLocationProducts if {@code true}, products should be restricted to those available at the location or
     *                            stock location
     * @param location            the location used to exclude service and template products. May be {@code null}.
     *                            Only relevant when {@code useLocationProducts == true}
     * @param stockLocation       if {@code useLocationProducts == false}, products must either have the stock location,
     *                            or no stock location. If {@code useLocationProducts == true}, products must have the
     *                            stock location     @return a new query
     */
    public static ArchetypeQuery create(String[] archetypes, String name, String species, boolean useLocationProducts,
                                        Party location, Party stockLocation) {
        ArchetypeQuery query = new ArchetypeQuery(archetypes, true, true);
        query.getArchetypeConstraint().setAlias("p");
        if (name != null) {
            query.add(eq("name", name));
        }
        if (species != null) {
            addSpeciesConstraint(query, archetypes, species);
        }
        if (useLocationProducts && location != null) {
            addLocationConstraint(query, archetypes, location);
        }
        if (stockLocation != null) {
            addStockLocationConstraint(query, archetypes, useLocationProducts, stockLocation);
        }
        return query;
    }

    /**
     * Adds a constraint to only include those products for a species, or that have no species classifications.
     *
     * @param query      the query
     * @param archetypes the product archetypes being queried
     * @param species    the species lookup code
     * @return the query
     */
    public static ArchetypeQuery addSpeciesConstraint(ArchetypeQuery query, String[] archetypes, String species) {
        String alias = query.getArchetypeConstraint().getAlias();
        query.add(leftJoin("species", "s"));
        query.add(or(eq("s.code", species),
                     notExists(subQuery(archetypes, "p2").add(join("species", "s2").add(idEq(alias, "p2"))))));
        return query;
    }

    /**
     * Adds a location constraint if the product archetypes being queried include template or service products.
     * <p/>
     * This excludes any product that link to the location.
     *
     * @param query      the query
     * @param archetypes the product archetypes
     * @param location   the practice location
     * @return the query
     */
    public static ArchetypeQuery addLocationConstraint(ArchetypeQuery query, String[] archetypes, Party location) {
        if (contains(archetypes, TEMPLATE, SERVICE)) {
            // exclude any products that link to the location
            String alias = query.getArchetypeConstraint().getAlias();
            query.add(notExists(subQuery(archetypes, "p3").add(join("locations", "l").add(eq("target", location)).add(
                    idEq(alias, "p3")))));
        }
        return query;
    }

    /**
     * Adds a stock location constraint if the product archetypes being queried include medication or merchandise
     * products.
     * <p/>
     * This only returns medication and merchandise products that are present at the stock location.
     *
     * @param query               the query
     * @param archetypes          the product archetypes
     * @param useLocationProducts if {@code true}, only those products that have a relationship with the stock location
     *                            will be returned. If {@code false}, those products that have a relationship or no
     *                            relationship are returned
     * @param stockLocation       the stock location
     * @return the query
     */
    public static ArchetypeQuery addStockLocationConstraint(ArchetypeQuery query, String[] archetypes,
                                                            boolean useLocationProducts, Party stockLocation) {
        if (contains(archetypes, MEDICATION, MERCHANDISE)) {
            // only join on stockLocation if medication or merchandise products are being queried
            String alias = query.getArchetypeConstraint().getAlias();
            query.add(leftJoin("stockLocations", "sl"));
            OrConstraint or = Constraints.or(eq("sl.target", stockLocation), not(isA(alias, MEDICATION, MERCHANDISE)));
            if (!useLocationProducts) {
                // return products that have no stock location relationship
                or.add(notExists(subQuery(archetypes, "p4").add(join("stockLocations", "sl2").add(idEq(alias, "p4")))));
            }
            query.add(or);
        }
        return query;
    }

    /**
     * Helper to determine if an array of short names contains on or more short names.
     *
     * @param archetypes the archetype short names
     * @param matches    the archetype short names to match on
     * @return {@code true} if {@code shortNames} contains at least on of {@code matches}
     */
    private static boolean contains(String[] archetypes, String... matches) {
        for (String match : matches) {
            if (ArrayUtils.contains(archetypes, match)) {
                return true;
            }
        }
        return ArrayUtils.contains(archetypes, "product.*");
    }

}
