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

import java.math.BigDecimal;

/**
 * A gap claim is an insurance claim that is submitted to the insurer, and the insurer calculates a benefit amount.<br/>
 * The customer pays the gap, which is the difference between the total claim and the benefit amount.<br/>
 * The invoices associated with the claim must be unpaid.
 *
 * @author Tim Anderson
 */
public interface GapClaim extends Claim {

    enum GapStatus {
        PENDING,             // benefit status is pending
        RECEIVED,            // benefit amount has been received
        PAID,                // customer has paid some or all of the claim
        NOTIFIED;            // insurer has been notified of payment

        public boolean isA(String status) {
            return name().equals(status);
        }
    }

    /**
     * Returns the benefit amount.
     *
     * @return the benefit amount
     */
    BigDecimal getBenefitAmount();

    /**
     * Returns the amount that the customer has paid towards the claim.
     *
     * @return the amount the customer has paid
     */
    BigDecimal getPaid();

    /**
     * Returns the gap amount. This is the difference between the claim total and the benefit amount.
     *
     * @return the gap amount
     */
    BigDecimal getGapAmount();

    /**
     * Returns the notes associated with the benefit amount.
     *
     * @return the notes associated with the benefit amount. May be {@code null}
     */
    String getBenefitNotes();

    /**
     * Returns the gap claim status.
     *
     * @return the status, or {@code null} if this is not a gap claim
     */
    GapStatus getGapStatus();

    /**
     * Updates the gap claim with the benefit.
     * <p>
     * This is only valid when the gap status is {@link GapStatus#PENDING}.<br/>
     * This sets the gap status to {@link GapStatus#RECEIVED}.
     *
     * @param amount the benefit amount
     * @param notes  notes associated with the benefit amount
     * @throws IllegalStateException if the gap status is not {@link GapStatus#PENDING}
     */
    void setBenefit(BigDecimal amount, String notes);

    /**
     * Updates the {@link GapStatus} to {@link GapStatus#NOTIFIED}.
     * <p>
     * This is only valid when the gap status is {@link GapStatus#PAID}.
     */
    void paymentNotified();
}
