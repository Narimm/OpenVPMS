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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.insurance.claim;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.insurance.InsuranceRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.act.Act;
import org.openvpms.component.model.act.ActIdentity;
import org.openvpms.component.model.act.ActRelationship;
import org.openvpms.component.model.act.FinancialAct;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.model.user.User;
import org.openvpms.domain.practice.Location;
import org.openvpms.insurance.internal.InsuranceFactory;
import org.openvpms.insurance.service.GapInsuranceService;
import org.openvpms.insurance.service.InsuranceService;
import org.openvpms.insurance.service.InsuranceServices;
import org.openvpms.insurance.service.Times;
import org.openvpms.web.component.util.ErrorHelper;

import java.time.OffsetDateTime;

/**
 * Manages the relationship between a claim and a policy.
 *
 * @author Tim Anderson
 */
public class ClaimContext {

    /**
     * The claim.
     */
    private final IMObjectBean claim;

    /**
     * The customer.
     */
    private final Party customer;

    /**
     * The patient.
     */
    private final Party patient;

    /**
     * The logged in user.
     */
    private final User user;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The insurance rules.
     */
    private final InsuranceRules rules;

    /**
     * The insurance services.
     */
    private final InsuranceServices insuranceServices;

    /**
     * The insurance factory.
     */
    private final InsuranceFactory factory;

    /**
     * The location.
     */
    private Location location;

    /**
     * Determines if the policy has changed.
     */
    private boolean modified;

    /**
     * The policy.
     */
    private Act policy;

    /**
     * The insurer.
     */
    private Party insurer;

    /**
     * The policy number.
     */
    private String policyNumber;

    /**
     * The insurance service.
     */
    private InsuranceService insuranceService;

    /**
     * Determines if the insurer supports gap claims.
     */
    private boolean supportsGapClaims;

    /**
     * Constructs a new {@link ClaimContext}.
     *
     * @param claim             the claim
     * @param customer          the customer
     * @param patient           the patient
     * @param user              the logged in user
     * @param location          the practice location. May be {@code null}
     * @param service           the archetype service
     * @param insuranceServices the insurance services
     * @param factory           the insurance factory
     */
    public ClaimContext(FinancialAct claim, Party customer, Party patient, User user, Party location,
                        IArchetypeService service, InsuranceRules rules,
                        InsuranceServices insuranceServices, InsuranceFactory factory) {
        this.customer = customer;
        this.patient = patient;
        this.user = user;
        this.service = service;
        this.rules = rules;
        this.insuranceServices = insuranceServices;
        this.factory = factory;
        this.claim = service.getBean(claim);
        this.location = getLocation(location);
        setPolicy(this.claim.getTarget("policy", Act.class));
    }

    /**
     * Returns the customer.
     *
     * @return the customer
     */
    public Party getCustomer() {
        return customer;
    }

    /**
     * Returns the patient.
     *
     * @return the patient
     */
    public Party getPatient() {
        return patient;
    }

    /**
     * Returns the insurer.
     *
     * @return the insurer, or {@code null} if the claim has no policy
     */
    public Party getInsurer() {
        return insurer;
    }

    /**
     * Sets the insurer.
     * <p>
     * If the patient has an existing policy for the insurer, the policy and policy number will be updated to reflect
     * this, otherwise they will be cleared.
     *
     * @param insurer the insurer. May be {@code null}
     */
    public void setInsurer(Party insurer) {
        Act newPolicy = (insurer != null) ? rules.getPolicy(customer, patient, insurer) : null;
        if (newPolicy != null) {
            setPolicy(newPolicy);
        } else {
            updateInsurer(insurer);
            this.policy = null;
            this.policyNumber = null;
        }
    }

    /**
     * Returns the policy.
     *
     * @return the policy. May be {@code null}
     */
    public Act getPolicy() {
        return policy;
    }

    /**
     * Sets the policy.
     *
     * @param policy the policy. May be {@code null}
     */
    public void setPolicy(Act policy) {
        Party newInsurer = null;
        String newPolicyNumber = null;
        this.policy = policy;
        if (policy != null) {
            IMObjectBean bean = service.getBean(policy);
            newPolicyNumber = getPolicyNumber(bean);

            Reference reference = customer.getObjectReference();
            if (!reference.equals(bean.getTargetRef("customer"))) {
                throw new IllegalArgumentException("Argument 'policy' must be for customer=" + reference);
            }
            newInsurer = bean.getTarget("insurer", Party.class);
        }
        policyNumber = newPolicyNumber;
        updateInsurer(newInsurer);
    }

