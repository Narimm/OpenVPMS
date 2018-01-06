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

package org.openvpms.component.model.product;

import org.openvpms.component.model.lookup.Lookup;
import org.openvpms.component.model.object.IMObject;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

/**
 * Represents the price of a {@link Product}.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public interface ProductPrice extends IMObject {

    /**
     * Determines if the price is a fixed price.
     *
     * @return <tt>true</tt> if the price is a fixed price
     */
    boolean isFixed();

    /**
     * Determines if the price is a fixed price.
     *
     * @param fixed <tt>true</tt> if the price is a fixed price
     */
    void setFixed(boolean fixed);

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
     * Returns the from date.
     *
     * @return the from date
     */
    Date getFromDate();

    /**
     * Sets the from date.
     *
     * @param date the from date
     */
    void setFromDate(Date date);

    /**
     * Returns the date that the price is valid to.
     *
     * @return the date
     */
    Date getToDate();

    /**
     * Sets the date that the price is valid to.
     *
     * @param date the date
     */
    void setToDate(Date date);

    /**
     * Returns the classifications for this price.
     *
     * @return the classifications
     */
    Set<Lookup> getClassifications();

    /**
     * Adds a classification.
     *
     * @param classification the classification to add
     */
    void addClassification(Lookup classification);

    /**
     * Removes a classification.
     *
     * @param classification the classification to remove
     */
    void removeClassification(Lookup classification);
}
