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

package org.openvpms.insurance.claim;

import org.openvpms.component.model.party.Party;
import org.openvpms.component.model.user.User;
import org.openvpms.insurance.exception.InsuranceException;
import org.openvpms.insurance.policy.Animal;
import org.openvpms.insurance.policy.Policy;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Patient insurance claim.
 *
 * @author Tim Anderson
 */
public interface Claim {

    enum Status {
        PENDING,    // claim is pending. User can make changes
        POSTED,     // claim is finalised. No further changes may be made prior to submission.
        SUBMITTED,  // claim has been submitted to the insurer
        ACCEPTED,   // claim has been accepted, and is being processed
        SETTLED,    // claim has been settled by the insurer
        DECLINED,   // claim has been declined by the insurer
        CANCELLING, // claim is in the process of being cancelled
        CANCELLED;  // claim has been cancelled

        public boolean isA(String status) {
            return name().equals(status);
        }
    }

    /**
     * Returns the OpenVPMS identifier for this claim.
     *
     * @return the identifier
     */
    long getId();

    /**
     * Returns the claim identifier, issued by the insurer.
     *
     * @return the claim identifier, or {@code null} if none has been issued
     */
    String getInsurerId();

    /**
     * Sets the claim identifier, issued by the insurer.
     * <p>
     * A claim can have a single identifier issued by an insurer. To avoid duplicates, each insurance service must
     * provide a unique archetype.
     *
     * @param archetype the identifier archetype. Must have an <em>actIdentity.insuranceClaim</em> prefix.
     * @param id        the claim identifier
     * @throws InsuranceException if the identifier cannot be set
     */
    void setInsurerId(String archetype, String id);

    /**
     * Returns the date when the claim was created.
     *
     * @return the date
     */
    Date getCreated();

    /**
     * Returns the date when the claim was completed.
     * <p>
     * This represents the date when the claim was cancelled, settled, or declined.
     *
     * @return the date, or {@code null} if the claim hasn't been completed
     */
    Date getCompleted();

    /**
     * Returns the discount amount, including tax.
     *
     * @return the discount amount
     */
    BigDecimal getDiscount();

    /**
     * Returns the discount tax amount.
     *
     * @return the discount tax amount
     */
    BigDecimal getDiscountTax();

    /**
     * Returns the total amount being claimed, including tax.
     *
     * @return the total amount
     */
    BigDecimal getTotal();

    /**
     * Returns the total tax amount.
     *
     * @return the tax amount
     */
    BigDecimal getTotalTax();

    /**
     * Returns the animal that the claim applies to.
     *
     * @return the animal
     */
    Animal getAnimal();

    /**
     * Returns the policy that a claim is being made on.
     *
     * @return the policy
     */
    Policy getPolicy();

    /**
     * Returns the claim status.
     *
     * @return the claim status
     */
    Status getStatus();

    /**
     * Sets the claim status.
     *
     * @param status the claim status
     */
    void setStatus(Status status);

    /**
     * Sets the claim status, along with any message from the insurer.
     *
     * @param status  the status
     * @param message the message. May be {@code null}
     */
    void setStatus(Status status, String message);

    /**
     * Returns the conditions being claimed.
     *
     * @return the conditions being claimed
     */
    List<Condition> getConditions();

    /**
     * Returns the clinical history for the patient.
     *
     * @return the clinical history
     */
    List<Note> getClinicalHistory();

    /**
     * Returns the attachments.
     *
     * @return the attachments
     */
    List<Attachment> getAttachments();

    /**
     * Returns the clinician responsible for the claim.
     *
     * @return the clinician
     */
    User getClinician();

    /**
     * Returns the claim handler.
     *
     * @return the claim handler
     */
    ClaimHandler getClaimHandler();

    /**
     * Returns the location where the claim was created.
     *
     * @return the practice location
     */
    Party getLocation();

    /**
     * Sets a message on the claim. This may be used by insurance service to convey to users the status of the claim,
     * or why a claim was declined.
     *
     * @param message the message. May be {@code null}
     */
    void setMessage(String message);

    /**
     * Returns the message.
     *
     * @return the message. May be {@code null}
     */
    String getMessage();

    /**
     * Determines if this claim can be cancelled.
     *
     * @return {@code true} if the claim is {@link Status#PENDING}, {@link Status#POSTED}, {@link Status#SUBMITTED}
     * or {@link Status#ACCEPTED}.
     */
    boolean canCancel();

    /**
     * Finalises the claim prior to submission.
     * <p>
     * The claim can only be finalised if it has {@link Status#PENDING PENDING} status, and all attachments have
     * content, and no attachments have {@link Attachment.Status#ERROR ERROR} status.
     *
     * @throws InsuranceException if the claim cannot be finalised
     */
    void finalise();

}
