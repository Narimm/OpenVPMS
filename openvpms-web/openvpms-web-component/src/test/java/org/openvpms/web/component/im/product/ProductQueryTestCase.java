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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.product;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductTestHelper;
import org.openvpms.archetype.rules.stock.StockArchetypes;
import org.openvpms.archetype.rules.stock.StockRules;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.query.AbstractEntityQueryTest;
import org.openvpms.web.component.im.query.QueryTestHelper;

import java.math.BigDecimal;
import java.util.List;


/**
 * Tests the {@link ProductQuery} class.
 *
 * @author Tim Anderson
 */
public class ProductQueryTestCase extends AbstractEntityQueryTest<Product> {

    /**
     * The stock rules.
     */
    private StockRules rules;

    /**
     * Product archetype short names.
     */
    private static final String[] SHORT_NAMES = new String[]{
            ProductArchetypes.MEDICATION, ProductArchetypes.SERVICE, ProductArchetypes.MERCHANDISE,
            ProductArchetypes.TEMPLATE, ProductArchetypes.PRICE_TEMPLATE};

    /**
     * Canine species lookup code.
     */
    private static final String CANINE = "CANINE";

    /**
     * Feline species lookup code.
     */
    private static final String FELINE = "FELINE";

    /**
     * Sets up the test case.
     */
    @Override
    @Before
    public void setUp() {
        super.setUp();
        rules = new StockRules(getArchetypeService());
    }

    /**
     * Tests constraining the query on stock location.
     */
    @Test
    public void testQueryByStockLocation() {
        StockRules rules = new StockRules(getArchetypeService());
        Party stockLocation1 = createStockLocation();
        Party stockLocation2 = createStockLocation();

        save(stockLocation1);
        save(stockLocation2);

        // create 3 products
        Product product1 = createObject(true);
        Product product2 = createObject(true);
        Product product3 = createObject(true);

        // associate product1 with stockLocation1
        rules.updateStock(product1, stockLocation1, BigDecimal.ONE);

        // associate product2 with stockLocation2
        rules.updateStock(product2, stockLocation2, BigDecimal.ONE);

        // tests the query without constraints. All products should be returned
        ProductQuery query = createQuery();
        List<IMObjectReference> matches = getObjectRefs(query);
        checkExists(product1, query, matches, true);
        checkExists(product2, query, matches, true);
        checkExists(product3, query, matches, true);

        // now constraint to stockLocation1. Note that product3 is returned as it has no stock location relationship
        query.setStockLocation(stockLocation1);
        matches = getObjectRefs(query);
        checkExists(product1, query, matches, true);
        checkExists(product2, query, matches, false);
        checkExists(product3, query, matches, true);

        // now constraint to stockLocation2. Note that product3 is returned as it has not stock location relationship
        query.setStockLocation(stockLocation2);
        matches = getObjectRefs(query);
        checkExists(product1, query, matches, false);
        checkExists(product2, query, matches, true);
        checkExists(product3, query, matches, true);
    }

    /**
     * Tests constraining the query on species.
     */
    @Test
    public void testQueryBySpecies() {
        Product canineProduct1 = createProduct(CANINE, true);
        Product canineProduct2 = createProduct(CANINE, true);
        Product felineProduct = createProduct(FELINE, true);
        Product universalProduct = createProduct(null, true);

        // tests the query without constraining to a particular species. All products should be returned
        ProductQuery query = createQuery();
        List<IMObjectReference> matches = getObjectRefs(query);
        checkExists(canineProduct1, query, matches, true);
        checkExists(canineProduct2, query, matches, true);
        checkExists(felineProduct, query, matches, true);
        checkExists(universalProduct, query, matches, true);

        // now constrain query to canine products. Feline products should be excluded
        query.setSpecies(CANINE);
        matches = getObjectRefs(query);

        checkExists(canineProduct1, query, matches, true);
        checkExists(canineProduct2, query, matches, true);
        checkExists(felineProduct, query, matches, false);
        checkExists(universalProduct, query, matches, true);

        // now constrain query to feline products. Canine products should be excluded
        query.setSpecies(FELINE);
        matches = getObjectRefs(query);
        checkExists(canineProduct1, query, matches, false);
        checkExists(canineProduct2, query, matches, false);
        checkExists(felineProduct, query, matches, true);
        checkExists(universalProduct, query, matches, true);
    }

