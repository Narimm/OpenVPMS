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

package org.openvpms.archetype.rules.finance.statement;

import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * Tests the {@link EndOfPeriodProcessor} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class EndOfPeriodProcessorTestCase extends AbstractStatementTest {

    /**
     * Verifies that the processor cannot be constructed with an invalid date.
     */
    public void testStatementDate() {
        Date now = new Date();
        try {
            new EndOfPeriodProcessor(now, true);
            fail("Expected StatementProcessorException to be thrown");
        } catch (StatementProcessorException expected) {
            assertEquals(
                    StatementProcessorException.ErrorCode.InvalidStatementDate,
                    expected.getErrorCode());
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        try {
            new EndOfPeriodProcessor(calendar.getTime(), true);
        } catch (StatementProcessorException exception) {
            fail("Construction failed with exception: " + exception);
        }
    }

    /**
     * Tests end of period.
     */
    public void testEndOfPeriod() {
        StatementRules rules = new StatementRules();
        Party customer = getCustomer();
        Date statementDate = getDate("2007-01-01");

        assertFalse(rules.hasStatement(customer, statementDate));
        List<Act> acts = getActs(customer, statementDate);
        assertEquals(0, acts.size());

        FinancialAct invoice1 = createChargesInvoice(
                new Money(100), getDatetime("2007-01-01 10:00:00"));
        save(invoice1);

        acts = getActs(customer, statementDate);
        assertEquals(1, acts.size());
        checkAct(acts.get(0), invoice1, FinancialActStatus.POSTED);

        EndOfPeriodProcessor processor
                = new EndOfPeriodProcessor(statementDate, true);
        processor.process(customer);

        assertTrue(rules.hasStatement(customer, statementDate));
        acts = getActs(customer, statementDate);
        assertEquals(2, acts.size());
        checkAct(acts.get(0), invoice1, FinancialActStatus.POSTED);
        checkAct(acts.get(1), "act.customerAccountClosingBalance",
                 new BigDecimal("100"));

        // verify more acts aren't generated for the same statement date
        processor.process(customer);
        acts = getActs(customer, statementDate);
        assertEquals(2, acts.size());

        // save a new invoice after the statement date
        FinancialAct invoice2 = createChargesInvoice(
                new Money(100), getDatetime("2007-01-02 10:00:00"));
        save(invoice2);

        // verify it doesn't appear in the statement
        acts = getActs(customer, statementDate);
        assertEquals(2, acts.size());

        statementDate = getDate("2007-01-02");
        assertFalse(rules.hasStatement(customer, statementDate));

        acts = getActs(customer, statementDate);
        assertEquals(2, acts.size());
        checkAct(acts.get(0), "act.customerAccountOpeningBalance",
                 new BigDecimal("100"));
        checkAct(acts.get(1), invoice2, FinancialActStatus.POSTED);
    }

    /**
     * Verifies that any COMPLETED charge acts are posted on end-of-period.
     */
    public void testPostCompleted() {
        Party customer = getCustomer();

        final Money amount = new Money(100);
        FinancialAct invoice1 = createChargesInvoice(
                amount, getDatetime("2007-01-01 10:00:00"));
        invoice1.setStatus(FinancialActStatus.COMPLETED);  // will be posted
        save(invoice1);

        FinancialAct invoice2 = createChargesInvoice(
                amount, getDatetime("2007-01-01 11:00:00"));
        invoice2.setStatus(FinancialActStatus.IN_PROGRESS);  // won't be posted
        save(invoice2);

        FinancialAct invoice3 = createChargesInvoice(
                amount, getDatetime("2007-01-01 11:00:00"));
        invoice3.setStatus(FinancialActStatus.ON_HOLD);    // won't be posted
        save(invoice3);

        FinancialAct invoice4 = createChargesInvoice(
                amount, getDatetime("2007-01-01 11:00:00")); // already posted
        save(invoice4);

        Date statementDate = getDate("2007-02-01");    // perform end-of-period
        EndOfPeriodProcessor processor
                = new EndOfPeriodProcessor(statementDate, true);
        processor.process(customer);

        // verify the acts for the period match that expected
        List<Act> acts = getPostedActs(customer, statementDate);
        assertEquals(3, acts.size());
        checkAct(acts, invoice1, FinancialActStatus.POSTED); // first 2 acts
        checkAct(acts, invoice4, FinancialActStatus.POSTED); // in any order
        checkAct(acts.get(2), "act.customerAccountClosingBalance",
                 new BigDecimal("200"));
    }

    /**
     * Verifies that any COMPLETED charge acts are not posted when
     * <tt>postCompletedCharges</tt> is false.
     */
    public void testNoPostCompleted() {
        Party customer = getCustomer();

        final Money amount = new Money(100);
        FinancialAct invoice1 = createChargesInvoice(
                amount, getDatetime("2007-01-01 10:00:00"));
        invoice1.setStatus(FinancialActStatus.COMPLETED);  // won't be posted
        save(invoice1);

        FinancialAct invoice2 = createChargesInvoice(
                amount, getDatetime("2007-01-01 11:00:00"));
        invoice2.setStatus(FinancialActStatus.IN_PROGRESS);  // won't be posted
        save(invoice2);

        FinancialAct invoice3 = createChargesInvoice(
                amount, getDatetime("2007-01-01 11:00:00"));
        invoice3.setStatus(FinancialActStatus.ON_HOLD);    // won't be posted
        save(invoice3);

        FinancialAct invoice4 = createChargesInvoice(
                amount, getDatetime("2007-01-01 11:00:00"));
        save(invoice4);

        Date statementDate = getDate("2007-02-01");    // perform end-of-period
        EndOfPeriodProcessor processor
                = new EndOfPeriodProcessor(statementDate, false);
        processor.process(customer);

        // verify the acts for the period match that expected
        List<Act> acts = getPostedActs(customer, statementDate);
        assertEquals(2, acts.size());
        checkAct(acts.get(0), invoice4, FinancialActStatus.POSTED);
        checkAct(acts.get(1), "act.customerAccountClosingBalance", amount);
    }

    /**
     * Tests end of period with account fees.
     */
    public void testEndOfPeriodWithAccountFees() {
        Party customer = getCustomer();
        BigDecimal feeAmount = new BigDecimal("25.00");
        customer.addClassification(createAccountType(30, DateUnits.DAYS,
                                                     feeAmount));
        save(customer);

        final Money amount = new Money(100);
        FinancialAct invoice = createChargesInvoice(amount);
        invoice.setActivityStartTime(getDatetime("2007-01-01 10:00:00"));
        save(invoice);
        Date statementDate = getDate("2007-05-02");

        EndOfPeriodProcessor processor
                = new EndOfPeriodProcessor(statementDate, true);
        processor.process(customer);

        List<Act> acts = getActs(customer, statementDate);
        assertEquals(3, acts.size());

        assertEquals(invoice, acts.get(0));
        checkAct(acts.get(1), "act.customerAccountDebitAdjust", feeAmount);
        checkAct(acts.get(2), "act.customerAccountClosingBalance",
                 amount.add(feeAmount));
    }

}
