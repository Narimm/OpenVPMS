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
import org.openvpms.insurance.exception.InsuranceException;
import org.openvpms.insurance.i18n.InsuranceMessages;
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
     * @throws InsuranceException for any error
     */
    @Override
    public String getInsurerId() {
        ActIdentity identity = getIdentity();
        if (identity == null) {
            throw new InsuranceException(InsuranceMessages.policyHasNoId());
        }
        return identity.getIdentity();
    }

    /**
     * Returns the date when the policy expires.
     *
     * @return the policy expiry date
     * @throws InsuranceException for any error
     */
    @Override
    public Date getExpiryDate() {
        Date date = policy.getAct().getActivityEndTime();
        if (date == null) {
            throw new InsuranceException(InsuranceMessages.policyHasNoExpiryDate());
        }
        return date;
    }

    /**
     * Returns the policy holder.
     *
     * @return the policy holder
     * @throws InsuranceException for any error
     */
    @Override
    public PolicyHolder getPolicyHolder() {
        if (policyHolder == null) {
            Party customer = (Party) policy.getNodeParticipant("customer");
            if (customer == null) {
                throw new InsuranceException(InsuranceMessages.policyHasNoCustomer());
            }
            policyHolder = new PolicyHolderImpl(customer, customerRules);
        }
        return policyHolder;
    }

    /**
     * Returns the animal that the policy applies to.
     *
     * @return the animal
     * @throws InsuranceException for any error
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
     * @throws InsuranceException for any error
     */
    @Override
    public Party getInsurer() {
        if (insurer == null) {
            insurer = (Party) policy.getNodeParticipant("insurer");
            if (insurer == null) {
                throw new InsuranceException(InsuranceMessages.policyHasNoInsurer());
            }
        }
        return insurer;
    }

    /**
     * Returns the patient.
     *
     * @return the patient
     * @throws InsuranceException for any error
     */
    public Party getPatient() {
        Party patient = (Party) policy.getNodeParticipant("patient");
        if (patient == null) {
            throw new InsuranceException(InsuranceMessages.policyHasNoPatient());
        }
        return patient;
    }

    /**
     * Returns the policy identity, as specified by the insurance provider.
     *
     * @return the policy identity, or {@code null} if none is registered
     */
    protected ActIdentity getIdentity() {
        return policy.getValue("insuranceId", PredicateUtils.truePredicate(), ActIdentity.class);
    }

}
