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

package org.openvpms.web.component.property;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;


/**
 * {@link IntegerPropertyTransformer} test case.
 *
 * @author Tim Anderson
 */
public class IntegerPropertyTransformerTestCase {

    /**
     * Tests {@link IntegerPropertyTransformer#apply} for an integer node.
     */
    @Test
    public void testIntegerApply() {
        SimpleProperty property = new SimpleProperty("int", Integer.class);
        IntegerPropertyTransformer handler = new IntegerPropertyTransformer(property);

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
            // not supported by Integer.valueOf()
            assertEquals(property, expected.getProperty());
        }

        // test numeric conversions
        assertEquals(one, handler.apply(new Long(1)));
        assertEquals(one, handler.apply(new BigDecimal(1.0)));
        assertEquals(one, handler.apply(new Double(1.5)));
    }

    /**
     * Checks validation of integers with a range.
     */
    @Test
    public void testRange() {
        SimpleProperty property = new SimpleProperty("int", Integer.class);
        IntegerPropertyTransformer handler = new IntegerPropertyTransformer(property, -1, 10);

        try {
            handler.apply("-2");
        } catch (PropertyException expected) {
            assertEquals(property, expected.getProperty());
            assertEquals("Int is less than -1", expected.getMessage());
        }

        try {
            handler.apply("11");
        } catch (PropertyException expected) {
            assertEquals(property, expected.getProperty());
            assertEquals("Int is greater than 10", expected.getMessage());
        }

        Integer int1 = (Integer) handler.apply("1");
        assertEquals(new Integer(1), int1);

        Integer zero = (Integer) handler.apply(0);
        assertEquals(new Integer(0), zero);
    }

    /**
     * Verifies that an optional integer can be left empty, but if specified it must meet its assertions.
     */
    @Test
    public void testOptionalInteger() {
        SimpleProperty property = new SimpleProperty("count", Integer.class);
        property.setRequired(false);
        IntegerPropertyTransformer handler = new IntegerPropertyTransformer(property, 0, null);

        assertNull(handler.apply(""));
        assertNull(handler.apply(null));
        try {
            handler.apply("-1");
        } catch (PropertyException expected) {
            assertEquals(property, expected.getProperty());
            assertEquals("Count is less than 0", expected.getMessage());
        }

        assertEquals(1, handler.apply("1"));
    }

}
