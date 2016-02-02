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

import org.junit.Test;

import java.math.BigDecimal;

import static java.math.BigDecimal.valueOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.rules.math.WeightUnits.GRAMS;
import static org.openvpms.archetype.rules.math.WeightUnits.KILOGRAMS;
import static org.openvpms.archetype.rules.math.WeightUnits.POUNDS;
import static org.openvpms.archetype.test.TestHelper.checkEquals;

/**
 * Tests the {@link Weight} class.
 *
 * @author Tim Anderson
 */
public class WeightTestCase {

    /**
     * Tests the {@link Weight#convert(WeightUnits)} and {@link Weight#toKilograms()} methods.
     */
    @Test
    public void testConvert() {
        Weight kg = new Weight(BigDecimal.TEN, KILOGRAMS);

        checkEquals(BigDecimal.TEN, kg.toKilograms());
        checkEquals(BigDecimal.TEN, kg.convert(KILOGRAMS));
        checkEquals(new BigDecimal("10000"), kg.convert(WeightUnits.GRAMS));
        checkEquals(new BigDecimal("22.04622622"), kg.convert(WeightUnits.POUNDS));

        Weight g = new Weight(BigDecimal.TEN, WeightUnits.GRAMS);
        checkEquals(new BigDecimal("0.01"), g.toKilograms());
        checkEquals(new BigDecimal("0.01"), g.convert(KILOGRAMS));
        checkEquals(BigDecimal.TEN, g.convert(WeightUnits.GRAMS));
        checkEquals(new BigDecimal("0.02204623"), g.convert(WeightUnits.POUNDS));

        Weight lb = new Weight(BigDecimal.TEN, WeightUnits.POUNDS);
        checkEquals(new BigDecimal("4.53592370"), lb.toKilograms());
        checkEquals(new BigDecimal("4.53592370"), lb.convert(KILOGRAMS));
        checkEquals(new BigDecimal("4535.9237"), lb.convert(WeightUnits.GRAMS));
        checkEquals(BigDecimal.TEN, lb.convert(WeightUnits.POUNDS));
    }

    /**
     * Tests the {@link Weight#isZero()} methods.
     */
    @Test
    public void testIsZero() {
        assertTrue(Weight.ZERO.isZero());
        Weight weight1 = new Weight(BigDecimal.TEN, WeightUnits.KILOGRAMS);
        Weight weight2 = new Weight(BigDecimal.ZERO, WeightUnits.KILOGRAMS);
        assertFalse(weight1.isZero());
        assertTrue(weight2.isZero());
    }

    /**
     * Tests the {@link Weight#between(BigDecimal, BigDecimal, WeightUnits)} method.
     */
    @Test
    public void testBetween() {
        Weight kg = new Weight(BigDecimal.ONE, KILOGRAMS);
        assertTrue(kg.between(valueOf(0.5), valueOf(2), KILOGRAMS));
        assertFalse(kg.between(valueOf(0), valueOf(1), KILOGRAMS));
        assertTrue(kg.between(valueOf(1), valueOf(2), KILOGRAMS));

        assertTrue(kg.between(valueOf(500), valueOf(2000), GRAMS));
        assertFalse(kg.between(valueOf(0), valueOf(1000), GRAMS));
        assertTrue(kg.between(valueOf(1000), valueOf(2000), GRAMS));

        assertTrue(kg.between(valueOf(2), valueOf(3), POUNDS));
        assertFalse(kg.between(valueOf(0), valueOf(2.20462), POUNDS));
        assertTrue(kg.between(valueOf(2.20462), valueOf(3), POUNDS));
    }

    /**
     * Tests the {@link Weight#compareTo(Weight)} method.
     */
    @Test
    public void testCompareTo() {
        Weight kg = new Weight(BigDecimal.ONE, KILOGRAMS);
        assertEquals(0, kg.compareTo(kg));

        Weight lb = kg.to(POUNDS);
        assertEquals(POUNDS, lb.getUnits());
        assertEquals(0, kg.compareTo(lb));
        assertEquals(0, lb.compareTo(kg));

        Weight kg2 = new Weight(2, KILOGRAMS);
        assertEquals(-1, lb.compareTo(kg2));
        assertEquals(1, kg2.compareTo(lb));
    }
}
