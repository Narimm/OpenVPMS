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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.property;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.exception.OpenVPMSException;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;


/**
 * A {@link Property} that delegates to another.
 *
 * @author Tim Anderson
 */
public abstract class DelegatingProperty implements CollectionProperty {


    /**
     * The underlying property.
     */
    private final Property property;

    /**
     * Constructs a {@code DelegatingProperty}
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
     * @return {@code true} if the value was removed
     */
    public boolean remove(Object value) {
        return ((CollectionProperty) property).remove(value);
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
     * @return the maximum cardinality, or {@code -1} if it is unbounded
     */
    public int getMaxCardinality() {
        return ((CollectionProperty) property).getMaxCardinality();
    }

    /**
     * Determines the relationship of the elements of the collection to the
     * object.
     *
     * @return {@code true} if the objects are children of the parent object,
     *         or {@code false} if they are its peer
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
     * @return the description. May be {@code null}
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
     * @return {@code true} if the value was set, {@code false} if it
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
     * @return the maximum length, or {@code -1} if it is unbounded
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
     * @return {@code true} if it is a boolean
     */
    public boolean isBoolean() {
        return property.isBoolean();
    }

    /**
     * Determines if the property is a string.
     *
     * @return {@code true} if it is a string
     */
    public boolean isString() {
        return property.isString();
    }

    /**
     * Determines if the property is numeric.
     *
     * @return {@code true} if it is numeric
     */
    public boolean isNumeric() {
        return property.isNumeric();
    }

    /**
     * Determines if the property is a date.
     *
     * @return {@code true} if it is a date
     */
    public boolean isDate() {
        return property.isDate();
    }

    /**
     * Determines if the property is a money property.
     *
     * @return {@code true} it is a money property
     */
    public boolean isMoney() {
        return property.isMoney();
    }

    /**
     * Determines if the property is an object reference.
     *
     * @return {@code true} if it is an object reference
     */
    public boolean isObjectReference() {
        return property.isObjectReference();
    }

    /**
     * Determines if the property is a lookup.
     *
     * @return {@code true} if it is a lookup
     */
    public boolean isLookup() {
        return property.isLookup();
    }

    /**
     * Determines if the property is a collection.
     *
     * @return {@code true} if it is a collection
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
     * @return {@code true} if the value is derived, otherwise {@code false}
     */
    public boolean isDerived() {
        return property.isDerived();
    }

    /**
     * Determines if the property is read-only.
     *
     * @return {@code true} if the property is read-only
     */
    public boolean isReadOnly() {
        return property.isReadOnly();
    }

    /**
     * Determines if the property is hidden.
     *
     * @return {@code true} if the property is hidden; otherwise {@code false}
     */
    public boolean isHidden() {
        return property.isHidden();
    }

    /**
     * Determines if the property is required.
     *
     * @return {@code true} if the property is required; otherwise
     *         {@code false}
     */
    public boolean isRequired() {
        return property.isRequired();
    }

    /**
     * Returns the boolean value of the property.
     *
     * @return the value of the property, or {@code false} if the property is null
     * @throws OpenVPMSException if conversion fails
     */
    @Override
    public boolean getBoolean() {
        return property.getBoolean();
    }

    /**
     * Returns the boolean value of the property.
     *
     * @param defaultValue the value to return if the property value is {@code null}
     * @return the value of the property, or {@code defaultValue} if it is {@code null}
     * @throws OpenVPMSException if conversion fails
     */
    @Override
    public boolean getBoolean(boolean defaultValue) {
        return property.getBoolean(defaultValue);
    }

    /**
     * Returns the integer value of the property.
     *
     * @return the value of the property, or {@code 0} if the property is null
     * @throws OpenVPMSException if conversion fails
     */
    @Override
    public int getInt() {
        return property.getInt();
    }

    /**
     * Returns the integer value of the property.
     *
     * @param defaultValue the value to return if the property value is {@code null}
     * @return the value of the property, or {@code defaultValue} if it is {@code null}
     * @throws OpenVPMSException if conversion fails
     */
    @Override
    public int getInt(int defaultValue) {
        return property.getInt(defaultValue);
    }

    /**
     * Returns the long value of the property.
     *
     * @return the value of the property, or {@code 0} if the property is null
     * @throws OpenVPMSException if conversion fails
     */
    @Override
    public long getLong() {
        return property.getLong();
    }

    /**
     * Returns the long value of the property.
     *
     * @param defaultValue the value to return if the property value is {@code null}
     * @return the value of the property, or {@code defaultValue} if it is {@code null}
     * @throws OpenVPMSException if conversion fails
     */
    @Override
    public long getLong(long defaultValue) {
        return property.getLong(defaultValue);
    }

    /**
     * Returns the string value of the property.
     *
     * @return the value of the property. May be {@code null}
     * @throws OpenVPMSException if conversion fails
     */
    @Override
    public String getString() {
        return property.getString();
    }

    /**
     * Returns the string value of the property.
     *
     * @param defaultValue the value to return if the property value is {@code null}
     * @return the value of the property, or {@code defaultValue} if it is {@code null}
     * @throws OpenVPMSException if conversion fails
     */
    @Override
    public String getString(String defaultValue) {
        return property.getString(defaultValue);
    }

    /**
     * Returns the {@code BigDecimal} value of the property.
     *
     * @return the value of the property. May be {@code null}
     * @throws OpenVPMSException if conversion fails
     */
    @Override
    public BigDecimal getBigDecimal() {
        return property.getBigDecimal();
    }

    /**
     * Returns the {@code BigDecimal} value of the property.
     *
     * @param defaultValue the value to return if the property value is {@code null}
     * @return the value of the property, or {@code defaultValue} if it is {@code null}
     * @throws OpenVPMSException if conversion fails
     */
    @Override
    public BigDecimal getBigDecimal(BigDecimal defaultValue) {
        return property.getBigDecimal(defaultValue);
    }

    /**
     * Returns the {@code Money} value of the property.
     *
     * @return the value of the property. May be {@code null}
     * @throws OpenVPMSException if conversion fails
     */
    @Override
    public Money getMoney() {
        return property.getMoney();
    }

    /**
     * Returns the {@code BigDecimal} value of the property.
     *
     * @param defaultValue the value to return if the property value is {@code null}
     * @return the value of the property, or {@code defaultValue} if it is {@code null}
     * @throws OpenVPMSException if conversion fails
     */
    @Override
    public Money getMoney(Money defaultValue) {
        return property.getMoney(defaultValue);
    }

    /**
     * Returns the {@code Date} value of the property.
     *
     * @return the value of the property. May be {@code null}
     * @throws OpenVPMSException if conversion fails
     */
    @Override
    public Date getDate() {
        return property.getDate();
    }

    /**
     * Returns the {@code Date} value of the property.
     *
     * @param defaultValue the value to return if the property value is {@code null}
     * @return the value of the property, or {@code defaultValue} if it is {@code null}
     * @throws OpenVPMSException if conversion fails
     */
    @Override
    public Date getDate(Date defaultValue) {
        return property.getDate(defaultValue);
    }

    /**
     * Returns the reference value of the property.
     *
     * @return the property value. May be {@code null}
     * @throws OpenVPMSException if conversion fails
     */
    @Override
    public IMObjectReference getReference() {
        return property.getReference();
    }

    /**
     * Sets the property transformer.
     *
     * @param transformer the property transformer. May be {@code null}
     */
    public void setTransformer(PropertyTransformer transformer) {
        property.setTransformer(transformer);
    }

    /**
     * Returns the property transformer.
     *
     * @return the property transfoer. May be {@code null}
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
     * @return the property descriptor, or {@code null} if the property has
     *         no descriptor
     */
    public NodeDescriptor getDescriptor() {
        return property.getDescriptor();
    }

    /**
     * Determines if the object has been modified.
     *
     * @return {@code true} if the object has been modified
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
     * Adds a listener to be notified when this changes, specifying the order of the listener.
     *
     * @param listener the listener to add
     * @param index    the index to add the listener at. The 0-index listener is notified first
     */
    public void addModifiableListener(ModifiableListener listener, int index) {
        property.addModifiableListener(listener, index);
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
     * Adds a listener to be notified of errors.
     *
     * @param listener the listener to add
     */
    @Override
    public void addErrorListener(ErrorListener listener) {
        property.addErrorListener(listener);
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    @Override
    public void removeErrorListener(ErrorListener listener) {
        property.removeErrorListener(listener);
    }

    /**
     * Determines if the object is valid.
     *
     * @return {@code true} if the object is valid; otherwise {@code false}
     */
    public boolean isValid() {
        return property.isValid();
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    public boolean validate(Validator validator) {
        return property.validate(validator);
    }

    /**
     * Resets the cached validity state of the object, to force revalidation to of the object and its descendants.
     */
    public void resetValid() {
        property.resetValid();
    }

}
