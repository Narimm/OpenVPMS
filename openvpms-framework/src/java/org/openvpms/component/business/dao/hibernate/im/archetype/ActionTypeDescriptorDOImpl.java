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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.hibernate.im.archetype;

import org.openvpms.component.business.domain.archetype.ArchetypeId;


/**
 * Implementation of the {@link ActionTypeDescriptorDO} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2007-07-10 15:32:07 +1000 (Tue, 10 Jul 2007) $
 */
public class ActionTypeDescriptorDOImpl extends DescriptorDOImpl
        implements ActionTypeDescriptorDO {

    /**
     * The class name.
     */
    private String className;

    /**
     * The method name.
     */
    private String methodName;


    /**
     * Default constructor.
     */
    public ActionTypeDescriptorDOImpl() {
        setArchetypeId(new ArchetypeId("descriptor.actionType.1.0"));
    }

    /**
     * Returns the class name.
     *
     * @return the class name
     */
    public String getClassName() {
        return className;
    }

    /**
     * Sets the class name.
     *
     * @param className the class name
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Returns the method name.
     *
     * @return the method name
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Sets the method name.
     *
     * @param methodName the method name
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
}
