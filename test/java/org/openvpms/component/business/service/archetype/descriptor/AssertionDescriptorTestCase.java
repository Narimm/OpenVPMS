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

import static org.junit.Assert.*;
import org.junit.Test;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.ArchetypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;


/**
 * Test assertion descriptors for archetypes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
@ContextConfiguration("descriptor-test-appcontext.xml")
public class AssertionDescriptorTestCase extends AbstractJUnit4SpringContextTests {


    /**
     * Holds a reference to the entity service
     */
    @Autowired
    private ArchetypeService service;


    /**
     * Test that the assertion descriptors are returned in the order they were
     * entered.
     *
     * @throws Exception for any error
     */
    @Test
    public void testAssertionDescriptorOrdering() throws Exception {
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor("party.personbernief");
        assertNotNull(adesc);
        NodeDescriptor ndesc = adesc.getNodeDescriptor("identities");
        assertNotNull(ndesc);
        assertTrue(ndesc.getAssertionDescriptors().size() == 5);
        int currIndex = 0;
        String assertName = "dummyAssertion";
        for (AssertionDescriptor desc : ndesc.getAssertionDescriptorsInIndexOrder()) {
            String name = desc.getName();
            if (name.startsWith(assertName)) {
                int index = Integer.parseInt(
                        name.substring(assertName.length()));
                if (index > currIndex) {
                    currIndex = index;
                } else {
                    fail("Assertions are not returned in the correct order currIndex: "
                         + currIndex + " index: " + index);
                }
            }
        }

        // clone and test it again
        NodeDescriptor clone = (NodeDescriptor) ndesc.clone();
        assertNotNull(clone);
        assertTrue(clone.getAssertionDescriptors().size() == 5);
        currIndex = 0;
        for (AssertionDescriptor desc : clone.getAssertionDescriptorsInIndexOrder()) {
            String name = desc.getName();
            if (name.startsWith(assertName)) {
                int index = Integer.parseInt(
                        name.substring(assertName.length()));
                if (index > currIndex) {
                    currIndex = index;
                } else {
                    fail("Assertions are not returned in the correct order currIndex: "
                         + currIndex + " index: " + index);
                }
            }
        }
    }

    /**
     * Test that the properties within an assertion are returned in the
     * order that they are defined.
     */
    @Test
    public void testAssertionPropertyOrder() {
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor("party.personbernief");
        assertNotNull(adesc);
        NodeDescriptor ndesc = adesc.getNodeDescriptor("identities");
        assertNotNull(ndesc);

        int index = 0;
        for (String shortName : ndesc.getArchetypeRange()) {
            switch (index++) {
                case 0:
                    assertTrue(shortName.equals("entityIdentity.animalAlias"));
                    break;
                case 1:
                    assertTrue(shortName.equals("entityIdentity.personAlias"));
                    break;
                case 2:
                    assertTrue(
                            shortName.equals("entityIdentity.customerAlias1"));
                    break;
                case 3:
                    assertTrue(
                            shortName.equals("entityIdentity.customerAlias"));
                    break;
                default:
                    fail("The short name " + shortName + " should not be defined");
                    break;
            }
        }
    }

    /**
     * Deleting nodes with archetypes assertions cause a validation error.
     */
    @Test
    public void testOBF10() {
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor("party.person");
        assertNotNull(adesc);

        // find and remove the classifications node
        assertTrue(adesc.getNodeDescriptor("classifications") != null);
        adesc.removeNodeDescriptor("classifications");
        assertTrue(adesc.getNodeDescriptor("classifications") == null);

        // remove the title node
        assertTrue(adesc.getNodeDescriptor("title") != null);
        adesc.removeNodeDescriptor("title");
        assertTrue(adesc.getNodeDescriptor("title") == null);

        // now validate the archetype
        service.validateObject(adesc);
    }

}
