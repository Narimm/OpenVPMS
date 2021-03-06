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

package org.openvpms.web.jobs.recordlocking;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Locks medical records starting prior to a certain time.
 * <p>
 * This bypasses the Archetype Service in order to efficiently update large numbers of records.
 *
 * @author Tim Anderson
 */
class MedicalRecordLocker {

    /**
     * The session factory.
     */
    private final HibernateTemplate template;

    /**
     * The transaction manager.
     */
    private final PlatformTransactionManager transactionManager;

    /**
     * Constructs a {@link MedicalRecordLocker}.
     *
     * @param factory            the Hibernate session factory
     * @param transactionManager the transaction manager
     */
    public MedicalRecordLocker(SessionFactory factory, PlatformTransactionManager transactionManager) {
        template = new HibernateTemplate(factory);
        this.transactionManager = transactionManager;
    }

    /**
     * Locks records starting on or before the specified time.
     *
     * @param shortNames the act archetype short names
     * @param startTime  the start time on/prior to which acts should be locked
     * @param batchSize  the maximum no. of records to lock
     * @return the no. of locked records
     */
    public int lock(final String[] shortNames, final Date startTime, final int batchSize) {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        return transaction.execute(new TransactionCallback<Integer>() {
            @Override
            public Integer doInTransaction(TransactionStatus transactionStatus) {
                return template.execute(session -> {
                    int updated = 0;
                    List ids = getIds(session, shortNames, startTime, batchSize);
                    if (!ids.isEmpty()) {
                        updated = update(session, ids);
                    }
                    return updated;
                });
            }
        });
    }

    /**
     * Returns up to {@code batchSize} identifiers of act that need to be locked.
     *
     * @param session    the session
     * @param shortNames the act archetype short names
     * @param startTime  the start time on/prior to which acts should be locked
     * @param batchSize  the batch szie
     * @return the act identifiers
     */
    protected List getIds(Session session, String[] shortNames, Date startTime, int batchSize) {
        String select = "select id" +
                        " from org.openvpms.component.business.dao.hibernate.im.act.ActDOImpl"
                        + " where status <> 'POSTED' and status <> 'CANCELLED'"
                        + " and archetypeId.shortName in (:shortNames)"
                        + " and activityStartTime <= :startTime";
        Query query = session.createQuery(select);
        query.setParameterList("shortNames", Arrays.asList(shortNames));
        query.setParameter("startTime", startTime);
        query.setMaxResults(batchSize);
        return query.list();
    }

    /**
     * Finalises acts matching the supplied identifiers.
     * <p>
     * This ensures updated acts have their version numbers incremented.
     *
     * @param session the session
     * @param ids     the act identifiers
     * @return the count of updated acts
     */
    protected int update(Session session, List ids) {
        Query update = session.createSQLQuery(
                "update acts"
                + " set status='POSTED',"
                + " version=version + 1"
                + " where status <> 'POSTED' and status <> 'CANCELLED'"
                + " and act_id in (:ids)");
        update.setParameterList("ids", ids);
        return update.executeUpdate();
    }

}
