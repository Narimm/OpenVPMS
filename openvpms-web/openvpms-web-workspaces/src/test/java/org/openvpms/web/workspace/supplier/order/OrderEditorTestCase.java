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

package org.openvpms.web.workspace.supplier.order;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.supplier.SupplierTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.test.AbstractAppTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link OrderEditor}.
 *
 * @author Tim Anderson
 */
public class OrderEditorTestCase extends AbstractAppTest {

    /**
     * The practice.
     */
    private Party practice;

    /**
     * Sets up the test.
     */
    @Before
    public void setUp() {
        super.setUp();
        practice = TestHelper.getPractice(); // sets up the practice with a 0% tax rate
    }

    /**
     * Verifies that the amount is recalculated if the tax rate changes between edits for an order.
     */
    @Test
    public void testTaxRateChangeForInProgressOrder() {
        // create an order with a 0% tax rate.
        BigDecimal amount = new BigDecimal("20");
        FinancialAct order = createOrder(amount, ActStatus.IN_PROGRESS);
        checkEquals(amount, order.getTotal());
        checkEquals(BigDecimal.ZERO, order.getTaxAmount());

        // change the tax rate to 10%
        practice.addClassification(TestHelper.createTaxType(new BigDecimal("10")));

        // edit the order
        edit(order);

        // verify that total has changed
        checkEquals(new BigDecimal("22"), order.getTotal());
        checkEquals(new BigDecimal("2"), order.getTaxAmount());
    }

    /**
     * Verifies that the amount is not recalculated if the tax rate changes between edits for a {@code POSTED} order.
     */
    @Test
    public void testTaxRateChangeForPostedOrder() {
        // create an order with a 0% tax rate.
        BigDecimal amount = new BigDecimal("10");
        FinancialAct order = createOrder(amount, ActStatus.POSTED);
        checkEquals(amount, order.getTotal());
        checkEquals(BigDecimal.ZERO, order.getTaxAmount());

        // change the tax rate to 10%
        practice.addClassification(TestHelper.createTaxType(new BigDecimal("10")));

        // edit the order
        edit(order);

        // verify that total hasn't changed
        checkEquals(new BigDecimal("10"), order.getTotal());
        checkEquals(BigDecimal.ZERO, order.getTaxAmount());
    }

    /**
     * Helper to edit an order.
     * <p/>
     * If the order isn't {@code POSTED}, amounts will recalculate
     *
     * @param order the order
     */
    private void edit(FinancialAct order) {
        User author = TestHelper.createUser();
        Context context = new LocalContext();
        context.setPractice(practice);
        context.setUser(author);
        DefaultLayoutContext layoutContext = new DefaultLayoutContext(context, new HelpContext("foo", null));
        OrderEditor editor = new OrderEditor(order, null, layoutContext);
        editor.getComponent();
        assertTrue(editor.save());
    }

    /**
     * Creates an order.
     *
     * @param amount the order amount
     * @param status the order status
     * @return a new order
     */
    private FinancialAct createOrder(BigDecimal amount, String status) {
        Product product = TestHelper.createProduct();
        Party supplier = TestHelper.createSupplier();
        Party stockLocation = SupplierTestHelper.createStockLocation();
        List<FinancialAct> acts = SupplierTestHelper.createOrder(amount, supplier, stockLocation, product);
        FinancialAct order = acts.get(0);
        order.setStatus(status);
        save(acts);
        return order;
    }

}
