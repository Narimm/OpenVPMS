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

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface ProductPriceDO extends IMObjectDO {
    /**
     * @return Returns the product.
     */
    ProductDO getProduct();

    /**
     * @param product The product to set.
     */
    void setProduct(ProductDO product);

    /**
     * @return Returns the fixed.
     */
    boolean isFixed();

    /**
     * @param fixed The fixed to set.
     */
    void setFixed(boolean fixed);

    /**
     * @return Returns the fromDate.
     */
    Date getFromDate();

    /**
     * @param fromDate The fromDate to set.
     */
    void setFromDate(Date fromDate);

    /**
     * @return Returns the price.
     */
    BigDecimal getPrice();

    /**
     * @param price The price to set.
     */
    void setPrice(BigDecimal price);

    /**
     * @return Returns the thruDate.
     */
    Date getThruDate();

    /**
     * @param thruDate The thruDate to set.
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
