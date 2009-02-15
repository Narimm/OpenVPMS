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

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.system.common.test.BaseTestCase;

import java.util.Hashtable;
import java.util.Vector;


/**
 * Test the
 * {@link org.openvpms.component.business.service.archetype.IArchetypeService}
 *
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeDescriptorCacheFSTestCase extends BaseTestCase {

    /**
     * Test the creation of a new registry with a null filename
     */
    public void testCreationWithNullFileName() throws Exception {
        String assertionFile = (String) this.getTestData()
                .getTestCaseParameter("testCreationWithNullFileName", "normal",
                        "assertionFile");
        try {
            new ArchetypeDescriptorCacheFS(null, null, assertionFile);
        } catch (ArchetypeDescriptorCacheException exception) {
            assertEquals(
                ArchetypeDescriptorCacheException.ErrorCode.NoDirSpecified,
                exception.getErrorCode());
        }
    }

    /**
     * Test the creation of a new registry with invalid filename
     */
    public void testCreationWithInvalidFileName() throws Exception {
        String assertionFile = (String) this.getTestData()
                .getTestCaseParameter("testCreationWithInvalidFileName",
                        "normal", "assertionFile");
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
    public void testCreationWithValidFileContent() throws Exception {
        String assertionFile = (String) this.getTestData()
                .getTestCaseParameter("testCreationWithValidFileContent",
                        "valid-files", "assertionFile");
        Vector testData = (Vector) this.getTestData().getTestCaseParameter(
                "testCreationWithValidFileContent", "valid-files", "files");
        for (Object str : testData) {
            new ArchetypeDescriptorCacheFS((String) str, assertionFile);
        }
    }

    /**
     * Test reading an invalid archetype registry file
     */
    public void testCreationWithInvalidFileContent() throws Exception {
        String assertionFile = (String) this.getTestData()
                .getTestCaseParameter("testCreationWithInvalidFileContent",
                        "invalid-files", "assertionFile");
        Vector testData = (Vector) this.getTestData().getTestCaseParameter(
                "testCreationWithInvalidFileContent", "invalid-files", "files");
        for (Object str : testData) {
            try {
                new ArchetypeDescriptorCacheFS((String) str, assertionFile);
            } catch (ArchetypeDescriptorCacheException exception) {
                assertEquals(
                        ArchetypeDescriptorCacheException.ErrorCode.InvalidFile,
                        exception.getErrorCode());
            }
        }
    }

    /**
     * Test valid retrieval from registry
     */
    public void testValidEntryRetrieval() throws Exception {
        String assertionFile = (String) this.getTestData()
                .getTestCaseParameter("testValidEntryRetrieval",
                        "valid-retrieval", "assertionFile");
        String validFile = (String) this.getTestData().getTestCaseParameter(
                "testValidEntryRetrieval", "valid-retrieval", "file");
        Vector archetypes = (Vector) this.getTestData().getTestCaseParameter(
                "testValidEntryRetrieval", "valid-retrieval", "archetypes");

        ArchetypeDescriptorCacheFS cache
                = new ArchetypeDescriptorCacheFS(validFile, assertionFile);
        for (Object archetype : archetypes) {
            String key = (String) archetype;
            ArchetypeDescriptor descriptor = cache.getArchetypeDescriptor(key);
            assertNotNull("Looking for " + key, descriptor);
            assertNotNull(descriptor.getType());
        }
    }

    /**
     * Test invalid retrieval from registry
     */
    public void testInvalidEntryRetrieval() throws Exception {
        String assertionFile = (String) this.getTestData()
                .getTestCaseParameter("testInvalidEntryRetrieval",
                        "invalid-retrieval", "assertionFile");
        String validFile = (String) this.getTestData().getTestCaseParameter(
                "testInvalidEntryRetrieval", "invalid-retrieval", "file");
        Vector archetypes = (Vector) this.getTestData().getTestCaseParameter(
                "testInvalidEntryRetrieval", "invalid-retrieval", "archetypes");

        ArchetypeDescriptorCacheFS cache = new ArchetypeDescriptorCacheFS(validFile,
                assertionFile);
        for (Object archetype : archetypes) {
            String key = (String) archetype;
            assertNull(("Looking for " + key),
                       cache.getArchetypeDescriptor(key));
        }
    }

    /**
     * Test the creation on an archetype service using directory and extension
     * arguments. This depends on the adl files being copied to the target
     * directory
     */
    public void testLoadingArchetypesFromDir() throws Exception {
        Hashtable params = getTestData().getGlobalParams();
        String dir = (String) params.get("dir");
        String assertionFile = (String) params.get("assertionFile");
        String extension = (String) params.get("extension");
        int recordCount1 = (Integer) this.getTestData().getTestCaseParameter(
                "testLoadingArchetypesFromDir", "normal", "recordCount1");

        ArchetypeDescriptorCacheFS cache = new ArchetypeDescriptorCacheFS(dir,
                new String[] {extension}, assertionFile);
        assertEquals(recordCount1, cache.getArchetypeDescriptors().size());
    }

    /**
     * Test the retrieval of archetypes using regular expression. IT uses the
     * adl files, which reside in src/archetypes/
     */
    public void testRetrievalByShortName() throws Exception {
        Hashtable cparams = getTestData().getGlobalParams();
        Hashtable params = this.getTestData().getTestCaseParams(
                "testRetrievalByShortName", "normal");

        ArchetypeDescriptorCacheFS cache = new ArchetypeDescriptorCacheFS(
                (String) cparams.get("dir"),
                new String[] { (String) cparams.get("extension") },
                (String) cparams.get("assertionFile"));

        // test retrieval of all records that start with entityRelationship
        assertEquals(((Integer) params.get("recordCount1")).intValue(),
                     cache.getArchetypeDescriptors("entityRelationship.*").size());

        // test retrieval for anything with party.animal
        assertEquals(((Integer) params.get("recordCount2")).intValue(),
                     cache.getArchetypeDescriptors("*party.animal*").size());

        // test retrieval for anything that starts with party.person
        assertEquals(((Integer) params.get("recordCount3")).intValue(),
                   cache.getArchetypeDescriptors("party.person*").size());

        // test retrieval for anything that matches party.person
        assertEquals(((Integer) params.get("recordCount4")).intValue(),
                     cache.getArchetypeDescriptors("party.person").size());
    }

}
