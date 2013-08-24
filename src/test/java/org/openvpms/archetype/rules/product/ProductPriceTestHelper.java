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

import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

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
     * @param from         the active from date. May be {@code null}
     * @param to           the active to date. May be {@code null}
     * @param defaultPrice {@code true} if the price is the default
     * @return a new fixed price
     */
    public static ProductPrice createFixedPrice(BigDecimal price, BigDecimal cost, BigDecimal markup, String from,
                                                String to, boolean defaultPrice) {
        ProductPrice result = createFixedPrice(from, to, defaultPrice);
        IMObjectBean bean = new IMObjectBean(result);
        bean.setValue("price", price);
        bean.setValue("cost", cost);
        bean.setValue("markup", markup);
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
     * @param price  the price
     * @param cost   the cost price
     * @param markup the markup
     * @param from   the active from date. May be {@code null}
     * @param to     the active to date. May be {@code null}
     * @return a new fixed price
     */
    public static ProductPrice createUnitPrice(BigDecimal price, BigDecimal cost, BigDecimal markup, String from,
                                               String to) {
        ProductPrice result = createUnitPrice(from, to);
        IMObjectBean bean = new IMObjectBean(result);
        bean.setValue("price", price);
        bean.setValue("cost", cost);
        bean.setValue("markup", markup);
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
     * @param from         the active from date. May be {@code null}
     * @param to           the active to date. May be {@code null}
     * @param defaultPrice {@code true} if the price is the default
     * @return a new fixed price
     */
    public static ProductPrice createFixedPrice(String price, String cost, String markup, String from, String to,
                                                boolean defaultPrice) {
        return createFixedPrice(new BigDecimal(price), new BigDecimal(cost), new BigDecimal(markup), from, to,
                                defaultPrice);
    }

    /**
     * Helper to create a new unit price.
     *
     * @param price  the price
     * @param cost   the cost price
     * @param markup the markup
     * @param from   the active from date. May be {@code null}
     * @param to     the active to date. May be {@code null}
     * @return a new fixed price
     */
    public static ProductPrice createUnitPrice(String price, String cost, String markup, String from, String to) {
        return createUnitPrice(new BigDecimal(price), new BigDecimal(cost), new BigDecimal(markup), from, to);
    }
}
