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

package org.openvpms.component.business.dao.hibernate.im.act;

import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;

import java.math.BigDecimal;


/**
 * Data object interface corresponding to the {@link FinancialAct} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface FinancialActDO extends ActDO {

    /**
     * Determines if this is a credit or debit transaction.
     *
     * @return <tt>true</tt> if it's a credit, <tt>false</tt> if it's a debit
     */
    boolean isCredit();

    /**
     * Determines if this is a credit or debit transaction.
     *
     * @param credit if <tt>true</tt> it's a credit. If <tt>false</tt>
     *               it's a debit
     */
    void setCredit(boolean credit);

    /**
     * Returns the fixed amount.
     *
     * @return the fixed amount
     */
    Money getFixedAmount();

    /**
     * Sets the fixed amount.
     *
     * @param fixedAmount the fixed amount
     */
    void setFixedAmount(Money fixedAmount);

    /**
     * Determines if the act has been printed.
     *
     * @return <tt>true</tt> if the act has been printed
     */
    boolean isPrinted();

    /**
     * Determines if the act has been printed.
     *
     * @param printed if <tt>true</tt>, the act has been printed
     */
    void setPrinted(boolean printed);

    /**
     * Returns the quantity.
     *
     * @return the quantity
     */
    BigDecimal getQuantity();

    /**
     * Sets the quantity.
     *
     * @param quantity the quantity
     */
    void setQuantity(BigDecimal quantity);

    /**
     * Returns the tax amount.
     *
     * @return the tax amount
     */
    Money getTaxAmount();

    /**
     * Sets the tax amount.
     *
     * @param taxAmount the tax amount
     */
    void setTaxAmount(Money taxAmount);

    /**
     * Returns the total.
     *
     * @return the total
     */
    Money getTotal();

    /**
     * Sets the total.
     *
     * @param total the total
     */
    void setTotal(Money total);

    /**
     * Returns the unit amount.
     *
     * @return the unit amount.
     */
    Money getUnitAmount();

    /**
     * Sets the unit amount.
     *
     * @param unitAmount the unit amount
     */
    void setUnitAmount(Money unitAmount);

    /**
     * Returns the fixed cost.
     *
     * @return the fixed cost
     */
    Money getFixedCost();

    /**
     * Sets the fixed cost.
     *
     * @param fixedCost the fixed cost
     */
    void setFixedCost(Money fixedCost);

    /**
     * Returns the unit cost.
     *
     * @return the unit cost
     */
    Money getUnitCost();

    /**
     * Sets the unit cost.
     *
     * @param unitCost the unit cost
     */
    void setUnitCost(Money unitCost);

    /**
     * Returns the allocated amount.
     * <p>For debits, it is the amount of credits
     * that have been allocated against the total amount. If allocated = total
     * then the debt is fully paid.
     * </p>
     * <p/>
     * For credits, it is the amount of the credit that has been allocated
     * against a debit. If allocated = total then credit has been fully
     * allocated.
     *
     * @return the allocated amount
     */
    Money getAllocatedAmount();

    /**
     * Sets the allocated amount.
     *
     * @param amount the allocated amount
     * @see #getAllocatedAmount
     */
    void setAllocatedAmount(Money amount);
}
