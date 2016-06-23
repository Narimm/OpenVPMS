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

import org.junit.Test;
import org.openvpms.archetype.rules.math.Currency;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.practice.PracticeArchetypes;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.archetype.rules.product.ProductPriceTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

/**
 * Tests the {@link ProductPricingContext}.
 *
 * @author Tim Anderson
 */
public class ProductPricingContextTestCase extends ArchetypeServiceTest {

    /**
     * Tests product pricing with a no tax and {@code showPricesTaxInclusive = false}.
     */
    @Test
    public void testProductPricingWithNoTaxAndShowPricesTaxExclusive() {
        checkPrice("10.05", "10.05", false, "0.00", "0.20");
        checkPrice("10.06", "10.062", false, "0.00", "0.20");
    }

    /**
     * Tests product pricing with a no tax and {@code showPricesTaxInclusive = true}.
     */
    @Test
    public void testProductPricingWithNoTaxAndShowPricesTaxInclusive() {
        checkPrice("10.00", "10.05", true, "0.00", "0.20");
        checkPrice("10.00", "10.062", true, "0.00", "0.20");
        checkPrice("10.20", "10.11", true, "0.00", "0.20");
    }

    /**
     * Tests product pricing with a 10% tax rate and {@code showPricesTaxInclusive = false}.
     */
    @Test
    public void testProductPricingWithTaxAndShowPricesTaxExclusive() {
        checkPrice("10.05", "10.05", false, "10.00", "0.20");
        checkPrice("10.06", "10.062", false, "10.00", "0.20");
    }

    /**
     * Tests product pricing with a 10% tax rate and {@code showPricesTaxInclusive = true}.
     */
    @Test
    public void testProductPricingWithTaxAndShowPricesTaxInclusive() {
        checkPrice("11.00", "10.05", true, "10.00", "0.20");
        checkPrice("11.00", "10.062", true, "10.00", "0.20");
        checkPrice("11.20", "10.11", true, "10.00", "0.20");
    }

    /**
     * Verifies a prices matches that expected.
     *
     * @param expected               the expected price
     * @param taxExPrice             the tax-exclusive price
     * @param showPricesTaxInclusive if {@code true}, the price should include tax
     * @param taxRate                the tax rate
     * @param minPrice               the currency minimum price
     */
    private void checkPrice(String expected, String taxExPrice, boolean showPricesTaxInclusive, String taxRate,
                            String minPrice) {
        Party practice = (Party) TestHelper.create(PracticeArchetypes.PRACTICE);
        IMObjectBean bean = new IMObjectBean(practice);
        bean.setValue("showPricesTaxInclusive", showPricesTaxInclusive);

        Party location = (Party) TestHelper.create(PracticeArchetypes.LOCATION);

        LocationRules locationRules = new LocationRules(getArchetypeService());
        ProductPriceRules productPriceRules = new ProductPriceRules(getArchetypeService());
        Currency currency = getCurrency(new BigDecimal(minPrice));
        ProductPricingContext context = new ProductPricingContext(currency, practice, location, productPriceRules,
                                                                  locationRules);

        Product product = (Product) TestHelper.create(ProductArchetypes.MEDICATION);
        BigDecimal rate = new BigDecimal(taxRate);
        if (!MathRules.isZero(rate)) {
            Lookup taxType = TestHelper.createTaxType(rate);
            product.addClassification(taxType);
        }
        BigDecimal price = context.getPrice(product, getPrice(taxExPrice));
        checkEquals(new BigDecimal(expected), price);
    }

    /**
     * Creates a product price.
     *
     * @param price the price
     * @return a new price
     */
    private ProductPrice getPrice(String price) {
        return ProductPriceTestHelper.createUnitPrice(price, "0", "0", "100", (Date) null, null);
    }

    /**
     * Creates a currency with a minimum price.
     *
     * @param minPrice the minimum price
     * @return a new currency
     */
    private Currency getCurrency(BigDecimal minPrice) {
        return new Currency(java.util.Currency.getInstance("AUD"), RoundingMode.HALF_UP, new BigDecimal("0.05"),
                            minPrice);
    }
}
