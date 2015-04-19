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

import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDOImpl;
import org.openvpms.component.business.domain.archetype.ArchetypeId;

import java.util.HashSet;
import java.util.Set;


/**
 * Implementation of the {@link SecurityRoleDO} class.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public class SecurityRoleDOImpl extends IMObjectDOImpl implements SecurityRoleDO {

    /**
     * The set of granted authorities for this role.
     */
    private Set<ArchetypeAuthorityDO> authorities = new HashSet<ArchetypeAuthorityDO>();


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
        authorities.add(authority);
    }

    /**
     * Removes an authority.
     *
     * @param authority the authority to remove
     */
    public void removeAuthority(ArchetypeAuthorityDO authority) {
        authorities.remove(authority);
    }

    /**
     * Sets the authorities.
     *
     * @param authorities the authorities to set
     */
    protected void setAuthorities(Set<ArchetypeAuthorityDO> authorities) {
        this.authorities = authorities;
    }

}
