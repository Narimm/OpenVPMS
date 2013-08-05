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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.finance.statement;

import org.openvpms.archetype.component.processor.Processor;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.CLOSING_BALANCE;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.OPENING_BALANCE;
import static org.openvpms.archetype.rules.finance.statement.StatementProcessorException.ErrorCode.InvalidStatementDate;


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
     * The statement date timestamp.
     */
    private final Date timestamp;

    /**
     * If <tt>true</tt>, post completed charges.
     */
    private final boolean postCompletedCharges;

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
     * @param practice             the practice
     * @throws StatementProcessorException if the statement date is invalid
     */
    public EndOfPeriodProcessor(Date statementDate,
                                boolean postCompletedCharges,
                                Party practice) {
        this(statementDate, postCompletedCharges, practice,
             ArchetypeServiceHelper.getArchetypeService(),
             LookupServiceHelper.getLookupService());
    }

    /**
     * Creates a new <tt>EndOfPeriodProcessor</tt>.
     *
     * @param statementDate        the statement date. Must be a date prior to
     *                             today.
     * @param postCompletedCharges if <tt>true</tt>, post completed charges
     * @param practice             the practice
     * @param service              the archetype service
     * @param lookups              the lookup service
     * @throws StatementProcessorException if the statement date is invalid
     */
    public EndOfPeriodProcessor(Date statementDate,
                                boolean postCompletedCharges,
                                Party practice, IArchetypeService service,
                                ILookupService lookups) {
        this.service = service;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        if (calendar.getTime().compareTo(statementDate) < 0) {
            throw new StatementProcessorException(InvalidStatementDate,
                                                  statementDate);
        }
        acts = new StatementActHelper(service);
        account = new CustomerAccountRules(service);
        statement = new StatementRules(practice, service, lookups);
        timestamp = acts.getStatementTimestamp(statementDate);
        this.postCompletedCharges = postCompletedCharges;
    }

    /**
     * Process a customer.
     *
     * @param customer the customer to process
     * @throws OpenVPMSException for any error
     */
    public void process(Party customer) {
        StatementPeriod period = new StatementPeriod(customer, timestamp,
                                                     acts);
        if (!period.hasStatement()) {
            boolean needStatement = false;
            Date open = period.getOpeningBalanceTimestamp();
            Date close = period.getClosingBalanceTimestamp();
            if (postCompletedCharges) {
                for (Act act : acts.getCompletedCharges(
                        customer, timestamp, open, close)) {
                    post(act, period);
                    needStatement = true;
                }
            }
            BigDecimal balance = null;
            if (!needStatement) {
                balance = account.getBalance(customer, open, close,
                                             period.getOpeningBalance());
                if (balance.compareTo(BigDecimal.ZERO) == 0) {
                    if (acts.hasAccountActivity(customer, open, close)) {
                        needStatement = true;
                    }
                } else {
                    needStatement = true;
                }
            }
            if (needStatement) {
                if (balance == null) {
                    balance = account.getBalance(customer, open, close,
                                                 period.getOpeningBalance());
                }
                createPeriodEnd(customer, period, balance);
            }
        }
    }

    /**
     * Generates an <em>act.customerAccountClosingBalance</em> and
     * <em>act.customerAccountOpeningBalance</em> for the specified customer.
     *
     * @param customer the customer
     * @param period   the statement period
     * @param balance  the closing balance
     * @throws ArchetypeServiceException for any error
     */
    public void createPeriodEnd(Party customer, StatementPeriod period,
                                BigDecimal balance) {
        BigDecimal overdue = BigDecimal.ZERO;
        FinancialAct fee = null;
        if (balance.compareTo(BigDecimal.ZERO) != 0) {
            Date overdueDate = account.getOverdueDate(customer, timestamp);
            overdue = account.getOverdueBalance(customer, timestamp,
                                                overdueDate);
            if (overdue.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal accountFee = statement.getAccountFee(
                        customer, timestamp);
                if (accountFee.compareTo(BigDecimal.ZERO) != 0) {
                    Date feeStartTime = period.getFeeTimestamp();
                    fee = statement.createAccountingFeeAdjustment(customer,
                                                                  accountFee,
                                                                  feeStartTime);
                    balance = balance.add(accountFee);
                }
            }
        }
        boolean reverseCredit = false;
        if (balance.signum() == -1) {
            balance = balance.negate();
            // need to switch the credit/debit flags on the closing and
            // opening balances respectively.
            reverseCredit = true;
        }

        FinancialAct close = createAct(CLOSING_BALANCE, customer, balance);
        FinancialAct open = createAct(OPENING_BALANCE, customer, balance);

        if (reverseCredit) {
            close.setCredit(!close.isCredit());
            open.setCredit(!open.isCredit());
        }

        ActBean bean = new ActBean(close, service);
        bean.setValue("overdueBalance", overdue);

        // ensure the acts are ordered correctly, ie. close before open
        Date closeTime = period.getClosingBalanceTimestamp();
        Date openTime = new Date(closeTime.getTime() + 1000);
        close.setActivityStartTime(closeTime);
        open.setActivityStartTime(openTime);
        if (fee != null) {
            service.save(Arrays.asList((IMObject) fee, close, open));
        } else {
            service.save(Arrays.asList((IMObject) close, open));
        }
    }

    /**
     * Posts a completed charge act. This sets the status to <tt>POSTED<tt>,
     * and the startTime to 1 second less than the statement timestamp.
     *
     * @param act    the act to post
     * @param period the statement period
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void post(Act act, StatementPeriod period) {
        act.setActivityStartTime(period.getCompletedChargeTimestamp());
        act.setStatus(ActStatus.POSTED);
        service.save(act);
    }

    /**
     * Helper to create an act for a customer.
     *
     * @param shortName the act short name
     * @param customer  the customer
     * @param total     the act total
     * @return a new act
     */
    private FinancialAct createAct(String shortName, Party customer,
                                   BigDecimal total) {
        FinancialAct act = (FinancialAct) service.create(shortName);
        Date startTime = new Date();
        act.setActivityStartTime(startTime);
        ActBean bean = new ActBean(act, service);
        bean.addParticipation("participation.customer", customer);
        act.setTotal(new Money(total));
        return act;
    }

}
