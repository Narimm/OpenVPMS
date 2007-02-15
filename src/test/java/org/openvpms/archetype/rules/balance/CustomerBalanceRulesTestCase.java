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

import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.ArrayList;
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
public class CustomerBalanceRulesTestCase extends ArchetypeServiceTest {

    /**
     * The customer.
     */
    private Party customer;

    /**
     * The patient.
     */
    private Party patient;

    /**
     * The product.
     */
    private Product product;


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
    public void testCalculateBalanceForChargesInvoiceAndPayment() {
        Money amount = new Money(100);
        FinancialAct invoice = createChargesInvoice(amount);
        FinancialAct payment = createPayment(amount);
        checkCalculateBalanceForSameAmount(invoice, payment);
    }

    /**
     * Verifies that an <em>act.customerAccountChargesCounter</em> is
     * offset by an <em>act.customerAccountPayment</em> for the same amount.
     */
    public void testCalculateBalanceForChargesCounterAndPayment() {
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
    public void testCalculateBalanceForChargesInvoiceAndCredit() {
        Money amount = new Money(100);
        FinancialAct invoice = createChargesInvoice(amount);
        FinancialAct credit = createChargesCredit(amount);
        checkCalculateBalanceForSameAmount(invoice, credit);
    }

    /**
     * Verifies that an <em>act.customerAccountRefund</em> is offset by an
     * <em>act.customerAccountPayment</em> for the same amount.
     */
    public void testCalculateBalanceForRefundAndPayment() {
        Money amount = new Money(100);
        FinancialAct refund = createRefund(amount);
        FinancialAct payment = createCreditAdjust(amount);
        checkCalculateBalanceForSameAmount(refund, payment);
    }

    /**
     * Verifies that an <em>act.customerAccountDebitAdjust</em> is offset by an
     * <em>act.customerAccountCreditAdjust</em> for the same amount.
     */
    public void testCalculateBalanceForDebitAndCreditAdjust() {
        Money amount = new Money(100);
        FinancialAct debit = createDebitAdjust(amount);
        FinancialAct credit = createCreditAdjust(amount);
        checkCalculateBalanceForSameAmount(debit, credit);
    }

    /**
     * Verifies that an <em>act.customerAccountInitialBalance</em> is offset by
     * an <em>act.customerAccountBadDebt</em> for the same amount.
     */
    public void testCalculateBalanceForInitialBalanceAndBadDebt() {
        Money amount = new Money(100);
        FinancialAct debit = createInitialBalance(amount);
        FinancialAct credit = createBadDebt(amount);
        checkCalculateBalanceForSameAmount(debit, credit);
    }

    /**
     * Tests the {@link CustomerBalanceRules#calculateBalance}.
     */
    public void testCalculateBalance() {
        Money hundred = new Money(100);
        Money sixty = new Money(60);
        Money forty = new Money(40);
        FinancialAct invoice = createChargesInvoice(hundred);
        save(invoice);

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
     * Verifies two <code>BigDecimals</code> are equal.
     *
     * @param a the first value
     * @param b the second value
     */
    private void checkEquals(BigDecimal a, BigDecimal b) {
        if (a.compareTo(b) != 0) {
            fail("Expected " + a + ", but got " + b);
        }
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        customer = TestHelper.createCustomer();
        patient = TestHelper.createPatient();
        product = TestHelper.createProduct();
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
        assertEquals(customer, bean.getParticipant(
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
        assertEquals(amount, debit.getTotal());
        assertTrue(credit.isCredit());
        assertFalse(debit.isCredit());

        // save and reload the credit. The allocated amount should be unchanged
        save(credit);
        credit = (FinancialAct) get(credit);
        checkEquals(amount, credit.getTotal());
        checkEquals(BigDecimal.ZERO, credit.getAllocatedAmount());
        checkAllocation(credit);

        // save the debit. The allocated amount for the debit and credit should
        // be the same as the total
        save(debit);

        credit = (FinancialAct) get(credit);
        debit = (FinancialAct) get(debit);

        checkEquals(amount, credit.getTotal());
        checkEquals(amount, credit.getAllocatedAmount());
        checkAllocation(credit, debit);

        checkEquals(amount, debit.getTotal());
        checkEquals(amount, debit.getAllocatedAmount());
        checkAllocation(debit, credit);
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
     * Helper to create an <em>act.customerAccountChargesInvoice</em>.
     *
     * @param amount the act total
     * @return a new act
     */
    private FinancialAct createChargesInvoice(Money amount) {
        return createCharges("act.customerAccountChargesInvoice",
                             "act.customerAccountInvoiceItem",
                             "actRelationship.customerAccountInvoiceItem",
                             amount);
    }

    /**
     * Helper to create an <em>act.customerAccountChargesCounter</em>.
     *
     * @param amount the act total
     * @return a new act
     */
    private FinancialAct createChargesCounter(Money amount) {
        return createCharges("act.customerAccountChargesCounter",
                             "act.customerAccountCounterItem",
                             "actRelationship.customerAccountCounterItem",
                             amount);
    }

    /**
     * Helper to create an <em>act.customerAccountChargesCredit</em>.
     *
     * @param amount the act total
     * @return a new act
     */
    private FinancialAct createChargesCredit(Money amount) {
        return createCharges("act.customerAccountChargesCredit",
                             "act.customerAccountCreditItem",
                             "actRelationship.customerAccountCreditItem",
                             amount);
    }

    /**
     * Helper to create a charges act.
     *
     * @param shortName the act short name
     * @param amount    the act total
     * @return a new act
     */
    private FinancialAct createCharges(String shortName,
                                       String itemShortName,
                                       String relationshipShortName,
                                       Money amount) {
        FinancialAct act = createAct(shortName, amount);
        ActBean bean = new ActBean(act);
        FinancialAct item = (FinancialAct) create(itemShortName);
        item.setTotal(amount);
        ActBean itemBean = new ActBean(item);
        itemBean.addParticipation("participation.patient", patient);
        itemBean.addParticipation("participation.product", product);
        bean.addRelationship(relationshipShortName, item);
        return act;
    }

    /**
     * Helper to create an <em>act.customerAccountPayment</em>.
     *
     * @param amount the act total
     * @return a new payment
     */
    private FinancialAct createPayment(Money amount) {
        return createPaymentRefund("act.customerAccountPayment", amount);
    }

    /**
     * Helper to create an <em>act.customerAccountRefund</em>.
     *
     * @param amount the act total
     * @return a new payment
     */
    private FinancialAct createRefund(Money amount) {
        return createPaymentRefund("act.customerAccountRefund", amount);
    }

    /**
     * Helper to create a payment/refund.
     *
     * @param shortName the act short name
     * @param amount    the act total
     * @return a new payment
     */
    private FinancialAct createPaymentRefund(String shortName, Money amount) {
        FinancialAct act = createAct(shortName, amount);
        ActBean bean = new ActBean(act);
        Party till = (Party) create("party.organisationTill");
        bean.addParticipation("participation.till", till);
        bean.addParticipation("participation.patient", patient);
        return act;
    }

    /**
     * Helper to create an <em>act.customerAccountCreditAdjust</em>.
     *
     * @param amount the act total
     * @return a new credit adjustment
     */
    private FinancialAct createCreditAdjust(Money amount) {
        return createAct("act.customerAccountCreditAdjust", amount);
    }

    /**
     * Helper to create an <em>act.customerAccountDebitAdjust</em>.
     *
     * @param amount the act total
     * @return a new debit adjustment
     */
    private FinancialAct createDebitAdjust(Money amount) {
        return createAct("act.customerAccountDebitAdjust", amount);
    }

    /**
     * Helper to create an <em>act.customerAccountInitialBalance</em>.
     *
     * @param amount the act total
     * @return a new initial balance
     */
    private FinancialAct createInitialBalance(Money amount) {
        return createAct("act.customerAccountInitialBalance", amount);
    }

    /**
     * Helper to create an <em>act.customerAccountBadDebt</em>.
     *
     * @param amount the act total
     * @return a new bad debt
     */
    private FinancialAct createBadDebt(Money amount) {
        return createAct("act.customerAccountBadDebt", amount);
    }

    /**
     * Helper to create a new act, setting the total, adding a customer
     * participation and setting the status to 'POSTED'.
     *
     * @param shortName the act short name
     * @param amount    the act total
     * @return a new act
     */
    private FinancialAct createAct(String shortName, Money amount) {
        FinancialAct act = (FinancialAct) create(shortName);
        act.setStatus(FinancialActStatus.POSTED);
        act.setTotal(amount);
        ActBean bean = new ActBean(act);
        bean.addParticipation("participation.customer", customer);
        return act;
    }

}
