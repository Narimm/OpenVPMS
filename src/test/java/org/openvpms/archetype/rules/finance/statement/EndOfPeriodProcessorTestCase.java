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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.finance.statement;

import org.junit.Test;
import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.archetype.rules.finance.account.AccountType;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.openvpms.archetype.rules.act.ActStatus.POSTED;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.CLOSING_BALANCE;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.DEBIT_ADJUST;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.OPENING_BALANCE;

/**
 * Tests the {@link EndOfPeriodProcessor} class.
 *
 * @author Tim Anderson
 */
public class EndOfPeriodProcessorTestCase extends AbstractStatementTest {

    /**
     * Verifies that the processor cannot be constructed with an invalid date.
     */
    @Test
    public void testStatementDate() {
        Date now = new Date();

        try {
            new EndOfPeriodProcessor(now, true, getPractice(), service, lookups, accountRules);
            fail("Expected StatementProcessorException to be thrown");
        } catch (StatementProcessorException expected) {
            assertEquals(
                    StatementProcessorException.ErrorCode.InvalidStatementDate,
                    expected.getErrorCode());
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        try {
            new EndOfPeriodProcessor(calendar.getTime(), true, getPractice(), service, lookups, accountRules);
        } catch (StatementProcessorException exception) {
            fail("Construction failed with exception: " + exception);
        }
    }

    /**
     * Tests end of period.
     * <p/>
     * On completion, the following acts should be present:
     * <ul>
     * <li>1/1/2007 invoice 100.00
     * <li>1/1/2007 closing balance -100.00
     * <li>1/1/2007 opening balance 100.00
     * <li>2/1/2007 invoice 100.00
     * </ul>
     */
    @Test
    public void testEndOfPeriod() {
        StatementRules rules = new StatementRules(getPractice(), service, lookups, accountRules);
        Party customer = getCustomer();

        // 30 days account fee days i.e 30 days before overdue fees are
        // generated
        Lookup accountType = FinancialTestHelper.createAccountType(
                30, DateUnits.DAYS, new BigDecimal("10.00"), 30);
        customer.addClassification(accountType);
        save(customer);

        Date statementDate = getDate("2007-01-01");

        assertFalse(rules.hasStatement(customer, statementDate));
        List<Act> acts = getActs(customer, statementDate);
        assertEquals(0, acts.size());

        Money amount = new Money(100);
        List<FinancialAct> invoice1 = createChargesInvoice(
                amount, getDatetime("2007-01-01 10:00:00"));
        save(invoice1);

        acts = getActs(customer, statementDate);
        assertEquals(1, acts.size());
        checkAct(acts.get(0), invoice1.get(0), POSTED);

        EndOfPeriodProcessor processor
                = new EndOfPeriodProcessor(statementDate, true, getPractice(), service, lookups, accountRules);
        processor.process(customer);

        assertTrue(rules.hasStatement(customer, statementDate));
        acts = getActs(customer, statementDate);
        assertEquals(2, acts.size());
        checkAct(acts.get(0), invoice1.get(0), POSTED);
        checkClosingBalance(acts.get(1), amount, BigDecimal.ZERO);

        // verify more acts aren't generated for the same statement date
        processor.process(customer);
        acts = getActs(customer, statementDate);
        assertEquals(2, acts.size());

        // save a new invoice after the statement date
        List<FinancialAct> invoice2 = createChargesInvoice(
                amount, getDatetime("2007-01-02 10:00:00"));
        save(invoice2);

        // verify it doesn't appear in the statement
        acts = getActs(customer, statementDate);
        assertEquals(2, acts.size());

        statementDate = getDate("2007-01-02");
        assertFalse(rules.hasStatement(customer, statementDate));

        acts = getActs(customer, statementDate);
        assertEquals(2, acts.size());
        checkOpeningBalance(acts.get(0), new BigDecimal("100"));
        checkAct(acts.get(1), invoice2.get(0), POSTED);
    }

    /**
     * Verifies that any COMPLETED charge acts are posted on end-of-period.
     */
    @Test
    public void testPostCompleted() {
        Party customer = getCustomer();

        // 60 days account fee days i.e 60 days before overdue fees are
        // generated
        Lookup accountType = FinancialTestHelper.createAccountType(
                60, DateUnits.DAYS, new BigDecimal("10.00"), 60);
        customer.addClassification(accountType);
        save(customer);

        final Money amount = new Money(100);
        List<FinancialAct> invoice1 = createChargesInvoice(
                amount, getDatetime("2007-01-01 10:00:00"),
                FinancialActStatus.COMPLETED);  // will be posted
        save(invoice1);

        List<FinancialAct> invoice2 = createChargesInvoice(
                amount, getDatetime("2007-01-01 11:00:00"),
                FinancialActStatus.IN_PROGRESS);  // won't be posted
        save(invoice2);

        List<FinancialAct> invoice3 = createChargesInvoice(
                amount, getDatetime("2007-01-01 11:00:00"),
                FinancialActStatus.ON_HOLD);    // won't be posted
        save(invoice3);

        List<FinancialAct> invoice4 = createChargesInvoice(
                amount, getDatetime("2007-01-01 11:00:00")); // already posted
        save(invoice4);

        Date statementDate = getDate("2007-02-01");    // perform end-of-period
        EndOfPeriodProcessor processor
                = new EndOfPeriodProcessor(statementDate, true, getPractice(), service, lookups, accountRules);
        processor.process(customer);

        // verify the acts for the period match that expected
        List<Act> acts = getPostedActs(customer, statementDate);
        assertEquals(3, acts.size());
        checkAct(acts, invoice1.get(0), POSTED); // first 2 acts
        checkAct(acts, invoice4.get(0), POSTED); // in any order
        checkClosingBalance(acts.get(2), new BigDecimal("200"),
                            BigDecimal.ZERO);
    }

    /**
     * Verifies that any COMPLETED charge acts are not posted when
     * <tt>postCompletedCharges</tt> is false.
     */
    @Test
    public void testNoPostCompleted() {
        Party customer = getCustomer();

        final Money amount = new Money(100);
        List<FinancialAct> invoice1 = createChargesInvoice(
                amount, getDatetime("2007-01-01 10:00:00"),
                FinancialActStatus.COMPLETED);  // won't be posted
        save(invoice1);

        List<FinancialAct> invoice2 = createChargesInvoice(
                amount, getDatetime("2007-01-01 11:00:00"),
                FinancialActStatus.IN_PROGRESS);  // won't be posted
        save(invoice2);

        List<FinancialAct> invoice3 = createChargesInvoice(
                amount, getDatetime("2007-01-01 11:00:00"),
                FinancialActStatus.ON_HOLD);    // won't be posted
        save(invoice3);

        List<FinancialAct> invoice4 = createChargesInvoice(
                amount, getDatetime("2007-01-01 11:00:00"));
        save(invoice4);

        Date statementDate = getDate("2007-02-01");    // perform end-of-period
        EndOfPeriodProcessor processor
                = new EndOfPeriodProcessor(statementDate, false, getPractice(), service, lookups, accountRules);
        processor.process(customer);

        // verify the acts for the period match that expected
        List<Act> acts = getPostedActs(customer, statementDate);
        assertEquals(2, acts.size());
        checkAct(acts.get(0), invoice4.get(0), POSTED);
        checkClosingBalance(acts.get(1), amount, amount);
    }

    /**
     * Tests end of period with fixed account fees.
     */
    @Test
    public void testEndOfPeriodWithFixedAccountFees() {
        Party customer = getCustomer();
        BigDecimal feeAmount = new BigDecimal("25.00");

        // 30 days account fee days i.e 30 days before overdue fees are
        // generated
        Lookup accountType = FinancialTestHelper.createAccountType(
                30, DateUnits.DAYS, feeAmount, 30);
        customer.addClassification(accountType);
        save(customer);

        final Money amount = new Money(100);
        Date datetime = getDatetime("2007-01-01 10:00:00");
        List<FinancialAct> invoice = createChargesInvoice(
                amount, customer, datetime);
        save(invoice);

        // run end of period 29 days from when the invoice was posted
        Date statementDate = DateRules.getDate(datetime, 29, DateUnits.DAYS);

        EndOfPeriodProcessor processor
                = new EndOfPeriodProcessor(statementDate, true, getPractice(), service, lookups, accountRules);
        processor.process(customer);

        // verify there are 2 acts: the original invoice and closing balance
        List<Act> acts = getActs(customer, statementDate);
        assertEquals(2, acts.size());

        assertEquals(invoice.get(0), acts.get(0));
        FinancialAct closing = (FinancialAct) acts.get(1);
        checkClosingBalance(closing, amount, BigDecimal.ZERO);
        assertTrue(closing.isCredit());

        // run end of period 30 days from when the invoice was posted
        statementDate = DateRules.getDate(statementDate, 30, DateUnits.DAYS);

        processor = new EndOfPeriodProcessor(statementDate, true, getPractice(), service, lookups, accountRules);
        processor.process(customer);

        // verify there are 3 acts: an opening balance, an overdue fee,
        // and a closing balance
        acts = getActs(customer, statementDate);
        assertEquals(3, acts.size());

        BigDecimal closingBalance = amount.add(feeAmount);
        checkOpeningBalance(acts.get(0), amount);
        checkAct(acts.get(1), DEBIT_ADJUST, feeAmount);
        checkClosingBalance(acts.get(2), closingBalance, amount);

        // verify the fee has been added to the balance
        checkEquals(closingBalance, accountRules.getBalance(customer));
    }

    /**
     * Tests end of period with percentage account fees.
     */
    @Test
    public void testEndOfPeriodWithPercentageAccountFees() {
        Party customer = getCustomer();
        BigDecimal feePercent = new BigDecimal("1.25");

        // 30 days account fee days i.e 30 days before overdue fees are
        // generated, charging 1.25% on overdue fees
        Lookup accountType = FinancialTestHelper.createAccountType(
                30, DateUnits.DAYS, feePercent, AccountType.FeeType.PERCENTAGE, 30, BigDecimal.ZERO);
        customer.addClassification(accountType);
        save(customer);

        final Money amount = new Money(50);
        Date datetime = getDatetime("2007-01-01 10:00:00");
        List<FinancialAct> invoice = createChargesInvoice(amount, customer, datetime);
        save(invoice);

        // run end of period 29 days from when the invoice was posted
        Date statementDate = DateRules.getDate(datetime, 29, DateUnits.DAYS);

        EndOfPeriodProcessor processor = new EndOfPeriodProcessor(statementDate, true, getPractice(),
                                                                  service, lookups, accountRules);
        processor.process(customer);

        // verify there are 2 acts: the original invoice and closing balance
        List<Act> acts = getActs(customer, statementDate);
        assertEquals(2, acts.size());

        assertEquals(invoice.get(0), acts.get(0));
        FinancialAct closing = (FinancialAct) acts.get(1);
        checkClosingBalance(closing, amount, BigDecimal.ZERO);
        assertTrue(closing.isCredit());

        // run end of period 30 days from when the invoice was posted
        statementDate = DateRules.getDate(statementDate, 30, DateUnits.DAYS);

        processor = new EndOfPeriodProcessor(statementDate, true, getPractice(), service, lookups, accountRules);
        processor.process(customer);

        // verify there are 3 acts: an opening balance, an overdue fee,
        // and a closing balance
        acts = getActs(customer, statementDate);
        assertEquals(3, acts.size());

        BigDecimal feeAmount = new BigDecimal("0.63");   // ((50 * 1.25) / 100) rounded to 2 places
        BigDecimal closingBalance = amount.add(feeAmount);
        checkOpeningBalance(acts.get(0), amount);
        checkAct(acts.get(1), DEBIT_ADJUST, feeAmount);
        checkClosingBalance(acts.get(2), closingBalance, amount);

        // verify the fee has been added to the balance
        checkEquals(closingBalance, accountRules.getBalance(customer));
    }

    /**
     * Verifies that end of period is not performed if there is no account
     * activity.
     */
    @Test
    public void testEndOfPeriodForNoActivity() {
        Party customer = getCustomer();
        Date statementDate = getDate("2007-05-02");
        EndOfPeriodProcessor processor
                = new EndOfPeriodProcessor(statementDate, true, getPractice(), service, lookups, accountRules);
        processor.process(customer);
        List<Act> acts = getActs(customer, statementDate);
        assertEquals(0, acts.size());

        // check there is no opening balance for the next statement date
        Date statementDate2 = getDate("2007-06-30");
        acts = getActs(customer, statementDate2);
        assertEquals(0, acts.size());
    }

    /**
     * Verifies that end of period is performed if there is account activity
     * with a zero account balance.
     * <p/>
     * On completion, the following acts should be present:
     * <ul>
     * <li>1/1/2007 invoice   100.00
     * <li>1/1/2007 payment  -100.00
     * <li>2/5/2007 closing balance 0.0
     * <li>2/5/2007 opening balance 0.0
     * </ul>
     */
    @Test
    public void testEndOfPeriodForZeroBalance() {
        Party customer = getCustomer();

        Money amount = new Money(100);
        List<FinancialAct> invoice = createChargesInvoice(
                amount, getDatetime("2007-01-01 10:00:00"));
        save(invoice);
        FinancialAct payment = createPayment(
                amount, getDatetime("2007-01-01 11:00:00"));
        save(payment);

        // should be a zero balance
        checkEquals(BigDecimal.ZERO, accountRules.getBalance(customer));

        // now run end-of-period
        Date statementDate = getDate("2007-05-02");

        EndOfPeriodProcessor processor
                = new EndOfPeriodProcessor(statementDate, true, getPractice(), service, lookups, accountRules);
        processor.process(customer);
        List<Act> acts = getActs(customer, statementDate);
        assertEquals(3, acts.size());
        checkAct(acts.get(0), invoice.get(0), POSTED);
        checkAct(acts.get(1), payment, POSTED);
        FinancialAct closing = (FinancialAct) acts.get(2);
        checkClosingBalance(closing, BigDecimal.ZERO, BigDecimal.ZERO);
        assertTrue(closing.isCredit());

        // check there is an opening balance for the next statement date
        Date statementDate2 = getDate("2007-06-30");
        acts = getActs(customer, statementDate2);
        assertEquals(1, acts.size());
        checkOpeningBalance(acts.get(0), BigDecimal.ZERO);
    }

    /**
     * Verifies that the credit flag is set true and the total is positive for
     * <em>act.customerAccountOpeningBalance</em>
     * and <em>act.customerAccountClosingBalance</em> when there is a credit
     * balance.
     */
    @Test
    public void testEndOfPeriodForCreditBalance() {
        Party customer = getCustomer();

        // 30 days account fee days i.e 30 days before overdue fees are
        // generated
        Lookup accountType = FinancialTestHelper.createAccountType(
                30, DateUnits.DAYS, new BigDecimal("10.00"), 30);
        customer.addClassification(accountType);
        save(customer);

        Money amount = new Money(100);
        FinancialAct payment = createPayment(
                amount, getDatetime("2007-01-01 11:00:00"));
        save(payment);

        // should be a negative 100 balance
        checkEquals(amount.negate(), accountRules.getBalance(customer));

        // now run end-of-period
        Date statementDate = getDate("2007-02-01");

        EndOfPeriodProcessor processor
                = new EndOfPeriodProcessor(statementDate, true, getPractice(), service, lookups, accountRules);
        processor.process(customer);
        List<Act> acts = getActs(customer, statementDate);
        assertEquals(2, acts.size());
        checkAct(acts.get(0), payment, POSTED);
        FinancialAct closing = (FinancialAct) acts.get(1);
        checkClosingBalance(closing, amount, BigDecimal.ZERO);
        assertFalse(closing.isCredit());

        Date nextStatementDate = getDate("2007-03-01");
        acts = getActs(customer, nextStatementDate);
        assertEquals(1, acts.size());
        FinancialAct opening = (FinancialAct) acts.get(0);
        checkOpeningBalance(opening, amount);
        assertTrue(opening.isCredit());
        checkEquals(amount.negate(), accountRules.getBalance(customer));

        // run end of period again
        processor = new EndOfPeriodProcessor(nextStatementDate, true, getPractice(), service, lookups, accountRules);
        processor.process(customer);
        acts = getActs(customer, nextStatementDate);
        assertEquals(2, acts.size());
        opening = (FinancialAct) acts.get(0);
        closing = (FinancialAct) acts.get(1);
        checkOpeningBalance(opening, amount);
        checkClosingBalance(closing, amount, BigDecimal.ZERO);
        assertTrue(opening.isCredit());
        assertFalse(closing.isCredit());
    }

    /**
     * Verifies that end of period can be backdated and only includes those
     * acts prior to the statement date.
     */
    @Test
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
        List<FinancialAct> invoice1 = createChargesInvoice(
                amount, customer, getDatetime("2007-01-01 10:00:00"));
        save(invoice1);

        List<FinancialAct> invoice2 = createChargesInvoice(
                amount, customer, getDatetime("2007-02-02 12:00:00"));
        save(invoice2);

        // run end of period for the 2/1. Should only include invoice1,
        // and generate an account fee.
        Date statementDate1 = getDate("2007-02-01");

        EndOfPeriodProcessor processor
                = new EndOfPeriodProcessor(statementDate1, true, getPractice(), service, lookups, accountRules);
        processor.process(customer);

        // verify there are 3 acts: invoice1, an overdue fee,
        // and a closing balance
        List<Act> acts = getActs(customer, statementDate1);
        assertEquals(3, acts.size());

        BigDecimal balance = amount.add(feeAmount);
        checkAct(acts.get(0), invoice1.get(0), POSTED);
        checkAct(acts.get(1), DEBIT_ADJUST, feeAmount);
        checkClosingBalance(acts.get(2), balance, amount);

        // now check acts for the next statement period.
        Date statementDate2 = getDate("2007-03-01");
        acts = getActs(customer, statementDate2);
        assertEquals(2, acts.size());

        checkOpeningBalance(acts.get(0), balance);
        checkAct(acts.get(1), invoice2.get(0), POSTED);

        // run end of period for statementDate2
        processor = new EndOfPeriodProcessor(statementDate2, true, getPractice(), service, lookups, accountRules);
        processor.process(customer);

        // verify there are 4 acts: an opening balance, invoice2, a new overdue
        // fee, and a closing balance
        acts = getActs(customer, statementDate2);
        assertEquals(4, acts.size());

        checkOpeningBalance(acts.get(0), balance);
        checkAct(acts.get(1), invoice2.get(0), POSTED);
        checkAct(acts.get(2), DEBIT_ADJUST, feeAmount);

        balance = balance.multiply(BigDecimal.valueOf(2));
        checkClosingBalance(acts.get(3), balance, amount);

        // check there is an opening balance for the next statement date
        Date statementDate3 = getDate("2008-04-30");
        acts = getActs(customer, statementDate3);
        assertEquals(1, acts.size());
        checkOpeningBalance(acts.get(0), balance);
    }

