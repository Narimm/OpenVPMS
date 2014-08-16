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

import org.openvpms.component.business.domain.im.act.Act;

import java.util.Collections;
import java.util.List;

/**
 * Stock data imported by the {@link StockDataImporter}.
 *
 * @author Tim Anderson
 */
public class StockDataSet {

    /**
     * The stock data.
     */
    private final List<StockData> data;

    /**
     * The erroneous data.
     */
    private final List<StockData> errors;

    /**
     * The adjustment.
     */
    private final Act adjustment;

    /**
     * Constructs a {@link StockDataSet}.
     *
     * @param data       the loaded data
     * @param adjustment the adjustment
     */
    public StockDataSet(List<StockData> data, Act adjustment) {
        this(data, Collections.<StockData>emptyList(), adjustment);
    }

    /**
     * Constructs a {@link StockDataSet}.
     *
     * @param data   the filtered data
     * @param errors the erroneous data
     */
    public StockDataSet(List<StockData> data, List<StockData> errors) {
        this(data, errors, null);
    }

    /**
     * Constructs a {@link StockDataSet}.
     *
     * @param data       the filtered data
     * @param errors     the erroneous data
     * @param adjustment the adjustment. May be {@code null}
     */
    public StockDataSet(List<StockData> data, List<StockData> errors, Act adjustment) {
        this.data = data;
        this.errors = errors;
        this.adjustment = adjustment;
    }

    /**
     * Returns the stock data.
     *
     * @return the stock data
     */
    public List<StockData> getData() {
        return data;
    }

    /**
     * Returns the stock data that contains errors.
     *
     * @return the erroneous stock data
     */
    public List<StockData> getErrors() {
        return errors;
    }

    /**
     * Returns the adjustment.
     *
     * @return the adjustment, or {@code null} if none has been created
     */
    public Act getAdjustment() {
        return adjustment;
    }

}
