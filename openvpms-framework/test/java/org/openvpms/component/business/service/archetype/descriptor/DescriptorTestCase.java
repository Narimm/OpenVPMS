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
        assertTrue(descriptors.getArchetypeDescriptors().size() == 1);
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
}
