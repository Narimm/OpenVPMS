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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.product;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.math.Currencies;
import org.openvpms.archetype.rules.math.Currency;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.rules.product.PricingGroup.ALL;
import static org.openvpms.archetype.rules.product.ProductArchetypes.FIXED_PRICE;
import static org.openvpms.archetype.rules.product.ProductArchetypes.PRICING_GROUP;
import static org.openvpms.archetype.rules.product.ProductArchetypes.UNIT_PRICE;
import static org.openvpms.archetype.rules.product.ProductPriceTestHelper.addPriceTemplate;
import static org.openvpms.archetype.rules.product.ProductPriceTestHelper.createFixedPrice;
import static org.openvpms.archetype.rules.product.ProductPriceTestHelper.createPriceTemplate;
import static org.openvpms.archetype.rules.product.ProductPriceTestHelper.createUnitPrice;
import static org.openvpms.archetype.rules.util.DateRules.getToday;
import static org.openvpms.archetype.rules.util.DateRules.getTomorrow;
import static org.openvpms.archetype.rules.util.DateRules.getYesterday;
import static org.openvpms.archetype.test.TestHelper.getDate;
import static org.openvpms.archetype.test.TestHelper.getDatetime;


/**
 * Tests the {@link ProductPriceRules} class.
 *
 * @author Tim Anderson
 */
public class ProductPriceRulesTestCase extends AbstractProductTest {

    /**
     * The lookup service.
     */
    @Autowired
    ILookupService lookupService;

    /**
     * The <em>party.organisationPractice</em>, for taxes.
     */
    private Party practice;

    /**
     * The practice location currency.
     */
    private Currency currency;

    /**
     * The rules.
     */
    private ProductPriceRules rules;


    /**
     * Tests the {@link ProductPriceRules#getProductPrice(Product, String, Date, Lookup)} method.
     */
    @Test
    public void testGetProductPrice() {
        checkProductPrice(createMedication(), false);
        checkProductPrice(createMerchandise(), false);
        checkProductPrice(createService(), false);
        checkProductPrice(createPriceTemplate(), false);
        checkProductPrice(createTemplate(), false);
    }

    /**
     * Tests the {@link ProductPriceRules#getProductPrice(Product, String, Date, Lookup)} method for products
     * with multiple pricing groups.
     */
    @Test
    public void testGetProductPriceWithPriceGroups() {
        checkProductPriceWithPriceGroups(createMedication(), false);
        checkProductPriceWithPriceGroups(createMerchandise(), false);
        checkProductPriceWithPriceGroups(createService(), false);
        checkProductPriceWithPriceGroups(createPriceTemplate(), false);
        checkProductPriceWithPriceGroups(createTemplate(), false);
    }

    /**
     * Tests the {@link ProductPriceRules#getProductPrice} method for products that support
     * <em>product.priceTemplate</em>.
     */
    @Test
    public void testGetProductPriceForProductWithPriceTemplate() {
        checkProductPrice(createMedication(), true);
        checkProductPrice(createMerchandise(), true);
        checkProductPrice(createService(), true);
    }

    /**
     * Tests the {@link ProductPriceRules#getProductPrice} method for products with multiple pricing groups that support
     * <em>product.priceTemplate</em>.
     */
    @Test
    public void testGetProductPriceWithPriceGroupsForProductWithPriceTemplate() {
        checkProductPriceWithPriceGroups(createMedication(), true);
        checkProductPriceWithPriceGroups(createMerchandise(), true);
        checkProductPriceWithPriceGroups(createService(), true);
    }

    /**
     * Tests the {@link ProductPriceRules#getProductPrice(Product, BigDecimal, String, Date, Lookup)}  method.
     */
    @Test
    public void testGetProductPriceForPrice() {
        checkGetProductPriceForPrice(createMedication(), false);
        checkGetProductPriceForPrice(createMerchandise(), false);
        checkGetProductPriceForPrice(createService(), false);
        checkGetProductPriceForPrice(createPriceTemplate(), false);
        checkGetProductPriceForPrice(createTemplate(), false);
    }

    /**
     * Tests the {@link ProductPriceRules#getProductPrice(Product, BigDecimal, String, Date, Lookup)}  method, for
     * for products that support <em>product.priceTemplate</em>.
     */
    @Test
    public void testGetProductPriceForPriceForProductWithPriceTemplate() {
        checkGetProductPriceForPrice(createMedication(), true);
        checkGetProductPriceForPrice(createMerchandise(), true);
        checkGetProductPriceForPrice(createService(), true);
    }

    /**
     * Tests the {@link ProductPriceRules#getProductPrice(Product, BigDecimal, String, Date, Lookup)}  method.
     */
    @Test
    public void testGetProductPriceForPriceWithPriceGroups() {
        checkGetProductPriceForPriceWithPriceGroups(createMedication(), false);
        checkGetProductPriceForPriceWithPriceGroups(createMerchandise(), false);
        checkGetProductPriceForPriceWithPriceGroups(createService(), false);
        checkGetProductPriceForPriceWithPriceGroups(createPriceTemplate(), false);
        checkGetProductPriceForPriceWithPriceGroups(createTemplate(), false);
    }

    /**
     * Tests the {@link ProductPriceRules#getProductPrice(Product, BigDecimal, String, Date, Lookup)}  method, for
     * for products that support <em>product.priceTemplate</em>.
     */
    @Test
    public void testGetProductPriceForPriceWithPriceGroupsForProductWithPriceTemplate() {
        checkGetProductPriceForPriceWithPriceGroups(createMedication(), true);
        checkGetProductPriceForPriceWithPriceGroups(createMerchandise(), true);
        checkGetProductPriceForPriceWithPriceGroups(createService(), true);
    }

