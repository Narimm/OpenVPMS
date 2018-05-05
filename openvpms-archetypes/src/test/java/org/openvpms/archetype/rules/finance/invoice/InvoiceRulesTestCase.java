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

package org.openvpms.archetype.rules.finance.invoice;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductTestHelper;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.rules.act.ActStatus.COMPLETED;
import static org.openvpms.archetype.rules.act.ActStatus.IN_PROGRESS;
import static org.openvpms.archetype.rules.act.ActStatus.POSTED;


/**
 * Tests the {@link InvoiceRules} class when invoked by the
 * <em>archetypeService.save.act.customerAccountInvoiceItem.after.drl</em> '
 * rule. In order for these tests to be successful, the archetype service
 * must be configured to trigger the above rule.
 *
 * @author Tim Anderson
 */
public class InvoiceRulesTestCase extends ArchetypeServiceTest {

    /**
     * The customer.
     */
    private Party customer;

    /**
     * The patient.
     */
    private Party patient;

    /**
     * The clinician.
     */
    private User clinician;

    /**
     * The investigation types.
     */
    private Set<Entity> investigationTypes;

    /**
     * The document template.
     */
    private Entity template;


    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        customer = TestHelper.createCustomer();
        clinician = TestHelper.createClinician();
        patient = TestHelper.createPatient();
        investigationTypes = new HashSet<>();
        for (int i = 0; i < 4; ++i) {
            investigationTypes.add(ProductTestHelper.createInvestigationType());
        }
        template = createDocumentTemplate();
    }

    /**
     * Verifies that reminders that don't have status 'Completed' are removed
     * when an invoice item is deleted.
     */
    @Test
    public void testRemoveInvoiceItemIncompleteActs() {
        ActBean item = createInvoiceItem();
        item.addNodeParticipation("product", createProduct(true));
        item.save();

        // add investigation acts
        List<Act> investigations = createInvestigationActs();
        for (Act investigation : investigations) {
            item.addNodeRelationship("investigations", investigation);
            save(investigation);
        }

        // add document acts
        ChargeItemDocumentLinker linker = new ChargeItemDocumentLinker((FinancialAct) item.getAct(),
                                                                       getArchetypeService());
        linker.link();

        // add a reminder
        Act reminder = createReminder();
        item.addNodeRelationship("reminders", reminder);

        // add an alert
        Act alert = createAlert();
        item.addNodeRelationship("alerts", alert);

        save(item.getAct(), reminder, alert);

        item = get(item); // reload to ensure the item has saved correctly

        List<Act> documents = item.getNodeActs("documents");
        assertEquals(1, documents.size());

        // remove the item and verify it can't be retrieved
        IMObjectReference actRef = item.getAct().getObjectReference();
        remove(item.getAct());
        assertNull(get(actRef));

        // verify the investigations have been removed
        for (Act investigation : investigations) {
            assertNull(get(investigation));
        }

        // verify the reminder has been removed
        assertNull(get(reminder));

        // verify the alert has been removed
        assertNull(get(alert));

        // verify the documents have been removed
        for (Act document : documents) {
            assertNull(get(document));
        }
    }

    /**
     * Verifies that reminders, alerts and documents that have status 'Completed' or 'Posted' aren't removed when an
     * invoice item is deleted.
     */
    @Test
    public void testRemoveInvoiceItemCompleteActs() {
        ActBean item = createInvoiceItem();
        item.addNodeParticipation("product", createProduct(true));
        item.save();

        // add investigation acts
        List<Act> investigations = createInvestigationActs();
        for (Act investigation : investigations) {
            item.addNodeRelationship("investigations", investigation);
        }
        save(investigations);

        // add document acts
        ChargeItemDocumentLinker linker = new ChargeItemDocumentLinker((FinancialAct) item.getAct(),
                                                                       getArchetypeService());
        linker.link();

        // add a reminder
        Act reminder = createReminder();
        item.addNodeRelationship("reminders", reminder);

        // add an alert
        Act alert = createAlert();
        item.addNodeRelationship("alerts", alert);
        save(item.getAct(), reminder, alert);

        item = get(item); // reload to ensure the item has saved correctly

        List<Act> reminders = item.getNodeActs("reminders");
        assertEquals(1, reminders.size());

        List<Act> alerts = item.getNodeActs("alerts");
        assertEquals(1, alerts.size());

        List<Act> documents = item.getNodeActs("documents");
        assertEquals(1, documents.size());

        investigations.get(0).setStatus(ActStatus.POSTED);
        investigations.get(1).setStatus(ActStatus.POSTED);
        investigations.get(2).setStatus(ActStatus.POSTED);
        investigations.get(3).setStatus(ActStatus.POSTED);
        save(investigations);

        // set the reminder status to 'Completed'
        reminder.setStatus(COMPLETED);
        save(reminder);

        // set the alert status to 'Completed'
        alert.setStatus(COMPLETED);
        save(alert);

        // set the document status to 'Posted'
        Act document = documents.get(0);
        document.setStatus(POSTED);
        save(document);

        item = get(item); // reload to ensure the item has saved correctly

        // remove the item and verify it can't be retrieved
        IMObjectReference actRef = item.getAct().getObjectReference();
        remove(item.getAct());
        assertNull(get(actRef));

        // verify the investigations, reminder, alert and document haven't been removed
        for (Act investigation : investigations) {
            assertNotNull(get(investigation));
        }
        assertNotNull(get(reminder));
        assertNotNull(get(alert));
        assertNotNull(get(document));
    }

    /**
     * Verifies that reminders, alerts and documents that don't have status 'Completed' are removed when an invoice is
     * deleted.
     */
    @Test
    public void testRemoveInvoiceIncompleteActs() {
        ActBean invoice = createInvoice();
        ActBean item = createInvoiceItem();
        item.addNodeParticipation("product", createProduct(true));
        item.save();

        // add investigation acts
        List<Act> investigations = createInvestigationActs();
        for (Act investigation : investigations) {
            item.addNodeRelationship("investigations", investigation);
            save(investigation);
        }
        item.save();
        invoice.addNodeRelationship("items", item.getAct());
        save(item.getAct(), invoice.getAct());

        // add document acts
        ChargeItemDocumentLinker linker = new ChargeItemDocumentLinker((FinancialAct) item.getAct(),
                                                                       getArchetypeService());
        linker.link();

        // add a reminder
        Act reminder = createReminder();
        item.addNodeRelationship("reminders", reminder);

        // add an alert
        Act alert = createAlert();
        item.addNodeRelationship("alerts", alert);

        save(item.getAct(), reminder, alert);
        item = get(item); // reload to ensure the item has saved correctly

        List<Act> reminders = item.getNodeActs("reminders");
        assertEquals(1, reminders.size());

        List<Act> alerts = item.getNodeActs("alerts");
        assertEquals(1, alerts.size());

        List<Act> documents = item.getNodeActs("documents");
        assertEquals(1, documents.size());

        // remove the invoice and verify it can't be retrieved
        remove(invoice.getAct());
        assertNull(get(invoice.getAct()));

        // verify the investigations have been removed
        for (Act investigation : investigations) {
            assertNull(get(investigation));
        }

        // verify the reminders have been removed
        assertNull(get(reminder));

        // verify the alerts have been removed
        assertNull(get(alert));

        // verify the documents have been removed
        for (Act document : documents) {
            assertNull(get(document));
        }
    }

    /**
     * Verifies that investigations with POSTED status, and reminders, alerts and documents that have status
     * COMPLETED are not removed when an invoice is deleted.
     */
    @Test
    public void testRemoveInvoiceCompleteActs() {
        ActBean invoice = createInvoice();
        ActBean item = createInvoiceItem();
        item.addNodeParticipation("product", createProduct(true));
        item.save();

        // add investigation acts
        List<Act> investigations = createInvestigationActs();
        for (Act investigation : investigations) {
            item.addNodeRelationship("investigations", investigation);
            save(investigation);
        }
        item.save();
        invoice.addNodeRelationship("items", item.getAct());
        save(item.getAct(), invoice.getAct());

        // add document acts
        ChargeItemDocumentLinker linker = new ChargeItemDocumentLinker((FinancialAct) item.getAct(),
                                                                       getArchetypeService());
        linker.link();

        // add a reminder
        Act reminder = createReminder();
        item.addNodeRelationship("reminders", reminder);

        // add an alert
        Act alert = createAlert();
        item.addNodeRelationship("alerts", alert);

        save(item.getAct(), reminder, alert);

        item = get(item); // reload to ensure the item has saved correctly

        List<Act> reminders = item.getNodeActs("reminders");
        assertEquals(1, reminders.size());

        List<Act> alerts = item.getNodeActs("alerts");
        assertEquals(1, alerts.size());

        List<Act> documents = item.getNodeActs("documents");
        assertEquals(1, documents.size());

        // change the investigation statuses to statuses that should prevent their removal
        investigations.get(0).setStatus(ActStatus.POSTED);
        investigations.get(1).setStatus(ActStatus.POSTED);
        investigations.get(2).setStatus(ActStatus.CANCELLED);
        investigations.get(3).setStatus(ActStatus.CANCELLED);

        save(investigations);

        // set the reminder status to 'Completed'
        reminder.setStatus(COMPLETED);
        save(reminder);

        // set the alert status to 'Completed'
        alert.setStatus(COMPLETED);
        save(alert);

        // set the document status to 'Completed'
        Act document = documents.get(0);
        document.setStatus(COMPLETED);
        save(document);

        // remove the invoice and verify it can't be retrieved
        IMObjectReference actRef = invoice.getAct().getObjectReference();
        remove(invoice.getAct());
        assertNull(get(actRef));

        // verify the investigations, reminder and document haven't been removed
        for (Act investigation : investigations) {
            assertNotNull(get(investigation));
        }
        assertNotNull(get(reminder));
        assertNotNull(get(alert));
        assertNotNull(get(document));
    }

    /**
     * Verifies that investigations with results are not removed, regardless of status.
     */
    @Test
    public void testRemoveInvoiceItemHavingInvestigationsWithResults() {
        ActBean invoice = createInvoice();
        ActBean item = createInvoiceItem();
        item.addNodeParticipation("product", TestHelper.createProduct());
        item.save();

        // add investigation acts
        List<Act> investigations = createInvestigationActs();
        for (Act investigation : investigations) {
            item.addNodeRelationship("investigations", investigation);
        }
        Act investigation1 = investigations.get(0);
        Act investigation2 = investigations.get(1);
        Act investigation3 = investigations.get(2);
        Act investigation4 = investigations.get(3);
        addReport(investigation2, ActStatus.IN_PROGRESS);
        addReport(investigation3, ActStatus.POSTED);
        addReport(investigation4, ActStatus.CANCELLED);
        save(investigations);
        item.save();

        invoice.addNodeRelationship("items", item.getAct());
        save(item.getAct(), invoice.getAct());

        // remove the invoice and verify it can't be retrieved
        IMObjectReference actRef = invoice.getAct().getObjectReference();
        remove(invoice.getAct());
        assertNull(get(actRef));

        // verify only the IN_PROGRESS investigation with no document has been removed
        assertNull(get(investigation1));
        assertNotNull(get(investigation2));
        assertNotNull(get(investigation3));
        assertNotNull(get(investigation4));
    }

    /**
     * Verifies that demographic updates associated with a product are processed
     * when an invoice is posted.
     */
    @Test
    public void testDemographicUpdates() {
        Product product = createDesexingProduct();
        ActBean invoice = createInvoice();
        ActBean item = createInvoiceItem();
        item.addParticipation(ProductArchetypes.PRODUCT_PARTICIPATION, product);
        item.save();

        IMObjectBean bean = new IMObjectBean(get(patient));
        assertFalse(bean.getBoolean("desexed"));

        invoice.addRelationship(CustomerAccountArchetypes.INVOICE_ITEM_RELATIONSHIP, item.getAct());
        invoice.setStatus(ActStatus.POSTED);
        invoice.save();

        bean = new IMObjectBean(get(patient));
        assertTrue(bean.getBoolean("desexed"));
    }

    /**
     * Helper to create an <em>act.customerAccountChargesInvoice</em>.
     *
     * @return a new act
     */
    private ActBean createInvoice() {
        Act act = (Act) create(CustomerAccountArchetypes.INVOICE);
        act.setStatus(IN_PROGRESS);
        ActBean bean = new ActBean(act);
        bean.addParticipation(CustomerArchetypes.CUSTOMER_PARTICIPATION, customer);
        bean.addParticipation(PatientArchetypes.PATIENT_PARTICIPATION, patient);
        bean.addParticipation(UserArchetypes.CLINICIAN_PARTICIPATION, clinician);
        return bean;
    }

    /**
     * Helper to create an <em>act.customerAccountInvoiceItem</em>.
     *
     * @return a new act
     */
    private ActBean createInvoiceItem() {
        Act act = (Act) create(CustomerAccountArchetypes.INVOICE_ITEM);
        ActBean bean = new ActBean(act);
        bean.addParticipation(PatientArchetypes.PATIENT_PARTICIPATION, patient);
        bean.addParticipation(UserArchetypes.CLINICIAN_PARTICIPATION, clinician);
        return bean;
    }

    /**
     * Helper to create a reminder.
     *
     * @return a new reminder
     */
    private Act createReminder() {
        Entity reminderType = ReminderTestHelper.createReminderType();
        return ReminderTestHelper.createReminderWithDueDate(patient, reminderType, new Date());
    }

    /**
     * Helper to create an alert.
     *
     * @return a new alert
     */
    private Act createAlert() {
        Entity alertType = ReminderTestHelper.createAlertType("Z Test Alert");
        return ReminderTestHelper.createAlert(patient, alertType);
    }

    /**
     * Creates and saves a new document template.
     *
     * @return a new document template
     */
    private Entity createDocumentTemplate() {
        Entity template = (Entity) create("entity.documentTemplate");
        EntityBean bean = new EntityBean(template);
        bean.setValue("name", "XDocumentTemplate");
        bean.setValue("archetype", "act.patientDocumentForm");
        bean.save();

        return template;
    }

    /**
     * Creates and saves a new product.
     *
     * @param addRelationships if <tt>true</tt> add relationships to an investigation type and document
     * @return a new product
     */
    private Product createProduct(boolean addRelationships) {
        Product product = (Product) create("product.medication");
        EntityBean bean = new EntityBean(product);
        bean.setValue("name", "XProduct");
        if (addRelationships) {
            for (Entity investigation : investigationTypes) {
                bean.addRelationship("entityRelationship.productInvestigationType", investigation);
            }
            bean.addRelationship("entityRelationship.productDocument", template);
        }
        save(product, template);
        return product;
    }

    /**
     * Creates <em>act.patientInvestigation</em> acts for each of the investigation types.
     *
     * @return a list of investigation acts
     */
    private List<Act> createInvestigationActs() {
        List<Act> result = new ArrayList<>();
        for (Entity investigationType : investigationTypes) {
            Act act = PatientTestHelper.createInvestigation(patient, investigationType);
            result.add(act);
        }
        return result;
    }

    /**
     * Creates and saves a new desexing product, with associated
     * <em>lookup.demographicUpdate</em> to perform the desexing update.
     *
     * @return a new desexing product
     */
    private Product createDesexingProduct() {
        Product product = TestHelper.createProduct();
        Lookup lookup = (Lookup) create("lookup.demographicUpdate");
        lookup.setCode("XDESEXING_" + System.currentTimeMillis());
        IMObjectBean bean = new IMObjectBean(lookup);
        bean.setValue("nodeName", "patient.entity");
        bean.setValue("expression", "party:setPatientDesexed(.)");
        bean.save();
        product.addClassification(lookup);
        save(product);
        return product;
    }

    /**
     * Adds a report to an investigation, and updates it status.
     *
     * @param act    the investigation act
     * @param status the act status
     */
    private void addReport(Act act, String status) {
        PatientTestHelper.addReport((DocumentAct) act);
        act.setStatus(status);
    }

}
