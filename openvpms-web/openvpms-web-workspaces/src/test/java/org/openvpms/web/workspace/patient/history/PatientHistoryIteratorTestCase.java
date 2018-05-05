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

package org.openvpms.web.workspace.patient.history;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.math.WeightUnits;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.rules.product.ProductTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.doc.DocumentTestHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.Assert.assertEquals;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.INVOICE_ITEM;
import static org.openvpms.archetype.rules.patient.InvestigationArchetypes.PATIENT_INVESTIGATION;
import static org.openvpms.archetype.rules.patient.InvestigationArchetypes.PATIENT_INVESTIGATION_VERSION;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.CLINICAL_ADDENDUM;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.CLINICAL_NOTE;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.CLINICAL_PROBLEM;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.DOCUMENT_ATTACHMENT;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.DOCUMENT_ATTACHMENT_VERSION;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.DOCUMENT_FORM;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.DOCUMENT_IMAGE;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.DOCUMENT_IMAGE_VERSION;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.DOCUMENT_LETTER;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.DOCUMENT_LETTER_VERSION;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.PATIENT_MEDICATION;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.PATIENT_WEIGHT;
import static org.openvpms.archetype.rules.patient.PatientTestHelper.addAddendum;
import static org.openvpms.archetype.rules.patient.PatientTestHelper.createAddendum;
import static org.openvpms.archetype.rules.patient.PatientTestHelper.createDocumentForm;
import static org.openvpms.archetype.rules.patient.PatientTestHelper.createEvent;
import static org.openvpms.archetype.rules.patient.PatientTestHelper.createNote;
import static org.openvpms.archetype.rules.patient.PatientTestHelper.createProblem;
import static org.openvpms.archetype.rules.patient.PatientTestHelper.createWeight;
import static org.openvpms.archetype.test.TestHelper.createProduct;
import static org.openvpms.archetype.test.TestHelper.getDatetime;

/**
 * Tests the {@link PatientHistoryIterator}.
 *
 * @author Tim Anderson
 */
public class PatientHistoryIteratorTestCase extends ArchetypeServiceTest {

    /**
     * The short names to filter on.
     */
    private static final String[] SHORT_NAMES = new String[]{CLINICAL_PROBLEM, PATIENT_WEIGHT, CLINICAL_NOTE,
                                                             PATIENT_MEDICATION, CLINICAL_ADDENDUM,
                                                             PATIENT_INVESTIGATION, PATIENT_INVESTIGATION_VERSION,
                                                             DOCUMENT_ATTACHMENT, DOCUMENT_ATTACHMENT_VERSION,
                                                             DOCUMENT_FORM,
                                                             DOCUMENT_LETTER, DOCUMENT_LETTER_VERSION,
                                                             DOCUMENT_IMAGE, DOCUMENT_IMAGE_VERSION,
                                                             CustomerAccountArchetypes.INVOICE_ITEM};

    /**
     * Verifies that when a note is linked to both an event and a child problem, it is returned after the problem.
     */
    @Test
    public void testIterator() {
        Party patient = TestHelper.createPatient();
        User clinician = TestHelper.createClinician();

        Act weight = createWeight(getDatetime("2014-05-09 10:00:00"), patient, clinician);
        Act problemNote = createNote(getDatetime("2014-05-09 10:04:00"), patient, clinician);
        Act problem = createProblem(getDatetime("2014-05-09 10:05:00"), patient, clinician, problemNote);
        Act event = createEvent(getDatetime("2014-05-09 10:00:00"), patient, clinician, weight, problemNote, problem);

        List<Act> acts = Collections.singletonList(event);

        String[] none = new String[0];
        check(acts, none, true, event);
        check(acts, none, false, event);

        String[] problemWeight = {CLINICAL_PROBLEM, PATIENT_WEIGHT};
        check(acts, problemWeight, true, event, weight, problem);
        check(acts, problemWeight, false, event, problem, weight);

        check(acts, SHORT_NAMES, true, event, weight, problem, problemNote);
        check(acts, SHORT_NAMES, false, event, problem, problemNote, weight);
    }

