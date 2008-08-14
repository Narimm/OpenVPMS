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

package org.openvpms.component.business.service.archetype;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.lookup.LookupUtil;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.Arrays;
import java.util.Collection;


/**
 * Test that ability to create and query on parties.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
@SuppressWarnings("HardCodedStringLiteral")
public class ArchetypeServicePartyTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * Holds a reference to the entity service
     */
    private ArchetypeService service;


    /**
     * Test the creation of a simple contact with a contact classification.
     */
    public void testSimplePartyWithContactCreation() throws Exception {
        Lookup classification = createLookup("EMAIL");
        service.save(classification);
        Lookup classification1 = createLookup("HOME");
        service.save(classification1);

        Party person = createPerson("MR", "Jim", "Alateras");
        person.addContact(createContact(classification));
        person.addContact(createContact(classification1));
        service.save(person);

        Party person2 = (Party) get(person.getObjectReference());
        assertNotNull(person2);
        assertEquals(2, person.getContacts().size());
    }

    /**
     * Tests party removal.
     */
    public void testRemove() {
        Lookup classification = createLookup("HOME");
        service.save(classification);
        Party person = createPerson("MR", "Jim", "Alateras");
        Contact contact = createContact(classification);
        service.save(contact);
        person.addContact(contact);
        service.save(person);
        assertNotNull(get(person.getObjectReference()));
        assertNotNull(get(contact.getObjectReference()));

        // invalidate the object. Shouldn't prevent its removal
        person.getDetails().put("lastName", null);

        try {
            service.validateObject(person);
            fail("Expected the party to be invalid");
        } catch (ValidationException ignore) {
            // expected behaviour
        }

        // now remove it, and verify the associated contact has also been
        // removed
        service.remove(person);
        assertNull(get(person.getObjectReference()));
        assertNull(get(contact.getObjectReference()));
    }

    /**
     * Verifies that multiple parties can be saved via the
     * {@link IArchetypeService#save(Collection<IMObject>)} method.
     */
    public void testSaveCollection() {
        Lookup classification = createLookup("EMAIL");
        service.save(classification);
        Lookup classification1 = createLookup("HOME");
        service.save(classification1);

        Party person1 = createPerson("MR", "Jim", "Alateras");
        person1.addContact(createContact(classification));
        person1.addContact(createContact(classification1));

        Party person2 = createPerson("MR", "Tim", "Anderson");
        person1.addContact(createContact(classification));
        person1.addContact(createContact(classification1));

        // check the initial values of the ids
        assertEquals(-1, person1.getId());
        assertEquals(-1, person2.getId());

        // save the collection
        Collection<Party> col = Arrays.asList(person1, person2);
        service.save(col);

        // verify the ids have updated
        assertFalse(person1.getId() == -1);
        assertFalse(person2.getId() == -1);
        assertEquals(0, person1.getVersion());
        assertEquals(0, person2.getVersion());

        // verify the versions don't update until a change is made
        service.save(col);
        assertEquals(0, person1.getVersion());
        assertEquals(0, person2.getVersion());

        // update person1 and recheck versions after save
        person1.getDetails().put("lastName", "Foo");
        service.save(col);
        assertEquals(1, person1.getVersion());
        assertEquals(0, person2.getVersion());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    @Override
    protected String[] getConfigLocations() {
        return new String[]{
                "org/openvpms/component/business/service/archetype/archetype-service-appcontext.xml"
        };
    }

    /* (non-Javadoc)
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        service = (ArchetypeService) applicationContext.getBean(
                "archetypeService");
    }

    /**
     * Create a person with the specified title, firstName and lastName.
     *
     * @param title     the title
     * @param firstName the first name
     * @param lastName  the last name
     * @return a new person
     */
    private Party createPerson(String title, String firstName,
                               String lastName) {
        Party person = (Party) service.create("party.person");
        person.getDetails().put("lastName", lastName);
        person.getDetails().put("firstName", firstName);
        person.getDetails().put("title", title);

        return person;
    }

    /**
     * Create a contact with the specified classification.
     *
     * @param classification the classification
     * @return a new contact
     */
    private Contact createContact(Lookup classification) {
        Contact contact = (Contact) service.create("contact.location");

        contact.getDetails().put("address", "kalulu rd");
        contact.getDetails().put("suburb", "Belgrave");
        contact.getDetails().put("postcode", "3160");
        contact.getDetails().put("state", "VIC");
        contact.getDetails().put("country", "AU");
        contact.addClassification(classification);

        return contact;
    }

    /**
     * Creates a lookup with the specified code.
     *
     * @param code the code of the lookup
     * @return a new lookup
     */
    private Lookup createLookup(String code) {
        return LookupUtil.createLookup("lookup.contactPurpose", code);
    }

    /**
     * Helper to get an object from the archetype service given its reference.
     *
     * @param ref the object reference
     * @return the object or <tt>null</tt> if its not found
     */
    private IMObject get(IMObjectReference ref) {
        return service.get(ref);
    }
}
