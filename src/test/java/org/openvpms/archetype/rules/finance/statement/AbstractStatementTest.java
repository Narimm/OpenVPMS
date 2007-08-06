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

import org.openvpms.archetype.rules.finance.account.AbstractCustomerAccountTest;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;

import java.sql.Timestamp;
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
     * Helper to create a date-time given a string of the form
     * <em>yyyy-mm-dd hh:mm:ss</em>.
     *
     * @param value the value
     * @return the corresponding date-time
     */
    protected Date getDatetime(String value) {
        return Timestamp.valueOf(value);
    }

    /**
     * Helper to create a date given a string of the form <em>yyyy-mm-dd</em>.
     *
     * @param value the value
     * @return the corresponding date
     */
    protected Date getDate(String value) {
        return Timestamp.valueOf(value + " 0:0:0");
    }

    /**
     * Returns all acts for a particular statement date.
     *
     * @param customer      the customer
     * @param statementDate the statement date
     * @return the acts for the statement date
     */
    protected List<Act> getActs(Party customer, Date statementDate) {
        Date timestamp = new StatementRules().getStatementTimestamp(
                statementDate);
        StatementActHelper helper
                = new StatementActHelper(getArchetypeService());
        Iterable<Act> acts = helper.getActs(customer, timestamp);
        List<Act> result = new ArrayList<Act>();
        for (Act act : acts) {
            result.add(act);
        }
        return result;
    }

}
