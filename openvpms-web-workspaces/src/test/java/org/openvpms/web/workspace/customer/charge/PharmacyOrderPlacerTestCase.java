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

package org.openvpms.web.workspace.customer.charge;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.patient.PatientHistoryChanges;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.web.component.im.util.DefaultIMObjectCache;
import org.openvpms.web.test.AbstractAppTest;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static org.junit.Assert.assertEquals;
import static org.openvpms.web.workspace.customer.charge.CustomerChargeTestHelper.checkOrder;
import static org.openvpms.web.workspace.customer.charge.TestPharmacyOrderService.Order.Type.CANCEL;
import static org.openvpms.web.workspace.customer.charge.TestPharmacyOrderService.Order.Type.CREATE;
import static org.openvpms.web.workspace.customer.charge.TestPharmacyOrderService.Order.Type.UPDATE;

/**
 * Tests the {@link PharmacyOrderPlacer}.
 *
 * @author Tim Anderson
 */
public class PharmacyOrderPlacerTestCase extends AbstractAppTest {

    /**
     * The patient.
     */
    private Party patient;

    /**
     * The location.
     */
    private Party location;

    /**
     * The clinician.
     */
    private User clinician;

    /**
     * The pharmacy.
     */
    private Entity pharmacy;

    /**
     * The order service.
     */
    private TestPharmacyOrderService service;

    /**
     * The order placer.
     */
    private PharmacyOrderPlacer placer;

    /**
     * Sets up the test case.
     */
    @Override
    @Before
    public void setUp() {
        super.setUp();
        Party customer = TestHelper.createCustomer(false);
        patient = TestHelper.createPatient(true);
        location = TestHelper.createLocation();
        clinician = TestHelper.createClinician();
        pharmacy = CustomerChargeTestHelper.createPharmacy();
        service = new TestPharmacyOrderService();
        placer = new PharmacyOrderPlacer(customer, location, new DefaultIMObjectCache(), service);
    }

    /**
     * Tests the {@link PharmacyOrderPlacer#order(List, PatientHistoryChanges)} method.
     */
    @Test
    public void testOrder() {
        Product product1 = createProduct(pharmacy);
        Product product2 = createProduct(pharmacy);
        Product product3 = TestHelper.createProduct(); // not ordered via a pharmacy
        Act event = PatientTestHelper.createEvent(patient, clinician);
        Act item1 = createItem(product1, ONE, event);
        Act item2 = createItem(product2, TEN, event);
        Act item3 = createItem(product3, BigDecimal.valueOf(2), event);

        PatientHistoryChanges changes = new PatientHistoryChanges(clinician, location, getArchetypeService());

        placer.order(Arrays.asList(item1, item2, item3), changes);

        List<TestPharmacyOrderService.Order> orders = service.getOrders();
        assertEquals(2, orders.size());
        checkOrder(orders.get(0), CREATE, patient, product1, ONE, item1.getId(), item1.getActivityStartTime(),
                   clinician, pharmacy);
        checkOrder(orders.get(1), CREATE, patient, product2, TEN, item2.getId(), item2.getActivityStartTime(),
                   clinician, pharmacy);
    }

    /**
     * Verifies that when a quantity is changed, an update order is placed.
     */
    @Test
    public void testChangeQuantity() {
        Product product = createProduct(pharmacy);
        Act event = PatientTestHelper.createEvent(patient, clinician);
        FinancialAct item = createItem(product, ONE, event);

        List<Act> items = Arrays.<Act>asList(item);
        placer.initialise(items);

        PatientHistoryChanges changes = new PatientHistoryChanges(clinician, location, getArchetypeService());
        item.setQuantity(TEN);

        placer.order(items, changes);

        List<TestPharmacyOrderService.Order> orders = service.getOrders();
        assertEquals(1, orders.size());
        checkOrder(orders.get(0), UPDATE, patient, product, TEN, item.getId(),
                   item.getActivityStartTime(), clinician, pharmacy);
    }

