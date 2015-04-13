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

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.product.Product;

import java.math.BigDecimal;

/**
 * Represents a product included by a template.
 *
 * @author Tim Anderson
 */
public class TemplateProduct {

    /**
     * The included product.
     */
    private final Product product;

    /**
     * The low quantity.
     */
    private BigDecimal lowQuantity;

    /**
     * The high quantity.
     */
    private BigDecimal highQuantity;

    /**
     * Determines if prices should be zeroed when the product is charged.
     */
    private final boolean zeroPrice;


    /**
     * Constructs a {@link TemplateProduct}.
     *
     * @param product      the product
     * @param lowQuantity  the low quantity
     * @param highQuantity the high quantity
     */
    public TemplateProduct(Product product, BigDecimal lowQuantity, BigDecimal highQuantity, boolean zeroPrice) {
        this.product = product;
        this.lowQuantity = lowQuantity;
        this.highQuantity = highQuantity;
        this.zeroPrice = zeroPrice;
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
     * Adds quantities.
     *
     * @param lowQuantity  the low quantity to add
     * @param highQuantity the high quantity to add
     */
    public void add(BigDecimal lowQuantity, BigDecimal highQuantity) {
        this.lowQuantity = this.lowQuantity.add(lowQuantity);
        this.highQuantity = this.highQuantity.add(highQuantity);
    }

    /**
     * Determines if a product should have a zero price.
     *
     * @return {@code true} if the product should have a zero price
     */
    public boolean getZeroPrice() {
        return zeroPrice;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TemplateProduct) {
            TemplateProduct other = (TemplateProduct) obj;
            return ObjectUtils.equals(product, other.product) && zeroPrice == other.zeroPrice;
        }
        return false;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return product.hashCode();
    }

}
