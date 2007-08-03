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

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ObjectSet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Tests the {@link CustomerBalanceSummaryQuery} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerBalanceSummaryQueryTestCase
        extends AbstractCustomerAccountTest {

    /**
     * Tests the {@link CustomerBalanceSummaryQuery} class.
     */
    public void testQuery() {
        // add a 30 day payment term for accounts to the customer
        Party customer = getCustomer();
        Lookup accountType = createAccountType(30, DateUnits.DAYS);
        customer.addClassification(accountType);
        save(customer);

        Date startTime = java.sql.Date.valueOf("2007-1-1");

        // 60 days from saved, amount should be overdue
        Date now = DateRules.getDate(startTime, 60, DateUnits.DAYS);

        // create and save a new invoice
        final Money hundred = new Money(100);
        FinancialAct invoice = createChargesInvoice(hundred);
        invoice.setActivityStartTime(startTime);
        invoice.setStatus(ActStatus.POSTED);
        save(invoice);

        // pay half the invoice
        Date paymentStartTime = DateRules.getDate(startTime, 1, DateUnits.DAYS);
        final Money fifty = new Money(50);
        FinancialAct payment = createPayment(fifty);
        payment.setActivityStartTime(paymentStartTime);
        payment.setStatus(ActStatus.POSTED);
        save(payment);

        CustomerBalanceSummaryQuery query
                = new CustomerBalanceSummaryQuery(now);
        assertTrue(query.hasNext());
        ObjectSet set = null;
        while (query.hasNext()) {
            ObjectSet tmp = query.next();
            IMObjectReference ref = (IMObjectReference) tmp.get(
                    CustomerBalanceSummaryQuery.CUSTOMER_REFERENCE);
            if (customer.getObjectReference().equals(ref)) {
                set = tmp;
                break;
            }
        }
        assertNotNull(set);

        BigDecimal balance = (BigDecimal) set.get(
                CustomerBalanceSummaryQuery.BALANCE);
        BigDecimal overdue = (BigDecimal) set.get(
                CustomerBalanceSummaryQuery.OVERDUE_BALANCE);
        BigDecimal credit = (BigDecimal) set.get(
                CustomerBalanceSummaryQuery.CREDIT_BALANCE);
        Date paymentDate = (Date) set.get(
                CustomerBalanceSummaryQuery.LAST_PAYMENT_DATE);
        BigDecimal paymentAmount = (BigDecimal)
                set.get(CustomerBalanceSummaryQuery.LAST_PAYMENT_AMOUNT);
        Date invoiceDate = (Date) set.get(
                CustomerBalanceSummaryQuery.LAST_INVOICE_DATE);
        BigDecimal invoiceAmount = (BigDecimal)
                set.get(CustomerBalanceSummaryQuery.LAST_INVOICE_AMOUNT);

        checkEquals(fifty, balance);
        checkEquals(fifty, overdue);
        checkEquals(BigDecimal.ZERO, credit);
        assertEquals(paymentStartTime, paymentDate);
        checkEquals(fifty, paymentAmount);
        assertEquals(startTime, invoiceDate);
        checkEquals(hundred, invoiceAmount);

        assertFalse(query.hasNext());
    }

    /**
     * Tests the querying by individual account types.
     */
    public void testQueryByAccountType() {
        // add a 30 day payment term for accounts to the customer
        Party customer = getCustomer();
        Lookup accountType1 = createAccountType(30, DateUnits.DAYS);
        Lookup accountType2 = createAccountType(30, DateUnits.DAYS);
        customer.addClassification(accountType1);
        save(customer);

        Date startTime = java.sql.Date.valueOf("2007-1-1");

        // create and save a new invoice
        final Money hundred = new Money(100);
        FinancialAct invoice = createChargesInvoice(hundred);
        invoice.setActivityStartTime(startTime);
        invoice.setStatus(ActStatus.POSTED);
        save(invoice);

        // verify there is 1 act for accountType1
        CustomerBalanceSummaryQuery query
                = new CustomerBalanceSummaryQuery(new Date(), accountType1);
        checkSummaries(1, query);

        // verify there is 0 acts for accountType2
        CustomerBalanceSummaryQuery query2
                = new CustomerBalanceSummaryQuery(new Date(), accountType2);
        checkSummaries(0, query2);
    }


    /**
     * Tests the balance summary query for customers with overdue balances.
     */
    public void testOverdueQuery() {
        // add a 30 day payment term for accounts to the customer
        Party customer = getCustomer();
        IMObjectReference custRef = customer.getObjectReference();
        customer.addClassification(createAccountType(30, DateUnits.DAYS));
        save(customer);

        Date startTime = java.sql.Date.valueOf("2007-1-1");

        // 60 days from saved, amount should be overdue
        Date now = DateRules.getDate(startTime, 60, DateUnits.DAYS);

        // get all customers with overdue balances
        List<IMObjectReference> beforeAll
                = getCustomersWithOverdueBalances(now, 0, 0);

        // get all customers with balances 30 days overdue
        List<IMObjectReference> before30 = getCustomersWithOverdueBalances(
                now, 30, 0);

        // get all customers with balances 15 days overdue
        List<IMObjectReference> before15 = getCustomersWithOverdueBalances(
                now, 15, 0);

        // get all customers with balances between 15 and 30 days overdue
        List<IMObjectReference> before15btw30 = getCustomersWithOverdueBalances(
                now, 15, 30);

        // get all customers with balances between 15 and 45 days overdue
        List<IMObjectReference> before15btw45 = getCustomersWithOverdueBalances(
                now, 15, 45);

        // create and save a new invoice
        final Money amount = new Money(100);
        FinancialAct invoice = createChargesInvoice(amount);
        invoice.setActivityStartTime(startTime);
        save(invoice);

        // get all customers with overdue balances. Should include the customer
        List<IMObjectReference> after = getCustomersWithOverdueBalances(now, 0,
                                                                        0);
        assertEquals(beforeAll.size() + 1, after.size());
        assertTrue(after.contains(custRef));

        // re-retrieve the list, but limit to all overdue customers overdue
        // by more than 30 days. Should exclude the customer
        after = getCustomersWithOverdueBalances(now, 30, 0);
        assertEquals(before30.size(), after.size());
        assertFalse(after.contains(custRef));

        // re-retrieve the list, but limit to all overdue customers overdue
        // by more than 15 days. Should include the customer
        after = getCustomersWithOverdueBalances(now, 15, 0);
        assertEquals(before15.size() + 1, after.size());
        assertTrue(after.contains(custRef));

        // re-retrieve the list, but limit to all overdue customers overdue
        // by more than 15 days but less than 30 days. Should exclude the
        // customer
        after = getCustomersWithOverdueBalances(now, 15, 30);
        assertEquals(before15btw30.size(), after.size());
        assertFalse(after.contains(custRef));

        // re-retrieve the list, but limit to all overdue customers overdue
        // by more than 15 days but less than 45 days. Should include the
        // customer
        after = getCustomersWithOverdueBalances(now, 15, 45);
        assertEquals(before15btw45.size() + 1, after.size());
        assertTrue(after.contains(custRef));

        // pay the bill and verify the customer is no longer returned
        FinancialAct payment = createPayment(amount);
        save(payment);

        after = getCustomersWithOverdueBalances(now, 0, 0);
        assertEquals(beforeAll.size(), after.size());
        assertFalse(after.contains(custRef));
    }

    /**
     * Checks the no. of summaries for a query.
     *
     * @param expected the expected count
     * @param query    the query
     */
    private void checkSummaries(int expected,
                                CustomerBalanceSummaryQuery query) {
        int count = 0;
        while (query.hasNext()) {
            ++count;
            query.next();
        }
        assertEquals(expected, count);
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
    private List<IMObjectReference> getCustomersWithOverdueBalances(Date date,
                                                                    int from,
                                                                    int to) {
        List<IMObjectReference> result = new ArrayList<IMObjectReference>();
        CustomerBalanceSummaryQuery query = new CustomerBalanceSummaryQuery(
                date, from, to, null);
        while (query.hasNext()) {
            ObjectSet set = query.next();
            IMObjectReference ref = (IMObjectReference) set.get(
                    CustomerBalanceSummaryQuery.CUSTOMER_REFERENCE);
            result.add(ref);
        }
        return result;
    }


}
