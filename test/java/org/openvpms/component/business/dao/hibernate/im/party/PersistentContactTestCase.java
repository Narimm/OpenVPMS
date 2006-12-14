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


// hibernate
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.openvpms.component.business.dao.hibernate.im.HibernateInfoModelTestCase;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.Classification;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;

import java.util.HashSet;
import java.util.Set;

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
    }

    /**
     * Test the creation of a simple contact
     */
    public void testCreateSimpleContact() throws Exception {

        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial numbr of entries in address tabel
            int ccount = HibernatePartyUtil.getTableRowCount(session, "contact");
            // execute the test
            tx = session.beginTransaction();
            Classification purpose = createClassification("purpose");
            session.save(purpose);
            Contact contact = createContact();
            Party person = createPerson();
            person.addContact(contact);
            contact.addClassification(purpose);
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
            int bcount = HibernatePartyUtil.getTableRowCount(session, "classification");
            // execute the test
            tx = session.beginTransaction();

            Party person = createPerson();
            Contact contact = createContact();
            person.addContact(contact);
            session.save(person);
            tx.commit();

            // Retrieve the person and add a contact purpose to the contact
            person = (Party)session.get(Party.class, person.getUid());

            tx = session.beginTransaction();
            Classification purpose = createClassification("now");
            session.save(purpose);
            contact = person.getContacts().iterator().next();
            contact.addClassification(purpose);
            session.save(person);
            tx.commit();

            // Retrieve the contact check that  there is one contact purpose
            contact = (Contact)session.get(Contact.class, contact.getUid());
            assertTrue(contact.getClassifications().size() == 1);
            int acount1 = HibernatePartyUtil.getTableRowCount(session, "contact");
            int bcount1 = HibernatePartyUtil.getTableRowCount(session, "classification");
            assertTrue(acount1 == acount + 1);
            assertTrue(bcount1 == bcount + 1);

            // add another contract purpose
            tx = session.beginTransaction();
            Classification purpose1 = createClassification("now1");
            session.save(purpose1);
            contact.addClassification(purpose1);
            session.save(contact);
            tx.commit();

            // check that there is only one contact added to the database
            acount1 = HibernatePartyUtil.getTableRowCount(session, "contact");
            bcount1 = HibernatePartyUtil.getTableRowCount(session, "classification");
            assertTrue(acount1 == acount + 1);
            assertTrue(bcount1 == bcount + 2);

            // retrieve the contact and make sure there are 2 purposes
            contact = (Contact)session.get(Contact.class, contact.getUid());
            assertTrue(contact.getClassifications().size() == 2);

            // retrieve the person and ensure that there is only one address
            person = (Party)session.get(Party.class, person.getUid());
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
            int bcount = HibernatePartyUtil.getTableRowCount(session, "classification");

            tx = session.beginTransaction();

            Classification purpose = createClassification("now");
            session.save(purpose);
            Party person = createPerson();
            Contact contact = createContact();
            contact.addClassification(purpose );
            person.addContact(contact);
            session.save(person);
            tx.commit();

            // check the row counts
            int acount1 = HibernatePartyUtil.getTableRowCount(session, "contact");
            int bcount1 = HibernatePartyUtil.getTableRowCount(session, "classification");
            assertTrue(acount1 == acount + 1);
            assertTrue(bcount1 == bcount + 1);

            // retrieve the person and delete a contact purpose from the contact
            tx = session.beginTransaction();
            person = (Party)session.get(Party.class, person.getUid());
            assertTrue(person != null);
            assertTrue(person.getContacts().size() == 1);
            assertTrue(person.getContacts().iterator().next().getClassifications().size() == 1);

            contact = person.getContacts().iterator().next();
            contact.getClassifications().clear();
            session.save(person);

            // retrieve the contact and check the contact purposes
            contact = (Contact)session.get(Contact.class, contact.getUid());
            assertTrue(contact.getClassifications().size() == 0);

            // check the row counts
            acount1 = HibernatePartyUtil.getTableRowCount(session, "contact");
            bcount1 = HibernatePartyUtil.getTableRowCount(session, "classification");
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
     * Test the update of an address for a contact
     */
    public void testContactPurposeUpdateForContact()
    throws Exception {

        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial contact count
            int acount = HibernatePartyUtil.getTableRowCount(session, "contact");
            int bcount = HibernatePartyUtil.getTableRowCount(session, "classification");

            tx = session.beginTransaction();

            Classification purpose = createClassification("now");
            session.save(purpose);
            Party person = createPerson();
            Contact contact = createContact();
            contact.addClassification(purpose);
            person.addContact(contact);
            session.save(person);
            tx.commit();

            // check row counts
            int acount1 = HibernatePartyUtil.getTableRowCount(session, "contact");
            int bcount1 = HibernatePartyUtil.getTableRowCount(session, "classification");
            assertTrue(acount1 == acount + 1);
            assertTrue(bcount1 == bcount + 1);

            // retrieve the person and delete a contact purpose from the contact
            tx = session.beginTransaction();
            person = (Party)session.get(Party.class, person.getUid());
            assertTrue(person != null);
            assertTrue(person.getContacts().size() == 1);
            assertTrue(person.getContacts().iterator().next().getClassifications().size() == 1);

            contact = person.getContacts().iterator().next();
            contact.getClassifications().iterator().next().setName("later");
            session.save(person);

            // retrieve the contact and check the contact purposes
            contact = (Contact)session.get(Contact.class, contact.getUid());
            assertTrue(contact.getClassifications().size() == 1);
            assertTrue(contact.getClassifications().iterator().next().getName().equals("later"));

            // check row counts
            acount1 = HibernatePartyUtil.getTableRowCount(session, "contact");
            bcount1 = HibernatePartyUtil.getTableRowCount(session, "classification");
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
     * Tests the {@link Party#setContacts} method.
     */
    public void testSetContacts() throws Exception {
        Session session = currentSession();

        Transaction txn = session.beginTransaction();
        Party person = createPerson();
        Set<Contact> contacts = new HashSet<Contact>();
        contacts.add(createContact());
        contacts.add(createContact());
        person.setContacts(contacts);
        session.save(person);
        txn.commit();
        session.evict(person);
        person = (Party) session.get(Party.class, person.getUid());
        assertNotNull(person);
        assertEquals(2, person.getContacts().size());
        closeSession();
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
            int bcount = HibernatePartyUtil.getTableRowCount(session, "classification");

            tx = session.beginTransaction();

            Classification purpose = createClassification("now");
            session.save(purpose);
            Classification purpose1 = createClassification("later");
            session.save(purpose1);
            Party person = createPerson();
            Contact contact = createContact();
            contact.addClassification(purpose);
            contact.addClassification(purpose1);
            person.addContact(contact);
            session.saveOrUpdate(person);
            tx.commit();

            // check row counts
            int acount1 = HibernatePartyUtil.getTableRowCount(session, "contact");
            int bcount1 = HibernatePartyUtil.getTableRowCount(session, "classification");
            assertTrue(acount1 == acount + 1);
            assertTrue(bcount1 == bcount + 2);

            // retrieve the person and delete all contacts
            tx = session.beginTransaction();
            person = (Party)session.get(Party.class, person.getUid());
            assertTrue(person != null);
            assertTrue(person.getContacts().size() == 1);
            assertTrue(person.getContacts().iterator().next().getClassifications().size() == 2);

            person.getContacts().clear();
            session.saveOrUpdate(person);

            // retrieve and check the contacts
            person = (Party)session.get(Party.class, person.getUid());
            assertTrue(person != null);
            assertTrue(person.getContacts().size() == 0);

            // check row counts
            acount1 = HibernatePartyUtil.getTableRowCount(session, "contact");
            bcount1 = HibernatePartyUtil.getTableRowCount(session, "classification");
            assertTrue(acount1 == acount);
            assertTrue(bcount1 == bcount + 2);
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
     * Create a simple classification with the specified name
     *
     * @param name
     *            the name of the classification
     * @return Classification
     */
    private Classification createClassification(String name) throws Exception {
        Classification clas = new Classification();
        clas.setArchetypeIdAsString("openvpms-party-classification.current.1.0");
        clas.setName(name);

        return clas;
    }

    /**
     * Create a simple person
     *
     * @return person
     */
    private Party createPerson() throws Exception {
        return new Party(createPersonArchetypeId(), "person", "person", null, null);
    }

    /**
     * Return a person archetype id
     * @return a new person archetype id
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
