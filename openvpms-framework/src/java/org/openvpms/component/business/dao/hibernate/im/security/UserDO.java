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

import org.openvpms.component.business.dao.hibernate.im.entity.EntityDO;
import org.openvpms.component.business.domain.im.security.User;

import java.util.Set;


/**
 * Data object interface corresponding to the {@link User} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface UserDO extends EntityDO {

    /**
     * Returns the login name.
     *
     * @return the login name
     */
    String getUsername();

    /**
     * Sets the login name.
     *
     * @param name the login name
     */
    void setUsername(String name);

    /**
     * Returns the password.
     *
     * @return the password
     */
    String getPassword();

    /**
     * Sets the password.
     *
     * @param password the password
     */
    void setPassword(String password);

    /**
     * Returns the security roles that the user is a member of.
     *
     * @return the roles
     */
    Set<SecurityRoleDO> getRoles();

    /**
     * Makes the user a member of a security role.
     *
     * @param role the role
     */
    void addRole(SecurityRoleDO role);

    /**
     * Removes a user's membership from the specified security role.
     *
     * @param role the role
     */
    void removeRole(SecurityRoleDO role);
}
