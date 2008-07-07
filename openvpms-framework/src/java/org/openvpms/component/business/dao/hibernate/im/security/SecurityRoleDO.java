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
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.security.ArchetypeAwareGrantedAuthority;
import org.openvpms.component.business.domain.im.security.User;

import java.util.HashSet;
import java.util.Set;

/**
 * A role is associated with a user and has one or more
 * {@link ArchetypeAwareGrantedAuthority}
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class SecurityRoleDO extends IMObjectDO {

    /**
     * The set of granted authorities for this role
     */
    private Set<ArchetypeAuthorityDO> authorities =
            new HashSet<ArchetypeAuthorityDO>();

    /**
     * The set of {@link User}s that are members of this role.
     */
    private Set<UserDO> users = new HashSet<UserDO>();


    /**
     * Default constructor.
     */
    public SecurityRoleDO() {
        //no-op
    }

    /**
     * Creates a new <tt>SecurityRoleDO</tt>.
     *
     * @param archetypeId the archetype id
     */
    public SecurityRoleDO(ArchetypeId archetypeId) {
        super(archetypeId);
    }

    /**
     * @return Returns the authorities.
     */
    public Set<ArchetypeAuthorityDO> getAuthorities() {
        return authorities;
    }

    /**
     * Add the specified authority
     *
     * @param authority the authority to add
     */
    public void addAuthority(ArchetypeAuthorityDO authority) {
        authority.setRole(this);
        authorities.add(authority);
    }

    /**
     * Remove the specified authority
     *
     * @param authority the authhority to remove
     */
    public void removeAuthority(ArchetypeAuthorityDO authority) {
        authority.setRole(null);
        authorities.remove(authority);
    }

    /**
     * @return Returns the users.
     */
    public Set<UserDO> getUsers() {
        return users;
    }

    /**
     * Make the specified {@link UserDO} a member of this role.
     *
     * @param user
     */
    public void addUser(UserDO user) {
        users.add(user);
    }

    /**
     * Remove the specified user so it is no longer a member of this role.
     *
     * @param user
     */
    public void removeUser(UserDO user) {
        users.remove(user);
    }

    /**
     * @param users The users to set.
     */
    protected void setUsers(Set<UserDO> users) {
        this.users = users;
    }

    /**
     * @param authorities The authorities to set.
     */
    protected void setAuthorities(
            Set<ArchetypeAuthorityDO> authorities) {
        this.authorities = authorities;
    }

}
