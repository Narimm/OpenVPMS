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

package org.openvpms.insurance.internal.policy;

import org.apache.commons.collections.PredicateUtils;
import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActIdentity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.insurance.policy.Animal;
import org.openvpms.insurance.policy.Policy;
import org.openvpms.insurance.policy.PolicyHolder;

import java.util.Date;

/**
 * Default implementation of the {@link Policy} interface.
 *
 * @author Tim Anderson
 */
public class PolicyImpl implements Policy {

    /**
     * The policy.
     */
    private final ActBean policy;

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
     * The policy holder.
     */
    private PolicyHolder policyHolder;

    /**
     * The animal.
     */
    private Animal animal;

    /**
     * The insurer.
     */
    private Party insurer;


    /**
     * Constructs a {@link PolicyImpl}.
     *
     * @param policy        the policy
     * @param service       the archetype service
     * @param customerRules the customer rules
     * @param patientRules  the patient rules
     */
    public PolicyImpl(Act policy, IArchetypeService service, CustomerRules customerRules, PatientRules patientRules) {
        this.policy = new ActBean(policy, service);
        this.service = service;
        this.customerRules = customerRules;
        this.patientRules = patientRules;
    }

    /**
     * Returns the policy identifier, issued by the insurer.
     *
     * @return the policy identifier
     */
    @Override
    public String getPolicyId() {
        ActIdentity identity = getIdentity();
        if (identity == null) {
            throw new IllegalStateException("Policy has no policyId: " + policy.getAct().getId());
        }
        return identity.getIdentity();
    }

    /**
     * Returns the date when the policy expires.
     *
     * @return the policy expiry date
     */
    @Override
    public Date getExpiryDate() {
        return policy.getAct().getActivityEndTime();
    }

    /**
     * Returns the policy holder.
     *
     * @return the policy holder
     */
    @Override
    public PolicyHolder getPolicyHolder() {
        if (policyHolder == null) {
            Party customer = (Party) policy.getNodeParticipant("customer");
            if (customer == null) {
                throw new IllegalStateException("Policy has no customer: " + policy.getAct().getId());
            }
            policyHolder = new PolicyHolderImpl(customer, customerRules);
        }
        return null;
    }

    /**
     * Returns the animal that the policy applies to.
     *
     * @return the animal
     */
    @Override
    public Animal getAnimal() {
        if (animal == null) {
            Party patient = getPatient();
            animal = new AnimalImpl(patient, service, patientRules);
        }
        return animal;
    }

    /**
     * Returns the insurer that issued the policy.
     *
     * @return insurer that issued the policy
     */
    @Override
    public Party getInsurer() {
        if (insurer == null) {
            insurer = (Party) policy.getNodeParticipant("insurer");
            if (insurer == null) {
                throw new IllegalStateException("Policy has no insurer: " + policy.getAct().getId());
            }
        }
        return insurer;
    }

    /**
     * Returns the patient.
     *
     * @return the patient
     */
    public Party getPatient() {
        Party patient = (Party) policy.getNodeParticipant("patient");
        if (patient == null) {
            throw new IllegalStateException("Policy has no patient:" + policy.getAct().getId());
        }
        return patient;
    }

    /**
     * Returns the policy identity, as specified by the insurance provider.
     *
     * @return the policy identity, or {@code null} if none is registered
     */
    protected ActIdentity getIdentity() {
        return policy.getValue("policy", PredicateUtils.truePredicate(), ActIdentity.class);
    }

}
