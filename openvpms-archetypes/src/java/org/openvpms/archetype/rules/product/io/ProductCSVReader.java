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

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.document.Document;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Reads product data in the format written by {@link ProductCSVWriter}.
 *
 * @author Tim Anderson
 */
public class ProductCSVReader implements ProductReader {

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * The product identifier column.
     */
    private static final int ID = 0;

    /**
     * The product name column.
     */
    private static final int NAME = 1;

    /**
     * The product printed name column.
     */
    private static final int PRINTED_NAME = 2;

    /**
     * The product fixed price id.
     */
    private static final int FIXED_PRICE_ID = 3;

    /**
     * The product fixed price column.
     */
    private static final int FIXED_PRICE = 4;

    /**
     * The product fixed cost column.
     */
    private static final int FIXED_COST = 5;

    /**
     * The product fixed price start date column.
     */
    private static final int FIXED_PRICE_START_DATE = 6;

    /**
     * The product fixed price end date column.
     */
    private static final int FIXED_PRICE_END_DATE = 7;

    /**
     * The product unit price id column.
     */
    private static final int UNIT_PRICE_ID = 8;

    /**
     * The product unit price column.
     */
    private static final int UNIT_PRICE = 9;

    /**
     * The product unit cost column.
     */
    private static final int UNIT_COST = 10;

    /**
     * The product unit price start date column.
     */
    private static final int UNIT_PRICE_START_DATE = 11;

    /**
     * The product unit price end date column.
     */
    private static final int UNIT_PRICE_END_DATE = 12;


    private static final String[] DATE_FORMATS = {
            "yyyy-MM-dd", "yy-MM-dd", "dd/MM/yyyy", "dd/MM/yy"
    };

    /**
     * Constructs a {@link ProductCSVReader}.
     *
     * @param handlers the document handlers
     */
    public ProductCSVReader(DocumentHandlers handlers) {
        this.handlers = handlers;
    }

    /**
     * Reads a document.
     *
     * @param document the document to read
     * @return the read product data
     */
    public List<ProductData> read(Document document) {
        DocumentHandler documentHandler = handlers.get(document);
        List<ProductData> result = new ArrayList<ProductData>();

        try {
            CSVReader reader = new CSVReader(new InputStreamReader(documentHandler.getContent(document)));
            String[] header = reader.readNext();
            if (header.length < ProductCSVWriter.HEADER.length) {
                throw new ProductIOException(ProductIOException.ErrorCode.UnrecognisedDocument, document.getName());
            }
            for (int i = 0; i < header.length; ++i) {
                if (!header[i].equalsIgnoreCase(ProductCSVWriter.HEADER[i])) {
                    throw new ProductIOException(ProductIOException.ErrorCode.InvalidColumn, header[i]);
                }
            }

            int lineNo = 1;

            String[] line;
            ProductData data = null;
            while ((line = reader.readNext()) != null) {
                long id = getId(line, ID, lineNo, true);
                String name = getName(line, lineNo);
                String printedName = line[PRINTED_NAME];
                if (data == null || id != data.getId()) {
                    data = new ProductData(id, name, printedName, lineNo);
                    result.add(data);
                }
                long fixedId = getId(line, FIXED_PRICE_ID, lineNo, false);
                BigDecimal fixedPrice = getDecimal(line, FIXED_PRICE, lineNo);
                BigDecimal fixedCost = getDecimal(line, FIXED_COST, lineNo);
                Date fixedStartDate = getDate(line, FIXED_PRICE_START_DATE, lineNo, true);
                Date fixedEndDate = getDate(line, FIXED_PRICE_END_DATE, lineNo, false);
                long unitId = getId(line, UNIT_PRICE_ID, lineNo, false);
                BigDecimal unitPrice = getDecimal(line, UNIT_PRICE, lineNo);
                BigDecimal unitCost = getDecimal(line, UNIT_COST, lineNo);
                Date unitStartDate = getDate(line, UNIT_PRICE_START_DATE, lineNo, true);
                Date unitEndDate = getDate(line, UNIT_PRICE_END_DATE, lineNo, false);
                if (fixedPrice != null) {
                    data.addFixedPrice(fixedId, fixedPrice, fixedCost, fixedStartDate, fixedEndDate, lineNo);
                }
                if (unitPrice != null) {
                    data.addUnitPrice(unitId, unitPrice, unitCost, unitStartDate, unitEndDate, lineNo);
                }
                ++lineNo;
            }
        } catch (IOException exception) {
            throw new ProductIOException(ProductIOException.ErrorCode.ReadError, exception);
        }
        return result;
    }

