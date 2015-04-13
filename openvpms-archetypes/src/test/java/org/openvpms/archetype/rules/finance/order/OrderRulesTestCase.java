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

package org.openvpms.archetype.rules.finance.order;

import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Customer order rules.
 *
 * @author Tim Anderson
 */
public class OrderRulesTestCase extends ArchetypeServiceTest {

    /**
     * Tests the {@link OrderRules#hasOrders(Party)} method.
     */
    @Test
    public void testHasOrdersForCustomer() {
        OrderRules rules = new OrderRules(getArchetypeService());
        Party customer1 = TestHelper.createCustomer();
        Party customer2 = TestHelper.createCustomer();
        Party patient1 = TestHelper.createPatient();
        Party patient2 = TestHelper.createPatient();

        assertFalse(rules.hasOrders(customer1));
        Act act1 = createOrder(customer1, patient1);

        assertTrue(rules.hasOrders(customer1));
        assertFalse(rules.hasOrders(customer2));

        act1.setStatus(ActStatus.POSTED);
        save(act1);
        assertFalse(rules.hasOrders(customer1));

        // test for returns
        Act act2 = createReturn(customer2, patient2);
        assertTrue(rules.hasOrders(customer2));
        act2.setStatus(ActStatus.POSTED);
        save(act2);
        assertFalse(rules.hasOrders(customer2));
    }

    /**
     * Tests the {@link OrderRules#hasOrders(Party, Party)} method.
     */
    @Test
    public void testHasOrdersForCustomerAndPatient() {
        OrderRules rules = new OrderRules(getArchetypeService());
        Party customer1 = TestHelper.createCustomer();
        Party customer2 = TestHelper.createCustomer();
        Party patient1 = TestHelper.createPatient();
        Party patient2 = TestHelper.createPatient();

        assertFalse(rules.hasOrders(customer1, patient1));
        Act act1 = createOrder(customer1, patient1);

        assertTrue(rules.hasOrders(customer1, patient1));
        assertFalse(rules.hasOrders(customer1, patient2));
        assertFalse(rules.hasOrders(customer2, patient2));
        assertFalse(rules.hasOrders(customer2, patient1));

        act1.setStatus(ActStatus.POSTED);
        save(act1);
        assertFalse(rules.hasOrders(customer1, patient1));

        // test for returns
        Act act2 = createReturn(customer2, patient2);
        assertTrue(rules.hasOrders(customer2, patient2));
        act2.setStatus(ActStatus.POSTED);
        save(act2);
        assertFalse(rules.hasOrders(customer2, patient2));
    }

    /**
     * Tests the {@link OrderRules#getOrders(Party)} method.
     */
    @Test
    public void testGetOrdersForCustomer() {
        Party customer1 = TestHelper.createCustomer();
        Party customer2 = TestHelper.createCustomer();
        Party patient1 = TestHelper.createPatient();
        Party patient2 = TestHelper.createPatient();

        checkGetOrders(customer1);

        Act act1 = createOrder(customer1, patient1);
        Act act2 = createReturn(customer1, patient1);

        checkGetOrders(customer1, act1, act2);
        checkGetOrders(customer2);

        act1.setStatus(ActStatus.POSTED);
        save(act1);

        checkGetOrders(customer1, act2);

        // add another item to the return. Should still retrieve the one instance
        addItem(act2, patient1);
        checkGetOrders(customer1, act2);

        // add another item for a different patient
        addItem(act2, patient2);
        checkGetOrders(customer1, act2);
    }

    /**
     * Tests the {@link OrderRules#getOrders(Party, Party)} method.
     */
    @Test
    public void testGetOrdersForCustomerAndPatient() {
        Party customer1 = TestHelper.createCustomer();
        Party customer2 = TestHelper.createCustomer();
        Party patient1 = TestHelper.createPatient();
        Party patient2 = TestHelper.createPatient();

        checkGetOrders(customer1, patient1);

        Act act1 = createOrder(customer1, patient1);
        Act act2 = createReturn(customer1, patient1);

        checkGetOrders(customer1, patient1, act1, act2);
        checkGetOrders(customer1, patient2);
        checkGetOrders(customer2, patient2);
        checkGetOrders(customer2, patient1);

        act1.setStatus(ActStatus.POSTED);
        save(act1);

        checkGetOrders(customer1, patient1, act2);

        // add another item to the return. Should still retrieve the one instance
        addItem(act2, patient1);
        checkGetOrders(customer1, patient1, act2);

        // add another item for a different patient
        addItem(act2, patient2);
        checkGetOrders(customer1, patient1, act2);
        checkGetOrders(customer1, patient2, act2);
    }

    /**
     * Verifies the acts returned by {@link OrderRules#getOrders(Party, Party)} match that expected.
     *
     * @param customer the customer
     * @param expected the expected orders. Empty if no orders are expected
     */
    private void checkGetOrders(Party customer, Act... expected) {
        OrderRules rules = new OrderRules(getArchetypeService());
        List<Act> orders = rules.getOrders(customer);
        checkOrders(orders, expected);
    }

    /**
     * Verifies the acts returned by {@link OrderRules#getOrders(Party, Party)} match that expected.
     *
     * @param customer the customer
     * @param patient  the patient
     * @param expected the expected orders. Empty if no orders are expected
     */
    private void checkGetOrders(Party customer, Party patient, Act... expected) {
        OrderRules rules = new OrderRules(getArchetypeService());
        List<Act> orders = rules.getOrders(customer, patient);
        checkOrders(orders, expected);
    }

    /**
     * Verifies a list of orders match that expected.
     *
     * @param orders   the orders
     * @param expected the expected orders
     */
    private void checkOrders(List<Act> orders, Act[] expected) {
        assertEquals(expected.length, orders.size());
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], orders.get(i));
        }
    }

    /**
     * Creates an order for a customer and patient.
     *
     * @param customer the customer
     * @param patient  the patient
     * @return a new order
     */
    private Act createOrder(Party customer, Party patient) {
        return createOrderReturn(OrderArchetypes.PHARMACY_ORDER, customer, patient);
    }

    /**
     * Creates a return for a customer and patient.
     *
     * @param customer the customer
     * @param patient  the patient
     * @return a new return
     */
    private Act createReturn(Party customer, Party patient) {
        return createOrderReturn(OrderArchetypes.PHARMACY_RETURN, customer, patient);
    }

    /**
     * Creates an order or return.
     *
     * @param shortName the order/return archetype short name
     * @param customer  the customer
     * @param patient   the patient
     * @return a new order/return
     */
    private Act createOrderReturn(String shortName, Party customer, Party patient) {
        Act order = (Act) create(shortName);
        ActBean bean = new ActBean(order);
        bean.addNodeParticipation("customer", customer);

        addItem(order, patient);
        return order;
    }

    /**
     * Adds an item ot an order/return.
     *
     * @param act     the order or return act
     * @param patient the patient
     */
    private void addItem(Act act, Party patient) {
        String shortName = TypeHelper.isA(act, OrderArchetypes.PHARMACY_ORDER)
                           ? OrderArchetypes.PHARMACY_ORDER_ITEM : OrderArchetypes.PHARMACY_RETURN_ITEM;
        ActBean bean = new ActBean(act);
        Act item = (Act) create(shortName);
        ActBean itemBean = new ActBean(item);
        itemBean.addNodeParticipation("patient", patient);
        bean.addNodeRelationship("items", item);
        save(act, item);
    }


}
