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

package org.openvpms.component.business.service.archetype.rule;

import static org.junit.Assert.*;
import org.junit.Test;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Set;


/**
 * Tests the {@link ArchetypeRuleService} class.
 *
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate: 2005-12-08 00:31:09 +1100 (Thu, 08 Dec 2005) $
 */
@ContextConfiguration("rule-engine-appcontext.xml")
public class ArchetypeRuleServiceTestCase extends AbstractJUnit4SpringContextTests {

    /**
     * The archetype service.
     */
    @Autowired
    private IArchetypeRuleService service;

    /**
     * The transaction manager.
     */
    @Autowired
    private PlatformTransactionManager txnManager;


    /**
     * Verifies that rule engine is called when an object is being saved.
     * If both the before and after rules execute, the object will have
     * 'before' and 'after' properties set to <tt>true</tt>.
     * <p/>
     * Requires the <em>archetypeService.save.party.person.before</em> and
     * <em>archetypeService.save.party.person.after</em> rules.
     */
    @Test
    public void testSave() {
        Party person = createPerson("MR", "Jim", "Alateras");
        service.save(person);
        checkPerson(person);
    }

    /**
     * Verifies that rule engine is called when objects are being saved.
     * If both the before and after rules execute, the object will have
     * 'before' and 'after' properties set to <tt>true</tt>.
     * <p/>
     * Requires the <em>archetypeService.save.party.person.before</em> and
     * <em>archetypeService.save.party.person.after</em> rules.
     */
    @Test
    public void testSaveCollection() {
        Party person1 = createPerson("MR", "Jim", "Alateras");
        Party person2 = createPerson("MR", "Tim", "Anderson");
        Collection<IMObject> list = Arrays.asList((IMObject) person1, person2);
        service.save(list);
        checkPerson(person1);
        checkPerson(person2);
    }

    /**
     * Verifies that the rule engine is called when an object is removed.
     * <p/>
     * If both the before and after rules execute, the object will have
     * 'remove_before' and 'remove_after' properties set to <tt>true</tt>.
     * <p/>
     * Requires the <em>archetypeService.remove.party.person.before</em> and
     * <em>archetypeService.remove.party.person.after</em> rules.
     */
    @Test
    public void testRemoveObject() {
        Party person = createPerson("MR", "Tim", "Anderson");
        service.remove(person);

        // verify that the remove.before and after rules have been invoked
        assertEquals(Boolean.TRUE, person.getDetails().get("remove_after"));
        assertEquals(Boolean.TRUE, person.getDetails().get("remove_before"));

        // the save.before and after rules should not have been invoked
        assertNull(person.getDetails().get("before"));
        assertNull(person.getDetails().get("after"));

        // verify that the person has been removed
        person = reload(person);
        assertNull(person);
    }

    /**
     * Test for OVPMS-127.
     */
    @Test
    public void testOVPMS127() {
        Party personA = createPerson("MR", "Jim", "Alateras");
        service.save(personA);
        Party personB = createPerson("MR", "Oscar", "Alateras");
        service.save(personB);
        Party pet = createAnimal("lucky");
        service.save(pet);

        // create the entity relationships
        EntityRelationship rel = createEntityRelationship(
                personA, pet, "entityRelationship.animalOwner");
        pet.addEntityRelationship(rel);
        service.save(pet);
        assertNull(rel.getActiveEndTime());

        EntityRelationship rel1 = createEntityRelationship(
                personB, pet, "entityRelationship.animalOwner");
        pet.addEntityRelationship(rel1);
        service.save(pet);

        assertNotNull(rel.getActiveEndTime());
        assertNull(rel1.getActiveEndTime());

        pet = createAnimal("billy");
        service.save(pet);
        rel = createEntityRelationship(personB, pet,
                                       "entityRelationship.animalOwner");
        personB.addEntityRelationship(rel);
        service.save(personB);
    }

    /**
     * Verifies that when a rule throws an exception, it is propagated, and
     * no changes are saved.
     * This requires that:
     * <ul>
     * <li><em>the archetypeService.save.act.simple.before</em> rule throws an
     * IllegalStateException when the act status is "EXCEPTION_BEFORE"
     * <li><em>the archetypeService.save.act.simple.after</em> rule throws an
     * IllegalStateException when the act status is "EXCEPTION_AFTER"
     * <li>both rules set the act reason to the act status when they throw.
     * </ul>
     */
    @Test
    public void testException() {
        Act act = (Act) service.create("act.simple");
        checkException(act, null);
        checkException(act, "EXCEPTION_BEFORE");
        checkException(act, "EXCEPTION_AFTER");
    }

