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

// java core
import java.util.Date;

//spring-context
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

// openvpms-framework
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Address;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Person;
import org.openvpms.component.business.service.archetype.ArchetypeService;

/**
 * Test the entity service
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PersonContactTestCase extends
        AbstractDependencyInjectionSpringContextTests {
    
    /**
     * Holds a reference to the entity service
     */
    private EntityService entityService;
    
    /**
     * Holds a reference to the archetype service
     */
    private ArchetypeService archetypeService;
    

    public static void main(String[] args) {
        junit.textui.TestRunner.run(PersonContactTestCase.class);
    }

    /**
     * Default constructor
     */
    public PersonContactTestCase() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    @Override
    protected String[] getConfigLocations() {
        return new String[] { 
                "org/openvpms/component/business/service/entity/entity-service-appcontext.xml" 
                };
    }

    /**
     * Test the creation of a person with contacts and addresses as 
     * specified in the archetype
     */
    public void testValidPersonContactAddressCreation() 
    throws Exception {
        Person person = createPerson("Mr", "John", "Dillon");
        Contact contact = createContact("contact.home");
        Address address = createLocationAddress();
        
        contact.addAddress(address);
        person.addContact(contact);
        entityService.save(person);
        
        person = (Person)entityService.getById(person.getUid());
        assertTrue(person != null);
        assertTrue(person.getContacts().size() == 1);
        assertTrue(((Contact)person.getContacts().iterator().next()).getAddresses().size() == 1);
    }
    
    /**
     * Test that the many-to-many relationship between contact and 
     * address works.
     */
    public void testContactAddressRelationship()
    throws Exception {
        Person person1 = createPerson("Mr", "John", "Dimantaris");
        Person person2 = createPerson("Ms", "Jenny", "Love");
        
        Contact contact1 = createContact("contact.home");
        Contact contact2 = createContact("contact.home");
        
        Address address = createLocationAddress();
        
        contact1.addAddress(address);
        person1.addContact(contact1);
        entityService.save(person1);

        contact2.addAddress(address);
        person2.addContact(contact2);
        entityService.save(person2);
        
        // save the entities
        
        // now attempt to retrieve the entities
        person1 = (Person)entityService.getById(person1.getUid());
        assertTrue(person1 != null);
        assertTrue(person1.getContacts().size() == 1);
        assertTrue(((Contact)person1.getContacts().iterator().next()).getAddresses().size() == 1);

        person2 = (Person)entityService.getById(person2.getUid());
        assertTrue(person2 != null);
        assertTrue(person2.getContacts().size() == 1);
        assertTrue(((Contact)person2.getContacts().iterator().next()).getAddresses().size() == 1);
        
        // now delete the address from person1 and update it.
        ((Contact)person1.getContacts().iterator().next()).getAddresses().clear();
        assertTrue(((Contact)person1.getContacts().iterator().next()).getAddresses().size() == 0);
        entityService.save(person1);
        
        // retrieve the entities again and check that the addresses are
        // still valid
        person1 = (Person)entityService.getById(person1.getUid());
        assertTrue(person1 != null);
        assertTrue(person1.getContacts().size() == 1);
        assertTrue(((Contact)person1.getContacts().iterator().next()).getAddresses().size() == 0);
        
        person2 = (Person)entityService.getById(person2.getUid());
        assertTrue(person2 != null);
        assertTrue(person2.getContacts().size() == 1);
        assertTrue(((Contact)person2.getContacts().iterator().next()).getAddresses().size() == 1);
    }
    
    /**
     * Test the addition and removal of contacts
     */
    public void testContactLifecycle() 
    throws Exception {
        Person person = createPerson("Mr", "Jim", "Alateras");
        person.addContact(createContact("contact.home"));
        person.addContact(createContact("contact.home"));
        person.addContact(createContact("contact.home"));

        entityService.save(person);
        
        // retrieve and remove the first contact and update
        person = (Person)entityService.getById(person.getUid());
        assertTrue(person.getContacts().size() == 3);
        Contact contact = person.getContacts().iterator().next();
        person.getContacts().remove(contact);
        assertTrue(person.getContacts().size() == 2);
        entityService.save(person);
        
        // retrieve and ensure thagt there are only 2 contacts
        person = (Person)entityService.getById(person.getUid());
        assertTrue(person.getContacts().size() == 2);
    }
    
    /**
     * Test the addtion and remove of address to contact
     */
    public void testAddressLifecycle()
    throws Exception {
        Person person = createPerson("Ms", "Bernadette", "Feeney");
        Contact contact = createContact("contact.home");
        Address address = createLocationAddress();
        
        contact.addAddress(address);
        person.addContact(contact);
        entityService.save(person);

        // retrieve and add another address to the contact
        person = (Person)entityService.getById(person.getUid());
        contact = person.getContacts().iterator().next();
        assertTrue(contact.getAddresses().size() == 1);
        
        address = createLocationAddress();
        contact.addAddress(address);
        entityService.save(person);
        
        // retrieve and remove the first address
        person = (Person)entityService.getById(person.getUid());
        contact = person.getContacts().iterator().next();
        assertTrue(contact.getAddresses().size() == 2);
        contact.removeAddress(contact.getAddresses().iterator().next());
        entityService.save(person);
        
        // retrieve and check the number of addresses
        person = (Person)entityService.getById(person.getUid());
        contact = person.getContacts().iterator().next();
        assertTrue(contact.getAddresses().size() == 1);
    }
    
    
    
    /**
     * Create a valid location address
     * 
     * @return
     */
    private Address createLocationAddress() {
        Address address = (Address)archetypeService.createDefaultObject("address.location");
        assertTrue(address instanceof Address);
        
        address.getDetails().setAttribute("address", "5 Kalulu Rd");
        address.getDetails().setAttribute("suburb", "Belgrave");
        address.getDetails().setAttribute("postcode", "3160");
        address.getDetails().setAttribute("state", "Victoria");
        address.getDetails().setAttribute("country", "Australia");
        return address;
    }

    /**
     * Create a contact of the specified type 
     * 
     * @param shortName
     *            the archetype shortname
     * @return Contact
     */
    private Contact createContact(String shortName) {
        Contact contact = (Contact)archetypeService.createDefaultObject("contact.home");
        assertTrue(contact instanceof Contact);
        
        contact.setActiveStartTime(new Date());
        return contact;
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
        
        Person person = (Person)entity;
        person.setTitle(title);
        person.setFirstName(firstName);
        person.setLastName(lastName);
        
        return person;
    }
    
    /* (non-Javadoc)
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        
        this.entityService = (EntityService)applicationContext.getBean(
            "entityService");
        this.archetypeService = (ArchetypeService)applicationContext.getBean(
            "archetypeService");
    }

}
