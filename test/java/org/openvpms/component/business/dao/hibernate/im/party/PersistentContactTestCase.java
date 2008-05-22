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


import org.hibernate.Session;
import org.hibernate.Transaction;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;

import java.util.HashSet;
import java.util.Set;


/**
 * Tests {@link Contact} persistence via hibernate.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PersistentContactTestCase extends AbstractPersistentPartyTest {

    /**
     * Test the creation of a simple contact.
     */
    public void testCreateSimpleContact() throws Exception {
        Session session = getSession();

        // count the initial contacts
        int count = count(Contact.class);

        // execute the test
        Transaction tx = session.beginTransaction();
        Lookup purpose = createClassification("purpose");
        session.save(purpose);
        Contact contact = createContact();
        Party person = createPerson();
        person.addContact(contact);
        contact.addClassification(purpose);
        session.save(person);
        tx.commit();

        // ensure that the correct rows have been inserted
        assertEquals(count + 1, count(Contact.class));
    }

    /**
     * Test the addition of a contact purpose for a contact.
     */
    public void testContactPurposeAdditionForContact() {
        Session session = getSession();

        // get initial contact count
        int contacts = count(Contact.class);
        int lookups = count(Lookup.class);

        Transaction tx = session.beginTransaction();

        Party person = createPerson();
        Contact contact = createContact();
        person.addContact(contact);
        session.save(person);
        tx.commit();

        // Retrieve the person and add a contact purpose to the contact
        person = (Party) session.get(Party.class, person.getUid());

        tx = session.beginTransaction();
        Lookup purpose = createClassification("now");
        session.save(purpose);
        contact = person.getContacts().iterator().next();
        contact.addClassification(purpose);
        session.save(person);
        tx.commit();

        // Retrieve the contact check that  there is one contact purpose
        contact = (Contact) session.get(Contact.class, contact.getUid());
        assertEquals(1, contact.getClassifications().size());
        assertEquals(contacts + 1, count(Contact.class));
        assertEquals(lookups + 1, count(Lookup.class));

        // add another contract purpose
        tx = session.beginTransaction();
        Lookup purpose1 = createClassification("now1");
        session.save(purpose1);
        contact.addClassification(purpose1);
        session.save(contact);
        tx.commit();

        // check that there is only one contact added to the database
        assertEquals(contacts + 1, count(Contact.class));
        assertEquals(lookups + 2, count(Lookup.class));

        // retrieve the contact and make sure there are 2 purposes
        contact = (Contact) session.get(Contact.class, contact.getUid());
        assertEquals(2, contact.getClassifications().size());

        // retrieve the person and ensure that there is only one address
        person = (Party) session.get(Party.class, person.getUid());
        assertEquals(1, person.getContacts().size());
    }

    /**
     * Test the removal of an address for a contact.
     */
    public void testContactPurposeDeletionForContact() {
        Session session = getSession();

        // get initial contact count
        int contacts = count(Contact.class);
        int lookups = count(Lookup.class);

        Transaction tx = session.beginTransaction();

        Lookup purpose = createClassification("now");
        session.save(purpose);
        Party person = createPerson();
        Contact contact = createContact();
        contact.addClassification(purpose);
        person.addContact(contact);
        session.save(person);
        tx.commit();

        // check the row counts
        assertEquals(contacts + 1, count(Contact.class));
        assertEquals(lookups + 1, count(Lookup.class));

        // retrieve the person and delete a contact purpose from the contact
        tx = session.beginTransaction();
        person = (Party) session.get(Party.class, person.getUid());
        assertNotNull(person);
        assertEquals(1, person.getContacts().size());

        contact = person.getContacts().iterator().next();
        assertEquals(1, contact.getClassifications().size());

        contact.getClassifications().clear();
        session.save(person);

        // retrieve the contact and check the contact purposes
        contact = (Contact) session.get(Contact.class, contact.getUid());
        assertEquals(0, contact.getClassifications().size());
        tx.commit();

        // check the row counts
        assertEquals(contacts + 1, count(Contact.class));
        assertEquals(lookups + 1, count(Lookup.class));
    }

    /**
     * Test the update of an address for a contact.
     */
    public void testContactPurposeUpdateForContact() {
        Session session = getSession();

        // get initial contact count
        int contacts = count(Contact.class);
        int lookups = count(Lookup.class);

        Transaction tx = session.beginTransaction();

        Lookup purpose = createClassification("now");
        session.save(purpose);
        Party person = createPerson();
        Contact contact = createContact();
        contact.addClassification(purpose);
        person.addContact(contact);
        session.save(person);
        tx.commit();

        // check row counts
        assertEquals(contacts + 1, count(Contact.class));
        assertEquals(lookups + 1, count(Lookup.class));

        // retrieve the person and delete a contact purpose from the contact
        tx = session.beginTransaction();
        person = (Party) session.get(Party.class, person.getUid());
        assertNotNull(person);
        assertEquals(1, person.getContacts().size());
        contact = person.getContacts().iterator().next();
        assertEquals(1, contact.getClassifications().size());

        Lookup classification = contact.getClassifications().iterator().next();
        classification.setName("later");
        session.save(person);
        tx.commit();

        // retrieve the contact and check the contact purposes
        contact = (Contact) session.get(Contact.class, contact.getUid());
        assertEquals(1, contact.getClassifications().size());
        classification = contact.getClassifications().iterator().next();
        assertEquals("later", classification.getName());

        // check row counts
        assertEquals(contacts + 1, count(Contact.class));
        assertEquals(lookups + 1, count(Lookup.class));
    }

    /**
     * Tests the {@link Party#setContacts} method.
     */
    public void testSetContacts() {
        Session session = getSession();

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
     * Test deletion of Contact and associated classifications and details.
     */
    public void testContactDeletion() {
        Session session = getSession();

        // get initial counts
        int contacts = count(Contact.class);
        int lookups = count(Lookup.class);
        int details = countDetails(Contact.class);

        Transaction tx = session.beginTransaction();

        Lookup purpose = createClassification("now");
        session.save(purpose);
        Lookup purpose1 = createClassification("later");
        session.save(purpose1);
        Party person = createPerson();
        Contact contact = createContact();
        contact.addClassification(purpose);
        contact.addClassification(purpose1);
        person.addContact(contact);
        session.saveOrUpdate(person);
        tx.commit();

        // check row counts
        assertEquals(contacts + 1, count(Contact.class));
        assertEquals(lookups + 2, count(Lookup.class));
        assertEquals(details + 2, countDetails(Contact.class));

        // retrieve the person and delete all contacts
        tx = session.beginTransaction();
        person = (Party) session.get(Party.class, person.getUid());
        assertNotNull(person);
        assertEquals(1, person.getContacts().size());
        contact = person.getContacts().iterator().next();
        assertEquals(2, contact.getClassifications().size());

        person.getContacts().clear();
        session.saveOrUpdate(person);
        tx.commit();

        // retrieve and check the contacts
        person = (Party) session.get(Party.class, person.getUid());
        assertNotNull(person);
        assertEquals(0, person.getContacts().size());

        // check row counts
        assertEquals(contacts, count(Contact.class));
        assertEquals(lookups + 2, count(Lookup.class));
        assertEquals(details, countDetails(Contact.class));
    }

}
