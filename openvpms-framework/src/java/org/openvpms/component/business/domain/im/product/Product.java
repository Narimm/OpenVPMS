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


package org.openvpms.component.business.domain.im.product;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openvpms.component.business.domain.im.common.Entity;

/**
 * Represents a product category of objects
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class Product extends Entity {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * The date that the product was introduced and therefore 
     * available to sell
     */
    private Date introductionDate;
    
    /**
     * The date that the product was discontinued
     */
    private Date discontinuedDate;
    
    /**
     * The name that is printed on the invoice
     */
    private String printedName;
    
    /**
     * Maintains a list of {@link ProductPrice} for this product
     */
    private Set<ProductPrice> productPrices = new HashSet<ProductPrice>();
    
    /**
     * Default constructor
     */
    public Product() {
        // do nothing
    }

    /**
     * @return Returns the discontinuedDate.
     */
    public Date getDiscontinuedDate() {
        return discontinuedDate;
    }

    /**
     * @param discontinuedDate The discontinuedDate to set.
     */
    public void setDiscontinuedDate(Date discontinuedDate) {
        this.discontinuedDate = discontinuedDate;
    }

    /**
     * @return Returns the introductionDate.
     */
    public Date getIntroductionDate() {
        return introductionDate;
    }

    /**
     * @param introductionDate The introductionDate to set.
     */
    public void setIntroductionDate(Date introductionDate) {
        this.introductionDate = introductionDate;
    }

    /**
     * @return Returns the printedName.
     */
    public String getPrintedName() {
        return printedName;
    }

    /**
     * @param printedName The printedName to set.
     */
    public void setPrintedName(String printedName) {
        this.printedName = printedName;
    }

    /**
     * @return Returns the productPrices.
     */
    public Set<ProductPrice> getProductPrices() {
        return productPrices;
    }

    /**
     * @param productPrices The productPrices to set.
     */
    public void setProductPrices(Set<ProductPrice> productPrices) {
        this.productPrices = productPrices;
    }
    
    /**
     * Add the specified {@link ProductPrice} to the set.
     * 
     * @param productPrice 
     *            the product price to add
     */
    public void addProductPrice(ProductPrice productPrice) {
        this.productPrices.add(productPrice);
    }
    
    /**
     * Remove the specified {@link ProductPrice} from the set.
     * 
     * @param productPrice
     *            the product price to remove
     */
    public void removeProductPrice(ProductPrice productPrice) {
        this.productPrices.remove(productPrice);
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#toString()
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, 
                ToStringStyle.MULTI_LINE_STYLE);
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.Entity#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        Product copy = (Product)super.clone();
        copy.discontinuedDate = (Date)(this.discontinuedDate == null ?
                null : this.discontinuedDate.clone());
        copy.introductionDate = (Date)(this.introductionDate == null ?
                null : this.introductionDate.clone());
        copy.printedName = this.printedName;
        copy.productPrices = new HashSet<ProductPrice>(this.productPrices);

        return copy;
    }
}
