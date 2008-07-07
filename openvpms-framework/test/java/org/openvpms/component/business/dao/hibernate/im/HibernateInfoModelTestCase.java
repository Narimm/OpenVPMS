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

import junit.framework.TestCase;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.openvpms.component.business.dao.hibernate.im.act.ActDO;
import org.openvpms.component.business.dao.hibernate.im.act.ActRelationshipDO;
import org.openvpms.component.business.dao.hibernate.im.act.ParticipationDO;
import org.openvpms.component.business.dao.hibernate.im.archetype.ActionTypeDescriptorDO;
import org.openvpms.component.business.dao.hibernate.im.archetype.ArchetypeDescriptorDO;
import org.openvpms.component.business.dao.hibernate.im.archetype.AssertionDescriptorDO;
import org.openvpms.component.business.dao.hibernate.im.archetype.AssertionTypeDescriptorDO;
import org.openvpms.component.business.dao.hibernate.im.archetype.NodeDescriptorDO;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDO;
import org.openvpms.component.business.dao.hibernate.im.document.DocumentDO;
import org.openvpms.component.business.dao.hibernate.im.entity.EntityDO;
import org.openvpms.component.business.dao.hibernate.im.entity.EntityIdentityDO;
import org.openvpms.component.business.dao.hibernate.im.entity.EntityRelationshipDO;
import org.openvpms.component.business.dao.hibernate.im.entity.ReflectingObjectLoader;
import org.openvpms.component.business.dao.hibernate.im.lookup.LookupDO;
import org.openvpms.component.business.dao.hibernate.im.lookup.LookupRelationshipDO;
import org.openvpms.component.business.dao.hibernate.im.party.ContactDO;
import org.openvpms.component.business.dao.hibernate.im.product.ProductPriceDO;
import org.openvpms.component.business.dao.hibernate.im.security.ArchetypeAuthorityDO;
import org.openvpms.component.business.dao.hibernate.im.security.SecurityRoleDO;

import java.util.HashMap;
import java.util.Map;


/**
 * This is the base class for all hibernate persistence test cases.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class HibernateInfoModelTestCase extends TestCase {

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
     *
     * @throws Exception for any error
     */
    protected void setUp() throws Exception {
        // create the hibernate session factory
        Configuration config = new Configuration();
        config.addClass(ContactDO.class);
        config.addClass(EntityDO.class);
        config.addClass(ActDO.class);
        config.addClass(ActRelationshipDO.class);
        config.addClass(ParticipationDO.class);
        config.addClass(EntityRelationshipDO.class);
        config.addClass(EntityIdentityDO.class);
        config.addClass(LookupDO.class);
        config.addClass(LookupRelationshipDO.class);
        config.addClass(ArchetypeDescriptorDO.class);
        config.addClass(NodeDescriptorDO.class);
        config.addClass(AssertionDescriptorDO.class);
        config.addClass(AssertionTypeDescriptorDO.class);
        config.addClass(ActionTypeDescriptorDO.class);
        config.addClass(ProductPriceDO.class);
        config.addClass(SecurityRoleDO.class);
        config.addClass(ArchetypeAuthorityDO.class);
        config.addClass(DocumentDO.class);
        sessionFactory = config.buildSessionFactory();
    }

    /**
     * Cleans up after a test.
     *
     * @throws Exception for any error
     */
    protected void tearDown() throws Exception {
        if (session != null) {
            session.close();
        }
        sessionFactory.close();
    }

    /**
     * Creates a simple detail object.
     *
     * @return a new map
     */
    @SuppressWarnings("HardCodedStringLiteral")
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
     */
    protected int countDetails(Class type) {
        String hql = "select count(elements(p)) from "
                + type.getName() + " o right outer join o.details as p";
        Query query = getSession().createQuery(hql);
        return ((Number) query.list().get(0)).intValue();
    }

}
