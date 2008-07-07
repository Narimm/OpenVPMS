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
    protected void assembleDO(EntityIdentityDO result,
                              EntityIdentity source,
                              Context context) {
        super.assembleDO(result, source, context);
        result.setIdentity(source.getIdentity());
        Entity entity = source.getEntity();
        EntityDO target = null;
        if (entity != null) {
            Assembler assembler = context.getAssembler();
            target = (EntityDO) assembler.assemble(entity, context);
        }
        result.setEntity(target);
    }

    @Override
    protected void assembleObject(EntityIdentity result,
                                  EntityIdentityDO source,
                                  Context context) {
        super.assembleObject(result, source, context);
        result.setIdentity(source.getIdentity());
    }

    protected EntityIdentity create(EntityIdentityDO object) {
        return new EntityIdentity();
    }

    protected EntityIdentityDO create(EntityIdentity object) {
        return new EntityIdentityDO();
    }
}
