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
 * A property descriptor define a key type and a value. The key is the name of 
 * the property.
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class PropertyDescriptor  extends Descriptor {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The property type, which defaults to string
     */
    private String type = String.class.getName();
    
    /**
     * The property value
     */
    private String value;
    
    /**
     * Default constructor
     */
    public PropertyDescriptor() {
        setArchetypeId(new ArchetypeId("openvpms-system-descriptor.property.1.0"));
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

    /**
     * @return Returns the value.
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value The value to set.
     */
    public void setValue(String value) {
        this.value = value;
    }
}
