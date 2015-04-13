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

package org.openvpms.web.component.im.till;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link CashDrawer}.
 *
 * @author Tim Anderson
 */
public class CashDrawerTestCase extends ArchetypeServiceTest {

    /**
     * The customer.
     */
    private Party customer;

    /**
     * Sets up the test case
     */
    @Before
    public void setUp() {
        customer = TestHelper.createCustomer();
    }

    /**
     * Tests the {@link CashDrawer#canOpen()} method.
     */
    @Test
    public void testCanOpen() {
        Party till1 = createTill(null, null);
        CashDrawer drawer1 = new CashDrawer(till1);
        assertFalse(drawer1.canOpen());

        Party till2 = createTill("foo", "27,112,0,50,250");
        CashDrawer drawer2 = new CashDrawer(till2);
        assertTrue(drawer2.canOpen());
    }

    /**
     * Tests the {@link CashDrawer#needsOpen(Act)} method.
     */
    @Test
    public void testNeedsOpen() {
        Party till = createTill("foo", "27,112,0,50,250");
        CashDrawer drawer = new CashDrawer(till);

        Act payment1 = createPayment(till, CustomerAccountArchetypes.PAYMENT_CASH, ActStatus.IN_PROGRESS);
        assertFalse(drawer.needsOpen(payment1));

        payment1.setStatus(ActStatus.POSTED);
        assertTrue(drawer.needsOpen(payment1));

        Act payment2 = createPayment(till, CustomerAccountArchetypes.PAYMENT_CHEQUE, ActStatus.POSTED);
        assertTrue(drawer.needsOpen(payment2));

        Act payment3 = createPaymentEFT(till, ActStatus.POSTED, BigDecimal.ZERO);
        assertFalse(drawer.needsOpen(payment3));

        Act payment4 = createPaymentEFT(till, ActStatus.POSTED, BigDecimal.TEN);
        assertTrue(drawer.needsOpen(payment4));

        Act payment5 = createPayment(till, CustomerAccountArchetypes.PAYMENT_CREDIT, ActStatus.POSTED);
        assertTrue(drawer.needsOpen(payment5));

        Act payment6 = createPayment(till, CustomerAccountArchetypes.PAYMENT_DISCOUNT, ActStatus.POSTED);
        assertFalse(drawer.needsOpen(payment6));

        Act payment7 = createPayment(till, CustomerAccountArchetypes.PAYMENT_OTHER, ActStatus.POSTED);
        assertFalse(drawer.needsOpen(payment7));

        Act refund1 = createRefund(till, CustomerAccountArchetypes.REFUND_CASH, ActStatus.IN_PROGRESS);
        assertFalse(drawer.needsOpen(refund1));

        refund1.setStatus(ActStatus.POSTED);
        assertTrue(drawer.needsOpen(refund1));

        Act refund2 = createRefund(till, CustomerAccountArchetypes.REFUND_CHEQUE, ActStatus.POSTED);
        assertTrue(drawer.needsOpen(refund2));

        Act refund3 = createRefund(till, CustomerAccountArchetypes.REFUND_EFT, ActStatus.POSTED);
        assertFalse(drawer.needsOpen(refund3));

        Act refund5 = createRefund(till, CustomerAccountArchetypes.REFUND_CREDIT, ActStatus.POSTED);
        assertTrue(drawer.needsOpen(refund5));

        Act refund6 = createRefund(till, CustomerAccountArchetypes.REFUND_DISCOUNT, ActStatus.POSTED);
        assertFalse(drawer.needsOpen(refund6));

        Act refund7 = createRefund(till, CustomerAccountArchetypes.REFUND_OTHER, ActStatus.POSTED);
        assertFalse(drawer.needsOpen(refund7));
    }


    /**
     * Helper to create a till.
     *
     * @param printerName the printer name. May be {@code null}
     * @param command     the drawer command. May be {@code null}
     * @return a new till
     */
    private Party createTill(String printerName, String command) {
        Party till = TestHelper.createTill();
        IMObjectBean bean = new IMObjectBean(till);
        bean.setValue("printerName", printerName);
        bean.setValue("drawerCommand", command);
        bean.save();
        return till;
    }

    /**
     * Helper to create a payment with a single item.
     *
     * @param till          the till
     * @param itemShortName the item archetype short name
     * @param status        the payment status
     * @return a new payment
     */
    private FinancialAct createPayment(Party till, String itemShortName, String status) {
        FinancialAct payment = FinancialTestHelper.createPayment(BigDecimal.ONE, customer, till, status);
        FinancialAct item = FinancialTestHelper.createPaymentRefundItem(itemShortName, BigDecimal.ONE);
        return addItem(payment, item);
    }

    /**
     * Helper to create an EFT payment.
     *
     * @param till    the till
     * @param status  the payment status
     * @param cashout the cash-out amount
     * @return a new payment
     */
    private FinancialAct createPaymentEFT(Party till, String status, BigDecimal cashout) {
        FinancialAct payment = FinancialTestHelper.createPayment(BigDecimal.TEN, customer, till, status);
        FinancialAct item = FinancialTestHelper.createPaymentRefundItem(CustomerAccountArchetypes.PAYMENT_EFT,
                                                                        BigDecimal.ONE);
        ActBean bean = new ActBean(item);
        bean.setValue("cashout", cashout);
        return addItem(payment, item);
    }

    /**
     * Helper to create a refund with a single item.
     *
     * @param till          the till
     * @param itemShortName the item archetype short name
     * @param status        the payment status
     * @return a new payment
     */
    private FinancialAct createRefund(Party till, String itemShortName, String status) {
        FinancialAct refund = FinancialTestHelper.createRefund(BigDecimal.ONE, customer, till, status);
        FinancialAct item = FinancialTestHelper.createPaymentRefundItem(itemShortName, BigDecimal.ONE);
        return addItem(refund, item);
    }

    private FinancialAct createRefundEFT(Party till, String status, BigDecimal cashout) {
        FinancialAct payment = FinancialTestHelper.createRefund(BigDecimal.TEN, customer, till, status);
        FinancialAct item = FinancialTestHelper.createPaymentRefundItem(CustomerAccountArchetypes.REFUND_EFT,
                                                                        BigDecimal.ONE);
        ActBean bean = new ActBean(item);
        bean.setValue("cashout", cashout);
        return addItem(payment, item);
    }

    /**
     * Adds an item to an act.
     *
     * @param act  the parent act
     * @param item the item to add
     * @return the parent
     */
    private FinancialAct addItem(FinancialAct act, FinancialAct item) {
        ActBean bean = new ActBean(act);
        bean.addNodeRelationship("items", item);
        save(act, item);
        return act;
    }

}
