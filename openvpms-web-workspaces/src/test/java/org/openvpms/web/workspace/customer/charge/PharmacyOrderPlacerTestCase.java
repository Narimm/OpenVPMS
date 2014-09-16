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
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.hl7.PatientContext;
import org.openvpms.hl7.PharmacyOrderService;
import org.openvpms.web.component.im.util.DefaultIMObjectCache;
import org.openvpms.web.test.AbstractAppTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static org.junit.Assert.assertEquals;

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
    private OrderService service;

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
        pharmacy = createPharmacy();
        service = new OrderService();
        placer = new PharmacyOrderPlacer(customer, location, new DefaultIMObjectCache(), service);
    }

    private Entity createPharmacy() {
        Entity pharmacy = (Entity) create("party.organisationPharmacy");
        pharmacy.setName("ZPharmacy");
        save(pharmacy);
        return pharmacy;
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

        List<Order> orders = service.getOrders();
        assertEquals(2, orders.size());
        checkOrder(orders.get(0), Order.Type.CREATE, patient, product1, ONE, item1.getId(),
                   item1.getActivityStartTime(), clinician, pharmacy);
        checkOrder(orders.get(1), Order.Type.CREATE, patient, product2, TEN, item2.getId(),
                   item2.getActivityStartTime(), clinician, pharmacy);
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
        item.setQuantity(BigDecimal.TEN);

        placer.order(items, changes);

        List<Order> orders = service.getOrders();
        assertEquals(1, orders.size());
        checkOrder(orders.get(0), Order.Type.UPDATE, patient, product, TEN, item.getId(),
                   item.getActivityStartTime(), clinician, pharmacy);
    }

    /**
     * Verifies that when a patient is changed, an update order is placed.
     */
    @Test
    public void testChangePatient() {
        Product product = createProduct(pharmacy);
        Party patient2 = TestHelper.createPatient(true);
        FinancialAct item = createItem(product, ONE);

        List<Act> items = Arrays.<Act>asList(item);
        placer.initialise(items);

        PatientHistoryChanges changes = new PatientHistoryChanges(clinician, location, getArchetypeService());
        ActBean bean = new ActBean(item);
        bean.setNodeParticipant("patient", patient2);

        Act event = PatientTestHelper.createEvent(patient2, clinician);
        addChargeItem(event, item);
        save(event, item);

        placer.order(items, changes);

        List<Order> orders = service.getOrders();
        assertEquals(1, orders.size());
        checkOrder(orders.get(0), Order.Type.UPDATE, patient2, product, ONE, item.getId(),
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

        List<Order> orders = service.getOrders();
        assertEquals(1, orders.size());
        checkOrder(orders.get(0), Order.Type.UPDATE, patient, product2, ONE, item.getId(),
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

        List<Order> orders = service.getOrders();
        assertEquals(1, orders.size());
        checkOrder(orders.get(0), Order.Type.CANCEL, patient, product1, ONE, item.getId(),
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

        Entity pharmacy2 = createPharmacy();
        Product product2 = createProduct(pharmacy2);

        ActBean bean = new ActBean(item);
        bean.setNodeParticipant("product", product2);

        PatientHistoryChanges changes = new PatientHistoryChanges(clinician, location, getArchetypeService());
        placer.order(items, changes);

        List<Order> orders = service.getOrders();
        assertEquals(2, orders.size());
        checkOrder(orders.get(0), Order.Type.CANCEL, patient, product1, ONE, item.getId(),
                   item.getActivityStartTime(), clinician, pharmacy);
        checkOrder(orders.get(1), Order.Type.CREATE, patient, product2, ONE, item.getId(),
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

        List<Order> orders = service.getOrders();
        assertEquals(1, orders.size());
        checkOrder(orders.get(0), Order.Type.UPDATE, patient, product, ONE, item.getId(),
                   item.getActivityStartTime(), clinician2, pharmacy);
    }

    /**
     * Verifies an order matches that expected.
     *
     * @param order             the order
     * @param type              the expected type
     * @param patient           the expected patient
     * @param product           the expected product
     * @param quantity          the expected quantity
     * @param placerOrderNumber the expected placer order number
     * @param date              the expected date
     * @param clinician         the expected clinician
     * @param pharmacy          the expected pharmacy
     */
    private void checkOrder(Order order, Order.Type type, Party patient, Product product, BigDecimal quantity,
                            long placerOrderNumber, Date date, User clinician, Entity pharmacy) {
        assertEquals(type, order.type);
        assertEquals(patient, order.patient);
        assertEquals(product, order.product);
        checkEquals(quantity, order.quantity);
        assertEquals(placerOrderNumber, order.placerOrderNumber);
        assertEquals(date, order.date);
        assertEquals(clinician, order.clinician);
        assertEquals(pharmacy, order.pharmacy);
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

    private static class Order {

        enum Type {
            CREATE,
            UPDATE,
            CANCEL
        }

        private final Type type;

        private final Party patient;

        private final Product product;

        private final BigDecimal quantity;

        private final long placerOrderNumber;

        private final Date date;

        private final User clinician;

        private final Entity pharmacy;

        public Order(Type type, Party patient, Product product, BigDecimal quantity, long placerOrderNumber,
                     Date date, User clinician, Entity pharmacy) {
            this.type = type;
            this.patient = patient;
            this.product = product;
            this.quantity = quantity;
            this.placerOrderNumber = placerOrderNumber;
            this.date = date;
            this.clinician = clinician;
            this.pharmacy = pharmacy;
        }

    }

    private static class OrderService implements PharmacyOrderService {

        private List<Order> orders = new ArrayList<Order>();

        /**
         * Creates an order, placing it with the specified pharmacy.
         *
         * @param context           the patient context
         * @param product           the product to order
         * @param quantity          the quantity to order
         * @param placerOrderNumber the placer order number, to uniquely identify the order
         * @param date              the order date
         * @param pharmacy          the pharmacy. A <em>party.organisationPharmacy</em>
         */
        @Override
        public void createOrder(PatientContext context, Product product, BigDecimal quantity, long placerOrderNumber,
                                Date date, Entity pharmacy) {
            orders.add(new Order(Order.Type.CREATE, context.getPatient(), product, quantity, placerOrderNumber, date,
                                 context.getClinician(), pharmacy));
        }

        /**
         * Updates an order.
         *
         * @param context           the patient context
         * @param product           the product to order
         * @param quantity          the quantity to order
         * @param placerOrderNumber the placer order number, to uniquely identify the order
         * @param date              the order date
         * @param pharmacy          the pharmacy. A <em>party.organisationPharmacy</em>
         */
        @Override
        public void updateOrder(PatientContext context, Product product, BigDecimal quantity, long placerOrderNumber,
                                Date date, Entity pharmacy) {
            orders.add(new Order(Order.Type.UPDATE, context.getPatient(), product, quantity, placerOrderNumber, date,
                                 context.getClinician(), pharmacy));
        }

        /**
         * Cancels an order.
         *
         * @param context           the patient context
         * @param product           the product to order
         * @param quantity          the quantity to order
         * @param placerOrderNumber the placer order number, to uniquely identify the order
         * @param date              the order date
         * @param pharmacy          the pharmacy. A <em>party.organisationPharmacy</em>
         */
        @Override
        public void cancelOrder(PatientContext context, Product product, BigDecimal quantity, long placerOrderNumber,
                                Date date, Entity pharmacy) {
            orders.add(new Order(Order.Type.CANCEL, context.getPatient(), product, quantity, placerOrderNumber, date,
                                 context.getClinician(), pharmacy));
        }

        /**
         * Returns the orders.
         *
         * @return the orders
         */
        public List<Order> getOrders() {
            return orders;
        }

    }
}
