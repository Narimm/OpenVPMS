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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.finance.till;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.finance.deposit.DepositHelper;
import org.openvpms.archetype.rules.finance.deposit.DepositTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.rules.act.ActStatus.POSTED;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.CantAddActToTill;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.ClearedTill;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.InvalidTillArchetype;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.InvalidTransferTill;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.MissingRelationship;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.MissingTill;
import static org.openvpms.archetype.test.TestHelper.createTill;


/**
 * Tests the {@link TillRules} class.
 *
 * @author Tim Anderson
 */
public class TillRulesTestCase extends AbstractTillRulesTest {

    /**
     * The till.
     */
    private Party till;

    /**
     * The till business rules.
     */
    private TillRules rules;


    /**
     * Tests the {@link TillRules#clearTill)} method, with a zero cash float.
     * This should not create an <em>act.tillBalanceAdjustment</e>.
     */
    @Test
    public void testClearTillWithNoAdjustment() {
        final BigDecimal cashFloat = BigDecimal.ZERO;
        checkClearTillForUnclearedBalance(cashFloat, cashFloat);
    }

    /**
     * Tests the {@link TillRules#clearTill)} method, with an initial cash float
     * of {@code 40.0} and new cash float of {@code 20.0}.
     * This should create a credit adjustment of {@code 20.0}
     */
    @Test
    public void testClearTillWithCreditAdjustment() {
        final BigDecimal cashFloat = new BigDecimal(40);
        final BigDecimal newCashFloat = new BigDecimal(20);
        checkClearTillForUnclearedBalance(cashFloat, newCashFloat);
    }

    /**
     * Tests the {@link TillRules#clearTill)} method, with an initial cash float
     * of {@code 40.0} and new cash float of {@code 100.0}.
     * This should create a debit adjustment of {@code 60.0}
     */
    @Test
    public void testClearTillWithDebitAdjustment() {
        final BigDecimal cashFloat = new BigDecimal(40);
        final BigDecimal newCashFloat = new BigDecimal(100);
        checkClearTillForUnclearedBalance(cashFloat, newCashFloat);
    }

    /**
     * Verifies that an act doesn't get added to a new till balance if it
     * is subsequently saved after the till has been cleared.
     */
    @Test
    public void testClearTillAndReSave() {
        List<FinancialAct> payment = createPayment(till);
        payment.get(0).setStatus(POSTED);
        FinancialAct balance = checkAddToTillBalance(till, payment, false, BigDecimal.ONE);

        // clear the till
        Party account = DepositTestHelper.createDepositAccount();
        rules.clearTill(balance, BigDecimal.ZERO, account);

        // reload the act and save it to force TillRules.addToTill() to run
        // again. However the act should not be added to a new balance as
        // there is an existing actRelationship.tillBalanceItem relationship.
        Act latest = get(payment.get(0));
        save(latest);
        latest = get(latest);
        ActBean bean = new ActBean(latest);
        List<ActRelationship> relationships = bean.getRelationships("actRelationship.tillBalanceItem");
        assertEquals(1, relationships.size());
    }

    /**
     * Tests the {@link TillRules#transfer)} method.
     */
    @Test
    public void testTransfer() {
        List<FinancialAct> payment = createPayment(till);
        payment.get(0).setStatus(POSTED);
        FinancialAct balance = checkAddToTillBalance(till, payment, false, BigDecimal.ONE);

        // make sure using the latest version of the act (i.e, with relationship
        // to balance)
        Act act = get(payment.get(0));

        Party newTill = createTill();
        save(newTill);
        rules.transfer(balance, act, newTill);

        // reload the balance and make sure the payment has been removed
        balance = (FinancialAct) get(balance.getObjectReference());
        assertEquals(0, countRelationships(balance, payment.get(0)));

        // balance should now be zero.
        assertTrue(BigDecimal.ZERO.compareTo(balance.getTotal()) == 0);

        // make sure the payment has been added to a new balance
        FinancialAct newBalance = TillHelper.getUnclearedTillBalance(newTill, getArchetypeService());
        assertNotNull(newBalance);
        assertEquals(1, countRelationships(newBalance, payment.get(0)));
        assertEquals(TillBalanceStatus.UNCLEARED, newBalance.getStatus());

        assertTrue(BigDecimal.ONE.compareTo(newBalance.getTotal()) == 0);
    }