    /**
     * Returns the policy number.
     *
     * @return the policy number. May be {@code null}
     */
    public String getPolicyNumber() {
        return policyNumber;
    }

    /**
     * Sets the policy number.
     * <p>
     * If different to the existing policy, it clears the policy to ensure a new one is created.
     *
     * @param policyNumber the policy number. May be {@code null}
     * @return {@code true} if the policy number is different to the existing one
     */
    public boolean setPolicyNumber(String policyNumber) {
        boolean result = false;
        if (!StringUtils.equals(policyNumber, this.policyNumber)) {
            this.policyNumber = policyNumber;
            policy = null;
            result = true;
        }
        return result;
    }

    /**
     * Sets the practice location.
     *
     * @param location the location. May be {@code null}
     */
    public void setLocation(Party location) {
        this.location = getLocation(location);
        if (this.location != null) {
            updateService();
        }
    }

    /**
     * Returns the times when gap claims can be submitted.
     *
     * @return the gap claim submit times, or {@code null} if gap claims cannot be submitted
     */
    public Times getGapClaimSubmitTimes() {
        if (insurer != null && insuranceService != null && location != null && supportsGapClaims) {
            try {
                return ((GapInsuranceService) insuranceService).getGapClaimSubmitTimes(insurer, OffsetDateTime.now(),
                                                                                       location);
            } catch (Throwable exception) {
                ErrorHelper.show(exception);
            }
        }
        return null;
    }

    /**
     * Determines if the claim is a gap claim.
     *
     * @return {@code true} if the claim is a gap claim
     */
    public boolean isGapClaim() {
        return claim.getBoolean("gapClaim");
    }

    /**
     * Determines if the insurer supports gap claims.
     *
     * @return {@code true} if the insurer supports gap claims
     */
    public boolean supportsGapClaims() {
        return supportsGapClaims;
    }

    /**
     * Prepares the claim for save.
     * <p>
     * This creates the policy if required, and links the claim to it.
     */
    public void prepare() {
        if (insurer != null) {
            boolean addRelationship = false;
            if (policy == null) {
                policy = rules.getPolicyForClaim(customer, patient, insurer, policyNumber, user,
                                                 (FinancialAct) claim.getObject(), null);
                modified = true; // assume that if it is an existing policy, it has been updated with the policy
            }
            if (!ObjectUtils.equals(claim.getTargetRef("policy"), policy.getObjectReference())) {
                ActRelationship relationship = (ActRelationship) claim.setTarget("policy", policy);
                policy.addActRelationship(relationship);
                addRelationship = true;
            }
            modified = modified || addRelationship || policy.isNew();
        }
    }

    /**
     * Saves the policy if required.
     */
    public void save() {
        if (modified) {
            service.save(policy);
            modified = false;
        }
    }

    /**
     * Returns the policy number from a policy.
     *
     * @param bean the policy
     * @return the policy number or {@code null} if the policy doesn't have one
     */
    protected String getPolicyNumber(IMObjectBean bean) {
        ActIdentity insurerId = bean.getObject("insurerId", ActIdentity.class);
        return (insurerId != null) ? insurerId.getIdentity() : null;
    }

    /**
     * Updates the insurer.
     *
     * @param insurer the insurer. May be {@code null}
     */
    protected void updateInsurer(Party insurer) {
        this.insurer = insurer;
        updateService();
    }

    /**
     * Returns the location.
     *
     * @param location the location party. May be {@code null}
     * @return the corresponding location. May be {@code null}
     */
    private Location getLocation(Party location) {
        return location != null ? factory.getLocation(location) : null;
    }

    /**
     * Updates the insurance service when the insurer or location changes.
     */
    private void updateService() {
        InsuranceService newService = null;
        boolean newSupportsGapClaims = false;
        if (insurer != null && location != null) {
            newService = insuranceServices.getService(insurer);
            if (newService instanceof GapInsuranceService) {
                try {
                    newSupportsGapClaims = ((GapInsuranceService) newService).supportsGapClaims(insurer, policyNumber,
                                                                                                location);
                } catch (Throwable exception) {
                    ErrorHelper.show(exception);
                }
            }
        }

        insuranceService = newService;
        supportsGapClaims = newSupportsGapClaims;
    }

}
