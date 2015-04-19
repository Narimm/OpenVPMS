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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */


package org.openvpms.component.business.domain.im.act;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;

import java.math.BigDecimal;


/**
 * The financial act is used to model charge, refunds and payment acts to
 * name a few. It extends the {@link Act} class and adds additional
 * attributes
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class FinancialAct extends Act {

    /**
     * Serialisation version identifier.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Quantity of units being sold.
     */
    private BigDecimal quantity;

    /**
     * Fixed amount or fee.
     */
    private Money fixedAmount;

    /**
     * Unit amount or fee.
     */
    private Money unitAmount;

    /**
     * The fixed cost.
     */
    private Money fixedCost;

    /**
     * The unit cost.
     */
    private Money unitCost;

    /**
     * Tax amount.
     */
    private Money taxAmount;

    /**
     * The total for this act.
     */
    private Money total;

    /**
     * The allocated amount.
     */
    private Money allocatedAmount;

    /**
     * Determines if this financial transaction is a credit or debit.
     */
    private boolean credit;

    /**
     * Indicates whether it has been printed.
     */
    private boolean printed;


    /**
     * Default constructor.
     */
    public FinancialAct() {
        super();
    }

    /**
     * Determines if this is a credit or debit transaction.
     *
     * @return <code>true</code> if it's a credit, <code>false</code> if it's
     *         a debit
     */
    public boolean isCredit() {
        return credit;
    }

    /**
     * Determines if this is a credit or debit transaction.
     *
     * @param credit if <code>true</code> it's a credit. If <code>false</code>
     *               it's a debit
     */
    public void setCredit(boolean credit) {
        this.credit = credit;
    }

    /**
     * Returns the fixed amount.
     *
     * @return the fixed amount
     */
    public Money getFixedAmount() {
        return fixedAmount;
    }

    /**
     * Sets the fixed amount.
     *
     * @param fixedAmount the fixed amount
     */
    public void setFixedAmount(Money fixedAmount) {
        this.fixedAmount = fixedAmount;
    }

    /**
     * Sets the fixed cost.
     *
     * @param fixedCost the fixed cost
     */
    public void setFixedCost(Money fixedCost) {
        this.fixedCost = fixedCost;
    }

    /**
     * Returns the fixed cost.
     *
     * @return the fixed cost.
     */
    public Money getFixedCost() {
        return fixedCost;
    }

    /**
     * Sets the unit cost.
     *
     * @param unitCost the unit cost
     */
    public void setUnitCost(Money unitCost) {
        this.unitCost = unitCost;
    }

    /**
     * Returns the unit cost.
     *
     * @return the unit cost
     */
    public Money getUnitCost() {
        return unitCost;
    }

    /**
     * Determines if the act has been printed.
     *
     * @return <code>true</code> if the act has been printed
     */
    public boolean isPrinted() {
        return printed;
    }

    /**
     * Determines if the act has been printed.
     *
     * @param printed if <code>true</code>, the act has been printed
     */
    public void setPrinted(boolean printed) {
        this.printed = printed;
    }

    /**
     * Returns the quantity.
     *
     * @return the quantity
     */
    public BigDecimal getQuantity() {
        return quantity;
    }

    /**
     * Sets the quantity.
     *
     * @param quantity the quantity
     */
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    /**
     * Returns the tax amount.
     *
     * @return the tax amount
     */
    public Money getTaxAmount() {
        return taxAmount;
    }

    /**
     * Sets the tax amount.
     *
     * @param taxAmount the tax amount
     */
    public void setTaxAmount(Money taxAmount) {
        this.taxAmount = taxAmount;
    }

    /**
     * Returns the total.
     *
     * @return the total
     */
    public Money getTotal() {
        return total;
    }

    /**
     * Sets the total.
     *
     * @param total the total
     */
    public void setTotal(Money total) {
        this.total = total;
    }

    /**
     * Returns the unit amount.
     *
     * @return the unit amount.
     */
    public Money getUnitAmount() {
        return unitAmount;
    }

    /**
     * Sets the unit amount.
     *
     * @param unitAmount the unit amount
     */
    public void setUnitAmount(Money unitAmount) {
        this.unitAmount = unitAmount;
    }

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
     * </p>
     *
     * @return the allocated amount
     */
    public Money getAllocatedAmount() {
        return allocatedAmount;
    }

    /**
     * Sets the allocated amount.
     *
     * @param amount the allocated amount
     * @see #getAllocatedAmount
     */
    public void setAllocatedAmount(Money amount) {
        allocatedAmount = amount;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#toString()
     */
    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(null)
                .append("quantity", quantity)
                .append("fixedAmount", fixedAmount)
                .append("unitAmount", unitAmount)
                .append("fixedCost", fixedCost)
                .append("unitCost", unitCost)
                .append("taxAmount", taxAmount)
                .append("total", total)
                .append("allocatedAmount", allocatedAmount)
                .append("credit", credit)
                .append("printed", printed)
                .toString();
    }
}
