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

package org.openvpms.archetype.rules.balance;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Tests the {@link CustomerBalanceRules} class when triggered by the
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
public class CustomerBalanceRulesTestCase extends AbstractCustomerBalanceTest {

    /**
     * The rules.
     */
    private CustomerBalanceRules rules;


    /**
     * Verifies that when a posted <em>act.customerAccountChargesInvoice</em>
     * is saved, an <em>participation.customerAccountBalance</em> is
     * associated with it.
     */
    public void testAddChargesInvoiceToBalance() {
        checkAddToBalance(createChargesInvoice(new Money(100)));
    }

    /**
     * Verifies that when a posted <em>act.customerAccountChargesCounter</em>
     * is saved, an <em>participation.customerAccountBalance</em> is
     * associated with it.
     */
    public void testAddChargesCounterToBalance() {
        checkAddToBalance(createChargesCounter(new Money(100)));
    }

    /**
     * Verifies that when a posted <em>act.customerAccountChargesCredit</em>
     * is saved, an <em>participation.customerAccountBalance</em> is
     * associated with it.
     */
    public void testAddChargesCreditToBalance() {
        checkAddToBalance(createChargesCredit(new Money(100)));
    }

    /**
     * Verifies that when a posted <em>act.customerPayment</em>
     * is saved, an <em>participation.customerAccountBalance</em> is
     * associated with it.
     */
    public void testAddPaymentToBalance() {
        checkAddToBalance(createPayment(new Money(100)));
    }

    /**
     * Verifies that when a posted <em>act.customerRefund</em>
     * is saved, an <em>participation.customerAccountBalance</em> is
     * associated with it.
     */
    public void testAddRefundToBalance() {
        checkAddToBalance(createRefund(new Money(100)));
    }

    /**
     * Verifies that when a posted <em>act.customerAccountCreditAdjust</em>
     * is saved, an <em>participation.customerAccountBalance</em> is
     * associated with it.
     */
    public void testAddCreditAdjustToBalance() {
        checkAddToBalance(createCreditAdjust(new Money(100)));
    }

    /**
     * Verifies that when a posted <em>act.customerAccountDebitAdjust</em>
     * is saved, an <em>participation.customerAccountBalance</em> is
     * associated with it.
     */
    public void testAddDebitAdjustToBalance() {
        checkAddToBalance(createDebitAdjust(new Money(100)));
    }

    /**
     * Verifies that when a posted <em>act.customerAccountInitialBalance</em>
     * is saved, an <em>participation.customerAccountBalance</em> is
     * associated with it.
     */
    public void testAddInitialBalanceToBalance() {
        checkAddToBalance(createInitialBalance(new Money(100)));
    }

    /**
     * Verifies that when a posted <em>act.customerAccountBadDebt</em>
     * is saved, an <em>participation.customerAccountBalance</em> is
     * associated with it.
     */
    public void testAddBadDebtToBalance() {
        checkAddToBalance(createBadDebt(new Money(100)));
    }

    /**
     * Verifies that an <em>act.customerAccountChargesInvoice</em> is
     * offset by an <em>act.customerAccountPayment</em> for the same amount.
     */
    public void testGetBalanceForChargesInvoiceAndPayment() {
        Money amount = new Money(100);
        FinancialAct invoice = createChargesInvoice(amount);
        FinancialAct payment = createPayment(amount);
        checkCalculateBalanceForSameAmount(invoice, payment);
    }

    /**
     * Verifies that an <em>act.customerAccountChargesCounter</em> is
     * offset by an <em>act.customerAccountPayment</em> for the same amount.
     */
    public void testGetBalanceForChargesCounterAndPayment() {
        Money amount = new Money(100);
        FinancialAct counter = createChargesCounter(amount);
        FinancialAct payment = createPayment(amount);
        checkCalculateBalanceForSameAmount(counter, payment);
    }

    /**
     * Verifies that an <em>act.customerAccountChargesInvoice</em> is
     * offset by an <em>act.customerAccountChargesCredit</em> for the same
     * amount.
     */
    public void testGetBalanceForChargesInvoiceAndCredit() {
        Money amount = new Money(100);
        FinancialAct invoice = createChargesInvoice(amount);
        FinancialAct credit = createChargesCredit(amount);
        checkCalculateBalanceForSameAmount(invoice, credit);
    }

    /**
     * Verifies that an <em>act.customerAccountRefund</em> is offset by an
     * <em>act.customerAccountPayment</em> for the same amount.
     */
    public void testGetBalanceForRefundAndPayment() {
        Money amount = new Money(100);
        FinancialAct refund = createRefund(amount);
        FinancialAct payment = createCreditAdjust(amount);
        checkCalculateBalanceForSameAmount(refund, payment);
    }

