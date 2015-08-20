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

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.rules.math.MathRules.ONE_KILO_IN_POUNDS;
import static org.openvpms.archetype.rules.math.MathRules.ONE_POUND_IN_GRAMS;
import static org.openvpms.archetype.rules.math.MathRules.ONE_POUND_IN_KILOS;
import static org.openvpms.archetype.rules.math.MathRules.ONE_THOUSAND;
import static org.openvpms.archetype.rules.math.MathRules.convert;
import static org.openvpms.archetype.rules.math.WeightUnits.GRAMS;
import static org.openvpms.archetype.rules.math.WeightUnits.KILOGRAMS;
import static org.openvpms.archetype.rules.math.WeightUnits.POUNDS;

/**
 * Tests the {@link MathRules} class.
 *
 * @author Tim Anderson
 */
public class MathRulesTestCase {

    /**
     * Tests the {@link MathRules#convert(BigDecimal, WeightUnits, WeightUnits)} method.
     */
    @Test
    public void testConvert() {
        assertEquals(ZERO, convert(ZERO, KILOGRAMS, KILOGRAMS));
        assertEquals(ZERO, convert(ZERO, GRAMS, KILOGRAMS));
        assertEquals(ZERO, convert(ZERO, POUNDS, KILOGRAMS));

        // test Kg conversion
        assertEquals(ONE, convert(ONE, KILOGRAMS, KILOGRAMS));
        assertEquals(ONE_THOUSAND, convert(ONE, KILOGRAMS, GRAMS));
        assertEquals(ONE_KILO_IN_POUNDS, convert(ONE, KILOGRAMS, POUNDS));

        // test gram conversion
        assertEquals(ONE_THOUSAND, convert(ONE_THOUSAND, GRAMS, GRAMS));
        assertEquals(ONE, convert(ONE_THOUSAND, GRAMS, KILOGRAMS));
        assertEquals(ONE_KILO_IN_POUNDS, convert(ONE_THOUSAND, GRAMS, POUNDS));

        // test pound conversion
        assertEquals(ONE, convert(ONE, POUNDS, POUNDS));
        assertEquals(ONE_POUND_IN_KILOS, convert(ONE, POUNDS, KILOGRAMS));
        assertEquals(ONE_POUND_IN_GRAMS, convert(ONE, POUNDS, GRAMS));
    }

    /**
     * Tests the {@link MathRules#intersects(BigDecimal, BigDecimal, BigDecimal, BigDecimal)}.
     */
    @Test
    public void testIntersects() {
        // range1 before range2
        checkIntersects(false, "1", "10", "10", "20");
        checkIntersects(false, "1", "10", "11", "20");
        checkIntersects(false, "1", "10", "10", null);
        checkIntersects(false, null, "10", "10", "20");
        checkIntersects(false, null, "10", "11", "20");
        checkIntersects(false, null, "10", "10", null);
        checkIntersects(false, null, "10", "11", null);

        // range1 after range2
        checkIntersects(false, "10", "20", "1", "10");
        checkIntersects(false, "11", "20", "1", "10");
        checkIntersects(false, "10", null, "1", "10");
        checkIntersects(false, "11", null, "1", "10");
        checkIntersects(false, "10", "20", null, "10");
        checkIntersects(false, "10", null, null, "10");
        checkIntersects(false, "11", null, null, "10");

        // range1 overlaps start of range2
        checkIntersects(true, "1", "11", "10", "20");

        // range1 overlaps end of range2
        checkIntersects(true, "9", "20", "1", "10");

        // range1 == range2
        checkIntersects(true, "1", "20", "1", "20");

        // range1 within range2
        checkIntersects(true, "5", "6", "1", "20");
        checkIntersects(true, "1", "5", "1", "20");
        checkIntersects(true, "5", "20", "1", "20");

        // range2 within range1
        checkIntersects(true, "1", "20", "5", "6");
        checkIntersects(true, "1", "20", "1", "5");
        checkIntersects(true, "1", "20", "5", "20");
    }

    /**
     * Verifies that an unbounded numeric range intersects everything.
     */
    @Test
    public void testIntersectsForUnboundedNumericRange() {
        checkIntersects(true, null, null, null, null);
        checkIntersects(true, null, null, "1", null);
        checkIntersects(true, null, null, "1", "31");
        checkIntersects(true, null, null, null, "31");
        checkIntersects(true, "1", null, null, null);
        checkIntersects(true, "1", "31", null, null);
        checkIntersects(true, null, "31", null, null);
    }

    /**
     * Tests the {@link MathRules#intersects(BigDecimal, BigDecimal, BigDecimal, BigDecimal)}  method.
     *
     * @param intersects the expected result
     * @param from1      the start of the first date range. May be {@code null}
     * @param to1        the end of the first date range. May be {@code null}
     * @param from2      the start of the second date range. May be {@code null}
     * @param to2        the end of the second date range. May be {@code null}
     */
    private void checkIntersects(boolean intersects, String from1, String to1, String from2, String to2) {
        Assert.assertEquals(intersects, MathRules.intersects(
                getValue(from1), getValue(to1), getValue(from2), getValue(to2)));
    }

    /**
     * Helper to compare two {@code BigDecimal}s.
     *
     * @param expected the expected value
     * @param actual   the actual value
     */
    private void assertEquals(BigDecimal expected, BigDecimal actual) {
        assertTrue("Expected " + expected + ", but got " + actual, actual.compareTo(expected) == 0);
    }

    /**
     * Converts a string to a BigDecimal.
     *
     * @param value the value. May be {@code null}
     * @return the converted value or {@code null} if {@code value} is null
     */
    private BigDecimal getValue(String value) {
        return value != null ? new BigDecimal(value) : null;
    }
}
