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


package org.openvpms.component.business.domain.im.archetype.descriptor;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.property.NamedProperty;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyMap;


/**
 * An <tt>AssertionDescriptor</tt> describes an assertion on a {@link NodeDescriptor}, and is used to configure the
 * behaviour of its associated {@link AssertionTypeDescriptor}.
 * <br/>
 * By convention, the <tt>AssertionTypeDescriptor</tt> should have the same name as the <tt>AssertionDescriptor</tt> -
 * this is used to establish the association.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class AssertionDescriptor extends Descriptor {

    /**
     * Serialization version ID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The associated error message. This is used when the assertion fails
     */
    private String errorMessage;

    /**
     * The index of this assertion descriptor.
     */
    private int index;

    /**
     * Holds the properties that are required to evaluate the assertion. All
     * properties are in the form of key value pairs but in some instances it
     * may only be necessary to specify the value.
     */
    private PropertyMap propertyMap = new PropertyMap("root");

    /**
     * The assertion type descriptor that this configures. By convention, this should have the same name as the
     * assertion descriptor.
     */
    private transient AssertionTypeDescriptor descriptor;


    /**
     * Default constructor.
     */
    public AssertionDescriptor() {
        setArchetypeId(new ArchetypeId("descriptor.assertion.1.0"));
    }

    /**
     * Returns the error message.
     *
     * @return the error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the error message.
     *
     * @param errorMessage the error message
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Return the properties as a map.
     *
     * @return the properties
     */
    public PropertyMap getPropertyMap() {
        return propertyMap;
    }

    /**
     * Sets the properties.
     *
     * @param propertyMap the properties to set
     */
    public void setPropertyMap(PropertyMap propertyMap) {
        this.propertyMap = propertyMap;
    }

    /**
     * Adds a property.
     *
     * @param property the property to add
     */
    public void addProperty(NamedProperty property) {
        propertyMap.getProperties().put(property.getName(), property);
    }

    /**
     * Removes the specified property.
     *
     * @param property the property to remove
     */
    public void removeProperty(NamedProperty property) {
        propertyMap.getProperties().remove(property.getName());
    }

    /**
     * Removes the property with the specified name.
     *
     * @param name the property name
     */
    public void removeProperty(String name) {
        propertyMap.getProperties().remove(name);
    }

    /**
     * Retrieves the property descriptor with the specified name.
     *
     * @param name the property name
     * @return the named property, or <tt>null</tt> if none is found
     */
    public NamedProperty getProperty(String name) {
        return propertyMap.getProperties().get(name);
    }

    /**
     * Returns the properties.
     *
     * @return the properties
     */
    public NamedProperty[] getPropertiesAsArray() {
        return propertyMap.getProperties().values().toArray(
                new NamedProperty[propertyMap.getProperties().size()]);
    }

    /**
     * Sets the properties.
     *
     * @param properties the properties
     */
    public void setPropertiesAsArray(NamedProperty[] properties) {
        this.propertyMap = new PropertyMap("root");
        for (NamedProperty property : properties) {
            this.propertyMap.getProperties().put(property.getName(), property);
        }
    }

    /**
     * Returns the assertion index, used to order assertions in a collection.
     *
     * @return the assertion index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets the assertion index, used to order assertions in a collection.
     *
     * @param index the assertion index
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#toString()
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, STYLE);
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.archetype.descriptor.Descriptor#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        AssertionDescriptor copy = (AssertionDescriptor) super.clone();
        if (propertyMap != null) {
            copy.propertyMap = (PropertyMap) propertyMap.clone();
        }
        return copy;
    }

    /**
     * Returns the assertion type descriptor.
     *
     * @return the assertion type descriptor. May be <tt>null</tt>
     */
    public AssertionTypeDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Sets the assertion type descriptor.
     *
     * @param descriptor the assertion type descriptor. May be <tt>null</tt>
     */
    public void setDescriptor(AssertionTypeDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    /**
     * Evaluates the create action type of the associated assertion type descriptor, if one is defined.
     *
     * @param target the value to validate
     * @param node   the node descriptor
     * @throws AssertionException if the assertion cannot be evaluated
     */
    public void create(Object target, NodeDescriptor node) {
        if (descriptor == null) {
            throw new AssertionException(AssertionException.ErrorCode.NoAssertionTypeSpecified,
                                         new Object[]{this.getName(), node.getName()});
        }
        descriptor.create(target, node, this);
    }

    /**
     * Evaluates the validation action type of the associated assertion type descriptor, if one is defined.
     *
     * @param value  the value to validate
     * @param parent the parent object
     * @param node   the node descriptor
     * @return <tt>true</tt> if the value is valid, otherwise <tt>false</tt>
     * @throws AssertionException if the assertion cannot be evaluated
     */
    public boolean validate(Object value, IMObject parent, NodeDescriptor node) {
        if (descriptor == null) {
            throw new AssertionException(AssertionException.ErrorCode.NoAssertionTypeSpecified,
                                         new Object[]{this.getName(), node.getName()});
        }
        return descriptor.validate(value, parent, node, this);
    }

    /**
     * Evaluates the set action type of the associated assertion type descriptor, if one is defined.
     *
     * @param value  the value to set. May be <tt>null</tt>
     * @param parent the parent object
     * @param node   the node descriptor
     * @return the (possibly modified) value to set. May be <tt>null</tt>
     * @throws AssertionException if the assertion cannot be evaluated
     */
    public Object set(Object value, IMObject parent, NodeDescriptor node) {
        if (descriptor == null) {
            throw new AssertionException(AssertionException.ErrorCode.NoAssertionTypeSpecified,
                                         new Object[]{this.getName(), node.getName()});
        }
        return descriptor.set(value, parent, node, this);
    }

}
