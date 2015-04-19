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

package org.openvpms.component.business.dao.hibernate.im.product;

import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDOImpl;
import org.openvpms.component.business.dao.hibernate.im.lookup.LookupDO;
import org.openvpms.component.business.domain.archetype.ArchetypeId;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


/**
 * Implementation of the {@link ProductPriceDO} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2007-05-02 14:28:50 +1000 (Wed, 02 May 2007) $
 */
public class ProductPriceDOImpl extends IMObjectDOImpl
        implements ProductPriceDO {

    /**
     * The product that this price belongs to.
     */
    private ProductDO product;

    /**
     * The price of the product.
     */
    private BigDecimal price;

    /**
     * The date that the price is active from.
     */
    private Date fromDate;

    /**
     * The date that the price is active to.
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
    private Set<LookupDO> classifications = new HashSet<LookupDO>();


    /**
     * Default constructor
     */
    public ProductPriceDOImpl() {
        // do nothing
    }

    /**
     * Creates a new <tt>ProductPriceDOImpl</tt>.
     *
     * @param archetypeId the archetype id
     */
    public ProductPriceDOImpl(ArchetypeId archetypeId) {
        super(archetypeId);
    }

    /**
     * Returns the product that this price belongs to.
     *
     * @return the product. May be <tt>null</tt>
     */
    public ProductDO getProduct() {
        return product;
    }

    /**
     * Sets the product that this product belongs to.
     *
     * @param product the product to set. May be <tt>null</tt>
     */
    public void setProduct(ProductDO product) {
        this.product = product;
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
     * Determines if it is a fixed price.
     *
     * @return <tt>true</tt> if it is a fixed price
     */
    public boolean isFixed() {
        return fixed;
    }

    /**
     * Determines if it is a fixed price.
     *
     * @param fixed if <tt>true</tt>, it is a fixed price
     */
    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    /**
     * Returns the date that the price is active from.
     *
     * @return the active from date. May be <tt>null</tt>
     */
    public Date getFromDate() {
        return fromDate;
    }

    /**
     * Sets the date that the price is active from.
     *
     * @param fromDate the active from date. May be <tt>null</tt>
     */
    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    /**
     * Returns the date that the price is active to.
     *
     * @return the active to date. May be <tt>null</tt>
     */
    public Date getToDate() {
        return toDate;
    }

    /**
     * Sets the date that the price is active to.
     *
     * @param toDate the active to date. May be <tt>null</tt>
     */
    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    /**
     * Returns the classifications for this price.
     *
     * @return the clasifications
     */
    public Set<LookupDO> getClassifications() {
        return classifications;
    }

    /**
     * Add a classification.
     *
     * @param classification the classification to add
     */
    public void addClassification(LookupDO classification) {
        classifications.add(classification);
    }

    /**
     * Removes a classification.
     *
     * @param classification the classification to remove
     */
    public void removeClassification(LookupDO classification) {
        classifications.remove(classification);
    }

    /**
     * Sets the classifications for this price.
     *
     * @param classifications the classifications to set
     */
    protected void setClassifications(Set<LookupDO> classifications) {
        this.classifications = classifications;
    }

}
