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

package org.openvpms.component.model.act;

import java.math.BigDecimal;

/**
 * The financial act is used to model financial activities or transactions, such as invoices, payments and refunds.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public interface FinancialAct extends Act {

    /**
     * Determines if this is a credit or debit transaction.
     *
     * @return {@code true} if it's a credit, {@code false} if it's a debit
     */
    boolean isCredit();

    /**
     * Determines if this is a credit or debit transaction.
     *
     * @param credit if {@code true} it's a credit. If {@code false} it's a debit
     */
    void setCredit(boolean credit);

    /**
     * Sets the fixed cost.
     *
     * @param fixedCost the fixed cost
     */
    void setFixedCost(BigDecimal fixedCost);

    /**
     * Returns the fixed cost.
     *
     * @return the fixed cost.
     */
    BigDecimal getFixedCost();

    /**
     * Returns the fixed amount.
     *
     * @return the fixed amount
     */
    BigDecimal getFixedAmount();

    /**
     * Sets the fixed amount.
     *
     * @param fixedAmount the fixed amount
     */
    void setFixedAmount(BigDecimal fixedAmount);

    /**
     * Sets the unit cost.
     *
     * @param unitCost the unit cost
     */
    void setUnitCost(BigDecimal unitCost);

    /**
     * Returns the unit cost.
     *
     * @return the unit cost
     */
    BigDecimal getUnitCost();

    /**
     * Returns the unit amount.
     *
     * @return the unit amount.
     */
    BigDecimal getUnitAmount();

    /**
     * Sets the unit amount.
     *
     * @param unitAmount the unit amount
     */
    void setUnitAmount(BigDecimal unitAmount);

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
    BigDecimal getTaxAmount();

    /**
     * Sets the tax amount.
     *
     * @param taxAmount the tax amount
     */
    void setTaxAmount(BigDecimal taxAmount);

    /**
     * Returns the total.
     *
     * @return the total
     */
    BigDecimal getTotal();

    /**
     * Sets the total.
     *
     * @param total the total
     */
    void setTotal(BigDecimal total);

    /**
     * Returns the allocated amount.
     * <p>For debits, it is the amount of credits that have been allocated against the total amount.
     * If allocated = total, then the debt is fully paid.
     * </p>
     * <p/>
     * For credits, it is the amount of the credit that has been allocated against a debit.
     * If allocated = total then credit has been fully allocated.
     * </p>
     *
     * @return the allocated amount
     */
    BigDecimal getAllocatedAmount();

    /**
     * Sets the allocated amount.
     *
     * @param amount the allocated amount
     */
    void setAllocatedAmount(BigDecimal amount);

    /**
     * Determines if the act has been printed.
     *
     * @return {@code true} if the act has been printed
     */
    boolean isPrinted();

    /**
     * Determines if the act has been printed.
     *
     * @param printed if {@code true}, the act has been printed
     */
    void setPrinted(boolean printed);

}