    /**
     * Verifies that no account fees are generated if a payment is made
     * late, but prior to the statement date.
     * <p/>
     * On completion, the following acts should be present:
     * <ul>
     * <li>15/4/2008 invoice 50.00
     * <li>30/4/2008 closing balance -50.00
     * <li>30/4/2008 opening balance  50.00
     * <li>25/5/2008 payment -50.00
     * <li>31/5/2008 closing balance  0.00
     * <li>31/5/2008 opening balance  0.00
     * </ul>
     */
    @Test
    public void testLatePaymentForInvoiceInPriorStatementPeriod() {
        Party customer = getCustomer();
        BigDecimal feeAmount = new BigDecimal("10.00");

        // 30 days account fee days i.e 30 days before overdue fees are
        // generated
        Lookup accountType = FinancialTestHelper.createAccountType(
                30, DateUnits.DAYS, feeAmount, 30);
        customer.addClassification(accountType);
        save(customer);

        final Money amount = new Money("50.00");
        List<FinancialAct> invoice1 = createChargesInvoice(
                amount, customer, getDatetime("2008-04-15 10:00:00"));
        save(invoice1);

        // run end of period for the 30/04.
        Date statementDate1 = getDate("2008-04-30");

        EndOfPeriodProcessor processor
                = new EndOfPeriodProcessor(statementDate1, true, getPractice(), service, lookups, accountRules);
        processor.process(customer);

        // verify there are 2 acts: invoice1, and a closing balance
        List<Act> acts = getActs(customer, statementDate1);
        assertEquals(2, acts.size());

        checkAct(acts.get(0), invoice1.get(0), POSTED);
        checkClosingBalance(acts.get(1), amount, BigDecimal.ZERO);

        // now save a payment
        FinancialAct payment = createPayment(
                amount, getDatetime("2008-05-25 11:00:00"));
        save(payment);

        // run end of period for the 31/05
        Date statementDate2 = getDate("2008-05-31");
        acts = getActs(customer, statementDate2);
        assertEquals(2, acts.size());

        // run end of period for statementDate2
        processor = new EndOfPeriodProcessor(statementDate2, true, getPractice(), service, lookups, accountRules);
        processor.process(customer);

        // verify there are 3 acts: an opening balance, payment, and a closing
        // balance
        acts = getActs(customer, statementDate2);
        assertEquals(3, acts.size());
        checkOpeningBalance(acts.get(0), amount);
        checkAct(acts.get(1), payment, POSTED);
        checkClosingBalance(acts.get(2), BigDecimal.ZERO, BigDecimal.ZERO);

        // check there is an opening balance for the next statement date
        Date statementDate3 = getDate("2008-06-30");
        acts = getActs(customer, statementDate3);
        assertEquals(1, acts.size());
        checkOpeningBalance(acts.get(0), BigDecimal.ZERO);
    }

