/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.math;

import org.apache.commons.lang.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;


/**
 * Math rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
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
    public static BigDecimal divide(BigDecimal dividend, BigDecimal divisor,
                                    int scale) {
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
}
