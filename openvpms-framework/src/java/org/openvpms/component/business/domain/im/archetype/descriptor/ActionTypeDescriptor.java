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
 * The action type descriptor defines an action that is associated 
 * with an assertion. It is defined by a class and method name. An action
 * maybe something like 'assert' or 'get'.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class ActionTypeDescriptor extends Descriptor {
    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;
  
    /**
     * The class name that is associatd with this action type
     */
    private String className;
    
    /**
     * The method name associated with this action type
     */
    private String methodName;

    /**
     * Default constructor
     */
    public ActionTypeDescriptor() {
        this.setArchetypeId(new ArchetypeId("openvpms-system-descriptor.actionType.1.0"));
    }
    
    /**
     * @return Returns the className.
     */
    public String getClassName() {
        return className;
    }

    /**
     * @param className The className to set.
     */
    public void setClassName(String className) {
        this.className = className;
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
    
    
    
}
