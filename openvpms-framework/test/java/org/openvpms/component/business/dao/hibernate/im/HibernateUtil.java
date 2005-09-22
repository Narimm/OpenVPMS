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
public class HibernateUtil {
    /**
     * Return the number of rows in the specified table. It assumes that
     * there is a query in the form of table.getRowCount defined.
     * 
     * @param session
     *            execute the request on this session
     * @param table
     *            the name of the table (not the SQL table name)                    
     * @return int
     *            the number of rows
     * @throws Exception
     *            propage exception to caller.            
     */
    public static int getTableRowCount(Session session, String table)
    throws Exception {
        return ((Integer)session.getNamedQuery(table + ".getRowCount")
                .list().get(0)).intValue();
        
    }
}
