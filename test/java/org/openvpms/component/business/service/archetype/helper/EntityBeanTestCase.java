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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.service.archetype.helper;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.Date;
import java.util.List;


/**
 * Tests the {@link EntityBean} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class EntityBeanTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    private final String OWNER = "entityRelationship.animalOwner";


    /**
     * Tests the {@link EntityBean#addRelationship} and
     * {@link EntityBean#getRelationship)} methods.
     */
    public void testRelationships() {
        Party pet = (Party) create("party.animalpet");
        EntityBean bean = createBean("party.person");
        assertNull(bean.getRelationship(pet));

        EntityRelationship r = bean.addRelationship(OWNER, pet);
        checkRelationship(r, OWNER, bean.getEntity(), pet);
        r = bean.getRelationship(pet);
        checkRelationship(r, OWNER, bean.getEntity(), pet);

        bean.removeRelationship(r);
        assertNull(bean.getRelationship(pet));
    }

    /**
     * Tests the {@link EntityBean#getSourceEntity(String)}
     * and {@link EntityBean#getTargetEntity(String)} methods.
     */
    public void testGetEntity() {
        EntityBean pet = createPet();
        EntityBean person = createPerson();

        // add a relationship to the person and pet
        EntityRelationship r = person.addRelationship(OWNER, pet.getEntity());
        pet.getEntity().addEntityRelationship(r);

        // verify the person is the source, and pet the target
        assertEquals(person.getEntity(), person.getSourceEntity(OWNER));
        assertEquals(pet.getEntity(), person.getTargetEntity(OWNER));

        // verify the pet is the target, and the person the source
        assertEquals(pet.getEntity(), pet.getTargetEntity(OWNER));
        assertEquals(person.getEntity(), pet.getSourceEntity(OWNER));

        // mark the relationship inactive.
        r.setActive(false);
        assertNull(person.getSourceEntity(OWNER));
        assertNull(pet.getTargetEntity(OWNER));

        // mark the relationship active, and the patient inactive
        r.setActive(true);
        pet.getObject().setActive(false);
        pet.save();
        assertNull(person.getTargetEntity(OWNER));
        assertEquals(person.getEntity(), pet.getSourceEntity(OWNER));
    }

    /**
     * Tests the {@link EntityBean#getSourceEntity(String, Date)}
     * and {@link EntityBean#getTargetEntity(String, Date)} methods.
     */
    public void testGetEntityByTime() {
        EntityBean pet = createPet();
        EntityBean person = createPerson();

        // add a relationship to the person and pet
        EntityRelationship r = person.addRelationship(OWNER, pet.getEntity());
        pet.getEntity().addEntityRelationship(r);

        // verify the pet is returned for a time > the default start time
        Entity pet2 = person.getTargetEntity(OWNER, new Date());
        assertEquals(pet.getEntity(), pet2);

        // ... same for person
        Entity person2 = pet.getSourceEntity(OWNER, new Date());
        assertEquals(person.getEntity(), person2);

        // verify no entity returned if the relationship has no start time
        r.setActiveStartTime(null);
        assertNull(person.getTargetEntity(OWNER, new Date()));
        assertNull(pet.getSourceEntity(OWNER, new Date()));

        // now set the start and end time and verify that there is no entities
        // for a later time (use time addition due to system clock granularity)
        Date start = new Date();
        Date end = new Date(start.getTime() + 1);
        Date later = new Date(end.getTime() + 1);
        r.setActiveStartTime(start);
        r.setActiveEndTime(end);
        assertNull(person.getTargetEntity(OWNER, later));
        assertNull(pet.getSourceEntity(OWNER, later));
    }

    /**
     * Tests the {@link EntityBean#getSourceEntity(String)},
     * {@link EntityBean#getSourceEntity(String, boolean)},
     * {@link EntityBean#getSourceEntity(String, Date)},
     * {@link EntityBean#getSourceEntity(String, Date, boolean)},
     * {@link EntityBean#getTargetEntity(String)},
     * {@link EntityBean#getTargetEntity(String, boolean)},
     * {@link EntityBean#getTargetEntity(String, Date)}
     * and {@link EntityBean#getTargetEntity(String, Date, boolean)} methods
     * for active/inactive relationships and entities.
     */
    public void testGetEntityWithActive() {
        EntityBean petBean = createPet();
        Entity pet = petBean.getEntity();
        EntityBean personBean = createPerson();
        Entity person = personBean.getEntity();

        // add a relationship to the person and pet, and set inactive
        EntityRelationship r = personBean.addRelationship(OWNER, pet);
        pet.addEntityRelationship(r);
        r.setActive(false);

        assertNull(personBean.getTargetEntity(OWNER));
        assertNull(personBean.getTargetEntity(OWNER, new Date()));
        assertNull(petBean.getSourceEntity(OWNER));
        assertNull(petBean.getSourceEntity(OWNER, new Date()));
        assertEquals(pet, personBean.getTargetEntity(OWNER, false));
        assertEquals(pet, personBean.getTargetEntity(OWNER, new Date(), false));
        assertEquals(person, petBean.getSourceEntity(OWNER, false));
        assertEquals(person, petBean.getSourceEntity(OWNER, new Date(), false));

        // re-activate the relationship, but deactivate the entities
        r.setActive(true);
        pet.setActive(false);
        person.setActive(false);
        petBean.save();
        personBean.save();

        assertNull(personBean.getTargetEntity(OWNER));
        assertNull(personBean.getTargetEntity(OWNER, new Date()));
        assertNull(petBean.getSourceEntity(OWNER));
        assertNull(petBean.getSourceEntity(OWNER, new Date()));
        assertEquals(pet, personBean.getTargetEntity(OWNER, false));
        assertEquals(pet, personBean.getTargetEntity(OWNER, new Date(), false));
        assertEquals(person, petBean.getSourceEntity(OWNER, false));
        assertEquals(person, petBean.getSourceEntity(OWNER, new Date(), false));
    }

    /**
     * Tests the {@link EntityBean#getNodeSourceEntity(String)}
     * and {@link EntityBean#getNodeTargetEntity(String)} methods.
     */
    public void testGetNodeEntity() {
        EntityBean pet = createPet();
        EntityBean person = createPerson();

        // add a relationship to the person and pet
        EntityRelationship r = person.addRelationship(OWNER, pet.getEntity());
        pet.getEntity().addEntityRelationship(r);

        // verify the person is the source, and pet the target
        assertEquals(person.getEntity(),
                     person.getNodeSourceEntity("patients"));
        assertEquals(pet.getEntity(), person.getNodeTargetEntity("patients"));

        // verify the pet is the target, and the person the source
        assertEquals(pet.getEntity(), pet.getNodeTargetEntity("relationships"));
        assertEquals(person.getEntity(),
                     pet.getNodeSourceEntity("relationships"));

        // mark the relationship inactive.
        r.setActive(false);
        assertNull(person.getNodeSourceEntity("patients"));
        assertNull(pet.getNodeTargetEntity("relationships"));

        // mark the relationship active, and the pet inactive
        r.setActive(true);
        pet.getObject().setActive(false);
        pet.save();
        assertNull(person.getNodeTargetEntity("patients"));
        assertEquals(person.getEntity(),
                     person.getNodeSourceEntity("patients"));
    }

    /**
     * Tests the {@link EntityBean#getNodeSourceEntity(String, Date)}
     * and {@link EntityBean#getNodeTargetEntity(String, Date)} methods.
     */
    public void testGetNodeEntityByTime() {
        EntityBean pet = createPet();
        EntityBean person = createPerson();

        // add a relationship to the person and pet
        EntityRelationship r = person.addRelationship(OWNER, pet.getEntity());
        pet.getEntity().addEntityRelationship(r);

        // verify the pet is returned for a time > the default start time
        Entity pet2 = person.getNodeTargetEntity("patients", new Date());
        assertEquals(pet.getEntity(), pet2);

        // ... same for person
        Entity person2 = pet.getNodeSourceEntity("relationships", new Date());
        assertEquals(person.getEntity(), person2);

        // verify no entity returned if the relationship has no start time
        r.setActiveStartTime(null);
        assertNull(person.getNodeTargetEntity("patients", new Date()));
        assertNull(pet.getNodeSourceEntity("relationships", new Date()));

        // now set the start and end time and verify that there is no entities
        // for a later time (use time addition due to system clock granularity)
        Date start = new Date();
        Date end = new Date(start.getTime() + 1);
        Date later = new Date(end.getTime() + 1);
        r.setActiveStartTime(start);
        r.setActiveEndTime(end);
        assertNull(person.getNodeTargetEntity("patients", later));
        assertNull(pet.getNodeSourceEntity("relationships", later));
    }

    /**
     * Tests the {@link EntityBean#getNodeSourceEntity(String, Date)},
     * {@link EntityBean#getNodeSourceEntity(String, Date, boolean)},
     * {@link EntityBean#getNodeTargetEntity(String, Date)}
     * and {@link EntityBean#getNodeSourceEntity(String, Date, boolean)} methods
     * for active/inactive relationships and entities.
     */
    public void testGetNodeEntityByTimeAndActive() {
        EntityBean petBean = createPet();
        Entity pet = petBean.getEntity();
        EntityBean personBean = createPerson();
        Entity person = personBean.getEntity();

        // add a relationship to the person and pet, and set inactive
        EntityRelationship r = personBean.addRelationship(OWNER, pet);
        pet.addEntityRelationship(r);
        r.setActive(false);

        assertNull(personBean.getNodeTargetEntity("patients", new Date()));
        assertNull(petBean.getNodeSourceEntity("relationships", new Date()));
        assertEquals(pet,
                     personBean.getNodeTargetEntity("patients", new Date(),
                                                    false));
        assertEquals(person,
                     petBean.getNodeSourceEntity("relationships", new Date(),
                                                 false));

        // re-activate the relationship, but deactivate the entities
        r.setActive(true);
        pet.setActive(false);
        person.setActive(false);
        petBean.save();
        personBean.save();

        assertNull(personBean.getNodeTargetEntity("patients", new Date()));
        assertNull(petBean.getNodeSourceEntity("relationships", new Date()));
        assertEquals(pet,
                     personBean.getNodeTargetEntity("patients", new Date(),
                                                    false));
        assertEquals(person,
                     petBean.getNodeSourceEntity("relationships", new Date(),
                                                 false));
    }

    /**
     * Tests the {@link EntityBean#getNodeSourceEntities},
     * {@link EntityBean#getNodeSourceEntityRefs},
     * {@link EntityBean#getNodeTargetEntities} and
     * {@link EntityBean#getNodeTargetEntityRefs}  methods.
     */
    public void testGetNodeEntities() {
        EntityBean pet1 = createPet();
        EntityBean pet2 = createPet();
        EntityBean person = createPerson();

        // add a relationship to the person and pets
        EntityRelationship r1 = person.addRelationship(OWNER, pet1.getEntity());
        pet1.getEntity().addEntityRelationship(r1);
        EntityRelationship r2 = person.addRelationship(OWNER, pet2.getEntity());
        pet2.getEntity().addEntityRelationship(r2);

        List<Entity> pets = person.getNodeTargetEntities("patients");
        assertEquals(2, pets.size());
        assertTrue(pets.contains(pet1.getEntity()));
        assertTrue(pets.contains(pet2.getEntity()));

        List<IMObjectReference> petRefs
                = person.getNodeTargetEntityRefs("patients");
        assertEquals(2, petRefs.size());
        assertTrue(petRefs.contains(pet1.getReference()));
        assertTrue(petRefs.contains(pet2.getReference()));

        // now mark pet2 inactive. Will no longer be returned by
        // getNodeTargetEntities(). but will still be returned by
        // getNodeTargetEntityRefs()
        pet2.getObject().setActive(false);
        pet2.save();
        pets = person.getNodeTargetEntities("patients");
        assertEquals(1, pets.size());
        assertFalse(pets.contains(pet2.getEntity()));

        petRefs = person.getNodeTargetEntityRefs("patients");
        assertEquals(2, petRefs.size());

        // check sources
        List<Entity> customers = pet1.getNodeSourceEntities("customers");
        assertEquals(1, customers.size());
        assertTrue(customers.contains(person.getEntity()));

        List<IMObjectReference> custRefs
                = pet2.getNodeSourceEntityRefs("customers");
        assertEquals(1, custRefs.size());
        assertTrue(custRefs.contains(person.getReference()));
    }

    /**
     * Tests the {@link EntityBean#getNodeSourceEntities(String, Date)},
     * {@link EntityBean#getNodeSourceEntityRefs(String, Date)},
     * {@link EntityBean#getNodeTargetEntities(String, Date)} and
     * {@link EntityBean#getNodeTargetEntityRefs(String, Date)} methods.
     */
    public void testGetNodeEntitiesByTime() {
        EntityBean pet1 = createPet();
        EntityBean pet2 = createPet();
        EntityBean person = createPerson();

        // add a relationship to the person and pets
        EntityRelationship r1 = person.addRelationship(OWNER, pet1.getEntity());
        pet1.getEntity().addEntityRelationship(r1);
        EntityRelationship r2 = person.addRelationship(OWNER, pet2.getEntity());
        pet2.getEntity().addEntityRelationship(r2);

        // verify the pets are returned for a time > the default start time
        List<Entity> pets = person.getNodeTargetEntities("patients",
                                                         new Date());
        assertEquals(2, pets.size());
        assertTrue(pets.contains(pet1.getObject()));
        assertTrue(pets.contains(pet2.getObject()));

        List<IMObjectReference> petRefs
                = person.getNodeTargetEntityRefs("patients", new Date());
        assertEquals(2, petRefs.size());
        assertTrue(petRefs.contains(pet1.getReference()));
        assertTrue(petRefs.contains(pet2.getReference()));

        // now mark pet2 inactive. Will no longer be returned by
        // getNodeTargetEntities(). but will still be returned by
        // getNodeTargetEntityRefs()
        pet2.getObject().setActive(false);
        pet2.save();
        pets = person.getNodeTargetEntities("patients", new Date());
        assertEquals(1, pets.size());
        assertFalse(pets.contains(pet2.getEntity()));

        petRefs = person.getNodeTargetEntityRefs("patients", new Date());
        assertEquals(2, petRefs.size());

        // verify the person is returned for a time > the default start time
        List<Entity> people = pet1.getNodeSourceEntities("relationships",
                                                         new Date());
        assertEquals(1, people.size());
        assertTrue(people.contains(person.getObject()));

        List<IMObjectReference> peopleRefs
                = pet2.getNodeSourceEntityRefs("relationships", new Date());
        assertEquals(1, peopleRefs.size());
        assertTrue(peopleRefs.contains(person.getReference()));

        // verify no entity returned if the relationship has no start time
        r1.setActiveStartTime(null);
        r2.setActiveStartTime(null);
        pets = person.getNodeTargetEntities("patients", new Date());
        assertEquals(0, pets.size());
        people = pet2.getNodeSourceEntities("relationships", new Date());
        assertEquals(0, people.size());

        petRefs = person.getNodeTargetEntityRefs("patients", new Date());
        assertEquals(0, petRefs.size());
        peopleRefs = pet2.getNodeSourceEntityRefs("relationships", new Date());
        assertEquals(0, peopleRefs.size());

        // now set the start and end time and verify that there is no entities
        // for a later time (use time addition due to system clock granularity)
        Date start = new Date();
        Date end = new Date(start.getTime() + 1);
        Date later = new Date(end.getTime() + 1);
        r1.setActiveStartTime(start);
        r1.setActiveEndTime(end);
        r2.setActiveStartTime(start);
        r2.setActiveEndTime(end);
        pets = person.getNodeTargetEntities("patients", later);
        assertEquals(0, pets.size());
        people = pet2.getNodeSourceEntities("relationships", later);
        assertEquals(0, people.size());

        petRefs = person.getNodeTargetEntityRefs("patients", later);
        assertEquals(0, petRefs.size());
        peopleRefs = pet2.getNodeSourceEntityRefs("relationships", later);
        assertEquals(0, peopleRefs.size());
    }

    /**
     * Tests the {@link EntityBean#getNodeSourceEntities(String, Date)},
     * {@link EntityBean#getNodeSourceEntities(String, Date, boolean)},
     * {@link EntityBean#getNodeSourceEntityRefs(String, Date)},
     * {@link EntityBean#getNodeSourceEntityRefs(String, Date, boolean)},
     * {@link EntityBean#getNodeTargetEntities(String, Date)} and
     * {@link EntityBean#getNodeTargetEntities(String, Date, boolean)},
     * {@link EntityBean#getNodeTargetEntityRefs(String, Date)} and
     * {@link EntityBean#getNodeTargetEntityRefs(String, Date, boolean)} methods
     * for active/inactive relationships and entities.
     */
    public void testGetNodeEntitiesByTimeAndActive() {
        EntityBean pet1Bean = createPet();
        EntityBean pet2Bean = createPet();
        EntityBean personBean = createPerson();
        Entity pet1 = pet1Bean.getEntity();
        Entity pet2 = pet2Bean.getEntity();
        Entity person = personBean.getEntity();

        // add a relationship to the person and pets
        EntityRelationship r1 = personBean.addRelationship(OWNER, pet1);
        pet1Bean.getEntity().addEntityRelationship(r1);
        r1.setActive(false);

        EntityRelationship r2 = personBean.addRelationship(OWNER, pet2);
        pet2Bean.getEntity().addEntityRelationship(r2);
        r2.setActive(false);

        Date now = new Date();
        List<Entity> patients;
        List<IMObjectReference> patientRefs;
        List<Entity> relationships;
        List<IMObjectReference> relationshipRefs;

        patients = personBean.getNodeTargetEntities("patients", now);
        patientRefs = personBean.getNodeTargetEntityRefs("patients", now);
        relationships = pet1Bean.getNodeSourceEntities("relationships", now);
        relationshipRefs = pet1Bean.getNodeSourceEntityRefs("relationships",
                                                            now);
        assertTrue(patientRefs.isEmpty());
        assertTrue(patients.isEmpty());
        assertTrue(relationships.isEmpty());
        assertTrue(relationshipRefs.isEmpty());

        patients = personBean.getNodeTargetEntities("patients", now, false);
        patientRefs = personBean.getNodeTargetEntityRefs("patients", now,
                                                         false);
        relationships = pet1Bean.getNodeSourceEntities("relationships", now,
                                                       false);
        relationshipRefs = pet1Bean.getNodeSourceEntityRefs("relationships",
                                                            now, false);
        assertEquals(2, patients.size());
        assertEquals(2, patientRefs.size());
        assertEquals(1, relationships.size());
        assertEquals(1, relationshipRefs.size());

        // re-activate the relationship, but deactivate the entities
        r1.setActive(true);
        r2.setActive(true);
        pet1.setActive(false);
        pet2.setActive(false);
        person.setActive(false);
        pet1Bean.save();
        pet2Bean.save();
        personBean.save();

        patients = personBean.getNodeTargetEntities("patients", now);
        patientRefs = personBean.getNodeTargetEntityRefs("patients", now);
        relationships = pet1Bean.getNodeSourceEntities("relationships", now);
        relationshipRefs = pet1Bean.getNodeSourceEntityRefs("relationships",
                                                            now);
        assertTrue(patients.isEmpty());
        assertEquals(2, patientRefs.size());
        assertTrue(relationships.isEmpty());
        assertEquals(1, relationshipRefs.size());

        patients = personBean.getNodeTargetEntities("patients", now, false);
        patientRefs = personBean.getNodeTargetEntityRefs("patients", now,
                                                         false);
        relationships = pet1Bean.getNodeSourceEntities("relationships", now,
                                                       false);
        relationshipRefs = pet1Bean.getNodeSourceEntityRefs("relationships",
                                                            now, false);
        assertEquals(2, patients.size());
        assertEquals(2, patientRefs.size());
        assertEquals(1, relationships.size());
        assertEquals(1, relationshipRefs.size());
    }

    /**
     * Verifies that an entity relationship matches that expected.
     *
     * @param relationship the relationship
     * @param shortName    the expected short name
     * @param source       the expected source entity
     * @param target       the expected target enttiy
     */
    private void checkRelationship(EntityRelationship relationship,
                                   String shortName, Entity source,
                                   Entity target) {
        assertNotNull(relationship);
        assertEquals(shortName, relationship.getArchetypeId().getShortName());
        assertEquals(source.getObjectReference(), relationship.getSource());
        assertEquals(target.getObjectReference(), relationship.getTarget());
    }

    /**
     * Creates and saves a pet.
     *
     * @return a new pet
     */
    private EntityBean createPet() {
        EntityBean pet = createBean("party.animalpet");
        pet.setValue("name", "XAnimalPet");
        pet.setValue("species", "CANINE");
        pet.save();
        return pet;
    }

    /**
     * Creates and saves a person.
     *
     * @return a new person
     */
    private EntityBean createPerson() {
        EntityBean person = createBean("party.person");
        person.setValue("firstName", "J");
        person.setValue("lastName", "Zoo");
        person.save();
        return person;
    }

    /**
     * (non-Javadoc)
     *
     * @see AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    @Override
    protected String[] getConfigLocations() {
        return new String[]{
                "org/openvpms/component/business/service/archetype/archetype-service-appcontext.xml"
        };
    }

    /**
     * Helper to create an object.
     *
     * @param shortName the archetype short name
     * @return the new object
     */
    private IMObject create(String shortName) {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        IMObject object = service.create(shortName);
        assertNotNull(object);
        return object;
    }

    /**
     * Helper to create an entity and wrap it in an {@link EntityBean}.
     *
     * @param shortName the archetype short name
     * @return the bean wrapping an instance of <code>shortName</code>.
     */
    private EntityBean createBean(String shortName) {
        Entity object = (Entity) create(shortName);
        return new EntityBean(object);
    }

}
