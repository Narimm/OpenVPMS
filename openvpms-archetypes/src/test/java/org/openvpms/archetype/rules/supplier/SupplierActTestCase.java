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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.supplier;

import org.junit.Test;
import org.openvpms.archetype.rules.stock.StockArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


/**
 * Tests the <em>act.supplierAccountInvoice</em>, <em>act.supplierAccountCredit<em>
 * <em>act.supplierOrder</em>, <em>act.supplierDelivery</em> and <em>act.supplierReturn</em> archetypes.
 *
 * @author Tim Anderson
 */
public class SupplierActTestCase extends AbstractSupplierTest {

    /**
     * Verifies that when a supplier invoice is deleted, any child act is deleted with it.
     */
    @Test
    public void testDeleteInvoice() {
        checkDelete(SupplierArchetypes.INVOICE, SupplierArchetypes.INVOICE_ITEM);
    }

    /**
     * Verifies that when a supplier credit is deleted, any child act is deleted with it.
     */
    @Test
    public void testDeleteCredit() {
        checkDelete(SupplierArchetypes.CREDIT, SupplierArchetypes.CREDIT_ITEM);
    }

    /**
     * Verifies that when a supplier payment is deleted, any child act is deleted with it.
     */
    @Test
    public void testDeletePayment() {
        checkDelete(SupplierArchetypes.PAYMENT, SupplierArchetypes.PAYMENT_CASH);
    }

    /**
     * Verifies that when a supplier refund is deleted, any child act is deleted with it.
     */
    @Test
    public void testDeleteRefund() {
        checkDelete(SupplierArchetypes.REFUND, SupplierArchetypes.REFUND_CASH);
    }

    /**
     * Verifies that when a supplier order is deleted, any child act is deleted with it.
     */
    @Test
    public void testDeleteOrder() {
        checkDelete(SupplierArchetypes.ORDER, SupplierArchetypes.ORDER_ITEM);
    }

    /**
     * Verifies that when a supplier delivery is deleted, any child act is deleted with it.
     */
    @Test
    public void testDeleteDelivery() {
        checkDelete(SupplierArchetypes.DELIVERY, SupplierArchetypes.DELIVERY_ITEM);
    }

    /**
     * Verifies that when a supplier delivery is deleted, the linked order item is not deleted.
     */
    @Test
    public void testDeleteDeliveryLinkedToOrder() {
        List<Act> order = createActs(SupplierArchetypes.ORDER, SupplierArchetypes.ORDER_ITEM);
        List<Act> delivery = createActs(SupplierArchetypes.DELIVERY, SupplierArchetypes.DELIVERY_ITEM);

        Act deliveryItem = delivery.get(1);
        ActBean deliveryItemBean = new ActBean(deliveryItem);
        Act orderItem = order.get(1);
        deliveryItemBean.addNodeRelationship("order", orderItem);
        save(deliveryItem, orderItem);

        checkDelete(delivery);
        assertNotNull(get(order.get(0)));
        assertNotNull(get(orderItem));
    }

    /**
     * Verifies that when a supplier return is deleted, any child act is deleted with it.
     */
    @Test
    public void testDeleteReturn() {
        checkDelete(SupplierArchetypes.RETURN, SupplierArchetypes.RETURN_ITEM);
    }

    /**
     * Verifies that when a supplier return is deleted, the linked order item is not deleted.
     */
    @Test
    public void testDeleteReturnLinkedToOrder() {
        List<Act> order = createActs(SupplierArchetypes.ORDER, SupplierArchetypes.ORDER_ITEM);
        List<Act> supplierReturn = createActs(SupplierArchetypes.RETURN, SupplierArchetypes.RETURN_ITEM);

        Act returnItem = supplierReturn.get(1);
        ActBean returnItemBean = new ActBean(returnItem);
        Act orderItem = order.get(1);
        returnItemBean.addNodeRelationship("order", orderItem);
        save(returnItem, orderItem);

        checkDelete(supplierReturn);
        assertNotNull(get(order.get(0)));
        assertNotNull(get(orderItem));
    }

    /**
     * Verifies that when a supplier act is deleted, any child act is deleted with it.
     *
     * @param shortName     the parent act archetype short name
     * @param itemShortName the child act archetype short name
     */
    private void checkDelete(String shortName, String itemShortName) {
        List<Act> acts = createActs(shortName, itemShortName);

        checkDelete(acts);
    }

    /**
     * Verifies that when a supplier act is deleted, any child act is deleted with it.
     *
     * @param acts the parent and child acts
     */
    private void checkDelete(List<Act> acts) {
        Act parent = acts.get(0);
        Act child = acts.get(1);

        assertNotNull(get(parent));
        assertNotNull(get(child));

        remove(parent);
        assertNull(get(parent));
        assertNull(get(child));
    }

    /**
     * Creates a parent supplier act linked to a child act
     *
     * @param shortName     the parent act archetype short name
     * @param itemShortName the child act archetype short name
     * @return the two acts
     */
    private List<Act> createActs(String shortName, String itemShortName) {
        ActBean bean = createAct(shortName);
        Act act = bean.getAct();
        FinancialAct item = (FinancialAct) create(itemShortName);
        ActBean itemBean = new ActBean(item);
        itemBean.addParticipation(StockArchetypes.STOCK_PARTICIPATION, getProduct());
        item.setQuantity(BigDecimal.ONE);
        getArchetypeService().deriveValues(item);
        bean.addNodeRelationship("items", item);
        List<Act> acts = new ArrayList<Act>();
        acts.add(act);
        acts.add(item);
        save(acts);
        return acts;
    }

}