    /**
     * Verifies that items linked to a problem but not linked to an event are still returned.
     */
    @Test
    public void testUnlinkProblemItems() {
        Party patient = TestHelper.createPatient();
        User clinician = TestHelper.createClinician();

        Act weight = createWeight(getDatetime("2014-05-09 10:00:00"), patient, clinician);
        Act problemNote = createNote(getDatetime("2014-05-09 10:04:00"), patient, clinician);
        Act problem = createProblem(getDatetime("2014-05-09 10:05:00"), patient, clinician, problemNote);
        Act event = createEvent(getDatetime("2014-05-09 10:00:00"), patient, clinician, weight, problem);

        List<Act> acts = Collections.singletonList(event);

        String[] none = new String[0];
        check(acts, none, true, event);
        check(acts, none, false, event);

        String[] problemWeight = {CLINICAL_PROBLEM, PATIENT_WEIGHT};
        check(acts, problemWeight, true, event, weight, problem);
        check(acts, problemWeight, false, event, problem, weight);

        check(acts, SHORT_NAMES, true, event, weight, problem, problemNote);
        check(acts, SHORT_NAMES, false, event, problem, problemNote, weight);
    }

    /**
     * Tests inclusion/exclusion of invoice items.
     */
    @Test
    public void testInvoiceItems() {
        Party patient = TestHelper.createPatient();
        Product product = createProduct();
        User clinician = TestHelper.createClinician();

        Act weight = createWeight(getDatetime("2014-05-09 10:00:00"), patient, clinician);
        FinancialAct charge1 = createChargeItem(getDatetime("2014-05-09 10:01:00"), patient, product);
        Act medication1 = createMedication(getDatetime("2014-05-09 10:01:00"), patient, charge1);
        FinancialAct charge2 = createChargeItem(getDatetime("2014-05-09 10:02:00"), patient, product);
        Act problemNote = createNote(getDatetime("2014-05-09 10:04:00"), patient, clinician);
        Act problem = createProblem(getDatetime("2014-05-09 10:05:00"), patient, clinician, problemNote);
        FinancialAct charge3 = createChargeItem(getDatetime("2014-05-09 10:06:00"), patient, product);

        Act event = createEvent(getDatetime("2014-05-09 10:00:00"), patient, clinician,
                                weight, problemNote, problem, medication1);
        ActBean eventBean = new ActBean(event);
        eventBean.addNodeRelationship("chargeItems", charge1);
        eventBean.addNodeRelationship("chargeItems", charge2);
        eventBean.addNodeRelationship("chargeItems", charge3);
        save(event, charge1, charge2);

        List<Act> acts = Collections.singletonList(event);

        String[] withCharge = {CLINICAL_PROBLEM, PATIENT_WEIGHT, PATIENT_MEDICATION, INVOICE_ITEM, CLINICAL_NOTE};
        String[] noCharge = {CLINICAL_PROBLEM, PATIENT_WEIGHT, PATIENT_MEDICATION, CLINICAL_NOTE};

        check(acts, withCharge, true, event, weight, medication1, charge2, problem, problemNote, charge3);
        check(acts, withCharge, false, event, charge3, problem, problemNote, charge2, medication1, weight);

        check(acts, noCharge, true, event, weight, medication1, problem, problemNote);
        check(acts, noCharge, false, event, problem, problemNote, medication1, weight);
    }

