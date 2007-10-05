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

package org.openvpms.etl.load;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;


/**
 * Implementation of {@link ETLLogDAO} using hibernate.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ETLLogDAOImpl implements ETLLogDAO {

    /**
     * The hibernate session factory.
     */
    private final SessionFactory factory;


    /**
     * Constructs a new <tt>ETLLogDAOImpl</tt>.
     */
    public ETLLogDAOImpl() {
        this(new Configuration());
    }

    /**
     * Constructs a new <tt>ETLLogDAOImpl</tt>.
     *
     * @param properties configuration properties
     */
    public ETLLogDAOImpl(Properties properties) {
        this(new Configuration().addProperties(properties));
    }

    /**
     * Constructs a new <tt>ETLLogDAOImpl<tt>.
     *
     * @param factory the hibernate session factory
     */
    public ETLLogDAOImpl(SessionFactory factory) {
        this.factory = factory;
    }

    /**
     * Constructs a new <tt>ETLLogDAOImpl<tt>.
     *
     * @param config the hibernate configuration
     */
    protected ETLLogDAOImpl(Configuration config) {
        config.addClass(ETLLog.class);
        factory = config.buildSessionFactory();
    }

    /**
     * Saves a log.
     *
     * @param log the log to save
     */
    public void save(ETLLog log) {
        save(Arrays.asList(log));
    }

    /**
     * Saves a collection of logs.
     *
     * @param logs the logs to save
     */
    public void save(Iterable<ETLLog> logs) {
        Session session = factory.openSession();
        Transaction tx = session.beginTransaction();
        try {
            for (ETLLog log : logs) {
                session.saveOrUpdate(log);
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
     * Returns an {@link ETLLog} given its identifier.
     *
     * @param logId the log identifier
     * @return the corresponding log, or <tt>null</tt> if none is found
     */
    @SuppressWarnings("HardCodedStringLiteral")
    public ETLLog get(long logId) {
        ETLLog result = null;
        StringBuffer queryString = new StringBuffer();

        queryString.append("select l from ");
        queryString.append(ETLLog.class.getName());
        queryString.append(" as l where l.logId = :logId");

        Session session = factory.openSession();
        try {
            Query query = session.createQuery(queryString.toString());
            query.setParameter("logId", logId);
            List set = query.list();
            if (!set.isEmpty()) {
                result = (ETLLog) set.get(0);
            }
        } finally {
            session.clear();
            session.close();
        }
        return result;
    }


    /**
     * Returns all {@link ETLLog}s associated with a loader, legacy row
     * identifier and archetype.
     *
     * @param loader    the loader name. May be <tt>null</tt> to indicate all
     *                  loaders
     * @param rowId     the legacy row identifier
     * @param archetype the archetype short name. May be <tt>null</tt> to
     *                  indicate all objects with the same legacy identifier.
     *                  May contain '*' wildcards.
     * @return all logs matching the criteria
     */
    @SuppressWarnings({"unchecked", "HardCodedStringLiteral"})
    public List<ETLLog> get(String loader, String rowId, String archetype) {
        List result;
        StringBuffer queryString = new StringBuffer();

        queryString.append("select l from ");
        queryString.append(ETLLog.class.getName());
        queryString.append(" as l where l.rowId = :rowId");
        if (loader != null) {
            queryString.append(" and loader = :loader");
        }
        if (archetype != null) {
            if (archetype.contains("*")) {
                archetype = archetype.replace("*", "%");
                queryString.append(" and archetype like :archetype");
            } else {
                queryString.append(" and archetype = :archetype");
            }
        }

        Session session = factory.openSession();
        try {
            Query query = session.createQuery(queryString.toString());
            query.setParameter("rowId", rowId);
            if (loader != null) {
                query.setParameter("loader", loader);
            }
            if (archetype != null) {
                query.setParameter("archetype", archetype);
            }

            result = query.list();
        } finally {
            session.clear();
            session.close();
        }
        return (List<ETLLog>) result;
    }

    /**
     * Determines if a legacy row has been successfully processed.
     *
     * @param loader the loader name
     * @param rowId  the legacy row identifier
     * @return <tt>true> if the row has been sucessfully processed
     */
    @SuppressWarnings("HardCodedStringLiteral")
    public boolean processed(String loader, String rowId) {
        boolean processed;
        StringBuffer queryString = new StringBuffer();

        queryString.append("select errors from ");
        queryString.append(ETLLog.class.getName());
        queryString.append(" as l where l.rowId = :rowId and ");
        queryString.append("l.loader = :loader");
        Session session = factory.openSession();
        try {
            Query query = session.createQuery(queryString.toString());
            query.setParameter("rowId", rowId);
            query.setParameter("loader", loader);
            int rows = 0;
            boolean errors = false;
            for (Object value : query.list()) {
                ++rows;
                if (value != null) {
                    errors = true;
                    break;
                }
            }
            processed = (rows != 0 && !errors);
        } finally {
            session.clear();
            session.close();
        }
        return processed;
    }

    /**
     * Deletes all {@link ETLLog}s associated with a loader and legacy row
     * identifier.
     *
     * @param loader the loader name
     * @param rowId  the legacy row identifier
     */
    @SuppressWarnings("HardCodedStringLiteral")
    public void remove(String loader, String rowId) {
        StringBuffer queryString = new StringBuffer();

        queryString.append("delete from ");
        queryString.append(ETLLog.class.getName());
        queryString.append(" where loader = :loader and rowId = :rowId");

        Session session = factory.openSession();
        try {
            Query query = session.createQuery(queryString.toString());
            query.setParameter("loader", loader);
            query.setParameter("rowId", rowId);
            query.executeUpdate();
        } finally {
            session.clear();
            session.close();
        }
    }

}
