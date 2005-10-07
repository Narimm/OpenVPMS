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


package org.openvpms.component.business.service.archetype.descriptor;

// java core
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class AssertionDescriptor implements Serializable {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * A string that defines the assertion type. There can only be one 
     * instance of an assertion type per {@link NodeDescriptor}.
     */
    private String type;
    
    /**
     * The associated error message. This is used when the assertion fails
     */
    private String errorMessage;
    
    /**
     * Holds the properties that are required to evaluate the assertion. All
     * properties are in the form of key value pairs but in some instances it
     * may only be necessary to specify the value.
     */
    private HashMap<String, AssertionProperty> properties = 
        new HashMap<String, AssertionProperty>();

    /**
     * Default constructor
     */
    public AssertionDescriptor() {
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
    public Map<String, AssertionProperty> getPropertiesAsMap() {
        return properties;
    }

    /**
     * @return Returns the properties.
     */
    public AssertionProperty[] getProperties() {
        return (AssertionProperty[])properties.values().toArray(
                new AssertionProperty[properties.size()]);
    }

    /**
     * @param properties The properties to set.
     */
    public void setProperties(AssertionProperty[] properties) {
        this.properties = new HashMap<String, AssertionProperty>();
        for (AssertionProperty property : properties) {
            this.properties.put(property.getKey(), property);
        }
    }
}
