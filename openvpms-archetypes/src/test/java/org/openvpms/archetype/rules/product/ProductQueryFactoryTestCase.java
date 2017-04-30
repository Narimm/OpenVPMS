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

import org.junit.Test;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.QueryIterator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.rules.product.ProductArchetypes.MEDICATION;
import static org.openvpms.archetype.rules.product.ProductArchetypes.MERCHANDISE;
import static org.openvpms.archetype.rules.product.ProductArchetypes.SERVICE;
import static org.openvpms.archetype.rules.product.ProductArchetypes.TEMPLATE;


/**
 * Tests the {@link ProductQueryFactory} class.
 *
 * @author Tim Anderson
 */
public class ProductQueryFactoryTestCase extends ArchetypeServiceTest {

    /**
     * Feline species.
     */
    private static final String FELINE = "FELINE";

    /**
     * Canine species.
     */
    private static final String CANINE = "CANINE";


    /**
     * Tests the {@link IArchetypeQuery} instances returned by {@link ProductQueryFactory}.
     */
    @Test
    public void testCreateForSpeciesAndName() {
        // test query on all canine products
        IArchetypeQuery canine = ProductQueryFactory.create(new String[]{MEDICATION, TEMPLATE, SERVICE, MERCHANDISE},
                                                            null, CANINE, false, null, null);
        checkQuery(canine, true, false, true);

        // test query on all feline medication products
        IArchetypeQuery feline = ProductQueryFactory.create(new String[]{MEDICATION}, null, FELINE, false, null, null);
        checkQuery(feline, false, true, false);

        // test query on all canine products named XProduct*.
        IArchetypeQuery canine2 = ProductQueryFactory.create(new String[]{MEDICATION, TEMPLATE, SERVICE, MERCHANDISE},
                                                             "XProduct*", CANINE, false, null, null);
        checkQuery(canine2, true, false, true);

        // test query on all feline medication products named XProduct*.
        IArchetypeQuery feline2 = ProductQueryFactory.create(new String[]{MEDICATION}, "XProduct*", FELINE, false, null,
                                                             null);
        checkQuery(feline2, false, true, false);
    }

    /**
     * Tests querying products by stock location.
     */
    @Test
    public void testCreateForStockLocation() {
        Party location1 = TestHelper.createLocation();
        Party location2 = TestHelper.createLocation();
        Party stockLocation1 = ProductTestHelper.createStockLocation(location1);
        Party stockLocation2 = ProductTestHelper.createStockLocation(location2);

        Product medicationA = ProductTestHelper.createMedication();
        Product medicationB = ProductTestHelper.createMedication();
        Product medicationC = ProductTestHelper.createMedication();
        Product medicationD = ProductTestHelper.createMedication();
        Product merchandiseA = ProductTestHelper.createMerchandise();
        Product merchandiseB = ProductTestHelper.createMerchandise();
        Product merchandiseC = ProductTestHelper.createMerchandise();
        ProductTestHelper.setStockQuantity(medicationA, stockLocation1, BigDecimal.TEN);
        ProductTestHelper.setStockQuantity(medicationA, stockLocation2, BigDecimal.TEN);
        ProductTestHelper.setStockQuantity(medicationB, stockLocation1, BigDecimal.TEN);
        ProductTestHelper.setStockQuantity(medicationC, stockLocation2, BigDecimal.TEN);
        ProductTestHelper.setStockQuantity(merchandiseA, stockLocation1, BigDecimal.TEN);
        ProductTestHelper.setStockQuantity(merchandiseB, stockLocation2, BigDecimal.TEN);

        // with useLocationProducts=false, products with a stock location or no stock location are returned
        checkQuery(false, location1, stockLocation1, true, medicationA, medicationB, medicationD,
                   merchandiseA, merchandiseC);
        checkQuery(false, location1, stockLocation1, false, medicationC, merchandiseB);

        // with useLocationProducts=true, products must have a stock location relationship
        checkQuery(true, location1, stockLocation1, true, medicationA, medicationB, merchandiseA);
        checkQuery(true, location1, stockLocation1, false, medicationC, medicationD, merchandiseB, merchandiseC);
    }

