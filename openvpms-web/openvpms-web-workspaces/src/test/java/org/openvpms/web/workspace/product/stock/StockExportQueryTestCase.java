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

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.product.ProductTestHelper;
import org.openvpms.archetype.rules.stock.io.StockData;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.query.ResultSetIterator;
import org.openvpms.web.test.AbstractAppTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.openvpms.archetype.rules.product.ProductTestHelper.setStockQuantity;

/**
 * Tests the {@link StockExportQuery} class.
 *
 * @author Tim Anderson
 */
public class StockExportQueryTestCase extends AbstractAppTest {

    /**
     * The stock location.
     */
    private Party stockLocation;

    /**
     * The query.
     */
    private StockExportQuery query;

    /**
     * Sets up the test case.
     */
    @Override
    @Before
    public void setUp() {
        super.setUp();
        stockLocation = ProductTestHelper.createStockLocation();
        query = new StockExportQuery(stockLocation);
    }

    /**
     * Creates 3 products, linked to a stock location, and verifies the query returns them ordered by name.
     */
    @Test
    public void testQuery() {
        Product product1 = createProduct("A");
        Product product2 = createProduct("B");
        Product product3 = createProduct("C");

        setStockQuantity(product1, stockLocation, BigDecimal.ONE);
        setStockQuantity(product2, stockLocation, BigDecimal.TEN);
        setStockQuantity(product3, stockLocation, BigDecimal.ZERO);

        List<StockData> results = getResults();
        assertEquals(3, results.size());
        checkStock(results.get(0), product1, stockLocation, BigDecimal.ONE);
        checkStock(results.get(1), product2, stockLocation, BigDecimal.TEN);
        checkStock(results.get(2), product3, stockLocation, BigDecimal.ZERO);
    }

    /**
     * Verifies that a stock location with no relationships returns an empty set.
     */
    @Test
    public void testEmpty() {
        ResultSet<StockData> set = query.query();
        assertFalse(set.hasNext());
    }

    /**
     * Tests querying by product type.
     */
    @Test
    public void testQueryByProductType() {
        Entity productType = ProductTestHelper.createProductType("Vaccinations");
        Product product1 = createProduct("A", productType);
        Product product2 = createProduct("B");
        Product product3 = createProduct("C", productType);

        setStockQuantity(product1, stockLocation, BigDecimal.ONE);
        setStockQuantity(product2, stockLocation, BigDecimal.TEN);
        setStockQuantity(product3, stockLocation, BigDecimal.ZERO);

        query.setProductType(productType);
        List<StockData> results = getResults();
        assertEquals(2, results.size());
        checkStock(results.get(0), product1, stockLocation, BigDecimal.ONE);
        checkStock(results.get(1), product3, stockLocation, BigDecimal.ZERO);
    }

    /**
     * Tests querying by product group.
     */
    @Test
    public void testQueryByProductGroup() {
        Lookup group1 = TestHelper.getLookup("lookup.productGroup", "MERCHANDISE");
        Lookup group2 = TestHelper.getLookup("lookup.productGroup", "VACCINE");
        Product product1 = createProduct("A", group1);
        Product product2 = createProduct("B", group2);
        Product product3 = createProduct("C", group1);

        setStockQuantity(product1, stockLocation, BigDecimal.ONE);
        setStockQuantity(product2, stockLocation, BigDecimal.TEN);
        setStockQuantity(product3, stockLocation, BigDecimal.ZERO);

        query.setProductGroup(group1.getCode());
        List<StockData> results = getResults();
        assertEquals(2, results.size());
        checkStock(results.get(0), product1, stockLocation, BigDecimal.ONE);
        checkStock(results.get(1), product3, stockLocation, BigDecimal.ZERO);
    }

    /**
     * Tests querying by income type.
     */
    @Test
    public void testQueryByIncomeType() {
        Lookup incomeType1 = TestHelper.getLookup("lookup.productIncomeType", "INCOME_1");
        Lookup incomeType2 = TestHelper.getLookup("lookup.productIncomeType", "INCOME_2");
        Product product1 = createProduct("A", incomeType1);
        Product product2 = createProduct("B", incomeType2);
        Product product3 = createProduct("C", incomeType1);

        setStockQuantity(product1, stockLocation, BigDecimal.ONE);
        setStockQuantity(product2, stockLocation, BigDecimal.TEN);
        setStockQuantity(product3, stockLocation, BigDecimal.ZERO);

        query.setIncomeType(incomeType1.getCode());
        List<StockData> results = getResults();
        assertEquals(2, results.size());
        checkStock(results.get(0), product1, stockLocation, BigDecimal.ONE);
        checkStock(results.get(1), product3, stockLocation, BigDecimal.ZERO);
    }

    /**
     * Verifies that stock data matches that expected.
     *
     * @param stockData     the stock data
     * @param product       the expected product
     * @param stockLocation the expected stock location
     * @param quantity      the expected quantity
     */
    private void checkStock(StockData stockData, Product product, Party stockLocation, BigDecimal quantity) {
        assertEquals(stockData.getStockLocationId(), stockLocation.getId());
        assertEquals(stockData.getStockLocationName(), stockLocation.getName());
        assertEquals(stockData.getProductId(), product.getId());
        assertEquals(stockData.getProductName(), product.getName());
        checkEquals(stockData.getQuantity(), quantity);
        checkEquals(stockData.getQuantity(), quantity);
    }

    /**
     * Collects the results of the query.
     *
     * @return the results
     */
    private List<StockData> getResults() {
        List<StockData> results = new ArrayList<StockData>();
        ResultSetIterator<StockData> iterator = new ResultSetIterator<StockData>(query.query());
        while (iterator.hasNext()) {
            results.add(iterator.next());
        }
        return results;
    }

    /**
     * Creates a product with the specified name.
     *
     * @param name the product name
     * @return the product
     */
    private Product createProduct(String name) {
        Product product = TestHelper.createProduct();
        product.setName(name);
        save(product);
        return product;
    }

    /**
     * Creates a product with the specified product type.
     *
     * @param name        the product name
     * @param productType the product type
     * @return the product
     */
    private Product createProduct(String name, Entity productType) {
        Product product = ProductTestHelper.createProduct(productType);
        product.setName(name);
        save(product);
        return product;
    }

    /**
     * Creates a product with the specified classification.
     *
     * @param name           the product name
     * @param classification the product classification
     * @return the product
     */
    private Product createProduct(String name, Lookup classification) {
        Product product = createProduct(name);
        product.addClassification(classification);
        save(product);
        return product;
    }

}
