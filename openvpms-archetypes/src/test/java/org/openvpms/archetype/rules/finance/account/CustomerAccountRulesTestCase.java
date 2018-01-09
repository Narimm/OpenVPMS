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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.finance.account;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Test;
import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.archetype.rules.finance.invoice.ChargeItemEventLinker;
import org.openvpms.archetype.rules.finance.till.TillBalanceRules;
import org.openvpms.archetype.rules.patient.PatientHistoryChanges;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper;
import org.openvpms.archetype.rules.product.ProductTestHelper;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.business.service.ruleengine.RuleEngineException;
import org.openvpms.component.service.archetype.ValidationError;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.openvpms.archetype.rules.act.ActStatus.IN_PROGRESS;
import static org.openvpms.archetype.rules.act.ActStatus.POSTED;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.BALANCE_PARTICIPATION;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.CREDIT;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.CREDIT_ADJUST;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.CREDIT_ITEM;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.DEBIT_ADJUST;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.INVOICE;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.INVOICE_ITEM;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.PAYMENT;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.PAYMENT_CASH;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.PAYMENT_CHEQUE;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.PAYMENT_CREDIT;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.PAYMENT_DISCOUNT;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.PAYMENT_EFT;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.PAYMENT_OTHER;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.REFUND;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.REFUND_CASH;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.REFUND_CHEQUE;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.REFUND_CREDIT;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.REFUND_DISCOUNT;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.REFUND_EFT;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.REFUND_OTHER;
import static org.openvpms.archetype.test.TestHelper.getDate;

/**
 * Tests the {@link CustomerAccountRules} class when triggered by the
 * <em>archetypeService.save.act.customerAccountBadDebt.before</em>,
 * <em>archetypeService.save.act.customerAccountBadDebt.after</em>,
 * <em>archetypeService.save.act.customerAccountChargesCounter.before</em>,
 * <em>archetypeService.save.act.customerAccountChargesCounter.after</em>,
 * <em>archetypeService.save.act.customerAccountChargesCredit.before</em>,
 * <em>archetypeService.save.act.customerAccountChargesCredit.after</em>,
 * <em>archetypeService.save.act.customerAccountChargesInvoice.before</em>,
 * <em>archetypeService.save.act.customerAccountChargesInvoice.after</em>,
 * <em>archetypeService.save.act.customerAccountCreditAdjust.before</em>,
 * <em>archetypeService.save.act.customerAccountCreditAdjust.after</em>,
 * <em>archetypeService.save.act.customerAccountDebitAdjust.before</em>,
 * <em>archetypeService.save.act.customerAccountDebitAdjust.after</em>,
 * <em>archetypeService.save.act.customerAccountInitialBalance.before</em>,
 * <em>archetypeService.save.act.customerAccountInitialBalance.after</em>,
 * <em>archetypeService.save.act.customerAccountPayment.before</em>,
 * <em>archetypeService.save.act.customerAccountPayment.after</em>,
 * <em>archetypeService.save.act.customerAccountRefund.before</em> and.
 * <em>archetypeService.save.act.customerAccountRefund.after</em> rules.
 * In order for these tests to be successful, the archetype service
 * must be configured to trigger the above rules.
 *
 * @author Tim Anderson
 */
public class CustomerAccountRulesTestCase extends AbstractCustomerAccountTest {

    /**
     * Verifies that when a posted <em>act.customerAccountChargesInvoice</em>
     * is saved, an <em>participation.customerAccountBalance</em> is
     * associated with it.
     */
    @Test
    public void testAddChargesInvoiceToBalance() {
        checkAddToBalance(createChargesInvoice(new BigDecimal(100)));
    }

    /**
     * Verifies that when a posted <em>act.customerAccountChargesCounter</em>
     * is saved, an <em>participation.customerAccountBalance</em> is
     * associated with it.
     */
    @Test
    public void testAddChargesCounterToBalance() {
        checkAddToBalance(createChargesCounter(new BigDecimal(100)));
    }

    /**
     * Verifies that when a posted <em>act.customerAccountChargesCredit</em>
     * is saved, an <em>participation.customerAccountBalance</em> is
     * associated with it.
     */
    @Test
    public void testAddChargesCreditToBalance() {
        checkAddToBalance(createChargesCredit(new BigDecimal(100)));
    }

    /**
     * Verifies that when a posted <em>act.customerPayment</em>
     * is saved, an <em>participation.customerAccountBalance</em> is
     * associated with it.
     */
    @Test
    public void testAddPaymentToBalance() {
        checkAddToBalance(createPayment(new BigDecimal(100)));
    }

    /**
     * Verifies that when a posted <em>act.customerRefund</em>
     * is saved, an <em>participation.customerAccountBalance</em> is
     * associated with it.
     */
    @Test
    public void testAddRefundToBalance() {
        checkAddToBalance(createRefund(new BigDecimal(100)));
    }

    /**
     * Verifies that when a posted <em>act.customerAccountCreditAdjust</em>
     * is saved, an <em>participation.customerAccountBalance</em> is
     * associated with it.
     */
    @Test
    public void testAddCreditAdjustToBalance() {
        checkAddToBalance(createCreditAdjust(new BigDecimal(100)));
    }

    /**
     * Verifies that when a posted <em>act.customerAccountDebitAdjust</em>
     * is saved, an <em>participation.customerAccountBalance</em> is
     * associated with it.
     */
    @Test
    public void testAddDebitAdjustToBalance() {
        checkAddToBalance(createDebitAdjust(new BigDecimal(100)));
    }

    /**
     * Verifies that when a posted <em>act.customerAccountInitialBalance</em>
     * is saved, an <em>participation.customerAccountBalance</em> is
     * associated with it.
     */
    @Test
    public void testAddInitialBalanceToBalance() {
        checkAddToBalance(createInitialBalance(new BigDecimal(100)));
    }

    /**
     * Verifies that when a posted <em>act.customerAccountBadDebt</em>
     * is saved, an <em>participation.customerAccountBalance</em> is
     * associated with it.
     */
    @Test
    public void testAddBadDebtToBalance() {
        checkAddToBalance(createBadDebt(new BigDecimal(100)));
    }

    /**
     * Verifies that an <em>act.customerAccountChargesInvoice</em> is
     * offset by an <em>act.customerAccountPayment</em> for the same amount.
     */
    @Test
    public void testGetBalanceForChargesInvoiceAndPayment() {
        BigDecimal amount = new BigDecimal(100);
        List<FinancialAct> invoice = createChargesInvoice(amount);
        List<FinancialAct> payment = Collections.singletonList(createPayment(amount));
        checkCalculateBalanceForSameAmount(invoice, payment);
    }

    /**
     * Verifies that an <em>act.customerAccountChargesCounter</em> is
     * offset by an <em>act.customerAccountPayment</em> for the same amount.
     */
    @Test
    public void testGetBalanceForChargesCounterAndPayment() {
        BigDecimal amount = new BigDecimal(100);
        List<FinancialAct> counter = createChargesCounter(amount);
        List<FinancialAct> payment = Collections.singletonList(createPayment(amount));
        checkCalculateBalanceForSameAmount(counter, payment);
    }

