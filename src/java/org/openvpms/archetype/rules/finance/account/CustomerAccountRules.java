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

import org.openvpms.archetype.rules.act.FinancialActStatus;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.DEBITS;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
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
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.component.system.common.query.RelationalOp;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


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
     * Calculates the outstanding balance for a customer.
     *
     * @param customer the customer
     * @return the balance
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal getBalance(Party customer) {
        return calculator.getBalance(customer);
    }

    /**
     * Calculates the outstanding balance for a customer, incorporating acts
     * up to the specified date.
     *
     * @param customer the customer
     * @param date     the date
     * @return the balance
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal getBalance(Party customer, Date date) {
        return calculator.getBalance(customer, date);
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
    public BigDecimal getBalance(Party customer, Date from, Date to,
                                 BigDecimal openingBalance) {
        return calculator.getBalance(customer, from, to, openingBalance);
    }

    /**
     * Calculates a definitive outstanding balance for a customer.
     * <p/>
     * This sums total amounts for <em>all</em> POSTED acts associated with the
     * customer, rather than just using unallocated acts, and can be used
     * to detect account balance errors.
     *
     * @param customer the customer
     * @return the definitive balance
     * @throws ArchetypeServiceException    for any archetype service error
     * @throws CustomerAccountRuleException if an opening or closing balance
     *                                      is incorrect
     */
    public BigDecimal getDefinitiveBalance(Party customer) {
        return calculator.getDefinitiveBalance(customer);
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
     * Calculates the current overdue balance for a customer.
     * This is the sum of unallocated amounts in associated debits that have a
     * date less than the specified date less the overdue days.
     * The overdue days are specified in the customer's type node.
     * <p/>
     * NOTE: this method may not be used to determine an historical overdue
     * balance. For this, use {@link #getOverdueBalance(Party, Date, Date)
     * getOverdueBalance(Party customer, Date date, Date overdueDate)}.
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
     * Calculates the overdue balance for a customer as of a particular date.
     * <p/>
     * This sums any POSTED debits prior to <em>overdueDate</em> that had
     * not been fully allocated by credits as of <em>date</em>.
     *
     * @param customer    the customer
     * @param date        the date
     * @param overdueDate the date when amounts became overdue
     * @return the overdue balance
     */
    public BigDecimal getOverdueBalance(Party customer, Date date,
                                        Date overdueDate) {
        return calculator.getOverdueBalance(customer, date, overdueDate);
    }

    /**
     * Determines if a customer has an overdue balance within the nominated
     * day range past their standard terms.
     *
     * @param customer the customer
     * @param date     the date
     * @param from     the from day range
     * @param to       the to day range. Use <tt>&lt;= 0</tt> to indicate
     *                 all dates
     * @return <tt>true</tt> if the customer has an overdue balance within
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
        ArchetypeQuery query
                = CustomerAccountQueryFactory.createUnallocatedObjectSetQuery(
                customer, DEBITS);

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
     * @return the unbilled amount
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal getUnbilledAmount(Party customer) {
        return calculator.getUnbilledAmount(customer);
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
        return reverse(act, startTime, null);
    }

    /**
     * Reverses an act.
     *
     * @param act       the act to reverse
     * @param startTime the start time of the reversal
     * @param notes     notes indicating the reason for the reversal, to set the 'notes' node if the act has one.
     *                  May be <tt>null</tt>
     * @return the reversal of <tt>act</tt>
     * @throws ArchetypeServiceException for any archetype service error
     */
    public FinancialAct reverse(FinancialAct act, Date startTime, String notes) {
        IMObjectCopier copier = new IMObjectCopier(new CustomerActReversalHandler(act));
        List<IMObject> objects = copier.apply(act);
        FinancialAct reversal = (FinancialAct) objects.get(0);
        ActBean bean = new ActBean(reversal, service);
        if (bean.hasNode("notes")) {
            bean.setValue("notes", notes);
        }
        reversal.setStatus(FinancialActStatus.POSTED);
        reversal.setActivityStartTime(startTime);
        service.save(objects);
        return reversal;
    }

}
