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

package org.openvpms.insurance.test.service;

import org.openvpms.component.i18n.Message;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.model.object.IMObject;
import org.openvpms.component.model.object.Identity;
import org.openvpms.component.model.party.Party;
import org.openvpms.component.service.archetype.ArchetypeService;
import org.openvpms.domain.practice.Location;
import org.openvpms.insurance.claim.Claim;
import org.openvpms.insurance.claim.Claims;
import org.openvpms.insurance.claim.GapClaim;
import org.openvpms.insurance.exception.InsuranceException;
import org.openvpms.insurance.policy.Policy;
import org.openvpms.insurance.service.Changes;
import org.openvpms.insurance.service.ClaimValidationStatus;
import org.openvpms.insurance.service.Declaration;
import org.openvpms.insurance.service.GapInsuranceService;
import org.openvpms.insurance.service.InsuranceService;
import org.openvpms.insurance.service.Insurers;
import org.openvpms.insurance.service.PolicyValidationStatus;
import org.openvpms.insurance.service.Times;
import org.openvpms.insurance.test.internal.TestInsuranceMessages;
import org.openvpms.plugin.service.archetype.ArchetypeInstaller;
import org.openvpms.plugin.service.config.ConfigurableService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Test implementation of the {@link GapInsuranceService}.
 *
 * @author Tim Anderson
 */
@Component(immediate = true, service = {GapInsuranceService.class, InsuranceService.class, ConfigurableService.class})
public class TestInsuranceService implements GapInsuranceService, ConfigurableService {

    /**
     * The executor service for asynchronous processing of claims.
     */
    private final ScheduledExecutorService executorService;

    /**
     * The archetype service.
     */
    private ArchetypeService service;

    /**
     * The configuration.
     */
    private Entity config;

    /**
     * The claims.
     */
    private Claims claims;

    /**
     * The insurers.
     */
    private Insurers insurers;

    /**
     * The number of seconds to wait before accepting a claim, or 0 to accept immediately.
     */
    private volatile int acceptDelay = 30;

    /**
     * The number of seconds after accepting a gap claim to calculate a benefit, or 0 to calculate immediately.
     */
    private volatile int benefitDelay = 30;

    /**
     * The number of seconds to wait before cancelling a claim, or 0 to cancel immediately.
     */
    private volatile int cancelDelay = 30;

    /**
     * The number of seconds to wait before settling a claim, or 0 to settle immediately.
     */
    private volatile int settleDelay = 30;

    /**
     * The number of seconds to wait before declining a claim, or 0 to decline immediately.
     */
    private volatile int declineDelay = 30;

    /**
     * If non-null, reports policies ending with the characters as not found.
     */
    private volatile String notFoundPoliciesEndingWith;

    /**
     * If non-null, reports policies ending with the characters as expired.
     */
    private volatile String expiredPoliciesEndingWith;

    /**
     * If non-null, cancels claims with policies ending with the specified characters, on submit.
     */
    private volatile String cancelPoliciesEndingWith;

    /**
     * If non-null, declines claims with policies ending with the specified characters, after acceptance.
     */
    private volatile String declinePoliciesEndingWith;

    /**
     * The insurer identity.
     */
    private static final String INSURER_ID_ARCHETYPE = "entityIdentity.insurerTest";

    /**
     * The gap insurer identity.
     */
    private static final String GAP_INSURER_ID_ARCHETYPE = "entityIdentity.insurerTestGap";


