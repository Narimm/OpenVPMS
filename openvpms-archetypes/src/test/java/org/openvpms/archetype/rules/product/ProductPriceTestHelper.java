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

import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.model.entity.EntityLink;
import org.openvpms.component.model.lookup.Lookup;

import java.math.BigDecimal;
import java.util.Date;

import static org.openvpms.archetype.test.TestHelper.getDate;

/**
 * Product price helper methods for testing purposes.
 *
 * @author Tim Anderson
 */
public class ProductPriceTestHelper {

    /**
     * Helper to create a new fixed price.
     *
     * @param from         the active from date. May be {@code null}
     * @param to           the active to date. May be {@code null}
     * @param defaultPrice {@code true} if the price is the default
     * @param pricingGroup the pricing group
     * @return a new fixed price
     */
    public static ProductPrice createFixedPrice(Date from, Date to, boolean defaultPrice, Lookup pricingGroup) {
        ProductPrice result = createFixedPrice(from, to, defaultPrice);
        result.setName("Fixed Price - " + pricingGroup.getName());
        result.addClassification(pricingGroup);
        return result;
    }

    /**
     * Helper to create a new fixed price.
     *
     * @param from         the active from date. May be {@code null}
     * @param to           the active to date. May be {@code null}
     * @param defaultPrice {@code true} if the price is the default
     * @return a new fixed price
     */
    public static ProductPrice createFixedPrice(Date from, Date to, boolean defaultPrice) {
        ProductPrice result = createPrice(ProductArchetypes.FIXED_PRICE, from, to);
        IMObjectBean bean = new IMObjectBean(result);
        bean.setValue("default", defaultPrice);
        return result;
    }

    /**
     * Helper to create a new fixed price.
     *
     * @param price        the price
     * @param cost         the cost price
     * @param markup       the markup
     * @param maxDiscount  the maximum discount
     * @param from         the active from date. May be {@code null}
     * @param to           the active to date. May be {@code null}
     * @param defaultPrice {@code true} if the price is the default
     * @return a new fixed price
     */
    public static ProductPrice createFixedPrice(BigDecimal price, BigDecimal cost, BigDecimal markup,
                                                BigDecimal maxDiscount, String from, String to, boolean defaultPrice) {
        ProductPrice result = createFixedPrice(from, to, defaultPrice);
        IMObjectBean bean = new IMObjectBean(result);
        bean.setValue("price", price);
        bean.setValue("cost", cost);
        bean.setValue("markup", markup);
        bean.setValue("maxDiscount", maxDiscount);
        return result;
    }

    /**
     * Helper to create a new fixed price.
     *
     * @param price        the price
     * @param cost         the cost price
     * @param markup       the markup
     * @param maxDiscount  the maximum discount
     * @param from         the active from date. May be {@code null}
     * @param to           the active to date. May be {@code null}
     * @param defaultPrice {@code true} if the price is the default
     * @return a new fixed price
     */
    public static ProductPrice createFixedPrice(BigDecimal price, BigDecimal cost, BigDecimal markup,
                                                BigDecimal maxDiscount, Date from, Date to, boolean defaultPrice) {
        ProductPrice result = createFixedPrice(from, to, defaultPrice);
        IMObjectBean bean = new IMObjectBean(result);
        bean.setValue("price", price);
        bean.setValue("cost", cost);
        bean.setValue("markup", markup);
        bean.setValue("maxDiscount", maxDiscount);
        return result;
    }

    /**
     * Helper to create a new unit price.
     *
     * @param from the active from date. May be {@code null}
     * @param to   the active to date. May be {@code null}
     * @return a new fixed price
     */
    public static ProductPrice createUnitPrice(String from, String to) {
        return createPrice(ProductArchetypes.UNIT_PRICE, from, to);
    }

