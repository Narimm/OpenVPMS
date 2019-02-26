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
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Credit allocation state.
 *
 * @author Tim Anderson
 * @see CreditActAllocator
 */
public class CreditAllocation {

    /**
     * The credit to allocate.
     */
    private final FinancialAct credit;

    /**
     * Debits with with allocation blocks that require the user to make a decision about allocation order.
     */
    private final Map<FinancialAct, AllocationBlock> blocked;

    /**
     * Acts that have been modified as part of the allocation.
     */
    private final List<FinancialAct> modified;

    /**
     * Acts that are unallocated.
     */
    private final List<FinancialAct> debits;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Credit display name.
     */
    private String displayName;

    /**
     * Constructs a {@link CreditAllocation}.
     * <p>
     * Use this when there are no acts to allocate against.
     *
     * @param credit  the credit acts
     * @param service the archetype service
     */
    public CreditAllocation(FinancialAct credit, IArchetypeService service) {
        this(credit, Collections.emptyList(), service);
    }

    /**
     * Constructs a {@link CreditAllocation}.
     * <p>
     * Use this when no intervention is required to perform allocation.
     *
     * @param credit  the credit act
     * @param debits  the debit acts
     * @param service the archetype service
     */
    public CreditAllocation(FinancialAct credit, List<FinancialAct> debits, IArchetypeService service) {
        this(credit, debits, Collections.emptyMap(), service);
    }

    /**
     * Constructs a {@link CreditAllocation}.
     * <p>
     * Use this when manual intervention is required to perform allocation.
     *
     * @param credit  the credit act
     * @param debits  the debit acts
     * @param blocked debits that are blocked from automatic allocation
     * @param service the archetype service
     */
    public CreditAllocation(FinancialAct credit, List<FinancialAct> debits, Map<FinancialAct, AllocationBlock> blocked,
                            IArchetypeService service) {
        this(credit, debits, blocked, Collections.emptyList(), service);
    }

    /**
     * Constructs a {@link CreditAllocation}.
     * <p>
     * Use this when the credit has been allocated against acts except those invoices with allocation blocks.
     *
     * @param credit  the credit act
     * @param debits  the debit acts
     * @param blocked debits that are blocked from automatic allocation
     * @param service the archetype service
     */
    public CreditAllocation(FinancialAct credit, List<FinancialAct> debits, Map<FinancialAct, AllocationBlock> blocked,
                            List<FinancialAct> modified, IArchetypeService service) {
        this.credit = credit;
        this.debits = debits;
        this.blocked = blocked;
        this.modified = modified;
        this.service = service;
    }

    /**
     * Returns the credit display name.
     *
     * @return the display name
     */
    public String getDisplayName() {
        if (displayName == null) {
            displayName = DescriptorHelper.getDisplayName(credit, service);
        }
        return displayName;
    }

    /**
     * Returns the credit act.
     *
     * @return the act
     */
    public FinancialAct getCredit() {
        return credit;
    }

    /**
     * Returns the debit acts.
     *
     * @return the acts
     */
    public List<FinancialAct> getDebits() {
        return debits;
    }

    /**
     * Returns debits that have allocation blocks that require the user to make a decision about the allocation
     * order.
     *
     * @return the debits that have allocation bocks
     */
    public Map<FinancialAct, AllocationBlock> getBlocked() {
        return blocked;
    }

    /**
     * Returns any acts that have been modified and need to be saved.
     *
     * @return the acts
     */
    public List<FinancialAct> getModified() {
        return modified;
    }

    /**
     * Determines if the user must manually select the allocation order.
     *
     * @return {@code true} if the user must manually select the allocation order
     */
    public boolean overrideDefaultAllocation() {
        return modified.isEmpty() && !blocked.isEmpty();
    }

    /**
     * Determines if the credit was partially/fully allocated.
     *
     * @return {@code true} if the credit was partially/fully allocated, or {@code false} if it couldn't be allocated
     */
    public boolean isModified() {
        return !modified.isEmpty();
    }
}
