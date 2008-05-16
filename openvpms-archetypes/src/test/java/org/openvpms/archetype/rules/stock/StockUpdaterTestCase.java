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

package org.openvpms.archetype.rules.stock;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import static org.openvpms.archetype.rules.product.ProductArchetypes.PRODUCT_PARTICIPATION;
import static org.openvpms.archetype.rules.stock.StockArchetypes.*;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.List;


/**
 * Tests the {@link StockUpdater} class, when invoked by the
 * <em>archetypeService.save.act.stockTransfer.before</em>
 * <em>archetypeService.save.act.stockAdjust.before</em>
 * <em>archetypeService.save.act.customerAccountChargesInvoice.before</em>,
 * <em>archetypeService.save.act.customerAccountChargesCredit.before</em> and
 * <em>archetypeService.save.act.customerAccountChargesCounter.before</em>
 * rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class StockUpdaterTestCase extends ArchetypeServiceTest {

    /**
     * The organisation location.
     */
    private Party location;

    /**
     * The product.
     */
    private Product product;

    /**
     * The customer.
     */
    private Party customer;

    /**
     * The patient.
     */
    private Party patient;

    /**
     * The stock location.
     */
    private Party stockLocation;


    /**
     * Verifies that stock is updated when an <em>act.stockTransfer</em>
     * is posted.
     */
    public void testTransfer() {
        BigDecimal quantity = new BigDecimal(100);
        Party xferLocation = createStockLocation();
        Act act = (Act) create(STOCK_TRANSFER);
        ActBean bean = new ActBean(act);
        bean.addParticipation(STOCK_LOCATION_PARTICIPATION, stockLocation);
        bean.addParticipation(STOCK_XFER_LOCATION_PARTICIPATION, xferLocation);
        Act item = (Act) create(STOCK_TRANSFER_ITEM);
        ActBean itemBean = new ActBean(item);
        bean.addRelationship(STOCK_TRANSFER_ITEM_RELATIONSHIP, item);
        itemBean.addParticipation(PRODUCT_PARTICIPATION, product);
        itemBean.setValue("quantity", quantity);
        itemBean.save();
        bean.save();

        // verify transfer doesn't take place till the act is posted
        assertEquals(BigDecimal.ZERO, getStock(stockLocation));
        assertEquals(BigDecimal.ZERO, getStock(xferLocation));

        // post the transfer
        bean.setValue("status", ActStatus.POSTED);
        bean.save();

        // verify stock at the from and to locations. Note that stock may
        // go negative
        assertEquals(quantity.negate(), getStock(stockLocation));
        assertEquals(quantity, getStock(xferLocation));

        // verify subsequent save doesn't change the stock
        bean.save();
        assertEquals(quantity.negate(), getStock(stockLocation));
        assertEquals(quantity, getStock(xferLocation));
    }

    /**
     * Verifies that stock is updated when an <em>act.stockAdjust</em>
     * is posted.
     */
    public void testAdjust() {
        BigDecimal quantity = new BigDecimal(100);
        Act act = (Act) create(STOCK_ADJUST);
        ActBean bean = new ActBean(act);
        bean.addParticipation(STOCK_LOCATION_PARTICIPATION, stockLocation);
        Act item = (Act) create(STOCK_ADJUST_ITEM);
        ActBean itemBean = new ActBean(item);
        bean.addRelationship(STOCK_ADJUST_ITEM_RELATIONSHIP, item);
        itemBean.addParticipation(PRODUCT_PARTICIPATION, product);
        itemBean.setValue("quantity", quantity);
        itemBean.save();
        bean.save();

        // verify stock is not adjusted till the act is posted
        assertEquals(BigDecimal.ZERO, getStock(stockLocation));

        // post the act
        bean.setValue("status", ActStatus.POSTED);
        bean.save();

        // verify stock adjusted
        assertEquals(quantity, getStock(stockLocation));

        // verify subsequent save doesn't change the stock
        bean.save();
        assertEquals(quantity, getStock(stockLocation));
    }

    /**
     * Verifies that stock is updated when an invoice, counter and credit
     * charge is posted.
     * <p/>
     * Note that the quantity can go negative to reflect the fact that
     * a delivery may be processed through after the stock itself is actually
     * used.
     */
    public void testChargeUpdate() {
        BigDecimal initialQuantity = BigDecimal.ZERO;
        BigDecimal invoiceQuantity = BigDecimal.valueOf(5);
        BigDecimal counterQuantity = BigDecimal.valueOf(20);
        BigDecimal creditQuantity = BigDecimal.valueOf(30);

        // add a relationship between the location and stock location
        EntityBean locBean = new EntityBean(location);
        locBean.addRelationship("entityRelationship.locationStockLocation",
                                stockLocation);
        locBean.save();
        save(stockLocation);

        // verify the stock when an invoice is saved
        List<FinancialAct> invoice = FinancialTestHelper.createChargesInvoice(
                new Money(100), customer, patient, product,
                ActStatus.IN_PROGRESS);
        BigDecimal expected = initialQuantity.subtract(invoiceQuantity);
        checkStockUpdate(invoice, invoiceQuantity, expected);

        // verify the stock when a counter charge is saved
        List<FinancialAct> counter = FinancialTestHelper.createChargesCounter(
                new Money(90), customer, product, ActStatus.IN_PROGRESS);
        expected = expected.subtract(counterQuantity);
        checkStockUpdate(counter, counterQuantity, expected);

        // verify the stock when a credit is saved
        List<FinancialAct> credit = FinancialTestHelper.createChargesCredit(
                new Money(100), customer, patient, product,
                ActStatus.IN_PROGRESS);
        expected = expected.add(creditQuantity);
        checkStockUpdate(credit, creditQuantity, expected);
    }

    /**
     * Verifies that there are no stock changes if the location doesn't have
     * an associated stock location.
     */
    public void testStockUpdateForNoStockLocation() {
        BigDecimal expected = BigDecimal.ZERO;
        BigDecimal quantity = BigDecimal.valueOf(100);

        // verify the stock when an invoice is saved
        List<FinancialAct> invoice = FinancialTestHelper.createChargesInvoice(
                new Money(100), customer, patient, product,
                ActStatus.IN_PROGRESS);
        checkStockUpdate(invoice, quantity, expected);

        // verify the stock when a counter charge is saved
        List<FinancialAct> counter = FinancialTestHelper.createChargesCounter(
                new Money(90), customer, product, ActStatus.IN_PROGRESS);
        checkStockUpdate(counter, quantity, expected);

        // verify the stock when a credit is saved
        List<FinancialAct> credit = FinancialTestHelper.createChargesCredit(
                new Money(100), customer, patient, product,
                ActStatus.IN_PROGRESS);
        checkStockUpdate(credit, quantity, expected);
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        location = TestHelper.createLocation();
        product = TestHelper.createProduct();
        customer = TestHelper.createCustomer();
        patient = TestHelper.createPatient();
        stockLocation = createStockLocation();
    }

    /**
     * Verifies the stock quantities before and after a charge is posted.
     *
     * @param acts     the charge acts
     * @param quantity the charge item quantity
     * @param expected the expected quantity after the charge is posted
     */
    private void checkStockUpdate(List<FinancialAct> acts, BigDecimal quantity,
                                  BigDecimal expected) {
        FinancialAct act = acts.get(0);
        FinancialAct item = acts.get(1);
        ActBean bean = new ActBean(act);
        bean.addParticipation("participation.location", location);
        item.setQuantity(quantity);

        // saving un-POSTED act shouldn't cause stock update
        assertFalse(ActStatus.POSTED.equals(act.getStatus()));
        BigDecimal current = getStock(stockLocation);
        save(acts);
        assertEquals(current, getStock(stockLocation));

        // now post the act
        act.setStatus(ActStatus.POSTED);
        save(act);

        assertEquals(expected, getStock(stockLocation));
    }

    /**
     * Returns the stock in hand for the product and specified stock location.
     *
     * @param location the stock location
     * @return the stock in hand
     */
    private BigDecimal getStock(Party location) {
        product = get(product);
        EntityBean prodBean = new EntityBean(product);
        EntityRelationship rel = prodBean.getRelationship(location);
        if (rel != null) {
            IMObjectBean relBean = new IMObjectBean(rel);
            return relBean.getBigDecimal("quantity");
        }
        return BigDecimal.ZERO;
    }

    /**
     * Helper to create a stock location.
     *
     * @return a new stock location
     */
    private Party createStockLocation() {
        Party result = (Party) create(StockArchetypes.STOCK_LOCATION);
        result.setName("STOCK-LOCATION-" + result.hashCode());
        save(result);
        return result;
    }

}
