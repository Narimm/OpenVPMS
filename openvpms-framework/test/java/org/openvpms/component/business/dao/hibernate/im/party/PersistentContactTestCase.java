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
            // execute the test
            tx = session.beginTransaction();
            Address address = createAddress();
            
            Contact contact = new Contact(createContactArchetypeId());
            session.save(contact);
            
            // add the address
            session.save(address);
            contact.addAddress(address);
            tx.commit();
            
            // ensure that there is still one more address
            int acount1 = HibernatePartyUtil.getTableRowCount(session, "address");
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
            
            Contact contact = new Contact(createContactArchetypeId());
            session.save(contact);
            tx.commit();
            
            // Retrieve the contact for a specified id
            Contact rcontact = (Contact)session.get(Contact.class, contact.getUid());

            tx = session.beginTransaction();
            Address address = createAddress();
            session.save(address);
            rcontact.addAddress(address);
            tx.commit();
            
            // ensure that there is still one more address
            int acount1 = HibernatePartyUtil.getTableRowCount(session, "address");
            assertTrue(acount1 == acount + 1);
            
            // retrieve the contact and make sure there is only one address
            contact = getContactById(session, contact.getUid());
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
            
            Contact contact = new Contact(createContactArchetypeId());
            assertTrue(contact != null);
            
            session.save(contact);
            tx.commit();
            
            // retrieve the contact again and make sure the address count
            // is null
            tx = session.beginTransaction();
            contact = getContactById(session, contact.getUid());
            assertTrue(contact.getNumOfAddresses() == 0);
            

            Address address = createAddress();
            session.save(address);
            contact.addAddress(address);  
            tx.commit();
            
            // ensure that there is still one more address
            int acount1 = HibernatePartyUtil.getTableRowCount(session, "address");
            assertTrue(acount1 == acount + 1);
            
            // retrieve the contact and check that the address is als
            // retrieved
            contact = getContactById(session, contact.getUid());
            assertTrue(contact.getNumOfAddresses() == 1);
            
            // remove the address and save it
            tx = session.beginTransaction();
            contact.removeAddress(address);
            tx.commit();
            
            // check that the address removal worked
            contact = getContactById(session, contact.getUid());
            assertTrue(contact.getNumOfAddresses() == 0);
            
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
            // first we need to delete all Address and 
            // Contacts
            //HibernateUtil.deleteAllContacts(session);
            
            
            tx = session.beginTransaction();
            Contact contact = new Contact(createContactArchetypeId());
            assertTrue(contact != null);
            
            session.save(contact);

            Address address = createAddress();
            session.save(address);
            contact.addAddress(address); 
            tx.commit();
            
            // ensure that there is still one more address
            contact = getContactById(session, contact.getUid());
            assertTrue(contact.getNumOfAddresses() == 1);
            
            // retrieve the contact and check that the address is also
            // retrieved
            tx = session.beginTransaction();
            contact = getContactById(session, contact.getUid());
            assertTrue(contact.getNumOfAddresses() == 1);
            
            // retrieve the first address
            Address theAddress = contact.getAddressesAsArray()[0];
            assertTrue(theAddress != null);
            
            // add another element to the addresss
            address.getDetails().setAttribute("mobile", "04222368612");
            
            // remove the address and save it
            session.update(address);
            tx.commit();
            
            // check that the address removal worked
            contact = getContactById(session, contact.getUid());
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
