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

import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * Query for customers with overdue balances.
 *
 * @author Tim Anderson
 */
public class OverdueBalanceQuery {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The rules.
     */
    private final CustomerAccountRules rules;

    /**
     * The date.
     */
    private Date date;

    /**
     * The from-day range.
     */
    private int from;

    /**
     * The to-day range.
     */
    private int to;

    /**
     * The customer account type classification. May be {@code null}.
     */
    private Lookup accountType;

    /**
     * Constructs an {@link OverdueBalanceQuery}.
     *
     * @param service the archetype service
     * @param rules   the customer account rules
     */
    public OverdueBalanceQuery(IArchetypeService service, CustomerAccountRules rules) {
        this.service = service;
        this.rules = rules;
        date = new Date();
    }

    /**
     * Sets the date.
     *
     * @param date the date
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Sets the from day range.
     *
     * @param from the from day range
     */
    public void setFrom(int from) {
        this.from = from;
    }

    /**
     * Sets the to-day range.
     *
     * @param to the to day range. Use <code>&lt;= 0</code> to indicate
     *           all dates
     */
    public void setTo(int to) {
        this.to = to;
    }

    /**
     * Sets the customer account type.
     *
     * @param accountType the customer account type (an instance of
     *                    <em>lookup.customerAccountType</em>).
     *                    If {@code null} indicates to query all account types.
     */
    public void setAccountType(Lookup accountType) {
        this.accountType = accountType;
    }

    /**
     * Queries all customers that have overdue balances within the nominated
     * day range past their standard terms.
     *
     * @return an iterator over the list of customers
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Iterator<Party> query() {
        OutstandingBalanceQuery query = new OutstandingBalanceQuery(service);
        query.setDebitOnly(true);
        query.setAccountType(accountType);
        Iterator<Party> iterator = query.query();
        return new OverdueBalanceIterator(iterator, date, from, to);
    }

    /**
     * Iterator over a collection of customers that only returns those
     * that have an overdue balance.
     */
    class OverdueBalanceIterator implements Iterator<Party> {

        /**
         * The iterator to delegate to.
         */
        private final Iterator<Party> iterator;

        /**
         * The date to perform overdue balance checks against.
         */
        private final Date date;

        /**
         * The from day range.
         */
        private final int from;

        /**
         * To to day range. May be <code>&lt;= 0</code> to indicate all dates
         */
        private final int to;

        /**
         * The next customer to return.
         */
        private Party next;

        /**
         * @param iterator the customer iterator
         * @param date     the date to perform overdue balance checks against
         * @param from     the from day range
         * @param to       the to day range. Use <code>&lt;= 0</code> to
         *                 indicate all dates
         */
        public OverdueBalanceIterator(Iterator<Party> iterator, Date date,
                                      int from, int to) {
            this.iterator = iterator;
            this.date = date;
            this.from = from;
            this.to = to;
        }

        /**
         * Returns {@code true} if the iteration has more elements.
         *
         * @return {@code true} if the iterator has more elements
         */
        public boolean hasNext() {
            boolean result = false;
            while (iterator.hasNext()) {
                Party party = iterator.next();
                if (rules.hasOverdueBalance(party, date, from, to)) {
                    next = party;
                    result = true;
                    break;
                }
            }
            return result;
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration.
         * @throws NoSuchElementException iteration has no more elements.
         */
        public Party next() {
            if (next == null) {
                throw new NoSuchElementException();
            }
            Party result = next;
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
    }

}