    /**
     * Verifies that an <em>act.customerAccountChargesInvoice</em> is
     * offset by an <em>act.customerAccountChargesCredit</em> for the same
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
     * Verifies that an <em>act.customerAccountRefund</em> is offset by an
     * <em>act.customerAccountPayment</em> for the same amount.
     */
    @Test
    public void testGetBalanceForRefundAndPayment() {
        BigDecimal amount = new BigDecimal(100);
        List<FinancialAct> refund = Collections.singletonList(createRefund(amount));
        List<FinancialAct> payment = Collections.singletonList(createPayment(amount));
        checkCalculateBalanceForSameAmount(refund, payment);
    }

    /**
     * Verifies that an <em>act.customerAccountDebitAdjust</em> is offset by an
     * <em>act.customerAccountCreditAdjust</em> for the same amount.
     */
    @Test
    public void testGetBalanceForDebitAndCreditAdjust() {
        BigDecimal amount = new BigDecimal(100);
        List<FinancialAct> debit = Collections.singletonList(createDebitAdjust(amount));
        List<FinancialAct> credit = Collections.singletonList(createCreditAdjust(amount));
        checkCalculateBalanceForSameAmount(debit, credit);
    }

    /**
     * Verifies that an <em>act.customerAccountInitialBalance</em> is offset by
     * an <em>act.customerAccountBadDebt</em> for the same amount.
     */
    @Test
    public void testGetBalanceForInitialBalanceAndBadDebt() {
        BigDecimal amount = new BigDecimal(100);
        List<FinancialAct> debit = Collections.singletonList(createInitialBalance(amount));
        List<FinancialAct> credit = Collections.singletonList(createBadDebt(amount));
        checkCalculateBalanceForSameAmount(debit, credit);
    }

    /**
     * Tests the {@link CustomerAccountRules#getBalance} method.
     */
    @Test
    public void testGetBalance() {
        CustomerAccountRules rules = getRules();
        Party customer = getCustomer();
        BigDecimal hundred = new BigDecimal(100);
        BigDecimal sixty = new BigDecimal(60);
        BigDecimal forty = new BigDecimal(40);
        List<FinancialAct> invoiceActs = createChargesInvoice(hundred);
        save(invoiceActs);

        // check the balance
        checkEquals(hundred, rules.getBalance(customer));

        // make sure the invoice has an account balance participation
        FinancialAct invoice = invoiceActs.get(0);
        ActBean bean = new ActBean(invoice);
        assertEquals(customer, bean.getParticipant(
                "participation.customerAccountBalance"));

        // reload and verify act has not changed
        invoice = get(invoice);
        checkEquals(hundred, invoice.getTotal());
        checkEquals(ZERO, invoice.getAllocatedAmount());
        checkAllocation(invoice);

        // pay 60 of the debt
        FinancialAct payment1 = createPayment(sixty);
        save(payment1);

        // check the balance
        checkEquals(forty, rules.getBalance(customer));

        // reload and verify the acts have changed
        invoice = get(invoice);
        payment1 = get(payment1);

        checkEquals(hundred, invoice.getTotal());
        checkEquals(sixty, invoice.getAllocatedAmount());
        checkAllocation(invoice, payment1);

        checkEquals(sixty, payment1.getTotal());
        checkEquals(sixty, payment1.getAllocatedAmount());
        checkAllocation(payment1, invoice);

        // pay the remainder of the debt
        FinancialAct payment2 = createPayment(forty);
        save(payment2);

        // check the balance
        checkEquals(ZERO, rules.getBalance(customer));

        // reload and verify the acts have changed
        invoice = get(invoice);
        payment2 = get(payment2);

        checkEquals(hundred, invoice.getTotal());
        checkEquals(hundred, invoice.getAllocatedAmount());
        checkAllocation(invoice, payment1, payment2);

        checkEquals(forty, payment2.getTotal());
        checkEquals(forty, payment2.getAllocatedAmount());
        checkAllocation(payment2, invoice);
    }

    /**
     * Tests the {@link CustomerAccountRules#getBalance(Party, BigDecimal,
     * boolean)} method.
     */
    @Test
    public void testGetRunningBalance() {
        CustomerAccountRules rules = getRules();
        Party customer = getCustomer();
        BigDecimal hundred = new BigDecimal(100);
        BigDecimal sixty = new BigDecimal(60);
        BigDecimal forty = new BigDecimal(40);
        BigDecimal ten = new BigDecimal(10);
        BigDecimal five = new BigDecimal(5);
        List<FinancialAct> invoice = createChargesInvoice(hundred);
        save(invoice);

        // check the balance for a payment
        checkEquals(hundred, rules.getBalance(customer, ZERO, true));

        // check the balance for a refund
        checkEquals(ZERO, rules.getBalance(customer, ZERO, false));

        // simulate payment of 60. Running balance should be 40
        checkEquals(forty, rules.getBalance(customer, sixty, true));

        // overpay by 10
        FinancialAct payment1 = createPayment(new BigDecimal("110.0"));
        save(payment1);

        // check the balance for a payment
        checkEquals(ZERO, rules.getBalance(customer, ZERO, true));

        // check the balance for a refund
        checkEquals(ten, rules.getBalance(customer, ZERO, false));

        // check the balance after refunding 5
        checkEquals(five, rules.getBalance(customer, five, false));
    }

    /**
     * Tests the {@link CustomerAccountRules#getOverdueBalance} method.
     */
    @Test
    public void testGetCurrentOverdueBalance() {
        CustomerAccountRules rules = getRules();

        // add a 30 day payment term for accounts to the customer
        Party customer = getCustomer();
        customer.addClassification(createAccountType(30, DateUnits.DAYS));
        save(customer);

        // create and save a new invoice
        final BigDecimal amount = new BigDecimal(100);
        Date startTime = getDate("2007-01-01");
        List<FinancialAct> invoice = createChargesInvoice(amount, startTime);
        save(invoice);

        // check the invoice is not overdue on the day it is saved
        BigDecimal overdue = rules.getOverdueBalance(customer, startTime);
        checkEquals(ZERO, overdue);

        // 30 days from saved, amount shouldn't be overdue
        Date now = DateRules.getDate(startTime, 30, DateUnits.DAYS);
        overdue = rules.getOverdueBalance(customer, now);
        checkEquals(ZERO, overdue);

        // 31 days from saved, invoice should be overdue.
        now = DateRules.getDate(now, 1, DateUnits.DAYS);
        overdue = rules.getOverdueBalance(customer, now);
        checkEquals(amount, overdue);

        // now save a credit for the same date as the invoice with total
        // > invoice total. The balance should be negative, but the overdue
        // balance should be zero as it only sums debits.
        List<FinancialAct> creditActs = createChargesCredit(new BigDecimal(150));
        FinancialAct credit = creditActs.get(0);
        credit.setActivityStartTime(startTime);
        save(creditActs);
        checkBalance(new BigDecimal(-50));
        overdue = rules.getOverdueBalance(customer, now);
        checkEquals(ZERO, overdue);
    }