    /**
     * Constructs a {@link TestInsuranceService}.
     */
    public TestInsuranceService() {
        executorService = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Deactivates the service.
     */
    @Deactivate
    public void deactivate() {
        executorService.shutdown();
    }

    /**
     * Invoked when the service is registered, and each time the configuration is updated.
     *
     * @param config may be {@code null}, if no configuration exists, or the configuration is deactivated or removed
     */
    @Override
    public synchronized void setConfiguration(IMObject config) {
        this.config = (Entity) config;
        if (config != null && service != null) {
            IMObjectBean bean = service.getBean(config);
            acceptDelay = bean.getInt("acceptDelay");
            benefitDelay = bean.getInt("benefitDelay");
            cancelDelay = bean.getInt("cancelDelay");
            declineDelay = bean.getInt("declineDelay");
            settleDelay = bean.getInt("settleDelay");
            expiredPoliciesEndingWith = bean.getString("expiredPoliciesEndingWith");
            notFoundPoliciesEndingWith = bean.getString("notFoundPoliciesEndingWith");
            cancelPoliciesEndingWith = bean.getString("cancelPoliciesEndingWith");
            declinePoliciesEndingWith = bean.getString("declinePoliciesEndingWith");
        }
    }

    /**
     * Returns the configuration.
     *
     * @return the configuration. May be {@code null}
     */
    @Override
    public synchronized IMObject getConfiguration() {
        return config;
    }

    /**
     * Returns a display name for this service.
     *
     * @return a display name for this service
     */
    @Override
    public String getName() {
        return "Test Insurance Service";
    }

    /**
     * Returns the insurance service archetype that this supports.
     *
     * @return an <em>entity.insuranceService*</em> archetype
     */
    @Override
    public String getArchetype() {
        return "entity.insuranceServiceTest";
    }

    @Reference
    public void setArchetypeInstaller(ArchetypeInstaller installer) {
        installer.install(getClass().getResourceAsStream("/entity.insuranceServiceTest.adl"));
        installer.install(getClass().getResourceAsStream("/actIdentity.insuranceClaimTest.adl"));
        installer.install(getClass().getResourceAsStream("/entityIdentity.insurerTest.adl"));
        installer.install(getClass().getResourceAsStream("/entityIdentity.insurerTestGap.adl"));
    }

    /**
     * Registers the archetype service.
     *
     * @param service the archetype service
     */
    @Reference
    public synchronized void setArchetypeService(ArchetypeService service) {
        this.service = service;
    }

    /**
     * Registers the claims.
     *
     * @param claims the claims
     */
    @Reference
    public void setClaims(Claims claims) {
        this.claims = claims;
    }

    /**
     * Registers the insurers.
     *
     * @param insurers the insurers
     */
    @Reference
    public void setInsurers(Insurers insurers) {
        this.insurers = insurers;
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
        List<Party> added = new ArrayList<>();
        if (insurers != null && config != null) {
            addInsurer(INSURER_ID_ARCHETYPE, "T1", "Test Insurer 1", added);
            addInsurer(INSURER_ID_ARCHETYPE, "T2", "Test Insurer 2", added);
            addInsurer(INSURER_ID_ARCHETYPE, "T3", "Test Insurer 3", added);
            addInsurer(GAP_INSURER_ID_ARCHETYPE, "G1", "Test Gap Insurer 1", added);
            addInsurer(GAP_INSURER_ID_ARCHETYPE, "G2", "Test Gap Insurer 2", added);
            addInsurer(GAP_INSURER_ID_ARCHETYPE, "G3", "Test Gap Insurer 3", added);
        }
        return new Changes<>(added, Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Returns the declaration that users must accept, before submitting a claim.
     *
     * @param claim the claim
     * @return the declaration, or {@code null}, if no declaration is required
     */
    @Override
    public Declaration getDeclaration(Claim claim) {
        return () -> "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut " +
                     "labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco " +
                     "laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in " +
                     "voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat " +
                     "non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
    }

    /**
     * Validates a policy.
     *
     * @param policy   the policy
     * @param location the practice location
     * @return the validation status
     * @throws InsuranceException for any error
     */
    @Override
    public PolicyValidationStatus validate(Policy policy, Location location) {
        PolicyValidationStatus result;
        Party insurer = policy.getInsurer();
        String insurerId = insurers.getInsurerId(insurer);
        String policyNumber = policy.getPolicyNumber();
        if (policyNumber == null || !policyNumber.startsWith(insurerId)) {
            Message message = TestInsuranceMessages.invalidPolicyNumber(policyNumber, insurer, insurerId);
            result = PolicyValidationStatus.invalid(message.getMessage());
        } else if (policyNumber.length() != 10) {
            Message message = TestInsuranceMessages.policyNumberMustBe10Chars();
            result = PolicyValidationStatus.invalid(message.getMessage());
        } else if (notFoundPoliciesEndingWith != null && policyNumber.endsWith(notFoundPoliciesEndingWith)) {
            result = PolicyValidationStatus.notFound();
        } else if (expiredPoliciesEndingWith != null && policyNumber.endsWith(expiredPoliciesEndingWith)) {
            result = PolicyValidationStatus.expired();
        } else {
            result = PolicyValidationStatus.valid();
        }
        return result;
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
        PolicyValidationStatus status = validate(claim.getPolicy(), claim.getLocation());
        switch (status.getStatus()) {
            case INVALID:
                return ClaimValidationStatus.error(status.getMessage());
            case NOT_FOUND:
                return ClaimValidationStatus.error("The policy was not found");
            case EXPIRED:
                return ClaimValidationStatus.error("The policy has expired");
            case UNSUPPORTED:
                return ClaimValidationStatus.warning("The policy number could not be validated.");
            case CHANGE_POLICY_NUMBER:
                return ClaimValidationStatus.error(status.getMessage());
            default:
                return ClaimValidationStatus.valid();
        }
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
        if (claim.getStatus() != Claim.Status.POSTED) {
            throw new IllegalArgumentException("Claim must be POSTED");
        }
        claim.setInsurerId("actIdentity.insuranceClaimTest", UUID.randomUUID().toString());
        if (acceptDelay == 0) {
            accept(claim);
        } else {
            // set status to ACCEPTED after delay
            claim.setStatus(Claim.Status.SUBMITTED);
            String claimId = claim.getInsurerId();
            executorService.schedule(() -> accept(claimId), acceptDelay, TimeUnit.SECONDS);
        }
    }

    /**
     * Determines if the service can cancel a claim.
     *
     * @param claim the claim
     * @return {@code true} if the service can cancel the claim
     */
    @Override
    public boolean canCancel(Claim claim) {
        return claim.getStatus() != Claim.Status.CANCELLED
               && claim.getStatus() != Claim.Status.CANCELLING
               && claim.getStatus() != Claim.Status.DECLINED
               && claim.getStatus() != Claim.Status.SETTLED;
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
        if (cancelDelay == 0) {
            claim.setStatus(Claim.Status.CANCELLED, message);
        } else {
            claim.setStatus(Claim.Status.CANCELLING, message);
            String insurerId = claim.getInsurerId();
            executorService.schedule(() -> cancel(insurerId), cancelDelay, TimeUnit.SECONDS);
        }
    }

    /**
     * Determines if an insurer can receive gap claims.
     *
     * @param insurer      the insurer
     * @param policyNumber the policy number
     * @param location     the practice location
     * @return {@code true} if the insurer supports gap claims, otherwise {@code false}
     */
    @Override
    public boolean supportsGapClaims(Party insurer, String policyNumber, Location location) {
        Identity insurerId = insurers.getIdentifier(insurer);
        return insurerId != null && GAP_INSURER_ID_ARCHETYPE.equals(insurerId.getArchetype());
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
        claim.paymentNotified();
        if (claim.getStatus() == Claim.Status.ACCEPTED) {
            scheduleSettle(claim.getInsurerId());
        }
    }

    /**
     * Calculates a benefit for a gap claim.
     *
     * @param claimId the claim identifier
     */
    protected void calculateBenefit(String claimId) {
        GapClaim claim = (GapClaim) getClaim(claimId);
        if (claim.getStatus() == Claim.Status.ACCEPTED && claim.getGapStatus() == GapClaim.GapStatus.PENDING) {
            BigDecimal amount = claim.getTotal().multiply(new BigDecimal("0.75")).setScale(2, RoundingMode.HALF_UP);
            claim.setBenefit(amount, "Claim accepted");
        }
    }

    /**
     * Adds an insurer if it doesn't exist.
     *
     * @param archetype the identity archetype
     * @param id        the insurer identity
     * @param name      the insurer name
     * @param added     the list to add new insurers rto
     */
    private void addInsurer(String archetype, String id, String name, List<Party> added) {
        Party insurer = insurers.getInsurer(archetype, id);
        if (insurer == null) {
            insurer = insurers.createInsurer(archetype, id, name, "Online insurer", config);
            added.add(insurer);
        }
    }

    /**
     * Schedules calculation of a gap claim benefit.
     *
     * @param claimId the claim identifier
     */
    private void scheduleBenefit(String claimId) {
        schedule(claimId, this::calculateBenefit, benefitDelay);
    }

    /**
     * Schedules settlement of a claim.
     *
     * @param claimId the claim identifier
     */
    private void scheduleSettle(String claimId) {
        schedule(claimId, this::settle, settleDelay);
    }

    /**
     * Cancels a claim, if it is not {@link Claim.Status#SETTLED} or {@link Claim.Status#DECLINED}.
     *
     * @param claimId the claim identifier
     */
    private void cancel(String claimId) {
        Claim claim = getClaim(claimId);
        if (claim != null) {
            Claim.Status status = claim.getStatus();
            if (status != Claim.Status.CANCELLED && status != Claim.Status.SETTLED && status != Claim.Status.DECLINED) {
                // only update the status if it is not in a terminal state
                claim.setStatus(Claim.Status.CANCELLED);
            }
        }
    }

    /**
     * Returns a claim given its claim identifier.
     *
     * @param claimId the claim identifier
     * @return the corresponding claim
     */
    private Claim getClaim(String claimId) {
        return claims.getClaim("actIdentity.insuranceClaimTest", claimId);
    }

    /**
     * Accepts a claim, if it is {@link Claim.Status#SUBMITTED}.
     *
     * @param claimId the claim identifier
     */
    private void accept(String claimId) {
        Claim claim = getClaim(claimId);
        if (claim != null) {
            accept(claim);
        }
    }

    /**
     * Accepts a claim, if its status is {@link Claim.Status#POSTED} or {@link Claim.Status#SUBMITTED},
     * unless the claim is to be cancelled or declined.
     *
     * @param claim the claim
     */
    private void accept(Claim claim) {
        if (claim.getStatus() == Claim.Status.SUBMITTED) {
            String cancel = cancelPoliciesEndingWith;
            String decline = declinePoliciesEndingWith;
            if (cancel != null && claim.getInsurerId().endsWith(cancel)) {
                claim.setStatus(Claim.Status.CANCELLED, "Cancelled by the insurer");
            } else {
                claim.setStatus(Claim.Status.ACCEPTED);
                if (decline != null && claim.getInsurerId().endsWith(decline)) {
                    scheduleDecline(claim.getInsurerId());
                } else if (claim instanceof GapClaim) {
                    scheduleBenefit(claim.getInsurerId());
                } else {
                    scheduleSettle(claim.getInsurerId());
                }
            }
        }
    }

    /**
     * Settles a claim, if it is {@link Claim.Status#SETTLED}.
     *
     * @param claimId the claim identifier
     */
    private void settle(String claimId) {
        Claim claim = getClaim(claimId);
        if (claim.getStatus() == Claim.Status.ACCEPTED) {
            claim.setStatus(Claim.Status.SETTLED);
        }
    }

    /**
     * Schedules declining a claim.
     *
     * @param claimId the claim identifier
     */
    private void scheduleDecline(String claimId) {
        schedule(claimId, this::decline, declineDelay);
    }

    /**
     * Declines a claim.
     *
     * @param claimId the claim identifier
     */
    private void decline(String claimId) {
        Claim claim = getClaim(claimId);
        claim.setStatus(Claim.Status.DECLINED, "Declined by the insurer");
    }

    /**
     * Schedules an action for a claim, or runs it immediately if there is no delay.
     *
     * @param claimId the claim identifier
     * @param action  the action to run
     * @param delay   the delay
     */
    private void schedule(String claimId, Consumer<String> action, int delay) {
        if (delay == 0) {
            action.accept(claimId);
        } else {
            executorService.schedule(() -> action.accept(claimId), delay, TimeUnit.SECONDS);
        }
    }
}
