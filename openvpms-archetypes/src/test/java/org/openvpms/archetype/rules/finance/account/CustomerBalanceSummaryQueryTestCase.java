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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.finance.account;

import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.practice.Location;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.query.ObjectSet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.openvpms.archetype.test.TestHelper.getDate;

/**
 * Tests the {@link CustomerBalanceSummaryQuery} class.
 *
 * @author Tim Anderson
 */
public class CustomerBalanceSummaryQueryTestCase extends AbstractCustomerAccountTest {

    /**
     * Tests the {@link CustomerBalanceSummaryQuery} class.
     */
    @Test
    public void testQuery() {
        // add a 30 day payment term for accounts to the customer
        Party customer = getCustomer();
        Lookup accountType = createAccountType(30, DateUnits.DAYS);
        customer.addClassification(accountType);
        save(customer);

        Date startTime = getDate("2007-01-01");

        // 60 days from saved, amount should be overdue
        Date now = DateRules.getDate(startTime, 60, DateUnits.DAYS);

        // create and save a new invoice
        final Money hundred = new Money(100);
        List<FinancialAct> invoice = createChargesInvoice(hundred, startTime);
        save(invoice);

        // pay half the invoice
        Date paymentStartTime = DateRules.getDate(startTime, 1, DateUnits.DAYS);
        final Money fifty = new Money(50);
        FinancialAct payment = createPayment(fifty);
        payment.setActivityStartTime(paymentStartTime);
        payment.setStatus(ActStatus.POSTED);
        save(payment);

        CustomerBalanceSummaryQuery query = new CustomerBalanceSummaryQuery(now, getArchetypeService(),
                                                                            getLookupService(), getRules());
        assertTrue(query.hasNext());
        ObjectSet set = null;
        while (query.hasNext()) {
            ObjectSet tmp = query.next();
            IMObjectReference ref = tmp.getReference(CustomerBalanceSummaryQuery.CUSTOMER_REFERENCE);
            if (customer.getObjectReference().equals(ref)) {
                set = tmp;
                break;
            }
        }
        assertNotNull(set);

        BigDecimal balance = set.getBigDecimal(CustomerBalanceSummaryQuery.BALANCE);
        BigDecimal overdue = set.getBigDecimal(CustomerBalanceSummaryQuery.OVERDUE_BALANCE);
        BigDecimal credit = set.getBigDecimal(CustomerBalanceSummaryQuery.CREDIT_BALANCE);
        Date paymentDate = set.getDate(CustomerBalanceSummaryQuery.LAST_PAYMENT_DATE);
        BigDecimal paymentAmount = set.getBigDecimal(CustomerBalanceSummaryQuery.LAST_PAYMENT_AMOUNT);
        Date invoiceDate = set.getDate(CustomerBalanceSummaryQuery.LAST_INVOICE_DATE);
        BigDecimal invoiceAmount = set.getBigDecimal(CustomerBalanceSummaryQuery.LAST_INVOICE_AMOUNT);

        checkEquals(fifty, balance);
        checkEquals(fifty, overdue);
        checkEquals(BigDecimal.ZERO, credit);
        assertEquals(paymentStartTime, paymentDate);
        checkEquals(fifty, paymentAmount);
        assertEquals(startTime, invoiceDate);
        checkEquals(hundred, invoiceAmount);
    }

    /**
     * Tests the querying by individual account types.
     */
    @Test
    public void testQueryByAccountType() {
        IArchetypeService service = getArchetypeService();
        ILookupService lookups = getLookupService();
        CustomerAccountRules rules = getRules();

        // add a 30 day payment term for accounts to the customer
        Party customer = getCustomer();
        Lookup accountType1 = createAccountType(30, DateUnits.DAYS);
        Lookup accountType2 = createAccountType(30, DateUnits.DAYS);
        customer.addClassification(accountType1);
        save(customer);

        Date startTime = getDate("2007-01-01");

        // create and save a new invoice
        final Money hundred = new Money(100);
        List<FinancialAct> invoice = createChargesInvoice(hundred, startTime);
        save(invoice);

        // verify there is 1 act for accountType1
        CustomerBalanceSummaryQuery query = new CustomerBalanceSummaryQuery(new Date(), accountType1, service, lookups,
                                                                            rules);
        checkSummaries(1, query);

        // verify there is 0 acts for accountType2
        CustomerBalanceSummaryQuery query2 = new CustomerBalanceSummaryQuery(new Date(), accountType2, service, lookups,
                                                                             rules);
        checkSummaries(0, query2);
    }

