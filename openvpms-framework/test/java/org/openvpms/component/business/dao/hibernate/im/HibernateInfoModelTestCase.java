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

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.archetype.descriptor.ActionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.domain.im.security.ArchetypeAwareGrantedAuthority;
import org.openvpms.component.business.domain.im.security.SecurityRole;
import org.openvpms.component.system.common.test.BaseTestCase;
import org.openvpms.component.system.service.uuid.JUGGenerator;

import java.util.Map;
import java.util.HashMap;


/**
 * This is the base class for all hibernate persistence test cases.
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

        generator = new JUGGenerator();

        // create the hibernate session factory
        Configuration config = new Configuration();
        config.addClass(Contact.class);
        config.addClass(Entity.class);
        config.addClass(Act.class);
        config.addClass(ActRelationship.class);
        config.addClass(Participation.class);
        config.addClass(EntityRelationship.class);
        config.addClass(EntityIdentity.class);
        config.addClass(Lookup.class);
        config.addClass(LookupRelationship.class);
        config.addClass(ArchetypeDescriptor.class);
        config.addClass(NodeDescriptor.class);
        config.addClass(AssertionDescriptor.class);
        config.addClass(AssertionTypeDescriptor.class);
        config.addClass(ActionTypeDescriptor.class);
        config.addClass(ProductPrice.class);
        config.addClass(SecurityRole.class);
        config.addClass(ArchetypeAwareGrantedAuthority.class);
        config.addClass(Document.class);
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
        Session s = session.get();
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
        Session s = session.get();
        session.set(null);
        if (s != null)
            s.close();
    }

    /**
     * Create a simple detail object
     * 
     * @return DynamicAttributeMap
     */
    @SuppressWarnings("HardCodedStringLiteral")
    protected Map<String, String> createSimpleAttributeMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("dummy", "dummy");
        map.put("dummy1", "dummy1");
        return map;
    }
}
