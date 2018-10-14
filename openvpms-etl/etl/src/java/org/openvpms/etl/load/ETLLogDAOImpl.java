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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.etl.load;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.Collections;
import java.util.List;


/**
 * Implementation of {@link ETLLogDAO} using hibernate.
 *
 * @author Tim Anderson
 */
public class ETLLogDAOImpl implements ETLLogDAO {

    /**
     * The hibernate session factory.
     */
    private final SessionFactory factory;


    /**
     * Constructs an {@link ETLLogDAOImpl}.
     *
     * @param factory the hibernate session factory
     */
    public ETLLogDAOImpl(SessionFactory factory) {
        this.factory = factory;
    }

    /**
     * Saves a log.
     *
     * @param log the log to save
     */
    public void save(ETLLog log) {
        save(Collections.singletonList(log));
    }

    /**
     * Saves a collection of logs.
     *
     * @param logs the logs to save
     */
    public void save(Iterable<ETLLog> logs) {
        try (Session session = factory.openSession()){
            Transaction tx = session.beginTransaction();
            for (ETLLog log : logs) {
                session.saveOrUpdate(log);
            }
            tx.commit();
        }
    }

    /**
     * Returns an {@link ETLLog} given its identifier.
     *
     * @param logId the log identifier
     * @return the corresponding log, or {@code null} if none is found
     */
    @SuppressWarnings("HardCodedStringLiteral")
    public ETLLog get(long logId) {
        ETLLog result = null;
        StringBuilder queryString = new StringBuilder();

        queryString.append("select l from ");
        queryString.append(ETLLog.class.getName());
        queryString.append(" as l where l.logId = :logId");

        try (Session session = factory.openSession()){
            Query query = session.createQuery(queryString.toString());
            query.setParameter("logId", logId);
            List set = query.list();
            if (!set.isEmpty()) {
                result = (ETLLog) set.get(0);
            }
        }
        return result;
    }


    /**
     * Returns all {@link ETLLog}s associated with a loader, legacy row
     * identifier and archetype.
     *
     * @param loader    the loader name. May be {@code null} to indicate all loaders
     * @param rowId     the legacy row identifier
     * @param archetype the archetype short name. May be {@code null} to indicate all objects with the same legacy
     *                  identifier. May contain '*' wildcards.
     * @return all logs matching the criteria
     */
    @SuppressWarnings({"unchecked", "HardCodedStringLiteral"})
    public List<ETLLog> get(String loader, String rowId, String archetype) {
        List result;
        StringBuilder queryString = new StringBuilder();

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

        try (Session session = factory.openSession()){
            Query query = session.createQuery(queryString.toString());
            query.setParameter("rowId", rowId);
            if (loader != null) {
                query.setParameter("loader", loader);
            }
            if (archetype != null) {
                query.setParameter("archetype", archetype);
            }

            result = query.list();
        }
        return (List<ETLLog>) result;
    }

    /**
     * Determines if a legacy row has been successfully processed.
     *
     * @param loader the loader name
     * @param rowId  the legacy row identifier
     * @return {@code true} if the row has been successfully processed
     */
    @SuppressWarnings("HardCodedStringLiteral")
    public boolean processed(String loader, String rowId) {
        boolean processed;
        StringBuilder queryString = new StringBuilder();

        queryString.append("select errors from ");
        queryString.append(ETLLog.class.getName());
        queryString.append(" as l where l.rowId = :rowId and ");
        queryString.append("l.loader = :loader");
        try (Session session = factory.openSession()) {
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
        StringBuilder queryString = new StringBuilder();

        queryString.append("delete from ");
        queryString.append(ETLLog.class.getName());
        queryString.append(" where loader = :loader and rowId = :rowId");

        try (Session session = factory.openSession()){
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(queryString.toString());
            query.setParameter("loader", loader);
            query.setParameter("rowId", rowId);
            query.executeUpdate();
            transaction.commit();
        }
    }

}
