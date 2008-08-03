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

import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDOImpl;
import org.openvpms.component.business.domain.archetype.ArchetypeId;

import java.util.HashSet;
import java.util.Set;


/**
 * Implementation of the {@link SecurityRoleDO} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class SecurityRoleDOImpl extends IMObjectDOImpl
        implements SecurityRoleDO {

    /**
     * The set of granted authorities for this role.
     */
    private Set<ArchetypeAuthorityDO> authorities =
            new HashSet<ArchetypeAuthorityDO>();

    /**
     * The set of users that are members of this role.
     */
    private Set<UserDO> users = new HashSet<UserDO>();


    /**
     * Default constructor.
     */
    public SecurityRoleDOImpl() {
        //no-op
    }

    /**
     * Creates a new <tt>SecurityRoleDOImpl</tt>.
     *
     * @param archetypeId the archetype id
     */
    public SecurityRoleDOImpl(ArchetypeId archetypeId) {
        super(archetypeId);
    }

    /**
     * Returns the archetype authorities.
     *
     * @return the authorities
     */
    public Set<ArchetypeAuthorityDO> getAuthorities() {
        return authorities;
    }

    /**
     * Adds an authority.
     *
     * @param authority the authority to add
     */
    public void addAuthority(ArchetypeAuthorityDO authority) {
        authority.setRole(this);
        authorities.add(authority);
    }

    /**
     * Removes an authority.
     *
     * @param authority the authhority to remove
     */
    public void removeAuthority(ArchetypeAuthorityDO authority) {
        authority.setRole(null);
        authorities.remove(authority);
    }

    /**
     * Returns the users.
     *
     * @return the users
     */
    public Set<UserDO> getUsers() {
        return users;
    }

    /**
     * Adds a user.
     *
     * @param user the user to add
     */
    public void addUser(UserDO user) {
        users.add(user);
    }

    /**
     * Removes a user.
     *
     * @param user the user to remove
     */
    public void removeUser(UserDO user) {
        users.remove(user);
    }

    /**
     * Sets the users.
     *
     * @param users the users to set
     */
    protected void setUsers(Set<UserDO> users) {
        this.users = users;
    }

    /**
     * Sets the authorities.
     *
     * @param authorities the authorities to set
     */
    protected void setAuthorities(Set<ArchetypeAuthorityDO> authorities) {
        this.authorities = authorities;
        for (ArchetypeAuthorityDO authority : authorities) {
            authority.setRole(this);
        }
    }

}
