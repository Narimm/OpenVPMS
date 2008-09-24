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
import org.openvpms.component.business.dao.hibernate.im.common.PeriodRelationshipAssembler;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;


/**
 * Assembles {@link EntityRelationship}s from {@link EntityRelationshipDO}s
 * and vice-versa.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class EntityRelationshipAssembler
        extends PeriodRelationshipAssembler<EntityRelationship,
        EntityRelationshipDO> {

    /**
     * Creates a new <tt>EntityRelationshipAssembler</tt>.
     */
    public EntityRelationshipAssembler() {
        super(EntityRelationship.class, EntityRelationshipDO.class,
              EntityRelationshipDOImpl.class, EntityDO.class,
              EntityDOImpl.class);
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
    protected void assembleDO(EntityRelationshipDO target,
                              EntityRelationship source, DOState state,
                              Context context) {
        super.assembleDO(target, source, state, context);
        EntityIdentityDO identity = null;
        DOState identityState = getDO(source.getIdentity(),
                                      context);
        if (identityState != null) {
            identity = (EntityIdentityDO) identityState.getObject();
            state.addState(identityState);
        }
        target.setIdentity(identity);
        target.setSequence(source.getSequence());
    }

    /**
     * Assembles an object from a data object.
     *
     * @param target  the object to assemble
     * @param source  the object to assemble from
     * @param context the assembly context
     */
    @Override
    protected void assembleObject(EntityRelationship target,
                                  EntityRelationshipDO source,
                                  Context context) {
        super.assembleObject(target, source,context);
        EntityIdentity identity = getObject(source.getIdentity(),
                                            EntityIdentity.class,
                                            context);
        target.setIdentity(identity);
        target.setSequence(source.getSequence());
    }

    /**
     * Creates a new object.
     *
     * @param object the source data object
     * @return a new object corresponding to the supplied data object
     */
    protected EntityRelationship create(EntityRelationshipDO object) {
        return new EntityRelationship();
    }

    /**
     * Creates a new data object.
     *
     * @param object the source object
     * @return a new data object corresponding to the supplied object
     */
    protected EntityRelationshipDO create(EntityRelationship object) {
        return new EntityRelationshipDOImpl();
    }
}
