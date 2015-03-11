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

package org.openvpms.archetype.rules.supplier;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.product.ProductRules;
import org.openvpms.archetype.rules.product.ProductSupplier;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityLink;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * Tests the {@link DeliveryProcessor} class.
 *
 * @author Tim Anderson
 */
public class DeliveryProcessorTestCase extends AbstractSupplierTest {

    /**
     * The product rules.
     */
    private ProductRules rules;

    /**
     * Sets up the test case.
     */
    @Override
    @Before
    public void setUp() {
        super.setUp();
        rules = new ProductRules(getArchetypeService());
    }

    /**
     * Tests the {@link DeliveryProcessor} when invoked via the <em>archetypeService.save.act.supplierDelivery</em> and
     * the <em>archetypeService.save.act.supplierReturn</em> rules.
     */
    @Test
    public void testPostDelivery() {
        BigDecimal quantity = new BigDecimal(5);
        Act delivery = createDelivery(quantity, 1);

        checkProductStockLocationRelationship(null);
        // there should be no relationship until the delivery is posted

        delivery.setStatus(ActStatus.POSTED);
        save(delivery);
        checkProductStockLocationRelationship(quantity);

        // re-save the delivery, and verify the relationship hasn't changed
        save(delivery);
        checkProductStockLocationRelationship(quantity);

        Act orderReturn = createReturn(quantity, 1, BigDecimal.ONE);
        checkProductStockLocationRelationship(quantity);

        orderReturn.setStatus(ActStatus.POSTED);
        save(orderReturn);
        checkProductStockLocationRelationship(BigDecimal.ZERO);
    }

    /**
     * Tests that the {@link DeliveryProcessor} updates orders associated with
     * the delivery/return.
     */
    @Test
    public void testPostDeliveryUpdatesOrder() {
        BigDecimal quantity = new BigDecimal(5);
        BigDecimal delivery1Quantity = new BigDecimal(3);
        BigDecimal delivery2Quantity = new BigDecimal(7);
        BigDecimal unitPrice = BigDecimal.ONE;

        // create an order with a single item, and post it
        FinancialAct orderItem = createOrderItem(quantity, 1, unitPrice);
        Act order = createOrder(orderItem);
        order.setStatus(ActStatus.POSTED);
        save(order);

        checkOrder(order, orderItem, DeliveryStatus.PENDING, BigDecimal.ZERO);

        // create a new delivery associated with the order item
        Act delivery1 = createDelivery(delivery1Quantity, 1, BigDecimal.ONE,
                                       orderItem);

        // there should be no relationship until the delivery is posted,
        // and the order shouldn't update
        checkProductStockLocationRelationship(null);
        checkOrder(order, orderItem, DeliveryStatus.PENDING, BigDecimal.ZERO);

        // now post the delivery
        delivery1.setStatus(ActStatus.POSTED);
        save(delivery1);
        checkOrder(order, orderItem, DeliveryStatus.PART, delivery1Quantity);
        checkProductStockLocationRelationship(delivery1Quantity);

        // save the delivery again, and verify quantities don't change
        save(delivery1);
        checkOrder(order, orderItem, DeliveryStatus.PART, delivery1Quantity);
        checkProductStockLocationRelationship(delivery1Quantity);

        // now return that which was delivered by delivery1
        orderItem = get(orderItem); // refresh
        Act return1 = createReturn(delivery1Quantity, 1, BigDecimal.ONE,
                                   orderItem);
        return1.setStatus(ActStatus.POSTED);
        save(return1);
        checkOrder(order, orderItem, DeliveryStatus.PENDING, BigDecimal.ZERO);
        checkProductStockLocationRelationship(BigDecimal.ZERO);

        // create a new delivery associated with the order item
        orderItem = get(orderItem); // refresh
        Act delivery2 = createDelivery(delivery2Quantity, 1, BigDecimal.ONE,
                                       orderItem);
        delivery2.setStatus(ActStatus.POSTED);
        save(delivery2);
        checkOrder(order, orderItem, DeliveryStatus.FULL, delivery2Quantity);
        checkProductStockLocationRelationship(delivery2Quantity);
    }

