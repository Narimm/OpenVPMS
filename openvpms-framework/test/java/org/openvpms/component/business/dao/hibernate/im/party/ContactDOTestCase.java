/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.dao.hibernate.im.party;


import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.component.business.dao.hibernate.im.lookup.LookupDO;
import org.openvpms.component.business.dao.hibernate.im.lookup.LookupDOImpl;
import org.openvpms.component.business.domain.im.party.Contact;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * Tests {@link Contact} persistence via hibernate.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ContactDOTestCase extends AbstractPartyDOTest {

    /**
     * The initial no. of contacts in the database.
     */
    private int contacts;

    /**
     * The initial no. of lookups in the database.
     */
    private int lookups;


    /**
     * Test the creation of a simple contact.
     */
    @Test
    public void testCreateSimpleContact() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        LookupDO purpose = createClassification("purpose");
        session.save(purpose);
        ContactDO contact = createContact();
        PartyDO person = createPerson();
        person.addContact(contact);
        contact.addClassification(purpose);
        session.save(person);
        tx.commit();

        // ensure that the correct rows have been inserted
        assertEquals(contacts + 1, count(ContactDOImpl.class));
    }

    /**
     * Test the addition of a contact purpose for a contact.
     */
    @Test
    public void testContactPurposeAdditionForContact() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();

        PartyDO person = createPerson();
        ContactDO contact = createContact();
        person.addContact(contact);
        session.save(person);
        tx.commit();

        // Retrieve the person and add a contact purpose to the contact
        person = (PartyDO) session.get(PartyDOImpl.class, person.getId());

        tx = session.beginTransaction();
        LookupDO purpose = createClassification("now");
        session.save(purpose);
        contact = person.getContacts().iterator().next();
        contact.addClassification(purpose);
        session.save(person);
        tx.commit();

        // Retrieve the contact check that  there is one contact purpose
        contact = (ContactDO) session.get(ContactDOImpl.class, contact.getId());
        assertEquals(1, contact.getClassifications().size());
        assertEquals(contacts + 1, count(ContactDOImpl.class));
        assertEquals(lookups + 1, count(LookupDOImpl.class));

        // add another contract purpose
        tx = session.beginTransaction();
        LookupDO purpose1 = createClassification("now1");
        session.save(purpose1);
        contact.addClassification(purpose1);
        session.save(contact);
        tx.commit();

        // check that there is only one contact added to the database
        assertEquals(contacts + 1, count(ContactDOImpl.class));
        assertEquals(lookups + 2, count(LookupDOImpl.class));

        // retrieve the contact and make sure there are 2 purposes
        contact = (ContactDO) session.get(ContactDOImpl.class, contact.getId());
        assertEquals(2, contact.getClassifications().size());

        // retrieve the person and ensure that there is only one address
        person = (PartyDO) session.get(PartyDOImpl.class, person.getId());
        assertEquals(1, person.getContacts().size());
    }

    /**
     * Test the removal of an address for a contact.
     */
    @Test
    public void testContactPurposeDeletionForContact() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();

        LookupDO purpose = createClassification("now");
        session.save(purpose);
        PartyDO person = createPerson();
        ContactDO contact = createContact();
        contact.addClassification(purpose);
        person.addContact(contact);
        session.save(person);
        tx.commit();

        // check the row counts
        assertEquals(contacts + 1, count(ContactDOImpl.class));
        assertEquals(lookups + 1, count(LookupDOImpl.class));

        // retrieve the person and delete a contact purpose from the contact
        tx = session.beginTransaction();
        person = (PartyDO) session.get(PartyDOImpl.class, person.getId());
        assertNotNull(person);
        assertEquals(1, person.getContacts().size());

        contact = person.getContacts().iterator().next();
        assertEquals(1, contact.getClassifications().size());

        contact.getClassifications().clear();
        session.save(person);

        // retrieve the contact and check the contact purposes
        contact = (ContactDO) session.get(ContactDOImpl.class, contact.getId());
        assertEquals(0, contact.getClassifications().size());
        tx.commit();

        // check the row counts
        assertEquals(contacts + 1, count(ContactDOImpl.class));
        assertEquals(lookups + 1, count(LookupDOImpl.class));
    }

    /**
     * Test the update of an address for a contact.
     */
    @Test
    public void testContactPurposeUpdateForContact() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();

        LookupDO purpose = createClassification("now");
        session.save(purpose);
        PartyDO person = createPerson();
        ContactDO contact = createContact();
        contact.addClassification(purpose);
        person.addContact(contact);
        session.save(person);
        tx.commit();

        // check row counts
        assertEquals(contacts + 1, count(ContactDOImpl.class));
        assertEquals(lookups + 1, count(LookupDOImpl.class));

        // retrieve the person and delete a contact purpose from the contact
        tx = session.beginTransaction();
        person = (PartyDO) session.get(PartyDOImpl.class, person.getId());
        assertNotNull(person);
        assertEquals(1, person.getContacts().size());
        contact = person.getContacts().iterator().next();
        assertEquals(1, contact.getClassifications().size());

        LookupDO classification = contact.getClassifications().iterator().next();
        classification.setName("later");
        session.save(person);
        tx.commit();

        // retrieve the contact and check the contact purposes
        contact = (ContactDO) session.get(ContactDOImpl.class, contact.getId());
        assertEquals(1, contact.getClassifications().size());
        classification = contact.getClassifications().iterator().next();
        assertEquals("later", classification.getName());

        // check row counts
        assertEquals(contacts + 1, count(ContactDOImpl.class));
        assertEquals(lookups + 1, count(LookupDOImpl.class));
    }

    /**
     * Test deletion of Contact and associated classifications and details.
     */
    @Test
    public void testContactDeletion() {
        int details = countDetails(ContactDOImpl.class);

        Session session = getSession();
        Transaction tx = session.beginTransaction();

        LookupDO purpose = createClassification("now");
        session.save(purpose);
        LookupDO purpose1 = createClassification("later");
        session.save(purpose1);
        PartyDO person = createPerson();
        ContactDO contact = createContact();
        contact.addClassification(purpose);
        contact.addClassification(purpose1);
        person.addContact(contact);
        session.saveOrUpdate(person);
        tx.commit();

        // check row counts
        assertEquals(contacts + 1, count(ContactDOImpl.class));
        assertEquals(lookups + 2, count(LookupDOImpl.class));
        assertEquals(details + 2, countDetails(ContactDOImpl.class));

        // retrieve the person and delete all contacts
        tx = session.beginTransaction();
        person = (PartyDO) session.get(PartyDOImpl.class, person.getId());
        assertNotNull(person);
        assertEquals(1, person.getContacts().size());
        contact = person.getContacts().iterator().next();
        assertEquals(2, contact.getClassifications().size());

        person.getContacts().clear();
        session.saveOrUpdate(person);
        tx.commit();

        // retrieve and check the contacts
        person = (PartyDO) session.get(PartyDOImpl.class, person.getId());
        assertNotNull(person);
        assertEquals(0, person.getContacts().size());

        // check row counts
        assertEquals(contacts, count(ContactDOImpl.class));
        assertEquals(lookups + 2, count(LookupDOImpl.class));
        assertEquals(details, countDetails(ContactDOImpl.class));
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        contacts = count(ContactDOImpl.class);
        lookups = count(LookupDOImpl.class);
    }
}
