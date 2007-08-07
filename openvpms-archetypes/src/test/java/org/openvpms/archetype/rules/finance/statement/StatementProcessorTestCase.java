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
import java.sql.Timestamp;
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
     * Tests the {@link StatementProcessor#process(Party)} method.
     */
    public void testProcess() {
        Party customer = getCustomer();
        BigDecimal feeAmount = new BigDecimal("25.00");
        customer.addClassification(createAccountType(30, DateUnits.DAYS,
                                                     feeAmount));
        save(customer);

        FinancialAct invoice = createChargesInvoice(new Money(100));
        invoice.setActivityStartTime(Timestamp.valueOf("2007-01-01 10:00:00"));
        save(invoice);
        Date statementDate = Timestamp.valueOf("2007-01-02 10:00:00");

        // process the customer's statement. Should just return the invoice
        // acts
        List<Act> acts = processStatement(statementDate, customer);
        assertEquals(1, acts.size());
        checkAct(acts.get(0), invoice, ActStatus.POSTED);

        // process the customer's statement for 5/2. Amounts should be overdue
        // and a fee generated.
        statementDate = Timestamp.valueOf("2007-02-05 12:00:00");
        acts = processStatement(statementDate, customer);
        assertEquals(2, acts.size());
        checkAct(acts.get(0), invoice, ActStatus.POSTED);
        FinancialAct fee = (FinancialAct) acts.get(1);
        checkAct(fee, "act.customerAccountDebitAdjust", feeAmount);
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
        List<StatementEvent> events = listener.getEvents();
        assertEquals(1, events.size());
        StatementEvent event = events.get(0);
        assertEquals(customer, event.getCustomer());
        return getActs(event);
    }

    private class Listener implements ProcessorListener<StatementEvent> {
        private List<StatementEvent> events = new ArrayList<StatementEvent>();

        private List<StatementEvent> getEvents() {
            return events;
        }

        public void process(StatementEvent event) {
            events.add(event);
        }
    }

    private List<Act> getActs(StatementEvent event) {
        List<Act> result = new ArrayList<Act>();
        for (Act act : event.getActs()) {
            result.add(act);
        }
        return result;
    }
}
