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

import org.openvpms.component.business.domain.im.datatypes.property.PropertyMap;
import org.openvpms.component.business.domain.im.datatypes.property.NamedProperty;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface AssertionDescriptorDO extends DescriptorDO {
    /**
     * @return Returns the errorMessage.
     */
    String getErrorMessage();

    /**
     * @param errorMessage The errorMessage to set.
     */
    void setErrorMessage(String errorMessage);

    /**
     * Return the properties as a map
     *
     * @return Returns the properties.
     */
    PropertyMap getPropertyMap();

    /**
     * @param propertyMap the properties to add
     */
    void setPropertyMap(PropertyMap propertyMap);

    /**
     * Add the property to the collection
     * <p/>
     * param property
     * the property to add
     */
    void addProperty(NamedProperty property);

    /**
     * Remove the specified property
     *
     * @param property the property to remove
     */
    void removeProperty(NamedProperty property);

    /**
     * Remove the property with the specified name
     *
     * @param name the property name
     */
    void removeProperty(String name);

    /**
     * Retrieve the property descriptor with the specified name
     *
     * @return NamedProperty
     *         the named property or null
     * @param, name
     * the property name
     */
    NamedProperty getProperty(String name);

    /**
     * @return Returns the properties.
     */
    NamedProperty[] getPropertiesAsArray();

    /**
     * @param properties The properties to set.
     */
    void setPropertiesAsArray(NamedProperty[] properties);

    /**
     * @return Returns the index.
     */
    int getIndex();

    /**
     * @param index The index to set.
     */
    void setIndex(int index);
}
