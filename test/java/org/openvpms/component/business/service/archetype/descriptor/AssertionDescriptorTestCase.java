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

// spring framework

import org.apache.log4j.Logger;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.ArchetypeService;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;


/**
 * Test assertion descriptors for archetypes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class AssertionDescriptorTestCase
        extends AbstractDependencyInjectionSpringContextTests {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(AssertionDescriptorTestCase.class);


    /**
     * Holds a reference to the entity service
     */
    private ArchetypeService service;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(AssertionDescriptorTestCase.class);
    }

    /**
     * Constructor for DescriptorTestCase.
     */
    public AssertionDescriptorTestCase() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    @Override
    protected String[] getConfigLocations() {
        return new String[]{
                "org/openvpms/component/business/service/archetype/descriptor/descriptor-test-appcontext.xml"
        };
    }

    /* (non-Javadoc)
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        this.service = (ArchetypeService) applicationContext.getBean(
                "archetypeService");
    }

    /**
     * Test that the assertion descriptors are returned in the order they were
     * entered
     */
    public void testAssertionDescriptorOrdering()
            throws Exception {
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor(
                "person.bernief");
        assertTrue(adesc != null);
        NodeDescriptor ndesc = adesc.getNodeDescriptor("identities");
        assertTrue(ndesc != null);
        assertTrue(ndesc.getAssertionDescriptors().size() == 5);
        int currIndex = 0;
        String assertName = "dummyAssertion";
        for (AssertionDescriptor desc : ndesc.getAssertionDescriptorsInIndexOrder())
        {
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
        assertTrue(clone != null);
        assertTrue(clone.getAssertionDescriptors().size() == 5);
        currIndex = 0;
        for (AssertionDescriptor desc : clone.getAssertionDescriptorsInIndexOrder())
        {
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
    public void testAssertionPropertyOrder()
            throws Exception {
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor(
                "person.bernief");
        assertTrue(adesc != null);
        NodeDescriptor ndesc = adesc.getNodeDescriptor("identities");
        assertTrue(ndesc != null);

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
     * Deleting nodes with archetypes assertions cause a validation error
     */
    public void testOBF10()
            throws Exception {
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor(
                "person.person");
        assertTrue(adesc != null);

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
