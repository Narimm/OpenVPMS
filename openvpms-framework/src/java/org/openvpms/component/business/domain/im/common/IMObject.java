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
import java.util.Date;
import java.util.Map;

// commons-jxpath
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;

// commons-lang
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

// log4j
import org.apache.log4j.Logger;

// commons-id
import org.safehaus.uuid.UUIDGenerator;

//openvpms-framework
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.system.service.uuid.JUGGenerator;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyCollection;

/**
 * This is the base class for information model objects. An {@link IMObject} 
 * object is very generic and is constrained at runtime by applying constriants
 * on the object. These constraints are the foundation of archetypes and 
 * archetype languages such as ADL
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class IMObject implements Serializable, Cloneable {

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
     * SUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Indicates whether this object is active
     */
    private boolean active = true;
    
    /**
     * The archetype that is attached to this object. which defines
     * 
     */
    private ArchetypeId archetypeId;
    
    /**
     * Description of this entity
     */
    private String description;
    
    /**
     * This is the date and time that this object was last modified
     */
    private Date lastModified;
    
    /**
     * This is the link id for the object, which is used to associated one 
     * IMObject with another in hibernate. This is required to support cascade
     * save/updates and to work reliable in detached mode and to allow the 
     * a call to saveOrUpdate to be made. This is not be confused with the 
     * object id, which  is set by the mapping
     */
    private String linkId;
    
    /**
     * This is the name that this entity is known by. Each concrete instance 
     * must supply this.
     */
    private String name;
    
    /**
     * Uniquely identifies an instance of this class. This is the identifier
     * that is used for persistence.
     */
    private long uid = -1;
    
    /**
     * Indicates the version of this object
     */
    private long version;
    
    
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
        if (this.archetypeId != null)
            this.description = archetypeId.getConcept();
    }

    /**
     * Construct an instance of an info model object given the specified 
     * data.
     * 
     * @param archetypeId
     *            the archetype id.
     * @param name
     *            the name of the object
     * @param description
     *            the description for this object                        
     */
    public IMObject(ArchetypeId archetypeId, String name, String description) {
        this(archetypeId);
        this.name = name;
        this.description = description;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        IMObject copy = (IMObject)super.clone();
        
        copy.active = this.active;
        copy.archetypeId = (ArchetypeId)(this.archetypeId == null ? 
                null : this.archetypeId.clone());
        copy.description = this.description; 
        copy.lastModified = (Date)(this.lastModified == null ? 
                null :this.lastModified.clone());
        copy.linkId = this.linkId;
        copy.name = this.name;
        copy.uid = this.uid;
        copy.version = this.version;
        
        return copy;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        IMObject rhs = (IMObject)obj;
        return new EqualsBuilder()
            .append(linkId, rhs.linkId)
            .append(archetypeId, rhs.archetypeId)
            .isEquals();
    }

    /**
     * @return Returns the archetypeId.
     */
    public ArchetypeId getArchetypeId() {
        return archetypeId;
    }

    /**
     * Return the archetypeId as a string
     * 
     * @return String
     *            the fully qualified archetype id
     */
    public String getArchetypeIdAsString() {
        return (this.archetypeId == null) ? null : archetypeId.getQName();
    }
    
    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return Returns the lastModified.
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * @return Returns the linkId.
     */
    public String getLinkId() {
        return linkId;
    }
    
    /**
     * Return the object reference for this object. 
     * 
     * @return IMObjectReference
     * @throws 
     */

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return Returns the id.
     */
    public long getUid() {
        return this.uid;
    }

    /**
     * @return Returns the version.
     */
    public long getVersion() {
        return version;
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

    /**
     * @return Returns the active.
     */
    public boolean isActive() {
        return active;
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
     * This method will retrieve a reference to a value collection. If the 
     * returned object is already an instance of a collection then it will
     * return it as is. If the returned object is an instance of a Map then
     * it will return the value objects
     * 
     * @param path
     *            a xpath expression in to this object
     * @return Pointer   
     *            a pointer to the location         
     */
    public Pointer pathToCollection(String path) {
        Pointer ptr = pathToObject(path);
        if (ptr != null) {
            Object obj  = ptr.getValue();
            if (obj instanceof Map) {
                ptr = JXPathContext.newContext(obj).getPointer("values(.)");
            } else if (obj instanceof PropertyCollection){
                ptr = JXPathContext.newContext(obj).getPointer("values(.)");
            }
        }
        
        return ptr;
    }

    /**
     * This method will retrieve an attribute of this object
     * give an xpath
     * 
     * @param path
     *            an xpath expression in to this object
     * @return Pointer
     *            a pointer ot the object or null.
     */
    public Pointer pathToObject(String path) {
        Pointer ptr = null;
        
        try {
            ptr = JXPathContext.newContext(this).getPointer(path);
        } catch (Exception exception) {
            logger.warn("No path to: " + path + " for object of type: " 
                    + this.getClass().getName(), exception);
        }
        
        return ptr;
    }
    
    /**
     * @param active The active to set.
     */
    public void setActive(boolean active) {
        this.active = active;
    }
    /**
     * @param archetypeId The archetypeId to set.
     */
    public void setArchetypeId(ArchetypeId archetypeId) {
        this.archetypeId = archetypeId;
    }

    /**
     * Set the archetypeId from a string
     * 
     * @param archId
     *            the fully qualified archetype name
     */
    public void setArchetypeIdAsString(String archId) {
        this.archetypeId = new ArchetypeId(archId);
    }

    /**
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @param lastModified The lastModified to set.
     */
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * @param linkId The linkId to set.
     */
    @SuppressWarnings("unused")
    private void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @param id The id to set.
     */
    public void setUid(long id) {
        this.uid = id;
    }

    /**
     * @param version The version to set.
     */
    public void setVersion(long version) { 
        this.version = version;
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
}

