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

import org.junit.Before;
import org.openvpms.archetype.rules.stock.StockArchetypes;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


/**
 * Base class for supplier test cases.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AbstractSupplierTest extends ArchetypeServiceTest {

    /**
     * Package units for act items.
     */
    protected static final String PACKAGE_UNITS = "BOX";

    /**
     * UN/CEFACT unit code corresponding to the package units.
     */
    protected static final String PACKAGE_UNIT_CODE = "BX";

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

    /**
     * The practice.
     */
    private Party practice;

    /**
     * The practice location.
     */
    private Party practiceLocation;


    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        product = TestHelper.createProduct();
        stockLocation = createStockLocation();

        // create a practice for currency and tax calculation purposes
        practiceLocation = TestHelper.createLocation();
        EntityBean locBean = new EntityBean(practiceLocation);
        locBean.addRelationship("entityRelationship.locationStockLocation", stockLocation);
        practice = TestHelper.getPractice();
        EntityBean pracBean = new EntityBean(practice);
        pracBean.addRelationship("entityRelationship.practiceLocation", practiceLocation);
        locBean.save();
        pracBean.save();

        supplier = TestHelper.createSupplier();

        // set up a unit of measure
        Lookup uom = TestHelper.getLookup("lookup.uom", PACKAGE_UNITS);
        IMObjectBean bean = new IMObjectBean(uom);
        bean.setValue("unitCode", PACKAGE_UNIT_CODE);
        bean.save();
    }

    /**
     * Returns the supplier.
     *
     * @return the supplier
     */
    protected Party getSupplier() {
        return supplier;
    }

    /**
     * Returns the product.
     *
     * @return the product
     */
    protected Product getProduct() {
        return product;
    }

    /**
     * Returns the practice.
     *
     * @return the practice
     */
    protected Party getPractice() {
        return practice;
    }

    /**
     * Returns the practice location.
     *
     * @return the practice location
     */
    protected Party getPracticeLocation() {
        return practiceLocation;
    }

    /**
     * Returns the stock location.
     *
     * @return the stock location
     */
    protected Party getStockLocation() {
        return stockLocation;
    }

    /**
     * Creates an order associated with order items.
     *
     * @param supplier   the supplier
     * @param orderItems the order item
     * @return a new order
     */
    protected FinancialAct createOrder(Party supplier, FinancialAct... orderItems) {
        List<Act> toSave = new ArrayList<Act>();
        ActBean bean = createAct(SupplierArchetypes.ORDER, supplier);
        BigDecimal total = BigDecimal.ZERO;
        for (FinancialAct item : orderItems) {
            bean.addRelationship(SupplierArchetypes.ORDER_ITEM_RELATIONSHIP, item);
            total = total.add(item.getTotal());
            toSave.add(item);
        }
        bean.setValue("amount", total);
        toSave.add(bean.getAct());
        save(toSave);
        return (FinancialAct) bean.getAct();
    }

    /**
     * Creates an order associated with order items.
     *
     * @param orderItems the order items
     * @return a new order
     */
    protected FinancialAct createOrder(FinancialAct... orderItems) {
        return createOrder(supplier, orderItems);
    }

    /**
     * Creates an order item.
     *
     * @param quantity    the quantity
     * @param packageSize the package size
     * @param unitPrice   the unit price
     * @return a new order item
     */
    protected FinancialAct createOrderItem(BigDecimal quantity, int packageSize, BigDecimal unitPrice) {
        return createOrderItem(product, quantity, packageSize, unitPrice);
    }

    /**
     * Creates an order item.
     *
     * @param product     the product
     * @param quantity    the quantity
     * @param packageSize the package size
     * @param unitPrice   the unit price
     * @return a new order item
     */
    protected FinancialAct createOrderItem(Product product, BigDecimal quantity, int packageSize, BigDecimal unitPrice) {
        return createItem(SupplierArchetypes.ORDER_ITEM, product, quantity, packageSize, unitPrice, BigDecimal.ZERO);
    }

    /**
     * Creates and saveacs a delivery.
     *
     * @param quantity    the delivery quantity
     * @param packageSize the package size
     * @param unitPrice   the unit price
     * @return a new delivery
     */
    protected FinancialAct createDelivery(BigDecimal quantity, int packageSize, BigDecimal unitPrice) {
        return createActs(SupplierArchetypes.DELIVERY,
                          SupplierArchetypes.DELIVERY_ITEM,
                          SupplierArchetypes.DELIVERY_ITEM_RELATIONSHIP,
                          quantity, packageSize, unitPrice, BigDecimal.ZERO);
    }

    /**
     * Creates and saves a delivery.
     *
     * @param quantity    the delivery quantity
     * @param packageSize the package size
     * @param unitPrice   the unit price
     * @param listPrice   the list price
     * @return a new delivery
     */
    protected FinancialAct createDelivery(BigDecimal quantity, int packageSize, BigDecimal unitPrice,
                                          BigDecimal listPrice) {
        return createActs(SupplierArchetypes.DELIVERY,
                          SupplierArchetypes.DELIVERY_ITEM,
                          SupplierArchetypes.DELIVERY_ITEM_RELATIONSHIP,
                          quantity, packageSize, unitPrice, listPrice);
    }

    /**
     * Creates and saves a delivery.
     *
     * @param quantity    the delivery quantity
     * @param packageSize the package size
     * @return a new delivery
     */
    protected Act createDelivery(BigDecimal quantity, int packageSize) {
        return createDelivery(quantity, packageSize, null);
    }

    /**
     * Creates and saves a delivery, associated with an order item.
     *
     * @param quantity    the delivery quantity
     * @param packageSize the delivery package size
     * @param unitPrice   the delivery unit price
     * @param orderItem   the order item
     * @return a new delivery
     */
    protected FinancialAct createDelivery(BigDecimal quantity, int packageSize, BigDecimal unitPrice,
                                          FinancialAct orderItem) {
        FinancialAct item = createDeliveryItem(quantity, packageSize, unitPrice, orderItem);
        return createDelivery(item);
    }

    /**
     * Creates and saves a delivery, associated with a delivery item.
     *
     * @param deliveryItems the delivery items
     * @return a new delivery
     */
    protected FinancialAct createDelivery(FinancialAct... deliveryItems) {
        ActBean bean = createAct(SupplierArchetypes.DELIVERY);
        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        List<Act> toSave = new ArrayList<Act>();
        toSave.add(bean.getAct());
        for (FinancialAct deliveryItem : deliveryItems) {
            bean.addRelationship(SupplierArchetypes.DELIVERY_ITEM_RELATIONSHIP, deliveryItem);
            amount = amount.add(deliveryItem.getTotal());
            tax = tax.add(deliveryItem.getTaxAmount());
            toSave.add(deliveryItem);
        }
        bean.setValue("amount", amount);
        bean.setValue("tax", tax);
        save(toSave);

        return (FinancialAct) bean.getAct();
    }

    /**
     * Creates a delivery item, associated with an order item.
     *
     * @param quantity    the quantity
     * @param packageSize the package size
     * @param unitPrice   the unit price
     * @param orderItem   the order item
     * @return a new delivery item
     */
    protected FinancialAct createDeliveryItem(BigDecimal quantity, int packageSize, BigDecimal unitPrice,
                                            FinancialAct orderItem) {
        return createItem(SupplierArchetypes.DELIVERY_ITEM,
                          SupplierArchetypes.DELIVERY_ORDER_ITEM_RELATIONSHIP,
                          quantity, packageSize, unitPrice, orderItem);
    }

    /**
     * Creates a return.
     *
     * @param quantity    the return quantity
     * @param packageSize the return package size
     * @param unitPrice   the unit price
     * @return a new return
     */
    protected Act createReturn(BigDecimal quantity, int packageSize, BigDecimal unitPrice) {
        return createReturn(quantity, packageSize, unitPrice, BigDecimal.ZERO);
    }

    /**
     * Creates a return.
     *
     * @param quantity    the return quantity
     * @param packageSize the return package size
     * @param unitPrice   the unit price
     * @param listPrice   the list price
     * @return a new return
     */
    protected Act createReturn(BigDecimal quantity, int packageSize, BigDecimal unitPrice, BigDecimal listPrice) {
        return createActs(SupplierArchetypes.RETURN,
                          SupplierArchetypes.RETURN_ITEM,
                          SupplierArchetypes.RETURN_ITEM_RELATIONSHIP,
                          quantity, packageSize, unitPrice, listPrice);
    }

    /**
     * Create a new return, associated with an order item.
     *
     * @param quantity    the return quantity
     * @param packageSize the return package size
     * @param unitPrice   the unit price
     * @param orderItem   the order item
     * @return a new return
     */
    protected Act createReturn(BigDecimal quantity, int packageSize, BigDecimal unitPrice, FinancialAct orderItem) {
        ActBean bean = createAct(SupplierArchetypes.RETURN);
        Act item = createReturnItem(quantity, packageSize, unitPrice, orderItem);
        bean.addRelationship(SupplierArchetypes.RETURN_ITEM_RELATIONSHIP, item);
        bean.save();
        return bean.getAct();
    }

    /**
     * Creates a return item, associated with an order item.
     *
     * @param quantity    the quantity
     * @param packageSize the package size
     * @param unitPrice   the unit price
     * @param orderItem   the order item
     * @return a new return item
     */
    private FinancialAct createReturnItem(BigDecimal quantity, int packageSize, BigDecimal unitPrice,
                                          FinancialAct orderItem) {
        return createItem(SupplierArchetypes.RETURN_ITEM,
                          SupplierArchetypes.RETURN_ORDER_ITEM_RELATIONSHIP,
                          quantity, packageSize, unitPrice, orderItem);
    }

    /**
     * Creates and saves a new supplier act associated with an act item.
     *
     * @param shortName             the act short name
     * @param itemShortName         the act item short name
     * @param relationshipShortName the relationship short name
     * @param quantity              the item quantity
     * @param packageSize           the item package size
     * @param unitPrice             the item unit price
     * @param listPrice             the item list price
     * @return a new act
     */
    private FinancialAct createActs(String shortName, String itemShortName,
                                    String relationshipShortName,
                                    BigDecimal quantity, int packageSize,
                                    BigDecimal unitPrice,
                                    BigDecimal listPrice) {
        ActBean bean = createAct(shortName);
        FinancialAct item = createItem(itemShortName, quantity, packageSize,
                                       unitPrice, listPrice);

        bean.addRelationship(relationshipShortName, item);
        save(bean.getAct(), item);
        return (FinancialAct) bean.getAct();
    }

    /**
     * Creates a new supplier act.
     *
     * @param shortName the act short name
     * @param supplier  the supplier
     * @return a new act
     */
    protected ActBean createAct(String shortName, Party supplier) {
        Act act = (Act) create(shortName);
        ActBean bean = new ActBean(act);
        bean.addParticipation(SupplierArchetypes.SUPPLIER_PARTICIPATION, supplier);
        bean.addParticipation(StockArchetypes.STOCK_LOCATION_PARTICIPATION, stockLocation);
        return bean;
    }

    /**
     * Creates a new supplier act.
     *
     * @param shortName the act short name
     * @return a new act
     */
    protected ActBean createAct(String shortName) {
        return createAct(shortName, supplier);
    }

    /**
     * Creates a new supplier act item associated with an order item.
     *
     * @param shortName             the act short name
     * @param relationshipShortName the order item relationship short name
     * @param quantity              the quantity
     * @param packageSize           the package size
     * @param unitPrice             the unit price
     * @param orderItem             the order item
     * @return a new act
     */
    protected FinancialAct createItem(String shortName, String relationshipShortName, BigDecimal quantity,
                                      int packageSize, BigDecimal unitPrice, FinancialAct orderItem) {
        FinancialAct act = createItem(shortName, quantity, packageSize, unitPrice, BigDecimal.ZERO);
        ActBean bean = new ActBean(act);
        bean.addRelationship(relationshipShortName, orderItem);
        save(act, orderItem);
        return act;
    }

    /**
     * Creates a new supplier act item.
     *
     * @param shortName   the act short name
     * @param quantity    the quantity
     * @param packageSize the package size
     * @param unitPrice   the unit price
     * @param listPrice   the list price
     * @return a new act
     */
    protected FinancialAct createItem(String shortName, BigDecimal quantity, int packageSize, BigDecimal unitPrice,
                                      BigDecimal listPrice) {
        return createItem(shortName, product, quantity, packageSize, unitPrice, listPrice);
    }

    /**
     * Creates a new supplier act item.
     *
     * @param shortName   the act short name
     * @param product     the product
     * @param quantity    the quantity
     * @param packageSize the package size
     * @param unitPrice   the unit price
     * @param listPrice   the list price
     * @return a new act
     */
    protected FinancialAct createItem(String shortName, Product product, BigDecimal quantity, int packageSize,
                                      BigDecimal unitPrice, BigDecimal listPrice) {
        FinancialAct item = (FinancialAct) create(shortName);
        ActBean bean = new ActBean(item);
        bean.addParticipation(StockArchetypes.STOCK_PARTICIPATION, product);
        item.setQuantity(quantity);
        bean.setValue("packageSize", packageSize);
        bean.setValue("packageUnits", PACKAGE_UNITS);
        bean.setValue("unitPrice", unitPrice);
        bean.setValue("listPrice", listPrice);
        getArchetypeService().deriveValues(item);
        return item;
    }

    /**
     * Creates and saves a new stock location.
     *
     * @return a new stock location
     */
    protected Party createStockLocation() {
        return SupplierTestHelper.createStockLocation();
    }

    /**
     * Verifies that the <em>enttiyRelationship.productStockLocation</em>
     * assopociated with the product and stock location matches that expected.
     *
     * @param quantity the expected quantity, or <tt>null</tt> if the relationship shouldn't exist
     */
    protected void checkProductStockLocationRelationship(BigDecimal quantity) {
        product = get(product);
        EntityBean bean = new EntityBean(product);
        EntityRelationship relationship = bean.getRelationship(stockLocation);
        if (quantity == null) {
            assertNull(relationship);
        } else {
            assertNotNull(relationship);
            IMObjectBean relBean = new IMObjectBean(relationship);
            checkEquals(quantity, relBean.getBigDecimal("quantity"));
        }
    }

}
