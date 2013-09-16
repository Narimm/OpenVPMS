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

import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.util.List;

import static org.openvpms.archetype.rules.product.io.ProductIOException.ErrorCode.PriceNotFound;

/**
 * Product import helper methods.
 *
 * @author Tim Anderson
 */
class ProductImportHelper {

    /**
     * Returns the price with an identifier matching that in the data.
     *
     * @param data   the price data
     * @param prices the available prices
     * @return the corresponding price
     * @throws ProductIOException if the price is not found
     */
    public static ProductPrice getPrice(PriceData data, List<ProductPrice> prices) {
        long id = data.getId();
        for (ProductPrice price : prices) {
            if (price.getId() == id) {
                return price;
            }
        }
        throw new ProductIOException(PriceNotFound, data.getLine(), id);
    }

    /**
     * Determines if a price is the default price.
     *
     * @param bean the price bean
     * @return {@code true} if the price is the default, otherwise {@code false}
     */
    public static boolean isDefault(IMObjectBean bean) {
        return bean.hasNode("default") && bean.getBoolean("default");
    }
}
