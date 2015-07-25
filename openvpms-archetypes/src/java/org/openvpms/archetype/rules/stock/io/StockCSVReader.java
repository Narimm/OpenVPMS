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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.stock.io;

import org.openvpms.archetype.csv.AbstractCSVReader;
import org.openvpms.archetype.csv.CSVException;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.document.Document;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads stock data from a CSV document in the format written by {@link StockCSVWriter}.
 *
 * @author Tim Anderson
 */
public class StockCSVReader extends AbstractCSVReader {

    /**
     * The stock location id column.
     */
    private static final int LOCATION_ID = 0;

    /**
     * The stock location name column.
     */
    private static final int LOCATION_NAME = LOCATION_ID + 1;

    /**
     * The product id column.
     */
    private static final int PRODUCT_ID = LOCATION_NAME + 1;

    /**
     * The product name column.
     */
    private static final int PRODUCT_NAME = PRODUCT_ID + 1;

    /**
     * The selling units column.
     */
    private static final int SELLING_UNITS = PRODUCT_NAME + 1;

    /**
     * The quantity column.
     */
    private static final int QUANTITY = SELLING_UNITS + 1;

    /**
     * The new quantity column.
     */
    private static final int NEW_QUANTITY = QUANTITY + 1;


    /**
     * Constructs a {@link StockCSVReader}.
     *
     * @param handlers  the document handlers
     * @param separator the field separator
     */
    public StockCSVReader(DocumentHandlers handlers, char separator) {
        super(handlers, StockCSVWriter.HEADER, separator);
    }

    /**
     * Reads a document.
     *
     * @param document the document to read
     * @return the read stock data
     */
    public StockDataSet read(Document document) {
        List<StockData> data = new ArrayList<StockData>();
        List<StockData> errors = new ArrayList<StockData>();
        StockDataSet result = new StockDataSet(data, errors);

        List<String[]> lines = readLines(document);

        int lineNo = 2; // line 1 is the header

        for (String[] line : lines) {
            parse(line, data, errors, lineNo);
            ++lineNo;
        }
        return result;
    }

    /**
     * Parses a line.
     *
     * @param line   the line to parse
     * @param data   the parsed stock data
     * @param errors the stock data with errors
     * @param lineNo the line number
     */
    private void parse(String[] line, List<StockData> data, List<StockData> errors, int lineNo) {
        long stockLocationId = -1;
        String stockLocationName = null;
        long productId = -1;
        String productName = null;
        String sellingUnits = null;
        BigDecimal quantity = BigDecimal.ZERO;
        BigDecimal newQuantity = BigDecimal.ZERO;

        try {
            checkFields(line, lineNo);
            stockLocationId = getLong(line, LOCATION_ID, lineNo, true);
            stockLocationName = getString(line, LOCATION_NAME, lineNo, true);
            productId = getLong(line, PRODUCT_ID, lineNo, true);
            productName = getString(line, PRODUCT_NAME, lineNo, true);
            sellingUnits = getString(line, SELLING_UNITS, lineNo, false);
            quantity = getDecimal(line, QUANTITY, lineNo, true);
            newQuantity = getDecimal(line, NEW_QUANTITY, lineNo, true);
            data.add(new StockData(stockLocationId, stockLocationName, productId, productName, sellingUnits,
                                   quantity, newQuantity, lineNo));
        } catch (CSVException exception) {
            StockData invalid = new StockData(stockLocationId, stockLocationName, productId, productName, sellingUnits,
                                              quantity, newQuantity, lineNo);
            invalid.setError(exception.getMessage());
            errors.add(invalid);
        }
    }


}
