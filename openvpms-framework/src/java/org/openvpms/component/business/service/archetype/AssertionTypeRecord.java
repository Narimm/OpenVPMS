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


package org.openvpms.component.business.service.archetype;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//openevpms-framework
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.archetype.ArrayElement;
import org.openvpms.component.business.domain.archetype.Assertion;
import org.openvpms.component.business.domain.archetype.AssertionType;
import org.openvpms.component.business.domain.archetype.MapElement;
import org.openvpms.component.business.domain.archetype.Property;
import org.openvpms.component.business.domain.archetype.PropertyArray;
import org.openvpms.component.business.domain.archetype.PropertyMap;

/**
 * This class holds the details of an assertion type. An assertion type is 
 * in an archetype definition
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class AssertionTypeRecord implements Serializable {
    
    /**
     * Generated SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Holds a reference to the assertion type
     */
    private AssertionType assertionType;
    
    /**
     * This is the ethod that will be called when {@link #assertTrue} is called.
     */
    private Method method;
    

    /**
     * Construct an instance of this class using the specified assertion 
     * information
     * 
     * @param assertionType
     *            the assertion type information
     * 
     */
    public AssertionTypeRecord(AssertionType assertionType) {
        if (assertionType == null) {
            throw new AssertionException(
                    AssertionException.ErrorCode.NoAssertionTypeSpecified);
        }
        
        if (StringUtils.isEmpty(assertionType.getClazz()) ||
            StringUtils.isEmpty(assertionType.getMethod())) {
            throw new AssertionException(
                    AssertionException.ErrorCode.NoClassOrMethodSpecified,
                    new Object[] {assertionType.getName()});
        }
        this.assertionType = assertionType;
        
        
        try {
            Class clazz = Class.forName(assertionType.getClazz());
            method = clazz.getMethod(assertionType.getMethod(), 
                    new Class[]{Object.class, Map.class});
        } catch (Exception exception) {
            throw new AssertionException(
                    AssertionException.ErrorCode.FailedToLocateMethod,
                    new Object[] {assertionType.getClazz(), assertionType.getMethod()},
                    exception);
        }
        
    }

    /**
     * @return Returns the assertionType.
     */
    public AssertionType getAssertionType() {
        return assertionType;
    }

    /**
     * @param assertionType The assertionType to set.
     */
    public void setAssertionType(AssertionType assertionType) {
        this.assertionType = assertionType;
    }

    /**
     * This method will evaluate the assetion against the target object and
     * return true if the assertion holds and false otherwise.
     * <p>
     * The declared assertion is transformed into a method call, which takes the
     * target object as one parameter and a map of properties as the other 
     * parameter. 
     * 
     * @param target
     *            this is the object that is the subject of the assetion
     * @param assertion
     *            this is the assertion obect holds the parameters to the 
     *            method call
     * @return boolean
     * @throws AssertionException
     *            a runtime exception that is raised if the assertion cannot
     *            be evaluated.
     */
    public boolean assertTrue(Object target, Assertion assertion) {
        boolean result = false;
        Map<String, Object> properties = getProperties(assertion);
        

        try {
            result = ((Boolean)method.invoke(null, 
                    new Object[]{target, properties})).booleanValue();
        } catch (Exception exception) {
            throw new AssertionException(
                    AssertionException.ErrorCode.FailedToApplyAssertion,
                    new Object[] {assertionType.getName()},
                    exception);
            
        }
        
        return result;
    }
    
    /**
     * Process the properties associated with the assertion
     * 
     * @param assertion
     *            the assertion to process
     * @return Map
     *            the properties            
     */
    private Map<String, Object> getProperties(Assertion assertion) {
        Map<String, Object> properties = new HashMap<String, Object>();
        
        // process the simple properties
        for (Property property : assertion.getProperty()) {
            properties.put(property.getName(), property.getValue());
        }
        
        // process any array properties
        for (PropertyArray propArray : assertion.getPropertyArray()) {
            ArrayList<String> list = new ArrayList<String>();
            for (ArrayElement element : propArray.getArrayElement()) {
                list.add(element.getValue());
            }
            properties.put(propArray.getName(), list);
        }
        
        // process any array properties
        for (PropertyMap propMap : assertion.getPropertyMap()) {
            HashMap<String, String> map = new HashMap<String, String>();
            for (MapElement element : propMap.getMapElement()) {
                map.put(element.getKey(), element.getValue());
            }
            properties.put(propMap.getName(), map);
        }
        
        return properties;
    }

}