    /**
     * Verifies that when a problem is linked to 2 visits, only the items linked to the event will appear listed
     * under the problem for that event.
     */
    @Test
    public void testProblemLinkedTo2Visits() {
        Party patient = TestHelper.createPatient();
        User clinician = TestHelper.createClinician();

        Act note1a = createNote(getDatetime("2014-05-09 10:04:00"), patient, clinician);
        Act note1b = createNote(getDatetime("2014-05-09 10:05:00"), patient, clinician);
        Act note2 = createNote(getDatetime("2014-05-14 13:15:00"), patient, clinician);

        Act problem = createProblem(getDatetime("2014-05-09 10:05:00"), patient, clinician, note1a, note1b, note2);
        Act event1 = createEvent(getDatetime("2014-05-09 10:00:00"), patient, clinician, note1a, note1b, problem);

        Act event2 = createEvent(getDatetime("2014-05-14 13:10:00"), patient, clinician, note2, problem);

        List<Act> acts = Arrays.asList(event2, event1);
        check(acts, SHORT_NAMES, true, event2, problem, note2, event1, problem, note1a, note1b);
        check(acts, SHORT_NAMES, false, event2, problem, note2, event1, problem, note1b, note1a);
    }

    /**
     * Verifies that addendum records are ordered on increasing time, and appear after the note or medication they
     * are linked to.
     */
    @Test
    public void testAddendumRecords() {
        Party patient = TestHelper.createPatient();
        User clinician = TestHelper.createClinician();

        Act note = createNote(getDatetime("2016-03-20 10:00:05"), patient, clinician);
        FinancialAct charge = createChargeItem(getDatetime("2016-03-20 10:01:00"), patient, createProduct());
        Act medication = createMedication(getDatetime("2016-03-20 10:01:00"), patient, charge);

        Act addendum1 = createAddendum(getDatetime("2016-03-27 11:00:00"), patient, clinician);
        Act addendum2 = createAddendum(getDatetime("2016-03-27 12:00:00"), patient, clinician);
        addAddendum(note, addendum1);
        addAddendum(note, addendum2);
        Act addendum3 = createAddendum(getDatetime("2016-03-27 13:00:00"), patient, clinician);
        Act addendum4 = createAddendum(getDatetime("2016-03-27 14:00:00"), patient, clinician);
        addAddendum(medication, addendum3);
        addAddendum(medication, addendum4);

        Act event = createEvent(getDatetime("2016-03-20 10:00:00"), patient, clinician, note, addendum1, addendum2,
                                medication, addendum3, addendum4);

        List<Act> acts = Collections.singletonList(event);

        check(acts, SHORT_NAMES, true, event, note, addendum1, addendum2, medication, addendum3, addendum4);
        check(acts, SHORT_NAMES, false, event, medication, addendum3, addendum4, note, addendum1, addendum2);
    }

    /**
     * Verifies that addendum records linked to a note appear in the correct order when also linked to a problem and
     * event.
     */
    @Test
    public void testAddendumLinkedToProblem() {
        Party patient = TestHelper.createPatient();
        User clinician = TestHelper.createClinician();

        Act note = createNote(getDatetime("2016-03-20 10:00:05"), patient, clinician);
        FinancialAct charge = createChargeItem(getDatetime("2016-03-20 10:01:00"), patient, createProduct());
        Act medication = createMedication(getDatetime("2016-03-20 10:01:00"), patient, charge);

        Act addendum1 = createAddendum(getDatetime("2016-03-27 11:00:00"), patient, clinician);
        Act addendum2 = createAddendum(getDatetime("2016-03-27 12:00:00"), patient, clinician);
        addAddendum(note, addendum1);
        addAddendum(note, addendum2);
        Act addendum3 = createAddendum(getDatetime("2016-03-27 13:00:00"), patient, clinician);
        Act addendum4 = createAddendum(getDatetime("2016-03-27 14:00:00"), patient, clinician);
        addAddendum(medication, addendum3);
        addAddendum(medication, addendum4);

        Act problem = createProblem(getDatetime("2016-03-20 10:05:00"), patient, clinician, note, addendum1, addendum2,
                                    medication, addendum3, addendum4);
        Act event = createEvent(getDatetime("2016-03-20 10:00:00"), patient, clinician, note, addendum1, addendum2,
                                medication, addendum3, addendum4, problem);

        // test iteration from the event
        List<Act> acts1 = Collections.singletonList(event);
        check(acts1, SHORT_NAMES, true, event, problem, note, addendum1, addendum2, medication, addendum3, addendum4);
        check(acts1, SHORT_NAMES, false, event, problem, medication, addendum3, addendum4, note, addendum1, addendum2);
    }

