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

import org.openvpms.insurance.claim.Claim;

/**
 * Service for querying claims in OpenVPMS.
 * <p>
 * This is used by {@link InsuranceService} implementations to locate and update the status of claims issued by
 * OpenVPMS.
 *
 * @author Tim Anderson
 */
public interface ClaimService {

    /**
     * Returns a claim.
     *
     * @param archetype the claim identity archetype
     * @param claimId   the claim identifier
     * @return the corresponding claim, or {@code null} if none is found
     */
    Claim getClaim(String archetype, String claimId);
}
