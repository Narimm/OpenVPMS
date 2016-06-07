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

import org.openvpms.archetype.rules.finance.tax.TaxRules;
import org.openvpms.archetype.rules.math.Currency;
import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.product.PricingGroup;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.system.ServiceHelper;

import java.math.BigDecimal;

/**
 * Implementation of {@link PricingContext} for products.
 *
 * @author Tim Anderson
 */
public class ProductPricingContext extends AbstractPricingContext {

    private final boolean includeTax;
    private TaxRules taxRules;

    /**
     * Constructs a {@link ProductPricingContext}.
     *
     * @param currency      the currency
     * @param practice      the practice
     * @param location      the practice location. May be {@code null}
     * @param priceRules    the price rules
     * @param locationRules the location rules
     */
    public ProductPricingContext(Currency currency, Party practice, Party location, ProductPriceRules priceRules,
                                 LocationRules locationRules) {
        super(currency, location, priceRules, locationRules);
        taxRules = new TaxRules(practice, ServiceHelper.getArchetypeService());
        includeTax = includeTax(practice);
    }

    /**
     * Constructs a {@link ProductPricingContext}.
     *
     * @param currency     the currency
     * @param pricingGroup the pricing group
     * @param practice     the practice
     * @param location     the practice location. May be {@code null}
     * @param priceRules   the price rules
     */
    public ProductPricingContext(Currency currency, PricingGroup pricingGroup, Party practice, Party location,
                                 ProductPriceRules priceRules) {
        super(currency, pricingGroup, location, priceRules);
        taxRules = new TaxRules(practice, ServiceHelper.getArchetypeService());
        includeTax = includeTax(practice);
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

    protected boolean includeTax(Party practice) {
        IMObjectBean bean = new IMObjectBean(practice);
        return bean.getBoolean("showPricesTaxInclusive", true);
    }

}
