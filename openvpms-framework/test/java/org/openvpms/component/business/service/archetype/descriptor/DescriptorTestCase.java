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
import java.util.List;

// castor 
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;

// commons-lang
import org.apache.commons.lang.StringUtils;

// sax
import org.xml.sax.InputSource;

// openvpms-framework
import org.openvpms.component.system.common.test.BaseTestCase;


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
        Hashtable gparams = getTestData().getGlobalParams();
        String mfile = (String)gparams.get("mappingFile");
        String afile = (String)gparams.get("archetypeFile");
        
        // load the archetypes
        ArchetypeDescriptors descriptors = getArchetypeDescriptors(mfile, afile);
        assertTrue(descriptors.getArchetypeDescriptorsAsMap().size() == 1);
    }

    /**
     * Test that we can read the assertion types from an XML document
     */
    public void testAssertionTypeDescriptors()
    throws Exception {
        Hashtable gparams = getTestData().getGlobalParams();
        String mfile = (String)gparams.get("assertionMappingFile");
        String afile = (String)gparams.get("assertionFile");
        
        
        // load the mapping file
        // load the assertion types
        AssertionTypeDescriptors descriptors = getAssertionTypeDescriptors(mfile, afile);
        assertTrue(descriptors.getAssertionTypeDescriptors().size() == 5);
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
        assertTrue(descriptors.getArchetypeDescriptorsAsMap().size() == 1);
        
        // test that the archetype display name defaults to the name
        ArchetypeDescriptor descriptor = (ArchetypeDescriptor)descriptors
                    .getArchetypeDescriptors()[0];
        assertTrue(descriptor.getDisplayName().equals(descriptor.getName()));
        
        // iterate through the top level nodes and enusre that the 
        // display name is not null
        for (NodeDescriptor node : descriptor.getNodeDescriptors()) {
            assertTrue(StringUtils.isEmpty(node.getDisplayName()) == false);
        }
    }

    /**
     * Test that the max length defaults to the appropriate value for a node
     */
    public void testDefaultMaxLength()
    throws Exception {
        Hashtable gparams = getTestData().getGlobalParams();
        String mfile = (String)gparams.get("mappingFile");
        String afile = (String)gparams.get("archetypeFile");
        
        // load the archetypes
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
        Hashtable gparams = getTestData().getGlobalParams();
        String mfile = (String)gparams.get("mappingFile");
        String afile = (String)gparams.get("archetypeFile");
        String nodeName = (String)this.getTestData().getTestCaseParameter(
                "testIsLookup", "normal", "nodeName");
        
        ArchetypeDescriptors descriptors = getArchetypeDescriptors(mfile, afile);
        assertTrue(descriptors.getArchetypeDescriptors().length == 1);
        
        NodeDescriptor ndesc = descriptors.getArchetypeDescriptors()[0]
                         .getNodeDescriptor(nodeName);
        assertTrue(ndesc.isLookup());
        
    }
    
    /**
     * Test that getAllNodeDescriptors works
     */
    public void testGetAllNodeDescriptors()
    throws Exception {
        Hashtable gparams = getTestData().getGlobalParams();
        String mfile = (String)gparams.get("mappingFile");
        String afile = (String)gparams.get("archetypeFile");
        
        ArchetypeDescriptors descriptors = getArchetypeDescriptors(mfile, afile);
        assertTrue(descriptors.getArchetypeDescriptors().length == 1);
        
        ArchetypeDescriptor adesc = descriptors.getArchetypeDescriptors()[0];
        List<NodeDescriptor> ndesc = adesc.getAllNodeDescriptors();
        assertTrue(ndesc.size() == 7);
    }
    
    /**
     * Test that the isHidden method works
     */
    public void testIsHidden()
    throws Exception {
        Hashtable gparams = getTestData().getGlobalParams();
        String mfile = (String)gparams.get("mappingFile");
        String afile = (String)gparams.get("archetypeFile");
        String nodeName = (String)this.getTestData().getTestCaseParameter(
                "testIsHidden", "normal", "nodeName");
        
        ArchetypeDescriptors descriptors = getArchetypeDescriptors(mfile, afile);
        assertTrue(descriptors.getArchetypeDescriptors().length == 1);
        
        ArchetypeDescriptor adesc = descriptors.getArchetypeDescriptors()[0];
        NodeDescriptor ndesc  = adesc.getNodeDescriptor(nodeName);
        assertTrue(ndesc != null);
        assertTrue(ndesc.isHidden());
    }
    
    /**
     * Test the archetype range helper method.
     */
    public void testArchetypeRange()
    throws Exception {
        
        Hashtable gparams = getTestData().getGlobalParams();
        String mfile = (String)gparams.get("mappingFile");
        String afile = (String)this.getTestData().getTestCaseParameter(
                "testArchetypeRange", "normal", "archetypeFile");
        String nodeName = (String)this.getTestData().getTestCaseParameter(
                "testArchetypeRange", "normal", "nodeName");
        
        ArchetypeDescriptors descriptors = getArchetypeDescriptors(mfile, afile);
        assertTrue(descriptors.getArchetypeDescriptors().length == 1);
        
        ArchetypeDescriptor adesc = descriptors.getArchetypeDescriptors()[0];
        NodeDescriptor ndesc  = adesc.getNodeDescriptor(nodeName);
        assertTrue(ndesc != null);
        assertTrue(ndesc.getArchetypeRange().length == 2);
    }
    
    /**
     * Test that the default node descriptor is being correctly inserted into
     * the archetype descriptor.
     */
    public void testIdNodeDescriptorExists()
    throws Exception {
        Hashtable gparams = getTestData().getGlobalParams();
        String mfile = (String)gparams.get("mappingFile");
        String afile = (String)gparams.get("archetypeFile");
        
        ArchetypeDescriptors descriptors = getArchetypeDescriptors(mfile, afile);
        assertTrue(descriptors.getArchetypeDescriptors().length == 1);
        
        ArchetypeDescriptor adesc = descriptors.getArchetypeDescriptors()[0];
        assertTrue(adesc.getNodeDescriptor(NodeDescriptor.IDENTIFIER_NODE_NAME) != null);
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
    
    /**
     * Get assertion type descriptors
     * 
     * @param mfile
     *            the mapping file
     * @param afile
     *            the assertion type descriptor file            
     * @return AssertionTypeDescriptors
     * @throws Exception
     */
    private AssertionTypeDescriptors getAssertionTypeDescriptors(String mfile, String afile)
    throws Exception {
        Mapping mapping = new Mapping();
        mapping.loadMapping(new InputSource(new InputStreamReader(
                Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(mfile))));
        
        // set up the unmarshaller
        Unmarshaller unmarshaller = new Unmarshaller(mapping);
        return  (AssertionTypeDescriptors)unmarshaller.unmarshal(
                new InputSource(new InputStreamReader(
                Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(afile))));
    }
}
