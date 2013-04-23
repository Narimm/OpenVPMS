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

package org.openvpms.archetype.util;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.NodeResolver;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;


/**
 * Tests the {@link MacroEvaluator} class.
 *
 * @author Tim Anderson
 */
public class MacroEvaluatorTestCase extends ArchetypeServiceTest {

    /**
     * The macro evaluator.
     */
    MacroEvaluator macros;

    /**
     * The first macro.
     */
    private Lookup macro1;

    /**
     * The second macro.
     */
    private Lookup macro2;

    /**
     * The lookup service.
     */
    @Autowired
    private ILookupService lookupService;

    /**
     * Tests {@link MacroEvaluator#evaluate(String, Object)}.
     */
    @Test
    public void testMacros() {
        Party person = TestHelper.createCustomer();
        Object text1 = macros.evaluate("macro1", person);
        assertEquals("macro 1 text", text1);

        Object text2 = macros.evaluate("@macro2", person);
        assertEquals("onetwothree", text2);

        Object text3 = macros.evaluate("test macro1 @macro2 endtest", person);
        assertEquals("test macro 1 text onetwothree endtest", text3);

        Object text4 = macros.evaluate("displayName", person);
        assertEquals("Customer", text4);
    }

    /**
     * Verifies verifies that macros that throw exceptions don't expand.
     */
    @Test
    public void testExceptionMacro() {
        Party person = TestHelper.createCustomer();
        Object text = macros.evaluate("exceptionMacro", person);
        assertEquals("exceptionMacro", text);
    }

    /**
     * Verifies that non-existent macros don't expand.
     */
    @Test
    public void testNonExistentMacro() {
        Object text = macros.evaluate("non existent", new Object());
        assertEquals("non existent", text);
    }

    /**
     * Tests that nested macros are expanded.
     */
    @Test
    public void testNestedMacro() {
        Object text = macros.evaluate("nested", new Object());
        assertEquals("nested test: macro 1 text", text);
    }

    /**
     * Tests that numeric prefixes are expanded as the $number variable.
     */
    @Test
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
     * Verifies that inactive macros aren't picked up.
     */
    @Test
    public void testDeactivateMacro() {
        Party person = TestHelper.createCustomer();
        Object text = macros.evaluate("macro1", person);
        assertEquals("macro 1 text", text);

        macro1.setActive(false);
        save(macro1);

        text = macros.evaluate("macro1", person);
        assertEquals("macro1", text);
    }

    /**
     * Verifies that deleted macros aren't picked up.
     */
    @Test
    public void testDeleteMacro() {
        Party person = TestHelper.createCustomer();
        Object text2 = macros.evaluate("@macro2", person);
        assertEquals("onetwothree", text2);

        remove(macro2);

        text2 = macros.evaluate("@macro2", person);
        assertEquals("@macro2", text2);
    }

    /**
     * Verifies that declared variable can be access by macros.
     */
    @Test
    public void testDeclareVariable() {
        assertEquals("variableTest", macros.evaluate("variableTest", new Object())); // as variable not defined
        macros.declareVariable("variable", "foo");
        assertEquals("foo", macros.evaluate("variableTest", new Object()));
    }

    /**
     * Verifies that declared variables can be access by macros, and support the {@link NodeResolver} syntax.
     */
    @Test
    public void testDeclareIMObjectVariable() {
        // check that the macro doesn't expand if the underlying variable is not defined
        assertEquals("patientname", macros.evaluate("patientname", new Object()));

        // create a patient
        IMObject patient = TestHelper.createPatient();
        assertNotNull(patient.getName());

        // declare the variable
        macros.declareVariable("patient", patient);

        // verify that the macro expands correctly
        assertEquals(patient.getName(), macros.evaluate("patientname", new Object()));
    }

    /**
     * Verifies that declared variables can be access by macros, and support the {@link NodeResolver} syntax.
     */
    @Test
    public void testDeclareIMObjectVariableForLookup() {
        // check that the macro doesn't expand if the underlying variable is not defined
        assertEquals("patientspecies", macros.evaluate("patientspecies", new Object()));

        // create a canine patient
        IMObject patient = TestHelper.createPatient();

        // verify that the lookup name is not the same as its code
        Lookup lookup = TestHelper.getLookup("lookup.species", "CANINE");
        assertNotEquals(lookup.getName(), lookup.getCode());

        // declare the variable
        macros.declareVariable("patient", patient);

        // verify that the macro expands correctly
        assertEquals(lookup.getName(), macros.evaluate("patientspecies", new Object()));
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        macro1 = MacroTestHelper.createMacro("macro1", "'macro 1 text'");
        macro2 = MacroTestHelper.createMacro("@macro2", "concat('one', 'two', 'three')");
        MacroTestHelper.createMacro("displayName", "openvpms:get(., 'displayName')");
        MacroTestHelper.createMacro("exceptionMacro", "openvpms:get(., 'invalidnode')");
        MacroTestHelper.createMacro("nested", "concat('nested test: ', $macro1)");
        MacroTestHelper.createMacro("numbertest", "concat('input number: ', $number)");
        MacroTestHelper.createMacro("variableTest", "$variable");
        MacroTestHelper.createMacro("patientname", "$patient.name");
        MacroTestHelper.createMacro("patientspecies", "$patient.species");
        macros = new MacroEvaluator(new MacroCache(), new IMObjectVariables(getArchetypeService(), lookupService));
    }


}
