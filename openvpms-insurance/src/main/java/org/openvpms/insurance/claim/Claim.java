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

package org.openvpms.insurance.claim;

import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.insurance.exception.InsuranceException;
import org.openvpms.insurance.policy.Animal;
import org.openvpms.insurance.policy.Policy;

import java.util.List;

/**
 * Patient insurance claim.
 *
 * @author Tim Anderson
 */
public interface Claim {

    enum Status {
        PENDING,
        POSTED,
        SUBMITTED,
        ACCEPTED,
        SETTLED,
        DECLINED,
        CANCELLED;

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
     * Finalises the claim prior to submission.
     * <p>
     * The claim can only be finalised if it has {@link Status#PENDING PENDING} status, and all attachments have
     * content, and no attachments have {@link Attachment.Status#ERROR ERROR} status.
     *
     * @throws InsuranceException if the claim cannot be finalised
     */
    void finalise();

}
