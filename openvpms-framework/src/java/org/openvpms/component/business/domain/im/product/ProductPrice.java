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

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openvpms.component.business.domain.im.common.Classification;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.basic.DynamicAttributeMap;

/**
 * Represents the price of a {@link Product}
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class ProductPrice extends IMObject {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The product that it refers too
     */
    private Product product;
    
    /**
     * The price of the product
     */
    private BigDecimal price;
    
    /**
     * The product is valid from this date
     */
    private Date fromDate;
    
    /**
     * The product is valid through to this date
     */
    private Date thruDate;
    
    /**
     * Indicates whether this is a fixed or variable price (i.e.
     * do we multiple by the quantity sold
     */
    private boolean fixed;
    
    /**
     * The classification for the product price
     */
    private Set<Classification> classifications = new HashSet<Classification>();
    
    /**
     * A placeholder for all entity details, which denotes the dynamic and
     * adaptive details of the entity.
     */
    private DynamicAttributeMap details;
    
    
    /**
     * Default constructor
     */
    public ProductPrice() {
        // do nothing
    }

    /**
     * @return Returns the product.
     */
    public Product getProduct() {
        return product;
    }

    /**
     * @param product The product to set.
     */
    public void setProduct(Product product) {
        this.product = product;
    }

    /**
     * @return Returns the fixed.
     */
    public boolean isFixed() {
        return fixed;
    }

    /**
     * @param fixed The fixed to set.
     */
    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    /**
     * @return Returns the fromDate.
     */
    public Date getFromDate() {
        return fromDate;
    }

    /**
     * @param fromDate The fromDate to set.
     */
    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    /**
     * @return Returns the price.
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * @param price The price to set.
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * @return Returns the thruDate.
     */
    public Date getThruDate() {
        return thruDate;
    }

    /**
     * @param thruDate The thruDate to set.
     */
    public void setThruDate(Date thruDate) {
        this.thruDate = thruDate;
    }

    /**
     * @return Returns the details.
     */
    public DynamicAttributeMap getDetails() {
        return details;
    }

    /**
     * @param details The details to set.
     */
    public void setDetails(DynamicAttributeMap details) {
        this.details = details;
    }

    /**
     * Convenience method that return all the {@link Classification} as an array.
     * 
     * @return Classification[]
     */
    public Classification[] getClassificationsAsArray() {
        return (Classification[])classifications.toArray(
                new Classification[classifications.size()]);
    }
    
    /**
     * Return all the associated {@link Classification}
     * 
     * @return Set<Classification>
     */
    public Set<Classification> getClassifications() {
        return this.classifications;
    }
    
    /**
     * @param classifications The classifications to set.
     */
    public void setClassifications(Set<Classification> classifications) {
        this.classifications = classifications;
    }
    
    /**
     * Add a {@link Classification}
     * 
     * @param classification 
     *            the classification to add
     */
    public void addClassification(Classification classification) {
        classifications.add(classification);
    }

    /**
     * Remove the specified {@link Classification}.
     * 
     * @param classification
     */
    public void removeClassification(Classification classification) {
        classifications.remove(classification);
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
     * @see org.openvpms.component.business.domain.im.common.IMObject#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        ProductPrice copy = (ProductPrice)super.clone();
        copy.fixed = this.fixed;
        copy.fromDate = (Date)(this.fromDate == null ?
                null : this.fromDate.clone());;
        copy.price = this.price;
        copy.classifications = new HashSet<Classification>(this.classifications);
        copy.product = this.product;
        copy.thruDate  = (Date)(this.thruDate == null ?
                null : this.thruDate.clone());
        
        return copy;
    }
}