    /**
     * Tests the {@link CustomerAccountRules#getOverdueBalance(Party, Date,
     * Date)} method.
     */
    @Test
    public void testGetOverdueBalance() {
        CustomerAccountRules rules = getRules();

        // add a 30 day payment term for accounts to the customer
        Party customer = getCustomer();
        customer.addClassification(createAccountType(30, DateUnits.DAYS));
        save(customer);

        // create and save a new invoice
        final BigDecimal amount = new BigDecimal(100);
        Date startTime = getDate("2007-01-01");
        List<FinancialAct> invoice = createChargesInvoice(amount, startTime);
        save(invoice);

        // check the invoice is not overdue on the day it is saved
        Date overdueDate = rules.getOverdueDate(customer, startTime);
        BigDecimal overdue = rules.getOverdueBalance(customer, startTime, overdueDate);
        checkEquals(ZERO, overdue);

        // 30 days from saved, amount shouldn't be overdue
        Date now = DateRules.getDate(startTime, 30, DateUnits.DAYS);
        overdueDate = rules.getOverdueDate(customer, now);
        overdue = rules.getOverdueBalance(customer, now, overdueDate);
        checkEquals(ZERO, overdue);

        // 31 days from saved, invoice should be overdue.
        Date statementDate = DateRules.getDate(now, 1, DateUnits.DAYS);
        overdueDate = rules.getOverdueDate(customer, statementDate);
        overdue = rules.getOverdueBalance(customer, statementDate, overdueDate);
        checkEquals(amount, overdue);

        // now save a credit dated 32 days from saved.
        // The current balance should = -50, but the overdue balance as
        // of 31 days after saved should still be 100
        now = DateRules.getDate(statementDate, 1, DateUnits.DAYS);
        List<FinancialAct> credits = createChargesCredit(new BigDecimal(150));
        FinancialAct credit = credits.get(0);
        credit.setActivityStartTime(now);
        save(credits);

        checkBalance(new BigDecimal(-50));
        overdue = rules.getOverdueBalance(customer, statementDate, overdueDate);
        checkEquals(amount, overdue);
    }

    /**
     * Tests the {@link CustomerAccountRules#getCreditBalance(Party)} method.
     */
    @Test
    public void testGetCreditAmount() {
        CustomerAccountRules rules = getRules();

        final BigDecimal amount = new BigDecimal(100);
        List<FinancialAct> credits = createChargesCredit(amount);
        FinancialAct creditAdjust = createCreditAdjust(amount);
        FinancialAct payment = createPayment(amount);
        FinancialAct badDebt = createBadDebt(amount);

        FinancialAct chargesCredit = credits.get(0);
        chargesCredit.setStatus(IN_PROGRESS);
        save(credits);
        chargesCredit.setStatus(POSTED);

        FinancialAct[] acts = {chargesCredit, creditAdjust, payment, badDebt};
        for (int i = 0; i < acts.length; ++i) {
            save(acts[i]);
            BigDecimal multiplier = new BigDecimal(i + 1);

            // need to negate as credits are negative
            BigDecimal expected = amount.multiply(multiplier).negate();
            checkEquals(expected, rules.getCreditBalance(getCustomer()));
        }
    }

    /**
     * Tests the {@link CustomerAccountRules#getUnbilledAmount(Party)}  method.
     */
    @Test
    public void testGetUnbilledAmount() {
        CustomerAccountRules rules = getRules();

        final BigDecimal amount = new BigDecimal(100);
        List<FinancialAct> invoices = createChargesInvoice(amount);
        List<FinancialAct> counters = createChargesCounter(amount);
        List<FinancialAct> credits = createChargesCredit(amount);

        FinancialAct invoice = invoices.get(0);
        invoice.setStatus(IN_PROGRESS);

        FinancialAct counter = counters.get(0);
        counter.setStatus(IN_PROGRESS);

        FinancialAct credit = credits.get(0);
        credit.setStatus(IN_PROGRESS);

        checkEquals(ZERO, rules.getUnbilledAmount(getCustomer()));

        save(invoices);
        checkEquals(amount, rules.getUnbilledAmount(getCustomer()));

        save(counters);
        checkEquals(amount.multiply(new BigDecimal(2)),
                    rules.getUnbilledAmount(getCustomer()));

        save(credits);
        checkEquals(amount, rules.getUnbilledAmount(getCustomer()));

        credit.setStatus(POSTED);
        save(credit);
        checkEquals(amount.multiply(new BigDecimal(2)), rules.getUnbilledAmount(getCustomer()));

        counter.setStatus(POSTED);
        save(counter);
        checkEquals(amount, rules.getUnbilledAmount(getCustomer()));

        invoice.setStatus(POSTED);
        save(invoice);
        checkEquals(ZERO, rules.getUnbilledAmount(getCustomer()));
    }

    /**
     * Tests the reversal of customer account acts by {@link CustomerAccountRules#reverse}.
     */
    @Test
    public void testReverse() {
        checkReverse(createInitialBalance(new BigDecimal(25)), CREDIT_ADJUST);

        checkReverseCharge(createChargesInvoice(new BigDecimal(100)), CREDIT, CREDIT_ITEM);

        checkReverseCharge(createChargesCredit(new BigDecimal(50)), INVOICE, INVOICE_ITEM);

        checkReverseCharge(createChargesCounter(new BigDecimal(40)), CREDIT, CREDIT_ITEM);

        checkReverse(createPaymentCash(new BigDecimal(75)), REFUND, REFUND_CASH, false, null, new BigDecimal(75));

        checkReverse(createPaymentCheque(new BigDecimal(23)), REFUND, REFUND_CHEQUE, false, null, null);

        checkReverse(createPaymentCredit(new BigDecimal(24)), REFUND, REFUND_CREDIT, false, null, null);

        checkReverse(createPaymentDiscount(new BigDecimal(25)), REFUND, REFUND_DISCOUNT, false, null, null);

        checkReverse(createPaymentEFT(new BigDecimal(26)), REFUND, REFUND_EFT, false, null, null);

        checkReverse(createPaymentOther(new BigDecimal(26)), REFUND, REFUND_OTHER, false, null, null);

        checkReverse(createRefundCash(TEN), PAYMENT, PAYMENT_CASH, false, null, TEN);

        checkReverse(createRefundCheque(new BigDecimal(11)), PAYMENT, PAYMENT_CHEQUE, false, null, null);

        checkReverse(createRefundCredit(new BigDecimal(12)), PAYMENT, PAYMENT_CREDIT, false, null, null);

        checkReverse(createRefundDiscount(new BigDecimal(13)), PAYMENT, PAYMENT_DISCOUNT, false, null, null);

        checkReverse(createRefundEFT(new BigDecimal(15)), PAYMENT, PAYMENT_EFT, false, null, null);

        checkReverse(createRefundOther(new BigDecimal(15)), PAYMENT, PAYMENT_OTHER, false, null, null);

        checkReverse(createDebitAdjust(new BigDecimal(5)), CREDIT_ADJUST);

        checkReverse(createCreditAdjust(new BigDecimal(15)), DEBIT_ADJUST);

        checkReverse(createBadDebt(new BigDecimal(20)), DEBIT_ADJUST);
    }

