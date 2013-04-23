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
import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.finance.deposit.DepositHelper;
import org.openvpms.archetype.rules.finance.deposit.DepositTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.business.service.ruleengine.RuleEngineException;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.openvpms.archetype.rules.act.ActStatus.IN_PROGRESS;
import static org.openvpms.archetype.rules.act.ActStatus.POSTED;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.CantAddActToTill;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.ClearedTill;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.InvalidTillArchetype;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.InvalidTransferTill;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.MissingRelationship;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.MissingTill;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.UnclearedTillExists;
import static org.openvpms.archetype.test.TestHelper.createTill;


/**
 * Tests the {@link TillRules} class when invoked by the
 * <em>archetypeService.save.act.tillBalance.before</em>,
 * <em>archetypeService.save.act.customerAccountPayment.after</em> and
 * <em>archetypeService.save.act.customerAccountRefund.after</em> rules.
 * In order for these tests to be successful, the archetype service
 * must be configured to trigger the above rules.
 *
 * @author Tim Anderson
 */
public class TillRulesTestCase extends ArchetypeServiceTest {

    /**
     * The till.
     */
    private Party till;

    /**
     * The till business rules.
     */
    private TillRules rules;

    /**
     * The archetype service.
     */
    private IArchetypeService service;


    /**
     * Verifies that an <em>act.tillBalance</em> with 'Uncleared' status
     * can only be saved if there are no other uncleared till balances for
     * the same till.<br/>
     * Requires the rule <em>archetypeService.save.act.tillBalance.before</em>.
     */
    @Test
    public void testSaveUnclearedTillBalance() {
        ActBean balance1 = createBalance(TillBalanceStatus.UNCLEARED);
        balance1.save();

        // can save the same balance multiple times
        balance1.save();

        ActBean balance2 = createBalance(TillBalanceStatus.UNCLEARED);
        try {
            balance2.save();
            fail("Expected save of second uncleared till balance to fail");
        } catch (RuleEngineException expected) {
            Throwable cause = expected.getCause();
            while (cause != null && !(cause instanceof TillRuleException)) {
                cause = cause.getCause();
            }
            assertNotNull(cause);
            TillRuleException exception = (TillRuleException) cause;
            assertEquals(UnclearedTillExists, exception.getErrorCode());
        }
    }

    /**
     * Verifies that multiple <em>act.tillBalance</em> with 'Cleared' status can
     * be saved for the same till.<br/>
     * Requires the rule <em>archetypeService.save.act.tillBalance.before</em>.
     */
    @Test
    public void testSaveClearedTillBalance() {
        for (int i = 0; i < 3; ++i) {
            ActBean balance = createBalance(TillBalanceStatus.CLEARED);
            balance.save();
        }
    }

    /**
     * Verifies that {@link TillRules#checkCanSaveTillBalance} throws
     * TillRuleException if invoked for an invalid act.<br/>
     */
    @Test
    public void testCheckCanSaveTillBalanceWithInvalidAct() {
        ActBean bean = createAct("act.bankDeposit");
        FinancialAct act = (FinancialAct) bean.getAct();
        try {
            rules.checkCanSaveTillBalance(act);
        } catch (TillRuleException expected) {
            assertEquals(InvalidTillArchetype, expected.getErrorCode());
        }
    }

    /**
     * Verifies that {@link TillRules#addToTill} adds posted
     * <em>act.customerAccountPayment</em> and
     * <em>act.customerAccountRefund</em> to the associated till balance
     * when they are saved.
     * Requires the rules <em>archetypeServicfe.save.act.customerAccountPayment.after</em> and
     * <em>archetypeServicfe.save.act.customerAccountRefund.after</em>
     */
    @Test
    public void testAddToTillBalance() {
        List<FinancialAct> payment = createPayment();
        List<FinancialAct> refund = createRefund();

        checkAddToTillBalance(payment, false, BigDecimal.ZERO);
        checkAddToTillBalance(refund, false, BigDecimal.ZERO);

        payment.get(0).setStatus(POSTED);
        checkAddToTillBalance(payment, false, BigDecimal.ONE);
        // payment now updates balance
        checkAddToTillBalance(refund, true, BigDecimal.ONE);
        // refund not added

        refund.get(0).setStatus(POSTED);
        checkAddToTillBalance(refund, true, BigDecimal.ZERO);
        // refund now updates balance

        // verify that subsequent saves only get don't get added to the balance
        // again
        checkAddToTillBalance(payment, true, BigDecimal.ZERO);
        checkAddToTillBalance(refund, true, BigDecimal.ZERO);

        List<FinancialAct> payment2 = createPayment();
        payment2.get(0).setStatus(POSTED);
        checkAddToTillBalance(payment2, true, BigDecimal.ONE);
    }

