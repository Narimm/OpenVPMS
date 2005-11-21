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

// java core
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.openvpms.component.business.domain.archetype.ArchetypeId;

/**
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class AssertionDescriptor extends Descriptor {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * The associated error message. This is used when the assertion fails
     */
    private String errorMessage;
    
    /**
     * Holds the properties that are required to evaluate the assertion. All
     * properties are in the form of key value pairs but in some instances it
     * may only be necessary to specify the value.
     */
    private Map<String, PropertyDescriptor> propertyDescriptors = 
        new HashMap<String, PropertyDescriptor>();

    /**
     * Default constructor
     */
    public AssertionDescriptor() {
        setArchetypeId(new ArchetypeId("openvpms-system-descriptor.assertion.1.0"));
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
     * @return Returns the properties.
     */
    public Map<String, PropertyDescriptor> getPropertyDescriptors() {
        return propertyDescriptors;
    }

    /**
     * @param propertyDescriptors The propertyDescriptors to set.
     */
    public void setPropertyDescriptors(
            Map<String, PropertyDescriptor> propertyDescriptors) {
        this.propertyDescriptors = propertyDescriptors;
    }
    
    /**
     * Add the property descriptor to the list
     * 
     * param descriptor
     *            the property descriptor to add
     */
    public void addPropertyDescriptor(PropertyDescriptor descriptor) {
        propertyDescriptors.put(descriptor.getName(), descriptor);
    }
    
    /**
     * Remove the specified property descriptor
     * 
     * @param descriptor
     *            the property descriptor to remove
     */
    public void removePropertyDescriptor(PropertyDescriptor descriptor) {
        propertyDescriptors.remove(descriptor.getName());
    }
    
    /**
     * Remove the property descriptor with the specified key
     * 
     * @param key
     *            the property descriptor key
     */
    public void removePropertyDescriptor(String key) {
        propertyDescriptors.remove(key);
    }
    
    /**
     * Retrieve the property descriptor with the specified key
     * 
     * @param, key
     *            the property descriptor key
     * @return PropertyDescriptor            
     */
    public PropertyDescriptor getPropertyDescriptor(String key) {
        return propertyDescriptors.get(key);
    }

    /**
     * @return Returns the properties.
     */
    public PropertyDescriptor[] getPropertyDescriptorsAsArray() {
        return (PropertyDescriptor[])propertyDescriptors.values().toArray(
                new PropertyDescriptor[propertyDescriptors.size()]);
    }

    /**
     * @param properties The properties to set.
     */
    public void setPropertyDescriptorsAsArray(PropertyDescriptor[] properties) {
        this.propertyDescriptors = new HashMap<String, PropertyDescriptor>();
        for (PropertyDescriptor property : properties) {
            this.propertyDescriptors.put(property.getName(), property);
        }
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("name", getName())
            .append("errorMessage", errorMessage)
            .append("propertyDescriptors", propertyDescriptors)
            .toString();
    }

}
