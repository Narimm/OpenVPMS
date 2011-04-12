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

package org.openvpms.archetype.rules.finance.account;

import org.apache.commons.lang.time.DateUtils;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * Wrapper around an <em>lookup.accountType</tt>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
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
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Helper for % divisions.
     */
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);


    /**
     * Constructs an <tt>AccountType</tt>.
     *
     * @param lookup the lookup
     */
    public AccountType(Lookup lookup) {
        this(lookup, ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Creates a new <tt>AccountType</tt>.
     *
     * @param lookup  the lookup
     * @param service the archetype service
     */
    public AccountType(Lookup lookup, IArchetypeService service) {
        bean = new IMObjectBean(lookup, service);
        this.service = service;
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
     * @return the payment unit of measure, or <tt>null</tt> if none is
     *         specified
     */
    public DateUnits getPaymentUOM() {
        String value = bean.getString("paymentUom");
        return (value != null) ? DateUnits.valueOf(value) : null;
    }

    /**
     * Calculates the overdue payment date based on the specified date.
     * This is <tt>date - paymentTerms * paymentUOM</tt>
     *
     * @param date the date
     * @return the overdue date
     */
    public Date getOverdueDate(Date date) {
        DateUnits payment = getPaymentUOM();
        if (payment != null) {
            date = DateUtils.truncate(date, Calendar.DATE); // strip any time
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
     * If the fee type is <tt>FIXED</tt>, returns
     * {@link #getAccountFeeAmount()}.
     * <br/>
     * If the fee type is <tt>PERCENTAGE</tt>, returns
     * ({@link #getAccountFeeAmount()} * <tt>overdue</tt>)/100
     *
     * @param overdue the overdue amount
     * @return the account fee
     */
    public BigDecimal getAccountFee(BigDecimal overdue) {
        BigDecimal amount = getAccountFeeAmount();
        if (getFeeType() == FeeType.FIXED) {
            return amount;
        }
        return MathRules.divide(amount.multiply(overdue), HUNDRED, 2); // TODO - should use currency scale
    }

    /**
     * Returns the alert associated with this account type.
     *
     * @return the alert lookup, or <tt>null</tt> if this account type has no alert
     */
    public Lookup getAlert() {
        Lookup result = null;
        List<LookupRelationship> relationships = bean.getValues("alert", LookupRelationship.class);
        if (!relationships.isEmpty()) {
            IMObjectReference ref = relationships.get(0).getTarget();
            if (ref != null) {
                result = (Lookup) service.get(ref);
            }
        }
        return result;
    }

}
