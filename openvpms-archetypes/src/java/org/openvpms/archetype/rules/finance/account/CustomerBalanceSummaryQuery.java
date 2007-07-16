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
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.NamedQuery;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.component.system.common.query.ShortNameConstraint;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;


/**
 * Queries customer balance summaries.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerBalanceSummaryQuery implements Iterator<ObjectSet> {

    /**
     * The customer reference.
     */
    public static final String CUSTOMER_REFERENCE = "customer.objectReference";

    /**
     * The customer name.
     */
    public static final String CUSTOMER_NAME = "customer.name";

    /**
     * The customer balance.
     */
    public static final String BALANCE = "balance";

    /**
     * The customer overdue balance.
     */
    public static final String OVERDUE_BALANCE = "overdueBalance";

    /**
     * The customer credit balance.
     */
    public static final String CREDIT_BALANCE = "creditBalance";

    /**
     * The customer's last payment date.
     */
    public static final String LAST_PAYMENT_DATE = "lastPaymentDate";

    /**
     * The customer's last payment amount.
     */
    public static final String LAST_PAYMENT_AMOUNT = "lastPaymentAmount";

    /**
     * The customer's last invoice date.
     */
    public static final String LAST_INVOICE_DATE = "lastInvoiceDate";

    /**
     * The customer's last invoice amount.
     */
    public static final String LAST_INVOICE_AMOUNT = "lastInvoiceAmount";

    /**
     * The date.
     */
    private final Date date;

    /**
     * Determines if only overdue account summaries should be returned.
     */
    private final boolean overdue;

    /**
     * The overdue from-day range.
     */
    private int from;

    /**
     * The overrdue to-day range.
     */
    private int to;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The last retrieved set.
     */
    private ObjectSet last;

    /**
     * The next set to return.
     */
    private ObjectSet next;

    /**
     * The balance iterator.
     */
    private Iterator<ObjectSet> iterator;

    /**
     * The rules.
     */
    private final CustomerAccountRules rules;

    /**
     * Customer account type lookup cache.
     */
    private Map<String, Lookup> lookups = new HashMap<String, Lookup>();

    /**
     * Balance calculator helper.
     */
    private final BalanceCalculator balanceCalc;

    /**
     * Calculator helper.
     */
    private final ActCalculator calculator;

    /**
     * Object set key names.
     */
    private static final Set<String> NAMES;

    static {
        NAMES = new LinkedHashSet<String>();
        NAMES.add(CUSTOMER_REFERENCE);
        NAMES.add(CUSTOMER_NAME);
        NAMES.add(BALANCE);
        NAMES.add(OVERDUE_BALANCE);
        NAMES.add(CREDIT_BALANCE);
        NAMES.add(LAST_PAYMENT_DATE);
        NAMES.add(LAST_PAYMENT_AMOUNT);
        NAMES.add(LAST_INVOICE_DATE);
        NAMES.add(LAST_INVOICE_AMOUNT);
    }


    /**
     * Constructs a new <tt>CustomerBalanceSummaryQuery</tt> for all account
     * types.
     *
     * @param date the date
     */
    public CustomerBalanceSummaryQuery(Date date) {
        this(date, -1, -1, null);
    }

    /**
     * Constructs a new <tt>CustomerAccountBalanceSummaryQuery</tt> for a
     * particular account type.
     *
     * @param date        the date
     * @param accountType the account type
     */
    public CustomerBalanceSummaryQuery(Date date, Lookup accountType) {
        this(date, -1, -1, accountType);
    }

    /**
     * Constructs a new <tt>CustomerBalanceSummaryQuery</tt> that returns
     * overdue balances within the specified date range.
     *
     * @param date        the date
     * @param overdueFrom the overdue-from date. Use <code>&lt;= 0</code> to
     *                    indicate all dates
     * @param overdueTo   the overdue-to date. Use <code>&lt;= 0</code> to
     *                    indicate all dates
     * @param accountType the account type. May be <tt>null</tt> to indicate
     *                    all account types
     */
    public CustomerBalanceSummaryQuery(Date date, int overdueFrom,
                                       int overdueTo, Lookup accountType) {
        this(date, overdueFrom, overdueTo, accountType,
             ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Constructs a new <tt>CustomerBalanceSummaryQuery</tt> that returns
     * overdue balances within the specified date range.
     *
     * @param date        the date
     * @param overdueFrom the overdue-from date. Use <code>&lt;= 0</code> to
     *                    indicate all dates
     * @param overdueTo   the overdue-to date. Use <code>&lt;= 0</code> to
     *                    indicate all dates
     * @param accountType the account type. May be <tt>null</tt> to indicate
     *                    all account types
     * @param service     the archetype service
     */
    public CustomerBalanceSummaryQuery(Date date, int overdueFrom,
                                       int overdueTo, Lookup accountType,
                                       IArchetypeService service) {
        this.date = date;
        this.service = service;
        this.overdue = overdueFrom >= 0 && overdueTo >= 0;
        this.from = overdueFrom;
        this.to = overdueTo;
        rules = new CustomerAccountRules(service);
        Collection<String> names = Arrays.asList("e.name", "p.entity", "p.act",
                                                 "a.activityStartTime",
                                                 "a.total", "a.allocatedAmount",
                                                 "a.credit", "c.code");
        NamedQuery query;
        if (accountType == null) {
            query = new NamedQuery("getBalances", names);
        } else {
            query = new NamedQuery("getBalancesForAccountType", names);
            query.setParameter("accountType", accountType.getLinkId());
        }
        query.setMaxResults(1000);
        iterator = new ObjectSetQueryIterator(service, query);
        balanceCalc = new BalanceCalculator(service);
        calculator = new ActCalculator(service);
    }

    /**
     * Returns <tt>true</tt> if the iteration has more elements.
     *
     * @return <tt>true</tt> if the iterator has more elements.
     */
    public boolean hasNext() {
        if (next == null) {
            while (last != null || iterator.hasNext()) {
                next = doNext();
                if (next != null) {
                    break;
                }
            }
        }
        return (next != null);
    }

    /**
     * Returns the next element in the iteration.  Calling this method
     * repeatedly until the {@link #hasNext()} method returns false will
     * return each element in the underlying collection exactly once.
     *
     * @return the next element in the iteration.
     * @throws NoSuchElementException iteration has no more elements.
     */
    public ObjectSet next() {
        ObjectSet result = next;
        next = null;
        return result;
    }

    /**
     * Not supported.
     *
     * @throws UnsupportedOperationException if invoked
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the next set in the iteration.
     *
     * @return the next set or <tt>null</tt> if overdue balances are being
     *         queried and the current balance is not overdue
     */
    private ObjectSet doNext() {
        IMObjectReference current = null;
        Map<IMObjectReference, ObjectSet> sets
                = new LinkedHashMap<IMObjectReference, ObjectSet>();
        // the sets for the current customer, keyed on act. This is necessary
        // to filter duplicate rows, should a customer erroneously have > 1
        // account type configured

        if (last != null) {
            sets.put(getAct(last), last);
            current = getEntity(last);
            last = null;
        }
        while (iterator.hasNext()) {
            ObjectSet set = iterator.next();
            IMObjectReference party = getEntity(set);
            if (party != null) {
                if (current == null || current.equals(party)) {
                    current = party;
                    sets.put(getAct(set), set);
                } else {
                    last = set;
                    break;
                }
            }
        }
        if (sets.isEmpty()) {
            throw new NoSuchElementException();
        }

        String name = null;
        BigDecimal balance = BigDecimal.ZERO;
        BigDecimal overdueBalance = BigDecimal.ZERO;
        BigDecimal creditBalance = BigDecimal.ZERO;
        Date overdueDate = null;
        Date overdueFrom = null;
        Date overdueTo = null;
        String code;
        Lookup lookup = null;
        for (ObjectSet set : sets.values()) {
            name = (String) set.get("e.name");
            Date startTime = (Date) set.get("a.activityStartTime");
            if (startTime instanceof Timestamp) {
                startTime = new Date(startTime.getTime());
            }
            BigDecimal amount = (BigDecimal) set.get("a.total");
            BigDecimal allocated = (BigDecimal) set.get("a.allocatedAmount");
            boolean credit = (Boolean) set.get("a.credit");
            BigDecimal unallocated
                    = balanceCalc.getAllocatable(amount, allocated);
            balance = calculator.addAmount(balance, unallocated, credit);
            code = (String) set.get("c.code");
            if (code != null && lookup == null) {
                lookup = getLookup(code);
            }
            if (overdueDate == null) {
                if (lookup == null) {
                    overdueDate = date;
                } else {
                    overdueDate = rules.getOverdueDate(lookup, date);
                }
                overdueFrom = overdueDate;
                if (from > 0) {
                    overdueFrom = DateRules.getDate(overdueFrom, -from,
                                                    DateUnits.DAYS);
                }
                if (to > 0) {
                    overdueTo = DateRules.getDate(overdueDate, -to,
                                                  DateUnits.DAYS);
                }
            }
            if (!credit && startTime.compareTo(overdueFrom) < 0
                    && (overdueTo == null || (overdueTo != null
                    && startTime.compareTo(overdueTo) > 0))) {
                overdueBalance = calculator.addAmount(overdueBalance,
                                                      unallocated, credit);
            }
            if (credit) {
                creditBalance = calculator.addAmount(creditBalance, unallocated,
                                                     credit);
            }
        }
        if (overdueBalance.signum() < 0) {
            overdueBalance = BigDecimal.ZERO;
        }
        if (overdue && overdueBalance.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        ObjectSet result = new BalanceObjectSet(current);
        result.add(CUSTOMER_REFERENCE, current);
        result.add(CUSTOMER_NAME, name);
        result.add(BALANCE, balance);
        result.add(OVERDUE_BALANCE, overdueBalance);
        result.add(CREDIT_BALANCE, creditBalance);
        return result;
    }

    /**
     * Returns a lookup given its code.
     *
     * @param code the lookup code
     * @return the lookup corresponding to <tt>code</tt> or <tt>null</tt>
     *         if none is found
     */
    private Lookup getLookup(String code) {
        Lookup lookup;
        if ((lookup = lookups.get(code)) == null) {
            ILookupService lookupService
                    = LookupServiceHelper.getLookupService();
            lookup = lookupService.getLookup(
                    "lookup.customerAccountType", code);
            lookups.put(code, lookup);
        }
        return lookup;
    }

    /**
     * Helper to return the entity reference from a set.
     *
     * @param set the set
     * @return the entity
     */
    private IMObjectReference getEntity(ObjectSet set) {
        return (IMObjectReference) set.get("p.entity");
    }

    /**
     * Helper to return the act reference from a set.
     *
     * @param set the set
     * @return the ct
     */
    private IMObjectReference getAct(ObjectSet set) {
        return (IMObjectReference) set.get("p.act");
    }

    /**
     * Helper to lazily load ObjectSet values.
     */
    private class BalanceObjectSet extends ObjectSet {

        /**
         * The customer.
         */
        private final IMObjectReference customer;

        /**
         * Determines if the last payment details have been queried.
         */
        private boolean queriedLastPayment;

        /**
         * Determines if the last invoice details have been queried.
         */
        private boolean queriedLastInvoice;

        /**
         * Constructs a new <tt>BalanceObjectSet</tt>.
         *
         * @param customer the customer
         */
        public BalanceObjectSet(IMObjectReference customer) {
            this.customer = customer;
        }

        /**
         * Returns the object names, in the order they were queried.
         *
         * @return the object names
         */
        @Override
        public Set<String> getNames() {
            return NAMES;
        }

        /**
         * Returns the value of a object.
         *
         * @param name the object name
         * @return the object value. May be <code>null</code>
         */
        @Override
        public Object get(String name) {
            Object object = super.get(name);
            if (object == null) {
                if (LAST_PAYMENT_DATE.equals(name)) {
                    object = getLastPaymentDate();
                } else if (LAST_PAYMENT_AMOUNT.equals(name)) {
                    object = getLastPaymentAmount();
                } else if (LAST_INVOICE_DATE.equals(name)) {
                    object = getLastInvoiceDate();
                } else if (LAST_INVOICE_AMOUNT.equals(name)) {
                    object = getLastInvoiceAmount();
                }
            }
            return object;
        }

        /**
         * Returns the last payment date.
         *
         * @return the last payment date, or <tt>null</tt>
         */
        private Date getLastPaymentDate() {
            getLastPaymentDetails();
            return (Date) super.get(LAST_PAYMENT_DATE);
        }

        /**
         * Returns the last payment amount.
         *
         * @return the last payment amount, or <tt>null</tt>
         */
        private BigDecimal getLastPaymentAmount() {
            getLastPaymentDetails();
            return (BigDecimal) super.get(LAST_PAYMENT_AMOUNT);
        }

        /**
         * Gets the last payment details for the customer.
         */
        private void getLastPaymentDetails() {
            if (!queriedLastPayment) {
                queriedLastPayment = true;
                query("act.customerAccountPayment", LAST_PAYMENT_DATE,
                      LAST_PAYMENT_AMOUNT);
            }
        }

        /**
         * Returns the last invoice date.
         *
         * @return the last invoice date, or <tt>null</tt>
         */
        private Date getLastInvoiceDate() {
            getLastInvoiceDetails();
            return (Date) super.get(LAST_INVOICE_DATE);
        }

        /**
         * Returns the last invoice amount.
         *
         * @return the last invoice amount, or <tt>null</tt>
         */
        private BigDecimal getLastInvoiceAmount() {
            getLastInvoiceDetails();
            return (BigDecimal) super.get(LAST_INVOICE_AMOUNT);
        }

        /**
         * Gets the last invoice details.
         */
        private void getLastInvoiceDetails() {
            if (!queriedLastInvoice) {
                queriedLastInvoice = true;
                query("act.customerAccountChargesInvoice",
                      LAST_INVOICE_DATE, LAST_INVOICE_AMOUNT);
            }
        }

        /**
         * Queries the most recent date and amount for the specified act type.
         *
         * @param shortName the act short name
         * @param dateKey   the key to store the date under
         * @param amountKey the key to store the amount under
         * @return <tt>true</tt> if an act corresponding to the short name
         *         exists
         */
        private boolean query(String shortName, String dateKey,
                              String amountKey) {
            boolean found = false;
            ShortNameConstraint archetype = new ShortNameConstraint("act",
                                                                    shortName);
            ArchetypeQuery query = new ArchetypeQuery(archetype);
            CollectionNodeConstraint constraint
                    = new CollectionNodeConstraint("customer");
            constraint.add(new ObjectRefNodeConstraint("entity", customer));
            constraint.add(new ObjectRefNodeConstraint(
                    "act", new ArchetypeId(shortName)));
            // re-specify the act short name. to force utilisation of the
            // (faster) participation index. Ideally would only need to specify
            // the act short name on participations, but this isn't supported
            // by ArchetypeQuery.
            query.add(constraint);
            query.add(new NodeSortConstraint("startTime", false));
            query.add(new NodeSelectConstraint("act.startTime"));
            query.add(new NodeSelectConstraint("act.amount"));
            query.setMaxResults(1);
            Iterator<ObjectSet> iterator
                    = new ObjectSetQueryIterator(service, query);
            if (iterator.hasNext()) {
                ObjectSet set = iterator.next();
                set(dateKey, set.get("act.startTime"));
                set(amountKey, set.get("act.amount"));
                found = true;
            } else {
                set(dateKey, null);
                set(amountKey, null);
            }

            return found;
        }

        /**
         * Sets an object.
         *
         * @param name  the object name
         * @param value the object value
         * @return the object
         */
        private Object set(String name, Object value) {
            add(name, value);
            return value;
        }

    }

}
