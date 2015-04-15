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

import org.openvpms.archetype.rules.product.io.PriceData;
import org.openvpms.archetype.rules.product.io.ProductData;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.web.component.im.util.IMObjectHelper;

/**
 * Product price data.
 *
 * @author Tim Anderson
 */
class ProductPriceData {

    /**
     * The product data.
     */
    private final ProductData productData;

    /**
     * The fixed price. May be {@code null}
     */
    private final PriceData fixedPrice;

    /**
     * The unit price. May be {@code null}
     */
    private final PriceData unitPrice;

    /**
     * The product. May be {@code null}
     */
    private Product product;

    /**
     * Constructs a {@link ProductPriceData}.
     *
     * @param productData the product
     * @param fixedPrice  the fixed price. May be {@code null}
     * @param unitPrice   the unit price. May be {@code null}
     */
    public ProductPriceData(ProductData productData, PriceData fixedPrice, PriceData unitPrice) {
        this.productData = productData;
        this.fixedPrice = fixedPrice;
        this.unitPrice = unitPrice;
    }

    /**
     * Returns the product.
     *
     * @return the product
     */
    public ProductData getProductData() {
        return productData;
    }

    /**
     * Returns the fixed price.
     *
     * @return the fixed price. May be {@code null}
     */
    public PriceData getFixedPrice() {
        return fixedPrice;
    }

    /**
     * Returns the unit price.
     *
     * @return the unit price. May be {@code null}
     */
    public PriceData getUnitPrice() {
        return unitPrice;
    }

    /**
     * Returns the product.
     *
     * @return the product. May be {@code null}
     */
    public Product getProduct() {
        if (product == null && productData.getReference() != null) {
            product = (Product) IMObjectHelper.getObject(productData.getReference(), null);
        }
        return product;
    }
}
