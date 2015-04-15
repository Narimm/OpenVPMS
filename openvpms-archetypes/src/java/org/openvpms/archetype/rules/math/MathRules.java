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

package org.openvpms.archetype.rules.math;

import org.apache.commons.lang.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static java.math.BigDecimal.ROUND_HALF_UP;

/**
 * Math rules.
 *
 * @author Tim Anderson
 */
public class MathRules {

    /**
     * One hundred.
     */
    public static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    /**
     * One thousand.
     */
    public static final BigDecimal ONE_THOUSAND = BigDecimal.valueOf(1000);

    /**
     * One pound, in kilograms.
     */
    public static final BigDecimal ONE_POUND_IN_KILOS = new BigDecimal("0.45359237");

    /**
     * One pound in grams.
     */
    public static final BigDecimal ONE_POUND_IN_GRAMS = ONE_POUND_IN_KILOS.multiply(ONE_THOUSAND);

    /**
     * One kilo in pounds.
     */
    public static final BigDecimal ONE_KILO_IN_POUNDS = BigDecimal.ONE.divide(ONE_POUND_IN_KILOS, 8, ROUND_HALF_UP);

    /**
     * One gram in pounds.
     */
    public static final BigDecimal ONE_GRAM_IN_POUNDS = BigDecimal.ONE.divide(ONE_POUND_IN_GRAMS, 8, ROUND_HALF_UP);

    /**
     * Rounds a value to the default no. of decimal places.
     * todo - could be configurable via a properies file
     *
     * @param value the value
     * @return the rounded value
     */
    public static BigDecimal round(BigDecimal value) {
        return round(value, 2);
    }

    /**
     * Rounds a value.
     *
     * @param value the value
     * @param scale the no. of decimal places
     * @return the rounded value
     */
    public static BigDecimal round(BigDecimal value, int scale) {
        return value.setScale(scale, RoundingMode.HALF_UP);
    }

    /**
     * Performs a division, rounding the result to the specified no. of places.
     *
     * @param dividend the value to divide
     * @param divisor  the divisor
     * @param scale    the no. of decimal places
     * @return the divided value
     */
    public static BigDecimal divide(BigDecimal dividend, BigDecimal divisor, int scale) {
        return dividend.divide(divisor, scale, RoundingMode.HALF_UP);
    }

    /**
     * Performs a division, rounding the result to the specified no. of places.
     *
     * @param dividend the value to divide
     * @param divisor  the divisor
     * @param scale    the no. of decimal places
     * @return the divided value
     */
    public static BigDecimal divide(BigDecimal dividend, int divisor, int scale) {
        return divide(dividend, BigDecimal.valueOf(divisor), scale);
    }

    /**
     * Helper to determine if two decimals are equal.
     *
     * @param lhs the left-hand side. May be <tt>null</tt>
     * @param rhs right left-hand side. May be <tt>null</tt>
     * @return <tt>true</t> if they are equal, otherwise <tt>false</tt>
     */
    public static boolean equals(BigDecimal lhs, BigDecimal rhs) {
        if (lhs != null && rhs != null) {
            return lhs.compareTo(rhs) == 0;
        }
        return ObjectUtils.equals(lhs, rhs);
    }

    /**
     * Converts a weight from one unit to another.
     *
     * @param weight the weight to convert
     * @param from   the units to convert from
     * @param to     the units to convert to
     * @return the converted weight
     */
    public static BigDecimal convert(BigDecimal weight, WeightUnits from, WeightUnits to) {
        if (weight.compareTo(BigDecimal.ZERO) == 0 || from == to) {
            return weight;
        } else if (from == WeightUnits.KILOGRAMS) {
            if (to == WeightUnits.GRAMS) {
                return weight.multiply(ONE_THOUSAND);
            } else if (to == WeightUnits.POUNDS) {
                return divide(weight, ONE_POUND_IN_KILOS, 8);
            }
        } else if (from == WeightUnits.GRAMS) {
            if (to == WeightUnits.KILOGRAMS) {
                return divide(weight, ONE_THOUSAND, 2);
            } else if (to == WeightUnits.POUNDS) {
                return divide(weight, ONE_POUND_IN_GRAMS, 8);
            }
        } else if (from == WeightUnits.POUNDS) {
            if (to == WeightUnits.KILOGRAMS) {
                return weight.multiply(ONE_POUND_IN_KILOS);
            } else if (to == WeightUnits.GRAMS) {
                return weight.multiply(ONE_POUND_IN_GRAMS);
            }
        } else {
            throw new IllegalArgumentException("Unsupported weight units for argument 'from': " + from);
        }
        throw new IllegalArgumentException("Unsupported weight units for argument 'to': " + to);
    }

}
