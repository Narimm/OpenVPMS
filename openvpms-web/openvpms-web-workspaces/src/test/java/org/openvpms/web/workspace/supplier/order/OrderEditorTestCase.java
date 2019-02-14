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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.supplier.order;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.supplier.DeliveryStatus;
import org.openvpms.archetype.rules.supplier.SupplierTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.test.AbstractAppTest;

import java.math.BigDecimal;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.junit.Assert.assertEquals;
import static org.openvpms.archetype.rules.math.MathRules.ONE_HUNDRED;

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
        checkEquals(BigDecimal.TEN, order.getTotal());
        checkEquals(BigDecimal.ZERO, order.getTaxAmount());
    }

    /**
     * Tests changing the cancelled quantity on a finalised order.
     */
    @Test
    public void testChangeCancelledQuantity() {
        Party supplier = TestHelper.createSupplier();
        Party stockLocation = SupplierTestHelper.createStockLocation();
        Product product1 = TestHelper.createProduct();
        Product product2 = TestHelper.createProduct();
        FinancialAct item1 = SupplierTestHelper.createOrderItem(product1, BigDecimal.valueOf(20), 1, "BOX", ONE, ONE);
        FinancialAct item2 = SupplierTestHelper.createOrderItem(product2, ONE_HUNDRED, 1, "BOX", ONE, ONE);
        List<FinancialAct> order = SupplierTestHelper.createOrder(supplier, stockLocation, item1, item2);

        IMObjectBean item1Bean = getBean(item1);
        item1Bean.setValue("receivedQuantity", 20);
        IMObjectBean orderBean = getBean(order.get(0));
        orderBean.setValue("deliveryStatus", DeliveryStatus.PART.toString());
        save(order);

        OrderEditor editor = createEditor(order.get(0));
        OrderItemEditor item1Editor = getItemEditor(editor, item1);
        item1Editor.setCancelledQuantity(BigDecimal.valueOf(20));
        assertEquals(DeliveryStatus.PART.toString(), editor.getDeliveryStatus());

        OrderItemEditor item2Editor = getItemEditor(editor, item2);
        item2Editor.setCancelledQuantity(ONE_HUNDRED);
        assertEquals(DeliveryStatus.FULL.toString(), editor.getDeliveryStatus());

        item2Editor.setCancelledQuantity(ZERO);
        assertEquals(DeliveryStatus.PART.toString(), editor.getDeliveryStatus());
    }

    /**
     * Returns the editor for an item.
     *
     * @param editor the editor
     * @param item   the item
     * @return the item editor
     */
    private OrderItemEditor getItemEditor(OrderEditor editor, FinancialAct item) {
        ActRelationshipCollectionEditor items = editor.getItems();
        for (Act act : items.getCurrentActs()) {
            if (act.equals(item)) {
                return (OrderItemEditor) items.getEditor(act);
            }
        }
        throw new IllegalStateException("item not found");
    }

    /**
     * Helper to edit an order.
     * <p/>
     * If the order isn't {@code POSTED}, amounts will recalculate
     *
     * @param order the order
     */
    private void edit(FinancialAct order) {
        OrderEditor editor = createEditor(order);
        editor.save();
    }

    /**
     * Creates an order editor.
     *
     * @param order the order to edit
     * @return a new editor
     */
    private OrderEditor createEditor(FinancialAct order) {
        User author = TestHelper.createUser();
        Context context = new LocalContext();
        context.setPractice(practice);
        context.setUser(author);
        DefaultLayoutContext layoutContext = new DefaultLayoutContext(context, new HelpContext("foo", null));
        OrderEditor editor = new OrderEditor(order, null, layoutContext);
        editor.getComponent();
        return editor;
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
