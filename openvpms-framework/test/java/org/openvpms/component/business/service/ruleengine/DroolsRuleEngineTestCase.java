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
 *  $Id: DroolsRuleEngineTestCase.java 328 2005-12-07 13:31:09Z jalateras $
 */

package org.openvpms.component.business.service.ruleengine;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.ValidationError;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.Date;


/**
 * Test the
 * {@link org.openvpms.component.business.service.ruleengine.DroolsRuleEngine}
 * class.
 *
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate: 2005-12-08 00:31:09 +1100 (Thu, 08 Dec 2005) $
 */
@SuppressWarnings("HardCodedStringLiteral")
public class DroolsRuleEngineTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * Holds a reference to the entity service.
     */
    private IArchetypeService archetype;


    /**
     * Test that rule engine is called when this object is being saved.
     */
    public void testRuleEngineOnSave() throws Exception {
        try {
            Party person = createPerson("MR", "Jim", "Alateras");
            archetype.save(person);
        } catch (ValidationException exception) {
            for (ValidationError error : exception.getErrors()) {
                logger.error(error.toString());
            }

            //rethrow exception
            throw exception;
        }
    }

    /**
     * Test for OVPMS-127.
     */
    public void testOVPMS127() throws Exception {
        Party personA = createPerson("MR", "Jim", "Alateras");
        archetype.save(personA);
        Party personB = createPerson("MR", "Oscar", "Alateras");
        archetype.save(personB);
        Party pet = createAnimal("lucky");
        archetype.save(pet);

        // create the entity relationships
        EntityRelationship rel = createEntityRelationship(personA, pet,
                                                          "entityRelationship.animalOwner");
        pet.addEntityRelationship(rel);
        archetype.save(pet);
        assertTrue(rel.getActiveEndTime() == null);

        EntityRelationship rel1 = createEntityRelationship(personB, pet,
                                                           "entityRelationship.animalOwner");
        pet.addEntityRelationship(rel1);
        archetype.save(pet);

        assertTrue(rel.getActiveEndTime() != null);
        assertTrue(rel1.getActiveEndTime() == null);

        pet = createAnimal("billy");
        archetype.save(pet);
        rel = createEntityRelationship(personB, pet,
                                       "entityRelationship.animalOwner");
        personB.addEntityRelationship(rel);
        archetype.save(personB);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    @Override
    protected String[] getConfigLocations() {
        return new String[]{"org/openvpms/component/business/service/ruleengine/rule-engine-appcontext.xml"};
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        this.archetype = (IArchetypeService) applicationContext
                .getBean("archetypeService");
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
        Party person = (Party) archetype.create("party.person");
        person.getDetails().put("lastName", lastName);
        person.getDetails().put("firstName", firstName);
        person.getDetails().put("title", title);
        person.addContact(createPhoneContact());

        return person;
    }

    /**
     * Creates an animal entity.
     *
     * @param name the name of the pet
     * @return Animal
     */
    private Party createAnimal(String name) {
        Party pet = (Party) archetype.create("party.animalpet");
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
     * Creates a phone contact.
     *
     * @return Contact
     */
    private Contact createPhoneContact() {
        Contact contact = (Contact) archetype.create("contact.phoneNumber");
        contact.getDetails().put("areaCode", "03");
        contact.getDetails().put("telephoneNumber", "1234567");
        contact.getDetails().put("preferred", true);

        return contact;
    }

    /**
     * Create an entity relationship of the specified type between the
     * source and target entities.
     *
     * @param source    the source entity
     * @param target    the target entity
     * @param shortName the short name of the relationship to create
     * @return EntityRelationship
     */
    private EntityRelationship createEntityRelationship(Entity source,
                                                        Entity target,
                                                        String shortName) {
        EntityRelationship rel = (EntityRelationship) archetype.create(
                shortName);

        rel.setActiveStartTime(new Date());
        rel.setSequence(1);
        rel.setSource(source.getObjectReference());
        rel.setTarget(target.getObjectReference());

        return rel;

    }
}
