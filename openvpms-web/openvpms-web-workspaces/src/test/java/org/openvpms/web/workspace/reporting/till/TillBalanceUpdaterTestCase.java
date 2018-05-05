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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.till;

import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.finance.till.TillArchetypes;
import org.openvpms.archetype.rules.finance.till.TillBalanceRules;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link TillBalanceUpdater} class.
 *
 * @author Tim Anderson
 */
public class TillBalanceUpdaterTestCase extends ArchetypeServiceTest {

    /**
     * The transaction manager.
     */
    @Autowired
    PlatformTransactionManager transactionManager;

    /**
     * Verifies a payment can be added directly to an IN_PROGRESS till balance.
     */
    @Test
    public void testUpdateBalanceForPayment() {
        final Party till = TestHelper.createTill();
        final FinancialAct balance = FinancialTestHelper.createTillBalance(till);
        balance.setStatus(ActStatus.IN_PROGRESS);
        save(balance);

        Party customer = TestHelper.createCustomer();
        List<FinancialAct> payment = FinancialTestHelper.createPaymentCash(BigDecimal.TEN, customer, till,
                                                                           ActStatus.POSTED);
        checkUpdate(payment, balance, BigDecimal.TEN);

        // verifies no other till balance has been created.
        TillBalanceRules rules = new TillBalanceRules(getArchetypeService());
        assertNull(rules.getUnclearedBalance(till));
    }

    /**
     * Verifies a till balance adjustment can be added directly to an IN_PROGRESS till balance.
     */
    @Test
    public void testUpdateBalanceForAdjustment() {
        final Party till = TestHelper.createTill();
        final FinancialAct balance = FinancialTestHelper.createTillBalance(till);
        balance.setStatus(ActStatus.IN_PROGRESS);
        save(balance);

        FinancialAct act = (FinancialAct) create(TillArchetypes.TILL_BALANCE_ADJUSTMENT);
        ActBean bean = new ActBean(act);
        bean.setValue("credit", true);
        bean.addNodeParticipation("till", till);
        bean.setValue("amount", BigDecimal.TEN);

        checkUpdate(Collections.singletonList(act), balance, BigDecimal.TEN);

        // verify the adjustment can be edited
        bean.setValue("amount", BigDecimal.ONE);
        checkUpdate(Collections.singletonList(act), balance, BigDecimal.ONE);

        // verifies no other till balance has been created.
        TillBalanceRules rules = new TillBalanceRules(getArchetypeService());
        assertNull(rules.getUnclearedBalance(till));
    }

    /**
     * Verifies that acts can be added to a till balance.
     *
     * @param acts     the acts
     * @param balance  the balance
     * @param expected the expected balance after update
     */
    private void checkUpdate(final List<FinancialAct> acts, final FinancialAct balance, BigDecimal expected) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                TillBalanceUpdater updater = new TillBalanceUpdater(acts.get(0), balance);
                assertTrue(updater.validate());
                updater.prepare();
                save(acts);
                updater.commit();
                return null;
            }
        });
        FinancialAct balance2 = get(balance);
        checkEquals(expected, balance2.getTotal());
    }

}