    /**
     * Tests the {@link ProductPriceRules#getProductPrices} method.
     */
    @Test
    public void testGetProductPrices() {
        checkGetProductPrices(createMedication());
        checkGetProductPrices(createMerchandise());
        checkGetProductPrices(createService());
        checkGetProductPrices(createPriceTemplate());
        checkGetProductPrices(createTemplate());
    }

    /**
     * Tests the {@link ProductPriceRules#getProductPrices} method for products that may be associated with an
     * <em>product.priceTemplate</em> product.
     */
    @Test
    public void testGetProductPricesForProductWithPriceTemplate() {
        checkGetProductPricesForProductWithPriceTemplate(createMedication());
        checkGetProductPricesForProductWithPriceTemplate(createMerchandise());
        checkGetProductPricesForProductWithPriceTemplate(createService());
    }

    /**
     * Tests the {@link ProductPriceRules#getProductPrices} method for products with multiple pricing groups.
     */
    @Test
    public void testGetProductPricesWithPriceGroups() {
        checkGetProductPricesWithPriceGroups(createMedication());
        checkGetProductPricesWithPriceGroups(createMerchandise());
        checkGetProductPricesWithPriceGroups(createService());
        checkGetProductPricesWithPriceGroups(createPriceTemplate());
        checkGetProductPricesWithPriceGroups(createTemplate());
    }

    /**
     * Tests the {@link ProductPriceRules#getProductPrices} method for products that may be associated with an
     * <em>product.priceTemplate</em> product.
     */
    @Test
    public void testGetProductPricesWithPriceGroupsForProductWithPriceTemplate() {
        checkGetProductPricesWithPriceGroupsForProductWithPriceTemplate(createMedication());
        checkGetProductPricesWithPriceGroupsForProductWithPriceTemplate(createMerchandise());
        checkGetProductPricesWithPriceGroupsForProductWithPriceTemplate(createService());
    }

    /**
     * Tests the {@link ProductPriceRules#getPrice} method.
     */
    @Test
    public void testGetPrice() {
        checkGetPrice(createMedication());
        checkGetPrice(createMerchandise());
        checkGetPrice(createService());
        checkGetPrice(createPriceTemplate());
        checkGetPrice(createTemplate());
    }

    /**
     * Tests the {@link ProductPriceRules#getMarkup} method.
     */
    @Test
    public void testGetMarkup() {
        checkGetMarkup(createMedication());
        checkGetMarkup(createMerchandise());
        checkGetMarkup(createService());
        checkGetMarkup(createPriceTemplate());
        checkGetMarkup(createTemplate());
    }

    /**
     * Tests the {@link ProductPriceRules#getMaxDiscount(ProductPrice)} method.
     */
    @Test
    public void testGetMaxDiscount() {
        ProductPrice price = ProductPriceTestHelper.createPrice(FIXED_PRICE, new Date(), new Date());
        checkEquals(new BigDecimal(100), rules.getMaxDiscount(price));

        IMObjectBean bean = new IMObjectBean(price);
        bean.setValue("maxDiscount", 10);
        checkEquals(BigDecimal.TEN, rules.getMaxDiscount(price));
    }

    /**
     * Tests the {@link ProductPriceRules#updateUnitPrices(Product, BigDecimal, Party, Currency)} method.
     */
    @Test
    public void testUpdatePrices() {
        BigDecimal cost = BigDecimal.ONE;
        BigDecimal markup = BigDecimal.valueOf(100);
        BigDecimal price = BigDecimal.valueOf(2);
        BigDecimal maxDiscount = BigDecimal.valueOf(100);

        ProductPrice unit1 = createUnitPrice(price, cost, markup, maxDiscount, null, getYesterday()); // inactive
        ProductPrice unit2 = createUnitPrice(price, cost, markup, maxDiscount, getToday(), null);     // active
        ProductPrice unit3 = createUnitPrice(price, cost, markup, maxDiscount, (Date) null, null);    // active
        ProductPrice unit4 = createUnitPrice(price, cost, markup, maxDiscount, getTomorrow(), null);  // inactive

        Product product = TestHelper.createProduct();
        product.addProductPrice(unit1);
        product.addProductPrice(unit2);
        product.addProductPrice(unit3);
        product.addProductPrice(unit4);

        save(product);

        BigDecimal newCost = BigDecimal.valueOf(2);
        List<ProductPrice> updated = rules.updateUnitPrices(product, newCost, practice, currency);
        assertEquals(2, updated.size());
        assertFalse(updated.contains(unit1));
        assertTrue(updated.contains(unit2));
        assertTrue(updated.contains(unit3));
        assertFalse(updated.contains(unit4));

        BigDecimal newPrice = new BigDecimal("4.40");
        checkPrice(unit1, cost, price);
        checkPrice(unit2, newCost, newPrice);
        checkPrice(unit3, newCost, newPrice);
        checkPrice(unit4, cost, price);
    }

    /**
     * Sets up the test case.
     * <p/>
     * This sets up the practice to have a 10% tax on all products.
     */
    @Before
    public void setUp() {
        practice = createPractice();
        rules = new ProductPriceRules(getArchetypeService(), lookupService);
        IMObjectBean bean = new IMObjectBean(practice);
        Currencies currencies = new Currencies();
        currency = currencies.getCurrency(bean.getString("currency"));
    }

