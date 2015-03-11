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

package org.openvpms.archetype.rules.patient;

import org.openvpms.archetype.rules.math.WeightUnits;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.openvpms.archetype.test.TestHelper.create;
import static org.openvpms.archetype.test.TestHelper.save;

/**
 * Patient test helper methods.
 *
 * @author Tim Anderson
 */
public class PatientTestHelper {

    /**
     * Helper to create an <em>act.patientMedication</em>.
     *
     * @param patient the patient
     * @return a new act
     */
    public static Act createMedication(Party patient) {
        return createMedication(patient, TestHelper.createProduct());
    }

    /**
     * Helper to create an <em>act.patientMedication</em>.
     *
     * @param patient the patient
     * @param product the product
     * @return a new act
     */
    public static Act createMedication(Party patient, Product product) {
        Act act = (Act) create(PatientArchetypes.PATIENT_MEDICATION);
        ActBean bean = new ActBean(act);
        bean.addNodeParticipation("patient", patient);
        bean.addNodeParticipation("product", product);
        bean.save();
        return act;
    }

    /**
     * Helper to create an <em>act.patientClinicalEvent</em>.
     *
     * @param patient   the patient
     * @param clinician the clinician. May be {@code null}
     * @return a new act
     */
    public static Act createEvent(Party patient, User clinician) {
        return createAct(PatientArchetypes.CLINICAL_EVENT, new Date(), patient, clinician);
    }

    /**
     * Helper to create an <em>act.patientClinicalEvent</em>.
     * <p/>
     * This links the event to any items, and saves it.
     *
     * @param startTime the start time. May be {@code null}
     * @param patient   the patient
     * @return a new act
     */
    public static Act createEvent(Date startTime, Party patient, Act... items) {
        return createEvent(startTime, null, patient, null, items);
    }

    /**
     * Helper to create an <em>act.patientClinicalEvent</em>.
     * <p/>
     * This links the event to any items, and saves it.
     *
     * @param startTime the start time. May be {@code null}
     * @param patient   the patient
     * @param clinician the clinician. May be {@code null}
     * @return a new act
     */
    public static Act createEvent(Date startTime, Party patient, User clinician, Act... items) {
        return createEvent(startTime, null, patient, clinician, items);
    }

    /**
     * Helper to create an <em>act.patientClinicalEvent</em>.
     * <p/>
     * This links the event to any items, and saves it.
     *
     * @param startTime the start time. May be {@code null}
     * @param endTime   the end time. May be {@code null}
     * @param patient   the patient
     * @param clinician the clinician. May be {@code null}
     * @return a new act
     */
    public static Act createEvent(Date startTime, Date endTime, Party patient, User clinician, Act... items) {
        Act act = createEvent(patient, clinician);
        act.setActivityStartTime(startTime);
        act.setActivityEndTime(endTime);
        ActBean bean = new ActBean(act);
        for (Act item : items) {
            bean.addNodeRelationship("items", item);
        }
        List<Act> acts = new ArrayList<Act>();
        acts.add(act);
        acts.addAll(Arrays.asList(items));
        save(acts);
        return act;
    }

    /**
     * Helper to create an <em>act.patientClinicalProblem</em>.
     * <p/>
     * This links the problem to any items, and saves it.
     *
     * @param startTime the start time
     * @param patient   the patient
     * @param items     the problem items
     * @return a new act
     */
    public static Act createProblem(Date startTime, Party patient, Act... items) {
        return createProblem(startTime, patient, null, items);
    }

    /**
     * Helper to create an <em>act.patientClinicalProblem</em>.
     * <p/>
     * This links the problem to any items, and saves it.
     *
     * @param startTime the start time
     * @param patient   the patient
     * @param clinician the clinician. May be {@code null}
     * @param items     the problem items
     * @return a new act
     */
    public static Act createProblem(Date startTime, Party patient, User clinician, Act... items) {
        Act act = createAct(PatientArchetypes.CLINICAL_PROBLEM, startTime, patient, clinician);
        Lookup diagnosis = TestHelper.getLookup("lookup.diagnosis", "HEART_MURMUR");
        act.setReason(diagnosis.getCode());
        ActBean bean = new ActBean(act);
        for (Act item : items) {
            bean.addNodeRelationship("items", item);
        }
        List<Act> acts = new ArrayList<Act>();
        acts.add(act);
        acts.addAll(Arrays.asList(items));
        save(acts);
        return act;
    }

    /**
     * Creates an <em>act.patientClinicalNote</em>.
     *
     * @param startTime the act start time
     * @param patient   the patient
     * @return a new act
     */
    public static Act createNote(Date startTime, Party patient) {
        return createAct(PatientArchetypes.CLINICAL_NOTE, startTime, patient, null);
    }

    /**
     * Creates an <em>act.patientClinicalNote</em>.
     *
     * @param startTime the act start time
     * @param patient   the patient
     * @param clinician the clinician. May be {@code null}
     * @return a new act
     */
    public static Act createNote(Date startTime, Party patient, User clinician) {
        return createAct(PatientArchetypes.CLINICAL_NOTE, startTime, patient, clinician);
    }

    /**
     * Creates a new <em>act.patientWeight</em> for a patient for the current date, and saves it.
     *
     * @param patient the patient
     * @param weight  the weight
     * @return the weight act
     */
    public static Act createWeight(Party patient, BigDecimal weight, WeightUnits units) {
        return createWeight(patient, new Date(), weight, units);
    }

    /**
     * Creates a new <em>act.patientWeight</em> for a patient and saves it.
     *
     * @param patient   the patient
     * @param startTime the start time
     * @param weight    the weight
     * @param units     the weight units
     * @return the weight act
     */
    public static Act createWeight(Party patient, Date startTime, BigDecimal weight, WeightUnits units) {
        Act act = createWeight(startTime, patient, null);
        ActBean bean = new ActBean(act);
        bean.setValue("weight", weight);
        bean.setValue("units", units.toString());
        save(act);
        return act;
    }

    /**
     * Creates an <em>act.patientWeight</em>.
     *
     * @param startTime the act start time
     * @param patient   the patient
     * @return a new act
     */
    public static Act createWeight(Date startTime, Party patient) {
        return createWeight(startTime, patient, null);
    }

    /**
     * Creates an <em>act.patientWeight</em>.
     *
     * @param startTime the act start time
     * @param patient   the patient
     * @param clinician the clinician. May be {@code null}
     * @return a new act
     */
    public static Act createWeight(Date startTime, Party patient, User clinician) {
        return createAct(PatientArchetypes.PATIENT_WEIGHT, startTime, patient, clinician);
    }

    /**
     * Creates a patient act.
     *
     * @param shortName the act archetype short name
     * @param startTime the act start time
     * @param patient   the patient
     * @param clinician the clinician. May be {@code null}
     * @return a new act
     */
    public static Act createAct(String shortName, Date startTime, Party patient, User clinician) {
        Act act = (Act) create(shortName);
        act.setActivityStartTime(startTime);
        ActBean bean = new ActBean(act);
        bean.addNodeParticipation("patient", patient);
        if (clinician != null) {
            bean.addNodeParticipation("clinician", clinician);
        }
        return act;
    }

}
