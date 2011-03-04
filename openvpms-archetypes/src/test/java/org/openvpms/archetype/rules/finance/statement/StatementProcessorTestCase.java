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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.openvpms.archetype.component.processor.ProcessorListener;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Tests the {@link StatementProcessor} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class StatementProcessorTestCase extends AbstractStatementTest {

    /**
     * Verifies that the statement processor cannot be constructed with
     * an invalid date.
     */
    @Test
    public void testStatementDate() {
        Date now = new Date();
        try {
            new StatementProcessor(now, getPractice());
            fail("Expected StatementProcessorException to be thrown");
        } catch (StatementProcessorException expected) {
            assertEquals(
                    StatementProcessorException.ErrorCode.InvalidStatementDate,
                    expected.getErrorCode());
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        try {
            new StatementProcessor(calendar.getTime(), getPractice());
        } catch (StatementProcessorException exception) {
            fail("Construction failed with exception: " + exception);
        }
    }

    /**
     * Tests the {@link StatementProcessor#process(Party)} method a
     * statement date where end-of-period has not yet been run.
     * The statement should be a preview and include COMPLETED charges and
     * POSTED acts.
     */
    @Test
    public void testProcessPreview() {
        Party customer = getCustomer();
        BigDecimal feeAmount = new BigDecimal("25.00");
        Lookup accountType = FinancialTestHelper.createAccountType(
                30, DateUnits.DAYS, feeAmount, 30);
        customer.addClassification(accountType);
        save(customer);

        List<FinancialAct> invoices1 = createChargesInvoice(new Money(100));
        FinancialAct invoice1 = invoices1.get(0);
        invoice1.setActivityStartTime(getDatetime("2007-01-01 10:00:00"));
        save(invoices1);

        List<FinancialAct> invoices2 = createChargesInvoice(new Money(100));
        FinancialAct invoice2 = invoices2.get(0);
        invoice2.setActivityStartTime(getDatetime("2007-01-01 10:30:00"));
        invoice2.setStatus(ActStatus.COMPLETED);
        save(invoices2);

        List<FinancialAct> invoices3 = createChargesInvoice(new Money(10));
        FinancialAct invoice3 = invoices3.get(0);
        invoice3.setActivityStartTime(getDatetime("2007-01-01 11:00:00"));
        invoice3.setStatus(ActStatus.IN_PROGRESS);
        save(invoices3);

        List<FinancialAct> invoices4 = createChargesInvoice(new Money(10));
        FinancialAct invoice4 = invoices4.get(0);
        invoice4.setActivityStartTime(getDatetime("2007-01-03 11:00:00"));
        invoice4.setStatus(ActStatus.POSTED);
        save(invoices4);

        Date statementDate = getDate("2007-01-02");

        // process the customer's statement. Should just return the POSTED
        // and COMPLETED invoice acts. The invoice4 invoice won't be included
        List<Act> acts = processStatement(statementDate, customer);
        assertEquals(2, acts.size());
        checkAct(acts.get(0), invoice1, ActStatus.POSTED);
        checkAct(acts.get(1), invoice2, ActStatus.COMPLETED);

        // process the customer's statement for 5/2. Amounts should be overdue
        // and a fee generated.
        statementDate = getDate("2007-02-05");
        acts = processStatement(statementDate, customer);
        assertEquals(4, acts.size());

        // check the 3 invoices.
        checkAct(acts.get(0), invoice1, ActStatus.POSTED);
        checkAct(acts.get(1), invoice2, ActStatus.COMPLETED);
        checkAct(acts.get(2), invoice4, ActStatus.POSTED);
        FinancialAct fee = (FinancialAct) acts.get(3);

        // check the fee. This should not have been saved
        checkAct(fee, "act.customerAccountDebitAdjust", feeAmount);
        assertTrue(fee.isNew());
    }

    /**
     * Tests the {@link StatementProcessor#process(Party)} method for a
     * statement date where end-of-period has been run.
     */
    @Test
    public void testProcessEndOfPeriod() {
        Party customer = getCustomer();
        BigDecimal feeAmount = new BigDecimal("25.00");
        customer.addClassification(createAccountType(30, DateUnits.DAYS,
                                                     feeAmount));
        save(customer);

        List<FinancialAct> invoices1 = createChargesInvoice(new Money(100));
        FinancialAct invoice1 = invoices1.get(0);
        invoice1.setActivityStartTime(getDatetime("2007-01-01 10:00:00"));
        save(invoices1);

        List<FinancialAct> invoices2 = createChargesInvoice(new Money(100));
        FinancialAct invoice2 = invoices2.get(0);
        invoice2.setActivityStartTime(getDatetime("2007-01-01 10:30:00"));
        invoice2.setStatus(ActStatus.COMPLETED);
        save(invoices2);

        List<FinancialAct> invoices3 = createChargesInvoice(new Money(10));
        FinancialAct invoice3 = invoices3.get(0);
        invoice3.setActivityStartTime(getDatetime("2007-01-01 11:00:00"));
        invoice3.setStatus(ActStatus.IN_PROGRESS);
        save(invoices3);

        List<FinancialAct> invoices4 = createChargesInvoice(new Money(10));
        FinancialAct invoice4 = invoices4.get(0);
        invoice4.setActivityStartTime(getDatetime("2007-02-06 11:00:00"));
        invoice4.setStatus(ActStatus.COMPLETED);
        save(invoices4);

        Date statementDate = getDate("2007-02-05");

        // process the customer's statement for 5/2. Amounts should be overdue
        // and a fee generated. COMPLETED acts should be posted.
        // The invoice4 invoice won't be included.
        EndOfPeriodProcessor eop = new EndOfPeriodProcessor(statementDate,
                                                            true,
                                                            getPractice());
        eop.process(customer);

        List<Act> acts = processStatement(statementDate, customer);
        assertEquals(3, acts.size());

        // check the 2 invoices. These can be in any order
        checkAct(acts.get(0), invoice1, ActStatus.POSTED);
        checkAct(acts.get(1), invoice2, ActStatus.POSTED);

        // check the fee. This should have been saved
        FinancialAct fee = (FinancialAct) acts.get(2);
        checkAct(fee, "act.customerAccountDebitAdjust", feeAmount);
        assertFalse(fee.isNew());

        // re-run the statement process for the same date. As the statement
        // has been printed, it should be skipped.
        acts = processStatement(statementDate, customer, false, false);
        assertEquals(0, acts.size());

        // re-run, but this time reprocess printed statements
        acts = processStatement(statementDate, customer, true, true);
        assertEquals(3, acts.size());

        // preview the next statement date
        statementDate = getDate("2007-02-06");
        acts = processStatement(statementDate, customer);
        assertEquals(3, acts.size());
        checkAct(acts.get(0), "act.customerAccountOpeningBalance",
                 new BigDecimal("225.00"));
        checkAct(acts.get(1), invoice4, ActStatus.COMPLETED);
        checkAct(acts.get(2), "act.customerAccountDebitAdjust", feeAmount);
    }

    @Test
    public void testBackDatedStatements() {
        Party customer = getCustomer();

        // create an invoice
        Money amount = new Money(950);
        List<FinancialAct> invoices1 = createChargesInvoice(amount);
        FinancialAct invoice1 = invoices1.get(0);
        invoice1.setActivityStartTime(getDatetime("2007-12-29 10:00:00"));
        save(invoices1);

        // run EOP for 31/12/2007.
        Date statementDate1 = getDate("2007-12-31");
        EndOfPeriodProcessor eop = new EndOfPeriodProcessor(statementDate1,
                                                            true,
                                                            getPractice());
        eop.process(customer);

        // create a payment for 14/1/2008
        FinancialAct payment = createPayment(amount);
        payment.setActivityStartTime(getDatetime("2008-01-14 14:52:00"));
        save(payment);

        // backdate the statement to 1/1/2008. Should only include the
        // opening balance
        Date statementDate3 = getDate("2008-01-01");
        List<Act> acts = processStatement(statementDate3, customer);
        assertEquals(1, acts.size());
        checkAct(acts.get(0), "act.customerAccountOpeningBalance", amount);

        // now forward to the payment date. Statement should include opening
        // balance and payment
        Date statementDate4 = getDate("2008-01-14");
        acts = processStatement(statementDate4, customer);
        assertEquals(2, acts.size());
        checkAct(acts.get(0), "act.customerAccountOpeningBalance", amount);
        checkAct(acts.get(1), "act.customerAccountPayment", amount);

        // run EOP for the 31/1
        Date statementDate5 = getDate("2008-01-31");
        eop = new EndOfPeriodProcessor(statementDate5, true, getPractice());
        eop.process(customer);

        // check statement for 1/2. Balance should  be zero.
        Date statementDate6 = getDate("2008-02-01");
        acts = processStatement(statementDate6, customer);
        assertEquals(1, acts.size());
        checkAct(acts.get(0), "act.customerAccountOpeningBalance",
                 BigDecimal.ZERO);

        // backdate the statement to 14/1/2008. Should only include the
        // opening balance and payment
        processStatement(statementDate4, customer);
        acts = processStatement(statementDate4, customer);
        assertEquals(2, acts.size());
        checkAct(acts.get(0), "act.customerAccountOpeningBalance", amount);
        checkAct(acts.get(1), "act.customerAccountPayment", amount);
    }

    /**
     * Tests the {@link StatementProcessor#process(Party)} method a
     * statement date where end-of-period has not yet been run for an OTC customer.
     * The statement should be a preview and include COMPLETED charges and
     * POSTED acts.
     */
    @Test
    public void testProcessPreviewForOTCCustomer() {
        Party customer = (Party) create(CustomerArchetypes.OTC);
        customer.setName("Z OTC customer");
        save(customer);
        setCustomer(customer);

        List<FinancialAct> invoices1 = createChargesInvoice(new Money(100));
        FinancialAct invoice1 = invoices1.get(0);
        invoice1.setActivityStartTime(getDatetime("2007-01-01 10:00:00"));
        save(invoices1);

        List<FinancialAct> invoices2 = createChargesInvoice(new Money(100));
        FinancialAct invoice2 = invoices2.get(0);
        invoice2.setActivityStartTime(getDatetime("2007-01-01 10:30:00"));
        invoice2.setStatus(ActStatus.COMPLETED);
        save(invoices2);

        List<FinancialAct> invoices3 = createChargesInvoice(new Money(10));
        FinancialAct invoice3 = invoices3.get(0);
        invoice3.setActivityStartTime(getDatetime("2007-01-01 11:00:00"));
        invoice3.setStatus(ActStatus.IN_PROGRESS);
        save(invoices3);

        List<FinancialAct> invoices4 = createChargesInvoice(new Money(10));
        FinancialAct invoice4 = invoices4.get(0);
        invoice4.setActivityStartTime(getDatetime("2007-01-03 11:00:00"));
        invoice4.setStatus(ActStatus.POSTED);
        save(invoices4);

        Date statementDate = getDate("2007-01-02");

        // process the OTC customer's statement. Should just return the POSTED
        // and COMPLETED invoice acts. The invoice4 invoice won't be included
        List<Act> acts = processStatement(statementDate, customer);
        assertEquals(2, acts.size());
        checkAct(acts.get(0), invoice1, ActStatus.POSTED);
        checkAct(acts.get(1), invoice2, ActStatus.COMPLETED);

        // process the customer's statement for 5/2. Amounts are overdue, but as it is an OTC customer,
        // no fees are generated.
        statementDate = getDate("2007-02-05");
        acts = processStatement(statementDate, customer);
        assertEquals(3, acts.size());

        // check the 3 invoices.
        checkAct(acts.get(0), invoice1, ActStatus.POSTED);
        checkAct(acts.get(1), invoice2, ActStatus.COMPLETED);
        checkAct(acts.get(2), invoice4, ActStatus.POSTED);
    }

    /**
     * Helper to process a statement for a customer.
     *
     * @param statementDate the statement date
     * @param customer      the customer
     * @return the acts included in the statement
     */
    private List<Act> processStatement(Date statementDate, Party customer) {
        return processStatement(statementDate, customer, false, true);
    }

    /**
     * Helper to process a statement for a customer.
     *
     * @param statementDate   the statement date
     * @param customer        the customer
     * @param reprint         if <tt>true</tt> reprocess printed statements
     * @param expectStatement if <tt>true</tt> expect a {@link Statement}
     * @return the acts included in the statement. An empty list if
     *         no statement was expected
     */
    private List<Act> processStatement(Date statementDate, Party customer,
                                       boolean reprint,
                                       boolean expectStatement) {
        List<Act> acts;
        StatementRules rules = new StatementRules(getPractice());
        StatementProcessor processor
                = new StatementProcessor(statementDate, getPractice());
        processor.setReprint(reprint);
        Listener listener = new Listener();
        processor.addListener(listener);
        processor.process(customer);
        List<Statement> statements = listener.getStatements();
        if (expectStatement) {
            assertEquals(1, statements.size());
            Statement statement = statements.get(0);
            assertEquals(customer, statement.getCustomer());
            if (!statement.isPreview()) {
                rules.setPrinted(customer, statement.getStatementDate());
            }
            acts = getActs(statement);
        } else {
            assertEquals(0, statements.size());
            acts = Collections.emptyList();
        }
        return acts;
    }

    private class Listener implements ProcessorListener<Statement> {

        private List<Statement> statements = new ArrayList<Statement>();

        private List<Statement> getStatements() {
            return statements;
        }

        public void process(Statement statement) {
            statements.add(statement);
        }

    }

    private List<Act> getActs(Statement event) {
        List<Act> result = new ArrayList<Act>();
        for (Act act : event.getActs()) {
            result.add(act);
        }
        return result;
    }
}