    /**
     * Tests the {@link ProductPriceRules#getProductPrice(Product, BigDecimal, String, Date, Lookup)} method.
     *
     * @param product          the product to use
     * @param usePriceTemplate if {@code true} attach an <em>product.priceTemplate</em> to the product
     */
    private void checkProductPrice(Product product, boolean usePriceTemplate) {
        ProductPrice fixed1 = createFixedPrice("2008-01-01", "2008-01-31", false);
        ProductPrice fixed2 = createFixedPrice("2008-02-01", "2008-12-31", false);
        ProductPrice fixed3 = createFixedPrice("2008-03-01", null, true);

        ProductPrice unit1 = createUnitPrice("2008-01-01", "2008-01-10");
        ProductPrice unit2 = createUnitPrice("2008-02-01", null);

        assertNull(rules.getProductPrice(product, FIXED_PRICE, new Date(), null));
        assertNull(rules.getProductPrice(product, UNIT_PRICE, new Date(), null));

        product.addProductPrice(fixed1);
        product.addProductPrice(fixed2);
        product.addProductPrice(unit1);
        product.addProductPrice(unit2);

        checkPrice(null, FIXED_PRICE, "2007-01-01", product, null);
        checkPrice(fixed1, FIXED_PRICE, "2008-01-01", product, null);
        checkPrice(fixed1, FIXED_PRICE, "2008-01-31", product, null);
        checkPrice(fixed2, FIXED_PRICE, "2008-02-01", product, null);
        checkPrice(fixed2, FIXED_PRICE, "2008-12-31", product, null);
        checkPrice(null, FIXED_PRICE, "2009-01-01", product, null);

        checkPrice(null, UNIT_PRICE, "2007-12-31", product, null);
        checkPrice(unit1, UNIT_PRICE, "2008-01-01", product, null);
        checkPrice(unit1, UNIT_PRICE, "2008-01-10", product, null);
        checkPrice(null, UNIT_PRICE, "2008-01-11", product, null);
        checkPrice(unit2, UNIT_PRICE, "2008-02-01", product, null);
        checkPrice(unit2, UNIT_PRICE, "2010-02-01", product, null); // unbounded

        if (usePriceTemplate) {
            // verify that linked products are used if there are no matching prices for the date
            Product priceTemplate = createPriceTemplate(fixed3);
            priceTemplate.setName("XPriceTemplate");

            addPriceTemplate(product, priceTemplate, "2008-01-01", null);

            checkPrice(fixed2, FIXED_PRICE, "2008-02-01", product, null);

            // fixed3 overrides fixed2 as it is the default
            checkPrice(fixed3, FIXED_PRICE, "2008-03-01", product, null);
        }
    }

    /**
     * Tests the {@link ProductPriceRules#getProductPrice(Product, BigDecimal, String, Date, Lookup)} method
     * when a product has prices with different pricing groups.
     *
     * @param product          the product to use
     * @param usePriceTemplate if {@code true} attach an <em>product.priceTemplate</em> to the product
     */
    private void checkProductPriceWithPriceGroups(Product product, boolean usePriceTemplate) {
        Lookup groupA = TestHelper.getLookup(PRICING_GROUP, "A");
        Lookup groupB = TestHelper.getLookup(PRICING_GROUP, "B");

        ProductPrice fixed1A = createFixedPrice("2008-01-01", "2008-01-31", false, groupA);
        ProductPrice fixed1B = createFixedPrice("2008-01-01", "2008-01-31", false, groupB);
        ProductPrice fixed1C = createFixedPrice("2008-01-01", "2008-01-31", false);

        ProductPrice fixed2A = createFixedPrice("2008-02-01", "2008-12-31", false, groupA);
        ProductPrice fixed2B = createFixedPrice("2008-02-01", "2008-12-31", false, groupB);
        ProductPrice fixed2C = createFixedPrice("2008-02-01", "2008-12-31", false);

        ProductPrice fixed3A = createFixedPrice("2008-03-01", null, true, groupA);
        ProductPrice fixed3B = createFixedPrice("2008-03-01", null, true, groupB);
        ProductPrice fixed3C = createFixedPrice("2008-03-01", null, true);

        ProductPrice unit1A = createUnitPrice("2008-01-01", "2008-01-10", groupA);
        ProductPrice unit1B = createUnitPrice("2008-01-01", "2008-01-10", groupB);
        ProductPrice unit1C = createUnitPrice("2008-01-01", "2008-01-10");

        ProductPrice unit2A = createUnitPrice("2008-02-01", null, groupA);
        ProductPrice unit2B = createUnitPrice("2008-02-01", null, groupB);
        ProductPrice unit2C = createUnitPrice("2008-02-01", null);

        assertNull(rules.getProductPrice(product, FIXED_PRICE, new Date(), null));
        assertNull(rules.getProductPrice(product, UNIT_PRICE, new Date(), null));

        product.addProductPrice(fixed1A);
        product.addProductPrice(fixed1B);
        product.addProductPrice(fixed1C);
        product.addProductPrice(fixed2A);
        product.addProductPrice(fixed2B);
        product.addProductPrice(fixed2C);
        product.addProductPrice(unit1A);
        product.addProductPrice(unit1B);
        product.addProductPrice(unit1C);
        product.addProductPrice(unit2A);
        product.addProductPrice(unit2B);
        product.addProductPrice(unit2C);

        checkPrice(null, FIXED_PRICE, "2007-01-01", product, groupA);
        checkPrice(null, FIXED_PRICE, "2007-01-01", product, groupB);
        checkPrice(null, FIXED_PRICE, "2007-01-01", product, null);

        checkPrice(fixed1A, FIXED_PRICE, "2008-01-01", product, groupA);
        checkPrice(fixed1A, FIXED_PRICE, "2008-01-31", product, groupA);
        checkPrice(fixed2A, FIXED_PRICE, "2008-02-01", product, groupA);
        checkPrice(fixed2A, FIXED_PRICE, "2008-12-31", product, groupA);
        checkPrice(null, FIXED_PRICE, "2009-01-01", product, null);

        checkPrice(null, UNIT_PRICE, "2007-12-31", product, null);
        checkPrice(unit1A, UNIT_PRICE, "2008-01-01", product, groupA);
        checkPrice(unit1A, UNIT_PRICE, "2008-01-10", product, groupA);
        checkPrice(unit1B, UNIT_PRICE, "2008-01-01", product, groupB);
        checkPrice(unit1B, UNIT_PRICE, "2008-01-10", product, groupB);
        checkPrice(unit1C, UNIT_PRICE, "2008-01-01", product, null);
        checkPrice(unit1C, UNIT_PRICE, "2008-01-10", product, null);

        checkPrice(null, UNIT_PRICE, "2008-01-11", product, null);
        checkPrice(unit2A, UNIT_PRICE, "2008-02-01", product, groupA);
        checkPrice(unit2A, UNIT_PRICE, "2010-02-01", product, groupA); // unbounded
        checkPrice(unit2B, UNIT_PRICE, "2008-02-01", product, groupB);
        checkPrice(unit2B, UNIT_PRICE, "2010-02-01", product, groupB); // unbounded
        checkPrice(unit2C, UNIT_PRICE, "2008-02-01", product, null);
        checkPrice(unit2C, UNIT_PRICE, "2010-02-01", product, null); // unbounded

        if (usePriceTemplate) {
            // verify that linked products are used if there are no matching prices for the date
            Product priceTemplate = createPriceTemplate(fixed3A, fixed3B, fixed3C);
            priceTemplate.setName("XPriceTemplate");

            addPriceTemplate(product, priceTemplate, "2008-01-01", null);

            checkPrice(fixed2A, FIXED_PRICE, "2008-02-01", product, groupA);

            // fixed3 overrides fixed2 as it is the default
            checkPrice(fixed3A, FIXED_PRICE, "2008-03-01", product, groupA);
            checkPrice(fixed3B, FIXED_PRICE, "2008-03-01", product, groupB);
            checkPrice(fixed3C, FIXED_PRICE, "2008-03-01", product, null);
        }
    }

