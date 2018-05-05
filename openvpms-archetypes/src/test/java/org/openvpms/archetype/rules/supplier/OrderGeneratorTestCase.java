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

package org.openvpms.archetype.rules.supplier;

import org.apache.commons.lang.ObjectUtils;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.finance.tax.TaxRules;
import org.openvpms.archetype.rules.practice.PracticeArchetypes;
import org.openvpms.archetype.rules.product.ProductSupplier;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.EntityLink;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Tests the {@link OrderGenerator}.
 *
 * @author Tim Anderson
 */
public class OrderGeneratorTestCase extends AbstractSupplierTest {

    /**
     * The tax rules.
     */
    private TaxRules taxRules;

    /**
     * GST tax rate.
     */
    private Lookup gst;


    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        super.setUp();
        Party practice = (Party) create(PracticeArchetypes.PRACTICE);
        gst = TestHelper.createTaxType(BigDecimal.TEN);
        practice.addClassification(gst);
        taxRules = new TaxRules(practice, getArchetypeService());
    }

    /**
     * Tests the {@link OrderGenerator#getOrderableStock(Party, Party, boolean)}  method.
     */
    @Test
    public void testGetOrderableStock() {
        OrderGenerator generator = new OrderGenerator(taxRules, getArchetypeService());
        Party stockLocation = SupplierTestHelper.createStockLocation();
        Party supplier = TestHelper.createSupplier();
        Product product1 = TestHelper.createProduct();
        Product product2 = TestHelper.createProduct();
        Product product3 = TestHelper.createProduct();
        Product product4 = TestHelper.createProduct();
        Product product5 = TestHelper.createProduct();
        Product product6 = TestHelper.createProduct();

        addRelationships(product1, stockLocation, supplier, true, 1, 10, 5);  // below critical level
        addRelationships(product2, stockLocation, supplier, true, 5, 10, 5);  // at critical level
        addRelationships(product3, stockLocation, supplier, true, 6, 10, 5);  // above critical level
        addRelationships(product4, stockLocation, supplier, true, 9, 10, 5);  // below ideal level
        addRelationships(product5, stockLocation, supplier, true, 10, 10, 5); // at ideal level
        addRelationships(product6, stockLocation, supplier, true, 11, 10, 5); // above ideal level

        // check stock at or below critical levels
        List<Stock> atOrBelowCritical = generator.getOrderableStock(supplier, stockLocation, false);
        assertEquals(2, atOrBelowCritical.size());
        checkStock(atOrBelowCritical, product1, supplier, stockLocation, 1, 0, 9);
        checkStock(atOrBelowCritical, product2, supplier, stockLocation, 5, 0, 5);

        // check stock at or below ideal levels
        List<Stock> atOrBelowIdeal = generator.getOrderableStock(supplier, stockLocation, true);
        assertEquals(4, atOrBelowIdeal.size());
        checkStock(atOrBelowIdeal, product1, supplier, stockLocation, 1, 0, 9);
        checkStock(atOrBelowIdeal, product2, supplier, stockLocation, 5, 0, 5);
        checkStock(atOrBelowIdeal, product3, supplier, stockLocation, 6, 0, 4);
        checkStock(atOrBelowIdeal, product4, supplier, stockLocation, 9, 0, 1);
    }

    /**
     * Checks that the on-hand, on-order and to-order quantities is calculated correctly when there are outstanding
     * orders.
     */
    @Test
    public void testGetOrderableStockForPendingOrders() {
        OrderGenerator generator = new OrderGenerator(taxRules, getArchetypeService());
        Party stockLocation = SupplierTestHelper.createStockLocation();
        Party supplier1 = TestHelper.createSupplier();
        Party supplier2 = TestHelper.createSupplier();
        Product product1 = TestHelper.createProduct();
        Product product2 = TestHelper.createProduct();
        Product product3 = TestHelper.createProduct();

        addRelationships(product1, stockLocation, supplier1, true, 1, 10, 6);
        addRelationships(product2, stockLocation, supplier1, true, 2, 10, 5);
        addRelationships(product3, stockLocation, supplier2, true, 1, 10, 5);
        createOrder(product1, supplier1, stockLocation, 2, OrderStatus.IN_PROGRESS);
        createOrder(product1, supplier1, stockLocation, 1, OrderStatus.COMPLETED);
        createOrder(product1, supplier1, stockLocation, 1, OrderStatus.POSTED);
        createOrder(product1, supplier1, stockLocation, 1, OrderStatus.ACCEPTED);

        createOrder(product2, supplier1, stockLocation, 3, OrderStatus.ACCEPTED);

        // shouldn't impact totals
        createOrder(product1, supplier1, stockLocation, 1, OrderStatus.CANCELLED);
        createOrder(product2, supplier1, stockLocation, 1, OrderStatus.CANCELLED);
        createOrder(product3, supplier2, stockLocation, 1, OrderStatus.IN_PROGRESS);
        createOrder(product3, supplier2, stockLocation, 1, OrderStatus.COMPLETED);
        createOrder(product3, supplier2, stockLocation, 1, OrderStatus.POSTED);
        createOrder(product3, supplier2, stockLocation, 1, OrderStatus.ACCEPTED);

        supplier1 = get(supplier1);
        List<Stock> stock = generator.getOrderableStock(supplier1, stockLocation, false);
        assertEquals(2, stock.size());
        checkStock(stock, product1, supplier1, stockLocation, 1, 5, 4);
        checkStock(stock, product2, supplier1, stockLocation, 2, 3, 5);
    }

    /**
     * Verifies that inactive products, or products with inactive product-supplier or product-stock location
     * relationships are ignored.
     */
    @Test
    public void testGetOrderableStockIgnoresInactiveProducts() {
        OrderGenerator generator = new OrderGenerator(taxRules, getArchetypeService());
        Party stockLocation = SupplierTestHelper.createStockLocation();
        Party supplier1 = TestHelper.createSupplier();
        Product product1 = TestHelper.createProduct();
        Product product2 = TestHelper.createProduct();
        Product product3 = TestHelper.createProduct();

        addRelationships(product1, stockLocation, supplier1, true, 1, 10, 6);
        ProductSupplier ps = addRelationships(product2, stockLocation, supplier1, true, 2, 10, 5);
        addProductSupplierRelationship(product3, supplier1, true, BigDecimal.ONE, 1);
        EntityLink er = addProductStockLocationRelationship(product3, stockLocation, null, 1, 10, 5);

        supplier1 = get(supplier1);
        List<Stock> stock = generator.getOrderableStock(supplier1, stockLocation, false);
        assertEquals(3, stock.size());
        checkStock(stock, product1, supplier1, stockLocation, 1, 0, 9);
        checkStock(stock, product2, supplier1, stockLocation, 2, 0, 8);
        checkStock(stock, product3, supplier1, stockLocation, 1, 0, 9);

        // disable product1, and verify product2 and product3 are returned
        product1.setActive(false);
        save(product1);
        stock = generator.getOrderableStock(supplier1, stockLocation, false);
        assertEquals(2, stock.size());
        checkStock(stock, product2, supplier1, stockLocation, 2, 0, 8);
        checkStock(stock, product3, supplier1, stockLocation, 1, 0, 9);

        // disable the product-supplier relationship for product2
        ps.setActiveEndTime(new Date(System.currentTimeMillis() - 1000));
        ps.save();
        stock = generator.getOrderableStock(supplier1, stockLocation, false);
        assertEquals(1, stock.size());
        checkStock(stock, product3, supplier1, stockLocation, 1, 0, 9);

        // disable the product-stock location relationship for product3
        er.setActiveEndTime(new Date(System.currentTimeMillis() - 1000));
        save(er);
        stock = generator.getOrderableStock(supplier1, stockLocation, false);
        assertEquals(0, stock.size());
    }

    /**
     * Verifies that the stock to order is calculated correctly if there is a part delivery where the received
     * quantity is greater than that ordered.
     */
    @Test
    public void testGetOrderableStockWhereReceivedQtyGreaterThanOrdered() {
        OrderGenerator generator = new OrderGenerator(taxRules, getArchetypeService());
        Party stockLocation = SupplierTestHelper.createStockLocation();
        Party supplier1 = TestHelper.createSupplier();
        Product product1 = TestHelper.createProduct();

        addRelationships(product1, stockLocation, supplier1, true, 0, 200, 10);

        // create a part delivered order
        List<FinancialAct> acts = createOrder(product1, supplier1, stockLocation, 100, 1, OrderStatus.ACCEPTED, 20, 0,
                                              DeliveryStatus.PART);

        List<Stock> stock = generator.getOrderableStock(supplier1, stockLocation, true);
        assertEquals(1, stock.size());
        checkStock(stock, product1, supplier1, stockLocation, 0, 80, 120);

        // update received quantity to greater than ordered
        ActBean itemBean = new ActBean(acts.get(1));
        itemBean.setValue("receivedQuantity", 150);
        itemBean.save();

        stock = generator.getOrderableStock(supplier1, stockLocation, true);
        assertEquals(1, stock.size());
        checkStock(stock, product1, supplier1, stockLocation, 0, 0, 200);
    }

    /**
     * Verifies that the package size of existing orders is taken into account when calculating stock to order.
     */
    @Test
    public void testGetOrderableStockForPackageSizeChange() {
        OrderGenerator generator = new OrderGenerator(taxRules, getArchetypeService());
        Party stockLocation = SupplierTestHelper.createStockLocation();
        Party supplier1 = TestHelper.createSupplier();
        Product product1 = TestHelper.createProduct();

        // create an order for 10 boxes, with a package size of 2
        FinancialAct orderItem = createOrderItem(product1, BigDecimal.valueOf(10), 2, BigDecimal.ONE);
        createOrder(orderItem, supplier1, stockLocation, OrderStatus.ACCEPTED, 0, 0, DeliveryStatus.PENDING);

        // set the new package size to 5
        addRelationships(product1, stockLocation, supplier1, true, 0, 200, 10, BigDecimal.valueOf(4), 5);

        List<Stock> stock = generator.getOrderableStock(supplier1, stockLocation, true);
        assertEquals(1, stock.size());

        checkStock(stock, product1, supplier1, stockLocation, 0, 20, 36);
    }

    /**
     * Verifies that stock on order at other stock locations for the same supplier doesn't impact stock for the current
     * location.
     */
    @Test
    public void testGetOrderForStockForStockOrderAtDifferentStockLocations() {
        OrderGenerator generator = new OrderGenerator(taxRules, getArchetypeService());
        Party stockLocation1 = SupplierTestHelper.createStockLocation();
        Party stockLocation2 = SupplierTestHelper.createStockLocation();
        Party supplier1 = TestHelper.createSupplier();
        Product product1 = TestHelper.createProduct();
        Product product2 = TestHelper.createProduct();

        addRelationships(product1, stockLocation1, supplier1, true, 1, 10, 6);
        addRelationships(product2, stockLocation1, supplier1, true, 2, 10, 5);

        addRelationships(product1, stockLocation2, supplier1, true, 1, 10, 6);
        addRelationships(product2, stockLocation2, supplier1, true, 2, 10, 5);

        createOrder(product1, supplier1, stockLocation1, 5, OrderStatus.IN_PROGRESS);
        createOrder(product2, supplier1, stockLocation1, 3, OrderStatus.ACCEPTED);

        createOrder(product1, supplier1, stockLocation2, 5, OrderStatus.IN_PROGRESS);
        createOrder(product2, supplier1, stockLocation2, 3, OrderStatus.ACCEPTED);

        supplier1 = get(supplier1);
        List<Stock> stock = generator.getOrderableStock(supplier1, stockLocation1, false);
        assertEquals(2, stock.size());
        checkStock(stock, product1, supplier1, stockLocation1, 1, 5, 4);
        checkStock(stock, product2, supplier1, stockLocation1, 2, 3, 5);
    }

    /**
     * Tests creation of an order based on the amount of stock on hand.
     */
    @Test
    public void testCreateOrder() {
        OrderGenerator generator = new OrderGenerator(taxRules, getArchetypeService());
        Party stockLocation = SupplierTestHelper.createStockLocation();
        Party supplier = TestHelper.createSupplier();
        Product product1 = TestHelper.createProduct();
        Product product2 = TestHelper.createProduct();

        addRelationships(product1, stockLocation, supplier, true, 1, 10, 5, new BigDecimal("2.0"), 1);
        addRelationships(product2, stockLocation, supplier, true, 1, 10, 5, BigDecimal.ONE, 1);

        List<FinancialAct> order = generator.createOrder(supplier, stockLocation, false);
        assertEquals(3, order.size());
        save(order);
        BigDecimal total = new BigDecimal("29.70");
        BigDecimal tax = new BigDecimal("2.70");
        checkOrder(order.get(0), supplier, stockLocation, total, tax);
        checkOrderItem(order, product1, BigDecimal.valueOf(9), new BigDecimal("19.80"), new BigDecimal("1.80"));
        checkOrderItem(order, product2, BigDecimal.valueOf(9), new BigDecimal("9.90"), new BigDecimal("0.90"));
    }

    /**
     * Verifies that when the idealQty is less the than the package size, an order for a single package will be
     * created.
     */
    @Test
    public void testCreateOrderForQuantityLessThanPackageSize() {
        OrderGenerator generator = new OrderGenerator(taxRules, getArchetypeService());
        Party stockLocation = SupplierTestHelper.createStockLocation();
        Party supplier = TestHelper.createSupplier();
        Product product1 = TestHelper.createProduct();

        addRelationships(product1, stockLocation, supplier, true, 0, 2, 2, new BigDecimal("2.0"), 10);

        List<FinancialAct> order = generator.createOrder(supplier, stockLocation, false);
        assertEquals(2, order.size());
        save(order);
        BigDecimal total = new BigDecimal("2.20");
        BigDecimal tax = new BigDecimal("0.20");
        checkOrder(order.get(0), supplier, stockLocation, total, tax);
        checkOrderItem(order.get(1), product1, BigDecimal.ONE, total, tax);
    }

    /**
     * Verifies that when the idealQty is greater the than the package size, the order will round up to the correct
     * quantity.
     */
    @Test
    public void testCreateOrderForQuantityGreaterThanPackageSize() {
        OrderGenerator generator = new OrderGenerator(taxRules, getArchetypeService());
        Party stockLocation = SupplierTestHelper.createStockLocation();
        Party supplier = TestHelper.createSupplier();
        Product product1 = TestHelper.createProduct();

        addRelationships(product1, stockLocation, supplier, true, 0, 22, 22, new BigDecimal("2.0"), 10);

        BigDecimal total = new BigDecimal("6.60");
        BigDecimal tax = new BigDecimal("0.60");

        List<FinancialAct> order = generator.createOrder(supplier, stockLocation, false);
        assertEquals(2, order.size());
        save(order);
        checkOrder(order.get(0), supplier, stockLocation, total, tax);
        checkOrderItem(order.get(1), product1, BigDecimal.valueOf(3), total, tax);
    }

    /**
     * Verifies that tax amounts are rounded correctly, if a product is taxed.
     */
    @Test
    public void testCreateOrderForProductWithTax() {
        OrderGenerator generator = new OrderGenerator(taxRules, getArchetypeService());
        Party stockLocation = SupplierTestHelper.createStockLocation();
        Party supplier = TestHelper.createSupplier();
        Product product = TestHelper.createProduct();
        product.addClassification(gst);
        save(product);

        BigDecimal total = new BigDecimal("1292.54");
        BigDecimal taxAmount = new BigDecimal("117.50");
        BigDecimal quantity = new BigDecimal("96");
        addRelationships(product, stockLocation, supplier, true, 0, quantity.intValue(), 0, new BigDecimal("12.24"), 1);

        List<FinancialAct> order = generator.createOrder(supplier, stockLocation, false);
        save(order);
        checkOrder(order.get(0), supplier, stockLocation, total, taxAmount);
        checkOrderItem(order.get(1), product, quantity, total, taxAmount);
    }

    /**
     * Verifies that products are ordered if there is no stock, and there are completed orders.
     */
    @Test
    public void testCreateOrderWithPriorDeliveries() {
        OrderGenerator generator = new OrderGenerator(taxRules, getArchetypeService());
        Party stockLocation = SupplierTestHelper.createStockLocation();
        Party supplier = TestHelper.createSupplier();
        Product product = TestHelper.createProduct();
        save(product);

        // create a cancelled order of 200 units
        createOrder(product, supplier, stockLocation, 200, 1, OrderStatus.CANCELLED, 0, 0, DeliveryStatus.PENDING);

        // create a fully delivered order of 100 units
        createOrder(product, supplier, stockLocation, 100, 1, OrderStatus.ACCEPTED, 100, 0, DeliveryStatus.FULL);

        // create a part delivered order. 50 units left to deliver
        createOrder(product, supplier, stockLocation, 100, 1, OrderStatus.ACCEPTED, 50, 0, DeliveryStatus.PART);

        // 0 units on hand, want 100
        addRelationships(product, stockLocation, supplier, true, 0, 100, 100, new BigDecimal("2.0"), 1);

        BigDecimal total = new BigDecimal("110.0");
        BigDecimal taxAmount = new BigDecimal("10.0");
        BigDecimal quantity = new BigDecimal("50");

        List<FinancialAct> order = generator.createOrder(supplier, stockLocation, true);
        assertEquals(2, order.size());
        save(order);
        checkOrder(order.get(0), supplier, stockLocation, total, taxAmount);
        checkOrderItem(order.get(1), product, quantity, total, taxAmount);
    }

    /**
     * Verifies that the supplier on an product-stock location relationship is selected over the preferred supplier.
     */
    @Test
    public void testStockLocationSupplier() {
        OrderGenerator generator = new OrderGenerator(taxRules, getArchetypeService());
        Party stockLocation = SupplierTestHelper.createStockLocation();
        Party supplier1 = TestHelper.createSupplier();
        Party supplier2 = TestHelper.createSupplier();
        Product product1 = TestHelper.createProduct();

        addProductSupplierRelationship(product1, supplier1, true, new BigDecimal("2.0"), 1);
        addProductSupplierRelationship(product1, supplier2, false, new BigDecimal("3.0"), 1);
        addProductStockLocationRelationship(product1, stockLocation, supplier2, 1, 10, 5);
        save(product1, supplier1, supplier2, stockLocation);

        // supplier1 has preferred flag set, but should be ignored as the product-stock location specifies supplier
        List<FinancialAct> order1 = generator.createOrder(supplier1, stockLocation, false);
        assertEquals(0, order1.size());

        BigDecimal total = new BigDecimal("29.70");
        BigDecimal tax = new BigDecimal("2.70");

        // verify order created for supplier2
        List<FinancialAct> order2 = generator.createOrder(supplier2, stockLocation, false);
        assertEquals(2, order2.size());
        save(order2);
        checkOrder(order2.get(0), supplier2, stockLocation, total, tax);
        checkOrderItem(order2.get(1), product1, BigDecimal.valueOf(9), total, tax);
    }

    /**
     * Verifies that if a product has 2 supplier has product supplier relationships to the same supplier,
     * the preferred one is chosen.
     */
    @Test
    public void testMultipleProductSupplierRelationshipsForProductSelectsPreferred() {
        OrderGenerator generator = new OrderGenerator(taxRules, getArchetypeService());
        Party stockLocation = SupplierTestHelper.createStockLocation();
        Party supplier1 = TestHelper.createSupplier();
        Product product1 = TestHelper.createProduct();

        addProductSupplierRelationship(product1, supplier1, false, new BigDecimal("3.0"), 1);
        addProductSupplierRelationship(product1, supplier1, true, new BigDecimal("2.0"), 1);
        addProductStockLocationRelationship(product1, stockLocation, null, 1, 10, 5);

        List<FinancialAct> order = generator.createOrder(supplier1, stockLocation, false);
        assertEquals(2, order.size());
        save(order);
        BigDecimal total = new BigDecimal("19.80");
        BigDecimal tax = new BigDecimal("1.80");
        checkOrder(order.get(0), supplier1, stockLocation, total, tax);
        checkOrderItem(order.get(1), product1, BigDecimal.valueOf(9), total, tax);
    }

    /**
     * Verifies that if a product has multiple preferred product-supplier relationships, the one with the lowest
     * id is chosen.
     */
    @Test
    public void testMultiplePreferredProductSupplierRelationshipsSelectsLowestId() {
        OrderGenerator generator = new OrderGenerator(taxRules, getArchetypeService());
        Party stockLocation = SupplierTestHelper.createStockLocation();
        Party supplier1 = TestHelper.createSupplier();
        Product product1 = TestHelper.createProduct();

        addProductSupplierRelationship(product1, supplier1, true, new BigDecimal("2.0"), 1);
        addProductSupplierRelationship(product1, supplier1, true, new BigDecimal("3.0"), 1);
        addProductStockLocationRelationship(product1, stockLocation, null, 1, 10, 5);

        List<FinancialAct> order = generator.createOrder(supplier1, stockLocation, false);
        assertEquals(2, order.size());
        save(order);
        BigDecimal total = new BigDecimal("19.80");
        BigDecimal tax = new BigDecimal("1.80");
        checkOrder(order.get(0), supplier1, stockLocation, total, tax);
        checkOrderItem(order.get(1), product1, BigDecimal.valueOf(9), total, tax);
    }

    /**
     * Verifies that if a product has multiple product-supplier relationships, and a stock location has a preferred
     * supplier, the product-supplier relationships the relationship with the lowest id is selected.
     */
    @Test
    public void testMultipleSupplierRelationshipsWithPreferredStockLocationSelectsLowestId() {
        OrderGenerator generator = new OrderGenerator(taxRules, getArchetypeService());
        Party stockLocation = SupplierTestHelper.createStockLocation();
        Party supplier1 = TestHelper.createSupplier();
        Party supplier2 = TestHelper.createSupplier();
        Product product1 = TestHelper.createProduct();

        addProductSupplierRelationship(product1, supplier1, false, new BigDecimal("2.0"), 1);
        addProductSupplierRelationship(product1, supplier1, false, new BigDecimal("3.0"), 1);
        addProductSupplierRelationship(product1, supplier2, true, new BigDecimal("4.0"), 1);
        addProductStockLocationRelationship(product1, stockLocation, supplier1, 1, 10, 5);

        List<FinancialAct> order = generator.createOrder(supplier1, stockLocation, false);
        assertEquals(2, order.size());
        save(order);
        BigDecimal total = new BigDecimal("19.80");
        BigDecimal tax = new BigDecimal("1.80");
        checkOrder(order.get(0), supplier1, stockLocation, total, tax);
        checkOrderItem(order.get(1), product1, BigDecimal.valueOf(9), total, tax);
    }

    /**
     * Verifies that if a product has multiple preferred product-supplier relationships, and a stock location has a
     * preferred supplier, the product-supplier relationships the relationship with the lowest id is selected.
     */
    @Test
    public void testMultiplePreferredSupplierRelationshipsWithPreferredStockLocationSelectsLowestId() {
        OrderGenerator generator = new OrderGenerator(taxRules, getArchetypeService());
        Party stockLocation = SupplierTestHelper.createStockLocation();
        Party supplier1 = TestHelper.createSupplier();
        Party supplier2 = TestHelper.createSupplier();
        Product product1 = TestHelper.createProduct();

        addProductSupplierRelationship(product1, supplier1, true, new BigDecimal("2.0"), 1);
        addProductSupplierRelationship(product1, supplier1, true, new BigDecimal("3.0"), 1);
        addProductSupplierRelationship(product1, supplier2, true, new BigDecimal("4.0"), 1);
        addProductStockLocationRelationship(product1, stockLocation, supplier1, 1, 10, 5);

        List<FinancialAct> order = generator.createOrder(supplier1, stockLocation, false);
        assertEquals(2, order.size());
        save(order);
        BigDecimal total = new BigDecimal("19.80");
        BigDecimal tax = new BigDecimal("1.80");
        checkOrder(order.get(0), supplier1, stockLocation, total, tax);
        checkOrderItem(order.get(1), product1, BigDecimal.valueOf(9), total, tax);
    }

    /**
     * Verifies an order matches that expected.
     *
     * @param order         the order
     * @param supplier      the expected supplier
     * @param stockLocation the expected stock location
     * @param total         the expected total
     * @param tax           the expected tax
     */
    private void checkOrder(FinancialAct order, Party supplier, Party stockLocation, BigDecimal total, BigDecimal tax) {
        ActBean bean = new ActBean(order);
        assertTrue(bean.isA(SupplierArchetypes.ORDER));
        assertEquals(supplier.getObjectReference(), bean.getNodeParticipantRef("supplier"));
        assertEquals(stockLocation.getObjectReference(), bean.getNodeParticipantRef("stockLocation"));
        checkEquals(total, order.getTotal());
        checkEquals(tax, order.getTaxAmount());
    }

    /**
     * Verifies an order item is present in an order
     *
     * @param order    the order
     * @param product  the expected product
     * @param quantity the expected quantity
     * @param total    the expected total
     * @param tax      the expected tax
     */
    private void checkOrderItem(List<FinancialAct> order, Product product, BigDecimal quantity, BigDecimal total,
                                BigDecimal tax) {
        for (FinancialAct act : order) {
            ActBean bean = new ActBean(act);
            if (bean.isA(SupplierArchetypes.ORDER_ITEM) && ObjectUtils.equals(product.getObjectReference(),
                                                                              bean.getNodeParticipantRef("product"))) {
                checkOrderItem(act, product, quantity, total, tax);
                return;
            }
        }
        fail("Order item not found");
    }

    /**
     * Verifies an order item matches that expected.
     *
     * @param item     the order item
     * @param product  the expected product
     * @param quantity the expected quantity
     * @param total    the expected total
     * @param tax      the expected tax
     */
    private void checkOrderItem(FinancialAct item, Product product, BigDecimal quantity,
                                BigDecimal total, BigDecimal tax) {
        ActBean bean = new ActBean(item);
        assertTrue(bean.isA(SupplierArchetypes.ORDER_ITEM));
        assertEquals(product.getObjectReference(), bean.getNodeParticipantRef("product"));
        checkEquals(quantity, item.getQuantity());
        checkEquals(total, item.getTotal());
        checkEquals(tax, item.getTaxAmount());
    }

    /**
     * Verifies the values in a {@code Stock} match that expected.
     *
     * @param stock         the stock to check
     * @param product       the expected product
     * @param supplier      the expected supplier
     * @param stockLocation the expected stock location
     * @param quantity      the expected on-hand quantity
     * @param onOrder       the expected on-order quantity
     * @param toOrder       the expected to-order quantity
     */
    private void checkStock(List<Stock> stock, Product product, Party supplier, Party stockLocation, int quantity,
                            int onOrder, int toOrder) {
        Stock s = getStock(stock, product);
        assertNotNull(s);
        checkStock(s, product, supplier, stockLocation, quantity, onOrder, toOrder);
    }

    /**
     * Returns a {@link Stock} corresponding to the supplied product.
     *
     * @param stock   the stock list
     * @param product the product
     * @return the corresponding stock, or {@code null} if none is found
     */
    private Stock getStock(List<Stock> stock, Product product) {
        Stock result = null;
        for (Stock s : stock) {
            if (s.getProduct().equals(product)) {
                assertNull(result);
                result = s;
            }
        }
        return result;
    }

    /**
     * Verifies the values in a {@code Stock} match that expected.
     *
     * @param stock         the stock to check
     * @param product       the expected product
     * @param supplier      the expected supplier
     * @param stockLocation the expected stock location
     * @param quantity      the expected on-hand quantity
     * @param onOrder       the expected on-order quantity
     * @param toOrder       the expected to-order quantity
     */
    private void checkStock(Stock stock, Product product, Party supplier, Party stockLocation, int quantity,
                            int onOrder, int toOrder) {
        assertEquals(product, stock.getProduct());
        assertEquals(supplier, stock.getSupplier());
        assertEquals(stockLocation, stock.getStockLocation());
        checkEquals(BigDecimal.valueOf(quantity), stock.getQuantity());
        checkEquals(BigDecimal.valueOf(onOrder), stock.getOnOrder());
        checkEquals(BigDecimal.valueOf(toOrder), stock.getToOrder());
    }

    /**
     * Creates an order.
     *
     * @param product       the product to order
     * @param supplier      the supplier to order from
     * @param stockLocation the stock location for delivery to
     * @param quantity      the order quantity
     * @param status        the order status
     * @return a new order
     */
    private FinancialAct createOrder(Product product, Party supplier, Party stockLocation, int quantity, String status) {
        return createOrder(product, supplier, stockLocation, quantity, 1, status);
    }

    /**
     * Creates an order.
     *
     * @param product       the product to order
     * @param supplier      the supplier to order from
     * @param stockLocation the stock location for delivery to
     * @param quantity      the order quantity
     * @param packageSize   the package size
     * @param status        the order status
     * @return a new order
     */
    private FinancialAct createOrder(Product product, Party supplier, Party stockLocation, int quantity,
                                     int packageSize, String status) {
        List<FinancialAct> order = createOrder(product, supplier, stockLocation, quantity, packageSize, status, 0, 0,
                                               DeliveryStatus.PENDING);
        return order.get(0);
    }

    /**
     * Creates an order.
     *
     * @param product           the product to order
     * @param supplier          the supplier to order from
     * @param stockLocation     the stock location for delivery to
     * @param quantity          the order quantity
     * @param status            the order status
     * @param receivedQuantity  the received quantity
     * @param cancelledQuantity the cancelled quantity
     * @param deliveryStatus    the delivery status
     * @return a new order
     */
    private List<FinancialAct> createOrder(Product product, Party supplier, Party stockLocation, int quantity,
                                           int packageSize, String status, int receivedQuantity, int cancelledQuantity,
                                           DeliveryStatus deliveryStatus) {
        FinancialAct orderItem = createOrderItem(product, BigDecimal.valueOf(quantity), packageSize, BigDecimal.ONE);
        FinancialAct order = createOrder(orderItem, supplier, stockLocation, status, receivedQuantity,
                                         cancelledQuantity, deliveryStatus);
        return Arrays.asList(order, orderItem);
    }

    /**
     * Creates an order.
     *
     * @param orderItem         the order item
     * @param supplier          the supplier to order from
     * @param stockLocation     the stock location for delivery to
     * @param status            the order status
     * @param receivedQuantity  the received quantity
     * @param cancelledQuantity the cancelled quantity
     * @param deliveryStatus    the delivery status
     * @return a new order
     */
    private FinancialAct createOrder(FinancialAct orderItem, Party supplier, Party stockLocation,
                                     String status, int receivedQuantity, int cancelledQuantity,
                                     DeliveryStatus deliveryStatus) {
        ActBean itemBean = new ActBean(orderItem);
        itemBean.setValue("receivedQuantity", BigDecimal.valueOf(receivedQuantity));
        itemBean.setValue("cancelledQuantity", BigDecimal.valueOf(cancelledQuantity));
        FinancialAct order = createOrder(supplier, stockLocation, orderItem);
        ActBean orderBean = new ActBean(order);
        order.setStatus(status);
        orderBean.setValue("deliveryStatus", deliveryStatus.toString());
        save(order, orderItem);
        return order;
    }

    /**
     * Creates relationships between a product and stock location and product and supplier.
     *
     * @param product       the product
     * @param stockLocation the stock location
     * @param supplier      the supplier
     * @param preferred     indicates if the supplier is the preferred supplier
     * @param quantity      the quantity
     * @param idealQty      the ideal quantity
     * @param criticalQty   the critical quantity
     * @return the product-supplier relationship
     */
    private ProductSupplier addRelationships(Product product, Party stockLocation, Party supplier, boolean preferred,
                                             int quantity, int idealQty, int criticalQty) {
        return addRelationships(product, stockLocation, supplier, preferred, quantity, idealQty, criticalQty,
                                BigDecimal.ONE, 1);
    }

    /**
     * Creates relationships between a product and stock location and product and supplier.
     *
     * @param product       the product
     * @param stockLocation the stock location
     * @param supplier      the supplier
     * @param preferred     indicates if the supplier is the preferred supplier
     * @param quantity      the quantity
     * @param idealQty      the ideal quantity
     * @param criticalQty   the critical quantity
     * @param unitPrice     the unit price
     * @return the product-supplier relationship
     */
    private ProductSupplier addRelationships(Product product, Party stockLocation, Party supplier, boolean preferred,
                                             int quantity, int idealQty, int criticalQty, BigDecimal unitPrice,
                                             int packageSize) {
        return addRelationships(product, stockLocation, supplier, preferred, BigDecimal.valueOf(quantity), idealQty,
                                criticalQty, unitPrice, packageSize);
    }

    /**
     * Creates relationships between a product and stock location and product and supplier.
     *
     * @param product       the product
     * @param stockLocation the stock location
     * @param supplier      the supplier
     * @param preferred     indicates if the supplier is the preferred supplier
     * @param quantity      the quantity
     * @param idealQty      the ideal quantity
     * @param criticalQty   the critical quantity
     * @param unitPrice     the unit price
     * @return the product-supplier relationship
     */
    private ProductSupplier addRelationships(Product product, Party stockLocation, Party supplier, boolean preferred,
                                             BigDecimal quantity, int idealQty, int criticalQty, BigDecimal unitPrice,
                                             int packageSize) {
        addProductStockLocationRelationship(product, stockLocation, null, quantity, idealQty, criticalQty);
        return addProductSupplierRelationship(product, supplier, preferred, unitPrice, packageSize);
    }

    /**
     * Adds a product-supplier relationship.
     *
     * @param product     the product
     * @param supplier    the supplier
     * @param preferred   indiciates if its the preferred relationship
     * @param unitPrice   the unit price
     * @param packageSize the package size
     * @return the relationship
     */
    private ProductSupplier addProductSupplierRelationship(Product product, Party supplier, boolean preferred,
                                                           BigDecimal unitPrice,
                                                           int packageSize) {
        EntityBean bean = new EntityBean(product);
        ProductSupplier ps = new ProductSupplier(bean.addNodeTarget("suppliers", supplier),
                                                 getArchetypeService());
        ps.setPreferred(preferred);
        ps.setPackageSize(packageSize);
        ps.setNettPrice(unitPrice);
        save(product, supplier);
        return ps;
    }

    /**
     * Adds a product-stock location relationship.
     *
     * @param product       the product
     * @param stockLocation the stock location
     * @param supplier      the supplier
     * @param quantity      the quantity
     * @param idealQty      the ideal quantity
     * @param criticalQty   the critical quantity
     * @return a new relationship
     */
    private EntityLink addProductStockLocationRelationship(Product product, Party stockLocation, Party supplier,
                                                           int quantity, int idealQty, int criticalQty) {
        return addProductStockLocationRelationship(product, stockLocation, supplier, BigDecimal.valueOf(quantity),
                                                   idealQty, criticalQty);
    }

    /**
     * Adds a product-stock location relationship.
     *
     * @param product       the product
     * @param stockLocation the stock location
     * @param supplier      the supplier
     * @param quantity      the quantity
     * @param idealQty      the ideal quantity
     * @param criticalQty   the critical quantity
     * @return a new relationship
     */
    private EntityLink addProductStockLocationRelationship(Product product, Party stockLocation, Party supplier,
                                                           BigDecimal quantity, int idealQty, int criticalQty) {
        EntityBean bean = new EntityBean(product);
        EntityLink relationship = (EntityLink) bean.addNodeTarget("stockLocations", stockLocation);
        IMObjectBean productStockLocation = new IMObjectBean(relationship);
        if (supplier != null) {
            productStockLocation.setValue("supplier", supplier.getObjectReference());
        }
        productStockLocation.setValue("quantity", quantity);
        productStockLocation.setValue("idealQty", idealQty);
        productStockLocation.setValue("criticalQty", criticalQty);
        save(product, stockLocation);
        return relationship;
    }
}