    /**
     * Tests searching.
     */
    @Test
    public void testSearch() {
        Party patient = TestHelper.createPatient();
        User clinician = TestHelper.createClinician();
        Entity investigationType = ProductTestHelper.createInvestigationType();
        investigationType.setName("Pathology");
        save(investigationType);

        Act weight = createWeight(patient, getDatetime("2018-02-09 10:00:00"), new BigDecimal("5.1"),
                                  WeightUnits.KILOGRAMS);
        Act note1 = createNote(getDatetime("2018-02-09 10:02:00"), patient, clinician, "note 1");
        Act addendum1 = createAddendum(getDatetime("2018-02-10 09:00:00"), patient, clinician, "note 1 addendum");
        addAddendum(note1, addendum1);

        Act note2 = createNote(getDatetime("2018-02-09 10:03:00"), patient, clinician, "note 2 a");
        Act problemNote = createNote(getDatetime("2018-02-09 10:04:00"), patient, clinician, "note 2 b");
        Act problem = createProblem(getDatetime("2018-02-09 10:05:00"), patient, clinician, "OFF_FOOD", "HEART_MURMUR",
                                    problemNote);

        Act investigation = PatientTestHelper.createInvestigation(getDatetime("2018-01-09 11:00:00"), patient,
                                                                  clinician, TestHelper.createLocation(),
                                                                  investigationType);

        Product product = ProductTestHelper.createMedication();
        product.setName("drug");
        save(product);
        Entity batch = ProductTestHelper.createBatch("1234567", product, new Date());

        Act medication = PatientTestHelper.createMedication(getDatetime("2018-02-09 11:05:00"), patient, product);
        IMObjectBean bean = new IMObjectBean(medication);
        bean.setValue("label", "Take once a day before meals");
        bean.setTarget("batch", batch);
        bean.save();

        Entity documentTemplate = DocumentTestHelper.createDocumentTemplate(DOCUMENT_FORM, "Vaccination Certificate");
        save(documentTemplate);
        DocumentAct form = createDocumentForm(getDatetime("2018-01-09 12:00:00"), patient, null, documentTemplate);

        DocumentAct letterVersion = PatientTestHelper.createDocumentLetterVersion(getDatetime("2018-01-09 12:05:00"));
        letterVersion.setFileName("Referral 1.pdf");
        DocumentAct letter = PatientTestHelper.createDocumentLetter(getDatetime("2018-01-09 12:04:00"), patient,
                                                                    letterVersion);
        letter.setFileName("Referral 2.pdf");
        save(letter, letterVersion);

        Product service = ProductTestHelper.createService();
        service.setName("Consult Fee");
        save(service);
        FinancialAct charge = FinancialTestHelper.createInvoiceItem(getDatetime("2018-01-09 12:05:00"), patient,
                                                                    clinician, service, ONE, TEN, ZERO, ZERO, ZERO);
        save(charge);

        Act event = createEvent(getDatetime("2018-02-09 10:00:00"), patient, clinician, weight, note1, addendum1, note2,
                                problemNote, problem, investigation, medication, form, letter, charge);

        List<Act> acts = Collections.singletonList(event);

        // type search
        check(acts, "Weight", true, event, weight);
        check(acts, "Problem", true, event, problem);
        check(acts, "Visit", true, event);

        // note and addendum search
        check(acts, "note 1", true, event, note1, addendum1);
        check(acts, "note 2", true, event, note2, problemNote);

        // investigation type
        check(acts, "pathology", true, event, investigation);

        // investigation id
        check(acts, Long.toString(investigation.getId()), true, event, investigation);

        // medication
        check(acts, "drug", true, event, medication);
        check(acts, "once a day", true, event, medication);

        // medication batch
        check(acts, "4567", true, event, medication);

        // weight search
        check(acts, "5.1", true, event, weight);

        // form search
        check(acts, "certificate", true, event, form);

        // letter
        check(acts, "referral", true, event, letter, letterVersion);
        check(acts, "referral 2", true, event, letter);

        // verify the parent letter is still included, even if there is only a match on the version
        check(acts, "referral 1", true, event, letter, letterVersion);

        // check problem
        check(acts, "off food", true, event, problem);
        check(acts, "heart murmur", true, event, problem);

        // charge
        check(acts, "fee", true, event, charge);
    }

