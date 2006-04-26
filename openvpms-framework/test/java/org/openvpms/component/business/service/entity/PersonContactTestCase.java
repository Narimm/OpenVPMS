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

//spring-context
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

// openvpms-framework
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeQueryHelper;
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
     * Holds a reference to the archetype service
     */
    private ArchetypeService service;
    

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
    public void testValidPersonContactCreation() 
    throws Exception {
        Party person = createPerson("Mr", "John", "Dillon");
        Contact contact = createLocationContact();
        person.addContact(contact);
        service.save(person);
        
        person = (Party)ArchetypeQueryHelper.getByUid(service,
                person.getArchetypeId(), person.getUid());
        assertTrue(person != null);
        assertTrue(person.getContacts().size() == 1);
    }
    
    /**
     * Test that the many-to-many relationship between contact and 
     * address works.
     */
    public void testContactRelationship()
    throws Exception {
        Party person1 = createPerson("Mr", "John", "Dimantaris");
        Party person2 = createPerson("Ms", "Jenny", "Love");
        
        Contact contact1 = createLocationContact();
        Contact contact2 = createLocationContact();
        person1.addContact(contact1);
        service.save(person1);

        person2.addContact(contact2);
        service.save(person2);
        
        // save the entities
        
        // now attempt to retrieve the entities
        person1 = (Party)ArchetypeQueryHelper.getByUid(service,
                person1.getArchetypeId(), person1.getUid());
        assertTrue(person1 != null);
        assertTrue(person1.getContacts().size() == 1);

        person2 = (Party)ArchetypeQueryHelper.getByUid(service,
                person2.getArchetypeId(), person2.getUid());
        assertTrue(person2 != null);
        assertTrue(person2.getContacts().size() == 1);
        
        // now delete the address from person1 and update it.
        person1.getContacts().clear();
        assertTrue(person1.getContacts().size() == 0);
        service.save(person1);
        
        // retrieve the entities again and check that the addresses are
        // still valid
        person1 = (Party)ArchetypeQueryHelper.getByUid(service,
                person1.getArchetypeId(), person1.getUid());
        assertTrue(person1 != null);
        assertTrue(person1.getContacts().size() == 0);
        
        person2 = (Party)ArchetypeQueryHelper.getByUid(service,
                person2.getArchetypeId(), person2.getUid());
        assertTrue(person2 != null);
        assertTrue(person2.getContacts().size() == 1);
    }
    
    /**
     * Test the addition and removal of contacts
     */
    public void testContactLifecycle() 
    throws Exception {
        Party person = createPerson("Mr", "Jim", "Alateras");
        person.addContact(createLocationContact());
        person.addContact(createLocationContact());
        person.addContact(createLocationContact());

        service.save(person);
        
        // retrieve and remove the first contact and update
        person = (Party)ArchetypeQueryHelper.getByUid(service,
                person.getArchetypeId(), person.getUid());
        assertTrue(person.getContacts().size() == 3);
        Contact contact = person.getContacts().iterator().next();
        person.getContacts().remove(contact);
        assertTrue(person.getContacts().size() == 2);
        service.save(person);
        
        // retrieve and ensure thagt there are only 2 contacts
        person = (Party)ArchetypeQueryHelper.getByUid(service,
                person.getArchetypeId(), person.getUid());
        assertTrue(person.getContacts().size() == 2);
    }
    
    
    
    /**
     * Create a valid location contact
     * 
     * @return
     */
    private Contact createLocationContact() {
        Contact contact = (Contact)service.create("contact.location");
        
        contact.getDetails().setAttribute("address", "5 Kalulu Rd");
        contact.getDetails().setAttribute("suburb", "Belgrave");
        contact.getDetails().setAttribute("postcode", "3160");
        contact.getDetails().setAttribute("state", "Victoria");
        contact.getDetails().setAttribute("country", "Australia");
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
    public Party createPerson(String title, String firstName, String lastName) {
        Party person = (Party)service.create("person.person");
        person.getDetails().setAttribute("lastName", lastName);
        person.getDetails().setAttribute("firstName", firstName);
        person.getDetails().setAttribute("title", title);
        
        return person;
    }
    
    /* (non-Javadoc)
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        
        this.service = (ArchetypeService)applicationContext.getBean("archetypeService");
    }

}
