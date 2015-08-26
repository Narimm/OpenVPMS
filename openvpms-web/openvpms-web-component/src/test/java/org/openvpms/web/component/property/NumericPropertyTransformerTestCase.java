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

package org.openvpms.web.component.property;

import org.junit.Test;
import org.openvpms.archetype.test.TestHelper;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * {@link NumericPropertyTransformer} test case.
 *
 * @author Tim Anderson
 */
public class NumericPropertyTransformerTestCase {

    /**
     * Tests {@link NumericPropertyTransformer#apply} for an integer node.
     */
    @Test
    public void testIntegerApply() {
        SimpleProperty property = new SimpleProperty("int", Integer.class);
        NumericPropertyTransformer handler = new NumericPropertyTransformer(property);

        final Integer one = 1;

        // test string conversions
        try {
            handler.apply("abc");
            fail("expected conversion from 'abc' to fail");
        } catch (PropertyException expected) {
            assertEquals(property, expected.getProperty());
        }

        Integer int1 = (Integer) handler.apply("1");
        assertEquals(one, int1);

        try {
            handler.apply("1.0");
            fail("expected conversion from '1.0' to fail");
        } catch (PropertyException expected) {
            // not supported by Integer.parseInt()
            assertEquals(property, expected.getProperty());
        }

        // test numeric conversions
        assertEquals(one, handler.apply(new Long(1)));
        assertEquals(one, handler.apply(new BigDecimal(1.0)));
        assertEquals(one, handler.apply(new Double(1.5)));
    }

    /**
     * Tests {@link NumericPropertyTransformer#apply} for a BigDecimal node.
     */
    @Test
    public void testDecimalApply() {
        final BigDecimal one = new BigDecimal("1.0");
        final BigDecimal half = new BigDecimal("0.5");

        SimpleProperty property = new SimpleProperty("dec", BigDecimal.class);
        NumericPropertyTransformer handler = new NumericPropertyTransformer(property);

        // test string conversions
        try {
            handler.apply("abc");
        } catch (PropertyException expected) {
            assertEquals(property, expected.getProperty());
        }

        // Note: BigDecimal.compareTo() is used instead of equals as equals
        // considers equal values with different scales to be different.
        BigDecimal dec1 = (BigDecimal) handler.apply("0.5");
        assertTrue(half.compareTo(dec1) == 0);

        BigDecimal dec2 = (BigDecimal) handler.apply("1.0");
        assertTrue(one.compareTo(dec2) == 0);

        // test numeric conversions
        BigDecimal dec3 = (BigDecimal) handler.apply(new Long(1));
        assertTrue(one.compareTo(dec3) == 0);

        BigDecimal dec4 = (BigDecimal) handler.apply(new BigDecimal(0.5));
        assertTrue(half.compareTo(dec4) == 0);

        BigDecimal dec5 = (BigDecimal) handler.apply(new Double(0.5));
        assertTrue(half.compareTo(dec5) == 0);
    }

    /**
     * Checks validation of number properties with {@code positive == true}.
     */
    @Test
    public void testPositive() {
        SimpleProperty property = new SimpleProperty("int", Integer.class);
        NumericPropertyTransformer handler = new NumericPropertyTransformer(property, true);

        try {
            handler.apply("-1");
        } catch (PropertyException expected) {
            assertEquals(property, expected.getProperty());
            assertEquals("Int must be >= 0", expected.getMessage());
        }

        Integer int1 = (Integer) handler.apply("1");
        assertEquals(new Integer(1), int1);

        Integer zero = (Integer) handler.apply(0);
        assertEquals(new Integer(0), zero);
    }

    /**
     * Verifies that an optional numeric can be left empty, but if specified it must meet its assertions.
     */
    @Test
    public void testOptionalNumeric() {
        SimpleProperty property = new SimpleProperty("concentration", BigDecimal.class);
        property.setRequired(false);
        NumericPropertyTransformer handler = new NumericPropertyTransformer(property, true);

        assertNull(handler.apply(""));
        assertNull(handler.apply(null));
        try {
            handler.apply("-1");
        } catch (PropertyException expected) {
            assertEquals(property, expected.getProperty());
            assertEquals("Concentration must be >= 0", expected.getMessage());
        }

        TestHelper.checkEquals(BigDecimal.ONE, (BigDecimal) handler.apply("1"));
    }

}