    /**
     * Verifies that {@link PatientHistoryIterator} returns the expected acts, in the correct order.
     *
     * @param events        the events
     * @param search        the search criteria
     * @param sortAscending if {@code true} sort items on ascending timestamp; otherwise sort on descending timestamp
     * @param expected      the expected acts
     */
    private void check(List<Act> events, String search, boolean sortAscending, Act... expected) {
        check(events, SHORT_NAMES, search, sortAscending, expected);
    }

    /**
     * Verifies that {@link PatientHistoryIterator} returns the expected acts, in the correct order.
     *
     * @param events        the events
     * @param shortNames    the child act short names
     * @param sortAscending if {@code true} sort items on ascending timestamp; otherwise sort on descending timestamp
     * @param expected      the expected acts
     */
    private void check(List<Act> events, String[] shortNames, boolean sortAscending, Act... expected) {
        check(events, shortNames, null, sortAscending, expected);
    }

    /**
     * Verifies that {@link PatientHistoryIterator} returns the expected acts, in the correct order.
     *
     * @param events        the events
     * @param shortNames    the child act short names
     * @param search        the search criteria. May be {@code null}
     * @param sortAscending if {@code true} sort items on ascending timestamp; otherwise sort on descending timestamp
     * @param expected      the expected acts
     */
    private void check(List<Act> events, String[] shortNames, String search, boolean sortAscending, Act[] expected) {
        int index = 0;
        List<Act> acts = getActs(events, shortNames, search, sortAscending);
        assertEquals(expected.length, acts.size());
        for (Act act : acts) {
            assertEquals(expected[index++], act);
        }
    }

    /**
     * Creates a medication act.
     *
     * @param startTime  the start time
     * @param patient    the patient
     * @param chargeItem the charge item. May be {@code null}
     * @return a new medication act
     */
    private Act createMedication(Date startTime, Party patient, FinancialAct chargeItem) {
        Act medication = PatientTestHelper.createMedication(patient);
        medication.setActivityStartTime(startTime);
        if (chargeItem != null) {
            ActBean bean = new ActBean(chargeItem);
            bean.addNodeRelationship("dispensing", medication);
            save(chargeItem, medication);
        } else {
            save(medication);
        }
        return medication;
    }

    /**
     * Creates a new charge item.
     *
     * @param startTime the start time
     * @param patient   the patient
     * @param product   the product
     * @return a new charge item
     */
    private FinancialAct createChargeItem(Date startTime, Party patient, Product product) {
        FinancialAct item = FinancialTestHelper.createChargeItem(INVOICE_ITEM, patient, product, ONE);
        item.setActivityStartTime(startTime);
        save(item);
        return item;
    }

    /**
     * Verifies that {@link PatientHistoryIterator} returns the expected acts, in the correct order.
     *
     * @param events        the events
     * @param shortNames    the child act short names
     * @param search        the search criteria
     * @param sortAscending if {@code true} sort items on ascending timestamp; otherwise sort on descending timestamp
     */
    private List<Act> getActs(List<Act> events, String[] shortNames, String search, boolean sortAscending) {
        TextSearch textSearch = null;
        if (search != null) {
            textSearch = new TextSearch(search, true, true, getArchetypeService());
        }
        PatientHistoryIterator iterator = new PatientHistoryIterator(events, shortNames, textSearch, sortAscending);
        List<Act> result = new ArrayList<>();
        CollectionUtils.addAll(result, iterator);
        return result;
    }
}
