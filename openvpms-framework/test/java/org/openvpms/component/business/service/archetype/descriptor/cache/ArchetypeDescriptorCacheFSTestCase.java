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

// java-core
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

// openvpms-framework
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;

// openvpms-test-component
import org.openvpms.component.system.common.test.BaseTestCase;

/**
 * Test the
 * {@link org.openvpms.component.business.service.archetype.IArchetypeService}
 * 
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeDescriptorCacheFSTestCase extends BaseTestCase {

    /**
     * @param args  
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ArchetypeDescriptorCacheFSTestCase.class);
    }

    /**
     * Constructor for ArchetypeDescriptorCacheFSTestCase.
     * 
     * @param name
     */
    public ArchetypeDescriptorCacheFSTestCase(String name) {
        super(name);
    }

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
            assertTrue(exception.getErrorCode() == 
                ArchetypeDescriptorCacheException.ErrorCode.NoDirSpecified);
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
            assertTrue(exception.getErrorCode() == 
                ArchetypeDescriptorCacheException.ErrorCode.InvalidFile);
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
        Iterator iter = testData.iterator();
        while (iter.hasNext()) {
            new ArchetypeDescriptorCacheFS((String) iter.next(), assertionFile);
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
        Iterator iter = testData.iterator();
        while (iter.hasNext()) {
            try {
                new ArchetypeDescriptorCacheFS((String) iter.next(), assertionFile);
            } catch (ArchetypeDescriptorCacheException exception) {
                assertTrue(exception.getErrorCode() == 
                    ArchetypeDescriptorCacheException.ErrorCode.InvalidFile);
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

        ArchetypeDescriptorCacheFS cache = new ArchetypeDescriptorCacheFS(validFile,
                assertionFile);
        Iterator iter = archetypes.iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            ArchetypeDescriptor descriptor = cache.getArchetypeDescriptor(key);
            assertTrue(("Looking for " + key), (descriptor != null));
            assertTrue(descriptor.getType() != null);
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
        Iterator iter = archetypes.iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            assertTrue(("Looking for " + key), 
                    cache.getArchetypeDescriptor(key) == null);
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
        int recordCount1 = ((Integer) this.getTestData().getTestCaseParameter(
                "testLoadingArchetypesFromDir", "normal", "recordCount1"))
                .intValue(); 

        ArchetypeDescriptorCacheFS cache = new ArchetypeDescriptorCacheFS(dir,
                new String[] {extension}, assertionFile);
        assertTrue("Count must be " + cache.getArchetypeDescriptors().size(),
                cache.getArchetypeDescriptors().size() == recordCount1);
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
        assertTrue(cache.getArchetypeDescriptors("entityRelationship\\..*").size() == ((Integer) params
                .get("recordCount1")).intValue());

        // test retrieval for anything with animal
        assertTrue(cache.getArchetypeDescriptors(".*pet.*").size() == ((Integer) params
                .get("recordCount2")).intValue());

        // test retrieval for anything that starts with person
        assertTrue(cache.getArchetypeDescriptors("person.*").size() == ((Integer) params
                .get("recordCount3")).intValue());

        // test retrieval for anything that matchers person\\.person
        assertTrue(cache.getArchetypeDescriptors("person\\.person").size() == ((Integer) params
                .get("recordCount4")).intValue());
    }

    /**
     * 
     */
    public void testRetrievalByReferenceModelName() throws Exception {
        Hashtable cparams = getTestData().getGlobalParams();
        Hashtable params = this.getTestData().getTestCaseParams(
                "testRetrievalByReferenceModelName", "normal");

        ArchetypeDescriptorCacheFS cache = new ArchetypeDescriptorCacheFS(
                (String) cparams.get("dir"),
                new String[] { (String) cparams.get("extension") },
                (String) cparams.get("assertionFile"));

        // test retrieval of all records that start with entityRelationship
        assertTrue(cache.getArchetypeDescriptorsByRmName("party").size() == ((Integer) params
                .get("recordCount1")).intValue());

        // test retrieval for anything with animal
        assertTrue(cache.getArchetypeDescriptorsByRmName("common").size() == ((Integer) params
                .get("recordCount2")).intValue());

        // test retrieval for anything that starts with person
        assertTrue(cache.getArchetypeDescriptorsByRmName("lookup").size() == ((Integer) params
                .get("recordCount3")).intValue());
    }
}
