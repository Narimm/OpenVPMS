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

// commons-lang
import org.apache.commons.lang.StringUtils;

//openvpms-framework
import org.openvpms.component.business.domain.im.party.Address;
import org.openvpms.component.business.domain.im.party.Animal;
import org.openvpms.component.business.domain.im.party.Person;
import org.openvpms.component.business.service.archetype.ArchetypeService;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.service.archetype.descriptor.NodeDescriptor;


// openvpms-test-component
import org.openvpms.component.system.common.test.BaseTestCase;

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
        String assertionFile = (String)this.getTestData().getTestCaseParameter(
                "testCreationWithNullFileName", "normal", "assertionFile");
        try {
            new ArchetypeService(null, null, assertionFile);
        } catch (ArchetypeServiceException exception) {
            assertTrue(exception.getErrorCode() == 
                ArchetypeServiceException.ErrorCode.NoDirSpecified);
        }
    }
    
    /**
     * Test the creation of a new registry with invalid filename
     */
    public void testCreationWithInvalidFileName() 
    throws Exception {
        String assertionFile = (String)this.getTestData().getTestCaseParameter(
                "testCreationWithInvalidFileName", "normal", "assertionFile");
        try {
            new ArchetypeService("file-does-not-exist", assertionFile);
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
        String assertionFile = (String)this.getTestData().getTestCaseParameter(
                "testCreationWithValidFileContent", "valid-files", "assertionFile");
        Vector testData = (Vector)this.getTestData().getTestCaseParameter(
                "testCreationWithValidFileContent", "valid-files", "files");
        Iterator iter = testData.iterator();
        while (iter.hasNext()) {
            new ArchetypeService((String)iter.next(), assertionFile);
        }
    }
    
    /**
     * Test reading an invalid archetype registry file
     */
    public void testCreationWithInvalidFileContent()
    throws Exception {
        String assertionFile = (String)this.getTestData().getTestCaseParameter(
                "testCreationWithInvalidFileContent", "invalid-files", "assertionFile");
        Vector testData = (Vector)this.getTestData().getTestCaseParameter(
                "testCreationWithInvalidFileContent", "invalid-files", "files");
        Iterator iter = testData.iterator();
        while (iter.hasNext()) {
            try {
                new ArchetypeService((String)iter.next(), assertionFile);
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
        String assertionFile = (String)this.getTestData().getTestCaseParameter(
                "testValidEntryRetrieval", "valid-retrieval", "assertionFile");
        String validFile = (String)this.getTestData().getTestCaseParameter(
                "testValidEntryRetrieval", "valid-retrieval", "file");
        Vector archetypes = (Vector)this.getTestData().getTestCaseParameter(
                "testValidEntryRetrieval", "valid-retrieval", "archetypes");
        
        ArchetypeService registry = new ArchetypeService(validFile, assertionFile);
        Iterator iter = archetypes.iterator();
        while (iter.hasNext()) {
            String key = (String)iter.next();
            ArchetypeDescriptor descriptor = registry.getArchetypeDescriptor(key);
            assertTrue(("Looking for " + key), (descriptor != null));
            assertTrue(descriptor.getArchetypeId() != null);
        }
    }

    /**
     * Test invalid retrieval from registry
     */
    public void testInvalidEntryRetrieval()
    throws Exception {
        String assertionFile = (String)this.getTestData().getTestCaseParameter(
                "testInvalidEntryRetrieval", "invalid-retrieval", "assertionFile");
        String validFile = (String)this.getTestData().getTestCaseParameter(
                "testInvalidEntryRetrieval", "invalid-retrieval", "file");
        Vector archetypes = (Vector)this.getTestData().getTestCaseParameter(
                "testInvalidEntryRetrieval", "invalid-retrieval", "archetypes");
        
        ArchetypeService registry = new ArchetypeService(validFile, assertionFile);
        Iterator iter = archetypes.iterator();
        while (iter.hasNext()) {
            String key = (String)iter.next();
            assertTrue(("Looking for " + key), registry.getArchetypeDescriptor(key) == null);
        }
    }
    
    /**
     * Test the creation on an archetype service using directory and extension
     * arguments. This depends on the adl files being copied to the target
     * directory
     */
    public void testLoadingArchetypesFromDir()
    throws Exception {
        Hashtable params = getTestData().getGlobalParams();
        String assertionFile = (String)getTestData().getTestCaseParameter(
                "testLoadingArchetypesFromDir", "normal", "assertionFile");
        String dir = (String)params.get("dir");
        String extension = (String)params.get("extension");
        int recordCount1 = ((Integer)this.getTestData().getTestCaseParameter(
                "testLoadingArchetypesFromDir", "normal", "recordCount1"))
                .intValue();
        
        ArchetypeService registry = new ArchetypeService(dir, 
                new String[]{extension}, assertionFile);
        assertTrue(registry.getArchetypeDescriptors().length == recordCount1);
    }
    
    /**
     * Test the retrieval of archetypes using regular expression. IT uses the 
     * adl files, which reside in src/archetypes/
     */
    public void testRetrievalByShortName()
    throws Exception {
        Hashtable cparams = getTestData().getGlobalParams();
        Hashtable params = this.getTestData().getTestCaseParams(
                "testRetrievalByShortName", "normal");
        
        ArchetypeService registry = new ArchetypeService((String)cparams.get("dir"), 
                new String[]{(String)cparams.get("extension")},
                (String)cparams.get("assertionFile"));

        // test retrieval of all records that start with entityRelationship
        assertTrue(registry.getArchetypeDescriptors("entityRelationship\\..*").length 
                == ((Integer)params.get("recordCount1")).intValue());
        
        // test retrieval for anything with animal
        assertTrue(registry.getArchetypeDescriptors(".*pet.*").length 
                == ((Integer)params.get("recordCount2")).intValue());
        
        // test retrieval for anything that starts with person
        assertTrue(registry.getArchetypeDescriptors("person.*").length 
                == ((Integer)params.get("recordCount3")).intValue());
        
        // test retrieval for anything that matchers person\\.person
        assertTrue(registry.getArchetypeDescriptors("person\\.person").length 
                == ((Integer)params.get("recordCount4")).intValue());
    }
    
    /**
     * 
     */
    public void testRetrievalByReferenceModelName()
    throws Exception {
        Hashtable cparams = getTestData().getGlobalParams();
        Hashtable params = this.getTestData().getTestCaseParams(
                "testRetrievalByReferenceModelName", "normal");
        
        ArchetypeService registry = new ArchetypeService(
                (String)cparams.get("dir"), 
                new String[]{(String)cparams.get("extension")},
                (String)cparams.get("assertionFile"));

        // test retrieval of all records that start with entityRelationship
        assertTrue(registry.getArchetypeDescriptorsByRmName("party").length 
                == ((Integer)params.get("recordCount1")).intValue());
        
        // test retrieval for anything with animal
        assertTrue(registry.getArchetypeDescriptorsByRmName("common").length 
                == ((Integer)params.get("recordCount2")).intValue());
        
        // test retrieval for anything that starts with person
        assertTrue(registry.getArchetypeDescriptorsByRmName("lookup").length 
                == ((Integer)params.get("recordCount3")).intValue());
    }

    /**
     * Test that we can successfully call createDefaultObject on every 
     * archetype loaded in the registry
     */
    public void testCreateDefaultObject()
    throws Exception {
        Hashtable params = getTestData().getGlobalParams();
        String assertionFile = (String)params.get("assertionFile");
        String dir = (String)params.get("dir");
        String extension = (String)params.get("extension");
        
        ArchetypeService registry = new ArchetypeService(dir, 
                new String[]{extension}, assertionFile);

        for (ArchetypeDescriptor descriptor : registry.getArchetypeDescriptors()) {
            assertTrue("Creating " + descriptor.getArchetypeQName(),
                    registry.createDefaultObject(descriptor.getArchetypeId()) != null);
        }
    }
    
    /**
     * Test create an instance of animal.pet
     */
    public void testCreationAnimalPet()
    throws Exception {
        Hashtable params = getTestData().getGlobalParams();
        String assertionFile = (String)params.get("assertionFile");
        String dir = (String)params.get("dir");
        String extension = (String)params.get("extension");
        
        ArchetypeService registry = new ArchetypeService(dir, 
                new String[]{extension}, assertionFile);

        Animal animal = (Animal)registry.createDefaultObject("animal.pet");
        assertTrue(animal != null);
    }
    
    /**
     * Test that a node value for an {@link IMObject can be retrieved from
     * a {@link NodeDescriptor}
     */
    public void testGetValueFromNodeDescriptor()
    throws Exception {
        Hashtable params = getTestData().getGlobalParams();
        String assertionFile = (String)params.get("assertionFile");
        String dir = (String)params.get("dir");
        String extension = (String)params.get("extension");
        
        ArchetypeService service = new ArchetypeService(dir, 
                new String[]{extension}, assertionFile);
        Person person = (Person)service.createDefaultObject("person.person");
        person.setTitle("Mr");
        person.setFirstName("Jim");
        person.setLastName("Alateras");
        
        NodeDescriptor ndesc = service.getArchetypeDescriptor(
                person.getArchetypeId()).getNodeDescriptor("description");
        assertTrue(ndesc.getValue(person).equals(person.getArchetypeId().getConcept()));

        ndesc = service.getArchetypeDescriptor(person
                .getArchetypeId()).getNodeDescriptor("name");
        assertTrue(ndesc.getValue(person).equals("Jim Alateras"));
    }
    
    /**
     * Test that default vcalues are assigned for lookups
     */
    public void testDefaultValuesForLookups()
    throws Exception {
        Hashtable params = getTestData().getGlobalParams();
        String assertionFile = (String)params.get("assertionFile");
        String dir = (String)params.get("dir");
        String extension = (String)params.get("extension");
        
        ArchetypeService service = new ArchetypeService(dir, 
                new String[]{extension}, assertionFile);

        Address address = (Address)service.createDefaultObject("address.location");
        assertTrue(address.getDetails().getAttribute("country") != null);
        assertTrue(address.getDetails().getAttribute("country").equals("Australia"));
        assertTrue(address.getDetails().getAttribute("state") != null);
        assertTrue(address.getDetails().getAttribute("state").equals("Victoria"));
        assertTrue(StringUtils.isEmpty((String)address.getDetails().getAttribute("suburb")));
        assertTrue(StringUtils.isEmpty((String)address.getDetails().getAttribute("postCode")));
        
        Person person = (Person)service.createDefaultObject("person.person");
        assertTrue(person != null);
        assertTrue(person.getTitle().equals("Mr"));
    }
}
