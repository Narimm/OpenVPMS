package org.openvpms.component.business.dao.hibernate.im.common;

import org.apache.commons.lang.builder.StandardToStringStyle;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.datatypes.basic.TypedValue;
import org.openvpms.component.business.domain.im.datatypes.basic.TypedValueMap;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class IMObjectDOImpl implements IMObjectDO {

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
     * This is the name that this entity is known by. Each concrete instance
     * must supply this.
     */
    private String name;

    /**
     * The identifier assigned by the persistence layer when the object is
     * saved.
     */
    private long id = -1;

    /**
     * A client assigned identifier.
     */
    private String linkId;

    /**
     * Indicates the version of this object
     */
    private long version;

    /**
     * Holds details about the entity identity.
     */
    private Map<String, TypedValue> details = new HashMap<String, TypedValue>();
    private IMObjectReference reference;


    /**
     * toString() style.
     */
    protected static final StandardToStringStyle STYLE;

    static {
        STYLE = new StandardToStringStyle();
        STYLE.setUseShortClassName(true);
        STYLE.setUseIdentityHashCode(false);
    }


    /**
     * Default constructor.
     */
    public IMObjectDOImpl() {
    }

    /**
     * Creates a new <tt>IMObjectDO</tt>.
     *
     * @param archetypeId the archetype id
     */
    public IMObjectDOImpl(ArchetypeId archetypeId) {
        setArchetypeId(archetypeId);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IMObjectDO)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        IMObjectDO rhs = (IMObjectDO) obj;
        return getObjectReference().equals(rhs.getObjectReference());
    }

    /**
     * @return Returns the archetypeId.
     */
    public ArchetypeId getArchetypeId() {
        return archetypeId;
    }

    /**
     * Create and return an {@link IMObjectReference} for this object
     *
     * @return IMObjectReference
     */
    public IMObjectReference getObjectReference() {
        if (reference == null) {
            reference = new IMObjectReference(getArchetypeId(), getId(),
                                              getLinkId());
        }
        return reference;
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
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return Returns the id.
     */
    public long getId() {
        return id;
    }

    /**
     * @param id The id to set.
     */
    public void setId(long id) {
        this.id = id;
        reference = null;
    }

    public void setLinkId(String linkId) {
        this.linkId = linkId;
        reference = null;
    }

    public String getLinkId() {
        if (linkId == null && id == -1) {
            linkId = UUID.randomUUID().toString();
        }
        return linkId;
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
        reference = null;
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
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the details.
     */
    public Map<String, Object> getDetails() {
        return new TypedValueMap(details);
    }

    /**
     * @param details The details to set.
     */
    public void setDetails(Map<String, Object> details) {
        this.details = TypedValueMap.create(details);
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
                .append("linkId", linkId)
                .append("archetypeId", archetypeId)
                .append("version", version)
                .toString();
    }
}

