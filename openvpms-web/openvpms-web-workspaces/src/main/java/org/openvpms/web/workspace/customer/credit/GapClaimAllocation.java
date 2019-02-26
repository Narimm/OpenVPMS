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

package org.openvpms.web.workspace.customer.credit;

import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.insurance.claim.GapClaim;
import org.openvpms.insurance.internal.InsuranceFactory;
import org.openvpms.insurance.internal.claim.GapClaimImpl;
import org.openvpms.web.resource.i18n.Messages;

import java.math.BigDecimal;

/**
 * Associates a payment (or other credit) allocation with a gap claim.
 *
 * @author Tim Anderson
 */
public class GapClaimAllocation {

    /**
     * The allocation status.
     */
    public enum Status {
        NO_BENEFIT_PARTIAL_PAYMENT, // no benefit has been received from the insurer, and payment is less than the claim total
        NO_BENEFIT_FULL_PAYMENT,    // no benefit has been received from the insurer, and the claim has been fully paid
        ALLOCATION_LESS_THAN_GAP,   // benefit has been received from the insurer, but the allocation is less than the gap amount
        ALLOCATION_EQUAL_TO_GAP,    // benefit has been received from the insurer, and the allocation is equal to the the gap amount
        ALLOCATION_GREATER_THAN_GAP,// benefit has been received from the insurer, and the allocation is greater than the gap amount
        FULL_PAYMENT,               // benefit has been received from the insurer, and the claim has been fully paid
    }

    /**
     * The gap claim.
     */
    private final GapClaimImpl claim;

    /**
     * The allocation. This is the sum of the allocations of the associated invoices, incorporating any new allocation.
     */
    private final BigDecimal allocation;

    /**
     * The sum of existing invoice allocations.
     */
    private final BigDecimal existingAllocation;

    /**
     * The sum of new credit allocations.
     */
    private final BigDecimal newAllocation;

    /**
     * The allocation status.
     */
    private final Status status;


    /**
     * Constructs a {@link GapClaimAllocation}.
     *
     * @param act                the gap claim act
     * @param existingAllocation the existing allocation. This is the sum of the allocations of the associated invoices
     * @param newAllocation      the sum of the new credit allocations
     * @param factory            the insurance factory
     */
    public GapClaimAllocation(FinancialAct act, BigDecimal existingAllocation, BigDecimal newAllocation,
                              InsuranceFactory factory) {
        this.allocation = existingAllocation.add(newAllocation);
        this.existingAllocation = existingAllocation;
        this.newAllocation = newAllocation;
        claim = (GapClaimImpl) factory.createClaim(act);
        if (benefitPending()) {
            if (allocation.compareTo(claim.getTotal()) < 0) {
                status = Status.NO_BENEFIT_PARTIAL_PAYMENT;
            } else {
                status = Status.NO_BENEFIT_FULL_PAYMENT;
            }
        } else {
            BigDecimal gapAmount = claim.getGapAmount();
            int compareTo = allocation.compareTo(gapAmount);
            if (compareTo < 0) {
                status = Status.ALLOCATION_LESS_THAN_GAP;
            } else if (compareTo == 0) {
                status = Status.ALLOCATION_EQUAL_TO_GAP;
            } else {
                // paying more than the gap amount
                if (allocation.compareTo(claim.getTotal()) < 0) {
                    // but less than the claim total
                    status = Status.ALLOCATION_GREATER_THAN_GAP;
                } else {
                    status = Status.FULL_PAYMENT;
                }
            }
        }
    }

    /**
     * Returns the claim total.
     *
     * @return the claim total
     */
    public BigDecimal getTotal() {
        return claim.getTotal();
    }

    /**
     * Returns the allocation status.
     *
     * @return the allocation
     */
    public GapClaimAllocation.Status getStatus() {
        return status;
    }

    /**
     * Returns the allocation.
     * <p>
     * This is the sum of the existing allocation, and any new payment allocation.
     *
     * @return the allocation
     */
    public BigDecimal getAllocation() {
        return allocation;
    }

    /**
     * Returns the existing claim allocation.
     * <p>
     * This is the sum of the existing invoice allocations.
     *
     * @return the existing allocation
     */
    public BigDecimal getExistingAllocation() {
        return existingAllocation;
    }

    /**
     * Returns the new credit allocation.
     * <p>
     * This is the sum of the existing allocation, and any new credit allocation.
     *
     * @return the new allocation
     */
    public BigDecimal getNewAllocation() {
        return newAllocation;
    }

    /**
     * Determines if any allocation has been made.
     *
     * @return {@code true} if an allocation has been made, otherwise {@code false}
     */
    public boolean isAllocated() {
        return !MathRules.isZero(allocation);
    }

    /**
     * Returns the gap amount.
     *
     * @return the gap amount
     */
    public BigDecimal getGapAmount() {
        return claim.getGapAmount();
    }

    /**
     * Returns the claim.
     *
     * @return the claim
     */
    public GapClaimImpl getClaim() {
        return claim;
    }

    /**
     * Returns the insurer.
     *
     * @return the insurer
     */
    public Party getInsurer() {
        return (Party) claim.getInsurer();
    }

    /**
     * Determines if a benefit amount has not yet been received from the insurer.
     *
     * @return {@code true} if a benefit hasn't been received, {@code false} if it has
     */
    public boolean benefitPending() {
        return claim.getGapStatus() == GapClaim.GapStatus.PENDING;
    }

    /**
     * Records the gap amount as being paid.
     * <p>
     * For non-zero benefit amounts, it creates a credit adjustment.
     *
     * @param practice the practice
     * @param location the practice location
     * @param author   the author
     * @return the credit adjustment, or {@code null} if none was created
     */
    public FinancialAct gapPaid(Party practice, Party location, User author) {
        String notes = Messages.format("customer.credit.gap.benefitadjustment", claim.getInsurerId());
        return claim.gapPaid(practice, location, author, notes);
    }

    /**
     * Marks the claim as fully paid.
     */
    public void fullyPaid() {
        claim.fullyPaid();
    }

}
