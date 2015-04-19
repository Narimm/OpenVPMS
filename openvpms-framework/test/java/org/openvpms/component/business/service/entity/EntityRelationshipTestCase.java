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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.AbstractArchetypeServiceTest;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;


/**
 * Test the entity service.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
@ContextConfiguration("entity-service-appcontext.xml")
public class EntityRelationshipTestCase extends AbstractArchetypeServiceTest {

    /**
     * Test the creation of a simple entity relationship between a person and an animal.
     */
    @Test
    public void testSimpleEntityRelationship() {
        Party person = createPerson("MR", "Jim", "Alateras");
        Party pet = createAnimal("buddy");
        save(person);
        save(pet);

        // we can ony create entity relationship with persistent objects
        EntityRelationship rel = createEntityRelationship(person, pet);
        validateObject(rel);
        person.addEntityRelationship(rel);
        save(person);

        // now retrieve them and ensure that the correct entity relationship
        // exists
        pet = (Party) get(pet.getObjectReference());
        assertEquals(1, pet.getEntityRelationships().size());
        validateObject(person.getEntityRelationships().iterator().next());
    }

    /**
     * Test the creation of entities and entity relationships.
     */
    @Test
    public void testEntityAndEntityRelationship() {
        Party person = createPerson("MR", "Jim", "Alateras");
        save(person);
        Party pet = createAnimal("buddy");
        save(pet);
        EntityRelationship rel = createEntityRelationship(person, pet);
        person.addEntityRelationship(rel);
        save(person);

        // retrieve the person again
        person = (Party) get(person.getObjectReference());
        assertNotNull(person);
        assertEquals(1, person.getEntityRelationships().size());

        // retrieve the pet again
        pet = (Party) get(pet.getObjectReference());
        assertNotNull(pet);
        assertEquals(1, pet.getEntityRelationships().size());
    }

    /**
     * Test the we can create an entity relationship and then deleting it.
     */
    @Test
    public void testEntityRelationshipDeletion() {
        Party person = createPerson("MR", "Jim", "Alateras");
        Party pet = createAnimal("buddy");
        save(person);
        save(pet);

        // we can ony create entity relationship with persistent objects
        EntityRelationship rel = createEntityRelationship(person, pet);
        person.addEntityRelationship(rel);
        save(person);

        // now retrieve them and ensure that the correct entity relationship
        // exists
        person = (Party) get(person.getObjectReference());
        assertEquals(1, person.getEntityRelationships().size());
        pet = (Party) get(pet.getObjectReference());
        assertEquals(1, pet.getEntityRelationships().size());

        // retrieve the entity relationship
        rel = person.getEntityRelationships().iterator().next();
        person.removeEntityRelationship(rel);
        save(person);

        // now retrieve both the animal and the person and check the entity
        // relatioships
        person = (Party) get(person.getObjectReference());
        assertEquals(0, person.getEntityRelationships().size());
        pet = (Party) get(pet.getObjectReference());
        assertEquals(0, pet.getEntityRelationships().size());
    }

    /**
     * Test that we can add relationships to the target entity and that it
     * will appear on the source entity.
     */
    @Test
    public void testAddAndRemoveEntityRelationshipToTargetEntity() {
        Party person = createPerson("MR", "Jim", "Alateras");
        Party pet = createAnimal("buddy");
        save(person);
        save(pet);

        // we can ony create entity relationship with persistent objects
        EntityRelationship rel = createEntityRelationship(person, pet);
        pet.addEntityRelationship(rel);
        save(pet);

        // now retrieve them and ensure that the correct entity relationship exists
        person = (Party) get(person.getObjectReference());
        assertEquals(1, person.getEntityRelationships().size());
        pet = (Party) get(pet.getObjectReference());
        assertEquals(1, pet.getEntityRelationships().size());

        // now remove the relationship and make sure it all works correctly
        pet.removeEntityRelationship(pet.getEntityRelationships().iterator().next());
        save(pet);

        // check the relationsahips again
        person = (Party) get(person.getObjectReference());
        assertEquals(0, person.getEntityRelationships().size());
        pet = (Party) get(pet.getObjectReference());
        assertEquals(0, pet.getEntityRelationships().size());

    }

    /**
     * Test the manipulation of multiple entity relationships.
     */
    @Test
    public void testManipulationMultipleEntityRelationships() {
        Party person = createPerson("MR", "Jim", "Alateras");
        Party pet1 = createAnimal("buddy");
        Party pet2 = createAnimal("boxer");
        Party pet3 = createAnimal("dude");
        save(person);
        save(pet1);
        save(pet2);
        save(pet3);

        // create a number of entity relationships
        person.addEntityRelationship(createEntityRelationship(person, pet1));
        person.addEntityRelationship(createEntityRelationship(person, pet2));
        person.addEntityRelationship(createEntityRelationship(person, pet3));
        save(person);

        // now retrieve them and ensure that the correct entity relationship exists
        person = (Party) get(person.getObjectReference());
        assertEquals(3, person.getEntityRelationships().size());
        pet1 = (Party) get(pet1.getObjectReference());
        assertEquals(1, pet1.getEntityRelationships().size());

        // remove one of the relationships from the target side and check them again
        pet1.removeEntityRelationship(pet1.getEntityRelationships().iterator().next());
        save(pet1);
        person = (Party) get(person.getObjectReference());
        assertEquals(2, person.getEntityRelationships().size());
        pet1 = (Party) get(pet1.getObjectReference());
        assertEquals(0, pet1.getEntityRelationships().size());
        pet2 = (Party) get(pet2.getObjectReference());
        assertEquals(1, pet2.getEntityRelationships().size());
        pet3 = (Party) get(pet3.getObjectReference());
        assertEquals(1, pet3.getEntityRelationships().size());
    }

    /**
     * Test the we can clone and entity relationship object.
     *
     * @throws Exception for any error
     */
    @Test
    public void testEntityRelationshipClone() throws Exception {
        Party person = createPerson("MR", "Jim", "Alateras");
        Party pet = createAnimal("buddy");
        EntityRelationship rel = createEntityRelationship(person, pet);
        EntityRelationship copy = (EntityRelationship) rel.clone();
        assertEquals(person.getObjectReference(), copy.getSource());
        assertEquals(pet.getObjectReference(), copy.getTarget());
    }

    /**
     * Test bug 133.
     */
    @Test
    public void testOVPMS133() {
        Party person = createPerson("MR", "Jim", "Alateras");
        Party pet = createAnimal("buddy");
        save(person);
        save(pet);

        // we can ony create entity relationship with persistent objects
        EntityRelationship rel = createEntityRelationship(person, pet);
        person.addEntityRelationship(rel);
        save(person);

        // now retrieve them and ensure that the correct entity relationship
        // exists
        person = (Party) get(person.getObjectReference());
        assertEquals(1, person.getEntityRelationships().size());
        rel = person.getEntityRelationships().iterator().next();
        assertNotNull(rel);
        Party samePerson = (Party) get(rel.getSource());
        assertEquals(person.getId(), samePerson.getId());
        assertEquals(person.getVersion(), samePerson.getVersion());
    }

    /**
     * Test bug 176.
     */
    @Test
    public void testOVPMS176() {
        String[] shortNames = {"entityRelationship.animal*"};
        int aCount = ArchetypeQueryHelper.get(getArchetypeService(), shortNames, false, 0,
                                              ArchetypeQuery.ALL_RESULTS).getResults().size();

        // create a new entity relationsip and save it
        Party person = createPerson("MR", "Jim", "Alateras");
        Party pet = createAnimal("buddy");
        save(person);
        save(pet);
        EntityRelationship rel = createEntityRelationship(person, pet, "entityRelationship.animalOwner");
        person.addEntityRelationship(rel);
        rel = createEntityRelationship(person, pet, "entityRelationship.animalCarer");
        person.addEntityRelationship(rel);

        save(person);
        // now check that the get actually works
        int aCount1 = ArchetypeQueryHelper.get(getArchetypeService(), shortNames, false, 0,
                                               ArchetypeQuery.ALL_RESULTS).getResults().size();
        assertEquals(aCount + 2, aCount1);
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
        Party person = (Party) create("party.person");
        person.getDetails().put("lastName", lastName);
        person.getDetails().put("firstName", firstName);
        person.getDetails().put("title", title);

        return person;
    }

    /*
    * Create an animal entity.
    *
    * @param name the name of the pet
    * @return a new entity
    */
    private Party createAnimal(String name) {
        Party pet = (Party) create("party.animalpet");
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
        EntityRelationship rel = (EntityRelationship) create(shortName);
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
        return createEntityRelationship(source, target, "entityRelationship.animalCarer");
    }

}
