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

package org.openvpms.component.business.service.archetype.descriptor.cache;

import junit.framework.TestCase;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;

import java.io.File;


/**
 * Test the {@link ArchetypeDescriptorCacheFS}.
 *
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeDescriptorCacheFSTestCase extends TestCase {

    /**
     * Test the creation of a new registry with a null filename.
     */
    public void testCreationWithNullFileName() {
        String assertionFile = "org/openvpms/archetype/assertionTypes.xml";
        try {
            new ArchetypeDescriptorCacheFS(null, null, assertionFile);
        } catch (ArchetypeDescriptorCacheException exception) {
            assertEquals(
                    ArchetypeDescriptorCacheException.ErrorCode.NoDirSpecified,
                    exception.getErrorCode());
        }
    }

    /**
     * Test the creation of a new registry with invalid filename.
     */
    public void testCreationWithInvalidFileName() {
        String assertionFile = "org/openvpms/archetype/assertionTypes.xml";
        try {
            new ArchetypeDescriptorCacheFS("file-does-not-exist", assertionFile);
        } catch (ArchetypeDescriptorCacheException exception) {
            assertEquals(ArchetypeDescriptorCacheException.ErrorCode.InvalidFile,
                         exception.getErrorCode());
        }
    }

    /**
     * Test reading a valid archetype registry file
     */
    public void testCreationWithValidFileContent() {
        String[] files = {"valid-archetype-file-1.adl",
                          "valid-archetype-file-2.adl",
                          "valid-archetype-file-3.adl"};
        String assertionFile = "org/openvpms/archetype/assertionTypes.xml";

        for (String file : files) {
            String path = getPath(file);
            new ArchetypeDescriptorCacheFS(path, assertionFile);
        }
    }

    /**
     * Test reading an invalid archetype file.
     */
    public void testCreationWithInvalidFileContent() {
        String[] files = {"invalid-archetype-file-1.xml",
                          "invalid-archetype-file-2.xml",
                          "invalid-archetype-file-3.xml"};
        String assertionFile = "org/openvpms/archetype/assertionTypes.xml";

        for (String file : files) {
            String path = getPath(file);
            try {
                new ArchetypeDescriptorCacheFS(path, assertionFile);
            } catch (ArchetypeDescriptorCacheException exception) {
                assertEquals(
                        ArchetypeDescriptorCacheException.ErrorCode.InvalidFile, exception.getErrorCode());
            }
        }
    }

    /**
     * Test retrieval from the cache.
     */
    public void testRetrieval() {
        String assertionFile = "org/openvpms/archetype/assertionTypes.xml";
        String validFile = getPath("valid-archetype-file-2.adl");
        String[] archetypes = {"party.personcustomer", "party.animalpet", "party.supplier", "party.administrator"};

        // verify the expected descriptors are there
        ArchetypeDescriptorCacheFS cache = new ArchetypeDescriptorCacheFS(validFile, assertionFile);
        for (String archetype : archetypes) {
            ArchetypeDescriptor descriptor = cache.getArchetypeDescriptor(archetype);
            assertNotNull(descriptor);
            assertNotNull(descriptor.getType());
        }

        // verify an invalid descriptor produces no results
        assertNull(cache.getArchetypeDescriptor("party.personcustomer1"));
    }

    /**
     * Test the creation of a cache using directory and extension arguments.
     */
    public void testLoadingArchetypesFromDir() {
        String dir = getPath(".");
        String assertionFile = getPath("valid-assertion-type-file-1.xml");
        String extension = "adl";
        int count = 5;

        ArchetypeDescriptorCacheFS cache = new ArchetypeDescriptorCacheFS(dir, new String[]{extension}, assertionFile);
        assertEquals(count, cache.getArchetypeDescriptors().size());
    }

    /**
     * Test the retrieval of archetypes using regular expression.
     */
    public void testRetrievalByShortName() {
        String dir = getPath(".");
        String assertionFile = getPath("valid-assertion-type-file-1.xml");
        String extension = "adl";

        ArchetypeDescriptorCacheFS cache = new ArchetypeDescriptorCacheFS(dir, new String[]{extension}, assertionFile);

        // test retrieval of all records that start with party
        assertEquals(5, cache.getArchetypeDescriptors("party.*").size());

        // test retrieval for anything with party.person
        assertEquals(2, cache.getArchetypeDescriptors("*party.person*").size());

        // test retrieval for anything that starts with party.person
        assertEquals(2, cache.getArchetypeDescriptors("party.person*").size());

        // test retrieval for anything that matches party.person
        assertEquals(1, cache.getArchetypeDescriptors("party.person").size());
    }

    /**
     * Helper to get the path of a file relative to the current package.
     *
     * @param file the file name
     * @return the path of the file
     */
    private String getPath(String file) {
        return getClass().getPackage().getName().replace('.', File.separatorChar) + File.separatorChar + file;
    }

}