    /**
     * Verifies that {@link TillRules#addToTill} throws
     * TillRuleException if invoked for an invalid act.
     */
    @Test
    public void testAddToTillBalanceWithInvalidAct() {
        ActBean bean = createAct("act.bankDeposit");
        try {
            rules.addToTill(bean.getAct());
        } catch (TillRuleException expected) {
            assertEquals(CantAddActToTill, expected.getErrorCode());
        }
    }

    /**
     * Verifies that {@link TillRules#addToTill} throws
     * TillRuleException if invoked for an act with no till.
     */
    @Test
    public void testAddToTillBalanceWithNoTill() {
        ActBean act = createAct("act.customerAccountPayment");
        act.setStatus(IN_PROGRESS);
        Party party = TestHelper.createCustomer();
        act.setParticipant("participation.customer", party);
        try {
            rules.addToTill(act.getAct());
        } catch (TillRuleException expected) {
            assertEquals(MissingTill, expected.getErrorCode());
        }
    }

    /**
     * Tests the {@link TillRules#clearTill)} method, with a zero cash float.
     * This should not create an <em>act.tillBalanceAdjustment</e>.
     */
    @Test
    public void testClearTillWithNoAdjustment() {
        final BigDecimal cashFloat = BigDecimal.ZERO;
        checkClearTill(cashFloat, cashFloat);
    }

    /**
     * Tests the {@link TillRules#clearTill)} method, with an initial cash float
     * of <code>40.0</code> and new cash float of <code>20.0</code>.
     * This should create a credit adjustment of <code>20.0</code>
     */
    @Test
    public void testClearTillWithCreditAdjustment() {
        final BigDecimal cashFloat = new BigDecimal(40);
        final BigDecimal newCashFloat = new BigDecimal(20);
        checkClearTill(cashFloat, newCashFloat);
    }

    /**
     * Tests the {@link TillRules#clearTill)} method, with an initial cash float
     * of <code>40.0</code> and new cash float of <code>100.0</code>.
     * This should create a debit adjustment of <code>60.0</code>
     */
    @Test
    public void testClearTillWithDebitAdjustment() {
        final BigDecimal cashFloat = new BigDecimal(40);
        final BigDecimal newCashFloat = new BigDecimal(100);
        checkClearTill(cashFloat, newCashFloat);
    }

    /**
     * Verifies that an act doesn't get added to a new till balance if it
     * is subsequently saved after the till has been cleared.
     */
    @Test
    public void testClearTillAndReSave() {
        List<FinancialAct> payment = createPayment();
        payment.get(0).setStatus(POSTED);
        FinancialAct balance = checkAddToTillBalance(payment, false,
                                                     BigDecimal.ONE);

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
        List<ActRelationship> relationships = bean.getRelationships(
                "actRelationship.tillBalanceItem");
        assertEquals(1, relationships.size());
    }

    /**
     * Tests the {@link TillRules#transfer)} method.
     */
    @Test
    public void testTransfer() {
        List<FinancialAct> payment = createPayment();
        payment.get(0).setStatus(POSTED);
        FinancialAct balance = checkAddToTillBalance(payment, false, BigDecimal.ONE);

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
        FinancialAct newBalance
                = rules.getUnclearedTillBalance(newTill.getObjectReference());
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
        List<FinancialAct> payment = createPayment();
        service.save(payment);
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
        Act act = rules.createTillBalance(till);
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
        Act act = rules.createTillBalance(till);
        act.setStatus(TillBalanceStatus.CLEARED);
        List<FinancialAct> payment = createPayment();
        payment.get(0).setStatus(POSTED);
        service.save(payment);
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
        Act act = rules.createTillBalance(till);
        List<FinancialAct> payment = createPayment();
        payment.get(0).setStatus(POSTED);
        service.save(payment);
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
        Act balance = rules.createTillBalance(till);
        ActBean balanceBean = new ActBean(balance);
        ActBean payment = createAct("act.customerAccountPayment");
        payment.setStatus(POSTED);
        Party party = TestHelper.createCustomer();
        payment.setParticipant("participation.customer", party);
        balanceBean.addRelationship("actRelationship.tillBalanceItem",
                                    payment.getAct());

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
        List<FinancialAct> payment = createPayment();
        payment.get(0).setStatus(POSTED);
        Act balance = checkAddToTillBalance(payment, false, BigDecimal.ONE);

        try {
            rules.transfer(balance, payment.get(0), till);
        } catch (TillRuleException expected) {
            assertEquals(InvalidTransferTill, expected.getErrorCode());
        }
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        till = createTill();
        save(till);
        rules = new TillRules();
        service = ArchetypeServiceHelper.getArchetypeService();
    }

