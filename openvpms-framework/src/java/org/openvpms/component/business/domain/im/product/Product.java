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


package org.openvpms.component.business.domain.im.product;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openvpms.component.business.domain.im.common.Entity;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a product category of objects
 *
 * @author Jim Alateras
 */
public class Product extends Entity implements org.openvpms.component.model.product.Product {

    /**
     * Maintains a list of {@link ProductPrice} for this product
     */
    private Set<org.openvpms.component.model.product.ProductPrice> productPrices = new HashSet<>();

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor
     */
    public Product() {
        // do nothing
    }

    /**
     * @return Returns the productPrices.
     */
    @Override
    public Set<org.openvpms.component.model.product.ProductPrice> getProductPrices() {
        return productPrices;
    }

    /**
     * Add the specified {@link ProductPrice} to the set.
     *
     * @param price the product price to add
     */
    public void addProductPrice(org.openvpms.component.model.product.ProductPrice price) {
        ((ProductPrice) price).setProduct(this);
        this.productPrices.add(price);
    }

    /**
     * Remove the specified {@link ProductPrice} from the set.
     *
     * @param price the product price to remove
     */
    public void removeProductPrice(org.openvpms.component.model.product.ProductPrice price) {
        this.productPrices.remove(price);
    }


    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#toString()
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.Entity#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        Product copy = (Product) super.clone();
        copy.productPrices = new HashSet<>(this.productPrices);

        return copy;
    }

    /**
     * @param productPrices The productPrices to set.
     */
    protected void setProductPrices(Set<org.openvpms.component.model.product.ProductPrice> productPrices) {
        this.productPrices = productPrices;
    }

}
