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
 *  $Id: IAuditDAO.java 292 2005-10-28 02:25:05Z jalateras $
 */

package org.openvpms.component.business.dao.im.audit;

// openvpms-domain
import java.util.List;

// openvpms-framework
import org.openvpms.component.business.domain.im.audit.AuditRecord;

/**
 * This interface provides data access object (DAO) support for objects of 
 * type {@link AuditRecord}. The class includes the capability to perform save 
 * and query data. Audit Records are immutable and cannot be modified.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2005-10-28 12:25:05 +1000 (Fri, 28 Oct 2005) $
 */
public interface IAuditDAO {
    /**
     * Insert the specified {@link AuditRecord}.
     * 
     * @param audit
     *            the audit record to insert
     * @throws AuditDAOException
     *             a runtime exception if the request cannot complete
     */
    public void insert(AuditRecord audit);

    /**
     * Retrieve the {@link AuditRecord} with the specified id
     * 
     * @param id
     *            the id of the audit record
     * @return AuditRecord
     * @throws AuditDAOException            
     */
    public AuditRecord getById(long id);
    
    /**
     * Retrieve all the {@link AuditRecord} instance with the specified
     * fully qualified archetypeId and objectId
     * 
     * @param archetypeId
     *            the archetype of the object
     * @param objectId
     *            the identity of the object
     * @return List<AuditRecord>
     * @throws AuditDAOException                        
     */
    public List<AuditRecord> getByObjectId(String archetypeId, long uid);
}
