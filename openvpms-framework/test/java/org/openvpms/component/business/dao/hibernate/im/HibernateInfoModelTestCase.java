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

// hibernate
import java.io.File;

import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.cfg.Configuration;

// openvpms-framework
import org.openvpms.component.system.common.test.BaseTestCase;
import org.openvpms.component.system.service.uuid.JUGGenerator;

// jug
import org.safehaus.uuid.UUIDGenerator;

/**
 * This is the base class for all hibernate persistence test cases
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public abstract class HibernateInfoModelTestCase extends BaseTestCase {

    /**
     * A UUID generator.
     */
    private JUGGenerator generator;
    
    /**
     * A Hibernate session factory.
     */
    private SessionFactory sessionFactory;
    
    /**
     * static to hold all session
     */
    public static final ThreadLocal<Session> session = new ThreadLocal<Session>();


    /**
     * mail line 
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(HibernateInfoModelTestCase.class);
    }

    /**
     * Constructor for HibernateInfoModelTestCase.
     * 
     * @param name
     */
    protected HibernateInfoModelTestCase(String name) {
        super(name);
    }

    /*
     * @see BaseTestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    
        generator = new JUGGenerator(
                UUIDGenerator.getInstance().getDummyAddress().toString());
        
        // create the hibernate session factory
        Configuration config = new Configuration();
        config.addDirectory(new File("target/classes/org/openvpms/component/business/domain/im"));
        config.addDirectory(new File("target/classes/org/openvpms/component/business/domain/im/party"));
        this.sessionFactory = config.buildSessionFactory();
    }

    /*
     * @see BaseTestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * @return Returns the generator.
     */
    protected JUGGenerator getGenerator() {
        return generator;
    }

    /**
     * @return Returns the sessionFactory.
     */
    protected SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public Session currentSession() throws Exception {
        Session s = (Session) session.get();
        // Open a new Session, if this Thread has none yet
        if (s == null) {
            s = getSessionFactory().openSession();
            session.set(s);
        }
        return s;
    }

    public void closeSession() throws Exception {
        Session s = (Session) session.get();
        session.set(null);
        if (s != null)
            s.close();
    }

}
