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

package org.openvpms.archetype.rules.stock;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * Tests the {@link ChargeStockUpdater} class, when invoked by the
 * <em>archetypeService.save.act.customerAccountInvoiceItem.before</em>,
 * <em>archetypeService.remove.act.customerAccountChargesInvoice.before</em>,
 * <em>archetypeService.remove.act.customerAccountInvoiceItem.before</em>,
 * <em>archetypeService.save.act.customerAccountCreditItem.before</em>,
 * <em>archetypeService.remove.act.customerAccountChargesCredit.before</em>,
 * <em>archetypeService.remove.act.customerAccountCreditItem.before</em>,
 * <em>archetypeService.save.act.customerAccountCounterItem.before</em>,
 * <em>archetypeService.remove.act.customerAccountChargesCounter.before</em> and
 * <em>archetypeService.remove.act.customerAccountCounterItem.before</em>
 * rules.
 *
 * @author Tim Anderson
 */
public class ChargeStockUpdaterTestCase extends AbstractStockTest {

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
     * The transaction manager.
     */
    private PlatformTransactionManager txnManager;


    /**
     * Verifies that stock is updated for an invoice.
     */
    @Test
    public void testInvoiceStockUpdate() {
        checkStockUpdate(createInvoice());
    }

    /**
     * Verifies that stock is updated when an invoice is removed.
     */
    @Test
    public void testInvoiceRemoval() {
        checkChargeRemoval(createInvoice());
    }

    /**
     * Verifies that stock is updated when an invoice item is removed.
     */
    @Test
    public void testInvoiceItemRemoval() {
        checkItemRemoval(createInvoice());
    }

    /**
     * Verifies that stock is updated for a counter charge.
     */
    @Test
    public void testCounterChargeStockUpdate() {
        checkStockUpdate(createCounterCharge());
    }

    /**
     * Verifies that stock is updated when a counter charge item is removed.
     */
    @Test
    public void testCounterChargeRemoval() {
        checkChargeRemoval(createCounterCharge());
    }

    /**
     * Verifies that stock is updated when a counter charge item is removed.
     */
    @Test
    public void testCounterChargeItemRemoval() {
        checkItemRemoval(createCounterCharge());
    }

    /**
     * Verifies that stock is updated for a credit charge.
     */
    @Test
    public void testCreditChargeStockUpdate() {
        checkStockUpdate(createCreditCharge());
    }

    /**
     * Verifies that stock is updated when a credit charge is removed.
     */
    @Test
    public void testCreditChargeRemoval() {
        checkChargeRemoval(createCreditCharge());
    }

    /**
     * Verifies that stock is updated when a credit charge item is removed.
     */
    @Test
    public void testCreditChargeItemRemoval() {
        checkItemRemoval(createCreditCharge());
    }

