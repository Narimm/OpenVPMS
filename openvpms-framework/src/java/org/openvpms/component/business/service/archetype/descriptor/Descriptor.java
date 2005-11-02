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


package org.openvpms.component.business.service.archetype.descriptor;

// java core
import java.io.Serializable;

// commons-lang
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

// commons-id
import org.safehaus.uuid.UUIDGenerator;

// log4j
import org.apache.log4j.Logger;

// openvpms-framework
import org.openvpms.component.system.service.uuid.JUGGenerator;

/**
 * All the descriptor classes inherit from this base class, which provides
 * support for identity, hibernate and serialization
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public abstract class Descriptor implements Serializable {

    /**
     * SUID.
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * An internal UUID generator
     */
    @SuppressWarnings("unused")
    private static JUGGenerator generator = new JUGGenerator(
            UUIDGenerator.getInstance().getDummyAddress().toString());
    
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(Descriptor.class);

    /**
     * Indicates the version of this object
     */
    private long version;
    
    /**
     * This is the link id for the object, which is used to associated one 
     * descriptor with another in hibernate. This is required to support cascade
     * save/updates and to work reliable in detached mode and to allow the 
     * a call to saveOrUpdate to be made. This is not be confused with the 
     * object id, which is set by the mapping
     */
    private String linkId;
    
    /**
     * Uniquely identifies an instance of this class. This is the identifier
     * that is used for persistence.
     */
    private long uid = -1;
    
    
    /**
     * Default constructor
     */
    public Descriptor() {
        this.linkId = generator.nextId();
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
     * @return Returns the linkId.
     */
    public String getLinkId() {
        return linkId;
    }

    /**
     * @param linkId The linkId to set.
     */
    @SuppressWarnings("unused")
    private void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        Descriptor rhs = (Descriptor)obj;
        return new EqualsBuilder()
            .append(linkId, rhs.linkId)
            .isEquals();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(linkId)    
            .toHashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("uid", uid)
            .append("linkId", linkId)
            .toString();
    }
}
