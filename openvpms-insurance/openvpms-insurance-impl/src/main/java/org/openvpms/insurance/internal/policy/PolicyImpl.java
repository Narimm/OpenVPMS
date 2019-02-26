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

import org.openvpms.archetype.rules.party.PartyRules;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.model.act.Act;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.object.Identity;
import org.openvpms.component.model.party.Party;
import org.openvpms.domain.customer.Customer;
import org.openvpms.domain.internal.customer.CustomerImpl;
import org.openvpms.domain.internal.patient.PatientImpl;
import org.openvpms.domain.patient.Patient;
import org.openvpms.insurance.exception.InsuranceException;
import org.openvpms.insurance.internal.i18n.InsuranceMessages;
import org.openvpms.insurance.policy.Policy;

import java.time.OffsetDateTime;

/**
 * Default implementation of the {@link Policy} interface.
 *
 * @author Tim Anderson
 */
public class PolicyImpl implements Policy {

    /**
     * The policy.
     */
    private final IMObjectBean policy;

    /**
     * The underlying act.
     */
    private final Act act;

    /**
     * The archetype service.
     */
    private final IArchetypeRuleService service;

    /**
     * The party rules.
     */
    private final PartyRules partyRules;

    /**
     * The patient rules.
     */
    private final PatientRules patientRules;

    /**
     * The customer.
     */
    private Party customer;

    /**
     * The policy holder.
     */
    private Customer policyHolder;

    /**
     * The patient.
     */
    private Patient animal;

    /**
     * The insurer.
     */
    private Party insurer;


    /**
     * Constructs a {@link PolicyImpl}.
     *
     * @param policy       the policy
     * @param service      the archetype service
     * @param partyRules   the party rules
     * @param patientRules the patient rules
     */
    public PolicyImpl(Act policy, IArchetypeRuleService service, PartyRules partyRules, PatientRules patientRules) {
        this.policy = service.getBean(policy);
        this.act = policy;
        this.service = service;
        this.partyRules = partyRules;
        this.patientRules = patientRules;
    }

    /**
     * Returns the OpenVPMS identifier for this policy.
     *
     * @return the identifier
     */
    @Override
    public long getId() {
        return policy.getObject().getId();
    }

    /**
     * Returns the policy number.
     * <p>
     * This is short for {@code getInsurerId().getIdentity()}
     *
     * @return the policy number, or {@code null} if none has been assigned
     */
    @Override
    public String getPolicyNumber() {
        Identity insurerId = policy.getObject("insurerId", Identity.class);
        return (insurerId != null) ? insurerId.getIdentity() : null;
    }

    /**
     * Returns the date when the policy expires.
     *
     * @return the policy expiry date, or {@code null} if it is not known
     */
    @Override
    public OffsetDateTime getExpiryDate() {
        return DateRules.toOffsetDateTime(act.getActivityEndTime());
    }

    /**
     * Returns the policy holder.
     *
     * @return the policy holder
     * @throws InsuranceException for any error
     */
    @Override
    public Customer getPolicyHolder() {
        if (policyHolder == null) {
            customer = policy.getTarget("customer", Party.class);
            if (customer == null) {
                throw new InsuranceException(InsuranceMessages.policyHasNoCustomer());
            }
            policyHolder = new CustomerImpl(customer, service, partyRules);
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
    public Patient getAnimal() {
        if (animal == null) {
            Party patient = getPatient();
            animal = new PatientImpl(patient, service, patientRules);
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
            insurer = policy.getTarget("insurer", Party.class);
            if (insurer == null) {
                throw new InsuranceException(InsuranceMessages.policyHasNoInsurer());
            }
        }
        return insurer;
    }

    /**
     * Returns the policy act.
     *
     * @return the policy act
     */
    public Act getAct() {
        return act;
    }

    /**
     * Returns the customer.
     *
     * @return the customer
     */
    public Party getCustomer() {
        getPolicyHolder();
        return customer;
    }

    /**
     * Returns the patient.
     *
     * @return the patient
     * @throws InsuranceException for any error
     */
    public Party getPatient() {
        Party patient = policy.getTarget("patient", Party.class);
        if (patient == null) {
            throw new InsuranceException(InsuranceMessages.policyHasNoPatient());
        }
        return patient;
    }

}