    /**
     * Tests that the {@link DeliveryProcessor} updates an order that is fulfilled by multiple deliveries.
     */
    @Test
    public void testOrderWithMultipleDeliveries() {
        BigDecimal quantity = new BigDecimal(5);
        BigDecimal unitPrice = BigDecimal.ONE;

        // create an order with a two items, and post it
        FinancialAct orderItem1 = createOrderItem(BigDecimal.TEN, 1, unitPrice);
        FinancialAct orderItem2 = createOrderItem(BigDecimal.TEN, 1, unitPrice);
        Act order = createOrder(orderItem1, orderItem2);
        order.setStatus(ActStatus.POSTED);
        save(order);

        checkDeliveryStatus(order, DeliveryStatus.PENDING);

        // create a new delivery associated with the order item
        FinancialAct delivery1Item = createDeliveryItem(quantity, 1, unitPrice, orderItem1);
        FinancialAct delivery1 = createDelivery(delivery1Item);

        // there should be no relationship until the delivery is posted,
        // and the order shouldn't update
        checkProductStockLocationRelationship(null);
        checkDeliveryStatus(order, DeliveryStatus.PENDING);
        checkReceivedQuantity(orderItem1, BigDecimal.ZERO);
        checkReceivedQuantity(orderItem2, BigDecimal.ZERO);

        // now post the delivery
        delivery1.setStatus(ActStatus.POSTED);
        save(delivery1);
        checkDeliveryStatus(order, DeliveryStatus.PART);
        checkReceivedQuantity(orderItem1, quantity);
        checkReceivedQuantity(orderItem2, BigDecimal.ZERO);
        checkProductStockLocationRelationship(quantity);

        // create a new delivery that fulfills the remainder of orderItem1
        orderItem1 = get(orderItem1);
        FinancialAct delivery2Item = createDeliveryItem(quantity, 1, unitPrice, orderItem1);
        FinancialAct delivery2 = createDelivery(delivery2Item);
        delivery2.setStatus(ActStatus.POSTED);
        save(delivery2);

        // check the order
        checkDeliveryStatus(order, DeliveryStatus.PART);

        // create a new delivery that fulfills the order
        FinancialAct delivery3Item = createDeliveryItem(BigDecimal.TEN, 1, unitPrice, orderItem2);
        FinancialAct delivery3 = createDelivery(delivery3Item);
        delivery3.setStatus(ActStatus.POSTED);
        save(delivery3);

        // check the order
        checkDeliveryStatus(order, DeliveryStatus.FULL);
    }

    /**
     * Verifies that the order quantity is updated correctly when a
     * delivery/return is posted with a different package size.
     */
    @Test
    public void testQuantityConversion() {
        BigDecimal quantity = BigDecimal.ONE;
        int packageSize = 20;
        BigDecimal unitPrice = BigDecimal.ONE;

        FinancialAct orderItem = createOrderItem(quantity, packageSize,
                                                 unitPrice);
        Act order = createOrder(orderItem);
        order.setStatus(ActStatus.POSTED);
        save(order);

        // deliver 2 units, containing 5 items each
        Act delivery1 = createDelivery(new BigDecimal(2), 5, unitPrice,
                                       orderItem);
        delivery1.setStatus(ActStatus.POSTED);
        save(delivery1);
        checkOrder(order, orderItem, DeliveryStatus.PART,
                   new BigDecimal("0.5"));

        // now return the units
        orderItem = get(orderItem); // refresh
        Act return1 = createReturn(new BigDecimal(1), 10, unitPrice, orderItem);
        return1.setStatus(ActStatus.POSTED);
        save(return1);
        checkOrder(order, orderItem, DeliveryStatus.PENDING, BigDecimal.ZERO);

        // deliver 10 units, containing 2 items each
        orderItem = get(orderItem); // refresh
        Act delivery2 = createDelivery(new BigDecimal(10), 2, unitPrice,
                                       orderItem);
        delivery2.setStatus(ActStatus.POSTED);
        save(delivery2);
        checkOrder(order, orderItem, DeliveryStatus.FULL, quantity);
    }

