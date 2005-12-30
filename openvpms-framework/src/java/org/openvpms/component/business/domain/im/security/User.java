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

import java.util.HashSet;
import java.util.Set;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.userdetails.UserDetails;
import org.openvpms.component.business.domain.im.common.Entity;

/**
 * This class represents the user details and the list of associated 
 * authorities.
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class User extends Entity implements UserDetails {

    /**
     * Default UID
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * The user's password
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
     * @param username
     *            the user name
     * @param password
     *            the password
     */
    public User(String username, String password, boolean active) {
        setName(username);
        this.password = password;
        setActive(active);
    }

    /* (non-Javadoc)
     * @see org.acegisecurity.userdetails.UserDetails#isAccountNonExpired()
     */
    public boolean isAccountNonExpired() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.acegisecurity.userdetails.UserDetails#isAccountNonLocked()
     */
    public boolean isAccountNonLocked() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.acegisecurity.userdetails.UserDetails#getAuthorities()
     */
    public GrantedAuthority[] getAuthorities() {
        // TODO For performance we may need to cache the authorities for
        // each user.
        HashSet<GrantedAuthority> authorities = new  HashSet<GrantedAuthority>();
        for (SecurityRole role : roles) {
            authorities.addAll(role.getAuthorities());
        }
        
        return authorities.toArray(new GrantedAuthority[authorities.size()]);
    }

    /* (non-Javadoc)
     * @see org.acegisecurity.userdetails.UserDetails#isCredentialsNonExpired()
     */
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.acegisecurity.userdetails.UserDetails#isEnabled()
     */
    public boolean isEnabled() {
        return isActive();
    }

    /* (non-Javadoc)
     * @see org.acegisecurity.userdetails.UserDetails#getPassword()
     */
    public String getPassword() {
        return password;
    }

    /* (non-Javadoc)
     * @see org.acegisecurity.userdetails.UserDetails#getUsername()
     */
    public String getUsername() {
        return getName();
    }

    /**
     * @param userName The userName to set.
     */
    public void setUsername(String userName) {
        setName(userName);
    }

    /**
     * @param password The password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return Returns the roles.
     */
    public Set<SecurityRole> getRoles() {
        return roles;
    }

    /**
     * @param roles The roles to set.
     */
    public void setRoles(Set<SecurityRole> roles) {
        this.roles = roles;
    }
    
    /**
     * Make this user a member of the specified {@link SecurityRole}
     * 
     * @param role
     *            the role it should become a member off
     */
    public void addRole(SecurityRole role) {
        role.addUser(this);
        roles.add(role);
    }
    
    /**
     * Delete user's membership from the specified {@link SecurityRole}
     * 
     * @param role
     */
    public void removeRole(SecurityRole role) {
        role.removeUser(this);
        roles.remove(role);
    }
}
