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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.edit.act;

import java.math.BigDecimal;

/**
 * Represents a product include low and high quantity.
 *
 * @author Tim Anderson
 */
public class Quantity {

    /**
     * The low quantity.
     */
    private final BigDecimal lowQuantity;

    /**
     * The high quantity.
     */
    private final BigDecimal highQuantity;


    /**
     * Constructs a {@link Quantity}.
     *
     * @param lowQuantity  the low quantity
     * @param highQuantity the high quantity
     */
    public Quantity(BigDecimal lowQuantity, BigDecimal highQuantity) {
        this.lowQuantity = lowQuantity;
        this.highQuantity = highQuantity;
    }

    /**
     * Returns the low quantity.
     *
     * @return the low quantity
     */
    public BigDecimal getLowQuantity() {
        return lowQuantity;
    }

    /**
     * Returns the high quantity.
     *
     * @return the high quantity
     */
    public BigDecimal getHighQuantity() {
        return highQuantity;
    }

    /**
     * Multiplies quantities.
     *
     * @param quantity the quantity to multiply
     * @return the result of the multiplication
     */
    public Quantity multiply(Quantity quantity) {
        BigDecimal low = lowQuantity.multiply(quantity.getLowQuantity());
        BigDecimal high = highQuantity.multiply(quantity.getHighQuantity());
        return new Quantity(low, high);
    }

    /**
     * Adds quantities.
     *
     * @param quantity the quantity to add
     * @return the result of the addition
     */
    public Quantity add(Quantity quantity) {
        BigDecimal low = lowQuantity.add(quantity.getLowQuantity());
        BigDecimal high = highQuantity.add(quantity.getHighQuantity());
        return new Quantity(low, high);
    }
}
