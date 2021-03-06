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


/**
 * An assertion property extends {@link NamedProperty} and adds a type
 * and a value.
 *
 * @author Jim Alateras
 */
public class AssertionProperty extends NamedProperty
        implements org.openvpms.component.model.archetype.AssertionProperty {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The property type is a fully-qualified class name.
     */
    private String type = "java.lang.String";

    /**
     * The value of the property. All values are specified as strings
     */
    private String value;


    /**
     * Default constructor
     */
    public AssertionProperty() {
        setArchetypeId(new ArchetypeId("descriptor.assertionProperty.1.0"));
    }

    /**
     * @return Returns the type.
     */
    public String getType() {
        return type;
    }

    /**
     * @param type The type to set.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return Returns the value.
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value The value to set.
     */
    public void setValue(String value) {
        this.value = value;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.datatypes.property.NamedProperty#setValue(java.lang.Object)
     */
    @Override
    public void setValue(Object value) {
        setValue((String) value);
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.archetype.descriptor.NamedProperty#toString()
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                                                  ToStringStyle.MULTI_LINE_STYLE);
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.datatypes.property.NamedProperty#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        AssertionProperty copy = (AssertionProperty) super.clone();
        copy.type = this.type;
        copy.value = this.value;

        return copy;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof AssertionProperty) {
            return ObjectUtils.equals(getName(), ((AssertionProperty) obj).getName())
                   && ObjectUtils.equals(value, ((AssertionProperty) obj).value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getName())
                .append(value)
                .toHashCode();
    }

}
