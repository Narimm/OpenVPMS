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

package org.openvpms.component.model.archetype;

import org.openvpms.component.model.object.IMObject;

/**
 * An {@code AssertionDescriptor} describes an assertion on a {@link NodeDescriptor}, and is used to configure the
 * behaviour of its associated {@link AssertionTypeDescriptor}.
 * <br/>
 * By convention, the {@code AssertionTypeDescriptor} should have the same name as the {@code AssertionDescriptor} -
 * this is used to establish the association.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public interface AssertionDescriptor extends IMObject {

    /**
     * Return the assertion properties.
     *
     * @return the properties
     */
    PropertyMap getPropertyMap();

    /**
     * Adds an assertion property.
     *
     * @param property the property to add
     */
    void addProperty(NamedProperty property);

    /**
     * Removes an assertion property.
     *
     * @param property the property to remove
     */
    void removeProperty(NamedProperty property);

    /**
     * Removes an assertion property.
     *
     * @param name the name of the property to remove
     */
    void removeProperty(String name);

    /**
     * Returns the assertion property by name.
     *
     * @param name the property name
     * @return the corresponding assertion property, or {@code null} if none is found
     */
    NamedProperty getProperty(String name);

    /**
     * Returns the index.
     *
     * @return the index
     */
    int getIndex();

    /**
     * Sets the index.
     *
     * @param index the index to set
     */
    void setIndex(int index);

    /**
     * Returns the error message.
     *
     * @return the error message
     */
    String getErrorMessage();

    /**
     * Sets the error message.
     *
     * @param errorMessage the error message to set
     */
    void setErrorMessage(String errorMessage);

    /**
     * Evaluates the validation action type of the associated assertion type descriptor, if one is defined.
     *
     * @param value  the value to validate
     * @param parent the parent object
     * @param node   the node descriptor
     * @return {@code true} if the value is valid, otherwise {@code false}
     */
    boolean validate(Object value, IMObject parent, NodeDescriptor node);
}