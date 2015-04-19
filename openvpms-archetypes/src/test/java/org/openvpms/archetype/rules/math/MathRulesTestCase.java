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
     * Helper to compare two {@code BigDecimal}s.
     *
     * @param expected the expected value
     * @param actual   the actual value
     */
    private void assertEquals(BigDecimal expected, BigDecimal actual) {
        assertTrue("Expected " + expected + ", but got " + actual, actual.compareTo(expected) == 0);
    }

}
