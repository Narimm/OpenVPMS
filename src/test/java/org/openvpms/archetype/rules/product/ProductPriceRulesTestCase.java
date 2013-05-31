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

package org.openvpms.archetype.rules.product;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.math.Currencies;
import org.openvpms.archetype.rules.math.Currency;
import org.openvpms.archetype.test.ArchetypeServiceTest;
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
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.rules.product.ProductArchetypes.FIXED_PRICE;
import static org.openvpms.archetype.rules.product.ProductArchetypes.UNIT_PRICE;
import static org.openvpms.archetype.test.TestHelper.getDate;
import static org.openvpms.archetype.test.TestHelper.getDatetime;


/**
 * Tests the {@link ProductPriceRules} class.
 *
 * @author Tim Anderson
 */
public class ProductPriceRulesTestCase extends ArchetypeServiceTest {

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
     * Tests the {@link ProductPriceRules#getProductPrice} method.
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
     * Tests the {@link ProductPriceRules#getProductPrice(Product, BigDecimal, String, Date)}  method.
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
     * Tests the {@link ProductPriceRules#getProductPrice(Product, BigDecimal, String, Date)}  method, for
     * for products that support <em>product.priceTemplate</em>.
     */
    @Test
    public void testGetProductPriceForPriceForProductWithPriceTemplate() {
        checkGetProductPriceForPrice(createMedication(), true);
        checkGetProductPriceForPrice(createMerchandise(), true);
        checkGetProductPriceForPrice(createService(), true);
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
        ProductPrice price = createPrice(FIXED_PRICE, new Date(), new Date());
        checkEquals(new BigDecimal(100), rules.getMaxDiscount(price));

        IMObjectBean bean = new IMObjectBean(price);
        bean.setValue("maxDiscount", 10);
        checkEquals(BigDecimal.TEN, rules.getMaxDiscount(price));
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
     * Tests the {@link ProductPriceRules#getProductPrices} method.
     *
     * @param product          the product to use
     * @param usePriceTemplate if <tt>true</tt> attach an <em>product.priceTemplate</em> to the product
     */
    private void checkProductPrice(Product product, boolean usePriceTemplate) {
        ProductPrice fixed1 = createFixedPrice("2008-01-01", "2008-01-31", false);
        ProductPrice fixed2 = createFixedPrice("2008-02-01", "2008-12-31", false);
        ProductPrice fixed3 = createFixedPrice("2008-03-01", null, true);

        ProductPrice unit1 = createPrice(UNIT_PRICE, "2008-01-01", "2008-01-10");
        ProductPrice unit2 = createPrice(UNIT_PRICE, "2008-02-01", null);

        assertNull(rules.getProductPrice(product, FIXED_PRICE, new Date()));
        assertNull(rules.getProductPrice(product, UNIT_PRICE, new Date()));

        product.addProductPrice(fixed1);
        product.addProductPrice(fixed2);
        product.addProductPrice(unit1);
        product.addProductPrice(unit2);

        checkPrice(null, FIXED_PRICE, "2007-01-01", product);
        checkPrice(fixed1, FIXED_PRICE, "2008-01-01", product);
        checkPrice(fixed1, FIXED_PRICE, "2008-01-31", product);
        checkPrice(fixed2, FIXED_PRICE, "2008-02-01", product);
        checkPrice(fixed2, FIXED_PRICE, "2008-12-31", product);
        checkPrice(null, FIXED_PRICE, "2009-01-01", product);

        checkPrice(null, UNIT_PRICE, "2007-12-31", product);
        checkPrice(unit1, UNIT_PRICE, "2008-01-01", product);
        checkPrice(unit1, UNIT_PRICE, "2008-01-10", product);
        checkPrice(null, UNIT_PRICE, "2008-01-11", product);
        checkPrice(unit2, UNIT_PRICE, "2008-02-01", product);
        checkPrice(unit2, UNIT_PRICE, "2010-02-01", product); // unbounded

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

            checkPrice(fixed2, FIXED_PRICE, "2008-02-01", product);

            // fixed3 overrides fixed2 as it is the default
            checkPrice(fixed3, FIXED_PRICE, "2008-03-01", product);
        }
    }

    /**
     * Tests the {@link ProductPriceRules#getProductPrice(Product, BigDecimal, String, Date)}  method.
     *
     * @param product          the product to test
     * @param usePriceTemplate if <tt>true</tt> attach an <em>product.priceTemplate</em> to the product
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

        ProductPrice unit1 = createPrice(UNIT_PRICE, "2008-01-01", "2008-01-10");
        ProductPrice unit2 = createPrice(UNIT_PRICE, "2008-02-01", null);

        unit1.setPrice(one);
        unit2.setPrice(two);

        // should be no prices returned until one is registered
        assertNull(rules.getProductPrice(product, one, FIXED_PRICE, new Date()));
        assertNull(rules.getProductPrice(product, one, UNIT_PRICE, new Date()));

        // add prices
        product.addProductPrice(fixed1);
        product.addProductPrice(fixed2);
        product.addProductPrice(unit1);
        product.addProductPrice(unit2);

        checkPrice(null, two, FIXED_PRICE, "2008-01-01", product);
        checkPrice(fixed1, one, FIXED_PRICE, "2008-01-01", product);
        checkPrice(null, two, FIXED_PRICE, "2008-01-31", product);
        checkPrice(fixed1, one, FIXED_PRICE, "2008-01-31", product);
        checkPrice(null, one, FIXED_PRICE, "2008-02-01", product);
        checkPrice(fixed2, two, FIXED_PRICE, getDatetime("2008-12-31 23:45:00"), product); // verify time is ignored
        checkPrice(null, two, FIXED_PRICE, "2009-01-01", product);

        checkPrice(null, one, UNIT_PRICE, "2007-12-31", product);
        checkPrice(null, two, UNIT_PRICE, "2008-01-01", product);
        checkPrice(unit1, one, UNIT_PRICE, "2008-01-01", product);
        checkPrice(null, two, UNIT_PRICE, "2008-01-10", product);
        checkPrice(unit1, one, UNIT_PRICE, "2008-01-10", product);
        checkPrice(null, two, UNIT_PRICE, "2008-01-11", product);
        checkPrice(null, three, UNIT_PRICE, "2008-02-01", product);
        checkPrice(unit2, two, UNIT_PRICE, "2008-02-01", product);
        checkPrice(null, three, UNIT_PRICE, "2010-02-01", product);
        checkPrice(unit2, two, UNIT_PRICE, "2010-02-01", product); // unbounded

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

            checkPrice(fixed2, two, FIXED_PRICE, "2008-02-01", product);
            checkPrice(fixed3, three, FIXED_PRICE, "2008-03-01", product);
            checkPrice(fixed2, two, FIXED_PRICE, "2008-03-01", product);
        }
    }

    /**
     * Tests the {@link ProductPriceRules#getProductPrices} method.
     *
     * @param product the product
     */
    private void checkGetProductPrices(Product product) {
        ProductPrice fixed1 = createPrice(FIXED_PRICE, "2008-01-01", "2008-01-31");
        ProductPrice fixed2 = createPrice(FIXED_PRICE, "2008-01-01", "2008-12-31");

        product.addProductPrice(fixed1);
        product.addProductPrice(fixed2);
        save(product);

        product = get(product);

        Set<ProductPrice> prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2007-01-01"));
        assertTrue(prices.isEmpty());

        prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2008-01-01"));
        assertEquals(2, prices.size());
        assertTrue(prices.contains(fixed1));
        assertTrue(prices.contains(fixed2));

        prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2008-02-01"));
        assertEquals(1, prices.size());
        assertFalse(prices.contains(fixed1));
        assertTrue(prices.contains(fixed2));

        prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2009-01-01"));
        assertEquals(0, prices.size());
    }

    /**
     * Checks the {@link ProductPriceRules#getProductPrices(Product, String, Date)} method for products that may
     * be linked to a price template.
     *
     * @param product the product. Either a medication, merchandise or service
     */
    private void checkGetProductPricesForProductWithPriceTemplate(Product product) {
        ProductPrice fixed1 = createPrice(FIXED_PRICE, "2008-01-01", "2008-01-31");
        ProductPrice fixed2 = createPrice(FIXED_PRICE, "2008-01-01", "2008-12-31");
        ProductPrice fixed3 = createPrice(FIXED_PRICE, "2008-02-01", null);

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

        Set<ProductPrice> prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2007-01-01"));
        assertTrue(prices.isEmpty());

        prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2008-01-01"));
        assertEquals(2, prices.size());
        assertTrue(prices.contains(fixed1));
        assertTrue(prices.contains(fixed2));
        assertFalse(prices.contains(fixed3));

        prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2008-02-01"));
        assertEquals(2, prices.size());
        assertFalse(prices.contains(fixed1));
        assertTrue(prices.contains(fixed2));
        assertTrue(prices.contains(fixed3));

        prices = rules.getProductPrices(product, FIXED_PRICE, getDate("2009-01-01"));
        assertEquals(1, prices.size());
        assertFalse(prices.contains(fixed1));
        assertFalse(prices.contains(fixed2));
        assertTrue(prices.contains(fixed3));
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
     * Helper to create a new price template product.
     *
     * @return a new price template product
     */
    private Product createPriceTemplate() {
        return createProduct(ProductArchetypes.PRICE_TEMPLATE);
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
     * @param expected  the expected price
     * @param shortName the price short name
     * @param date      the date that the price applies to
     * @param product   the product to use
     */
    private void checkPrice(ProductPrice expected, String shortName, String date, Product product) {
        checkPrice(expected, shortName, getDate(date), product);
    }

    /**
     * Verifies a price matches that expected.
     *
     * @param expected  the expected price
     * @param shortName the price short name
     * @param date      the date that the price applies to
     * @param product   the product to use
     */
    private void checkPrice(ProductPrice expected, String shortName, Date date, Product product) {
        assertEquals(expected, rules.getProductPrice(product, shortName, date));
    }

    /**
     * Verifies a price matches that expected.
     *
     * @param expected  the expected price
     * @param price     the price
     * @param shortName the price short name
     * @param date      the date that the price applies to
     * @param product   the product to use
     */
    private void checkPrice(ProductPrice expected, BigDecimal price, String shortName, String date, Product product) {
        checkPrice(expected, price, shortName, getDate(date), product);
    }

    /**
     * Verifies a price matches that expected.
     *
     * @param expected  the expected price
     * @param price     the price
     * @param shortName the price short name
     * @param date      the date that the price applies to
     * @param product   the product to use
     */
    private void checkPrice(ProductPrice expected, BigDecimal price, String shortName, Date date, Product product) {
        assertEquals(expected, rules.getProductPrice(product, price, shortName, date));
    }

    /**
     * Helper to create a new fixed price.
     *
     * @param from         the active from date. May be <tt>null</tt>
     * @param to           the active to date. May be <tt>null</tt>
     * @param defaultPrice <tt>true</tt> if the price is the default
     * @return a new fixed price
     */
    private ProductPrice createFixedPrice(String from, String to, boolean defaultPrice) {
        Date fromDate = (from != null) ? getDate(from) : null;
        Date toDate = (to != null) ? getDate(to) : null;
        return createFixedPrice(fromDate, toDate, defaultPrice);
    }

    /**
     * Helper to create a new fixed price.
     *
     * @param from         the active from date. May be <tt>null</tt>
     * @param to           the active to date. May be <tt>null</tt>
     * @param defaultPrice <tt>true</tt> if the price is the default
     * @return a new fixed price
     */
    private ProductPrice createFixedPrice(Date from, Date to, boolean defaultPrice) {
        ProductPrice result = createPrice(ProductArchetypes.FIXED_PRICE, from, to);
        IMObjectBean bean = new IMObjectBean(result);
        bean.setValue("default", defaultPrice);
        return result;
    }

    /**
     * Helper to create a new price.
     *
     * @param shortName the short name
     * @param from      the active from date. May be <tt>null</tt>
     * @param to        the active to date. May be <tt>null</tt>
     * @return a new price
     */
    private ProductPrice createPrice(String shortName, String from, String to) {
        Date fromDate = (from != null) ? getDate(from) : null;
        Date toDate = (to != null) ? getDate(to) : null;
        return createPrice(shortName, fromDate, toDate);
    }

    /**
     * Helper to create a new price.
     *
     * @param shortName the short name
     * @param from      the active from date. May be <tt>null</tt>
     * @param to        the active to date. May be <tt>null</tt>
     * @return a new price
     */
    private ProductPrice createPrice(String shortName, Date from, Date to) {
        ProductPrice result = (ProductPrice) create(shortName);
        result.setName("XPrice");
        result.setPrice(BigDecimal.ONE);
        result.setFromDate(from);
        result.setToDate(to);
        return result;
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
