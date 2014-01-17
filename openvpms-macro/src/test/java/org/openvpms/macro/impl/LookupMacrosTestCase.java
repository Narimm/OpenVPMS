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

package org.openvpms.macro.impl;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.macro.MacroException;
import org.openvpms.macro.Macros;
import org.openvpms.macro.MapVariables;
import org.openvpms.macro.Position;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Tests {@link LookupMacros}.
 *
 * @author Tim Anderson
 */
public class LookupMacrosTestCase extends ArchetypeServiceTest {

    /**
     * The macros.
     */
    private Macros macros;

    /**
     * The first macro.
     */
    private Lookup macro1;

    /**
     * The second macro.
     */
    private Lookup macro2;

    /**
     * Customer to test against.
     */
    private Party customer;

    /**
     * The lookup service.
     */
    @Autowired
    private ILookupService lookupService;

    /**
     * The macro1 macro text expansion.
     */
    private static final String MACRO1_TEXT = "expanded text";

    /**
     * Tests the {@link LookupMacros#run(String, Object)} method.
     */
    @Test
    public void testRun() {
        String text1 = macros.run("macro1", customer);
        assertEquals(MACRO1_TEXT, text1);

        String text2 = macros.run("@macro2", customer);
        assertEquals("onetwothree", text2);

        String text4 = macros.run("displayName", customer);
        assertEquals("Customer", text4);
    }

    /**
     * Verifies that if a macro throws an exception, it is propagated as a {@link MacroException} by
     * {@link Macros#run(String, Object)}.
     */
    @Test(expected = MacroException.class)
    public void testRunException() {
        macros.run("exceptionMacro", customer);
    }

    /**
     * Verifies that {@code null} is returned by {@link Macros#run(String, Object)}if a macro doesn't exist .
     */
    @Test
    public void testRunForNoMacro() {
        assertNull(macros.run("nonExistent", customer));
    }

    /**
     * Verifies that a macro referenced as a variable by another macro is also run.
     */
    @Test
    public void testRunEmbeddedMacro() {
        String text = macros.run("nested", new Object());
        assertEquals("nested test: expanded text", text);
    }

    /**
     * Verifies that if a macro is called recursively from {@link Macros#run(String, Object)}, a
     * {@link MacroException} is thrown.
     */
    @Test
    public void testRunMacroRecursion() {
        try {
            macros.run("recursivemacro1", new Object());
            fail("Expected MacroException to be thrown");
        } catch (MacroException expected) {
            assertEquals("Macro recursivemacro1 called recursively from recursivemacro1 => recursivemacro2",
                         expected.getMessage());
        }
    }

    /**
     * Tests that numeric prefixes are expanded as the $number variable by the {@link LookupMacros#run(String, Object)}
     * method.
     */
    @Test
    public void testRunNumericPrefix() {
        String text1 = macros.run("numbertest", null);
        assertEquals("input number: ", text1);

        String text2 = macros.run("99numbertest", null);
        assertEquals("input number: 99", text2);

        String text3 = macros.run("0.5numbertest", null);
        assertEquals("input number: 0.5", text3);

        String text4 = macros.run("1/2numbertest", null);
        assertEquals("input number: 1/2", text4);

        // not a valid no. but pass through anyway unchanged
        String text5 = macros.run("1/2.0/3numbertest", null);
        assertEquals("input number: 1/2.0/3", text5);
    }

    /**
     * Verifies that declared variables can be accessed by macros.
     */
    @Test
    public void testRunDeclareVariable() {
        try {
            macros.run("variableTest", null);
            fail("Expected MacroException to be thrown");
        } catch (MacroException expected) {
            // as variable not defined
        }

        MapVariables variables = new MapVariables();
        variables.add("variable", "foo");
        assertEquals("foo", macros.run("variableTest", null, variables));
    }

    /**
     * Tests {@link LookupMacros#runAll(String, Object)}.
     */
    @Test
    public void testRunAll() {
        String text = macros.runAll("test macro1 @macro2 endtest", customer);
        assertEquals("test expanded text onetwothree endtest", text);
    }

    /**
     * Verifies that exceptions thrown by macros in {@link Macros#runAll} are swallowed, and the macro is retained
     * in the text.
     */
    @Test
    public void testRunAllException() {
        String text1 = macros.runAll("exceptionMacro", customer);
        assertEquals("exceptionMacro", text1);

        String text2 = macros.runAll("A exceptionMacro B", customer);
        assertEquals("A exceptionMacro B", text2);
    }

