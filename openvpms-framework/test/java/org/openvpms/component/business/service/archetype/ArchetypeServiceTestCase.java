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


package org.openvpms.component.business.service.archetype;


// java-core
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

//openvpms-framework
import org.openvpms.component.business.service.archetype.ArchetypeRecord;
import org.openvpms.component.business.service.archetype.ArchetypeService;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;


// openvpms-test-component
import org.openvpms.component.system.common.test.BaseTestCase;
import org.openvpms.component.system.service.uuid.IUUIDGenerator;
import org.openvpms.component.system.service.uuid.JUGGenerator;
import org.safehaus.uuid.UUIDGenerator;

/**
 * Test the {@link org.openvpms.component.business.service.archetype.IArchetypeService}
 *
 * @author   <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class ArchetypeServiceTestCase extends BaseTestCase {
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ArchetypeServiceTestCase.class);
    }

    /**
     * Constructor for ArchetypeServiceTestCase.
     * 
     * @param name
     */
    public ArchetypeServiceTestCase(String name) {
        super(name);
    }

    /**
     * Test the creation of a new registry with a null filename
     */
    public void testCreationWithNullFileName() 
    throws Exception {
        try {
            new ArchetypeService(null, null);
        } catch (ArchetypeServiceException exception) {
            assertTrue(exception.getErrorCode() == 
                ArchetypeServiceException.ErrorCode.NoFileSpecified);
        }
    }
    
    /**
     * Test the creation of a new registry with invalid filename
     */
    public void testCreationWithInvalidFileName() 
    throws Exception {
        try {
            new ArchetypeService(createUUIDGenerator(),"file-does-not-exist");
        } catch (ArchetypeServiceException exception) {
            assertTrue(exception.getErrorCode() == 
                ArchetypeServiceException.ErrorCode.InvalidFile);
        }
    }
   
    /**
     * Test reading a valid archetype registry file
     */
    public void testCreationWithValidFileContent()
    throws Exception {
        Vector testData = (Vector)this.getTestData().getTestCaseParameter(
                "testCreationWithValidFileContent", "valid-files", "files");
        Iterator iter = testData.iterator();
        while (iter.hasNext()) {
            new ArchetypeService(createUUIDGenerator(),(String)iter.next());
        }
    }
    
    /**
     * Test reading an invalid archetype registry file
     */
    public void testCreationWithInvalidFileContent()
    throws Exception {
        Vector testData = (Vector)this.getTestData().getTestCaseParameter(
                "testCreationWithInvalidFileContent", "invalid-files", "files");
        Iterator iter = testData.iterator();
        while (iter.hasNext()) {
            try {
                new ArchetypeService(createUUIDGenerator(), (String)iter.next());
            } catch (ArchetypeServiceException exception) {
                assertTrue(exception.getErrorCode() == 
                    ArchetypeServiceException.ErrorCode.InvalidFile);
            }
        }
    }

    /**
     * Test valid retrieval from registry
     */
    public void testValidEntryRetrieval()
    throws Exception {
        String validFile = (String)this.getTestData().getTestCaseParameter(
                "testValidEntryRetrieval", "valid-retrieval", "file");
        Vector archetypes = (Vector)this.getTestData().getTestCaseParameter(
                "testValidEntryRetrieval", "valid-retrieval", "archetypes");
        
        ArchetypeService registry = new ArchetypeService(createUUIDGenerator(),
                validFile);
        Iterator iter = archetypes.iterator();
        while (iter.hasNext()) {
            ArchetypeRecord record = registry.getArchetypeRecord((String)iter.next());
            assertTrue(record != null);
            assertTrue(record.getArchetypeId() != null);
            assertTrue(record.getInfoModelClass() != null);
            assertTrue(record.getInfoModelClass() != null);
        }
    }

    /**
     * Test invalid retrieval from registry
     */
    public void testInvalidEntryRetrieval()
    throws Exception {
        String validFile = (String)this.getTestData().getTestCaseParameter(
                "testInvalidEntryRetrieval", "invalid-retrieval", "file");
        Vector archetypes = (Vector)this.getTestData().getTestCaseParameter(
                "testInvalidEntryRetrieval", "invalid-retrieval", "archetypes");
        
        ArchetypeService registry = new ArchetypeService(createUUIDGenerator(), 
                validFile);
        Iterator iter = archetypes.iterator();
        while (iter.hasNext()) {
            assertTrue(registry.getArchetypeRecord((String)iter.next()) == null);
        }
    }
    
    /**
     * Test the creation on an archetype service using directory and extension
     * arguments. This depends on the adl files being copied to the target
     * directory
     */
    public void testLoadingArchetypesFromDir()
    throws Exception {
        String dir = (String)this.getTestData().getTestCaseParameter(
                "testLoadingArchetypesFromDir", "normal", "dir");
        String extension = (String)this.getTestData().getTestCaseParameter(
                "testLoadingArchetypesFromDir", "normal", "extension");
        int recordCount1 = ((Integer)this.getTestData().getTestCaseParameter(
                "testLoadingArchetypesFromDir", "normal", "recordCount1"))
                .intValue();
        
        ArchetypeService registry = new ArchetypeService(createUUIDGenerator(),
                dir, new String[]{extension});
        assertTrue(registry.getArchetypeRecords().length == recordCount1);
    }
    
    /**
     * Test the retrieval of archetypes using regular expression. IT uses the 
     * adl files, which reside in src/archetypes/
     */
    public void testRetrievalThroughRegularExpression()
    throws Exception {
        Hashtable params = this.getTestData().getTestCaseParams(
                "testRetrievalThroughRegularExpression", "normal");
        
        ArchetypeService registry = new ArchetypeService(createUUIDGenerator(),
                (String)params.get("dir"), 
                new String[]{(String)params.get("extension")});

        // test retrieval of all records that start with entityRelationship
        assertTrue(registry.getArchetypeRecords("entityRelationship\\..*").length 
                == ((Integer)params.get("recordCount1")).intValue());
        
        // test retrieval for anything with animal
        assertTrue(registry.getArchetypeRecords(".*animal.*").length 
                == ((Integer)params.get("recordCount2")).intValue());
        
        // test retrieval for anything that starts with person
        assertTrue(registry.getArchetypeRecords("person.*").length 
                == ((Integer)params.get("recordCount3")).intValue());
        
        // test retrieval for anything that matchers person\\.person
        assertTrue(registry.getArchetypeRecords("person\\.person").length 
                == ((Integer)params.get("recordCount4")).intValue());
    }
    
    /**
     * Create a UUID geenrator
     * 
     * @return IUUIDGenerator
     */
    private IUUIDGenerator createUUIDGenerator() {
        return new JUGGenerator(UUIDGenerator.getInstance()
               .getDummyAddress().toString());  
    }
}
