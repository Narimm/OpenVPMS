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

import org.openvpms.component.business.service.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.service.archetype.descriptor.NodeDescriptor;

/**
 * This class has static methods for local reference data assertions. All
 * the static methods return boolean and take an object and a property
 * map as parameters.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class LookupAssertions {

    /**
     * Default constructor
     */
    public LookupAssertions() {
    }
    
    /**
     * Check that the target object is in the specified list.
     * 
     * @param target
     *            the target object
     * @param node
     *            the node descriptor for this assertion
     * @param assertion
     *            the particular assertion                        
     */
    public static boolean isStringValueInList(Object target, 
            NodeDescriptor node, AssertionDescriptor assertion) {
        Map properties = assertion.getPropertyDescriptors();
        return (properties == null) ? false : 
            properties.containsKey((String)target);
    }
    
    /**
     * Always return true
     * 
     * @param target
     *            the target object
     * @param node
     *            the node descriptor for this assertion
     * @param assertion
     *            the particular assertion                        
     */
    public static boolean alwaysTrue(Object target, 
            NodeDescriptor node, AssertionDescriptor assertion) {
        return true;
    }
}
