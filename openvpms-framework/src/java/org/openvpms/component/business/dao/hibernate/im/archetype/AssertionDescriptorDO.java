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

package org.openvpms.component.business.dao.hibernate.im.archetype;

import org.openvpms.component.business.domain.im.datatypes.property.NamedProperty;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyMap;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;


/**
 * Data object interface corresponding to the {@link AssertionDescriptor}
 * class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface AssertionDescriptorDO extends DescriptorDO {

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
     * Return the assertion properties.
     *
     * @return the properties
     */
    PropertyMap getPropertyMap();

    /**
     * Sets the assertion properties.
     *
     * @param properties the properties
     */
    void setPropertyMap(PropertyMap properties);

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
     * @return the corresponding assertion property, or <tt>null</tt> if none
     *         is found
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
}