    /**
     * Tests the {@link ProductPriceRules#getProductPrice(Product, BigDecimal, String, Date, Lookup)}  method.
     *
     * @param product          the product to test
     * @param usePriceTemplate if {@code true} attach an <em>product.priceTemplate</em> to the product
     */
    private void checkGetProductPriceForPrice(Product product, boolean usePriceTemplate) {
        BigDecimal one = BigDecimal.ONE;
        BigDecimal two = new BigDecimal("2.0");
        BigDecimal three = new BigDecimal("3.0");

        ProductPrice fixed1 = createFixedPrice(getDate("2008-01-01"), getDatetime("2008-01-31 10:00:00"), false);
        ProductPrice fixed2 = createFixedPrice("2008-02-01", "2008-12-31", false);
        ProductPrice fixed3 = createFixedPrice("2008-03-01", null, true);

        fixed1.setPrice(one);
        fixed2.setPrice(two);
        fixed3.setPrice(three);

        ProductPrice unit1 = createUnitPrice("2008-01-01", "2008-01-10");
        ProductPrice unit2 = createUnitPrice("2008-02-01", null);

        unit1.setPrice(one);
        unit2.setPrice(two);

        // should be no prices returned until one is registered
        assertNull(rules.getProductPrice(product, one, FIXED_PRICE, new Date(), null));
        assertNull(rules.getProductPrice(product, one, UNIT_PRICE, new Date(), null));

        // add prices
        product.addProductPrice(fixed1);
        product.addProductPrice(fixed2);
        product.addProductPrice(unit1);
        product.addProductPrice(unit2);

        checkPrice(null, two, FIXED_PRICE, "2008-01-01", product, null);
        checkPrice(fixed1, one, FIXED_PRICE, "2008-01-01", product, null);
        checkPrice(null, two, FIXED_PRICE, "2008-01-31", product, null);
        checkPrice(fixed1, one, FIXED_PRICE, "2008-01-31", product, null);
        checkPrice(null, one, FIXED_PRICE, "2008-02-01", product, null);

        // verify time is ignored
        checkPrice(fixed2, two, FIXED_PRICE, getDatetime("2008-12-31 23:45:00"), product, null);

        checkPrice(null, two, FIXED_PRICE, "2009-01-01", product, null);

        checkPrice(null, one, UNIT_PRICE, "2007-12-31", product, null);
        checkPrice(null, two, UNIT_PRICE, "2008-01-01", product, null);
        checkPrice(unit1, one, UNIT_PRICE, "2008-01-01", product, null);
        checkPrice(null, two, UNIT_PRICE, "2008-01-10", product, null);
        checkPrice(unit1, one, UNIT_PRICE, "2008-01-10", product, null);
        checkPrice(null, two, UNIT_PRICE, "2008-01-11", product, null);
        checkPrice(null, three, UNIT_PRICE, "2008-02-01", product, null);
        checkPrice(unit2, two, UNIT_PRICE, "2008-02-01", product, null);
        checkPrice(null, three, UNIT_PRICE, "2010-02-01", product, null);
        checkPrice(unit2, two, UNIT_PRICE, "2010-02-01", product, null); // unbounded

        if (usePriceTemplate) {
            // verify that linked products are used if there are no matching prices
            // for the date
            Product priceTemplate = createPriceTemplate();
            priceTemplate.addProductPrice(fixed3);
            priceTemplate.setName("XPriceTemplate");
            save(priceTemplate);

            EntityBean bean = new EntityBean(product);
            EntityRelationship relationship = bean.addRelationship(
                    ProductArchetypes.PRODUCT_LINK_RELATIONSHIP, priceTemplate);
            relationship.setActiveStartTime(getDate("2008-01-01"));
            bean.save();

            checkPrice(fixed2, two, FIXED_PRICE, "2008-02-01", product, null);
            checkPrice(fixed3, three, FIXED_PRICE, "2008-03-01", product, null);
            checkPrice(fixed2, two, FIXED_PRICE, "2008-03-01", product, null);
        }
    }

