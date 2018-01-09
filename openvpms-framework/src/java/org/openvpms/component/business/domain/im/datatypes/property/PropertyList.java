/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */


package org.openvpms.component.business.domain.im.datatypes.property;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openvpms.component.business.domain.archetype.ArchetypeId;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;


/**
 * A property list extends {@link NamedProperty} and implements the {@link PropertyCollection} interface.
 *
 * @author Jim Alateras
 */
public class PropertyList extends NamedProperty implements org.openvpms.component.model.archetype.PropertyList,
        PropertyCollection {

    /**
     * The property type is a a fully qualified archetype id.
     */
    private Set<org.openvpms.component.model.archetype.NamedProperty> properties = new LinkedHashSet<>();

    /**
     * Default SUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor
     */
    public PropertyList() {
        setArchetypeId(new ArchetypeId("descriptor.propertyList.1.0"));
    }

    /**
     * @return Returns the properties.
     */
    public Set<org.openvpms.component.model.archetype.NamedProperty> getProperties() {
        return properties;
    }

    /**
     * @param properties The properties to set.
     */
    public void setProperties(Set<org.openvpms.component.model.archetype.NamedProperty> properties) {
        this.properties = properties;
    }

    /**
     * Add the specified property to the list
     *
     * @param property the property to add
     */
    public void addProperty(org.openvpms.component.model.archetype.NamedProperty property) {
        properties.add(property);
    }

    /**
     * Remove the specified property from the list
     *
     * @param property the property to remove
     */
    public void removeProperty(org.openvpms.component.model.archetype.NamedProperty property) {
        properties.remove(property);
    }

    /**
     * @return Returns the properties.
     */
    public NamedProperty[] getPropertiesAsArray() {
        return properties.toArray(new NamedProperty[properties.size()]);
    }

    /**
     * <br/>
     * NOTE: this is used by castor serialisation.
     *
     * @param properties The properties to set.
     */
    public void setPropertiesAsArray(NamedProperty[] properties) {
        this.properties = new LinkedHashSet<>();
        Collections.addAll(this.properties, properties);
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.datatypes.property.PropertyCollection#values()
     */
    @Override
    public Collection values() {
        return properties;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.archetype.descriptor.NamedProperty#getValue()
     */
    @Override
    public Object getValue() {
        return properties;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.datatypes.property.NamedProperty#setValue(java.lang.Object)
     */
    @Override
    @SuppressWarnings("unchecked")
    public void setValue(Object value) {
        properties = (Set<org.openvpms.component.model.archetype.NamedProperty>) value;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.archetype.descriptor.NamedProperty#toString()
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.datatypes.property.NamedProperty#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        PropertyList copy = (PropertyList) super.clone();
        copy.properties = new LinkedHashSet<>(this.properties);
        return copy;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof PropertyList) {
            return ObjectUtils.equals(getName(), ((PropertyList) obj).getName())
                   && properties.equals(((PropertyList) obj).properties);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getName())
                .append(properties)
                .toHashCode();
    }

}