    /**
     * Verifies that stock is updated correctly if a charge is saved multiple times in a transaction.
     */
    @Test
    public void testMultipleSaveInTxn() {
        final List<FinancialAct> acts = createInvoice();
        FinancialAct item = acts.get(1);
        BigDecimal initialQuantity = BigDecimal.ZERO;
        BigDecimal quantity = BigDecimal.valueOf(5);

        item.setQuantity(quantity);

        checkEquals(initialQuantity, getStock(stockLocation, product));
        BigDecimal expected = getQuantity(initialQuantity, quantity, false);

        TransactionTemplate template = new TransactionTemplate(txnManager);
        template.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                save(acts);
                save(acts);
            }
        });
        checkEquals(expected, getStock(stockLocation, product));

        save(acts);  // stock shouldn't change if resaved
        checkEquals(expected, getStock(stockLocation, product));
    }

    /**
     * Verifies that the stock is updated correctly if referred to by two different items in a transaction.
     */
    @Test
    public void testMultipleStockUpdatesInTxn() {
        final List<FinancialAct> acts = new ArrayList<FinancialAct>(createInvoice());
        final FinancialAct item1 = acts.get(1);
        final FinancialAct item2 = FinancialTestHelper.createItem(CustomerAccountArchetypes.INVOICE_ITEM, Money.ONE,
                                                                  patient, product);
        addStockLocation(item2);
        acts.add(item2);

        BigDecimal initialQuantity = BigDecimal.ZERO;
        BigDecimal quantity = BigDecimal.valueOf(5);

        item1.setQuantity(quantity);
        item2.setQuantity(quantity);

        checkEquals(initialQuantity, getStock(stockLocation, product));
        BigDecimal expected = getQuantity(initialQuantity, quantity.add(quantity), false);

        TransactionTemplate template = new TransactionTemplate(txnManager);
        template.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                save(acts);
            }
        });
        checkEquals(expected, getStock(stockLocation, product));

        template.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                item1.setQuantity(BigDecimal.ONE);
                save(item1);
                remove(item2);
            }
        });
        expected = getQuantity(initialQuantity, BigDecimal.ONE, false);
        checkEquals(expected, getStock(stockLocation, product));
    }

    /**
     * Verifies that stock is updated correctly if an item is saved twice in the one transaction, but the first
     * save is incomplete.
     */
    @Test
    public void testPartialSaveInTxn() {
        final List<FinancialAct> acts = createInvoice();
        final FinancialAct invoice = acts.get(0);
        final FinancialAct item = acts.get(1);
        BigDecimal initialQuantity = BigDecimal.ZERO;
        BigDecimal quantity = BigDecimal.valueOf(5);

        item.setQuantity(quantity);

        checkEquals(initialQuantity, getStock(stockLocation, product));
        BigDecimal expected = getQuantity(initialQuantity, quantity, false);

        TransactionTemplate template = new TransactionTemplate(txnManager);
        template.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                save(item);
                save(item);
                save(invoice);
            }
        });
        checkEquals(expected, getStock(stockLocation, product));

        save(acts);  // stock shouldn't change if resaved
        checkEquals(expected, getStock(stockLocation, product));
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        product = TestHelper.createProduct();
        customer = TestHelper.createCustomer();
        patient = TestHelper.createPatient();
        stockLocation = createStockLocation();
        txnManager = applicationContext.getBean(PlatformTransactionManager.class);
    }

    /**
     * Verifies that stock is updated when for a charge.
     *
     * @param acts the charge act and item
     */
    private void checkStockUpdate(List<FinancialAct> acts) {
        FinancialAct item = acts.get(1);
        BigDecimal initialQuantity = BigDecimal.ZERO;
        BigDecimal quantity = BigDecimal.valueOf(5);
        Product product2 = TestHelper.createProduct();

        boolean credit = TypeHelper.isA(
                item, CustomerAccountArchetypes.CREDIT_ITEM);
        BigDecimal expected = getQuantity(initialQuantity, quantity, credit);

        item.setQuantity(quantity);

        checkEquals(initialQuantity, getStock(stockLocation, product));

        save(acts);
        checkEquals(expected, getStock(stockLocation, product));

        save(acts);  // stock shouldn't change if resaved
        checkEquals(expected, getStock(stockLocation, product));

        item = get(item);
        BigDecimal updatedQty = new BigDecimal(10);
        item.setQuantity(updatedQty);
        expected = getQuantity(initialQuantity, updatedQty, credit);
        save(item);
        checkEquals(expected, getStock(stockLocation, product));

        item = get(item);
        save(item);
        checkEquals(expected, getStock(stockLocation, product));

        ActBean itemBean = new ActBean(item);
        itemBean.setParticipant(ProductArchetypes.PRODUCT_PARTICIPATION,
                                product2);
        save(item);
        checkEquals(BigDecimal.ZERO, getStock(stockLocation, product));
        checkEquals(expected, getStock(stockLocation, product2));
    }

    /**
     * Verifies that stock is updated when a charge is removed.
     *
     * @param acts the charge act and item
     */
    private void checkChargeRemoval(List<FinancialAct> acts) {
        BigDecimal quantity = new BigDecimal(10);
        FinancialAct act = acts.get(0);
        FinancialAct item = acts.get(1);
        item.setQuantity(quantity);

        save(acts);

        BigDecimal expected = getQuantity(BigDecimal.ZERO, quantity,
                                          act.isCredit());

        checkEquals(expected, getStock(stockLocation, product));

        // now remove the charge
        remove(act);

        checkEquals(BigDecimal.ZERO, getStock(stockLocation, product));
    }

    private void checkItemRemoval(List<FinancialAct> acts) {
        BigDecimal quantity = new BigDecimal(10);
        FinancialAct item = acts.get(1);
        item.setQuantity(quantity);

        save(acts);

        boolean credit = TypeHelper.isA(
                item, CustomerAccountArchetypes.CREDIT_ITEM);

        BigDecimal expected = getQuantity(BigDecimal.ZERO, quantity, credit);

        checkEquals(expected, getStock(stockLocation, product));
        remove(item);

        checkEquals(BigDecimal.ZERO, getStock(stockLocation, product));
    }

    private List<FinancialAct> createInvoice() {
        List<FinancialAct> acts = FinancialTestHelper.createChargesInvoice(
                new Money(100), customer, patient, product,
                ActStatus.IN_PROGRESS);
        addStockLocation(acts.get(1));
        return acts;
    }

    private List<FinancialAct> createCreditCharge() {
        List<FinancialAct> acts = FinancialTestHelper.createChargesCredit(
                new Money(100), customer, patient, product,
                ActStatus.IN_PROGRESS);
        addStockLocation(acts.get(1));
        return acts;
    }

    private List<FinancialAct> createCounterCharge() {
        List<FinancialAct> acts = FinancialTestHelper.createChargesCounter(
                new Money(100), customer, product, ActStatus.IN_PROGRESS);
        addStockLocation(acts.get(1));
        return acts;
    }

    private void addStockLocation(FinancialAct item) {
        ActBean bean = new ActBean(item);
        bean.addParticipation(StockArchetypes.STOCK_LOCATION_PARTICIPATION,
                              stockLocation);
    }


    private BigDecimal getQuantity(BigDecimal current, BigDecimal change,
                                   boolean credit) {
        return (credit) ? current.add(change) : current.subtract(change);
    }

    private BigDecimal getStock(Party location, Product product) {
        product = get(product);
        EntityBean prodBean = new EntityBean(product);
        EntityRelationship rel = prodBean.getRelationship(location);
        if (rel != null) {
            IMObjectBean relBean = new IMObjectBean(rel);
            return relBean.getBigDecimal("quantity");
        }
        return BigDecimal.ZERO;
    }

}
