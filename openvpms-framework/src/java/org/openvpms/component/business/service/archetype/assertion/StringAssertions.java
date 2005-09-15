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
 * This class has a number of static assertion functions. All static functions
 * take two parameters, the target object and a map of properties and return 
 * a boolean value.
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class StringAssertions {

    /**
     * Default constrcutor 
     */
    public StringAssertions() {
    }

    /**
     * Test that the length of the string is less than the max length
     * 
     * @param target
     *            the target object to test.
     * @param properties    
     *            a list of properties
     * @return boolean                        
     */
    public static boolean withinMaxLength(Object target, Map properties) {
        String str = (String)target;
        int maxLength = Integer.parseInt((String)properties.get("maxLength"));
        
        return (str.length() <= maxLength);
    }
    
    /**
     * Test that the target object matches the specified regular expression
     * 
     * @param target
     *            the string to test
     * @param properties
     *            a list of properties
     * @return boolean                              
     */
    public static boolean regularExpressionMatch(Object target, Map properties) {
        String str = (String)target;
        String regExpr = (String)properties.get("expression");
        
        if (str == null) {
            return false;
        } else {
            return str.matches(regExpr);
        }
    }
}
