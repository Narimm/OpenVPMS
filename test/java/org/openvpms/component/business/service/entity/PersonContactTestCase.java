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

import static org.junit.Assert.*;
import org.junit.Test;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.AbstractArchetypeServiceTest;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.springframework.test.context.ContextConfiguration;

import java.util.Random;


/**
 * Tests {@link Contact}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
@ContextConfiguration("entity-service-appcontext.xml")
public class PersonContactTestCase extends AbstractArchetypeServiceTest {

    /**
     * Test the creation of a person with contacts and addresses as
     * specified in the archetype
     */
    @Test
    public void testValidPersonContactCreation() {
        Party person = createPerson("party.person", "MR", "John", "Dillon");
        Contact contact = createLocationContact();
        person.addContact(contact);
        save(person);

        person = get(person);
        assertNotNull(person);
        assertEquals(1, person.getContacts().size());
    }

    /**
     * Test that the many-to-many relationship between contact and
     * address works.
     */
    @Test
    public void testContactRelationship() {
        Party person1 = createPerson("party.person", "MR", "John", "Dimantaris");
        Party person2 = createPerson("party.person", "MS", "Jenny", "Love");

        Contact contact1 = createLocationContact();
        Contact contact2 = createLocationContact();
        person1.addContact(contact1);
        save(person1);

        person2.addContact(contact2);
        save(person2);

        // save the entities

        // now attempt to retrieve the entities
        person1 = get(person1);
        assertNotNull(person1);
        assertTrue(person1.getContacts().size() == 1);

        person2 = get(person2);
        assertNotNull(person2);
        assertTrue(person2.getContacts().size() == 1);

        // now delete the address from person1 and update it.
        person1.getContacts().clear();
        assertTrue(person1.getContacts().size() == 0);
        save(person1);

        // retrieve the entities again and check that the addresses are
        // still valid
        person1 = get(person1);
        assertNotNull(person1);
        assertTrue(person1.getContacts().size() == 0);

        person2 = get(person2);
        assertNotNull(person2);
        assertTrue(person2.getContacts().size() == 1);
    }

    /**
     * Test the addition and removal of contacts
     */
    @Test
    public void testContactLifecycle() {
        Party person = createPerson("party.person", "MR", "Jim", "Alateras");
        person.addContact(createLocationContact());
        person.addContact(createLocationContact());
        person.addContact(createLocationContact());

        save(person);

        // retrieve and remove the first contact and update
        person = get(person);
        assertTrue(person.getContacts().size() == 3);
        Contact contact = person.getContacts().iterator().next();
        person.getContacts().remove(contact);
        assertTrue(person.getContacts().size() == 2);
        save(person);

        // retrieve and ensure thagt there are only 2 contacts
        person = get(person);
        assertTrue(person.getContacts().size() == 2);
    }

    /**
     * Test for OBF-49
     */
    @Test
    public void testOBF049() {
        Party person = createPerson("party.personobf49", "MR", "Jim", "Alateras");
        try {
            validateObject(person);
            fail("This should not have validated");
        } catch (ValidationException exception) {
            // ingore
        }

        // add classification
        person.addClassification(createLookup("lookup.staff"));
        person.addClassification(createLookup("lookup.patient"));
        validateObject(person);

        // add another classification
        try {
            person.addClassification(createLookup("lookup.patient"));
            validateObject(person);
            fail("This should not have validated");
        } catch (ValidationException exception) {
            // ingore
        }
    }

    /**
     * Create a valid location contact
     *
     * @return a new contact
     */
    private Contact createLocationContact() {
        Contact contact = (Contact) create("contact.location");

        contact.getDetails().put("address", "5 Kalulu Rd");
        contact.getDetails().put("suburb", "Belgrave");
        contact.getDetails().put("postcode", "3160");
        contact.getDetails().put("state", "VIC");
        contact.getDetails().put("country", "AU");
        return contact;
    }

    /**
     * Create a person
     *
     * @param shortName the type
     * @param title     the person's title
     * @param firstName the person's first name
     * @param lastName  the person's last name
     * @return Person
     */
    private Party createPerson(String shortName, String title, String firstName,
                               String lastName) {
        Party person = (Party) create(shortName);
        person.getDetails().put("lastName", lastName);
        person.getDetails().put("firstName", firstName);
        person.getDetails().put("title", title);

        return person;
    }

    /**
     * Creates a new lookup of the specified type.
     *
     * @param shortName the lookup short name
     * @return a new lookup
     */
    private Lookup createLookup(String shortName) {
        Lookup result = (Lookup) create(shortName);
        String code = shortName + new Random().nextInt();
        result.setCode(code);
        return result;
    }

}
