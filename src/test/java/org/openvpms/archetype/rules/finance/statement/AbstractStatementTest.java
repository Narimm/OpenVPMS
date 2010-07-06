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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.archetype.rules.finance.account.AbstractCustomerAccountTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Base class for statement test cases.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AbstractStatementTest extends AbstractCustomerAccountTest {

    /**
     * The practice.
     */
    private Party practice;

    /**
     * Returns the practice.
     *
     * @return the practice
     */
    protected Party getPractice() {
        if (practice == null) {
            practice = (Party) create("party.organisationPractice");
        }
        return practice;
    }

    /**
     * Helper to create a date-time given a string of the form
     * <em>yyyy-mm-dd hh:mm:ss</em>.
     *
     * @param value the value
     * @return the corresponding date-time
     */
    protected Date getDatetime(String value) {
        return TestHelper.getDatetime(value);
    }

    /**
     * Helper to create a date given a string of the form <em>yyyy-mm-dd</em>.
     *
     * @param value the value
     * @return the corresponding date
     */
    protected Date getDate(String value) {
        return TestHelper.getDate(value);
    }

    /**
     * Returns all acts for a statement date.
     *
     * @param customer      the customer
     * @param statementDate the statement date
     * @return the acts for the statement date
     */
    protected List<Act> getActs(Party customer, Date statementDate) {
        StatementActHelper helper
                = new StatementActHelper(getArchetypeService());
        Date timestamp = helper.getStatementTimestamp(statementDate);
        Iterable<Act> acts = helper.getActs(customer, timestamp);
        List<Act> result = new ArrayList<Act>();
        for (Act act : acts) {
            result.add(act);
        }
        return result;
    }

    /**
     * Returns all posted acts for a statement date.
     *
     * @param customer      the customer
     * @param statementDate the statement date
     * @return the posted acts for the statement date
     */
    protected List<Act> getPostedActs(Party customer, Date statementDate) {
        StatementActHelper helper
                = new StatementActHelper(getArchetypeService());
        Date open = helper.getOpeningBalanceTimestamp(customer, statementDate);
        Date close = helper.getClosingBalanceTimestamp(customer, statementDate,
                                                       open);
        Iterable<Act> acts = helper.getPostedActs(customer, open, close, true);
        List<Act> result = new ArrayList<Act>();
        for (Act act : acts) {
            result.add(act);
        }
        return result;
    }

    /**
     * Verifies that an act matches the expected short name and amount, and
     * is POSTED.
     *
     * @param act       the act to verify
     * @param shortName the expected archetype short name
     * @param amount    the expected amount
     */
    protected void checkAct(Act act, String shortName, BigDecimal amount) {
        checkAct(act, shortName, amount, FinancialActStatus.POSTED);
    }

    /**
     * Verifies that an act matches the expected short name and amount, and
     * status.
     *
     * @param act       the act to verify
     * @param shortName the expected archetype short name
     * @param amount    the expected amount
     * @param status    the expected act status
     */
    protected void checkAct(Act act, String shortName, BigDecimal amount,
                            String status) {
        assertTrue(TypeHelper.isA(act, shortName));
        assertEquals(status, act.getStatus());
        checkEquals(amount, ((FinancialAct) act).getTotal());
    }

    /**
     * Verfies an act matches the expected act and status.
     *
     * @param act      the act to verify
     * @param expected the expected act
     * @param status   the expected status
     */
    protected void checkAct(Act act, FinancialAct expected, String status) {
        assertEquals(expected, act);
        checkAct(act, expected.getArchetypeId().getShortName(),
                 expected.getTotal(), status);
    }

    /**
     * Verifies an act appears in a list of acts with the expected status.
     *
     * @param acts     the acts
     * @param expected the expected act
     * @param status   the expected status
     */
    protected void checkAct(List<Act> acts, FinancialAct expected,
                            String status) {
        for (Act act : acts) {
            if (act.equals(expected)) {
                checkAct(act, expected, status);
                return;
            }
        }
        fail("Expected act " + expected + " not found in acts");
    }
}
