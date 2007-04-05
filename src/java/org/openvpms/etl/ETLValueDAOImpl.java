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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;


/**
 * Implementation of {@link ETLValueDAO} using hibernate.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ETLValueDAOImpl implements ETLValueDAO {

    private final SessionFactory factory;


    /**
     * Constructs a new <tt>ETLValueDAOImpl</tt>.
     */
    public ETLValueDAOImpl() {
        this(new Configuration());
    }

    /**
     * Constructs a new <tt>ETLValueDAOImpl</tt>.
     *
     * @param properties configuration properties
     */
    public ETLValueDAOImpl(Properties properties) {
        this(new Configuration().addProperties(properties));
    }

    /**
     * Constructs a new <tt>ETLValueDAOImpl<tt>.
     *
     * @param factory the hibernate session factory
     */
    public ETLValueDAOImpl(SessionFactory factory) {
        this.factory = factory;
    }

    /**
     * Constructs a new <tt>ETLValueDAOImpl<tt>.
     *
     * @param config the hibernate configuration
     */
    protected ETLValueDAOImpl(Configuration config) {
        config.addClass(ETLValue.class);
        factory = config.buildSessionFactory();
    }

    /**
     * Save a value.
     *
     * @param value the value to save
     */
    public void save(ETLValue value) {
        save(Arrays.asList(value));
    }

    /**
     * Save a collection of values
     *
     * @param values the values to save
     */
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

    /**
     * Returns a collection of values, ordered on
     * {@link ETLValue#getObjectId()}.
     *
     * @param firstResult the index of the first result
     * @param maxResults  the maximum no. of results, or <tt>-1</tt> for all
     *                    results
     * @return a collection of values
     */
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

    /**
     * Returns all {@link ETLValue}s associated with an object identifier.
     *
     * @param objectId the object identifier
     * @return all values associated with <tt>objectId</tt>
     */
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

    /**
     * Returns all {@link ETLValue}s associated with a legacy identifier and
     * archetype short name.
     *
     * @param legacyId  the legacy identifier
     * @param archetype the archetype short name
     * @return all values with matching <tt>legacyId</tt> and <tt>archetype</tt>
     */
    @SuppressWarnings("unchecked")
    public List<ETLValue> get(String legacyId, String archetype) {
        StringBuffer queryString = new StringBuffer();

        queryString.append("select v from ");
        queryString.append(ETLValue.class.getName());
        queryString.append(" as v where v.legacyId = :legacyId and ");
        if (archetype.contains("*")) {
            archetype = archetype.replace("*", "%");
            queryString.append("v.archetype like :archetype");
        } else {
            queryString.append("v.archetype = :archetype");
        }

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

    /**
     * Returns all distinct archetypes and their corresponding nodes referred
     * to {@link ETLValue} instances.
     *
     * @return the archetypes and their corresponding nodes
     */
    public Collection<ETLArchetype> getArchetypes() {
        List<ETLArchetype> result = new ArrayList<ETLArchetype>();
        StringBuffer queryString = new StringBuffer();

        queryString.append("select distinct v.archetype, v.name from ");
        queryString.append(ETLValue.class.getName());
        queryString.append(" as v order by v.archetype");

        Session session = factory.openSession();
        try {
            Query query = session.createQuery(queryString.toString());
            Iterator iterator = query.iterate();
            String archetype = null;
            List<String> names = new ArrayList<String>();
            while (iterator.hasNext()) {
                Object[] values = (Object[]) iterator.next();
                if (archetype != null && !archetype.equals(values[0])) {
                    result.add(new ETLArchetype(archetype, names));
                    names.clear();
                }
                archetype = (String) values[0];
                names.add((String) values[1]);
            }
            if (archetype != null) {
                result.add(new ETLArchetype(archetype, names));
            }
        } finally {
            session.clear();
            session.close();
        }
        return result;
    }

    /**
     * Returns all distinct values for an archetype and node name referred
     * to by {@link ETLValue} instances. This excludes reference values.
     *
     * @param archetype the archetype short name
     * @param name      the node name
     * @return distinct values for the archetype and node name
     */
    public Collection<String> getDistinctValues(String archetype, String name) {
        List<String> result = new ArrayList<String>();
        StringBuffer queryString = new StringBuffer();

        queryString.append("select distinct v.value from ");
        queryString.append(ETLValue.class.getName());
        queryString.append(" as v where archetype=:archetype and name=:name");
        queryString.append(" and v.reference=false");

        Session session = factory.openSession();
        try {
            Query query = session.createQuery(queryString.toString());
            query.setParameter("archetype", archetype);
            query.setParameter("name", name);
            Iterator iterator = query.iterate();
            while (iterator.hasNext()) {
                result.add((String) iterator.next());
            }
        } finally {
            session.clear();
            session.close();
        }
        return result;
    }

    /**
     * Returns all distinct value pairs for an archetype and node pair,
     * referred to by {@link ETLValue} instances with the same objectId.
     * This excludes reference values
     */
    public Collection<ETLPair> getDistinctValuePairs(
            String archetype, String name1, String name2) {
        List<ETLPair> result = new ArrayList<ETLPair>();
        StringBuffer queryString = new StringBuffer();

        queryString.append("select distinct a.value, b.value from ");
        queryString.append(ETLValue.class.getName());
        queryString.append(" as a, ");
        queryString.append(ETLValue.class.getName());
        queryString.append(" as b where a.objectId=b.objectId ");
        queryString.append("and a.archetype=b.archetype ");
        queryString.append("and a.archetype=:archetype and a.name=:name1 ");
        queryString.append("and b.name=:name2 and a.reference=false ");
        queryString.append("and b.reference=false");

        Session session = factory.openSession();
        try {
            Query query = session.createQuery(queryString.toString());
            query.setParameter("archetype", archetype);
            query.setParameter("name1", name1);
            query.setParameter("name2", name2);
            Iterator iterator = query.iterate();
            while (iterator.hasNext()) {
                Object[] values = (Object[]) iterator.next();
                result.add(new ETLPair((String) values[0], (String) values[1]));
            }
        } finally {
            session.clear();
            session.close();
        }
        return result;
    }
}
