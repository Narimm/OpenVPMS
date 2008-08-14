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

import org.openvpms.component.business.dao.hibernate.im.common.Context;
import org.openvpms.component.business.dao.hibernate.im.common.DOState;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectAssembler;
import org.openvpms.component.business.dao.hibernate.im.common.Assembler;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityIdentity;


/**
 * An {@link Assembler} responsible for assembling {@link EntityIdentityDO}
 * instances from {@link EntityIdentity}s and vice-versa.
 *  *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class EntityIdentityAssembler
        extends IMObjectAssembler<EntityIdentity, EntityIdentityDO> {

    /**
     * Creates a new <tt>EntityIdentityAssembler</tt>.
     */
    public EntityIdentityAssembler() {
        super(EntityIdentity.class, EntityIdentityDO.class,
              EntityIdentityDOImpl.class);
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
    protected void assembleDO(EntityIdentityDO target, EntityIdentity source,
                              DOState state, Context context) {
        super.assembleDO(target, source, state, context);
        target.setIdentity(source.getIdentity());

        EntityDO entity = null;
        DOState entityState = getDO(source.getEntity(), context);
        if (entityState != null) {
            entity = (EntityDO) entityState.getObject();
            state.addState(entityState);
        }
        target.setEntity(entity);
    }

    /**
     * Assembles an object from a data object.
     *
     * @param target  the object to assemble
     * @param source  the object to assemble from
     * @param context the assembly context
     */
    @Override
    protected void assembleObject(EntityIdentity target,
                                  EntityIdentityDO source, Context context) {
        super.assembleObject(target, source, context);
        target.setIdentity(source.getIdentity());
        target.setEntity(getObject(source.getEntity(), Entity.class, context));
    }

    /**
     * Creates a new object.
     *
     * @param object the source data object
     * @return a new object corresponding to the supplied data object
     */
    protected EntityIdentity create(EntityIdentityDO object) {
        return new EntityIdentity();
    }

    /**
     * Creates a new data object.
     *
     * @param object the source object
     * @return a new data object corresponding to the supplied object
     */
    protected EntityIdentityDO create(EntityIdentity object) {
        return new EntityIdentityDOImpl();
    }
}
