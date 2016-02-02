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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.math;

import org.apache.commons.lang.builder.HashCodeBuilder;

import java.math.BigDecimal;
import java.util.Date;

/**
 * A weight value and the units it is expressed in.
 * <p/>
 * Weights have an optional date, indicating when the weight was taken.
 *
 * @author Tim Anderson
 */
public class Weight implements Comparable<Weight> {

    /**
     * Zero weight.
     */
    public static final Weight ZERO = new Weight(BigDecimal.ZERO, WeightUnits.KILOGRAMS);

    /**
     * The weight.
     */
    private final BigDecimal weight;

    /**
     * The weight units.
     */
    private final WeightUnits units;

    /**
     * The date/time when the weight was taken.
     */
    private Date date;

    /**
     * Constructs a {@link Weight}, expressed in kilograms.
     *
     * @param weight the weight
     */
    public Weight(int weight) {
        this(weight, WeightUnits.KILOGRAMS);
    }

    /**
     * Constructs a {@link Weight}, expressed in kilograms.
     *
     * @param weight the weight
     */
    public Weight(BigDecimal weight) {
        this(weight, WeightUnits.KILOGRAMS);
    }

    /**
     * Constructs a {@link Weight}.
     *
     * @param weight the weight
     * @param units  the weight units
     */
    public Weight(BigDecimal weight, WeightUnits units) {
        this(weight, units, null);
    }

    /**
     * Constructs a {@link Weight}.
     *
     * @param weight the weight
     * @param units  the weight units
     */
    public Weight(int weight, WeightUnits units) {
        this(BigDecimal.valueOf(weight), units);
    }

    /**
     * Constructs a {@link Weight}.
     *
     * @param weight the weight
     * @param units  the weight units
     * @param date   the date/time when the weight was taken. May be {@code null}
     */
    public Weight(BigDecimal weight, WeightUnits units, Date date) {
        this.weight = weight;
        this.units = units;
        this.date = date;
    }

    /**
     * Returns the weight.
     *
     * @return the weight
     */
    public BigDecimal getWeight() {
        return weight;
    }

    /**
     * Determines if the weight is zero.
     *
     * @return {@code true} if the weight is zero
     */
    public boolean isZero() {
        return MathRules.isZero(weight);
    }

    /**
     * Returns the weight units.
     *
     * @return the weight units
     */
    public WeightUnits getUnits() {
        return units;
    }

    /**
     * Returns the date/time when the weight was taken.
     *
     * @return the date/time, or {@code null} if it is not known
     */
    public Date getDate() {
        return date;
    }

    /**
     * Converts the weight to kilograms.
     *
     * @return the weight in kilograms
     */
    public BigDecimal toKilograms() {
        return convert(WeightUnits.KILOGRAMS);
    }

    /**
     * Converts the weight to the specified units.
     *
     * @param to the units to convert to
     * @return the converted weight
     */
    public BigDecimal convert(WeightUnits to) {
        return MathRules.convert(weight, units, to);
    }

    /**
     * Converts the weight to the specified units.
     *
     * @param to the units to convert to
     * @return the converted weight
     */
    public Weight to(WeightUnits to) {
        return new Weight(convert(to), to, date);
    }

    /**
     * Determines if the weight falls between the specified values.
     *
     * @param lower the lower bound, inclusive
     * @param upper the upper bound, exclusive
     * @param units the units the lower and upper bound are expressed in
     * @return {@code true} if the weight falls between the specified values
     */
    public boolean between(BigDecimal lower, BigDecimal upper, WeightUnits units) {
        BigDecimal converted = convert(units);
        return lower.compareTo(converted) <= 0 && upper.compareTo(converted) > 0;
    }

    /**
     * Compares this object with the specified object for order.  Returns a negative integer, zero, or a positive
     * integer as this object is less than, equal to, or greater than the specified object.
     * <p/>
     * NOTE: this does not compare dates.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     * the specified object.
     */
    @Override
    public int compareTo(Weight o) {
        BigDecimal converted = o.convert(units);
        return weight.compareTo(converted);
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * <p/>
     * NOTE: this does not compare dates.
     *
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Weight && compareTo((Weight) obj) == 0;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(weight).append(units).hashCode();
    }

    /**
     * Returns a string representation of the weight.
     *
     * @return a string representation of the weight
     */
    public String toString() {
        return weight + " " + units;
    }
}
