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

package org.openvpms.component.business.service.entity;

// spring-context
import java.util.Calendar;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.datatypes.quantity.datetime.DvDateTime;
import org.openvpms.component.business.domain.im.party.Person;
import org.openvpms.component.business.domain.im.party.Animal;

/**
 * Test the entity service
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class EntityServiceTestCase extends
        AbstractDependencyInjectionSpringContextTests {
    
    /**
     * Holds a reference to the entity service
     */
    private EntityService entityService;
    

    public static void main(String[] args) {
        junit.textui.TestRunner.run(EntityServiceTestCase.class);
    }

    /**
     * Default constructor
     */
    public EntityServiceTestCase() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    @Override
    protected String[] getConfigLocations() {
        return new String[] { 
                "org/openvpms/component/business/service/entity/entity-service-appcontext.xml" 
                };
    }

    /**
     * Test that we can create a {@link Person} through this service
     */
    public void testPersonCreation()
    throws Exception {
        Entity entity = entityService.create("person.person");
        assertTrue(entity instanceof Person);
        
        Person person = (Person)entity;
        person.setLastName("Alateras");
        person.setFirstName("Jim");
        person.setTitle("Mr");
        
        entityService.insert(person);
    }


    /**
     * Test that we can create a {@link Animal} through this service
     */
    public void testAnimalCreation()
    throws Exception {
        // create and insert a new pet
        entityService.insert(createPet("brutus"));
    }

    /**
     * Test that we can locate entities by RmName only
     */
    public void testFindWithRmName()
    throws Exception {
        
        // get the initial count
        Entity[] before = entityService.findEntities("party", null, null, null);
        
        // create and insert a new pet
        entityService.insert(createPet("brutus"));
        
        // now get a new count
        Entity[] after = entityService.findEntities("party", null, null, null);
        assertTrue(after.length == before.length + 1);
    }

    /**
     * Test that we can locate entities by RmName and EntityName only
     */
    public void testFindWithEntityName()
    throws Exception {
        
        // get the initial count
        Entity[] before = entityService.findEntities("party", "animal", null, null);
        
        // create and insert a new pet
        entityService.insert(createPet("simon"));
        
        // now get a new count
        Entity[] after = entityService.findEntities("party", "animal", null, null);
        assertTrue(after.length == before.length + 1);
    }

    /**
     * Test that we can locate entities by RmName, EntityName and ConceptName
     */
    public void testFindWithConceptName()
    throws Exception {
        
        // get the initial count
        Entity[] before = entityService.findEntities("party", "animal", "pet", null);
        
        // create and insert a new pet
        entityService.insert(createPet("jack"));
        
        // now get a new count
        Entity[] after = entityService.findEntities("party", "animal", "pet", null);
        assertTrue(after.length == before.length + 1);
    }


    /**
     * Test that we can locate entities by RmName, EntityName, ConceptName
     * and instance name
     */
    public void testFindWithInstanceName()
    throws Exception {
        
        // get the initial count
        Entity[] before = entityService.findEntities("party", "animal", "pet", "brutus");
        
        // create and insert a new pet
        entityService.insert(createPet("brutus"));
        
        // now get a new count
        Entity[] after = entityService.findEntities("party", "animal", "pet", "brutus");
        assertTrue(after.length == before.length + 1);
    }

    /**
     * Create a pet with the specified name
     * 
     * @param name
     *            the pet's name
     * @return Animal                  
     */
    private Animal createPet(String name) {
        Entity entity = entityService.create("animal.pet");
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
        pet.setDateOfBirth(new DvDateTime(date));
        
        return pet;
    }
    
    /* (non-Javadoc)
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        
        this.entityService = (EntityService)applicationContext.getBean(
                "entityService");
    }

}