    /**
     * Helper to create a new unit price.
     *
     * @param from the active from date. May be {@code null}
     * @param to   the active to date. May be {@code null}
     * @return a new fixed price
     */
    public static ProductPrice createUnitPrice(Date from, Date to) {
        return createPrice(ProductArchetypes.UNIT_PRICE, from, to);
    }

    /**
     * Helper to create a new unit price.
     *
     * @param from         the active from date. May be {@code null}
     * @param to           the active to date. May be {@code null}
     * @param pricingGroup the pricing group
     * @return a new fixed price
     */
    public static ProductPrice createUnitPrice(String from, String to, Lookup pricingGroup) {
        ProductPrice price = createPrice(ProductArchetypes.UNIT_PRICE, from, to);
        price.setName("Unit Price - " + pricingGroup.getName());
        price.addClassification(pricingGroup);
        return price;
    }

    /**
     * Helper to create a new unit price.
     *
     * @param price       the price
     * @param cost        the cost price
     * @param markup      the markup
     * @param maxDiscount the maximum discount
     * @param from        the active from date. May be {@code null}
     * @param to          the active to date. May be {@code null}
     * @return a new fixed price
     */
    public static ProductPrice createUnitPrice(BigDecimal price, BigDecimal cost, BigDecimal markup,
                                               BigDecimal maxDiscount, String from, String to) {
        return createUnitPrice(price, cost, markup, maxDiscount, getDate(from), getDate(to));
    }

    /**
     * Helper to create a new unit price.
     *
     * @param price       the price
     * @param cost        the cost price
     * @param markup      the markup
     * @param maxDiscount the maximum discount
     * @param from        the active from date. May be {@code null}
     * @param to          the active to date. May be {@code null}   @return a new fixed price
     */
    public static ProductPrice createUnitPrice(BigDecimal price, BigDecimal cost, BigDecimal markup,
                                               BigDecimal maxDiscount, Date from, Date to) {
        ProductPrice result = createUnitPrice(from, to);
        IMObjectBean bean = new IMObjectBean(result);
        bean.setValue("price", price);
        bean.setValue("cost", cost);
        bean.setValue("markup", markup);
        bean.setValue("maxDiscount", maxDiscount);
        return result;
    }

    /**
     * Helper to create a new fixed price.
     *
     * @param from         the active from date. May be {@code null}
     * @param to           the active to date. May be {@code null}
     * @param defaultPrice {@code true} if the price is the default
     * @param pricingGroup the pricing group
     * @return a new fixed price
     */
    public static ProductPrice createFixedPrice(String from, String to, boolean defaultPrice, Lookup pricingGroup) {
        ProductPrice fixedPrice = createFixedPrice(from, to, defaultPrice);
        fixedPrice.addClassification(pricingGroup);
        return fixedPrice;
    }

    /**
     * Helper to create a new fixed price.
     *
     * @param from         the active from date. May be {@code null}
     * @param to           the active to date. May be {@code null}
     * @param defaultPrice {@code true} if the price is the default
     * @return a new fixed price
     */
    public static ProductPrice createFixedPrice(String from, String to, boolean defaultPrice) {
        Date fromDate = (from != null) ? getDate(from) : null;
        Date toDate = (to != null) ? getDate(to) : null;
        return createFixedPrice(fromDate, toDate, defaultPrice);
    }

    /**
     * Helper to create a new price.
     *
     * @param shortName the short name
     * @param from      the active from date. May be {@code null}
     * @param to        the active to date. May be {@code null}
     * @return a new price
     */
    public static ProductPrice createPrice(String shortName, Date from, Date to) {
        ProductPrice result = (ProductPrice) TestHelper.create(shortName);
        result.setName("XPrice");
        result.setPrice(BigDecimal.ONE);
        result.setFromDate(from);
        result.setToDate(to);
        return result;
    }

    /**
     * Helper to create a new price.
     *
     * @param shortName the short name
     * @param from      the active from date. May be {@code null}
     * @param to        the active to date. May be {@code null}
     * @return a new price
     */
    public static ProductPrice createPrice(String shortName, String from, String to) {
        Date fromDate = (from != null) ? getDate(from) : null;
        Date toDate = (to != null) ? getDate(to) : null;
        return createPrice(shortName, fromDate, toDate);
    }

