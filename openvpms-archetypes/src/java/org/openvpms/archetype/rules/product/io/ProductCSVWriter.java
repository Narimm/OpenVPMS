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

import au.com.bytecode.opencsv.CSVWriter;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.finance.tax.TaxRules;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
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
            "Product Id", "Product Name", "Product Printed Name", "Fixed Price Id", "Fixed Price", "Fixed Cost",
            "Fixed Price Max Discount", "Fixed Price Start Date", "Fixed Price End Date", "Default Fixed Price",
            "Unit Price Id", "Unit Price", "Unit Cost", "Unit Price Max Discount", "Unit Price Start Date",
            "Unit Price End Date", "Tax Rate", "Notes"};

    /**
     * The mime type of the exported document.
     */
    public static final String MIME_TYPE = "text/csv";

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The product price rules.
     */
    private final ProductPriceRules rules;

    /**
     * The tax rules.
     */
    private final TaxRules taxRules;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * Field separator.
     */
    static final char SEPARATOR = ',';

    /**
     * The prices to write.
     */
    private enum Prices {
        ALL, LATEST, RANGE
    }

    /**
     * Constructs a {@link ProductCSVWriter}.
     *
     * @param service  the archetype service
     * @param rules    the price rules
     * @param taxRules the tax rules
     * @param handlers the document handlers
     */
    public ProductCSVWriter(IArchetypeService service, ProductPriceRules rules, TaxRules taxRules,
                            DocumentHandlers handlers) {
        this.service = service;
        this.rules = rules;
        this.taxRules = taxRules;
        this.handlers = handlers;
    }

    /**
     * Writes product data to a document.
     *
     * @param products            the products to write
     * @param latest              if {@code true}, output the latest price, else output all prices
     * @param includeLinkedPrices if {@code true} include prices linked from other products
     * @return the document
     */
    @Override
    public Document write(Iterator<Product> products, boolean latest, boolean includeLinkedPrices) {
        Prices prices = (latest) ? Prices.LATEST : Prices.ALL;
        return write(products, prices, null, null, includeLinkedPrices);
    }

    /**
     * Writes product data to a document.
     * <p/>
     * This writes prices active within a date range
     *
     * @param products            the products to write
     * @param from                the price start date. May be {@code null}
     * @param to                  the price end date. May be {@code null}
     * @param includeLinkedPrices if {@code true} include prices linked from other products
     * @return the document
     */
    @Override
    public Document write(Iterator<Product> products, Date from, Date to, boolean includeLinkedPrices) {
        return write(products, Prices.RANGE, from, to, includeLinkedPrices);
    }

    /**
     * Helper to return a date as a string.
     *
     * @param date the date. May be {@code null}
     * @return the date as a string. May be {@code null}
     */
    protected String getDate(Date date) {
        return (date != null) ? new java.sql.Date(date.getTime()).toString() : null;
    }

    /**
     * Writes product data to a document.
     *
     * @param products            the products to write
     * @param prices              determines which prices to write
     * @param from                the price start date. May be {@code null}
     * @param to                  the price end date. May be {@code null}
     * @param includeLinkedPrices if {@code true} include prices linked from other products
     * @return the document
     */
    private Document write(Iterator<Product> products, Prices prices, Date from, Date to, boolean includeLinkedPrices) {
        StringWriter writer = new StringWriter();
        CSVWriter csv = new CSVWriter(writer, SEPARATOR);
        csv.writeNext(HEADER);
        while (products.hasNext()) {
            write(products.next(), prices, from, to, includeLinkedPrices, csv);
        }
        String name = "products-" + new java.sql.Date(System.currentTimeMillis()).toString() + ".csv";

        DocumentHandler handler = handlers.get(name, MIME_TYPE);
        byte[] buffer = writer.getBuffer().toString().getBytes(Charset.forName("UTF-8"));
        return handler.create(name, new ByteArrayInputStream(buffer), MIME_TYPE, buffer.length);
    }

    /**
     * Writes a product.
     *
     * @param product             the product to write
     * @param prices              the prices to write
     * @param from                the from date. May be {@code null}
     * @param to                  the to date. May be {@code null}
     * @param includeLinkedPrices if {@code true} include prices linked from other products
     * @param writer              the writer to write to
     */
    private void write(Product product, Prices prices, Date from, Date to, boolean includeLinkedPrices,
                       CSVWriter writer) {
        IMObjectBean bean = new IMObjectBean(product);

        String productId = bean.getString("id");
        String name = bean.getString("name");

        List<ProductPrice> fixedPrices = getPrices(product, ProductArchetypes.FIXED_PRICE, prices, from, to,
                                                   includeLinkedPrices);
        List<ProductPrice> unitPrices = getPrices(product, ProductArchetypes.UNIT_PRICE, prices, from, to,
                                                  includeLinkedPrices);
        String printedName = bean.getString("printedName");
        String tax = taxRules.getTaxRate(product).toString();

        int count = Math.max(fixedPrices.size(), unitPrices.size());
        if (count == 0) {
            count = 1;
        }
        for (int i = 0; i < count; ++i) {
            ProductPrice fixedPrice = i < fixedPrices.size() ? fixedPrices.get(i) : null;
            ProductPrice unitPrice = i < unitPrices.size() ? unitPrices.get(i) : null;
            String fixedId = null;
            String fixed = null;
            String fixedCost = null;
            String fixedMaxDiscount = null;
            String fixedStartDate = null;
            String fixedEndDate = null;
            String defaultFixedPrice = null;
            String notes = null;
            if (fixedPrice != null) {
                IMObjectBean fixedBean = new IMObjectBean(fixedPrice, service);
                fixedId = fixedBean.getString("id");
                fixed = fixedPrice.getPrice().toString();
                fixedCost = fixedBean.getBigDecimal("cost", BigDecimal.ZERO).toString();
                fixedMaxDiscount = fixedBean.getBigDecimal("maxDiscount", MathRules.ONE_HUNDRED).toString();
                fixedStartDate = getDate(fixedPrice.getFromDate());
                fixedEndDate = getDate(fixedPrice.getToDate());
                defaultFixedPrice = fixedBean.getString("default", "false").toLowerCase();
                if (!ObjectUtils.equals(fixedPrice.getProduct(), product)) {
                    // TODO - hack to format message
                    notes = new ProductIOException(ProductIOException.ErrorCode.LinkedPrice, -1,
                                                   fixedPrice.getProduct().getName(),
                                                   fixedPrice.getProduct().getId()).getMessage();
                }
            }
            String unitId = null;
            String unit = null;
            String unitCost = null;
            String unitMaxDiscount = null;
            String unitStartDate = null;
            String unitEndDate = null;
            if (unitPrice != null) {
                IMObjectBean unitBean = new IMObjectBean(unitPrice, service);
                unitId = unitBean.getString("id");
                unit = unitPrice.getPrice().toString();
                unitCost = unitBean.getBigDecimal("cost", BigDecimal.ZERO).toString();
                unitMaxDiscount = unitBean.getBigDecimal("maxDiscount", MathRules.ONE_HUNDRED).toString();
                unitStartDate = getDate(unitPrice.getFromDate());
                unitEndDate = getDate(unitPrice.getToDate());
            }
            String[] line = {productId, name, printedName, fixedId, fixed, fixedCost, fixedMaxDiscount, fixedStartDate,
                             fixedEndDate, defaultFixedPrice, unitId, unit, unitCost, unitMaxDiscount, unitStartDate,
                             unitEndDate, tax, notes};
            writer.writeNext(line);
        }
    }

    /**
     * Returns prices matching some criteria.
     *
     * @param product             the product
     * @param shortName           the price archetype short name
     * @param prices              the prices to return
     * @param from                the start date range, if prices is {@link Prices#RANGE}. May be {@code null}
     * @param to                  the end date range, if prices is {@link Prices#RANGE}. May be {@code null}
     * @param includeLinkedPrices if {@code true} include prices linked from other products
     * @return the matching prices
     */
    private List<ProductPrice> getPrices(Product product, String shortName, Prices prices, Date from, Date to,
                                         boolean includeLinkedPrices) {
        List<ProductPrice> result = new ArrayList<ProductPrice>();
        if (prices == Prices.LATEST) {
            List<ProductPrice> list = rules.getProductPrices(product, shortName, includeLinkedPrices);
            if (!list.isEmpty()) {
                result.add(list.get(0));
            }
        } else if (prices == Prices.ALL) {
            result.addAll(rules.getProductPrices(product, shortName, includeLinkedPrices));
        } else {
            result.addAll(rules.getProductPrices(product, shortName, from, to, includeLinkedPrices));
        }
        return result;
    }


}
