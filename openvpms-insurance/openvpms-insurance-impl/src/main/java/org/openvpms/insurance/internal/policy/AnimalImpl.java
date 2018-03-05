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

package org.openvpms.insurance.internal.policy;

import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.insurance.policy.Animal;

import java.util.Date;

/**
 * Default implementation of the {@link Animal} interface.
 *
 * @author Tim Anderson
 */
public class AnimalImpl implements Animal {

    /**
     * The patient.
     */
    private final Party patient;

    /**
     * The patient bean.
     */
    private final IMObjectBean bean;

    /**
     * The patient rules.
     */
    private final PatientRules patientRules;

    /**
     * Constructs an {@link AnimalImpl}.
     *
     * @param patient      the patient
     * @param service      the archetype service
     * @param patientRules the patient rules
     */
    public AnimalImpl(Party patient, IArchetypeRuleService service, PatientRules patientRules) {
        this.patient = patient;
        bean = service.getBean(patient);
        this.patientRules = patientRules;
    }

    /**
     * Returns the OpenVPMS identifier for the animal.
     *
     * @return the OpenVPMS identifier for the animal
     */
    @Override
    public long getId() {
        return patient.getId();
    }

    /**
     * Returns the animal's name.
     *
     * @return the animal's name
     */
    @Override
    public String getName() {
        return patient.getName();
    }

    /**
     * Returns the animal's date of birth.
     *
     * @return the animal's date of birth. May be {@code null}
     */
    @Override
    public Date getDateOfBirth() {
        return patientRules.getDateOfBirth(patient);
    }

    /**
     * Returns the species name.
     *
     * @return the species name. May be {@code null}
     */
    @Override
    public String getSpecies() {
        return patientRules.getPatientSpecies(patient);
    }

    /**
     * Returns the breed name.
     *
     * @return the breed name
     */
    @Override
    public String getBreed() {
        return patientRules.getPatientBreed(patient);
    }

    /**
     * Returns the animal's sex.
     *
     * @return the sex
     */
    @Override
    public Sex getSex() {
        String code = bean.getString("sex");
        return (code != null) ? Sex.valueOf(code) : Sex.UNSPECIFIED;
    }

    /**
     * Returns the animal's colour.
     *
     * @return the animal's colour. May be {@code null}
     */
    @Override
    public String getColour() {
        return patientRules.getPatientColour(patient);
    }

    /**
     * Returns the animal's microchip number.
     *
     * @return the animal's microchip number. May be {@code null}
     */
    @Override
    public String getMicrochip() {
        return patientRules.getMicrochipNumber(patient);
    }

    /**
     * Returns the date when the animal was created in OpenVPMS.
     *
     * @return the date. May be {@code null}
     */
    @Override
    public Date getCreatedDate() {
        return bean.getDate("createdDate");
    }
}
