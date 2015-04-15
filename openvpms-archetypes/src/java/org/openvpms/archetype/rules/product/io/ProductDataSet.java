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

import java.util.List;

/**
 * Valid and erroneous product data.
 *
 * @author Tim Anderson
 */
public class ProductDataSet {

    /**
     * The product data.
     */
    private final List<ProductData> data;

    /**
     * The erroneous data.
     */
    private final List<ProductData> errors;

    /**
     * Constructs a {@link ProductDataSet}.
     *
     * @param data   the filtered data
     * @param errors the erroneous data
     */
    public ProductDataSet(List<ProductData> data, List<ProductData> errors) {
        this.data = data;
        this.errors = errors;
    }

    /**
     * Returns the product data.
     *
     * @return the product data
     */
    public List<ProductData> getData() {
        return data;
    }

    /**
     * Returns the product data that contains errors.
     *
     * @return the erroneous product data
     */
    public List<ProductData> getErrors() {
        return errors;
    }
}
