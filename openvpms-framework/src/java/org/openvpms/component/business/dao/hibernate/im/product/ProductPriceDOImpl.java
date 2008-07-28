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
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.archetype.ArchetypeId;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents the price of a {@link Product}
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2007-05-02 14:28:50 +1000 (Wed, 02 May 2007) $
 */
public class ProductPriceDOImpl extends IMObjectDOImpl
        implements ProductPriceDO {

    /**
     * The product that it refers to.
     */
    private ProductDO product;

    /**
     * The price of the product.
     */
    private BigDecimal price;

    /**
     * The product is valid from this date.
     */
    private Date fromDate;

    /**
     * The product is valid through to this date.
     */
    private Date thruDate;

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
     * Creates a new <tt>ProductPriceDO</tt>.
     *
     * @param archetypeId the archetype id.
     */
    public ProductPriceDOImpl(ArchetypeId archetypeId) {
        super(archetypeId);
    }

    /**
     * @return Returns the product.
     */
    public ProductDO getProduct() {
        return product;
    }

    /**
     * @param product The product to set.
     */
    public void setProduct(ProductDO product) {
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