    /**
     * Tests the {@link ProductPriceRules#getProductPrice(Product, BigDecimal, String, Date, Lookup)}  method
     * when a product has prices with different pricing groups.
     *
     * @param product          the product to test
     * @param usePriceTemplate if {@code true} attach an <em>product.priceTemplate</em> to the product
     */
    private void checkGetProductPriceForPriceWithPriceGroups(Product product, boolean usePriceTemplate) {
        Lookup groupA = TestHelper.getLookup(PRICING_GROUP, "A");
        Lookup groupB = TestHelper.getLookup(PRICING_GROUP, "B");
        BigDecimal one = BigDecimal.ONE;
        BigDecimal two = new BigDecimal("2.0");
        BigDecimal three = new BigDecimal("3.0");

        ProductPrice fixed1A = createFixedPrice(getDate("2008-01-01"), getDatetime("2008-01-31 10:00:00"), false,
                                                groupA);
        ProductPrice fixed1B = createFixedPrice(getDate("2008-01-01"), getDatetime("2008-01-31 10:00:00"), false,
                                                groupB);
        ProductPrice fixed1C = createFixedPrice(getDate("2008-01-01"), getDatetime("2008-01-31 10:00:00"), false);
        ProductPrice fixed2A = createFixedPrice("2008-02-01", "2008-12-31", false, groupA);
        ProductPrice fixed2B = createFixedPrice("2008-02-01", "2008-12-31", false, groupB);
        ProductPrice fixed2C = createFixedPrice("2008-02-01", "2008-12-31", false);
        ProductPrice fixed3A = createFixedPrice("2008-03-01", null, true, groupA);
        ProductPrice fixed3B = createFixedPrice("2008-03-01", null, true, groupB);
        ProductPrice fixed3C = createFixedPrice("2008-03-01", null, true);

        fixed1A.setPrice(one);
        fixed1B.setPrice(one);
        fixed1C.setPrice(one);
        fixed2A.setPrice(two);
        fixed2B.setPrice(two);
        fixed2C.setPrice(two);
        fixed3A.setPrice(three);
        fixed3B.setPrice(three);
        fixed3C.setPrice(three);

        ProductPrice unit1A = createUnitPrice("2008-01-01", "2008-01-10", groupA);
        ProductPrice unit1B = createUnitPrice("2008-01-01", "2008-01-10", groupB);
        ProductPrice unit1C = createUnitPrice("2008-01-01", "2008-01-10");
        ProductPrice unit2A = createUnitPrice("2008-02-01", null, groupA);
        ProductPrice unit2B = createUnitPrice("2008-02-01", null, groupB);
        ProductPrice unit2C = createUnitPrice("2008-02-01", null);

        unit1A.setPrice(one);
        unit1B.setPrice(one);
        unit1C.setPrice(one);
        unit2A.setPrice(two);
        unit2B.setPrice(two);
        unit2C.setPrice(two);

        // should be no prices returned until one is registered
        assertNull(rules.getProductPrice(product, one, FIXED_PRICE, new Date(), null));
        assertNull(rules.getProductPrice(product, one, UNIT_PRICE, new Date(), null));

        // add prices
        product.addProductPrice(fixed1A);
        product.addProductPrice(fixed1B);
        product.addProductPrice(fixed1C);
        product.addProductPrice(fixed2A);
        product.addProductPrice(fixed2B);
        product.addProductPrice(fixed2C);
        product.addProductPrice(unit1A);
        product.addProductPrice(unit1B);
        product.addProductPrice(unit1C);
        product.addProductPrice(unit2A);
        product.addProductPrice(unit2B);
        product.addProductPrice(unit2C);

        checkPrice(null, two, FIXED_PRICE, "2008-01-01", product, null);
        checkPrice(fixed1A, one, FIXED_PRICE, "2008-01-01", product, groupA);
        checkPrice(fixed1B, one, FIXED_PRICE, "2008-01-01", product, groupB);
        checkPrice(fixed1C, one, FIXED_PRICE, "2008-01-01", product, null);
        checkPrice(null, two, FIXED_PRICE, "2008-01-31", product, null);
        checkPrice(fixed1A, one, FIXED_PRICE, "2008-01-31", product, groupA);
        checkPrice(fixed1B, one, FIXED_PRICE, "2008-01-31", product, groupB);
        checkPrice(fixed1C, one, FIXED_PRICE, "2008-01-31", product, null);
        checkPrice(null, one, FIXED_PRICE, "2008-02-01", product, null);

        // verify time is ignored
        checkPrice(fixed2A, two, FIXED_PRICE, getDatetime("2008-12-31 23:45:00"), product, groupA);
        checkPrice(fixed2B, two, FIXED_PRICE, getDatetime("2008-12-31 23:45:00"), product, groupB);
        checkPrice(fixed2C, two, FIXED_PRICE, getDatetime("2008-12-31 23:45:00"), product, null);

        checkPrice(null, two, FIXED_PRICE, "2009-01-01", product, null);

        checkPrice(null, one, UNIT_PRICE, "2007-12-31", product, null);
        checkPrice(null, two, UNIT_PRICE, "2008-01-01", product, null);
        checkPrice(unit1A, one, UNIT_PRICE, "2008-01-01", product, groupA);
        checkPrice(unit1B, one, UNIT_PRICE, "2008-01-01", product, groupB);
        checkPrice(unit1C, one, UNIT_PRICE, "2008-01-01", product, null);
        checkPrice(null, two, UNIT_PRICE, "2008-01-10", product, null);
        checkPrice(unit1A, one, UNIT_PRICE, "2008-01-10", product, groupA);
        checkPrice(unit1B, one, UNIT_PRICE, "2008-01-10", product, groupB);
        checkPrice(unit1C, one, UNIT_PRICE, "2008-01-10", product, null);
        checkPrice(null, two, UNIT_PRICE, "2008-01-11", product, null);
        checkPrice(null, three, UNIT_PRICE, "2008-02-01", product, null);
        checkPrice(unit2A, two, UNIT_PRICE, "2008-02-01", product, groupA);
        checkPrice(unit2B, two, UNIT_PRICE, "2008-02-01", product, groupB);
        checkPrice(unit2C, two, UNIT_PRICE, "2008-02-01", product, null);
        checkPrice(null, three, UNIT_PRICE, "2010-02-01", product, null);
        checkPrice(unit2A, two, UNIT_PRICE, "2010-02-01", product, groupA); // unbounded
        checkPrice(unit2B, two, UNIT_PRICE, "2010-02-01", product, groupB); // unbounded
        checkPrice(unit2C, two, UNIT_PRICE, "2010-02-01", product, null); // unbounded

        if (usePriceTemplate) {
            // verify that linked products are used if there are no matching prices
            // for the date
            Product priceTemplate = createPriceTemplate();
            priceTemplate.addProductPrice(fixed3A);
            priceTemplate.addProductPrice(fixed3B);
            priceTemplate.addProductPrice(fixed3C);
            priceTemplate.setName("XPriceTemplate");
            save(priceTemplate);

            EntityBean bean = new EntityBean(product);
            EntityRelationship relationship = bean.addRelationship(
                    ProductArchetypes.PRODUCT_LINK_RELATIONSHIP, priceTemplate);
            relationship.setActiveStartTime(getDate("2008-01-01"));
            bean.save();

            checkPrice(fixed2A, two, FIXED_PRICE, "2008-02-01", product, groupA);
            checkPrice(fixed2B, two, FIXED_PRICE, "2008-02-01", product, groupB);
            checkPrice(fixed2C, two, FIXED_PRICE, "2008-02-01", product, null);
            checkPrice(fixed3A, three, FIXED_PRICE, "2008-03-01", product, groupA);
            checkPrice(fixed3B, three, FIXED_PRICE, "2008-03-01", product, groupB);
            checkPrice(fixed3C, three, FIXED_PRICE, "2008-03-01", product, null);
            checkPrice(fixed2A, two, FIXED_PRICE, "2008-03-01", product, groupA);
            checkPrice(fixed2B, two, FIXED_PRICE, "2008-03-01", product, groupB);
            checkPrice(fixed2C, two, FIXED_PRICE, "2008-03-01", product, null);
        }
    }

