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

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * This class describes an validation error for a descriptor.
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class DescriptorValidationError implements Serializable {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The type of descriptor that is in error
     */
    private Descriptor.DescriptorType descriptorType;
    
    /**
     * The name of the object instance if one exists
     */
    private String instanceName;
    
    /**
     * The attribute that is in error
     */
    private String attributeName;
    
    /**
     * The error type
     */
    private Descriptor.ValidationError error;
    
    /**
     * Creae a validation error using the specified parameters
     * 
     * @param descriptor
     *            the type of descriptor in error
     * @param instance
     *            the name of the instance, if it exists (optional)
     * @param attribure
     *            the attribute that is invalid
     * @param error
     *            the error type.                                    
     */
    public DescriptorValidationError(Descriptor.DescriptorType type, 
            String instance, String attribute, Descriptor.ValidationError error) {
        this.descriptorType = type;
        this.instanceName = instance;
        this.attributeName = attribute;
        this.error = error;
    }

    /**
     * @return Returns the attributeName.
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * @param attributeName The attributeName to set.
     */
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    /**
     * @return Returns the descriptorType.
     */
    public Descriptor.DescriptorType getDescriptorType() {
        return descriptorType;
    }

    /**
     * @param descriptorType The descriptorType to set.
     */
    public void setDescriptorType(Descriptor.DescriptorType descriptorType) {
        this.descriptorType = descriptorType;
    }

    /**
     * @return Returns the error.
     */
    public Descriptor.ValidationError getError() {
        return error;
    }

    /**
     * @param error The error to set.
     */
    public void setError(Descriptor.ValidationError error) {
        this.error = error;
    }

    /**
     * @return Returns the instanceName.
     */
    public String getInstanceName() {
        return instanceName;
    }

    /**
     * @param instanceName The instanceName to set.
     */
    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("descriptorType", descriptorType)
            .append("instanceName", instanceName)
            .append("attributeName", attributeName)
            .append("error", error)
            .toString();
    }

}
