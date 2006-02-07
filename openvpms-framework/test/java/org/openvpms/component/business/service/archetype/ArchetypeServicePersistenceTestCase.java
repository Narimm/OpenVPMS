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
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Classification;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.party.Person;
import org.openvpms.component.business.domain.im.party.Animal;
import org.openvpms.component.business.domain.im.security.SecurityRole;

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
    public void  testCreatePerson()
    throws Exception {
        Entity entity = (Entity)service.create("person.person");
        assertTrue(entity instanceof Person);
        
        Person person = (Person)entity;
        person.setLastName("Alateras");
        person.setFirstName("Jim");
        person.setTitle("Mr");
        
        try {
            service.save(person);
        } catch (ValidationException exception) {
            dump(exception);
        }
    }

    /**
     * Test create, retrieve and update person
     */
    public void  testPersonLifecycle()
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
    public void  testAnimalCreation()
    throws Exception {
        // create and insert a new pet
        service.save(createPet("brutus"));
    }

    /**
     * Test that we can create a {@link Lookup} through this service
     */
    public void  testLookupCreation()
    throws Exception {
        // create and insert a new lookup
        Lookup lookup = createCountryLookup("South Africa");
        service.save(lookup);
        assertTrue(service.getById(lookup.getArchetypeId(), lookup.getUid()) != null);
    }

    /**
     * Test that we can locate entities by RmName only
     * @TODO This is not currently supported.
     */
    public void  xtestFindWithRmName()
    throws Exception {
        
        // get the initial count
        List before = service.get("party", null, null, null, false);
        
        // create and insert a new pet
        service.save(createPet("brutus"));
        
        // now get a new count
        List after = service.get("party", null, null, null, false);
        assertTrue(after.size() == before.size() + 1);
    }

    /**
     * Test that we can locate entities by partial RmName only
     * @TODO This is not currently supported.
     */
    public void  xtestFindWithPartialRmName()
    throws Exception {
        
        // get the initial count..this should not be allowed
        List before = service.get("par*", null, null, null, false);
        
        // create and insert a new pet
        service.save(createPet("testFindWithPartialRmName"));
        
        // now get a new count
        List after = service.get("par*", null, null, null, false);
        assertTrue(after.size() == before.size() + 1);
    }

    /**
     * Test that we can locate entities by RmName and EntityName only
     */
    public void  testFindWithEntityName()
    throws Exception {
        
        // get the initial count
        List before = service.get("party", "animal", null, null, false);
        
        // create and insert a new pet
        service.save(createPet("simon"));
        
        // now get a new count
        List after = service.get("party", "animal", null, null, false);
        assertTrue(after.size() == before.size() + 1);
    }

    /**
     * Test that we can locate entities by RmName and partial EntityName 
     */
    public void  testFindWithPartialEntityName()
    throws Exception {
        
        // get the initial count
        List before = service.get("party", "ani*", null, null, false);
        
        // create and insert a new pet
        service.save(createPet("testFindWithPartialEntityName"));
        
        // now get a new count
        List after = service.get("party", "ani*", null, null, false);
        assertTrue(after.size() == before.size() + 1);
        
        for (Object entity : after) {
            assertTrue(((Entity)entity).getArchetypeId().getEntityName().matches("ani.*"));
        }

        // now  test with a starts with
        after = service.get("party", "*mal", null, null, false);
        for (Object entity : after) {
            assertTrue(((Entity)entity).getArchetypeId().getEntityName().matches(".*mal"));
        }

        // now  test with a start and ends with
        after = service.get("party", "*nim*", null, null, false);
        for (Object entity : after) {
            assertTrue(((Entity)entity).getArchetypeId().getEntityName().matches(".*nim.*"));
        }
    }

    /**
     * Test that we can locate entities by RmName, EntityName and ConceptName
     */
    public void  testFindWithConceptName()
    throws Exception {
        
        // get the initial count
        List before = service.get("party", "animal", "pet", null, false);
        
        // create and insert a new pet
        service.save(createPet("jack"));
        
        // now get a new count
        List after = service.get("party", "animal", "pet", null, false);
        assertTrue(after.size() == before.size() + 1);
    }
    
    /**
     * Test that we can locate entities by RmName, EntityName and partial 
     * conceptName
     */
    public void  testFindWithPartialConceptName()
    throws Exception {
        
        // get the initial count
        List before = service.get("party", "animal", "p*", null, false);
        
        // create and insert a new pet
        service.save(createPet("testFindWithPartialConceptName"));
        
        // now get a new count
        List after = service.get("party", "animal", "p*", null, false);
        assertTrue(after.size() == before.size() + 1);
        for (Object entity : after) {
            assertTrue(((Entity)entity).getArchetypeId().getConcept().matches("p.*"));
        }

        // now test with a starts with
        after = service.get("party", "animal", "*et", null, false);
        for (Object entity : after) {
            assertTrue(((Entity)entity).getArchetypeId().getConcept().matches(".*et"));
        }

        // now test with a start and ends with
        after = service.get("party", "animal", "*e*", null, false);
        for (Object entity : after) {
            assertTrue(((Entity)entity).getArchetypeId().getConcept().matches(".*et.*"));
        }
    }

    /**
     * Test that we can locate entities by RmName, EntityName, ConceptName
     * and instance name
     */
    public void  testFindWithInstanceName()
    throws Exception {
        
        // get the initial count
        List before = service.get("party", "animal", "pet", "brutus", false);
        
        // create and insert a new pet
        service.save(createPet("brutus"));
        
        // now get a new count
        List after = service.get("party", "animal", "pet", "brutus", false);
        assertTrue(after.size() == before.size() + 1);
    }


    /**
     * Test that we can locate entities by RmName, EntityName, ConceptName
     * and partial instance name
     */
    public void  testFindWithPartialInstanceName()
    throws Exception {
        
        // get the initial count
        List before = service.get("party", "animal", "pet", "br*", false);
        
        // create and insert a new pet
        service.save(createPet("brutus"));
        
        // now get a new count
        List after = service.get("party", "animal", "pet", "br*", false, false);
        assertTrue(after.size() == before.size() + 1);
        for (Object entity : after) {
            assertTrue(((Entity)entity).getName().matches("br.*"));
        }

        // now test with a starts with
        after = service.get("party", "animal", "pet", "*tus", false);
        for (Object entity : after) {
            assertTrue(((Entity)entity).getName().matches(".*tus"));
        }

        // now test with a start and ends with
        after = service.get("party", "animal", "pet", "*utu*", false);
        for (Object entity : after) {
            assertTrue(((Entity)entity).getName().matches(".*utu.*"));
        }
    }
    
    /**
     * Test for OVPMS-147, where I pass in a set of archetypes short names and 
     * return a list of objects
     */
    public void testOVPMS147()
    throws Exception {
        // setup a list of archetype short names
        String[] shortNames = {"animal.p*", "person.person"};
        
        // get the initial count
        List<IMObject> before = service.get(shortNames, null, false, false);
        
        // create a new person and check the count 
        service.save(createPerson("Ms", "Bernadette", "Feeney"));
        service.save(createPerson("Ms", "Rose", "Feeney Alateras"));
        List<IMObject> after = service.get(shortNames, null, false, false);
        assertTrue(after.size() == before.size() + 2);
        
        // create a new lookup
        service.save(createCountryLookup("Algeria"));
        after = service.get(shortNames, null, false, false);
        assertTrue(after.size() == before.size() + 2);
    }
    
    /**
     * Test that that we can retrieve short names using a combination
     * of rmName, entityName and conceptName
     */
    public void  testGetArchetypeShortNames()
    throws Exception {
        List<String> shortNames = service.getArchetypeShortNames("jimaparty", null, 
                null, true);
        assertTrue(shortNames.size() == 0);
        
        shortNames = service.getArchetypeShortNames("party", null, 
                null, true);
        assertTrue(shortNames.size() > 0);
        for (String shortName : shortNames) {
            ArchetypeDescriptor desc = service.getArchetypeDescriptor(shortName);
            ArchetypeId archId = desc.getType();
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
            ArchetypeId archId = desc.getType();
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
            ArchetypeId archId = desc.getType();
            if ((archId.getRmName().matches("party")) &&
                (archId.getEntityName().matches("per.*")) &&
                (desc.isPrimary())) {
                continue;
            }
            fail("shortName: " + shortName + " in invalid for rmName=party,entityName=per*");
        }
        
        // test with a partial entity name
        shortNames = service.getArchetypeShortNames("party", "*tact", 
                null, false);
        assertTrue(shortNames.size() > 0);
        for (String shortName : shortNames) {
            ArchetypeDescriptor desc = service.getArchetypeDescriptor(shortName);
            ArchetypeId archId = desc.getType();
            if ((archId.getRmName().matches("party")) &&
                (archId.getEntityName().matches(".*tact"))) {
                continue;
            }
            fail("shortName: " + shortName + " in invalid for rmName=party,entityName=*dress");
        }
    }
    
    /**
     * Test that we can retrieve all the object for a particular 
     * classification.
     */
    public void testGetCandidateChildren()
    throws Exception {
        Classification cf = (Classification)service.create("classification.staff");
        cf.setName("vet");
        cf.setDescription("vet");
        service.save(cf);
        
        cf = (Classification)service.create("classification.staff");
        cf.setName("vet1");
        cf.setDescription("vet1");
        service.save(cf);
        
        cf = (Classification)service.create("classification.patient");
        cf.setName("premium");
        cf.setDescription("premium");
        service.save(cf);
        
        cf = (Classification)service.create("classification.patient");
        cf.setName("standard");
        cf.setDescription("standard");
        service.save(cf);
        
        Person person = (Person)service.create("person.person");
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor(
                person.getArchetypeId());
        NodeDescriptor ndesc = adesc.getNodeDescriptor("classifications");
        List<IMObject> children = 
            ArchetypeServiceHelper.getCandidateChildren(service, ndesc, person);
        assertTrue(children.size() > 0);
        for (IMObject child : children) {
            boolean matchFound = false;
            for (String shortName : ndesc.getArchetypeRange()) {
                if (child.getArchetypeId().getShortName().equals(shortName)) {
                    matchFound = true;
                    break;
                }
            }
            
            // if a match cannot be found then signal an error
            if (!matchFound) {
                fail(child.getArchetypeId() + " does not match " + 
                        ndesc.getArchetypeRange().toString());
                
            }
        }
    }
    
    /**
     * Test active flag on pet object
     */
    public void testActiveFlagOnAnimal()
    throws Exception {
        Animal pet = createPet("jimbo");
        service.save(pet);
        
        ArchetypeId aid = pet.getArchetypeId();
        Animal retrievePet = (Animal)service.get(aid.getRmName(),  
                aid.getEntityName(), aid.getConcept(), pet.getName(), true).get(0);
        
        assertTrue(retrievePet != null);
        retrievePet.setActive(false);
        service.save(retrievePet);
        
        retrievePet = (Animal)service.getById(retrievePet.getArchetypeId(), 
                retrievePet.getUid());
        assertTrue(retrievePet != null);
        assertTrue(retrievePet.isActive() == false);
    }
    
    /**
     * Test the we can retrieve values for each NodeDescriptor specified 
     * for the Archetype
     */
    public void testGetValueForEachNodeDescriptor() throws Exception {
        Party party = (Party)service.create("party.customerperson");
        assertTrue(party != null);
        assertTrue(party.getDetails() != null);
        
        party.getDetails().setAttribute("title", "Mr");
        party.getDetails().setAttribute("firstName", "Jim");
        party.getDetails().setAttribute("lastName", "Alateras");
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor(party.getArchetypeId());
        for (NodeDescriptor ndesc : adesc.getAllNodeDescriptors()) {
            //party.pathToObject(ndesc.getPath());
            //error("Value for " + ndesc.getName() + " is " + party.pathToObject(ndesc.getName()) == null ? null : party.pathToObject(ndesc.getName()).getValue());
            ndesc.getValue(party);
        }
        
        
        service.save(party);
        party = (Party)service.getById(party.getArchetypeId(), party.getUid());
        assertTrue(party != null);
        assertTrue(party.getDetails() != null);
        assertTrue(party != null);
        assertTrue(party.getDetails().getAttribute("title").equals("Mr"));
    }

    /**
     * Test the creation of a SecurityRole
     */
    public void testOVPMS115()
    throws Exception {
        
        // retrieve the initial count
        int count = service.get("system", "security", "role", null, true).size();
        SecurityRole role = (SecurityRole)service.create("security.role");
        role.setName("administrator");
        service.save(role);
        
        // retrieve by id
        role = (SecurityRole)service.getById(role.getArchetypeId(), role.getUid());
        assertTrue(role != null);
        assertTrue(role.getName().equals("administrator"));
        
        // retrieve the count after the add
        int count1 = service.get("system", "security", "role", null, true).size();
        assertTrue(count1 == count + 1);
    }
    
    /**
     * Test OVPMS-135 bug
     */
    public void testOVPMS135()
    throws Exception {
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor("classification.staff");
        assertTrue(adesc != null);
        NodeDescriptor ndesc = adesc.getNodeDescriptor("alias");
        assertTrue(ndesc != null);
        
        IMObject object = service.create("classification.staff");
        object.setName("jima");
        object.setDescription("head chef");
        assertTrue(object.pathToObject(ndesc.getPath()) != null);
        service.save(object);
        
        object = service.getById(object.getArchetypeId(), object.getUid());
        assertTrue(object != null);
        assertTrue(object.pathToObject(ndesc.getPath()) != null);
    }
    
    /**
     * Test that we can suppor OVPMS-146
     */
    public void testOVPMS146() 
    throws Exception {
        Entity entity = (Entity)service.create("person.person");
        assertTrue(entity instanceof Person);
        
        Person person = (Person)entity;
        person.setLastName("Alateras");
        person.setFirstName("Jim");
        person.setTitle("Mr");
        service.save(person);
        
        IMObjectReference ref = new IMObjectReference(person);
        person = (Person)service.get(ref);
        assertTrue(person.getUid() == ref.getUid());
        assertTrue(person.getLastName().equals("Alateras"));
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
     * Create a person with the specified title, firstName and LastName
     * 
     * @param title
     * @param firstName
     * @param lastName
     * 
     * @return Person
     */
    public Person createPerson(String title, String firstName, String lastName) {
        Person person = (Person)service.create("person.person");
        person.setLastName(lastName);
        person.setFirstName(firstName);
        person.setTitle(title);
        
        return person;
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

    /**
     * Display the validation errors
     * 
     * @param exception
     *            the validation exception 
     */
    private void dump(ValidationException exception) {
        for (ValidationError error : exception.getErrors()) {
            logger.error("node: " + error.getNodeName()
                    + " message: " + error.getErrorMessage());
        }
    }
}
