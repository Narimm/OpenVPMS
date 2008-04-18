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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.finance.statement;

import org.openvpms.component.business.domain.im.party.Party;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;


/**
 * Customer statement period information.
 * <p/>
 * TODO: there are a number of deficiencies in statement processsing, namely
 * that it relies on 'magic' timestamps for the opening and closing balance
 * and fee acts. These are relative to the statementTimestamp, which is set to
 * <em>&lt;statementDate&gt; 23:59:00</em>.
 * <p/>
 * The closing balance timestamp is <em>statementTimestamp + 2 secs</em>
 * <p/>
 * The opening balance timestamp is <em>statementTimestamp + 3 secs</em>
 * <p/>
 * The timestamp for completed charges being posted is
 * <em>statementTimestamp - 1 sec</em>
 * <p/>
 * The timestamp for fee acts is <em>statementTimestamp + 1 sec</em>
 * <p/>
 * This leads to the following limitations/restrictions:
 * <ul>
 * <li>Account acts cannot be backdated. If an account act has it start time
 * backdated after end of period is run, it will not be included in a subsequent
 * end of period.
 * </li>
 * <li>End of period must be run for days prior to the current day, to avoid
 * excluding acts up to the closing timestamp.
 * </li>
 * </ul>
 * A better approach would be to link all account acts to a 'current statement'
 * act. This would remove the need for magic timestamps, and enable end of
 * period to be run at any time.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class StatementPeriod {

    /**
     * The statement date timestamp.
     */
    private Date statementTime;


    /**
     * Determines if there is a statement for the period.
     */
    private boolean statement;

    /**
     * The opening balance timestamp. May be <tt>null</tt>
     */
    private Date openTime;

    /**
     * The closing balance timestamp.
     */
    private Date closeTime;

    /**
     * The opening balance, as of the opening balance timestamp.
     */
    private BigDecimal openingBalance = BigDecimal.ZERO;

    /**
     * Determines if the statement has been printed.
     */
    private boolean printed;


    /**
     * Creates a new <tt>StatementPeriod</tt>.
     *
     * @param customer      the customer
     * @param statementDate the statement date
     * @param helper        the statement helper
     */
    public StatementPeriod(Party customer, Date statementDate,
                           StatementActHelper helper) {
        statementTime = helper.getStatementTimestamp(statementDate);
        StatementActHelper.ActState open = helper.getOpeningBalanceState(
                customer, statementTime);
        if (open != null) {
            openTime = open.getStartTime();
            openingBalance = open.getAmount();
        }
        StatementActHelper.ActState close = helper.getClosingBalanceState(
                customer, statementTime, openTime);
        if (close != null) {
            closeTime = close.getStartTime();
            printed = close.isPrinted();
            statement = true;
        } else {
            closeTime = getTimestamp(2);
        }
    }

    /**
     * Returns the statement date timestamp.
     *
     * @return the statement date timestamp
     */
    public Date getStatementTimestamp() {
        return statementTime;
    }

    /**
     * Determines if there is a statement for the period.
     *
     * @return <tt>true</tt> if there is a statement for the period
     */
    public boolean hasStatement() {
        return statement;
    }

    /**
     * Returns the opening balance start time.
     *
     * @return the opening balance start time. May be <tt>null</tt>
     */
    public Date getOpeningBalanceTimestamp() {
        return openTime;
    }

    /**
     * Returns the opening balance.
     *
     * @return the opening balance
     */
    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }

    /**
     * Returns the closing balance start time.
     *
     * @return the closing balance start time. If there is no statement for
     *         the period, it is set to statementTimestamp + 2 secs
     */
    public Date getClosingBalanceTimestamp() {
        return closeTime;
    }

    /**
     * Returns a timestamp to assign to charges being posted as part of
     * the end of period process.
     *
     * @return a timestamp relative to the statement timestamp
     */
    public Date getCompletedChargeTimestamp() {
        return getTimestamp(-1);
    }

    /**
     * Returns a timestamp to assign to fee acts.
     *
     * @return a timestamp relative to the statement timestamp
     */
    public Date getFeeTimestamp() {
        return getTimestamp(1);
    }

    /**
     * Determines if the statement has been printed.
     *
     * @return <tt>true</tt> if the statement has been printed
     */
    public boolean isPrinted() {
        return printed;
    }

    /**
     * Returns a timestamp relative to the statement timestamp.
     *
     * @param addSeconds the no. of seconds to add
     * @return the new timestamp
     */
    private Date getTimestamp(int addSeconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(statementTime);
        calendar.add(Calendar.SECOND, addSeconds);
        return calendar.getTime();
    }

}
