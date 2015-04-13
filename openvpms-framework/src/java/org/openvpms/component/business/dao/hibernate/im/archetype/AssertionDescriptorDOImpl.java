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
 *
 *  Implementation of the {@link AssertionDescriptorDO} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2007-07-10 15:32:07 +1000 (Tue, 10 Jul 2007) $
 */
public class AssertionDescriptorDOImpl extends DescriptorDOImpl
        implements AssertionDescriptorDO {

    /**
     * The error message, used when the assertion fails.
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
    private PropertyMap properties = new PropertyMap("root");


    /**
     * Default constructor.
     */
    public AssertionDescriptorDOImpl() {
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
     * @param errorMessage the error message to set
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Return the assertion properties.
     *
     * @return the properties
     */
    public PropertyMap getPropertyMap() {
        return properties;
    }

    /**
     * Sets the assertion properties.
     *
     * @param properties the properties
     */
    public void setPropertyMap(PropertyMap properties) {
        this.properties = properties;
    }

    /**
     * Adds an assertion property.
     *
     * @param property the property to add
     */
    public void addProperty(NamedProperty property) {
        properties.getProperties().put(property.getName(), property);
    }

    /**
     * Removes an assertion property.
     *
     * @param property the property to remove
     */
    public void removeProperty(NamedProperty property) {
        properties.getProperties().remove(property.getName());
    }

    /**
     * Removes an assertion property.
     *
     * @param name the name of the property to remove
     */
    public void removeProperty(String name) {
        properties.getProperties().remove(name);
    }

    /**
     * Returns the assertion property by name.
     *
     * @param name the property name
     * @return the corresponding assertion property, or <tt>null</tt> if none
     *         is found
     */
    public NamedProperty getProperty(String name) {
        return properties.getProperties().get(name);
    }

    /**
     * Returns the index.
     *
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets the index.
     *
     * @param index the index to set
     */
    public void setIndex(int index) {
        this.index = index;
    }

}
