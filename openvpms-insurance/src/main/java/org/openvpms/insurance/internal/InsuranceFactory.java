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

package org.openvpms.insurance.internal;

import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.insurance.claim.Claim;
import org.openvpms.insurance.internal.claim.ClaimImpl;
import org.openvpms.insurance.internal.policy.PolicyImpl;
import org.openvpms.insurance.policy.Policy;

/**
 * Factory for insurance {@link Policy} and {@link Claim} instances.
 *
 * @author Tim Anderson
 */
public class InsuranceFactory {

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
     * Constructs a {@link InsuranceFactory}.
     *
     * @param service       the archetype service
     * @param customerRules the customer rules
     * @param patientRules  the patient rules
     */
    public InsuranceFactory(IArchetypeService service, CustomerRules customerRules, PatientRules patientRules) {
        this.service = service;
        this.customerRules = customerRules;
        this.patientRules = patientRules;
    }

    /**
     * Creates a policy, given an <em>act.patientInsurancePolicy</em>.
     *
     * @param policy the policy act
     * @return the corresponding policy
     */
    public Policy createPolicy(Act policy) {
        return new PolicyImpl(policy, service, customerRules, patientRules);
    }

    /**
     * Creates a claim, given an <em>act.patientInsuranceClaim</em>
     *
     * @param claim the claim act
     * @return the corresponding claim
     */
    public Claim createClaim(Act claim) {
        return new ClaimImpl(claim, service, customerRules, patientRules);
    }
}
