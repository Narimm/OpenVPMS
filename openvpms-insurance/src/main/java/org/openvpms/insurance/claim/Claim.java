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
     * Sets the cleaim identifier, issued by the insurer.
     *
     * @param id the claim identifier
     */
    void setClaimId(String id);

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

}
