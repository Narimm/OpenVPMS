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

package org.openvpms.archetype.rules.stock.io;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.product.ProductTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Tests the {@link StockDataImporter}.
 *
 * @author Tim Anderson
 */
public class StockDataImporterTestCase extends ArchetypeServiceTest {

    /**
     * The document handlers.
     */
    private DocumentHandlers handlers;

    /**
     * The importer.
     */
    private StockDataImporter importer;

    /**
     * The user to assign to acts.
     */
    private User user;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        handlers = new DocumentHandlers(getArchetypeService());
        importer = new StockDataImporter(getArchetypeService(), handlers, ',');
        user = TestHelper.createUser();
    }

    /**
     * Tests loading a CSV file and creating an adjustment.
     */
    @Test
    public void testLoad() {
        Party location = ProductTestHelper.createStockLocation();
        Product product1 = TestHelper.createProduct();
        Product product2 = TestHelper.createProduct();
        Product product3 = TestHelper.createProduct();
        Product product4 = TestHelper.createProduct();
        StockData data1 = createStockData(location, product1, "Mls", BigDecimal.ONE, BigDecimal.TEN);
        StockData data2 = createStockData(location, product2, "Each", BigDecimal.ONE, BigDecimal.ONE);
        StockData data3 = createStockData(location, product3, "Packet", new BigDecimal("-10"), BigDecimal.ZERO);
        StockData data4 = createStockData(location, product4, "Unit", new BigDecimal("-10"), new BigDecimal("-5"));
        Document document = createCSV(Arrays.asList(data1, data2, data3, data4));
        StockDataSet set = importer.load(document, user, "A note");
        assertEquals(3, set.getData().size());
        assertEquals(0, set.getErrors().size());
        assertNotNull(set.getAdjustment());
        ActBean bean = new ActBean(set.getAdjustment());
        assertEquals("A note", bean.getAct().getReason());
        assertEquals(user, bean.getNodeParticipant("author"));
        List<Act> items = bean.getNodeActs("items");
        assertEquals(3, items.size());

        // verify that stock for product1, product3 and product4 have been adjusted
        checkItem(items, product1, new BigDecimal("9"));
        checkItem(items, product3, BigDecimal.TEN);
        checkItem(items, product4, new BigDecimal("5"));
    }

    /**
     * Verifies that if there are no changes, the returned {@link StockDataSet} is empty, and no adjustment is created.
     */
    @Test
    public void testNoChanges() {
        Party location = ProductTestHelper.createStockLocation();
        Product product = TestHelper.createProduct();
        StockData data = createStockData(location, product, "Mls", BigDecimal.ONE, BigDecimal.ONE);
        Document document = createCSV(Arrays.asList(data));
        StockDataSet set = importer.load(document, user, "A note");
        assertEquals(0, set.getErrors().size());
        assertEquals(0, set.getData().size());
        assertNull(set.getAdjustment());
    }

    /**
     * Verifies that an error is raised if there are multiple stock locations.
     */
    @Test
    public void testMultipleStockLocations() {
        Party location1 = ProductTestHelper.createStockLocation();
        Party location2 = ProductTestHelper.createStockLocation();
        Product product = TestHelper.createProduct();
        StockData data1 = createStockData(location1, product, "Mls", BigDecimal.ONE, BigDecimal.TEN);
        StockData data2 = createStockData(location2, product, "Mls", BigDecimal.ONE, BigDecimal.TEN);
        Document document = createCSV(Arrays.asList(data1, data2));
        StockDataSet set = importer.load(document, user, "A note");
        assertEquals(1, set.getErrors().size());
        assertEquals("Expected " + location1.getId() + " for Stock Location Identifier but got " + location2.getId(),
                     set.getErrors().get(0).getError());
        assertEquals(1, set.getData().size());
        assertNull(set.getAdjustment());
    }

    /**
     * Verifies an error is raised if a stock location is missing.
     */
    @Test
    public void testMissingLocation() {
        Party location = ProductTestHelper.createStockLocation();
        Product product = TestHelper.createProduct();
        StockData data = createStockData(location, product, "Mls", BigDecimal.ONE, BigDecimal.TEN);
        remove(location);
        Document document = createCSV(Arrays.asList(data));
        StockDataSet set = importer.load(document, user, "A note");
        assertEquals(1, set.getErrors().size());
        assertEquals("Stock location not found", set.getErrors().get(0).getError());
        assertEquals(0, set.getData().size());
        assertNull(set.getAdjustment());
    }

    /**
     * Verifies an error is raised if a stock location identifier doesn't correspond to the stock location name.
     */
    @Test
    public void testDifferentStockLocationName() {
        Party location = ProductTestHelper.createStockLocation();
        location.setName("Old name");
        StockData data = createStockData(location, TestHelper.createProduct(), "Mls", BigDecimal.ONE, BigDecimal.TEN);
        location.setName("New name");
        save(location);
        Document document = createCSV(Arrays.asList(data));
        StockDataSet set = importer.load(document, user, "A note");
        assertEquals(1, set.getErrors().size());
        assertEquals("Expected 'New name' but got 'Old name'", set.getErrors().get(0).getError());
        assertEquals(0, set.getData().size());
        assertNull(set.getAdjustment());
    }

    /**
     * Verifies an error is raised if a product is missing.
     */
    @Test
    public void testMissingProduct() {
        Party location = ProductTestHelper.createStockLocation();
        Product product = TestHelper.createProduct();
        StockData data = createStockData(location, product, "Mls", BigDecimal.ONE, BigDecimal.TEN);
        remove(product);
        Document document = createCSV(Arrays.asList(data));
        StockDataSet set = importer.load(document, user, "A note");
        assertEquals(1, set.getErrors().size());
        assertEquals("Product not found", set.getErrors().get(0).getError());
        assertEquals(0, set.getData().size());
        assertNull(set.getAdjustment());
    }

    /**
     * Verifies an error is raised if a product identifier doesn't correspond to the product name.
     */
    @Test
    public void testDifferentProductName() {
        Party location = ProductTestHelper.createStockLocation();
        Product product = TestHelper.createProduct();
        product.setName("Old name");
        save(product);
        StockData data = createStockData(location, product, "Mls", BigDecimal.ONE, BigDecimal.TEN);
        product.setName("New name");
        save(product);
        Document document = createCSV(Arrays.asList(data));
        StockDataSet set = importer.load(document, user, "A note");
        assertEquals(1, set.getErrors().size());
        assertEquals("Expected 'New name' but got 'Old name'", set.getErrors().get(0).getError());
        assertEquals(0, set.getData().size());
        assertNull(set.getAdjustment());
    }

    /**
     * Verifies an error is raised if quantities are the same, but products are different.
     */
    @Test
    public void testMissingProductForSameQuantities() {
        Party location = ProductTestHelper.createStockLocation();
        Product product = TestHelper.createProduct();
        StockData data = createStockData(location, product, "Mls", BigDecimal.ONE, BigDecimal.ONE);
        remove(product);
        Document document = createCSV(Arrays.asList(data));
        StockDataSet set = importer.load(document, user, "A note");
        assertEquals(1, set.getErrors().size());
        assertEquals("Product not found", set.getErrors().get(0).getError());
        assertEquals(0, set.getData().size());
        assertNull(set.getAdjustment());
    }

    private Document createCSV(List<StockData> data) {
        StockCSVWriter writer = new StockCSVWriter(handlers, ',');
        return writer.write("test.csv", data.iterator());
    }

    private void checkItem(List<Act> acts, Product product, BigDecimal quantity) {
        for (Act act : acts) {
            ActBean bean = new ActBean(act);
            if (product.getObjectReference().equals(bean.getNodeParticipantRef("product"))) {
                checkEquals(quantity, bean.getBigDecimal("quantity"));
                return;
            }
        }
        fail("Product not found");
    }

    /**
     * Creates a new {@link StockData}.
     *
     * @param stockLocation the stock location
     * @param product       the product
     * @param sellingUnits  the product selling units
     * @param quantity      the current stock quantity
     * @param newQuantity   the new stock quantity
     * @return a new {@link StockData}
     */
    private StockData createStockData(Party stockLocation, Product product, String sellingUnits, BigDecimal quantity,
                                      BigDecimal newQuantity) {
        return new StockData(stockLocation.getId(), stockLocation.getName(), product.getId(),
                             product.getName(), sellingUnits, quantity, newQuantity);
    }

}
