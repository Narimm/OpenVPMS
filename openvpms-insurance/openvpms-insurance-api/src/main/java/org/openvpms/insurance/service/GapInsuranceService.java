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

package org.openvpms.insurance.service;

import org.openvpms.component.model.party.Party;
import org.openvpms.domain.practice.Location;
import org.openvpms.insurance.claim.GapClaim;
import org.openvpms.insurance.exception.InsuranceException;

import java.time.OffsetDateTime;

/**
 * An insurance service that supports {@link GapClaim gap claims}.
 *
 * @author Tim Anderson
 */
public interface GapInsuranceService extends InsuranceService {

    /**
     * Determines if an insurer and policy supports gap claims.
     * <p>
     * For implementations that require a policy number, this should always return {@code false} if none is provided.
     *
     * @param insurer      the insurer
     * @param policyNumber the policy number, or {@code null} if it is not known
     * @param location     the practice location
     * @return {@code true} if gap claims are supported, otherwise {@code false}
     * @throws InsuranceException for any error
     */
    boolean supportsGapClaims(Party insurer, String policyNumber, Location location);

    /**
     * Returns the times when a gap claim may be submitted, for the specified insurer and date.
     * <p>
     * If claims may not be submitted on the specified date, this returns the next available date range.
     * <p>
     * This is provided for insurers that only allow gap claim submission on certain dates and times.<p/>
     *
     * @param insurer  the insurer
     * @param date     the date
     * @param location the practice location
     * @return the times, or {@code null} if claims may not be submitted on or after the specified date.<br/>
     * For insurers that can accept gap claims at any time, return {@link Times#UNBOUNDED}
     * @throws InsuranceException for any error
     */
    Times getGapClaimSubmitTimes(Party insurer, OffsetDateTime date, Location location);

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
     * @param claim   the gap claim
     * @throws InsuranceException for any error
     */
    void notifyPayment(GapClaim claim);

}
