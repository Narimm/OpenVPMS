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

import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * Query for customers with overdue balances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OverdueBalanceQuery {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The rules.
     */
    private final CustomerBalanceRules rules;


    /**
     * Creates a new <code>OverdueBalanceQuery</code>.
     */
    public OverdueBalanceQuery() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Creates a new <code>OverdueBalanceQuery</code>.
     *
     * @param service the archetype service
     */
    public OverdueBalanceQuery(IArchetypeService service) {
        this.service = service;
        this.rules = new CustomerBalanceRules(service);
    }

    /**
     * Queries all customers that have overdue balances within the nominated
     * day range past their standard terms.
     *
     * @param date the date
     * @param from the from day range
     * @param to   the to day range. Use <code>&lt;= 0</code> to indicate
     *             all dates
     * @return an iterator over the list of customers
     */
    public Iterator<Party> query(Date date, int from, int to) {
        OutstandingBalanceQuery query = new OutstandingBalanceQuery(service);
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
         * Returns <tt>true</tt> if the iteration has more elements.
         *
         * @return <tt>true</tt> if the iterator has more elements
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
