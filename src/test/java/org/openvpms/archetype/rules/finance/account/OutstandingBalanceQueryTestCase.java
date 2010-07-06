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
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * Tests the {@link OutstandingBalanceQuery} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OutstandingBalanceQueryTestCase extends AbstractCustomerAccountTest {

    /**
     * Tests the {@link OutstandingBalanceQuery#query} method.
     */
    @Test
    public void testQuery() {
        Party customer = getCustomer();
        List<Party> before = getCustomersWithOutstandingBalances();

        // add a 30 day payment term for accounts to the customer
        customer.addClassification(createAccountType(30, DateUnits.DAYS));
        save(customer);

        // create and save a new invoice
        final Money amount = new Money(100);
        Date startTime = java.sql.Date.valueOf("2007-1-1");
        List<FinancialAct> invoice = createChargesInvoice(amount, startTime);
        save(invoice);

        List<Party> after = getCustomersWithOutstandingBalances();
        assertEquals(before.size() + 1, after.size());
        assertTrue(after.contains(customer));

        // pay the bill and verify the customer is no longer returned
        FinancialAct payment = createPayment(amount);
        save(payment);

        List<Party> now = getCustomersWithOutstandingBalances();
        assertEquals(before.size(), now.size());
        assertFalse(now.contains(customer));
    }

    /**
     * Tests the {@link OutstandingBalanceQuery#query} method when used
     * in conjunction with
     * {@link OutstandingBalanceQuery#setAccountType(Lookup)}
     */
    @Test
    public void testQueryWithAccountType() {
        Lookup accountType1 = createAccountType(30, DateUnits.DAYS);
        Lookup accountType2 = createAccountType(60, DateUnits.DAYS);

        // verify no customers returned
        OutstandingBalanceQuery query = new OutstandingBalanceQuery();
        query.setAccountType(accountType1);
        assertFalse(query.query().hasNext());

        // add the account type to the customer
        Party customer = getCustomer();
        customer.addClassification(accountType1);
        save(customer);

        // verify no customers returned by query
        assertFalse(query.query().hasNext());

        // create and save a new invoice
        final Money amount = new Money(100);
        Date startTime = java.sql.Date.valueOf("2007-1-1");
        List<FinancialAct> invoice = createChargesInvoice(amount, startTime);
        save(invoice);

        // verify the customer is now returned
        Iterator<Party> iter = query.query();
        assertTrue(iter.hasNext());
        assertEquals(customer, iter.next());
        assertFalse(iter.hasNext());

        query.setAccountType(accountType2);
        assertFalse(query.query().hasNext());
    }

    /**
     * Returns all customers with outstanding balances.
     *
     * @return a list of all customers with outstanding balances
     */
    private List<Party> getCustomersWithOutstandingBalances() {
        List<Party> result = new ArrayList<Party>();
        OutstandingBalanceQuery query = new OutstandingBalanceQuery();
        Iterator<Party> iterator = query.query();
        while (iterator.hasNext()) {
            result.add(iterator.next());
        }
        return result;
    }

}
