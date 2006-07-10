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

import static org.openvpms.archetype.rules.till.TillRuleException.ErrorCode.*;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.ruleengine.RuleEngineException;

import java.math.BigDecimal;
import java.util.Date;


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
        Act balance1 = createBalance("Uncleared");
        save(balance1);

        // can save the same balance multiple times
        save(balance1);

        Act balance2 = createBalance("Uncleared");
        try {
            save(balance2);
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
            Act balance = createBalance("Cleared");
            save(balance);
        }
    }

    /**
     * Verifies that {@link TillRules#checkCanSaveTillBalance} throws
     * TillRuleException if invoked for an invalid act.<br/>
     */
    public void testCheckCanSaveTillBalanceWithInvalidAct() {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        FinancialAct act = createAct("act.bankDeposit");
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
        FinancialAct payment = createPayment();
        FinancialAct refund = createRefund();

        checkAddToTillBalance(payment, false);
        checkAddToTillBalance(refund, false);

        payment.setStatus("Posted");
        checkAddToTillBalance(payment, false); // payment now updates balance
        checkAddToTillBalance(refund, true);   // refund not added

        refund.setStatus("Posted");
        checkAddToTillBalance(refund, true);   // refund now updates balance

        // verify that subsequent saves only get don't get added to the balance
        // again
        checkAddToTillBalance(payment, true);
        checkAddToTillBalance(refund, true);
    }

    /**
     * Verifies that {@link TillRules#addToTill} throws
     * TillRuleException if invoked for an invalid act.
     */
    public void testAddToTillBalanceWithInvalidAct() {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        FinancialAct act = createAct("act.bankDeposit");
        try {
            TillRules.addToTill(act, service);
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
        FinancialAct act = createAct("act.customerAccountPayment");
        act.setStatus("In Progress");
        Party party = createCustomer();
        TillHelper.addParticipation("participation.customer", act,
                                    party.getObjectReference());
        try {
            TillRules.addToTill(act, service);
        } catch (TillRuleException expected) {
            assertEquals(MissingTill, expected.getErrorCode());
        }
    }

    /**
     * Tests the {@link TillRules#clearTill)} method.
     */
    public void testClearTill() {
        Party account = createAccount();
        FinancialAct balance = createBalance("Uncleared");
        balance.setTotal(new Money(100));
        save(balance);

        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        TillRules.clearTill(balance, new Money(100), account, service);

        assertEquals("Cleared", balance.getStatus());

        Party till = (Party) get(_till.getObjectReference());
        IMObjectBean bean = new IMObjectBean(till);
        BigDecimal tillFloat = bean.getBigDecimal("tillFloat");
        Date lastCleared = bean.getDate("lastCleared");
        Date now = new Date();

        assertTrue(tillFloat.compareTo(new Money(100)) == 0);
        assertTrue(now.compareTo(lastCleared) == 1); // expect now > lastCleared
    }

    /**
     * Tests the {@link TillRules#transfer)} method.
     */
    public void testTransfer() {
        FinancialAct payment = createPayment();
        payment.setStatus("Posted");
        FinancialAct balance = checkAddToTillBalance(payment, false);

        Party newTill = createTill();
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        TillRules.transfer(balance, payment, newTill, service);

        // make sure the payment has been removed from the original balance
        assertEquals(0, countRelationships(balance, payment));

        // make sure the payment has been added to a new balance
        FinancialAct newBalance = TillHelper.getUnclearedTillBalance(
                newTill.getObjectReference());
        assertNotNull(newBalance);
        assertEquals(1, countRelationships(newBalance, payment));
        assertEquals("Uncleared", newBalance.getStatus());
    }

    /**
     * Verifies that {@link TillRules#transfer} throws TillRuleException if
     * invoked for an invalid balance.
     */
    public void testTransferWithInvalidBalance() {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        FinancialAct act = createAct("act.bankDeposit");
        FinancialAct payment = createPayment();
        try {
            TillRules.transfer(act, payment, _till, service);
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
        FinancialAct act = TillHelper.createTillBalance(
                _till.getObjectReference());
        FinancialAct payment = createAct("act.bankDeposit");
        try {
            TillRules.transfer(act, payment, _till, service);
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
        FinancialAct act = TillHelper.createTillBalance(
                _till.getObjectReference());
        act.setStatus("Cleared");
        FinancialAct payment = createPayment();
        payment.setStatus("Posted");
        try {
            TillRules.transfer(act, payment, _till, service);
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
        FinancialAct act = TillHelper.createTillBalance(
                _till.getObjectReference());
        FinancialAct payment = createPayment();
        payment.setStatus("Posted");
        try {
            TillRules.transfer(act, payment, _till, service);
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
        FinancialAct balance = TillHelper.createTillBalance(
                _till.getObjectReference());
        FinancialAct act = createAct("act.customerAccountPayment");
        act.setStatus("Posted");
        Party party = createCustomer();
        TillHelper.addParticipation("participation.customer", act,
                                    party.getObjectReference());
        TillHelper.addRelationship("actRelationship.tillBalanceItem",
                                   balance, act);
        try {
            TillRules.transfer(balance, act, _till, service);
        } catch (TillRuleException expected) {
            assertEquals(MissingTill, expected.getErrorCode());
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
    }

    /**
     * Verifies that {@link TillRules#addToTill} adds acts with status 'Posted'
     * to the associated till balance when its saved, while other statuses are
     * ignored.
     *
     * @param act           the act to save
     * @param balanceExists determines if the balance should exist prior to the
     *                      save
     * @return the balance, if it exists
     */
    private FinancialAct checkAddToTillBalance(FinancialAct act,
                                               boolean balanceExists) {
        boolean posted = "Posted".equals(act.getStatus());
        FinancialAct balance = TillHelper.getUnclearedTillBalance(
                _till.getObjectReference());
        boolean existsPriorToSave = (balance != null);
        if (balanceExists) {
            assertNotNull(balance);
        } else {
            assertNull(balance);
        }
        save(act);

        balance = TillHelper.getUnclearedTillBalance(
                _till.getObjectReference());

        if (posted || existsPriorToSave) {
            assertNotNull(balance);
        } else {
            assertNull(balance);
        }
        if (balance != null) {
            int found = countRelationships(balance, act);
            if (posted) {
                assertTrue("Act not added to till balance", found != 0);
                assertFalse("Act added to till balance more than once",
                            found > 1);
            } else {
                assertTrue("Act without Posted status added to till balance",
                           found == 0);
            }
        }
        return balance;
    }

    /**
     * Helper to create an <em>act.tillBalance</em>.
     *
     * @param status the act status
     * @return a new act
     */
    private FinancialAct createBalance(String status) {
        FinancialAct act = createAct("act.tillBalance");
        act.setStatus(status);
        TillHelper.addTill(act, _till.getObjectReference());
        return act;
    }

    /**
     * Helper to create an <em>act.customerAccountPayment</em>.
     *
     * @return a new act
     */
    private FinancialAct createPayment() {
        FinancialAct act = createAct("act.customerAccountPayment");
        act.setStatus("In Progress");
        Party party = createCustomer();
        TillHelper.addTill(act, _till.getObjectReference());
        TillHelper.addParticipation("participation.customer", act,
                                    party.getObjectReference());
        return act;
    }

    /**
     * Helper to create an <em>act.customerAccountRefund</em>.
     *
     * @return a new act
     */
    private FinancialAct createRefund() {
        FinancialAct act = createAct("act.customerAccountRefund");
        act.setStatus("In Progress");
        Party party = createCustomer();
        TillHelper.addTill(act, _till.getObjectReference());
        TillHelper.addParticipation("participation.customer", act,
                                    party.getObjectReference());
        return act;
    }

    /**
     * Helper to create a new act.
     *
     * @param shortName the act short name
     * @return a new act
     */
    private FinancialAct createAct(String shortName) {
        FinancialAct act = (FinancialAct) create(shortName);
        assertNotNull(act);
        return act;
    }

    /**
     * Creates and saves a new till.
     *
     * @return the new till
     */
    private Party createTill() {
        Party till = (Party) create("party.organisationTill");
        assertNotNull(till);
        till.setName("TillRulesTestCase-Till" + hashCode());
        save(till);
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