    /**
     * Tests reversal of an allocated act.
     */
    @Test
    public void testReverseAllocated() {
        CustomerAccountRules rules = getRules();

        BigDecimal amount = new BigDecimal(100);
        List<FinancialAct> invoice = createChargesInvoice(amount);
        save(invoice);

        FinancialAct payment = createPayment(amount);
        save(payment);

        checkBalance(ZERO);

        rules.reverse(payment, new Date(), "Test reversal", null, false);
        checkBalance(amount);
    }

    /**
     * Tests reversal of an unallocated act.
     */
    @Test
    public void testReverseUnallocated() {
        CustomerAccountRules rules = getRules();

        BigDecimal amount = new BigDecimal(100);
        List<FinancialAct> invoice = createChargesInvoice(amount);
        save(invoice);

        checkBalance(amount);

        rules.reverse(invoice.get(0), new Date(), "Test reversal", null, false);
        checkBalance(ZERO);
    }

    /**
     * Tests reversal of a partially allocated act.
     */
    @Test
    public void testReversePartiallyAllocated() {
        CustomerAccountRules rules = getRules();

        BigDecimal amount = new BigDecimal(100);
        BigDecimal sixty = new BigDecimal(60);
        BigDecimal forty = new BigDecimal(40);

        // create a new invoice for $100
        List<FinancialAct> invoices = createChargesInvoice(amount);
        save(invoices);
        FinancialAct invoice = invoices.get(0);
        checkEquals(ZERO, invoice.getAllocatedAmount());

        // pay $60. Payment is fully allocated, $60 of invoice is allocated.
        FinancialAct payment = createPayment(sixty);
        save(payment);
        checkEquals(sixty, payment.getAllocatedAmount());

        invoice = get(invoice);
        checkEquals(sixty, invoice.getAllocatedAmount());

        // $40 outstanding balance
        checkBalance(forty);

        // reverse the payment.
        FinancialAct reversal = rules.reverse(payment, new Date(), "Test reversal", null, false);
        checkEquals(ZERO, reversal.getAllocatedAmount());

        // invoice and payment retain their allocations
        invoice = get(invoice);
        checkEquals(sixty, invoice.getAllocatedAmount());

        payment = get(payment);
        checkEquals(sixty, payment.getAllocatedAmount());

        checkBalance(amount);
    }

    /**
     * Verifies an act can't be reversed twice.
     */
    @Test
    public void testReverseTwice() {
        CustomerAccountRules rules = getRules();

        BigDecimal amount = new BigDecimal(100);
        List<FinancialAct> invoice = createChargesInvoice(amount);
        save(invoice);

        checkBalance(amount);

        rules.reverse(invoice.get(0), new Date(), "Test reversal", null, false);
        checkBalance(ZERO);

        try {
            rules.reverse(invoice.get(0), new Date(), "Test reversal 2", null, false);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            // do nothing
        }
    }

    /**
     * Checks the behaviour of the {@code hide} parameter, when reversing transactions.
     */
    @Test
    public void testReverseHide() {
        List<FinancialAct> invoice1 = createChargesInvoice(new BigDecimal(100));
        save(invoice1);
        checkReverse(invoice1.get(0), "act.customerAccountChargesCredit", "act.customerAccountCreditItem", true, null, null);

        List<FinancialAct> invoice2 = createChargesInvoice(new BigDecimal(100));
        save(invoice2);

        checkReverse(invoice2.get(0), "act.customerAccountChargesCredit", "act.customerAccountCreditItem", false, null, null);
    }

    /**
     * Tests the {@link CustomerAccountRules#isReversed(FinancialAct)} method and
     * {@link CustomerAccountRules#isReversal(FinancialAct)} methods.
     */
    @Test
    public void testIsReversedIsReversal() {
        List<FinancialAct> invoice = createChargesInvoice(new BigDecimal(100));
        save(invoice);
        CustomerAccountRules rules = getRules();
        FinancialAct act = invoice.get(0);
        FinancialAct reverse = rules.reverse(act, new Date());

        assertTrue(rules.isReversed(act));
        assertFalse(rules.isReversed(reverse));

        assertFalse(rules.isReversal(act));
        assertTrue(rules.isReversal(reverse));

        FinancialAct reverse2 = rules.reverse(reverse, new Date());

        assertTrue(rules.isReversed(reverse));
        assertTrue(rules.isReversal(reverse));

        assertFalse(rules.isReversed(reverse2));
        assertTrue(rules.isReversal(reverse2));
    }

    /**
     * Tests the {@link CustomerAccountRules#setHidden(FinancialAct, boolean)} method.
     */
    @Test
    public void testSetHidden() {
        CustomerAccountRules rules = getRules();

        BigDecimal amount = new BigDecimal(100);
        List<FinancialAct> invoice = createChargesInvoice(amount);
        save(invoice);

        checkBalance(amount);

        FinancialAct act = invoice.get(0);
        assertFalse(rules.isHidden(act));

        FinancialAct reversal = rules.reverse(act, new Date(), "Test reversal", null, false);
        assertFalse(rules.isHidden(reversal));

        checkBalance(ZERO);

        rules.setHidden(act, true);
        checkBalance(ZERO);

        rules.setHidden(reversal, true);
        checkBalance(ZERO);

        assertTrue(rules.isHidden(act));
        assertTrue(rules.isHidden(reversal));

        rules.setHidden(act, false);
        assertFalse(rules.isHidden(act));
        checkBalance(ZERO);

        rules.setHidden(reversal, false);
        assertFalse(rules.isHidden(reversal));
        checkBalance(ZERO);
    }

    /**
     * Verifies that the reversal of a reversal cannot be hidden by
     * {@link CustomerAccountRules#reverse(FinancialAct, Date, String, String, boolean)}.
     * <p>
     * If allowed by default, the customer statement would not add up.
     */
    @Test
    public void testHideIgnoredForReverseOfReverse() {
        CustomerAccountRules rules = getRules();

        BigDecimal amount = new BigDecimal(100);
        List<FinancialAct> invoice = createChargesInvoice(amount);
        save(invoice);

        checkBalance(amount);

        FinancialAct act = invoice.get(0);
        FinancialAct reversal = rules.reverse(act, new Date(), "Test reversal", null, true);
        checkBalance(ZERO);

        assertTrue(rules.isHidden(act));
        assertTrue(rules.isHidden(reversal));

        FinancialAct reversal2 = rules.reverse(reversal, new Date(), "Test reversal 2", null, true);

        act = get(act);
        reversal = get(reversal);

        assertTrue(rules.isHidden(act));
        assertTrue(rules.isHidden(reversal));
        assertFalse(rules.isHidden(reversal2));

        checkBalance(amount);
    }

