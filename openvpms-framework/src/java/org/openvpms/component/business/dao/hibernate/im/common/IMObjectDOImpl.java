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


/**
 * Implementation of the {@link IMObjectDO} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IMObjectDOImpl implements IMObjectDO {

    /**
     * The archetype identifier.
     */
    private ArchetypeId archetypeId;

    /**
     * The object identifier assigned by the persistence layer when the
     * object is saved. If <tt>-1</tt>, indicates that the object is not
     * persistent.
     */
    private long id = -1;

    /**
     * The object link identifier, a UUID used to link objects until they can
     * be made persistent, and to provide support for object equality.
     */
    private String linkId;

    /**
     * The object reference.
     */
    private IMObjectReference reference;

    /**
     * The persistent version of the object.
     */
    private long version;

    /**
     * The object name.
     */
    private String name;

    /**
     * The object description.
     */
    private String description;

    /**
     * The timestamp when the object was last modified.
     */
    private Date lastModified;

    /**
     * Indicates whether the object is active or not.
     */
    private boolean active = true;


    /**
     * Name value pairs representing the dynamic object details.
     */
    private Map<String, TypedValue> details = new HashMap<String, TypedValue>();


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
     * Creates a new <tt>IMObjectDOImpl</tt>.
     *
     * @param archetypeId the archetype identifier
     */
    public IMObjectDOImpl(ArchetypeId archetypeId) {
        setArchetypeId(archetypeId);
    }

    /**
     * Returns the archetype identifier.
     *
     * @return the archetype identifier
     */
    public ArchetypeId getArchetypeId() {
        return archetypeId;
    }

    /**
     * Sets the archetype identifier.
     *
     * @param archetypeId the archetype identifier
     */
    public void setArchetypeId(ArchetypeId archetypeId) {
        this.archetypeId = archetypeId;
        reference = null;
    }

    /**
     * Returns the object identifier.
     * <p/>
     * This is assigned when the object is made persistent.
     *
     * @return the object identifer, or <tt>-1</tt> if the object is not
     *         persistent
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the object identifier.
     *
     * @param id the object identifier. If <tt>-1</tt>, indicates that the
     *           object is not persistent
     */
    public void setId(long id) {
        this.id = id;
        reference = null;
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
        if (linkId == null && id == -1) {
            linkId = UUID.randomUUID().toString();
        }
        return linkId;
    }

    /**
     * Sets the object link identifier.
     *
     * @param linkId the link identifier
     */
    public void setLinkId(String linkId) {
        this.linkId = linkId;
        reference = null;
    }

    /**
     * Returns an object reference for this object.
     *
     * @return the object reference
     */
    public IMObjectReference getObjectReference() {
        if (reference == null) {
            reference = new IMObjectReference(getArchetypeId(), getId(),
                                              getLinkId());
        }
        return reference;
    }

    /**
     * Returns the object version.
     * <p/>
     * This is the persistent version of the object in the database, and is
     * incremented each time the object is committed. It is used to prevent
     * concurrent modification.
     *
     * @return returns the version
     */
    public long getVersion() {
        return version;
    }

    /**
     * Sets the object version.
     *
     * @param version the version
     */
    public void setVersion(long version) {
        this.version = version;
    }

    /**
     * Returns the object name.
     *
     * @return the name. May be <tt>null</tt>
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the object name.
     *
     * @param name the object name. May be <tt>null</tt>
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the object description.
     *
     * @return the description. May be <tt>null</tt>
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the object description.
     *
     * @param description The description. May be <tt>null</tt>
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the timestamp when the object was last modified.
     *
     * @return the last modified timestamp. May be <tt>null</tt>
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * Sets the timestamp when the object was last modified.
     *
     * @param lastModified the last modified timestamp
     */
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Determines if the object is active.
     *
     * @return <tt>true</tt> if the object is active, <tt>false</tt> if it
     *         is inactive
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Determines if the object is active.
     *
     * @param active if <tt>true</tt>, the object is active, otherwise it is
     *               inactive
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Determines if the object is new. A new object is one that has not
     * been made persistent.
     *
     * @return <tt>true</tt> if the object is new, <tt>false</tt> if it has
     *         been made persistent
     */
    public boolean isNew() {
        return id == -1;
    }

    /**
     * Returns a map of named objects, used to represent the dynamic details
     * of this.
     *
     * @return the details
     */
    public Map<String, Object> getDetails() {
        return new TypedValueMap(details);
    }

    /**
     * Sets a map of named objects, used to represent the dynamic details
     * of the this.
     *
     * @param details the details to set
     */
    public void setDetails(Map<String, Object> details) {
        this.details = TypedValueMap.create(details);
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     * @return <tt>true</tt> if this object is the same as the obj
     *         argument; <tt>false</tt> otherwise
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
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        return getLinkId().hashCode();
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
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