    /**
     * Verifies that {@link TillRules#transfer} throws TillRuleException if
     * invoked for an invalid balance.
     */
    @Test
    public void testTransferWithInvalidBalance() {
        ActBean act = createAct("act.bankDeposit");
        List<FinancialAct> payment = createPayment(till);
        getArchetypeService().save(payment);
        try {
            rules.transfer(act.getAct(), payment.get(0), till);
        } catch (TillRuleException expected) {
            assertEquals(InvalidTillArchetype, expected.getErrorCode());
        }
    }

    /**
     * Verifies that {@link TillRules#transfer} throws TillRuleException if
     * invoked for an invalid act.
     */
    @Test
    public void testTransferWithInvalidAct() {
        Act act = TillHelper.createTillBalance(till, getArchetypeService());
        ActBean payment = createAct("act.bankDeposit");
        try {
            rules.transfer(act, payment.getAct(), till);
        } catch (TillRuleException expected) {
            assertEquals(CantAddActToTill, expected.getErrorCode());
        }
    }

    /**
     * Verifies that {@link TillRules#transfer} throws TillRuleException if
     * invoked with a cleared balance.
     */
    @Test
    public void testTransferWithClearedBalance() {
        Act act = TillHelper.createTillBalance(till, getArchetypeService());
        act.setStatus(TillBalanceStatus.CLEARED);
        List<FinancialAct> payment = createPayment(till);
        payment.get(0).setStatus(POSTED);
        getArchetypeService().save(payment);
        try {
            rules.transfer(act, payment.get(0), createTill());
        } catch (TillRuleException expected) {
            assertEquals(ClearedTill, expected.getErrorCode());
        }
    }

    /**
     * Verifies that {@link TillRules#transfer} throws TillRuleException if
     * there is no relationship between the balance and act to transfer.
     */
    @Test
    public void testTransferWithNoRelationship() {
        Act act = TillHelper.createTillBalance(till, getArchetypeService());
        List<FinancialAct> payment = createPayment(till);
        payment.get(0).setStatus(POSTED);
        getArchetypeService().save(payment);
        try {
            rules.transfer(act, payment.get(0), createTill());
        } catch (TillRuleException expected) {
            assertEquals(MissingRelationship, expected.getErrorCode());
        }
    }

    /**
     * Verifies that {@link TillRules#transfer} throws TillRuleException if
     * the act to be transferred has no till participation.
     */
    @Test
    public void testTransferWithNoParticipation() {
        Act balance = TillHelper.createTillBalance(till, getArchetypeService());
        ActBean balanceBean = new ActBean(balance);
        ActBean payment = createAct("act.customerAccountPayment");
        payment.setStatus(POSTED);
        Party party = TestHelper.createCustomer();
        payment.setParticipant("participation.customer", party);
        balanceBean.addRelationship("actRelationship.tillBalanceItem", payment.getAct());

        try {
            rules.transfer(balance, payment.getAct(), createTill());
        } catch (TillRuleException expected) {
            assertEquals(MissingTill, expected.getErrorCode());
        }
    }

    /**
     * Verifies that {@link TillRules#transfer} throws TillRuleException if
     * an attempt is made to transfer to the same till.
     */
    @Test
    public void testTransferToSameTill() {
        List<FinancialAct> payment = createPayment(till);
        payment.get(0).setStatus(POSTED);
        Act balance = checkAddToTillBalance(till, payment, false, BigDecimal.ONE);

        try {
            rules.transfer(balance, payment.get(0), till);
        } catch (TillRuleException expected) {
            assertEquals(InvalidTransferTill, expected.getErrorCode());
        }
    }

    /**
     * Tests the {@link TillRules#isClearInProgress(Entity)} method.
     */
    @Test
    public void testIsClearInProgress() {
        assertFalse(rules.isClearInProgress(till));

        ActBean balanceBean = createBalance(till, TillBalanceStatus.UNCLEARED);
        balanceBean.save();
        assertFalse(rules.isClearInProgress(till));
        balanceBean.setStatus(TillBalanceStatus.IN_PROGRESS);
        balanceBean.save();
        assertTrue(rules.isClearInProgress(till));

        balanceBean.setStatus(TillBalanceStatus.CLEARED);
        balanceBean.save();
        assertFalse(rules.isClearInProgress(till));
    }

