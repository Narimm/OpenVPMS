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
package org.openvpms.archetype.function.macro;

import org.apache.commons.jxpath.JXPathContext;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.archetype.util.MacroTestHelper;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.jxpath.JXPathHelper;


/**
 * Tests the {@link MacroFunctions} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class MacroFunctionsTestCase extends ArchetypeServiceTest {

    /**
     * Tests the single argument version:
     * <pre>
     *   macro:eval('macroname')
     * </pre>
     * This evaluates the macro against the context object.
     */
    @Test
    public void testSingleArgEval() {
        Party customer = TestHelper.createCustomer(false);
        JXPathContext ctx = JXPathHelper.newContext(customer);
        assertEquals("Customer", ctx.getValue("macro:eval('displayName')"));
    }


    /**
     * Tests the two argument version:
     * <pre>
     *   macro:eval('macroname', somecontext)
     * </pre>
     * This evaluates the macro against the specified context object.
     */
    @Test
    public void testTwoArgEval() {
        Party customer = TestHelper.createCustomer(false);

        JXPathContext ctx = JXPathHelper.newContext(customer);
        assertEquals("Customer", ctx.getValue("macro:eval('displayName', .)"));

        ctx = JXPathHelper.newContext(new Object());
        ctx.getVariables().declareVariable("customer", customer);
        assertEquals("Customer", ctx.getValue("macro:eval('displayName', $customer)"));
    }

    /**
     * Tests that numeric prefixes are expanded as the $number variable.
     */
    @Test
    public void testNumericPrefix() {
        Object dummy = new Object();
        JXPathContext ctx = JXPathHelper.newContext(dummy);

        // verify that when no prefix is specified, the number doesn't evaluate
        // to anything
        Object text1 = ctx.getValue("macro:eval('numbertest')");
        assertEquals("input number: ", text1);

        Object text2 = ctx.getValue("macro:eval('99numbertest')");
        assertEquals("input number: 99", text2);
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        MacroTestHelper.createMacro("displayName", "openvpms:get(., 'displayName')");
        MacroTestHelper.createMacro("numbertest", "concat('input number: ', $number)");
    }

}
