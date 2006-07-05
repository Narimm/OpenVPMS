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

import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.ruleengine.RuleEngineException;


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
            assertEquals(TillRuleException.ErrorCode.UnclearedTillExists,
                         exception.getErrorCode());
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
            assertEquals(TillRuleException.ErrorCode.InvalidTillArchetype,
                         expected.getErrorCode());
        }
    }

    /**
     * Verifies that {@link TillRules#addToTill} adds
     * <em>act.customerAccountPayment</em> and
     * <em>act.customerAccountRefund</em>  to the associated till balance
     * when they are saved.
     * Requires the rules <em>archetypeServicfe.save.act.customerAccountPayment.after</em> and
     * <em>archetypeServicfe.save.act.customerAccountRefund.after</em>
     */
    public void testAddToTillBalance() {
        FinancialAct payment = createPayment();
        FinancialAct refund = createRefund();
        checkAddToTillBalance(payment, false);
        checkAddToTillBalance(refund, true);

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
            assertEquals(TillRuleException.ErrorCode.CantAddActToTill,
                         expected.getErrorCode());
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
     * Verifies that {@link TillRules#addToTill} adds an act to the associated
     * till balance when its saved.
     *
     * @param act           the act to save
     * @param balanceExists determines if the balance should exist prior to the
     *                      save
     */
    private void checkAddToTillBalance(FinancialAct act,
                                       boolean balanceExists) {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        FinancialAct balance = TillRules.getUnclearedTillBalance(
                _till.getObjectReference(), service);
        if (balanceExists) {
            assertNotNull(balance);
        } else {
            assertNull(balance);
        }
        save(act);

        balance = TillRules.getUnclearedTillBalance(
                _till.getObjectReference(), service);
        assertNotNull(balance);
        int found = 0;
        for (ActRelationship relationship :
                balance.getSourceActRelationships()) {
            if (relationship.getTarget().equals(act.getObjectReference())) {
                found++;
            }
        }
        assertTrue("Act not added to till balance", found != 0);
        assertFalse("Act added to till balance more than once", found > 1);
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
        addParticipation(act, _till, "participation.till");
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
        addParticipation(act, _till, "participation.till");
        addParticipation(act, party, "participation.customer");
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
        addParticipation(act, _till, "participation.till");
        addParticipation(act, party, "participation.customer");
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
     * Adds a participation.
     *
     * @param act           the act to add to
     * @param entity        the participation entity             `
     * @param participation the participation short name
     */
    private void addParticipation(Act act, Entity entity,
                                  String participation) {
        Participation p = (Participation) create(participation);
        assertNotNull(p);
        p.setAct(act.getObjectReference());
        p.setEntity(entity.getObjectReference());
        act.addParticipation(p);
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
     * Creates a new customer.
     *
     * @return a new customer
     */
    private Party createCustomer() {
        Party party = (Party) create("party.customerperson");
        assertNotNull(party);
        return party;
    }

}
