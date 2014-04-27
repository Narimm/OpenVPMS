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

import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;


/**
 * Base class for product tests.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AbstractProductTest extends ArchetypeServiceTest {


    /**
     * Helper to add an <em>productPrice.unitPrice</em> to a product.
     *
     * @param product the product
     * @param price   the price
     * @return the unit price
     */
    protected ProductPrice addUnitPrice(Product product, BigDecimal price) {
        return addUnitPrice(product, BigDecimal.ZERO, price);
    }

    /**
     * Helper to add an <em>productPrice.unitPrice</em> to a product, and save
     * the product.
     *
     * @param product the product
     * @param cost    the cost
     * @param price   the price
     * @return the unit price
     */
    protected ProductPrice addUnitPrice(Product product, BigDecimal cost,
                                        BigDecimal price) {
        return addUnitPrice(product, cost, price, true);
    }

    /**
     * Helper to add an <em>productPrice.unitPrice</em> to a product.
     *
     * @param product the product
     * @param cost    the cost
     * @param price   the price
     * @param save    if <tt>true</tt> save the product
     * @return the unit price
     */
    protected ProductPrice addUnitPrice(Product product, BigDecimal cost,
                                        BigDecimal price, boolean save) {
        ProductPrice unitPrice
                = (ProductPrice) create("productPrice.unitPrice");
        IMObjectBean priceBean = new IMObjectBean(unitPrice);
        priceBean.setValue("cost", cost);
        priceBean.setValue("markup", BigDecimal.valueOf(100));
        priceBean.setValue("price", price);
        product.addProductPrice(unitPrice);
        if (save) {
            save(product);
        }
        return unitPrice;
    }

    /**
     * Verifies a price matches that expected.
     *
     * @param price         the price
     * @param expectedCost  the expected cost
     * @param expectedPrice the expected price
     */
    protected void checkPrice(ProductPrice price, BigDecimal expectedCost, BigDecimal expectedPrice) {
        IMObjectBean bean = new IMObjectBean(price);
        checkEquals(expectedCost, bean.getBigDecimal("cost"));
        checkEquals(expectedPrice, bean.getBigDecimal("price"));
    }
}
