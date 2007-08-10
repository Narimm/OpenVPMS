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

        Date statementDate = getDate("2007-01-02");

        // process the customer's statement. Should just return the POSTED
        // and COMPLETED invoice acts
        List<Act> acts = processStatement(statementDate, customer);
        assertEquals(2, acts.size());
        checkAct(acts.get(0), invoice1, ActStatus.POSTED);
        checkAct(acts.get(1), invoice2, ActStatus.COMPLETED);

        // process the customer's statement for 5/2. Amounts should be overdue
        // and a fee generated.
        statementDate = getDate("2007-02-05");
        acts = processStatement(statementDate, customer);
        assertEquals(3, acts.size());

        // check the 2 invoices. These can be in any order
        checkAct(acts.get(0), invoice1, ActStatus.POSTED);
        checkAct(acts.get(1), invoice2, ActStatus.COMPLETED);
        FinancialAct fee = (FinancialAct) acts.get(2);

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

        Date statementDate = getDate("2007-02-05");

        // process the customer's statement for 5/2. Amounts should be overdue
        // and a fee generated. COMPLETED acts should be posted.
        EndOfPeriodProcessor eop = new EndOfPeriodProcessor(statementDate,
                                                            true);
        eop.process(customer);

        List<Act> acts = processStatement(statementDate, customer);
        assertEquals(4, acts.size());

        // check the 2 invoices. These can be in any order
        checkAct(acts.get(0), invoice1, ActStatus.POSTED);
        checkAct(acts.get(1), invoice2, ActStatus.POSTED);

        // check the fee. This should have been saved
        FinancialAct fee = (FinancialAct) acts.get(2);
        checkAct(fee, "act.customerAccountDebitAdjust", feeAmount);
        assertFalse(fee.isNew());

        // check the closing balance
        FinancialAct close = (FinancialAct) acts.get(3);
        checkAct(close, "act.customerAccountClosingBalance",
                 new BigDecimal("225.00"));

        // preview the next statement date
        statementDate = getDate("2007-02-06");
        acts = processStatement(statementDate, customer);
        assertEquals(2, acts.size());
        checkAct(acts.get(0), "act.customerAccountOpeningBalance",
                 new BigDecimal("225.00"));
        checkAct(acts.get(1), "act.customerAccountDebitAdjust", feeAmount);
    }

    /**
     * Helper to process a statement for a customer.
     *
     * @param statementDate the statement date
     * @param customer      the customer
     * @return the acts included in the statement
     */
    private List<Act> processStatement(Date statementDate, Party customer) {
        StatementProcessor processor = new StatementProcessor(statementDate);
        Listener listener = new Listener();
        processor.addListener(listener);
        processor.process(customer);
        List<Statement> events = listener.getEvents();
        assertEquals(1, events.size());
        Statement event = events.get(0);
        assertEquals(customer, event.getCustomer());
        return getActs(event);
    }

    private class Listener implements ProcessorListener<Statement> {
        private List<Statement> events = new ArrayList<Statement>();

        private List<Statement> getEvents() {
            return events;
        }

        public void process(Statement event) {
            events.add(event);
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
