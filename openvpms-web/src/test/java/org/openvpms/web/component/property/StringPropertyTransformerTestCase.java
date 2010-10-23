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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.property;

import static org.junit.Assert.*;
import org.junit.Test;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.web.test.AbstractAppTest;
import org.openvpms.archetype.test.TestHelper;


/**
 * {@link StringPropertyTransformer} test case.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-09-04 05:00:00Z $
 */
public class StringPropertyTransformerTestCase extends AbstractAppTest {

    /**
     * Tests {@link StringPropertyTransformer#apply}.
     */
    @Test
    public void testApply() {
        Party person = TestHelper.createCustomer();
        NodeDescriptor descriptor = getDescriptor(person, "name");
        Property property = new IMObjectProperty(person, descriptor);
        StringPropertyTransformer handler
                = new StringPropertyTransformer(property);

        assertNull(handler.apply(null));
        assertNull(handler.apply(""));
        assertEquals("abc", handler.apply("abc"));

        assertEquals("1", handler.apply(1));
    }

    /**
     * Verifies that an exception is thrown if a string contains control
     * characters.
     */
    @Test
    public void testExceptionForControlChars() {
        String bad = "abcd\u000012345";
        Party person = TestHelper.createCustomer();
        NodeDescriptor descriptor = getDescriptor(person, "name");
        Property property = new IMObjectProperty(person, descriptor);
        StringPropertyTransformer handler
                = new StringPropertyTransformer(property);
        try {
            handler.apply(bad);
            fail("Expected PropertyException to be thrown");
        } catch (PropertyException expected) {
            // expected behaviour
        }
    }

    /**
     * Tests macro expansion by {@link StringPropertyTransformer#apply}.
     */
    @Test
    public void testMacroExpansion() {
        Party person = TestHelper.createCustomer();
        NodeDescriptor descriptor = getDescriptor(person, "lastName");
        Property property = new IMObjectProperty(person, descriptor);
        StringPropertyTransformer handler
                = new StringPropertyTransformer(property);

        Object text1 = handler.apply("macro1");
        assertEquals("macro 1 text", text1);

        Object text2 = handler.apply("macro2");
        assertEquals("onetwothree", text2);

        Object text3 = handler.apply("macro1 macro2");
        assertEquals("macro 1 text onetwothree", text3);

        Object text4 = handler.apply("displayName");
        assertEquals("Customer", text4);

        // verifies that invalid macros don't expand
        Object text5 = handler.apply("invalidNode");
        assertEquals("invalidNode", text5);

        // verifies that non-existent macros don't expand
        Object text6 = handler.apply("non existent");
        assertEquals("non existent", text6);
    }

    /**
     * Helper to return a node descriptor.
     *
     * @param object the object
     * @param node   the node name
     * @return the node descriptor
     */
    protected NodeDescriptor getDescriptor(IMObject object, String node) {
        ArchetypeDescriptor type
                = DescriptorHelper.getArchetypeDescriptor(object);
        assertNotNull(type);
        NodeDescriptor result = type.getNodeDescriptor(node);
        assertNotNull(result);
        return result;
    }

    /**
     * Sets up the test case.
     */
    @Override
    public void setUp() {
        super.setUp();
        createMacro("macro1", "'macro 1 text'");
        createMacro("macro2", "concat('one', 'two', 'three')");
        createMacro("displayName", "openvpms:get(., 'displayName')");
        createMacro("exceptionMacro", "openvpms:get(., 'invalidnode')");
        createMacro("nested", "concat('nested test: ', $macro1)");
        createMacro("numbertest", "concat('input number: ', $number)");
    }

    /**
     * Helper to create and save a macro.
     *
     * @param code       the macro code
     * @param expression the macro expression
     */
    private void createMacro(String code, String expression) {
        deleteExisting(code);
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        Lookup macro = (Lookup) service.create("lookup.macro");
        IMObjectBean bean = new IMObjectBean(macro);
        bean.setValue("code", code);
        bean.setValue("name", code);
        bean.setValue("expression", expression);
        bean.save();
    }

    /**
     * Deletes any exising macro with the specified code, to avoid duplicate
     * errors.
     *
     * @param code the macro code
     */
    private void deleteExisting(String code) {
        ArchetypeQuery query = new ArchetypeQuery("lookup.macro", false, false);
        query.add(new NodeConstraint("code", code));
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        for (IMObject object : service.get(query).getResults()) {
            service.remove(object);
        }
    }

}
