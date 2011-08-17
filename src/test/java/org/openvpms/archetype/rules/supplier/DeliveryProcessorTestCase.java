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

import static org.junit.Assert.*;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.product.ProductRules;
import org.openvpms.archetype.rules.product.ProductSupplier;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.Set;


/**
 * Tests the {@link DeliveryProcessor} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DeliveryProcessorTestCase extends AbstractSupplierTest {

    /**
     * Tests the {@link DeliveryProcessor} when invoked via
     * the <em>archetypeService.save.act.supplierDelivery</em> and
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

    private void checkPrice(Product product, BigDecimal cost,
                            BigDecimal price) {
        product = get(getProduct()); // reload product
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
    private void checkOrder(Act order, FinancialAct orderItem,
                            DeliveryStatus status, BigDecimal quantity) {
        order = get(order);
        orderItem = get(orderItem);
        ActBean bean = new ActBean(order);
        assertEquals(status.toString(), bean.getString("deliveryStatus"));

        ActBean itemBean = new ActBean(orderItem);
        checkEquals(quantity, itemBean.getBigDecimal("receivedQuantity"));
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
        ProductRules rules = new ProductRules();
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
        ProductRules rules = new ProductRules();
        Party supplier = get(getSupplier()); // make sure using the latest
        Product product = get(getProduct()); // instance of each
        ProductSupplier ps = rules.createProductSupplier(product, supplier);
        ps.setPackageUnits(PACKAGE_UNITS);
        return ps;
    }

}
