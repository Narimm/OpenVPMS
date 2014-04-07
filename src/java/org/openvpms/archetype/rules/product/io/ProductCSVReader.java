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
import java.util.Arrays;
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
     * Supported day-month-year date formats.
     */
    public static final SimpleDateFormat[] DAY_MONTH_YEAR_FORMATS = {
            new SimpleDateFormat("dd/MM/yy"), new SimpleDateFormat("dd-MM-yy")
    };

    /**
     * Supported year-month-day date formats.
     */
    public static final SimpleDateFormat[] YEAR_MONTH_DAY_FORMATS = {
            new SimpleDateFormat("yy/MM/dd"), new SimpleDateFormat("yy-MM-dd")
    };

    /**
     * Supported month-day-year date formats.
     */
    public static final SimpleDateFormat[] MONTH_DAY_YEAR_FORMATS = {
            new SimpleDateFormat("MM/dd/yy"), new SimpleDateFormat("MM-dd-yy")
    };

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * Formats to parse dates with.
     */
    private List<SimpleDateFormat> formats = Arrays.asList(DAY_MONTH_YEAR_FORMATS);

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
     * The fixed price max discount column.
     */
    private static final int FIXED_PRICE_MAX_DISCOUNT = 6;

    /**
     * The product fixed price start date column.
     */
    private static final int FIXED_PRICE_START_DATE = 7;

    /**
     * The product fixed price end date column.
     */
    private static final int FIXED_PRICE_END_DATE = 8;

    /**
     * The product default fixed price column.
     */
    private static final int DEFAULT_FIXED_PRICE = 9;

    /**
     * The fixed price groups column.
     */
    private static final int FIXED_PRICE_GROUPS = 10;

    /**
     * The product unit price id column.
     */
    private static final int UNIT_PRICE_ID = 11;

    /**
     * The product unit price column.
     */
    private static final int UNIT_PRICE = 12;

    /**
     * The product unit cost column.
     */
    private static final int UNIT_COST = 13;

    /**
     * The unit price max discount column.
     */
    private static final int UNIT_PRICE_MAX_DISCOUNT = 14;

    /**
     * The product unit price start date column.
     */
    private static final int UNIT_PRICE_START_DATE = 15;

    /**
     * The product unit price end date column.
     */
    private static final int UNIT_PRICE_END_DATE = 16;

    /**
     * The unit price groups column.
     */
    private static final int UNIT_PRICE_GROUPS = 17;

    /**
     * The product tax rate column.
     */
    private static final int TAX_RATE = 18;

    static {
        for (DateFormat format : DAY_MONTH_YEAR_FORMATS) {
            format.setLenient(false);
        }
        for (DateFormat format : YEAR_MONTH_DAY_FORMATS) {
            format.setLenient(false);
        }
        for (DateFormat format : MONTH_DAY_YEAR_FORMATS) {
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
     * Sets the date formats used to parse dates.
     * <p/>
     * <em>Warning:</em> do not specify multiple formats that have different d/m/y ordering.
     *
     * @param formats the date formats
     */
    public void setDateFormats(List<SimpleDateFormat> formats) {
        this.formats = formats;
    }

    /**
     * Returns the date formats detected in the document.
     *
     * @param document the document
     * @return the detected date formats
     * @throws ProductIOException if the document is invalid
     */
    public List<SimpleDateFormat> getDateFormats(Document document) {
        List<SimpleDateFormat> result = new ArrayList<SimpleDateFormat>();
        List<String[]> lines = readLines(document);
        Set<String> dates = new LinkedHashSet<String>();
        for (int i = 0; i < lines.size(); ++i) {
            String[] line = lines.get(i);
            int lineNo = i + 2;
            if (line.length < ProductCSVWriter.HEADER.length) {
                throw new ProductIOException(ProductIOException.ErrorCode.InvalidLine, lineNo);
            }
            addDate(line, FIXED_PRICE_START_DATE, lineNo, dates);
            addDate(line, FIXED_PRICE_END_DATE, lineNo, dates);
            addDate(line, UNIT_PRICE_START_DATE, lineNo, dates);
            addDate(line, UNIT_PRICE_END_DATE, lineNo, dates);
        }
        if (!dates.isEmpty()) {
            result.addAll(getDateFormats(dates, DAY_MONTH_YEAR_FORMATS));
            result.addAll(getDateFormats(dates, YEAR_MONTH_DAY_FORMATS));
            result.addAll(getDateFormats(dates, MONTH_DAY_YEAR_FORMATS));
            if (result.isEmpty()) {
                throw new ProductIOException(ProductIOException.ErrorCode.UnrecognisedDateFormat, -1);
            }
        }
        return result;
    }

    /**
     * Reads a document.
     *
     * @param document the document to read
     * @return the read product data
     */
    public ProductDataSet read(Document document) {
        List<ProductData> data = new ArrayList<ProductData>();
        List<ProductData> errors = new ArrayList<ProductData>();
        ProductDataSet result = new ProductDataSet(data, errors);

        List<String[]> lines = readLines(document);

        int lineNo = 2; // line 1 is the header

        ProductData current = null;
        for (String[] line : lines) {
            current = parse(line, current, data, errors, lineNo);
            ++lineNo;
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
     * @param lineNo  the line number
     * @return the product data, or {@code null} if it couldn't be parsed
     */
    private ProductData parse(String[] line, ProductData current, List<ProductData> data, List<ProductData> errors,
                              int lineNo) {
        long id = -1;
        String name = null;
        String printedName = null;
        BigDecimal tax = null;

        try {
            id = getId(line, ID, lineNo, true);
            name = getName(line, lineNo);
            printedName = getValue(line, PRINTED_NAME, lineNo, false);
            tax = getDecimal(line, TAX_RATE, lineNo);
        } catch (ProductIOException exception) {
            ProductData invalid = new ProductData(id, name, printedName, tax, lineNo);
            invalid.setError(exception.getMessage(), exception.getLine());
            errors.add(invalid);
            return null;
        }
        if (current == null || id != current.getId()) {
            current = new ProductData(id, name, printedName, tax, lineNo);
            data.add(current);
        }
        try {
            long fixedId = getId(line, FIXED_PRICE_ID, lineNo, false);
            BigDecimal fixedPrice = getDecimal(line, FIXED_PRICE, lineNo);
            BigDecimal fixedCost = getDecimal(line, FIXED_COST, lineNo);
            BigDecimal fixedMaxDiscount = getDecimal(line, FIXED_PRICE_MAX_DISCOUNT, lineNo);
            Date fixedStartDate = getDate(line, FIXED_PRICE_START_DATE, lineNo, fixedPrice != null);
            Date fixedEndDate = getDate(line, FIXED_PRICE_END_DATE, lineNo, false);
            boolean defaultFixedPrice = getBoolean(line, DEFAULT_FIXED_PRICE, lineNo);
            String[] fixedPriceGroups = getPricingGroups(line, FIXED_PRICE_GROUPS);
            long unitId = getId(line, UNIT_PRICE_ID, lineNo, false);
            BigDecimal unitPrice = getDecimal(line, UNIT_PRICE, lineNo);
            BigDecimal unitCost = getDecimal(line, UNIT_COST, lineNo);
            BigDecimal unitMaxDiscount = getDecimal(line, UNIT_PRICE_MAX_DISCOUNT, lineNo);
            Date unitStartDate = getDate(line, UNIT_PRICE_START_DATE, lineNo, unitCost != null);
            Date unitEndDate = getDate(line, UNIT_PRICE_END_DATE, lineNo, false);
            String[] unitPriceGroups = getPricingGroups(line, UNIT_PRICE_GROUPS);
            if (fixedPrice != null) {
                current.addFixedPrice(fixedId, fixedPrice, fixedCost, fixedMaxDiscount, fixedStartDate, fixedEndDate,
                                      defaultFixedPrice, fixedPriceGroups, lineNo);
            }
            if (unitPrice != null) {
                current.addUnitPrice(unitId, unitPrice, unitCost, unitMaxDiscount, unitStartDate, unitEndDate,
                                     unitPriceGroups, lineNo);
            }
        } catch (ProductIOException exception) {
            current.setError(exception.getMessage(), exception.getLine());
            errors.add(current);
            current = null;
        }
        return current;
    }

    /**
     * Reads the document into an array of lines.
     *
     * @param document the document to read
     * @return the lines
     */
    private List<String[]> readLines(Document document) {
        try {
            DocumentHandler documentHandler = handlers.get(document);
            CSVReader reader = new CSVReader(new InputStreamReader(documentHandler.getContent(document)));
            String[] header = reader.readNext();
            if (header.length < ProductCSVWriter.HEADER.length) {
                throw new ProductIOException(ProductIOException.ErrorCode.UnrecognisedDocument, 1, document.getName());
            }
            for (int i = 0; i < header.length; ++i) {
                if (!header[i].equalsIgnoreCase(ProductCSVWriter.HEADER[i])) {
                    throw new ProductIOException(ProductIOException.ErrorCode.InvalidColumn, 1, header[i]);
                }
            }
            return reader.readAll();
        } catch (IOException exception) {
            throw new ProductIOException(ProductIOException.ErrorCode.ReadError, -1, exception);
        }
    }

    /**
     * Returns the date formats that can parse the supplied dates.
     *
     * @param dates   the dates
     * @param formats the available formats
     * @return the formats that can parse the supplied dates
     */
    private Set<SimpleDateFormat> getDateFormats(Set<String> dates, SimpleDateFormat[] formats) {
        Set<SimpleDateFormat> result = new HashSet<SimpleDateFormat>();
        for (String date : dates) {
            for (SimpleDateFormat format : formats) {
                try {
                    format.parse(date);
                    result.add(format);
                    if (result.size() == formats.length) {
                        // all possible formats used
                        break;
                    }
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
    private Date getDate(String[] line, int index, int lineNo, boolean required) {
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

    private String[] getPricingGroups(String[] line, int index) {
        String[] result = {};
        String value = StringUtils.trimToNull(line[index]);
        if (value != null) {
            result = value.split(" ");
        }
        return result;
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
