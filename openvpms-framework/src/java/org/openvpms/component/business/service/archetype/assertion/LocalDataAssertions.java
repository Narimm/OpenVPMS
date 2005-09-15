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


package org.openvpms.component.business.service.archetype.assertion;

import java.util.Map;

/**
 * This class has static methods for local reference data assertions. All
 * the static methods return boolean and take an object and a property
 * map as parameters.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class LocalDataAssertions {

    /**
     * Default constructor
     */
    public LocalDataAssertions() {
    }
    
    /**
     * Check that the target object is in the specified list.
     * 
     * @param target
     *            the target object
     * @param properties
     *            the properties used for the assertion           
     */
    public static boolean isStringValueInList(Object target, Map properties) {
        String str = (String)target;
        Map list = (Map)properties.get("entries");
        boolean override = false;
        
        // check to see if the override attribute has been specified
        if (properties.get("override") != null) {
            override = Boolean.parseBoolean((String)properties.get("override"));
        }
        
        // if override is allowed then always return true
        if (override) {
            return true;
        } 
        
        return list.containsValue(str);
    }
}
