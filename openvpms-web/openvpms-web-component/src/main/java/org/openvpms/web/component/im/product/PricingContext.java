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

package org.openvpms.web.component.im.product;

import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Determines the price for a product.
 * <p>
 * This takes into account:
 * <li>
 * <li>service ratios</li>
 * <li>pricing groups</li>
 * <li>customer tax rate exclusions</li>
 * </li>
 *
 * @author Tim Anderson
 */
public interface PricingContext {

    /**
     * Returns the service ratio for a product and date.
     *
     * @param product the product
     * @param date    the date
     * @return the service ratio, or {@code null} if none is defined
     */
    BigDecimal getServiceRatio(Product product, Date date);

    /**
     * Returns the tax-inclusive price given a tax-exclusive price and service ratio.
     * <p>
     * This takes into account customer tax exclusions.
     *
     * @param product      the product
     * @param price        the tax-exclusive price
     * @param serviceRatio the service ratio. May be {@code null}
     * @return the tax-inclusive price, rounded according to the currency conventions
     */
    BigDecimal getPrice(Product product, ProductPrice price, BigDecimal serviceRatio);

    /**
     * Returns the fixed prices for a product.
     *
     * @param product the product
     * @param date    the date
     * @return the fixed prices
     */
    List<ProductPrice> getFixedPrices(Product product, Date date);

    /**
     * Returns the default fixed price for a product.
     *
     * @param product the product
     * @param date    the date
     * @return the fixed price, or {@code null} if none is found
     */
    ProductPrice getFixedPrice(Product product, Date date);

    /**
     * Returns the unit price for a product.
     *
     * @param product the product
     * @param date    the date
     * @return the unit price, or {@code null} if none is found
     */
    ProductPrice getUnitPrice(Product product, Date date);

    /**
     * Returns the first product price with the specified short name and price, active as of the date.
     *
     * @param shortName    the price short name
     * @param price        the tax-inclusive price
     * @param serviceRatio the service ratio, or {@link BigDecimal#ONE} if no ratio applies
     * @param product      the product
     * @param date         the date
     * @return the product price, or {@code null} if none is found
     */
    ProductPrice getProductPrice(String shortName, BigDecimal price, BigDecimal serviceRatio, Product product,
                                 Date date);
}
