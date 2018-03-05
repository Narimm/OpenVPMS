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

package org.openvpms.insurance.internal;

import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.insurance.claim.Claim;
import org.openvpms.insurance.internal.claim.ClaimImpl;
import org.openvpms.insurance.internal.policy.PolicyImpl;
import org.openvpms.insurance.policy.Policy;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Factory for insurance {@link Policy} and {@link Claim} instances.
 *
 * @author Tim Anderson
 */
public class InsuranceFactory {

    /**
     * The archetype service.
     */
    private final IArchetypeRuleService service;

    /**
     * The customer rules.
     */
    private final CustomerRules customerRules;

    /**
     * The patient rules.
     */
    private final PatientRules patientRules;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * The transaction manager.
     */
    private final PlatformTransactionManager transactionManager;

    /**
     * Constructs a {@link InsuranceFactory}.
     *
     * @param service            the archetype service
     * @param customerRules      the customer rules
     * @param patientRules       the patient rules
     * @param handlers           the document handlers
     * @param transactionManager the transaction manager
     */
    public InsuranceFactory(IArchetypeRuleService service, CustomerRules customerRules, PatientRules patientRules,
                            DocumentHandlers handlers,
                            PlatformTransactionManager transactionManager) {
        this.service = service;
        this.customerRules = customerRules;
        this.patientRules = patientRules;
        this.handlers = handlers;
        this.transactionManager = transactionManager;
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
        return createClaim(claim, service);
    }

    /**
     * Creates a claim, given an <em>act.patientInsuranceClaim</em>
     *
     * @param claim   the claim act
     * @param service the archetype service to use
     * @return the corresponding claim
     */
    public Claim createClaim(Act claim, IArchetypeRuleService service) {
        return new ClaimImpl(claim, service, customerRules, patientRules, handlers, transactionManager);
    }
}
