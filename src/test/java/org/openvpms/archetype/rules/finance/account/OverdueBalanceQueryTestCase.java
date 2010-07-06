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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * Tests the {@link OverdueBalanceQuery} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OverdueBalanceQueryTestCase extends AbstractCustomerAccountTest {

    /**
     * Tests the {@link OverdueBalanceQuery#query} method.
     */
    @Test
    public void testQuery() {
        // add a 30 day payment term for accounts to the customer
        Party customer = getCustomer();
        customer.addClassification(createAccountType(30, DateUnits.DAYS));
        save(customer);

        Date startTime = java.sql.Date.valueOf("2007-1-1");

        // 60 days from saved, amount should be overdue
        Date now = DateRules.getDate(startTime, 60, DateUnits.DAYS);

        // get all customers with overdue balances
        List<Party> beforeAll = getCustomersWithOverdueBalances(now, 0, 0);

        // get all customers with balances 30 days overdue
        List<Party> before30 = getCustomersWithOverdueBalances(now, 30, 0);

        // get all customers with balances 15 days overdue
        List<Party> before15 = getCustomersWithOverdueBalances(now, 15, 0);

        // get all customers with balances between 15 and 30 days overdue
        List<Party> before15btw30 = getCustomersWithOverdueBalances(now, 15,
                                                                    30);

        // get all customers with balances between 15 and 45 days overdue
        List<Party> before15btw45 = getCustomersWithOverdueBalances(now, 15,
                                                                    45);

        // create and save a new invoice
        final Money amount = new Money(100);
        List<FinancialAct> invoice = createChargesInvoice(amount, startTime);
        save(invoice);

        // get all customers with overdue balances. Should include the customer
        List<Party> after = getCustomersWithOverdueBalances(now, 0, 0);
        assertEquals(beforeAll.size() + 1, after.size());
        assertTrue(after.contains(customer));

        // re-retrieve the list, but limit to all overdue customers overdue
        // by more than 30 days. Should exclude the customer
        after = getCustomersWithOverdueBalances(now, 30, 0);
        assertEquals(before30.size(), after.size());
        assertFalse(after.contains(customer));

        // re-retrieve the list, but limit to all overdue customers overdue
        // by more than 15 days. Should include the customer
        after = getCustomersWithOverdueBalances(now, 15, 0);
        assertEquals(before15.size() + 1, after.size());
        assertTrue(after.contains(customer));

        // re-retrieve the list, but limit to all overdue customers overdue
        // by more than 15 days but less than 30 days. Should exclude the
        // customer
        after = getCustomersWithOverdueBalances(now, 15, 30);
        assertEquals(before15btw30.size(), after.size());
        assertFalse(after.contains(customer));

        // re-retrieve the list, but limit to all overdue customers overdue
        // by more than 15 days but less than 45 days. Should include the
        // customer
        after = getCustomersWithOverdueBalances(now, 15, 45);
        assertEquals(before15btw45.size() + 1, after.size());
        assertTrue(after.contains(customer));

        // pay the bill and verify the customer is no longer returned
        FinancialAct payment = createPayment(amount);
        save(payment);

        after = getCustomersWithOverdueBalances(now, 0, 0);
        assertEquals(beforeAll.size(), after.size());
        assertFalse(after.contains(customer));
    }

    /**
     * Returns a list of customers with overdue balances.
     *
     * @param date the date
     * @param from the from day range
     * @param to   the to day range. Use <code>&lt;= 0</code> to indicate
     *             all dates
     * @return a list of customers with overdue balances
     */
    private List<Party> getCustomersWithOverdueBalances(Date date, int from,
                                                        int to) {
        List<Party> result = new ArrayList<Party>();
        OverdueBalanceQuery query = new OverdueBalanceQuery();
        query.setDate(date);
        query.setFrom(from);
        query.setTo(to);
        Iterator<Party> iterator = query.query();
        while (iterator.hasNext()) {
            result.add(iterator.next());
        }
        return result;
    }

}
