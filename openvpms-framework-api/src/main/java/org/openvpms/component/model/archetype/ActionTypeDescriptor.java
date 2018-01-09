/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.model.archetype;

import org.openvpms.component.model.object.IMObject;

/**
 * The action type descriptor defines an action that is associated with an assertion.
 * It is defined by a class and method name. An action maybe something like 'assert' or 'get'.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public interface ActionTypeDescriptor extends IMObject {

    /**
     * Returns the class name associated with this action type.
     *
     * @return the fully qualified class name
     */
    String getClassName();

    /**
     * Sets the class name associated with this action type.
     *
     * @param className the fully qualified class name
     */
    void setClassName(String className);

    /**
     * Returns the method name associated with this action type.
     *
     * @return the method name
     */
    String getMethodName();

    /**
     * Sets the method name associated with this action type.
     *
     * @param methodName the method name
     */
    void setMethodName(String methodName);

}
