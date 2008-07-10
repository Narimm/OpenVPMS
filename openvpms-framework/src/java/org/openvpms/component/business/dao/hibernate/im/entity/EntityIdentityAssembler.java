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

package org.openvpms.component.business.dao.hibernate.im.entity;

import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectAssembler;
import org.openvpms.component.business.dao.hibernate.im.common.Context;
import org.openvpms.component.business.dao.hibernate.im.common.Assembler;
import org.openvpms.component.business.dao.hibernate.im.common.DOState;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class EntityIdentityAssembler
        extends IMObjectAssembler<EntityIdentity, EntityIdentityDO> {

    public EntityIdentityAssembler() {
        super(EntityIdentity.class, EntityIdentityDO.class);
    }

    @Override
    protected void assembleDO(EntityIdentityDO target, EntityIdentity source,
                                 DOState state, Context context) {
        super.assembleDO(target, source, state, context);
        target.setIdentity(source.getIdentity());
        Entity entity = source.getEntity();
        EntityDO targetEntity = null;
        if (entity != null) {
            Assembler assembler = context.getAssembler();
            DOState entityState = assembler.assemble(entity, context);
            targetEntity = (EntityDO) entityState.getObject();
        }
        target.setEntity(targetEntity);
    }

    @Override
    protected void assembleObject(EntityIdentity target,
                                  EntityIdentityDO source,
                                  Context context) {
        super.assembleObject(target, source, context);
        target.setIdentity(source.getIdentity());
    }

    protected EntityIdentity create(EntityIdentityDO object) {
        return new EntityIdentity();
    }

    protected EntityIdentityDO create(EntityIdentity object) {
        return new EntityIdentityDO();
    }
}
