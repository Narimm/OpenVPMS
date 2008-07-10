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
import org.openvpms.component.business.dao.hibernate.im.entity.EntityAssembler;
import org.openvpms.component.business.domain.im.security.User;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class UserAssembler extends EntityAssembler<User, UserDO> {

    public UserAssembler() {
        super(User.class, UserDO.class);
    }

    @Override
    protected void assembleDO(UserDO target, User source,
                                 DOState state, Context context) {
        super.assembleDO(target, source, state, context);
        target.setUsername(source.getUsername());
        target.setPassword(source.getPassword());
    }

    @Override
    protected void assembleObject(User target, UserDO source,
                                  Context context) {
        super.assembleObject(target, source, context);
        target.setUsername(source.getUsername());
        target.setPassword(source.getPassword());
    }

    protected User create(UserDO object) {
        return new User();
    }

    protected UserDO create(User object) {
        return new UserDO();
    }
}
