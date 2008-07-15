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
 *  $Id: AuditDAOHibernate.java 328 2005-12-07 13:31:09Z jalateras $
 */


package org.openvpms.component.business.dao.hibernate.im.audit;

import org.openvpms.component.business.dao.im.audit.AuditDAOException;
import org.openvpms.component.business.dao.im.audit.IAuditDAO;
import org.openvpms.component.business.domain.im.audit.AuditRecord;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.List;


/**
 * This is an implementation of the IAuditDAO DAO for hibernate. It uses the
 * Spring Framework's template classes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2005-12-08 00:31:09 +1100 (Thu, 08 Dec 2005) $
 */
public class AuditDAOHibernate extends HibernateDaoSupport
        implements IAuditDAO {

    /**
     * Query by object identifier and archetype identifier.
     */
    private static final String AUDIT_QUERY = "from  "
            + AuditRecord.class.getName()
            + " as auditRecord where auditRecord.archetypeId = ? and "
            + "auditRecord.objectId = ?";

    /**
     * Default constructor
     */
    public AuditDAOHibernate() {
        super();
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.dao.im.audit.IAuditDAO#getByArchetypeIdAndId(java.lang.String, long)
     */
    @SuppressWarnings("unchecked")
    public List<AuditRecord> getByObjectId(String archetypeId, long objectId) {
        List<AuditRecord> results;
        try {
            Object[] values = new Object[]{archetypeId, objectId};
            results = getHibernateTemplate().find(AUDIT_QUERY, values);
        } catch (Exception exception) {
            throw new AuditDAOException(
                    AuditDAOException.ErrorCode.FailedToFindAuditRecords,
                    new Object[]{archetypeId, objectId}, exception);
        }

        return results;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.dao.im.audit.IAuditDAO#getById(long)
     */
    public AuditRecord getById(long id) {
        try {
            return (AuditRecord) getHibernateTemplate().load(AuditRecord.class,
                                                             id);
        } catch (Exception exception) {
            throw new AuditDAOException(
                    AuditDAOException.ErrorCode.FailedToFindAuditRecordById,
                    new Object[]{id});
        }
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.dao.im.audit.IAuditDAO#insert(org.openvpms.component.business.domain.im.audit.AuditRecord)
     */
    public void insert(AuditRecord audit) {
        try {
            getHibernateTemplate().save(audit);
        } catch (Exception exception) {
            throw new AuditDAOException(
                    AuditDAOException.ErrorCode.FailedToInsertAuditRecord,
                    new Object[]{audit.getArchetypeId(), audit.getObjectId()},
                    exception);
        }
    }
}