    /**
     * Verifies that the <em>entityRelationship.productSupplier</em> is
     * updated when a delivery is <em>POSTED</em>.
     */
    @Test
    public void testProductSupplierUpdate() {
        BigDecimal quantity = BigDecimal.ONE;
        int packageSize = 20;
        BigDecimal unitPrice1 = new BigDecimal("10.00");
        BigDecimal unitPrice2 = new BigDecimal("12.50");

        Act delivery1 = createDelivery(quantity, packageSize, unitPrice1);
        checkProductSupplier(-1, null); // not yet posted

        delivery1.setStatus(ActStatus.POSTED);
        save(delivery1);
        checkProductSupplier(packageSize, unitPrice1);

        Act delivery2 = createDelivery(quantity, packageSize, unitPrice2);
        delivery2.setStatus(ActStatus.POSTED);
        save(delivery2);
        checkProductSupplier(packageSize, unitPrice2);

        // verify a return doesn't update the product-supplier relationship
        Act return1 = createReturn(quantity, packageSize, unitPrice1);
        return1.setStatus(ActStatus.POSTED);
        save(return1);
        checkProductSupplier(packageSize, unitPrice2);
    }

    /**
     * Verifies that <em>productPrice.unitPrice</em>s associated with a product
     * are updated.
     * <p/>
     */
    @Test
    public void testUnitPriceUpdate() {
        Product product = getProduct();
        BigDecimal initialCost = BigDecimal.ZERO;
        BigDecimal initialPrice = BigDecimal.ONE;

        // add a new price
        ProductPrice price = (ProductPrice) create("productPrice.unitPrice");
        IMObjectBean priceBean = new IMObjectBean(price);
        priceBean.setValue("cost", initialCost);
        priceBean.setValue("markup", BigDecimal.valueOf(100));
        priceBean.setValue("price", initialPrice);
        product.addProductPrice(price);
        save(product);

        // create a product-supplier relationship.
        // By default, it should not trigger auto price updates
        int packageSize = 20;
        ProductSupplier ps = createProductSupplier();
        ps.setPackageSize(packageSize);
        assertFalse(ps.isAutoPriceUpdate());
        ps.save();

        // post a delivery, and verify prices don't update
        BigDecimal unitPrice1 = new BigDecimal("10.00");
        BigDecimal quantity = BigDecimal.ONE;
        BigDecimal listPrice = new BigDecimal("20.00");

        Act delivery1 = createDelivery(quantity, packageSize, unitPrice1,
                                       listPrice);
        delivery1.setStatus(ActStatus.POSTED);
        save(delivery1);

        checkPrice(product, initialCost, initialPrice);

        // reload product-supplier relationship and set to auto update prices
        ps = getProductSupplier(packageSize);
        assertNotNull(ps);
        ps.setAutoPriceUpdate(true);
        ps.save();

        // post another delivery
        Act delivery2 = createDelivery(quantity, packageSize, unitPrice1,
                                       listPrice);

        delivery2.setStatus(ActStatus.POSTED);
        save(delivery2);

        // verify that the price has updated
        checkPrice(product, new BigDecimal("1.00"), new BigDecimal("2.00"));

        // now post a return. The price shouldn't update
        BigDecimal unitPrice2 = new BigDecimal("8.00");
        BigDecimal listPrice2 = new BigDecimal("15.00");
        Act delReturn = createReturn(quantity, packageSize, unitPrice2,
                                     listPrice2);
        delReturn.setStatus(ActStatus.POSTED);
        save(delReturn);

        // verify that the price has not updated
        checkPrice(product, new BigDecimal("1.00"), new BigDecimal("2.00"));

        // post another delivery
        BigDecimal unitPrice3 = new BigDecimal("8.00");
        BigDecimal listPrice3 = new BigDecimal("16.00");
        Act delivery3 = createDelivery(quantity, packageSize, unitPrice3, listPrice3);

        delivery3.setStatus(ActStatus.POSTED);
        save(delivery3);
        checkPrice(product, new BigDecimal("0.80"), new BigDecimal("1.60"));

        // now mark the supplier inactive. Subsequent deliveries shouldn't update prices
        Party supplier = get(getSupplier());
        supplier.setActive(false);
        save(supplier);

        // post another delivery
        BigDecimal unitPrice4 = new BigDecimal("10.00");
        BigDecimal listPrice4 = new BigDecimal("20.00");
        Act delivery4 = createDelivery(quantity, packageSize, unitPrice4, listPrice4);

        delivery4.setStatus(ActStatus.POSTED);
        save(delivery4);

        // verify that the price has not updated
        checkPrice(product, new BigDecimal("0.80"), new BigDecimal("1.60"));
    }