    /**
     * Verifies that non-existent macros aren't touched by {@link Macros#runAll(String, Object)}.
     */
    @Test
    public void testRunAllForNoMacro() {
        String text = macros.runAll("non existent", new Object());
        assertEquals("non existent", text);
    }

    /**
     * Tests that embedded macros are expanded by {@link Macros#runAll(String, Object)}.
     * Verifies that a macro referenced as a variable by another macro is also run by {@link Macros#runAll}.
     */
    @Test
    public void testRunAllEmbeddedMacro() {
        String text = macros.runAll("nested", null);
        assertEquals("nested test: expanded text", text);
    }

    /**
     * Verifies that if a macro is called recursively by {@link Macros#runAll} it is not expanded.
     */
    @Test
    public void testRunAllRecursion() {
        String text1 = macros.runAll("recursivemacro1", customer);
        assertEquals("recursivemacro1", text1);

        String text2 = macros.runAll("A recursivemacro1 B", customer);
        assertEquals("A recursivemacro1 B", text2);
    }

    /**
     * Verifies that if a macro is called recursively from {@link Macros#runAll}, the macro doesn't expand
     */
    @Test
    public void testRunAllMacroRecursion() {
        // verify that recursivemacro1 fails
        try {
            macros.run("recursivemacro1", new Object());
            fail("Expected MacroException to be thrown");
        } catch (MacroException expected) {
            // expected behaviour
        }
        // verify it isn't expanded when called from runAll
        String result = macros.runAll("A recursivemacro1 B", new Object());
        assertEquals("A recursivemacro1 B", result);
    }

    /**
     * Tests that numeric prefixes are expanded as the $number variable by {@link Macros#runAll}.
     */
    @Test
    public void testRunAllNumericPrefix() {
        String text1 = macros.runAll("A numbertest B", null);
        assertEquals("A input number:  B", text1);

        String text2 = macros.runAll("A 99numbertest B", null);
        assertEquals("A input number: 99 B", text2);

        String text3 = macros.runAll("A 0.5numbertest B", null);
        assertEquals("A input number: 0.5 B", text3);

        String text4 = macros.runAll("A 1/2numbertest B", null);
        assertEquals("A input number: 1/2 B", text4);

        String text5 = macros.runAll("A 1/2.0/3numbertest B", null);
        assertEquals("A input number: 1/2.0/3 B", text5);
    }

    /**
     * Verifies that declared variable can be access by macros in {@link Macros#runAll}.
     */
    @Test
    public void testRunAllDeclareVariable() {
        assertEquals("variableTest", macros.runAll("variableTest", new Object())); // as variable not defined

        MapVariables variables = new MapVariables();
        variables.add("variable", "foo");
        assertEquals("foo", macros.runAll("variableTest", new Object(), variables, null));
    }

    @Test
    public void testRunAllCursorPosition() {
        // check that if the text doesn't contain macros, the cursor doesn't move
        String text = "foo bar";
        checkRunAllCursorPosition(text, text, 0, 0);
        checkRunAllCursorPosition(text, text, text.length(), text.length());

        // if the cursor is on a macro, check that it moves to the end of the text
        checkRunAllCursorPosition("macro1", MACRO1_TEXT, 0, MACRO1_TEXT.length());
        checkRunAllCursorPosition("macro1", MACRO1_TEXT, 5, MACRO1_TEXT.length());
        checkRunAllCursorPosition("@empty", "", 5, 0);

        // check that if the cursor is before macros, it doesn't move
        checkRunAllCursorPosition("x @empty x", "x  x", 1, 1);

        // check that if the cursor is after macros, it remains positioned on the same text
        checkRunAllCursorPosition("x @empty x", "x  x", 9, 3);
        checkRunAllCursorPosition("@empty macro1 x", " " + MACRO1_TEXT + " x", 14, 15);
    }

    private void checkRunAllCursorPosition(String text, String expectedText, int startPosition, int expectedPosition) {
        Position position = new Position(startPosition);
        String expanded = macros.runAll(text, customer, null, position);
        assertEquals(expectedText, expanded);
        assertEquals(startPosition, position.getOldPosition());
        assertEquals(expectedPosition, position.getNewPosition());
    }