    /**
     * Tests constraining the query by both species and stock location
     */
    @Test
    public void testQueryBySpeciesAndStockLocation() {
        Party stockLocation1 = createStockLocation();
        Party stockLocation2 = createStockLocation();

        save(stockLocation1);
        save(stockLocation2);

        // create 5 products, and link to appropriate stock locations
        Product canineStock1 = createProduct(CANINE, true);  // link to stockLocation1
        Product canineStock2 = createProduct(CANINE, true);  // link to stockLocation2
        Product felineStock1 = createProduct(FELINE, true);  // link to stockLocation1
        Product felineNoStock = createProduct(FELINE, true); // don't link to any stock location
        Product universalStock2 = createProduct(null, true); // link to stockLocation2

        rules.updateStock(canineStock1, stockLocation1, BigDecimal.ONE);
        rules.updateStock(canineStock2, stockLocation2, BigDecimal.ONE);
        rules.updateStock(felineStock1, stockLocation1, BigDecimal.ONE);
        rules.updateStock(universalStock2, stockLocation2, BigDecimal.ONE);

        // tests the query without constraints. All products should be returned
        ProductQuery query = createQuery();
        List<IMObjectReference> matches = getObjectRefs(query);
        checkExists(canineStock1, query, matches, true);
        checkExists(canineStock2, query, matches, true);
        checkExists(felineStock1, query, matches, true);
        checkExists(felineNoStock, query, matches, true);
        checkExists(universalStock2, query, matches, true);

        // now limit to stock location 1, CANINE products
        query.setStockLocation(stockLocation1);
        query.setSpecies(CANINE);
        matches = getObjectRefs(query);
        checkExists(canineStock1, query, matches, true);
        checkExists(canineStock2, query, matches, false);
        checkExists(felineStock1, query, matches, false);
        checkExists(felineNoStock, query, matches, false);
        checkExists(universalStock2, query, matches, false);

        // now limit to stock location 1, FELINE products
        query.setStockLocation(stockLocation1);
        query.setSpecies(FELINE);
        matches = getObjectRefs(query);
        checkExists(canineStock1, query, matches, false);
        checkExists(canineStock2, query, matches, false);
        checkExists(felineStock1, query, matches, true);
        checkExists(felineNoStock, query, matches, true);
        checkExists(universalStock2, query, matches, false);

        // now limit to stock location 2, FELINE products
        query.setStockLocation(stockLocation2);
        query.setSpecies(FELINE);
        matches = getObjectRefs(query);
        checkExists(canineStock1, query, matches, false);
        checkExists(canineStock2, query, matches, false);
        checkExists(felineStock1, query, matches, false);
        checkExists(felineNoStock, query, matches, true);
        checkExists(universalStock2, query, matches, true);
    }

    /**
     * Tests exclusion of products via {@link ProductQuery#setExcludeTemplateOnlyProducts(boolean)}.
     */
    @Test
    public void testExcludeTemplateOnlyProducts() {
        Product product1 = createObject(true);
        Product product2 = createObject(true);
        Product product3 = createObject(true);
        IMObjectBean bean = new IMObjectBean(product2);
        bean.setValue("templateOnly", true);
        bean.save();

        ProductQuery query = createQuery();

        // test default behaviour
        List<IMObjectReference> matches = getObjectRefs(query);
        checkExists(product1, query, matches, true);
        checkExists(product2, query, matches, true);
        checkExists(product3, query, matches, true);

        // test exclusion
        query.setExcludeTemplateOnlyProducts(true);
        matches = getObjectRefs(query);
        checkExists(product1, query, matches, true);
        checkExists(product2, query, matches, false);
        checkExists(product3, query, matches, true);

        // test inclusion
        query.setExcludeTemplateOnlyProducts(false);
        matches = getObjectRefs(query);
        checkExists(product1, query, matches, true);
        checkExists(product2, query, matches, true);
        checkExists(product3, query, matches, true);
    }

