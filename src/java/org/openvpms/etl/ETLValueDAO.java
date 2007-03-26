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
public class ETLValueDAO {

    private final SessionFactory factory;

    public ETLValueDAO(Properties properties) {
        this(new Configuration().addProperties(properties));
    }

    public ETLValueDAO(SessionFactory factory) {
        this.factory = factory;
    }

    protected ETLValueDAO(Configuration config) {
        config.addClass(ETLValue.class);
        factory = config.buildSessionFactory();
    }

    public ETLValueDAO() {
        this(new Configuration());
    }

    public void save(ETLValue value) {
        save(Arrays.asList(value));
    }

    public void save(Iterable<ETLValue> values) {
        Session session = factory.openSession();
        Transaction tx = session.beginTransaction();
        try {
            for (ETLValue value : values) {
                session.saveOrUpdate(value);
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
        }
    }

    @SuppressWarnings("unchecked")
    public List<ETLValue> get(int firstResult, int maxResults) {
        List result;
        StringBuffer queryString = new StringBuffer();

        queryString.append("select v from ");
        queryString.append(ETLValue.class.getName());
        queryString.append(" as v order by v.objectId");

        Session session = factory.openSession();
        try {
            Query query = session.createQuery(queryString.toString());
            query.setFirstResult(firstResult);
            query.setMaxResults(maxResults);
            result = query.list();
        } finally {
            session.clear();
            session.close();
        }
        return (List<ETLValue>) result;
    }

    /**
     * Returns an {@link ETLValue} given its value identifier.
     *
     * @param valueId the value identifier
     * @return the corresponding value, or <tt>null</tt> if none is found
     */
    public ETLValue get(long valueId) {
        ETLValue result = null;
        StringBuffer queryString = new StringBuffer();

        queryString.append("select v from ");
        queryString.append(ETLValue.class.getName());
        queryString.append(" as v where v.valueId = :valueId");

        Session session = factory.openSession();
        try {
            Query query = session.createQuery(queryString.toString());
            query.setParameter("valueId", valueId);
            List set = query.list();
            if (!set.isEmpty()) {
                result = (ETLValue) set.get(0);
            }
        } finally {
            session.clear();
            session.close();
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<ETLValue> get(String objectId) {
        StringBuffer queryString = new StringBuffer();

        queryString.append("select v from ");
        queryString.append(ETLValue.class.getName());
        queryString.append(" as v where v.objectId = :objectId");

        Session session = factory.openSession();
        List set;
        try {
            Query query = session.createQuery(queryString.toString());
            query.setParameter("objectId", objectId);
            set = query.list();
        } finally {
            session.clear();
            session.close();
        }
        return (List<ETLValue>) set;
    }

    @SuppressWarnings("unchecked")
    public List<ETLValue> get(String legacyId, String archetype) {
        StringBuffer queryString = new StringBuffer();

        queryString.append("select v from ");
        queryString.append(ETLValue.class.getName());
        queryString.append(" as v where v.legacyId = :legacyId and " +
                "v.archetype = :archetype");

        Session session = factory.openSession();
        List set;
        try {
            Query query = session.createQuery(queryString.toString());
            query.setParameter("legacyId", legacyId);
            query.setParameter("archetype", archetype);
            set = query.list();
        } finally {
            session.clear();
            session.close();
        }
        return (List<ETLValue>) set;
    }

}