    /**
     * Tests the {@link ProductPriceRules#getProductPrices} method.
     *
     * @param product the product
     */
    private void checkGetProductPrices(Product product) {
        ProductPrice fixed1 = ProductPriceTestHelper.createPrice(FIXED_PRICE, "2008-01-01", "2008-01-31");
        ProductPrice fixed2 = ProductPriceTestHelper.createPrice(FIXED_PRICE, "2008-01-01", "2008-12-31");

        product.addProductPrice(fixed1);
        product.addProductPrice(fixed2);
        save(product);

        product = get(product);

        List<ProductPrice> prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2007-01-01"), ALL);
        assertTrue(prices.isEmpty());

        prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2008-01-01"), ALL);
        assertEquals(2, prices.size());
        assertTrue(prices.contains(fixed1));
        assertTrue(prices.contains(fixed2));

        prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2008-02-01"), ALL);
        assertEquals(1, prices.size());
        assertFalse(prices.contains(fixed1));
        assertTrue(prices.contains(fixed2));

        prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2009-01-01"), ALL);
        assertEquals(0, prices.size());
    }

    /**
     * Tests the {@link ProductPriceRules#getProductPrices} method when a product has prices with different pricing
     * groups.
     *
     * @param product the product
     */
    private void checkGetProductPricesWithPriceGroups(Product product) {
        Lookup groupA = TestHelper.getLookup(PRICING_GROUP, "A");
        Lookup groupB = TestHelper.getLookup(PRICING_GROUP, "B");
        ProductPrice fixed1A = ProductPriceTestHelper.createFixedPrice("2008-01-01", "2008-01-31", false, groupA);
        ProductPrice fixed1B = ProductPriceTestHelper.createFixedPrice("2008-01-01", "2008-01-31", false, groupB);
        ProductPrice fixed1C = ProductPriceTestHelper.createFixedPrice("2008-01-01", "2008-01-31", false);
        ProductPrice fixed2A = ProductPriceTestHelper.createFixedPrice("2008-01-01", "2008-12-31", false, groupA);
        ProductPrice fixed2B = ProductPriceTestHelper.createFixedPrice("2008-01-01", "2008-12-31", false, groupB);
        ProductPrice fixed2C = ProductPriceTestHelper.createFixedPrice("2008-01-01", "2008-12-31", false);

        product.addProductPrice(fixed1A);
        product.addProductPrice(fixed1B);
        product.addProductPrice(fixed1C);
        product.addProductPrice(fixed2A);
        product.addProductPrice(fixed2B);
        product.addProductPrice(fixed2C);
        save(product);

        product = get(product);

        List<ProductPrice> prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2007-01-01"), ALL);
        assertTrue(prices.isEmpty());

        prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2008-01-01"), ALL);
        checkPrices(prices, fixed1A, fixed1B, fixed1C, fixed2A, fixed2B, fixed2C);

        prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2008-01-01"), new PricingGroup(groupA));
        checkPrices(prices, fixed1A, fixed1C, fixed2A, fixed2C);

        prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2008-01-01"), new PricingGroup(groupB));
        checkPrices(prices, fixed1B, fixed1C, fixed2B, fixed2C);

        prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2008-01-01"), new PricingGroup(null));
        checkPrices(prices, fixed1C, fixed2C);

        prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2008-02-01"), ALL);
        checkPrices(prices, fixed2A, fixed2B, fixed2C);

        prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2008-02-01"), new PricingGroup(groupA));
        checkPrices(prices, fixed2A, fixed2C);

        prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2008-02-01"), new PricingGroup(groupB));
        checkPrices(prices, fixed2B, fixed2C);

        prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2008-02-01"), new PricingGroup(null));
        checkPrices(prices, fixed2C);

        prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2009-01-01"), ALL);
        assertEquals(0, prices.size());

        prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2009-01-01"), new PricingGroup(groupA));
        assertEquals(0, prices.size());

        prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2009-01-01"), new PricingGroup(groupB));
        assertEquals(0, prices.size());

        prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2009-01-01"), new PricingGroup(null));
        assertEquals(0, prices.size());
    }

