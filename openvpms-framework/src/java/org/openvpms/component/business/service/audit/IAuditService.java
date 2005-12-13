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


package org.openvpms.component.business.service.audit;

// java core
import java.util.List;

// openvpms-framework
import org.openvpms.component.business.domain.im.audit.AuditRecord;

/**
 * This defines the public interface of the Audit Service, which is 
 * more restrictive than other services. The service only provides read
 * only capabilities.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public interface IAuditService {
    /**
     * Return the {@link AuditRecord} with the specified id
     * 
     * @param id
     *            the identity of the record to retrieve
     * @return AuditRecord
     *            the associated record or null if one does not exist
     * @throws AuditServiceException
     *            if it cannot complete the request            
     */
    public AuditRecord getById(long id);
    
    /**
     * Return all the {@link AuditRecord} instances for the specified object
     * id and archetypeId. Remember that the the object id is scoped to an 
     * archetypeId.
     * 
     * @param archetypeId
     *            the archetype id of the object
     * @param objectId
     *            the target object id [This is not the uid of the audit record]
     * @return List<AuditRecord>
     *            the list of matching records or null
     * @throws AuditServiceException                                    
     */
    public List<AuditRecord> getbyObjectId(String archetypeId, long id);
}
