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
 *  $Id:$
 */

package org.openvpms.component.business.domain.entitytype;

// java-core
import java.io.Serializable;

// commons-lang
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

// openvpms-common-exception
import org.openvpms.component.business.domain.datatype.DtText;
import org.openvpms.component.business.domain.datatype.DtBoolean;

/**
 * This is the other class in the type-square relationship. A property type can
 * be used to extend the definition of an entity type at runtime. A property
 * type is {@link java.io.Serializable}.
 * <p>
 * A {@link PropertyType} cannot be versioned only the
 * {@link org.openvpms.component.business.domain.entitytype.EntityType} has
 * versioning capabilities. It is expected that the underlying type will
 * responsible for versioning and object evolution.
 * 
 * TODO How do we validate that a PropertyType has been correctly formed.
 * 
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $Revision$
 */
public class PropertyType implements Serializable {

    /**
     * Generated SUID.
     */
    private static final long serialVersionUID = -7788093434408365092L;

    /**
     * The namespace is used to qualify the property type name. The namespace
     * and the name uniquely identify the property type. If a namespace is not
     * provided or is null then the property type belongs to the global or
     * default namespace.
     */
    private DtText namespace;

    /**
     * The name of the property type. It is scoped within the namespace.
     */
    private DtText name;

    /**
     * A brief description of the property type.
     */
    private DtText description;

    /**
     * The fully-qualified class name, which represents the property type.
     */
    private DtText type;

    /**
     * Indicates whether this property is mandatory.
     * 
     * TODO Do we need to specifiy min and max cardinality
     */
    private DtBoolean mandatory;

    /**
     * A property <b>may</b> be associated with an attribute name, which may
     * also be different to the {@link #name} of the property type.
     */
    private DtText attributeName;

    /**
     * @return Returns the attributeName.
     */
    public DtText getAttributeName() {
        return attributeName;
    }

    /**
     * @param attributeName
     *            The attributeName to set.
     */
    public void setAttributeName(final DtText attributeName) {
        this.attributeName = attributeName;
    }

    /**
     * Checks whether there is an attribute name associated with this property
     * type.
     * 
     * @return DtBoolean true if attribute name has been specified
     */
    public boolean hasAttributeName() {
        return this.attributeName != null;
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
     * Indicates whether the property type is mandatory.
     * 
     * @return Returns the mandatory.
     */
    public DtBoolean isMandatory() {
        return mandatory;
    }

    /**
     * @param mandatory
     *            The mandatory to set.
     */
    public void setMandatory(final DtBoolean mandatory) {
        this.mandatory = mandatory;
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

    /**
     * @return Returns the type.
     */
    public DtText getType() {
        return type;
    }

    /**
     * @param type
     *            The type to set.
     */
    public void setType(final DtText type) {
        this.type = type;
    }

    /**
     * Check that this property type is valid. A valid property type must have
     * a non-null name and type)
     * 
     * TODO Define the validation strategy for OpenVPMS
     * 
     * @return boolean  true if valid
     */
    public boolean isValid() {
        return this.name.isEmpty() || 
               this.type.isEmpty();
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
