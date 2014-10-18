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

package org.openvpms.archetype.rules.supplier.account;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link SupplierAccountRules} class.
 *
 * @author Tim Anderson
 */
public class SupplierAccountRulesTestCase extends ArchetypeServiceTest {

    /**
     * The supplier.
     */
    private Party supplier;

    /**
     * The supplier account rules.
     */
    private SupplierAccountRules rules;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        supplier = TestHelper.createSupplier();
        rules = new SupplierAccountRules(getArchetypeService());
    }

    /**
     * Tests the {@link SupplierAccountRules#getBalance(Party)} method.
     */
    @Test
    public void testGetBalance() {
        BigDecimal hundred = new BigDecimal(100);
        BigDecimal sixty = new BigDecimal(60);
        BigDecimal forty = new BigDecimal(40);

        checkEquals(BigDecimal.ZERO, rules.getBalance(supplier));

        // verify IN_PROGRESS acts don't affect the balance
        List<FinancialAct> invoice = createChargesInvoice(hundred);
        invoice.get(0).setStatus(ActStatus.IN_PROGRESS);
        save(invoice);
        checkEquals(BigDecimal.ZERO, rules.getBalance(supplier));

        // now post it and check the balance
        invoice.get(0).setStatus(ActStatus.POSTED);
        save(invoice);
        checkEquals(hundred, rules.getBalance(supplier));

        // pay 60 of the debt
        List<FinancialAct> payment1 = createPaymentCash(sixty);
        save(payment1);

        // check the balance
        checkEquals(forty, rules.getBalance(supplier));

        // pay the remainder of the debt
        List<FinancialAct> payment2 = createPaymentCredit(forty);
        save(payment2);

        // check the balance
        checkEquals(BigDecimal.ZERO, rules.getBalance(supplier));
    }

    /**
     * Verifies that an <em>act.supplierAccountChargesInvoice</em> is
     * offset by an <em>act.supplierAccountPayment</em> for the same amount.
     */
    @Test
    public void testGetBalanceForChargesInvoiceAndPayment() {
        BigDecimal amount = new BigDecimal(100);
        List<FinancialAct> invoice = createChargesInvoice(amount);
        List<FinancialAct> payment = createPaymentCash(amount);
        checkCalculateBalanceForSameAmount(invoice, payment);
    }

    /**
     * Verifies that an <em>act.supplierAccountChargesInvoice</em> is
     * offset by an <em>act.supplierAccountChargesCredit</em> for the same
     * amount.
     */
    @Test
    public void testGetBalanceForChargesInvoiceAndCredit() {
        BigDecimal amount = new BigDecimal(100);
        List<FinancialAct> invoice = createChargesInvoice(amount);
        List<FinancialAct> credit = createChargesCredit(amount);
        checkCalculateBalanceForSameAmount(invoice, credit);
    }

    /**
     * Verifies that an <em>act.supplierAccountRefund</em> is offset by an
     * <em>act.supplierAccountPayment</em> for the same amount.
     */
    @Test
    public void testGetBalanceForRefundAndPayment() {
        BigDecimal amount = new BigDecimal(100);
        List<FinancialAct> refund = createRefundCash(amount);
        List<FinancialAct> payment = createPaymentCash(amount);
        checkCalculateBalanceForSameAmount(refund, payment);
    }

    /**
     * Tests the reversal of supplier account acts by {@link SupplierAccountRules#reverse}.
     */
    @Test
    public void testReverse() {
        checkReverse(createChargesInvoice(new BigDecimal(100)), SupplierArchetypes.CREDIT,
                     SupplierArchetypes.CREDIT_ITEM);

        checkReverse(createChargesCredit(new BigDecimal(50)), SupplierArchetypes.INVOICE,
                     SupplierArchetypes.INVOICE_ITEM);

        checkReverse(createPaymentCash(new BigDecimal(75)), SupplierArchetypes.REFUND, SupplierArchetypes.REFUND_CASH);

        checkReverse(createPaymentCheque(new BigDecimal(23)), SupplierArchetypes.REFUND,
                     SupplierArchetypes.REFUND_CHEQUE);

        checkReverse(createPaymentCredit(new BigDecimal(24)), SupplierArchetypes.REFUND,
                     SupplierArchetypes.REFUND_CREDIT);

        checkReverse(createPaymentEFT(new BigDecimal(26)), SupplierArchetypes.REFUND,
                     SupplierArchetypes.REFUND_EFT);

        checkReverse(createRefundCash(new BigDecimal(10)), SupplierArchetypes.PAYMENT, SupplierArchetypes.PAYMENT_CASH);

        checkReverse(createRefundCheque(new BigDecimal(11)), SupplierArchetypes.PAYMENT,
                     SupplierArchetypes.PAYMENT_CHEQUE);

        checkReverse(createRefundCredit(new BigDecimal(12)), SupplierArchetypes.PAYMENT,
                     SupplierArchetypes.PAYMENT_CREDIT);

        checkReverse(createRefundEFT(new BigDecimal(15)), SupplierArchetypes.PAYMENT, SupplierArchetypes.PAYMENT_EFT);
    }

    /**
     * Verifies that a debit is offset by a credit of the same amount.
     *
     * @param debits  the debit acts
     * @param credits the credit act
     */
    private void checkCalculateBalanceForSameAmount(List<FinancialAct> debits, List<FinancialAct> credits) {
        FinancialAct debit = debits.get(0);
        FinancialAct credit = credits.get(0);

        BigDecimal amount = credit.getTotal();
        checkEquals(amount, debit.getTotal());

        assertTrue(credit.isCredit());
        assertFalse(debit.isCredit());

        // save and reload the debit. The allocated amount should be unchanged
        save(debits);

        // check the outstanding balance
        checkBalance(amount);

        // save the credit. The allocated amount for the debit and credit should
        // be the same as the total
        save(credits);

        credit = get(credit);
        debit = get(debit);

        checkEquals(amount, credit.getTotal());
        checkEquals(amount, debit.getTotal());

        // check the outstanding balance
        checkBalance(BigDecimal.ZERO);
    }


    private List<FinancialAct> createChargesInvoice(BigDecimal amount) {
        return createChargeActs(SupplierArchetypes.INVOICE, SupplierArchetypes.INVOICE_ITEM, amount,
                                TestHelper.createProduct());
    }

    private List<FinancialAct> createChargesCredit(BigDecimal amount) {
        return createChargeActs(SupplierArchetypes.CREDIT, SupplierArchetypes.CREDIT_ITEM, amount,
                                TestHelper.createProduct());
    }


    private List<FinancialAct> createPaymentCash(BigDecimal amount) {
        return createPaymentRefund(SupplierArchetypes.PAYMENT, SupplierArchetypes.PAYMENT_CASH, amount);
    }

    private List<FinancialAct> createPaymentCheque(BigDecimal amount) {
        return createPaymentRefund(SupplierArchetypes.PAYMENT, SupplierArchetypes.PAYMENT_CHEQUE, amount);
    }

    private List<FinancialAct> createPaymentCredit(BigDecimal amount) {
        return createPaymentRefund(SupplierArchetypes.PAYMENT, SupplierArchetypes.PAYMENT_CREDIT, amount);
    }

    private List<FinancialAct> createPaymentEFT(BigDecimal amount) {
        return createPaymentRefund(SupplierArchetypes.PAYMENT, SupplierArchetypes.PAYMENT_EFT, amount);
    }

    private List<FinancialAct> createRefundCash(BigDecimal amount) {
        return createPaymentRefund(SupplierArchetypes.REFUND, SupplierArchetypes.REFUND_CASH, amount);
    }

    private List<FinancialAct> createRefundCheque(BigDecimal amount) {
        return createPaymentRefund(SupplierArchetypes.REFUND, SupplierArchetypes.REFUND_CHEQUE, amount);
    }

    private List<FinancialAct> createRefundCredit(BigDecimal amount) {
        return createPaymentRefund(SupplierArchetypes.REFUND, SupplierArchetypes.REFUND_CREDIT, amount);
    }

    private List<FinancialAct> createRefundEFT(BigDecimal amount) {
        return createPaymentRefund(SupplierArchetypes.REFUND, SupplierArchetypes.REFUND_EFT, amount);
    }

    private List<FinancialAct> createChargeActs(String shortName, String itemShortName, BigDecimal amount,
                                                Product product) {
        FinancialAct act = (FinancialAct) create(shortName);
        FinancialAct item = (FinancialAct) create(itemShortName);
        ActBean bean = new ActBean(act);
        ActBean itemBean = new ActBean(item);
        bean.setValue("amount", amount);
        bean.setValue("status", ActStatus.POSTED);
        bean.addNodeParticipation("supplier", supplier);
        itemBean.setNodeParticipant("product", product);
        itemBean.setValue("unitPrice", amount);
        itemBean.setValue("quantity", 1);
        bean.addNodeRelationship("items", item);
        return Arrays.asList(act, item);
    }

    private List<FinancialAct> createPaymentRefund(String shortName, String itemShortName, BigDecimal amount) {
        FinancialAct act = (FinancialAct) create(shortName);
        FinancialAct item = (FinancialAct) create(itemShortName);
        ActBean bean = new ActBean(act);
        ActBean itemBean = new ActBean(item);
        bean.setValue("amount", amount);
        bean.setValue("status", ActStatus.POSTED);
        if (itemBean.isA(SupplierArchetypes.PAYMENT_CASH, SupplierArchetypes.REFUND_CASH)) {
            itemBean.setValue("roundedAmount", amount);
            if (itemBean.isA(SupplierArchetypes.PAYMENT_CASH)) {
                itemBean.setValue("tendered", amount);
            }
        } else if (itemBean.isA(SupplierArchetypes.PAYMENT_CHEQUE, SupplierArchetypes.REFUND_CHEQUE)) {
            itemBean.setValue("bank", TestHelper.getLookup("lookup.bank", "CBA"));
        } else if (itemBean.isA(SupplierArchetypes.PAYMENT_CREDIT, SupplierArchetypes.REFUND_CREDIT)) {
            itemBean.setValue("creditCard", TestHelper.getLookup("lookup.creditcard", "VISA"));
        }
        bean.addNodeParticipation("supplier", supplier);
        itemBean.setValue("amount", amount);
        bean.addNodeRelationship("items", item);
        return Arrays.asList(act, item);
    }


    /**
     * Verifies that an act can be reversed by {@link SupplierAccountRules#reverse}, and has the correct child act.
     *
     * @param acts          the acts to reverse
     * @param shortName     the reversal act short name
     * @param itemShortName the reversal act child short name.
     */
    private void checkReverse(List<FinancialAct> acts, String shortName, String itemShortName) {
        FinancialAct act = acts.get(0);
        act.setStatus(ActStatus.POSTED);
        save(acts);
        BigDecimal amount = act.getTotal();

        // check the balance
        BigDecimal balance = act.isCredit() ? amount.negate() : amount;
        checkBalance(balance);

        Date now = new Date();
        FinancialAct reversal = rules.reverse(act, now);
        assertTrue(TypeHelper.isA(reversal, shortName));
        ActBean bean = new ActBean(reversal);
        if (itemShortName != null) {
            List<Act> items = bean.getNodeActs("items");
            assertEquals(1, items.size());
            Act item = items.get(0);
            assertTrue(TypeHelper.isA(item, itemShortName));
            if (TypeHelper.isA(item, SupplierArchetypes.PAYMENT_CASH, SupplierArchetypes.REFUND_CASH)) {
                ActBean itemBean = new ActBean(item);
                BigDecimal roundedAmount = (BigDecimal) itemBean.getValue("roundedAmount");
                checkEquals(amount, roundedAmount);
            }
            checkEquals(amount, ((FinancialAct) item).getTotal());
        } else {
            if (bean.hasNode("items")) {
                List<Act> items = bean.getNodeActs("items");
                assertEquals(0, items.size());
            }
        }

        // check the balance
        checkBalance(BigDecimal.ZERO);
    }

    private void checkBalance(BigDecimal amount) {
        checkEquals(amount, rules.getBalance(supplier));
    }

}
