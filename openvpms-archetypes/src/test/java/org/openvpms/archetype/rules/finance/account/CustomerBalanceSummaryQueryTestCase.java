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
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ObjectSet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


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
        FinancialAct invoice = createChargesInvoice(hundred, startTime);
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
        FinancialAct invoice = createChargesInvoice(amount, startTime);
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
     * Verifies that balances are returned for customer ranges.
     */
    public void testQueryByCustomerRange() {
        // create some customers with names starting with A, B and Z
        Party customerA = TestHelper.createCustomer(
                "Foo", "A" + System.currentTimeMillis(), true);
        Lookup accountType = createAccountType(30, DateUnits.DAYS);
        customerA.addClassification(accountType);
        save(customerA);

        Party customerB = TestHelper.createCustomer(
                "Foo", "B" + System.currentTimeMillis(), true);
        Party customerZ = TestHelper.createCustomer(
                "Foo", "Z" + System.currentTimeMillis(), true);

        Set<Party> customers = new HashSet<Party>();
        customers.add(customerA);
        customers.add(customerB);
        customers.add(customerZ);

        // save invoices for each of the customers
        FinancialAct invoice1 = createChargesInvoice(new Money(100), customerA);
        FinancialAct invoice2 = createChargesInvoice(new Money(100), customerB);
        FinancialAct invoice3 = createChargesInvoice(new Money(100), customerZ);
        save(invoice1);
        save(invoice2);
        save(invoice3);

        // check queries
        CustomerBalanceSummaryQuery query1 = new CustomerBalanceSummaryQuery(
                new Date(), null, customerA.getName(), null);
        CustomerBalanceSummaryQuery query2 = new CustomerBalanceSummaryQuery(
                new Date(), null, customerA.getName(), customerZ.getName());
        CustomerBalanceSummaryQuery query3 = new CustomerBalanceSummaryQuery(
                new Date(), null, customerB.getName(), customerZ.getName());
        CustomerBalanceSummaryQuery query4 = new CustomerBalanceSummaryQuery(
                new Date(), accountType, customerA.getName(), null);
        CustomerBalanceSummaryQuery query5 = new CustomerBalanceSummaryQuery(
                new Date(), accountType, customerA.getName(),
                customerZ.getName());
        CustomerBalanceSummaryQuery query6 = new CustomerBalanceSummaryQuery(
                new Date(), accountType, customerB.getName(),
                customerZ.getName());

        checkCustomers(query1, customers, customerA, customerB, customerZ);
        checkCustomers(query2, customers, customerA, customerB, customerZ);
        checkCustomers(query3, customers, customerB, customerZ);
        checkCustomers(query4, customers, customerA);
        checkCustomers(query5, customers, customerA);
        checkCustomers(query6, customers);

        // check wildcard queries
        // NOTE: for reasons not immediately clear, the <= operator for
        // values containing % appears to operate as a <. E.g, compare query2
        // with wildcard2.
        CustomerBalanceSummaryQuery wildcard1 = new CustomerBalanceSummaryQuery(
                new Date(), null, "A*", null);
        CustomerBalanceSummaryQuery wildcard2 = new CustomerBalanceSummaryQuery(
                new Date(), null, "A*", "Z*");
        CustomerBalanceSummaryQuery wildcard3 = new CustomerBalanceSummaryQuery(
                new Date(), null, "B*", "Z*");
        CustomerBalanceSummaryQuery wildcard4 = new CustomerBalanceSummaryQuery(
                new Date(), accountType, "A*", null);
        CustomerBalanceSummaryQuery wildcard5 = new CustomerBalanceSummaryQuery(
                new Date(), accountType, "A*", "Z*");
        CustomerBalanceSummaryQuery wildcard6 = new CustomerBalanceSummaryQuery(
                new Date(), accountType, "B*", "Z*");

        checkCustomers(wildcard1, customers, customerA, customerB, customerZ);
        checkCustomers(wildcard2, customers, customerA, customerB);
        checkCustomers(wildcard3, customers, customerB);
        checkCustomers(wildcard4, customers, customerA);
        checkCustomers(wildcard5, customers, customerA);
        checkCustomers(wildcard6, customers);
    }

    /**
     * Verifies that credit balances can be excluded.
     */
    public void testExcludeCreditBalance() {
        Party customer1 = getCustomer();
        Party customer2 = TestHelper.createCustomer();

        Date startTime = java.sql.Date.valueOf("2007-1-1");

        // create and save a new invoice for customer1
        final Money hundred = new Money(100);
        FinancialAct invoice = createChargesInvoice(hundred, customer1);
        invoice.setActivityStartTime(startTime);
        save(invoice);

        // create and save a new payment for customer2. Will leave a credit
        // balance.
        FinancialAct payment = createPayment(hundred, customer2);
        payment.setActivityStartTime(startTime);
        save(payment);

        Date now = new Date();

        // verify the credit balance is included
        CustomerBalanceSummaryQuery includeBalanceQuery
                = new CustomerBalanceSummaryQuery(now, true, 0, 0, false,
                                                  null, null, null);
        Map<IMObjectReference, ObjectSet> sets = getSets(includeBalanceQuery);
        Set<IMObjectReference> customers = sets.keySet();
        assertTrue(customers.contains(customer1.getObjectReference()));
        assertTrue(customers.contains(customer2.getObjectReference()));

        // verify the credit balance is excluded
        CustomerBalanceSummaryQuery excludeBalanceQuery
                = new CustomerBalanceSummaryQuery(now, true, 0, 0, true,
                                                  null, null, null);

        sets = getSets(excludeBalanceQuery);
        customers = sets.keySet();
        assertTrue(customers.contains(customer1.getObjectReference()));
        assertFalse(customers.contains(customer2.getObjectReference()));
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

    /**
     * Verifies that customers appear in the query results.
     *
     * @param query     the query to check
     * @param customers the customers
     * @param expected  the expected customers
     */
    private void checkCustomers(CustomerBalanceSummaryQuery query,
                                Set<Party> customers, Party ... expected) {
        Set<IMObjectReference> customerSet = new HashSet<IMObjectReference>();
        Set<IMObjectReference> expectedSet = new HashSet<IMObjectReference>();
        Set<IMObjectReference> found = new HashSet<IMObjectReference>();
        for (Party customer : customers) {
            customerSet.add(customer.getObjectReference());
        }
        for (Party customer : expected) {
            expectedSet.add(customer.getObjectReference());
        }
        Map<IMObjectReference, ObjectSet> sets = getSets(query);
        for (IMObjectReference ref : sets.keySet()) {
            if (expectedSet.contains(ref)) {
                found.add(ref);
            } else if (customerSet.contains(ref)) {
                fail("Found customer not expected in balance results");
            }
        }
        assertEquals(expected.length, found.size());
    }

    private Map<IMObjectReference, ObjectSet> getSets(
            CustomerBalanceSummaryQuery query) {
        Map<IMObjectReference, ObjectSet> result
                = new HashMap<IMObjectReference, ObjectSet>();
        while (query.hasNext()) {
            ObjectSet set = query.next();
            IMObjectReference ref = (IMObjectReference) set.get(
                    CustomerBalanceSummaryQuery.CUSTOMER_REFERENCE);
            result.put(ref, set);
        }
        return result;
    }

}
