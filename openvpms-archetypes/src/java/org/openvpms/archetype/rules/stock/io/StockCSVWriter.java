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

import au.com.bytecode.opencsv.CSVWriter;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.document.Document;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Iterator;

import static org.openvpms.archetype.csv.AbstractCSVReader.MIME_TYPE;

/**
 * Writes stock data to a CSV document.
 *
 * @author Tim Anderson
 */
public class StockCSVWriter {

    /**
     * Stock location id heading name.
     */
    static final String STOCK_LOCATION_ID = "Stock Location Identifier";

    /**
     * Stock location name heading name.
     */
    static final String STOCK_LOCATION_NAME = "Stock Location Name";

    /**
     * The CSV header line.
     */
    public static final String[] HEADER = {
            STOCK_LOCATION_ID, STOCK_LOCATION_NAME, "Product Identifier", "Product Name", "Selling Units",
            "Quantity", "New Quantity"};

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * The field separator.
     */
    private final char separator;


    /**
     * Constructs a {@link StockCSVWriter}.
     *
     * @param handlers  the document handlers
     * @param separator the field separator
     */
    public StockCSVWriter(DocumentHandlers handlers, char separator) {
        this.handlers = handlers;
        this.separator = separator;
    }

    /**
     * Writes stock data to a document.
     *
     * @param name  the document name
     * @param stock the stock data to write
     * @return the document
     */
    public Document write(String name, Iterator<StockData> stock) {
        StringWriter writer = new StringWriter();
        CSVWriter csv = new CSVWriter(writer, separator);
        csv.writeNext(HEADER);
        while (stock.hasNext()) {
            write(stock.next(), csv);
        }
        DocumentHandler handler = handlers.get(name, MIME_TYPE);
        byte[] buffer = writer.getBuffer().toString().getBytes(Charset.forName("UTF-8"));
        return handler.create(name, new ByteArrayInputStream(buffer), MIME_TYPE, buffer.length);
    }

    /**
     * Writes a line of stock data.
     *
     * @param data   the data to write
     * @param writer the writer to write to
     */
    private void write(StockData data, CSVWriter writer) {
        String line[] = {Long.toString(data.getStockLocationId()),
                         data.getStockLocationName(),
                         Long.toString(data.getProductId()),
                         data.getProductName(),
                         data.getSellingUnits(),
                         data.getQuantity().toString(),
                         data.getNewQuantity().toString()};
        writer.writeNext(line);
        if (writer.checkError()) {
            throw new IllegalStateException("Failed to write stock data");
        }
    }

}
