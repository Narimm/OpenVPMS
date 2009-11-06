/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.finance.invoice;

import org.openvpms.archetype.rules.act.ActStatus;
import static org.openvpms.archetype.rules.act.ActStatus.*;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import static org.openvpms.archetype.rules.patient.MedicalRecordRules.CLINICAL_EVENT_ITEM;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.archetype.rules.patient.InvestigationActStatus;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;


/**
 * Tests the {@link InvoiceRules} class when invoked by the
 * <em>archetypeService.save.act.customerAccountInvoiceItem.after.drl</em> '
 * rule. In order for these tests to be successful, the archetype service
 * must be configured to trigger the above rule.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
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
     * The reminder.
     */
    private Entity reminderType;

    /**
     * The document template.
     */
    private Entity template;


    /**
     * Verifies that <em>act.patientReminder</em> and
     * <em>act.patientDocument*</em>s are associated with an invoice item when
     * it is saved.
     */
    public void testSaveInvoiceItem() {
        ActBean item = createInvoiceItem();
        Product product = createProduct(true);
        Act medication = createMedication(product);
        item.addParticipation(ProductArchetypes.PRODUCT_PARTICIPATION, product);
        item.addRelationship(CustomerAccountArchetypes.DISPENSING_ITEM_RELATIONSHIP, medication);
        save(medication, item.getAct());

        // add investigation acts
        List<Act> investigations = createInvestigationActs();
        for (Act investigation : investigations) {
            item.addRelationship(CustomerAccountArchetypes.INVESTIGATION_ITEM_RELATIONSHIP, investigation);
            save(investigation);
        }
        save(item.getAct());
        item = reload(item); // reload to ensure the item has saved correctly

        Date startTime = item.getAct().getActivityStartTime();

        // make sure a reminder has been added
        List<Act> reminders = item.getNodeActs("reminders");
        assertEquals(1, reminders.size());
        Act reminder = reminders.get(0);

        // verify the start time is the same as the invoice item start time
        assertEquals(startTime, reminder.getActivityStartTime());

        // verify the end time has been set
        ReminderRules rules = new ReminderRules(getArchetypeService());
        Date endTime = rules.calculateReminderDueDate(startTime, reminderType);
        assertEquals(endTime, reminder.getActivityEndTime());

        // make sure a document has been added
        List<Act> documents = item.getNodeActs("documents");
        assertEquals(1, documents.size());
        Act document = documents.get(0);
        assertTrue(TypeHelper.isA(document, "act.patientDocumentForm"));

        // verify the start time is the same as the invoice item start time
        assertEquals(startTime, document.getActivityStartTime());

        // check reminder participations
        ActBean reminderBean = new ActBean(reminder);
        assertEquals(patient, reminderBean.getParticipant(PatientArchetypes.PATIENT_PARTICIPATION));
        assertEquals(reminderType, reminderBean.getParticipant(ReminderArchetypes.REMINDER_TYPE_PARTICIPATION));
        assertEquals(product, reminderBean.getParticipant(ProductArchetypes.PRODUCT_PARTICIPATION));

        // check document participations
        ActBean docBean = new ActBean(document);
        assertEquals(patient, docBean.getParticipant(PatientArchetypes.PATIENT_PARTICIPATION));
        assertEquals(template, docBean.getParticipant("participation.documentTemplate"));

        // verify the investigation, dispensing and document acts have been added to the visit
        MedicalRecordRules medRules = new MedicalRecordRules();
        Act event = medRules.getEvent(patient, startTime);
        assertNotNull(event);
        ActBean eventBean = new ActBean(event);
        for (Act investigation : investigations) {
            assertTrue(eventBean.hasRelationship(CLINICAL_EVENT_ITEM, investigation));
        }
        assertTrue(eventBean.hasRelationship(CLINICAL_EVENT_ITEM, medication));
        assertTrue(eventBean.hasRelationship(CLINICAL_EVENT_ITEM, document));

        // change the product participation to one without a reminder.
        // The invoice should no longer have any associated reminders or
        // documents
        item.setParticipant(ProductArchetypes.PRODUCT_PARTICIPATION, createProduct(false));
        item.save();
        item = reload(item);

        reminders = item.getNodeActs("reminders");
        assertTrue(reminders.isEmpty());

        documents = item.getNodeActs("documents");
        assertTrue(documents.isEmpty());
    }

    /**
     * Verifies that reminders that don't have status 'Completed' are removed
     * when an invoice item is deleted.
     */
    public void testRemoveInvoiceItemIncompleteActs() {
        ActBean item = createInvoiceItem();
        item.addParticipation(ProductArchetypes.PRODUCT_PARTICIPATION, createProduct(true));
        item.save();

        // add investigation acts
        List<Act> investigations = createInvestigationActs();
        for (Act investigation : investigations) {
            item.addRelationship(CustomerAccountArchetypes.INVESTIGATION_ITEM_RELATIONSHIP, investigation);
            save(investigation);
        }
        save(item.getAct());
        item = reload(item); // reload to ensure the item has saved correctly

        List<Act> reminders = item.getNodeActs("reminders");
        assertEquals(1, reminders.size());

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

        // verify the reminders have been removed
        for (Act reminder : reminders) {
            assertNull(get(reminder));
        }

        // verify the documents have been removed
        for (Act document : documents) {
            assertNull(get(document));
        }
    }

    /**
     * Verifies that reminders and documents that don't have status 'Completed'
     * or 'Posted' aren't removed when an invoice item is deleted.
     */
    public void testRemoveInvoiceItemCompleteActs() {
        ActBean item = createInvoiceItem();
        item.addParticipation(ProductArchetypes.PRODUCT_PARTICIPATION, createProduct(true));
        item.save();

        // add investigation acts
        List<Act> investigations = createInvestigationActs();
        for (Act investigation : investigations) {
            item.addRelationship(CustomerAccountArchetypes.INVESTIGATION_ITEM_RELATIONSHIP, investigation);
        }
        save(investigations);
        item.save();
        item = reload(item); // reload to ensure the item has saved correctly

        List<Act> reminders = item.getNodeActs("reminders");
        assertEquals(1, reminders.size());

        List<Act> documents = item.getNodeActs("documents");
        assertEquals(1, documents.size());

        investigations.get(0).setStatus(InvestigationActStatus.COMPLETED);
        investigations.get(1).setStatus(InvestigationActStatus.PRELIMINARY);
        investigations.get(2).setStatus(InvestigationActStatus.FINAL);
        save(investigations);

        // set the reminder status to 'Completed'
        Act reminder = reminders.get(0);
        reminder.setStatus(COMPLETED);
        save(reminder);

        // set the document status to 'Posted'
        Act document = documents.get(0);
        document.setStatus(POSTED);
        save(document);

        item = reload(item); // reload to ensure the item has saved correctly

        // remove the item and verify it can't be retrieved
        IMObjectReference actRef = item.getAct().getObjectReference();
        remove(item.getAct());
        assertNull(get(actRef));

        // verify the investigations, reminder and document haven't been removed
        for (Act investigation : investigations) {
            assertNotNull(get(investigation));
        }
        assertNotNull(get(reminder));
        assertNotNull(get(document));
    }

    /**
     * Verifies that reminders and documents that don't have status 'Completed'
     * are removed when an invoice is deleted.
     */
    public void testRemoveInvoiceIncompleteActs() {
        ActBean invoice = createInvoice();
        ActBean item = createInvoiceItem();
        item.addParticipation(ProductArchetypes.PRODUCT_PARTICIPATION, createProduct(true));
        item.save();

        // add investigation acts
        List<Act> investigations = createInvestigationActs();
        for (Act investigation : investigations) {
            item.addRelationship(CustomerAccountArchetypes.INVESTIGATION_ITEM_RELATIONSHIP, investigation);
            save(investigation);
        }
        item.save();
        invoice.addRelationship(CustomerAccountArchetypes.INVOICE_ITEM_RELATIONSHIP, item.getAct());
        invoice.save();

        item = reload(item); // reload to ensure the item has saved correctly

        List<Act> reminders = item.getNodeActs("reminders");
        assertEquals(1, reminders.size());

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
        for (Act reminder : reminders) {
            assertNull(get(reminder));
        }

        // verify the documents have been removed
        for (Act document : documents) {
            assertNull(get(document));
        }
    }

    /**
     * Verifies that investigations, reminders and documents that have status
     * 'Completed' are not removed when an invoice is deleted.
     */
    public void testRemoveInvoiceCompleteActs() {
        ActBean invoice = createInvoice();
        ActBean item = createInvoiceItem();
        item.addParticipation(ProductArchetypes.PRODUCT_PARTICIPATION, createProduct(true));
        item.save();

        // add investigation acts
        List<Act> investigations = createInvestigationActs();
        for (Act investigation : investigations) {
            item.addRelationship(CustomerAccountArchetypes.INVESTIGATION_ITEM_RELATIONSHIP, investigation);
            save(investigation);
        }
        item.save();
        invoice.addRelationship(CustomerAccountArchetypes.INVOICE_ITEM_RELATIONSHIP, item.getAct());
        invoice.save();
        item = reload(item); // reload to ensure the item has saved correctly

        List<Act> reminders = item.getNodeActs("reminders");
        assertEquals(1, reminders.size());

        List<Act> documents = item.getNodeActs("documents");
        assertEquals(1, documents.size());

        // change the investigation statuses to statuses that should prevent their removal
        investigations.get(0).setStatus(InvestigationActStatus.COMPLETED);
        investigations.get(1).setStatus(InvestigationActStatus.PRELIMINARY);
        investigations.get(2).setStatus(InvestigationActStatus.FINAL);
        save(investigations);

        // set the reminder status to 'Completed'
        Act reminder = reminders.get(0);
        reminder.setStatus(COMPLETED);
        save(reminder);

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
        assertNotNull(get(document));
    }

    /**
     * Verifies that demographic updates associated with a product are processed
     * when an invoice is posted.
     */
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
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        customer = TestHelper.createCustomer();
        clinician = TestHelper.createClinician();
        patient = TestHelper.createPatient();
        investigationTypes = new HashSet<Entity>();
        for (int i = 0; i < 3; ++i) {
            investigationTypes.add(createInvestigationType());
        }
        reminderType = ReminderTestHelper.createReminderType();
        template = createDocumentTemplate();
    }

    /**
     * Helper to create an <em>act.customerAccountChargesInvoice</em>.
     *
     * @return a new act
     */
    private ActBean createInvoice() {
        Act act = createAct(CustomerAccountArchetypes.INVOICE);
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
        Act act = createAct(CustomerAccountArchetypes.INVOICE_ITEM);
        ActBean bean = new ActBean(act);
        bean.addParticipation(PatientArchetypes.PATIENT_PARTICIPATION, patient);
        bean.addParticipation(UserArchetypes.CLINICIAN_PARTICIPATION, clinician);
        return bean;
    }

    /**
     * Helper to create an <em>entity.investigationType</em>.
     *
     * @return a new investigation type
     */
    private Entity createInvestigationType() {
        Entity investigation = (Entity) create(InvestigationArchetypes.INVESTIGATION_TYPE);
        investigation.setName("X-TestInvestigationType-" + investigation.hashCode());
        save(investigation);
        return investigation;
    }

    /**
     * Helper to create a new act.
     *
     * @param shortName the act short name
     * @return a new act
     */
    private Act createAct(String shortName) {
        Act act = (Act) create(shortName);
        assertNotNull(act);
        return act;
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
     * @param addRelationships if <tt>true</tt> add relationships to an investigation type, reminder and document
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
            bean.addRelationship("entityRelationship.productReminder", reminderType);
            bean.addRelationship("entityRelationship.productDocument", template);
        }
        save(product, reminderType, template);
        return product;
    }

    /**
     * Creates a new <em>act.patientMedication</em>.
     *
     * @param product the product
     * @return a new medication
     */
    private Act createMedication(Product product) {
        Act act = createAct("act.patientMedication");
        ActBean bean = new ActBean(act);
        bean.addParticipation(PatientArchetypes.PATIENT_PARTICIPATION, patient);
        bean.addParticipation(ProductArchetypes.PRODUCT_PARTICIPATION, product);
        return act;
    }

    /**
     * Creates <em>act.patientInvestigation</em> acts for each of the investigation types.
     *
     * @return a list of investigation acts
     */
    private List<Act> createInvestigationActs() {
        List<Act> result = new ArrayList<Act>();
        for (Entity investigationType : investigationTypes) {
            Act act = createAct(InvestigationArchetypes.PATIENT_INVESTIGATION);
            ActBean bean = new ActBean(act);
            bean.addParticipation(PatientArchetypes.PATIENT_PARTICIPATION, patient);
            bean.addParticipation(InvestigationArchetypes.INVESTIGATION_TYPE_PARTICIPATION, investigationType);
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
     * Helper to reload an act.
     *
     * @param bean the act bean
     * @return the reloaded act
     */
    private ActBean reload(ActBean bean) {
        Act act = (Act) get(bean.getAct().getObjectReference());
        return new ActBean(act);
    }

}
