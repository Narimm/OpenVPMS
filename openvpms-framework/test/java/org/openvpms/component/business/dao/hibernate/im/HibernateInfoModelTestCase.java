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

// java core
import java.util.Calendar;

//hibernate
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.cfg.Configuration;

// openvpms-framework
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.ActRelationship;
import org.openvpms.component.business.domain.im.common.Classification;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityClassification;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.datatypes.basic.DynamicAttributeMap;
import org.openvpms.component.business.domain.im.datatypes.quantity.DvInterval;
import org.openvpms.component.business.domain.im.datatypes.quantity.datetime.DvDateTime;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.domain.im.party.Address;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.system.common.test.BaseTestCase;
import org.openvpms.component.system.service.uuid.JUGGenerator;

// jug
import org.safehaus.uuid.UUIDGenerator;

/**
 * This is the class for all hibernate persistence test cases
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
        config.addClass(Address.class);
        config.addClass(Contact.class);
        config.addClass(Classification.class);
        config.addClass(Entity.class);
        config.addClass(EntityClassification.class);
        config.addClass(Act.class);
        config.addClass(ActRelationship.class);
        config.addClass(Participation.class);
        config.addClass(EntityRelationship.class);
        config.addClass(EntityIdentity.class);
        config.addClass(Lookup.class);
        config.addClass(LookupRelationship.class);
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

    /**
     * Get the current hibernate session
     * 
     * @return Session
     * @throws Exception
     */
    public Session currentSession() throws Exception {
        Session s = (Session) session.get();
        // Open a new Session, if this Thread has none yet
        if (s == null) {
            s = getSessionFactory().openSession();
            session.set(s);
        }
        return s;
    }

    /**
     * Close the current hibernate session
     * 
     * @throws Exception
     */
    public void closeSession() throws Exception {
        Session s = (Session) session.get();
        session.set(null);
        if (s != null)
            s.close();
    }
    
    /**
     * Return a {@link DvDateTime} interval
     * 
     * @return DvInterval<DvDateTime>
     */
    protected DvInterval<DvDateTime> createTimeInterval() {
        Calendar lower = Calendar.getInstance();
        Calendar upper = Calendar.getInstance();
        
        lower.set(1963, 12, 20);
        upper.set(1963, 12, 25);
        return new DvInterval<DvDateTime>(new DvDateTime(lower), 
                new DvDateTime(upper));
    }
    
    /**
     * Create a simple detail object
     * 
     * @return DynamicAttributeMap
     */
    protected DynamicAttributeMap createSimpleAttributeMap() {
        DynamicAttributeMap map = new DynamicAttributeMap();
        map.setAttribute("dummy", "dummy");
        map.setAttribute("dummy1", "dummy1");
        
        return map;
    }
}