    /**
     * Verifies that no accounting fee is generated if the overdue amount
     * is less than the fee balance amount.
     * <p/>
     * On completion, the following acts should be present:
     * <ul>
     * <li>30/4/08 opening balance 155.00
     * <li>15/5/08 payment         -150.00
     * <li>31/5/08 closing balance -5.00
     * <li>31/5/08 opening balance 5.00
     * </ul>
     */
    @Test
    public void testNoFeeForOverdueLessThanFeeBalance() {
        Party customer = getCustomer();
        BigDecimal feeAmount = new BigDecimal("10.00");
        BigDecimal feeBalance = new BigDecimal("10.00");

        // create account type where 30 days must elapse before overdue fees are
        // generated for amounts >= $10
        Lookup accountType = FinancialTestHelper.createAccountType(
                30, DateUnits.DAYS, feeAmount, AccountType.FeeType.FIXED, 30, feeBalance);
        customer.addClassification(accountType);
        save(customer);

        final Money amount = new Money("155.00");
        List<FinancialAct> invoice1 = createChargesInvoice(
                amount, customer, getDatetime("2008-04-29 10:00:00"));
        save(invoice1);

        // run end of period for the 30/04.
        Date statementDate1 = getDate("2008-04-30");

        EndOfPeriodProcessor processor
                = new EndOfPeriodProcessor(statementDate1, true, getPractice(), service, lookups, accountRules);
        processor.process(customer);

        // verify there are 2 acts: invoice1, and a closing balance
        List<Act> acts = getActs(customer, statementDate1);
        assertEquals(2, acts.size());

        checkAct(acts.get(0), invoice1.get(0), POSTED);
        checkClosingBalance(acts.get(1), amount, BigDecimal.ZERO);

        // now save a payment
        final Money amount2 = new Money("150");
        FinancialAct payment = createPayment(
                amount2, getDatetime("2008-05-15 11:00:00"));
        save(payment);

        // run end of period for the 30/05
        Date statementDate2 = getDate("2008-05-30");
        processor = new EndOfPeriodProcessor(statementDate2, true, getPractice(), service, lookups, accountRules);
        processor.process(customer);

        // verify there are 3 acts: an opening balance, payment, and a closing
        // balance
        acts = getActs(customer, statementDate2);
        assertEquals(3, acts.size());
        Money amount3 = new Money("5.00");
        checkOpeningBalance(acts.get(0), amount);
        checkAct(acts.get(1), payment, POSTED);
        checkClosingBalance(acts.get(2), amount3, amount3);

        // check there is an opening balance for the next statement date
        Date statementDate3 = getDate("2008-06-30");
        acts = getActs(customer, statementDate3);
        assertEquals(1, acts.size());
        checkOpeningBalance(acts.get(0), amount3);
    }

