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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.etl;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ETLObjectDAO {

    private final SessionFactory factory;
    private ThreadLocal<Session> session = new ThreadLocal<Session>();

    public ETLObjectDAO(Properties properties) {
        this(new Configuration().addProperties(properties));
    }

    public ETLObjectDAO(SessionFactory factory) {
        this.factory = factory;
    }

    protected ETLObjectDAO(Configuration config) {
        config.addClass(ETLObject.class);
        config.addClass(ETLNode.class);
        config.addClass(ETLValue.class);
        factory = config.buildSessionFactory();
    }

    public ETLObjectDAO() {
        this(new Configuration());
    }

    public void save(ETLObject object) {
        save(Arrays.asList(object));
    }

    public void save(Iterable<ETLObject> objects) {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        try {
            for (ETLObject object : objects) {
                session.saveOrUpdate(object);
            }
            tx.commit();
        } catch (RuntimeException exception) {
            if (tx != null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            session.clear();
            session.close();
            this.session.set(null);
        }
    }

    private Session getSession() {
        Session result = session.get();
        if (result == null) {
            result = factory.openSession();
            session.set(result);
        }
        return result;
    }

    public ETLObject get(long objectId) {
        ETLObject result = null;
        StringBuffer queryString = new StringBuffer();

        queryString.append("select o from ");
        queryString.append(ETLObject.class.getName());
        queryString.append(" as o where o.objectId = :objectId");

        Session session = getSession();
        Query query = session.createQuery(queryString.toString());
        query.setParameter("objectId", objectId);
        List set = query.list();
        if (!set.isEmpty()) {
            result = (ETLObject) set.get(0);
        }
        return result;
    }

    public ETLObject get(String archetype, String legacyId) {
        ETLObject result = null;
        StringBuffer queryString = new StringBuffer();

        queryString.append("select o from ");
        queryString.append(ETLObject.class.getName());
        queryString.append(" as o where o.legacyId = :legacyId and " +
                "o.archetype = :archetype");

        Session session = getSession();
        Query query = session.createQuery(queryString.toString());
        query.setParameter("legacyId", legacyId);
        query.setParameter("archetype", archetype);
        query.setMaxResults(2);
        List set = query.list();
        if (!set.isEmpty()) {
            if (set.size() > 1) {
                throw new RuntimeException("Query resolves to > 1 object");
            }
            result = (ETLObject) set.get(0);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<ETLObject> get(int firstResult, int maxResults) {
        List result;
        StringBuffer queryString = new StringBuffer();

        queryString.append("select o from ");
        queryString.append(ETLObject.class.getName());
        queryString.append(" as o order by o.objectId");

        Session session = getSession();
        Query query = session.createQuery(queryString.toString());
        query.setFirstResult(firstResult);
        query.setMaxResults(maxResults);
        result = query.list();
        return (List<ETLObject>) result;
    }
}