    /**
     * Verifies that an <em>act.customerAccountDebitAdjust</em> is offset by an
     * <em>act.customerAccountCreditAdjust</em> for the same amount.
     */
    public void testGetBalanceForDebitAndCreditAdjust() {
        Money amount = new Money(100);
        FinancialAct debit = createDebitAdjust(amount);
        FinancialAct credit = createCreditAdjust(amount);
        checkCalculateBalanceForSameAmount(debit, credit);
    }

    /**
     * Verifies that an <em>act.customerAccountInitialBalance</em> is offset by
     * an <em>act.customerAccountBadDebt</em> for the same amount.
     */
    public void testGetBalanceForInitialBalanceAndBadDebt() {
        Money amount = new Money(100);
        FinancialAct debit = createInitialBalance(amount);
        FinancialAct credit = createBadDebt(amount);
        checkCalculateBalanceForSameAmount(debit, credit);
    }

    /**
     * Tests the {@link CustomerBalanceRules#getBalance} method.
     */
    public void testGetBalance() {
        Party customer = getCustomer();
        Money hundred = new Money(100);
        Money sixty = new Money(60);
        Money forty = new Money(40);
        FinancialAct invoice = createChargesInvoice(hundred);
        save(invoice);

        // check the balance
        checkEquals(hundred, rules.getBalance(customer));

        // make sure the invoice has an account balance participation
        ActBean bean = new ActBean(invoice);
        assertEquals(customer, bean.getParticipant(
                "participation.customerAccountBalance"));

        // reload and verify act has not changed
        invoice = (FinancialAct) get(invoice);
        checkEquals(hundred, invoice.getTotal());
        checkEquals(BigDecimal.ZERO, invoice.getAllocatedAmount());
        checkAllocation(invoice);

        // pay 60 of the debt
        FinancialAct payment1 = createPayment(sixty);
        save(payment1);

        // check the balance
        checkEquals(forty, rules.getBalance(customer));

        // reload and verify the acts have changed
        invoice = (FinancialAct) get(invoice);
        payment1 = (FinancialAct) get(payment1);

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
        invoice = (FinancialAct) get(invoice);
        payment2 = (FinancialAct) get(payment2);

        checkEquals(hundred, invoice.getTotal());
        checkEquals(hundred, invoice.getAllocatedAmount());
        checkAllocation(invoice, payment1, payment2);

        checkEquals(forty, payment2.getTotal());
        checkEquals(forty, payment2.getAllocatedAmount());
        checkAllocation(payment2, invoice);
    }

    /**
     * Tests the {@link CustomerBalanceRules#getBalance(Party, BigDecimal,
     * boolean)} method.
     */
    public void testGetRunningBalance() {
        Party customer = getCustomer();
        Money hundred = new Money(100);
        Money sixty = new Money(60);
        Money forty = new Money(40);
        Money ten = new Money(10);
        Money five = new Money(5);
        FinancialAct invoice = createChargesInvoice(hundred);
        save(invoice);

        // check the balance for a payment
        checkEquals(hundred, rules.getBalance(customer, BigDecimal.ZERO, true));

        // check the balance for a refund
        checkEquals(BigDecimal.ZERO, rules.getBalance(customer, BigDecimal.ZERO,
                                                      false));

        // simulate payment of 60. Running balance should be 40
        checkEquals(forty, rules.getBalance(customer, sixty, true));

        // overpay by 10
        FinancialAct payment1 = createPayment(new Money("110.0"));
        save(payment1);

        // check the balance for a payment
        checkEquals(BigDecimal.ZERO, rules.getBalance(customer, BigDecimal.ZERO,
                                                      true));

        // check the balance for a refund
        checkEquals(ten, rules.getBalance(customer, BigDecimal.ZERO, false));

        // check the balance after refunding 5
        checkEquals(five, rules.getBalance(customer, five, false));
    }

    /**
     * Tests the {@link CustomerBalanceRules#getBalance(Party)} for acts
     * with a zero total. These should not have a
     * <em>participation.customerAccountBalance</em> participation
     * or <em>actRelationship.customerAccountAllocation<em> relationship.
     */
    public void testAddZeroTotalToBalance() {
        Money zero = new Money(0);
        FinancialAct invoice = createChargesInvoice(zero);
        save(invoice);
        invoice = (FinancialAct) get(invoice);
        ActBean invoiceBean = new ActBean(invoice);
        assertNull(invoiceBean.getParticipant(
                "participation.customerAccountBalance"));

        FinancialAct payment = createPayment(zero);
        save(payment);

        invoice = (FinancialAct) get(invoice);
        invoiceBean = new ActBean(invoice);

        assertTrue(invoiceBean.getRelationships(
                "actRelationship.customerAccountAllocation").isEmpty());

        payment = (FinancialAct) get(payment);
        ActBean payBean = new ActBean(payment);
        assertNull(payBean.getParticipant(
                "participation.customerAccountBalance"));
        assertTrue(payBean.getRelationships(
                "actRelationship.customerAccountAllocation").isEmpty());

        // check the balance
        checkEquals(zero, rules.getBalance(getCustomer()));
    }

