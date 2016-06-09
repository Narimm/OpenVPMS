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

import org.openvpms.archetype.rules.finance.tax.CustomerTaxRules;
import org.openvpms.archetype.rules.math.Currency;
import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;

import java.math.BigDecimal;

/**
 * Implementation of the {@link PricingContext} for customers.
 *
 * @author Tim Anderson
 */
public class CustomerPricingContext extends AbstractPricingContext {

    /**
     * The customer.
     */
    private final Party customer;

    /**
     * The customer tax rules.
     */
    private final CustomerTaxRules taxRules;

    /**
     * Constructs a {@link CustomerPricingContext}.
     *
     * @param customer      the customer
     * @param location      the practice location. May be {@code null}
     * @param currency      the currency
     * @param priceRules    the price rules
     * @param locationRules the location rules
     * @param taxRules      the tax rules
     */
    public CustomerPricingContext(Party customer, Party location, Currency currency, ProductPriceRules priceRules,
                                  LocationRules locationRules, CustomerTaxRules taxRules) {
        super(currency, location, priceRules, locationRules);
        this.customer = customer;
        this.taxRules = taxRules;
    }

    /**
     * Returns the tax rate for a product, minus any tax exclusions.
     *
     * @param product the product
     * @return the product tax rate
     */
    @Override
    protected BigDecimal getTaxRate(Product product) {
        return taxRules.getTaxRate(product, customer);
    }

}
