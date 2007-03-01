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

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.component.system.common.query.ShortNameConstraint;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
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
     * The customer.
     */
    public static final String CUSTOMER = "customer";

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
     * The customers.
     */
    private final Iterator<Party> customers;

    /**
     * The date.
     */
    private final Date date;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The rules.
     */
    private final CustomerBalanceRules rules;

    private static final Set<String> NAMES;

    static {
        NAMES = new LinkedHashSet<String>();
        NAMES.add(CUSTOMER);
        NAMES.add(BALANCE);
        NAMES.add(OVERDUE_BALANCE);
        NAMES.add(CREDIT_BALANCE);
        NAMES.add(LAST_PAYMENT_DATE);
        NAMES.add(LAST_PAYMENT_AMOUNT);
        NAMES.add(LAST_INVOICE_DATE);
        NAMES.add(LAST_INVOICE_AMOUNT);
    }

    /**
     * Constructs a new <tt>CustomerBalanceSummaryQuery</tt>.
     *
     * @param customers an iterator over the customers
     * @param date      the date
     */
    public CustomerBalanceSummaryQuery(Iterator<Party> customers, Date date) {
        this(customers, date, ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Constructs a new <code>CustomerBalanceSummaryQuery</code>.
     *
     * @param customers an iterator over the customers
     * @param date      the date
     * @param service   the archetype service
     */
    public CustomerBalanceSummaryQuery(Iterator<Party> customers, Date date,
                                       IArchetypeService service) {
        this.customers = customers;
        this.date = date;
        this.service = service;
        rules = new CustomerBalanceRules(service);
    }

    /**
     * Returns <tt>true</tt> if the iteration has more elements.
     *
     * @return <tt>true</tt> if the iterator has more elements.
     */
    public boolean hasNext() {
        return customers.hasNext();
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
        Party customer = customers.next();
        return new BalanceObjectSet(customer);
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
     * Helper to lazily load ObjectSet values.
     */
    private class BalanceObjectSet extends ObjectSet {

        /**
         * The customer.
         */
        private final Party customer;

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
        public BalanceObjectSet(Party customer) {
            this.customer = customer;
            add(CUSTOMER, customer);
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
                if (BALANCE.equals(name)) {
                    object = set(name, rules.getBalance(customer));
                } else if (OVERDUE_BALANCE.equals(name)) {
                    object = set(name, rules.getOverdueBalance(customer, date));
                } else if (CREDIT_BALANCE.equals(name)) {
                    object = set(name, rules.getCreditBalance(customer));
                } else if (LAST_PAYMENT_DATE.equals(name)) {
                    object = getLastPaymentDate();
                } else if (LAST_PAYMENT_AMOUNT.equals(name)) {
                    object = getLastPaymentAmount();
                } else if (LAST_INVOICE_DATE.equals(name)) {
                    object = getLastInvoiceDate();
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
            if (getLastPaymentDetails()) {
                return (Date) super.get(LAST_PAYMENT_DATE);
            }
            return null;
        }

        /**
         * Returns the last payment amount.
         *
         * @return the last payment amount, or <tt>null</tt>
         */
        private BigDecimal getLastPaymentAmount() {
            if (getLastPaymentDetails()) {
                return (BigDecimal) super.get(LAST_PAYMENT_AMOUNT);
            }
            return null;
        }

        /**
         * Gets the last payment details for the customer.
         *
         * @return <tt>true</tt> if the details could be found
         */
        private boolean getLastPaymentDetails() {
            if (!queriedLastPayment) {
                queriedLastPayment = true;
                return query("act.customerAccountPayment", LAST_PAYMENT_DATE,
                             LAST_PAYMENT_AMOUNT);
            }
            return false;
        }

        /**
         * Returns the last invoice date.
         *
         * @return the last invoice date, or <tt>null</tt>
         */
        private Date getLastInvoiceDate() {
            Date result = null;
            if (!queriedLastInvoice) {
                queriedLastInvoice = true;
                if (query("act.customerAccountChargesInvoice",
                          LAST_INVOICE_DATE, LAST_INVOICE_AMOUNT)) {
                    result = (Date) super.get(LAST_INVOICE_DATE);
                }
            }
            return result;
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
            constraint.add(new ObjectRefNodeConstraint(
                    "entity", customer.getObjectReference()));
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
