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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.till;

import static org.openvpms.archetype.rules.act.ActStatus.IN_PROGRESS;
import static org.openvpms.archetype.rules.act.ActStatus.POSTED;
import org.openvpms.archetype.rules.deposit.DepositHelper;
import static org.openvpms.archetype.rules.till.TillRuleException.ErrorCode.*;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.business.service.ruleengine.RuleEngineException;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;


/**
 * Tests the {@link TillRules} class when invoked by the
 * <em>archetypeService.save.act.tillBalance.before</em>,
 * <em>archetypeService.save.act.customerAccountPayment.after</em> and
 * <em>archetypeService.save.act.customerAccountRefund.after</em> rules.
 * In order for these tests to be successful, the archetype service
 * must be configured to trigger the above rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
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
     * Verifies that an <em>act.tillBalance</em> with 'Uncleared' status
     * can only be saved if there are no other uncleared till balances for
     * the same till.<br/>
     * Requires the rule <em>archetypeService.save.act.tillBalance.before</em>.
     */
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
            assertTrue(cause instanceof TillRuleException);
            TillRuleException exception = (TillRuleException) cause;
            assertEquals(UnclearedTillExists, exception.getErrorCode());
        }
    }

    /**
     * Verifies that multiple <em>act.tillBalance</em> with 'Cleared' status can
     * be saved for the same till.<br/>
     * Requires the rule <em>archetypeService.save.act.tillBalance.before</em>.
     */
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
    public void testAddToTillBalance() {
        ActBean payment = createPayment();
        ActBean refund = createRefund();

        checkAddToTillBalance(payment, false, BigDecimal.ZERO);
        checkAddToTillBalance(refund, false, BigDecimal.ZERO);

        payment.setStatus(POSTED);
        checkAddToTillBalance(payment, false, BigDecimal.ONE);
        // payment now updates balance
        checkAddToTillBalance(refund, true, BigDecimal.ONE);
        // refund not added

        refund.setStatus(POSTED);
        checkAddToTillBalance(refund, true, BigDecimal.ZERO);
        // refund now updates balance

        // verify that subsequent saves only get don't get added to the balance
        // again
        checkAddToTillBalance(payment, true, BigDecimal.ZERO);
        checkAddToTillBalance(refund, true, BigDecimal.ZERO);
    }

    /**
     * Verifies that {@link TillRules#addToTill} throws
     * TillRuleException if invoked for an invalid act.
     */
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
    public void testAddToTillBalanceWithNoTill() {
        ActBean act = createAct("act.customerAccountPayment");
        act.setStatus(IN_PROGRESS);
        Party party = createCustomer();
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
    public void testClearTillWithNoAdjustment() {
        final BigDecimal cashFloat = BigDecimal.ZERO;
        checkClearTill(cashFloat, cashFloat);
    }

    /**
     * Tests the {@link TillRules#clearTill)} method, with an initial cash float
     * of <code>40.0</code> and new cash float of <code>20.0</code>.
     * This should create a credit adjustment of <code>20.0</code>
     */
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
    public void testClearTillWithDebitAdjustment() {
        final BigDecimal cashFloat = new BigDecimal(40);
        final BigDecimal newCashFloat = new BigDecimal(100);
        checkClearTill(cashFloat, newCashFloat);
    }

    /**
     * Tests the {@link TillRules#transfer)} method.
     */
    public void testTransfer() {
        ActBean payment = createPayment();
        payment.setStatus(POSTED);
        FinancialAct balance = checkAddToTillBalance(payment, false,
                                                     BigDecimal.ONE);

        // make sure using the latest version of the act (i.e, with relationship
        // to balance)
        Act act = (Act) get(payment.getAct().getObjectReference());

        Party newTill = createTill();
        save(newTill);
        rules.transfer(balance, act, newTill);

        // reload the balance and make sure the payment has been removed
        balance = (FinancialAct) get(balance.getObjectReference());
        assertEquals(0, countRelationships(balance, payment.getAct()));

        // balance should now be zero.
        assertTrue(BigDecimal.ZERO.compareTo(balance.getTotal()) == 0);

        // make sure the payment has been added to a new balance
        FinancialAct newBalance
                = rules.getUnclearedTillBalance(newTill.getObjectReference());
        assertNotNull(newBalance);
        assertEquals(1, countRelationships(newBalance, payment.getAct()));
        assertEquals(TillBalanceStatus.UNCLEARED, newBalance.getStatus());

        assertTrue(BigDecimal.ONE.compareTo(newBalance.getTotal()) == 0);
    }

    /**
     * Verifies that {@link TillRules#transfer} throws TillRuleException if
     * invoked for an invalid balance.
     */
    public void testTransferWithInvalidBalance() {
        ActBean act = createAct("act.bankDeposit");
        ActBean payment = createPayment();
        try {
            rules.transfer(act.getAct(), payment.getAct(), till);
        } catch (TillRuleException expected) {
            assertEquals(InvalidTillArchetype, expected.getErrorCode());
        }
    }

    /**
     * Verifies that {@link TillRules#transfer} throws TillRuleException if
     * invoked for an invalid act.
     */
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
    public void testTransferWithClearedBalance() {
        Act act = rules.createTillBalance(till);
        act.setStatus(TillBalanceStatus.CLEARED);
        ActBean payment = createPayment();
        payment.setStatus(POSTED);
        try {
            rules.transfer(act, payment.getAct(), createTill());
        } catch (TillRuleException expected) {
            assertEquals(ClearedTill, expected.getErrorCode());
        }
    }

    /**
     * Verifies that {@link TillRules#transfer} throws TillRuleException if
     * there is no relationship between the balance and act to transfer.
     */
    public void testTransferWithNoRelationship() {
        Act act = rules.createTillBalance(till);
        ActBean payment = createPayment();
        payment.setStatus(POSTED);
        try {
            rules.transfer(act, payment.getAct(), createTill());
        } catch (TillRuleException expected) {
            assertEquals(MissingRelationship, expected.getErrorCode());
        }
    }

    /**
     * Verifies that {@link TillRules#transfer} throws TillRuleException if
     * the act to be transferred has no till participation.
     */
    public void testTransferWithNoParticipation() {
        Act balance = rules.createTillBalance(till);
        ActBean balanceBean = new ActBean(balance);
        ActBean payment = createAct("act.customerAccountPayment");
        payment.setStatus(POSTED);
        Party party = createCustomer();
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
    public void testTransferToSameTill() {
        ActBean payment = createPayment();
        payment.setStatus(POSTED);
        Act balance = checkAddToTillBalance(payment, false, BigDecimal.ONE);

        try {
            rules.transfer(balance, payment.getAct(), till);
        } catch (TillRuleException expected) {
            assertEquals(InvalidTransferTill, expected.getErrorCode());
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
        till = createTill();
        save(till);
        rules = new TillRules();
    }

    /**
     * Verifies that {@link TillRules#addToTill} adds acts with status 'Posted'
     * to the associated till balance when its saved, while other statuses are
     * ignored.
     *
     * @param act           the act to save
     * @param balanceExists determines if the balance should exist prior to the
     *                      save
     * @param expectedTotal the expected total balance
     * @return the balance, if it exists
     */
    private FinancialAct checkAddToTillBalance(ActBean act,
                                               boolean balanceExists,
                                               BigDecimal expectedTotal) {
        boolean posted = POSTED.equals(act.getStatus());
        FinancialAct balance = rules.getUnclearedTillBalance(
                till.getObjectReference());
        boolean existsPriorToSave = (balance != null);
        if (balanceExists) {
            assertNotNull(balance);
        } else {
            assertNull(balance);
        }
        act.save();

        balance = rules.getUnclearedTillBalance(till.getObjectReference());

        if (posted || existsPriorToSave) {
            assertNotNull(balance);
        } else {
            assertNull(balance);
        }
        if (balance != null) {
            int found = countRelationships(balance, act.getAct());
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

        Party account = createAccount();
        ActBean balanceBean = createBalance(TillBalanceStatus.UNCLEARED);
        balanceBean.save();

        // make sure there is no uncleared deposit for the accouunt
        FinancialAct deposit = DepositHelper.getUndepositedDeposit(account);
        assertNull(deposit);

        // clear the till
        FinancialAct balance = (FinancialAct) balanceBean.getAct();
        balance = rules.clearTill(balance, newCashFloat, account);

        // make sure the balance is updated
        assertEquals(TillBalanceStatus.CLEARED, balance.getStatus());

        BigDecimal total = newCashFloat.subtract(initialCashFloat);

        if (initialCashFloat.compareTo(newCashFloat) != 0) {
            // expect a till balance adjustment to have been made
            Set<ActRelationship> rels
                    = balance.getSourceActRelationships();
            assertEquals(1, rels.size());
            ActRelationship r = rels.toArray(new ActRelationship[0])[0];
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
     * Helper to create an <em>act.customerAccountPayment</em> with an amount
     * value of 1.0, wrapped in a bean.
     *
     * @return a new act
     */
    private ActBean createPayment() {
        ActBean act = createAct("act.customerAccountPayment");
        act.setStatus(IN_PROGRESS);
        act.setValue("amount", new BigDecimal("1.0"));
        Party party = createCustomer();
        act.setParticipant("participation.till", till);
        act.setParticipant("participation.customer", party);
        return act;
    }

    /**
     * Helper to create an <em>act.customerAccountRefund</em> with an amount
     * value of 1.0, wrapped in a bean.
     *
     * @return a new act
     */
    private ActBean createRefund() {
        ActBean act = createAct("act.customerAccountRefund");
        act.setStatus(IN_PROGRESS);
        act.setValue("amount", new BigDecimal("1.0"));
        Party party = createCustomer();
        act.setParticipant("participation.till", till);
        act.setParticipant("participation.customer", party);
        return act;
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
     * Creates a new till.
     *
     * @return the new till
     */
    private Party createTill() {
        Party till = (Party) create("party.organisationTill");
        assertNotNull(till);
        till.setName("TillRulesTestCase-Till" + hashCode());
        return till;
    }

    /**
     * Creates and saves a new deposit account.
     *
     * @return a new account
     */
    private Party createAccount() {
        Party account = (Party) create("party.organisationDeposit");
        assertNotNull(account);
        account.setName("TillRulesTestCase-Account" + hashCode());
        IMObjectBean bean = new IMObjectBean(account);
        bean.setValue("bank", "Westpac");
        bean.setValue("branch", "Eltham");
        bean.setValue("accountNumber", "123-456-789");
        bean.setValue("accountName", "Foo");
        save(account);
        return account;
    }

    /**
     * Creates a new customer.
     *
     * @return a new customer
     */
    private Party createCustomer() {
        Party party = (Party) create("party.customerperson");
        assertNotNull(party);
        return party;
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
        for (ActRelationship relationship : source.getSourceActRelationships())
        {
            if (relationship.getTarget().equals(target.getObjectReference())) {
                found++;
            }
        }
        return found;
    }

}
