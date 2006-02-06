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
 *  $Id: HibernateSecurityUtil.java 325 2005-12-05 15:26:58Z jalateras $
 */


package org.openvpms.component.business.dao.hibernate.im.security;

// java 
import java.util.List;

//hibernate 
import org.hibernate.Session;
import org.hibernate.Transaction;

// openvpms framework
import org.openvpms.component.business.dao.hibernate.im.HibernateUtil;
import org.openvpms.component.business.domain.im.party.Contact;

/**
 * This class provides some utility functions to manipulate persistent 
 * objects using hibernate.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate: 2005-12-06 02:26:58 +1100 (Tue, 06 Dec 2005) $
 */
public class HibernateSecurityUtil extends HibernateUtil {
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
            session.delete((Contact)element);
        }
        tx.commit();
    }
}