    /**
     * Verifies that <em>participation.customerAccountBalance</em>
     * participations are not present when an act has a zero total.
     */
    @Test
    public void testZeroAct() {
        // save an IN_PROGRESS invoice with a zero total, and verify it
        // has no balance participation
        List<FinancialAct> invoices = createChargesInvoice(ZERO);
        FinancialAct invoice = invoices.get(0);
        invoice.setStatus(IN_PROGRESS);
        save(invoices);
        ActBean bean = new ActBean(invoice);
        assertNull(bean.getParticipation(BALANCE_PARTICIPATION));

        // change to a non-zero total and save. Should now have a participation
        invoice.setTotal(Money.TEN);
        save(invoice);
        assertNotNull(bean.getParticipation(BALANCE_PARTICIPATION));

        // revert to a zero total and save. The participation should be removed
        invoice.setTotal(Money.ZERO);
        save(invoice);
        assertNull(bean.getParticipation(BALANCE_PARTICIPATION));

        // Post the invoice and verify no participation added
        invoice.setStatus(POSTED);
        save(invoice);
        assertNull(bean.getParticipation(BALANCE_PARTICIPATION));
    }

    /**
     * Verifies that older unallocated balances are allocated prior to more
     * recent ones for OVPMS-795.
     */
    @Test
    public void testAllocationOrder() {
        BigDecimal sixty = new BigDecimal(60);
        BigDecimal forty = new BigDecimal(40);
        BigDecimal twenty = new BigDecimal(20);
        Date chargeTime1 = getDate("2007-01-01");
        Date chargeTime2 = getDate("2007-03-30");

        List<FinancialAct> invoice1 = createChargesInvoice(sixty, chargeTime1);
        save(invoice1);
        List<FinancialAct> invoice2 = createChargesInvoice(forty, chargeTime2);
        save(invoice2);

        Date payTime1 = getDate("2007-04-01");
        FinancialAct payment1 = createPayment(forty, payTime1);
        save(payment1);

        FinancialAct reload1 = get(invoice1.get(0));
        FinancialAct reload2 = get(invoice2.get(0));
        checkEquals(forty, reload1.getAllocatedAmount());
        checkEquals(ZERO, reload2.getAllocatedAmount());

        Date payTime2 = getDate("2007-04-02");
        FinancialAct payment2 = createPayment(twenty, payTime2);
        save(payment2);

        reload1 = get(invoice1.get(0));
        reload2 = get(invoice2.get(0));
        checkEquals(sixty, reload1.getAllocatedAmount());
        checkEquals(ZERO, reload2.getAllocatedAmount());

        Date payTime3 = getDate("2007-04-03");
        FinancialAct payment3 = createPayment(forty, payTime3);
        save(payment3);

        reload1 = get(invoice1.get(0));
        reload2 = get(invoice2.get(0));
        checkEquals(sixty, reload1.getAllocatedAmount());
        checkEquals(forty, reload2.getAllocatedAmount());
    }

    /**
     * Tests the {@link CustomerAccountRules#getInvoice(Party)} method.
     */
    @Test
    public void testGetInvoice() {
        CustomerAccountRules rules = getRules();

        Party customer = getCustomer();
        assertNull(rules.getInvoice(customer));

        // verify posted, on hold invoices not returned
        createInvoice(getDate("2013-05-02"), POSTED);
        createInvoice(getDate("2013-05-02"), FinancialActStatus.ON_HOLD);
        assertNull(rules.getInvoice(customer));

        FinancialAct invoice2 = createInvoice(getDate("2013-05-02"), FinancialActStatus.COMPLETED);
        assertEquals(invoice2, rules.getInvoice(customer));

        // verify back-dated IN_PROGRESS invoice returned in preference to COMPLETED invoice
        FinancialAct invoice3 = createInvoice(getDate("2013-05-01"), IN_PROGRESS);
        assertEquals(invoice3, rules.getInvoice(customer));

        // verify more recent IN_PROGRESS returned
        FinancialAct invoice4 = createInvoice(getDate("2013-05-05"), IN_PROGRESS);
        assertEquals(invoice4, rules.getInvoice(customer));
    }

    /**
     * Tests the {@link CustomerAccountRules#getCredit(Party)} method.
     */
    @Test
    public void testGetCredit() {
        CustomerAccountRules rules = getRules();

        Party customer = getCustomer();
        assertNull(rules.getCredit(customer));

        // verify posted credits not returned
        createCredit(getDate("2013-05-02"), POSTED);
        assertNull(rules.getInvoice(customer));

        FinancialAct credit2 = createCredit(getDate("2013-05-02"), FinancialActStatus.COMPLETED);
        assertEquals(credit2, rules.getCredit(customer));

        // verify back-dated IN_PROGRESS invoice returned in preference to COMPLETED credit
        FinancialAct credit3 = createCredit(getDate("2013-05-01"), IN_PROGRESS);
        assertEquals(credit3, rules.getCredit(customer));

        // verify more recent IN_PROGRESS returned
        FinancialAct credit4 = createCredit(getDate("2013-05-05"), IN_PROGRESS);
        assertEquals(credit4, rules.getCredit(customer));
    }

