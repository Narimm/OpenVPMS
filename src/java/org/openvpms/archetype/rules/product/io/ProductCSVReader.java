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
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.document.Document;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


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
     * The product default fixed price column.
     */
    private static final int DEFAULT_FIXED_PRICE = 8;

    /**
     * The product unit price id column.
     */
    private static final int UNIT_PRICE_ID = 9;

    /**
     * The product unit price column.
     */
    private static final int UNIT_PRICE = 10;

    /**
     * The product unit cost column.
     */
    private static final int UNIT_COST = 11;

    /**
     * The product unit price start date column.
     */
    private static final int UNIT_PRICE_START_DATE = 12;

    /**
     * The product unit price end date column.
     */
    private static final int UNIT_PRICE_END_DATE = 13;

    /**
     * Supported day-month-year date formats.
     */
    private static final DateFormat[] DMY_FORMAT = {
            new SimpleDateFormat("dd/MM/yyyy"), new SimpleDateFormat("dd/MM/yy"),
            new SimpleDateFormat("dd-MM-yyyy"), new SimpleDateFormat("dd-MM-yy")
    };

    /**
     * Supported year-month-day date formats.
     */
    private static final DateFormat[] YMD_FORMAT = {
            new SimpleDateFormat("yyyy-MM-dd"), new SimpleDateFormat("yy-MM-dd"),
            new SimpleDateFormat("yyyy/MM/dd"), new SimpleDateFormat("yy/MM/dd")
    };

    /**
     * Supported month-day-year date formats.
     */
    private static final DateFormat[] MDY_FORMAT = {
            new SimpleDateFormat("MM-dd-yyyy"), new SimpleDateFormat("MM-dd-yy"),
            new SimpleDateFormat("MM/dd/yyyy"), new SimpleDateFormat("MM/dd/yy")
    };

    static {
        for (DateFormat format : DMY_FORMAT) {
            format.setLenient(false);
        }
        for (DateFormat format : YMD_FORMAT) {
            format.setLenient(false);
        }
        for (DateFormat format : MDY_FORMAT) {
            format.setLenient(false);
        }
    }

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
    public ProductDataSet read(Document document) {
        DocumentHandler documentHandler = handlers.get(document);
        List<ProductData> data = new ArrayList<ProductData>();
        List<ProductData> errors = new ArrayList<ProductData>();
        ProductDataSet result = new ProductDataSet(data, errors);

        try {
            CSVReader reader = new CSVReader(new InputStreamReader(documentHandler.getContent(document)));
            String[] header = reader.readNext();
            if (header.length < ProductCSVWriter.HEADER.length) {
                throw new ProductIOException(ProductIOException.ErrorCode.UnrecognisedDocument, -1, document.getName());
            }
            for (int i = 0; i < header.length; ++i) {
                if (!header[i].equalsIgnoreCase(ProductCSVWriter.HEADER[i])) {
                    throw new ProductIOException(ProductIOException.ErrorCode.InvalidColumn, -1, header[i]);
                }
            }

            List<String[]> lines = reader.readAll();
            Set<String> dates = new LinkedHashSet<String>();
            for (int i = 0; i < lines.size(); ++i) {
                String[] line = lines.get(i);
                if (line.length < ProductCSVWriter.HEADER.length) {
                    throw new ProductIOException(ProductIOException.ErrorCode.InvalidLine, i + 1);
                }
                int lineNo = i + 1;
                addDate(line, FIXED_PRICE_START_DATE, lineNo, dates);
                addDate(line, FIXED_PRICE_END_DATE, lineNo, dates);
                addDate(line, UNIT_PRICE_START_DATE, lineNo, dates);
                addDate(line, UNIT_PRICE_END_DATE, lineNo, dates);
            }
            Set<DateFormat> formats = Collections.emptySet();
            if (!dates.isEmpty()) {
                formats = getDateFormats(dates, DMY_FORMAT);
                if (formats.isEmpty()) {
                    formats = getDateFormats(dates, YMD_FORMAT);
                    if (formats.isEmpty()) {
                        formats = getDateFormats(dates, MDY_FORMAT);
                        if (formats.isEmpty()) {
                            throw new ProductIOException(ProductIOException.ErrorCode.UnrecognisedDateFormat, -1);
                        }
                    }
                }
            }

            int lineNo = 2; // line 1 is the header

            ProductData current = null;
            for (String[] line : lines) {
                current = parse(line, current, data, errors, formats, lineNo);
                ++lineNo;
            }
        } catch (IOException exception) {
            throw new ProductIOException(ProductIOException.ErrorCode.ReadError, -1, exception);
        }
        return result;
    }

    /**
     * Parses a line.
     *
     * @param line    the line to parse
     * @param current the current product
     * @param data    the parsed product data
     * @param errors  the product data with errors
     * @param formats the expected date formats
     * @param lineNo  the line number
     * @return the product data, or {@code null} if it couldn't be parsed
     */
    private ProductData parse(String[] line, ProductData current, List<ProductData> data, List<ProductData> errors,
                              Set<DateFormat> formats, int lineNo) {
        long id = -1;
        String name = null;
        String printedName = null;

        try {
            id = getId(line, ID, lineNo, true);
            name = getName(line, lineNo);
            printedName = getValue(line, PRINTED_NAME, lineNo, false);
        } catch (ProductIOException exception) {
            ProductData invalid = new ProductData(id, name, printedName, lineNo);
            invalid.setError(exception.getMessage(), exception.getLine());
            errors.add(invalid);
            return null;
        }
        if (current == null || id != current.getId()) {
            current = new ProductData(id, name, printedName, lineNo);
            data.add(current);
        }
        try {
            long fixedId = getId(line, FIXED_PRICE_ID, lineNo, false);
            BigDecimal fixedPrice = getDecimal(line, FIXED_PRICE, lineNo);
            BigDecimal fixedCost = getDecimal(line, FIXED_COST, lineNo);
            Date fixedStartDate = getDate(line, FIXED_PRICE_START_DATE, lineNo, fixedPrice != null, formats);
            Date fixedEndDate = getDate(line, FIXED_PRICE_END_DATE, lineNo, false, formats);
            boolean defaultFixedPrice = getBoolean(line, DEFAULT_FIXED_PRICE, lineNo);
            long unitId = getId(line, UNIT_PRICE_ID, lineNo, false);
            BigDecimal unitPrice = getDecimal(line, UNIT_PRICE, lineNo);
            BigDecimal unitCost = getDecimal(line, UNIT_COST, lineNo);
            Date unitStartDate = getDate(line, UNIT_PRICE_START_DATE, lineNo, unitCost != null, formats);
            Date unitEndDate = getDate(line, UNIT_PRICE_END_DATE, lineNo, false, formats);
            if (fixedPrice != null) {
                current.addFixedPrice(fixedId, fixedPrice, fixedCost, fixedStartDate, fixedEndDate, defaultFixedPrice,
                                      lineNo);
            }
            if (unitPrice != null) {
                current.addUnitPrice(unitId, unitPrice, unitCost, unitStartDate, unitEndDate, lineNo);
            }
        } catch (ProductIOException exception) {
            current.setError(exception.getMessage(), exception.getLine());
            errors.add(current);
            current = null;
        }
        return current;
    }

    /**
     * Returns the date formats that can parse the supplied dates.
     *
     * @param dates   the dates
     * @param formats the available formats
     * @return the formats that can parse the supplied dates
     */
    private Set<DateFormat> getDateFormats(Set<String> dates, DateFormat[] formats) {
        Set<DateFormat> result = new HashSet<DateFormat>();
        for (String date : dates) {
            for (DateFormat format : formats) {
                try {
                    format.parse(date);
                    result.add(format);
                } catch (ParseException ignore) {
                    // do nothing
                }
            }
        }
        return result;
    }

    /**
     * Adds a date from the specified line to {@code dates}.
     *
     * @param line   the line
     * @param index  the date index
     * @param lineNo the line no
     * @param dates  the dates to add to
     */
    private void addDate(String[] line, int index, int lineNo, Set<String> dates) {
        String value = getValue(line, index, lineNo, false);
        if (value != null) {
            dates.add(value);
        }
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
        String value = getValue(line, index, lineNo, required);
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
        return getValue(line, NAME, lineNo, true);
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
     *
     * @param line     the line
     * @param index    the date column index
     * @param lineNo   the line no.
     * @param required if {@code true}, the date is required
     * @return the date, or {@code null} if there is no date
     */
    private Date getDate(String[] line, int index, int lineNo, boolean required, Set<DateFormat> formats) {
        String value = getValue(line, index, lineNo, required);
        Date result = null;
        if (value != null) {
            for (DateFormat format : formats) {
                try {
                    result = format.parse(value);
                    break;
                } catch (ParseException ignore) {
                    // do nothing
                }
            }
            if (result == null) {
                reportInvalid(ProductCSVWriter.HEADER[index], value, lineNo);
            }
        }
        return result;
    }

    /**
     * Returns a boolean at the specified line.
     *
     * @param line   the line
     * @param index  the boolean column index
     * @param lineNo the line no.
     * @return the date, or {@code null} if there is no date
     */
    private boolean getBoolean(String[] line, int index, int lineNo) {
        String value = getValue(line, index, lineNo, false);
        return value != null ? Boolean.valueOf(value) : false;
    }

    /**
     * Returns a value from a line.
     *
     * @param line     the line
     * @param index    the value index
     * @param lineNo   the line number
     * @param required if {@code true}, the value must be present
     * @return the value. May be {@code null} if {@code required} is {@code false}
     * @throws ProductIOException if the value is required but not present
     */
    private String getValue(String[] line, int index, int lineNo, boolean required) {
        String value = StringUtils.trimToNull(line[index]);
        if (value == null && required) {
            throw new ProductIOException(ProductIOException.ErrorCode.RequiredValue,
                                         lineNo, ProductCSVWriter.HEADER[index]);
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
        throw new ProductIOException(ProductIOException.ErrorCode.InvalidValue, lineNo, name, value);
    }

}
