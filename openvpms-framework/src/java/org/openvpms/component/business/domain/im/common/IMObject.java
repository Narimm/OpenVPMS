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

package org.openvpms.component.business.domain.im.common;

// java core
import java.io.Serializable;

// openvpms-framework
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.system.service.uuid.JUGGenerator;
import org.safehaus.uuid.UUIDGenerator;

/**
 * This is the base class for information model objects. An {@link IMObject} 
 * object is very generic and is constrained at runtime by applying constriants
 * on the object. These constraints are the foundation of archetypes and 
 * archetype languages such as ADL
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class IMObject implements Serializable {

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
    private static final Logger logger = Logger.getLogger(IMObject.class);

    /**
     * Indicates the version of this object
     */
    private long version;
    
    /**
     * This is the link id for the object, which is used to associated one 
     * IMObject with another in hibernate. This is required to support cascade
     * save/updates and to work reliable in detached mode and to allow the 
     * a call to saveOrUpdate to be made. This is not be confused with the 
     * object id, which  is set by the mapping
     */
    private String linkId;
    
    
    /**
     * Uniquely identifies an instance of this class. This is the identifier
     * that is used for persistence.
     */
    private long uid = -1;
    
    /**
     * The archetype that is attached to this object
     */
    private ArchetypeId archetypeId;
    
    /**
     * Default constructor
     */
    public IMObject() {
        this.linkId = generator.nextId();
    }

    /**
     * Construct an instance of an info model object given the specified 
     * data.
     * 
     * @param archetypeId
     *            the archetype id.
     */
    public IMObject(ArchetypeId archetypeId) {
        this();
        this.archetypeId = archetypeId;
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
     * @return Returns the archetypeId.
     */
    public ArchetypeId getArchetypeId() {
        return archetypeId;
    }

    /**
     * @param archetypeId The archetypeId to set.
     */
    public void setArchetypeId(ArchetypeId archetypeId) {
        this.archetypeId = archetypeId;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        IMObject rhs = (IMObject)obj;
        return new EqualsBuilder()
            .append(uid, rhs.uid)
            .append(archetypeId, rhs.archetypeId)
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
            .append("archetypeId", archetypeId)
            .append("linkId", linkId)
            .toString();
    }
    
    /**
     * Return true if this is a new object and false otherwise. A new object
     * is one that has been created but not yet persisted
     * 
     * @return boolean
     */
    public boolean isNew() {
        return uid == -1;
    }

    /**
     * This method will retrieve an attribute of this object
     * give an xpath
     * 
     * @param path
     *            an xpath expression in to this object
     * @return Object
     */
    public Pointer pathToObject(String path) {
        return JXPathContext.newContext(this).getPointer(path);
    }
}

