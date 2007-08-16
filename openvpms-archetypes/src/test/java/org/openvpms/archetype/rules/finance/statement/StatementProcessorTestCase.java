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

import org.openvpms.archetype.component.processor.ProcessorListener;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
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
    public void testStatementDate() {
        Date now = new Date();
        try {
            new StatementProcessor(now);
            fail("Expected StatementProcessorException to be thrown");
        } catch (StatementProcessorException expected) {
            assertEquals(
                    StatementProcessorException.ErrorCode.InvalidStatementDate,
                    expected.getErrorCode());
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        try {
            new StatementProcessor(calendar.getTime());
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
    public void testProcessPreview() {
        Party customer = getCustomer();
        BigDecimal feeAmount = new BigDecimal("25.00");
        customer.addClassification(createAccountType(30, DateUnits.DAYS,
                                                     feeAmount));
        save(customer);

        FinancialAct invoice1 = createChargesInvoice(new Money(100));
        invoice1.setActivityStartTime(getDatetime("2007-01-01 10:00:00"));
        save(invoice1);

        FinancialAct invoice2 = createChargesInvoice(new Money(100));
        invoice2.setActivityStartTime(getDatetime("2007-01-01 10:30:00"));
        invoice2.setStatus(ActStatus.COMPLETED);
        save(invoice2);

        FinancialAct invoice3 = createChargesInvoice(new Money(10));
        invoice3.setActivityStartTime(getDatetime("2007-01-01 11:00:00"));
        invoice3.setStatus(ActStatus.IN_PROGRESS);
        save(invoice3);

        FinancialAct invoice4 = createChargesInvoice(new Money(10));
        invoice4.setActivityStartTime(getDatetime("2007-01-03 11:00:00"));
        invoice4.setStatus(ActStatus.POSTED);
        save(invoice4);

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
    public void testProcessEndOfPeriod() {
        Party customer = getCustomer();
        BigDecimal feeAmount = new BigDecimal("25.00");
        customer.addClassification(createAccountType(30, DateUnits.DAYS,
                                                     feeAmount));
        save(customer);

        FinancialAct invoice1 = createChargesInvoice(new Money(100));
        invoice1.setActivityStartTime(getDatetime("2007-01-01 10:00:00"));
        save(invoice1);

        FinancialAct invoice2 = createChargesInvoice(new Money(100));
        invoice2.setActivityStartTime(getDatetime("2007-01-01 10:30:00"));
        invoice2.setStatus(ActStatus.COMPLETED);
        save(invoice2);

        FinancialAct invoice3 = createChargesInvoice(new Money(10));
        invoice3.setActivityStartTime(getDatetime("2007-01-01 11:00:00"));
        invoice3.setStatus(ActStatus.IN_PROGRESS);
        save(invoice3);

        FinancialAct invoice4 = createChargesInvoice(new Money(10));
        invoice4.setActivityStartTime(getDatetime("2007-02-06 11:00:00"));
        invoice4.setStatus(ActStatus.COMPLETED);
        save(invoice4);

        Date statementDate = getDate("2007-02-05");

        // process the customer's statement for 5/2. Amounts should be overdue
        // and a fee generated. COMPLETED acts should be posted.
        // The invoice4 invoice won't be included.
        EndOfPeriodProcessor eop = new EndOfPeriodProcessor(statementDate,
                                                            true);
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
        StatementRules rules = new StatementRules();
        StatementProcessor processor = new StatementProcessor(statementDate);
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
