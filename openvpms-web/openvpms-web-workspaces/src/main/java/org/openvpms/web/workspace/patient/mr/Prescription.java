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

import org.openvpms.archetype.rules.patient.prescription.PrescriptionRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.im.edit.SaveHelper;

import java.math.BigDecimal;

/**
 * Medication prescription.
 *
 * @author Tim Anderson
 */
public class Prescription {

    /**
     * The prescription act.
     */
    private final ActBean prescription;

    /**
     * The patient reference.
     */
    private final IMObjectReference patient;

    /**
     * The medication product reference.
     */
    private final IMObjectReference product;

    /**
     * The prescription rules.
     */
    private final PrescriptionRules rules;

    /**
     * The parent prescriptions.
     */
    private final Prescriptions prescriptions;


    /**
     * Constructs a {@link Prescription}.
     *
     * @param prescription  the prescription
     * @param rules         the prescription rules
     * @param prescriptions the parent prescriptions
     */
    public Prescription(Act prescription, PrescriptionRules rules, Prescriptions prescriptions) {
        this.prescription = new ActBean(prescription);
        this.rules = rules;
        this.prescriptions = prescriptions;

        patient = this.prescription.getNodeParticipantRef("patient");
        product = this.prescription.getNodeParticipantRef("product");
    }

    /**
     * Saves the prescription act.
     *
     * @return {@code true} if the save was successful
     */
    public boolean save() {
        return SaveHelper.save(prescription.getAct());
    }

    /**
     * Returns the prescription patient.
     *
     * @return the patient
     */
    public IMObjectReference getPatient() {
        return patient;
    }

    /**
     * Returns the prescription product.
     *
     * @return the product
     */
    public IMObjectReference getProduct() {
        return product;
    }

    /**
     * Returns the prescription label.
     *
     * @return the prescription label. May be {@code null}
     */
    public String getLabel() {
        return prescription.getString("label");
    }

    /**
     * Adds a medication to a prescription.
     *
     * @param medication the medication
     */
    public void addMedication(Act medication) {
        prescription.addNodeRelationship("dispensing", medication);
        prescriptions.addMedication(medication);
    }

    /**
     * Removes a medication act from the prescription.
     *
     * @param medication the medication
     */
    public void removeMedication(Act medication) {
        ActRelationship relationship = prescription.getRelationship(medication);
        if (relationship != null) {
            medication.removeActRelationship(relationship);
            prescription.removeRelationship(relationship);
        }
    }

    /**
     * Returns the prescription act.
     *
     * @return the act. An <em>act.patientPrescription</em>
     */
    public Act getAct() {
        return prescription.getAct();
    }

    /**
     * Determines if the prescription can be dispensed.
     *
     * @return {@code true} if there is remaining quantity
     */
    public boolean canDispense() {
        return 1 + rules.getRepeats(prescription.getAct()) > prescription.getValues("dispensing").size();
    }

    /**
     * Returns the quantity to dispense.
     *
     * @return the quantity to dispense
     */
    public BigDecimal getQuantity() {
        return rules.getQuantity(prescription.getAct());
    }
}
