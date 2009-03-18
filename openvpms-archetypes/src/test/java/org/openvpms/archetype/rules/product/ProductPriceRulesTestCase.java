/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.product;

import org.openvpms.archetype.rules.math.Currencies;
import org.openvpms.archetype.rules.math.Currency;
import static org.openvpms.archetype.rules.product.ProductArchetypes.FIXED_PRICE;
import static org.openvpms.archetype.rules.product.ProductArchetypes.UNIT_PRICE;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import static org.openvpms.archetype.test.TestHelper.getDate;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Random;
import java.util.Set;


/**
 * Tests the {@link ProductPriceRules} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ProductPriceRulesTestCase extends ArchetypeServiceTest {

    /**
     * The product.
     */
    private Product product;

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
    public void testGetProductPrice() {
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

        checkPrice(null, FIXED_PRICE, "2007-01-01");
        checkPrice(fixed1, FIXED_PRICE, "2008-01-01");
        checkPrice(fixed1, FIXED_PRICE, "2008-01-31");
        checkPrice(fixed2, FIXED_PRICE, "2008-02-01");
        checkPrice(fixed2, FIXED_PRICE, "2008-12-31");
        checkPrice(null, FIXED_PRICE, "2009-01-01");

        checkPrice(null, UNIT_PRICE, "2007-12-31");
        checkPrice(unit1, UNIT_PRICE, "2008-01-01");
        checkPrice(unit1, UNIT_PRICE, "2008-01-10");
        checkPrice(null, UNIT_PRICE, "2008-01-11");
        checkPrice(unit2, UNIT_PRICE, "2008-02-01");
        checkPrice(unit2, UNIT_PRICE, "2010-02-01"); // unbounded

        // verify that linked products are used if there are no matching prices
        // for the date
        Product priceTemplate = (Product) create(
                ProductArchetypes.PRICE_TEMPLATE);
        priceTemplate.addProductPrice(fixed3);
        priceTemplate.setName("XPriceTemplate");
        save(priceTemplate);

        EntityBean bean = new EntityBean(product);
        EntityRelationship relationship = bean.addRelationship(
                ProductArchetypes.PRODUCT_LINK_RELATIONSHIP, priceTemplate);
        relationship.setActiveStartTime(getDate("2008-01-01"));
        bean.save();

        product = get(product);
        checkPrice(fixed2, FIXED_PRICE, "2008-02-01");

        // fixed3 overrides fixed2 as it is the default
        checkPrice(fixed3, FIXED_PRICE, "2008-03-01");
    }

    /**
     * Tests the {@link ProductPriceRules#getProductPrice(Product, BigDecimal,
     * String, Date)}  method.
     */
    public void testGetProductPriceForPrice() {
        BigDecimal one = BigDecimal.ONE;
        BigDecimal two = new BigDecimal("2.0");
        BigDecimal three = new BigDecimal("3.0");

        ProductPrice fixed1 = createFixedPrice("2008-01-01", "2008-01-31", false);
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
        assertNull(rules.getProductPrice(product, one, FIXED_PRICE,
                                         new Date()));
        assertNull(rules.getProductPrice(product, one, UNIT_PRICE, new Date()));

        // add prices
        product.addProductPrice(fixed1);
        product.addProductPrice(fixed2);
        product.addProductPrice(unit1);
        product.addProductPrice(unit2);

        checkPrice(null, two, FIXED_PRICE, "2008-01-01");
        checkPrice(fixed1, one, FIXED_PRICE, "2008-01-01");
        checkPrice(null, two, FIXED_PRICE, "2008-01-31");
        checkPrice(fixed1, one, FIXED_PRICE, "2008-01-31");
        checkPrice(null, one, FIXED_PRICE, "2008-02-01");
        checkPrice(fixed2, two, FIXED_PRICE, "2008-12-31");
        checkPrice(null, two, FIXED_PRICE, "2009-01-01");

        checkPrice(null, one, UNIT_PRICE, "2007-12-31");
        checkPrice(null, two, UNIT_PRICE, "2008-01-01");
        checkPrice(unit1, one, UNIT_PRICE, "2008-01-01");
        checkPrice(null, two, UNIT_PRICE, "2008-01-10");
        checkPrice(unit1, one, UNIT_PRICE, "2008-01-10");
        checkPrice(null, two, UNIT_PRICE, "2008-01-11");
        checkPrice(null, three, UNIT_PRICE, "2008-02-01");
        checkPrice(unit2, two, UNIT_PRICE, "2008-02-01");
        checkPrice(null, three, UNIT_PRICE, "2010-02-01");
        checkPrice(unit2, two, UNIT_PRICE, "2010-02-01"); // unbounded

        // verify that linked products are used if there are no matching prices
        // for the date
        Product priceTemplate = (Product) create(
                ProductArchetypes.PRICE_TEMPLATE);
        priceTemplate.addProductPrice(fixed3);
        priceTemplate.setName("XPriceTemplate");
        save(priceTemplate);

        EntityBean bean = new EntityBean(product);
        EntityRelationship relationship = bean.addRelationship(
                ProductArchetypes.PRODUCT_LINK_RELATIONSHIP, priceTemplate);
        relationship.setActiveStartTime(getDate("2008-01-01"));
        bean.save();

        product = get(product);
        checkPrice(fixed2, two, FIXED_PRICE, "2008-02-01");
        checkPrice(fixed3, three, FIXED_PRICE, "2008-03-01");
        checkPrice(fixed2, two, FIXED_PRICE, "2008-03-01");
    }

    /**
     * Tests the {@link ProductPriceRules#getProductPrices} method.
     */
    public void testGetProductPrices() {
        ProductPrice fixed1 = createPrice(FIXED_PRICE, "2008-01-01", "2008-01-31");
        ProductPrice fixed2 = createPrice(FIXED_PRICE, "2008-01-01",
                                          "2008-12-31");
        ProductPrice fixed3 = createPrice(FIXED_PRICE, "2008-02-01", null);

        product.addProductPrice(fixed1);
        product.addProductPrice(fixed2);

        Product priceTemplate = (Product) create(
                ProductArchetypes.PRICE_TEMPLATE);
        priceTemplate.addProductPrice(fixed3);
        priceTemplate.setName("XPriceTemplate");
        save(priceTemplate);

        EntityBean bean = new EntityBean(product);
        EntityRelationship relationship = bean.addRelationship(
                ProductArchetypes.PRODUCT_LINK_RELATIONSHIP, priceTemplate);
        relationship.setActiveStartTime(getDate("2008-01-01"));
        bean.save();

        product = get(product);

        Set<ProductPrice> prices = rules.getProductPrices(product, FIXED_PRICE,
                                                          getDate("2007-01-01"));
        assertTrue(prices.isEmpty());

        prices = rules.getProductPrices(product, FIXED_PRICE,
                                        getDate("2008-01-01"));
        assertEquals(2, prices.size());
        assertTrue(prices.contains(fixed1));
        assertTrue(prices.contains(fixed2));
        assertFalse(prices.contains(fixed3));

        prices = rules.getProductPrices(product, FIXED_PRICE,
                                        getDate("2008-02-01"));
        assertEquals(2, prices.size());
        assertFalse(prices.contains(fixed1));
        assertTrue(prices.contains(fixed2));
        assertTrue(prices.contains(fixed3));

        prices = rules.getProductPrices(product, FIXED_PRICE,
                                        getDate("2009-01-01"));
        assertEquals(1, prices.size());
        assertFalse(prices.contains(fixed1));
        assertFalse(prices.contains(fixed2));
        assertTrue(prices.contains(fixed3));
    }

    /**
     * Tests the {@link ProductPriceRules#getPrice} method.
     */
    public void testGetPrice() {
        BigDecimal cost = BigDecimal.ONE;
        BigDecimal markup = BigDecimal.valueOf(100); // 100% markup
        BigDecimal price = rules.getPrice(product, cost, markup, practice,
                                          currency);
        assertEquals(new BigDecimal("2.20"), price);
    }

    /**
     * Tests the {@link ProductPriceRules#getMarkup} method.
     */
    public void testGetMarkup() {
        BigDecimal cost = BigDecimal.ONE;
        BigDecimal price = new BigDecimal("2.20");
        BigDecimal markup = rules.getMarkup(product, cost, price, practice);
        assertEquals(BigDecimal.valueOf(100), markup);
    }

    /**
     * Sets up the test case.
     * <p/>
     * This sets up the practice to have a 10% tax on all products.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        product = TestHelper.createProduct();
        practice = createPractice();
        rules = new ProductPriceRules();
        IMObjectBean bean = new IMObjectBean(practice);
        Currencies currencies = new Currencies();
        currency = currencies.getCurrency(bean.getString("currency"));
    }

    /**
     * Verfies a price matches that expected.
     *
     * @param expected  the expected price
     * @param shortName the price short name
     * @param date      the date that the price applies to
     */
    private void checkPrice(ProductPrice expected, String shortName,
                            String date) {
        assertEquals(expected,
                     rules.getProductPrice(product, shortName, getDate(date)));
    }

    /**
     * Verfies a price matches that expected.
     *
     * @param expected  the expected price
     * @param price     the price
     * @param shortName the price short name
     * @param date      the date that the price applies to
     */
    private void checkPrice(ProductPrice expected, BigDecimal price,
                            String shortName, String date) {
        assertEquals(expected,
                     rules.getProductPrice(product, price, shortName,
                                           getDate(date)));
    }

    /**
     * Helper to create a new fixed price.
     *
     * @param from         the active from date. May be <tt>null</tt>
     * @param to           the active to date. May be <tt>null</tt>
     * @param defaultPrice <tt>true</tt> if the price is the default
     * @return a new fixed price
     */
    private ProductPrice createFixedPrice(String from, String to,
                                          boolean defaultPrice) {
        ProductPrice result = createPrice(ProductArchetypes.FIXED_PRICE,
                                          from, to);
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
        ProductPrice result = (ProductPrice) create(shortName);
        result.setName("XPrice");
        Date fromDate = (from != null) ? getDate(from) : null;
        Date toDate = (to != null) ? getDate(to) : null;
        result.setPrice(BigDecimal.ONE);
        result.setFromDate(fromDate);
        result.setToDate(toDate);
        return result;
    }

    /**
     * Helper to create an <em>party.organisationPractice</em> with a 10%
     * tax rate.
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