    /**
     * Verifies that batches are created when a delivery is finalised.
     */
    @Test
    public void testBatchCreation() {
        Party manufacturer = (Party) create(SupplierArchetypes.MANUFACTURER);
        manufacturer.setName("Z Manufacturer");
        save(manufacturer);

        // create a new delivery associated with the order item
        FinancialAct item1 = createDeliveryItem("batch1", null, null);
        Date expiry2 = TestHelper.getDate("2015-01-01");
        Date expiry3 = TestHelper.getDate("2015-06-01");
        Date expiry4 = TestHelper.getDate("2016-01-01");
        FinancialAct item2 = createDeliveryItem(null, expiry2, null);
        FinancialAct item3 = createDeliveryItem("batch3", expiry3, manufacturer);
        FinancialAct item4 = createDeliveryItem("batch4", expiry4, manufacturer);

        Entity batch4 = rules.createBatch(getProduct(), "batch4", expiry4, manufacturer);
        save(batch4);

        FinancialAct delivery1 = createDelivery(item1, item2, item3, item4);
        delivery1.setStatus(ActStatus.POSTED);
        save(delivery1);
        checkBatch("batch1", null, null);
        checkBatch(null, expiry2, null);
        checkBatch("batch3", expiry3, manufacturer);
        Entity actual = checkBatch("batch4", expiry4, manufacturer); // should be updated with the stock location
        assertEquals(batch4, actual);
    }

    /**
     * Verifies a batch exists with the specified attributes.
     *
     * @param batchNumber  the batch number. May be {@code null}
     * @param expiryDate   the batch expiry date. May be {@code null}
     * @param manufacturer the batch manufacturer. May be {@code null}
     * @return the batch
     */
    private Entity checkBatch(String batchNumber, Date expiryDate, Party manufacturer) {
        List<Entity> batches = rules.getBatches(getProduct(), batchNumber, expiryDate, manufacturer);
        assertEquals(1, batches.size());
        EntityBean bean = new EntityBean(batches.get(0));
        if (batchNumber != null) {
            assertEquals(batchNumber, bean.getString("name"));
        } else {
            assertEquals(getProduct().getName(), bean.getString("name"));
        }
        assertEquals(manufacturer, bean.getNodeTargetObject("manufacturer"));

        List<EntityLink> product = bean.getValues("product", EntityLink.class);
        assertEquals(1, product.size());
        IMObjectBean productBean = new IMObjectBean(product.get(0));
        assertEquals(expiryDate, productBean.getDate("activeEndTime"));
        assertTrue(bean.getNodeTargetEntityRefs("stockLocations").contains(getStockLocation().getObjectReference()));
        return bean.getEntity();
    }

    /**
     * Verifies a product has a single price with the expected cost and price.
     *
     * @param product the product
     * @param cost    the expected cost
     * @param price   the expected price
     */
    private void checkPrice(Product product, BigDecimal cost, BigDecimal price) {
        product = get(product); // reload product
        Set<ProductPrice> prices = product.getProductPrices();
        assertEquals(1, prices.size());
        ProductPrice p = prices.toArray(new ProductPrice[prices.size()])[0];
        IMObjectBean bean = new IMObjectBean(p);
        checkEquals(cost, bean.getBigDecimal("cost"));
        checkEquals(price, bean.getBigDecimal("price"));
    }

