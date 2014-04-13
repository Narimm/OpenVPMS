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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.finance.account;

import org.openvpms.archetype.rules.act.ActCalculator;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.openvpms.archetype.rules.customer.CustomerArchetypes.CUSTOMER_PARTICIPATION;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.ACCOUNT_ACTS;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.CLOSING_BALANCE;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.OPENING_BALANCE;


/**
 * Generates account balances for a customer.
 * <p/>
 * This replaces any existing balance participations and account allocations.
 * Opening and closing balances are updated if the
 */
public class CustomerBalanceGenerator {

    /**
     * The customer.
     */
    private Party customer;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Iterator over the customer's financial acts.
     */
    private final Iterator<FinancialAct> iterator;

    /**
     * The unallocated acts.
     */
    private final List<FinancialAct> unallocated
            = new ArrayList<FinancialAct>();

    /**
     * The modified acts, with associated versions, to determine if
     * they need to be saved.
     */
    private final Map<FinancialAct, Long> modified
            = new HashMap<FinancialAct, Long>();

    /**
     * Total no. of acts
     */
    private int acts;

    /**
     * The balance updater.
     */
    private final CustomerBalanceUpdater updater;


    /**
     * Constructs a new <tt>CustomerBalanceGenerator</tt>.
     *
     * @param customer the customer
     * @param service  the archetype service
     */
    public CustomerBalanceGenerator(Party customer, IArchetypeService service) {
        this.customer = customer;
        this.service = service;
        updater = new CustomerBalanceUpdater(service);
        iterator = getActs(customer);
    }

    /**
     * Generate the balance for the customer.
     *
     * @return the final balance
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal generate() {
        FinancialAct act;
        ActCalculator calculator = new ActCalculator(service);
        BigDecimal runningTotal = BigDecimal.ZERO;
        while ((act = getNext()) != null) {
            if (TypeHelper.isA(act, OPENING_BALANCE, CLOSING_BALANCE)) {
                BigDecimal total = calculator.getTotal(act);
                boolean credit = TypeHelper.isA(act, CLOSING_BALANCE);
                BigDecimal expectedTotal = runningTotal;
                boolean expectedCredit = credit;
                if (runningTotal.signum() == -1) {
                    expectedTotal = runningTotal.negate();
                    expectedCredit = !credit;
                }
                if (!MathRules.equals(total.abs(), expectedTotal)
                    || act.isCredit() != expectedCredit) {
                    act.setTotal(new Money(expectedTotal));
                    act.setCredit(expectedCredit);
                    modified(act);
                    changed(act, total, runningTotal);
                }
            } else {
                addToBalance(act);
                if (ActStatus.POSTED.equals(act.getStatus())) {
                    runningTotal = calculator.addAmount(runningTotal,
                                                        act.getTotal(),
                                                        act.isCredit());
                }
            }
        }
        save();
        return runningTotal;
    }

    /**
     * Returns the customer.
     *
     * @return the customer
     */
    protected Party getCustomer() {
        return customer;
    }

    /**
     * Invoked when an act is changed.
     * <p/>
     * This method is a no-op.
     *
     * @param act       the act
     * @param fromTotal the original total
     * @param toTotal   the new total
     */
    protected void changed(FinancialAct act, BigDecimal fromTotal,
                           BigDecimal toTotal) {
    }

    /**
     * Returns the no. of acts that were changed.
     *
     * @return the no. of acts that were changed
     */
    public int getModified() {
        return modified.size();
    }

    /**
     * Returns the no. of acts that were processed.
     *
     * @return the no. of acts that were processed
     */
    public int getProcessed() {
        return acts;
    }

    /**
     * Adds an act to the balance.
     *
     * @param act the act to add
     */
    private void addToBalance(FinancialAct act) {
        Money allocated = act.getAllocatedAmount();
        if (allocated == null || allocated.compareTo(Money.ZERO) != 0) {
            act.setAllocatedAmount(Money.ZERO);
            modified(act);
        }
        if (act.getTotal() == null) {
            act.setTotal(Money.ZERO);
            modified(act);
        }
        ActBean bean = new ActBean(act, service);
        if (bean.removeParticipation(
                CustomerAccountArchetypes.BALANCE_PARTICIPATION) != null) {
            modified(act);
        }
        for (ActRelationship relationship : bean.getRelationships(
                CustomerAccountArchetypes.ACCOUNT_ALLOCATION_RELATIONSHIP)) {
            bean.removeRelationship(relationship);
            modified(act);
        }
        if (!updater.inBalance(act)) { // false for 0 totals
            updater.addToBalance(act);
            modified(act);
        }
    }

    /**
     * Returns the next available act.
     *
     * @return the next available act, or <tt>null</tt>
     */
    private FinancialAct getNext() {
        if (iterator.hasNext()) {
            ++acts;
            return iterator.next();
        }
        return null;
    }

    /**
     * Marks an act as being modified.
     * <p/>
     * If it is a debit/credit act, adds it to the list of unallocated acts.
     *
     * @param act the act
     */
    private void modified(FinancialAct act) {
        if (modified.put(act, act.getVersion()) == null) {
            if (TypeHelper.isA(act, CustomerAccountArchetypes.DEBITS_CREDITS)) {
                unallocated.add(act);
            }
        }
    }

    /**
     * Saves unallocated acts.
     */
    @SuppressWarnings("unchecked")
    private void save() {
        Set<IMObject> unsaved = new HashSet<IMObject>(modified.keySet());
        if (!unallocated.isEmpty()) {
            // Update the customer balance. This will save any acts
            // that it changes. Need to check versions to determine if
            // the acts that this method has changed also need to be saved
            List<FinancialAct> updated = updater.updateBalance(
                    null, unallocated.iterator());
            unsaved.removeAll(updated);
        }
        if (!unsaved.isEmpty()) {
            service.save(unsaved);
        }
    }

    /**
     * Returns an iterator over the debit/credit acts for a customer.
     *
     * @param customer the customer
     * @return an iterator of debit/credit acts
     */
    private Iterator<FinancialAct> getActs(Party customer) {
        String[] shortNames = ACCOUNT_ACTS;
        ArchetypeQuery query = new ArchetypeQuery(shortNames, true, true);
        CollectionNodeConstraint constraint = new CollectionNodeConstraint(
                "customer", CUSTOMER_PARTICIPATION, true, true);
        constraint.add(new ObjectRefNodeConstraint(
                "entity", customer.getObjectReference()));
        query.add(constraint);
        query.add(new NodeSortConstraint("startTime", true));
        query.add(new NodeSortConstraint("id", true));
        query.setMaxResults(1000);
        return new IMObjectQueryIterator<FinancialAct>(service, query);
    }

}
