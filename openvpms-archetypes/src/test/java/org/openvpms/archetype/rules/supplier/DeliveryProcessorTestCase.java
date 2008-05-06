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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.supplier;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;


/**
 * Tests the {@link DeliveryProcessor} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DeliveryProcessorTestCase extends ArchetypeServiceTest {

    /**
     * The supplier.
     */
    private Party supplier;

    /**
     * The stock location.
     */
    private Party stockLocation;

    /**
     * The product.
     */
    private Product product;
    private static final String PACKAGE_UNITS = "BOX";


    /**
     * Tests the {@link DeliveryProcessor} when invoked via
     * the <em>archetypeService.save.act.supplierDelivery</em> and
     * the <em>archetypeService.save.act.supplierReturn</em> rules.
     */
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

        Act orderReturn = createReturn(quantity, 1);
        checkProductStockLocationRelationship(quantity);

        orderReturn.setStatus(ActStatus.POSTED);
        save(orderReturn);
        checkProductStockLocationRelationship(BigDecimal.ZERO);
    }

    /**
     * Tests that the {@link DeliveryProcessor} updates orders associated with
     * the delivery/return.
     */
    public void testPostDeliveryUpdatesOrder() {
        BigDecimal quantity = new BigDecimal(5);
        BigDecimal delivery1Quantity = new BigDecimal(3);
        BigDecimal delivery2Quantity = new BigDecimal(7);

        // create an order with a single item, and post it
        FinancialAct orderItem = createOrderItem(quantity, 1);
        Act order = createOrder(orderItem);
        order.setStatus(ActStatus.POSTED);
        save(order);

        checkOrder(order, orderItem, DeliveryStatus.PENDING, BigDecimal.ZERO);

        // create a new delivery associated with the order item
        Act delivery1 = createDelivery(delivery1Quantity, 1, orderItem);

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
        Act return1 = createReturn(delivery1Quantity, 1, orderItem);
        return1.setStatus(ActStatus.POSTED);
        save(return1);
        checkOrder(order, orderItem, DeliveryStatus.PENDING, BigDecimal.ZERO);
        checkProductStockLocationRelationship(BigDecimal.ZERO);

        // create a new delivery associated with the order item
        orderItem = get(orderItem); // refresh
        Act delivery2 = createDelivery(delivery2Quantity, 1, orderItem);
        delivery2.setStatus(ActStatus.POSTED);
        save(delivery2);
        checkOrder(order, orderItem, DeliveryStatus.FULL, delivery2Quantity);
        checkProductStockLocationRelationship(delivery2Quantity);
    }

    /**
     * Verifies that the order quantity is updated correctly when a
     * delivery/return is posted with a different package size.
     */
    public void testQuantityConversion() {
        BigDecimal quantity = BigDecimal.ONE;
        int packageSize = 20;
        FinancialAct orderItem = createOrderItem(quantity, packageSize);
        Act order = createOrder(orderItem);
        order.setStatus(ActStatus.POSTED);
        save(order);

        // deliver 2 units, containing 5 items each
        Act delivery1 = createDelivery(new BigDecimal(2), 5, orderItem);
        delivery1.setStatus(ActStatus.POSTED);
        save(delivery1);
        checkOrder(order, orderItem, DeliveryStatus.PART,
                   new BigDecimal("0.5"));

        // now return the units
        orderItem = get(orderItem); // refresh
        Act return1 = createReturn(new BigDecimal(1), 10, orderItem);
        return1.setStatus(ActStatus.POSTED);
        save(return1);
        checkOrder(order, orderItem, DeliveryStatus.PENDING, BigDecimal.ZERO);

        // deliver 10 units, containing 2 items each
        orderItem = get(orderItem); // refresh
        Act delivery2 = createDelivery(new BigDecimal(10), 2, orderItem);
        delivery2.setStatus(ActStatus.POSTED);
        save(delivery2);
        checkOrder(order, orderItem, DeliveryStatus.FULL, quantity);
    }

    /**
     * Verifies that the <em>entityRelationship.productSupplier</em> is
     * updated when a delivery is <em>POSTED</em>
     */
    public void testProductSupplierUpdate() {
        BigDecimal quantity = BigDecimal.ONE;
        int packageSize = 20;
        BigDecimal nettPrice1 = new BigDecimal("10.00");
        BigDecimal nettPrice2 = new BigDecimal("12.50");

        Act delivery1 = createDelivery(quantity, packageSize, nettPrice1);
        checkProductSupplier(-1, null); // not yet posted

        delivery1.setStatus(ActStatus.POSTED);
        save(delivery1);
        checkProductSupplier(packageSize, nettPrice1);

        Act delivery2 = createDelivery(quantity, packageSize, nettPrice2);
        delivery2.setStatus(ActStatus.POSTED);
        save(delivery2);
        checkProductSupplier(packageSize, nettPrice2);

        // verify a return doesn't update the product-supplier relationship
        Act return1 = createReturn(quantity, packageSize, nettPrice1);
        return1.setStatus(ActStatus.POSTED);
        save(return1);
        checkProductSupplier(packageSize, nettPrice2);
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        product = TestHelper.createProduct();
        stockLocation = createStockLocation();
        supplier = TestHelper.createSupplier();
        TestHelper.getClassification("lookup.uom", PACKAGE_UNITS);
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
    private void checkOrder(Act order, FinancialAct orderItem,
                            DeliveryStatus status, BigDecimal quantity) {
        order = get(order);
        orderItem = get(orderItem);
        ActBean bean = new ActBean(order);
        assertEquals(status.toString(), bean.getString("deliveryStatus"));

        ActBean itemBean = new ActBean(orderItem);
        assertEquals(quantity, itemBean.getBigDecimal("receivedQuantity"));
    }

    /**
     * Verifies that the <em>enttiyRelationship.productStockLocation</em>
     * assopociated with the product and stock location matches that expected.
     *
     * @param quantity the expected quantity, or <tt>null</tt> if the
     *                 relationship shouldn't exist
     */
    private void checkProductStockLocationRelationship(BigDecimal quantity) {
        product = get(product);
        EntityBean bean = new EntityBean(product);
        EntityRelationship relationship = bean.getRelationship(stockLocation);
        if (quantity == null) {
            assertNull(relationship);
        } else {
            assertNotNull(relationship);
            IMObjectBean relBean = new IMObjectBean(relationship);
            assertEquals(quantity, relBean.getBigDecimal("quantity"));
        }
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
        SupplierRules rules = new SupplierRules();
        supplier = get(supplier);
        product = get(product);
        if (packageSize < 0) {
            assertNull(rules.getProductSupplier(supplier, product, packageSize,
                                                PACKAGE_UNITS));
        } else {
            ProductSupplier ps
                    = rules.getProductSupplier(supplier, product, packageSize,
                                               PACKAGE_UNITS);
            assertNotNull(ps);
            assertEquals(nettPrice, ps.getNettPrice());
        }
    }

    /**
     * Creates an order associated with an order item.
     *
     * @param orderItem the order item
     * @return a new order
     */
    private Act createOrder(FinancialAct orderItem) {
        ActBean bean = createAct(SupplierArchetypes.ORDER);
        bean.addRelationship(SupplierArchetypes.ORDER_ITEM_RELATIONSHIP,
                             orderItem);
        bean.save();
        save(orderItem);
        return bean.getAct();
    }

    /**
     * Creates an order item.
     *
     * @param quantity    the quantity
     * @param packageSize the package size
     * @return a new order item
     */
    private FinancialAct createOrderItem(BigDecimal quantity, int packageSize) {
        FinancialAct item = createItem(SupplierArchetypes.ORDER_ITEM, quantity,
                                       packageSize, null);
        ActBean bean = new ActBean(item);
        bean.setValue("packageSize", packageSize);
        return item;
    }

    /**
     * Creates and saves a delivery.
     *
     * @param quantity    the delivery quantity
     * @param packageSize the package size
     * @param nettPrice   the nett price. May be <tt>null</tt>
     * @return a new delivery
     */
    private Act createDelivery(BigDecimal quantity, int packageSize,
                               BigDecimal nettPrice) {
        return createActs(SupplierArchetypes.DELIVERY,
                          SupplierArchetypes.DELIVERY_ITEM,
                          SupplierArchetypes.DELIVERY_ITEM_RELATIONSHIP,
                          quantity, packageSize, nettPrice);
    }

    /**
     * Creates and saves a delivery.
     *
     * @param quantity    the delivery quantity
     * @param packageSize the package size
     * @return a new delivery
     */
    private Act createDelivery(BigDecimal quantity, int packageSize) {
        return createDelivery(quantity, packageSize, (BigDecimal) null);
    }

    /**
     * Creates and saves a delivery, associated with an order item.
     *
     * @param quantity    the delivery quantity
     * @param packageSize the delivery package size
     * @param orderItem   the order item
     * @return a new delivery
     */
    private Act createDelivery(BigDecimal quantity, int packageSize,
                               FinancialAct orderItem) {
        ActBean bean = createAct(SupplierArchetypes.DELIVERY);
        Act item = createDeliveryItem(quantity, packageSize, orderItem);
        bean.addRelationship(SupplierArchetypes.DELIVERY_ITEM_RELATIONSHIP,
                             item);
        bean.save();
        return bean.getAct();
    }

    /**
     * Creates a delivery item, associated with an order item.
     *
     * @param quantity    the quantity
     * @param packageSize the package size
     * @param orderItem   the order item
     * @return a new delivery item
     */
    private FinancialAct createDeliveryItem(BigDecimal quantity,
                                            int packageSize,
                                            FinancialAct orderItem) {
        return createItem(SupplierArchetypes.DELIVERY_ITEM,
                          SupplierArchetypes.DELIVERY_ORDER_ITEM_RELATIONSHIP,
                          quantity, packageSize, orderItem);
    }

    /**
     * Creates a return.
     *
     * @param quantity    the return quantity
     * @param packageSize the return package size
     * @return a new return
     */
    private Act createReturn(BigDecimal quantity, int packageSize) {
        return createReturn(quantity, packageSize, (BigDecimal) null);
    }

    /**
     * Creates a return.
     *
     * @param quantity    the return quantity
     * @param packageSize the return package size
     * @param nettPrice   the nett price. May be <tt>null</tt>
     * @return a new return
     */
    private Act createReturn(BigDecimal quantity, int packageSize,
                             BigDecimal nettPrice) {
        return createActs(SupplierArchetypes.RETURN,
                          SupplierArchetypes.RETURN_ITEM,
                          SupplierArchetypes.RETURN_ITEM_RELATIONSHIP,
                          quantity, packageSize, nettPrice);
    }

    /**
     * Create a new return, associated with an order item.
     *
     * @param quantity    the return quantity
     * @param packageSize the return package size
     * @param orderItem   the order item
     * @return a new return
     */
    private Act createReturn(BigDecimal quantity, int packageSize,
                             FinancialAct orderItem) {
        ActBean bean = createAct(SupplierArchetypes.RETURN);
        Act item = createReturnItem(quantity, packageSize, orderItem);
        bean.addRelationship(SupplierArchetypes.RETURN_ITEM_RELATIONSHIP,
                             item);
        bean.save();
        return bean.getAct();
    }

    /**
     * Creates a return item, associated with an order item.
     *
     * @param quantity    the quantity
     * @param packageSize the package size
     * @param orderItem   the order item
     * @return a new return item
     */
    private FinancialAct createReturnItem(BigDecimal quantity,
                                          int packageSize,
                                          FinancialAct orderItem) {
        return createItem(SupplierArchetypes.RETURN_ITEM,
                          SupplierArchetypes.RETURN_ORDER_ITEM_RELATIONSHIP,
                          quantity, packageSize, orderItem);
    }

    /**
     * Creates and saves a new supplier act associated with an act item.
     *
     * @param shortName             the act short name
     * @param itemShortName         the act item short name
     * @param relationshipShortName the relationship short name
     * @param quantity              the item quantity
     * @param packageSize           the item package size
     * @param nettPrice             the item nett price
     * @return a new act
     */
    private Act createActs(String shortName, String itemShortName,
                           String relationshipShortName, BigDecimal quantity,
                           int packageSize, BigDecimal nettPrice) {
        ActBean bean = createAct(shortName);
        FinancialAct item = createItem(itemShortName, quantity, packageSize,
                                       nettPrice);

        bean.addRelationship(relationshipShortName, item);
        bean.save();
        save(item);
        return bean.getAct();
    }

    /**
     * Creates a new supplier act.
     *
     * @param shortName the act short name
     * @return a new act
     */
    private ActBean createAct(String shortName) {
        Act act = (Act) create(shortName);
        ActBean bean = new ActBean(act);
        bean.addParticipation(SupplierArchetypes.SUPPLIER_PARTICIPATION,
                              supplier);
        bean.addParticipation(SupplierArchetypes.STOCK_LOCATION_PARTICIPATION,
                              stockLocation);
        return bean;
    }

    /**
     * Creates a new supplier act item associated with an order item.
     *
     * @param shortName             the act short name
     * @param relationshipShortName the order item relationship short name
     * @param quantity              the quantity
     * @param packageSize           the package size
     * @param orderItem             the order item
     * @return a new act
     */
    private FinancialAct createItem(String shortName,
                                    String relationshipShortName,
                                    BigDecimal quantity,
                                    int packageSize,
                                    FinancialAct orderItem) {
        FinancialAct act = createItem(shortName, quantity, packageSize,
                                      null);
        ActBean bean = new ActBean(act);
        bean.addRelationship(relationshipShortName, orderItem);
        bean.save();
        save(orderItem);
        return act;
    }

    /**
     * Creates a new supplier act item.
     *
     * @param shortName   the act short name
     * @param quantity    the quantity
     * @param packageSize the package size
     * @param nettPrice   the nett price. May be <tt>null</tt>
     * @return a new act
     */
    private FinancialAct createItem(String shortName, BigDecimal quantity,
                                    int packageSize, BigDecimal nettPrice) {
        FinancialAct item = (FinancialAct) create(shortName);
        ActBean bean = new ActBean(item);
        bean.addParticipation(SupplierArchetypes.PRODUCT_PARTICIPATION,
                              product);
        item.setQuantity(quantity);
        bean.setValue("packageSize", packageSize);
        bean.setValue("packageUnits", PACKAGE_UNITS);
        bean.setValue("nettPrice", nettPrice);
        return item;
    }

    /**
     * Creates and saves a new stock location.
     *
     * @return a new stock location
     */
    private Party createStockLocation() {
        Party stockLocation = (Party) create(SupplierArchetypes.STOCK_LOCATION);
        stockLocation.setName("STOCK-LOCATION-" + stockLocation.hashCode());
        save(stockLocation);
        return stockLocation;
    }
}
