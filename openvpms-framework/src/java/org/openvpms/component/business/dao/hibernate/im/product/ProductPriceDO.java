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

import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDO;
import org.openvpms.component.business.dao.hibernate.im.lookup.LookupDO;
import org.openvpms.component.business.domain.im.product.ProductPrice;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;


/**
 * Data object interface corresponding to the {@link ProductPrice} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface ProductPriceDO extends IMObjectDO {

    /**
     * Returns the product that this price belongs to.
     *
     * @return the product. May be <tt>null</tt>
     */
    ProductDO getProduct();

    /**
     * Sets the product that this product belongs to.
     *
     * @param product the product to set. May be <tt>null</tt>
     */
    void setProduct(ProductDO product);

    /**
     * Returns the price.
     *
     * @return the price
     */
    BigDecimal getPrice();

    /**
     * Sets the price.
     *
     * @param price the price
     */
    void setPrice(BigDecimal price);

    /**
     * Determines if it is a fixed price.
     *
     * @return <tt>true</tt> if it is a fixed price
     */
    boolean isFixed();

    /**
     * Determines if it is a fixed price.
     *
     * @param fixed if <tt>true</tt>, it is a fixed price
     */
    void setFixed(boolean fixed);

    /**
     * Returns the date that the price is active from.
     *
     * @return the active from date. May be <tt>null</tt>
     */
    Date getFromDate();

    /**
     * Sets the date that the price is active from.
     *
     * @param fromDate the active from date. May be <tt>null</tt>
     */
    void setFromDate(Date fromDate);

    /**
     * Returns the date that the price is active to.
     *
     * @return the active to date. May be <tt>null</tt>
     */
    Date getThruDate();

    /**
     * Sets the date that the price is active to.
     *
     * @param thruDate the active to date. May be <tt>null</tt>
     */
    void setThruDate(Date thruDate);

    /**
     * Returns the classifications for this price.
     *
     * @return the clasifications
     */
    Set<LookupDO> getClassifications();

    /**
     * Add a classification.
     *
     * @param classification the classification to add
     */
    void addClassification(LookupDO classification);

    /**
     * Removes a classification.
     *
     * @param classification the classification to remove
     */
    void removeClassification(LookupDO classification);
}
