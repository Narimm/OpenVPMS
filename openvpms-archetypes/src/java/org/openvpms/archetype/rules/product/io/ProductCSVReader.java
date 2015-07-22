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

package org.openvpms.archetype.rules.product.io;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.csv.AbstractCSVReader;
import org.openvpms.archetype.csv.CSVException;
import org.openvpms.archetype.csv.CSVReaderException;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.lookup.ILookupService;

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

import static org.openvpms.archetype.rules.product.io.ProductCSVWriter.HEADER;


/**
 * Reads product data in the format written by {@link ProductCSVWriter}.
 *
 * @author Tim Anderson
 */
public class ProductCSVReader extends AbstractCSVReader implements ProductReader {

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
     * The lookup service.
     */
    private final ILookupService lookups;

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
     * @param lookups  the lookups service
     */
    public ProductCSVReader(DocumentHandlers handlers, ILookupService lookups) {
        super(handlers, HEADER, ProductCSVWriter.SEPARATOR);
        this.lookups = lookups;
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
            checkFields(line, lineNo);
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
     * Verifies the line has at least {@code HEADER.length - 1} fields. This is because the last field (Notes) is not
     * required, and may not be supplied by Excel if it is empty.
     *
     * @param line   the line
     * @param lineNo the line number
     * @throws CSVReaderException if the line has the incorrect no. of fields
     */
    @Override
    protected void checkFields(String[] line, int lineNo) {
        if (line.length < HEADER.length - 1) {
            throw new CSVReaderException(CSVReaderException.ErrorCode.InvalidLine, lineNo, lineNo, HEADER.length, line.length);
        }
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
            checkFields(line, lineNo);
            id = getLong(line, ID, lineNo, true);
            name = getName(line, lineNo);
            printedName = getString(line, PRINTED_NAME, lineNo, false);
            tax = getDecimal(line, TAX_RATE, lineNo, true);
        } catch (CSVException exception) {
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
            long fixedId = getLong(line, FIXED_PRICE_ID, lineNo, false);
            BigDecimal fixedPrice = getDecimal(line, FIXED_PRICE, lineNo, false);
            BigDecimal fixedCost = getDecimal(line, FIXED_COST, lineNo, fixedPrice != null);
            BigDecimal fixedMaxDiscount = getDecimal(line, FIXED_PRICE_MAX_DISCOUNT, lineNo, fixedPrice != null);
            Date fixedStartDate = getDate(line, FIXED_PRICE_START_DATE, lineNo, fixedPrice != null);
            Date fixedEndDate = getDate(line, FIXED_PRICE_END_DATE, lineNo, false);
            boolean defaultFixedPrice = getBoolean(line, DEFAULT_FIXED_PRICE, lineNo);
            Set<Lookup> fixedPriceGroups = getPricingGroups(line, FIXED_PRICE_GROUPS, lineNo);
            long unitId = getLong(line, UNIT_PRICE_ID, lineNo, false);
            BigDecimal unitPrice = getDecimal(line, UNIT_PRICE, lineNo, false);
            BigDecimal unitCost = getDecimal(line, UNIT_COST, lineNo, unitPrice != null);
            BigDecimal unitMaxDiscount = getDecimal(line, UNIT_PRICE_MAX_DISCOUNT, lineNo, unitPrice != null);
            Date unitStartDate = getDate(line, UNIT_PRICE_START_DATE, lineNo, unitPrice != null);
            Date unitEndDate = getDate(line, UNIT_PRICE_END_DATE, lineNo, false);
            Set<Lookup> unitPriceGroups = getPricingGroups(line, UNIT_PRICE_GROUPS, lineNo);
            if (fixedPrice != null) {
                current.addFixedPrice(fixedId, fixedPrice, fixedCost, fixedMaxDiscount, fixedStartDate, fixedEndDate,
                                      defaultFixedPrice, fixedPriceGroups, lineNo);
            }
            if (unitPrice != null) {
                current.addUnitPrice(unitId, unitPrice, unitCost, unitMaxDiscount, unitStartDate, unitEndDate,
                                     unitPriceGroups, lineNo);
            }
        } catch (CSVException exception) {
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
        String value = getString(line, index, lineNo, false);
        if (value != null) {
            dates.add(value);
        }
    }

    /**
     * Returns the product name at the specified line.
     *
     * @param line   the line
     * @param lineNo the line no.
     * @return the product name
     */
    private String getName(String[] line, int lineNo) {
        return getString(line, NAME, lineNo, true);
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
        String value = getString(line, index, lineNo, required);
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
                reportInvalid(HEADER[index], value, lineNo);
            }
        }
        return result;
    }

    /**
     * Parsers the pricing groups from the specified line.
     *
     * @param line   the line
     * @param index  the value index
     * @param lineNo the line number
     * @return the pricing groups
     * @throws ProductIOException if a pricing group code is invalid
     */
    private Set<Lookup> getPricingGroups(String[] line, int index, int lineNo) {
        Set<Lookup> result = new HashSet<Lookup>();
        String[] codes = {};
        String value = StringUtils.trimToNull(line[index]);
        if (value != null) {
            codes = value.split(" ");
        }
        for (String code : codes) {
            Lookup lookup = lookups.getLookup(ProductArchetypes.PRICING_GROUP, code);
            if (lookup == null) {
                reportInvalid(HEADER[index], code, lineNo);
            } else {
                result.add(lookup);
            }
        }

        return result;
    }

}
