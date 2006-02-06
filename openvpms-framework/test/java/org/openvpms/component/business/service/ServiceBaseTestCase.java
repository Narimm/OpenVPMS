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

package org.openvpms.component.business.service;

// spring-context
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

// hibernate
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;


/**
 * Test the entity service
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class ServiceBaseTestCase extends
        AbstractDependencyInjectionSpringContextTests {

    /**
     * static to hold all session
     */
    private static final ThreadLocal<Session> session = new ThreadLocal<Session>();
    
    /**
     * Hibernate session factory
     */
    protected SessionFactory sessionFactory;

    /**
     * Default constructor
     */
    public ServiceBaseTestCase() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        this.sessionFactory = (SessionFactory) applicationContext
                .getBean("sessionFactory");
        assertTrue(sessionFactory != null);
    }

    /**
     * Get the current hibernate session
     * 
     * @return Session
     * @throws Exception
     */
    protected Session currentSession() throws Exception {
        Session s = (Session) session.get();
        // Open a new Session, if this Thread has none yet
        if (s == null) {
            s = sessionFactory.openSession();
            session.set(s);
        }
        return s;
    }

    /**
     * Close the current hibernate session
     * 
     * @throws Exception
     */
    protected void closeSession() throws Exception {
        Session s = (Session) session.get();
        session.set(null);
        if (s != null)
            s.close();
    }
    
    /**
     * Get an object by id using the specified named query
     */
    protected Object getObjectById(String queryName, long uid) 
    throws Exception {
        try {
            Session session = currentSession();
            Query query = session.getNamedQuery(queryName);
            query.setParameter("uid", uid);
            if (query.list().size() == 0) {
                return null;
            } else {
                return query.list().get(0);
            }
        } finally {
            closeSession();
        }
    }
}
