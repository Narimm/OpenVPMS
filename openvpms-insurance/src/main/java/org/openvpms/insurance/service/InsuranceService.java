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

package org.openvpms.insurance.service;

import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.insurance.claim.Claim;

/**
 * The service for managing insurance claims.
 *
 * @author Tim Anderson
 */
public interface InsuranceService {

    /**
     * Returns the insurance service archetype that this supports.
     *
     * @return an <em>entity.insuranceService*</em> archetype
     */
    String getArchetype();

    /**
     * Synchronises insurers.
     * <p>
     * This adds insurers that aren't already present, updates existing insurers if required, and deactivates
     * insurers that are no longer relevant.
     *
     * @return the changes that were made
     */
    Changes<Party> synchroniseInsurers();

    /**
     * Returns the declaration that users must accept, before submitting a claim.
     *
     * @return the declaration, or {@code null}, if no declaration is required
     */
    Declaration getDeclaration();

    /**
     * Submit a claim.
     * <p>
     * The claim status must be {@link Claim.Status#POSTED}. On successful submission, it will be updated to
     * {@link Claim.Status#SUBMITTED}.
     *
     * @param claim the claim to submit
     */
    void submit(Claim claim);

    /**
     * Submit an attachment to an existing claim.
     *
     * @param claim the claim
     */
    void submitAttachment(Claim claim, Document attachment);

    /**
     * Cancels a claim.
     *
     * @param claim the claim
     */
    void cancel(Claim claim);
}