    /**
     * Verifies that an accounting fee is generated if the overdue amount
     * is equal to the fee balance amount.
     * <p/>
     * On completion, the following acts should be present:
     * <ul>
     * <li>15/4/08 invoice 160.00
     * <li>30/4/08 closing balance -160.00
     * <li>30/4/08 opening balance 160.00
     * <li>25/5/08 payment         -150.00
     * <li>31/5/08 debit adjust    10.00
     * <li>31/5/08 closing balance -20.00
     * <li>31/5/08 opening balance 20.00
     * </ul>
     */
    @Test
    public void testFeeForOverdueEqualFeeBalance() {
        Party customer = getCustomer();
        BigDecimal feeAmount = new BigDecimal("10.00");
        BigDecimal feeBalance = new BigDecimal("10.00");

        // create account type where 30 days must elapse before overdue fees are
        // generated for amounts >= $10
        Lookup accountType = FinancialTestHelper.createAccountType(
                30, DateUnits.DAYS, feeAmount, AccountType.FeeType.FIXED, 30, feeBalance);
        customer.addClassification(accountType);
        save(customer);

        final Money amount = new Money("160.00");
        List<FinancialAct> invoice1 = createChargesInvoice(
                amount, customer, getDatetime("2008-04-15 10:00:00"));
        save(invoice1);

        // run end of period for the 30/04.
        Date statementDate1 = getDate("2008-04-30");

        EndOfPeriodProcessor processor
                = new EndOfPeriodProcessor(statementDate1, true, getPractice(), service, lookups, accountRules);
        processor.process(customer);

        // verify there are 2 acts: invoice1, and a closing balance
        List<Act> acts = getActs(customer, statementDate1);
        assertEquals(2, acts.size());

        checkAct(acts.get(0), invoice1.get(0), POSTED);
        checkClosingBalance(acts.get(1), amount, BigDecimal.ZERO);

        // now save a payment
        final Money amount2 = new Money("150");
        FinancialAct payment = createPayment(
                amount2, getDatetime("2008-05-25 11:00:00"));
        save(payment);

        // run end of period for the 31/05
        Date statementDate2 = getDate("2008-05-31");
        processor = new EndOfPeriodProcessor(statementDate2, true, getPractice(), service, lookups, accountRules);
        processor.process(customer);

        // verify there are 4 acts: an opening balance, payment, fee, and a
        // closing balance
        acts = getActs(customer, statementDate2);
        assertEquals(4, acts.size());
        Money amount3 = new Money("10.00");
        BigDecimal closingBalance = amount3.add(feeAmount);
        checkOpeningBalance(acts.get(0), amount);
        checkAct(acts.get(1), payment, POSTED);
        checkAct(acts.get(2), DEBIT_ADJUST, feeAmount);
        checkClosingBalance(acts.get(3), closingBalance, amount3);

        // verify the fee has been added to the balance
        checkEquals(closingBalance, accountRules.getBalance(customer));

        // check there is an opening balance for the next statement date
        Date statementDate3 = getDate("2008-06-30");
        acts = getActs(customer, statementDate3);
        assertEquals(1, acts.size());
        checkOpeningBalance(acts.get(0), closingBalance);
    }

