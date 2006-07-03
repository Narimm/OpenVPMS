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
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.ruleengine.RuleEngineException;


/**
 * Tests the {@link TillRules} class when invoked by the
 * <em>archetypeService.save.act.tillBalance.before.drl</em> rule
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
     * Verrifies that an <em>act.tillBalance</em> with 'Uncleared' status
     * can only be saved if there are no other uncleared till balances for
     * the same till.
     */
    public void testSaveUnclearedTillBalance() {
        Act balance1 = createBalance("Uncleared");
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
     * be saved for the same till.
     */
    public void testSaveClearedTillBalance() {
        for (int i = 0; i < 3; ++i) {
            Act balance = createBalance("Cleared");
            save(balance);
        }
    }

    /**
     * Verifies that {@link TillRules#checkUnclearedTillBalance} throws
     * TillRuleException if invoked for an invalid act.
     */
    public void testCheckUnclearedTillWithInvalidAct() {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        FinancialAct act = createAct("act.bankDeposit");
        try {
            TillRules.checkUnclearedTillBalance(service, act);
        } catch (TillRuleException expected) {
            assertEquals(TillRuleException.ErrorCode.InvalidTillArchetype,
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
     * Helper to create an <em>act.tillBalance</em>.
     *
     * @param status the act status
     * @return a new act
     */
    protected FinancialAct createBalance(String status) {
        FinancialAct act = createAct("act.tillBalance");
        act.setStatus(status);
        addParticipation(act, _till, "participation.till");
        return act;
    }

    /**
     * Helper to create a new act.
     *
     * @param shortName the act short name
     * @return a new act
     */
    protected FinancialAct createAct(String shortName) {
        FinancialAct act = (FinancialAct) create(shortName);
        assertNotNull(act);
        return act;
    }

    /**
     * Helper to add a relationship between two acts.
     *
     * @param source    the source act
     * @param target    the target act
     * @param shortName the act relationship short name
     */
    protected void addActRelationship(Act source, Act target,
                                      String shortName) {
        ActRelationship relationship = (ActRelationship) create(shortName);
        assertNotNull(relationship);
        relationship.setSource(source.getObjectReference());
        relationship.setTarget(target.getObjectReference());
        source.addActRelationship(relationship);
        target.addActRelationship(relationship);
    }

    /**
     * Adds a participation.
     *
     * @param act           the act to add to
     * @param entity        the participation entity             `
     * @param participation the participation short name
     */
    protected void addParticipation(Act act, Entity entity,
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
    protected Party createTill() {
        Party till = (Party) create("party.organisationTill");
        assertNotNull(till);
        till.setName("TillRulesTestCase-Till" + hashCode());
        save(till);
        return till;
    }

}
