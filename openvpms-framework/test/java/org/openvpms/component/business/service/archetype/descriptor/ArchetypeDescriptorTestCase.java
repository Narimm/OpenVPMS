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

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptors;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptors;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.system.common.test.BaseTestCase;
import org.xml.sax.InputSource;

import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.List;


/**
 * Test the all the archetype related descriptors.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
@SuppressWarnings("HardCodedStringLiteral")
public class ArchetypeDescriptorTestCase extends BaseTestCase {

    /**
     * Test that we can read an archetype file with a single archetype.
     *
     * @throws Exception for any error
     */
    public void testSingleArchetypeDescripor() throws Exception {
        Hashtable gparams = getTestData().getGlobalParams();
        String mfile = (String) gparams.get("mappingFile");
        String afile = (String) gparams.get("archetypeFile");

        // load the archetypes
        ArchetypeDescriptors descriptors = getArchetypeDescriptors(mfile,
                                                                   afile);
        assertTrue(descriptors.getArchetypeDescriptors().size() == 1);
    }

    /**
     * Test that we can read the assertion types from an XML document.
     *
     * @throws Exception for any error
     */
    public void testAssertionTypeDescriptors() throws Exception {
        Hashtable gparams = getTestData().getGlobalParams();
        String mfile = (String) gparams.get("assertionMappingFile");
        String afile = (String) gparams.get("assertionFile");

        // load the mapping file
        // load the assertion types
        AssertionTypeDescriptors descriptors = getAssertionTypeDescriptors(
                mfile, afile);
        assertEquals(8, descriptors.getAssertionTypeDescriptors().size());
    }

    /**
     * Test that getAllNodeDescriptors works.
     *
     * @throws Exception for any error
     */
    public void testGetAllNodeDescriptors() throws Exception {
        Hashtable gparams = getTestData().getGlobalParams();
        String mfile = (String) gparams.get("mappingFile");
        String afile = (String) gparams.get("archetypeFile");

        ArchetypeDescriptors descriptors = getArchetypeDescriptors(mfile,
                                                                   afile);
        assertTrue(descriptors.getArchetypeDescriptors().size() == 1);

        ArchetypeDescriptor adesc = descriptors.getArchetypeDescriptorsAsArray()[0];
        List<NodeDescriptor> ndesc = adesc.getAllNodeDescriptors();
        assertTrue(ndesc.size() == 7);
    }

    /**
     * Test that the default node descriptor is being correctly inserted into
     * the archetype descriptor.
     *
     * @throws Exception for any error
     */
    public void testIdNodeDescriptorExists() throws Exception {
        Hashtable gparams = getTestData().getGlobalParams();
        String mfile = (String) gparams.get("mappingFile");
        String afile = (String) gparams.get("archetypeFile");

        ArchetypeDescriptors descriptors = getArchetypeDescriptors(mfile,
                                                                   afile);
        assertTrue(descriptors.getArchetypeDescriptors().size() == 1);

        ArchetypeDescriptor adesc = descriptors.getArchetypeDescriptorsAsArray()[0];
        assertTrue(adesc.getNodeDescriptor(
                NodeDescriptor.IDENTIFIER_NODE_NAME) != null);
    }

    /**
     * Test that it can retrieve the simple and complex node descriptors.
     *
     * @throws Exception for any error
     */
    public void testGetNodeDescriptorMethods() throws Exception {
        Hashtable gparams = getTestData().getGlobalParams();
        String mfile = (String) gparams.get("mappingFile");
        String afile = (String) this.getTestData().getTestCaseParameter(
                "testGetNodeDescriptorMethods", "normal", "archetypeFile");

        ArchetypeDescriptors descriptors = getArchetypeDescriptors(mfile,
                                                                   afile);
        assertEquals(2, descriptors.getArchetypeDescriptors().size());
        ArchetypeDescriptor descriptor = descriptors.getArchetypeDescriptors().get(
                "party.person");
        assertEquals("party.person", descriptor.getType().getShortName());
        assertEquals(11, descriptor.getAllNodeDescriptors().size());
        assertEquals(7, descriptor.getSimpleNodeDescriptors().size());
        assertEquals(4, descriptor.getComplexNodeDescriptors().size());
    }

    /**
     * Get archetype descriptors.
     *
     * @param mfile the mapping file
     * @param afile the archetype descriptor file
     * @return ArchetypeDescriptors
     * @throws Exception for any error
     */
    private ArchetypeDescriptors getArchetypeDescriptors(String mfile,
                                                         String afile)
            throws Exception {
        Mapping mapping = new Mapping();
        mapping.loadMapping(new InputSource(new InputStreamReader(
                Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream(mfile))));

        // set up the unmarshaller
        Unmarshaller unmarshaller = new Unmarshaller(mapping);
        return (ArchetypeDescriptors) unmarshaller.unmarshal(
                new InputSource(new InputStreamReader(
                        Thread.currentThread().getContextClassLoader()
                                .getResourceAsStream(afile))));
    }

    /**
     * Get assertion type descriptors.
     *
     * @param mfile the mapping file
     * @param afile the assertion type descriptor file
     * @return AssertionTypeDescriptors
     * @throws Exception for any error
     */
    private AssertionTypeDescriptors getAssertionTypeDescriptors(String mfile,
                                                                 String afile)
            throws Exception {
        Mapping mapping = new Mapping();
        mapping.loadMapping(new InputSource(new InputStreamReader(
                Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream(mfile))));

        // set up the unmarshaller
        Unmarshaller unmarshaller = new Unmarshaller(mapping);
        return (AssertionTypeDescriptors) unmarshaller.unmarshal(
                new InputSource(new InputStreamReader(
                        Thread.currentThread().getContextClassLoader()
                                .getResourceAsStream(afile))));
    }
}
