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

package org.openvpms.archetype.rules.finance.account;

import org.openvpms.archetype.rules.act.ActCalculator;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.act.FinancialActStatus;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountActTypes.*;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountRuleException.ErrorCode.MissingCustomer;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
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
import org.openvpms.component.business.service.archetype.helper.IMObjectCopier;
import org.openvpms.component.system.common.query.AndConstraint;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.component.system.common.query.RelationalOp;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


/**
 * Customer account rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z` $
 */
public class CustomerAccountRules {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Balance calculator.
     */
    private final BalanceCalculator calculator;


    /**
     * Creates a new <tt>CustomerBalanceRules</tt>.
     */
    public CustomerAccountRules() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Creates a new <tt>CustomerBalanceRules</tt>.
     *
     * @param service the archetype service
     */
    public CustomerAccountRules(IArchetypeService service) {
        this.service = service;
        calculator = new BalanceCalculator(service);
    }

    /**
     * Adds an act to the customer balance. Invoked prior to the act being
     * saved.
     *
     * @param act the act to add
     * @throws CustomerAccountRuleException if the act is posted but contains
     *                                      no customer
     */
    public void addToBalance(FinancialAct act) {
        if (FinancialActStatus.POSTED.equals(act.getStatus())
                && !inBalance(act)) {
            ActBean bean = new ActBean(act, service);
            Party customer = (Party) bean.getParticipant(
                    "participation.customer");
            if (customer == null) {
                throw new CustomerAccountRuleException(MissingCustomer, act);
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
     * @throws CustomerAccountRuleException if the act is posted but contains
     *                                      no customer
     */
    public void updateBalance(FinancialAct act) {
        if (FinancialActStatus.POSTED.equals(act.getStatus())
                && hasBalanceParticipation(act)) {
            ActBean bean = new ActBean(act, service);
            Party customer = (Party) bean.getParticipant(
                    "participation.customer");
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
        return calculator.getBalance(customer);
    }

    /**
     * Calculates a new balance for a customer from the current outstanding
     * balance and a running total.
     * If the new balance is:
     * <ul>
     * <li>&lt; 0 returns 0.00 for payments, or -balance for refunds</li>
     * <li>&gt; 0 returns 0.00 for refunds</li>
     * </ul>
     *
     * @param customer the customer
     * @param total    the running total
     * @param payment  if <tt>true</tt> indicates the total is for a payment,
     *                 if <tt>false</tt> indicates it is for a refund
     * @return the new balance
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal getBalance(Party customer, BigDecimal total,
                                 boolean payment) {
        BigDecimal balance = getBalance(customer);
        BigDecimal result;
        if (payment) {
            result = balance.subtract(total);
        } else {
            result = balance.add(total);
        }
        if (result.signum() == -1) {
            result = (payment) ? BigDecimal.ZERO : result.negate();
        } else if (result.signum() == 1 && !payment) {
            result = BigDecimal.ZERO;
        }
        return result;
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
        return calculator.getOverdueBalance(customer, overdue);
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
            overdueFrom = DateRules.getDate(overdueFrom, -from, DateUnits.DAYS);
        }
        if (to > 0) {
            overdueTo = DateRules.getDate(overdue, -to, DateUnits.DAYS);
        }

        // query all overdue debit acts
        ArchetypeQuery query = QueryFactory.createUnallocatedObjectSetQuery(
                customer, DEBIT_SHORT_NAMES);

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
        ObjectSetQueryIterator iterator
                = new ObjectSetQueryIterator(service, query);
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
        Date overdue = date;
        if (bean.hasNode("type")) {
            List<Lookup> types = bean.getValues("type", Lookup.class);
            if (!types.isEmpty()) {
                overdue = getOverdueDate(types.get(0), date);
            }
        }
        return overdue;
    }

    /**
     * Returns the overdue date relative to the specified date for a customer
     * type.
     *
     * @param type a <em>lookup.customerAccountType</em>
     * @param date the date
     * @return the overdue date
     */
    public Date getOverdueDate(Lookup type, Date date) {
        return new AccountType(type, service).getOverdueDate(date);
    }

    /**
     * Calculates the sum of all unallocated credits for a customer.
     *
     * @param customer the customer
     * @return the credit amount
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal getCreditBalance(Party customer) {
        return calculator.getCreditBalance(customer);
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
        ArchetypeQuery query = QueryFactory.createQuery(customer, shortNames);

        query.add(new NodeSelectConstraint("a.amount"));
        query.add(new NodeSelectConstraint("a.credit"));
        query.add(new NodeConstraint("status", RelationalOp.NE,
                                     ActStatus.POSTED));
        Iterator<ObjectSet> iterator
                = new ObjectSetQueryIterator(service, query);

        ActCalculator calculator = new ActCalculator(service);
        BigDecimal result = BigDecimal.ZERO;
        while (iterator.hasNext()) {
            ObjectSet set = iterator.next();
            BigDecimal amount = (BigDecimal) set.get("a.amount");
            if (amount != null) {
                boolean credit = (Boolean) set.get("a.credit");
                result = calculator.addAmount(result, amount, credit);
            }
        }
        return result;
    }

    /**
     * Reverses an act.
     *
     * @param act       the act to reverse
     * @param startTime the start time of the reversal
     * @return the reversal of <tt>act</tt>
     * @throws ArchetypeServiceException for any archetype service error
     */
    public FinancialAct reverse(FinancialAct act, Date startTime) {
        IMObjectCopier copier
                = new IMObjectCopier(new CustomerActReversalHandler(act));
        FinancialAct reversal = (FinancialAct) copier.copy(act);
        reversal.setStatus(FinancialActStatus.POSTED);
        reversal.setActivityStartTime(startTime);
        service.save(reversal);
        return reversal;
    }

    /**
     * Generates an <em>act.customerAccountClosingBalance</em> and
     * <em>act.customerAccountOpeningBalance</em> for the specified customer,
     * using their account balance for the total of each act.
     *
     * @param customer the customer
     * @throws ArchetypeServiceException for any error
     */
    public void createPeriodEnd(Party customer) {
        BigDecimal total = getBalance(customer);
        FinancialAct close = createAct("act.customerAccountClosingBalance",
                                       customer, total);
        FinancialAct open = createAct("act.customerAccountOpeningBalance",
                                      customer, total);
        // ensure the acts are ordered correctly, ie. close before open
        Calendar calendar = Calendar.getInstance();
        close.setActivityStartTime(calendar.getTime());
        calendar.add(Calendar.MINUTE, 1);
        open.setActivityStartTime(calendar.getTime());
        service.save(open);   // TODO - should be saved in 1 transaction
        service.save(close); // see OBF-114
    }

    /**
     * Returns the startTime of the first
     * <tt>act.customerAccountOpeningBalance</tt> for a customer, before the
     * specified date.
     *
     * @param customer the customer
     * @param date     the date
     * @return the opening balance act startTime, or <tt>null</tt> if none is
     *         found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Date getOpeningBalanceDateBefore(Party customer, Date date) {
        return getActStartTime(OPENING_BALANCE, customer, date, RelationalOp.LT,
                               false);
    }

    /**
     * Returns the startTime of the first
     * <tt>act.customerAccountClosingBalance</tt> for a customer, before the
     * specified date.
     *
     * @param customer the customer
     * @param date     the date
     * @return the closing balance act startTime, or <tt>null</tt> if none is
     *         found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Date getClosingBalanceDateBefore(Party customer, Date date) {
        return getActStartTime(CLOSING_BALANCE, customer, date, RelationalOp.LT,
                               false);
    }

    /**
     * Returns the startTime of the first
     * <tt>act.customerAccountClosingBalance</tt> for a customer, after the
     * specified date.
     *
     * @param customer the customer
     * @param date     the date
     * @return the closing balance act startTime, or <tt>null</tt> if none is
     *         found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Date getClosingBalanceDateAfter(Party customer, Date date) {
        return getActStartTime(CLOSING_BALANCE, customer, date, RelationalOp.GT,
                               true);
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
     * Calculates the balance for a customer.
     *
     * @param act         the act that triggered the update.
     *                    May be <tt>null</tt>
     * @param unallocated the unallocated acts
     */
    void updateBalance(FinancialAct act, Iterator<FinancialAct> unallocated) {
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
     * Returns unallocated acts for a customer.
     *
     * @param customer the customer
     * @param exclude  the act to exclude. May be <tt>null</tt>
     * @return unallocated acts for the customer
     * @throws ArchetypeServiceException for any archetype service error
     */
    private Iterator<FinancialAct> getUnallocatedActs(Party customer,
                                                      Act exclude) {
        ArchetypeQuery query = QueryFactory.createUnallocatedQuery(
                customer, SHORT_NAMES, exclude);
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
     * Returns the startTime of a customer act whose startTime is before/after
     * the specified date, depending on the supplied operator.
     *
     * @param shortName     the act short name
     * @param customer      the customer
     * @param date          the date
     * @param operator      the operator
     * @param sortAscending if <tt>true</tt> sort acts on ascending startTime;
     *                      otherwise sort them on descending startTime
     * @return the startTime, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    private Date getActStartTime(String shortName, Party customer, Date date,
                                 RelationalOp operator,
                                 boolean sortAscending) {
        ArchetypeQuery query = QueryFactory.createQuery(
                customer, new String[]{shortName});
        if (date != null) {
            query.add(new NodeConstraint("a.startTime", operator, date));
        }
        query.add(new NodeSelectConstraint("a.startTime"));
        query.add(new NodeSortConstraint("startTime", sortAscending));
        query.setMaxResults(1);
        ObjectSetQueryIterator iter = new ObjectSetQueryIterator(service,
                                                                 query);
        if (iter.hasNext()) {
            ObjectSet set = iter.next();
            return (Date) set.get("a.startTime");
        }
        return null;
    }

    /**
     * Helper to create an act for a customer.
     *
     * @param shortName the act short name
     * @param customer  the customer
     * @param total     the act total
     * @return a new act
     */
    private FinancialAct createAct(String shortName, Party customer,
                                   BigDecimal total) {
        FinancialAct act = (FinancialAct) service.create(shortName);
        Date startTime = new Date();
        act.setActivityStartTime(startTime);
        ActBean bean = new ActBean(act, service);
        bean.addParticipation("participation.customer", customer);
        act.setTotal(new Money(total));
        return act;
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
            return calculator.getAllocatable(
                    act.getTotal(), act.getAllocatedAmount());
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
                    ACCOUNT_ALLOCATION_SHORTNAME, credit.getAct());
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
