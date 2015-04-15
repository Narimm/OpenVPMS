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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.product.io;

import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;

/**
 * Links a product with an optional fixed and unit price.
 *
 * @author Tim Anderson
 */
class ProductPrices {

    /**
     * The product.
     */
    private final Product product;

    /**
     * The fixed price. May be {@code null}
     */
    private final ProductPrice fixedPrice;

    /**
     * The unit price. May be {@code null}
     */
    private final ProductPrice unitPrice;

    /**
     * Constructs a {@link ProductPrices}.
     *
     * @param product    the product
     * @param fixedPrice the fixed price. May be {@code null}
     * @param unitPrice  the unit price. May be {@code null}
     */
    public ProductPrices(Product product, ProductPrice fixedPrice, ProductPrice unitPrice) {
        this.product = product;
        this.fixedPrice = fixedPrice;
        this.unitPrice = unitPrice;
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
     * Returns the fixed price.
     *
     * @return the fixed price. May be {@code null}
     */
    public ProductPrice getFixedPrice() {
        return fixedPrice;
    }

    /**
     * Returns the unit price.
     *
     * @return the unit price. May be {@code null}
     */
    public ProductPrice getUnitPrice() {
        return unitPrice;
    }

}
