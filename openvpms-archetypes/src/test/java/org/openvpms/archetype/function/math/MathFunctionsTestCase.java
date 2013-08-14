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

package org.openvpms.archetype.function.math;

import org.apache.commons.jxpath.JXPathContext;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.component.system.common.jxpath.JXPathHelper;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link MathFunctions} methods.
 *
 * @author Tim Anderson
 */
public class MathFunctionsTestCase {

    /**
     * The functions.
     */
    private MathFunctions math = new MathFunctions();

    /**
     * JXPath context used to evaluate expressions.
     */
    private JXPathContext ctx;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        // register the functions with JXPath.
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("math", math);
        new JXPathHelper(map);
        ctx = JXPathHelper.newContext(new Object());
    }

    /**
     * Tests the {@link MathFunctions#roundAmount(BigDecimal)} method.
     * <p/>
     * Note: this currently rounds to 2 decimal places, using HALF_UP rounding convention.
     */
    @Test
    public void testRoundAmount() {
        assertEquals(new BigDecimal("12.12"), math.roundAmount(new BigDecimal("12.123")));
        assertEquals(new BigDecimal("1.11"), math.roundAmount(new BigDecimal("1.105")));
        assertEquals(new BigDecimal("1.15"), math.roundAmount(new BigDecimal("1.145")));
        assertEquals(new BigDecimal("1.14"), math.roundAmount(new BigDecimal("1.144")));

        // test evaluation using JXPath
        assertEquals(new BigDecimal("1.12"), (BigDecimal) ctx.getValue("math:roundAmount(1.124)"));
        assertEquals(new BigDecimal("1.13"), (BigDecimal) ctx.getValue("math:roundAmount(1.125)"));
    }

    /**
     * Tests the {@link MathFunctions#round(BigDecimal, int)} method.
     */
    @Test
    public void testRound() {
        assertEquals(new BigDecimal("12.12"), math.round(new BigDecimal("12.123"), 2));
        assertEquals(new BigDecimal("1.11"), math.round(new BigDecimal("1.105"), 2));
        assertEquals(new BigDecimal("1.15"), math.round(new BigDecimal("1.145"), 2));
        assertEquals(new BigDecimal("1.14"), math.round(new BigDecimal("1.144"), 2));

        // test evaluation using JXPath
        assertEquals(new BigDecimal("1.12"), (BigDecimal) ctx.getValue("math:round(1.124, 2)"));
        assertEquals(new BigDecimal("1.13"), (BigDecimal) ctx.getValue("math:round(1.125, 2)"));
        assertEquals(new BigDecimal("3.143"), (BigDecimal) ctx.getValue("math:round(22 div 7, 3)"));
    }

    /**
     * Tests the {@link MathFunctions#pow} method.
     */
    @Test
    public void testPow() {
        assertEquals(BigDecimal.valueOf(16), math.pow(BigDecimal.valueOf(2), 4));
        assertEquals(new BigDecimal("9.8596"), math.pow(BigDecimal.valueOf(3.14), 2));

        // test evaluation using JXPath
        assertEquals(BigDecimal.valueOf(16), (BigDecimal) ctx.getValue("math:pow(2, 4)"));
        assertEquals(new BigDecimal("9.8596"), (BigDecimal) ctx.getValue("math:pow(3.14, 2)"));
    }

    /**
     * Helper to compare two {@code BigDecimal}s.
     *
     * @param expected the expected value
     * @param actual   the actual value
     */
    private static void assertEquals(BigDecimal expected, BigDecimal actual) {
        assertTrue(actual.compareTo(expected) == 0);
    }
}