    /**
     * Tests the {@link TillRules#startClearTill(FinancialAct, BigDecimal)} method.
     */
    @Test
    public void testStartClearTill() {
        FinancialAct balance1 = (FinancialAct) createBalance(till, TillBalanceStatus.UNCLEARED).getAct();
        rules.startClearTill(balance1, BigDecimal.ZERO);
        balance1 = get(balance1);
        assertEquals(TillBalanceStatus.IN_PROGRESS, balance1.getStatus());

        List<FinancialAct> payment = createPayment(till);
        payment.get(0).setStatus(POSTED);
        save(payment);

        // make sure the payment went into a new till balance
        FinancialAct balance2 = TillHelper.getUnclearedTillBalance(till, getArchetypeService());
        assertNotNull(balance2);
        assertFalse(balance1.getId() == balance2.getId());
        ActBean bean = new ActBean(balance2);
        assertTrue(bean.hasNodeTarget("items", payment.get(0)));
    }

    /**
     * Tests the {@link TillRules#startClearTill} method folli, with a zero cash float.
     * This should not create an <em>act.tillBalanceAdjustment</e>.
     */
    @Test
    public void testStartClearTillWithNoAdjustment() {
        final BigDecimal cashFloat = BigDecimal.ZERO;
        checkStartClearTill(cashFloat, cashFloat);
    }

    /**
     * Tests the {@link TillRules#startClearTill} method, with an initial cash float
     * of {@code 40.0} and new cash float of {@code 20.0}.
     * This should create a credit adjustment of {@code 20.0}
     */
    @Test
    public void testStartClearTillWithCreditAdjustment() {
        final BigDecimal cashFloat = new BigDecimal(40);
        final BigDecimal newCashFloat = new BigDecimal(20);
        checkStartClearTill(cashFloat, newCashFloat);
    }

    /**
     * Tests the {@link TillRules#startClearTill)} method, with an initial cash float
     * of {@code 40.0} and new cash float of {@code 100.0}.
     * This should create a debit adjustment of {@code 60.0}
     */
    @Test
    public void testStartClearTillWithDebitAdjustment() {
        final BigDecimal cashFloat = new BigDecimal(40);
        final BigDecimal newCashFloat = new BigDecimal(100);
        checkStartClearTill(cashFloat, newCashFloat);
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        till = createTill();
        save(till);
        rules = new TillRules(getArchetypeService(), applicationContext.getBean(PlatformTransactionManager.class));
    }

    /**
     * Checks the behaviour of the {@link TillRules#clearTill(FinancialAct, BigDecimal, Party)} method.
     *
     * @param initialCashFloat the initial cash float value
     * @param newCashFloat     the new cash float value
     */
    private void checkClearTillForUnclearedBalance(BigDecimal initialCashFloat, BigDecimal newCashFloat) {
        setTillCashFloat(initialCashFloat);

        Party account = DepositTestHelper.createDepositAccount();
        ActBean balanceBean = createBalance(till, TillBalanceStatus.UNCLEARED);
        balanceBean.save();
        assertNull(balanceBean.getAct().getActivityEndTime());

        // make sure there is no uncleared deposit for the account
        FinancialAct deposit = DepositHelper.getUndepositedDeposit(account);
        assertNull(deposit);

        // clear the till
        FinancialAct balance = (FinancialAct) balanceBean.getAct();
        rules.clearTill(balance, newCashFloat, account);

        // make sure the balance is updated
        BigDecimal expectedBalance = checkBalance(initialCashFloat, newCashFloat, balance, TillBalanceStatus.CLEARED);
        checkDeposit(account, balanceBean, expectedBalance);
    }

    private void checkDeposit(Party account, ActBean balanceBean, BigDecimal expectedBalance) {
        FinancialAct deposit = DepositHelper.getUndepositedDeposit(account);
        // make sure a new uncleared bank deposit exists, with a relationship to the till balance
        assertNotNull(deposit);
        ActBean depBean = new ActBean(deposit);
        assertNotNull(depBean.getRelationship(balanceBean.getAct()));
        assertTrue(expectedBalance.compareTo(deposit.getTotal()) == 0);
    }