    /**
     * Checks the {@link ProductPriceRules#getProductPrices(Product, String, Date, PricingGroup)} method for products
     * that may be linked to a price template.
     *
     * @param product the product. Either a medication, merchandise or service
     */
    private void checkGetProductPricesForProductWithPriceTemplate(Product product) {
        ProductPrice fixed1 = ProductPriceTestHelper.createPrice(FIXED_PRICE, "2008-01-01", "2008-01-31");
        ProductPrice fixed2 = ProductPriceTestHelper.createPrice(FIXED_PRICE, "2008-01-01", "2008-12-31");
        ProductPrice fixed3 = ProductPriceTestHelper.createPrice(FIXED_PRICE, "2008-02-01", null);

        product.addProductPrice(fixed1);
        product.addProductPrice(fixed2);

        Product priceTemplate = createPriceTemplate();
        priceTemplate.addProductPrice(fixed3);
        priceTemplate.setName("XPriceTemplate");
        save(priceTemplate);

        EntityBean bean = new EntityBean(product);
        EntityRelationship relationship = bean.addRelationship(
                ProductArchetypes.PRODUCT_LINK_RELATIONSHIP, priceTemplate);
        relationship.setActiveStartTime(getDate("2008-01-01"));
        bean.save();

        product = get(product);

        List<ProductPrice> prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2007-01-01"), ALL);
        assertTrue(prices.isEmpty());

        prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2008-01-01"), ALL);
        assertEquals(2, prices.size());
        assertTrue(prices.contains(fixed1));
        assertTrue(prices.contains(fixed2));
        assertFalse(prices.contains(fixed3));

        prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2008-02-01"), ALL);
        assertEquals(2, prices.size());
        assertFalse(prices.contains(fixed1));
        assertTrue(prices.contains(fixed2));
        assertTrue(prices.contains(fixed3));

        prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2009-01-01"), ALL);
        assertEquals(1, prices.size());
        assertFalse(prices.contains(fixed1));
        assertFalse(prices.contains(fixed2));
        assertTrue(prices.contains(fixed3));
    }

    /**
     * Checks the {@link ProductPriceRules#getProductPrices(Product, String, Date, PricingGroup)} method for products
     * that may be linked to a price template.
     *
     * @param product the product. Either a medication, merchandise or service
     */
    private void checkGetProductPricesWithPriceGroupsForProductWithPriceTemplate(Product product) {
        Lookup groupA = TestHelper.getLookup(PRICING_GROUP, "A");
        Lookup groupB = TestHelper.getLookup(PRICING_GROUP, "B");

        ProductPrice fixed1A = ProductPriceTestHelper.createFixedPrice("2008-01-01", "2008-01-31", false, groupA);
        ProductPrice fixed1B = ProductPriceTestHelper.createFixedPrice("2008-01-01", "2008-01-31", false, groupB);
        ProductPrice fixed1C = ProductPriceTestHelper.createFixedPrice("2008-01-01", "2008-01-31", false);

        ProductPrice fixed2A = ProductPriceTestHelper.createFixedPrice("2008-01-01", "2008-12-31", false, groupA);
        ProductPrice fixed2B = ProductPriceTestHelper.createFixedPrice("2008-01-01", "2008-12-31", false, groupB);
        ProductPrice fixed2C = ProductPriceTestHelper.createFixedPrice("2008-01-01", "2008-12-31", false);

        ProductPrice fixed3A = ProductPriceTestHelper.createFixedPrice("2008-02-01", null, false, groupA);
        ProductPrice fixed3B = ProductPriceTestHelper.createFixedPrice("2008-02-01", null, false, groupB);
        ProductPrice fixed3C = ProductPriceTestHelper.createFixedPrice("2008-02-01", null, false);

        product.addProductPrice(fixed1A);
        product.addProductPrice(fixed1B);
        product.addProductPrice(fixed1C);
        product.addProductPrice(fixed2A);
        product.addProductPrice(fixed2B);
        product.addProductPrice(fixed2C);

        Product priceTemplate = createPriceTemplate();
        priceTemplate.addProductPrice(fixed3A);
        priceTemplate.addProductPrice(fixed3B);
        priceTemplate.addProductPrice(fixed3C);
        priceTemplate.setName("XPriceTemplate");
        save(priceTemplate);

        EntityBean bean = new EntityBean(product);
        EntityRelationship relationship = bean.addRelationship(
                ProductArchetypes.PRODUCT_LINK_RELATIONSHIP, priceTemplate);
        relationship.setActiveStartTime(getDate("2008-01-01"));
        bean.save();

        product = get(product);

        List<ProductPrice> prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2007-01-01"), ALL);
        assertTrue(prices.isEmpty());

        prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2008-01-01"), ALL);
        checkPrices(prices, fixed1A, fixed1B, fixed1C, fixed2A, fixed2B, fixed2C);

        prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2008-01-01"), new PricingGroup(groupA));
        checkPrices(prices, fixed1A, fixed1C, fixed2A, fixed2C);

        prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2008-01-01"), new PricingGroup(groupB));
        checkPrices(prices, fixed1B, fixed1C, fixed2B, fixed2C);

        prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2008-01-01"), new PricingGroup(null));
        checkPrices(prices, fixed1C, fixed2C);

        prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2008-02-01"), ALL);
        checkPrices(prices, fixed2A, fixed2B, fixed2C, fixed3A, fixed3B, fixed3C);

        prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2008-02-01"), new PricingGroup(groupA));
        checkPrices(prices, fixed2A, fixed2C, fixed3A, fixed3C);

        prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2008-02-01"), new PricingGroup(groupB));
        checkPrices(prices, fixed2B, fixed2C, fixed3B, fixed3C);

        prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2008-02-01"), new PricingGroup(null));
        checkPrices(prices, fixed2C, fixed3C);

        prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2009-01-01"), ALL);
        checkPrices(prices, fixed3A, fixed3B, fixed3C);

        prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2009-01-01"), new PricingGroup(groupA));
        checkPrices(prices, fixed3A, fixed3C);

        prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2009-01-01"), new PricingGroup(groupB));
        checkPrices(prices, fixed3B, fixed3C);

        prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2009-01-01"), new PricingGroup(null));
        checkPrices(prices, fixed3C);
    }

