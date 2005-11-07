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

// spring-context
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import org.apache.log4j.Logger;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Person;
import org.openvpms.component.business.domain.im.party.Animal;
import org.openvpms.component.business.service.archetype.descriptor.ArchetypeDescriptor;

/**
 * Test the persistence side of the archetype service
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeServicePersistenceTestCase extends
        AbstractDependencyInjectionSpringContextTests {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(ArchetypeServicePersistenceTestCase.class);
    
    /**
     * Holds a reference to the entity service
     */
    private ArchetypeService service;
    

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ArchetypeServicePersistenceTestCase.class);
    }

    /**
     * Default constructor
     */
    public ArchetypeServicePersistenceTestCase() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    @Override
    protected String[] getConfigLocations() {
        return new String[] { 
                "org/openvpms/component/business/service/archetype/archetype-service-appcontext.xml" 
                };
    }

    /**
     * Test that we can create a {@link Person} through this service
     */
    public void testCreatePerson()
    throws Exception {
        Entity entity = (Entity)service.create("person.person");
        assertTrue(entity instanceof Person);
        
        Person person = (Person)entity;
        person.setLastName("Alateras");
        person.setFirstName("Jim");
        person.setTitle("Mr");
        
        service.save(person);
    }

    /**
     * Test create, retrieve and update person
     */
    public void testPersonLifecycle()
    throws Exception {
        Entity entity = (Entity)service.create("person.person");
        assertTrue(entity instanceof Person);
        
        Person person = (Person)entity;
        person.setLastName("Alateras");
        person.setFirstName("Jim");
        person.setTitle("Mr");
        
        Contact contact = new Contact();
        contact.setArchetypeId(new ArchetypeId(
                "openvpms-contact-contact.draft.1.0"));
        
        Date start = new Date();
        Date end = new Date(start.getTime() + (7*24*60*60*1000));
        contact.setActiveStartTime(start);
        contact.setActiveEndTime(end);
        person.addContact(contact);
        assertTrue(person.getContacts().size() == 1);
        service.save(person);
        
        person = (Person)service.getById(person.getArchetypeId(), person.getUid());
        assertTrue(person.getContacts().size() == 1);
        person.setFirstName("Grace");
        service.save(person);
    }

    /**
     * Test that we can create a {@link Animal} through this service
     */
    public void testAnimalCreation()
    throws Exception {
        // create and insert a new pet
        service.save(createPet("brutus"));
    }

    /**
     * Test that we can create a {@link Lookup} through this service
     */
    public void testLookupCreation()
    throws Exception {
        // create and insert a new lookup
        Lookup lookup = createCountryLookup("South Africa");
        service.save(lookup);
        assertTrue(service.getById(lookup.getArchetypeId(), lookup.getUid()) != null);
    }

    /**
     * Test that we can locate entities by RmName only
     */
    public void testFindWithRmName()
    throws Exception {
        
        // get the initial count
        List before = service.get("party", null, null, null);
        
        // create and insert a new pet
        service.save(createPet("brutus"));
        
        // now get a new count
        List after = service.get("party", null, null, null);
        assertTrue(after.size() == before.size() + 1);
    }

    /**
     * Test that we can locate entities by partial RmName only
     */
    public void testFindWithPartialRmName()
    throws Exception {
        
        // get the initial count..this should not be allowed
        List before = service.get("par*", null, null, null);
        
        // create and insert a new pet
        service.save(createPet("testFindWithPartialRmName"));
        
        // now get a new count
        List after = service.get("par*", null, null, null);
        assertTrue(after.size() == before.size() + 1);
    }

    /**
     * Test that we can locate entities by RmName and EntityName only
     */
    public void testFindWithEntityName()
    throws Exception {
        
        // get the initial count
        List before = service.get("party", "animal", null, null);
        
        // create and insert a new pet
        service.save(createPet("simon"));
        
        // now get a new count
        List after = service.get("party", "animal", null, null);
        assertTrue(after.size() == before.size() + 1);
    }

    /**
     * Test that we can locate entities by RmName and partial EntityName 
     */
    public void testFindWithPartialEntityName()
    throws Exception {
        
        // get the initial count
        List before = service.get("party", "ani*", null, null);
        
        // create and insert a new pet
        service.save(createPet("testFindWithPartialEntityName"));
        
        // now get a new count
        List after = service.get("party", "ani*", null, null);
        assertTrue(after.size() == before.size() + 1);
        
        for (Object entity : after) {
            assertTrue(((Entity)entity).getArchetypeId().getEntityName().matches("ani.*"));
        }

        // now test with a starts with
        after = service.get("party", "*mal", null, null);
        for (Object entity : after) {
            assertTrue(((Entity)entity).getArchetypeId().getEntityName().matches(".*mal"));
        }

        // now test with a start and ends with
        after = service.get("party", "*nim*", null, null);
        for (Object entity : after) {
            assertTrue(((Entity)entity).getArchetypeId().getEntityName().matches(".*nim.*"));
        }
    }

    /**
     * Test that we can locate entities by RmName, EntityName and ConceptName
     */
    public void testFindWithConceptName()
    throws Exception {
        
        // get the initial count
        List before = service.get("party", "animal", "pet", null);
        
        // create and insert a new pet
        service.save(createPet("jack"));
        
        // now get a new count
        List after = service.get("party", "animal", "pet", null);
        assertTrue(after.size() == before.size() + 1);
    }
    
    /**
     * Test that we can locate entities by RmName, EntityName and partial 
     * conceptName
     */
    public void testFindWithPartialConceptName()
    throws Exception {
        
        // get the initial count
        List before = service.get("party", "animal", "p*", null);
        
        // create and insert a new pet
        service.save(createPet("testFindWithPartialConceptName"));
        
        // now get a new count
        List after = service.get("party", "animal", "p*", null);
        assertTrue(after.size() == before.size() + 1);
        for (Object entity : after) {
            assertTrue(((Entity)entity).getArchetypeId().getConcept().matches("p.*"));
        }

        // now test with a starts with
        after = service.get("party", "animal", "*et", null);
        for (Object entity : after) {
            assertTrue(((Entity)entity).getArchetypeId().getConcept().matches(".*et"));
        }

        // now test with a start and ends with
        after = service.get("party", "animal", "*e*", null);
        for (Object entity : after) {
            assertTrue(((Entity)entity).getArchetypeId().getConcept().matches(".*et.*"));
        }
    }

    /**
     * Test that we can locate entities by RmName, EntityName, ConceptName
     * and instance name
     */
    public void testFindWithInstanceName()
    throws Exception {
        
        // get the initial count
        List before = service.get("party", "animal", "pet", "brutus");
        
        // create and insert a new pet
        service.save(createPet("brutus"));
        
        // now get a new count
        List after = service.get("party", "animal", "pet", "brutus");
        assertTrue(after.size() == before.size() + 1);
    }


    /**
     * Test that we can locate entities by RmName, EntityName, ConceptName
     * and partial instance name
     */
    public void testFindWithPartialInstanceName()
    throws Exception {
        
        // get the initial count
        List before = service.get("party", "animal", "pet", "br*");
        
        // create and insert a new pet
        service.save(createPet("brutus"));
        
        // now get a new count
        List after = service.get("party", "animal", "pet", "br*");
        assertTrue(after.size() == before.size() + 1);
        for (Object entity : after) {
            assertTrue(((Entity)entity).getName().matches("br.*"));
        }

        // now test with a starts with
        after = service.get("party", "animal", "pet", "*tus");
        for (Object entity : after) {
            assertTrue(((Entity)entity).getName().matches(".*tus"));
        }

        // now test with a start and ends with
        after = service.get("party", "animal", "pet", "*utu*");
        for (Object entity : after) {
            assertTrue(((Entity)entity).getName().matches(".*utu.*"));
        }
    }
    
    /**
     * Test that that we can retrieve short names using a combination
     * of rmName, entityName and conceptName
     */
    public void testGetArchetypeShortNames()
    throws Exception {
        List<String> shortNames = service.getArchetypeShortNames("jimaparty", null, 
                null, true);
        assertTrue(shortNames.size() == 0);
        
        shortNames = service.getArchetypeShortNames("party", null, 
                null, true);
        assertTrue(shortNames.size() > 0);
        for (String shortName : shortNames) {
            ArchetypeDescriptor desc = service.getArchetypeDescriptor(shortName);
            ArchetypeId archId = desc.getArchetypeId();
            if ((archId.getRmName().matches("party")) &&
                (desc.isPrimary())) {
                continue;
            }
            fail("shortName: " + shortName + " in invalid for rmName=party");
        }

        // check when primaryOnly is set to false
        int count = shortNames.size();
        shortNames = service.getArchetypeShortNames("party", null, 
                null, false);
        assertTrue(shortNames.size() > count);
        
        
        shortNames = service.getArchetypeShortNames("party", "person", 
                null, true);
        assertTrue(shortNames.size() > 0);
        for (String shortName : shortNames) {
            ArchetypeDescriptor desc = service.getArchetypeDescriptor(shortName);
            ArchetypeId archId = desc.getArchetypeId();
            if ((archId.getRmName().matches("party")) &&
                    (archId.getEntityName().matches("person")) &&
                (desc.isPrimary())) {
                continue;
            }
            fail("shortName: " + shortName + " in invalid for rmName=party,entityName=person");
        }
        
        // test with a partial entity name
        shortNames = service.getArchetypeShortNames("party", "per*", 
                null, true);
        assertTrue(shortNames.size() > 0);
        for (String shortName : shortNames) {
            ArchetypeDescriptor desc = service.getArchetypeDescriptor(shortName);
            ArchetypeId archId = desc.getArchetypeId();
            if ((archId.getRmName().matches("party")) &&
                (archId.getEntityName().matches("per.*")) &&
                (desc.isPrimary())) {
                continue;
            }
            fail("shortName: " + shortName + " in invalid for rmName=party,entityName=per*");
        }
        
        // test with a partial entity name
        shortNames = service.getArchetypeShortNames("party", "*dress", 
                null, false);
        assertTrue(shortNames.size() > 0);
        for (String shortName : shortNames) {
            ArchetypeDescriptor desc = service.getArchetypeDescriptor(shortName);
            ArchetypeId archId = desc.getArchetypeId();
            if ((archId.getRmName().matches("party")) &&
                (archId.getEntityName().matches(".*dress"))) {
                continue;
            }
            fail("shortName: " + shortName + " in invalid for rmName=party,entityName=*dress");
        }
    }
    
    
    /**
     * Create a pet with the specified name
     * 
     * @param name
     *            the pet's name
     * @return Animal                  
     */
    private Animal createPet(String name) {
        Entity entity = (Entity)service.create("animal.pet");
        assertTrue(entity instanceof Animal);
        
        Animal pet = (Animal)entity;
        pet.setName("brutus");
        pet.setBreed("dog");
        pet.setColour("brown");
        pet.setSex("unspecified");
        pet.setSpecies("k9");
        pet.setDescription("A dog");
        
        Calendar date = Calendar.getInstance();
        date.set(1963, 12, 20);
        pet.setDateOfBirth(new Date());
        
        return pet;
    }
    
    
    /**
     * Create a country lookup
     * 
     * @param name
     *            the role name
     * @return Lookup                  
     */
    private Lookup createCountryLookup(String name) {
        Lookup lookup = (Lookup)service.create("lookup.country");
        assertTrue(lookup instanceof Lookup);
        
        lookup.setCode(name);
        lookup.setValue(name);
        return lookup;
    }
    
    /* (non-Javadoc)
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        
        this.service = (ArchetypeService)applicationContext.getBean(
                "archetypeService");
    }

}
