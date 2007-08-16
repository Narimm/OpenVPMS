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

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.AccountType;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.archetype.rules.finance.tax.TaxRules;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


/**
 * Statement rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class StatementRules {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Customer account rules.
     */
    private final CustomerAccountRules account;

    /**
     * Statement act helper.
     */
    private final StatementActHelper acts;

    /**
     * Tax rules.
     */
    private final TaxRules tax;


    /**
     * Creates a new <tt>StatementRules</tt>.
     */
    public StatementRules() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Creates a new <tt>StatementRules</tt>.
     *
     * @param service the archetype service
     */
    public StatementRules(IArchetypeService service) {
        this.service = service;
        account = new CustomerAccountRules(service);
        acts = new StatementActHelper(service);
        tax = new TaxRules(service);
    }

    /**
     * Determines if a customer has had end-of-period run on or after a
     * particular date.
     *
     * @param date the date
     * @return <tt>true</tt> if end-of-period has been run on or after the date
     * @throws ArchetypeServiceException for any archetype service error
     */
    public boolean hasStatement(Party customer, Date date) {
        return acts.hasStatement(customer, date);
    }

    /**
     * Marks a statement as being printed.
     *
     * @param statementDate the statement date
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void setPrinted(Party customer, Date statementDate) {
        FinancialAct act = acts.getClosingBalance(customer, statementDate);
        if (act != null) {
            act.setPrinted(true);
            service.save(act);
        }
    }

    /**
     * Returns the account fee for a customer, based on the customer's
     * account type.
     * A non-zero account fee will be returned if:
     * <ul>
     * <li>the customer has an account type
     * (<em>lookup.customerAccountType</em>);</li>
     * <li>there is a non-zero overdue balance for the account fee date
     * (derived from the specified date + <tt>accountFeeDays</tt>);</li>
     * <li>the overdue balance is greater than <tt>accountFeeBalance</tt>; and
     * </li>
     * <li>the account fee is greater than <tt>accountFeeMinimum</tt>.
     * The account fee is calculated as:
     * <ul>
     * <li><tt>overdue * accountFeeAmount</tt> if the <tt>accountFee</tt> is
     * <tt>"PERCENTAGE"</tt>; or</li>
     * <li><tt>accountFeeAmount</tt> if the <tt>accountFee</tt> is
     * <tt>"FIXED"</tt></li>
     * </ul></li>
     * </ul>
     *
     * @param customer the customer
     * @param date     the statement date
     * @return the account fee, or <tt>BigDecimal.ZERO</tt> if there is no fee
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal getAccountFee(Party customer, Date date) {
        BigDecimal result = BigDecimal.ZERO;
        AccountType accountType = getAccountType(customer);
        if (accountType != null) {
            Date feeDate = accountType.getAccountFeeDate(date);
            BigDecimal overdue = account.getOverdueBalance(customer, feeDate);
            BigDecimal feeBalance = accountType.getAccountFeeBalance();
            if (overdue.compareTo(BigDecimal.ZERO) != 0
                    && overdue.compareTo(feeBalance) >= 0) {
                BigDecimal fee = accountType.getAccountFee(overdue);
                if (fee.compareTo(accountType.getAccountFeeMinimum()) >= 0) {
                    result = fee;
                }
            }
        }
        return result;
    }

    /**
     * Creates an <em>act.customerAccountDebitAdjust</em> for the customer
     * with the specified fee.
     *
     * @param customer  the customer
     * @param fee       the accounting fee
     * @param startTime the act start time
     */
    public FinancialAct createAccountingFeeAdjustment(Party customer,
                                                      BigDecimal fee,
                                                      Date startTime) {
        FinancialAct act = (FinancialAct) service.create(
                "act.customerAccountDebitAdjust");
        ActBean bean = new ActBean(act, service);
        bean.addParticipation("participation.customer", customer);
        act.setTotal(new Money(fee));
        act.setActivityStartTime(startTime);
        act.setStatus(ActStatus.POSTED);
        tax.calculateTax(act);
        bean.setValue("notes", "Accounting Fee"); // TODO - localise
        return act;
    }

    /**
     * Applies an accounting fee to a customer account.
     * This saves an <em>act.customerAccountDebitAdjust</em> for the customer
     * with the specified fee.
     *
     * @param customer  the customer
     * @param fee       the accounting fee
     * @param startTime the fee start time
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void applyAccountingFee(Party customer, BigDecimal fee,
                                   Date startTime) {
        FinancialAct act = createAccountingFeeAdjustment(customer, fee,
                                                         startTime);
        service.save(act);
    }

    /**
     * Helper to return the account type for a customer.
     *
     * @param customer the customer
     * @return the account type, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    private AccountType getAccountType(Party customer) {
        IMObjectBean bean = new IMObjectBean(customer, service);
        List<Lookup> accountTypes = bean.getValues("type", Lookup.class);
        if (!accountTypes.isEmpty()) {
            return new AccountType(accountTypes.get(0), service);
        }
        return null;
    }
}