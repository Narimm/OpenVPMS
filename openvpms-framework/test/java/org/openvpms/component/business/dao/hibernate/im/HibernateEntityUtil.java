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


package org.openvpms.component.business.dao.hibernate.im;

// java 

//hibernate 
import net.sf.hibernate.Session;


/**
 * This class provides some utility functions to manipulate persistent 
 * objects using hibernate.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class HibernateEntityUtil {
    /**
     * Return the number of rows in the classification table
     * 
     * @param session 
     *            the session to use
     * @return int
     * @throws Exception
     *            propagate exception to caller            
     */
    public static int getClassificationRowCount(Session session)
    throws Exception {
        return ((Integer)session.getNamedQuery("classification.getRowCount")
                .list().get(0)).intValue();
        
    }
    
    /**
     * Return the number of rows in the entity identity table.
     * 
     * @param session
     *            execute the request on this session
     * @return int
     * @throws Exception
     *            propage exception to caller.            
     */
    public static int getEntityIdentityRowCount(Session session)
    throws Exception {
        return ((Integer)session.getNamedQuery("entityIdentity.getRowCount")
                .list().get(0)).intValue();
        
    }
}