    /**
     * Verifies that when a patient is changed, a cancellation and new order is generated.
     */
    @Test
    public void testChangePatient() {
        Product product = createProduct(pharmacy);
        Act event1 = PatientTestHelper.createEvent(patient, clinician);
        FinancialAct item = createItem(product, ONE, event1);
        Party patient2 = TestHelper.createPatient(true);

        List<Act> items = Arrays.<Act>asList(item);
        placer.initialise(items);

        PatientHistoryChanges changes = new PatientHistoryChanges(clinician, location, getArchetypeService());
        ActBean bean = new ActBean(item);
        bean.setNodeParticipant("patient", patient2);

        Act event2 = PatientTestHelper.createEvent(patient2, clinician);
        addChargeItem(event2, item);
        save(event2, item);

        placer.order(items, changes);

        List<TestPharmacyOrderService.Order> orders = service.getOrders();
        assertEquals(2, orders.size());
        checkOrder(orders.get(0), CANCEL, patient, product, ONE, item.getId(),
                   item.getActivityStartTime(), clinician, pharmacy);
        checkOrder(orders.get(1), CREATE, patient2, product, ONE, item.getId(),
                   item.getActivityStartTime(), clinician, pharmacy);
    }

    /**
     * Verifies that when a product is changed, an update order is placed.
     */
    @Test
    public void testChangeProduct() {
        Product product1 = createProduct(pharmacy);
        Product product2 = createProduct(pharmacy);
        Act event = PatientTestHelper.createEvent(patient, clinician);
        FinancialAct item = createItem(product1, ONE, event);

        List<Act> items = Arrays.<Act>asList(item);
        placer.initialise(items);

        PatientHistoryChanges changes = new PatientHistoryChanges(clinician, location, getArchetypeService());
        ActBean bean = new ActBean(item);
        bean.setNodeParticipant("product", product2);

        placer.order(items, changes);

        List<TestPharmacyOrderService.Order> orders = service.getOrders();
        assertEquals(2, orders.size());
        checkOrder(orders.get(0), CANCEL, patient, product1, ONE, item.getId(),
                   item.getActivityStartTime(), clinician, pharmacy);
        checkOrder(orders.get(1), CREATE, patient, product2, ONE, item.getId(),
                   item.getActivityStartTime(), clinician, pharmacy);
    }

    /**
     * Verifies that when a product is changed to a non-pharmacy product, the order is cancelled.
     */
    @Test
    public void testChangeProductToNonPharmacyProduct() {
        Product product1 = createProduct(pharmacy);
        Product product2 = TestHelper.createProduct();
        Act event = PatientTestHelper.createEvent(patient, clinician);
        FinancialAct item = createItem(product1, ONE, event);

        List<Act> items = Arrays.<Act>asList(item);
        placer.initialise(items);

        PatientHistoryChanges changes = new PatientHistoryChanges(clinician, location, getArchetypeService());
        ActBean bean = new ActBean(item);
        bean.setNodeParticipant("product", product2);

        placer.order(items, changes);

        List<TestPharmacyOrderService.Order> orders = service.getOrders();
        assertEquals(1, orders.size());
        checkOrder(orders.get(0), CANCEL, patient, product1, ONE, item.getId(),
                   item.getActivityStartTime(), clinician, pharmacy);
    }

    /**
     * Verifies that when a product and pharmacy is changed, a cancellation is issued to the old pharmacy, and a new
     * order is created.
     */
    @Test
    public void testChangeProductAndPharmacy() {
        Product product1 = createProduct(pharmacy);
        Act event = PatientTestHelper.createEvent(patient, clinician);
        FinancialAct item = createItem(product1, ONE, event);

        List<Act> items = Arrays.<Act>asList(item);
        placer.initialise(items);

        Entity pharmacy2 = CustomerChargeTestHelper.createPharmacy();
        Product product2 = createProduct(pharmacy2);

        ActBean bean = new ActBean(item);
        bean.setNodeParticipant("product", product2);

        PatientHistoryChanges changes = new PatientHistoryChanges(clinician, location, getArchetypeService());
        placer.order(items, changes);

        List<TestPharmacyOrderService.Order> orders = service.getOrders();
        assertEquals(2, orders.size());
        checkOrder(orders.get(0), CANCEL, patient, product1, ONE, item.getId(),
                   item.getActivityStartTime(), clinician, pharmacy);
        checkOrder(orders.get(1), CREATE, patient, product2, ONE, item.getId(),
                   item.getActivityStartTime(), clinician, pharmacy2);
    }

