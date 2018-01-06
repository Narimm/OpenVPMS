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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.charge;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.finance.invoice.ChargeItemEventLinker;
import org.openvpms.archetype.rules.patient.InvestigationActStatus;
import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.archetype.rules.patient.PatientHistoryChanges;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.system.common.cache.MapIMObjectCache;
import org.openvpms.hl7.impl.LaboratoriesImpl;
import org.openvpms.hl7.impl.PharmaciesImpl;
import org.openvpms.hl7.io.Connectors;
import org.openvpms.hl7.laboratory.Laboratories;
import org.openvpms.hl7.patient.PatientContext;
import org.openvpms.hl7.patient.PatientContextFactory;
import org.openvpms.hl7.patient.PatientEventServices;
import org.openvpms.hl7.patient.PatientInformationService;
import org.openvpms.hl7.pharmacy.Pharmacies;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.test.AbstractAppTest;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.openvpms.web.workspace.customer.charge.CustomerChargeTestHelper.checkOrder;
import static org.openvpms.web.workspace.customer.charge.TestPharmacyOrderService.Order.Type.CANCEL;
import static org.openvpms.web.workspace.customer.charge.TestPharmacyOrderService.Order.Type.CREATE;
import static org.openvpms.web.workspace.customer.charge.TestPharmacyOrderService.Order.Type.UPDATE;

/**
 * Tests the {@link OrderPlacer}.
 *
 * @author Tim Anderson
 */
public class OrderPlacerTestCase extends AbstractAppTest {

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
     * The laboratory.
     */
    private Entity laboratory;

    /**
     * The pharmacy order service.
     */
    private TestPharmacyOrderService pharmacyOrderService;

    /**
     * The laboratory order service.
     */
    private TestLaboratoryOrderService laboratoryOrderService;

    /**
     * The order placer.
     */
    private OrderPlacer placer;

    /**
     * The patient information service.
     */
    private PatientInformationService informationService;

    /**
     * The user.
     */
    private User user;

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
        pharmacy = CustomerChargeTestHelper.createPharmacy(location);
        laboratory = CustomerChargeTestHelper.createLaboratory(location);
        pharmacyOrderService = new TestPharmacyOrderService();
        laboratoryOrderService = new TestLaboratoryOrderService();
        user = TestHelper.createUser();
        Connectors connectors = Mockito.mock(Connectors.class);
        PatientEventServices patientEventServices = Mockito.mock(PatientEventServices.class);
        Pharmacies pharmacies = new PharmaciesImpl(getArchetypeService(), connectors, patientEventServices);
        Laboratories laboratories = new LaboratoriesImpl(getArchetypeService(), connectors, patientEventServices);
        PatientContextFactory factory = ServiceHelper.getBean(PatientContextFactory.class);
        informationService = Mockito.mock(PatientInformationService.class);
        MapIMObjectCache cache = new MapIMObjectCache(ServiceHelper.getArchetypeService());