    /**
     * Verifies that inactive macros aren't picked up.
     */
    @Test
    public void testDeactivateMacro() {
        String text = macros.run("macro1", customer);
        assertEquals(MACRO1_TEXT, text);

        macro1.setActive(false);
        save(macro1);

        assertNull(macros.run("macro1", customer));
        assertEquals("macro1", macros.runAll("macro1", customer));
    }

    /**
     * Verifies that deleted macros aren't picked up.
     */
    @Test
    public void testDeleteMacro() {
        String text2 = macros.run("@macro2", customer);
        assertEquals("onetwothree", text2);

        remove(macro2);

        assertNull(macros.run("@macro2", customer));
        assertEquals("@macro2", macros.runAll("@macro2", customer));
    }

    /**
     * Verifies that a macro can call another macro if it is prefixed with a '$', and that the $number variable
     * can be used.
     */
    @Test
    public void testMacroCallingMacro() {
        MacroTestHelper.createMacro("testDispensingVerb", "'Give'");
        MacroTestHelper.createMacro("testDispensingUnits", "'tablet'");
        MacroTestHelper.createMacro(
                "@testTwiceADay",
                "concat($testDispensingVerb, ' ', $number, ' ', $testDispensingUnits, '(s) Twice a Day')");
        String text2 = macros.run("3@testTwiceADay", new Object());
        assertEquals(text2, "Give 3 tablet(s) Twice a Day");
    }

    /**
     * Verifies that a macro can call another macro using macro:eval(), and the variables are also available to the
     * child.
     */
    @Test
    public void testRunMacroCallingMacroWithVariable() {
        MacroTestHelper.createMacro("m_customerId", "openvpms:get($customer, 'id')");
        MacroTestHelper.createMacro("@cid", "macro:eval('m_customerId')");

        // define the the $customer variable and verify it can be accessed by the underlying m_customerId macro
        MapVariables variables = new MapVariables();
        variables.add("customer", customer);

        assertEquals("-1", macros.run("@cid", new Object(), variables));
        assertEquals("-1", macros.runAll("@cid", new Object(), variables, null));
    }

    /**
     * Verifies that the $number variable is scoped, so that a nested macro may have a different value for $number
     * than the outer macro.
     */
    @Test
    public void testRunNumberVariableScoping() {
        MacroTestHelper.createMacro("testNumber", "$number");
        MacroTestHelper.createMacro("testNestedNumber", "concat($number, macro:eval('2testNumber'))");

        assertEquals("3", macros.run("3testNumber", new Object()));
        assertEquals("3", macros.runAll("3testNumber", new Object()));

        assertEquals("32", macros.run("3testNestedNumber", new Object()));
        assertEquals("32", macros.runAll("3testNestedNumber", new Object()));
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        customer = TestHelper.createCustomer(false);

        macro1 = MacroTestHelper.createMacro("macro1", "'" + MACRO1_TEXT + "'");
        macro2 = MacroTestHelper.createMacro("@macro2", "concat('one', 'two', 'three')");
        MacroTestHelper.createMacro("@empty", "''");
        MacroTestHelper.createMacro("displayName", "openvpms:get(., 'displayName')");
        MacroTestHelper.createMacro("exceptionMacro", "openvpms:get(., 'invalidnode')");
        MacroTestHelper.createMacro("nested", "concat('nested test: ', $macro1)");
        MacroTestHelper.createMacro("numbertest", "concat('input number: ', $number)");
        MacroTestHelper.createMacro("variableTest", "$variable");
        MacroTestHelper.createMacro("recursivemacro1", "$recursivemacro2");
        MacroTestHelper.createMacro("recursivemacro2", "$recursivemacro1");
        macros = new LookupMacros(lookupService, getArchetypeService(), new DocumentHandlers());

        // register the macro functions
        Map properties = new HashMap();
        properties.put("macro", new TestMacroFunctions(macros));
        new JXPathHelper(properties);
    }

    /**
     * Hack to ensure that {@link MacroFunctions} is always working with the current instance of {@link LookupMacros}.
     * <p/>
     * This is required as JXPathHelper caches functions in a singleton.
     */
    private static class TestMacroFunctions extends MacroFunctions {

        private static Macros macros;

        public TestMacroFunctions(Macros macros) {
            super(macros);
            this.macros = macros;
        }

        @Override
        public String eval(String macro, Object context) {
            return macros.run(macro, context);
        }
    }

}


