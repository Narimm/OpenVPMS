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

package org.openvpms.component.business.dao.hibernate.im.security;

import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDO;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface ArchetypeAuthorityDO extends IMObjectDO {
    /**
     * Returns the archetype short name.
     *
     * @return the archetype short name
     */
    String getShortName();

    /**
     * Sets the archetype short name.
     *
     * @param shortName the archetype short name
     */
    void setShortName(String shortName);

    /**
     * Returns the method.
     *
     * @return the method
     */
    String getMethod();

    /**
     * Sets the method.
     *
     * @param method the method
     */
    void setMethod(String method);

    /**
     * Returns the service name.
     *
     * @return the service name
     */
    String getServiceName();

    /**
     * Sets the service name.
     *
     * @param name the service name
     */
    void setServiceName(String name);

    /**
     * Returns the role.
     *
     * @return the role
     */
    SecurityRoleDO getRole();

    /**
     * Sets the role.
     *
     * @param role the role
     */
    void setRole(SecurityRoleDO role);
}
