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
import org.openvpms.component.business.dao.hibernate.im.common.SetAssembler;
import org.openvpms.component.business.dao.hibernate.im.entity.EntityAssembler;
import org.openvpms.component.business.domain.im.security.SecurityRole;
import org.openvpms.component.business.domain.im.security.User;


/**
 * Assembles {@link User}s from {@link UserDO}s and vice-versa.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class UserAssembler extends EntityAssembler<User, UserDO> {

    /**
     * Assembles sets of roles.
     */
    private static final SetAssembler<SecurityRole, SecurityRoleDO> ROLES
            = SetAssembler.create(
            SecurityRole.class, SecurityRoleDO.class, true);


    /**
     * Creates a new <tt>UserAssembler</tt>.
     */
    public UserAssembler() {
        super(User.class, UserDO.class, UserDOImpl.class);
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
    protected void assembleDO(UserDO target, User source, DOState state,
                              Context context) {
        super.assembleDO(target, source, state, context);
        target.setUsername(source.getUsername());
        target.setPassword(source.getPassword());
        ROLES.assembleDO(target.getRoles(), source.getRoles(),
                         state, context);
    }

    /**
     * Assembles an object from a data object.
     *
     * @param target  the object to assemble
     * @param source  the object to assemble from
     * @param context the assembly context
     */
    @Override
    protected void assembleObject(User target, UserDO source, Context context) {
        super.assembleObject(target, source, context);
        target.setUsername(source.getUsername());
        target.setPassword(source.getPassword());
        ROLES.assembleObject(target.getRoles(), source.getRoles(), context);
    }

    /**
     * Creates a new object.
     *
     * @param object the source data object
     * @return a new object corresponding to the supplied data object
     */
    protected User create(UserDO object) {
        return new User();
    }

    /**
     * Creates a new data object.
     *
     * @param object the source object
     * @return a new data object corresponding to the supplied object
     */
    protected UserDO create(User object) {
        return new UserDOImpl();
    }
}
