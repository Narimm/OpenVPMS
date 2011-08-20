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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.archetype.rules.finance.invoice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.CLINICAL_EVENT_ITEM;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;

import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * Tests the {@link ChargeItemEventLinker} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class ChargeItemEventLinkerTestCase extends ArchetypeServiceTest {

    /**
     * The clinician.
     */
    private User clinician;

    /**
     * The rules.
     */
    private MedicalRecordRules rules;


    /**
     * Tests linking of a single charge item's act to an event.
     */
    @Test
    public void testSingleLink() {
        Date startTime = new Date();
        Party patient = TestHelper.createPatient();
        FinancialAct item = createInvoiceItem(startTime, patient);

        assertNull(rules.getEvent(patient, startTime));
        ChargeItemEventLinker linker = new ChargeItemEventLinker(getArchetypeService());
        linker.link(item);

        Act event = rules.getEvent(patient, startTime);
        assertNotNull(event);

        checkEvent(item, event);

        // verify that linking again succeeds
        linker.link(item);
        checkEvent(item, event);
    }


    /**
     * Verifies that a single event is created if multiple charge items are saved for the same start time.
     */
    @Test
    public void testMultipleLinkSameStartTime() {
        Date startTime = new Date();
        Party patient = TestHelper.createPatient();
        FinancialAct item1 = createInvoiceItem(startTime, patient);
        FinancialAct item2 = createInvoiceItem(startTime, patient);

        ChargeItemEventLinker linker = new ChargeItemEventLinker(getArchetypeService());
        List<FinancialAct> items = Arrays.asList(item1, item2);
        linker.link(items);

        MedicalRecordRules medRules = new MedicalRecordRules();
        Act event = medRules.getEvent(patient, startTime);
        assertNotNull(event);

        checkEvent(item1, event);
        checkEvent(item2, event);

        // verify that linking again succeeds
        linker.link(items);
        checkEvent(item1, event);
        checkEvent(item2, event);
    }

    /**
     * Verifies that multiple events are created if the charge items have start times on different days.
     */
    @Test
    public void testMultipleLinkDifferentStartTime() {
        Date startTime1 = TestHelper.getDate("2011-01-09");
        Date startTime2 = TestHelper.getDate("2011-01-02");
        Party patient = TestHelper.createPatient();
        FinancialAct item1 = createInvoiceItem(startTime1, patient);
        FinancialAct item2 = createInvoiceItem(startTime2, patient);

        ChargeItemEventLinker linker = new ChargeItemEventLinker(getArchetypeService());
        List<FinancialAct> items = Arrays.asList(item1, item2);
        linker.link(items);

        MedicalRecordRules medRules = new MedicalRecordRules();
        Act event1 = medRules.getEvent(patient, startTime1);
        Act event2 = medRules.getEvent(patient, startTime2);
        assertNotNull(event1);
        assertNotNull(event2);
        assertFalse(event1.equals(event2));

        checkEvent(item1, event1);
        checkEvent(item2, event2);

        // verify that linking again succeeds
        linker.link(items);
        checkEvent(item1, event1);
        checkEvent(item2, event2);
    }

    /**
     * Verifies that multiple events are created if there are different patients.
     */
    @Test
    public void testDifferentPatients() {
        Date startTime = new Date();
        Party patient1 = TestHelper.createPatient();
        Party patient2 = TestHelper.createPatient();
        FinancialAct item1 = createInvoiceItem(startTime, patient1);
        FinancialAct item2 = createInvoiceItem(startTime, patient2);

        ChargeItemEventLinker linker = new ChargeItemEventLinker(getArchetypeService());
        List<FinancialAct> items = Arrays.asList(item1, item2);
        linker.link(items);

        MedicalRecordRules medRules = new MedicalRecordRules();
        Act event1 = medRules.getEvent(patient1, startTime);
        assertNotNull(event1);

        Act event2 = medRules.getEvent(patient2, startTime);
        assertNotNull(event2);
        assertFalse(event1.equals(event2));

        checkEvent(item1, event1);
        checkEvent(item2, event2);

        // verify that linking again succeeds
        linker.link(items);
        checkEvent(item1, event1);
        checkEvent(item2, event2);
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        clinician = TestHelper.createClinician();
        rules = new MedicalRecordRules();
    }

    /**
     * Checks that a patient clinical event has links to the releveat charge items acts.
     *
     * @param item  the charge item
     * @param event the event
     */
    private void checkEvent(FinancialAct item, Act event) {
        item = get(item);
        event = get(event);
        assertNotNull(item);
        assertNotNull(event);
        ActBean bean = new ActBean(item);
        List<Act> dispensing = bean.getNodeActs("dispensing");
        List<Act> investigations = bean.getNodeActs("investigations");
        List<Act> documents = bean.getNodeActs("documents");
        assertEquals(1, dispensing.size());
        assertEquals(1, investigations.size());
        assertEquals(1, documents.size());

        ActBean eventBean = new ActBean(event);
        assertTrue(eventBean.hasRelationship(CLINICAL_EVENT_ITEM, investigations.get(0)));
        assertTrue(eventBean.hasRelationship(CLINICAL_EVENT_ITEM, dispensing.get(0)));
        assertTrue(eventBean.hasRelationship(CLINICAL_EVENT_ITEM, documents.get(0)));
    }

    /**
     * Creates a new invoice item.
     * <p/>
     * Each item has a single medication, investigation and document act.
     *
     * @param startTime the start time
     * @param patient   the patient
     * @return a new invoice item
     */
    private FinancialAct createInvoiceItem(Date startTime, Party patient) {
        FinancialAct act = (FinancialAct) create(CustomerAccountArchetypes.INVOICE_ITEM);
        ActBean bean = new ActBean(act);
        act.setActivityStartTime(startTime);
        bean.addParticipation(PatientArchetypes.PATIENT_PARTICIPATION, patient);
        bean.addParticipation(UserArchetypes.CLINICIAN_PARTICIPATION, clinician);

        Entity investigationType = createInvestigationType();
        Entity template = createDocumentTemplate();
        Product product = createProduct(investigationType, template);

        // add medication act
        Act medication = createMedication(product, patient);
        bean.addParticipation(ProductArchetypes.PRODUCT_PARTICIPATION, product);
        bean.addRelationship(CustomerAccountArchetypes.DISPENSING_ITEM_RELATIONSHIP, medication);

        // add investigation act
        Act investigation = createInvestigation(investigationType, patient);
        bean.addRelationship(CustomerAccountArchetypes.INVESTIGATION_ITEM_RELATIONSHIP, investigation);

        save(act, medication, investigation);

        // add document acts
        ChargeItemDocumentLinker linker = new ChargeItemDocumentLinker(act, getArchetypeService());
        linker.link();
        return act;
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
     * @param investigationType the investigation type
     * @param template          the document template
     * @return a new product
     */
    private Product createProduct(Entity investigationType, Entity template) {
        Product product = (Product) create("product.medication");
        EntityBean bean = new EntityBean(product);
        bean.setValue("name", "XProduct");
        bean.addRelationship("entityRelationship.productInvestigationType", investigationType);
        bean.addRelationship("entityRelationship.productDocument", template);
        save(product, investigationType, template);
        return product;
    }

    /**
     * Creates a new <em>act.patientMedication</em>.
     *
     * @param product the product
     * @param patient the patient
     * @return a new medication
     */
    private Act createMedication(Product product, Party patient) {
        Act act = (Act) create("act.patientMedication");
        ActBean bean = new ActBean(act);
        bean.addParticipation(PatientArchetypes.PATIENT_PARTICIPATION, patient);
        bean.addParticipation(ProductArchetypes.PRODUCT_PARTICIPATION, product);
        return act;
    }

    /**
     * Creates an <em>act.patientInvestigation</em> for an investigation type.
     *
     * @param investigationType the investigation type
     * @param patient           the patient
     * @return a list of investigation acts
     */
    private Act createInvestigation(Entity investigationType, Party patient) {
        Act act = (Act) create(InvestigationArchetypes.PATIENT_INVESTIGATION);
        ActBean bean = new ActBean(act);
        bean.addParticipation(PatientArchetypes.PATIENT_PARTICIPATION, patient);
        bean.addParticipation(InvestigationArchetypes.INVESTIGATION_TYPE_PARTICIPATION, investigationType);
        return act;
    }

}
