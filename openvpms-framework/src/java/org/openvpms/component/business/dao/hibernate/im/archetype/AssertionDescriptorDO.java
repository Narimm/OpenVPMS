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

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.datatypes.property.NamedProperty;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyMap;

/**
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2007-07-10 15:32:07 +1000 (Tue, 10 Jul 2007) $
 */
public class AssertionDescriptorDO extends DescriptorDO {

    /**
     * The associated error message. This is used when the assertion fails
     */
    private String errorMessage;

    /**
     * The index of this assertion descriptor
     */
    private int index;

    /**
     * Holds the properties that are required to evaluate the assertion. All
     * properties are in the form of key value pairs but in some instances it
     * may only be necessary to specify the value.
     */
    private PropertyMap propertyMap = new PropertyMap("root");

    /**
     * Default constructor
     */
    public AssertionDescriptorDO() {
        setArchetypeId(new ArchetypeId("descriptor.assertion.1.0"));
    }

    /**
     * @return Returns the errorMessage.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @param errorMessage The errorMessage to set.
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Return the properties as a map
     *
     * @return Returns the properties.
     */
    public PropertyMap getPropertyMap() {
        return propertyMap;
    }

    /**
     * @param propertyMap the properties to add
     */
    public void setPropertyMap(PropertyMap propertyMap) {
        this.propertyMap = propertyMap;
    }

    /**
     * Add the property to the collection
     * <p/>
     * param property
     * the property to add
     */
    public void addProperty(NamedProperty property) {
        propertyMap.getProperties().put(property.getName(), property);
    }

    /**
     * Remove the specified property
     *
     * @param property the property to remove
     */
    public void removeProperty(NamedProperty property) {
        propertyMap.getProperties().remove(property.getName());
    }

    /**
     * Remove the property with the specified name
     *
     * @param name the property name
     */
    public void removeProperty(String name) {
        propertyMap.getProperties().remove(name);
    }

    /**
     * Retrieve the property descriptor with the specified name
     *
     * @return NamedProperty
     *         the named property or null
     * @param, name
     * the property name
     */
    public NamedProperty getProperty(String name) {
        return propertyMap.getProperties().get(name);
    }

    /**
     * @return Returns the properties.
     */
    public NamedProperty[] getPropertiesAsArray() {
        return propertyMap.getProperties().values().toArray(
                new NamedProperty[propertyMap.getProperties().size()]);
    }

    /**
     * @param properties The properties to set.
     */
    public void setPropertiesAsArray(NamedProperty[] properties) {
        this.propertyMap = new PropertyMap("root");
        for (NamedProperty property : properties) {
            this.propertyMap.getProperties().put(property.getName(), property);
        }
    }

    /**
     * @return Returns the index.
     */
    public int getIndex() {
        return index;
    }

    /**
     * @param index The index to set.
     */
    public void setIndex(int index) {
        this.index = index;
    }


}
