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

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.BalanceCalculator;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.account.CustomerAccountQueryFactory;
import org.openvpms.archetype.rules.finance.account.CustomerBalanceUpdater;
import org.openvpms.archetype.rules.insurance.InsuranceRules;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.QueryIterator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Allocates a credit act to one or more debit acts.
 *
 * @author Tim Anderson
 */
public class CreditActAllocator {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The insurance rules.
     */
    private final InsuranceRules insuranceRules;

    /**
     * The customer balance calculator.
     */
    private final BalanceCalculator calculator;

    /**
     * The customer balance updater.
     */
    private final CustomerBalanceUpdater updater;

    /**
     * Constructs a {@link CreditActAllocator}.
     *
     * @param service        the archetype service
     * @param insuranceRules the insurance rules
     */
    public CreditActAllocator(IArchetypeService service, InsuranceRules insuranceRules) {
        this.service = service;
        this.insuranceRules = insuranceRules;
        calculator = new BalanceCalculator(service);
        updater = new CustomerBalanceUpdater(service);
    }

    /**
     * Allocates a credit to unallocated debits.
     *
     * @param credit the credit
     * @return the allocation
     */
    public CreditAllocation allocate(FinancialAct credit) {
        return allocate(credit, Collections.emptyList(), false);
    }

    /**
     * Allocates a credit to unallocated debits.
     * <br/>
     * This will allocate to invoices associated with gap claims last, unless the invoices are provided in the
     * {@code debits} list.
     * <br/>
     * If the customer has unallocated debits other than those provided, these will be allocated to after the
     * supplied debits are fully allocated.
     *
     * @param credit            the credit
     * @param debits            debit acts to allocate first. These may be associated with a gap claim. May be empty
     * @param partialAllocation if {@code true}, perform partial allocation to non-gap claim acts
     * @return the results of the allocation
     */
    public CreditAllocation allocate(FinancialAct credit, List<FinancialAct> debits, boolean partialAllocation) {
        CreditAllocation result;
        if (!credit.isCredit()) {
            throw new IllegalArgumentException("Argument 'credit' must be a credit");
        }
        if (!debits.isEmpty()) {
            checkDebits(debits);
        }
        if (ActStatus.POSTED.equals(credit.getStatus()) && !calculator.isAllocated(credit)) {
            BigDecimal amountToAllocate = calculator.getAllocatable(credit);
            IMObjectBean bean = service.getBean(credit);
            Reference customer = bean.getTargetRef("customer");
            if (customer == null) {
                throw new IllegalStateException("Failed to determine customer for " + credit.getArchetype());
            }
            ArchetypeQuery query = CustomerAccountQueryFactory.createUnallocatedQuery(
                    customer, CustomerAccountArchetypes.DEBITS, null);
            List<FinancialAct> allUnallocated = new ArrayList<>(debits);
            query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
            QueryIterator<FinancialAct> saved = new IMObjectQueryIterator<>(service, query);
            while (saved.hasNext()) {
                FinancialAct act = saved.next();
                if (!allUnallocated.contains(act)) {
                    allUnallocated.add(act);
                }
            }

            if (allUnallocated.isEmpty()) {
                result = new CreditAllocation(credit, service);
            } else {
                Map<FinancialAct, AllocationBlock> blocked = new LinkedHashMap<>();
                // debits that are blocked from automatic allocation (e.g. associated with an outstanding gap claim)

                List<FinancialAct> toAllocate = getAllocatable(allUnallocated, debits, blocked);
                if (blocked.isEmpty() && debits.isEmpty()) {
                    // no intervention required to perform allocation
                    result = new CreditAllocation(credit, toAllocate, service);
                } else {
                    BigDecimal unallocated = BigDecimal.ZERO;
                    for (FinancialAct act : toAllocate) {
                        BigDecimal allocatable = calculator.getAllocatable(act);
                        unallocated = unallocated.add(allocatable);
                    }
                    if (unallocated.compareTo(amountToAllocate) >= 0
                        || (partialAllocation && unallocated.compareTo(BigDecimal.ZERO) > 0)) {
                        updater.addToBalance(credit);
                        for (FinancialAct act : toAllocate) {
                            updater.addToBalance(act);
                        }
                        List<FinancialAct> updated = updater.updateBalance(credit, toAllocate.iterator(), false);
                        result = new CreditAllocation(credit, toAllocate, blocked, updated, service);
                    } else {
                        List<FinancialAct> unallocatedDebits = new ArrayList<>(toAllocate);
                        unallocatedDebits.addAll(blocked.keySet());
                        result = new CreditAllocation(credit, unallocatedDebits, blocked, service);
                    }
                }
            }
        } else {
            // no acts to allocate against
            result = new CreditAllocation(credit, service);
        }
        return result;
    }

