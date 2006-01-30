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


//openvpms-framework
import java.util.Date;

import org.openvpms.component.business.service.ServiceBaseTestCase;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Animal;
import org.openvpms.component.business.domain.im.party.Person;
import org.openvpms.component.business.service.archetype.ArchetypeService;
import org.openvpms.component.business.service.archetype.ValidationError;
import org.openvpms.component.business.service.archetype.ValidationException;

/**
 * Test the entity service
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class EntityRelationshipTestCase extends
    ServiceBaseTestCase {

    /**
     * Holds a reference to the archetype service
     */
    private ArchetypeService service;
    

    public static void main(String[] args) {
        junit.textui.TestRunner.run(EntityRelationshipTestCase.class);
    }

    /**
     * Default constructor
     */
    public EntityRelationshipTestCase() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    @Override
    protected String[] getConfigLocations() {
        return new String[] { "org/openvpms/component/business/service/entity/entity-service-appcontext.xml" };
    }


    /**
     * Test the creation of a simple entity relationship between a person 
     * and a animal
     */
    public void testSimpleEntityRelationship() 
    throws Exception {
        try {
            Person person = createPerson("Mr", "Jim", "Alateras");
            Animal pet = createAnimal("buddy");
            service.save(person);
            service.save(pet);
            
            // we can ony create entity relationship with persistent objects
            EntityRelationship rel = createEntityRelationship(person, pet);
            service.validateObject(rel);
            person.addEntityRelationship(rel);
            pet.addEntityRelationship((EntityRelationship)rel.clone());
            service.save(person);
            service.save(pet);
            
            // now retrieve them and ensure that the correct entity relationship
            // exists
            person = (Person)service.getById(person.getArchetypeId(), person.getUid());
            assertTrue(person.getEntityRelationships().size() == 1);
            pet = (Animal)service.getById(pet.getArchetypeId(), pet.getUid());
            assertTrue(pet.getEntityRelationships().size() == 1);
            service.validateObject(person.getEntityRelationships().iterator().next());
        } catch (ValidationException exception) {
            for (ValidationError error : exception.getErrors()) {
                logger.error("Node:" + error.getNodeName() + " Error:" + error.getErrorMessage());
            }
        }
    }
    
    /**
     * Test the we can create an entity relationship and then deleting it
     */
    public void testEntityRelationshipDeletion()
    throws Exception {
        Person person = createPerson("Mr", "Jim", "Alateras");
        Animal pet = createAnimal("buddy");
        service.save(person);
        service.save(pet);
        
        // we can ony create entity relationship with persistent objects
        EntityRelationship rel = createEntityRelationship(person, pet);
        person.addEntityRelationship(rel);
        pet.addEntityRelationship((EntityRelationship)rel.clone());
        service.save(person);
        service.save(pet);
        
        // now retrieve them and ensure that the correct entity relationship
        // exists
        person = (Person)service.getById(person.getArchetypeId(), person.getUid());
        assertTrue(person.getEntityRelationships().size() == 1);
        pet = (Animal)service.getById(pet.getArchetypeId(), pet.getUid());
        assertTrue(pet.getEntityRelationships().size() == 1);
        
        // retrieve the entity relationship
        rel = person.getEntityRelationships().iterator().next();
        person.removeEntityRelationship(rel);
        service.save(person);
        
        // now retrieve both the animal and the person and check the entity
        // relatioships
        person = (Person)service.getById(person.getArchetypeId(), person.getUid());
        assertTrue(person.getEntityRelationships().size() == 0);
        pet = (Animal)service.getById(pet.getArchetypeId(), pet.getUid());
        assertTrue(pet.getEntityRelationships().size() == 1);
    }
    
    /**
     * Test the we can clone and entity relationship object
     */
    public void testEntityRelationshipClone()
    throws Exception {
        Person person = createPerson("Mr", "Jim", "Alateras");
        Animal pet = createAnimal("buddy");
        EntityRelationship rel = createEntityRelationship(person, pet);
        EntityRelationship copy = (EntityRelationship)rel.clone();
        copy.setReason("none at all");
        assertTrue(rel.getReason() == null || rel.getReason().length() == 0);
        assertTrue(copy.getReason() != null && copy.getReason().length() > 0);
    }
    
    /**
     * Create a person
     * 
     * @param title
     *            the person's title
     * @param firstName
     *            the person's first name
     * @param lastName
     *            the person's last name
     * @return Person
     */
    private Person createPerson(String title, String firstName, String lastName) {
        Entity entity = (Entity)service.create("person.person");
        assertTrue(entity instanceof Person);

        Person person = (Person) entity;
        person.setTitle(title);
        person.setFirstName(firstName);
        person.setLastName(lastName);

        return person;
    }
    
    /**
     * Create an anima entity
     * 
     * @param name
     *            the name of the pet
     * @return Animal
     */
    private Animal createAnimal(String name) {
        Animal animal = (Animal)service.create("animal.pet");
        assertTrue(animal != null);

        animal.setSpecies("dog");
        animal.setBreed("collie");
        animal.setColour("brown");
        animal.setName(name);
        animal.setSex("male");
        animal.setDateOfBirth(new Date());

        return animal;
    }

    /**
     * Create an {@link EntityRelationship} between the source and target
     * entities
     * 
     * @param source
     *            the souce entity
     * @param target
     *            the target entity
     * @return EntityRelationship                        
     */
    private EntityRelationship createEntityRelationship(Entity source, Entity target) {
        EntityRelationship rel = (EntityRelationship)service
            .create("entityRelationship.animalCarer");
        
        rel.setActiveStartTime(new Date());
        rel.setSequence(1);
        rel.setSource(new IMObjectReference(source));
        rel.setTarget(new IMObjectReference(target));

        return rel;
        
    }
    
    /**
     * Create an entity identity with the specified identity
     * 
     * @param identity
     *            the identity to assign
     * @return EntityIdentity
     */
    @SuppressWarnings("unused")
    private EntityIdentity createEntityIdentity(String identity) {
        EntityIdentity eidentity = (EntityIdentity) service
                .create("entityIdentity.personAlias");
        assertTrue(eidentity != null);

        eidentity.setIdentity(identity);
        return eidentity;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        this.service = (ArchetypeService) applicationContext
                .getBean("archetypeService");
        assertTrue(service != null);
    }
}
