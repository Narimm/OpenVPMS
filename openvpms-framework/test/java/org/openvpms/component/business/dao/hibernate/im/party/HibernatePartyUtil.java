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

// java 
import java.util.List;

//hibernate 
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

// openvpms framework
import org.openvpms.component.business.dao.hibernate.im.HibernateUtil;
import org.openvpms.component.business.domain.im.party.Address;
import org.openvpms.component.business.domain.im.party.Contact;

/**
 * This class provides some utility functions to manipulate persistent 
 * objects using hibernate.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class HibernatePartyUtil extends HibernateUtil {
    /**
     * Retrieve all the persistent address instances
     * 
     * @param session
     *            the hibernate session to use
     * @return List
     * @throws Exception
     *            propagate all the exceptions to the caller            
     */
    static List getAllAddresses(Session session)
    throws Exception {
        return session.getNamedQuery("address.getAllAddresses").list();
    }
    
    /**
     * Delete all the address records
     * 
     * @throws Exception
     */
    static  void deleteAllAddresses(Session session)
    throws Exception {
        List list = getAllAddresses(session);
        Transaction tx = session.beginTransaction();
        for (Object element : list) {
            Address address = (Address)element;
            address.setContacts(null);
            session.delete(address);
            
        }
        tx.commit();
    }
    
    /**
     * Retrieve all the persistent contact instances
     * 
     * @param session
     *            the hibernate session to use
     * @return List
     * @throws Exception
     *            propagate all the exceptions to the caller            
     */
    static List getAllContacts(Session session)
    throws Exception {
        return session.getNamedQuery("contact.getAllContacts").list();
    }
    
    /**
     * Delete all the contact records
     * 
     * @throws Exception
     */
    static  void deleteAllContacts(Session session)
    throws Exception {
        List list = getAllContacts(session);
        Transaction tx = session.beginTransaction();
        for (Object element : list) {
            Contact contact = (Contact)element;
            for (Address address : contact.getAddressesAsArray()) {
                contact.removeAddress(address);
                session.delete(address);
            }
            session.delete(contact);
        }
        tx.commit();
    }
}
