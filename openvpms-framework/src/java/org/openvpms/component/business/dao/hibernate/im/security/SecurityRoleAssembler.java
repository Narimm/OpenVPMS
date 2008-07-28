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

import org.openvpms.component.business.dao.hibernate.im.common.Context;
import org.openvpms.component.business.dao.hibernate.im.common.DOState;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectAssembler;
import org.openvpms.component.business.dao.hibernate.im.common.SetAssembler;
import org.openvpms.component.business.domain.im.security.ArchetypeAwareGrantedAuthority;
import org.openvpms.component.business.domain.im.security.SecurityRole;
import org.openvpms.component.business.domain.im.security.User;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SecurityRoleAssembler
        extends IMObjectAssembler<SecurityRole, SecurityRoleDO> {

    private SetAssembler<User, UserDO> USERS
            = SetAssembler.create(User.class, UserDOImpl.class);

    private SetAssembler<ArchetypeAwareGrantedAuthority,
            ArchetypeAuthorityDO> AUTHS
            = SetAssembler.create(ArchetypeAwareGrantedAuthority.class,
                                  ArchetypeAuthorityDOImpl.class);

    public SecurityRoleAssembler() {
        super(SecurityRole.class, SecurityRoleDO.class,
              SecurityRoleDOImpl.class);
    }

    @Override
    protected void assembleDO(SecurityRoleDO target, SecurityRole source,
                              DOState state, Context context) {
        super.assembleDO(target, source, state, context);
        AUTHS.assembleDO(target.getAuthorities(), source.getAuthorities(),
                         state, context);
        USERS.assembleDO(target.getUsers(), source.getUsers(), state, context);
    }

    @Override
    protected void assembleObject(SecurityRole target, SecurityRoleDO source,
                                  Context context) {
        super.assembleObject(target, source, context);
        AUTHS.assembleObject(target.getAuthorities(), source.getAuthorities(),
                             context);
        USERS.assembleObject(target.getUsers(), source.getUsers(), context);
    }

    protected SecurityRole create(SecurityRoleDO object) {
        return new SecurityRole();
    }

    protected SecurityRoleDO create(SecurityRole object) {
        return new SecurityRoleDOImpl();
    }
}
