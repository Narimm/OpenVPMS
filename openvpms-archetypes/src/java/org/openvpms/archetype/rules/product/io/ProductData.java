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

import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.component.business.domain.im.common.IMObjectReference;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Product data used by {@link ProductReader}.
 *
 * @author Tim Anderson
 */
public class ProductData {

    /**
     * The product identifier.
     */
    private final long id;

    /**
     * The product name.
     */
    private final String name;

    /**
     * The printed name. May be {@code null}
     */
    private String printedName;

    /**
     * The tax rate, expressed as a percentage.
     */
    private BigDecimal taxRate;

    /**
     * Line that the data was read from.
     */
    private final int line;

    /**
     * The fixed prices.
     */
    private List<PriceData> fixedPrices = new ArrayList<PriceData>();

    /**
     * The unit prices.
     */
    private List<PriceData> unitPrices = new ArrayList<PriceData>();

    /**
     * Reference to an existing product.
     */
    private IMObjectReference reference;

    /**
     * Processing error message.
     */
    private String error;

    /**
     * Error line.
     */
    private int errorLine;

    /**
     * Constructs a {@link ProductData}.
     *
     * @param id          the product identifier
     * @param name        the product name
     * @param printedName the product printed name. May be {@code null}
     * @param taxRate     the tax rate, expressed as a percentage
     * @param line        the line the data came from
     */
    public ProductData(long id, String name, String printedName, BigDecimal taxRate, int line) {
        this.id = id;
        this.name = name;
        this.printedName = printedName;
        this.taxRate = taxRate;
        this.line = line;
    }

    /**
     * Constructs a {@link ProductData}.
     *
     * @param source the source product data to copy from. Prices are not copied.
     */
    public ProductData(ProductData source) {
        this.id = source.getId();
        this.name = source.getName();
        this.printedName = source.getPrintedName();
        this.taxRate = source.getTaxRate();
        this.line = source.getLine();
        this.reference = source.getReference();
    }

    /**
     * Returns the product identifier.
     *
     * @return the product identifier
     */
    public long getId() {
        return id;
    }

    /**
     * Returns the product reference.
     *
     * @return the product reference. May be {@code null}
     */
    public IMObjectReference getReference() {
        return reference;
    }

    /**
     * Sets the product reference.
     *
     * @param reference the product reference. May be {@code null}
     */
    public void setReference(IMObjectReference reference) {
        this.reference = reference;
    }

    /**
     * Returns the product name.
     *
     * @return the product name. May be {@code null}
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the product printed name
     *
     * @return the product printed name. May be {@code null}
     */
    public String getPrintedName() {
        return printedName;
    }

    /**
     * Sets the product printed name.
     *
     * @param printedName the printed name. May be {@code null}
     */
    public void setPrintedName(String printedName) {
        this.printedName = printedName;
    }

    /**
     * Returns the tax rate.
     *
     * @return the tax rate, expressed as a percentage. May be {@code null}
     */
    public BigDecimal getTaxRate() {
        return taxRate;
    }

    /**
     * Adds a price.
     *
     * @param price the price to add
     */
    public void addPrice(PriceData price) {
        if (ProductArchetypes.FIXED_PRICE.equals(price.getShortName())) {
            fixedPrices.add(price);
        } else {
            unitPrices.add(price);
        }
    }

    /**
     * Adds a fixed price.
     *
     * @param id           the price identifier, or {@code -1} if it is a new price
     * @param price        the price
     * @param cost         the cost price
     * @param maxDiscount  the maximum discount
     * @param from         the price start date. May be {@code null}
     * @param to           the price end date. May be {@code null}
     * @param defaultPrice {@code true} if the price is the default
     * @param groups       the pricing group codes
     * @param line         the line the price was read from
     */
    public void addFixedPrice(long id, BigDecimal price, BigDecimal cost, BigDecimal maxDiscount, Date from, Date to,
                              boolean defaultPrice, String[] groups, int line) {
        fixedPrices.add(new PriceData(id, ProductArchetypes.FIXED_PRICE, price, cost, maxDiscount, from, to,
                                      defaultPrice, groups, line));
    }

    /**
     * Adds a unit price.
     *
     * @param id          the price identifier, or {@code -1} if it is a new price
     * @param price       the price
     * @param cost        the cost price
     * @param maxDiscount the maximum discount
     * @param from        the price start date. May be {@code null}
     * @param to          the price end date. May be {@code null}
     * @param groups      the pricing group codes
     * @param line        the line the price was read from
     */
    public void addUnitPrice(long id, BigDecimal price, BigDecimal cost, BigDecimal maxDiscount, Date from, Date to,
                             String[] groups, int line) {
        unitPrices.add(new PriceData(id, ProductArchetypes.UNIT_PRICE, price, cost, maxDiscount, from, to, groups,
                                     line));
    }

    /**
     * Returns the fixed prices.
     *
     * @return the fixed prices
     */
    public List<PriceData> getFixedPrices() {
        return fixedPrices;
    }

    /**
     * Returns the unit prices.
     *
     * @return the unit prices
     */
    public List<PriceData> getUnitPrices() {
        return unitPrices;
    }

    /**
     * Returns the line that the product data was read from.
     *
     * @return the line
     */
    public int getLine() {
        return line;
    }

    /**
     * Returns any error message generated while processing the product.
     *
     * @return the error message, or {@code null} if there is no error
     */
    public String getError() {
        return error;
    }

    /**
     * Sets an error message to indicate that the product is invalid.
     *
     * @param error the error message. May be {@code null}
     * @param line  the line that the error occurred on
     */
    public void setError(String error, int line) {
        this.error = error;
        this.errorLine = line;
    }

    /**
     * Returns the line that a processing error occurred on.
     *
     * @return the error line
     */
    public int getErrorLine() {
        return errorLine;
    }
}
