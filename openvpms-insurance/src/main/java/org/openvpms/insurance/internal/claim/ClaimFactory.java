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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.insurance.internal.claim;

import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.insurance.claim.Claim;

/**
 * Factory for insurance {@link Claim} instances, given an <em>act.patientInsuranceClaim</em>.
 *
 * @author Tim Anderson
 */
public class ClaimFactory {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The customer rules.
     */
    private final CustomerRules customerRules;

    /**
     * The patient rules.
     */
    private final PatientRules patientRules;

    /**
     * Constructs a {@link ClaimFactory}.
     *
     * @param service       the archetype service
     * @param customerRules the customer rules
     * @param patientRules  the patient rules
     */
    public ClaimFactory(IArchetypeService service, CustomerRules customerRules, PatientRules patientRules) {
        this.service = service;
        this.customerRules = customerRules;
        this.patientRules = patientRules;
    }

    /**
     * Creates a claim, given an <em>act.patientInsuranceClaim</em>
     *
     * @param claim the claim act
     * @return the corresponding claim
     */
    public Claim create(Act claim) {
        return new ClaimImpl(claim, service, customerRules, patientRules);
    }
}
