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

// java core
import java.util.Date;

// hibernate
import org.hibernate.Session;
import org.hibernate.Transaction;

// openvpms-framework
import org.openvpms.component.business.dao.hibernate.im.HibernateInfoModelTestCase;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.ContactPurpose;
import org.openvpms.component.business.domain.im.party.Person;

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
            int ccount = HibernatePartyUtil.getTableRowCount(session, "contact");
            // execute the test
            tx = session.beginTransaction();
            Person person = createPerson();
            Contact contact = createContact();
            person.addContact(contact);
            contact.addContactPurpose(createContactPurpose("purpose"));
            session.save(person);
            tx.commit();
            
            // ensure that the correct rows have been inserted
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
     * Test the addition of a contact purpose for a contact
     */
    public void testContactPurposeAdditionForContact() throws Exception {

        Session session = currentSession();
        Transaction tx = null;
        
        try {
            // get initial contact count
            int acount = HibernatePartyUtil.getTableRowCount(session, "contact");
            int bcount = HibernatePartyUtil.getTableRowCount(session, "contactPurpose");
            // execute the test
            tx = session.beginTransaction();
            
            Person person = createPerson();
            Contact contact = createContact();
            person.addContact(contact);
            session.save(person);
            tx.commit();
            
            // Retrieve the person and add a contact purpose to the contact
            person = (Person)session.get(Person.class, person.getUid());

            tx = session.beginTransaction();
            ContactPurpose purpose = createContactPurpose("now");
            contact = person.getContacts().iterator().next();
            contact.addContactPurpose(purpose);
            session.save(person);
            tx.commit();
            
            // Retrieve the contact check that  there is one contact purpose
            contact = (Contact)session.get(Contact.class, contact.getUid());
            assertTrue(contact.getContactPurposes().size() == 1);
            int acount1 = HibernatePartyUtil.getTableRowCount(session, "contact");
            int bcount1 = HibernatePartyUtil.getTableRowCount(session, "contactPurpose");
            assertTrue(acount1 == acount + 1);
            assertTrue(bcount1 == bcount + 1);
            
            // add another contract purpose
            tx = session.beginTransaction();
            contact.addContactPurpose(createContactPurpose("now1"));
            session.save(contact);
            tx.commit();
            
            // check that there is only one contact added to the database 
            acount1 = HibernatePartyUtil.getTableRowCount(session, "contact");
            bcount1 = HibernatePartyUtil.getTableRowCount(session, "contactPurpose");
            assertTrue(acount1 == acount + 1);
            assertTrue(bcount1 == bcount + 2);
            
            // retrieve the contact and make sure there are 2 purposes
            contact = (Contact)session.get(Contact.class, contact.getUid());
            assertTrue(contact.getContactPurposes().size() == 2);
            
            // retrieve the person and ensure that there is only one address
            person = (Person)session.get(Person.class, person.getUid());
            assertTrue(person.getContacts().size() == 1);
            
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
    public void testContactPurposeDeletionForContact()
    throws Exception {

        Session session = currentSession();
        Transaction tx = null;
        
        try {
            // get initial contact count
            int acount = HibernatePartyUtil.getTableRowCount(session, "contact");
            int bcount = HibernatePartyUtil.getTableRowCount(session, "contactPurpose");
            
            tx = session.beginTransaction();
            
            Person person = createPerson();
            Contact contact = createContact();
            contact.addContactPurpose(createContactPurpose("now"));
            person.addContact(contact);
            session.save(person);
            tx.commit();
            
            // check the row counts
            int acount1 = HibernatePartyUtil.getTableRowCount(session, "contact");
            int bcount1 = HibernatePartyUtil.getTableRowCount(session, "contactPurpose");
            assertTrue(acount1 == acount + 1);
            assertTrue(bcount1 == bcount + 1);
            
            // retrieve the person and delete a contact purpose from the contact
            tx = session.beginTransaction();
            person = (Person)session.get(Person.class, person.getUid());
            assertTrue(person != null);
            assertTrue(person.getContacts().size() == 1);
            assertTrue(person.getContacts().iterator().next().getContactPurposes().size() == 1);
            
            contact = person.getContacts().iterator().next();
            contact.getContactPurposes().clear();
            session.save(person);
            
            // retrieve the contact and check the contact purposes
            contact = (Contact)session.get(Contact.class, contact.getUid());
            assertTrue(contact.getContactPurposes().size() == 0);
            
            // check the row counts
            acount1 = HibernatePartyUtil.getTableRowCount(session, "contact");
            bcount1 = HibernatePartyUtil.getTableRowCount(session, "contactPurpose");
            assertTrue(acount1 == acount + 1);
            assertTrue(bcount1 == bcount);
            
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
    public void testContactPurposeUpdateForContact()
    throws Exception {

        Session session = currentSession();
        Transaction tx = null;
        
        try {
            // get initial contact count
            int acount = HibernatePartyUtil.getTableRowCount(session, "contact");
            int bcount = HibernatePartyUtil.getTableRowCount(session, "contactPurpose");
            
            tx = session.beginTransaction();
            
            Person person = createPerson();
            Contact contact = createContact();
            contact.addContactPurpose(createContactPurpose("now"));
            person.addContact(contact);
            session.save(person);
            tx.commit();
            
            // check row counts
            int acount1 = HibernatePartyUtil.getTableRowCount(session, "contact");
            int bcount1 = HibernatePartyUtil.getTableRowCount(session, "contactPurpose");
            assertTrue(acount1 == acount + 1);
            assertTrue(bcount1 == bcount + 1);
            
            // retrieve the person and delete a contact purpose from the contact
            tx = session.beginTransaction();
            person = (Person)session.get(Person.class, person.getUid());
            assertTrue(person != null);
            assertTrue(person.getContacts().size() == 1);
            assertTrue(person.getContacts().iterator().next().getContactPurposes().size() == 1);
            
            contact = person.getContacts().iterator().next();
            contact.getContactPurposes().iterator().next().setName("later");
            session.save(person);
            
            // retrieve the contact and check the contact purposes
            contact = (Contact)session.get(Contact.class, contact.getUid());
            assertTrue(contact.getContactPurposes().size() == 1);
            assertTrue(contact.getContactPurposes().iterator().next().getName().equals("later"));
            
            // check row counts
            acount1 = HibernatePartyUtil.getTableRowCount(session, "contact");
            bcount1 = HibernatePartyUtil.getTableRowCount(session, "contactPurpose");
            assertTrue(acount1 == acount + 1);
            assertTrue(bcount1 == bcount + 1);
            
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
     * Test deletion of Contact and associated ContactPurposes
     */
    public void testContactDeletion()
    throws Exception {
        Session session = currentSession();
        Transaction tx = null;
        
        try {
            // get initial contact count
            int acount = HibernatePartyUtil.getTableRowCount(session, "contact");
            int bcount = HibernatePartyUtil.getTableRowCount(session, "contactPurpose");
            
            tx = session.beginTransaction();
            
            Person person = createPerson();
            Contact contact = createContact();
            contact.addContactPurpose(createContactPurpose("now"));
            contact.addContactPurpose(createContactPurpose("later"));
            person.addContact(contact);
            session.save(person);
            tx.commit();
            
            // check row counts
            int acount1 = HibernatePartyUtil.getTableRowCount(session, "contact");
            int bcount1 = HibernatePartyUtil.getTableRowCount(session, "contactPurpose");
            assertTrue(acount1 == acount + 1);
            assertTrue(bcount1 == bcount + 2);
            
            // retrieve the person and delete all contacts 
            tx = session.beginTransaction();
            person = (Person)session.get(Person.class, person.getUid());
            assertTrue(person != null);
            assertTrue(person.getContacts().size() == 1);
            assertTrue(person.getContacts().iterator().next().getContactPurposes().size() == 2);
            
            person.getContacts().clear();
            session.save(person);
            
            // retrieve and check the contacts
            person = (Person)session.get(Person.class, person.getUid());
            assertTrue(person != null);
            assertTrue(person.getContacts().size() == 0);

            // check row counts
            acount1 = HibernatePartyUtil.getTableRowCount(session, "contact");
            bcount1 = HibernatePartyUtil.getTableRowCount(session, "contactPurpose");
            assertTrue(acount1 == acount);
            assertTrue(bcount1 == bcount);
        } catch (Exception exception) { 
            if (tx != null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            closeSession();
        }
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
     * Create a simple contact
     * 
     * @return Contact
     */
    private Contact createContact() throws Exception {
        Contact contact = new Contact();
        contact.setArchetypeId(createContactArchetypeId());
        contact.setDetails(createSimpleAttributeMap());
        
        return contact;
    }
    
    /**
     * Create a simple contact purpose with the specified name
     * 
     * @param purpose
     *            the name of the purpose
     * @return ContactPurpose
     */
    private ContactPurpose createContactPurpose(String purpose) throws Exception {
        ContactPurpose cpur = new ContactPurpose();
        cpur.setArchetypeIdAsString("penvpms-party-contactPurpose.current.1.0");
        cpur.setFromDate(new Date());
        cpur.setThruDate(new Date());
        cpur.setName(purpose);
        
        return cpur;
    }
    
    /**
     * Create a simple person
     * 
     * @rturn person
     */
    private Person createPerson() throws Exception {
        return new Person(createPersonArchetypeId(), "person", "Mr", "Jim", 
                "Alateras", null);
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
    
}
