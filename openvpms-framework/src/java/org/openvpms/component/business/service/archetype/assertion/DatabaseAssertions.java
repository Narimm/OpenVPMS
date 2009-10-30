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


package org.openvpms.component.business.service.archetype.assertion;

// hibernate

import org.hibernate.SessionFactory;


/**
 * This class contains a number of static methods for evaluating database
 * related assertions.  
 * <p>
 * It has a static attribute to a {@link SessionFactory} instance, which must
 * be set before any instance calls are made.
 *  
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
@Deprecated
public class DatabaseAssertions {

    /**
     * This is a reference to a hibernate session factory, which can be used
     * to request information from the database. 
     * 
     * TODO Do we need to consider support for multiple database instances.
     */
    private static SessionFactory sessionFactory;
    
    /**
     * Default constructor 
     */
    public DatabaseAssertions() {
    }

    /**
     * @return Returns the sessionFactory.
     */
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * This will set the value of the session factory and must be called before
     * any instance methods are called.
     * 
     *@param sessionFactory The sessionFactory to set.
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        DatabaseAssertions.sessionFactory = sessionFactory;
    }
}
