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

package org.openvpms.archetype.rules.finance.statement;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.CLOSING_BALANCE;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.OPENING_BALANCE;
import org.openvpms.archetype.rules.finance.account.CustomerAccountQueryFactory;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.IterableIMObjectQuery;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.component.system.common.query.OrConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.openvpms.component.system.common.query.ShortNameConstraint;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * Helper for performing statement act queries.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class StatementActHelper {


    public class ActState {

        private final Date startTime;

        private final BigDecimal amount;

        private final boolean printed;

        public ActState(Date startTime, BigDecimal amount, boolean printed) {
            this.startTime = startTime;
            this.amount = amount;
            this.printed = printed;
        }

        public Date getStartTime() {
            return startTime;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public boolean isPrinted() {
            return printed;
        }
    }


    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The short names to query. This contains all debit/credit parent acts,
     * and opening and closing balances.
     */
    private static final String[] SHORT_NAMES;

    /**
     * Charge short names.
     */
    private static final String[] CHARGE_SHORT_NAMES = {
            CustomerAccountArchetypes.INVOICE,
            CustomerAccountArchetypes.COUNTER,
            CustomerAccountArchetypes.CREDIT
    };

    static {
        List<String> shortNames = new ArrayList<String>();
        shortNames.addAll(Arrays.asList(
                CustomerAccountArchetypes.DEBITS_CREDITS));
        shortNames.add(CustomerAccountArchetypes.OPENING_BALANCE);
        shortNames.add(CustomerAccountArchetypes.CLOSING_BALANCE);
        SHORT_NAMES = shortNames.toArray(new String[shortNames.size()]);
    }


    /**
     * Creates a new <tt>StatementActHelper</tt>.
     *
     * @param service the archetype service
     */
    public StatementActHelper(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Returns the timestamp for statement processing.
     *
     * @param statementDate the statement date
     * @return the date
     */
    public Date getStatementTimestamp(Date statementDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(statementDate);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * Determines if a customer has had end-of-period run on or after a
     * particular date.
     *
     * @param customer the customer
     * @param date     the date
     * @return <tt>true</tt> if end-of-period has been run on or after the date
     * @throws ArchetypeServiceException for any archetype service error
     */
    public boolean hasStatement(Party customer, Date date) {
        ActState state = getClosingBalanceAfter(customer, date);
        return (state != null);
    }

    /**
     * Returns the closing balance act for the specified statement date.
     *
     * @param customer      the customer
     * @param statementDate the statement date
     * @return the closing balance for the statement date, or <tt>null</tt> if
     *         none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public FinancialAct getClosingBalance(Party customer, Date statementDate) {
        ArchetypeQuery query = CustomerAccountQueryFactory.createQuery(
                customer, new String[]{CLOSING_BALANCE});
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(statementDate);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        query.add(new NodeConstraint("act.startTime", RelationalOp.GTE,
                                     calendar.getTime()));
        calendar.add(Calendar.DATE, 1);
        query.add(new NodeConstraint("act.startTime", RelationalOp.LT,
                                     calendar.getTime()));
        query.setMaxResults(1);
        Iterator<FinancialAct> iter = new IMObjectQueryIterator<FinancialAct>(
                service, query);
        return (iter.hasNext()) ? iter.next() : null;
    }

    /**
     * Returns all statement acts for a customer between the opening balance
     * prior to the specified date, and the corresponding closing balance
     * inclusive. Acts may have any status.
     *
     * @param customer      the customer
     * @param statementDate the statement date
     * @return the statement acts
     * @throws ArchetypeServiceException for any archetype service error
     * @throws ArchetypeQueryException   for any archetype query error
     */
    public Iterable<Act> getActs(Party customer, Date statementDate) {
        Date open = getOpeningBalanceTimestamp(customer, statementDate);
        Date close = getClosingBalanceTimestamp(customer, statementDate, open);
        ArchetypeQuery query = createQuery(customer, open, close, true,
                                           null);
        return new IterableIMObjectQuery<Act>(service, query);
    }

    /**
     * Returns all POSTED statement acts between the opening and closing balance
     * timestamps. The result includes the opening balance, but excludes the
     * closing balance.
     *
     * @param customer                the customer
     * @param openingBalanceTimestamp the opening balance timestamp. May be
     *                                <tt>null</tt>
     * @param closingBalanceTimestamp the closing balance timestamp. May be
     *                                <tt>null</tt>
     * @param includeClosingBalance   if <tt>true</tt> includes the closing
     *                                balance act, if present
     * @return the posted acts
     * @throws ArchetypeServiceException for any archetype service error
     * @throws ArchetypeQueryException   for any archetype query error
     */
    public Iterable<Act> getPostedActs(Party customer,
                                       Date openingBalanceTimestamp,
                                       Date closingBalanceTimestamp,
                                       boolean includeClosingBalance) {
        ArchetypeQuery query = createQuery(customer, openingBalanceTimestamp,
                                           closingBalanceTimestamp,
                                           includeClosingBalance,
                                           ActStatus.POSTED);
        return new IterableIMObjectQuery<Act>(service, query);
    }

    /**
     * Returns all COMPLETED charge act for a customer, between the
     * opening balance prior to the specified date, and the corresponding
     * closing balance. If there is no closing balance, returns up to the
     * statement date timestamp.
     *
     * @param customer      the customer
     * @param statementDate the statement date
     * @return any charge acts with COMPLETED status
     * @throws ArchetypeServiceException for any archetype service error
     * @throws ArchetypeQueryException   for any archetype query error
     */
    public Iterable<Act> getCompletedCharges(Party customer,
                                             Date statementDate) {
        Date openTimestamp = getOpeningBalanceTimestamp(customer,
                                                        statementDate);
        Date closeTimestamp = getClosingBalanceTimestamp(customer,
                                                         openTimestamp,
                                                         statementDate);
        return getCompletedCharges(customer, statementDate, openTimestamp,
                                   closeTimestamp);
    }

    /**
     * Returns all COMPLETED charge act for a customer, between the
     * opening and closing balance timestamps.
     * If there is no closing balance timestamp, uses the statement date
     * timestamp.
     *
     * @param customer                the customer
     * @param statementDate           the statement date
     * @param openingBalanceTimestamp the opening balance timestamp. May be
     *                                <tt>null</tt>
     * @param closingBalanceTimestamp the closing balance timestamp.
     *                                May be <tt>null</tt>
     * @return any charge acts with COMPLETED status
     * @throws ArchetypeServiceException for any archetype service error
     * @throws ArchetypeQueryException   for any archetype query error
     */
    public Iterable<Act> getCompletedCharges(Party customer, Date statementDate,
                                             Date openingBalanceTimestamp,
                                             Date closingBalanceTimestamp) {
        if (closingBalanceTimestamp == null) {
            closingBalanceTimestamp = getStatementTimestamp(statementDate);
        }
        ArchetypeQuery query = createQuery(customer, CHARGE_SHORT_NAMES,
                                           openingBalanceTimestamp, false,
                                           closingBalanceTimestamp, false,
                                           ActStatus.COMPLETED);
        return new IterableIMObjectQuery<Act>(service, query);
    }

    /**
     * Returns all POSTED statement acts and COMPLETED charge acts for a
     * customer from the opening balance timestamp to the end of the statement
     * date. <p/>
     *
     * @param customer                the customer
     * @param statementDate           the date
     * @param openingBalanceTimestamp the opening balance timestamp. May be
     *                                <tt>null</tt>
     * @return the statement acts
     * @throws ArchetypeServiceException for any archetype service error
     * @throws ArchetypeQueryException   for any archetype query error
     */
    public Iterable<Act> getPostedAndCompletedActs(
            Party customer, Date statementDate, Date openingBalanceTimestamp) {
        Date close = getStatementTimestamp(statementDate);
        ArchetypeQuery query = createQuery(customer, openingBalanceTimestamp,
                                           close, false, null);
        query.add(new OrConstraint()
                .add(new NodeConstraint("status", ActStatus.POSTED))
                .add(new NodeConstraint("status", ActStatus.COMPLETED)));
        return new IterableIMObjectQuery<Act>(service, query);
    }

    /**
     * Returns the opening balance timestamp for a customer and statement date.
     *
     * @param customer      the customer
     * @param statementDate the statement date
     * @return the opening balance, or <tt>null</tt> if none is found
     */
    public Date getOpeningBalanceTimestamp(Party customer, Date statementDate) {
        StatementActHelper.ActState state
                = getOpeningBalanceState(customer, statementDate);
        return (state != null) ? state.getStartTime() : null;
    }

    /**
     * Returns the closing balance timestamp for a customer relative to a
     * statement date and opening balance timestamp.
     *
     * @param customer                the customer
     * @param statementDate           the statement date
     * @param openingBalanceTimestamp the opening balance timestamp. May be
     *                                <tt>null</tt>
     * @return the closing balance timestamp, or <tt>null</tt> if none is found
     */
    public Date getClosingBalanceTimestamp(Party customer, Date statementDate,
                                           Date openingBalanceTimestamp) {
        ActState state = getClosingBalanceState(customer, statementDate,
                                                openingBalanceTimestamp);
        return (state != null) ? state.getStartTime() : null;
    }

    /**
     * Returns the opening balance act state for a customer prior to  the
     * specified statement date.
     *
     * @param customer      the customer
     * @param statementDate the statement date
     * @return the opening balance state
     */
    public ActState getOpeningBalanceState(Party customer, Date statementDate) {
        return getActState(OPENING_BALANCE, customer, statementDate,
                           RelationalOp.LT, false);
    }


    /**
     * Returns the closing balance act state for a customer relative to a
     * statement date and opening balance timestamp.
     *
     * @param customer                the customer
     * @param statementDate           the statement date
     * @param openingBalanceTimestamp the opening balance timestamp. May be
     *                                <tt>null</tt>
     * @return the closing balance state, or <tt>null</tt> if none is found
     */
    public ActState getClosingBalanceState(Party customer, Date statementDate,
                                           Date openingBalanceTimestamp) {
        ActState result;
        if (openingBalanceTimestamp == null) {
            result = getClosingBalanceBefore(customer, statementDate);
            if (result == null) {
                result = getClosingBalanceAfter(customer, statementDate);
            }
        } else {
            result = getClosingBalanceAfter(customer, openingBalanceTimestamp);
        }
        return result;
    }

    /**
     * Determines if there is any account activity between the specified
     * timetamps for a customer.
     *
     * @param customer                the customer
     * @param openingBalanceTimestamp the opening balance timestamp.
     *                                May be <tt>null</tt>
     * @param closingBalanceTimestamp the closing balance timestamp.
     *                                May be <tt>null</tt>
     * @return <tt>true</tt> if there is any account activity between the
     *         specified times
     */
    public boolean hasAccountActivity(Party customer,
                                      Date openingBalanceTimestamp,
                                      Date closingBalanceTimestamp) {
        ArchetypeQuery query = createQuery(customer, SHORT_NAMES,
                                           openingBalanceTimestamp, false,
                                           closingBalanceTimestamp, false,
                                           null);
        query.add(new NodeSelectConstraint("act.startTime"));
        query.setMaxResults(1);
        Iterator<ObjectSet> iter = new ObjectSetQueryIterator(service, query);
        return iter.hasNext();
    }

    /**
     * Returns the state of a customer act whose startTime is before/after
     * the specified date, depending on the supplied operator.
     *
     * @param shortName     the act short name
     * @param customer      the customer
     * @param date          the date
     * @param operator      the operator
     * @param sortAscending if <tt>true</tt> sort acts on ascending startTime;
     *                      otherwise sort them on descending startTime
     * @return the state, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public ActState getActState(String shortName, Party customer,
                                Date date, RelationalOp operator,
                                boolean sortAscending) {
        ArchetypeQuery query = CustomerAccountQueryFactory.createQuery(
                customer, new String[]{shortName});
        if (date != null) {
            query.add(new NodeConstraint("act.startTime", operator, date));
        }
        query.add(new NodeSelectConstraint("act.startTime"));
        query.add(new NodeSelectConstraint("act.amount"));
        query.add(new NodeSelectConstraint("act.credit"));
        query.add(new NodeSelectConstraint("act.printed"));
        query.add(new NodeSortConstraint("startTime", sortAscending));
        query.setMaxResults(1);
        ObjectSetQueryIterator iter = new ObjectSetQueryIterator(service,
                                                                 query);
        if (iter.hasNext()) {
            ObjectSet set = iter.next();
            Date startTime = (Date) set.get("act.startTime");
            BigDecimal amount = (BigDecimal) set.get("act.amount");
            boolean credit = (Boolean) set.get("act.credit");
            boolean printed = (Boolean) set.get("act.printed");
            if (credit) {
                amount = amount.negate();
            }
            return new ActState(startTime, amount, printed);
        }
        return null;
    }

    /**
     * Returns the state of the first
     * <tt>act.customerAccountClosingBalance</tt> for a customer, before the
     * specified timetamp.
     *
     * @param customer  the customer
     * @param timestamp the timestamp
     * @return the closing balance act startTime, or <tt>null</tt> if none is
     *         found
     * @throws ArchetypeServiceException for any archetype service error
     */
    private ActState getClosingBalanceBefore(Party customer, Date timestamp) {
        return getActState(CLOSING_BALANCE, customer, timestamp,
                           RelationalOp.LT, false);
    }

    /**
     * Returns the state of the first
     * <tt>act.customerAccountClosingBalance</tt> for a customer, after
     * the specified timestamp.
     *
     * @param customer the customer
     * @param timetamp the timestamp
     * @return the closing balance act state, or <tt>null</tt> if none is
     *         found
     * @throws ArchetypeServiceException for any archetype service error
     */
    private ActState getClosingBalanceAfter(Party customer, Date timetamp) {
        return getActState(CLOSING_BALANCE, customer, timetamp,
                           RelationalOp.GT, true);
    }

    /**
     * Helper to create a query for all account act types between
     * the opening and closing balance timetamps. This includes the opening
     * balance act, and optionally the closing balance.
     *
     * @param customer                the customer
     * @param openingBalanceTimestamp the opening balance timestamp.
     *                                May be <tt>null</tt>
     * @param closingBalanceTimestamp the closing balance timestamp.
     *                                May be <tt>null</tt>
     * @param includeClosingBalance   if <tt>true</tt> includes the closing
     *                                balance act, if present
     * @param status                  the status. May be <tt>null</tt>
     * @return a new query
     * @throws ArchetypeServiceException for any archetype service error
     */
    private ArchetypeQuery createQuery(Party customer,
                                       Date openingBalanceTimestamp,
                                       Date closingBalanceTimestamp,
                                       boolean includeClosingBalance,
                                       String status) {
        return createQuery(customer, SHORT_NAMES, openingBalanceTimestamp,
                           true, closingBalanceTimestamp,
                           includeClosingBalance, status);
    }

    /**
     * Helper to creates a query for all account act types between
     * the opening and closing balance timetamps. The query includes the
     * opening balance.
     *
     * @param customer                the customer
     * @param shortNames              the account act short names
     * @param openingBalanceTimestamp the opening balance timestamp.
     *                                May be <tt>null</tt>
     * @param includeOpeningBalance   if <tt>true</tt> includes the opening
     *                                balance act, if present
     * @param closingBalanceTimestamp the closing balance timestamp.
     *                                May be <tt>null</tt>
     * @param includeClosingBalance   if <tt>true</tt> includes the closing
     *                                balance act, if present
     * @param status                  the status. May be <tt>null</tt>
     * @return a new query
     * @throws ArchetypeServiceException for any archetype service error
     */
    private ArchetypeQuery createQuery(Party customer,
                                       String[] shortNames,
                                       Date openingBalanceTimestamp,
                                       boolean includeOpeningBalance,
                                       Date closingBalanceTimestamp,
                                       boolean includeClosingBalance,
                                       String status) {
        ShortNameConstraint archetypes = new ShortNameConstraint(
                "act", shortNames, false, false);
        ArchetypeQuery query = new ArchetypeQuery(archetypes);
        CollectionNodeConstraint constraint = new CollectionNodeConstraint(
                "customer", "participation.customer", false, false);
        constraint.add(new ObjectRefNodeConstraint(
                "entity", customer.getObjectReference()));
        if (openingBalanceTimestamp != null) {
            RelationalOp op = (includeOpeningBalance) ? RelationalOp.GTE
                                                      : RelationalOp.GT;
            query.add(new NodeConstraint("startTime", op,
                                         openingBalanceTimestamp));
        }
        if (closingBalanceTimestamp != null) {
            RelationalOp op = (includeClosingBalance) ? RelationalOp.LTE
                                                      : RelationalOp.LT;
            query.add(new NodeConstraint("startTime", op,
                                         closingBalanceTimestamp));
        }
        if (status != null) {
            query.add(new NodeConstraint("status", status));
        }
        query.add(constraint);
        query.add(new NodeSortConstraint("startTime"));
        query.add(new NodeSortConstraint("id"));
        return query;
    }

}
