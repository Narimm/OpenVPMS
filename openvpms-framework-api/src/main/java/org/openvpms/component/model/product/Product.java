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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.model.product;

import org.openvpms.component.model.entity.Entity;

import java.util.Set;

/**
 * Represents a product that may be bought or sold.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public interface Product extends Entity {

    /**
     * Returns the product prices.
     *
     * @return the product prices.
     */
    Set<ProductPrice> getProductPrices();

    /**
     * Adds a price.
     *
     * @param price the product price to add
     */
    void addProductPrice(ProductPrice price);

    /**
     * Remoces a price.
     *
     * @param price the product price to add
     */
    void removeProductPrice(ProductPrice price);

}
