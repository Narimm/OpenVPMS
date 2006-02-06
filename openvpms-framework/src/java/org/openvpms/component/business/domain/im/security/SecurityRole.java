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

// java-core
import java.util.HashSet;
import java.util.Set;

// openvpms-framework
import org.openvpms.component.business.domain.im.common.IMObject;

/**
 * A role is associated with a user and has one or more 
 * {@link ArchetypeAwareGrantedAuthority}
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class SecurityRole extends IMObject {
    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The set of granted authorities for this role
     */
    private Set<ArchetypeAwareGrantedAuthority> authorities =
        new HashSet<ArchetypeAwareGrantedAuthority>();
    
    /**
     * The set of {@link User}s that are members of this role.
     */
    private Set<User> users = new HashSet<User>();
    
    /**
     * Default constructor 
     */
    public SecurityRole() {
        //no-op
    }

    /**
     * @return Returns the authorities.
     */
    public Set<ArchetypeAwareGrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * @param authorities The authorities to set.
     */
    public void setAuthorities(Set<ArchetypeAwareGrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    /**
     * Add the specified authority
     * 
     * @param authority
     *            the authority to add
     */
    public void addAuthority(ArchetypeAwareGrantedAuthority authority) {
        authority.setRole(this);
        authorities.add(authority);
    }
    
    /**
     * Remove the specified authority
     * 
     * @param authority
     *            the authhority to remove
     */
    public void removeAuthority(ArchetypeAwareGrantedAuthority authority) {
        authority.setRole(null);
        authorities.remove(authority);
    }

    /**
     * @return Returns the users.
     */
    public Set<User> getUsers() {
        return users;
    }

    /**
     * @param users The users to set.
     */
    public void setUsers(Set<User> users) {
        this.users = users;
    }

    /**
     * Make the specified {@link User} a member of this role.
     * 
     * @param user
     */
    public void addUser(User user) {
        users.add(user);
    }
    
    /**
     * Remove the specified user so it is no longer a member of this role.
     * 
     * @param user
     */
    public void removeUser(User user) {
        users.remove(user);
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        SecurityRole copy = (SecurityRole)super.clone();
        copy.authorities = new HashSet<ArchetypeAwareGrantedAuthority>(this.authorities);
        copy.users = new HashSet<User>(this.users);
        
        return copy;
    }
}
