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

import org.openvpms.component.business.domain.im.document.Document;
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
        SETTLED,
        DECLINED,
        CANCELLED
    }

    /**
     * Returns the OpenVPMS identifier for this claim.
     *
     * @return the claim identifier
     */
    long getId();

    /**
     * Returns the claim identifier, issued by the insurer.
     *
     * @return the claim identifier, or {@code null} if none has been issued
     */
    String getClaimId();

    /**
     * Sets the claim identifier, issued by the insurer.
     * <p>
     * A claim can have a single identifier issued by an insurer. To avoid duplicates, each insurance service must
     * provide a unique archetype.
     *
     * @param archetype the identifier archetype. Must have an <em>actIdentity.insuranceClaim</em> prefix.
     * @param id        the claim identifier
     */
    void setClaimId(String archetype, String id);

    /**
     * Returns the policy that a claim is being made on.
     *
     * @return the policy
     */
    Policy getPolicy();

    /**
     * Returns the claim status
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
     * Adds an attachment to the claim.
     * <p>
     * This may be done while the status is one of {@link Status#PENDING PENDING} {@link Status#POSTED POSTED}, or
     * {@link Status#SUBMITTED SUBMITTED}. For the latter two statuses, it is used to supplement an existing claim.
     * <p>
     * The caller is responsible for submitting the attachment to the insurer.
     *
     * @param attachment the attachment
     */
    void addAttachment(Document attachment);

}
