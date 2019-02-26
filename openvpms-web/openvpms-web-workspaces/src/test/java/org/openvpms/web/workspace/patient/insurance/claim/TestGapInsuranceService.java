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

import org.openvpms.component.model.party.Party;
import org.openvpms.domain.practice.Location;
import org.openvpms.insurance.claim.Claim;
import org.openvpms.insurance.claim.GapClaim;
import org.openvpms.insurance.exception.InsuranceException;
import org.openvpms.insurance.policy.Policy;
import org.openvpms.insurance.service.Changes;
import org.openvpms.insurance.service.ClaimValidationStatus;
import org.openvpms.insurance.service.Declaration;
import org.openvpms.insurance.service.GapInsuranceService;
import org.openvpms.insurance.service.PolicyValidationStatus;
import org.openvpms.insurance.service.Times;

import java.time.OffsetDateTime;

/**
 * Test implementation of the {@link GapInsuranceService}.
 *
 * @author Tim Anderson
 */
public class TestGapInsuranceService implements GapInsuranceService {

    private int paymentNotified = 0;

    /**
     * Determines if an insurer and policy supports gap claims.
     * <p>
     * For implementations that require a policy number, this should always return {@code false} if
     * none is provided.
     *
     * @param insurer      the insurer
     * @param policyNumber the policy number, or {@code null} if it is not known
     * @param location     the practice location
     * @return {@code true} if gap claims are supported, otherwise {@code false}
     */
    @Override
    public boolean supportsGapClaims(Party insurer, String policyNumber, Location location) {
        return true;
    }

    /**
     * Returns the times when a gap claim may be submitted, for the specified insurer and date.
     * <p>
     * If claims may not be submitted on the specified date, this returns the next available date range.
     * <p>
     * This is provided for insurers that only allow gap claim submission on certain dates and times.<p/>
     *
     * @param insurer  the insurer
     * @param date     the date
     * @param location the context
     * @return the times, or {@code null} if claims may not be submitted on or after the specified date.<br/>
     * For insurers that can accept gap claims at any time, return {@link Times#UNBOUNDED}
     */
    @Override
    public Times getGapClaimSubmitTimes(Party insurer, OffsetDateTime date, Location location) {
        return Times.UNBOUNDED;
    }

    /**
     * Invoked to notify the insurer that a gap claim has been part or fully paid by the customer.
     * <p>
     * Part payment occurs if the insurer updated the claim with a non-zero benefit amount, and the customer has
     * accepted it. They have paid the gap amount, i.e. the difference between the claim total and the benefit amount.
     * <br/>
     * The insurer is responsible for paying the practice the benefit amount.
     * <p>
     * Full payment occurs if the insurer did not provide a benefit amount, or the benefit amount was rejected by
     * the customer. In this case, the insurer is responsible for settling the claim with the customer.
     * <p/>
     * For full payment, this can be invoked after the claim has been submitted, but not yet accepted by the insurer.
     * <p/>
     * On success, the {@link GapClaim#paymentNotified()} method should be invoked.
     *
     * @param claim the gap claim
     * @throws InsuranceException for any error
     */
    @Override
    public void notifyPayment(GapClaim claim) {
        paymentNotified++;
        claim.paymentNotified();
    }

    /**
     * Returns the number of times {@link GapInsuranceService#notifyPayment(GapClaim)} was invoked.
     *
     * @return the invocation count
     */
    public int getPaymentNotified() {
        return paymentNotified;
    }

    /**
     * Returns a display name for this service.
     *
     * @return a display name for this service
     */
    @Override
    public String getName() {
        return "Test Service";
    }

    /**
     * Returns the insurance service archetype that this supports.
     *
     * @return an <em>entity.insuranceService*</em> archetype
     */
    @Override
    public String getArchetype() {
        return null;
    }

    /**
     * Synchronises insurers.
     * <p>
     * This adds insurers that aren't already present, updates existing insurers if required, and deactivates
     * insurers that are no longer relevant.
     *
     * @return the changes that were made
     */
    @Override
    public Changes<Party> synchroniseInsurers() {
        return null;
    }

    /**
     * Returns the declaration that users must accept, before submitting a claim.
     *
     * @param claim the claim
     * @return the declaration, or {@code null}, if no declaration is required
     */
    @Override
    public Declaration getDeclaration(Claim claim) {
        return null;
    }

    /**
     * Validates a policy.
     *
     * @param policy the policy
     * @param location the practice location
     * @return the validation status
     * @throws InsuranceException for any error
     */
    @Override
    public PolicyValidationStatus validate(Policy policy, Location location) {
        return PolicyValidationStatus.valid();
    }

    /**
     * Validate a claim, prior to its submission.
     *
     * @param claim the claim
     * @return the validation status
     * @throws InsuranceException for any error
     */
    @Override
    public ClaimValidationStatus validate(Claim claim) {
        return ClaimValidationStatus.valid();
    }

    /**
     * Submit a claim.
     * <p>
     * The claim status must be {@link Claim.Status#POSTED}. On successful submission, it will be updated to:
     * <ul>
     * <li>{@link Claim.Status#ACCEPTED}, for services that support synchronous submission</li>
     * <li>{@link Claim.Status#SUBMITTED}, for services that support asynchronous submission. It is the
     * responsibility of the service to update the status to {@link Claim.Status#ACCEPTED}</li>
     * </ul>
     * If the service rejects the claim, it may set the status to {@link Claim.Status#PENDING} to allow the user
     * to add any missing details, and throw an {@link InsuranceException} containing the reason for the rejection.
     * <ul>
     *
     * @param claim       the claim to submit
     * @param declaration the declaration the user accepted. May be {@code null} if no declaration was required
     * @throws InsuranceException for any error
     */
    @Override
    public void submit(Claim claim, Declaration declaration) {
        claim.setStatus(Claim.Status.ACCEPTED);
    }

    /**
     * Determines if the service can cancel a claim.
     *
     * @param claim the claim
     * @return {@code true} if the service can cancel the claim
     */
    @Override
    public boolean canCancel(Claim claim) {
        return true;
    }

    /**
     * Cancels a claim.
     * <p>
     * The claim must have {@link Claim.Status#PENDING}, {@link Claim.Status#POSTED}, {@link Claim.Status#SUBMITTED}
     * or {@link Claim.Status#ACCEPTED} status.
     * <p>
     * Services that support synchronous cancellation set the status to {@link Claim.Status#CANCELLED}.<br/>
     * Services that support asynchronous cancellation should set the status to {@link Claim.Status#CANCELLING}
     *
     * @param claim   the claim
     * @param message a reason for the cancellation. This will update the <em>message</em> on the claim
     * @throws InsuranceException for any error
     */
    @Override
    public void cancel(Claim claim, String message) {
        claim.setStatus(Claim.Status.CANCELLED, message);
    }
}
