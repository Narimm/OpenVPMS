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

package org.openvpms.archetype.rules.stock.io;

import org.openvpms.archetype.rules.product.io.ProductIOException;
import org.openvpms.archetype.rules.stock.StockArchetypes;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Filters {@link StockData} to exclude unchanged and erroneous data.
 *
 * @author Tim Anderson
 */
class StockDataFilter {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Constructs an {@link StockDataFilter}.
     *
     * @param service the archetype service
     */
    public StockDataFilter(IArchetypeService service) {
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
    public StockDataSet filter(List<StockData> input) {
        long stockLocationId = -1;
        String stockLocationName = null;
        IMObjectReference stockLocation = null;
        List<StockData> output = new ArrayList<StockData>();
        List<StockData> errors = new ArrayList<StockData>();

        for (StockData data : input) {
            try {
                if (stockLocation == null) {
                    stockLocationId = data.getStockLocationId();
                    stockLocationName = data.getStockLocationName();
                    stockLocation = getStockLocation(data);
                } else {
                    checkStockLocation(data, stockLocationId, stockLocationName);
                }
                // make sure the product can be resolved. Do this prior to checking quantities to ensure errors aren't
                // excluded
                data.setProduct(getProduct(data));

                BigDecimal quantity = data.getQuantity();
                BigDecimal newQuantity = data.getNewQuantity();
                if (quantity.compareTo(newQuantity) != 0) {
                    data.setStockLocation(stockLocation);
                    output.add(data);
                }
            } catch (ProductIOException exception) {
                data.setError(exception.getMessage());
                errors.add(data);
            }
        }
        return new StockDataSet(output, errors);
    }

    /**
     * Checks the stock location against that expected.
     *
     * @param data              the stock data to check
     * @param stockLocationId   the expected stock location id
     * @param stockLocationName the expected stock location name
     */
    private void checkStockLocation(StockData data, long stockLocationId, String stockLocationName) {
        if (stockLocationId != data.getStockLocationId()) {
            throw new ProductIOException(ProductIOException.ErrorCode.UnexpectedValue, data.getLine(),
                                         Long.toString(stockLocationId), StockCSVWriter.STOCK_LOCATION_ID,
                                         Long.toString(data.getStockLocationId()));
        }
        if (!stockLocationName.equalsIgnoreCase(data.getStockLocationName())) {
            throw new ProductIOException(ProductIOException.ErrorCode.UnexpectedValue, data.getLine(),
                                         stockLocationName, StockCSVWriter.STOCK_LOCATION_NAME,
                                         data.getStockLocationName());
        }
    }

    /**
     * Returns the stock location reference.
     *
     * @param data the stock data
     * @return the product reference
     * @throws ProductIOException if the stock location is not found
     */
    private IMObjectReference getStockLocation(StockData data) {
        IMObjectReference reference = getReference(StockArchetypes.STOCK_LOCATION, data.getStockLocationId(),
                                                   data.getStockLocationName(), data.getLine());
        if (reference == null) {
            throw new ProductIOException(ProductIOException.ErrorCode.StockLocationNotFound, data.getLine());
        }
        return reference;
    }

    /**
     * Returns the product reference.
     *
     * @param data the stock data
     * @return the product reference
     * @throws ProductIOException if the product is not found
     */
    private IMObjectReference getProduct(StockData data) {
        IMObjectReference reference = getReference("product.*", data.getProductId(), data.getProductName(),
                                                   data.getLine());
        if (reference == null) {
            throw new ProductIOException(ProductIOException.ErrorCode.ProductNotFound, data.getLine());
        }
        return reference;
    }

    /**
     * Returns the an object reference given its archetype short name, and id.
     * <p/>
     * The object name must match that supplied.
     *
     * @param shortName the archetype short name. May contain wildcards
     * @param id        the object id
     * @param name      the expected object name
     * @param line      the line the details come from, for error reporting purposes
     * @return the product reference
     * @throws ProductIOException if the product is not found
     */
    private IMObjectReference getReference(String shortName, long id, String name, int line) {
        ArchetypeQuery query = new ArchetypeQuery(shortName);
        query.add(Constraints.eq("id", id));
        IMObjectQueryIterator<IMObject> iterator = new IMObjectQueryIterator<IMObject>(service, query);
        if (iterator.hasNext()) {
            IMObject object = iterator.next();
            if (!name.equalsIgnoreCase(object.getName())) {
                throw new ProductIOException(ProductIOException.ErrorCode.InvalidName, line, object.getName(),
                                             name);
            }
            return object.getObjectReference();
        }
        return null;
    }
}

