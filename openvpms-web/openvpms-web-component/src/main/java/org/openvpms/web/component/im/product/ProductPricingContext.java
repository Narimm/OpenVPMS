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

import org.openvpms.archetype.rules.finance.tax.TaxRules;
import org.openvpms.archetype.rules.math.Currency;
import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.product.PricingGroup;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.archetype.rules.product.ServiceRatioService;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.system.ServiceHelper;

import java.math.BigDecimal;

/**
 * Implementation of {@link PricingContext} for products.
 *
 * @author Tim Anderson
 */
public class ProductPricingContext extends AbstractPricingContext {

    /**
     * Determines if prices are displayed including tax.
     */
    private final boolean includeTax;

    /**
     * The tax rules.
     */
    private TaxRules taxRules;

    /**
     * Constructs a {@link ProductPricingContext}.
     *
     * @param currency      the currency
     * @param practice      the practice
     * @param location      the practice location. May be {@code null}
     * @param priceRules    the price rules
     * @param locationRules the location rules
     * @param serviceRatios the service ratios
     */
    public ProductPricingContext(Currency currency, Party practice, Party location, ProductPriceRules priceRules,
                                 LocationRules locationRules, ServiceRatioService serviceRatios) {
        super(currency, location, priceRules, locationRules, serviceRatios);
        taxRules = new TaxRules(practice, ServiceHelper.getArchetypeService());
        includeTax = includeTax(practice);
    }

    /**
     * Constructs a {@link ProductPricingContext}.
     *
     * @param currency      the currency
     * @param pricingGroup  the pricing group
     * @param practice      the practice
     * @param location      the practice location. May be {@code null}
     * @param priceRules    the price rules
     * @param serviceRatios the service ratios
     */
    public ProductPricingContext(Currency currency, PricingGroup pricingGroup, Party practice, Party location,
                                 ProductPriceRules priceRules, ServiceRatioService serviceRatios) {
        super(currency, pricingGroup, location, priceRules, serviceRatios);
        taxRules = new TaxRules(practice, ServiceHelper.getArchetypeService());
        includeTax = includeTax(practice);
    }

    /**
     * Returns the tax-inclusive price given a tax-exclusive price and service ratio.
     *
     * @param product the product
     * @param price   the tax-exclusive price
     * @param serviceRatio the service ratio. May be {@code null}
     * @return the tax-inclusive price, rounded according to the practice currency conventions
     */
    @Override
    public BigDecimal getPrice(Product product, ProductPrice price, BigDecimal serviceRatio) {
        BigDecimal result;
        if (includeTax) {
            result = super.getPrice(product, price, serviceRatio);
        } else {
            BigDecimal taxExPrice = price.getPrice();
            if (taxExPrice != null) {
                if (serviceRatio != null) {
                    taxExPrice = taxExPrice.multiply(serviceRatio);
                }
                // don't round to the minimum price as this only applies when tax is included
                result = getCurrency().round(taxExPrice);
            } else {
                result = BigDecimal.ZERO;
            }
        }
        return result;
    }

    /**
     * Returns the tax rate for a product, minus any tax exclusions.
     *
     * @param product the product
     * @return the product tax rate
     */
    @Override
    protected BigDecimal getTaxRate(Product product) {
        return includeTax ? taxRules.getTaxRate(product) : BigDecimal.ZERO;
    }

    /**
     * Determines if prices are displayed including tax.
     *
     * @param practice the practice
     * @return {@code true} if prices are displayed including tax
     */
    protected boolean includeTax(Party practice) {
        IMObjectBean bean = new IMObjectBean(practice);
        return bean.getBoolean("showPricesTaxInclusive", true);
    }

}
