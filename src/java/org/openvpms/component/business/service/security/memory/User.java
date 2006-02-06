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


package org.openvpms.component.business.service.security.memory;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.userdetails.UserDetails;
import org.openvpms.component.business.domain.im.common.IMObject;

/**
 * This class represents the user details and the list of associated 
 * authorities.
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class User extends IMObject implements UserDetails {

    /**
     * Default UID
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * The user's name
     */
    private String username;
    
    /**
     * The user's password
     */
    private String password;
    
    /**
     * The array of granted authorities for the user
     */
    private GrantedAuthority[] authorities;
    
    
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
     * @param authorities                        
     */
    public User(String username, String password, boolean active, 
            GrantedAuthority[] authorities) {
        this.username = username;
        this.password = password;
        this.authorities = authorities;
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
        // TODO Auto-generated method stub
        return authorities;
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
        return username;
    }

    /**
     * @param userName The userName to set.
     */
    public void setUsername(String userName) {
        this.username = userName;
    }

    /**
     * @param authorities The authorities to set.
     */
    public void setAuthorities(GrantedAuthority[] authorities) {
        this.authorities = authorities;
    }

    /**
     * @param password The password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