    /**
     * Verifies that when an invoice is reversed, any invoice items, medication, investigation and document acts are
     * unlinked from the patient history.
     * <p>
     * Also ensures that:
     * <ul>
     * <li>demographic updates aren't triggered in the process of performing the reversal, which requires
     * the invoice items to be saved.</li>
     * <li>stock quantities match that expected</li>
     * </ul>
     */
    @Test
    public void testReverseInvoiceRemovesEntriesFromHistory() {
        CustomerAccountRules rules = getRules();

        PatientRules patientRules = new PatientRules(null, getArchetypeService(), getLookupService());
        Party patient = getPatient();
        User author = TestHelper.createUser();
        Party location = TestHelper.createLocation();

        // add a demographics update to the product that sets the patient as desexed when the invoice is POSTED
        Product product = getProduct();
        ProductTestHelper.addDemographicUpdate(product, "patient.entity", "party:setPatientDesexed(.)");

        IMObjectRelationship stock = initStockQuantity(new BigDecimal("10"));

        // create the invoice
        List<FinancialAct> invoice1 = createChargesInvoice(new BigDecimal(100));
        FinancialAct invoice = invoice1.get(0);
        invoice.setStatus(IN_PROGRESS);
        Act item1 = invoice1.get(1);
        Act medication = PatientTestHelper.createMedication(patient, getProduct());
        Act investigation = PatientTestHelper.createInvestigation(patient, ProductTestHelper.createInvestigationType());
        Act document = PatientTestHelper.createDocumentForm(patient);
        ActBean itemBean = new ActBean(item1);
        itemBean.addNodeRelationship("dispensing", medication);
        itemBean.addNodeRelationship("investigations", investigation);
        itemBean.addNodeRelationship("documents", document);
        assertEquals(3, item1.getSourceActRelationships().size());

        // create another invoice
        List<FinancialAct> invoice2 = createChargesInvoice(new BigDecimal(10), getCustomer(),
                                                           TestHelper.createProduct());
        Act item2 = invoice2.get(1);

        List<IMObject> toSave = new ArrayList<>();
        toSave.addAll(invoice1);
        toSave.add(medication);
        toSave.add(investigation);
        toSave.add(document);
        toSave.addAll(invoice2);
        save(toSave);

        checkStock(stock, new BigDecimal("9"));

        assertFalse(patientRules.isDesexed(get(patient)));

        // now create the event
        Act event = PatientTestHelper.createEvent(patient);
        ActBean eventBean = new ActBean(event);

        // link the charge items, medication, investigation and document to the event
        ChargeItemEventLinker linker = new ChargeItemEventLinker(getArchetypeService());
        linker.link(event, item1, new PatientHistoryChanges(author, location, getArchetypeService()));
        linker.link(event, item2, new PatientHistoryChanges(author, location, getArchetypeService()));

        // verify they are linked
        List<Act> charges = eventBean.getNodeActs("chargeItems");
        assertEquals(2, charges.size());
        assertTrue(charges.contains(item1));
        assertTrue(charges.contains(item2));

        List<Act> items = eventBean.getNodeActs("items");
        assertEquals(3, items.size());
        assertTrue(items.contains(medication));
        assertTrue(items.contains(investigation));
        assertTrue(items.contains(document));

        // now post the invoice. The demographic update should be executed
        invoice.setStatus(POSTED);
        save(invoice);

        // stock should remain the same
        checkStock(stock, new BigDecimal("9"));

        patient = get(patient);
        assertTrue(patientRules.isDesexed(patient));

        // now set the patient as not desexed. When the invoice is reversed, the demographic update shouldn't fire
        // again.
        IMObjectBean bean = new IMObjectBean(patient);
        bean.setValue("desexed", false);
        bean.save();

        // reverse the invoice.
        FinancialAct credit = rules.reverse(invoice, new Date());
        assertTrue(TypeHelper.isA(credit, CREDIT));
        ActBean creditBean = new ActBean(credit);
        List<Act> creditItems = creditBean.getNodeActs("items");
        assertEquals(1, creditItems.size());
        Act creditItem = creditItems.get(0);
        assertEquals(0, creditItem.getSourceActRelationships().size()); // should not have relationships

        event = get(event);  // reload the event
        eventBean = new ActBean(event);

        // ensure the item, medication and investigation still exist
        item1 = get(item1);
        assertNotNull(item1);
        assertNotNull(get(medication));
        assertNotNull(get(investigation));
        assertNotNull(get(document));

        // verify that item1, medication, investigation and document are no longer linked to the event
        charges = eventBean.getNodeActs("chargeItems");
        assertEquals(1, charges.size());
        assertEquals(item2, charges.get(0));

        items = eventBean.getNodeActs("items");
        assertEquals(0, items.size());

        // verify the item, medication, investigation and document are still linked to the invoice item
        assertEquals(3, item1.getSourceActRelationships().size());

        // verify the demographic update didn't get run again
        assertFalse(patientRules.isDesexed(get(patient)));

        // verify stock has reverted to its initial value
        checkStock(stock, new BigDecimal("10"));
    }

    /**
     * Verifies that when an invoice with reminders is reversed, the reminder is retained.
     * TODO - it probably should be deleted or cancelled.
     */
    @Test
    public void testReverseInvoiceWithReminder() {
        CustomerAccountRules rules = getRules();

        Party patient = getPatient();

        // create the invoice
        List<FinancialAct> invoiceActs = createChargesInvoice(new BigDecimal(100));
        FinancialAct invoice = invoiceActs.get(0);
        invoice.setStatus(IN_PROGRESS);
        Act item = invoiceActs.get(1);
        Act reminder = ReminderTestHelper.createReminder(patient, ReminderTestHelper.createReminderType());
        ActBean itemBean = new ActBean(item);
        itemBean.addNodeRelationship("reminders", reminder);
        assertEquals(1, item.getSourceActRelationships().size());
        List<IMObject> toSave = new ArrayList<>();
        toSave.addAll(invoiceActs);
        toSave.add(reminder);
        save(toSave);

        FinancialAct credit = rules.reverse(invoice, new Date());
        assertTrue(TypeHelper.isA(credit, CREDIT));
        ActBean creditBean = new ActBean(credit);
        List<Act> creditItems = creditBean.getNodeActs("items");
        assertEquals(1, creditItems.size());
        Act creditItem = creditItems.get(0);
        assertEquals(0, creditItem.getSourceActRelationships().size()); // should not have relationships

        item = get(item);
        itemBean = new ActBean(item);
        reminder = get(reminder);
        assertNotNull(reminder);
        itemBean.getNodeActs("reminders").contains(reminder);
    }

    /**
     * Verifies that a reversal of a payment or refund can be added to the specified till balance.
     */
    @Test
    public void testReverseToSpecifiedTillBalance() {
        TillBalanceRules rules = new TillBalanceRules(getArchetypeService());
        Party till = getTill();
        assertNull(rules.getUnclearedBalance(till));
        BigDecimal amount = new BigDecimal(75);
        FinancialAct payment1 = createPaymentCash(amount);
        payment1.setStatus(POSTED);
        save(payment1);
        FinancialAct balance1 = rules.getUnclearedBalance(till);
        assertNotNull(balance1);
        checkEquals(amount, balance1.getTotal());
        balance1.setStatus(IN_PROGRESS);  // i.e. clear in progress
        save(balance1);

        // create a new payment. This should go into a different till balance
        FinancialAct payment2 = createPaymentCash(TEN);
        payment2.setStatus(POSTED);
        save(payment2);
        FinancialAct balance2 = rules.getUnclearedBalance(till);
        assertNotNull(balance2);
        assertNotEquals(balance1.getId(), balance2.getId());
        checkEquals(TEN, balance2.getTotal());

        // now reverse payment1 into balance1. The new total should be zero.
        FinancialAct refund = checkReverse(payment1, REFUND, REFUND_CASH, false, balance1, amount);

        balance1 = get(balance1);
        ActBean bean = new ActBean(balance1);
        assertTrue(bean.hasNodeTarget("items", payment1));
        assertTrue(bean.hasNodeTarget("items", refund));
        checkEquals(ZERO, balance1.getTotal());
    }

    /**
     * Tests the {@link CustomerAccountRules#hasAccountActs(Party)} method.
     */
    @Test
    public void testHasAccountActs() {
        Party customer = getCustomer();
        CustomerAccountRules rules = getRules();
        assertFalse(rules.hasAccountActs(customer));
        save(createChargesInvoice(BigDecimal.TEN));
        assertTrue(rules.hasAccountActs(customer));
    }

