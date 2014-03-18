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

package org.openvpms.archetype.rules.product.io;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.Assert.assertTrue;

/**
 * Product I/O helper methods.
 *
 * @author Tim Anderson
 */
public class AbstractProductIOTest extends ArchetypeServiceTest {

    /**
     * Creates and saves a product with the specified name, printed name and prices.
     *
     * @param name        the product name
     * @param printedName the product printed name. May be {@code null}
     * @return a new product
     */
    protected Product createProduct(String name, String printedName, ProductPrice... prices) {
        Product result = TestHelper.createProduct();
        IMObjectBean bean = new IMObjectBean(result);
        bean.setValue("name", name);
        bean.setValue("printedName", printedName);
        for (ProductPrice price : prices) {
            result.addProductPrice(price);
        }
        bean.save();
        return result;
    }

    /**
     * Creates a new {@link ProductData} from a {@link Product}.
     *
     * @param product the product
     * @return the corresponding product data
     */
    protected ProductData createProduct(Product product) {
        return createProduct(product, BigDecimal.ZERO, true);
    }

    /**
     * Creates a new {@link ProductData} from a {@link Product}.
     *
     * @param product    the product
     * @param taxRate    the product tax rate, expressed as a percentage
     * @param copyPrices if {@code true} add the product's prices, else exclude them
     * @return the corresponding product data
     */
    protected ProductData createProduct(Product product, BigDecimal taxRate, boolean copyPrices) {
        ProductPriceRules rules = new ProductPriceRules(ArchetypeServiceHelper.getArchetypeService(),
                                                        LookupServiceHelper.getLookupService());
        IMObjectBean bean = new IMObjectBean(product);
        ProductData result = new ProductData(product.getId(), product.getName(), bean.getString("printedName"), taxRate,
                                             1);
        result.setReference(product.getObjectReference());
        if (copyPrices) {
            for (ProductPrice price : rules.getProductPrices(product, ProductArchetypes.FIXED_PRICE)) {
                IMObjectBean priceBean = new IMObjectBean(price);
                result.addFixedPrice(price.getId(), price.getPrice(), priceBean.getBigDecimal("cost"),
                                     priceBean.getBigDecimal("maxDiscount"), price.getFromDate(), price.getToDate(),
                                     priceBean.getBoolean("default"), 1);
            }
            for (ProductPrice price : rules.getProductPrices(product, ProductArchetypes.UNIT_PRICE)) {
                IMObjectBean priceBean = new IMObjectBean(price);
                result.addUnitPrice(price.getId(), price.getPrice(), priceBean.getBigDecimal("cost"),
                                    priceBean.getBigDecimal("maxDiscount"),
                                    price.getFromDate(), price.getToDate(), 1);
            }
        }
        return result;
    }

    /**
     * Verifies that a product contains the expected price.
     *
     * @param product  the product
     * @param expected the expected price
     */
    protected void checkPrice(Product product, ProductPrice expected) {
        IMObjectBean bean = new IMObjectBean(expected);
        BigDecimal expectedPrice = expected.getPrice();
        BigDecimal expectedCost = bean.getBigDecimal("cost");
        BigDecimal expectedMarkup = bean.getBigDecimal("markup");
        BigDecimal expectedMaxDiscount = bean.getBigDecimal("maxDiscount");
        Date expectedFrom = expected.getFromDate();
        Date expectedTo = expected.getToDate();
        boolean isDefault = bean.hasNode("default") && bean.getBoolean("default");

        String shortName = expected.getArchetypeId().getShortName();
        checkPrice(product, shortName, expectedPrice, expectedCost, expectedMarkup, expectedMaxDiscount, expectedFrom,
                   expectedTo, isDefault);
    }

    /**
     * Verifies that a product contains the expected fixed price.
     *
     * @param product             the product
     * @param expectedPrice       the expected price
     * @param expectedCost        the expected cost
     * @param expectedMarkup      the expected markup
     * @param expectedMaxDiscount the expected max discount
     * @param expectedFrom        the expected price start date
     * @param expectedTo          the expected price end date
     * @param expectedDefault     the expected default
     */
    protected void checkFixedPrice(Product product, BigDecimal expectedPrice, BigDecimal expectedCost,
                                   BigDecimal expectedMarkup, BigDecimal expectedMaxDiscount, Date expectedFrom,
                                   Date expectedTo, boolean expectedDefault) {
        checkPrice(product, ProductArchetypes.FIXED_PRICE, expectedPrice, expectedCost, expectedMarkup,
                   expectedMaxDiscount, expectedFrom, expectedTo, expectedDefault);
    }

    /**
     * Verifies that a product contains the expected unit price.
     *
     * @param product             the product
     * @param expectedPrice       the expected price
     * @param expectedCost        the expected cost
     * @param expectedMarkup      the expected markup
     * @param expectedMaxDiscount the expected max discount
     * @param expectedFrom        the expected price start date
     * @param expectedTo          the expected price end date
     */
    protected void checkUnitPrice(Product product, BigDecimal expectedPrice, BigDecimal expectedCost,
                                  BigDecimal expectedMarkup, BigDecimal expectedMaxDiscount, Date expectedFrom,
                                  Date expectedTo) {
        checkPrice(product, ProductArchetypes.UNIT_PRICE, expectedPrice, expectedCost, expectedMarkup,
                   expectedMaxDiscount, expectedFrom, expectedTo, false);
    }

    /**
     * Verifies that a product contains the expected price.
     *
     * @param product             the product
     * @param shortName           the price archetype short name
     * @param expectedPrice       the expected price
     * @param expectedCost        the expected cost
     * @param expectedMarkup      the expected markup
     * @param expectedMaxDiscount the expected maximum discount
     * @param expectedFrom        the expected price start date
     * @param expectedTo          the expected price end date
     * @param expectedDefault     the expected default. Only applies to fixed prices
     */
    protected void checkPrice(Product product, String shortName, BigDecimal expectedPrice, BigDecimal expectedCost,
                              BigDecimal expectedMarkup, BigDecimal expectedMaxDiscount, Date expectedFrom,
                              Date expectedTo, boolean expectedDefault) {
        boolean found = false;
        for (ProductPrice price : product.getProductPrices()) {
            IMObjectBean priceBean = new IMObjectBean(price);
            if (price.getArchetypeId().getShortName().equals(shortName)
                && price.getPrice().compareTo(expectedPrice) == 0
                && priceBean.getBigDecimal("cost").compareTo(expectedCost) == 0
                && priceBean.getBigDecimal("markup").compareTo(expectedMarkup) == 0
                && priceBean.getBigDecimal("maxDiscount").compareTo(expectedMaxDiscount) == 0
                && ObjectUtils.equals(expectedFrom, price.getFromDate())
                && ObjectUtils.equals(expectedTo, price.getToDate())) {

                found = !priceBean.hasNode("default") || priceBean.getBoolean("default") == expectedDefault;
                break;
            }
        }
        assertTrue("Failed to find price", found);
    }

}