    private void setTillCashFloat(BigDecimal initialCashFloat) {
        IMObjectBean tillBean = new IMObjectBean(till);
        tillBean.setValue("tillFloat", initialCashFloat);
        tillBean.save();
    }

    /**
     * Checks the behaviour of the {@link TillRules#startClearTill(FinancialAct, BigDecimal)} method and
     * {@link TillRules#clearTill(FinancialAct, Party)} methods.
     *
     * @param initialCashFloat the initial cash float value
     * @param newCashFloat     the new cash float value
     */
    private void checkStartClearTill(BigDecimal initialCashFloat, BigDecimal newCashFloat) {
        setTillCashFloat(initialCashFloat);
        Party account = DepositTestHelper.createDepositAccount();

        ActBean balanceBean = createBalance(till, TillBalanceStatus.UNCLEARED);
        balanceBean.save();
        assertNull(balanceBean.getAct().getActivityEndTime());

        // start clearing the till
        FinancialAct balance = (FinancialAct) balanceBean.getAct();
        rules.startClearTill(balance, newCashFloat);

        BigDecimal expectedBalance = checkBalance(initialCashFloat, newCashFloat, balance,
                                                  TillBalanceStatus.IN_PROGRESS);

        rules.clearTill(balance, account);
        checkDeposit(account, balanceBean, expectedBalance);
    }


    private BigDecimal checkBalance(BigDecimal initialCashFloat, BigDecimal newCashFloat, FinancialAct balance,
                                    String status) {
        // make sure the balance is updated
        assertEquals(status, balance.getStatus());
        // end time should be > startTime < now
        Date startTime = balance.getActivityStartTime();
        Date endTime = balance.getActivityEndTime();
        if (TillBalanceStatus.CLEARED.equals(status)) {
            // CLEARED balances have an end time
            assertEquals(1, endTime.compareTo(startTime));
            assertEquals(-1, endTime.compareTo(new Date()));
        } else {
            // IN_PROGRESS balances do not
            assertNull(endTime);
        }

        BigDecimal total = newCashFloat.subtract(initialCashFloat);

        if (initialCashFloat.compareTo(newCashFloat) != 0) {
            // expect a till balance adjustment to have been made
            Set<ActRelationship> rels = balance.getSourceActRelationships();
            assertEquals(1, rels.size());
            ActRelationship r = rels.toArray(new ActRelationship[rels.size()])[0];
            Act target = (Act) get(r.getTarget());
            assertTrue(TypeHelper.isA(target, "act.tillBalanceAdjustment"));
            ActBean adjBean = new ActBean(target);
            BigDecimal amount = adjBean.getBigDecimal("amount");

            boolean credit = (newCashFloat.compareTo(initialCashFloat) < 0);
            BigDecimal adjustmentTotal = total.abs();
            assertTrue(adjustmentTotal.compareTo(amount) == 0);
            assertEquals(credit, adjBean.getBoolean("credit"));
        } else {
            // no till balance adjustment should have been generated
            assertTrue(balance.getSourceActRelationships().isEmpty());
        }

        // check the till balance.
        BigDecimal expectedBalance = total.negate();
        assertTrue(expectedBalance.compareTo(balance.getTotal()) == 0);

        // make sure the till is updated
        Party till = (Party) get(this.till.getObjectReference());
        IMObjectBean bean = new IMObjectBean(till);
        BigDecimal currentFloat = bean.getBigDecimal("tillFloat");
        Date lastCleared = bean.getDate("lastCleared");
        Date now = new Date();

        assertTrue(currentFloat.compareTo(newCashFloat) == 0);
        assertTrue(now.compareTo(lastCleared) == 1); // expect now > lastCleared
        return expectedBalance;
    }

    /**
     * Counts the no. of times an act appears as the target act in a set
     * of act relationships.
     *
     * @param source the source act
     * @param target the target act
     * @return the no. of times {@code target} appears as a target
     */
    private int countRelationships(Act source, Act target) {
        int found = 0;
        for (ActRelationship relationship : source.getSourceActRelationships()) {
            if (relationship.getTarget().equals(target.getObjectReference())) {
                found++;
            }
        }
        return found;
    }

}