    /**
     * Verifies that customer balances can be queried by a customer's practice location.
     */
    @Test
    public void testQueryByLocation() {
        Party location1 = TestHelper.createLocation();
        Party location2 = TestHelper.createLocation();

        Party customer1 = TestHelper.createCustomer(location1);
        Party customer2 = TestHelper.createCustomer(location2);
        Party customer3 = TestHelper.createCustomer();

        Set<Party> customers = new HashSet<Party>();
        customers.add(customer1);
        customers.add(customer2);
        customers.add(customer3);

        // save invoices for each of the customers
        List<FinancialAct> invoice1 = createChargesInvoice(new Money(100), customer1);
        List<FinancialAct> invoice2 = createChargesInvoice(new Money(100), customer2);
        List<FinancialAct> invoice3 = createChargesInvoice(new Money(100), customer3);
        save(invoice1);
        save(invoice2);
        save(invoice3);

        Date date = new Date();
        // check queries
        checkLocation(date, Location.ALL, customers, customer1, customer2, customer3);
        checkLocation(date, Location.NONE, customers, customer3);
        checkLocation(date, new Location(location1), customers, customer1);
        checkLocation(date, new Location(location2), customers, customer2);
    }

    /**
     * Tests the balance summary query for customers with overdue balances.
     */
    @Test
    public void testOverdueQuery() {
        // add a 30 day payment term for accounts to the customer
        Party customer = getCustomer();
        IMObjectReference custRef = customer.getObjectReference();
        customer.addClassification(createAccountType(30, DateUnits.DAYS));
        save(customer);

        Date startTime = getDate("2007-01-01");

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
        List<FinancialAct> invoice = createChargesInvoice(amount, startTime);
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
    @Test
    public void testQueryByCustomerRange() {
        IArchetypeService service = getArchetypeService();
        ILookupService lookups = getLookupService();
        CustomerAccountRules rules = getRules();

        // create some customers with names starting with A, B and Z
        Party customerA = TestHelper.createCustomer("Foo", "A" + System.currentTimeMillis(), true);
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
        List<FinancialAct> invoice1 = createChargesInvoice(new Money(100),
                                                           customerA);
        List<FinancialAct> invoice2 = createChargesInvoice(new Money(100),
                                                           customerB);
        List<FinancialAct> invoice3 = createChargesInvoice(new Money(100),
                                                           customerZ);
        save(invoice1);
        save(invoice2);
        save(invoice3);

        // check queries
        CustomerBalanceSummaryQuery query1 = new CustomerBalanceSummaryQuery(
                new Date(), null, customerA.getName(), null, service, lookups, rules);
        CustomerBalanceSummaryQuery query2 = new CustomerBalanceSummaryQuery(
                new Date(), null, customerA.getName(), customerZ.getName(), service, lookups, rules);
        CustomerBalanceSummaryQuery query3 = new CustomerBalanceSummaryQuery(
                new Date(), null, customerB.getName(), customerZ.getName(), service, lookups, rules);
        CustomerBalanceSummaryQuery query4 = new CustomerBalanceSummaryQuery(
                new Date(), accountType, customerA.getName(), null, service, lookups, rules);
        CustomerBalanceSummaryQuery query5 = new CustomerBalanceSummaryQuery(
                new Date(), accountType, customerA.getName(), customerZ.getName(), service, lookups, rules);
        CustomerBalanceSummaryQuery query6 = new CustomerBalanceSummaryQuery(
                new Date(), accountType, customerB.getName(), customerZ.getName(), service, lookups, rules);

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
        CustomerBalanceSummaryQuery wildcard1
                = new CustomerBalanceSummaryQuery(new Date(), null, "A*", null, service, lookups, rules);
        CustomerBalanceSummaryQuery wildcard2
                = new CustomerBalanceSummaryQuery(new Date(), null, "A*", "Z*", service, lookups, rules);
        CustomerBalanceSummaryQuery wildcard3
                = new CustomerBalanceSummaryQuery(new Date(), null, "B*", "Z*", service, lookups, rules);
        CustomerBalanceSummaryQuery wildcard4
                = new CustomerBalanceSummaryQuery(new Date(), accountType, "A*", null, service, lookups, rules);
        CustomerBalanceSummaryQuery wildcard5
                = new CustomerBalanceSummaryQuery(new Date(), accountType, "A*", "Z*", service, lookups, rules);
        CustomerBalanceSummaryQuery wildcard6
                = new CustomerBalanceSummaryQuery(new Date(), accountType, "B*", "Z*", service, lookups, rules);

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
    @Test
    public void testExcludeCreditBalance() {
        CustomerAccountRules rules = getRules();
        ILookupService lookups = getLookupService();
        Party customer1 = getCustomer();
        Party customer2 = TestHelper.createCustomer();

        Date startTime = getDate("2007-01-01");

        // create and save a new invoice for customer1
        final Money hundred = new Money(100);
        List<FinancialAct> invoice
                = createChargesInvoice(hundred, customer1, startTime);
        save(invoice);

        // create and save a new payment for customer2. Will leave a credit
        // balance.
        FinancialAct payment = createPayment(hundred, customer2);
        payment.setActivityStartTime(startTime);
        save(payment);

        Date now = new Date();

        // verify the credit balance is included
        IArchetypeService service = getArchetypeService();
        CustomerBalanceSummaryQuery includeBalanceQuery
                = new CustomerBalanceSummaryQuery(now, true, 0, 0, false, null, null, null, Location.ALL,
                                                  service, lookups, rules);
        Map<IMObjectReference, ObjectSet> sets = getSets(includeBalanceQuery);
        Set<IMObjectReference> customers = sets.keySet();
        assertTrue(customers.contains(customer1.getObjectReference()));
        assertTrue(customers.contains(customer2.getObjectReference()));

        // verify the credit balance is excluded
        CustomerBalanceSummaryQuery excludeBalanceQuery
                = new CustomerBalanceSummaryQuery(now, true, 0, 0, true, null, null, null, Location.ALL,
                                                  service, lookups, rules);

        sets = getSets(excludeBalanceQuery);
        customers = sets.keySet();
        assertTrue(customers.contains(customer1.getObjectReference()));
        assertFalse(customers.contains(customer2.getObjectReference()));
    }

    /**
     * Verifies that customers with the same name have their balances correctly
     * calculated.
     */
    @Test
    public void testForSameName() {
        IArchetypeService service = getArchetypeService();
        ILookupService lookups = getLookupService();
        CustomerAccountRules rules = getRules();
        Party customer1 = getCustomer();
        long id = System.currentTimeMillis();
        Party customer2 = TestHelper.createCustomer();
        setName(customer1, id);
        setName(customer2, id);

        BigDecimal cust1Balance = BigDecimal.ZERO;
        BigDecimal cust2Balance = BigDecimal.ZERO;

        Date startTime = getDate("2007-01-01");

        // create and save invoices for customer1
        final Money hundred = new Money(100);
        for (int i = 0; i < 10; ++i) {
            List<FinancialAct> invoice1 = createChargesInvoice(hundred, customer1, startTime);
            save(invoice1);
            cust1Balance = cust1Balance.add(hundred);
        }
        // create and save invoices for customer2
        for (int i = 0; i < 12; ++i) {
            List<FinancialAct> invoice2 = createChargesInvoice(hundred, customer2, startTime);
            save(invoice2);
            cust2Balance = cust2Balance.add(hundred);
        }

        Date now = new Date();

        // verify the query returns two sets, one for each customer, with
        // the correct balance
        CustomerBalanceSummaryQuery query
                = new CustomerBalanceSummaryQuery(now, null, customer1.getName(), customer1.getName(), service, lookups,
                                                  rules);
        Map<IMObjectReference, ObjectSet> sets = getSets(query);
        assertEquals(2, sets.size());
        ObjectSet cust1Set = sets.get(customer1.getObjectReference());
        ObjectSet cust2Set = sets.get(customer2.getObjectReference());
        assertNotNull(cust1Set);
        assertNotNull(cust2Set);

        checkEquals(cust1Balance, cust1Set.getBigDecimal(CustomerBalanceSummaryQuery.BALANCE));
        checkEquals(cust2Balance, cust2Set.getBigDecimal(CustomerBalanceSummaryQuery.BALANCE));
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
     * Verifies that the customers returned by {@link CustomerBalanceSummaryQuery} are those expected for the
     * specified date and {@link Location}.
     *
     * @param date      the date
     * @param location  the practice location(s) to query
     * @param customers the test customers
     * @param expected  the expected customers
     */
    private void checkLocation(Date date, Location location, Set<Party> customers, Party... expected) {
        CustomerBalanceSummaryQuery query = new CustomerBalanceSummaryQuery(
                date, null, null, null, location, getArchetypeService(), getLookupService(), getRules());
        checkCustomers(query, customers, expected);
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
    private List<IMObjectReference> getCustomersWithOverdueBalances(Date date, int from, int to) {
        List<IMObjectReference> result = new ArrayList<IMObjectReference>();
        CustomerAccountRules rules = getRules();
        CustomerBalanceSummaryQuery query = new CustomerBalanceSummaryQuery(date, from, to, null, getArchetypeService(),
                                                                            getLookupService(), rules);
        while (query.hasNext()) {
            ObjectSet set = query.next();
            IMObjectReference ref = set.getReference(CustomerBalanceSummaryQuery.CUSTOMER_REFERENCE);
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
    private void checkCustomers(CustomerBalanceSummaryQuery query, Set<Party> customers, Party... expected) {
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

    /**
     * Returns the sets from a query keyed on customer reference.
     * Fails if the are multiple balances for the one customer.
     *
     * @param query the query
     * @return the sets
     */
    private Map<IMObjectReference, ObjectSet> getSets(CustomerBalanceSummaryQuery query) {
        Map<IMObjectReference, ObjectSet> result = new HashMap<IMObjectReference, ObjectSet>();
        while (query.hasNext()) {
            ObjectSet set = query.next();
            IMObjectReference ref = set.getReference(
                    CustomerBalanceSummaryQuery.CUSTOMER_REFERENCE);
            if (result.containsKey(ref)) {
                fail("Duplicate customer " + ref);
            }
            result.put(ref, set);
        }
        return result;
    }

    /**
     * Helper to set the name for a customer.
     *
     * @param customer the customer
     * @param id       the name id
     */
    private void setName(Party customer, long id) {
        IMObjectBean bean = new IMObjectBean(customer);
        bean.setValue("firstName", "Foo");
        bean.setValue("lastName", "Bar-" + id);
        bean.save();
    }
}