    /**
     * Returns the identifier at the specified line.
     *
     * @param line     the line
     * @param lineNo   the line no.
     * @param required if {@code true}, the id is required
     * @return the identifier, or {@code -1} if it is optional and not present
     */
    private long getId(String[] line, int index, int lineNo, boolean required) {
        String value = (required) ? getRequired(line, index, lineNo) : line[index];
        long result = -1;
        if (!StringUtils.isEmpty(value)) {
            try {
                result = Long.valueOf(value);
            } catch (NumberFormatException exception) {
                reportInvalid(ProductCSVWriter.HEADER[index], value, lineNo);
            }
        }
        return result;
    }

    /**
     * Returns the product name at the specified line.
     *
     * @param line   the line
     * @param lineNo the line no.
     * @return the product name
     */
    private String getName(String[] line, int lineNo) {
        return getRequired(line, NAME, lineNo);
    }

    /**
     * Returns a decimal value at the specified line.
     *
     * @param line   the line
     * @param index  the price column index
     * @param lineNo the line no.
     * @return the value, or {@code null} if there is no value
     */
    private BigDecimal getDecimal(String[] line, int index, int lineNo) {
        String value = line[index];
        BigDecimal result = null;
        try {
            if (!StringUtils.isEmpty(value)) {
                result = new BigDecimal(value);
            }
        } catch (NumberFormatException exception) {
            reportInvalid(ProductCSVWriter.HEADER[index], value, lineNo);
        }
        return result;
    }

    /**
     * Returns a date at the specified line.
     * <p/>
     * Note that this currently doesn't handle formats where the month is first.
     * Either need to scan the data to determine the date format, or specify it upfront. TODO
     *
     * @param line     the line
     * @param index    the date column index
     * @param lineNo   the line no.
     * @param required if {@code true}, the date is required
     * @return the date, or {@code null} if there is no date
     */
    private Date getDate(String[] line, int index, int lineNo, boolean required) {
        String value = (required) ? getRequired(line, index, lineNo) : line[index];
        Date result = null;
        try {
            if (!StringUtils.isEmpty(value)) {
                result = DateUtils.parseDate(value, DATE_FORMATS);
            }
        } catch (ParseException exception) {
            reportInvalid(ProductCSVWriter.HEADER[index], value, lineNo);
        }
        return result;
    }

    /**
     * Returns a required value.
     *
     * @param line   the line
     * @param index  the column index
     * @param lineNo the line no.
     * @return the value
     * @throws ProductIOException if the value isn't present
     */
    private String getRequired(String[] line, int index, int lineNo) {
        String value = StringUtils.trimToNull(line[index]);
        if (value == null) {
            throw new ProductIOException(ProductIOException.ErrorCode.RequiredValue,
                                         ProductCSVWriter.HEADER[index], lineNo);
        }
        return value;
    }

    /**
     * Reports an invalid value.
     *
     * @param name   the column name
     * @param value  the invalid value
     * @param lineNo the line no.
     * @throws ProductIOException
     */
    private void reportInvalid(String name, String value, int lineNo) {
        throw new ProductIOException(ProductIOException.ErrorCode.InvalidValue, name, value, lineNo);
    }


}
