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

package org.openvpms.archetype.rules.finance.account;

import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.lookup.Lookup;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;


/**
 * Wrapper around an <em>lookup.accountType}.
 *
 * @author Tim Anderson
 */
public class AccountType {

    /**
     * The account fee type.
     */
    public enum FeeType {

        FIXED, PERCENTAGE
    }

    /**
     * Bean wrapper for the lookup.
     */
    private final IMObjectBean bean;

    /**
     * Constructs an {@link AccountType}.
     *
     * @param lookup  the lookup
     * @param service the archetype service
     */
    public AccountType(Lookup lookup, IArchetypeService service) {
        bean = service.getBean(lookup);
    }

    /**
     * Returns the account type name.
     *
     * @return the account type name
     */
    public String getName() {
        return bean.getString("name");
    }

    /**
     * Returns the payment terms.
     *
     * @return the payment terms
     */
    public int getPaymentTerms() {
        return bean.getInt("paymentTerms");
    }

    /**
     * The payment unit of measure.
     *
     * @return the payment unit of measure, or {@code null} if none is specified
     */
    public DateUnits getPaymentUOM() {
        String value = bean.getString("paymentUom");
        return (value != null) ? DateUnits.valueOf(value) : null;
    }

    /**
     * Calculates the overdue payment date based on the specified date.
     * This is {@code date - paymentTerms * paymentUOM}
     *
     * @param date the date
     * @return the overdue date
     */
    public Date getOverdueDate(Date date) {
        DateUnits payment = getPaymentUOM();
        if (payment != null) {
            date = DateRules.getDate(date); // strip any time
            int days = getPaymentTerms();
            return DateRules.getDate(date, -days, payment);
        }
        return date;
    }

    /**
     * Returns the account fee type.
     *
     * @return the account fee type
     */
    public FeeType getFeeType() {
        String type = bean.getString("accountFee");
        return "FIXED".equals(type) ? FeeType.FIXED : FeeType.PERCENTAGE;
    }

    /**
     * Returns the account fee amount.
     *
     * @return the account fee amount
     */
    public BigDecimal getAccountFeeAmount() {
        return bean.getBigDecimal("accountFeeAmount");
    }

    /**
     * Returns the minimum account fee.
     *
     * @return the minimum account fee
     */
    public BigDecimal getAccountFeeMinimum() {
        return bean.getBigDecimal("accountFeeMinimum");
    }

    /**
     * Returns the account fee balance.
     *
     * @return the account fee balance
     */
    public BigDecimal getAccountFeeBalance() {
        return bean.getBigDecimal("accountFeeBalance");
    }

    /**
     * Returns the account fee days.
     *
     * @return the account fee days
     */
    public int getAccountFeeDays() {
        return bean.getInt("accountFeeDays");
    }

    /**
     * Returns the account fee message.
     *
     * @return the account fee message
     */
    public String getAccountFeeMessage() {
        return bean.getString("accountFeeMessage");
    }

    /**
     * Calculates an account fee date.
     * This is the specified date - {@link #getAccountFeeDays}.
     *
     * @param date the date
     * @return the account fee date
     */
    public Date getAccountFeeDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -getAccountFeeDays());
        return calendar.getTime();
    }

    /**
     * Calculates an account fee on an overdue amount.
     * <br/>
     * If the fee type is {@code FIXED}, returns {@link #getAccountFeeAmount()}.
     * <br/>
     * If the fee type is {@code PERCENTAGE}, returns
     * ({@link #getAccountFeeAmount()} * {@code overdue})/100
     *
     * @param overdue the overdue amount
     * @return the account fee
     */
    public BigDecimal getAccountFee(BigDecimal overdue) {
        BigDecimal amount = getAccountFeeAmount();
        if (getFeeType() == FeeType.FIXED) {
            return amount;
        }
        return MathRules.divide(amount.multiply(overdue), MathRules.ONE_HUNDRED, 2); // TODO - should use currency scale
    }

    /**
     * Returns the alert associated with this account type.
     *
     * @return the alert lookup, or {@code null} if this account type has no alert
     */
    public Lookup getAlert() {
        return bean.getTarget("alert", Lookup.class);
    }

}