    /**
     * Tests the {@link CustomerBalanceRules#getOverdueBalance} method.
     */
    public void testGetOverdueBalance() {
        // add a 30 day payment term for accounts to the customer
        Party customer = getCustomer();
        customer.addClassification(createAccountType(30, DateRules.DAYS));
        save(getCustomer());

        // create and save a new invoice
        final Money amount = new Money(100);
        Date startTime = java.sql.Date.valueOf("2007-1-1");
        FinancialAct invoice = createChargesInvoice(amount);
        invoice.setActivityStartTime(startTime);
        save(invoice);

        // check the invoice is not overdue on the day it is saved
        BigDecimal overdue = rules.getOverdueBalance(customer, startTime);
        checkEquals(BigDecimal.ZERO, overdue);

        // 30 days from saved, amount shouldn't be overdue
        Date now = DateRules.getDate(startTime, 30, DateRules.DAYS);
        overdue = rules.getOverdueBalance(customer, now);
        checkEquals(BigDecimal.ZERO, overdue);

        // 31 days from saved, invoice should be overdue.
        now = DateRules.getDate(now, 1, DateRules.DAYS);
        overdue = rules.getOverdueBalance(customer, now);
        checkEquals(amount, overdue);

        // now save a credit for the same date as the invoice with total
        // > invoice total. The balance should be negative, but the overdue
        // balance should be zero as it only sums debits.
        FinancialAct credit = createChargesCredit(new Money(150));
        credit.setActivityStartTime(startTime);
        save(credit);
        checkEquals(new Money(-50), rules.getBalance(getCustomer()));
        overdue = rules.getOverdueBalance(customer, now);
        checkEquals(BigDecimal.ZERO, overdue);
    }