    /**
     * Verifies that the delivery status and received quantity on an order
     * and order item matches that expected.
     *
     * @param order     the order
     * @param orderItem the order item
     * @param status    the expected delivery status
     * @param quantity  the expected quantity
     */
    private void checkOrder(Act order, FinancialAct orderItem, DeliveryStatus status, BigDecimal quantity) {
        checkDeliveryStatus(order, status);
        checkReceivedQuantity(orderItem, quantity);
    }

    /**
     * Verfies that an order item has the expected received quantity.
     *
     * @param orderItem        the order item
     * @param receivedQuantity the expected
     */
    private void checkReceivedQuantity(FinancialAct orderItem, BigDecimal receivedQuantity) {
        orderItem = get(orderItem);
        ActBean itemBean = new ActBean(orderItem);
        checkEquals(receivedQuantity, itemBean.getBigDecimal("receivedQuantity"));
    }

    /**
     * Verifies that the delivery status of an order matches that expected.
     *
     * @param order  the order
     * @param status the expected delivery status
     */
    private void checkDeliveryStatus(Act order, DeliveryStatus status) {
        order = get(order);
        ActBean bean = new ActBean(order);
        assertEquals(status.toString(), bean.getString("deliveryStatus"));
    }

    /**
     * Verifies that the <em>entityRelationship.productSupplier</em> associated
     * with the supplier and product matches that expected.
     *
     * @param packageSize the expected package size, or <tt>-1</tt> if the
     *                    relationship shouldn't exist
     * @param nettPrice   the expected nett price
     */
    private void checkProductSupplier(int packageSize, BigDecimal nettPrice) {
        if (packageSize < 0) {
            assertNull(getProductSupplier(packageSize));
        } else {
            ProductSupplier ps = getProductSupplier(packageSize);
            assertNotNull(ps);
            checkEquals(nettPrice, ps.getNettPrice());
        }
    }

    /**
     * Returns product supplier for the specified package size.
     *
     * @param packageSize the package size
     * @return the product supplier, or <tt>null</tt> if none is found
     */
    private ProductSupplier getProductSupplier(int packageSize) {
        ProductRules rules = new ProductRules(getArchetypeService());
        Party supplier = get(getSupplier()); // make sure using the latest
        Product product = get(getProduct()); // instance of each
        return rules.getProductSupplier(product, supplier, null, packageSize, PACKAGE_UNITS);
    }

    /**
     * Helper to create a new product supplier relationship.
     *
     * @return the new relationship
     */
    private ProductSupplier createProductSupplier() {
        ProductRules rules = new ProductRules(getArchetypeService());
        Party supplier = get(getSupplier()); // make sure using the latest
        Product product = get(getProduct()); // instance of each
        ProductSupplier ps = rules.createProductSupplier(product, supplier);
        ps.setPackageUnits(PACKAGE_UNITS);
        return ps;
    }

    /**
     * Creates a delivery item with batch details.
     *
     * @param batchNumber  the batch number. May be {@code null}
     * @param expiryDate   the expiry date. May be {@code null}
     * @param manufacturer the manufacturer. May be {@code null}
     * @return a new delivery item
     */
    protected FinancialAct createDeliveryItem(String batchNumber, Date expiryDate, Party manufacturer) {
        FinancialAct item = createItem(SupplierArchetypes.DELIVERY_ITEM, BigDecimal.ONE, 1, BigDecimal.TEN,
                                       BigDecimal.ZERO);
        ActBean bean = new ActBean(item);
        bean.setValue("batchNumber", batchNumber);
        bean.setValue("expiryDate", expiryDate);
        if (manufacturer != null) {
            bean.setNodeParticipant("manufacturer", manufacturer);
        }
        return item;
    }

}
