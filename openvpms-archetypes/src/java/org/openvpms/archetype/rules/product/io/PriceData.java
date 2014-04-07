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

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.Date;

import static org.openvpms.archetype.rules.product.ProductArchetypes.FIXED_PRICE;

/**
 * Price data.
 *
 * @author Tim Anderson
 */
public class PriceData {

    /**
     * The price identifier.
     */
    private final long id;

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
     * The maximum discount.
     */
    private BigDecimal maxDiscount;

    /**
     * The price start date. May be {@code null}
     */
    private Date from;

    /**
     * The price end date. May be {@code null}
     */
    private Date to;

    /**
     * Indicates if the price is the default price. Only applies to fixed prices.
     */
    private boolean defaultPrice;

    /**
     * The pricing groups.
     */
    private String[] pricingGroups;

    /**
     * The line that the price was read from.
     */
    private final int line;

    /**
     * Constructs a {@link PriceData}.
     *
     * @param id            the price identifier, or {@code -1} if it is a new price
     * @param shortName     the price archetype short name
     * @param price         the price
     * @param cost          the cost price
     * @param maxDiscount   the maximum discount
     * @param from          the price start date. May be {@code null}
     * @param to            the price end date. May be {@code null}
     * @param pricingGroups the pricing groups
     * @param line          the line that the price was read from
     */
    public PriceData(long id, String shortName, BigDecimal price, BigDecimal cost, BigDecimal maxDiscount, Date from,
                     Date to, String[] pricingGroups, int line) {
        this(id, shortName, price, cost, maxDiscount, from, to, false, pricingGroups, line);
    }

    /**
     * Constructs a {@link PriceData}.
     *
     * @param id            the price identifier, or {@code -1} if it is a new price
     * @param shortName     the price archetype short name
     * @param price         the price
     * @param cost          the cost price
     * @param maxDiscount   the maximum discount
     * @param from          the price start date. May be {@code null}
     * @param to            the price end date. May be {@code null}
     * @param defaultPrice  if {@code true}, indicates the price is the default price
     * @param pricingGroups the pricing groups
     * @param line          the line that the price was read from
     */
    public PriceData(long id, String shortName, BigDecimal price, BigDecimal cost, BigDecimal maxDiscount, Date from,
                     Date to, boolean defaultPrice, String[] pricingGroups, int line) {
        this.id = id;
        this.shortName = shortName;
        this.price = price;
        this.cost = cost;
        this.maxDiscount = maxDiscount;
        this.from = from;
        this.to = to;
        this.defaultPrice = defaultPrice;
        this.pricingGroups = pricingGroups;
        this.line = line;
    }

    /**
     * Constructs a {@link ProductData}.
     *
     * @param source  the source price to initialise from
     * @param service the archetype service
     */
    public PriceData(ProductPrice source, IArchetypeService service) {
        this.id = source.getId();
        this.shortName = source.getArchetypeId().getShortName();
        this.price = source.getPrice();
        IMObjectBean bean = new IMObjectBean(source, service);
        this.cost = bean.getBigDecimal("cost");
        this.maxDiscount = bean.getBigDecimal("maxDiscount");
        this.from = source.getFromDate();
        this.to = source.getToDate();
        this.defaultPrice = ProductIOHelper.isDefault(bean);
        this.pricingGroups = ProductIOHelper.getPricingGroups(source, service);
        this.line = -1;
    }

    /**
     * Returns the price identifier.
     *
     * @return the price id. If {@code -1}, indicates the price is a new price
     */
    public long getId() {
        return id;
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
     * Returns the price maximum discount.
     *
     * @return the maximum discount, expressed as a percentage
     */
    public BigDecimal getMaxDiscount() {
        return maxDiscount;
    }

    /**
     * Sets the price maximum discount.
     *
     * @param maxDiscount the maximum discount, expressed as a percentage
     */
    public void setMaxDiscount(BigDecimal maxDiscount) {
        this.maxDiscount = maxDiscount;
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
     * Sets the price start date.
     *
     * @param from the price start date. May be {@code null}
     */
    public void setFrom(Date from) {
        this.from = from;
    }

    /**
     * Returns the price end date
     *
     * @return the price end date. May be {@code null}
     */
    public Date getTo() {
        return to;
    }

    /**
     * Sets the price end date.
     *
     * @param to the price end date. May be {@code null}
     */
    public void setTo(Date to) {
        this.to = to;
    }

    /**
     * Determines if the price is the default price.
     * <p/>
     * This only applies to fixed prices.
     *
     * @return {@code true} if the price is the default price
     */
    public boolean isDefault() {
        return defaultPrice;
    }

    /**
     * Determines if the price is the default price.
     * <p/>
     * This only applies to fixed prices.
     *
     * @param defaultPrice if {@code true}, indicates that the price is the default price
     */
    public void setDefault(boolean defaultPrice) {
        this.defaultPrice = defaultPrice;
    }

    /**
     * Returns the pricing groups.
     *
     * @return the pricing groups
     */
    public String[] getPricingGroups() {
        return pricingGroups;
    }

    /**
     * Returns the line that the price was read from.
     *
     * @return the line
     */
    public int getLine() {
        return line;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof PriceData) {
            PriceData o = (PriceData) obj;
            return id == o.id && shortName.equals(o.shortName) && price.compareTo(o.price) == 0
                   && cost.compareTo(o.cost) == 0 && ObjectUtils.equals(from, o.from) && ObjectUtils.equals(to, o.to)
                   && (!FIXED_PRICE.equals(shortName) || defaultPrice == o.isDefault());
        }
        return false;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return (int) id ^ shortName.hashCode() ^ price.hashCode() ^ cost.hashCode();
    }

}
