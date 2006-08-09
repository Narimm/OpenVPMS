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

import org.openvpms.archetype.rules.deposit.DepositHelper;
import static org.openvpms.archetype.rules.till.TillRuleException.ErrorCode.*;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
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
    private Party _till;


    /**
     * Verifies that an <em>act.tillBalance</em> with 'Uncleared' status
     * can only be saved if there are no other uncleared till balances for
     * the same till.<br/>
     * Requires the rule <em>archetypeService.save.act.tillBalance.before</em>.
     */
    public void testSaveUnclearedTillBalance() {
        ActBean balance1 = createBalance("Uncleared");
        balance1.save();

        // can save the same balance multiple times
        balance1.save();

        ActBean balance2 = createBalance("Uncleared");
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
            ActBean balance = createBalance("Cleared");
            balance.save();
        }
    }

    /**
     * Verifies that {@link TillRules#checkCanSaveTillBalance} throws
     * TillRuleException if invoked for an invalid act.<br/>
     */
    public void testCheckCanSaveTillBalanceWithInvalidAct() {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        ActBean bean = createAct("act.bankDeposit");
        FinancialAct act = (FinancialAct) bean.getAct();
        try {
            TillRules.checkCanSaveTillBalance(act, service);
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

        payment.setStatus("Posted");
        checkAddToTillBalance(payment, false, BigDecimal.ONE);
        // payment now updates balance
        checkAddToTillBalance(refund, true, BigDecimal.ONE);
        // refund not added

        refund.setStatus("Posted");
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
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        ActBean bean = createAct("act.bankDeposit");
        try {
            TillRules.addToTill(bean.getAct(), service);
        } catch (TillRuleException expected) {
            assertEquals(CantAddActToTill, expected.getErrorCode());
        }
    }

    /**
     * Verifies that {@link TillRules#addToTill} throws
     * TillRuleException if invoked for an act with no till.
     */
    public void testAddToTillBalanceWithNoTill() {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        ActBean act = createAct("act.customerAccountPayment");
        act.setStatus("In Progress");
        Party party = createCustomer();
        act.setParticipant("participation.customer", party);
        try {
            TillRules.addToTill(act.getAct(), service);
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
        payment.setStatus("Posted");
        FinancialAct balance = checkAddToTillBalance(payment, false,
                                                     BigDecimal.ONE);

        // make sure using the latest version of the act (i.e, with relationship
        // to balance)
        Act act = (Act) get(payment.getAct().getObjectReference());

        Party newTill = createTill();
        save(newTill);
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        TillRules.transfer(balance, act, newTill, service);

        // reload the balance and make sure the payment has been removed
        balance = (FinancialAct) get(balance.getObjectReference());
        assertEquals(0, countRelationships(balance, payment.getAct()));

        // balance should now be zero.
        assertTrue(BigDecimal.ZERO.compareTo(balance.getTotal()) == 0);

        // make sure the payment has been added to a new balance
        FinancialAct newBalance = TillHelper.getUnclearedTillBalance(
                newTill.getObjectReference());
        assertNotNull(newBalance);
        assertEquals(1, countRelationships(newBalance, payment.getAct()));
        assertEquals("Uncleared", newBalance.getStatus());

        assertTrue(BigDecimal.ONE.compareTo(newBalance.getTotal()) == 0);
    }

    /**
     * Verifies that {@link TillRules#transfer} throws TillRuleException if
     * invoked for an invalid balance.
     */
    public void testTransferWithInvalidBalance() {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        ActBean act = createAct("act.bankDeposit");
        ActBean payment = createPayment();
        try {
            TillRules.transfer(act.getAct(), payment.getAct(), _till, service);
        } catch (TillRuleException expected) {
            assertEquals(InvalidTillArchetype, expected.getErrorCode());
        }
    }

    /**
     * Verifies that {@link TillRules#transfer} throws TillRuleException if
     * invoked for an invalid act.
     */
    public void testTransferWithInvalidAct() {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        Act act = TillHelper.createTillBalance(
                _till.getObjectReference());
        ActBean payment = createAct("act.bankDeposit");
        try {
            TillRules.transfer(act, payment.getAct(), _till, service);
        } catch (TillRuleException expected) {
            assertEquals(CantAddActToTill, expected.getErrorCode());
        }
    }

    /**
     * Verifies that {@link TillRules#transfer} throws TillRuleException if
     * invoked with a cleared balance.
     */
    public void testTransferWithClearedBalance() {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        Act act = TillHelper.createTillBalance(_till.getObjectReference());
        act.setStatus("Cleared");
        ActBean payment = createPayment();
        payment.setStatus("Posted");
        try {
            TillRules.transfer(act, payment.getAct(), createTill(), service);
        } catch (TillRuleException expected) {
            assertEquals(ClearedTill, expected.getErrorCode());
        }
    }

    /**
     * Verifies that {@link TillRules#transfer} throws TillRuleException if
     * there is no relationship between the balance and act to transfer.
     */
    public void testTransferWithNoRelationship() {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        Act act = TillHelper.createTillBalance(_till.getObjectReference());
        ActBean payment = createPayment();
        payment.setStatus("Posted");
        try {
            TillRules.transfer(act, payment.getAct(), createTill(), service);
        } catch (TillRuleException expected) {
            assertEquals(MissingRelationship, expected.getErrorCode());
        }
    }

    /**
     * Verifies that {@link TillRules#transfer} throws TillRuleException if
     * the act to be transferred has no till participation.
     */
    public void testTransferWithNoParticipation() {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        Act balance = TillHelper.createTillBalance(_till.getObjectReference());
        ActBean balanceBean = new ActBean(balance);
        ActBean payment = createAct("act.customerAccountPayment");
        payment.setStatus("Posted");
        Party party = createCustomer();
        payment.setParticipant("participation.customer", party);
        balanceBean.addRelationship("actRelationship.tillBalanceItem",
                                    payment.getAct());

        try {
            TillRules.transfer(balance, payment.getAct(), createTill(),
                               service);
        } catch (TillRuleException expected) {
            assertEquals(MissingTill, expected.getErrorCode());
        }
    }

    /**
     * Verifies that {@link TillRules#transfer} throws TillRuleException if
     * an attempt is made to transfer to the same till.
     */
    public void testTransferToSameTill() {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        ActBean payment = createPayment();
        payment.setStatus("Posted");
        Act balance = checkAddToTillBalance(payment, false, BigDecimal.ONE);

        try {
            TillRules.transfer(balance, payment.getAct(), _till, service);
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
        _till = createTill();
        save(_till);
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
        boolean posted = "Posted".equals(act.getStatus());
        FinancialAct balance = TillHelper.getUnclearedTillBalance(
                _till.getObjectReference());
        boolean existsPriorToSave = (balance != null);
        if (balanceExists) {
            assertNotNull(balance);
        } else {
            assertNull(balance);
        }
        act.save();

        balance = TillHelper.getUnclearedTillBalance(
                _till.getObjectReference());

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

        IMObjectBean tillBean = new IMObjectBean(_till);
        tillBean.setValue("tillFloat", initialCashFloat);
        tillBean.save();

        Party account = createAccount();
        ActBean balanceBean = createBalance("Uncleared");
        balanceBean.save();

        // make sure there is no uncleared deposit for the accouunt
        FinancialAct deposit = DepositHelper.getUndepositedDeposit(account);
        assertNull(deposit);

        // clear the till
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        FinancialAct balance = (FinancialAct) balanceBean.getAct();
        balance = TillRules.clearTill(balance, newCashFloat, account, service);

        // make sure the balance is updated
        assertEquals("Cleared", balance.getStatus());

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
        Party till = (Party) get(_till.getObjectReference());
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
        act.setParticipant("participation.till", _till);
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
        act.setStatus("In Progress");
        act.setValue("amount", new BigDecimal("1.0"));
        Party party = createCustomer();
        act.setParticipant("participation.till", _till);
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
        act.setStatus("In Progress");
        act.setValue("amount", new BigDecimal("1.0"));
        Party party = createCustomer();
        act.setParticipant("participation.till", _till);
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
