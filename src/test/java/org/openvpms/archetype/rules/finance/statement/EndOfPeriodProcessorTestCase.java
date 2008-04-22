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
import org.openvpms.archetype.rules.finance.account.CustomerAccountActTypes;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.lookup.Lookup;
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
        checkAct(acts.get(1), CustomerAccountActTypes.CLOSING_BALANCE,
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
        checkAct(acts.get(0), CustomerAccountActTypes.OPENING_BALANCE,
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
        checkAct(acts.get(2), CustomerAccountActTypes.CLOSING_BALANCE,
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
        checkAct(acts.get(1), CustomerAccountActTypes.CLOSING_BALANCE, amount);
    }

    /**
     * Tests end of period with account fees.
     */
    public void testEndOfPeriodWithAccountFees() {
        Party customer = getCustomer();
        BigDecimal feeAmount = new BigDecimal("25.00");

        // 30 days account fee days i.e 30 days before overdue fees are
        // generated
        Lookup accountType = FinancialTestHelper.createAccountType(
                30, DateUnits.DAYS, feeAmount, 30);
        customer.addClassification(accountType);
        save(customer);

        final Money amount = new Money(100);
        FinancialAct invoice = createChargesInvoice(amount, customer);
        Date datetime = getDatetime("2007-01-01 10:00:00");
        invoice.setActivityStartTime(datetime);
        save(invoice);

        // run end of period 29 days from when the invoice was posted
        Date statementDate = DateRules.getDate(datetime, 29, DateUnits.DAYS);

        EndOfPeriodProcessor processor
                = new EndOfPeriodProcessor(statementDate, true);
        processor.process(customer);

        // verify there are 2 acts: the original invoice and closing balance
        List<Act> acts = getActs(customer, statementDate);
        assertEquals(2, acts.size());

        assertEquals(invoice, acts.get(0));
        FinancialAct closing = (FinancialAct) acts.get(1);
        checkAct(closing, CustomerAccountActTypes.CLOSING_BALANCE, amount);
        assertTrue(closing.isCredit());

        // run end of period 30 days from when the invoice was posted
        statementDate = DateRules.getDate(statementDate, 30, DateUnits.DAYS);

        processor = new EndOfPeriodProcessor(statementDate, true);
        processor.process(customer);

        // verify there are 3 acts: an opening balance, an overdue fee,
        // and a closing balance
        acts = getActs(customer, statementDate);
        assertEquals(3, acts.size());

        checkAct(acts.get(0), CustomerAccountActTypes.OPENING_BALANCE, amount);
        checkAct(acts.get(1), CustomerAccountActTypes.DEBIT_ADJUST, feeAmount);
        checkAct(acts.get(2), CustomerAccountActTypes.CLOSING_BALANCE,
                 amount.add(feeAmount));
    }

    /**
     * Verifies that end of period is not performed if there is no account
     * activity.
     */
    public void testEndOfPeriodForNoActivity() {
        Party customer = getCustomer();
        Date statementDate = getDate("2007-05-02");
        EndOfPeriodProcessor processor
                = new EndOfPeriodProcessor(statementDate, true);
        processor.process(customer);
        List<Act> acts = getActs(customer, statementDate);
        assertEquals(0, acts.size());
    }

    /**
     * Verifies that end of period is performed if there is a account activity
     * with a zero account balance.
     */
    public void testEndOfPeriodForZeroBalance() {
        Party customer = getCustomer();

        Money amount = new Money(100);
        FinancialAct invoice = createChargesInvoice(
                amount, getDatetime("2007-01-01 10:00:00"));
        save(invoice);
        FinancialAct payment = createPayment(
                amount, getDatetime("2007-01-01 11:00:00"));
        save(payment);

        // should be a zero balance
        CustomerAccountRules rules = new CustomerAccountRules();
        checkEquals(BigDecimal.ZERO, rules.getBalance(customer));

        // now run end-of-period
        Date statementDate = getDate("2007-05-02");

        EndOfPeriodProcessor processor
                = new EndOfPeriodProcessor(statementDate, true);
        processor.process(customer);
        List<Act> acts = getActs(customer, statementDate);
        assertEquals(3, acts.size());
        checkAct(acts.get(0), invoice, FinancialActStatus.POSTED);
        checkAct(acts.get(1), payment, FinancialActStatus.POSTED);
        FinancialAct closing = (FinancialAct) acts.get(2);
        checkAct(closing, CustomerAccountActTypes.CLOSING_BALANCE,
                 BigDecimal.ZERO);
        assertTrue(closing.isCredit());
    }


    /**
     * Verifies that the credit flag is set true and the total is positive for
     * <em>act.customerAccountOpeningBalance</em>
     * and <em>act.customerAccountClosingBalance</em>.
     */
    public void testEndOfPeriodForCreditBalance() {
        Party customer = getCustomer();

        Money amount = new Money(100);
        FinancialAct payment = createPayment(
                amount, getDatetime("2007-01-01 11:00:00"));
        save(payment);

        // should be a negative 100 balance
        CustomerAccountRules rules = new CustomerAccountRules();
        checkEquals(amount.negate(), rules.getBalance(customer));

        // now run end-of-period
        Date statementDate = getDate("2007-02-01");

        EndOfPeriodProcessor processor
                = new EndOfPeriodProcessor(statementDate, true);
        processor.process(customer);
        List<Act> acts = getActs(customer, statementDate);
        assertEquals(2, acts.size());
        checkAct(acts.get(0), payment, FinancialActStatus.POSTED);
        FinancialAct closing = (FinancialAct) acts.get(1);
        checkAct(closing, CustomerAccountActTypes.CLOSING_BALANCE, amount);
        assertFalse(closing.isCredit());

        Date nextStatementDate = getDate("2007-03-01");
        acts = getActs(customer, nextStatementDate);
        assertEquals(1, acts.size());
        FinancialAct opening = (FinancialAct) acts.get(0);
        checkAct(opening, CustomerAccountActTypes.OPENING_BALANCE, amount);
        assertTrue(opening.isCredit());

        checkEquals(amount.negate(), rules.getBalance(customer));
        processor = new EndOfPeriodProcessor(nextStatementDate, true);
        processor.process(customer);
        acts = getActs(customer, nextStatementDate);
        assertEquals(2, acts.size());
        opening = (FinancialAct) acts.get(0);
        assertTrue(opening.isCredit());
        closing = (FinancialAct) acts.get(1);
        assertFalse(closing.isCredit());
        checkAct(acts.get(0), CustomerAccountActTypes.OPENING_BALANCE, amount);
        checkAct(acts.get(1), CustomerAccountActTypes.CLOSING_BALANCE, amount);

    }

    /**
     * Verifies that end of period can be backdated and only includes those
     * acts prior to the statement date.
     */
    public void testBackdatedEOP() {
        Party customer = getCustomer();
        BigDecimal feeAmount = new BigDecimal("25.00");

        // 30 days account fee days i.e 30 days before overdue fees are
        // generated
        Lookup accountType = FinancialTestHelper.createAccountType(
                30, DateUnits.DAYS, feeAmount, 30);
        customer.addClassification(accountType);
        save(customer);

        final Money amount = new Money(100);
        FinancialAct invoice1 = createChargesInvoice(amount, customer);
        invoice1.setActivityStartTime(getDatetime("2007-01-01 10:00:00"));
        save(invoice1);

        FinancialAct invoice2 = createChargesInvoice(amount, customer);
        invoice2.setActivityStartTime(getDatetime("2007-02-02 12:00:00"));
        save(invoice2);

        // run end of period for the 2/1. Should only include invoice1,
        // and generate an account fee.
        Date statementDate1 = getDate("2007-02-01");

        EndOfPeriodProcessor processor
                = new EndOfPeriodProcessor(statementDate1, true);
        processor.process(customer);

        // verify there are 3 acts: invoice1, an overdue fee,
        // and a closing balance
        List<Act> acts = getActs(customer, statementDate1);
        assertEquals(3, acts.size());

        BigDecimal balance = amount.add(feeAmount);
        checkAct(acts.get(0), invoice1, FinancialActStatus.POSTED);
        checkAct(acts.get(1), CustomerAccountActTypes.DEBIT_ADJUST, feeAmount);
        checkAct(acts.get(2), CustomerAccountActTypes.CLOSING_BALANCE, balance);

        // now check acts for the next statement period.
        Date statementDate2 = getDate("2007-03-01");
        acts = getActs(customer, statementDate2);
        assertEquals(2, acts.size());

        checkAct(acts.get(0), CustomerAccountActTypes.OPENING_BALANCE, balance);
        checkAct(acts.get(1), invoice2, FinancialActStatus.POSTED);

        // run end of period for statemenDate2
        processor = new EndOfPeriodProcessor(statementDate2, true);
        processor.process(customer);

        // verify there are 4 acts: an opening balance, invoice2, a new overdue
        // fee, and a closing balance
        acts = getActs(customer, statementDate2);
        assertEquals(4, acts.size());

        checkAct(acts.get(0), CustomerAccountActTypes.OPENING_BALANCE, balance);
        checkAct(acts.get(1), invoice2, FinancialActStatus.POSTED);
        checkAct(acts.get(2), CustomerAccountActTypes.DEBIT_ADJUST, feeAmount);

        balance = balance.multiply(BigDecimal.valueOf(2));
        checkAct(acts.get(3), CustomerAccountActTypes.CLOSING_BALANCE, balance);
    }

}
