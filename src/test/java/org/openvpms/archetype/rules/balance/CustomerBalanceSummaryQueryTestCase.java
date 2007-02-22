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
        final Money amount = new Money(100);
        FinancialAct invoice = createChargesInvoice(amount);
        invoice.setActivityStartTime(startTime);
        save(invoice);

        List<Party> customers = Arrays.asList(customer);

        CustomerBalanceSummaryQuery query
                = new CustomerBalanceSummaryQuery(customers.iterator(), now);
        assertTrue(query.hasNext());
        ObjectSet set = query.next();
        assertEquals(customer, set.get(CustomerBalanceSummaryQuery.CUSTOMER));
        set.get(CustomerBalanceSummaryQuery.BALANCE);
        set.get(CustomerBalanceSummaryQuery.OVERDUE_BALANCE);
        set.get(CustomerBalanceSummaryQuery.CREDIT_BALANCE);
        set.get(CustomerBalanceSummaryQuery.LAST_PAYMENT_DATE);
        set.get(CustomerBalanceSummaryQuery.LAST_PAYMENT_AMOUNT);
        set.get(CustomerBalanceSummaryQuery.LAST_INVOICE_AMOUNT);

        assertFalse(query.hasNext());
    }


}