    /**
     * Tests the {@link ProductPriceRules#getPrice(Product, BigDecimal, BigDecimal, Party, Currency)} method.
     *
     * @param product the product to test
     */
    private void checkGetPrice(Product product) {
        BigDecimal cost = BigDecimal.ONE;
        BigDecimal markup = BigDecimal.valueOf(100); // 100% markup
        BigDecimal price = rules.getPrice(product, cost, markup, practice, currency);
        checkEquals(new BigDecimal("2.20"), price);
    }

    /**
     * Tests the {@link ProductPriceRules#getMarkup} method.
     *
     * @param product the product to test
     */
    private void checkGetMarkup(Product product) {
        BigDecimal cost = BigDecimal.ONE;
        BigDecimal price = new BigDecimal("2.20");
        BigDecimal markup = rules.getMarkup(product, cost, price, practice);
        checkEquals(BigDecimal.valueOf(100), markup);
    }

    /**
     * Checks prices against those expected.
     *
     * @param expected the expected prices
     * @param prices   the actual prices
     */
    private void checkPrices(List<ProductPrice> expected, ProductPrice... prices) {
        assertEquals(expected.size(), prices.length);
        for (ProductPrice price : prices) {
            assertTrue(expected.contains(price));
        }
    }

    /**
     * Helper to create a new medication product.
     *
     * @return a new medication product
     */
    private Product createMedication() {
        return createProduct(ProductArchetypes.MEDICATION);
    }

    /**
     * Helper to create a new merchandise product.
     *
     * @return a new merchandise product
     */
    private Product createMerchandise() {
        return createProduct(ProductArchetypes.MERCHANDISE);
    }

    /**
     * Helper to create a new service product.
     *
     * @return a new service product
     */
    private Product createService() {
        return createProduct(ProductArchetypes.SERVICE);
    }

    /**
     * Helper to create a new template product.
     *
     * @return a new template product
     */
    private Product createTemplate() {
        return createProduct(ProductArchetypes.TEMPLATE);
    }

    /**
     * Helper to create a new product.
     *
     * @param shortName the product archetype short name
     * @return a new product
     */
    private Product createProduct(String shortName) {
        Product product = (Product) create(shortName);
        product.setName("XProduct-" + System.currentTimeMillis());
        return product;
    }

    /**
     * Verifies a price matches that expected.
     *
     * @param expected     the expected price
     * @param shortName    the price short name
     * @param date         the date that the price applies to
     * @param product      the product to use
     * @param pricingGroup the pricing group. May be {@code null}
     */
    private void checkPrice(ProductPrice expected, String shortName, String date, Product product,
                            Lookup pricingGroup) {
        checkPrice(expected, shortName, getDate(date), product, pricingGroup);
    }

    /**
     * Verifies a price matches that expected.
     *
     * @param expected     the expected price
     * @param shortName    the price short name
     * @param date         the date that the price applies to
     * @param product      the product to use
     * @param pricingGroup the pricing group. May be {@code null}
     */
    private void checkPrice(ProductPrice expected, String shortName, Date date, Product product, Lookup pricingGroup) {
        assertEquals(expected, rules.getProductPrice(product, shortName, date, pricingGroup));
    }

    /**
     * Verifies a price matches that expected.
     *
     * @param expected     the expected price
     * @param price        the price
     * @param shortName    the price short name
     * @param date         the date that the price applies to
     * @param product      the product to use
     * @param pricingGroup the pricing group. May be {@code null}
     */
    private void checkPrice(ProductPrice expected, BigDecimal price, String shortName, String date, Product product,
                            Lookup pricingGroup) {
        checkPrice(expected, price, shortName, getDate(date), product, pricingGroup);
    }

    /**
     * Verifies a price matches that expected.
     *
     * @param expected     the expected price
     * @param price        the price
     * @param shortName    the price short name
     * @param date         the date that the price applies to
     * @param product      the product to use
     * @param pricingGroup the pricing group. May be {@code null}
     */
    private void checkPrice(ProductPrice expected, BigDecimal price, String shortName, Date date, Product product,
                            Lookup pricingGroup) {
        assertEquals(expected, rules.getProductPrice(product, price, shortName, date, pricingGroup));
    }

    /**
     * Helper to create an <em>party.organisationPractice</em> with a 10% tax rate.
     *
     * @return the practice
     */
    private Party createPractice() {
        Party practice = TestHelper.getPractice();

        // add a 10% tax rate
        Lookup tax = (Lookup) create("lookup.taxType");
        IMObjectBean taxBean = new IMObjectBean(tax);
        taxBean.setValue("code", "XTAXTYPE" + Math.abs(new Random().nextInt()));
        taxBean.setValue("rate", new BigDecimal(10));
        taxBean.save();
        practice.addClassification(tax);
        save(practice);

        return practice;
    }
}