    /**
     * Verifies the behaviour of having different overdue and fee date terms.
     * <p/>
     * On completion, the following acts should be present:
     * <ul>
     * <li>15/4/07 invoice 160.00
     * <li>30/4/07 closing balance -160.00
     * <li>30/4/07 opening balance 160.00
     * <li>31/5/07 closing balance -160.00
     * <li>31/5/07 opening balance 160.00
     * <li>30/6/07 fee             10.00
     * <li>30/6/07 closing balance -170.00
     * <li>30/6/07 opening balance 170.00
     * </ul>
     */
    @Test
    public void testDifferentOverdueDateAndFeeDate() {
        Party customer = getCustomer();
        BigDecimal feeAmount = new BigDecimal("10.00");
        BigDecimal feeBalance = new BigDecimal("10.00");

        // create account type where 60 days must elapse before fees are
        // generated for amounts >= $10. Amounts are overdue after 30 days
        Lookup accountType = FinancialTestHelper.createAccountType(
                30, DateUnits.DAYS, feeAmount, AccountType.FeeType.FIXED, 60, feeBalance);
        customer.addClassification(accountType);
        save(customer);

        final Money amount = new Money("160.00");
        List<FinancialAct> invoice1 = createChargesInvoice(
                amount, customer, getDatetime("2007-04-15 10:00:00"));
        save(invoice1);

        // run end of period for the 30/04.
        Date statementDate1 = getDate("2007-04-30");

        EndOfPeriodProcessor processor
                = new EndOfPeriodProcessor(statementDate1, true, getPractice(), service, lookups, accountRules);
        processor.process(customer);

        // verify there are 2 acts: invoice1, and a closing balance
        List<Act> acts = getActs(customer, statementDate1);
        assertEquals(2, acts.size());

        checkAct(acts.get(0), invoice1.get(0), POSTED);
        checkClosingBalance(acts.get(1), amount, BigDecimal.ZERO);

        // run end of period for the 31/05
        Date statementDate2 = getDate("2007-05-31");
        processor = new EndOfPeriodProcessor(statementDate2, true, getPractice(), service, lookups, accountRules);
        processor.process(customer);

        // verify there are 2 acts: an opening balance, and a closing balance.
        // The amount should now be overdue
        acts = getActs(customer, statementDate2);
        assertEquals(2, acts.size());
        checkOpeningBalance(acts.get(0), amount);
        checkClosingBalance(acts.get(1), amount, amount);

        // run end of period for the 30/6
        Date statementDate3 = getDate("2007-06-30");
        processor = new EndOfPeriodProcessor(statementDate3, true, getPractice(), service, lookups, accountRules);
        processor.process(customer);

        // verify there are 3 acts: an opening balance, fee, and a closing
        // balance
        acts = getActs(customer, statementDate3);
        assertEquals(3, acts.size());
        BigDecimal closingBalance = amount.add(feeAmount);
        checkOpeningBalance(acts.get(0), amount);
        checkAct(acts.get(1), DEBIT_ADJUST, feeAmount);
        checkClosingBalance(acts.get(2), closingBalance, amount);

        // verify the fee has been added to the balance
        checkEquals(closingBalance, accountRules.getBalance(customer));

        // check there is an opening balance for the next statement date
        Date statementDate4 = getDate("2007-07-31");
        acts = getActs(customer, statementDate4);
        assertEquals(1, acts.size());
        checkOpeningBalance(acts.get(0), closingBalance);
    }

    /**
     * Verifies that an opening balance matches that expected.
     *
     * @param act    the act to verify
     * @param amount the expected amount
     */
    private void checkOpeningBalance(Act act, BigDecimal amount) {
        checkAct(act, OPENING_BALANCE, amount, FinancialActStatus.POSTED);
    }

    /**
     * Verifies that a closing balance matches that expected.
     *
     * @param act     the act to verify
     * @param amount  the expected amount
     * @param overdue the expected overdue amount
     */
    private void checkClosingBalance(Act act, BigDecimal amount,
                                     BigDecimal overdue) {
        checkAct(act, CLOSING_BALANCE, amount, FinancialActStatus.POSTED);
        ActBean bean = new ActBean(act);
        checkEquals(overdue, bean.getBigDecimal("overdueBalance"));
    }

}
