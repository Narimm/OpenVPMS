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


package org.openvpms.component.business.domain.im.security;

import org.openvpms.component.business.domain.im.common.Entity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This class represents the user details and the list of associated
 * authorities.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class User extends Entity implements UserDetails {

    /**
     * Default UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The user's login name.
     */
    private String userName;

    /**
     * The user's password.
     */
    private String password;

    /**
     * The list of {@link SecurityRole}s that the user is a member off
     */
    private Set<SecurityRole> roles = new HashSet<SecurityRole>();


    /**
     * Default constructor
     */
    public User() {
        // no op
    }

    /**
     * Create a user with the specified parameters.
     *
     * @param username the user's login name
     * @param password the user's password
     * @param active   determines if the user is active
     */
    public User(String username, String password, boolean active) {
        this.userName = username;
        this.password = password;
        setName(username);
        setActive(active);
    }

    /**
     * Indicates whether the user's account has expired. An expired account cannot be authenticated.
     *
     * @return <code>true</code> if the user's account is valid (ie non-expired), <code>false</code> if no longer valid
     */
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is locked or unlocked. A locked user cannot be authenticated.
     *
     * @return <code>true</code> if the user is not locked, <code>false</code> otherwise
     */
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Returns the authorities granted to the user.
     *
     * @return the authorities, sorted by natural key (never <code>null</code>)
     */
    public Collection<GrantedAuthority> getAuthorities() {
        // TODO For performance we may need to cache the authorities for each user. 
        HashSet<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
        for (SecurityRole role : roles) {
            authorities.addAll(role.getAuthorities());
        }

        return authorities;
    }


    /**
     * Indicates whether the user's credentials (password) has expired. Expired credentials prevent
     * authentication.
     *
     * @return <code>true</code> if the user's credentials are valid (ie non-expired), <code>false</code> if no longer
     *         valid (ie expired)
     */
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is enabled or disabled. A disabled user cannot be authenticated.
     *
     * @return <code>true</code> if the user is enabled, <code>false</code> otherwise
     */
    public boolean isEnabled() {
        return isActive();
    }

    /**
     * Returns the password used to authenticate the user. Cannot return <code>null</code>.
     *
     * @return the password (never <code>null</code>)
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the username used to authenticate the user. Cannot return <code>null</code>.
     *
     * @return the username (never <code>null</code>)
     */
    public String getUsername() {
        return userName;
    }

    /**
     * Sets the user's login name.
     *
     * @param userName the user's login name
     */
    public void setUsername(String userName) {
        this.userName = userName;
    }

    /**
     * Sets the user's password.
     *
     * @param password the password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the roles.
     *
     * @return the roles
     */
    public Set<SecurityRole> getRoles() {
        return roles;
    }

    /**
     * Make this user a member of the specified {@link SecurityRole}.
     *
     * @param role the role it should become a member off
     */
    public void addRole(SecurityRole role) {
        role.addUser(this);
        roles.add(role);
    }

    /**
     * Delete user's membership from the specified {@link SecurityRole}.
     *
     * @param role the role to remove
     */
    public void removeRole(SecurityRole role) {
        role.removeUser(this);
        roles.remove(role);
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.Entity#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        User copy = (User) super.clone();
        copy.password = this.password;
        copy.roles = new HashSet<SecurityRole>(this.roles);

        return copy;
    }

    /**
     * Sets the roles.
     *
     * @param roles the roles to set.
     */
    protected void setRoles(Set<SecurityRole> roles) {
        this.roles = roles;
    }

}
