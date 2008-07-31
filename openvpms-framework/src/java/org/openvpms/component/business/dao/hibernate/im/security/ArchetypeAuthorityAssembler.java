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
import org.openvpms.component.business.domain.im.security.ArchetypeAwareGrantedAuthority;
import org.openvpms.component.business.domain.im.security.SecurityRole;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ArchetypeAuthorityAssembler
        extends IMObjectAssembler<ArchetypeAwareGrantedAuthority,
        ArchetypeAuthorityDO> {

    public ArchetypeAuthorityAssembler() {
        super(ArchetypeAwareGrantedAuthority.class,
              ArchetypeAuthorityDO.class,
              ArchetypeAuthorityDOImpl.class);
    }

    @Override
    protected void assembleDO(ArchetypeAuthorityDO target,
                              ArchetypeAwareGrantedAuthority source,
                              DOState state, Context context) {
        super.assembleDO(target, source, state, context);
        target.setMethod(source.getMethod());
        SecurityRoleDO role = null;
        DOState roleState = getDO(source.getRole(),
                                  context);
        if (roleState != null) {
            role = (SecurityRoleDO) roleState.getObject();
            state.addState(roleState);
        }
        target.setRole(role);
        target.setServiceName(source.getServiceName());
        target.setMethod(source.getMethod());
        target.setShortName(source.getArchetypeShortName());
    }

    @Override
    protected void assembleObject(ArchetypeAwareGrantedAuthority target,
                                  ArchetypeAuthorityDO source,
                                  Context context) {
        super.assembleObject(target, source, context);
        target.setMethod(source.getMethod());
        target.setRole(
                getObject(source.getRole(), SecurityRole.class, context));
        target.setServiceName(source.getServiceName());
        target.setMethod(source.getMethod());
        target.setArchetypeShortName(source.getShortName());
    }

    protected ArchetypeAwareGrantedAuthority create(
            ArchetypeAuthorityDO object) {
        return new ArchetypeAwareGrantedAuthority();
    }

    protected ArchetypeAuthorityDO create(
            ArchetypeAwareGrantedAuthority object) {
        return new ArchetypeAuthorityDOImpl();
    }
}
