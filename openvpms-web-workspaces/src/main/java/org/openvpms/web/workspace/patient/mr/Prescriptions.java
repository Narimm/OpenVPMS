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

package org.openvpms.web.workspace.patient.mr;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.prescription.PrescriptionRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.im.util.IMObjectHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Manages prescriptions associated with an invoice.
 *
 * @author Tim Anderson
 */
public class Prescriptions {

    /**
     * The prescriptions, keyed on act reference.
     */
    private final Map<IMObjectReference, Prescription> prescriptions = new HashMap<IMObjectReference, Prescription>();

    /**
     * The medication acts, keyed on their reference.
     */
    private final Map<IMObjectReference, Act> medications = new HashMap<IMObjectReference, Act>();

    /**
     * The prescription rules.
     */
    private final PrescriptionRules rules;


    /**
     * Constructs a {@link Prescriptions}.
     *
     * @param items the charge items
     * @param rules the prescription rules
     */
    public Prescriptions(List<Act> items, PrescriptionRules rules) {
        this.rules = rules;
        for (Act item : items) {
            ActBean bean = new ActBean(item);
            for (Act medication : bean.getNodeActs("dispensing")) {
                ActBean medBean = new ActBean(medication);
                IMObjectReference product = medBean.getNodeParticipantRef("product");
                Act prescription = medBean.getSourceAct(PatientArchetypes.PRESCRIPTION_MEDICATION);
                if (product != null && prescription != null) {
                    prescriptions.put(prescription.getObjectReference(), new Prescription(prescription, rules, this));
                    medications.put(medication.getObjectReference(), medication);
                }
            }
        }
    }

    /**
     * Removes an invoice item.
     * <p/>
     * If the item is linked to a prescription, this relationship is removed.
     *
     * @param item the invoice item
     */
    public void removeItem(Act item) {
        Act medication = getMedication(item);
        if (medication != null) {
            medications.remove(medication.getObjectReference());
            Prescription prescription = getMedicationPrescription(medication);
            if (prescription != null) {
                prescription.removeMedication(medication);
            }
        }
    }

    /**
     * Returns a prescription with remaining repeats for the given patient and product, if one exists.
     *
     * @param patient the patient
     * @param product the product
     * @return the prescription, or {@code null} if none exists
     */
    public Act getPrescription(Party patient, Product product) {
        Prescription result = null;
        IMObjectReference productRef = product.getObjectReference();
        IMObjectReference patientRef = patient.getObjectReference();

        List<Act> exclude = new ArrayList<Act>(); // unsaved prescriptions that have been fully dispensed
        for (Prescription prescription : prescriptions.values()) {
            if (ObjectUtils.equals(patientRef, prescription.getPatient())
                && ObjectUtils.equals(productRef, prescription.getProduct())) {
                if (prescription.canDispense()) {
                    result = prescription;
                    break;
                } else {
                    exclude.add(prescription.getAct());
                }
            }
        }
        if (result == null) {
            Act act = rules.getPrescription(patient, product, exclude);
            if (act != null) {
                result = new Prescription(act, rules, this);
                prescriptions.put(act.getObjectReference(), result);
            }
        }
        return (result != null) ? result.getAct() : null;
    }

    /**
     * Adds a prescription.
     *
     * @param act the prescription act
     */
    public void add(Act act) {
        prescriptions.put(act.getObjectReference(), new Prescription(act, rules, this));
    }

    /**
     * Adds an <em>act.patientMedication</em> act.
     * <p/>
     * This is used to track unsaved medication acts to ensure references to them are removed if they are deleted before
     * being saved.
     *
     * @param medication the medication
     */
    public void addMedication(Act medication) {
        medications.put(medication.getObjectReference(), medication);
    }

    /**
     * Creates a prescription for an act.
     *
     * @param act the prescription act
     * @return a {@code Prescription} corresponding to {@code act}
     */
    public Prescription create(Act act) {
        Prescription result = prescriptions.get(act.getObjectReference());
        if (result == null) {
            result = new Prescription(act, rules, this);
            prescriptions.put(act.getObjectReference(), result);
        }
        return result;
    }

    /**
     * Saves the prescriptions.
     *
     * @return {@code true} if the save was successful
     */
    public boolean save() {
        for (Prescription prescription : prescriptions.values()) {
            if (!prescription.save()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Removes a medication.
     * <p/>
     * This removes any reference to a prescription.
     *
     * @param medication the medication
     */
    public void removeMedication(Act medication) {
        Prescription prescription = getPrescriptionForMedication(medication);
        if (prescription != null) {
            prescription.removeMedication(medication);
        }
    }

    /**
     * Returns the prescription associated with a medication, if it is cached.
     *
     * @param medication the medication
     * @return the corresponding prescription, or {@code null} if none is cached
     */
    private Prescription getMedicationPrescription(Act medication) {
        Prescription result = null;
        ActRelationship relationship = getPrescriptionRelationship(medication);
        if (relationship != null) {
            result = prescriptions.get(relationship.getSource());
        }
        return result;
    }

    /**
     * Returns the medication reference associated with a charge item.
     *
     * @param item the charge item
     * @return the medication, or {@code null} if none is found
     */
    private Act getMedication(Act item) {
        ActBean bean = new ActBean(item);
        List<IMObjectReference> refs = bean.getNodeTargetObjectRefs("dispensing");
        if (!refs.isEmpty()) {
            return medications.get(refs.get(0));
        }
        return null;
    }

    /**
     * Returns the prescription relationship associated with a medication.
     *
     * @param medication the medication
     * @return the prescription relationship, or {@code null} if none exists
     */
    private ActRelationship getPrescriptionRelationship(Act medication) {
        ActBean bean = new ActBean(medication);
        return bean.getRelationship(PatientArchetypes.PRESCRIPTION_MEDICATION);
    }

    /**
     * Returns the prescription associated with a medication.
     *
     * @param medication the medication
     * @return the prescription, or {@code null} if none exists
     */
    private Prescription getPrescriptionForMedication(Act medication) {
        Prescription result = null;
        ActRelationship relationship = getPrescriptionRelationship(medication);
        if (relationship != null) {
            Prescription prescription = prescriptions.get(relationship.getSource());
            if (prescription != null) {
                result = prescription;
            }
            if (result == null) {
                Act act = (Act) IMObjectHelper.getObject(relationship.getSource(), null);
                if (act != null) {
                    result = new Prescription(act, rules, this);
                    prescriptions.put(act.getObjectReference(), result);
                }
            }
        }
        return result;
    }
}
