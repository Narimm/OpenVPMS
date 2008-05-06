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

import org.openvpms.archetype.rules.act.FinancialActStatus;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountActTypes.ACCOUNT_BALANCE_SHORTNAME;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.List;


/**
 * Tests the {@link CustomerBalanceGenerator} tool.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerBalanceGeneratorTestCase
        extends AbstractCustomerAccountTest {

    /**
     * The account rules.
     */
    private CustomerAccountRules rules;



    /**
     * Verifies that an <em>act.customerAccountChargesInvoice</em> is
     * offset by an <em>act.customerAccountPayment</em> for the same amount.
     */
    public void testGenerateBalanceForChargesInvoiceAndPayment() {
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
     * Tests generation given a customer id.
     */
    public void testGenerateForCustomerId() {
        Party customer = getCustomer();
        long id = customer.getUid();
        Money amount = new Money(100);
        FinancialAct debit = createInitialBalance(amount);
        save(debit);

        assertEquals(BigDecimal.ZERO, rules.getBalance(customer));

        CustomerBalanceGenerator generator
                = new CustomerBalanceGenerator(getArchetypeService());
        generator.generate(id);
        assertEquals(amount, rules.getBalance(customer));
        FinancialAct credit = createBadDebt(amount);
        save(credit);
        generator.generate(id);
        assertEquals(BigDecimal.ZERO, rules.getBalance(customer));
    }

    /**
     * Tests generation given a customer name.
     */
    public void testGenerateForCustomerName() {
        Party customer = getCustomer();
        String name = customer.getName();
        Money amount = new Money(100);
        FinancialAct debit = createInitialBalance(amount);
        save(debit);

        assertEquals(BigDecimal.ZERO, rules.getBalance(customer));

        CustomerBalanceGenerator generator
                = new CustomerBalanceGenerator(getArchetypeService());
        generator.generate(name);
        assertEquals(amount, rules.getBalance(customer));
        FinancialAct credit = createBadDebt(amount);
        save(credit);
        generator.generate(name);
        assertEquals(BigDecimal.ZERO, rules.getBalance(customer));
    }

    /**
     * Verifies that an <em>participation.customerAccountBalance</tt> is
     * added for acts that don't have <tt>POSTED</tt> status.
     */
    public void testAddParticipationForNonPostedActs() {
        FinancialAct invoice1 = createChargesInvoice(new Money(100));
        checkAddParticipationForNonPostedAct(invoice1,
                                             FinancialActStatus.IN_PROGRESS);
        FinancialAct invoice2 = createChargesInvoice(new Money(100));
        checkAddParticipationForNonPostedAct(invoice2,
                                             FinancialActStatus.ON_HOLD);
        FinancialAct invoice3 = createChargesInvoice(new Money(100));
        checkAddParticipationForNonPostedAct(invoice3,
                                             FinancialActStatus.COMPLETED);
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
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        rules = new CustomerAccountRules();
    }

    /**
     * Verifies that a debit is offset by a credit of the same amount.
     *
     * @param debit  the debit act
     * @param credit the credit act
     */
    private void checkCalculateBalanceForSameAmount(FinancialAct debit,
                                                    FinancialAct credit) {
        CustomerBalanceGenerator generator
                = new CustomerBalanceGenerator(getArchetypeService());
        Party customer = getCustomer();

        // initial balance is zero
        assertEquals(BigDecimal.ZERO, rules.getBalance(customer));
        assertTrue(generator.check(customer));

        // save the debit act, and verify the balance is the same as the debit
        // total
        save(debit);
        // definitive balance out of sync with balance until generate invoked
        assertFalse(generator.check(customer));
        generator.generate(customer);
        assertEquals(debit.getTotal(), rules.getBalance(customer));

        assertTrue(generator.check(customer)); // should now be in sync

        // verify an participation.customerAccountBalance has been added to
        // the debit act
        debit = get(debit);
        Participation balanceParticipation1
                = getAccountBalanceParticipation(debit);
        assertNotNull(balanceParticipation1);

        // regenerate the balance. This should remove the existing participation
        // and add a new one
        generator.setRegenerate(true);
        generator.generate(customer);

        // verify the participation has changed
        debit = get(debit);
        Participation balanceParticipation2
                = getAccountBalanceParticipation(debit);
        assertNotNull(balanceParticipation2);
        assertFalse(balanceParticipation1.equals(balanceParticipation2));

        // save the credit act and update the balance. Balance should be zero
        save(credit);
        generator.setRegenerate(false);
        generator.generate(customer);
        assertEquals(BigDecimal.ZERO, rules.getBalance(customer));

        // verify there is an actRelationship.customerAccountAllocation
        // linking the acts
        debit = get(debit);
        credit = get(credit);
        ActRelationship debitAlloc = getAccountAllocationRelationship(debit);
        ActRelationship creditAlloc = getAccountAllocationRelationship(credit);
        checkAllocation(debitAlloc, debit, credit, debit.getTotal());
        checkAllocation(creditAlloc, debit, credit, debit.getTotal());
        assertTrue(generator.check(customer));

        // Now delete the credit. The balance will not be updated to reflect
        // the deletion.
        remove(credit);
        assertEquals(BigDecimal.ZERO, rules.getBalance(customer));
        generator.generate(customer);
        assertEquals(BigDecimal.ZERO, rules.getBalance(customer));
        assertEquals(debit.getTotal(), debit.getAllocatedAmount());

        // need to regenerate to get the correct balance
        assertFalse(generator.check(customer));
        generator.setRegenerate(true);
        generator.generate(customer);
        debit = get(debit);
        assertEquals(BigDecimal.ZERO, debit.getAllocatedAmount());
        assertEquals(debit.getTotal(), rules.getBalance(customer));
        assertTrue(generator.check(customer));

        // participation.customerAccountBalance will have been recreated
        Participation balanceParticipation3
                = getAccountBalanceParticipation(debit);
        assertNotNull(balanceParticipation3);
        assertFalse(balanceParticipation3.equals(balanceParticipation2));

        // actRelationship.customerAccountAllocation will have been removed
        assertNull(getAccountAllocationRelationship(debit));
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
        assertEquals(allocated, bean.getBigDecimal("allocatedAmount"));
    }

    /**
     * Verifies that an <em>participation.customerAccountBalance</tt> is
     * added for acts that don't have <tt>POSTED</tt> status.
     *
     * @param act    the act
     * @param status the act status
     */
    private void checkAddParticipationForNonPostedAct(FinancialAct act,
                                                      String status) {
        assertFalse(FinancialActStatus.POSTED.equals(status));
        CustomerBalanceGenerator generator
                = new CustomerBalanceGenerator(getArchetypeService(),
                                               false, true);
        Party customer = getCustomer();

        act.setStatus(status);
        save(act);

        generator.generate(customer);
        act = get(act);

        // verify the act hasn't affected the balance
        assertEquals(BigDecimal.ZERO, rules.getBalance(customer));

        // verify a participation.customerAccountBalance has been added,
        // linked to the customer
        ActBean bean = new ActBean(act);
        assertEquals(customer, bean.getParticipant(ACCOUNT_BALANCE_SHORTNAME));

        // verify that there is no account allocation relationship
        assertNull(getAccountAllocationRelationship(act));

        // verify the allocated amount is zero
        assertEquals(BigDecimal.ZERO, act.getAllocatedAmount());
    }

    /**
     * Helper to get the <em>participation.customerAccountBalance</em>
     * associated with an act.
     *
     * @param act
     * @return the participation, or <tt>null</tt>
     */
    private Participation getAccountBalanceParticipation(FinancialAct act) {
        ActBean bean = new ActBean(act);
        return bean.getParticipation(ACCOUNT_BALANCE_SHORTNAME);
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
                CustomerAccountActTypes.ACCOUNT_ALLOCATION_SHORTNAME);
        return (!relationships.isEmpty()) ? relationships.get(0) : null;
    }

}
