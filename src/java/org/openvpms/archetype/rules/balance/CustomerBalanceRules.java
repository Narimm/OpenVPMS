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

import org.apache.commons.lang.time.DateUtils;
import org.openvpms.archetype.rules.act.ActCalculator;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.act.FinancialActStatus;
import static org.openvpms.archetype.rules.balance.CustomerBalanceRuleException.ErrorCode.MissingCustomer;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.AndConstraint;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.RelationalOp;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
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
     * Bad debt act short name.
     */
    private static final String BAD_DEBT
            = "act.customerAccountBadDebt";

    /**
     * Counter charge act short name.
     */
    private static final String CHARGES_COUNTER
            = "act.customerAccountChargesCounter";

    /**
     * Invoice charge act short name.
     */
    private static final String CHARGES_INVOICE
            = "act.customerAccountChargesInvoice";

    /**
     * Credit charge act short name.
     */
    private static final String CHARGES_CREDIT
            = "act.customerAccountChargesCredit";

    /**
     * Credit adjust act short name.
     */
    private static final String CREDIT_ADJUST
            = "act.customerAccountCreditAdjust";

    /**
     * Payment act short name.
     */
    private static final String PAYMENT
            = "act.customerAccountPayment";

    /**
     * Debit adjust act short name.
     */
    private static final String DEBIT_ADJUST = "act.customerAccountDebitAdjust";

    /**
     * Refund act short name.
     */
    private static final String REFUND = "act.customerAccountRefund";

    /**
     * Initial balance act short name.
     */
    private static final String INITIAL_BALANCE
            = "act.customerAccountInitialBalance";

    /**
     * Short names of the credit and debit acts the affect the balance.
     */
    static final String[] SHORT_NAMES = {
            CHARGES_COUNTER,
            CHARGES_CREDIT,
            CHARGES_INVOICE,
            CREDIT_ADJUST,
            DEBIT_ADJUST,
            PAYMENT,
            REFUND,
            INITIAL_BALANCE,
            BAD_DEBT};

    /**
     * All customer debit act short names.
     */
    static final String[] DEBIT_SHORT_NAMES = {
            CHARGES_COUNTER, CHARGES_INVOICE, DEBIT_ADJUST, REFUND, INITIAL_BALANCE
    };

    /**
     * All customer credit act short names.
     */
    static final String[] CREDIT_SHORT_NAMES = {
            CHARGES_CREDIT, CREDIT_ADJUST, PAYMENT, BAD_DEBT};

    /**
     * The customer account balance participation short name.
     */
    static final String ACCOUNT_BALANCE_SHORTNAME
            = "participation.customerAccountBalance";

    /**
     * The customer account balance act relationship short name.
     */
    static final String ACCOUNT_ALLOCATION_SHORTNAME
            = "actRelationship.customerAccountAllocation";

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
     * Updates the balance for the customer associated with the supplied
     * act. Invoked after the act is saved.
     *
     * @param act the act
     * @throws ArchetypeServiceException    for any archetype service error
     * @throws CustomerBalanceRuleException if the act is posted but contains
     *                                      no customer
     */
    public void updateBalance(FinancialAct act) {
        if (FinancialActStatus.POSTED.equals(act.getStatus())
                && hasBalanceParticipation(act)) {
            ActBean bean = new ActBean(act, service);
            Party customer = (Party) bean.getParticipant(
                    "participation.customer");
            if (customer == null) {
                throw new CustomerBalanceRuleException(MissingCustomer, act);
            }
            updateBalance(act, customer);
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
    public boolean inBalance(FinancialAct act) {
        boolean result = hasBalanceParticipation(act);
        if (!result) {
            ActBean bean = new ActBean(act, service);
            List<ActRelationship> relationships = bean.getRelationships(
                    ACCOUNT_ALLOCATION_SHORTNAME);
            if (!relationships.isEmpty()) {
                result = true;
            } else {
                // check for a zero total.
                Money total = act.getTotal();
                if (total != null && total.compareTo(BigDecimal.ZERO) == 0) {
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * Calculates the outstanding balance for a customer.
     *
     * @param customer the customer
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal getBalance(Party customer) {
        Iterator<FinancialAct> iterator = getUnallocatedActs(customer, null);
        return calculateBalance(iterator);
    }

    /**
     * Calculates the overdue balance for a customer.
     * This is the sum of unallocated amounts in associated debits that have a
     * date less than the specified date less the overdue days.
     * The overdue days are specified in the customer's type node.
     *
     * @param customer the customer
     * @param date     the date
     * @return the overdue balance
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal getOverdueBalance(Party customer, Date date) {
        Date overdue = getOverdueDate(customer, date);

        // query all overdue debit acts
        ArchetypeQuery query = createQuery(customer, DEBIT_SHORT_NAMES,
                                           null);
        query.add(new NodeConstraint("startTime", RelationalOp.LT, overdue));
        Iterator<FinancialAct> iterator
                = new IMObjectQueryIterator<FinancialAct>(service, query);

        BigDecimal amount = calculateBalance(iterator);
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            amount = BigDecimal.ZERO;
        }
        return amount;
    }

    /**
     * Determines if a customer has an overdue balance within the nominated
     * day range past their standard terms.
     *
     * @param customer the customer
     * @param date     the date
     * @param from     the from day range
     * @param to       the to day range. Use <code>&lt;= 0</code> to indicate
     *                 all dates
     * @return <code>true</code> if the customer has an overdue balance within
     *         the day range past their standard terms.
     */
    public boolean hasOverdueBalance(Party customer, Date date, int from,
                                     int to) {
        Date overdue = getOverdueDate(customer, date);
        Date overdueFrom = overdue;
        Date overdueTo = null;
        if (from > 0) {
            overdueFrom = DateRules.getDate(overdueFrom, -from, DateRules.DAYS);
        }
        if (to > 0) {
            overdueTo = DateRules.getDate(overdue, -to, DateRules.DAYS);
        }

        // query all overdue debit acts
        ArchetypeQuery query = createQuery(customer, DEBIT_SHORT_NAMES, null);

        NodeConstraint fromStartTime
                = new NodeConstraint("startTime", RelationalOp.LT, overdueFrom);
        if (overdueTo == null) {
            query.add(fromStartTime);
        } else {
            NodeConstraint toStartTime = new NodeConstraint("startTime",
                                                            RelationalOp.GT,
                                                            overdueTo);
            AndConstraint and = new AndConstraint();
            and.add(fromStartTime);
            and.add(toStartTime);
            query.add(and);
        }
        query.setMaxResults(1);
        Iterator<FinancialAct> iterator
                = new IMObjectQueryIterator<FinancialAct>(service, query);
        return iterator.hasNext();
    }

    /**
     * Returns the overdue date relative to the specified date, for a
     * customer.
     *
     * @param customer the customer
     * @param date     the date
     * @return the overdue date
     */
    public Date getOverdueDate(Party customer, Date date) {
        IMObjectBean bean = new IMObjectBean(customer, service);
        List<Lookup> types = bean.getValues("type", Lookup.class);
        Date overdue;
        if (!types.isEmpty()) {
            IMObjectBean type = new IMObjectBean(types.get(0), service);
            date = DateUtils.truncate(date, Calendar.DATE); // strip any time
            int days = type.getInt("paymentTerms");
            String units = type.getString("paymentUom");
            overdue = DateRules.getDate(date, -days, units);
        } else {
            overdue = date;
        }
        return overdue;
    }

    /**
     * Calculates the sum of all unallocated credits for a customer.
     *
     * @param customer the customer
     * @return the credit amount
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal getCreditBalance(Party customer) {
        ArchetypeQuery query = createQuery(customer, CREDIT_SHORT_NAMES, null);
        Iterator<FinancialAct> iterator
                = new IMObjectQueryIterator<FinancialAct>(service, query);
        return calculateBalance(iterator);
    }

    /**
     * Calculates the sum of all unbilled charge acts for a customer.
     *
     * @param customer the customer
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal getUnbilledAmount(Party customer) {
        String[] shortNames = {CHARGES_INVOICE, CHARGES_COUNTER,
                               CHARGES_CREDIT};
        ArchetypeQuery query = new ArchetypeQuery(shortNames, true, true);
        query.add(new NodeConstraint("status", RelationalOp.NE,
                                     ActStatus.POSTED));
        CollectionNodeConstraint constraint = new CollectionNodeConstraint(
                "customer", "participation.customer", true, true);
        constraint.add(new ObjectRefNodeConstraint(
                "entity", customer.getObjectReference()));
        query.add(constraint);

        Iterator<Act> iterator = new IMObjectQueryIterator<Act>(service, query);
        ActCalculator calculator = new ActCalculator(service);
        return calculator.sum(iterator, "amount");
    }

    /**
     * Calculates the balance for the supplied customer.
     *
     * @param act the act that triggered the update
     * @param customer the customer
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void updateBalance(FinancialAct act, Party customer) {
        Iterator<FinancialAct> results = getUnallocatedActs(customer, act);
        List<BalanceAct> debits = new ArrayList<BalanceAct>();
        List<BalanceAct> credits = new ArrayList<BalanceAct>();

        if (act.isCredit()) {
            credits.add(new BalanceAct(act));
        } else {
            debits.add(new BalanceAct(act));
        }
        while (results.hasNext()) {
            FinancialAct a = results.next();
            if (a.isCredit()) {
                credits.add(new BalanceAct(a));
            } else {
                debits.add(new BalanceAct(a));
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
     * Calculates the oustanding balance.
     *
     * @param iterator an iterator over the collection
     * @return the outstanding balance
     */
    private BigDecimal calculateBalance(Iterator<FinancialAct> iterator) {
        BigDecimal total = BigDecimal.ZERO;
        ActCalculator calculator = new ActCalculator(service);
        while (iterator.hasNext()) {
            BalanceAct act = new BalanceAct(iterator.next());
            BigDecimal unallocated = act.getAllocatable();
            total = calculator.addAmount(total, unallocated, act.isCredit());
        }
        return total;
    }

    /**
     * Returns unallocated acts for a customer.
     *
     * @param customer the customer
     * @param exclude the act to exclude. May be <tt>null</tt>
     * @return unallocated acts for the customer
     * @throws ArchetypeServiceException for any archetype service error
     */
    private Iterator<FinancialAct> getUnallocatedActs(Party customer,
                                                      Act exclude) {
        ArchetypeQuery query = createQuery(customer, exclude);

        return new IMObjectQueryIterator<FinancialAct>(service, query);
    }

    /**
     * Creates a query for unallocated acts for the specified customer.
     *
     * @param customer the customer
     * @param exclude the act to exclude. May be <tt>null</tt
     * @return a new query
     */
    private ArchetypeQuery createQuery(Party customer, Act exclude) {
        return createQuery(customer, SHORT_NAMES, exclude);
    }

    /**
     * Creates a query for unallocated acts for the specified customer.
     *
     * @param customer   the customer
     * @param shortNames the act short names
     * @param exclude the act to exclude. May be <tt>null</tt>
     * @return a new query
     */
    private ArchetypeQuery createQuery(Party customer, String[] shortNames,
                                       Act exclude) {
        ArchetypeQuery query = new ArchetypeQuery(shortNames, true, true);
        CollectionNodeConstraint constraint = new CollectionNodeConstraint(
                "accountBalance", ACCOUNT_BALANCE_SHORTNAME, true, true);
        constraint.add(new ObjectRefNodeConstraint(
                "entity", customer.getObjectReference()));
        if (exclude != null) {
            constraint.add(new ObjectRefNodeConstraint(
                    "act", RelationalOp.NE, exclude.getObjectReference()));
        }
        query.add(constraint);
        query.add(new NodeSortConstraint("startTime", false));
        return query;
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
            if (amount == null) {
                amount = BigDecimal.ZERO;
            }
            BigDecimal allocated = act.getAllocatedAmount();
            if (allocated == null) {
                allocated = BigDecimal.ZERO;
            }
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
         * @param credit    the credit act
         * @param allocated the allocated amount
         */
        public void addRelationship(BalanceAct credit, BigDecimal allocated) {
            ActBean bean = new ActBean(act, service);
            ActRelationship relationship = bean.addRelationship(
                    ACCOUNT_ALLOCATION_SHORTNAME,
                    credit.getAct());
            IMObjectBean relBean = new IMObjectBean(relationship, service);
            relBean.setValue("allocatedAmount", allocated);
        }

        /**
         * Determines if the act is a credit or debit.
         *
         * @return <code>true</code> if the act is a credit, <code>false</code>
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
         * @return <code>true</code> if the act has been modified
         */
        public boolean isDirty() {
            return dirty;
        }

    }

}
