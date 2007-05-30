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

package org.openvpms.archetype.util;

import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;


/**
 * Tests the {@link MacroEvaluator} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class MacroEvaluatorTestCase extends ArchetypeServiceTest {

    /**
     * The macro evaluator.
     */
    MacroEvaluator macros;


    /**
     * Tests {@link MacroEvaluator#evaluate(String, Object)}.
     */
    public void testMacros() {
        Party person = TestHelper.createCustomer();
        Object text1 = macros.evaluate("macro1", person);
        assertEquals("macro 1 text", text1);

        Object text2 = macros.evaluate("macro2", person);
        assertEquals("onetwothree", text2);

        Object text3 = macros.evaluate("test macro1 macro2 endtest",
                                       person);
        assertEquals("test macro 1 text onetwothree endtest", text3);

        Object text4 = macros.evaluate("displayName", person);
        assertEquals("Customer(Person)", text4);
    }

    /**
     * Verifies verifies that macros that throw exceptions don't expand.
     */
    public void testExceptionMacro() {
        Party person = TestHelper.createCustomer();
        Object text = macros.evaluate("exceptionMacro", person);
        assertEquals("exceptionMacro", text);

    }

    /**
     * Verifies that non-existent macros don't expand.
     */
    public void testNonExistentMacro() {
        Object text = macros.evaluate("non existent", new Object());
        assertEquals("non existent", text);
    }

    /**
     * Tests that nested macros are expanded.
     */
    public void testNestedMacro() {
        Object text = macros.evaluate("nested", new Object());
        assertEquals("nested test: macro 1 text", text);
    }

    /**
     * Tests that numeric prefixes are expanded as the $number variable.
     */
    public void testNumericPrefix() {
        Object dummy = new Object();
        // verify that when no prefix is specified, the number doesn't evaluate
        // to anything
        Object text1 = macros.evaluate("numbertest", dummy);
        assertEquals("input number: ", text1);

        Object text2 = macros.evaluate("99numbertest", dummy);
        assertEquals("input number: 99", text2);

        Object text3 = macros.evaluate("0.5numbertest", dummy);
        assertEquals("input number: 0.5", text3);

        Object text4 = macros.evaluate("1/2numbertest", dummy);
        assertEquals("input number: 1/2", text4);

        // not a valid no. but pass through anyway unchanged
        Object text5 = macros.evaluate("1/2.0/3numbertest", dummy);
        assertEquals("input number: 1/2.0/3", text5);
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        createMacro("macro1", "'macro 1 text'");
        createMacro("macro2", "concat('one', 'two', 'three')");
        createMacro("displayName", "openvpms:get(., 'displayName')");
        createMacro("exceptionMacro", "openvpms:get(., 'invalidnode')");
        createMacro("nested", "concat('nested test: ', $macro1)");
        createMacro("numbertest", "concat('input number: ', $number)");
        macros = new MacroEvaluator(new MacroCache());
    }

    /**
     * Helper to create and save a macro.
     *
     * @param code       the macro code
     * @param expression the macro expression
     */
    private void createMacro(String code, String expression) {
        Lookup macro = (Lookup) create("lookup.macro");
        IMObjectBean bean = new IMObjectBean(macro);
        bean.setValue("code", code);
        bean.setValue("name", code);
        bean.setValue("expression", expression);
        bean.save();
    }

}
