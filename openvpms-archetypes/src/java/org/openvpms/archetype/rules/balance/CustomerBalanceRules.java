/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.balance;

import org.openvpms.archetype.rules.act.FinancialActStatus;
import static org.openvpms.archetype.rules.balance.CustomerBalanceRuleException.ErrorCode.MissingCustomer;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.QueryIterator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;


/**
 * Customer account balance rule.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerBalanceRules {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Short names of the credit and debit acts the affect the balance.
     */
    private static final String[] SHORT_NAMES = {
            "act.customerAccountChargesCounter",
            "act.customerAccountChargesCredit",
            "act.customerAccountChargesInvoice",
            "act.customerAccountCreditAdjust",
            "act.customerAccountDebitAdjust",
            "act.customerAccountPayment",
            "act.customerAccountRefund",
            "act.customerAccountInitialBalance",
            "act.customerAccountBadDebt"};

    /**
     * The customer account balance short name.
     */
    private static final String ACCOUNT_BALANCE_SHORTNAME
            = "participation.customerAccountBalance";


    /**
     * Creates a new <code>CustomerBalanceRules</code>.
     */
    public CustomerBalanceRules() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Creates a new <code>CustomerBalanceRules</code>.
     *
     * @param service the archetype service
     */
    public CustomerBalanceRules(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Adds an act to the customer balance. Invoked prior to the act being
     * saved.
     *
     * @param act the act to add
     * @throws CustomerBalanceRuleException if the act is posted but contains
     *                                      no customer
     */
    public void addToBalance(FinancialAct act) {
        if (FinancialActStatus.POSTED.equals(act.getStatus())
                && !inBalance(act)) {
            ActBean bean = new ActBean(act, service);
            Party customer = (Party) bean.getParticipant(
                    "participation.customer");
            if (customer == null) {
                throw new CustomerBalanceRuleException(MissingCustomer, act);
            }
            bean.addParticipation(ACCOUNT_BALANCE_SHORTNAME, customer);
        }
    }

    /**
     * Calculates the balance for the customer associated with the supplied
     * act. Invoked after the act is saved.
     *
     * @param act the act
     * @throws ArchetypeServiceException    for any archetype service error
     * @throws CustomerBalanceRuleException if the act is posted but contains
     *                                      no customer
     */
    public void calculateBalance(FinancialAct act) {
        if (FinancialActStatus.POSTED.equals(act.getStatus())
                && hasBalanceParticipation(act)) {
            ActBean bean = new ActBean(act, service);
            Party customer = (Party) bean.getParticipant(
                    "participation.customer");
            if (customer == null) {
                throw new CustomerBalanceRuleException(MissingCustomer, act);
            }
            calculateBalance(customer);
        }
    }

    /**
     * Calculates the balance for the supplied customer.
     *
     * @param customer the customer
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void calculateBalance(Party customer) {
        ArchetypeQuery query = new ArchetypeQuery(SHORT_NAMES, true, true);
        CollectionNodeConstraint constraint = new CollectionNodeConstraint(
                "accountBalance", ACCOUNT_BALANCE_SHORTNAME, true, true);
        constraint.add(new ObjectRefNodeConstraint(
                "entity", customer.getObjectReference()));
        query.add(constraint);
        query.add(new NodeSortConstraint("startTime", false));
        QueryIterator<FinancialAct> results
                = new IMObjectQueryIterator<FinancialAct>(service, query);
        List<BalanceAct> debits = new ArrayList<BalanceAct>();
        List<BalanceAct> credits = new ArrayList<BalanceAct>();
        while (results.hasNext()) {
            FinancialAct act = results.next();
            if (act.isCredit()) {
                credits.add(new BalanceAct(act));
            } else {
                debits.add(new BalanceAct(act));
            }
        }
        List<IMObject> modified = new ArrayList<IMObject>();
        for (BalanceAct credit : credits) {
            for (ListIterator<BalanceAct> iter = debits.listIterator();
                 iter.hasNext();) {
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
            // save all modified acts in the one transaction
            service.save(modified);
        }
    }

    /**
     * Determines if an act is already in the customer account balance.
     *
     * @param act the act
     * @return <code>true</code> if the act has no
     *         <em>act.customerAccountBalance</em> participation and has been
     *         fully allocated
     */
    private boolean inBalance(FinancialAct act) {
        boolean result = hasBalanceParticipation(act);
        if (!result) {
            BigDecimal total = act.getTotal();
            BigDecimal allocated = act.getAllocatedAmount();
            if (total != null && allocated != null) {
                result = (total.compareTo(allocated) == 0);
            }
        }
        return result;
    }

    /**
     * Determines if an act has an <em>participation.customerAccountBalance<em>.
     *
     * @param act the act
     * @return <code>true</code> if the participation is present
     */
    private boolean hasBalanceParticipation(FinancialAct act) {
        ActBean bean = new ActBean(act, service);
        return bean.getParticipantRef(ACCOUNT_BALANCE_SHORTNAME) != null;
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
         * Determines if the act has been modified to.
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
            BigDecimal amount = act.getTotal();
            BigDecimal allocated = act.getAllocatedAmount();
            return amount.subtract(allocated);
        }

        /**
         * Determines if the act has been fully allocated.
         *
         * @return <code>true</code> if the act has been full allocated
         */
        public boolean isAllocated() {
            return getAllocatable().compareTo(BigDecimal.ZERO) <= 0;
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
                bean.removeParticipation(ACCOUNT_BALANCE_SHORTNAME);
            }
            dirty = true;
        }

        /**
         * Adds an <em>actRelationship.customerAccountAllocation</em>.
         *
         * @param credit the credit act
         * @param allocated the allocated amount
         */
        public void addRelationship(BalanceAct credit, BigDecimal allocated)  {
            ActBean bean = new ActBean(act, service);
            ActRelationship relationship = bean.addRelationship(
                    "actRelationship.customerAccountAllocation",
                    credit.getAct());
            IMObjectBean relBean = new IMObjectBean(relationship, service);
            relBean.setValue("allocatedAmount", allocated);
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
         * @return <code>true</code> if the act has been modified
         */
        public boolean isDirty() {
            return dirty;
        }

    }

}
