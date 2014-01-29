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

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;

import java.util.ArrayList;
import java.util.List;

import static org.openvpms.archetype.rules.product.io.ProductIOException.ErrorCode.InvalidName;
import static org.openvpms.archetype.rules.product.io.ProductIOException.ErrorCode.NotFound;


/**
 * Filters {@link ProductData} to exclude unchanged and erroneous data.
 *
 * @author Tim Anderson
 */
public class ProductDataFilter {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The product comparer, used to determine changes.
     */
    private final ProductDataComparator comparer;

    /**
     * Constructs an {@link ProductDataFilter}.
     *
     * @param service the archetype service
     * @param rules   the price rules
     */
    public ProductDataFilter(ProductPriceRules rules, IArchetypeService service) {
        comparer = new ProductDataComparator(rules, service);
        this.service = service;
    }

    /**
     * Filters data.
     * <p/>
     * This excludes any data that has not changed.
     * Note that this modifies the input data.
     *
     * @param input the data to filter
     * @return the filtered data
     */
    public ProductDataSet filter(List<ProductData> input) {
        List<ProductData> output = new ArrayList<ProductData>();
        List<ProductData> errors = new ArrayList<ProductData>();

        for (ProductData data : input) {
            ArchetypeQuery query = new ArchetypeQuery("product.*");
            query.add(Constraints.eq("id", data.getId()));
            IMObjectQueryIterator<Product> iterator = new IMObjectQueryIterator<Product>(service, query);
            if (iterator.hasNext()) {
                Product product = iterator.next();
                if (!StringUtils.equalsIgnoreCase(product.getName(), data.getName())) {
                    addError(errors, data, new ProductIOException(InvalidName, data.getLine(), product.getName()));
                } else {
                    try {
                        ProductData modified = comparer.compare(product, data);
                        if (modified != null) {
                            output.add(modified);
                        }
                    } catch (ProductIOException exception) {
                        addError(errors, data, exception);
                    }
                }
            } else {
                addError(errors, data, new ProductIOException(NotFound, data.getLine()));
            }
        }
        return new ProductDataSet(output, errors);
    }

    /**
     * Adds an error for a product.
     *
     * @param errors the errors
     * @param data   the erroneous product
     * @param error  the error to add
     */
    private void addError(List<ProductData> errors, ProductData data, ProductIOException error) {
        data.setError(error.getMessage(), error.getLine());
        errors.add(data);
    }

}
