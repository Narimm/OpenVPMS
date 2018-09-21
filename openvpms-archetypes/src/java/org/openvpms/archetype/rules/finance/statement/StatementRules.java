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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.finance.statement;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.AccountType;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.account.CustomerAccountQueryFactory;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.archetype.rules.finance.tax.CustomerTaxRules;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.IterableIMObjectQuery;
import org.openvpms.component.system.common.util.IterableChain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.OPENING_BALANCE;


/**
 * Statement rules.
 *
 * @author Tim Anderson
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
    private final CustomerTaxRules tax;


    /**
     * Constructs a {@link StatementRules}.
     *
     * @param practice the practice
     * @param service  the archetype service
     * @param rules    the customer account rules
     */
    public StatementRules(Party practice, IArchetypeService service, CustomerAccountRules rules) {
        this.service = service;
        account = rules;
        acts = new StatementActHelper(service);
        tax = new CustomerTaxRules(practice, service);
    }

    /**
     * Determines if a customer has had end-of-period run on or after a
     * particular date.
     *
     * @param customer the customer
     * @param date     the date
     * @return {@code true} if end-of-period has been run on or after the date
     * @throws ArchetypeServiceException for any archetype service error
     */
    public boolean hasStatement(Party customer, Date date) {
        return acts.hasStatement(customer, date);
    }

    /**
     * Preview acts that will be in a customer's next statement.
     * <p>
     * Returns all POSTED statement acts and optionally COMPLETED charge acts for the given statement date. <p/>
     * This adds (but does not save) an accounting fee act if an accounting fee is required.
     * <p>
     * This should be invoked given a {@code from} timestamp of the last opening balance for the customer. If the
     * customer has no opening balance, {@code null} may be supplied.
     *
     * @param customer                the customer
     * @param date                    the statement date, used to calculate the account fee
     * @param includeCompletedCharges if {@code true} include COMPLETED charges
     * @param includeFee              if {@code true}, include an accounting fee if one is required
     * @return the statement acts
     * @throws ArchetypeServiceException for any archetype service error
     * @throws ArchetypeQueryException   for any archetype query error
     */
    public Iterable<FinancialAct> getStatementPreview(Party customer, Date date, boolean includeCompletedCharges,
                                                      boolean includeFee) {
        FinancialAct opening = getLastOpeningBalance(customer);
        return getStatementPreview(customer, opening != null ? opening.getActivityStartTime() : null, date,
                                   includeCompletedCharges, includeFee);
    }

    /**
     * Preview acts that will be in a customer's next statement.
     * <p>
     * Returns all POSTED statement acts and COMPLETED charge acts for a customer between two dates. <p/>
     * This adds (but does not save) an accounting fee act if an accounting fee is required.
     * <p>
     * This should be invoked given a {@code from} timestamp of the last opening balance for the customer. If the
     * customer has no opening balance, {@code null} may be supplied.
     *
     * @param customer                the customer
     * @param from                    the from date. May be {@code null}
     * @param to                      the to date. This corresponds to the statement date
     * @param includeCompletedCharges if {@code true} include COMPLETED charges
     * @param includeFee              if {@code true}, include an accounting fee if one is required
     * @return the statement acts
     * @throws ArchetypeServiceException for any archetype service error
     * @throws ArchetypeQueryException   for any archetype query error
     */
    public Iterable<FinancialAct> getStatementPreview(Party customer, Date from, Date to,
                                                      boolean includeCompletedCharges, boolean includeFee) {
        Date statementTime = acts.getStatementTimestamp(to);
        Iterable<FinancialAct> result;
        if (includeCompletedCharges) {
            result = acts.getPostedAndCompletedActs(customer, statementTime, from);
        } else {
            result = acts.getPostedActs(customer, from, statementTime, false);
        }
        if (includeFee) {
            BigDecimal fee = getAccountFee(customer, to);
            if (fee.compareTo(BigDecimal.ZERO) != 0) {
                Date date = StatementPeriod.getFeeTimestamp(statementTime);
                FinancialAct feeAct = createAccountingFeeAdjustment(customer, fee, date);
                result = new IterableChain<>(result, feeAct);
            }
        }
        return result;
    }

    /**
     * Returns all POSTED statement acts from the opening balance prior to the specified date, and including all acts
     * prior to any closing balance on the date. The result includes the opening balance,
     * but excludes the closing balance.
     *
     * @param customer the customer
     * @param date     the statement date
     * @return the posted acts
     * @throws ArchetypeServiceException for any archetype service error
     * @throws ArchetypeQueryException   for any archetype query error
     */
    public Iterable<FinancialAct> getStatement(Party customer, Date date) {
        FinancialAct opening = account.getOpeningBalanceBefore(customer, date);
        return getStatement(customer, opening != null ? opening.getActivityStartTime() : null, date);
    }

    /**
     * Returns all POSTED statement acts between the specified timestamps. The result includes the opening balance,
     * but excludes the closing balance.
     *
     * @param customer the customer
     * @param from     the from date. Should corresponding to an opening balance timestamp. May be {@code null}
     * @param to       the to date. This corresponds to the statement date, and should be before the next opening
     *                 balance
     * @return the posted acts
     * @throws ArchetypeServiceException for any archetype service error
     * @throws ArchetypeQueryException   for any archetype query error
     */
    public Iterable<FinancialAct> getStatement(Party customer, Date from, Date to) {
        Date statementTime = acts.getStatementTimestamp(to);
        // TODO - this is nuts. Reliance on magic timestamps is extremely brittle
        Date closingBalanceTimestamp = StatementPeriod.getClosingBalanceTimestamp(statementTime);
        return acts.getPostedActs(customer, from, closingBalanceTimestamp, false);
    }

    /**
     * Returns all POSTED statement acts between the specified timestamps. If the from date is non-null, this will
     * include a dummy opening balance. All other opening and closing balances will be excluded.
     *
     * @param customer the customer
     * @param from     the from date, inclusive. May be {@code null}
     * @param to       the to date, exclusive. May be {@code null}
     * @return the posted acts
     * @throws ArchetypeServiceException for any archetype service error
     * @throws ArchetypeQueryException   for any archetype query error
     */
    public Iterable<FinancialAct> getStatementRange(Party customer, Date from, Date to) {
        FinancialAct openingBalance = null;
        if (from != null) {
            FinancialAct before = account.getOpeningBalanceBefore(customer, from);
            BigDecimal balance;
            if (before != null) {
                balance = account.getBalance(customer, before.getActivityStartTime(), from, before.getTotal());
            } else {
                balance = account.getBalance(customer, null, from, BigDecimal.ZERO);
            }
            openingBalance = account.createOpeningBalance(customer, from, balance);
        }

        ArchetypeQuery query = CustomerAccountQueryFactory.createQuery(
                customer, CustomerAccountArchetypes.DEBITS_CREDITS);
        if (from != null) {
            query.add(Constraints.gte("startTime", from));
        }
        if (to != null) {
            query.add(Constraints.lt("startTime", to));
        }
        query.add(Constraints.sort("startTime"));
        query.add(Constraints.sort("id"));
        IterableIMObjectQuery<FinancialAct> acts = new IterableIMObjectQuery<>(service, query);
        return (openingBalance != null) ? new IterableChain<>(openingBalance, acts) : acts;
    }

    /**
     * Marks a statement as being printed.
     *
     * @param customer      the customer
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
     * (derived from the specified date - {@code accountFeeDays});</li>
     * <li>the overdue balance is &gt= {@code accountFeeBalance}; and
     * </li>
     * <li>the account fee is greater than {@code accountFeeMinimum}.
     * The account fee is calculated as:
     * <ul>
     * <li>{@code overdue * accountFeeAmount} if the {@code accountFee} is
     * {@code "PERCENTAGE"}; or</li>
     * <li>{@code accountFeeAmount} if the {@code accountFee} is
     * {@code "FIXED"}</li>
     * </ul></li>
     * </ul>
     *
     * @param customer      the customer
     * @param statementDate the statement date
     * @return the account fee, or {@code BigDecimal.ZERO} if there is no fee
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal getAccountFee(Party customer, Date statementDate) {
        BigDecimal result = BigDecimal.ZERO;
        AccountType accountType = getAccountType(customer);
        if (accountType != null) {
            statementDate = acts.getStatementTimestamp(statementDate);
            Date feeDate = accountType.getAccountFeeDate(statementDate);
            feeDate = acts.getStatementTimestamp(feeDate);
            BigDecimal overdue = account.getOverdueBalance(
                    customer, statementDate, feeDate);
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
     * @return the adjustment act
     */
    public FinancialAct createAccountingFeeAdjustment(Party customer, BigDecimal fee, Date startTime) {
        FinancialAct act = (FinancialAct) service.create(CustomerAccountArchetypes.DEBIT_ADJUST);
        ActBean bean = new ActBean(act, service);
        bean.addNodeParticipation("customer", customer);
        act.setTotal(new Money(fee));
        act.setActivityStartTime(startTime);
        act.setStatus(ActStatus.POSTED);
        tax.calculateTax(act);

        String notes = "Accounting Fee";
        AccountType accountType = getAccountType(customer);
        if (accountType != null) {
            if (!StringUtils.isEmpty(accountType.getAccountFeeMessage())) {
                notes = accountType.getAccountFeeMessage();
            }
        }
        bean.setValue("notes", notes);
        return act;
    }

    /**
     * Returns the last (i.e. most recent) opening balance for a customer.
     *
     * @param customer the customer
     * @return the opening balance, or {@code null} if none is found
     */
    private FinancialAct getLastOpeningBalance(Party customer) {
        ArchetypeQuery query = CustomerAccountQueryFactory.createQuery(customer, OPENING_BALANCE);
        query.add(Constraints.sort("startTime", false));
        query.add(Constraints.sort("id", false));
        query.setMaxResults(1);
        Iterator<FinancialAct> iterator = new IMObjectQueryIterator<>(service, query);
        return iterator.hasNext() ? iterator.next() : null;
    }

    /**
     * Helper to return the account type for a customer.
     *
     * @param customer the customer
     * @return the account type, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    private AccountType getAccountType(Party customer) {
        IMObjectBean bean = new IMObjectBean(customer, service);
        if (bean.hasNode("type")) {
            List<Lookup> accountTypes = bean.getValues("type", Lookup.class);
            if (!accountTypes.isEmpty()) {
                return new AccountType(accountTypes.get(0), service);
            }
        }
        return null;
    }
}