    /**
     * Verifies a cash payments can be reversed where the {@code rounded amount <> amount}.
     */
    @Test
    public void testReverseCashWithRoundedAmountDifferentToAmount() {
        BigDecimal amount = new BigDecimal("51.54");
        BigDecimal rounded = new BigDecimal("51.55");
        List<FinancialAct> acts = FinancialTestHelper.createPaymentCash(amount, getCustomer(), getTill(), POSTED);
        ActBean item = new ActBean(acts.get(1));
        item.setValue("roundedAmount", rounded);
        item.setValue("tendered", rounded);
        save(acts);
        FinancialAct payment1 = acts.get(0);
        FinancialAct refund = checkReverse(payment1, REFUND, REFUND_CASH, false, null, rounded);
        checkReverse(refund, PAYMENT, PAYMENT_CASH, false, null, rounded);
    }


    /**
     * Verifies that when a refund of a cash payments is reversed, the new tendered amount reflects the rounded amount
     * of the refund, not the original tendered amount.
     * This is because the tendered amount is not preserved in the refund.
     */
    @Test
    public void testReverseCashWithTenderedDifferentToAmount() {
        BigDecimal amount = new BigDecimal("51.54");
        BigDecimal rounded = new BigDecimal("51.55");
        BigDecimal tendered = new BigDecimal("60.00");
        List<FinancialAct> acts = FinancialTestHelper.createPaymentCash(amount, getCustomer(), getTill(), POSTED);
        ActBean item = new ActBean(acts.get(1));
        item.setValue("roundedAmount", rounded);
        item.setValue("tendered", tendered);
        save(acts);
        checkEquals(new BigDecimal("8.45"), item.getBigDecimal("change"));
        FinancialAct payment1 = acts.get(0);
        FinancialAct refund = checkReverse(payment1, REFUND, REFUND_CASH, false, null, rounded);
        FinancialAct payment2 = checkReverse(refund, PAYMENT, PAYMENT_CASH, false, null, rounded);
        ActBean bean = new ActBean(payment2);
        ActBean cash = new ActBean(bean.getNodeActs("items").get(0));

        // verify the tendered amount in the new payment is not that of the original, as the refund doesn't store
        // the information.
        checkEquals(rounded, cash.getBigDecimal("tendered"));
        checkEquals(ZERO, cash.getBigDecimal("change"));
    }

    /**
     * Verifies that customer account acts cannot be created with negative amounts.
     */
    @Test
    public void testNegativeAmountsUnsupported() {
        BigDecimal minusOne = new BigDecimal(-1);
        checkNegativeAmounts(createChargesInvoice(minusOne));
        checkNegativeAmounts(createChargesCounter(minusOne));
        checkNegativeAmounts(createChargesCredit(minusOne));
        checkNegativeAmounts(createPayment(minusOne));
        checkNegativeAmounts(createRefund(minusOne));
        checkNegativeAmounts(createCreditAdjust(minusOne));
        checkNegativeAmounts(createDebitAdjust(minusOne));
        checkNegativeAmounts(createInitialBalance(minusOne));
        checkNegativeAmounts(createBadDebt(minusOne));
    }

    /**
     * Verifies that a customer account act cannot be created with a negative amount.
     *
     * @param act the act to check
     */
    private void checkNegativeAmounts(FinancialAct act) {
        checkNegativeAmounts(Collections.singletonList(act));
    }

    /**
     * Verifies that a customer account act cannot be created with a negative amount.
     *
     * @param acts the acts to check
     */
    private void checkNegativeAmounts(List<FinancialAct> acts) {
        try {
            save(acts);
            fail("Expected save of negative " + acts.get(0).getArchetypeId().getShortName() + " to fail");
        } catch (ValidationException expected) {
            List<ValidationError> errors = expected.getErrors();
            assertEquals(1, errors.size());
            String message = errors.get(0).getMessage();
            assertTrue("Value must be >= 0.0".equals(message) || "Value must be > 0.0".equals(message));
        }
    }

    /**
     * Verifies that when an act is saved, a <em>participation.customerAccountBalance</em> is associated with it.
     * <p>
     * This ensures that the:
     * <ul>
     * <li>customer has no prior account acts before saving</li>
     * <li>has account acts after saving</li>
     * <li>{@link CustomerBalanceUpdater#checkInitialBalance(FinancialAct)} is invoked to reject initial
     * balances being saved when other account acts are present</li>
     * </ul>
     *
     * @param acts the acts. The first act is the parent charge/credit
     */
    private void checkAddToBalance(List<FinancialAct> acts) {
        CustomerAccountRules rules = getRules();
        Party customer = getCustomer();
        ActBean bean = new ActBean(acts.get(0));
        assertNull(bean.getParticipant("participation.customerAccountBalance"));
        save(acts);
        Act act = get(acts.get(0));
        bean = new ActBean(act);
        assertEquals(getCustomer(), bean.getParticipant("participation.customerAccountBalance"));
        assertTrue(rules.hasAccountActs(customer));

        try {
            save(createInitialBalance(BigDecimal.TEN));
            fail("Expected save of Initial Balance to fail");
        } catch (RuleEngineException expected) {
            Throwable rootCause = ExceptionUtils.getRootCause(expected);
            assertTrue(rootCause instanceof CustomerAccountRuleException);
            CustomerAccountRuleException cause = (CustomerAccountRuleException) rootCause;
            assertEquals(CustomerAccountRuleException.ErrorCode.CannotCreateInitialBalance, cause.getErrorCode());
        }
    }

    /**
     * Verifies that when an act is saved, a <em>participation.customerAccountBalance</em> is associated with it.
     *
     * @param act the act
     */
    private void checkAddToBalance(FinancialAct act) {
        checkAddToBalance(Collections.singletonList(act));
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
        debit = get(debit);
        checkEquals(amount, debit.getTotal());
        checkEquals(ZERO, debit.getAllocatedAmount());
        checkAllocation(debit);

        // check the outstanding balance
        checkBalance(amount);

        // save the credit. The allocated amount for the debit and credit should
        // be the same as the total
        save(credits);

        credit = get(credit);
        debit = get(debit);

        checkEquals(amount, credit.getTotal());
        checkEquals(amount, credit.getAllocatedAmount());
        checkAllocation(credit, debit);

        checkEquals(amount, debit.getTotal());
        checkEquals(amount, debit.getAllocatedAmount());
        checkAllocation(debit, credit);

        // check the outstanding balance
        checkBalance(ZERO);
    }

    /**
     * Verifies that the customer account balance matches that expected.
     *
     * @param amount the expected amount
     */
    private void checkBalance(BigDecimal amount) {
        CustomerAccountRules rules = getRules();

        Party customer = getCustomer();
        checkEquals(amount, rules.getBalance(customer));
        checkEquals(amount, rules.getDefinitiveBalance(customer));
    }

