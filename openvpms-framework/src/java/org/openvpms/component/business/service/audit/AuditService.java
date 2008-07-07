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

import java.util.Date;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.openvpms.component.business.dao.im.audit.IAuditDAO;
import org.openvpms.component.business.domain.im.audit.AuditRecord;
import org.openvpms.component.business.domain.im.common.IMObject;

/**
 * The class intercepts requests to specified service endpoints and creates
 * and persists an audit record}. The record is logged in its own transaction.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class AuditService implements MethodInterceptor, IAuditService {
    /**
     * Define a logger for this class
     */
    private static final Logger logger = Logger
            .getLogger(AuditService.class);

    /**
     * The DAO used for persisting records
     */
    private IAuditDAO dao;
    
    
    /**
     * Construct an instance of this service passing it the dao it will use
     * to access the persistence storage.
     * 
     * @param dao
     *            the data access object
     */
    public AuditService(IAuditDAO dao) {
        this.dao = dao;
    }
    
    /**
     * @return Returns the dao.
     */
    public IAuditDAO getDao() {
        return dao;
    }

    /**
     * @param dao The dao to set.
     */
    public void setDao(IAuditDAO dao) {
        this.dao = dao;
    }


    /* (non-Javadoc)
     * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
     */
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object rval = invocation.proceed();
        afterMethodInvocation(invocation);
        
        return rval;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.audit.IAuditService#getById(long)
     */
    public AuditRecord getById(long id) {
        try {
            return dao.getById(id);
        } catch (Exception exception) {
            throw new AuditServiceException(
                    AuditServiceException.ErrorCode.FailedToGetById,
                    new Object[] {id}, exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.audit.IAuditService#getbyObjectId(org.openvpms.component.business.domain.archetype.ArchetypeId, long)
     */
    public List<AuditRecord> getByObjectId(String archetypeId, long id) {
        try {
            return dao.getByObjectId(archetypeId, id);
        } catch (Exception exception) {
            throw new AuditServiceException(
                    AuditServiceException.ErrorCode.FailedToGetByObjectId,
                    new Object[] {archetypeId, id}, exception);
        }
    }
    
    /**
     * This method is called after the business logic of the intercepted
     * message
     * 
     * @param invocation
     *            the intercepted method.           
     */
    private void afterMethodInvocation(MethodInvocation invocation) {
        if (invocation.getArguments()[0] instanceof IMObject) {
            IMObject imObject = (IMObject)invocation.getArguments()[0];
            try {
                AuditRecord audit = new AuditRecord();
                audit.setArchetypeId(imObject.getArchetypeIdAsString());
                audit.setObjectId(imObject.getId());
                audit.setOperation(invocation.getMethod().getName());
                audit.setService(invocation.getThis().getClass().getName());
                audit.setUser("unknown");
                audit.setTimeStamp(new Date());
                
                // insert the object
                dao.insert(audit);
            } catch (Exception exception) {
                logger.error("Error in AuditService.afterMethodInvocation " +
                        exception);
            }
        }
    }
        
}