    /**
     * Verifies that when a clinician is changed, an update order is placed.
     */
    @Test
    public void testChangeClinician() {
        Product product = createProduct(pharmacy);
        User clinician2 = TestHelper.createClinician();
        Act event = PatientTestHelper.createEvent(patient, clinician);
        FinancialAct item = createItem(product, ONE, event);

        List<Act> items = Arrays.<Act>asList(item);
        placer.initialise(items);

        PatientHistoryChanges changes = new PatientHistoryChanges(clinician, location, getArchetypeService());
        ActBean bean = new ActBean(item);
        bean.setNodeParticipant("clinician", clinician2);
        bean.save();

        placer.order(items, changes);

        List<TestPharmacyOrderService.Order> orders = service.getOrders();
        assertEquals(1, orders.size());
        checkOrder(orders.get(0), UPDATE, patient, product, ONE, item.getId(),
                   item.getActivityStartTime(), clinician2, pharmacy);
    }

    /**
     * Verifies that if an item is removed, a cancellation is generated.
     */
    @Test
    public void testOrderWithCancel() {
        Product product1 = createProduct(pharmacy);
        Product product2 = createProduct(pharmacy);
        Act event = PatientTestHelper.createEvent(patient, clinician);
        Act item1 = createItem(product1, ONE, event);
        Act item2 = createItem(product2, TEN, event);

        placer.initialise(Arrays.asList(item1));

        PatientHistoryChanges changes = new PatientHistoryChanges(clinician, location, getArchetypeService());
        placer.order(Arrays.asList(item2), changes);

        List<TestPharmacyOrderService.Order> orders = service.getOrders();
        assertEquals(2, orders.size());
        checkOrder(orders.get(0), CREATE, patient, product2, TEN, item2.getId(),
                   item2.getActivityStartTime(), clinician, pharmacy);
        checkOrder(orders.get(1), CANCEL, patient, product1, ONE, item1.getId(),
                   item1.getActivityStartTime(), clinician, pharmacy);
    }

    /**
     * Tests the {@link PharmacyOrderPlacer#cancel} method.
     */
    @Test
    public void testCancel() {
        Product product1 = createProduct(pharmacy);
        Product product2 = createProduct(pharmacy);
        Product product3 = TestHelper.createProduct(); // not ordered via a pharmacy
        Act event = PatientTestHelper.createEvent(patient, clinician);
        Act item1 = createItem(product1, ONE, event);
        Act item2 = createItem(product2, TEN, event);
        Act item3 = createItem(product3, BigDecimal.valueOf(2), event);

        List<Act> items = Arrays.asList(item1, item2, item3);
        placer.initialise(items);
        placer.cancel();

        List<TestPharmacyOrderService.Order> orders = service.getOrders(true);
        assertEquals(2, orders.size());
        checkOrder(orders.get(0), CANCEL, patient, product1, ONE, item1.getId(), item1.getActivityStartTime(),
                   clinician, pharmacy);
        checkOrder(orders.get(1), CANCEL, patient, product2, TEN, item2.getId(), item2.getActivityStartTime(),
                   clinician, pharmacy);
    }

    /**
     * Creates an invoice item linked to an event.
     *
     * @param product  the product
     * @param quantity the quantity
     * @param event    the event
     * @return a new invoice item
     */
    private FinancialAct createItem(Product product, BigDecimal quantity, Act event) {
        FinancialAct item = createItem(product, quantity);
        addChargeItem(event, item);
        return item;
    }

    /**
     * Adds a charge item to an event.
     *
     * @param event the event
     * @param item  the charge item
     */
    private void addChargeItem(Act event, FinancialAct item) {
        ActBean itemBean = new ActBean(item);
        for (IMObject object : itemBean.getValues("event")) {
            itemBean.removeValue("event", object);
        }
        ActBean bean = new ActBean(event);
        bean.addNodeRelationship("chargeItems", item);
        save(event, item);
    }

    /**
     * Creates a charge item.
     *
     * @param product  the product
     * @param quantity the quantity
     * @return a new charge item
     */
    private FinancialAct createItem(Product product, BigDecimal quantity) {
        FinancialAct item = FinancialTestHelper.createItem(CustomerAccountArchetypes.INVOICE_ITEM, Money.ONE, patient,
                                                           product);
        ActBean bean = new ActBean(item);
        bean.setNodeParticipant("clinician", clinician);
        item.setQuantity(quantity);
        save(item);
        return item;
    }

    /**
     * Creates a product dispensed via a pharmacy.
     *
     * @param pharmacy the pharmacy
     * @return a new product
     */
    private Product createProduct(Entity pharmacy) {
        Product product = TestHelper.createProduct();
        EntityBean bean = new EntityBean(product);
        bean.addNodeTarget("pharmacy", pharmacy);
        bean.save();
        return product;
    }

}
