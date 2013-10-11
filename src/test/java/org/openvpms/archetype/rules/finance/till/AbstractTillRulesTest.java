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

import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.rules.act.ActStatus.IN_PROGRESS;
import static org.openvpms.archetype.rules.act.ActStatus.POSTED;
import static org.openvpms.component.business.domain.im.datatypes.quantity.Money.ONE;

/**
 * Base class for Till test cases.
 *
 * @author Tim Anderson
 */
public abstract class AbstractTillRulesTest extends ArchetypeServiceTest {

    /**
     * Verifies that {@link TillBalanceRules#addToTill} adds acts with status 'Posted'
     * to the associated till balance when its saved, while other statuses are
     * ignored.
     *
     * @param till          the till
     * @param acts          the acts to save
     * @param balanceExists determines if the balance should exist prior to the
     *                      save
     * @param expectedTotal the expected total balance
     * @return the balance, if it exists
     */
    protected FinancialAct checkAddToTillBalance(Entity till, final List<FinancialAct> acts, boolean balanceExists,
                                                 BigDecimal expectedTotal) {
        IArchetypeService service = getArchetypeService();
        FinancialAct act = acts.get(0);
        boolean posted = POSTED.equals(act.getStatus());
        FinancialAct balance = TillHelper.getUnclearedTillBalance(till, service);
        boolean existsPriorToSave = (balance != null);
        if (balanceExists) {
            assertNotNull(balance);
        } else {
            assertNull(balance);
        }
        service.save(acts);
        balance = TillHelper.getUnclearedTillBalance(till, service);

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
                assertFalse("Act added to till balance more than once", found > 1);
            } else {
                assertTrue("Act without Posted status added to till balance", found == 0);
            }
            BigDecimal total = balance.getTotal();
            assertTrue(expectedTotal.compareTo(total) == 0);
        }
        return balance;
    }

    /**
     * Helper to create an <em>act.tillBalance</em> wrapped in a bean.
     *
     * @param status the act status
     * @return a new act
     */
    protected ActBean createBalance(Party till, String status) {
        ActBean act = createAct(TillArchetypes.TILL_BALANCE);
        act.setStatus(status);
        act.addNodeParticipation("till", till);
        return act;
    }

    /**
     * Helper to create a new <em>act.customerAccountPayment</em> and
     * corresponding <em>act.customerAccountPaymentCash</em>, with a total
     * value of 1.0.
     *
     * @param till the till
     * @return a list containing the payment act and its item
     */
    protected List<FinancialAct> createPayment(Party till) {
        Party party = TestHelper.createCustomer();
        return FinancialTestHelper.createPayment(ONE, party, till, IN_PROGRESS);
    }

    /**
     * Helper to create a new <em>act.customerAccountRefund</em> and
     * corresponding <em>act.customerAccountRefundCash</em>, with a total
     * value of 1.0.
     *
     * @param till the till
     * @return a new act
     */
    protected List<FinancialAct> createRefund(Party till) {
        Party party = TestHelper.createCustomer();
        return FinancialTestHelper.createRefund(ONE, party, till, IN_PROGRESS);
    }

    /**
     * Helper to create a new act, wrapped in a bean.
     *
     * @param shortName the act short name
     * @return a new act wrapped in a bean
     */
    protected ActBean createAct(String shortName) {
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
