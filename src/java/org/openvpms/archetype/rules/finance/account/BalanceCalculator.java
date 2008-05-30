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
import org.openvpms.archetype.rules.act.FinancialActStatus;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.*;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.component.system.common.query.RelationalOp;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;


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
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal getBalance(Party customer) {
        ArchetypeQuery query
                = CustomerAccountQueryFactory.createUnallocatedObjectSetQuery(
                customer, DEBITS_CREDITS);
        Iterator<ObjectSet> iterator
                = new ObjectSetQueryIterator(service, query);
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
        ArchetypeQuery query
                = CustomerAccountQueryFactory.createUnallocatedObjectSetQuery(
                customer, DEBITS_CREDITS);
        Iterator<ObjectSet> iterator
                = new ObjectSetQueryIterator(service, query);
        query.add(new NodeConstraint("startTime", RelationalOp.LT, date));
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
     */
    public BigDecimal getBalance(Party customer, Date from, Date to,
                                 BigDecimal openingBalance) {
        ArchetypeQuery query = CustomerAccountQueryFactory.createObjectSetQuery(
                customer, DEBITS_CREDITS, true);
        query.add(new NodeConstraint("status", FinancialActStatus.POSTED));
        if (from != null) {
            query.add(new NodeConstraint("startTime", RelationalOp.GTE,
                                         from));
        }
        if (to != null) {
            query.add(new NodeConstraint("startTime", RelationalOp.LTE, to));
        }
        Iterator<ObjectSet> iterator
                = new ObjectSetQueryIterator(service, query);
        return calculateDefinitiveBalance(iterator, openingBalance);
    }

    /**
     * Calculates a definitive outstanding balance for a customer.
     * This sums total amounts for <em>all</em> POSTED acts associated with the
     * customer, rather than just using unallocated acts, and can be used
     * to detect account balance errors.
     *
     * @param customer the customer
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal getDefinitiveBalance(Party customer) {
        return getBalance(customer, null, null, BigDecimal.ZERO);
    }

    /**
     * Calculates the overdue balance for a customer.
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
        ArchetypeQuery query
                = CustomerAccountQueryFactory.createUnallocatedObjectSetQuery(
                customer, DEBITS);
        query.add(new NodeConstraint("startTime", RelationalOp.LT, date));
        Iterator<ObjectSet> iterator
                = new ObjectSetQueryIterator(service, query);

        BigDecimal amount = calculateBalance(iterator);
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            amount = BigDecimal.ZERO;
        }
        return amount;
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
            BigDecimal amount = (BigDecimal) set.get("a.amount");
            BigDecimal allocated = (BigDecimal) set.get("a.allocatedAmount");
            boolean credit = (Boolean) set.get("a.credit");
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
            BigDecimal amount = (BigDecimal) set.get("a.amount");
            boolean credit = (Boolean) set.get("a.credit");
            total = calculator.addAmount(total, amount, credit);
        }
        return total;
    }

}
