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

// java core
import java.math.BigDecimal;

// commons-lang
import org.apache.commons.lang.builder.ToStringBuilder;

// openvpms-framework
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;

/**
 * The financial act is used to model charge, refunds and payment acts to
 * name a few. It extends the {@link Act} class and adds additional
 * attributes
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class FinancialAct extends Act {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Quantity of units being sold
     */
    private BigDecimal quantity;
    
    /**
     * Fixed amount or fee
     */
    private Money fixedAmount;
    
    /**
     * Unit amount or fee
     */
    private Money unitAmount;
    
    /**
     * Tax am ount
     */
    private Money taxAmount;
    
    /**
     * The total for this act
     */
    private Money total;
    
    /**
     * Is this financial transaction a credit
     */
    private boolean credit;
    
    /**
     * Indicates whether it has been printed
     */
    private boolean printed;
    
    
    /**
     * Default constructor
     */
    public FinancialAct() {
        super();
    }

    /**
     * @return Returns the credit.
     */
    public boolean isCredit() {
        return credit;
    }

    /**
     * @param credit The credit to set.
     */
    public void setCredit(boolean credit) {
        this.credit = credit;
    }

    /**
     * @return Returns the fixedAmount.
     */
    public Money getFixedAmount() {
        return fixedAmount;
    }

    /**
     * @param fixedAmount The fixedAmount to set.
     */
    public void setFixedAmount(Money fixedAmount) {
        this.fixedAmount = fixedAmount;
    }

    /**
     * @return Returns the printed.
     */
    public boolean isPrinted() {
        return printed;
    }

    /**
     * @param printed The printed to set.
     */
    public void setPrinted(boolean printed) {
        this.printed = printed;
    }

    /**
     * @return Returns the quantity.
     */
    public BigDecimal getQuantity() {
        return quantity;
    }

    /**
     * @param quantity The quantity to set.
     */
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    /**
     * @return Returns the taxAmount.
     */
    public Money getTaxAmount() {
        return taxAmount;
    }

    /**
     * @param taxAmount The taxAmount to set.
     */
    public void setTaxAmount(Money taxAmount) {
        this.taxAmount = taxAmount;
    }

    /**
     * @return Returns the total.
     */
    public Money getTotal() {
        return total;
    }

    /**
     * @param total The total to set.
     */
    public void setTotal(Money total) {
        this.total = total;
    }

    /**
     * @return Returns the unitAmount.
     */
    public Money getUnitAmount() {
        return unitAmount;
    }

    /**
     * @param unitAmount The unitAmount to set.
     */
    public void setUnitAmount(Money unitAmount) {
        this.unitAmount = unitAmount;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        FinancialAct copy = (FinancialAct)super.clone();
        
        copy.credit = this.credit;
        copy.fixedAmount = this.fixedAmount;
        copy.printed = this.printed;
        copy.quantity = this.quantity;
        copy.taxAmount = this.taxAmount;
        copy.total = this.total;
        copy.unitAmount = this.unitAmount;
        
        return copy;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .appendSuper(null)
            .append("quantity", quantity)
            .append("fixedAmount", fixedAmount)
            .append("unitAmount", unitAmount)
            .append("taxAmount", taxAmount)
            .append("total", total)
            .append("credit", credit)
            .append("printed", printed)
            .toString();
    }
}
