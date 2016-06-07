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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.product;

import org.openvpms.archetype.rules.math.Currency;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.product.PricingGroup;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static java.math.BigDecimal.ONE;

/**
 * Abstract implementation of {@link PricingContext}.
 *
 * @author Tim Anderson
 */
public abstract class AbstractPricingContext implements PricingContext {

    /**
     * The currency.
     */
    private final Currency currency;

    /**
     * The practice location. May be {@code null}
     */
    private final Party location;

    /**
     * The pricing group.
     */
    private PricingGroup pricingGroup;

    /**
     * The price rules.
     */
    private final ProductPriceRules rules;

    /**
     * Constructs a {@link AbstractPricingContext}.
     *
     * @param currency      the currency
     * @param location      the practice location. May be {@code null}
     * @param priceRules    the price rules
     * @param locationRules the location rules
     */
    public AbstractPricingContext(Currency currency, Party location, ProductPriceRules priceRules,
                                  LocationRules locationRules) {
        this.currency = currency;
        this.pricingGroup = getPricingGroup(location, locationRules);
        this.location = location;
        this.rules = priceRules;
    }

    /**
     * Constructs a {@link AbstractPricingContext}.
     *
     * @param currency     the currency
     * @param pricingGroup the pricing group
     * @param location     the practice location. May be {@code null}
     * @param priceRules   the price rules
     */
    public AbstractPricingContext(Currency currency, PricingGroup pricingGroup, Party location,
                                  ProductPriceRules priceRules) {
        this.currency = currency;
        this.pricingGroup = pricingGroup;
        this.location = location;
        this.rules = priceRules;
    }

    /**
     * Returns the tax-inclusive price given a tax-exclusive price.
     * <p/>
     * This takes into account:
     * <ul>
     * <li>customer tax exclusions</li>
     * <li>service ratios</li>
     * </ul>
     *
     * @param product the product
     * @param price   the tax-exclusive price
     * @return the tax-inclusive price, rounded according to the practice currency conventions
     */
    @Override
    public BigDecimal getPrice(Product product, ProductPrice price) {
        BigDecimal serviceRatio = getServiceRatio(product);
        // TODO - calculate service ratio before or after tax?
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal taxExPrice = price.getPrice();
        if (taxExPrice != null) {
            result = rules.getTaxIncPrice(taxExPrice, getTaxRate(product), currency);
            if (!MathRules.equals(serviceRatio, ONE)) {
                result = currency.roundPrice(result.multiply(serviceRatio));
            }
        }
        return result;
    }

    /**
     * Returns the fixed prices for a product.
     *
     * @param product the product
     * @param date    the date
     * @return the fixed prices
     */
    @Override
    public List<ProductPrice> getFixedPrices(Product product, Date date) {
        return rules.getProductPrices(product, ProductArchetypes.FIXED_PRICE, date, pricingGroup);
    }

    /**
     * Returns the default fixed price for a product.
     *
     * @param product the product
     * @param date    the date
     * @return the fixed price, or {@code null} if none is found
     */
    @Override
    public ProductPrice getFixedPrice(Product product, Date date) {
        return rules.getProductPrice(product, ProductArchetypes.FIXED_PRICE, date, pricingGroup.getGroup());
    }

    /**
     * Returns the unit price for a product.
     *
     * @param product the product
     * @param date    the date
     * @return the unit price, or {@code null} if none is found
     */
    @Override
    public ProductPrice getUnitPrice(Product product, Date date) {
        return rules.getProductPrice(product, ProductArchetypes.UNIT_PRICE, date, pricingGroup.getGroup());
    }

    /**
     * Determines the service ratio for a product at a practice location.
     *
     * @param product the product. May be {@code null}
     * @return the service ratio
     */
    public BigDecimal getServiceRatio(Product product) {
        BigDecimal result = BigDecimal.ONE;
        if (product != null && location != null) {
            result = rules.getServiceRatio(product, location);
        }
        return result;
    }

    /**
     * Sets the pricing group.
     *
     * @param group the pricing group
     */
    public void setPricingGroup(PricingGroup group) {
        this.pricingGroup = group;
    }

    /**
     * Returns the pricing group.
     *
     * @return the pricing group
     */
    public PricingGroup getPricingGroup() {
        return pricingGroup;
    }

    /**
     * Returns the price rules.
     *
     * @return the price rules
     */
    protected ProductPriceRules getRules() {
        return rules;
    }

    /**
     * Returns the tax rate for a product, minus any tax exclusions.
     *
     * @param product the product
     * @return the product tax rate
     */
    protected abstract BigDecimal getTaxRate(Product product);

    /**
     * Determines the pricing group from the location.
     *
     * @param location the location. May be {@code null}
     * @return the pricing group. May be {@code null}
     */
    protected PricingGroup getPricingGroup(Party location, LocationRules rules) {
        Lookup result = null;
        if (location != null) {
            result = rules.getPricingGroup(location);
        }
        return new PricingGroup(result);
    }
}
