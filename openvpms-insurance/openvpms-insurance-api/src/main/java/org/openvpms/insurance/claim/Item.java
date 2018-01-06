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

package org.openvpms.insurance.claim;

import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.model.product.Product;

import java.math.BigDecimal;
import java.util.Date;

/**
 * An invoice item being charged on a claim.
 *
 * @author Tim Anderson
 */
public interface Item {

    /**
     * Returns the invoice item identifier.
     *
     * @return the invoice item identifier
     */
    long getId();

    /**
     * Returns the date when the invoice item was charged.
     *
     * @return the date
     */
    Date getDate();

    /**
     * Returns the product.
     *
     * @return the product
     */
    Product getProduct();

    /**
     * Returns the product type.
     *
     * @return the product type. May be {@code null}
     */
    Entity getProductType();

    /**
     * Returns the quantity.
     *
     * @return the quantity
     */
    BigDecimal getQuantity();

    /**
     * Returns the discount amount, including tax.
     *
     * @return the discount amount
     */
    BigDecimal getDiscount();

    /**
     * Returns the discount tax amount.
     *
     * @return the discount tax amount
     */
    BigDecimal getDiscountTax();

    /**
     * Returns the total amount, including tax.
     *
     * @return the total amount
     */
    BigDecimal getTotal();

    /**
     * Returns the total tax amount.
     *
     * @return the tax amount
     */
    BigDecimal getTotalTax();

}
