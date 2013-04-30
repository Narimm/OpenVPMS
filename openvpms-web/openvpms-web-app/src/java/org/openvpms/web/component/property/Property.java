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
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;


/**
 * Property that provides notification on modification.
 *
 * @author Tim Anderson
 */
public interface Property extends Modifiable {

    /**
     * Returns the property name.
     *
     * @return the property name
     */
    String getName();

    /**
     * Returns the property display name.
     *
     * @return the display name
     */
    String getDisplayName();

    /**
     * Returns the property description.
     *
     * @return the description. May be {@code null}
     */
    String getDescription();

    /**
     * Sets the value of the property.
     * The value will only be set if it is valid, and different to the existing
     * value. If the value is set, any listeners will be notified.
     *
     * @param value the property value
     * @return {@code true} if the value was set, {@code false} if it
     *         cannot be set due to error, or is the same as the existing value
     */
    boolean setValue(Object value);

    /**
     * Returns the value of the property.
     *
     * @return the property value
     */
    Object getValue();

    /**
     * Returns the minimum length of the property.
     *
     * @return the minimum length
     */
    int getMinLength();

    /**
     * Returns the maximum length of the property.
     *
     * @return the maximum length, or {@code -1} if it is unbounded
     */
    int getMaxLength();

    /**
     * Returns the property type.
     *
     * @return the property type
     */
    Class getType();

    /**
     * Determines if the property is a boolean.
     *
     * @return {@code true} if it is a boolean
     */
    boolean isBoolean();

    /**
     * Determines if the property is a string.
     *
     * @return {@code true} if it is a string
     */
    boolean isString();

    /**
     * Determines if the property is numeric.
     *
     * @return {@code true} if it is numeric
     */
    boolean isNumeric();

    /**
     * Determines if the property is a date.
     *
     * @return {@code true} if it is a date
     */
    boolean isDate();

    /**
     * Determines if the property is a money property.
     *
     * @return {@code true} it is a money property
     */
    boolean isMoney();

    /**
     * Determines if the property is an object reference.
     *
     * @return {@code true} if it is an object reference
     */
    boolean isObjectReference();

    /**
     * Determines if the property is a lookup.
     *
     * @return {@code true} if it is a lookup
     */
    boolean isLookup();

    /**
     * Determines if the property is a collection.
     *
     * @return {@code true} if it is a collection
     */
    boolean isCollection();

    /**
     * Returns the archetype short names that this property may support.
     * <p/>
     * Wildcards are expanded.
     *
     * @return the archetype short names
     * @throws ArchetypeServiceException for any error
     */
    String[] getArchetypeRange();

    /**
     * Determines if the property value is derived from an expression.
     *
     * @return {@code true} if the value is derived, otherwise {@code false}
     */
    boolean isDerived();

    /**
     * Determines if the property is read-only.
     *
     * @return {@code true} if the property is read-only
     */
    boolean isReadOnly();

    /**
     * Determines if the property is hidden.
     *
     * @return {@code true} if the property is hidden; otherwise {@code false}
     */
    boolean isHidden();

    /**
     * Determines if the property is required.
     *
     * @return {@code true} if the property is required; otherwise
     *         {@code false}
     */
    boolean isRequired();

    /**
     * Sets the property transformer.
     *
     * @param transformer the property transformer. May be {@code null}
     */
    void setTransformer(PropertyTransformer transformer);

    /**
     * Returns the property transformer.
     *
     * @return the property transfoer. May be {@code null}
     */
    PropertyTransformer getTransformer();

    /**
     * Notify any listeners that they need to refresh and marks this modified.
     */
    void refresh();

    /**
     * Returns the property descriptor.
     *
     * @return the property descriptor, or {@code null} if the property has no descriptor
     */
    NodeDescriptor getDescriptor();

}
