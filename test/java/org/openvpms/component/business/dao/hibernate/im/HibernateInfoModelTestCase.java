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

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.After;
import org.junit.Before;
import org.openvpms.component.business.dao.hibernate.im.act.ActDOImpl;
import org.openvpms.component.business.dao.hibernate.im.act.ActRelationshipDOImpl;
import org.openvpms.component.business.dao.hibernate.im.act.ParticipationDOImpl;
import org.openvpms.component.business.dao.hibernate.im.archetype.ActionTypeDescriptorDOImpl;
import org.openvpms.component.business.dao.hibernate.im.archetype.ArchetypeDescriptorDOImpl;
import org.openvpms.component.business.dao.hibernate.im.archetype.AssertionDescriptorDOImpl;
import org.openvpms.component.business.dao.hibernate.im.archetype.AssertionTypeDescriptorDOImpl;
import org.openvpms.component.business.dao.hibernate.im.archetype.NodeDescriptorDOImpl;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDO;
import org.openvpms.component.business.dao.hibernate.im.document.DocumentDOImpl;
import org.openvpms.component.business.dao.hibernate.im.entity.EntityDOImpl;
import org.openvpms.component.business.dao.hibernate.im.entity.EntityIdentityDOImpl;
import org.openvpms.component.business.dao.hibernate.im.entity.EntityRelationshipDOImpl;
import org.openvpms.component.business.dao.hibernate.im.entity.ReflectingObjectLoader;
import org.openvpms.component.business.dao.hibernate.im.lookup.LookupDOImpl;
import org.openvpms.component.business.dao.hibernate.im.lookup.LookupRelationshipDOImpl;
import org.openvpms.component.business.dao.hibernate.im.party.ContactDOImpl;
import org.openvpms.component.business.dao.hibernate.im.product.ProductPriceDOImpl;
import org.openvpms.component.business.dao.hibernate.im.security.ArchetypeAuthorityDOImpl;
import org.openvpms.component.business.dao.hibernate.im.security.SecurityRoleDOImpl;

import java.util.HashMap;
import java.util.Map;


/**
 * This is the base class for all hibernate persistence test cases.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class HibernateInfoModelTestCase {

    /**
     * The hibernate session factory.
     */
    private SessionFactory sessionFactory;

    /**
     * The current session.
     */
    private Session session;

    /**
     * Helper to fully load object graphs.
     */
    ReflectingObjectLoader loader = new ReflectingObjectLoader();


    /**
     * Returns current hibernate session.
     *
     * @return the current hibernate session
     */
    public Session getSession() {
        if (session == null) {
            session = createSession();
        }
        return session;
    }

    /**
     * Creates a new hibernate session.
     *
     * @return a new session
     */
    public Session createSession() {
        return sessionFactory.openSession();
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        // create the hibernate session factory
        Configuration config = new Configuration();
        config.addClass(ContactDOImpl.class);
        config.addClass(EntityDOImpl.class);
        config.addClass(ActDOImpl.class);
        config.addClass(ActRelationshipDOImpl.class);
        config.addClass(ParticipationDOImpl.class);
        config.addClass(EntityRelationshipDOImpl.class);
        config.addClass(EntityIdentityDOImpl.class);
        config.addClass(LookupDOImpl.class);
        config.addClass(LookupRelationshipDOImpl.class);
        config.addClass(ArchetypeDescriptorDOImpl.class);
        config.addClass(NodeDescriptorDOImpl.class);
        config.addClass(AssertionDescriptorDOImpl.class);
        config.addClass(AssertionTypeDescriptorDOImpl.class);
        config.addClass(ActionTypeDescriptorDOImpl.class);
        config.addClass(ProductPriceDOImpl.class);
        config.addClass(SecurityRoleDOImpl.class);
        config.addClass(ArchetypeAuthorityDOImpl.class);
        config.addClass(DocumentDOImpl.class);
        sessionFactory = config.buildSessionFactory();
    }

    /**
     * Cleans up after a test.
     */
    @After
    public void tearDown() {
        if (session != null) {
            session.close();
        }
        sessionFactory.close();
    }

    /**
     * Returns the session factory.
     *
     * @return the session factory
     */
    protected SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Creates a simple detail object.
     *
     * @return a new map
     */
    protected Map<String, Object> createSimpleAttributeMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("dummy", "dummy");
        map.put("dummy1", "dummy1");
        return map;
    }

    /**
     * Reloads an object from the database, using a new session.
     *
     * @param object the object to reload
     * @return the reloaded object or <tt>null</tt> if it can't be found
     */
    @SuppressWarnings("unchecked")
    protected <T extends IMObjectDO> T reload(T object) {
        Object result;
        Session session = createSession();
        try {
            result = session.get(object.getClass(), object.getId());
            if (result != null) {
                loader.load(result);
            }
        } finally {
            session.close();
        }
        return (T) result;
    }

    /**
     * Counts instances of the specified type.
     *
     * @param type the type
     * @return the instance count
     */
    protected int count(Class type) {
        String hql = "select count(o) from " + type.getName() + " as o";
        Query query = getSession().createQuery(hql);
        return ((Number) query.list().get(0)).intValue();
    }

    /**
     * Counts the total no. of 'details' elements for a type.
     *
     * @param type the type
     * @return the instance count
     */
    protected int countDetails(Class type) {
        String hql = "select count(elements(p)) from "
                     + type.getName() + " o right outer join o.details as p";
        Query query = getSession().createQuery(hql);
        return ((Number) query.list().get(0)).intValue();
    }

}
