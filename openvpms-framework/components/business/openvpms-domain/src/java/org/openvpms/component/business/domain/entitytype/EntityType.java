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

package org.openvpms.component.business.domain.entitytype;

// java core
import java.io.Serializable;
import java.util.Set;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

// commons-lang
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

//openvpms-common-domain
import org.openvpms.component.business.domain.datatype.DtText;

/**
 * This is the base class of the AOM (adaptive object model) domain model.
 * 
 * TODO How do we validate that an EntityType has been correctly formed.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $Revision$
 */
public class EntityType implements Serializable {

    /**
     * The generated SUID.
     */
    private static final long serialVersionUID = 6879425920445795783L;

    /**
     * The namespace that an entity belongs too. The namespace qualifies the
     * entity name. An empty or null namespace implies the global namespace.
     */
    private DtText namespace;

    /**
     * Uniquely identifies the entity within the namespace.
     */
    private DtText name;

    /**
     * Briefly describes the entity.
     */
    private DtText description;

    /**
     * The version of the entity, which is used to support type evolution.
     */
    private DtText version;

    /**
     * This is the class name that is associated with this entity.
     */
    private DtText className;

    /**
     * Maintains a list of property types for this entity type.
     */
    private Map<DtText, PropertyType> propertyTypes;

    /**
     * The parent for this entity type. Can be null.
     * 
     */
    private EntityType parent;

    /**
     * The children for this entity type. Can be an empty set.
     * TODO Add support for children
     */
    //private Set<EntityType> children;

    /**
     * Default constructor.
     */
    public EntityType() {
        propertyTypes = new HashMap<DtText, PropertyType>();
    }

    /**
     * @return Returns the className.
     */
    public DtText getClassName() {
        return className;
    }

    /**
     * @param className
     *            The className to set.
     */
    public void setClassName(final DtText className) {
        this.className = className;
    }

    /**
     * @return Returns the description.
     */
    public DtText getDescription() {
        return description;
    }

    /**
     * @param description
     *            The description to set.
     */
    public void setDescription(final DtText description) {
        this.description = description;
    }

    /**
     * @return Returns the name.
     */
    public DtText getName() {
        return name;
    }

    /**
     * @param name
     *            The name to set.
     */
    public void setName(final DtText name) {
        this.name = name;
    }

    /**
     * @return Returns the namespace.
     */
    public DtText getNamespace() {
        return namespace;
    }

    /**
     * @param namespace
     *            The namespace to set.
     */
    public void setNamespace(final DtText namespace) {
        this.namespace = namespace;
    }

    /*
     * @return Returns the version.
     */
    public DtText getVersion() {
        return version;
    }

    /**
     * @param version
     *            The version to set.
     */
    public void setVersion(final DtText version) {
        this.version = version;
    }

    /**
     * Add the specified {@link PropertyType} to this entity type. If the
     * property type is invalid or the property type already exists then throw
     * an {@link EntityTypeException}.
     * <p>
     * The fully qualified property name is used as a key and therefore must be
     * globally unique.
     * 
     * @param pType
     *            the property type to add
     * @throws EntityTypeException
     *             if property is invalid or already exists
     */
    public void addProperty(PropertyType pType) throws EntityTypeException {
        // check to see that the property is valid
        if (!pType.isValid()) {
            throw new EntityTypeException(
                    EntityTypeException.ErrorCode.INVALID_PROPERTY_TYPE,
                    new String[] { pType.getName().getValue() }, null);
        }

        // check that the property does not already exists
        if (propertyTypes.containsKey(pType.getName())) {
            throw new EntityTypeException(
                    EntityTypeException.ErrorCode.PROPERTY_TYPE_ALREADY_EXISTS,
                    new String[] { pType.getName().getValue(),
                            this.getName().getValue() }, null);
        }

        propertyTypes.put(pType.getName(), pType);
    }

    /**
     * Remove the property specified by <code>name</code>. If a empty or null
     * property is passed in then fail silently.
     * 
     * @param pName
     *            the name of the property
     * @throws EntityTypeException
     *             if the property cannot be removed
     */
    public void removeProperty(DtText pName) throws EntityTypeException {
        // check that a valid property name has been specified
        if (StringUtils.isEmpty(pName.getValue())) {
            return;
        }

        propertyTypes.remove(pName);
    }

    /**
     * Return the name of all the {@link PropertyType} defined for this entity.
     * 
     * @return List<String> the property names
     */
    public Set<DtText> getPropertyNames() {
        return propertyTypes.keySet();
    }

    /**
     * Return all the {@link PropertyType} instances defined for this entity
     * 
     * @return Collection<PropertyType>
     */
    public Collection<PropertyType> getPropertyValues() {
        return propertyTypes.values();
    }

    /**
     * Return the {@link PropertyType} for the specified name or null if one
     * does not exist.
     * 
     * @param pName
     *            the name of the property
     * @return PropertyType
     */
    public PropertyType getProperty(DtText pName) {
        return propertyTypes.get(pName);
    }

    /**
     * Return the parent to this entity type.
     * 
     * @return EntityType 
     *              the parent or null if no parent
     */
    public EntityType getParent() {
        return parent;
    }

    /**
     * @param parent
     *            The parent to set.
     */
    public void setParent(EntityType parent) {
        this.parent = parent;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.MULTI_LINE_STYLE);
    }
}
