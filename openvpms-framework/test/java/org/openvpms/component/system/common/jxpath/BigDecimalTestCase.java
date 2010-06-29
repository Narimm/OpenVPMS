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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.component.system.common.jxpath;

import org.apache.commons.jxpath.JXPathContext;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.MathContext;


/**
 * Tests that BigDecimals are used for numeric operations in jxpath.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class BigDecimalTestCase {

    /**
     * The test context.
     */
    private JXPathContext context;

    /**
     * Verifies that a BigDecimal is used to represent numeric values.
     */
    @Test
    public void testConstant() {
        Object value = context.getValue("1.0");
        assertTrue(value instanceof BigDecimal);
    }

    /**
     * Verifies that BigDecimals are used to add two values.
     */
    @Test
    public void testAdd() {
        double doubleValue = 0.7 + 0.1;
        BigDecimal value = (BigDecimal) context.getValue("0.7 + 0.1");
        assertFalse(value.compareTo(new BigDecimal(doubleValue)) == 0);
        checkEquals(new BigDecimal("0.8"), value);
    }

    /**
     * Verifies that BigDecimals are used to subtract one value from another.
     */
    @Test
    public void testSubtract() {
        double doubleValue = 0.8 - 0.1;  // can't be represented accurately
        BigDecimal value = (BigDecimal) context.getValue("0.8 - 0.1");
        assertFalse(value.compareTo(new BigDecimal(doubleValue)) == 0);
        checkEquals(new BigDecimal("0.7"), value);
    }

    /**
     * Verifies that BigDecimals are used to multiply two values.
     */
    @Test
    public void testMultiply() {
        double doubleValue = 0.1 * 0.1;    // can't be represented accurately
        BigDecimal expectedValue = new BigDecimal("0.01");
        assertFalse(new BigDecimal(doubleValue).compareTo(expectedValue) == 0);
        BigDecimal value = (BigDecimal) context.getValue("0.1 * 0.1");
        checkEquals(new BigDecimal("0.01"), value);
    }

    /**
     * Verifies that BigDecimals are used to divide one value by another.
     */
    @Test
    public void testDivide() {
        BigDecimal value = (BigDecimal) context.getValue("1 div 3");
        BigDecimal expected = new BigDecimal(1).divide(new BigDecimal(3), MathContext.DECIMAL128);
        checkEquals(expected, value);
    }

    /**
     * Verifies that BigDecimals are used to compare floating point numbers for equality.
     */
    @Test
    public void testEquals() {
        assertEquals(0.1, 0.100000000000000001, 0.0); // note truncation
        assertEquals(Boolean.TRUE, context.getValue("0.1 = 0.100000000"));
        assertEquals(Boolean.FALSE, context.getValue("0.1 = 0.100000000000000001"));
    }

    /**
     * Tests the != operator.
     */
    @Test
    public void testNotEquals() {
        assertEquals(Boolean.TRUE, context.getValue("0.1 != 0.100000001"));
        assertEquals(Boolean.FALSE, context.getValue("0.1 != 0.100000000"));
    }

    /**
     * Tests the &gt; operator.
     */
    @Test
    public void testGreaterThan() {
        assertEquals(Boolean.TRUE, context.getValue("0.100000000000000001 > 0.1"));
        assertEquals(Boolean.FALSE, context.getValue("0.1 > 0.100000000000000001"));
    }

    /**
     * Tests the &gt;= operator.
     */
    @Test
    public void testGreaterThanOrEqual() {
        assertEquals(Boolean.TRUE, context.getValue("0.100000000000000001 >= 0.1"));
        assertEquals(Boolean.FALSE, context.getValue("0.1 >= 0.100000000000000001"));
    }

    /**
     * Tests the &lt; operator.
     */
    @Test
    public void testLessThan() {
        assertEquals(Boolean.TRUE, context.getValue("0.1 < 0.100000000000000001"));
        assertEquals(Boolean.FALSE, context.getValue("0.100000000000000001 < 0.1"));
    }

    /**
     * Tests the &lt;= operator.
     */
    @Test
    public void testLessThanOrEqual() {
        assertEquals(Boolean.TRUE, context.getValue("0.1 <= 0.100000000000000001"));
        assertEquals(Boolean.FALSE, context.getValue("0.100000000000000001 <= 0.1"));
    }

    /**
     * Tests the mod operator.
     */
    @Test
    public void testMod() {
        assertEquals(BigDecimal.ONE, context.getValue("3 mod 2"));
    }

    /**
     * Tests the negation operator.
     */
    @Test
    public void testNegate() {
        BigDecimal value = (BigDecimal) context.getValue("- -0.100000000000000001");
        checkEquals(new BigDecimal("0.100000000000000001"), value);

        value = (BigDecimal) context.getValue("- - -0.100000000000000001");
        checkEquals(new BigDecimal("-0.100000000000000001"), value);
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        new JXPathHelper();
        context = JXPathHelper.newContext(new Object());
    }

    /**
     * Verifies that two BigDecimals are equal.
     *
     * @param expected the expected value
     * @param actual   the actual value
     */
    private void checkEquals(BigDecimal expected, BigDecimal actual) {
        assertTrue(actual.compareTo(expected) == 0);
    }
}
