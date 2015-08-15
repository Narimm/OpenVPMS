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

package org.openvpms.archetype.rules.math;

import java.math.BigDecimal;

/**
 * A weight value and the units it is expressed in.
 *
 * @author Tim Anderson
 */
public class Weight {

    /**
     * Zero weight.
     */
    public static final Weight ZERO = new Weight(BigDecimal.ZERO, WeightUnits.KILOGRAMS);

    /**
     * The weight.
     */
    private final BigDecimal weight;

    /**
     * The weight units
     */
    private final WeightUnits units;

    /**
     * Constructs a {@link Weight}, expressed in kilograms.
     *
     * @param weight the weight
     */
    public Weight(int weight) {
        this(BigDecimal.valueOf(weight), WeightUnits.KILOGRAMS);
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
        this.weight = weight;
        this.units = units;
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
     * Returns a string representation of the weight.
     *
     * @return a string representation of the weight
     */
    public String toString() {
        return weight + " " + units;
    }
}
