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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.property;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;

import java.util.Collection;


/**
 * A {@link Property} that delegates to another.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class DelegatingProperty implements CollectionProperty {


    /**
     * The underlying property.
     */
    private final Property property;

    /**
     * Creates a new <tt>DelegatingProperty</tt>
     *
     * @param property the property to delegate to
     */
    public DelegatingProperty(Property property) {
        this.property = property;
    }

    /**
     * Add a value.
     *
     * @param value the value to add
     */
    public void add(Object value) {
        ((CollectionProperty) property).add(value);
    }

    /**
     * Remove a value.
     *
     * @param value the value to remove
     */
    public void remove(Object value) {
        ((CollectionProperty) property).remove(value);
    }

    /**
     * Returns the collection.
     *
     * @return the collection
     */
    public Collection getValues() {
        return ((CollectionProperty) property).getValues();
    }

    /**
     * Returns the no. of elements in the collection
     *
     * @return the no. of elements in the collection
     */
    public int size() {
        return ((CollectionProperty) property).size();
    }

    /**
     * Returns the minimum cardinality.
     *
     * @return the minimum cardinality
     */
    public int getMinCardinality() {
        return ((CollectionProperty) property).getMinCardinality();
    }

    /**
     * Returns the maximum cardinality.
     *
     * @return the maximum cardinality, or <tt>-1</tt> if it is unbounded
     */
    public int getMaxCardinality() {
        return ((CollectionProperty) property).getMaxCardinality();
    }

    /**
     * Determines the relationship of the elements of the collection to the
     * object.
     *
     * @return <tt>true</tt> if the objects are children of the parent object,
     *         or <tt>false</tt> if they are its peer
     */
    public boolean isParentChild() {
        return ((CollectionProperty) property).isParentChild();
    }

    /**
     * Returns the property name.
     *
     * @return the property name
     */
    public String getName() {
        return property.getName();
    }

    /**
     * Returns the property display name.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return property.getDisplayName();
    }

    /**
     * Returns the property description.
     *
     * @return the description. May be <tt>null</tt>
     */
    public String getDescription() {
        return property.getDescription();
    }

    /**
     * Sets the value of the property.
     * The value will only be set if it is valid, and different to the existing
     * value. If the value is set, any listeners will be notified.
     *
     * @param value the property value
     * @return <tt>true</tt> if the value was set, <tt>false</tt> if it
     *         cannot be set due to error, or is the same as the existing value
     */
    public boolean setValue(Object value) {
        return property.setValue(value);
    }

    /**
     * Returns the value of the property.
     *
     * @return the property value
     */
    public Object getValue() {
        return property.getValue();
    }

    /**
     * Returns the minimum length of the property.
     *
     * @return the minimum length
     */
    public int getMinLength() {
        return property.getMinLength();
    }

    /**
     * Returns the maximum length of the property.
     *
     * @return the maximum length
     */
    public int getMaxLength() {
        return property.getMaxLength();
    }

    /**
     * Returns the property type.
     *
     * @return the property type
     */
    public Class getType() {
        return property.getType();
    }

    /**
     * Determines if the property is a boolean.
     *
     * @return <tt>true</tt> if it is a boolean
     */
    public boolean isBoolean() {
        return property.isBoolean();
    }

    /**
     * Determines if the property is a string.
     *
     * @return <tt>true</tt> if it is a string
     */
    public boolean isString() {
        return property.isString();
    }

    /**
     * Determines if the property is numeric.
     *
     * @return <tt>true</tt> if it is numeric
     */
    public boolean isNumeric() {
        return property.isNumeric();
    }

    /**
     * Determines if the property is a date.
     *
     * @return <tt>true</tt> if it is a date
     */
    public boolean isDate() {
        return property.isDate();
    }

    /**
     * Determines if the property is a money property.
     *
     * @return <tt>true</tt> it is a money property
     */
    public boolean isMoney() {
        return property.isMoney();
    }

    /**
     * Determines if the property is an object reference.
     *
     * @return <tt>true</tt> if it is an object reference
     */
    public boolean isObjectReference() {
        return property.isObjectReference();
    }

    /**
     * Determines if the property is a lookup.
     *
     * @return <tt>true</tt> if it is a lookup
     */
    public boolean isLookup() {
        return property.isLookup();
    }

    /**
     * Determines if the property is a collection.
     *
     * @return <tt>true</tt> if it is a collection
     */
    public boolean isCollection() {
        return property.isCollection();
    }

    /**
     * Returns the archetype short names that this property may support.
     *
     * @return the archetype short names
     * @throws ArchetypeServiceException for any error
     */
    public String[] getArchetypeRange() {
        return property.getArchetypeRange();
    }

    /**
     * Determines if the property value is derived from an expression.
     *
     * @return <tt>true</tt> if the value is derived, otherwise <tt>false</tt>
     */
    public boolean isDerived() {
        return property.isDerived();
    }

    /**
     * Determines if the property is read-only.
     *
     * @return <tt>true</tt> if the property is read-only
     */
    public boolean isReadOnly() {
        return property.isReadOnly();
    }

    /**
     * Determines if the property is hidden.
     *
     * @return <tt>true</tt> if the property is hidden; otherwise <tt>false</tt>
     */
    public boolean isHidden() {
        return property.isHidden();
    }

    /**
     * Determines if the property is required.
     *
     * @return <tt>true</tt> if the property is required; otherwise
     *         <tt>false</tt>
     */
    public boolean isRequired() {
        return property.isRequired();
    }

    /**
     * Sets the property transformer.
     *
     * @param transformer the property transformer. May be <tt>null</tt>
     */
    public void setTransformer(PropertyTransformer transformer) {
        property.setTransformer(transformer);
    }

    /**
     * Returns the property transformer.
     *
     * @return the property transfoer. May be <tt>null</tt>
     */
    public PropertyTransformer getTransformer() {
        return property.getTransformer();
    }

    /**
     * Notify any listeners that they need to refresh and marks this modified.
     */
    public void refresh() {
        property.refresh();
    }

    /**
     * Returns the property descriptor.
     *
     * @return the property descriptor, or <tt>null</tt> if the property has
     *         no descriptor
     */
    public NodeDescriptor getDescriptor() {
        return property.getDescriptor();
    }

    /**
     * Determines if the object has been modified.
     *
     * @return <tt>true</tt> if the object has been modified
     */
    public boolean isModified() {
        return property.isModified();
    }

    /**
     * Clears the modified status of the object.
     */
    public void clearModified() {
        property.clearModified();
    }

    /**
     * Adds a listener to be notified when this changes.
     *
     * @param listener the listener to add
     */
    public void addModifiableListener(ModifiableListener listener) {
        property.addModifiableListener(listener);
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    public void removeModifiableListener(ModifiableListener listener) {
        property.removeModifiableListener(listener);
    }

    /**
     * Determines if the object is valid.
     *
     * @return <tt>true</tt> if the object is valid; otherwise
     *         <tt>false</tt>
     */
    public boolean isValid() {
        return property.isValid();
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return <tt>true</tt> if the object and its descendents are valid
     *         otherwise <tt>false</tt>
     */
    public boolean validate(Validator validator) {
        return property.validate(validator);
    }
}
