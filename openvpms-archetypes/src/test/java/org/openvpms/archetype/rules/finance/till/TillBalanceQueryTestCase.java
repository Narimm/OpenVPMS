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

package org.openvpms.archetype.rules.finance.till;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.ObjectSet;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.openvpms.archetype.rules.act.ActStatus.POSTED;

/**
 * Tests the {@link TillBalanceQuery} class.
 *
 * @author Tim Anderson
 */
public class TillBalanceQueryTestCase extends ArchetypeServiceTest {

    /**
     * The customer.
     */
    private Party customer;

    /**
     * The till.
     */
    private Party till;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        customer = TestHelper.createCustomer();
        till = FinancialTestHelper.createTill();
    }

    /**
     * Tests the query when a till balance has a payment and adjustment.
     */
    @Test
    public void testPaymentWithAdjustment() {
        Date date1 = TestHelper.getDatetime("2015-01-22 10:00:00");
        Date date2 = TestHelper.getDatetime("2015-04-22 11:00:00");

        FinancialAct balance = FinancialTestHelper.createTillBalance(till);
        List<FinancialAct> acts = FinancialTestHelper.createPaymentCash(BigDecimal.TEN, customer, till, POSTED);
        assertEquals(2, acts.size());
        FinancialAct payment = acts.get(0);
        payment.setActivityStartTime(date1);
        FinancialAct paymentItem = acts.get(1);

        // create an adjustment that reverses the payment
        FinancialAct adjustment = (FinancialAct) create(TillArchetypes.TILL_BALANCE_ADJUSTMENT);
        ActBean bean = new ActBean(adjustment);
        bean.setValue("startTime", date2);
        bean.setValue("credit", false);
        bean.addNodeParticipation("till", till);
        bean.setValue("amount", BigDecimal.TEN);

        ActBean balanceBean = new ActBean(balance);
        balanceBean.addNodeRelationship("items", payment);
        balanceBean.addNodeRelationship("items", adjustment);
        save(balance, payment, paymentItem, adjustment);

        TillBalanceQuery query = new TillBalanceQuery(balance, getArchetypeService());
        List<ObjectSet> results = query.query().getResults();
        assertEquals(2, results.size());
        check(results.get(0), balance, payment, paymentItem, BigDecimal.TEN);
        check(results.get(1), balance, adjustment, null, BigDecimal.TEN.negate());
    }

    /**
     * Tests the query when a till balance has a payment reversed by a refund.
     */
    @Test
    public void testPaymentWithRefund() {
        Date date1 = TestHelper.getDatetime("2015-01-22 10:00:00");
        Date date2 = TestHelper.getDatetime("2015-04-22 11:00:00");

        FinancialAct balance = FinancialTestHelper.createTillBalance(till);
        List<FinancialAct> acts = FinancialTestHelper.createPaymentCash(BigDecimal.TEN, customer, till, POSTED);
        assertEquals(2, acts.size());
        FinancialAct payment = acts.get(0);
        payment.setActivityStartTime(date1);
        FinancialAct paymentItem = acts.get(1);

        ActBean bean = new ActBean(balance);
        bean.addNodeRelationship("items", payment);
        save(balance, payment, paymentItem);

        CustomerAccountRules rules = applicationContext.getBean(CustomerAccountRules.class);
        FinancialAct refund = rules.reverse(acts.get(0), date2, "reversal", null, true, balance);
        ActBean refundBean = new ActBean(refund);
        List<Act> items = refundBean.getNodeActs("items");
        assertEquals(1, items.size());
        Act refundItem = items.get(0);

        TillBalanceQuery query = new TillBalanceQuery(balance, getArchetypeService());
        List<ObjectSet> results = query.query().getResults();
        assertEquals(2, results.size());
        check(results.get(0), balance, payment, paymentItem, BigDecimal.TEN);
        check(results.get(1), balance, refund, refundItem, BigDecimal.TEN.negate());
    }

    /**
     * Verifies a set has the expected details.
     *
     * @param set     the set to check
     * @param balance the expected act.tillBalance
     * @param act     the expected act
     * @param item    the expected act item. May be {@code null}
     * @param amount  the expected amount
     */
    private void check(ObjectSet set, FinancialAct balance, FinancialAct act, Act item, BigDecimal amount) {
        assertEquals(balance, set.get("tillBalance"));
        assertEquals(act, set.get("act"));
        assertEquals(item, set.get("item"));
        checkEquals(amount, set.getBigDecimal("amount"));
    }

}
