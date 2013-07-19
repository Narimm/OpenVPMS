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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.mr;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.patient.prescription.PrescriptionRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.util.IMObjectHelper;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

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
     * The medication acts linked to the prescription.
     */
    private final Map<IMObjectReference, Act> medication = new HashMap<IMObjectReference, Act>();

    /**
     * The prescription rules.
     */
    private final PrescriptionRules rules;

    /**
     * Constructs a {@link Prescription}.
     *
     * @param prescription the prescription
     * @param rules        the prescription rules
     */
    public Prescription(Act prescription, PrescriptionRules rules) {
        this(prescription, null, rules);
    }

    /**
     * Constructs a {@link Prescription}.
     *
     * @param prescription      the prescription
     * @param currentMedication the current medication
     * @param rules             the prescription rules
     */
    public Prescription(Act prescription, Act currentMedication, PrescriptionRules rules) {
        this.prescription = new ActBean(prescription);
        this.rules = rules;
        patient = this.prescription.getNodeParticipantRef("patient");
        product = this.prescription.getNodeParticipantRef("product");

        IMObjectReference currentRef = null;
        if (currentMedication != null) {
            currentRef = currentMedication.getObjectReference();
            medication.put(currentRef, currentMedication);
        }
        for (IMObjectReference ref : this.prescription.getNodeTargetObjectRefs("dispensing")) {
            if (!ObjectUtils.equals(currentRef, ref)) {
                Act act = (Act) IMObjectHelper.getObject(ref, null);
                if (act != null) {
                    medication.put(act.getObjectReference(), act);
                }
            }
        }
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
     * Adds a medication to a prescription.
     *
     * @param medication the medication
     */
    public void addMedication(Act medication) {
        prescription.addNodeRelationship("dispensing", medication);
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
     * Removes a medication act from the prescription.
     *
     * @param medication the medication
     */
    public void removeMedication(Act medication) {
        ActRelationship relationship = prescription.getRelationship(medication);
        if (relationship != null) {
            prescription.removeRelationship(relationship);
        }
    }

    /**
     * Returns the quantity remaining to be dispensed.
     *
     * @return the quantity
     */
    public BigDecimal getRemainingQuantity() {
        BigDecimal totalQuantity = rules.getTotalQuantity(prescription.getAct());
        BigDecimal dispensed = BigDecimal.ZERO;
        for (Act act : medication.values()) {
            ActBean bean = new ActBean(act);
            BigDecimal quantity = bean.getBigDecimal("quantity", BigDecimal.ZERO);
            dispensed = dispensed.add(quantity);
        }
        return totalQuantity.subtract(dispensed);
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
        BigDecimal remaining = getRemainingQuantity();
        return remaining.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Returns the quantity to dispense.
     *
     * @return the quantity to dispense
     */
    public BigDecimal getQuantityToDispense() {
        BigDecimal remaining = getRemainingQuantity();
        BigDecimal quantity = rules.getQuantity(prescription.getAct());
        return remaining.compareTo(quantity) < 0 ? remaining : quantity;
    }
}