    /**
     * Tests querying products by location.
     */
    @Test
    public void testCreateForLocation() {
        Party location1 = TestHelper.createLocation();
        Party location2 = TestHelper.createLocation();

        Product serviceA = ProductTestHelper.createService();
        Product serviceB = ProductTestHelper.createService();
        Product serviceC = ProductTestHelper.createService();
        Product serviceD = ProductTestHelper.createService();
        Product templateA = ProductTestHelper.createTemplate();
        Product templateB = ProductTestHelper.createTemplate();
        Product templateC = ProductTestHelper.createTemplate();

        ProductTestHelper.addLocationExclusion(serviceA, location2);
        ProductTestHelper.addLocationExclusion(serviceB, location1);
        ProductTestHelper.addLocationExclusion(serviceC, location1);
        ProductTestHelper.addLocationExclusion(serviceC, location2);
        ProductTestHelper.addLocationExclusion(templateA, location2);
        ProductTestHelper.addLocationExclusion(templateB, location1);

        // with useLocationProducts=false, all service and template products are returned
        checkQuery(false, location1, null, true, serviceA, serviceB, serviceC, serviceD, templateA, templateB);

        // with useLocationProducts=false, products must not be excluded at the practice location
        checkQuery(true, location1, null, true, serviceA, serviceD, templateA, templateC);
        checkQuery(true, location1, null, false, serviceB, serviceC, templateB);
    }

    /**
     * Checks a query against the following 3 products:
     * <ul>
     * <li>a canine <em>product.service</em></li>
     * <li>a feline <em>product.medication</em></li>
     * <li>a <em>product.merchandise</em</li>
     * </ul>
     *
     * @param query             the query
     * @param expectCanine      if {@code true} the query should select the canine product.service
     * @param expectFeline      if {@code true} the query should select the feline product.medication
     * @param expectMerchandise if {@code true} the query should select the product.merchandise
     */
    private void checkQuery(IArchetypeQuery query, boolean expectCanine, boolean expectFeline,
                            boolean expectMerchandise) {
        IArchetypeService service = getArchetypeService();
        query.setCountResults(true);

        // get the current count of products
        int count = service.get(query).getTotalResults();

        // create new products for canines, felines and all species
        Product canineProduct = TestHelper.createProduct(SERVICE, CANINE);
        Product felineProduct = TestHelper.createProduct(MEDICATION, FELINE);
        Product merchProduct = TestHelper.createProduct(MERCHANDISE, null);

        boolean canineFound = false;
        boolean felineFound = false;
        boolean merchandiseFound = false;

        int matches = 0;
        if (expectCanine) {
            ++matches;
        }
        if (expectFeline) {
            ++matches;
        }
        if (expectMerchandise) {
            ++matches;
        }
        int newCount = service.get(query).getTotalResults();
        assertEquals(count + matches, newCount);

        // verify that the expected products are in the results
        QueryIterator<Product> iterator = new IMObjectQueryIterator<>(query);
        while (iterator.hasNext()) {
            Product product = iterator.next();
            if (product.equals(canineProduct)) {
                canineFound = true;
            } else if (product.equals(felineProduct)) {
                felineFound = true;
            } else if (product.equals(merchProduct)) {
                merchandiseFound = true;
            }
        }

        assertEquals(expectCanine, canineFound);
        assertEquals(expectFeline, felineFound);
        assertEquals(expectMerchandise, merchandiseFound);
    }

    /**
     * Checks a query by location and/or stock location.
     *
     * @param useLocationProducts if {@code true}, products should be restricted to those available at the location or
     *                            stock location
     * @param location            the practice location. May be {@code null}
     * @param stockLocation       the stock location. May be {@code null}
     * @param exists              if {@code true}, the products must exist in the returned result
     * @param products            the products to check
     */
    private void checkQuery(boolean useLocationProducts, Party location, Party stockLocation, boolean exists,
                            Product... products) {
        String[] archetypes = {"product.*"};
        ArchetypeQuery query = ProductQueryFactory.create(archetypes, null, null, useLocationProducts, location,
                                                          stockLocation);
        List<Product> list = new ArrayList<>(Arrays.asList(products));
        IMObjectQueryIterator<Product> iterator = new IMObjectQueryIterator<>(query);
        while (iterator.hasNext()) {
            Product next = iterator.next();
            if (list.contains(next)) {
                assertTrue(exists);
            }
        }
    }


}