    /**
     * Verifies that if a rule throws an exception, all changes are rolled
     * back.
     */
    @Test
    public void testTransactionRollbackOnException() {
        Party person = createPerson("MR", "T", "Anderson");
        Act act = (Act) service.create("act.simple");
        service.save(person);
        service.save(act);

        // start a new transaction
        TransactionStatus status = txnManager.getTransaction(
                new DefaultTransactionDefinition());

        // change some details
        person.getDetails().put("lastName", "Foo");

        service.save(person);

        try {
            // make the act.simple.after save rule throw an exception
            act.setStatus("EXCEPTION_AFTER");
            service.save(act);
            fail("Expected save to fail");
        } catch (Exception expected) {
            // expected
        }
        try {
            txnManager.commit(status);
            fail("Expected commit to fail");
        } catch (TransactionException expected) {
            // verify that no changes are made persistent
            person = reload(person);
            act = reload(act);
            assertEquals(person.getName(), "Anderson,T");
            assertNull(act.getStatus());
        }
    }

    /**
     * Verifies that a rule can establish its own isolated transaction.
     * This saves an act.simple which triggers a rule which creates and saves a
     * new act in a separate transaction, prior to throwing an exception. The
     * new act should save, but the original act shouldn't.
     *
     * @see ActSimpleRules#insertNewActInIsolation
     */
    @Test
    public void testTransactionIsolation() {
        Act act = (Act) service.create("act.simple");
        try {
            act.setStatus("INSERT_NEW_AND_THROW");
            service.save(act);
            fail("Expected save to throw exception");
        } catch (Exception expected) {
            // expected
        }

        // ensure the act has not been saved as the exception should have rolled
        // back the transaction
        assertTrue(act.isNew());

        // verify that there is an associated act that did save
        Set<ActRelationship> relationships = act.getActRelationships();
        assertEquals(1, relationships.size());
        ActRelationship relationship = relationships.iterator().next();
        Act related = (Act) get(relationship.getSource());
        assertNotNull(related);
    }

    /**
     * Verifies that the before and after rules have been invoked after a
     * <em>party.person</em> has been saved/removed.
     * If both rules execute, the object will have 'before' and 'after'
     * properties set to <tt>true</tt>.
     *
     * @param person the person
     */
    private void checkPerson(Party person) {
        assertEquals(Boolean.TRUE, person.getDetails().get("before"));
        assertEquals(Boolean.TRUE, person.getDetails().get("after"));

        person = reload(person);
        assertNotNull(person);

        // verify the persistent instance has the before property
        assertEquals(Boolean.TRUE, person.getDetails().get("before"));
    }

    /**
     * Tests the behaviour of rules throwing exceptions.
     *
     * @param act    the act
     * @param status the act status
     * @see #testException()
     */
    private void checkException(Act act, String status) {
        long version = act.getVersion();
        try {
            act.setStatus(status);
            service.save(act);
            if (status != null) {
                fail("Expected save of act.simple to fail");
            }
        } catch (Throwable exception) {
            if (status == null) {
                fail("Expected save of act.simple to succeed");
            } else {
                // verify that the correct rule threw the exception
                assertEquals(status, act.getReason());

                // verify that an IllegalStateException is the root cause
                while (exception.getCause() != null) {
                    exception = exception.getCause();
                }
                if (!(exception instanceof IllegalStateException)) {
                    fail("Expected rule to throw IllegalStateException");
                }

                if (!act.isNew()) {
                    // verify that the changes weren't saved
                    Act original = reload(act);
                    assertEquals(version, original.getVersion());
                }
            }
        }
    }

    /**
     * Helper to retrieve an object given its reference.
     *
     * @param ref the object reference
     * @return the corresponding object or <tt>null</tt>
     */
    private IMObject get(IMObjectReference ref) {
        return service.get(ref);
    }

    /**
     * Helper to reload an object.
     *
     * @param object the object to reload
     * @return the reloaded object
     */
    @SuppressWarnings("unchecked")
    private <T extends IMObject> T reload(T object) {
        return (T) get(object.getObjectReference());
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
     * Creates a phone contact.
     *
     * @return Contact
     */
    private Contact createPhoneContact() {
        Contact contact = (Contact) service.create("contact.phoneNumber");
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
        EntityRelationship rel = (EntityRelationship) service.create(
                shortName);

        rel.setActiveStartTime(new Date());
        rel.setSource(source.getObjectReference());
        rel.setTarget(target.getObjectReference());

        return rel;
    }
}
