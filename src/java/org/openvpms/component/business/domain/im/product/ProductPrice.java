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

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


/**
 * Represents the price of a {@link Product}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ProductPrice extends IMObject {

    /**
     * Serialisation version identifier.
     */
    private static final long serialVersionUID = 2L;

    /**
     * The product that it refers to.
     */
    private Product product;

    /**
     * The price of the product.
     */
    private BigDecimal price;

    /**
     * The price is valid from this date.
     */
    private Date fromDate;

    /**
     * The price is valid to this date.
     */
    private Date toDate;

    /**
     * Indicates whether this is a fixed or variable price (i.e.
     * do we multiple by the quantity sold.
     */
    private boolean fixed;

    /**
     * The classifications for the product price.
     */
    private Set<Lookup> classifications = new HashSet<Lookup>();


    /**
     * Default constructor.
     */
    public ProductPrice() {
    }

    /**
     * Returns the product.
     *
     * @return the product
     */
    public Product getProduct() {
        return product;
    }

    /**
     * Sets the product.
     *
     * @param product the product
     */
    public void setProduct(Product product) {
        this.product = product;
    }

    /**
     * Determines if the price is a fixed price.
     *
     * @return <tt>true</tt> if the price is a fixed price
     */
    public boolean isFixed() {
        return fixed;
    }

    /**
     * Determines if the price is a fixed price.
     *
     * @param fixed <tt>true</tt> if the price is a fixed price
     */
    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    /**
     * Returns the price.
     *
     * @return the price
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Sets the price.
     *
     * @param price the price
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * Returns the from date.
     *
     * @return the from date
     */
    public Date getFromDate() {
        return fromDate;
    }

    /**
     * Sets the from date.
     *
     * @param date the from date
     */
    public void setFromDate(Date date) {
        fromDate = date;
    }

    /**
     * Returns the date that the price is valid to.
     *
     * @return the date
     */
    public Date getToDate() {
        return toDate;
    }

    /**
     * Sets the date that the price is valid to.
     *
     * @param date the date
     */
    public void setToDate(Date date) {
        toDate = date;
    }

    /**
     * @return Returns the thruDate.
     * @deprecated use {@link #getToDate()}
     */
    @Deprecated
    public Date getThruDate() {
        return getToDate();
    }

    /**
     * @param thruDate The thruDate to set.
     * @deprecated use {@link #setToDate(Date)}
     */
    @Deprecated
    public void setThruDate(Date thruDate) {
        setToDate(thruDate);
    }

    /**
     * Convenience method that return all the classifications as an array.
     *
     * @return the classifications
     */
    public Lookup[] getClassificationsAsArray() {
        return classifications.toArray(new Lookup[classifications.size()]);
    }

    /**
     * Returns the classifications for this price.
     *
     * @return the clasifications
     */
    public Set<Lookup> getClassifications() {
        return classifications;
    }

    /**
     * Sets the classifications for this price.
     *
     * @param classifications the classifications to set
     */
    public void setClassifications(Set<Lookup> classifications) {
        this.classifications = classifications;
    }

    /**
     * Add a classification.
     *
     * @param classification the classification to add
     */
    public void addClassification(Lookup classification) {
        classifications.add(classification);
    }

    /**
     * Removes a classification.
     *
     * @param classification the classification to remove
     */
    public void removeClassification(Lookup classification) {
        classifications.remove(classification);
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        ProductPrice copy = (ProductPrice) super.clone();
        copy.classifications = new HashSet<Lookup>(classifications);
        return copy;
    }
}