    /**
     * Tests exclusion of products via {@link ProductQuery#setUseLocationProducts(boolean)}.
     */
    @Test
    public void testUseLocationProducts() {
        Party stockLocation = createStockLocation();
        Party location = TestHelper.createLocation();

        Product medication1 = ProductTestHelper.createMedication();
        Product medication2 = ProductTestHelper.createMedication();
        Product merchandise1 = ProductTestHelper.createMerchandise();
        Product merchandise2 = ProductTestHelper.createMerchandise();
        Product service1 = ProductTestHelper.createService();
        Product service2 = ProductTestHelper.createService();
        Product template1 = ProductTestHelper.createTemplate();
        Product template2 = ProductTestHelper.createTemplate();
        Product priceTemplate1 = ProductTestHelper.createPriceTemplate();

        rules.updateStock(medication1, stockLocation, BigDecimal.ONE);
        rules.updateStock(merchandise1, stockLocation, BigDecimal.ONE);
        ProductTestHelper.addLocationExclusion(service1, location);
        ProductTestHelper.addLocationExclusion(template1, location);

        ProductQuery query = createQuery();

        // test default behaviour
        QueryTestHelper.checkExists(query, medication1, medication2, merchandise1, merchandise2, service1, service2,
                                    template1, template2, priceTemplate1);

        // verify location has no effect until useLocationProducts = true
        query.setLocation(location);
        QueryTestHelper.checkExists(query, medication1, medication2, merchandise1, merchandise2, service1, service2,
                                    template1, template2, priceTemplate1);

        // verify useLocationProducts = true has no effect until the location or stockLocation is set
        query.setLocation(null);
        query.setUseLocationProducts(true);
        QueryTestHelper.checkExists(query, medication1, medication2, merchandise1, merchandise2, service1, service2,
                                    template1, template2, priceTemplate1);

        // set the location. Products with the location are *excluded*
        query.setLocation(location);
        QueryTestHelper.checkExists(query, medication1, medication2, merchandise1, merchandise2, service2,
                                    template2, priceTemplate1);
        QueryTestHelper.checkNotExists(query, service1, template1);

        // set the stock location. Products with the stock location are *included*
        query.setStockLocation(stockLocation);
        QueryTestHelper.checkExists(query, medication1, merchandise1, service2, template2, priceTemplate1);
        QueryTestHelper.checkNotExists(query, medication2, merchandise2, service1, template1);
    }

    /**
     * Creates a new query.
     *
     * @return a new query
     */
    @Override
    protected ProductQuery createQuery() {
        return new ProductQuery(SHORT_NAMES, new LocalContext());
    }

    /**
     * Creates a new object, selected by the query.
     *
     * @param value a value that can be used to uniquely identify the object
     * @param save  if <tt>true</tt> save the object, otherwise don't save it
     * @return the new object
     */
    protected Product createObject(String value, boolean save) {
        Product product = createProduct(null, false);
        product.setName(value);
        if (save) {
            save(product);
        }
        return product;
    }

    /**
     * Generates a unique value which may be used for querying objects on.
     *
     * @return a unique value
     */
    protected String getUniqueValue() {
        return getUniqueValue("ZProduct");
    }

    /**
     * Helper to create a product, optionally associated with a species.
     *
     * @param species the species lookup code. May be <tt>null</tt>
     * @param save    if <tt>true</tt> saves the product
     * @return a new product
     */
    protected Product createProduct(String species, boolean save) {
        return TestHelper.createProduct(ProductArchetypes.MEDICATION, species, save);
    }

    /**
     * Helper to create a stock location.
     *
     * @return a new stock location
     */
    protected Party createStockLocation() {
        Party result = (Party) create(StockArchetypes.STOCK_LOCATION);
        result.setName("STOCK-LOCATION-" + result.hashCode());
        save(result);
        return result;
    }

}
