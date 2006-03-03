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

// java core
import java.io.InputStreamReader;
import java.util.Hashtable;

// castor 
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;

// commons-lang

// sax
import org.xml.sax.InputSource;

// openvpms-framework
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptors;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.system.common.test.BaseTestCase;


/**
 * Test assertion descriptors for archetypes.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class AssertionDescriptorTestCase extends BaseTestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(AssertionDescriptorTestCase.class);
    }

    /**
     * Constructor for DescriptorTestCase.
     * @param name
     */
    public AssertionDescriptorTestCase(String name) {
        super(name);
    }

    /*
     * @see BaseTestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see BaseTestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test that the display name for the archetype and node default to the
     * name of those elements
     */
    public void testDefaultDisplayName()
    throws Exception {
        Hashtable gparams = getTestData().getGlobalParams();
        String mfile = (String)gparams.get("mappingFile");
        String afile = (String)gparams.get("archetypeFile");
        
        // load the archetypes
        ArchetypeDescriptors descriptors = getArchetypeDescriptors(mfile, afile);
        assertTrue(descriptors.getArchetypeDescriptors().size() == 3);
        
        assertTrue(descriptors.getArchetypeDescriptors().get("assertion.archetypeRange") != null);
    }
    
    /**
     * Test that the assertion descriptors are returned in the order they were 
     * entered
     */
    public void testAssertionDescriptorOrdering()
    throws Exception {
        Hashtable gparams = getTestData().getGlobalParams();
        String mfile = (String)gparams.get("mappingFile");
        String afile = (String)gparams.get("archetypeFile");
        
        // load the archetypes
        ArchetypeDescriptors descriptors = getArchetypeDescriptors(mfile, afile);
        ArchetypeDescriptor adesc = descriptors.getArchetypeDescriptors().get("person.person");
        assertTrue(adesc != null);
        NodeDescriptor ndesc = adesc.getNodeDescriptor("identities");
        assertTrue(ndesc != null);
        assertTrue(ndesc.getAssertionDescriptors().size() == 5);
        int currIndex = 0;
        String assertName = "dummyAssertion";
        for (AssertionDescriptor desc : ndesc.getAssertionDescriptorsAsArray()) {
            String name = desc.getName();
            if (name.startsWith(assertName)) {
                int index = Integer.parseInt(name.substring(assertName.length()));
                if (index > currIndex) {
                    currIndex = index;
                } else {
                    fail("Assertions are not returned in the correct order currIndex: " 
                            + currIndex + " index: " + index);
                }
            }
        }
        
        // clone and test it again
        NodeDescriptor clone = (NodeDescriptor)ndesc.clone();
        assertTrue(clone != null);
        assertTrue(clone.getAssertionDescriptors().size() == 5);
        currIndex = 0;
        for (AssertionDescriptor desc : clone.getAssertionDescriptorsAsArray()) {
            String name = desc.getName();
            if (name.startsWith(assertName)) {
                int index = Integer.parseInt(name.substring(assertName.length()));
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
     * Get archetype descriptors
     * 
     * @param mfile
     *            the mapping file
     * @param afile
     *            the archetype descriptor file            
     * @return ArchetypeDescriptors
     * @throws Exception
     */
    private ArchetypeDescriptors getArchetypeDescriptors(String mfile, String afile)
    throws Exception {
        Mapping mapping = new Mapping();
        mapping.loadMapping(new InputSource(new InputStreamReader(
                Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(mfile))));
        
        // set up the unmarshaller
        Unmarshaller unmarshaller = new Unmarshaller(mapping);
        return  (ArchetypeDescriptors)unmarshaller.unmarshal(
                new InputSource(new InputStreamReader(
                Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(afile))));
    }
}
