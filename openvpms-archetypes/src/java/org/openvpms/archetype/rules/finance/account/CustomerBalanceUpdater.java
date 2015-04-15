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

import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static org.openvpms.archetype.rules.customer.CustomerArchetypes.CUSTOMER_PARTICIPATION;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.ACCOUNT_ALLOCATION_RELATIONSHIP;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.BALANCE_PARTICIPATION;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.DEBITS_CREDITS;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountRuleException.ErrorCode.MissingCustomer;


/**
 * Updates the customer account balance.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerBalanceUpdater {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Balance calculator.
     */
    private final BalanceCalculator calculator;


    public CustomerBalanceUpdater(IArchetypeService service) {
        this.service = service;
        calculator = new BalanceCalculator(service);
    }

    /**
     * Adds an act to the customer balance.
     * <p/>
     * Should be invoked prior to the act being saved.
     *
     * @param act the act to add
     * @throws CustomerAccountRuleException if the act is posted but contains
     *                                      no customer
     */
    public void addToBalance(FinancialAct act) {
        ActBean bean = new ActBean(act, service);
        if (hasBalanceParticipation(bean)) {
            if (calculator.isAllocated(act)) {
                // will occur if a non-zero act is changed to a zero act
                bean.removeParticipation(BALANCE_PARTICIPATION);
            }
        } else if (!calculator.isAllocated(act)) {
            addBalanceParticipation(bean);
        }
    }

    /**
     * Updates the balance for the customer associated with the supplied
     * act. Invoked after the act is saved.
     *
     * @param act the act
     * @throws ArchetypeServiceException    for any archetype service error
     * @throws CustomerAccountRuleException if the act is posted but contains
     *                                      no customer
     */
    public void updateBalance(FinancialAct act) {
        if (FinancialActStatus.POSTED.equals(act.getStatus())
            && hasBalanceParticipation(act)) {
            ActBean bean = new ActBean(act, service);
            Party customer = (Party) bean.getParticipant(
                    CUSTOMER_PARTICIPATION);
            if (customer == null) {
                throw new CustomerAccountRuleException(MissingCustomer, act);
            }
            updateBalance(act, customer);
        }
    }

    /**
     * Determines if an act is already in the customer account balance.
     *
     * @param act the act
     * @return <tt>true</tt> if the act has no
     *         <em>act.customerAccountBalance</em> participation and has been
     *         fully allocated
     */
    public boolean inBalance(FinancialAct act) {
        boolean result = hasBalanceParticipation(act);
        if (!result) {
            result = calculator.isAllocated(act);
        }
        return result;
    }

    /**
     * Calculates the balance for a customer.
     *
     * @param act         the act that triggered the update.
     *                    May be <tt>null</tt>
     * @param unallocated the unallocated acts
     * @return a list of the acts that were updated
     */
    public List<FinancialAct> updateBalance(
            FinancialAct act, Iterator<FinancialAct> unallocated) {
        List<BalanceAct> debits = new ArrayList<BalanceAct>();
        List<BalanceAct> credits = new ArrayList<BalanceAct>();

        if (act != null) {
            if (act.isCredit()) {
                credits.add(new BalanceAct(act));
            } else {
                debits.add(new BalanceAct(act));
            }
        }
        while (unallocated.hasNext()) {
            FinancialAct a = unallocated.next();
            if (a.isCredit()) {
                credits.add(new BalanceAct(a));
            } else {
                debits.add(new BalanceAct(a));
            }
        }
        List<FinancialAct> modified = new ArrayList<FinancialAct>();
        for (BalanceAct credit : credits) {
            for (ListIterator<BalanceAct> iter = debits.listIterator();
                 iter.hasNext(); ) {
                BalanceAct debit = iter.next();
                allocate(credit, debit);
                if (debit.isAllocated()) {
                    iter.remove();
                }
                if (debit.isDirty()) {
                    modified.add(debit.getAct());
                }
            }
            if (credit.isDirty()) {
                modified.add(credit.getAct());
            }
        }
        if (!modified.isEmpty()) {
            // save all updates in the one transaction
            service.save(modified);
        }
        return modified;
    }

    /**
     * Calculates the balance for the supplied customer.
     *
     * @param act      the act that triggered the update.
     * @param customer the customer
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void updateBalance(FinancialAct act, Party customer) {
        Iterator<FinancialAct> results = getUnallocatedActs(customer, act);
        updateBalance(act, results);
    }

    /**
     * Adds an <em>participation.customerAccountBalance</em> to an act.
     *
     * @param act the act
     */
    private void addBalanceParticipation(ActBean act) {
        IMObjectReference customer
                = act.getParticipantRef(CUSTOMER_PARTICIPATION);
        if (customer == null) {
            throw new CustomerAccountRuleException(MissingCustomer,
                                                   act.getAct());
        }
        act.addParticipation(BALANCE_PARTICIPATION, customer);
    }

    /**
     * Determines if an act has an <em>participation.customerAccountBalance<em>.
     *
     * @param act the act
     * @return <tt>true</tt> if the participation is present
     */
    private boolean hasBalanceParticipation(FinancialAct act) {
        ActBean bean = new ActBean(act, service);
        return hasBalanceParticipation(bean);
    }

    /**
     * Determines if an act has an <em>participation.customerAccountBalance<em>.
     *
     * @param act the act
     * @return <tt>true</tt> if the participation is present
     */
    private boolean hasBalanceParticipation(ActBean act) {
        return act.getParticipantRef(BALANCE_PARTICIPATION) != null;
    }

    /**
     * Returns unallocated acts for a customer.
     *
     * @param customer the customer
     * @param exclude  the act to exclude. May be <tt>null</tt>
     * @return unallocated acts for the customer
     * @throws ArchetypeServiceException for any archetype service error
     */
    private Iterator<FinancialAct> getUnallocatedActs(Party customer,
                                                      Act exclude) {
        ArchetypeQuery query = CustomerAccountQueryFactory.createUnallocatedQuery(
                customer, DEBITS_CREDITS, exclude);
        return new IMObjectQueryIterator<FinancialAct>(service, query);
    }

    /**
     * Allocates an amount from a credit to a debit.
     *
     * @param credit the credit act
     * @param debit  the debit act
     */
    private void allocate(BalanceAct credit, BalanceAct debit) {
        BigDecimal creditToAlloc = credit.getAllocatable();
        if (creditToAlloc.compareTo(BigDecimal.ZERO) > 0) {
            // have money to allocate
            BigDecimal debitToAlloc = debit.getAllocatable();
            if (creditToAlloc.compareTo(debitToAlloc) <= 0) {
                // can allocate all the credit
                debit.addAllocated(creditToAlloc);
                debit.addRelationship(credit, creditToAlloc);
                credit.addAllocated(creditToAlloc);
            } else {
                // can allocate some of the credit
                debit.addAllocated(debitToAlloc);
                debit.addRelationship(credit, debitToAlloc);
                credit.addAllocated(debitToAlloc);
            }
        }
    }

    /**
     * Wrapper for performing operations on an act that affects the customer
     * account balance.
     */
    class BalanceAct {

        /**
         * The act to delegate to.
         */
        private final FinancialAct act;

        /**
         * Determines if the act has been modified.
         */
        private boolean dirty;

        public BalanceAct(FinancialAct act) {
            this.act = act;
        }

        /**
         * Returns the amount of this act yet to be allocated.
         *
         * @return the amount yet to be allocated
         */
        public BigDecimal getAllocatable() {
            return calculator.getAllocatable(act);
        }

        /**
         * Determines if the act has been fully allocated.
         *
         * @return <tt>true</tt> if the act has been full allocated
         */
        public boolean isAllocated() {
            return calculator.isAllocated(act);
        }

        /**
         * Adds to the allocated amount. If the act is fully allocated, the
         * <em>participation.customerAccountBalance</em> participation is
         * removed.
         *
         * @param allocated the allocated amount
         */
        public void addAllocated(BigDecimal allocated) {
            BigDecimal value = act.getAllocatedAmount().add(allocated);
            act.setAllocatedAmount(new Money(value));
            if (isAllocated()) {
                ActBean bean = new ActBean(act, service);
                bean.removeParticipation(BALANCE_PARTICIPATION);
            }
            dirty = true;
        }

        /**
         * Adds an <em>actRelationship.customerAccountAllocation</em>.
         *
         * @param credit    the credit act
         * @param allocated the allocated amount
         */
        public void addRelationship(BalanceAct credit, BigDecimal allocated) {
            ActBean debitBean = new ActBean(act, service);
            ActRelationship relationship = debitBean.addRelationship(
                    ACCOUNT_ALLOCATION_RELATIONSHIP, credit.getAct());
            IMObjectBean relBean = new IMObjectBean(relationship, service);
            relBean.setValue("allocatedAmount", allocated);
        }

        /**
         * Determines if the act is a credit or debit.
         *
         * @return <tt>true</tt> if the act is a credit, <tt>false</tt>
         *         if it is a debit
         */
        public boolean isCredit() {
            return act.isCredit();
        }

        /**
         * Returns the underlying act.
         *
         * @return the underlying act
         */
        public FinancialAct getAct() {
            return act;
        }

        /**
         * Determines if the act has been modified.
         *
         * @return <tt>true</tt> if the act has been modified
         */
        public boolean isDirty() {
            return dirty;
        }

    }

}
