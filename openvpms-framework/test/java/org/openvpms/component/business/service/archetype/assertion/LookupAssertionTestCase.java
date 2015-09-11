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

package org.openvpms.component.business.service.archetype.assertion;

import org.junit.Test;
import org.openvpms.component.business.domain.im.archetype.descriptor.ActionContext;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.datatypes.property.AssertionProperty;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyList;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link LookupAssertions} class.
 * <p>
 * TODO - this should be converted to use {@link ActionContext}.
 *
 * @author Tim Anderson
 */
public class LookupAssertionTestCase {

    /**
     * Tests the {@link LookupAssertions#isStringValueInList(Object, NodeDescriptor, AssertionDescriptor)} method.
     */
    @Test
    public void testIsStringValueInList() {
        NodeDescriptor node = new NodeDescriptor();
        AssertionDescriptor assertion = new AssertionDescriptor();
        assertFalse(LookupAssertions.isStringValueInList("FOO", node, assertion));
        PropertyList entries = new PropertyList();
        entries.setName("entries");
        assertion.addProperty(entries);

        // test string properties
        entries.addProperty(create("BAR", "bar"));
        assertFalse(LookupAssertions.isStringValueInList("FOO", node, assertion));

        entries.addProperty(create("FOO", "foo"));
        assertTrue(LookupAssertions.isStringValueInList("FOO", node, assertion));

        // test conversion to string
        entries.addProperty(create("1", "1"));
        assertFalse(LookupAssertions.isStringValueInList(0, node, assertion));
        assertTrue(LookupAssertions.isStringValueInList(1, node, assertion));
    }

    /**
     * Helper to create an assertion property.
     *
     * @param name  the property name
     * @param value the property value
     * @return a new assertion property
     */
    private AssertionProperty create(String name, String value) {
        AssertionProperty result = new AssertionProperty();
        result.setName(name);
        result.setValue(value);
        return result;
    }

}