        OrderServices services = new OrderServices(pharmacyOrderService, pharmacies, laboratoryOrderService,
                                                   laboratories, factory, informationService,
                                                   ServiceHelper.getBean(MedicalRecordRules.class));
        placer = new OrderPlacer(customer, location, user, cache, services);
    }

    /**
     * Tests the {@link OrderPlacer#order(List, PatientHistoryChanges)} method for a pharmacy order.
     */
    @Test
    public void testPharmacyOrder() {
        Entity productType = createProductType(pharmacy);
        Product product1 = createPharmacyProduct(pharmacy);
        Product product2 = ProductTestHelper.createMedication(productType);
        Product product3 = TestHelper.createProduct(); // not ordered via a pharmacy
        Act event = PatientTestHelper.createEvent(patient, clinician);
        Act item1 = createItem(product1, ONE, event);
        Act item2 = createItem(product2, TEN, event);
        Act item3 = createItem(product3, BigDecimal.valueOf(2), event);

        PatientHistoryChanges changes = new PatientHistoryChanges(clinician, location, getArchetypeService());

        placer.order(Arrays.asList(item1, item2, item3), changes);

        List<TestPharmacyOrderService.Order> orders = pharmacyOrderService.getOrders();
        assertEquals(2, orders.size());
        checkOrder(orders.get(0), CREATE, patient, product1, ONE, item1.getId(), item1.getActivityStartTime(),
                   clinician, pharmacy);
        checkOrder(orders.get(1), CREATE, patient, product2, TEN, item2.getId(), item2.getActivityStartTime(),
                   clinician, pharmacy);

        // patient information updates should not be sent
        verify(informationService, times(0)).updated(Mockito.<PatientContext>any(), eq(user));
    }

    /**
     * Tests creation of a laboratory order.
     */
    @Test
    public void testLaboratoryOrder() {
        Entity investigationType = ProductTestHelper.createInvestigationType(laboratory, "1234567890");
        Product product = createLaboratoryProduct(investigationType);
        Act event = PatientTestHelper.createEvent(patient, clinician);
        FinancialAct item1 = createItem(product, ONE, event);
        Act investigation = createInvestigation(investigationType, event);
        ActBean bean = new ActBean(item1);
        bean.addNodeRelationship("investigations", investigation);
        save(item1, investigation);
        assertEquals(InvestigationActStatus.PENDING, investigation.getStatus2());

        PatientHistoryChanges changes = new PatientHistoryChanges(clinician, location, getArchetypeService());
        placer.order(Arrays.asList(item1, investigation), changes);

        List<TestLaboratoryOrderService.LabOrder> orders = laboratoryOrderService.getOrders();
        assertEquals(1, orders.size());
        checkOrder(orders.get(0), TestLaboratoryOrderService.LabOrder.Type.CREATE, patient, investigation.getId(),
                   investigation.getActivityStartTime(), clinician, laboratory);
        assertEquals(InvestigationActStatus.SENT, investigation.getStatus2());

        // patient information updates should not be sent
        verify(informationService, times(0)).updated(Mockito.<PatientContext>any(), eq(user));
    }

    /**
     * Verifies that when a quantity is changed, an update order is placed.
     */
    @Test
    public void testChangeQuantity() {
        Product product = createPharmacyProduct(pharmacy);
        Act event = PatientTestHelper.createEvent(patient, clinician);
        FinancialAct item = createItem(product, ONE, event);

        placer.initialise(item);

        PatientHistoryChanges changes = new PatientHistoryChanges(clinician, location, getArchetypeService());
        item.setQuantity(TEN);

        placer.order(Collections.<Act>singletonList(item), changes);

        List<TestPharmacyOrderService.Order> orders = pharmacyOrderService.getOrders();
        assertEquals(1, orders.size());
        checkOrder(orders.get(0), UPDATE, patient, product, TEN, item.getId(),
                   item.getActivityStartTime(), clinician, pharmacy);

        // patient information updates should not be sent
        verify(informationService, times(0)).updated(Mockito.<PatientContext>any(), eq(user));
    }

    /**
     * Verifies that when a patient is changed, a cancellation and new order is generated.
     */
    @Test
    public void testChangePatient() {
        Product product = createPharmacyProduct(pharmacy);
        Act event1 = PatientTestHelper.createEvent(patient, clinician);
        FinancialAct item = createItem(product, ONE, event1);
        Party patient2 = TestHelper.createPatient(true);

        List<Act> items = Collections.<Act>singletonList(item);
        placer.initialise(item);

        PatientHistoryChanges changes = new PatientHistoryChanges(clinician, location, getArchetypeService());
        ActBean bean = new ActBean(item);
        bean.setNodeParticipant("patient", patient2);

        Act event2 = PatientTestHelper.createEvent(patient2, clinician);
        addChargeItem(event2, item);
        save(event2, item);

        placer.order(items, changes);

        List<TestPharmacyOrderService.Order> orders = pharmacyOrderService.getOrders();
        assertEquals(2, orders.size());
        checkOrder(orders.get(0), CANCEL, patient, product, ONE, item.getId(),
                   item.getActivityStartTime(), clinician, pharmacy);
        checkOrder(orders.get(1), CREATE, patient2, product, ONE, item.getId(),
                   item.getActivityStartTime(), clinician, pharmacy);

        // patient information updates should not be sent
        verify(informationService, times(0)).updated(Mockito.<PatientContext>any(), eq(user));
    }

    /**
     * Verifies that when a product is changed, an update order is placed.
     */
    @Test
    public void testChangeProduct() {
        Product product1 = createPharmacyProduct(pharmacy);
        Product product2 = createPharmacyProduct(pharmacy);
        Act event = PatientTestHelper.createEvent(patient, clinician);
        FinancialAct item = createItem(product1, ONE, event);

        List<Act> items = Collections.<Act>singletonList(item);
        placer.initialise(item);

        PatientHistoryChanges changes = new PatientHistoryChanges(clinician, location, getArchetypeService());
        ActBean bean = new ActBean(item);
        bean.setNodeParticipant("product", product2);

        placer.order(items, changes);

        List<TestPharmacyOrderService.Order> orders = pharmacyOrderService.getOrders();
        assertEquals(2, orders.size());
        checkOrder(orders.get(0), CANCEL, patient, product1, ONE, item.getId(),
                   item.getActivityStartTime(), clinician, pharmacy);
        checkOrder(orders.get(1), CREATE, patient, product2, ONE, item.getId(),
                   item.getActivityStartTime(), clinician, pharmacy);

        // patient information updates should not be sent
        verify(informationService, times(0)).updated(Mockito.<PatientContext>any(), eq(user));
    }

    /**
     * Verifies that when a product is changed to a non-pharmacy product, the order is cancelled.
     * <p/>
     * NOTE: this functionality is no longer supported when editing, as the patient and product are set read-only
     * when an order is issued.
     */
    @Test
    public void testChangeProductToNonPharmacyProduct() {
        Product product1 = createPharmacyProduct(pharmacy);
        Product product2 = TestHelper.createProduct();
        Act event = PatientTestHelper.createEvent(patient, clinician);
        FinancialAct item = createItem(product1, ONE, event);

        List<Act> items = Collections.<Act>singletonList(item);
        placer.initialise(item);

        PatientHistoryChanges changes = new PatientHistoryChanges(clinician, location, getArchetypeService());
        ActBean bean = new ActBean(item);
        bean.setNodeParticipant("product", product2);

        placer.order(items, changes);

        List<TestPharmacyOrderService.Order> orders = pharmacyOrderService.getOrders();
        assertEquals(1, orders.size());
        checkOrder(orders.get(0), CANCEL, patient, product1, ONE, item.getId(),
                   item.getActivityStartTime(), clinician, pharmacy);

        // patient information updates should not be sent
        verify(informationService, times(0)).updated(Mockito.<PatientContext>any(), eq(user));
    }

    /**
     * Verifies that when a product and pharmacy is changed, a cancellation is issued to the old pharmacy, and a new
     * order is created.
     * <p/>
     * NOTE: this functionality is no longer supported when editing, as the patient and product are set read-only
     * when an order is issued.
     */
    @Test
    public void testChangeProductAndPharmacy() {
        Product product1 = createPharmacyProduct(pharmacy);
        Act event = PatientTestHelper.createEvent(patient, clinician);
        FinancialAct item = createItem(product1, ONE, event);

        List<Act> items = Collections.<Act>singletonList(item);
        placer.initialise(item);

        Entity pharmacy2 = CustomerChargeTestHelper.createPharmacy(location);
        Product product2 = createPharmacyProduct(pharmacy2);

        ActBean bean = new ActBean(item);
        bean.setNodeParticipant("product", product2);

        PatientHistoryChanges changes = new PatientHistoryChanges(clinician, location, getArchetypeService());
        placer.order(items, changes);

        List<TestPharmacyOrderService.Order> orders = pharmacyOrderService.getOrders();
        assertEquals(2, orders.size());
        checkOrder(orders.get(0), CANCEL, patient, product1, ONE, item.getId(),
                   item.getActivityStartTime(), clinician, pharmacy);
        checkOrder(orders.get(1), CREATE, patient, product2, ONE, item.getId(),
                   item.getActivityStartTime(), clinician, pharmacy2);

        // patient information updates should not be sent
        verify(informationService, times(0)).updated(Mockito.<PatientContext>any(), eq(user));
    }

    /**
     * Verifies that when a clinician is changed, an update order is placed.
     */
    @Test
    public void testChangeClinician() {
        Product product = createPharmacyProduct(pharmacy);
        User clinician2 = TestHelper.createClinician();
        Act event = PatientTestHelper.createEvent(patient, clinician);
        FinancialAct item = createItem(product, ONE, event);

        List<Act> items = Collections.<Act>singletonList(item);
        placer.initialise(item);

        PatientHistoryChanges changes = new PatientHistoryChanges(clinician, location, getArchetypeService());
        ActBean bean = new ActBean(item);
        bean.setNodeParticipant("clinician", clinician2);
        bean.save();

        placer.order(items, changes);

        List<TestPharmacyOrderService.Order> orders = pharmacyOrderService.getOrders();
        assertEquals(1, orders.size());
        checkOrder(orders.get(0), UPDATE, patient, product, ONE, item.getId(),
                   item.getActivityStartTime(), clinician2, pharmacy);

        // patient information updates should not be sent
        verify(informationService, times(0)).updated(Mockito.<PatientContext>any(), eq(user));
    }

    /**
     * Verifies that if an item is removed, a cancellation is generated.
     */
    @Test
    public void testOrderWithCancel() {
        Product product1 = createPharmacyProduct(pharmacy);
        Product product2 = createPharmacyProduct(pharmacy);
        Act event = PatientTestHelper.createEvent(patient, clinician);
        Act item1 = createItem(product1, ONE, event);
        Act item2 = createItem(product2, TEN, event);

        placer.initialise(item1);

        PatientHistoryChanges changes = new PatientHistoryChanges(clinician, location, getArchetypeService());
        placer.order(Collections.singletonList(item2), changes);

        List<TestPharmacyOrderService.Order> orders = pharmacyOrderService.getOrders();
        assertEquals(2, orders.size());
        checkOrder(orders.get(0), CREATE, patient, product2, TEN, item2.getId(),
                   item2.getActivityStartTime(), clinician, pharmacy);
        checkOrder(orders.get(1), CANCEL, patient, product1, ONE, item1.getId(),
                   item1.getActivityStartTime(), clinician, pharmacy);

        // patient information updates should not be sent
        verify(informationService, times(0)).updated(Mockito.<PatientContext>any(), eq(user));
    }

    /**
     * Tests the {@link OrderPlacer#cancel} method.
     */
    @Test
    public void testCancel() {
        Product product1 = createPharmacyProduct(pharmacy);
        Product product2 = createPharmacyProduct(pharmacy);
        Product product3 = TestHelper.createProduct(); // not ordered via a pharmacy
        Act event = PatientTestHelper.createEvent(patient, clinician);
        Act item1 = createItem(product1, ONE, event);
        Act item2 = createItem(product2, TEN, event);
        Act item3 = createItem(product3, BigDecimal.valueOf(2), event);

        List<Act> items = Arrays.asList(item1, item2, item3);
        for (Act item : items) {
            placer.initialise(item);
        }
        placer.cancel();

        List<TestPharmacyOrderService.Order> orders = pharmacyOrderService.getOrders(true);
        assertEquals(2, orders.size());
        checkOrder(orders.get(0), CANCEL, patient, product1, ONE, item1.getId(), item1.getActivityStartTime(),
                   clinician, pharmacy);
        checkOrder(orders.get(1), CANCEL, patient, product2, TEN, item2.getId(), item2.getActivityStartTime(),
                   clinician, pharmacy);

        // patient information updates should not be sent
        verify(informationService, times(0)).updated(Mockito.<PatientContext>any(), eq(user));
    }

    /**
     * Verifies that {@link PatientInformationService#updated(PatientContext, User)} is invoked if a visit is created
     * during charging.
     */
    @Test
    public void testPatientInformationNotificationForNewEvent() {
        Product product = createPharmacyProduct(pharmacy);
        FinancialAct item = createItem(product, ONE);
        PatientHistoryChanges changes = new PatientHistoryChanges(clinician, location, getArchetypeService());
        ChargeItemEventLinker linker = new ChargeItemEventLinker(getArchetypeService());
        linker.prepare(Collections.singletonList(item), changes);
        changes.save();
        placer.order(Collections.<Act>singletonList(item), changes);

        List<TestPharmacyOrderService.Order> orders = pharmacyOrderService.getOrders();
        assertEquals(1, orders.size());
        checkOrder(orders.get(0), CREATE, patient, product, ONE, item.getId(), item.getActivityStartTime(),
                   clinician, pharmacy);

        // patient information updates should not be sent
        verify(informationService, times(1)).updated(Mockito.<PatientContext>any(), eq(user));
    }

    /**
     * Verifies that {@link PatientInformationService#updated(PatientContext, User)} is invoked if a completed visit is
     * linked to during charging.
     */
    @Test
    public void testPatientInformationNotificationForCompletedEvent() {
        Product product = createPharmacyProduct(pharmacy);
        Act event = PatientTestHelper.createEvent(patient, clinician);
        event.setActivityEndTime(new Date());
        save(event);

        FinancialAct item = createItem(product, ONE);
        PatientHistoryChanges changes = new PatientHistoryChanges(clinician, location, getArchetypeService());
        ChargeItemEventLinker linker = new ChargeItemEventLinker(getArchetypeService());
        linker.prepare(Collections.singletonList(item), changes);
        changes.save();
        placer.order(Collections.<Act>singletonList(item), changes);

        List<TestPharmacyOrderService.Order> orders = pharmacyOrderService.getOrders();
        assertEquals(1, orders.size());
        checkOrder(orders.get(0), CREATE, patient, product, ONE, item.getId(), item.getActivityStartTime(),
                   clinician, pharmacy);

        // patient information updates should not be sent
        verify(informationService, times(1)).updated(Mockito.<PatientContext>any(), eq(user));
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
     * Creates an investigation linked to an event.
     *
     * @param investigationType the investigation type
     * @param event             the event
     * @return a new investigation
     */
    private Act createInvestigation(Entity investigationType, Act event) {
        Act investigation = PatientTestHelper.createInvestigation(patient, clinician, investigationType);
        ActBean bean = new ActBean(event);
        bean.addNodeRelationship("items", investigation);
        save(event, investigation);
        return investigation;
    }

    /**
     * Adds a charge item to an event.
     *
     * @param event the event
     * @param item  the charge item
     */
    private void addChargeItem(Act event, FinancialAct item) {
        ActBean itemBean = new ActBean(item);
        for (IMObject object : itemBean.getValues("event", IMObject.class)) {
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
        FinancialAct item = FinancialTestHelper.createChargeItem(CustomerAccountArchetypes.INVOICE_ITEM, patient,
                                                                 product, BigDecimal.ONE);
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
    private Product createPharmacyProduct(Entity pharmacy) {
        Product product = TestHelper.createProduct();
        EntityBean bean = new EntityBean(product);
        bean.addNodeTarget("pharmacy", pharmacy);
        bean.save();
        return product;
    }

    /**
     * Creates a product ordered via a laboratory.
     *
     * @param investigationType the investigation type
     * @return a new product
     */
    private Product createLaboratoryProduct(Entity investigationType) {
        Product product = TestHelper.createProduct(ProductArchetypes.SERVICE, null);
        EntityBean bean = new EntityBean(product);
        bean.addNodeTarget("investigationTypes", investigationType);
        bean.save();
        return product;
    }

    /**
     * Creates a product type linked to a pharmacy.
     *
     * @param pharmacy the pharmacy
     * @return a new product type
     */
    private Entity createProductType(Entity pharmacy) {
        Entity productType = ProductTestHelper.createProductType("ZTestProductType");
        EntityBean bean = new EntityBean(productType);
        bean.addNodeTarget("pharmacy", pharmacy);
        bean.save();
        return productType;
    }

}
