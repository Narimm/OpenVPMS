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


package org.openvpms.component.business.service.archetype.castor;


// java-core
import java.util.Iterator;
import java.util.Vector;

//openvpms-archetype-registry
import org.openvpms.component.business.service.archetype.ArchetypeRecord;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;

// openvpms-test-component
import org.openvpms.component.system.common.test.BaseTestCase;

/**
 * Test the {@link org.openvpms.component.business.service.archetype.IArchetypeRegistry}
 *
 * @author   <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class CastorArchetypeRegistryTestCase extends BaseTestCase {
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(CastorArchetypeRegistryTestCase.class);
    }

    /**
     * Constructor for CastorArchetypeRegistryTestCase.
     * 
     * @param name
     */
    public CastorArchetypeRegistryTestCase(String name) {
        super(name);
    }

    /**
     * Test the creation of a new registry with a null filename
     */
    public void testCreationWithNullFileName() 
    throws Exception {
        try {
            new CastorArchetypeRegistry(null);
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
            new CastorArchetypeRegistry("file-does-not-exist");
        } catch (ArchetypeServiceException exception) {
            assertTrue(exception.getErrorCode() == 
                ArchetypeServiceException.ErrorCode.FailedToInitializeRegistry);
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
            new CastorArchetypeRegistry((String)iter.next());
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
                new CastorArchetypeRegistry((String)iter.next());
            } catch (ArchetypeServiceException exception) {
                assertTrue(exception.getErrorCode() == 
                    ArchetypeServiceException.ErrorCode.FailedToInitializeRegistry);
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
        
        CastorArchetypeRegistry registry = new CastorArchetypeRegistry(validFile);
        Iterator iter = archetypes.iterator();
        while (iter.hasNext()) {
            ArchetypeRecord record = registry.getArchetypeRecord((String)iter.next());
            assertTrue(record != null);
            assertTrue(record.getArchetypeId() != null);
            assertTrue(record.getInfoModelVersion() != null);
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
        
        CastorArchetypeRegistry registry = new CastorArchetypeRegistry(validFile);
        Iterator iter = archetypes.iterator();
        while (iter.hasNext()) {
            assertTrue(registry.getArchetypeRecord((String)iter.next()) == null);
        }
    }
}
