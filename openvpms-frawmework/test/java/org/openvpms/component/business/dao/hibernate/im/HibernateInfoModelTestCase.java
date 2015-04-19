/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */


package org.openvpms.component.business.dao.hibernate.im;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDO;
import org.openvpms.component.business.dao.hibernate.im.entity.ReflectingObjectLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.util.HashMap;
import java.util.Map;


/**
 * This is the base class for all hibernate persistence test cases.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
@ContextConfiguration("/datasource-context.xml")
public abstract class HibernateInfoModelTestCase extends AbstractJUnit4SpringContextTests {

    /**
     * The hibernate session factory.
     */
    @Autowired
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
     * Cleans up after a test.
     */
    @After
    public void tearDown() {
        if (session != null) {
            session.close();
        }
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