    /**
     * Tests the {@link CustomerBalanceRules#getCreditBalance(Party)} method.
     */
    public void testGetCreditAmount() {
        final Money amount = new Money(100);
        FinancialAct chargesCredit = createChargesCredit(amount);
        FinancialAct creditAdjust = createCreditAdjust(amount);
        FinancialAct payment = createPayment(amount);
        FinancialAct badDebt = createBadDebt(amount);

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
     * Tests the {@link CustomerBalanceRules#getUnbilledAmount(Party)}  method.
     */
    public void testGetUnbilledAmount() {
        final Money amount = new Money(100);
        FinancialAct invoice = createChargesInvoice(amount);
        FinancialAct counter = createChargesCounter(amount);
        FinancialAct credit = createChargesCredit(amount);
        invoice.setStatus(ActStatus.IN_PROGRESS);
        counter.setStatus(ActStatus.IN_PROGRESS);
        credit.setStatus(ActStatus.IN_PROGRESS);

        checkEquals(BigDecimal.ZERO, rules.getUnbilledAmount(getCustomer()));

        save(invoice);
        checkEquals(amount, rules.getUnbilledAmount(getCustomer()));

        save(counter);
        checkEquals(amount.multiply(new BigDecimal(2)),
                    rules.getUnbilledAmount(getCustomer()));

        save(credit);
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
    public void testReverse() {
        checkReverse(createChargesInvoice(new Money(100)),
                     "act.customerAccountChargesCredit");

        checkReverse(createChargesCredit(new Money(50)),
                     "act.customerAccountChargesInvoice");

        checkReverse(createChargesCounter(new Money(40)),
                     "act.customerAccountChargesCredit");

        checkReverse(createPaymentCash(new Money(75)),
                     "act.customerAccountRefund");

        checkReverse(createPaymentCheque(new Money(100)),
                     "act.customerAccountRefund");

        checkReverse(createRefund(new Money(10)), "act.customerAccountPayment");


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
    public void testReverseAllocated() {
        Money amount = new Money(100);
        FinancialAct invoice = createChargesInvoice(amount);
        save(invoice);

        FinancialAct payment = createPayment(amount);
        save(payment);

        checkEquals(BigDecimal.ZERO, rules.getBalance(getCustomer()));

        rules.reverse(payment, new Date());
        checkEquals(amount, rules.getBalance(getCustomer()));
    }

    /**
     * Tests reversal of an unallocated act.
     */
    public void testReverseUnallocated() {
        Money amount = new Money(100);
        FinancialAct invoice = createChargesInvoice(amount);
        save(invoice);

        checkEquals(amount, rules.getBalance(getCustomer()));

        rules.reverse(invoice, new Date());
        checkEquals(BigDecimal.ZERO, rules.getBalance(getCustomer()));
    }

    /**
     * Tests reversal of a partially allocated act.
     */
    public void testReversePartiallyAllocated() {
        Money amount = new Money(100);
        Money sixty = new Money(60);
        Money forty = new Money(40);

        // create a new invoice for $100
        FinancialAct invoice = createChargesInvoice(amount);
        save(invoice);
        checkEquals(BigDecimal.ZERO, invoice.getAllocatedAmount());

        // pay $60. Payment is fully allocated, $60 of invoice is allocated.
        FinancialAct payment = createPayment(sixty);
        save(payment);
        checkEquals(sixty, payment.getAllocatedAmount());

        invoice = (FinancialAct) get(invoice);
        checkEquals(sixty, invoice.getAllocatedAmount());

        // $40 outstanding balance
        checkEquals(forty, rules.getBalance(getCustomer()));

        // reverse the payment.
        FinancialAct reversal = rules.reverse(payment, new Date());
        checkEquals(BigDecimal.ZERO, reversal.getAllocatedAmount());

        // invoice and payment retain their allocations
        invoice = (FinancialAct) get(invoice);
        checkEquals(sixty, invoice.getAllocatedAmount());

        payment = (FinancialAct) get(payment);
        checkEquals(sixty, payment.getAllocatedAmount());

        checkEquals(amount, rules.getBalance(getCustomer()));
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        rules = new CustomerBalanceRules();
    }

    /**
     * Verfies that when an act is saved, a
     * <em>participation.customerAccountBalance</em> is associated with it.
     *
     * @param act the act
     */
    private void checkAddToBalance(FinancialAct act) {
        save(act);
        act = (FinancialAct) get(act);
        ActBean bean = new ActBean(act);
        assertEquals(getCustomer(), bean.getParticipant(
                "participation.customerAccountBalance"));
    }

    /**
     * Verifies that a debit is offset by a credit of the same amount.
     *
     * @param debit  the debit act
     * @param credit the credit act
     */
    private void checkCalculateBalanceForSameAmount(FinancialAct debit,
                                                    FinancialAct credit) {
        BigDecimal amount = credit.getTotal();
        checkEquals(amount, debit.getTotal());
        assertTrue(credit.isCredit());
        assertFalse(debit.isCredit());

        // save and reload the debit. The allocated amount should be unchanged
        save(debit);
        debit = (FinancialAct) get(debit);
        checkEquals(amount, debit.getTotal());
        checkEquals(BigDecimal.ZERO, debit.getAllocatedAmount());
        checkAllocation(debit);

        // check the outstanding balance
        checkEquals(amount, rules.getBalance(getCustomer()));

        // save the credit. The allocated amount for the debit and credit should
        // be the same as the total
        save(credit);

        credit = (FinancialAct) get(credit);
        debit = (FinancialAct) get(debit);

        checkEquals(amount, credit.getTotal());
        checkEquals(amount, credit.getAllocatedAmount());
        checkAllocation(credit, debit);

        checkEquals(amount, debit.getTotal());
        checkEquals(amount, debit.getAllocatedAmount());
        checkAllocation(debit, credit);

        // check the outstanding balance
        checkEquals(BigDecimal.ZERO, rules.getBalance(getCustomer()));
    }

    /**
     * Verifies the total amount allocated to an act matches that of the
     * amounts from the associated
     * <em>actRelationship.customerAccountllocation</em> relationships.
     *
     * @param act  the act
     * @param acts the acts contributing to the allocated amount
     */
    private void checkAllocation(FinancialAct act, FinancialAct ... acts) {
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
     * {@link CustomerBalanceRules#reverse}.
     *
     * @param act       the act to reverse
     * @param shortName the reversal act short name
     */
    private void checkReverse(FinancialAct act, String shortName) {
        BigDecimal amount = act.getTotal();
        save(act);

        // check the balance
        BigDecimal balance = act.isCredit() ? amount.negate() : amount;
        checkEquals(balance, rules.getBalance(getCustomer()));

        Date now = new Date();
        FinancialAct reversal = rules.reverse(act, now);
        assertTrue(TypeHelper.isA(reversal, shortName));

        // check the balance
        checkEquals(BigDecimal.ZERO, rules.getBalance(getCustomer()));
    }

}
