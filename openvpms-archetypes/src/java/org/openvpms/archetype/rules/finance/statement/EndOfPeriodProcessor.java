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

import org.openvpms.archetype.component.processor.Processor;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import static org.openvpms.archetype.rules.finance.statement.StatementProcessorException.ErrorCode.InvalidStatementDate;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;


/**
 * End-of-period statement processor.
 * <p/>
 * This performs end-of-period for a customer. End-of-period processing
 * includes:
 * <ul>
 * <li>the addition of accounting fees, if the customer has overdue balances
 * that incur a fee; and
 * <li>the creation of closing and opening balance acts</li>
 * </ul>
 * <p/>
 * End-of-period processing only occurs if the customer has no statement
 * on or after the specified statement date and:
 * <ul>
 * <li>there are COMPLETED invoices to be POSTED; or
 * <li>there is a non-zero balance; or
 * <li>there is a zero balance but there has been account activity since
 * the last opening balance</li>
 * </ul>
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class EndOfPeriodProcessor implements Processor<Party> {

    /**
     * The statement date.
     */
    private final Date statementDate;

    /**
     * If <tt>true</tt>, post completed charges.
     */
    private final boolean postCompletedCharges;

    /**
     * The statement timestamp.
     */
    private final Date timetamp;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Statement act query helper.
     */
    private final StatementActHelper acts;

    /**
     * Customer account rules.
     */
    private final CustomerAccountRules account;

    /**
     * Statement rules.
     */
    private final StatementRules statement;


    /**
     * Creates a new <tt>EndOfPeriodProcessor</tt>.
     *
     * @param statementDate        the statement date. Must be a date prior to
     *                             today.
     * @param postCompletedCharges if <tt>true</tt>, post completed charges
     * @throws StatementProcessorException if the statement date is invalid
     */
    public EndOfPeriodProcessor(Date statementDate,
                                boolean postCompletedCharges) {
        this(statementDate, postCompletedCharges,
             ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Creates a new <tt>EndOfPeriodProcessor</tt>.
     *
     * @param statementDate        the statement date. Must be a date prior to
     *                             today.
     * @param postCompletedCharges if <tt>true</tt>, post completed charges
     * @param service              the archetype service
     * @throws StatementProcessorException if the statement date is invalid
     */
    public EndOfPeriodProcessor(Date statementDate,
                                boolean postCompletedCharges,
                                IArchetypeService service) {
        this.service = service;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        if (calendar.getTime().compareTo(statementDate) < 0) {
            throw new StatementProcessorException(InvalidStatementDate,
                                                  statementDate);
        }
        acts = new StatementActHelper(service);
        account = new CustomerAccountRules(service);
        statement = new StatementRules(service);
        this.statementDate = statementDate;
        this.postCompletedCharges = postCompletedCharges;
        timetamp = statement.getStatementTimestamp(statementDate);
    }

    /**
     * Process a customer.
     *
     * @param customer the customer to process
     * @throws OpenVPMSException for any error
     */
    public void process(Party customer) {
        if (!statement.hasStatement(customer, statementDate)) {
            Period period = new Period(customer);
            boolean needStatement = false;
            if (postCompletedCharges) {
                for (Act act : acts.getCompletedCharges(customer, statementDate,
                                                        period.getOpen(),
                                                        period.getClose())) {
                    post(act);
                    needStatement = true;
                }
            }
            if (!needStatement) {
                BigDecimal balance = account.getBalance(customer, timetamp);
                if (balance.compareTo(BigDecimal.ZERO) == 0) {
                    if (acts.hasAccountActivity(customer, period.getOpen(),
                                                period.getClose())) {
                        needStatement = true;
                    }

                } else {
                    needStatement = true;
                }
            }
            if (needStatement) {
                BigDecimal fee = statement.getAccountFee(customer, timetamp);
                if (fee.compareTo(BigDecimal.ZERO) != 0) {
                    Date feeStartTime = getTimestamp(1);
                    statement.applyAccountingFee(customer, fee, feeStartTime);
                }
                Date endTimestamp = getTimestamp(2);
                account.createPeriodEnd(customer, endTimestamp);
            }
        }
    }

    /**
     * Posts a completed charge act. This sets the status to <tt>POSTED<tt>,
     * and the startTime to 1 second less than the statement timestamp.
     *
     * @param act the act to post
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void post(Act act) {
        act.setActivityStartTime(getTimestamp(-1));
        act.setStatus(ActStatus.POSTED);
        service.save(act);
    }

    /**
     * Returns a timestamp relative to the statement timestamp.
     *
     * @param addSeconds the no. of seconds to add
     * @return the new timestamp
     */
    private Date getTimestamp(int addSeconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(timetamp);
        calendar.add(Calendar.SECOND, addSeconds);
        return calendar.getTime();
    }

    private class Period {
        private Date open;
        private Date close;
        private final Party customer;
        private boolean init;

        public Period(Party customer) {
            this.customer = customer;
        }

        public Date getOpen() {
            if (!init) {
                init();
            }
            return open;
        }

        public Date getClose() {
            if (!init) {
                init();
            }
            return close;
        }

        private void init() {
            open = acts.getOpeningBalanceTimestamp(customer, statementDate);
            close = acts.getClosingBalanceTimestamp(customer, statementDate,
                                                    open);
            init = true;
        }
    }
}
