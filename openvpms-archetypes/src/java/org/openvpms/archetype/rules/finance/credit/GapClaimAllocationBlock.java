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

package org.openvpms.archetype.rules.finance.credit;

import org.openvpms.component.business.domain.im.act.FinancialAct;

import java.util.List;

/**
 * An {@link AllocationBlock} indicating a debit is being claimed in on or more gap claims.
 *
 * @author Tim Anderson
 */
public class GapClaimAllocationBlock implements AllocationBlock {

    /**
     * The gap claims.
     */
    private final List<FinancialAct> gapClaims;

    /**
     * Constructs a {@link GapClaimAllocationBlock}.
     *
     * @param gapClaims the gap claims the debit is claimed by
     */
    public GapClaimAllocationBlock(List<FinancialAct> gapClaims) {
        this.gapClaims = gapClaims;
    }

    /**
     * Returns the gap claims that the debit is claimed by.
     *
     * @return the gap claims
     */
    public List<FinancialAct> getGapClaims() {
        return gapClaims;
    }
}
