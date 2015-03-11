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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.history;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.INVOICE_ITEM;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.CLINICAL_NOTE;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.CLINICAL_PROBLEM;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.PATIENT_MEDICATION;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.PATIENT_WEIGHT;
import static org.openvpms.archetype.rules.patient.PatientTestHelper.createNote;
import static org.openvpms.archetype.rules.patient.PatientTestHelper.createProblem;
import static org.openvpms.archetype.rules.patient.PatientTestHelper.createWeight;
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
    private static final String[] SHORT_NAMES = new String[]{CLINICAL_PROBLEM, PATIENT_WEIGHT, CLINICAL_NOTE};

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
        Act event = PatientTestHelper.createEvent(getDatetime("2014-05-09 10:00:00"), patient, clinician,
                                                  weight, problemNote, problem);

        List<Act> acts = Arrays.asList(event);

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
        Act event = PatientTestHelper.createEvent(getDatetime("2014-05-09 10:00:00"), patient, clinician,
                                                  weight, problem);

        List<Act> acts = Arrays.asList(event);

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
        Party patient = TestHelper.createPatient(true);
        Product product = TestHelper.createProduct();
        User clinician = TestHelper.createClinician();

        Act weight = createWeight(getDatetime("2014-05-09 10:00:00"), patient, clinician);
        FinancialAct charge1 = createChargeItem(getDatetime("2014-05-09 10:01:00"), patient, product);
        Act medication1 = createMedication(getDatetime("2014-05-09 10:01:00"), patient, charge1);
        FinancialAct charge2 = createChargeItem(getDatetime("2014-05-09 10:02:00"), patient, product);
        Act problemNote = createNote(getDatetime("2014-05-09 10:04:00"), patient, clinician);
        Act problem = createProblem(getDatetime("2014-05-09 10:05:00"), patient, clinician, problemNote);
        FinancialAct charge3 = createChargeItem(getDatetime("2014-05-09 10:06:00"), patient, product);

        Act event = PatientTestHelper.createEvent(getDatetime("2014-05-09 10:00:00"), patient, clinician,
                                                  weight, problemNote, problem, medication1);
        ActBean eventBean = new ActBean(event);
        eventBean.addNodeRelationship("chargeItems", charge1);
        eventBean.addNodeRelationship("chargeItems", charge2);
        eventBean.addNodeRelationship("chargeItems", charge3);
        save(event, charge1, charge2);

        List<Act> acts = Arrays.asList(event);

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
        Party patient = TestHelper.createPatient(true);
        User clinician = TestHelper.createClinician();

        Act note1a = createNote(getDatetime("2014-05-09 10:04:00"), patient, clinician);
        Act note1b = createNote(getDatetime("2014-05-09 10:05:00"), patient, clinician);
        Act note2 = createNote(getDatetime("2014-05-14 13:15:00"), patient, clinician);

        Act problem = createProblem(getDatetime("2014-05-09 10:05:00"), patient, clinician, note1a, note1b, note2);
        Act event1 = PatientTestHelper.createEvent(getDatetime("2014-05-09 10:00:00"), patient, clinician,
                                                   note1a, note1b, problem);

        Act event2 = PatientTestHelper.createEvent(getDatetime("2014-05-14 13:10:00"), patient, clinician,
                                                   note2, problem);

        List<Act> acts = Arrays.asList(event2, event1);
        check(acts, SHORT_NAMES, true, event2, problem, note2, event1, problem, note1a, note1b);
        check(acts, SHORT_NAMES, false, event2, problem, note2, event1, problem, note1b, note1a);
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
        int index = 0;
        List<Act> acts = getActs(events, shortNames, sortAscending);
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
        FinancialAct item = FinancialTestHelper.createChargeItem(INVOICE_ITEM, patient, product, BigDecimal.ONE);
        item.setActivityStartTime(startTime);
        save(item);
        return item;
    }

    /**
     * Verifies that {@link PatientHistoryIterator} returns the expected acts, in the correct order.
     *
     * @param events        the events
     * @param shortNames    the child act short names
     * @param sortAscending if {@code true} sort items on ascending timestamp; otherwise sort on descending timestamp
     */
    private List<Act> getActs(List<Act> events, String[] shortNames, boolean sortAscending) {
        PatientHistoryIterator iterator = new PatientHistoryIterator(events, shortNames, sortAscending);
        List<Act> result = new ArrayList<Act>();
        CollectionUtils.addAll(result, iterator);
        return result;
    }
}
