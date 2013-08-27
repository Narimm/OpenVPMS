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
import static org.openvpms.archetype.test.TestHelper.getDate;

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
        return createProduct(product, true);
    }

    /**
     * Creates a new {@link ProductData} from a {@link Product}.
     *
     * @param product    the product
     * @param copyPrices if {@code true} add the product's prices, else exclude them
     * @return the corresponding product data
     */
    protected ProductData createProduct(Product product, boolean copyPrices) {
        ProductPriceRules rules = new ProductPriceRules(ArchetypeServiceHelper.getArchetypeService(),
                                                        LookupServiceHelper.getLookupService());
        IMObjectBean bean = new IMObjectBean(product);
        ProductData result = new ProductData(product.getId(), product.getName(), bean.getString("printedName"), 1);
        result.setReference(product.getObjectReference());
        if (copyPrices) {
            for (ProductPrice price : rules.getProductPrices(product, ProductArchetypes.FIXED_PRICE)) {
                IMObjectBean priceBean = new IMObjectBean(price);
                result.addFixedPrice(price.getId(), price.getPrice(), priceBean.getBigDecimal("cost"),
                                     price.getFromDate(), price.getToDate(), 1);
            }
            for (ProductPrice price : rules.getProductPrices(product, ProductArchetypes.UNIT_PRICE)) {
                IMObjectBean priceBean = new IMObjectBean(price);
                result.addUnitPrice(price.getId(), price.getPrice(), priceBean.getBigDecimal("cost"),
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
        Date expectedFrom = expected.getFromDate();
        Date expectedTo = expected.getToDate();

        String shortName = expected.getArchetypeId().getShortName();
        checkPrice(product, shortName, expectedPrice, expectedCost, expectedMarkup, expectedFrom, expectedTo);
    }

    /**
     * Verifies that a product contains the expected fixed price.
     *
     * @param product        the product
     * @param expectedPrice  the expected price
     * @param expectedCost   the expected cost
     * @param expectedMarkup the expected markup
     * @param expectedFrom   the expected price start date
     * @param expectedTo     the expected price end date
     */
    protected void checkFixedPrice(Product product, BigDecimal expectedPrice, BigDecimal expectedCost,
                                   BigDecimal expectedMarkup, Date expectedFrom, Date expectedTo) {
        checkPrice(product, ProductArchetypes.FIXED_PRICE, expectedPrice, expectedCost, expectedMarkup, expectedFrom,
                   expectedTo);
    }

    /**
     * Verifies that a product contains the expected unit price.
     *
     * @param product        the product
     * @param expectedPrice  the expected price
     * @param expectedCost   the expected cost
     * @param expectedMarkup the expected markup
     * @param expectedFrom   the expected price start date
     * @param expectedTo     the expected price end date
     */
    protected void checkUnitPrice(Product product, BigDecimal expectedPrice, BigDecimal expectedCost,
                                  BigDecimal expectedMarkup, Date expectedFrom, Date expectedTo) {
        checkPrice(product, ProductArchetypes.UNIT_PRICE, expectedPrice, expectedCost, expectedMarkup, expectedFrom,
                   expectedTo);
    }

    /**
     * Verifies that a product contains the expected fixed price.
     *
     * @param product        the product
     * @param expectedPrice  the expected price
     * @param expectedCost   the expected cost
     * @param expectedMarkup the expected markup
     * @param expectedFrom   the expected price start date
     * @param expectedTo     the expected price end date
     */
    protected void checkFixedPrice(Product product, String expectedPrice, String expectedCost,
                                   String expectedMarkup, String expectedFrom, String expectedTo) {
        checkPrice(product, ProductArchetypes.FIXED_PRICE, expectedPrice, expectedCost, expectedMarkup, expectedFrom,
                   expectedTo);
    }

    /**
     * Verifies that a product contains the expected unit price.
     *
     * @param product        the product
     * @param expectedPrice  the expected price
     * @param expectedCost   the expected cost
     * @param expectedMarkup the expected markup
     * @param expectedFrom   the expected price start date
     * @param expectedTo     the expected price end date
     */
    protected void checkUnitPrice(Product product, String expectedPrice, String expectedCost,
                                  String expectedMarkup, String expectedFrom, String expectedTo) {
        checkPrice(product, ProductArchetypes.UNIT_PRICE, expectedPrice, expectedCost, expectedMarkup, expectedFrom,
                   expectedTo);
    }

    /**
     * Verifies that a product contains the expected price.
     *
     * @param product        the product
     * @param shortName      the price archetype short name
     * @param expectedPrice  the expected price
     * @param expectedCost   the expected cost
     * @param expectedMarkup the expected markup
     * @param expectedFrom   the expected price start date
     * @param expectedTo     the expected price end date
     */
    protected void checkPrice(Product product, String shortName, String expectedPrice, String expectedCost,
                              String expectedMarkup, String expectedFrom, String expectedTo) {
        checkPrice(product, shortName, new BigDecimal(expectedPrice), new BigDecimal(expectedCost),
                   new BigDecimal(expectedMarkup), getDate(expectedFrom), getDate(expectedTo));
    }

    /**
     * Verifies that a product contains the expected price.
     *
     * @param product        the product
     * @param shortName      the price archetype short name
     * @param expectedPrice  the expected price
     * @param expectedCost   the expected cost
     * @param expectedMarkup the expected markup
     * @param expectedFrom   the expected price start date
     * @param expectedTo     the expected price end date
     */
    protected void checkPrice(Product product, String shortName, BigDecimal expectedPrice, BigDecimal expectedCost,
                              BigDecimal expectedMarkup, Date expectedFrom, Date expectedTo) {
        boolean found = false;
        for (ProductPrice price : product.getProductPrices()) {
            IMObjectBean priceBean = new IMObjectBean(price);
            if (price.getArchetypeId().getShortName().equals(shortName)
                && price.getPrice().compareTo(expectedPrice) == 0
                && priceBean.getBigDecimal("cost").compareTo(expectedCost) == 0
                && priceBean.getBigDecimal("markup").compareTo(expectedMarkup) == 0
                && ObjectUtils.equals(expectedFrom, price.getFromDate())
                && ObjectUtils.equals(expectedTo, price.getToDate())) {
                found = true;
                break;
            }
        }
        assertTrue("Failed to find price", found);
    }

}
