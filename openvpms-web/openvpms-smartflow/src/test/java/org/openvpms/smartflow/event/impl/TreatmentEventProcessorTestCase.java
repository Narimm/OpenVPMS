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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.smartflow.event.impl;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.order.CustomerPharmacyOrder;
import org.openvpms.archetype.rules.finance.order.OrderArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.smartflow.model.Treatment;
import org.openvpms.smartflow.model.TreatmentList;
import org.openvpms.smartflow.model.event.TreatmentEvent;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link TreatmentEventProcessor}.
 *
 * @author Tim Anderson
 */
public class TreatmentEventProcessorTestCase extends ArchetypeServiceTest {

    /**
     * The test location.
     */
    private Party location;

    /**
     * The test customer.
     */
    private Party customer;

    /**
     * The test patient.
     */
    private Party patient;

    /**
     * The test visit.
     */
    private Act visit;

    /**
     * The test product.
     */
    private Product product;

    /**
     * The event processor.
     */
    private TreatmentEventProcessor processor;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        location = TestHelper.createLocation();
        customer = TestHelper.createCustomer();
        patient = TestHelper.createPatient(customer);
        product = TestHelper.createProduct();
        visit = PatientTestHelper.createEvent(new Date(), patient);
        PatientRules rules = applicationContext.getBean(PatientRules.class);
        processor = new TreatmentEventProcessor(location, getArchetypeService(), rules);
    }

    /**
     * Tests {@link TreatmentEvent}s.
     */
    @Test
    public void testTreatment() {
        // create a treatment event and process it
        String treatmentGuid = UUID.randomUUID().toString();
        TreatmentEvent event1 = createTreatment(treatmentGuid, visit, product, BigDecimal.ONE, Treatment.ADDED_STATUS);

        // verify an order has been created
        List<CustomerPharmacyOrder> order1 = process(event1);
        assertEquals(1, order1.size());
        checkOrder(order1.get(0), customer, patient, product, BigDecimal.ONE, location);

        // now amend the treatment for a larger quantity
        TreatmentEvent event2 = createTreatment(treatmentGuid, visit, product, BigDecimal.TEN,
                                                Treatment.CHANGED_STATUS);
        List<CustomerPharmacyOrder> order2 = process(event2);
        assertEquals(1, order2.size());
        checkOrder(order2.get(0), customer, patient, product, BigDecimal.TEN, location);

        // now amend the treatment to a lesser quantity
        TreatmentEvent event3 = createTreatment(treatmentGuid, visit, product, BigDecimal.ONE,
                                                Treatment.CHANGED_STATUS);
        List<CustomerPharmacyOrder> order3 = process(event3);
        assertEquals(1, order3.size());
        checkOrder(order3.get(0), customer, patient, product, BigDecimal.ONE, location);

        // now remove the treatment
        TreatmentEvent event4 = createTreatment(treatmentGuid, visit, product, BigDecimal.ONE,
                                                Treatment.REMOVED_STATUS);
        List<CustomerPharmacyOrder> order4 = process(event4);
        assertEquals(0, order4.size());
    }

    /**
     * Verifies that a new order is created if a treatment is amended with a greater quantity and the original order
     * is POSTED.
     * <p/>
     * The new order has the difference in quantity.
     */
    @Test
    public void testIncreaseQuantityForPostedOrder() {
        // create a treatment event and process it
        String treatmentGuid = UUID.randomUUID().toString();
        TreatmentEvent event1 = createTreatment(treatmentGuid, visit, product, BigDecimal.ONE, Treatment.ADDED_STATUS);

        // verify an order has been created and post it.
        List<CustomerPharmacyOrder> order1 = process(event1);
        assertEquals(1, order1.size());
        checkOrder(order1.get(0), customer, patient, product, BigDecimal.ONE, location);
        post(order1.get(0));

        TreatmentEvent event2 = createTreatment(treatmentGuid, visit, product, BigDecimal.TEN,
                                                Treatment.CHANGED_STATUS);
        List<CustomerPharmacyOrder> order2 = process(event2);
        assertEquals(2, order2.size());
        checkOrder(order2.get(0), customer, patient, product, BigDecimal.ONE, location);        // the original order
        checkOrder(order2.get(1), customer, patient, product, BigDecimal.valueOf(9), location); // the new order

        // increase the quantity. The new order should be updated
        TreatmentEvent event3 = createTreatment(treatmentGuid, visit, product, BigDecimal.valueOf(11),
                                                Treatment.CHANGED_STATUS);
        List<CustomerPharmacyOrder> order3 = process(event3);
        assertEquals(2, order3.size());
        checkOrder(order3.get(0), customer, patient, product, BigDecimal.ONE, location); // the original order
        checkOrder(order3.get(1), customer, patient, product, BigDecimal.TEN, location); // the new order

        // reduce the quantity. The new order should be updated
        TreatmentEvent event4 = createTreatment(treatmentGuid, visit, product, BigDecimal.TEN,
                                                Treatment.CHANGED_STATUS);
        List<CustomerPharmacyOrder> order4 = process(event4);
        assertEquals(2, order4.size());
        checkOrder(order4.get(0), customer, patient, product, BigDecimal.ONE, location);        // the original order
        checkOrder(order4.get(1), customer, patient, product, BigDecimal.valueOf(9), location); // the new order

        // now reduce the quantity to that of the POSTED order. The IN_PROGRESS order should be removed
        TreatmentEvent event5 = createTreatment(treatmentGuid, visit, product, BigDecimal.ONE,
                                                Treatment.CHANGED_STATUS);
        List<CustomerPharmacyOrder> order5 = process(event5);
        assertEquals(1, order5.size());
        checkOrder(order4.get(0), customer, patient, product, BigDecimal.ONE, location);        // the original order
    }

    /**
     * Verifies that a new order return is created if a treatment is amended with a lesser quantity and the original
     * order is POSTED.
     * <p/>
     * The order return has the difference in quantity.
     */
    @Test
    public void testReduceQuantityForPostedOrder() {
        // create a treatment event and process it
        String treatmentGuid = UUID.randomUUID().toString();
        TreatmentEvent event1 = createTreatment(treatmentGuid, visit, product, BigDecimal.TEN, Treatment.ADDED_STATUS);

        // verify an order has been created and post it.
        List<CustomerPharmacyOrder> order1 = process(event1);
        assertEquals(1, order1.size());
        checkOrder(order1.get(0), customer, patient, product, BigDecimal.TEN, location);
        post(order1.get(0));

        // now reduce the quantity
        TreatmentEvent event2 = createTreatment(treatmentGuid, visit, product, BigDecimal.ONE,
                                                Treatment.CHANGED_STATUS);
        List<CustomerPharmacyOrder> order2 = process(event2);
        assertEquals(2, order2.size());
        checkOrder(order2.get(0), customer, patient, product, BigDecimal.TEN, location);         // the original order
        checkReturn(order2.get(1), customer, patient, product, BigDecimal.valueOf(9), location); // the new order return

        // now increase the quantity, but still less than that of the original order. The return should be updated
        TreatmentEvent event3 = createTreatment(treatmentGuid, visit, product, BigDecimal.valueOf(2),
                                                Treatment.CHANGED_STATUS);
        List<CustomerPharmacyOrder> order3 = process(event3);
        assertEquals(2, order3.size());
        checkOrder(order3.get(0), customer, patient, product, BigDecimal.TEN, location);         // the original order
        checkReturn(order3.get(1), customer, patient, product, BigDecimal.valueOf(8), location); // the new order return

        // now increase the quantity, greater than that of the original order. The return should be deleted, and
        // a new order created containing the difference
        TreatmentEvent event4 = createTreatment(treatmentGuid, visit, product, BigDecimal.valueOf(11),
                                                Treatment.CHANGED_STATUS);
        List<CustomerPharmacyOrder> order4 = process(event4);
        assertEquals(2, order4.size());
        checkOrder(order4.get(0), customer, patient, product, BigDecimal.TEN, location);  // the original order
        checkOrder(order4.get(1), customer, patient, product, BigDecimal.ONE, location);  // the new order
    }

    /**
     * Verifies that an IN_PROGRESS order is removed and a new order return is created if a treatment is amended with a
     * lesser quantity and the original order is POSTED.
     * <p/>
     * The order return has the difference in quantity.
     */
    @Test
    public void testReduceQuantityForPostedOrderWithInProgressOrder() {
        // create a treatment event and process it
        String treatmentGuid = UUID.randomUUID().toString();
        BigDecimal four = BigDecimal.valueOf(4);
        TreatmentEvent event1 = createTreatment(treatmentGuid, visit, product, four, Treatment.ADDED_STATUS);

        // verify an order has been created and post it.
        List<CustomerPharmacyOrder> order1 = process(event1);
        assertEquals(1, order1.size());
        checkOrder(order1.get(0), customer, patient, product, four, location);
        post(order1.get(0));

        // now increase the quantity
        TreatmentEvent event2 = createTreatment(treatmentGuid, visit, product, BigDecimal.TEN,
                                                Treatment.CHANGED_STATUS);
        List<CustomerPharmacyOrder> order2 = process(event2);
        assertEquals(2, order2.size());
        checkOrder(order2.get(0), customer, patient, product, four, location);                  // the original order
        checkOrder(order2.get(1), customer, patient, product, BigDecimal.valueOf(6), location); // the new order

        // now reduce the quantity, to less than that of the original order. The IN_PROGRESS order should be deleted
        // and a return created
        TreatmentEvent event3 = createTreatment(treatmentGuid, visit, product, BigDecimal.ONE,
                                                Treatment.CHANGED_STATUS);
        List<CustomerPharmacyOrder> order3 = process(event3);
        assertEquals(2, order3.size());
        checkOrder(order3.get(0), customer, patient, product, four, location);         // the original order
        checkReturn(order3.get(1), customer, patient, product, BigDecimal.valueOf(3), location); // the new order return
    }

    /**
     * Verifies that if there is an order return that has been POSTED, and the treatment is subsequently changed,
     * a new order is created to reflect the difference.
     */
    @Test
    public void testTreatmentChangedForPostedReturn() {
        // create a treatment event and process it
        String treatmentGuid = UUID.randomUUID().toString();
        TreatmentEvent event1 = createTreatment(treatmentGuid, visit, product, BigDecimal.TEN, Treatment.ADDED_STATUS);

        // verify an order has been created and post it.
        List<CustomerPharmacyOrder> order1 = process(event1);
        assertEquals(1, order1.size());
        checkOrder(order1.get(0), customer, patient, product, BigDecimal.TEN, location);
        post(order1.get(0));

        // now reduce the quantity, and verify a return is created
        TreatmentEvent event2 = createTreatment(treatmentGuid, visit, product, BigDecimal.ONE,
                                                Treatment.CHANGED_STATUS);
        List<CustomerPharmacyOrder> order2 = process(event2);
        assertEquals(2, order2.size());
        checkOrder(order2.get(0), customer, patient, product, BigDecimal.TEN, location);         // the original order

        ActBean returnItem = checkReturn(order2.get(1), customer, patient, product, BigDecimal.valueOf(9), location);
        // the new order return

        // change the return quantity and post it, to simulate a user editing it
        returnItem.setValue("quantity", BigDecimal.valueOf(8));
        returnItem.save();
        post(order2.get(1));

        TreatmentEvent event3 = createTreatment(treatmentGuid, visit, product, BigDecimal.valueOf(3),
                                                Treatment.CHANGED_STATUS);
        List<CustomerPharmacyOrder> order3 = process(event3);
        assertEquals(3, order3.size());
        checkOrder(order3.get(0), customer, patient, product, BigDecimal.TEN, location);  // the original order
        checkReturn(order3.get(1), customer, patient, product, BigDecimal.valueOf(8), location); // the changed return
        checkOrder(order3.get(2), customer, patient, product, BigDecimal.ONE, location);  // the new order
    }

    /**
     * Verifies that an IN_PROGRESS order is removed when a treatment is removed.
     */
    @Test
    public void testTreatmentRemovedForInProgressOrder() {
        // create a treatment event and process it
        String treatmentGuid = UUID.randomUUID().toString();
        TreatmentEvent event1 = createTreatment(treatmentGuid, visit, product, BigDecimal.TEN, Treatment.ADDED_STATUS);

        // verify an order has been created.
        List<CustomerPharmacyOrder> order1 = process(event1);
        assertEquals(1, order1.size());
        checkOrder(order1.get(0), customer, patient, product, BigDecimal.TEN, location);

        // now remove the treatment and verify the order is also removed
        TreatmentEvent event2 = createTreatment(treatmentGuid, visit, product, BigDecimal.TEN,
                                                Treatment.REMOVED_STATUS);
        List<CustomerPharmacyOrder> order2 = process(event2);
        assertEquals(0, order2.size());
    }

    /**
     * Verifies that a new order return is created if a treatment is removed but the original order has been POSTED.
     */
    @Test
    public void testTreatmentRemovedForPostedOrder() {
        // create a treatment event and process it
        String treatmentGuid = UUID.randomUUID().toString();
        TreatmentEvent event1 = createTreatment(treatmentGuid, visit, product, BigDecimal.TEN, Treatment.ADDED_STATUS);

        // verify an order has been created and post it.
        List<CustomerPharmacyOrder> order1 = process(event1);
        assertEquals(1, order1.size());
        checkOrder(order1.get(0), customer, patient, product, BigDecimal.TEN, location);
        post(order1.get(0));

        TreatmentEvent event2 = createTreatment(treatmentGuid, visit, product, BigDecimal.TEN,
                                                Treatment.REMOVED_STATUS);
        List<CustomerPharmacyOrder> order2 = process(event2);
        assertEquals(2, order2.size());
        checkOrder(order2.get(0), customer, patient, product, BigDecimal.TEN, location);
        checkReturn(order2.get(1), customer, patient, product, BigDecimal.TEN, location);
    }

    /**
     * Helper to process an event and return the associated orders
     *
     * @param event the event
     * @return orders associated with the event
     */
    private List<CustomerPharmacyOrder> process(TreatmentEvent event) {
        processor.process(event);
        String treatmentGuid = event.getObject().getTreatments().get(0).getTreatmentGuid();
        return TreatmentEventProcessor.getOrders(treatmentGuid, getArchetypeService());
    }

    /**
     * Posts an order.
     *
     * @param order the order to post
     */
    private void post(CustomerPharmacyOrder order) {
        ActBean bean = (order.hasOrder()) ? order.getOrder() : order.getReturn();
        bean.setStatus(ActStatus.POSTED);
        bean.save();
    }

    /**
     * Verifies an order matches that expected.
     *
     * @param order    the order
     * @param customer the expected customer
     * @param patient  the expected patient
     * @param product  the expected product
     * @param quantity the expected quantity
     * @param location the expected practice location
     */
    private void checkOrder(CustomerPharmacyOrder order, Party customer, Party patient, Product product,
                            BigDecimal quantity, Party location) {
        assertTrue(order.hasOrder());
        ActBean act = order.getOrder();
        assertEquals(customer, act.getNodeParticipant("customer"));
        ActBean item = order.getItem(product);
        assertNotNull(item);
        assertTrue(item.isA(OrderArchetypes.PHARMACY_ORDER_ITEM));
        assertEquals(patient, item.getNodeParticipant("patient"));
        checkEquals(quantity, item.getBigDecimal("quantity"));
        assertEquals(location, act.getNodeParticipant("location"));
    }

    /**
     * Verifies an order return matches that expected.
     *
     * @param orderReturn the order return
     * @param customer    the expected customer
     * @param patient     the expected patient
     * @param product     the expected product
     * @param quantity    the expected quantity
     * @param location    the expected practice location
     * @return the return item
     */
    private ActBean checkReturn(CustomerPharmacyOrder orderReturn, Party customer, Party patient, Product product,
                                BigDecimal quantity, Party location) {
        assertFalse(orderReturn.hasOrder());
        ActBean act = orderReturn.getReturn();
        assertEquals(customer, act.getNodeParticipant("customer"));
        ActBean item = orderReturn.getItem(product);
        assertNotNull(item);
        assertTrue(item.isA(OrderArchetypes.PHARMACY_RETURN_ITEM));
        assertEquals(patient, item.getNodeParticipant("patient"));
        checkEquals(quantity, item.getBigDecimal("quantity"));
        assertEquals(location, act.getNodeParticipant("location"));
        return item;
    }

    /**
     * Creates a treatment event.
     *
     * @param treatmentGuid the treatment identifier
     * @param visit         the patient visit
     * @param product       the product
     * @param quantity      the quantity
     * @param status        the status
     * @return a new event
     */
    private TreatmentEvent createTreatment(String treatmentGuid, Act visit, Product product, BigDecimal quantity,
                                           String status) {
        Treatment treatment = new Treatment();
        treatment.setTreatmentGuid(treatmentGuid);
        treatment.setHospitalizationId(Long.toString(visit.getId()));
        treatment.setInventoryId(Long.toString(product.getId()));
        treatment.setQty(quantity);
        treatment.setStatus(status);
        TreatmentEvent event = new TreatmentEvent();
        TreatmentList list = new TreatmentList();
        list.setTreatments(Collections.singletonList(treatment));
        event.setObject(list);
        return event;
    }

}
