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

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.insurance.claim.Claim;

/**
 * Locates {@link InsuranceService}.
 *
 * @author Tim Anderson
 */
public interface InsuranceServices {

    /**
     * Determines if claim can be submitted via an {@link InsuranceService}.
     *
     * @param claim the claim
     * @return {@code true} if the claim can be submitted
     */
    boolean canSubmit(Claim claim);

    /**
     * Returns the insurance service for the specified insurer.
     *
     * @param insurer the insurer
     * @return the insurance service, or {@code null} if none is found
     */
    InsuranceService getService(Party insurer);

}
