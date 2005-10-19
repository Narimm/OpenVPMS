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
import org.openvpms.component.business.domain.im.party.Animal;
import org.openvpms.component.business.domain.im.party.Person;
import org.openvpms.component.business.service.archetype.ArchetypeService;

/**
 * Test the entity service
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class EntityRelationshipTestCase extends
    ServiceBaseTestCase {

    /**
     * Holds a reference to the entity service
     */
    private EntityService entityService;

    /**
     * Holds a reference to the archetype service
     */
    private ArchetypeService archetypeService;
    

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
        Person person = createPerson("Mr", "Jim", "Alateras");
        Animal pet = createAnimal("buddy");
        entityService.save(person);
        entityService.save(pet);
        
        // we can ony create entity relationship with persistent objects
        EntityRelationship rel = createEntityRelationship(person, pet);
        person.addSourceEntityRelationship(rel);
        pet.addTargetEntityRelationship(rel);
        entityService.save(person);
        entityService.save(pet);
        
        // now retrieve them and ensure that the correct entity relationship
        // exists
        person = (Person)getObjectById("entity.getEntityById", person.getUid());
        assertTrue(person.getSourceEntityRelationships().size() == 1);
        pet = (Animal)getObjectById("entity.getEntityById", pet.getUid());
        assertTrue(pet.getTargetEntityRelationships().size() == 1);
        assertTrue(person.getSourceEntityRelationships().iterator().next()
                .equals(pet.getTargetEntityRelationships().iterator().next()));
    }
    
    /**
     * Test the we can create an entity relationship and then deleting it
     */
    public void testEntityRelationshipDeletion()
    throws Exception {
        Person person = createPerson("Mr", "Jim", "Alateras");
        Animal pet = createAnimal("buddy");
        entityService.save(person);
        entityService.save(pet);
        
        // we can ony create entity relationship with persistent objects
        EntityRelationship rel = createEntityRelationship(person, pet);
        person.addSourceEntityRelationship(rel);
        pet.addTargetEntityRelationship(rel);
        entityService.save(person);
        entityService.save(pet);
        
        // now retrieve them and ensure that the correct entity relationship
        // exists
        person = (Person)getObjectById("entity.getEntityById", person.getUid());
        assertTrue(person.getSourceEntityRelationships().size() == 1);
        pet = (Animal)getObjectById("entity.getEntityById", pet.getUid());
        assertTrue(pet.getTargetEntityRelationships().size() == 1);
        assertTrue(person.getSourceEntityRelationships().iterator().next()
                .equals(pet.getTargetEntityRelationships().iterator().next()));
        
        // retrieve the entity relationship
        rel = person.getSourceEntityRelationships().iterator().next();
        person.removeSourceEntityRelationship(rel);
        entityService.save(person);
        
        // now retrieve both the animal and the person and check the entity
        // relatioships
        person = (Person)getObjectById("entity.getEntityById", person.getUid());
        assertTrue(person.getSourceEntityRelationships().size() == 0);
        pet = (Animal)getObjectById("entity.getEntityById", pet.getUid());
        assertTrue(pet.getTargetEntityRelationships().size() == 0);
        
        // check that the entity relationship no longer exists
        assertTrue(getObjectById("entityRelationship.getById", rel.getUid()) == null);
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
        Entity entity = entityService.create("person.person");
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
        Animal animal = (Animal)entityService.create("animal.pet");
        assertTrue(animal != null);

        animal.setSpecies("dog");
        animal.setBreed("collie");
        animal.setColour("brown");
        animal.setName(name);
        animal.setSex("male");

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
        EntityRelationship rel = (EntityRelationship)archetypeService
            .createDefaultObject("entityRelationship.animalCarer");
        
        rel.setActiveStartTime(new Date());
        rel.setSequence(1);
        rel.setSource(source);
        rel.setTarget(target);

        return rel;
        
    }
    
    /**
     * Create an entity identity with the specified identity
     * 
     * @param identity
     *            the identity to assign
     * @return EntityIdentity
     */
    private EntityIdentity createEntityIdentity(String identity) {
        EntityIdentity eidentity = (EntityIdentity) archetypeService
                .createDefaultObject("entityIdentity.personAlias");
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

        this.entityService = (EntityService) applicationContext
                .getBean("entityService");
        assertTrue(entityService != null);
        this.archetypeService = (ArchetypeService) applicationContext
                .getBean("archetypeService");
        assertTrue(archetypeService != null);
    }
}
