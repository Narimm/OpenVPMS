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
import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.NamedQuery;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectRefSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.ACCOUNT_ACTS;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.CLOSING_BALANCE;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.COUNTER;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.CREDIT;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.CREDITS;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.DEBITS;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.DEBITS_CREDITS;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.INVOICE;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.OPENING_BALANCE;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountRuleException.ErrorCode.InvalidBalance;


/**
 * Customer balance calculator.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class BalanceCalculator {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;


    /**
     * Creates a new <tt>BalanceCalculator</tt>.
     *
     * @param service the archetype service
     */
    public BalanceCalculator(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Calculates the outstanding balance for a customer.
     *
     * @param customer the customer
     * @return the outstanding balance
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal getBalance(Party customer) {
        ArchetypeQuery query = CustomerAccountQueryFactory.createUnallocatedObjectSetQuery(customer, DEBITS_CREDITS);
        Iterator<ObjectSet> iterator = new ObjectSetQueryIterator(service, query);
        return calculateBalance(iterator);
    }

    /**
     * Calculates the outstanding balance for a customer, incorporating acts
     * up to the specified date.
     *
     * @param customer the customer
     * @param date     the date
     * @return the outstanding balance
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal getBalance(Party customer, Date date) {
        ArchetypeQuery query = CustomerAccountQueryFactory.createUnallocatedObjectSetQuery(customer, DEBITS_CREDITS);
        Iterator<ObjectSet> iterator = new ObjectSetQueryIterator(service, query);
        query.add(Constraints.lt("startTime", date));
        return calculateBalance(iterator);
    }

    /**
     * Calculates the balance for a customer for all POSTED acts
     * between two times, inclusive.
     *
     * @param customer       the customer
     * @param from           the from time. If <tt>null</tt>, indicates that
     *                       the time is unbounded
     * @param to             the to time. If <tt>null</tt>, indicates that the
     *                       time is unbounded
     * @param openingBalance the opening balance
     * @return the balance
     */
    public BigDecimal getBalance(Party customer, Date from, Date to, BigDecimal openingBalance) {
        ArchetypeQuery query = CustomerAccountQueryFactory.createObjectSetQuery(customer, DEBITS_CREDITS, true);
        query.add(Constraints.eq("status", FinancialActStatus.POSTED));
        if (from != null) {
            query.add(Constraints.gte("startTime", from));
        }
        if (to != null) {
            query.add(Constraints.lte("startTime", to));
        }
        Iterator<ObjectSet> iterator = new ObjectSetQueryIterator(service, query);
        return calculateDefinitiveBalance(iterator, openingBalance);
    }

    /**
     * Calculates a definitive outstanding balance for a customer.
     * This sums total amounts for <em>all</em> POSTED acts associated with the
     * customer, rather than just using unallocated acts, and can be used
     * to detect account balance errors.
     *
     * @param customer the customer
     * @return the outstanding balance
     * @throws ArchetypeServiceException    for any archetype service error
     * @throws CustomerAccountRuleException if an opening or closing balance
     *                                      doesn't match the expected balance
     */
    public BigDecimal getDefinitiveBalance(Party customer) {
        ArchetypeQuery query = CustomerAccountQueryFactory.createQuery(
                customer, ACCOUNT_ACTS);
        query.add(Constraints.sort("startTime"));
        query.add(Constraints.sort("id"));
        query.add(new ObjectRefSelectConstraint("act"));
        query.add(new NodeSelectConstraint("amount"));
        query.add(new NodeSelectConstraint("credit"));
        query.add(Constraints.eq("status", FinancialActStatus.POSTED));
        Iterator<ObjectSet> iterator
                = new ObjectSetQueryIterator(service, query);
        BigDecimal total = BigDecimal.ZERO;
        ActCalculator calculator = new ActCalculator(service);
        while (iterator.hasNext()) {
            ObjectSet set = iterator.next();
            BigDecimal amount = set.getBigDecimal("act.amount", BigDecimal.ZERO);
            boolean credit = set.getBoolean("act.credit");
            IMObjectReference act = set.getReference("act.reference");
            if (TypeHelper.isA(act, OPENING_BALANCE, CLOSING_BALANCE)) {
                if (TypeHelper.isA(act, CLOSING_BALANCE)) {
                    credit = !credit;
                }
                BigDecimal balance = (credit) ? amount.negate() : amount;
                if (balance.compareTo(total) != 0) {
                    throw new CustomerAccountRuleException(
                            InvalidBalance, act.getArchetypeId().getShortName(),
                            total, balance);
                }
            } else {
                total = calculator.addAmount(total, amount, credit);
            }
        }
        return total;
    }

    /**
     * Calculates the current overdue balance for a customer.
     * This is the sum of unallocated amounts in associated debits that have a
     * date less than the specified overdue date.
     *
     * @param customer the customer
     * @param date     the overdue date
     * @return the overdue balance
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal getOverdueBalance(Party customer, Date date) {
        // query all overdue debit acts
        ArchetypeQuery query = CustomerAccountQueryFactory.createUnallocatedObjectSetQuery(customer, DEBITS);
        query.add(Constraints.lt("startTime", date));
        Iterator<ObjectSet> iterator = new ObjectSetQueryIterator(service, query);

        BigDecimal amount = calculateBalance(iterator);
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            amount = BigDecimal.ZERO;
        }
        return amount;
    }

    /**
     * Calculates the overdue balance for a customer as of a particular date.
     * <p/>
     * This sums any POSTED debits prior to <em>overdueDate</em> that had
     * not been fully allocated by credits as of <em>date</em>.
     *
     * @param customer    the customer
     * @param date        the date
     * @param overdueDate the date when amounts became overdue
     * @return the overdue balance
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal getOverdueBalance(Party customer, Date date,
                                        Date overdueDate) {

        NamedQuery query = new NamedQuery("getOverdueAmounts", Arrays.asList(
                "id", "total", "allocatedTotal", "allocatedAmount",
                "overdueAllocationTime"));
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        query.setParameter("customer", customer.getId());
        query.setParameter("date", date);
        query.setParameter("overdueDate", overdueDate);

        ObjectSetQueryIterator iter = new ObjectSetQueryIterator(service,
                                                                 query);
        long lastId = -1;
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal allocatedTotal = BigDecimal.ZERO;
        BigDecimal result = BigDecimal.ZERO;
        while (iter.hasNext()) {
            ObjectSet set = iter.next();
            long uid = set.getLong("id");
            if (uid != lastId) {
                if (lastId != -1) {
                    result = result.add(total).subtract(allocatedTotal);
                }
                total = set.getBigDecimal("total", BigDecimal.ZERO);
                allocatedTotal = set.getBigDecimal("allocatedTotal",
                                                   BigDecimal.ZERO);
                lastId = uid;
            }
            BigDecimal allocatedAmount = set.getBigDecimal("allocatedAmount",
                                                           BigDecimal.ZERO);
            Date overdueAllocationTime = set.getDate("overdueAllocationTime");
            if (overdueAllocationTime != null) {
                // amount was allocated past date, so remove it
                allocatedTotal = allocatedTotal.subtract(allocatedAmount);
            }
        }
        result = result.add(total).subtract(allocatedTotal);
        return result;
    }

    /**
     * Calculates the sum of all unallocated credits for a customer.
     *
     * @param customer the customer
     * @return the credit amount
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal getCreditBalance(Party customer) {
        ArchetypeQuery query
                = CustomerAccountQueryFactory.createUnallocatedObjectSetQuery(
                customer, CREDITS);
        Iterator<ObjectSet> iterator = new ObjectSetQueryIterator(service,
                                                                  query);
        return calculateBalance(iterator);
    }

    /**
     * Returns the unbilled amount for a customer.
     *
     * @param customer the customer
     * @return the unbilled amount
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal getUnbilledAmount(Party customer) {
        String[] shortNames = {INVOICE, COUNTER,
                               CREDIT};
        ArchetypeQuery query
                = CustomerAccountQueryFactory.createUnbilledObjectSetQuery(
                customer, shortNames);
        Iterator<ObjectSet> iterator = new ObjectSetQueryIterator(service,
                                                                  query);
        return calculateBalance(iterator);
    }

    /**
     * Returns the amount of an act yet to be allocated.
     *
     * @param act the act
     * @return the amount yet to be allocated
     */
    public BigDecimal getAllocatable(FinancialAct act) {
        return getAllocatable(act.getTotal(), act.getAllocatedAmount());
    }

    /**
     * Helper to return the amount that may be allocated from a total.
     * If either value is <tt>null</tt> they are treated as being <tt>0.0</tt>.
     *
     * @param amount    the total amount. May be <tt>null</tt>
     * @param allocated the current amount allocated. May be <tt>null<tt>
     * @return <tt>amount - allocated</tt>
     */
    public BigDecimal getAllocatable(BigDecimal amount, BigDecimal allocated) {
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }
        if (allocated == null) {
            allocated = BigDecimal.ZERO;
        }
        return amount.subtract(allocated);
    }

    /**
     * Determines if the act has been fully allocated.
     *
     * @param act the act
     * @return <tt>true</tt> if the act has been full allocated
     */
    public boolean isAllocated(FinancialAct act) {
        return getAllocatable(act).compareTo(BigDecimal.ZERO) <= 0;
    }

    /**
     * Calculates the oustanding balance.
     *
     * @param iterator an iterator over the collection
     * @return the outstanding balance
     */
    protected BigDecimal calculateBalance(Iterator<ObjectSet> iterator) {
        BigDecimal total = BigDecimal.ZERO;
        ActCalculator calculator = new ActCalculator(service);
        while (iterator.hasNext()) {
            ObjectSet set = iterator.next();
            BigDecimal amount = set.getBigDecimal("act.amount", BigDecimal.ZERO);
            BigDecimal allocated = set.getBigDecimal("act.allocatedAmount", BigDecimal.ZERO);
            boolean credit = set.getBoolean("act.credit");
            BigDecimal unallocated = getAllocatable(amount, allocated);
            total = calculator.addAmount(total, unallocated, credit);
        }
        return total;
    }

    /**
     * Calculates a definitive balance, using act totals.
     *
     * @param iterator an iterator over the collection
     * @return the outstanding balance
     */
    protected BigDecimal calculateDefinitiveBalance(
            Iterator<ObjectSet> iterator) {
        return calculateDefinitiveBalance(iterator, BigDecimal.ZERO);
    }

    /**
     * Calculates a definitive balance, using act totals.
     *
     * @param iterator       an iterator over the collection
     * @param openingBalance the opening balance
     * @return the balance
     */
    protected BigDecimal calculateDefinitiveBalance(
            Iterator<ObjectSet> iterator, BigDecimal openingBalance) {
        BigDecimal total = openingBalance;
        ActCalculator calculator = new ActCalculator(service);
        while (iterator.hasNext()) {
            ObjectSet set = iterator.next();
            BigDecimal amount = set.getBigDecimal("act.amount", BigDecimal.ZERO);
            boolean credit = set.getBoolean("act.credit");
            total = calculator.addAmount(total, amount, credit);
        }
        return total;
    }

}
