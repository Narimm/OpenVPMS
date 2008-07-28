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

import java.util.Set;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface SecurityRoleDO extends IMObjectDO {
    /**
     * @return Returns the authorities.
     */
    Set<ArchetypeAuthorityDO> getAuthorities();

    /**
     * Add the specified authority
     *
     * @param authority the authority to add
     */
    void addAuthority(ArchetypeAuthorityDO authority);

    /**
     * Remove the specified authority
     *
     * @param authority the authhority to remove
     */
    void removeAuthority(ArchetypeAuthorityDO authority);

    /**
     * @return Returns the users.
     */
    Set<UserDO> getUsers();

    /**
     * Make the specified {@link UserDOImpl} a member of this role.
     *
     * @param user
     */
    void addUser(UserDO user);

    /**
     * Remove the specified user so it is no longer a member of this role.
     *
     * @param user
     */
    void removeUser(UserDO user);
}
