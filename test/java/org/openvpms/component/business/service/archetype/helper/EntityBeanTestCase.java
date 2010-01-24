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

import org.apache.commons.collections.Predicate;
import static org.junit.Assert.*;
import org.junit.Test;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.AbstractArchetypeServiceTest;
import org.openvpms.component.business.service.archetype.functor.IsA;
import org.openvpms.component.business.service.archetype.functor.RefEquals;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.List;


/**
 * Tests the {@link EntityBean} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
@ContextConfiguration("../archetype-service-appcontext.xml")
public class EntityBeanTestCase extends AbstractArchetypeServiceTest {

    /**
     * Owner entity relationship short name.
     */
    private final String OWNER = "entityRelationship.animalOwner";

    /**
     * Carer entity relationship short name.
     */
    private final String CARER = "entityRelationship.animalCarer";


    /**
     * Tests the {@link EntityBean#addRelationship} and
     * {@link EntityBean#getRelationship)} methods.
     */
    @Test
    public void testRelationships() {
        Party pet = (Party) create("party.animalpet");
        EntityBean bean = createPerson();
        assertNull(bean.getRelationship(pet));

        EntityRelationship r = bean.addRelationship(OWNER, pet);
        checkRelationship(r, OWNER, bean.getEntity(), pet);
        r = bean.getRelationship(pet);
        checkRelationship(r, OWNER, bean.getEntity(), pet);

        bean.removeRelationship(r);
        assertNull(bean.getRelationship(pet));
    }

    /**
     * Tests the {@link EntityBean#getRelationships} methods.
     */
    @Test
    public void testGetRelationships() {
        EntityBean petBean = createPet();
        EntityBean personBean = createPerson();
        Entity pet = petBean.getEntity();

        assertNull(personBean.getRelationship(pet));

        // add owner and carer relationships
        EntityRelationship r1 = personBean.addRelationship(OWNER, pet);
        EntityRelationship r2 = personBean.addRelationship(CARER, pet);

        // check that the getRelationships() method returns the correct values
        // for owners
        List<EntityRelationship> owners = personBean.getRelationships(OWNER);
        assertEquals(1, owners.size());
        assertTrue(owners.contains(r1));

        // ... and carers
        List<EntityRelationship> carers = personBean.getRelationships(CARER);
        assertEquals(1, carers.size());
        assertTrue(carers.contains(r2));

        // ... and supports wildcards
        List<EntityRelationship> all
                = personBean.getRelationships("entityRelationship.*");
        assertEquals(2, all.size());

        // de-activate the owner relationship
        deactivateRelationship(r1);
        assertEquals(0, personBean.getRelationships(OWNER).size());
        assertEquals(1, personBean.getRelationships(OWNER, false).size());

        // remove the owner relationship and verify its removal
        personBean.removeRelationship(r1);
        assertEquals(0, personBean.getRelationships(OWNER).size());

        // remove the carer relationship and verify its removal
        personBean.removeRelationship(r2);
        assertEquals(0, personBean.getRelationships(CARER).size());
    }

    /**
     * Tests the {@link EntityBean#getSourceEntity(String)}
     * and {@link EntityBean#getTargetEntity(String)} methods.
     */
    @Test
    public void testGetEntity() {
        EntityBean pet1Bean = createPet();
        EntityBean pet2Bean = createPet();
        EntityBean personBean = createPerson();
        Entity pet1 = pet1Bean.getEntity();
        Entity pet2 = pet2Bean.getEntity();
        Entity person = personBean.getEntity();

        // add a relationship to the person and pet
        EntityRelationship r = personBean.addRelationship(OWNER, pet1);

        // add an inactive owner relationship. Should never be returned.
        EntityRelationship inactive = personBean.addRelationship(OWNER, pet2);
        deactivateRelationship(inactive);

        // verify the person is the source, and pet the target
        assertEquals(person, personBean.getSourceEntity(OWNER));
        assertEquals(pet1, personBean.getTargetEntity(OWNER));

        // verify the pet is the target, and the person the source
        assertEquals(pet1, pet1Bean.getTargetEntity(OWNER));
        assertEquals(person, pet1Bean.getSourceEntity(OWNER));

        // mark the relationship inactive.
        deactivateRelationship(r);
        assertNull(personBean.getSourceEntity(OWNER));
        assertNull(pet1Bean.getTargetEntity(OWNER));
    }

    /**
     * Tests the {@link EntityBean#getSourceEntity(String, Date)}
     * and {@link EntityBean#getTargetEntity(String, Date)} methods.
     */
    @Test
    public void testGetEntityByTime() {
        EntityBean petBean = createPet();
        EntityBean personBean = createPerson();
        Entity pet = petBean.getEntity();
        Entity person = personBean.getEntity();

        // add an inactive owner relationship. Should never be returned.
        EntityBean inactivePet = createPet();
        EntityRelationship inactive
                = personBean.addRelationship(OWNER, inactivePet.getEntity());
        deactivateRelationship(inactive);

        // add a relationship to the person and pet
        EntityRelationship r = personBean.addRelationship(OWNER, pet);

        // verify the pet is returned for a time > the default start time
        Entity pet2 = personBean.getTargetEntity(OWNER, new Date());
        assertEquals(pet, pet2);

        // ... same for person
        Entity person2 = petBean.getSourceEntity(OWNER, new Date());
        assertEquals(person, person2);

        // verify the correct entity is returned if the relationship has no
        // start time (i.e, unbounded)
        r.setActiveStartTime(null);
        assertEquals(pet, personBean.getTargetEntity(OWNER, new Date()));
        assertEquals(person, petBean.getSourceEntity(OWNER, new Date()));

        // now set the start and end time and verify that there is no entities
        // for a later time (use time addition due to system clock granularity)
        Date start = new Date();
        Date end = new Date(start.getTime() + 1);
        Date later = new Date(end.getTime() + 1);
        r.setActiveStartTime(start);
        r.setActiveEndTime(end);
        assertNull(personBean.getTargetEntity(OWNER, later));
        assertNull(petBean.getSourceEntity(OWNER, later));
    }

    /**
     * Tests the {@link EntityBean#getSourceEntity(String)},
     * {@link EntityBean#getSourceEntity(String, boolean)},
     * {@link EntityBean#getSourceEntity(String, Date)},
     * {@link EntityBean#getSourceEntity(String, Date, boolean)},
     * {@link EntityBean#getSourceEntityRef(String)},
     * {@link EntityBean#getSourceEntityRef(String, boolean)},
     * {@link EntityBean#getTargetEntity(String)},
     * {@link EntityBean#getTargetEntity(String, boolean)},
     * {@link EntityBean#getTargetEntity(String, Date)}
     * {@link EntityBean#getTargetEntity(String, Date, boolean)},
     * {@link EntityBean#getTargetEntityRef(String)} and
     * {@link EntityBean#getTargetEntityRef(String, boolean)} methods
     * for active/inactive relationships.
     */
    @Test
    public void testGetEntityWithActive() {
        EntityBean pet1Bean = createPet();
        EntityBean pet2Bean = createPet();
        EntityBean personBean = createPerson();
        Entity pet1 = pet1Bean.getEntity();
        Entity pet2 = pet2Bean.getEntity();
        Entity person = personBean.getEntity();
        IMObjectReference pet1Ref = pet1.getObjectReference();
        IMObjectReference pet2Ref = pet2.getObjectReference();
        IMObjectReference personRef = person.getObjectReference();

        // add an owner relationship to the person and pet1
        EntityRelationship r = personBean.addRelationship(OWNER, pet1);
        Date rel1Active = new Date();

        // verify each of the getTarget* and getSource* methods get the
        // right entities
        assertEquals(pet1, personBean.getTargetEntity(OWNER));
        assertEquals(pet1, personBean.getTargetEntity(OWNER, rel1Active));
        assertEquals(person, pet1Bean.getSourceEntity(OWNER));
        assertEquals(person, pet1Bean.getSourceEntity(OWNER, rel1Active));
        assertEquals(pet1, personBean.getTargetEntity(OWNER, false));
        assertEquals(pet1,
                     personBean.getTargetEntity(OWNER, rel1Active, false));
        assertEquals(person, pet1Bean.getSourceEntity(OWNER, false));
        assertEquals(person,
                     pet1Bean.getSourceEntity(OWNER, rel1Active, false));

        assertEquals(pet1Ref, personBean.getTargetEntityRef(OWNER));
        assertEquals(pet1Ref, personBean.getTargetEntityRef(OWNER, false));
        assertEquals(personRef, pet1Bean.getSourceEntityRef(OWNER));
        assertEquals(personRef, pet1Bean.getSourceEntityRef(OWNER, false));

        // mark the relationship inactive. Will be considered active relative
        // to 'rel1Active' time.
        deactivateRelationship(r);

        // verify methods that require the relationship to be active return null
        assertNull(personBean.getTargetEntity(OWNER));
        assertNull(personBean.getTargetEntityRef(OWNER));
        assertNull(pet1Bean.getSourceEntity(OWNER));
        assertNull(pet1Bean.getSourceEntityRef(OWNER));

        // ... while those that don't get the right entities
        assertEquals(pet1, personBean.getTargetEntity(OWNER, false));
        assertEquals(pet1, personBean.getTargetEntity(OWNER, rel1Active));
        assertEquals(pet1Ref, personBean.getTargetEntityRef(OWNER, false));
        assertEquals(person, pet1Bean.getSourceEntity(OWNER, false));
        assertEquals(person, pet1Bean.getSourceEntity(OWNER, rel1Active));
        assertEquals(personRef, pet1Bean.getSourceEntityRef(OWNER, false));

        // add a new active owner relationship for pet2. This should be returned
        // instead of pet1.
        personBean.addRelationship(OWNER, pet2);
        Date rel2Active = new Date();

        assertEquals(pet2, personBean.getTargetEntity(OWNER));
        assertEquals(pet2, personBean.getTargetEntity(OWNER, rel2Active));
        assertEquals(person, pet2Bean.getSourceEntity(OWNER));
        assertEquals(person, pet2Bean.getSourceEntity(OWNER, rel2Active));
        assertEquals(pet2,
                     personBean.getTargetEntity(OWNER, rel2Active, false));
        assertEquals(person, pet2Bean.getSourceEntity(OWNER, false));
        assertEquals(person,
                     pet2Bean.getSourceEntity(OWNER, rel2Active, false));

        assertEquals(pet2Ref, personBean.getTargetEntityRef(OWNER));
        assertEquals(pet2Ref, personBean.getTargetEntityRef(OWNER, false));
        assertEquals(personRef, pet2Bean.getSourceEntityRef(OWNER));
        assertEquals(personRef, pet2Bean.getSourceEntityRef(OWNER, false));
    }

    /**
     * Tests the {@link EntityBean#getNodeSourceEntity(String)}
     * and {@link EntityBean#getNodeTargetEntity(String)} methods.
     */
    @Test
    public void testGetNodeEntity() {
        EntityBean pet1Bean = createPet();
        EntityBean pet2Bean = createPet();
        EntityBean personBean = createPerson();
        Entity pet1 = pet1Bean.getEntity();
        Entity pet2 = pet2Bean.getEntity();
        Entity person = personBean.getEntity();

        // add a relationship to the person and pet
        EntityRelationship r = personBean.addRelationship(OWNER, pet1);

        // add an inactive owner relationship. Should never be returned.
        EntityRelationship inactive = personBean.addRelationship(OWNER, pet2);
        deactivateRelationship(inactive);

        // verify the person is the source, and pet the target
        assertEquals(person, personBean.getNodeSourceEntity("patients"));
        assertEquals(pet1, personBean.getNodeTargetEntity("patients"));

        // verify the pet is the target, and the person the source
        assertEquals(pet1, pet1Bean.getNodeTargetEntity("relationships"));
        assertEquals(person, pet1Bean.getNodeSourceEntity("relationships"));

        // mark the relationship inactive.
        deactivateRelationship(r);
        assertNull(personBean.getNodeSourceEntity("patients"));
        assertNull(pet1Bean.getNodeTargetEntity("relationships"));
    }

    /**
     * Tests the {@link EntityBean#getNodeSourceEntity(String, Date)}
     * and {@link EntityBean#getNodeTargetEntity(String, Date)} methods.
     */
    @Test
    public void testGetNodeEntityByTime() {
        EntityBean petBean = createPet();
        EntityBean personBean = createPerson();
        Entity pet = petBean.getEntity();
        Entity person = personBean.getEntity();

        // add an inactive owner relationship. Should never be returned.
        EntityBean inactivePet = createPet();
        EntityRelationship inactive
                = personBean.addRelationship(OWNER, inactivePet.getEntity());
        deactivateRelationship(inactive);

        // add a relationship to the person and pet
        EntityRelationship r = personBean.addRelationship(OWNER, pet);

        // verify the pet is returned for a time > the default start time
        Entity pet2 = personBean.getNodeTargetEntity("patients", new Date());
        assertEquals(pet, pet2);

        // ... same for person
        Entity person2 = petBean.getNodeSourceEntity("relationships",
                                                     new Date());
        assertEquals(person, person2);

        // verify the correct entity is returned if the relationship has no
        // start time (i.e, unbounded)
        r.setActiveStartTime(null);
        assertEquals(pet,
                     personBean.getNodeTargetEntity("patients", new Date()));
        assertEquals(person,
                     petBean.getNodeSourceEntity("relationships", new Date()));

        // now set the start and end time and verify that there is no entities
        // for a later time (use time addition due to system clock granularity)
        Date start = new Date();
        Date end = new Date(start.getTime() + 1);
        Date later = new Date(end.getTime() + 1);
        r.setActiveStartTime(start);
        r.setActiveEndTime(end);
        assertNull(personBean.getNodeTargetEntity("patients", later));
        assertNull(petBean.getNodeSourceEntity("relationships", later));
    }

    /**
     * Tests the {@link EntityBean#getNodeSourceEntity(String)},
     * {@link EntityBean#getNodeSourceEntity(String, boolean)},
     * {@link EntityBean#getNodeSourceEntity(String, Date)},
     * {@link EntityBean#getNodeSourceEntity(String, Date, boolean)},
     * {@link EntityBean#getNodeTargetEntity(String)},
     * {@link EntityBean#getNodeTargetEntity(String, boolean)},
     * {@link EntityBean#getNodeTargetEntity(String, Date)}
     * and {@link EntityBean#getNodeTargetEntity(String, Date, boolean)} methods
     * for active/inactive relationships and entities.
     */
    @Test
    public void testGetNodeEntityWithActive() {
        final String pets = "patients"; // patients node name
        final String owners = "relationships"; // owners node name

        EntityBean pet1Bean = createPet();
        EntityBean pet2Bean = createPet();
        EntityBean personBean = createPerson();
        Entity pet1 = pet1Bean.getEntity();
        Entity pet2 = pet2Bean.getEntity();
        Entity person = personBean.getEntity();

        // add an owner relationship to the person and pet1
        EntityRelationship r = personBean.addRelationship(OWNER, pet1);

        // verify each of the getNodeTarget* and getNodeSource* methods get the
        // right entities
        Date rel1Active = new Date();
        assertEquals(pet1, personBean.getNodeTargetEntity(pets));
        assertEquals(pet1, personBean.getNodeTargetEntity(pets, rel1Active));
        assertEquals(person, pet1Bean.getNodeSourceEntity(owners));
        assertEquals(person, pet1Bean.getNodeSourceEntity(owners, rel1Active));
        assertEquals(pet1, personBean.getNodeTargetEntity(pets, false));
        assertEquals(pet1,
                     personBean.getNodeTargetEntity(pets, rel1Active, false));
        assertEquals(person, pet1Bean.getNodeSourceEntity(owners, false));
        assertEquals(person,
                     pet1Bean.getNodeSourceEntity(owners, rel1Active, false));

        // mark the relationship inactive
        deactivateRelationship(r);

        // verify methods that require the relationship to be active now return
        // null
        assertNull(personBean.getNodeTargetEntity(pets));
        assertNull(pet1Bean.getNodeSourceEntity(owners));

        // ... while those that don't get the right entities
        assertEquals(pet1, personBean.getNodeTargetEntity(pets, rel1Active));
        assertEquals(person, pet1Bean.getNodeSourceEntity(owners, rel1Active));
        assertEquals(pet1, personBean.getNodeTargetEntity(pets, false));
        assertEquals(pet1, personBean.getNodeTargetEntity(pets, rel1Active,
                                                          false));
        assertEquals(person, pet1Bean.getNodeSourceEntity(owners, false));
        assertEquals(person, pet1Bean.getNodeSourceEntity(owners, rel1Active,
                                                          false));

        // add a new active owner relationship for pet2. This should be returned
        // instead of pet1.
        personBean.addRelationship(OWNER, pet2);
        Date rel2Active = new Date();

        assertEquals(pet2, personBean.getNodeTargetEntity(pets));
        assertEquals(pet2, personBean.getNodeTargetEntity(pets, rel2Active));
        assertEquals(person, pet2Bean.getNodeSourceEntity(owners));
        assertEquals(person, pet2Bean.getNodeSourceEntity(owners, rel2Active));
        assertEquals(pet2,
                     personBean.getNodeTargetEntity(pets, rel2Active, false));
        assertEquals(person, pet2Bean.getNodeSourceEntity(owners, false));
        assertEquals(person,
                     pet2Bean.getNodeSourceEntity(owners, rel2Active, false));
    }

    /**
     * Tests the {@link EntityBean#getNodeSourceEntities},
     * {@link EntityBean#getNodeSourceEntityRefs},
     * {@link EntityBean#getNodeTargetEntities} and
     * {@link EntityBean#getNodeTargetEntityRefs}  methods.
     */
    @Test
    public void testGetNodeEntities() {
        EntityBean pet1 = createPet();
        EntityBean pet2 = createPet();
        EntityBean person = createPerson();

        // add a relationship to the person and pets
        person.addRelationship(OWNER, pet1.getEntity());
        person.addRelationship(OWNER, pet2.getEntity());

        // check targets
        List<Entity> pets = person.getNodeTargetEntities("patients");
        assertEquals(2, pets.size());
        assertTrue(pets.contains(pet1.getEntity()));
        assertTrue(pets.contains(pet2.getEntity()));

        List<IMObjectReference> petRefs
                = person.getNodeTargetEntityRefs("patients");
        assertEquals(2, petRefs.size());
        assertTrue(petRefs.contains(pet1.getReference()));
        assertTrue(petRefs.contains(pet2.getReference()));

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
    @Test
    public void testGetNodeEntitiesByTime() {
        EntityBean pet1 = createPet();
        EntityBean pet2 = createPet();
        EntityBean person = createPerson();

        // add a relationship to the person and pets
        EntityRelationship r1 = person.addRelationship(OWNER, pet1.getEntity());
        EntityRelationship r2 = person.addRelationship(OWNER, pet2.getEntity());

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

        // verify the person is returned for a time > the default start time
        List<Entity> people = pet1.getNodeSourceEntities("relationships",
                                                         new Date());
        assertEquals(1, people.size());
        assertTrue(people.contains(person.getObject()));

        List<IMObjectReference> peopleRefs
                = pet2.getNodeSourceEntityRefs("relationships", new Date());
        assertEquals(1, peopleRefs.size());
        assertTrue(peopleRefs.contains(person.getReference()));

        // verify the correct entity is returned if the relationships have no
        // start time (i.e, unbounded)
        r1.setActiveStartTime(null);
        r2.setActiveStartTime(null);
        pets = person.getNodeTargetEntities("patients", new Date());
        assertEquals(2, pets.size());
        people = pet2.getNodeSourceEntities("relationships", new Date());
        assertEquals(1, people.size());

        petRefs = person.getNodeTargetEntityRefs("patients", new Date());
        assertEquals(2, petRefs.size());
        peopleRefs = pet2.getNodeSourceEntityRefs("relationships", new Date());
        assertEquals(1, peopleRefs.size());

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
    @Test
    public void testGetNodeEntitiesByTimeAndActive() {
        EntityBean pet1Bean = createPet();
        EntityBean pet2Bean = createPet();
        EntityBean personBean = createPerson();
        Entity pet1 = pet1Bean.getEntity();
        Entity pet2 = pet2Bean.getEntity();
        Entity person = personBean.getEntity();

        // add a relationship to the person and pets
        EntityRelationship r1 = personBean.addRelationship(OWNER, pet1);
        EntityRelationship r2 = personBean.addRelationship(OWNER, pet2);

        Date activeTime = new Date();
        List<Entity> patients;
        List<IMObjectReference> patientRefs;
        List<Entity> relationships;
        List<IMObjectReference> relationshipRefs;

        patients = personBean.getNodeTargetEntities("patients", activeTime);
        patientRefs = personBean.getNodeTargetEntityRefs("patients",
                                                         activeTime);
        relationships = pet1Bean.getNodeSourceEntities("relationships",
                                                       activeTime);
        relationshipRefs = pet1Bean.getNodeSourceEntityRefs("relationships",
                                                            activeTime);
        assertEquals(2, patients.size());
        assertTrue(patients.contains(pet1));
        assertTrue(patients.contains(pet2));

        assertEquals(2, patientRefs.size());
        assertTrue(patientRefs.contains(pet1.getObjectReference()));
        assertTrue(patientRefs.contains(pet2.getObjectReference()));

        assertEquals(1, relationships.size());
        assertTrue(relationships.contains(person));

        assertEquals(1, relationshipRefs.size());
        assertTrue(relationshipRefs.contains(person.getObjectReference()));

        // deactivate the relationships
        deactivateRelationship(r1);
        deactivateRelationship(r2);

        // relationships will be active relative to 'activeTime'
        patients = personBean.getNodeTargetEntities("patients", activeTime,
                                                    false);
        patientRefs = personBean.getNodeTargetEntityRefs("patients",
                                                         activeTime);
        relationships = pet1Bean.getNodeSourceEntities("relationships",
                                                       activeTime, false);
        relationshipRefs = pet1Bean.getNodeSourceEntityRefs("relationships",
                                                            activeTime);
        assertEquals(2, patients.size());
        assertEquals(2, patientRefs.size());
        assertEquals(1, relationships.size());
        assertEquals(1, relationshipRefs.size());

        // no relationships should be active relative to the current time
        Date now = new Date();
        patients = personBean.getNodeTargetEntities("patients", now);
        patientRefs = personBean.getNodeTargetEntityRefs("patients", now);
        relationships = pet1Bean.getNodeSourceEntities("relationships", now);
        relationshipRefs = pet1Bean.getNodeSourceEntityRefs("relationships",
                                                            now);
        assertTrue(patientRefs.isEmpty());
        assertTrue(patients.isEmpty());
        assertTrue(relationships.isEmpty());
        assertTrue(relationshipRefs.isEmpty());
    }

    /**
     * Tests the {@link EntityBean#getNodeRelationships(String)}
     * {@link EntityBean#getNodeRelationships(String, Predicate)},
     * and {@link EntityBean#getNodeRelationship(String, Predicate)}
     * methods.
     */
    @Test
    public void testGetNodeRelationships() {
        EntityBean pet1Bean = createPet();
        EntityBean pet2Bean = createPet();
        EntityBean personBean = createPerson();
        Entity pet1 = pet1Bean.getEntity();
        Entity pet2 = pet2Bean.getEntity();

        // add a relationship to the person and pets
        EntityRelationship r1 = personBean.addRelationship(OWNER, pet1);
        EntityRelationship r2 = personBean.addRelationship(OWNER, pet2);

        // test the getNodeRelationships(String) method
        List<EntityRelationship> patients1
                = personBean.getNodeRelationships("patients");
        assertEquals(2, patients1.size());

        // test the getNodeRelationships(String, Predicate) method
        List<EntityRelationship> patients2
                = personBean.getNodeRelationships("patients", new IsA(OWNER));
        assertEquals(2, patients2.size());

        List<EntityRelationship> dummies
                = personBean.getNodeRelationships("patients", new IsA("dummy"));
        assertEquals(0, dummies.size());

        // test the getNodeRelationship(String, Predicate) method
        assertEquals(r1, personBean.getNodeRelationship(
                "patients", RefEquals.getTargetEquals(pet1)));

        assertEquals(r2, personBean.getNodeRelationship(
                "patients", RefEquals.getTargetEquals(pet2)));
    }

    /**
     * Tests the {@link EntityBean#getNodeSourceEntities(String, Predicate)}
     * and {@link EntityBean#getNodeTargetEntities(String, Predicate)} methods.
     */
    @Test
    public void testGetNodeEntitiesByPredicate() {
        EntityBean pet1Bean = createPet();
        EntityBean pet2Bean = createPet();
        EntityBean personBean = createPerson();
        Entity person = personBean.getEntity();
        Entity pet1 = pet1Bean.getEntity();
        Entity pet2 = pet2Bean.getEntity();

        // add a relationship to the person and pets
        personBean.addRelationship(OWNER, pet1);
        personBean.addRelationship(OWNER, pet2);

        // check source entities
        List<Entity> people
                = personBean.getNodeSourceEntities("patients", new IsA(OWNER));
        assertEquals(2, people.size());
        for (Entity e : people) {
            assertEquals(person, e);
        }

        // verify there are no references when the predicate evaluates false
        List<Entity> dummies = personBean.getNodeSourceEntities(
                "patients", new IsA("dummy"));
        assertEquals(0, dummies.size());

        // check target entities
        List<Entity> pets
                = personBean.getNodeTargetEntities("patients", new IsA(OWNER));
        assertEquals(2, pets.size());
        assertTrue(pets.contains(pet1));
        assertTrue(pets.contains(pet2));

        // check source entity references
        List<IMObjectReference> refs = personBean.getNodeSourceEntityRefs(
                "patients", new IsA(OWNER));
        assertEquals(2, refs.size());
        for (IMObjectReference r : refs) {
            assertEquals(person.getObjectReference(), r);
        }

        // verify there are no references when the predicate evaluates false
        List<IMObjectReference> dummyRefs = personBean.getNodeSourceEntityRefs(
                "patients", new IsA("dummy"));
        assertTrue(dummyRefs.isEmpty());

        // check target entity references
        List<IMObjectReference> petRefs = personBean.getNodeTargetEntityRefs(
                "patients", new IsA(OWNER));
        assertEquals(2, petRefs.size());
        assertTrue(petRefs.contains(pet1.getObjectReference()));
        assertTrue(petRefs.contains(pet2.getObjectReference()));
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
        EntityBean pet = new EntityBean((Entity) create("party.animalpet"));
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
        EntityBean person = new EntityBean((Entity) create("party.person"));
        person.setValue("firstName", "J");
        person.setValue("lastName", "Zoo");
        person.save();
        return person;
    }

    /**
     * Helper to deactivate a relationship and sleep for a second, so that
     * subsequent isActive() checks return false. The sleep in between
     * deactivating a relationship and calling isActive() is required due to
     * system time granularity.
     *
     * @param relationship the relationship to deactivate
     */
    private void deactivateRelationship(EntityRelationship relationship) {
        relationship.setActive(false);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignore) {
            // do nothing
        }
    }

}
