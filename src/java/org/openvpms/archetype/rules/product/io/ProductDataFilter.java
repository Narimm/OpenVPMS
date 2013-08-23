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

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

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
     * The product price rules.
     */
    private final ProductPriceRules rules;

    /**
     * Constructs an {@link ProductDataFilter}.
     *
     * @param service the archetype service
     * @param rules   the price rules
     */
    public ProductDataFilter(IArchetypeService service, ProductPriceRules rules) {
        this.service = service;
        this.rules = rules;
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
    public FilterResult filter(List<ProductData> input) {
        List<ProductData> output = new ArrayList<ProductData>();
        List<ProductData> errors = new ArrayList<ProductData>();

        for (ProductData data : input) {
            ArchetypeQuery query = new ArchetypeQuery("product.*");
            query.add(Constraints.eq("id", data.getId()));
            IMObjectQueryIterator<Product> iterator = new IMObjectQueryIterator<Product>(service, query);
            if (iterator.hasNext()) {
                Product product = iterator.next();
                if (!StringUtils.equalsIgnoreCase(product.getName(), data.getName())) {
                    addError(errors, data, new ProductIOException(InvalidName, product.getName()));
                } else {
                    ProductData modified = getModifiedPrices(data, product);
                    if (modified != null) {
                        output.add(data);
                    }
                }
            } else {
                addError(errors, data, new ProductIOException(NotFound));
            }
        }
        return new FilterResult(output, errors);
    }

    /**
     * Returns the product data with any prices that have changed from the original.
     * <p/>
     * NOTE: this modifies {@code data}
     *
     * @param data    the product data
     * @param product the original product
     * @return the product data containing only the modified prices, or {@code null} if it is unchanged from the
     *         original
     */
    private ProductData getModifiedPrices(ProductData data, Product product) {
        ProductData result = null;
        List<PriceData> fixedPrices = new ArrayList<PriceData>();
        List<PriceData> unitPrices = new ArrayList<PriceData>();
        Set<ProductPrice> currentFixedPrices = rules.getProductPrices(product, ProductArchetypes.FIXED_PRICE);
        Set<ProductPrice> currentUnitPrices = rules.getProductPrices(product, ProductArchetypes.UNIT_PRICE);
        for (PriceData price : data.getFixedPrices()) {
            if (!hasPrice(currentFixedPrices, price)) {
                fixedPrices.add(price);
            }
        }
        for (PriceData price : data.getUnitPrices()) {
            if (!hasPrice(currentUnitPrices, price)) {
                unitPrices.add(price);
            }
        }
        if (!fixedPrices.isEmpty() || !unitPrices.isEmpty()) {
            data.setFixedPrices(fixedPrices);
            data.setUnitPrices(unitPrices);
            data.setReference(product.getObjectReference());
            result = data;
        }
        return result;
    }

    /**
     * Adds an error for a product.
     *
     * @param errors the errors
     * @param data   the erroneous product
     * @param error  the error to add
     */
    private void addError(List<ProductData> errors, ProductData data, ProductIOException error) {
        data.setError(error.getMessage());
        errors.add(data);
    }

    /**
     * Determines if a price is present in a product's prices.
     *
     * @param prices the product prices
     * @param data   the price
     * @return {@code true} if the price is present
     */
    private boolean hasPrice(Set<ProductPrice> prices, PriceData data) {
        boolean result = false;
        for (ProductPrice price : prices) {
            if (datesEquals(price.getFromDate(), data.getFrom())
                && datesEquals(price.getToDate(), data.getTo())
                && price.getPrice().compareTo(data.getPrice()) == 0) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Determines if two dates are equal.
     * <p/>
     * This handles nulls and ignores any time component.
     *
     * @param date1 the first date. May be {@code null}
     * @param date2 the second date. May be {@code null}
     * @return {@code true} if the dates are equal
     */
    private boolean datesEquals(Date date1, Date date2) {
        boolean result;
        if (date1 == null || date2 == null) {
            result = (date1 == null && date2 == null);
        } else {
            result = DateRules.compareDates(date1, date2) == 0;
        }
        return result;
    }

}
