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

import org.apache.commons.jxpath.Pointer;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.StandardToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyCollection;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.component.system.service.uuid.JUGGenerator;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * This is the base class for information model objects. An {@link IMObject}
 * object is very generic and is constrained at runtime by applying constriants
 * on the object. These constraints are the foundation of archetypes and
 * archetype languages such as ADL
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class IMObject implements Serializable, Cloneable {

    /**
     * Serialization version identifier.
     */
    private static final long serialVersionUID = 2L;

    /**
     * Identifier assigned when the object is saved.
     */
    private long id = -1;

    /**
     * Indicates whether this object is active
     */
    private boolean active = true;

    /**
     * The archetype that is attached to this object. which defines
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
     * The object link identifier, a UUID used to link objects until they can
     * be made persistent, and to provide support for object equality.
     */
    private String linkId;

    /**
     * This is the name that this entity is known by. Each concrete instance
     * must supply this.
     */
    private String name;

    /**
     * Indicates the version of this object.
     */
    private long version;

    /**
     * Dynamic details of the object.
     */
    private Map<String, Object> details
            = new HashMap<String, Object>();

    /**
     * toString() style.
     */
    protected static final StandardToStringStyle STYLE;

    /**
     * An internal UUID generator.
     */
    private static final JUGGenerator generator = new JUGGenerator();

    /**
     * Define a logger for this class
     */
    private static final Log log = LogFactory.getLog(IMObject.class);


    static {
        STYLE = new StandardToStringStyle();
        STYLE.setUseShortClassName(true);
        STYLE.setUseIdentityHashCode(false);
    }


    /**
     * Default constructor.
     */
    public IMObject() {
    }

    /**
     * Creates a new <tt>IMObject</tt>.
     *
     * @param archetypeId the archetype id
     */
    public IMObject(ArchetypeId archetypeId) {
        this.archetypeId = archetypeId;
        if (this.archetypeId != null)
            this.description = archetypeId.getConcept();
    }

    /**
     * Creates a new <tt>IMObject</tt>.
     *
     * @param archetypeId the archetype id.
     * @param name        the name of the object
     * @param description the description for this object
     */
    public IMObject(ArchetypeId archetypeId, String name, String description) {
        this(archetypeId);
        this.name = name;
        this.description = description;
    }

    /**
     * Returns the object's persistent identifier.
     *
     * @return the object identifier, or <tt>-1</tt> if the object has not been
     *         saved
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the object persistent identifier.
     *
     * @param id the object identifier
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return Returns the id.
     * @deprecated use {@link #getId).
     */
    @Deprecated
    public long getUid() {
        return getId();
    }

    /**
     * @param id The id to set.
     * @deprecated use {@link #setId(long)} 
     */
    @Deprecated
    public void setUid(long id) {
        setId(id);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        IMObject copy = (IMObject) super.clone();
        copy.linkId = getLinkId();
        return copy;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof IMObject) {
            IMObject rhs = (IMObject) obj;
            return getObjectReference().equals(rhs.getObjectReference());
        }
        return false;
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
     *         the fully qualified archetype id
     */
    public String getArchetypeIdAsString() {
        return (this.archetypeId == null) ? null
                : archetypeId.getQualifiedName();
    }

    /**
     * Create and return an {@link IMObjectReference} for this object
     *
     * @return IMObjectReference
     */
    public IMObjectReference getObjectReference() {
        return new IMObjectReference(this);
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
     * Returns the object link identifier.
     * <p/>
     * This is a UUID that is used to link objects until they can be made
     * persistent, and to provide support for object equality.
     *
     * @return the link identifier
     */
    public String getLinkId() {
        if (linkId == null) {
            linkId = generator.nextId();
        }
        return linkId;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
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
        return getLinkId().hashCode();
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
        return id == -1;
    }

    /**
     * Returns the details.
     *
     * @return the details
     */
    public Map<String, Object> getDetails() {
        return details;
    }

    /**
     * Sets the details.
     *
     * @param details the details
     */
    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    /**
     * This method will retrieve a reference to a value collection. If the
     * returned object is already an instance of a collection then it will
     * return it as is. If the returned object is an instance of a Map then
     * it will return the value objects
     *
     * @param path a xpath expression in to this object
     * @return Pointer
     *         a pointer to the location
     */
    @Deprecated
    public Pointer pathToCollection(String path) {
        Pointer ptr = pathToObject(path);
        if (ptr != null) {
            Object obj = ptr.getValue();
            if (obj instanceof Map) {
                ptr = JXPathHelper.newContext(obj).getPointer("values(.)");
            } else if (obj instanceof PropertyCollection) {
                ptr = JXPathHelper.newContext(obj).getPointer("values(.)");
            }
        }

        return ptr;
    }

    /**
     * This method will retrieve an attribute of this object
     * give an xpath
     *
     * @param path an xpath expression in to this object
     * @return Pointer
     *         a pointer ot the object or null.
     */
    @Deprecated
    public Pointer pathToObject(String path) {
        Pointer ptr = null;

        try {
            ptr = JXPathHelper.newContext(this).getPointer(path);
        } catch (Exception exception) {
            log.warn("No path to: " + path + " for object of type: "
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
     * @param archId the fully qualified archetype name
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
    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
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
        return new ToStringBuilder(this, STYLE)
                .append("id", id)
                .append("archetypeId", archetypeId)
                .append("linkId", getLinkId())
                .append("version", version)
                .append("name", name)
                .toString();
    }
}

