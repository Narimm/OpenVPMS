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


import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeService;
import org.openvpms.component.business.service.archetype.ValidationError;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.Date;


/**
 * Test the entity service.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
@SuppressWarnings("HardCodedStringLiteral")
public class EntityRelationshipTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * Holds a reference to the archetype service.
     */
    private ArchetypeService service;


    /**
     * Test the creation of a simple entity relationship between a person
     * and a animal
     */
    public void testSimpleEntityRelationship()
            throws Exception {
        try {
            Party person = createPerson("MR", "Jim", "Alateras");
            Party pet = createAnimal("buddy");
            service.save(person);
            service.save(pet);

            // we can ony create entity relationship with persistent objects
            EntityRelationship rel = createEntityRelationship(person, pet);
            service.validateObject(rel);
            person.addEntityRelationship(rel);
            service.save(person);

            // now retrieve them and ensure that the correct entity relationship
            // exists
            pet = (Party) service.get(pet.getObjectReference());
            assertTrue(pet.getEntityRelationships().size() == 1);
            service.validateObject(
                    person.getEntityRelationships().iterator().next());
        } catch (ValidationException exception) {
            for (ValidationError error : exception.getErrors()) {
                logger.error(
                        "Node:" + error.getNode() + " Error:" + error.getMessage());
            }
        }
    }

    /**
     * Test the creation of entities and entity relationships
     */
    public void testEntityAndEntityRelationship()
            throws Exception {
        try {
            Party person = createPerson("MR", "Jim", "Alateras");
            service.save(person);
            Party pet = createAnimal("buddy");
            service.save(pet);
            EntityRelationship rel = createEntityRelationship(person, pet);
            person.addEntityRelationship(rel);
            service.save(person);

            // retrieve the person again
            person = (Party) service.get(person.getObjectReference());
            assertTrue(person != null);
            assertTrue(person.getEntityRelationships().size() == 1);

            // retrieve the pet again
            pet = (Party) service.get(pet.getObjectReference());
            assertTrue(pet != null);
            assertTrue(pet.getEntityRelationships().size() == 1);

        } catch (ValidationException exception) {
            for (ValidationError error : exception.getErrors()) {
                logger.error(
                        "Node:" + error.getNode() + " Error:" + error.getMessage());
            }
        }
    }

    /**
     * Test the we can create an entity relationship and then deleting it
     */
    public void testEntityRelationshipDeletion()
            throws Exception {
        Party person = createPerson("MR", "Jim", "Alateras");
        Party pet = createAnimal("buddy");
        service.save(person);
        service.save(pet);

        // we can ony create entity relationship with persistent objects
        EntityRelationship rel = createEntityRelationship(person, pet);
        person.addEntityRelationship(rel);
        service.save(person);

        // now retrieve them and ensure that the correct entity relationship
        // exists
        person = (Party) service.get(person.getObjectReference());
        assertTrue(person.getEntityRelationships().size() == 1);
        pet = (Party) service.get(pet.getObjectReference());
        assertTrue(pet.getEntityRelationships().size() == 1);

        // retrieve the entity relationship
        rel = person.getEntityRelationships().iterator().next();
        person.removeEntityRelationship(rel);
        service.save(person);

        // now retrieve both the animal and the person and check the entity
        // relatioships
        person = (Party) service.get(person.getObjectReference());
        assertTrue(person.getEntityRelationships().size() == 0);
        pet = (Party) service.get(pet.getObjectReference());
        assertTrue(pet.getEntityRelationships().size() == 0);
    }

    /**
     * Test that we can add relationships to the target entity and that it
     * will appear on the source entity.
     */
    public void testAddAndRemoveEntityRelationshipToTargetEntity()
            throws Exception {
        Party person = createPerson("MR", "Jim", "Alateras");
        Party pet = createAnimal("buddy");
        service.save(person);
        service.save(pet);

        // we can ony create entity relationship with persistent objects
        EntityRelationship rel = createEntityRelationship(person, pet);
        pet.addEntityRelationship(rel);
        service.save(pet);

        // now retrieve them and ensure that the correct entity relationship
        // exists
        person = (Party) service.get(person.getObjectReference());
        assertTrue(person.getEntityRelationships().size() == 1);
        pet = (Party) service.get(pet.getObjectReference());
        assertTrue(pet.getEntityRelationships().size() == 1);

        // now remove the relationship and make sure it all works
        // correctly
        pet.removeEntityRelationship(
                pet.getEntityRelationships().iterator().next());
        service.save(pet);

        // check the relationsahips again
        person = (Party) service.get(person.getObjectReference());
        assertTrue(person.getEntityRelationships().size() == 0);
        pet = (Party) service.get(pet.getObjectReference());
        assertTrue(pet.getEntityRelationships().size() == 0);

    }

    /**
     * Test the manipulation of multiple entity relationships
     */
    public void testManipulationMultipleEntityRelationships()
            throws Exception {
        Party person = createPerson("MR", "Jim", "Alateras");
        Party pet1 = createAnimal("buddy");
        Party pet2 = createAnimal("boxer");
        Party pet3 = createAnimal("dude");
        service.save(person);
        service.save(pet1);
        service.save(pet2);
        service.save(pet3);

        // create a number of entity relationships
        person.addEntityRelationship(createEntityRelationship(person, pet1));
        person.addEntityRelationship(createEntityRelationship(person, pet2));
        person.addEntityRelationship(createEntityRelationship(person, pet3));
        service.save(person);

        // now retrieve them and ensure that the correct entity relationship
        // exists
        person = (Party) service.get(person.getObjectReference());
        assertTrue(person.getEntityRelationships().size() == 3);
        pet1 = (Party) service.get(pet1.getObjectReference());
        assertTrue(pet1.getEntityRelationships().size() == 1);

        // remove one of the relationships from the target side and check
        // them again
        pet1.removeEntityRelationship(
                pet1.getEntityRelationships().iterator().next());
        service.save(pet1);
        person = (Party) service.get(person.getObjectReference());
        assertTrue(person.getEntityRelationships().size() == 2);
        pet1 = (Party) service.get(pet1.getObjectReference());
        assertTrue(pet1.getEntityRelationships().size() == 0);
        pet2 = (Party) service.get(pet2.getObjectReference());
        assertTrue(pet2.getEntityRelationships().size() == 1);
        pet3 = (Party) service.get(pet3.getObjectReference());
        assertTrue(pet3.getEntityRelationships().size() == 1);
    }

    /**
     * Test the we can clone and entity relationship object
     */
    public void testEntityRelationshipClone()
            throws Exception {
        Party person = createPerson("MR", "Jim", "Alateras");
        Party pet = createAnimal("buddy");
        EntityRelationship rel = createEntityRelationship(person, pet);
        EntityRelationship copy = (EntityRelationship) rel.clone();
        assertEquals(person.getObjectReference(), copy.getSource());
        assertEquals(pet.getObjectReference(), copy.getTarget());
    }

    /**
     * Test bug 133
     */
    public void testOVPMS133()
            throws Exception {
        Party person = createPerson("MR", "Jim", "Alateras");
        Party pet = createAnimal("buddy");
        service.save(person);
        service.save(pet);

        // we can ony create entity relationship with persistent objects
        EntityRelationship rel = createEntityRelationship(person, pet);
        person.addEntityRelationship(rel);
        service.save(person);

        // now retrieve them and ensure that the correct entity relationship
        // exists
        person = (Party) service.get(person.getObjectReference());
        assertTrue(person.getEntityRelationships().size() == 1);
        rel = person.getEntityRelationships().iterator().next();
        assertTrue(rel != null);
        Party samePerson = (Party) service.get(rel.getSource());
        assertTrue(person.getId() == samePerson.getId());
        assertTrue(person.getVersion() == samePerson.getVersion());
    }

    /**
     * Test bug 176
     */
    public void testOVPMS176()
            throws Exception {
        String[] shortNames = {"entityRelationship.animal*"};
        int aCount = ArchetypeQueryHelper.get(service, shortNames, false, 0,
                                              ArchetypeQuery.ALL_RESULTS).getResults().size();

        // create a new entity relationsip and save it
        Party person = createPerson("MR", "Jim", "Alateras");
        Party pet = createAnimal("buddy");
        service.save(person);
        service.save(pet);
        EntityRelationship rel = createEntityRelationship(person, pet,
                                                          "entityRelationship.animalOwner");
        person.addEntityRelationship(rel);
        rel = createEntityRelationship(person, pet,
                                       "entityRelationship.animalCarer");
        person.addEntityRelationship(rel);

        try {
            service.save(person);
        } catch (ValidationException exception) {
            for (ValidationError error : exception.getErrors()) {
                logger.error("[Validation Error] Node:"
                        + error.getNode() + " Message:" + error.getMessage());
            }

            throw exception;
        }

        // now check that the get actually works
        int aCount1 = ArchetypeQueryHelper.get(
                service, shortNames, false, 0,
                ArchetypeQuery.ALL_RESULTS).getResults().size();
        assertEquals(aCount + 2, aCount1);
    }

    /**
     * /*
     * (non-Javadoc)
     *
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    @Override
    protected String[] getConfigLocations() {
        return new String[]{"org/openvpms/component/business/service/entity/entity-service-appcontext.xml"};
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
        assertNotNull(service);
    }

    /**
     * Creates a person.
     *
     * @param title     the person's title
     * @param firstName the person's first name
     * @param lastName  the person's last name
     * @return Person
     */
    private Party createPerson(String title, String firstName,
                               String lastName) {
        Party person = (Party) service.create("party.person");
        person.getDetails().put("lastName", lastName);
        person.getDetails().put("firstName", firstName);
        person.getDetails().put("title", title);

        return person;
    }

    /*
    * Create an animal entity.
    *
    * @param name
    *            the name of the pet
    * @return Animal
    */
    private Party createAnimal(String name) {
        Party pet = (Party) service.create("party.animalpet");
        pet.setName(name);
        pet.getDetails().put("breed", "dog");
        pet.getDetails().put("colour", "brown");
        pet.getDetails().put("sex", "UNSPECIFIED");
        pet.getDetails().put("species", "k9");
        pet.setDescription("A dog");
        pet.getDetails().put("dateOfBirth", new Date());

        return pet;
    }

    /**
     * Create an entity relationship of the specified type between the
     * source and target entities
     *
     * @param source    the source entity
     * @param target    the target entity
     * @param shortName the short name of the relationship to create
     * @return EntityRelationship
     */
    private EntityRelationship createEntityRelationship(Entity source,
                                                        Entity target,
                                                        String shortName) {
        EntityRelationship rel = (EntityRelationship) service.create(shortName);

        rel.setActiveStartTime(new Date());
        rel.setSource(source.getObjectReference());
        rel.setTarget(target.getObjectReference());

        return rel;
    }

    /**
     * Create an {@link EntityRelationship} between the source and target
     * entities of type entityRelationship.animalCarer.
     *
     * @param source the souce entity
     * @param target the target entity
     * @return EntityRelationship
     */
    private EntityRelationship createEntityRelationship(Entity source,
                                                        Entity target) {
        return createEntityRelationship(source, target,
                                        "entityRelationship.animalCarer");
    }

}
