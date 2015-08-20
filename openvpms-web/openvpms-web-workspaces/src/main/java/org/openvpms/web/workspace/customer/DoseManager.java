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

package org.openvpms.web.workspace.customer;

import org.openvpms.archetype.rules.math.Weight;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.product.ProductRules;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper to return the dose of a product for a patient.
 * <br/>
 * This caches weights for patients.
 *
 * @author Tim Anderson
 */
public class DoseManager {

    /**
     * The patient weights, keyed on patient reference.
     */
    private Map<IMObjectReference, Weight> weights = new HashMap<>();

    /**
     * The patient rules.
     */
    private final PatientRules patientRules;

    /**
     * The product rules.
     */
    private final ProductRules productRules;

    /**
     * Constructs a {@link DoseManager}.
     *
     * @param patientRules the patient rules
     * @param productRules the product rules
     */
    public DoseManager(PatientRules patientRules, ProductRules productRules) {
        this.patientRules = patientRules;
        this.productRules = productRules;
    }

    /**
     * Returns the dose of a product for a patient, based on the patient's weight.
     *
     * @param product the product
     * @param patient the patient
     * @return the dose, or {@code 0} if no dose exists for the patient weight
     */
    public BigDecimal getDose(Product product, Party patient) {
        Weight weight = getWeight(patient);
        return productRules.getDose(product, weight, getSpecies(patient));
    }

    /**
     * Returns the weight of a patient.
     *
     * @param patient the patient
     * @return the patient's weight, or {@code 0} if its weight is not known
     */
    private Weight getWeight(Party patient) {
        Weight weight = weights.get(patient.getObjectReference());
        if (weight == null) {
            weight = patientRules.getWeight(patient);
            weights.put(patient.getObjectReference(), weight);
        }
        return weight;
    }

    /**
     * Returns the species of a patient.
     *
     * @param patient the patient
     * @return the patient species
     */
    private String getSpecies(Party patient) {
        IMObjectBean bean = new IMObjectBean(patient);
        return bean.getString("species");
    }

}
