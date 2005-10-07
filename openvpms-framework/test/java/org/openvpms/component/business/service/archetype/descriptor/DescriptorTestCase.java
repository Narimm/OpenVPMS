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

import java.io.InputStreamReader;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.openvpms.component.system.common.test.BaseTestCase;
import org.xml.sax.InputSource;

/**
 * Test the all the archetype related descriptors.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class DescriptorTestCase extends BaseTestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(DescriptorTestCase.class);
    }

    /**
     * Constructor for DescriptorTestCase.
     * @param name
     */
    public DescriptorTestCase(String name) {
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
     * Test that we can read an archetype file with a single archetype
     */
    public void testSingleArchetypeDescripor()
    throws Exception {
        String mfile = (String)this.getTestData().getTestCaseParameter(
                "testSingleArchetypeDescripor", "normal", "mappingFile");
        String afile = (String)this.getTestData().getTestCaseParameter(
                "testSingleArchetypeDescripor", "normal", "archetypeFile");
        
        // load the mapping file
        Mapping mapping = new Mapping();
        mapping.loadMapping(new InputSource(new InputStreamReader(
                Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(mfile))));
        
        // set up the unmarshaller
        Unmarshaller unmarshaller = new Unmarshaller(mapping);
        ArchetypeDescriptors descriptors = (ArchetypeDescriptors)unmarshaller
            .unmarshal(new InputSource(new InputStreamReader(
                    Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(afile))));
        assertTrue(descriptors.getArchetypeDescriptorsAsMap().size() == 1);
    }

    /**
     * Test that we can read the assertion types from an XML document
     */
    public void testAssertionTypeDescriptors()
    throws Exception {
        String mfile = (String)this.getTestData().getTestCaseParameter(
                "testAssertionTypeDescriptors", "normal", "mappingFile");
        String afile = (String)this.getTestData().getTestCaseParameter(
                "testAssertionTypeDescriptors", "normal", "assertionFile");
        
        // load the mapping file
        Mapping mapping = new Mapping();
        mapping.loadMapping(new InputSource(new InputStreamReader(
                Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(mfile))));
        
        // set up the unmarshaller
        Unmarshaller unmarshaller = new Unmarshaller(mapping);
        AssertionTypeDescriptors descriptors = (AssertionTypeDescriptors)unmarshaller
            .unmarshal(new InputSource(new InputStreamReader(
                    Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(afile))));
        assertTrue(descriptors.getAssertionTypeDescriptors().size() == 5);
    }
    
    /**
     * Test that the display name for the archetype and node default to the
     * name of those elements
     */
    public void testDefaultDisplayName()
    throws Exception {
        String mfile = (String)this.getTestData().getTestCaseParameter(
                "testSingleArchetypeDescripor", "normal", "mappingFile");
        String afile = (String)this.getTestData().getTestCaseParameter(
                "testSingleArchetypeDescripor", "normal", "archetypeFile");
        
        // load the mapping file
        Mapping mapping = new Mapping();
        mapping.loadMapping(new InputSource(new InputStreamReader(
                Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(mfile))));
        
        // set up the unmarshaller
        Unmarshaller unmarshaller = new Unmarshaller(mapping);
        ArchetypeDescriptors descriptors = (ArchetypeDescriptors)unmarshaller
            .unmarshal(new InputSource(new InputStreamReader(
                    Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(afile))));
        assertTrue(descriptors.getArchetypeDescriptorsAsMap().size() == 1);
        
        // test that the archetype display name defaults to the name
        ArchetypeDescriptor descriptor = (ArchetypeDescriptor)descriptors
                    .getArchetypeDescriptors()[0];
        assertTrue(descriptor.getDisplayName().equals(descriptor.getName()));
        
        // iterate through the top level nodes and enusre that the 
        // display name defaults to the name
        for (NodeDescriptor node : descriptor.getNodeDescriptors()) {
            assertTrue(node.getDisplayName().equals(node.getName())); 
        }
    }

    /**
     * Test that the max length defaults to the appropriate value for a node
     */
    public void testDefaultMaxLength()
    throws Exception {
        String mfile = (String)this.getTestData().getTestCaseParameter(
                "testSingleArchetypeDescripor", "normal", "mappingFile");
        String afile = (String)this.getTestData().getTestCaseParameter(
                "testSingleArchetypeDescripor", "normal", "archetypeFile");
        
        ArchetypeDescriptors descriptors = getArchetypeDescriptors(mfile, afile);
        assertTrue(descriptors.getArchetypeDescriptors().length == 1);
        
        // test that the archetype display name defaults to the name
        ArchetypeDescriptor descriptor = (ArchetypeDescriptor)descriptors
                    .getArchetypeDescriptors()[0];
        
        // iterate through the top level nodes and enusre that the 
        // display name defaults to the name
        for (NodeDescriptor node : descriptor.getNodeDescriptors()) {
            assertTrue(node.getMaxLength() == NodeDescriptor.DEFAULT_MAX_LENGTH);
        }
    }
    
    /**
     * Test that IsLookup works for a specified node
     */
    public void testIsLookup()
    throws Exception {
        String mfile = (String)this.getTestData().getTestCaseParameter(
                "testIsLookup", "normal", "mappingFile");
        String afile = (String)this.getTestData().getTestCaseParameter(
                "testIsLookup", "normal", "archetypeFile");
        String nodeName = (String)this.getTestData().getTestCaseParameter(
                "testIsLookup", "normal", "nodeName");
        
        ArchetypeDescriptors descriptors = getArchetypeDescriptors(mfile, afile);
        assertTrue(descriptors.getArchetypeDescriptors().length == 1);
        
        NodeDescriptor ndesc = descriptors.getArchetypeDescriptors()[0]
                         .getNodeDescriptor(nodeName);
        assertTrue(ndesc.isLookup());
        
    }
    
    /**
     * Get archetype descriptors
     * 
     * @param mfile
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
