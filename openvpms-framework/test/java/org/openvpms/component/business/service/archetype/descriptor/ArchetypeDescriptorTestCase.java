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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */


package org.openvpms.component.business.service.archetype.descriptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptors;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptors;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;

import java.io.InputStream;

/**
 * Test the all the archetype related descriptors.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeDescriptorTestCase {

    /**
     * Verifies that archetypes can be read from a file.
     */
    @Test
    public void testReadArchetypes() {
        // file with a single archetype
        ArchetypeDescriptors single = getArchetypeDescriptors("single-archetype.xml");
        assertEquals(1, single.getArchetypeDescriptors().size());

        // file with a couple of archetypes
        ArchetypeDescriptors multiple = getArchetypeDescriptors("archetypes.xml");
        assertEquals(2, multiple.getArchetypeDescriptors().size());
    }

    /**
     * Verifies that assertion type descriptors can be read from a file.
     */
    @Test
    public void testAssertionTypeDescriptors() {
        AssertionTypeDescriptors descriptors = getAssertionTypeDescriptors("assertion-test-types.xml");
        assertEquals(8, descriptors.getAssertionTypeDescriptors().size());
    }

    /**
     * Tests the node descriptor getter methods.
     *
     * @throws Exception for any error
     */
    @Test
    public void testGetNodeDescriptors() throws Exception {
        ArchetypeDescriptors descriptors = getArchetypeDescriptors("archetypes.xml");
        ArchetypeDescriptor descriptor = descriptors.getArchetypeDescriptors().get("party.person");
        assertNotNull(descriptor);
        assertEquals("party.person", descriptor.getType().getShortName());

        assertEquals(11, descriptor.getAllNodeDescriptors().size());
        assertEquals(7, descriptor.getSimpleNodeDescriptors().size());
        assertEquals(4, descriptor.getComplexNodeDescriptors().size());

        checkNode(descriptor, NodeDescriptor.IDENTIFIER_NODE_NAME, false);
        checkNode(descriptor, "title", false);
        checkNode(descriptor, "firstName", false);
        checkNode(descriptor, "initials", false);
        checkNode(descriptor, "lastName", false);
        checkNode(descriptor, "contacts", true);
        checkNode(descriptor, "classifications", true);
        checkNode(descriptor, "name", false);
        checkNode(descriptor, "description", false);
        checkNode(descriptor, "identities", true);
        checkNode(descriptor, "sourceRelationships", true);
    }

    /**
     * Checks that a node has the expected details.
     *
     * @param descriptor the archetype descriptor
     * @param name       the name of the node to check
     * @param isComplex  if <tt>true</tt> expects the node to be a complex node
     */
    private void checkNode(ArchetypeDescriptor descriptor, String name, boolean isComplex) {
        NodeDescriptor node = descriptor.getNodeDescriptor(name);
        assertNotNull(node);
        assertEquals(name, node.getName());
        assertEquals(isComplex, node.isComplexNode());
    }

    /**
     * Helper to read archetype descriptors from a file.
     *
     * @param file the file path
     * @return the corresponding archetype descriptors
     */
    private ArchetypeDescriptors getArchetypeDescriptors(String file) {
        InputStream stream = getClass().getResourceAsStream(file);
        assertNotNull(stream);
        return ArchetypeDescriptors.read(stream);
    }

    /**
     * Helper to read assertion type descriptors from a file.
     *
     * @param file the assertion type descriptor file
     * @return the corresponding assertion type descriptors
     */
    private AssertionTypeDescriptors getAssertionTypeDescriptors(String file) {
        InputStream stream = getClass().getResourceAsStream(file);
        assertNotNull(stream);
        return AssertionTypeDescriptors.read(stream);
    }

}
