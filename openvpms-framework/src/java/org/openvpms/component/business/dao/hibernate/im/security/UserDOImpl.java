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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.dao.hibernate.im.security;

import org.openvpms.component.business.dao.hibernate.im.party.PartyDOImpl;
import org.openvpms.component.business.domain.archetype.ArchetypeId;

import java.util.HashSet;
import java.util.Set;


/**
 * Implementation of the {@link UserDO} interface.
 *
 * @author Tim Anderson
 */
public class UserDOImpl extends PartyDOImpl implements UserDO {

    /**
     * The login name.
     */
    private String userName;

    /**
     * The password.
     */
    private String password;

    /**
     * The list of security roles that the user is a member of.
     */
    private Set<SecurityRoleDO> roles = new HashSet<SecurityRoleDO>();


    /**
     * Default constructor.
     */
    public UserDOImpl() {
        // no op
    }

    /**
     * Creates a new <tt>UserDOImpl</tt>.
     *
     * @param archetypeId the archetype id
     */
    public UserDOImpl(ArchetypeId archetypeId) {
        super(archetypeId);
    }

    /**
     * Returns the login name.
     *
     * @return the login name
     */
    public String getUsername() {
        return userName;
    }

    /**
     * Sets the login name.
     *
     * @param name the login name
     */
    public void setUsername(String name) {
        userName = name;
    }

    /**
     * Returns the password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     *
     * @param password the password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the security roles that the user is a member of.
     *
     * @return the roles
     */
    public Set<SecurityRoleDO> getRoles() {
        return roles;
    }

    /**
     * Makes the user a member of a security role.
     *
     * @param role the role
     */
    public void addRole(SecurityRoleDO role) {
        roles.add(role);
    }

    /**
     * Delete user's membership from the specified security role.
     *
     * @param role the role
     */
    public void removeRole(SecurityRoleDO role) {
        roles.remove(role);
    }

    /**
     * Sets the security roles.
     *
     * @param roles the roles
     */
    protected void setRoles(Set<SecurityRoleDO> roles) {
        this.roles = roles;
    }

}
