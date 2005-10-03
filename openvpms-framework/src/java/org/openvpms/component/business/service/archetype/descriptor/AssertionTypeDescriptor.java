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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * This is used to define the assertion type. It is used to map an assertion to
 * its type information
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class AssertionTypeDescriptor implements Serializable {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The name of the assertion
     */
    private String name;
    
    /**
     * The class, which contains the corresponding method
     */
    private String type;
    
    /**
     * The method name, which defines the assertion logic
     */
    private String methodName;
    
    /**
     * Default constructor
     */
    public AssertionTypeDescriptor() {
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
     * @return Returns the methodName.
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * @param methodName The methodName to set.
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
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
    public boolean assertTrue(Object target, AssertionDescriptor assertion) {
        Map properties = assertion.getProperties();
        

        try {
            Class clazz = Thread.currentThread()
                .getContextClassLoader().loadClass(getType());
            Method method = clazz.getMethod(getMethodName(), 
                    new Class[]{Object.class, Map.class});
            
            return ((Boolean)method.invoke(null, 
                    new Object[]{target, properties})).booleanValue();
        } catch (Exception exception) {
            throw new AssertionException(
                    AssertionException.ErrorCode.FailedToApplyAssertion,
                    new Object[] {getName()},
                    exception);
            
        }
    }
}