    /**
     * Allocates a credit act to a debit act.
     *
     * @param credit the credit
     * @param debit  the debit
     * @return the list of updated acts. These must be saved in order to update the customer balance
     */
    public List<FinancialAct> allocate(FinancialAct credit, FinancialAct debit) {
        return allocate(credit, Collections.singletonList(debit));
    }

    /**
     * Allocates a credit act to one or more debits.
     *
     * @param credit the credit
     * @param debits the debits
     * @return the list of updated acts. These must be saved in order to update the customer balance
     */
    public List<FinancialAct> allocate(FinancialAct credit, List<FinancialAct> debits) {
        List<FinancialAct> result = Collections.emptyList();
        if (!credit.isCredit()) {
            throw new IllegalArgumentException("Argument 'credit' must be a credit");
        }
        if (!ActStatus.POSTED.equals(credit.getStatus())) {
            throw new IllegalArgumentException("Argument 'credit' must be POSTED");
        }
        checkDebits(debits);
        if (ActStatus.POSTED.equals(credit.getStatus()) && !calculator.isAllocated(credit)) {
            IMObjectBean bean = service.getBean(credit);
            Party customer = bean.getTarget("customer", Party.class);
            if (customer == null) {
                throw new IllegalStateException("Failed to determine customer for " + credit.getArchetype());
            }
            updater.addToBalance(credit);
            for (FinancialAct debit : debits) {
                updater.addToBalance(debit);
            }
            result = updater.updateBalance(credit, debits.iterator(), false);
        }
        return result;
    }

    /**
     * Determines the acts that can be allocated to.
     *
     * @param unallocated all unallocated acts
     * @param debits      unallocated debit acts
     * @param blocked     collects debits that shouldn't be automatically allocated to
     * @return debit acts that can be automatically allocated
     */
    protected List<FinancialAct> getAllocatable(List<FinancialAct> unallocated, List<FinancialAct> debits,
                                                Map<FinancialAct, AllocationBlock> blocked) {
        List<FinancialAct> toAllocate = new ArrayList<>();
        for (FinancialAct act : unallocated) {
            if (!debits.contains(act)) {
                AllocationBlock block = getAllocationBlock(act);
                if (block != null) {
                    blocked.put(act, block);
                } else {
                    toAllocate.add(act);
                }
            } else {
                toAllocate.add(act);
            }
        }
        return toAllocate;
    }

    /**
     * Determines if there is any block to allocating a debit.
     * <p/>
     * This implementation returns an {@link GapClaimAllocationBlock} if the debit is an invoice associated with
     * a gap claim.
     *
     * @param debit the debit
     * @return the allocation block, indicating why the debit cannot be automatically allocated, or
     * {@code null} if the debit can be automatically allocated
     */
    protected AllocationBlock getAllocationBlock(FinancialAct debit) {
        AllocationBlock result = null;
        if (debit.isA(CustomerAccountArchetypes.INVOICE)) {
            List<FinancialAct> claims = new ArrayList<>();
            for (org.openvpms.component.model.act.FinancialAct claim : insuranceRules.getCurrentGapClaims(debit)) {
                claims.add((FinancialAct) claim);
            }
            if (!claims.isEmpty()) {
                result = new GapClaimAllocationBlock(claims);
            }
        }
        return result;
    }

    /**
     * Verifies that the debits are actually POSTED debits.
     *
     * @param debits the debits to check
     */
    private void checkDebits(List<FinancialAct> debits) {
        for (FinancialAct debit : debits) {
            if (debit.isCredit()) {
                throw new IllegalArgumentException("Argument 'debits' must contain a list of debit acts");
            }
            if (!ActStatus.POSTED.equals(debit.getStatus())) {
                throw new IllegalArgumentException("Argument 'debits' must contain a list of POSTED debit acts");
            }
        }
    }

}
