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

package org.openvpms.component.business.dao.hibernate.im.party;

// hjava core
import java.util.HashSet;
import java.util.List;

// hibernate
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

// openvpms-framework
import org.openvpms.component.business.dao.hibernate.im.HibernateInfoModelTestCase;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.party.Address;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Person;
import org.openvpms.component.business.domain.im.party.Role;

/**
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PersistentContactTestCase extends HibernateInfoModelTestCase {

    /**
     * main line
     * 
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(PersistentContactTestCase.class);
    }

    /**
     * Constructor for PersistentContactTestCase.
     * 
     * @param name
     */
    public PersistentContactTestCase(String name) {
        super(name);
    }

    /*
     * @see BaseTestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        HibernatePartyUtil.deleteAllContacts(currentSession());
    }

    /**
     * Test the creation of a simple contact
     */
    public void testCreateSimpleContact() throws Exception {

        Session session = currentSession();
        Transaction tx = null;
        
        try {
            HibernatePartyUtil.deleteAllContacts(session);
            
            // get initial numbr of entries in address tabel
            int acount = HibernatePartyUtil.getTableRowCount(session, "address");
            int ccount = HibernatePartyUtil.getTableRowCount(session, "contact");
            // execute the test
            tx = session.beginTransaction();
            Person person = createPerson();
            Address address = createAddress();
            
            Contact contact = new Contact(createContactArchetypeId());
            person.addAddress(address);
            person.addContact(contact);
            contact.addAddress(address);
            session.save(person);
            tx.commit();
            
            // ensure that the correct rows have been inserted
            int acount1 = HibernatePartyUtil.getTableRowCount(session, "address");
            assertTrue(acount1 == acount + 1);
            int ccount1 = HibernatePartyUtil.getTableRowCount(session, "contact");
            assertTrue(ccount1 == ccount + 1);
        } catch (Exception exception) { 
            if (tx != null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            closeSession();
        }
    }
    
    /**
     * Test the addition of an address to a contact
     */
    public void testAddressAdditionForContact() throws Exception {

        Session session = currentSession();
        Transaction tx = null;
        
        try {
            // get initial numbr of entries in address tabel
            int acount = HibernatePartyUtil.getTableRowCount(session, "address");
            // execute the test
            tx = session.beginTransaction();
            
            Person person = createPerson();
            Contact contact = new Contact(createContactArchetypeId());
            person.addContact(contact);
            session.save(person);
            tx.commit();
            
            // Retrieve the person and add a contact
            person = (Person)session.get(Person.class, person.getUid());

            tx = session.beginTransaction();
            Address address = createAddress();
            person.addAddress(address);
            session.save(person);
            
            // Retrieve the contact and add the address
            contact = (Contact)session.get(Contact.class, contact.getUid());
            contact.addAddress(address);
            session.save(contact);
            tx.commit();
            
            // ensure that there is still one more address
            int acount1 = HibernatePartyUtil.getTableRowCount(session, "address");
            assertTrue(acount1 == acount + 1);
            
            // retrieve the contact and make sure there is only one address
            contact = (Contact)session.get(Contact.class, contact.getUid());
            assertTrue(contact.getNumOfAddresses() == 1);
            
            // retrieve the person and ensure that there is only one address
            person = (Person)session.get(Person.class, person.getUid());
            assertTrue(person.getAddresses().size() == 1);
            
        } catch (Exception exception) { 
            if (tx != null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            closeSession();
        }
    }
    
    /**
     * Test the removal of an address for a contact
     */
    public void testAddressDeleteForContact()
    throws Exception {

        Session session = currentSession();
        Transaction tx = null;
        
        try {
            // get initial numbr of entries in address tabel
            int acount = HibernatePartyUtil.getTableRowCount(session, "address");
            // execute the test
            tx = session.beginTransaction();
            
            Person person = createPerson();
            Contact contact = new Contact(createContactArchetypeId());
            person.addContact(contact);
            session.save(person);
            tx.commit();
            
            // retrieve the person and add an address
            tx = session.beginTransaction();
            person = (Person)session.get(Person.class, person.getUid());
            assertTrue(person != null);
            
            Address address = createAddress();
            person.addAddress(address);
            contact.addAddress(address);
            session.save(person);
            tx.commit();
            
            // ensure that there is still one more address
            int acount1 = HibernatePartyUtil.getTableRowCount(session, "address");
            assertTrue(acount1 == acount + 1);
            
            // retrieve the contact and check that the address is also
            // retrieved
            contact = (Contact)session.get(Contact.class, contact.getUid());
            assertTrue(contact.getNumOfAddresses() == 1);
            
            // remove the address and save it
            tx = session.beginTransaction();
            contact.removeAddress(address);
            tx.commit();
            
            // check that the address removal worked
            contact = (Contact)session.get(Contact.class, contact.getUid());
            assertTrue(contact.getNumOfAddresses() == 0);
            
            // check that the address still exists on the person
            person = (Person)session.get(Person.class, person.getUid());
            assertTrue(person.getAddresses().size() == 1);
            
            // ensure that the address row count is correct
            acount1 = HibernatePartyUtil.getTableRowCount(session, "address");
            assertTrue(acount1 == acount + 1);
        } catch (Exception exception) { 
            if (tx != null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            closeSession();
        }
        
    }

    /**
     * Test the update of an address for a contact
     */
    public void testAddressUpdateForContact()
    throws Exception {

        Session session = currentSession();
        Transaction tx = null;
        
        try {
            tx = session.beginTransaction();
            Person person = createPerson();
            Contact contact = new Contact(createContactArchetypeId());
            person.addContact(contact);
            session.save(person);
            tx.commit();

            // retrieve the person and add an address
            tx = session.beginTransaction();
            person = (Person)session.get(Person.class, person.getUid());
            assertTrue(person != null);
            
            Address address = createAddress();
            person.addAddress(address);
            contact.addAddress(address);
            session.save(person);
            tx.commit();
            
            // ensure that there is still one more address
            contact = getContactById(session, contact.getUid());
            assertTrue(contact.getNumOfAddresses() == 1);
            
            // retrieve the contact and check that the address is also
            // retrieved
            tx = session.beginTransaction();
            contact = (Contact)session.get(Contact.class, contact.getUid());
            assertTrue(contact.getNumOfAddresses() == 1);
            
            // retrieve the first address
            Address theAddress = contact.getAddressesAsArray()[0];
            assertTrue(theAddress != null);
            
            // add another element to the addresss
            address.getDetails().setAttribute("mobile", "04222368612");
            
            // remove the address and save it
            contact.addAddress(address);
            session.update(contact);
            tx.commit();
            
            // check that the address removal worked
            contact = (Contact)session.get(Contact.class, contact.getUid());
            assertTrue(contact.getNumOfAddresses() == 1);
            
        } catch (Exception exception) { 
            if (tx != null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            closeSession();
        }
    }
    
    /**
     * Test case for OVPMS37
     */
    public void testOVPMS37()
    throws Exception {
        Contact contact = new Contact(createContactArchetypeId());
        contact.addAddress(createAddress()); 

        Address address = contact.getAddressesAsArray()[0];
        assertTrue(address != null);
        
        // add another element to the addresss
        address.getDetails().setAttribute("mobile", "04222368612");
        
        // adding the address to the contact should be cool since it
        // should replace the older object
        contact.addAddress(address);
        assertTrue(contact.getNumOfAddresses() == 1);
    }

    /**
     * Retrieve the contact with the specified id
     * 
     * @param session
     *            the session to use
     * @param id
     *          the identity of the contact
     * @return Contact
     */
    private Contact getContactById(Session session, long id) {
        Contact result = null;
        try {
            Query query = session.getNamedQuery("contact.getContactById");
            query.setLong("id", id);
            List rs = query.list();
            
            if (rs.size() != 1) {
                this.error("The query to contact.getContactById returned more than 1 record.");
            } else {
                result = (Contact)rs.get(0);
            }
        } catch (Exception exception) {
            this.error("Failed in getContactById", exception);
        }
        
        return result;
    }

    /*
     * @see BaseTestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        currentSession().flush();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.system.common.test.BaseTestCase#setUpTestData()
     */
    @Override
    protected void setUpTestData() throws Exception {
        // no test data
    }

    /**
     * Create a simple address
     * 
     * @return Address
     */
    private Address createAddress() throws Exception {
        return new Address(createAddressArchetypeId(), createSimpleAttributeMap());
    }
    
    /**
     * Create a simple person
     * 
     * @rturn person
     */
    private Person createPerson() throws Exception {
        return new Person(createPersonArchetypeId(), "person", "Mr", "Jim", 
                "Alateras", null, new HashSet<Contact>(), new HashSet<Role>(), null);
    }
    
    /**
     * Return a person archetype id
     * @return
     */
    private ArchetypeId createPersonArchetypeId() {
        return new ArchetypeId("openvpms-party-person.person.1.0");
    }

    /**
     * Return a contact archetype Id
     * 
     * @return ArchetypeId
     */
    private ArchetypeId createContactArchetypeId() {
        return new ArchetypeId("openvpms-party-contact.contact.1.0");
    }
    
    /**
     * Return an address archetype Id
     * 
     * @return ArchetypeId
     */
    private ArchetypeId createAddressArchetypeId() {
        return new ArchetypeId("openvpms-party-address.address.1.0");
    }
}
