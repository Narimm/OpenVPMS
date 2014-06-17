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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.function;

import org.apache.commons.jxpath.JXPathContext;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.function.expression.ExpressionFunctions;
import org.openvpms.component.system.common.jxpath.JXPathHelper;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


/**
 * Tests the {@link ExpressionFunctions} class.
 *
 * @author Tim Anderson
 */
public class ExpressionFunctionsTestCase {

    /**
     * The JXPath context.
     */
    private JXPathContext ctx;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        // register the functions with JXPath.
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("expr", new ExpressionFunctions("expr"));
        new JXPathHelper(map);
        ctx = JXPathHelper.newContext(new Object());
    }

    /**
     * Tests the {@code expr:if(condition, expression)} function.
     */
    @Test
    public void testIfThen() {
        assertNull(ctx.getValue("expr:if('a' = 'b', 'true')"));
        assertEquals("true", ctx.getValue("expr:if('a' != 'b', 'true')"));
    }

    /**
     * Tests the {@code expr:if(condition, thenExpression, elseExpression)} function.
     */
    @Test
    public void testIfThenElse() {
        assertEquals("false", ctx.getValue("expr:if('a' = 'b', 'true', 'false')"));
        assertEquals("true", ctx.getValue("expr:if('a' !='b', 'true', 'false')"));

        ctx.getVariables().declareVariable("x", null);
        assertEquals("false", ctx.getValue("expr:if($x, 'true', 'false')"));
        assertEquals("true", ctx.getValue("expr:if(not($x), 'true', 'false')"));

    }

    /**
     * Tests the {@link ExpressionFunctions#var} methods.
     */
    @Test
    public void testVar() {
        ctx.getVariables().declareVariable("foo", "bar");
        assertEquals("bar", ctx.getValue("expr:var('foo')"));
        assertNull(ctx.getValue("expr:var('novar')"));
        assertEquals("fail", ctx.getValue("expr:var('novar', 'fail')"));
    }

    /**
     * Tests the {@link ExpressionFunctions#concatIf} methods.
     */
    @Test
    public void testConcatIf() {
        assertEquals("", ctx.getValue("expr:concatIf('x','')"));
        assertEquals("", ctx.getValue("expr:concatIf('','x')"));
        assertEquals("xy", ctx.getValue("expr:concatIf('x','y')"));

        assertEquals("", ctx.getValue("expr:concatIf('','x','x')"));
        assertEquals("", ctx.getValue("expr:concatIf('x','','x')"));
        assertEquals("", ctx.getValue("expr:concatIf('x','x','')"));
        assertEquals("xyx", ctx.getValue("expr:concatIf('x','y','x')"));
    }

    /**
     * Tests the {@link ExpressionFunctions#trim(String, int)} methods.
     */
    @Test
    public void testTrim() {
        assertEquals("abc", ctx.getValue("expr:trim('abc', 4)"));
        assertEquals("ab", ctx.getValue("expr:trim('abc', 2)"));
        assertEquals("", ctx.getValue("expr:trim('', 255)"));
    }
}
