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

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ObjectSet;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * Tests the {@link CustomerBalanceSummaryQuery} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerBalanceSummaryQueryTestCase
        extends AbstractCustomerBalanceTest {

    /**
     * Tests the {@link CustomerBalanceSummaryQuery} class.
     */
    public void testQuery() {
        // add a 30 day payment term for accounts to the customer
        Party customer = getCustomer();
        customer.addClassification(createAccountType(30, DateRules.DAYS));
        save(customer);

        Date startTime = java.sql.Date.valueOf("2007-1-1");

        // 60 days from saved, amount should be overdue
        Date now = DateRules.getDate(startTime, 60, DateRules.DAYS);

        // create and save a new invoice
        final Money hundred = new Money(100);
        FinancialAct invoice = createChargesInvoice(hundred);
        invoice.setActivityStartTime(startTime);
        save(invoice);

        // pay half the invoice
        Date paymentStartTime = DateRules.getDate(startTime, 1, DateRules.DAYS);
        final Money fifty = new Money(50);
        FinancialAct payment = createPayment(fifty);
        payment.setActivityStartTime(paymentStartTime);
        save(payment);

        // query the customer
        List<Party> customers = Arrays.asList(customer);

        CustomerBalanceSummaryQuery query
                = new CustomerBalanceSummaryQuery(customers.iterator(), now);
        assertTrue(query.hasNext());
        ObjectSet set = query.next();
        assertEquals(customer, set.get(CustomerBalanceSummaryQuery.CUSTOMER));

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


}