    /**
     * Verifies the total amount allocated to an act matches that of the
     * amounts from the associated
     * <em>actRelationship.customerAccountAllocation</em> relationships.
     *
     * @param act  the act
     * @param acts the acts contributing to the allocated amount
     */
    private void checkAllocation(FinancialAct act, FinancialAct... acts) {
        IMObjectBean bean = new IMObjectBean(act);
        BigDecimal total = ZERO;
        List<ActRelationship> allocations = bean.getValues(
                "allocation", ActRelationship.class);
        List<FinancialAct> matches = new ArrayList<>();
        for (ActRelationship relationship : allocations) {
            if (act.isCredit()) {
                assertEquals(act.getObjectReference(),
                             relationship.getTarget());
                for (FinancialAct source : acts) {
                    if (source.getObjectReference().equals(
                            relationship.getSource())) {
                        matches.add(source);
                        break;
                    }
                }
            } else {
                assertEquals(act.getObjectReference(),
                             relationship.getSource());
                for (FinancialAct target : acts) {
                    if (target.getObjectReference().equals(
                            relationship.getTarget())) {
                        matches.add(target);
                        break;
                    }
                }
            }
            IMObjectBean relBean = new IMObjectBean(relationship);
            BigDecimal allocation = relBean.getBigDecimal("allocatedAmount");
            total = total.add(allocation);
        }
        checkEquals(act.getAllocatedAmount(), total);

        assertEquals(acts.length, matches.size());
    }

    /**
     * Verifies that an act can be reversed by
     * {@link CustomerAccountRules#reverse}.
     *
     * @param act       the act to reverse
     * @param shortName the reversal act short name
     */
    private void checkReverse(FinancialAct act, String shortName) {
        checkReverse(act, shortName, null, false, null, null);
    }

    /**
     * Verifies that a charge can be reversed by {@link CustomerAccountRules#reverse}.
     * <p>
     * This ensures that stock is updated appropriately.
     *
     * @param acts          the acts to reverse
     * @param shortName     the reversal act short name
     * @param itemShortName the reversal act child short name
     */
    private void checkReverseCharge(List<FinancialAct> acts, String shortName, String itemShortName) {
        BigDecimal quantity = new BigDecimal("10");
        IMObjectRelationship relationship = initStockQuantity(quantity);

        // save the charge
        save(acts);

        // verify the stock quantity has updated
        if (!acts.get(0).isCredit()) {
            checkStock(relationship, quantity.subtract(BigDecimal.ONE));
        } else {
            checkStock(relationship, quantity.add(BigDecimal.ONE));
        }

        // reverse the charge
        checkReverse(acts.get(0), shortName, itemShortName, false, null, null);

        // ensure the stock has gone back to its initial value
        checkStock(relationship, quantity);
    }

    /**
     * Initialises the quantity for the product and stock location.
     *
     * @param quantity the quantity
     * @return the <em>entityLink.productStockLocation</em> relationship
     */
    private IMObjectRelationship initStockQuantity(BigDecimal quantity) {
        Product product = get(getProduct());
        Party stockLocation = get(getStockLocation());
        return ProductTestHelper.setStockQuantity(product, stockLocation, quantity);
    }

    /**
     * Verifies the quantity of stock matches that expected.
     *
     * @param relationship the <em>entityLink.productStockLocation</em>
     * @param expected     the expected quantity
     */
    private void checkStock(IMObjectRelationship relationship, BigDecimal expected) {
        relationship = get(relationship);
        assertNotNull(relationship);
        IMObjectBean bean = new IMObjectBean(relationship);
        checkEquals(expected, bean.getBigDecimal("quantity"));
    }

    /**
     * Verifies that an act can be reversed by {@link CustomerAccountRules#reverse}, and has the correct child act,
     * if any, and that the {@code hide} flag of the original and reversed transactions match that expected.
     *
     * @param act           the act to reverse
     * @param shortName     the reversal act short name
     * @param itemShortName the reversal act child short name.
     * @param hide          if {@code true}, set the hide flag on both the original and reversed transactions
     * @param tillBalance   the till balance to add the reversal to. Only applies to payments and refunds.
     *                      May be {@code null}
     * @param roundedAmount the expected rounded amount, for cash payments/refunds, otherwise {@code null}
     * @return the reversal
     */
    private FinancialAct checkReverse(FinancialAct act, String shortName, String itemShortName, boolean hide,
                                      FinancialAct tillBalance, BigDecimal roundedAmount) {
        CustomerAccountRules rules = getRules();
        BigDecimal currentBalance = rules.getBalance(getCustomer());

        BigDecimal amount = act.getTotal();
        BigDecimal preBalance;
        BigDecimal reverseBalance;
        BigDecimal change = act.isCredit() ? amount.negate() : amount;

        if (!act.isNew()) {
            // already factored into the balance
            preBalance = currentBalance;
            reverseBalance = currentBalance.subtract(change);
        } else {
            preBalance = currentBalance.add(change);
            reverseBalance = currentBalance;
            act.setStatus(POSTED);
            save(act);
        }

        // check the balance
        checkBalance(preBalance);

        Date now = new Date();
        FinancialAct reversal = rules.reverse(act, now, "Test reversal", null, hide, tillBalance);
        assertTrue(TypeHelper.isA(reversal, shortName));
        ActBean bean = new ActBean(reversal);
        if (itemShortName != null) {
            List<Act> items = bean.getNodeActs("items");
            assertEquals(1, items.size());
            Act item = items.get(0);
            assertTrue(TypeHelper.isA(item, itemShortName));
            if (TypeHelper.isA(item, PAYMENT_CASH, REFUND_CASH)) {
                ActBean itemBean = new ActBean(item);
                checkEquals(roundedAmount, itemBean.getBigDecimal("roundedAmount"));
            }
            checkEquals(amount, ((FinancialAct) item).getTotal());
        } else {
            if (bean.hasNode("items")) {
                List<Act> items = bean.getNodeActs("items");
                assertEquals(0, items.size());
            }
        }

        assertTrue(rules.isReversed(act));
        assertFalse(rules.isReversed(reversal));

        // verify the two acts have a relationship
        ActBean original = new ActBean(act);
        assertTrue(original.hasNodeTarget("reversal", reversal));

        // check the notes and reference
        assertEquals("Test reversal", bean.getString("notes"));
        assertEquals(Long.toString(act.getId()), bean.getString("reference"));

        // check the hide flags
        assertEquals(hide, original.getBoolean("hide"));
        assertEquals(hide, bean.getBoolean("hide"));

        // check the balance
        checkBalance(reverseBalance);
        return reversal;
    }

    /**
     * Helper to create an invoice with the specified start time and status.
     *
     * @param startTime the start time
     * @param status    the status
     * @return the invoice
     */
    private FinancialAct createInvoice(Date startTime, String status) {
        List<FinancialAct> invoices = createChargesInvoice(ZERO);
        FinancialAct invoice = invoices.get(0);
        invoice.setActivityStartTime(startTime);
        invoice.setStatus(status);
        save(invoices);
        return invoice;
    }

    /**
     * Helper to create an invoice with the specified start time and status.
     *
     * @param startTime the start time
     * @param status    the status
     * @return the invoice
     */
    private FinancialAct createCredit(Date startTime, String status) {
        List<FinancialAct> acts = createChargesCredit(ZERO);
        FinancialAct invoice = acts.get(0);
        invoice.setActivityStartTime(startTime);
        invoice.setStatus(status);
        save(acts);
        return invoice;
    }

}
