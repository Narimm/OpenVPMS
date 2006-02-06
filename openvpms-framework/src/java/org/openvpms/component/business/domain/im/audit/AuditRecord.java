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


package org.openvpms.component.business.domain.im.audit;

// java core
import java.io.Serializable;
import java.util.Date;

// commons-lang
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * The class captures audit related information includign the archetypeId,
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class AuditRecord implements Serializable {

    /**
     * Defaulr SUID
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Indicates the version of this object
     */
    private long version;
    
    /**
     * Uniquely identifies an instance of this class. This is the identifier
     * that is used for persistence.
     */
    private long uid = -1;
    
    /**
     * The archetype id
     */
    private String archetypeId;
    
    /**
     * The object id
     */
    private long objectId;
    
    /**
     * The date time stamp
     */
    private Date timeStamp;
    
    /**
     * The service that was called
     */
    private String service;
    
    /**
     * The operation that was called
     */
    private String operation;
    
    /**
     * The user that made the call
     */
    private String user;

    
    /**
     * Default constructor
     */
    public AuditRecord() {
        // no-op
    }

    /**
     * @return Returns the version.
     */
    public long getVersion() {
        return version;
    }

    /**
     * @param version The version to set.
     */
    public void setVersion(long version) { 
        this.version = version;
    }

    /**
     * @return Returns the id.
     */
    public long getUid() {
        return this.uid;
    }

    /**
     * @param id The id to set.
     */
    public void setUid(long id) {
        this.uid = id;
    }

    /**
     * @return Returns the archetypeId.
     */
    public String getArchetypeId() {
        return archetypeId;
    }

    /**
     * @param archetypeId The archetypeId to set.
     */
    public void setArchetypeId(String archetypeId) {
        this.archetypeId = archetypeId;
    }

    /**
     * @return Returns the objectId.
     */
    public long getObjectId() {
        return objectId;
    }

    /**
     * @param objectId The objectId to set.
     */
    public void setObjectId(long objectId) {
        this.objectId = objectId;
    }

    /**
     * @return Returns the operation.
     */
    public String getOperation() {
        return operation;
    }

    /**
     * @param operation The operation to set.
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }

    /**
     * @return Returns the service.
     */
    public String getService() {
        return service;
    }

    /**
     * @param service The service to set.
     */
    public void setService(String service) {
        this.service = service;
    }

    /**
     * @return Returns the timeStamp.
     */
    public Date getTimeStamp() {
        return timeStamp;
    }

    /**
     * @param timeStamp The timeStamp to set.
     */
    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    /**
     * @return Returns the user.
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user The user to set.
     */
    public void setUser(String user) {
        this.user = user;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#toString()
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, 
                ToStringStyle.MULTI_LINE_STYLE);
    }
}
