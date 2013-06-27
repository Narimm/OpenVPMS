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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.dao.hibernate.im.security;

import org.openvpms.component.business.dao.hibernate.im.common.Context;
import org.openvpms.component.business.dao.hibernate.im.common.DOState;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectAssembler;
import org.openvpms.component.business.dao.hibernate.im.common.SetAssembler;
import org.openvpms.component.business.domain.im.security.ArchetypeAwareGrantedAuthority;
import org.openvpms.component.business.domain.im.security.SecurityRole;
import org.openvpms.component.business.domain.im.security.User;


/**
 * Assembles {@link SecurityRole}s from {@link SecurityRoleDO}s and vice-versa.
 *
 * @author Tim Anderson
 */
public class SecurityRoleAssembler extends IMObjectAssembler<SecurityRole, SecurityRoleDO> {

    /**
     * Assembles sets of users.
     */
    private SetAssembler<User, UserDO> USERS = SetAssembler.create(User.class, UserDO.class, true);

    /**
     * Assembles sets of authorities.
     */
    private SetAssembler<ArchetypeAwareGrantedAuthority, ArchetypeAuthorityDO> AUTHS
            = SetAssembler.create(ArchetypeAwareGrantedAuthority.class, ArchetypeAuthorityDO.class, true);


    /**
     * Constructs an {@link SecurityRoleAssembler}.
     */
    public SecurityRoleAssembler() {
        super(SecurityRole.class, SecurityRoleDO.class, SecurityRoleDOImpl.class);
    }

    /**
     * Assembles a data object from an object.
     *
     * @param target  the object to assemble
     * @param source  the object to assemble from
     * @param state   the data object state
     * @param context the assembly context
     */
    @Override
    protected void assembleDO(SecurityRoleDO target, SecurityRole source, DOState state, Context context) {
        super.assembleDO(target, source, state, context);
        AUTHS.assembleDO(target.getAuthorities(), source.getAuthorities(), state, context);
        USERS.assembleDO(target.getUsers(), source.getUsers(), state, context);
    }

    /**
     * Assembles an object from a data object.
     *
     * @param target  the object to assemble
     * @param source  the object to assemble from
     * @param context the assembly context
     */
    @Override
    protected void assembleObject(SecurityRole target, SecurityRoleDO source, Context context) {
        super.assembleObject(target, source, context);
        AUTHS.assembleObject(target.getAuthorities(), source.getAuthorities(), context);
        USERS.assembleObject(target.getUsers(), source.getUsers(), context);
    }

    /**
     * Creates a new object.
     *
     * @param object the source data object
     * @return a new object corresponding to the supplied data object
     */
    protected SecurityRole create(SecurityRoleDO object) {
        return new SecurityRole();
    }

    /**
     * Creates a new data object.
     *
     * @param object the source object
     * @return a new data object corresponding to the supplied object
     */
    protected SecurityRoleDO create(SecurityRole object) {
        return new SecurityRoleDOImpl();
    }
}
