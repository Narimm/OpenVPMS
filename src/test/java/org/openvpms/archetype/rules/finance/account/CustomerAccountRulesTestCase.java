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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.finance.account;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.BALANCE_PARTICIPATION;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerAccountRulesTestCase extends AbstractCustomerAccountTest {

    /**
     * The rules.
     */
    private CustomerAccountRules rules;


    /**
     * Verifies that when a posted <em>act.customerAccountChargesInvoice</em>
     * is saved, an <em>participation.customerAccountBalance</em> is
     * associated with it.
     */
    @Test
    public void testAddChargesInvoiceToBalance() {
        checkAddToBalance(createChargesInvoice(new Money(100)));
    }

    /**
     * Verifies that when a posted <em>act.customerAccountChargesCounter</em>
     * is saved, an <em>participation.customerAccountBalance</em> is
     * associated with it.
     */
    @Test
    public void testAddChargesCounterToBalance() {
        checkAddToBalance(createChargesCounter(new Money(100)));
    }

    /**
     * Verifies that when a posted <em>act.customerAccountChargesCredit</em>
     * is saved, an <em>participation.customerAccountBalance</em> is
     * associated with it.
     */
    @Test
    public void testAddChargesCreditToBalance() {
        checkAddToBalance(createChargesCredit(new Money(100)));
    }

    /**
     * Verifies that when a posted <em>act.customerPayment</em>
     * is saved, an <em>participation.customerAccountBalance</em> is
     * associated with it.
     */
    @Test
    public void testAddPaymentToBalance() {
        checkAddToBalance(createPayment(new Money(100)));
    }

    /**
     * Verifies that when a posted <em>act.customerRefund</em>
     * is saved, an <em>participation.customerAccountBalance</em> is
     * associated with it.
     */
    @Test
    public void testAddRefundToBalance() {
        checkAddToBalance(createRefund(new Money(100)));
    }

    /**
     * Verifies that when a posted <em>act.customerAccountCreditAdjust</em>
     * is saved, an <em>participation.customerAccountBalance</em> is
     * associated with it.
     */
    @Test
    public void testAddCreditAdjustToBalance() {
        checkAddToBalance(createCreditAdjust(new Money(100)));
    }

    /**
     * Verifies that when a posted <em>act.customerAccountDebitAdjust</em>
     * is saved, an <em>participation.customerAccountBalance</em> is
     * associated with it.
     */
    @Test
    public void testAddDebitAdjustToBalance() {
        checkAddToBalance(createDebitAdjust(new Money(100)));
    }

    /**
     * Verifies that when a posted <em>act.customerAccountInitialBalance</em>
     * is saved, an <em>participation.customerAccountBalance</em> is
     * associated with it.
     */
    @Test
    public void testAddInitialBalanceToBalance() {
        checkAddToBalance(createInitialBalance(new Money(100)));
    }

    /**
     * Verifies that when a posted <em>act.customerAccountBadDebt</em>
     * is saved, an <em>participation.customerAccountBalance</em> is
     * associated with it.
     */
    @Test
    public void testAddBadDebtToBalance() {
        checkAddToBalance(createBadDebt(new Money(100)));
    }

    /**
     * Verifies that an <em>act.customerAccountChargesInvoice</em> is
     * offset by an <em>act.customerAccountPayment</em> for the same amount.
     */
    @Test
    public void testGetBalanceForChargesInvoiceAndPayment() {
        Money amount = new Money(100);
        List<FinancialAct> invoice = createChargesInvoice(amount);
        List<FinancialAct> payment = Arrays.asList(createPayment(amount));
        checkCalculateBalanceForSameAmount(invoice, payment);
    }

    /**
     * Verifies that an <em>act.customerAccountChargesCounter</em> is
     * offset by an <em>act.customerAccountPayment</em> for the same amount.
     */
    @Test
    public void testGetBalanceForChargesCounterAndPayment() {
        Money amount = new Money(100);
        List<FinancialAct> counter = createChargesCounter(amount);
        List<FinancialAct> payment = Arrays.asList(createPayment(amount));
        checkCalculateBalanceForSameAmount(counter, payment);
    }

    /**
     * Verifies that an <em>act.customerAccountChargesInvoice</em> is
     * offset by an <em>act.customerAccountChargesCredit</em> for the same
     * amount.
     */
    @Test
    public void testGetBalanceForChargesInvoiceAndCredit() {
        Money amount = new Money(100);
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
        Money amount = new Money(100);
        List<FinancialAct> refund = Arrays.asList(createRefund(amount));
        List<FinancialAct> payment = Arrays.asList(createCreditAdjust(amount));
        checkCalculateBalanceForSameAmount(refund, payment);
    }

    /**
     * Verifies that an <em>act.customerAccountDebitAdjust</em> is offset by an
     * <em>act.customerAccountCreditAdjust</em> for the same amount.
     */
    @Test
    public void testGetBalanceForDebitAndCreditAdjust() {
        Money amount = new Money(100);
        List<FinancialAct> debit = Arrays.asList(createDebitAdjust(amount));
        List<FinancialAct> credit = Arrays.asList(createCreditAdjust(amount));
        checkCalculateBalanceForSameAmount(debit, credit);
    }

    /**
     * Verifies that an <em>act.customerAccountInitialBalance</em> is offset by
     * an <em>act.customerAccountBadDebt</em> for the same amount.
     */
    @Test
    public void testGetBalanceForInitialBalanceAndBadDebt() {
        Money amount = new Money(100);
        List<FinancialAct> debit = Arrays.asList(createInitialBalance(amount));
        List<FinancialAct> credit = Arrays.asList(createBadDebt(amount));
        checkCalculateBalanceForSameAmount(debit, credit);
    }

    /**
     * Tests the {@link CustomerAccountRules#getBalance} method.
     */
    @Test
    public void testGetBalance() {
        Party customer = getCustomer();
        Money hundred = new Money(100);
        Money sixty = new Money(60);
        Money forty = new Money(40);
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
        checkEquals(BigDecimal.ZERO, invoice.getAllocatedAmount());
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
        checkEquals(BigDecimal.ZERO, rules.getBalance(customer));

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
        Party customer = getCustomer();
        Money hundred = new Money(100);
        Money sixty = new Money(60);
        Money forty = new Money(40);
        Money ten = new Money(10);
        Money five = new Money(5);
        List<FinancialAct> invoice = createChargesInvoice(hundred);
        save(invoice);

        // check the balance for a payment
        checkEquals(hundred,
                     rules.getBalance(customer, BigDecimal.ZERO, true));

        // check the balance for a refund
        checkEquals(BigDecimal.ZERO,
                     rules.getBalance(customer, BigDecimal.ZERO,
                                      false));

        // simulate payment of 60. Running balance should be 40
        checkEquals(forty, rules.getBalance(customer, sixty, true));

        // overpay by 10
        FinancialAct payment1 = createPayment(new Money("110.0"));
        save(payment1);

        // check the balance for a payment
        checkEquals(BigDecimal.ZERO,
                     rules.getBalance(customer, BigDecimal.ZERO,
                                      true));

        // check the balance for a refund
        checkEquals(ten, rules.getBalance(customer, BigDecimal.ZERO, false));

        // check the balance after refunding 5
        checkEquals(five, rules.getBalance(customer, five, false));
    }

    /**
     * Tests the {@link CustomerAccountRules#getOverdueBalance} method.
     */
    @Test
    public void testGetCurrentOverdueBalance() {
        // add a 30 day payment term for accounts to the customer
        Party customer = getCustomer();
        customer.addClassification(createAccountType(30, DateUnits.DAYS));
        save(customer);

        // create and save a new invoice
        final Money amount = new Money(100);
        Date startTime = java.sql.Date.valueOf("2007-1-1");
        List<FinancialAct> invoice = createChargesInvoice(amount, startTime);
        save(invoice);

        // check the invoice is not overdue on the day it is saved
        BigDecimal overdue = rules.getOverdueBalance(customer, startTime);
        checkEquals(BigDecimal.ZERO, overdue);

        // 30 days from saved, amount shouldn't be overdue
        Date now = DateRules.getDate(startTime, 30, DateUnits.DAYS);
        overdue = rules.getOverdueBalance(customer, now);
        checkEquals(BigDecimal.ZERO, overdue);

        // 31 days from saved, invoice should be overdue.
        now = DateRules.getDate(now, 1, DateUnits.DAYS);
        overdue = rules.getOverdueBalance(customer, now);
        checkEquals(amount, overdue);

        // now save a credit for the same date as the invoice with total
        // > invoice total. The balance should be negative, but the overdue
        // balance should be zero as it only sums debits.
        List<FinancialAct> creditActs = createChargesCredit(new Money(150));
        FinancialAct credit = creditActs.get(0);
        credit.setActivityStartTime(startTime);
        save(creditActs);
        checkBalance(new Money(-50));
        overdue = rules.getOverdueBalance(customer, now);
        checkEquals(BigDecimal.ZERO, overdue);
    }

    /**
     * Tests the {@link CustomerAccountRules#getOverdueBalance(Party, Date,
     * Date)} method.
     */
    @Test
    public void testGetOverdueBalance() {
        // add a 30 day payment term for accounts to the customer
        Party customer = getCustomer();
        customer.addClassification(createAccountType(30, DateUnits.DAYS));
        save(customer);

        // create and save a new invoice
        final Money amount = new Money(100);
        Date startTime = java.sql.Date.valueOf("2007-1-1");
        List<FinancialAct> invoice = createChargesInvoice(amount, startTime);
        save(invoice);

        // check the invoice is not overdue on the day it is saved
        Date overdueDate = rules.getOverdueDate(customer, startTime);
        BigDecimal overdue = rules.getOverdueBalance(customer, startTime,
                                                     overdueDate);
        checkEquals(BigDecimal.ZERO, overdue);

        // 30 days from saved, amount shouldn't be overdue
        Date now = DateRules.getDate(startTime, 30, DateUnits.DAYS);
        overdueDate = rules.getOverdueDate(customer, now);
        overdue = rules.getOverdueBalance(customer, now, overdueDate);
        checkEquals(BigDecimal.ZERO, overdue);

        // 31 days from saved, invoice should be overdue.
        Date statementDate = DateRules.getDate(now, 1, DateUnits.DAYS);
        overdueDate = rules.getOverdueDate(customer, statementDate);
        overdue = rules.getOverdueBalance(customer, statementDate, overdueDate);
        checkEquals(amount, overdue);

        // now save a credit dated 32 days from saved.
        // The current balance should = -50, but the overdue balance as
        // of 31 days after saved should still be 100
        now = DateRules.getDate(statementDate, 1, DateUnits.DAYS);
        List<FinancialAct> credits = createChargesCredit(new Money(150));
        FinancialAct credit = credits.get(0);
        credit.setActivityStartTime(now);
        save(credits);

        checkBalance(new Money(-50));
        overdue = rules.getOverdueBalance(customer, statementDate, overdueDate);
        checkEquals(amount, overdue);
    }

    /**
     * Tests the {@link CustomerAccountRules#getCreditBalance(Party)} method.
     */
    @Test
    public void testGetCreditAmount() {
        final Money amount = new Money(100);
        List<FinancialAct> credits = createChargesCredit(amount);
        FinancialAct creditAdjust = createCreditAdjust(amount);
        FinancialAct payment = createPayment(amount);
        FinancialAct badDebt = createBadDebt(amount);

        FinancialAct chargesCredit = credits.get(0);
        chargesCredit.setStatus(ActStatus.IN_PROGRESS);
        save(credits);
        chargesCredit.setStatus(ActStatus.POSTED);

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
        final Money amount = new Money(100);
        List<FinancialAct> invoices = createChargesInvoice(amount);
        List<FinancialAct> counters = createChargesCounter(amount);
        List<FinancialAct> credits = createChargesCredit(amount);

        FinancialAct invoice = invoices.get(0);
        invoice.setStatus(ActStatus.IN_PROGRESS);

        FinancialAct counter = counters.get(0);
        counter.setStatus(ActStatus.IN_PROGRESS);

        FinancialAct credit = credits.get(0);
        credit.setStatus(ActStatus.IN_PROGRESS);

        checkEquals(BigDecimal.ZERO, rules.getUnbilledAmount(getCustomer()));

        save(invoices);
        checkEquals(amount, rules.getUnbilledAmount(getCustomer()));

        save(counters);
        checkEquals(amount.multiply(new BigDecimal(2)),
                     rules.getUnbilledAmount(getCustomer()));

        save(credits);
        checkEquals(amount, rules.getUnbilledAmount(getCustomer()));

        credit.setStatus(ActStatus.POSTED);
        save(credit);
        checkEquals(amount.multiply(new BigDecimal(2)),
                     rules.getUnbilledAmount(getCustomer()));

        counter.setStatus(ActStatus.POSTED);
        save(counter);
        checkEquals(amount, rules.getUnbilledAmount(getCustomer()));

        invoice.setStatus(ActStatus.POSTED);
        save(invoice);
        checkEquals(BigDecimal.ZERO, rules.getUnbilledAmount(getCustomer()));
    }

    /**
     * Tests the reversal of customer account acts.
     */
    @Test
    public void testReverse() {
        checkReverse(createChargesInvoice(new Money(100)),
                     "act.customerAccountChargesCredit",
                     "act.customerAccountCreditItem");

        checkReverse(createChargesCredit(new Money(50)),
                     "act.customerAccountChargesInvoice",
                     "act.customerAccountInvoiceItem");

        checkReverse(createChargesCounter(new Money(40)),
                     "act.customerAccountChargesCredit",
                     "act.customerAccountCreditItem");

        checkReverse(createPaymentCash(new Money(75)),
                     "act.customerAccountRefund",
                     "act.customerAccountRefundCash");

        checkReverse(createPaymentCheque(new Money(23)),
                     "act.customerAccountRefund",
                     "act.customerAccountRefundCheque");

        checkReverse(createPaymentCredit(new Money(24)),
                     "act.customerAccountRefund",
                     "act.customerAccountRefundCredit");

        checkReverse(createPaymentDiscount(new Money(25)),
                     "act.customerAccountRefund",
                     "act.customerAccountRefundDiscount");

        checkReverse(createPaymentEFT(new Money(26)),
                     "act.customerAccountRefund",
                     "act.customerAccountRefundEFT");

        checkReverse(createRefundCash(new Money(10)),
                     "act.customerAccountPayment",
                     "act.customerAccountPaymentCash");

        checkReverse(createRefundCheque(new Money(11)),
                     "act.customerAccountPayment",
                     "act.customerAccountPaymentCheque");

        checkReverse(createRefundCredit(new Money(12)),
                     "act.customerAccountPayment",
                     "act.customerAccountPaymentCredit");

        checkReverse(createRefundDiscount(new Money(13)),
                     "act.customerAccountPayment",
                     "act.customerAccountPaymentDiscount");

        checkReverse(createRefundEFT(new Money(15)),
                     "act.customerAccountPayment",
                     "act.customerAccountPaymentEFT");

        checkReverse(createDebitAdjust(new Money(5)),
                     "act.customerAccountCreditAdjust");

        checkReverse(createCreditAdjust(new Money(15)),
                     "act.customerAccountDebitAdjust");

        checkReverse(createBadDebt(new Money(20)),
                     "act.customerAccountDebitAdjust");

        checkReverse(createInitialBalance(new Money(25)),
                     "act.customerAccountCreditAdjust");
    }

    /**
     * Tests reversal of an allocated act.
     */
    @Test
    public void testReverseAllocated() {
        Money amount = new Money(100);
        List<FinancialAct> invoice = createChargesInvoice(amount);
        save(invoice);

        FinancialAct payment = createPayment(amount);
        save(payment);

        checkBalance(BigDecimal.ZERO);

        rules.reverse(payment, new Date(), "Test reversal");
        checkBalance(amount);
    }

    /**
     * Tests reversal of an unallocated act.
     */
    @Test
    public void testReverseUnallocated() {
        Money amount = new Money(100);
        List<FinancialAct> invoice = createChargesInvoice(amount);
        save(invoice);

        checkBalance(amount);

        rules.reverse(invoice.get(0), new Date(), "Test reversal");
        checkBalance(BigDecimal.ZERO);
    }

    /**
     * Tests reversal of a partially allocated act.
     */
    @Test
    public void testReversePartiallyAllocated() {
        Money amount = new Money(100);
        Money sixty = new Money(60);
        Money forty = new Money(40);

        // create a new invoice for $100
        List<FinancialAct> invoices = createChargesInvoice(amount);
        save(invoices);
        FinancialAct invoice = invoices.get(0);
        checkEquals(BigDecimal.ZERO, invoice.getAllocatedAmount());

        // pay $60. Payment is fully allocated, $60 of invoice is allocated.
        FinancialAct payment = createPayment(sixty);
        save(payment);
        checkEquals(sixty, payment.getAllocatedAmount());

        invoice = get(invoice);
        checkEquals(sixty, invoice.getAllocatedAmount());

        // $40 outstanding balance
        checkBalance(forty);

        // reverse the payment.
        FinancialAct reversal = rules.reverse(payment, new Date(), "Test reversal");
        checkEquals(BigDecimal.ZERO, reversal.getAllocatedAmount());

        // invoice and payment retain their allocations
        invoice = get(invoice);
        checkEquals(sixty, invoice.getAllocatedAmount());

        payment = get(payment);
        checkEquals(sixty, payment.getAllocatedAmount());

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
        List<FinancialAct> invoices = createChargesInvoice(Money.ZERO);
        FinancialAct invoice = invoices.get(0);
        invoice.setStatus(ActStatus.IN_PROGRESS);
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
        invoice.setStatus(ActStatus.POSTED);
        save(invoice);
        assertNull(bean.getParticipation(BALANCE_PARTICIPATION));
    }

    /**
     * Verifies that older unallocated balances are allocated prior to more
     * recent ones for OVPMS-795.
     */
    @Test
    public void testAllocationOrder() {
        Money sixty = new Money(60);
        Money forty = new Money(40);
        Money twenty = new Money(20);
        Date chargeTime1 = java.sql.Date.valueOf("2007-1-1");
        Date chargeTime2 = java.sql.Date.valueOf("2007-3-30");

        List<FinancialAct> invoice1 = createChargesInvoice(sixty, chargeTime1);
        save(invoice1);
        List<FinancialAct> invoice2 = createChargesInvoice(forty, chargeTime2);
        save(invoice2);

        java.sql.Date payTime1 = java.sql.Date.valueOf("2007-4-1");
        FinancialAct payment1 = createPayment(forty, payTime1);
        save(payment1);

        FinancialAct reload1 = get(invoice1.get(0));
        FinancialAct reload2 = get(invoice2.get(0));
        checkEquals(forty, reload1.getAllocatedAmount());
        checkEquals(BigDecimal.ZERO, reload2.getAllocatedAmount());

        java.sql.Date payTime2 = java.sql.Date.valueOf("2007-4-2");
        FinancialAct payment2 = createPayment(twenty, payTime2);
        save(payment2);

        reload1 = get(invoice1.get(0));
        reload2 = get(invoice2.get(0));
        checkEquals(sixty, reload1.getAllocatedAmount());
        checkEquals(BigDecimal.ZERO, reload2.getAllocatedAmount());

        java.sql.Date payTime3 = java.sql.Date.valueOf("2007-4-3");
        FinancialAct payment3 = createPayment(forty, payTime3);
        save(payment3);

        reload1 = get(invoice1.get(0));
        reload2 = get(invoice2.get(0));
        checkEquals(sixty, reload1.getAllocatedAmount());
        checkEquals(forty, reload2.getAllocatedAmount());
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void onSetUp() {
        rules = new CustomerAccountRules();
    }

    /**
     * Verfies that when an act is saved, a
     * <em>participation.customerAccountBalance</em> is associated with it.
     *
     * @param acts the act
     */
    private void checkAddToBalance(List<FinancialAct> acts) {
        ActBean bean = new ActBean(acts.get(0));
        assertNull(bean.getParticipant("participation.customerAccountBalance"));
        save(acts);
        Act act = get(acts.get(0));
        bean = new ActBean(act);
        assertEquals(getCustomer(), bean.getParticipant(
                "participation.customerAccountBalance"));
    }

    private void checkAddToBalance(Act act) {
        save(act);
        act = get(act);
        ActBean bean = new ActBean(act);
        assertEquals(getCustomer(), bean.getParticipant(
                "participation.customerAccountBalance"));

    }

    /**
     * Verifies that a debit is offset by a credit of the same amount.
     *
     * @param debits  the debit acts
     * @param credits the credit act
     */
    private void checkCalculateBalanceForSameAmount(List<FinancialAct> debits,
                                                    List<FinancialAct> credits) {
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
        checkEquals(BigDecimal.ZERO, debit.getAllocatedAmount());
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
        checkBalance(BigDecimal.ZERO);
    }

    /**
     * Verifies that the customer account balance matches that expected.
     *
     * @param amount the expected amount
     */
    private void checkBalance(BigDecimal amount) {
        Party customer = getCustomer();
        checkEquals(amount, rules.getBalance(customer));
        checkEquals(amount, rules.getDefinitiveBalance(customer));
    }

    /**
     * Verifies the total amount allocated to an act matches that of the
     * amounts from the associated
     * <em>actRelationship.customerAccountllocation</em> relationships.
     *
     * @param act  the act
     * @param acts the acts contributing to the allocated amount
     */
    private void checkAllocation(FinancialAct act, FinancialAct... acts) {
        IMObjectBean bean = new IMObjectBean(act);
        BigDecimal total = BigDecimal.ZERO;
        List<ActRelationship> allocations = bean.getValues(
                "allocation", ActRelationship.class);
        List<FinancialAct> matches = new ArrayList<FinancialAct>();
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
        checkReverse(act, shortName, null);
    }

    /**
     * Verifies that an act can be reversed by
     * {@link CustomerAccountRules#reverse}, and has the correct child act,
     * if any.
     *
     * @param acts          the acts to reverse
     * @param shortName     the reversal act short name
     * @param itemShortName the reversal act child short name.
     *                      May be <tt>null</tt>
     */
    private void checkReverse(List<FinancialAct> acts, String shortName,
                              String itemShortName) {
        save(acts);
        checkReverse(acts.get(0), shortName, itemShortName);
    }

    /**
     * Verifies that an act can be reversed by
     * {@link CustomerAccountRules#reverse}, and has the correct child act,
     * if any.
     *
     * @param act           the act to reverse
     * @param shortName     the reversal act short name
     * @param itemShortName the reversal act child short name.
     *                      May be <tt>null</tt>
     */
    private void checkReverse(FinancialAct act, String shortName,
                              String itemShortName) {
        BigDecimal amount = act.getTotal();
        act.setStatus(ActStatus.POSTED);
        save(act);

        // check the balance
        BigDecimal balance = act.isCredit() ? amount.negate() : amount;
        checkBalance(balance);

        Date now = new Date();
        FinancialAct reversal = rules.reverse(act, now, "Test reversal");
        assertTrue(TypeHelper.isA(reversal, shortName));
        ActBean bean = new ActBean(reversal);
        if (itemShortName != null) {
            List<Act> items = bean.getNodeActs("items");
            assertEquals(1, items.size());
            Act item = items.get(0);
            assertTrue(TypeHelper.isA(item, itemShortName));
            if (TypeHelper.isA(item, "act.customerAccountPaymentCash",
                               "act.customerAccountRefundCash")) {
                ActBean itemBean = new ActBean(item);
                BigDecimal roundedAmount
                        = (BigDecimal) itemBean.getValue("roundedAmount");
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

}
