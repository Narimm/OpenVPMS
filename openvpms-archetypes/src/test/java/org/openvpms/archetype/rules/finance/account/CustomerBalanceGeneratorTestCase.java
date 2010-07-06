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
import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.BALANCE_PARTICIPATION;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Tests the {@link CustomerBalanceGenerator} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
@ContextConfiguration(locations="/application-context.xml", inheritLocations = false)
public class CustomerBalanceGeneratorTestCase extends AbstractCustomerAccountTest {

    /**
     * The account rules.
     */
    private CustomerAccountRules rules;


    /**
     * Verifies that an <em>act.customerAccountChargesInvoice</em> is
     * offset by an <em>act.customerAccountPayment</em> for the same amount.
     */
    @Test
    public void testGenerateBalanceForChargesInvoiceAndPayment() {
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
     * Verifies that an <em>participation.customerAccountBalance</tt> is
     * added for acts that don't have <tt>POSTED</tt> status.
     */
    @Test
    public void testAddParticipationForNonPostedActs() {
        List<FinancialAct> invoice1 = createChargesInvoice(new Money(100));
        checkAddParticipationForNonPostedAct(invoice1,
                                             FinancialActStatus.IN_PROGRESS);
        List<FinancialAct> invoice2 = createChargesInvoice(new Money(100));
        checkAddParticipationForNonPostedAct(invoice2,
                                             FinancialActStatus.ON_HOLD);
        List<FinancialAct> invoice3 = createChargesInvoice(new Money(100));
        checkAddParticipationForNonPostedAct(invoice3,
                                             FinancialActStatus.COMPLETED);
    }

    /**
     * Verifies that opening and closing balances are updated with correct
     * amounts.
     */
    @Test
    public void testChangeOpeningAndClosingBalances() {
        Party customer = getCustomer();
        List<FinancialAct> invoice = createChargesInvoice(new Money(10));
        FinancialAct opening1 = createOpeningBalance(customer);
        FinancialAct payment = createPayment(new Money(30));
        FinancialAct closing1 = createClosingBalance(customer);
        FinancialAct opening2 = createOpeningBalance(customer);

        save(invoice);
        save(opening1, payment, closing1, opening2);

        checkEquals(BigDecimal.ZERO, rules.getBalance(customer));

        try {
            rules.getDefinitiveBalance(customer);
            fail("Expected getDefinitiveBalance() to fail");
        } catch (CustomerAccountRuleException expected) {
            assertEquals(CustomerAccountRuleException.ErrorCode.InvalidBalance,
                         expected.getErrorCode());
        }

        checkEquals(new BigDecimal(-20), generate(customer));

        checkEquals(new BigDecimal(-20), rules.getBalance(customer));

        opening1 = get(opening1);
        closing1 = get(closing1);
        opening2 = get(opening2);

        checkEquals(new BigDecimal(10), opening1.getTotal());
        assertFalse(opening1.isCredit());

        checkEquals(new BigDecimal(20), closing1.getTotal());
        assertFalse(closing1.isCredit());

        checkEquals(new BigDecimal(20), opening2.getTotal());
        assertTrue(opening2.isCredit());
    }

    /**
     * Returns the location of the spring config files.
     *
     * @return an array of config locations
     */
    protected String[] getConfigLocations() {
        return new String[]{"application-context.xml"};
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        rules = new CustomerAccountRules();
    }

    /**
     * Verifies that a debit is offset by a credit of the same amount.
     *
     * @param debits  the debit acts
     * @param credits the credit acts
     */
    private void checkCalculateBalanceForSameAmount(List<FinancialAct> debits,
                                                    List<FinancialAct> credits) {
        Party customer = getCustomer();

        // initial balance is zero
        checkEquals(BigDecimal.ZERO, rules.getBalance(customer));
        assertTrue(checkBalance(customer));

        // save the debit act, and verify the balance is the same as the debit
        // total
        save(debits);

        // definitive balance out of sync with balance until generate invoked
        assertFalse(checkBalance(customer));
        FinancialAct debit = debits.get(0);
        checkEquals(debit.getTotal(), generate(customer));
        checkEquals(debit.getTotal(), rules.getBalance(customer));

        assertTrue(checkBalance(customer)); // should now be in sync

        // verify an participation.customerAccountBalance has been added to
        // the debit act
        debit = get(debit);
        Participation balanceParticipation1
                = getAccountBalanceParticipation(debit);
        assertNotNull(balanceParticipation1);

        // regenerate the balance. This should remove the existing participation
        // and add a new one
        checkEquals(debit.getTotal(), generate(customer));

        // verify the participation has changed
        debit = get(debit);
        Participation balanceParticipation2
                = getAccountBalanceParticipation(debit);
        assertNotNull(balanceParticipation2);
        assertFalse(balanceParticipation1.equals(balanceParticipation2));

        // save the credit act and update the balance. Balance should be zero
        save(credits);
        checkEquals(BigDecimal.ZERO, generate(customer));
        checkEquals(BigDecimal.ZERO, rules.getBalance(customer));

        // verify there is an actRelationship.customerAccountAllocation
        // linking the acts
        debit = get(debit);
        FinancialAct credit = get(credits.get(0));
        ActRelationship debitAlloc = getAccountAllocationRelationship(debit);
        ActRelationship creditAlloc = getAccountAllocationRelationship(credit);
        checkAllocation(debitAlloc, debit, credit, debit.getTotal());
        checkAllocation(creditAlloc, debit, credit, debit.getTotal());
        assertTrue(checkBalance(customer));

        // Now delete the credit. The balance will not be updated to reflect
        // the deletion.
        remove(credit);
        checkEquals(BigDecimal.ZERO, rules.getBalance(customer));

        // need to regenerate to get the correct balance
        checkEquals(debit.getTotal(), generate(customer));
        checkEquals(debit.getTotal(), debit.getAllocatedAmount());
        debit = get(debit);
        checkEquals(BigDecimal.ZERO, debit.getAllocatedAmount());
        checkEquals(debit.getTotal(), rules.getBalance(customer));
        assertTrue(checkBalance(customer));

        // participation.customerAccountBalance will have been recreated
        Participation balanceParticipation3
                = getAccountBalanceParticipation(debit);
        assertNotNull(balanceParticipation3);
        assertFalse(balanceParticipation3.equals(balanceParticipation2));

        // actRelationship.customerAccountAllocation will have been removed
        assertNull(getAccountAllocationRelationship(debit));
    }

    /**
     * Determines if the balance as returned by
     * {@link BalanceCalculator#getBalance} matches that returned by
     * {@link BalanceCalculator#getDefinitiveBalance}.
     *
     * @param customer the customer
     * @return <tt>true</tt> if the balances match, otherwise <tt>false</tt>
     */
    private boolean checkBalance(Party customer) {
        BalanceCalculator calc = new BalanceCalculator(getArchetypeService());
        BigDecimal expected = calc.getDefinitiveBalance(customer);
        BigDecimal actual = calc.getBalance(customer);
        return expected.compareTo(actual) == 0;
    }

    /**
     * Verifies that an <em>actRelationship.customerAccountAllocation<em>
     * is associated with the correct acts, and has the expected allocated
     * amount.
     *
     * @param relationship the relationship to check
     * @param source       the expected source
     * @param target       the expected target
     * @param allocated    the expected allocated amount
     */
    private void checkAllocation(ActRelationship relationship,
                                 FinancialAct source, FinancialAct target,
                                 BigDecimal allocated) {
        assertNotNull(relationship);
        assertEquals(relationship.getSource(), source.getObjectReference());
        assertEquals(relationship.getTarget(), target.getObjectReference());
        IMObjectBean bean = new IMObjectBean(relationship);
        checkEquals(allocated, bean.getBigDecimal("allocatedAmount"));
    }

    /**
     * Verifies that an <em>participation.customerAccountBalance</tt> is
     * added for acts that don't have <tt>POSTED</tt> status.
     *
     * @param acts   the act and associated child acts
     * @param status the act status
     */
    private void checkAddParticipationForNonPostedAct(List<FinancialAct> acts,
                                                      String status) {
        assertFalse(FinancialActStatus.POSTED.equals(status));
        Party customer = getCustomer();

        FinancialAct act = acts.get(0);
        act.setStatus(status);
        save(acts);

        checkEquals(BigDecimal.ZERO, generate(customer));
        act = get(act);

        // verify the act hasn't affected the balance
        checkEquals(BigDecimal.ZERO, rules.getBalance(customer));

        // verify a participation.customerAccountBalance has been added,
        // linked to the customer
        ActBean bean = new ActBean(act);
        assertEquals(customer, bean.getParticipant(BALANCE_PARTICIPATION));

        // verify that there is no account allocation relationship
        assertNull(getAccountAllocationRelationship(act));

        // verify the allocated amount is zero
        checkEquals(BigDecimal.ZERO, act.getAllocatedAmount());
    }

    /**
     * Generates the balance for a customer.
     *
     * @param customer the customer
     * @return the balance
     */
    private BigDecimal generate(Party customer) {
        CustomerBalanceGenerator generator
                = new CustomerBalanceGenerator(customer, getArchetypeService());
        return generator.generate();
    }

    /**
     * Helper to get the <em>participation.customerAccountBalance</em>
     * associated with an act.
     *
     * @param act the act
     * @return the participation, or <tt>null</tt>
     */
    private Participation getAccountBalanceParticipation(FinancialAct act) {
        ActBean bean = new ActBean(act);
        return bean.getParticipation(BALANCE_PARTICIPATION);
    }

    /**
     * Helper to get the <em>actRelationship.customerAccountAllocation</em>
     * associated with an act.
     *
     * @param act the act
     * @return the relationship, or <tt>null</tt>
     */
    private ActRelationship getAccountAllocationRelationship(FinancialAct act) {
        ActBean bean = new ActBean(act);
        List<ActRelationship> relationships = bean.getRelationships(
                CustomerAccountArchetypes.ACCOUNT_ALLOCATION_RELATIONSHIP);
        return (!relationships.isEmpty()) ? relationships.get(0) : null;
    }

    /**
     * Helper to create an opening balance.
     *
     * @param customer the customer
     * @return the opening balance
     */
    private FinancialAct createOpeningBalance(Party customer) {
        FinancialAct act = (FinancialAct) create(
                CustomerAccountArchetypes.OPENING_BALANCE);
        ActBean bean = new ActBean(act);
        bean.addParticipation(CustomerArchetypes.CUSTOMER_PARTICIPATION,
                              customer);
        return act;
    }

    /**
     * Helper to create a closing balance.
     *
     * @param customer the customer
     * @return the closing balance
     */
    private FinancialAct createClosingBalance(Party customer) {
        FinancialAct act = (FinancialAct) create(
                CustomerAccountArchetypes.CLOSING_BALANCE);
        ActBean bean = new ActBean(act);
        bean.addParticipation(CustomerArchetypes.CUSTOMER_PARTICIPATION,
                              customer);
        return act;
    }

}
