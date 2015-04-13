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
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.ruleengine.RuleEngineException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.openvpms.archetype.rules.act.ActStatus.IN_PROGRESS;
import static org.openvpms.archetype.rules.act.ActStatus.POSTED;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.CantAddActToTill;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.InvalidTillArchetype;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.MissingTill;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.UnclearedTillExists;
import static org.openvpms.archetype.test.TestHelper.createTill;


/**
 * Tests the {@link TillBalanceRules} class when invoked by the
 * <em>archetypeService.save.act.tillBalance.before</em>,
 * <em>archetypeService.save.act.customerAccountPayment.after</em> and
 * <em>archetypeService.save.act.customerAccountRefund.after</em> rules.
 * In order for these tests to be successful, the archetype service
 * must be configured to trigger the above rules.
 *
 * @author Tim Anderson
 */
public class TillBalanceRulesTestCase extends AbstractTillRulesTest {

    /**
     * The till.
     */
    private Party till;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        till = createTill();
        save(till);
    }

    /**
     * Verifies that an <em>act.tillBalance</em> with 'Uncleared' status
     * can only be saved if there are no other uncleared till balances for
     * the same till.<br/>
     * Requires the rule <em>archetypeService.save.act.tillBalance.before</em>.
     */
    @Test
    public void testSaveUnclearedTillBalance() {
        ActBean balance1 = createBalance(till, TillBalanceStatus.UNCLEARED);
        balance1.save();

        // can save the same balance multiple times
        balance1.save();

        ActBean balance2 = createBalance(till, TillBalanceStatus.UNCLEARED);
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
            ActBean balance = createBalance(till, TillBalanceStatus.CLEARED);
            balance.save();
        }
    }

    /**
     * Verifies that {@link TillBalanceRules#checkCanSaveTillBalance} throws
     * TillRuleException if invoked for an invalid act.<br/>
     */
    @Test
    public void testCheckCanSaveTillBalanceWithInvalidAct() {
        TillBalanceRules rules = new TillBalanceRules(getArchetypeService());
        ActBean bean = createAct("act.bankDeposit");
        FinancialAct act = (FinancialAct) bean.getAct();
        try {
            rules.checkCanSaveTillBalance(act);
        } catch (TillRuleException expected) {
            assertEquals(InvalidTillArchetype, expected.getErrorCode());
        }
    }

    /**
     * Verifies that {@link TillBalanceRules#addToTill} adds posted
     * <em>act.customerAccountPayment</em> and
     * <em>act.customerAccountRefund</em> to the associated till balance
     * when they are saved.
     * Requires the rules <em>archetypeServicfe.save.act.customerAccountPayment.after</em> and
     * <em>archetypeServicfe.save.act.customerAccountRefund.after</em>
     */
    @Test
    public void testAddToTillBalance() {
        List<FinancialAct> payment = createPayment(till);
        List<FinancialAct> refund = createRefund(till);

        checkAddToTillBalance(till, payment, false, BigDecimal.ZERO);
        checkAddToTillBalance(till, refund, false, BigDecimal.ZERO);

        payment.get(0).setStatus(POSTED);
        checkAddToTillBalance(till, payment, false, BigDecimal.ONE);
        // payment now updates balance
        checkAddToTillBalance(till, refund, true, BigDecimal.ONE);
        // refund not added

        refund.get(0).setStatus(POSTED);
        checkAddToTillBalance(till, refund, true, BigDecimal.ZERO);
        // refund now updates balance

        // verify that subsequent saves only get don't get added to the balance
        // again
        checkAddToTillBalance(till, payment, true, BigDecimal.ZERO);
        checkAddToTillBalance(till, refund, true, BigDecimal.ZERO);

        List<FinancialAct> payment2 = createPayment(till);
        payment2.get(0).setStatus(POSTED);
        checkAddToTillBalance(till, payment2, true, BigDecimal.ONE);
    }

    /**
     * Verifies that {@link TillBalanceRules#addToTill} throws
     * TillRuleException if invoked for an invalid act.
     */
    @Test
    public void testAddToTillBalanceWithInvalidAct() {
        TillBalanceRules rules = new TillBalanceRules(getArchetypeService());
        ActBean bean = createAct("act.bankDeposit");
        try {
            rules.addToTill(bean.getAct());
        } catch (TillRuleException expected) {
            assertEquals(CantAddActToTill, expected.getErrorCode());
        }
    }

    /**
     * Verifies that {@link TillBalanceRules#addToTill} throws
     * TillRuleException if invoked for an act with no till.
     */
    @Test
    public void testAddToTillBalanceWithNoTill() {
        TillBalanceRules rules = new TillBalanceRules(getArchetypeService());
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

}