    /**
     * Verifies that {@link TillRules#addToTill} adds acts with status 'Posted'
     * to the associated till balance when its saved, while other statuses are
     * ignored.
     *
     * @param acts          the acts to save
     * @param balanceExists determines if the balance should exist prior to the
     *                      save
     * @param expectedTotal the expected total balance
     * @return the balance, if it exists
     */
    private FinancialAct checkAddToTillBalance(final List<FinancialAct> acts,
                                               boolean balanceExists,
                                               BigDecimal expectedTotal) {
        FinancialAct act = acts.get(0);
        boolean posted = POSTED.equals(act.getStatus());
        FinancialAct balance = rules.getUnclearedTillBalance(
                till.getObjectReference());
        boolean existsPriorToSave = (balance != null);
        if (balanceExists) {
            assertNotNull(balance);
        } else {
            assertNull(balance);
        }
        service.save(acts);
        balance = rules.getUnclearedTillBalance(till.getObjectReference());

        if (posted || existsPriorToSave) {
            assertNotNull(balance);
        } else {
            assertNull(balance);
        }
        if (balance != null) {
            Act latest = get(acts.get(0));
            int found = countRelationships(balance, latest);
            if (posted) {
                assertTrue("Act not added to till balance", found != 0);
                assertFalse("Act added to till balance more than once",
                            found > 1);
            } else {
                assertTrue("Act without Posted status added to till balance",
                           found == 0);
            }
            BigDecimal total = balance.getTotal();
            assertTrue(expectedTotal.compareTo(total) == 0);
        }
        return balance;
    }

    /**
     * Checks the behaviour of the {@link TillRules#clearTill} method.
     *
     * @param initialCashFloat the initial cash float value
     * @param newCashFloat     the new cash float value
     */
    private void checkClearTill(BigDecimal initialCashFloat,
                                BigDecimal newCashFloat) {

        IMObjectBean tillBean = new IMObjectBean(till);
        tillBean.setValue("tillFloat", initialCashFloat);
        tillBean.save();

        Party account = DepositTestHelper.createDepositAccount();
        ActBean balanceBean = createBalance(TillBalanceStatus.UNCLEARED);
        balanceBean.save();
        assertNull(balanceBean.getAct().getActivityEndTime());

        // make sure there is no uncleared deposit for the accouunt
        FinancialAct deposit = DepositHelper.getUndepositedDeposit(account);
        assertNull(deposit);

        // clear the till
        FinancialAct balance = (FinancialAct) balanceBean.getAct();
        balance = rules.clearTill(balance, newCashFloat, account);

        // make sure the balance is updated
        assertEquals(TillBalanceStatus.CLEARED, balance.getStatus());
        // end time should be > startTime < now
        Date startTime = balance.getActivityStartTime();
        Date endTime = balance.getActivityEndTime();
        assertEquals(1, endTime.compareTo(startTime));
        assertEquals(-1, endTime.compareTo(new Date()));

        BigDecimal total = newCashFloat.subtract(initialCashFloat);

        if (initialCashFloat.compareTo(newCashFloat) != 0) {
            // expect a till balance adjustment to have been made
            Set<ActRelationship> rels
                    = balance.getSourceActRelationships();
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

        // make sure a new uncleared bank deposit exists, with a relationship
        // to the till balance
        deposit = DepositHelper.getUndepositedDeposit(account);
        assertNotNull(deposit);
        ActBean depBean = new ActBean(deposit);
        assertNotNull(depBean.getRelationship(balanceBean.getAct()));
        assertTrue(expectedBalance.compareTo(deposit.getTotal()) == 0);
    }

    /**
     * Helper to create an <em>act.tillBalance</em> wrapped in a bean.
     *
     * @param status the act status
     * @return a new act
     */
    private ActBean createBalance(String status) {
        ActBean act = createAct("act.tillBalance");
        act.setStatus(status);
        act.setParticipant("participation.till", till);
        return act;
    }

    /**
     * Helper to create a new <em>act.customerAccountPayment</em> and
     * corresponding <em>act.customerAccountPaymentCash</em>, with a total
     * value of 1.0.
     *
     * @return a list containing the payment act and its item
     */
    private List<FinancialAct> createPayment() {
        Party party = TestHelper.createCustomer();
        return FinancialTestHelper.createPayment(
                Money.ONE, party, till, FinancialActStatus.IN_PROGRESS);
    }

    /**
     * Helper to create a new <em>act.customerAccountRefund</em> and
     * corresponding <em>act.customerAccountRefundCash</em>, with a total
     * value of 1.0.
     *
     * @return a new act
     */
    private List<FinancialAct> createRefund() {
        Party party = TestHelper.createCustomer();
        return FinancialTestHelper.createRefund(Money.ONE, party, till,
                                                FinancialActStatus.IN_PROGRESS);
    }

    /**
     * Helper to create a new act, wrapped in a bean.
     *
     * @param shortName the act short name
     * @return a new act wrapped in a bean
     */
    private ActBean createAct(String shortName) {
        Act act = (Act) create(shortName);
        assertNotNull(act);
        return new ActBean(act);
    }

    /**
     * Counts the no. of times an act appears as the target act in a set
     * of act relationships.
     *
     * @param source the source act
     * @param target the target act
     * @return the no. of times <code>target</code> appears as a target
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
