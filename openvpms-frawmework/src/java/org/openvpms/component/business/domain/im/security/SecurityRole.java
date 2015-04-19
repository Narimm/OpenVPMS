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


package org.openvpms.component.business.domain.im.security;

import org.openvpms.component.business.domain.im.common.IMObject;

import java.util.HashSet;
import java.util.Set;

/**
 * A role is associated with a user and has one or more {@link ArchetypeAwareGrantedAuthority}.
 *
 * @author Jim Alateras
 * @author Tim Aderson
 */
public class SecurityRole extends IMObject {

    /**
     * Default SUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The set of granted authorities for this role.
     */
    private Set<ArchetypeAwareGrantedAuthority> authorities = new HashSet<ArchetypeAwareGrantedAuthority>();


    /**
     * Default constructor.
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
     * Add the specified authority.
     *
     * @param authority the authority to add
     */
    public void addAuthority(ArchetypeAwareGrantedAuthority authority) {
        authorities.add(authority);
    }

    /**
     * Remove the specified authority.
     *
     * @param authority the authority to remove
     */
    public void removeAuthority(ArchetypeAwareGrantedAuthority authority) {
        authorities.remove(authority);
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        SecurityRole copy = (SecurityRole) super.clone();
        copy.authorities = new HashSet<ArchetypeAwareGrantedAuthority>(this.authorities);
        return copy;
    }
}