    /**
     * Helper to create a new fixed price.
     *
     * @param price        the price
     * @param cost         the cost price
     * @param markup       the markup
     * @param maxDiscount  the maximum discount
     * @param from         the active from date. May be {@code null}
     * @param to           the active to date. May be {@code null}
     * @param defaultPrice {@code true} if the price is the default
     * @return a new fixed price
     */
    public static ProductPrice createFixedPrice(String price, String cost, String markup, String maxDiscount,
                                                String from, String to, boolean defaultPrice) {
        return createFixedPrice(new BigDecimal(price), new BigDecimal(cost), new BigDecimal(markup),
                                new BigDecimal(maxDiscount), from, to, defaultPrice);
    }

    /**
     * Helper to create a new fixed price.
     *
     * @param price        the price
     * @param cost         the cost price
     * @param markup       the markup
     * @param maxDiscount  the maximum discount
     * @param from         the active from date. May be {@code null}
     * @param to           the active to date. May be {@code null}
     * @param defaultPrice {@code true} if the price is the default
     * @return a new fixed price
     */
    public static ProductPrice createFixedPrice(String price, String cost, String markup, String maxDiscount,
                                                Date from, Date to, boolean defaultPrice) {
        return createFixedPrice(new BigDecimal(price), new BigDecimal(cost), new BigDecimal(markup),
                                new BigDecimal(maxDiscount), from, to, defaultPrice);
    }

    /**
     * Helper to create a new unit price.
     *
     * @param price       the price
     * @param cost        the cost price
     * @param markup      the markup
     * @param maxDiscount the maximum discount
     * @param from        the active from date. May be {@code null}
     * @param to          the active to date. May be {@code null}
     * @return a new unit price
     */
    public static ProductPrice createUnitPrice(String price, String cost, String markup, String maxDiscount,
                                               String from, String to) {
        return createUnitPrice(new BigDecimal(price), new BigDecimal(cost), new BigDecimal(markup),
                               new BigDecimal(maxDiscount), from, to);
    }

    /**
     * Helper to create a new unit price.
     *
     * @param price       the price
     * @param cost        the cost price
     * @param markup      the markup
     * @param maxDiscount the maximum discount
     * @param from        the active from date. May be {@code null}
     * @param to          the active to date. May be {@code null}
     * @return a new unit price
     */
    public static ProductPrice createUnitPrice(String price, String cost, String markup, String maxDiscount,
                                               Date from, Date to) {
        return createUnitPrice(new BigDecimal(price), new BigDecimal(cost), new BigDecimal(markup),
                               new BigDecimal(maxDiscount), from, to);
    }

    /**
     * Helper to create a price template with the specified prices.
     *
     * @param prices the prices
     * @return a new price template
     */
    public static Product createPriceTemplate(ProductPrice... prices) {
        Product product = (Product) TestHelper.create(ProductArchetypes.PRICE_TEMPLATE);
        product.setName("XProduct-" + System.currentTimeMillis());
        for (ProductPrice price : prices) {
            product.addProductPrice(price);
        }
        return product;
    }

    /**
     * Helper to add a price template relationship to a product.
     * <p/>
     * Both product and price template will be saved.
     *
     * @param product       the product
     * @param priceTemplate the price template
     * @param from          the from date. May be {@code null}
     * @param to            the to date. May be {@code null}
     */
    public static void addPriceTemplate(Product product, Product priceTemplate, String from, String to) {
        IMObjectBean bean = new IMObjectBean(product);
        EntityLink relationship = (EntityLink) bean.setTarget("linked", priceTemplate);
        relationship.setActiveStartTime(getDate(from));
        relationship.setActiveEndTime(getDate(to));
        bean.save();
    }
}
