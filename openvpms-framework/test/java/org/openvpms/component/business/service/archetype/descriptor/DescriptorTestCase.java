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
