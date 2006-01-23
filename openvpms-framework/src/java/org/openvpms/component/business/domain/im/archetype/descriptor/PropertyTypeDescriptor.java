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

import org.openvpms.component.business.domain.archetype.ArchetypeId;

/**
 * A property type is associated with an assertion type and basically
 * declares the type of property and the cardinality of that property.
 * <p>
 * The property type is defined by an archetypeId
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class PropertyTypeDescriptor extends Descriptor {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The property type is a a fully qualified archetype id.
     */
    private String type;
    
    /**
     * Indicates whether this property is required. Defaults to false
     */
    private boolean required = false;
    
    
    /**
     * Default constructor 
     */
    public PropertyTypeDescriptor() {
        this.setArchetypeId(new ArchetypeId("openvpms-system-descriptor.propertyType.1.0"));
    }

    /**
     * @return Returns the required.
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * @param required The required to set.
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * @return Returns the type.
     */
    public String getType() {
        return type;
    }

    /**
     * @param type The type to set.
     */
    public void setType(String type) {
        this.type = type;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.archetype.descriptor.Descriptor#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        PropertyTypeDescriptor copy = (PropertyTypeDescriptor)super.clone();
        copy.required = this.required;
        copy.type = this.type;
        
        return copy;
    }
    
}
