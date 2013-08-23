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

import au.com.bytecode.opencsv.CSVWriter;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Writes product data as a CSV document.
 *
 * @author Tim Anderson
 */
public class ProductCSVWriter implements ProductWriter {

    /**
     * The file header.
     */
    public static final String[] HEADER = {
            "Product Identifier", "Product Name", "Product Printed Name", "Fixed Price", "Fixed Price Start Date",
            "Fixed Price End Date", "Unit Price", "Unit Price Start Date", "Unit Price End Date"};

    /**
     * The product price rules.
     */
    private final ProductPriceRules rules;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * Field separator.
     */
    private static final char SEPARATOR = ',';

    /**
     * The mime type of the exported document.
     */
    private static final String MIME_TYPE = "text/csv";

    /**
     * The prices to write.
     */
    private enum Prices {
        ALL, LATEST, RANGE
    }

    /**
     * Constructs a {@link ProductCSVWriter}.
     *
     * @param rules    the price rules
     * @param handlers the document handlers
     */
    public ProductCSVWriter(ProductPriceRules rules, DocumentHandlers handlers) {
        this.rules = rules;
        this.handlers = handlers;
    }

    /**
     * Writes product data to a document.
     *
     * @param products the products to write
     * @param latest   if {@code true}, output the latest price, else output all prices
     * @return the document
     */
    @Override
    public Document write(Iterator<Product> products, boolean latest) {
        Prices prices = (latest) ? Prices.LATEST : Prices.ALL;
        return write(products, prices, null, null);
    }

    /**
     * Writes product data to a document.
     * <p/>
     * This writes prices active within a date range
     *
     * @param products the products to write
     * @param from     the price start date. May be {@code null}
     * @param to       the price end date. May be {@code null}
     * @return the document
     */
    @Override
    public Document write(Iterator<Product> products, Date from, Date to) {
        return write(products, Prices.RANGE, from, to);
    }

    /**
     * Writes product data to a document.
     *
     * @param products the products to write
     * @param prices   determines which prices to write
     * @param from     the price start date. May be {@code null}
     * @param to       the price end date. May be {@code null}
     * @return the document
     */
    private Document write(Iterator<Product> products, Prices prices, Date from, Date to) {
        StringWriter writer = new StringWriter();
        CSVWriter csv = new CSVWriter(writer, SEPARATOR);
        csv.writeNext(HEADER);
        while (products.hasNext()) {
            write(products.next(), prices, from, to, csv);
        }
        String name = "products-" + new java.sql.Date(System.currentTimeMillis()).toString() + ".csv";

        DocumentHandler handler = handlers.get(name, MIME_TYPE);
        byte[] buffer = writer.getBuffer().toString().getBytes(Charset.forName("UTF-8"));
        return handler.create(name, new ByteArrayInputStream(buffer), MIME_TYPE, buffer.length);
    }

    /**
     * Writes a product.
     *
     * @param product the product to write
     * @param prices  the prices to write
     * @param from    the from date. May be {@code null}
     * @param to      the to date. May be {@code null}
     * @param writer  the writer to write to
     */
    private void write(Product product, Prices prices, Date from, Date to, CSVWriter writer) {
        IMObjectBean bean = new IMObjectBean(product);

        String productId = bean.getString("id");
        String name = bean.getString("name");

        List<ProductPrice> fixedPrices = getPrices(product, ProductArchetypes.FIXED_PRICE, prices, from, to);
        List<ProductPrice> unitPrices = getPrices(product, ProductArchetypes.UNIT_PRICE, prices, from, to);
        String printedName = bean.getString("printedName");

        int count = Math.max(fixedPrices.size(), unitPrices.size());
        if (count == 0) {
            count = 1;
        }
        for (int i = 0; i < count; ++i) {
            ProductPrice fixedPrice = i < fixedPrices.size() ? fixedPrices.get(i) : null;
            ProductPrice unitPrice = i < unitPrices.size() ? unitPrices.get(i) : null;
            String fixed = null;
            String fixedStartDate = null;
            String fixedEndDate = null;
            if (fixedPrice != null) {
                fixed = fixedPrice.getPrice().toString();
                fixedStartDate = getDate(fixedPrice.getFromDate());
                fixedEndDate = getDate(fixedPrice.getToDate());
            }
            String unit = null;
            String unitStartDate = unitPrice != null ? getDate(unitPrice.getFromDate()) : null;
            String unitEndDate = unitPrice != null ? getDate(unitPrice.getToDate()) : null;
            if (unitPrice != null) {
                unit = unitPrice.getPrice().toString();
                unitStartDate = getDate(unitPrice.getFromDate());
                unitEndDate = getDate(unitPrice.getToDate());
            }
            String[] line = {productId, name, printedName, fixed, fixedStartDate, fixedEndDate, unit, unitStartDate,
                             unitEndDate};
            writer.writeNext(line);
        }
    }

    /**
     * Helper to return a date as a string.
     *
     * @param date the date. May be {@code null}
     * @return the date as a string. May be {@code null}
     */
    private String getDate(Date date) {
        return (date != null) ? new java.sql.Date(date.getTime()).toString() : null;
    }

    /**
     * Returns prices matching some criteria.
     *
     * @param product   the product
     * @param shortName the price archetype short name
     * @param prices    the prices to return
     * @param from      the start date range, if prices is {@link Prices#RANGE}. May be {@code null}
     * @param to        the end date range, if prices is {@link Prices#RANGE}. May be {@code null}
     * @return the matching prices
     */
    private List<ProductPrice> getPrices(Product product, String shortName, Prices prices, Date from, Date to) {
        List<ProductPrice> result = new ArrayList<ProductPrice>();
        if (prices == Prices.LATEST) {
            ProductPrice price = rules.getProductPrice(product, shortName, new Date());
            if (price != null) {
                result.add(price);
            }
        } else if (prices == Prices.ALL) {
            result.addAll(rules.getProductPrices(product, shortName));
        } else {
            result.addAll(rules.getProductPrices(product, shortName, from, to));
        }
        if (result.size() > 1) {
            sort(result);
        }
        return result;
    }

    /**
     * Sorts prices.
     *
     * @param result the sorted prices
     */
    private void sort(List<ProductPrice> result) {
        Collections.sort(result, new Comparator<ProductPrice>() {
            @Override
            public int compare(ProductPrice o1, ProductPrice o2) {
                int result;
                if (ObjectUtils.equals(o1.getToDate(), o2.getToDate())) {
                    result = 0;
                } else if (o1.getToDate() == null) {
                    result = -1;
                } else if (o2.getToDate() == null) {
                    result = 1;
                } else {
                    result = DateRules.compareDates(o1.getToDate(), o2.getToDate());
                }
                if (result == 0) {
                    if (!ObjectUtils.equals(o1.getFromDate(), o2.getFromDate())) {
                        if (o1.getFromDate() == null) {
                            result = -1;
                        } else if (o2.getFromDate() == null) {
                            result = 1;
                        } else {
                            result = DateRules.compareDates(o1.getFromDate(), o2.getFromDate());
                        }
                    }
                }
                return result;
            }
        });
    }

}
