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

import java.util.HashMap;
import java.util.Map;


/**
 * This is the base class for all hibernate persistence test cases.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class HibernateInfoModelTestCase extends BaseTestCase {

    /**
     * A Hibernate session factory.
     */
    private SessionFactory sessionFactory;

    /**
     * The current session.
     */
    private Session session;


    /*
     * @see BaseTestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

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
        closeSession();
    }

    /**
     * Returns current hibernate session.
     *
     * @return the current hibernate session
     */
    public Session getSession() {
        if (session == null) {
            session = sessionFactory.openSession();
        }
        return session;
    }

    /**
     * Close the current hibernate session.
     */
    public void closeSession() {
        if (session != null) {
            session.close();
            session = null;
        }
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
        Query query = session.createQuery(hql);
        return ((Number) query.list().get(0)).intValue();
    }
}
