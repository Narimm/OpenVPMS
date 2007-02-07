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
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.Date;


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
        Party pet = (Party) create("animal.pet");
        EntityBean bean = createBean("person.person");
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
        EntityBean pet = createBean("animal.pet");
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
        EntityBean person = createBean("person.person");
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
