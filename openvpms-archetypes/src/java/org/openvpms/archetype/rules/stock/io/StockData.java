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

import org.openvpms.component.business.domain.im.common.IMObjectReference;

import java.math.BigDecimal;

/**
 * Stock data used by {@link StockCSVReader}.
 *
 * @author Tim Anderson
 */
public class StockData {

    /**
     * The stock location identifier.
     */
    private final long stockLocationId;

    /**
     * The stock location name.
     */
    private final String stockLocationName;

    /**
     * The stock location reference.
     */
    private IMObjectReference stockLocation;

    /**
     * The product identifier.
     */
    private final long productId;

    /**
     * The product name.
     */
    private final String productName;

    /**
     * The product reference.
     */
    private IMObjectReference product;

    /**
     * The selling units.
     */
    private final String sellingUnits;

    /**
     * The current stock quantity.
     */
    private final BigDecimal quantity;

    /**
     * The new stock quantity.
     */
    private final BigDecimal newQuantity;

    /**
     * The line that the stock data was read from.
     */
    private final int line;

    /**
     * Processing error message.
     */
    private String error;

    /**
     * Constructs a {@link StockData}.
     *
     * @param stockLocationId   the stock location identifier
     * @param stockLocationName the stock location name
     * @param productId         the product identifier
     * @param productName       the product name
     * @param sellingUnits      the product selling units
     * @param quantity          the current stock quantity
     * @param newQuantity       the new stock quantity
     */
    public StockData(long stockLocationId, String stockLocationName, long productId, String productName,
                     String sellingUnits, BigDecimal quantity, BigDecimal newQuantity) {
        this(stockLocationId, stockLocationName, productId, productName, sellingUnits, quantity, newQuantity, -1);
    }

    /**
     * Constructs a {@link StockData}.
     *
     * @param stockLocationId   the stock location identifier
     * @param stockLocationName the stock location name
     * @param productId         the product identifier
     * @param productName       the product name
     * @param sellingUnits      the product selling units
     * @param quantity          the current stock on hand quantity
     * @param newQuantity       the new stock quantity
     * @param line              the line the stock data was read from, or {@code -1} if it is unknown
     */
    public StockData(long stockLocationId, String stockLocationName, long productId, String productName,
                     String sellingUnits, BigDecimal quantity, BigDecimal newQuantity, int line) {
        this.stockLocationId = stockLocationId;
        this.stockLocationName = stockLocationName;
        this.productId = productId;
        this.productName = productName;
        this.sellingUnits = sellingUnits;
        this.quantity = quantity;
        this.newQuantity = newQuantity;
        this.line = line;
    }

    /**
     * Returns the stock location identifier.
     *
     * @return the stock location id
     */
    public long getStockLocationId() {
        return stockLocationId;
    }

    /**
     * Returns the stock location name
     *
     * @return the stock location name
     */
    public String getStockLocationName() {
        return stockLocationName;
    }

    /**
     * Sets the stock location reference.
     *
     * @param stockLocation the stock location reference
     */
    public void setStockLocation(IMObjectReference stockLocation) {
        this.stockLocation = stockLocation;
    }

    /**
     * Returns the stock location reference.
     *
     * @return the stock location reference
     */
    public IMObjectReference getStockLocation() {
        return stockLocation;
    }

    /**
     * Returns the product identifier.
     *
     * @return the product identifier
     */
    public long getProductId() {
        return productId;
    }

    /**
     * Returns the product name.
     *
     * @return the product name
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Sets the product reference.
     *
     * @param reference the reference. May be {@code null}
     */
    public void setProduct(IMObjectReference reference) {
        product = reference;
    }

    /**
     * Returns the product reference.
     *
     * @return the product reference. May be {@code null}
     */
    public IMObjectReference getProduct() {
        return product;
    }

    /**
     * Returns the product selling units.
     *
     * @return the product selling units
     */
    public String getSellingUnits() {
        return sellingUnits;
    }

    /**
     * Returns the current quantity.
     *
     * @return the current quantity
     */
    public BigDecimal getQuantity() {
        return quantity;
    }

    /**
     * Returns the new quantity.
     *
     * @return the new quantity
     */
    public BigDecimal getNewQuantity() {
        return newQuantity;
    }

    /**
     * Returns the line that the stock data was read from.
     *
     * @return the line, or {@code -1} if it is not known
     */
    public int getLine() {
        return line;
    }

    /**
     * Sets an error message to indicate that the stock data is invalid.
     *
     * @param error the error message
     */
    public void setError(String error) {
        this.error = error;
    }

    /**
     * Returns any error message generated while processing the product.
     *
     * @return the error message, or {@code null} if there is no error
     */
    public String getError() {
        return error;
    }

}
