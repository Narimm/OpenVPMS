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

import java.math.BigDecimal;
import java.util.Date;

/**
 * Price data.
 *
 * @author Tim Anderson
 */
public class PriceData {

    /**
     * The price archetype short name.
     */
    private final String shortName;

    /**
     * The price.
     */
    private BigDecimal price;

    /**
     * The cost price.
     */
    private BigDecimal cost;

    /**
     * The price start date. May be {@code null}
     */
    private final Date from;

    /**
     * The price end date. May be {@code null}
     */
    private final Date to;

    /**
     * Constructs a {@link PriceData}.
     *
     * @param shortName the price archetype short name
     * @param price     the price
     * @param cost      the cost price
     * @param from      the price start date. May be {@code null}
     * @param to        the price end date. May be {@code null}
     */
    public PriceData(String shortName, BigDecimal price, BigDecimal cost, Date from, Date to) {
        this.shortName = shortName;
        this.price = price;
        this.cost = cost;
        this.from = from;
        this.to = to;
    }

    /**
     * Returns the price archetype short name.
     *
     * @return the price archetype short name.
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * Returns the price.
     *
     * @return the price
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Sets the price.
     *
     * @param price the price
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * Returns the cost price.
     *
     * @return the cost price
     */
    public BigDecimal getCost() {
        return cost;
    }

    /**
     * Sets the cost price.
     *
     * @param cost the cost price
     */
    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    /**
     * Returns the price start date.
     *
     * @return the price start date. May be {@code null}
     */
    public Date getFrom() {
        return from;
    }

    /**
     * Returns the price end date
     *
     * @return the price end date. May be {@code null}
     */
    public Date getTo() {
        return to;
    }
}
