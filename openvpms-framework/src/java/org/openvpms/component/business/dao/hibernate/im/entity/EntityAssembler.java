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
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectAssembler;
import org.openvpms.component.business.dao.hibernate.im.common.SetAssembler;
import org.openvpms.component.business.dao.hibernate.im.common.DOState;
import org.openvpms.component.business.dao.hibernate.im.lookup.LookupDO;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.lookup.Lookup;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class EntityAssembler<T extends Entity, DO extends EntityDO>
        extends IMObjectAssembler<T, DO> {

    private static final SetAssembler<EntityIdentity, EntityIdentityDO>
            IDENT = SetAssembler.create(EntityIdentity.class,
                                        EntityIdentityDO.class);

    private static final SetAssembler<Lookup, LookupDO> LOOKUPS
            = SetAssembler.create(Lookup.class, LookupDO.class);

    private static final SetAssembler<EntityRelationship, EntityRelationshipDO>
            RELATIONSHIP = SetAssembler.create(EntityRelationship.class,
                                               EntityRelationshipDO.class);


    public EntityAssembler(Class<T> type, Class<DO> typeDO) {
        super(type, typeDO);
    }

    @Override
    protected void assembleDO(DO target, T source, DOState state,
                                 Context context) {
        super.assembleDO(target, source, state, context);

        IDENT.assembleDO(target.getIdentities(), source.getIdentities(),
                         state, context);

        LOOKUPS.assembleDO(target.getClassifications(),
                           source.getClassifications(),
                           state, context);

        RELATIONSHIP.assembleDO(target.getSourceEntityRelationships(),
                                source.getSourceEntityRelationships(),
                                state, context);

        RELATIONSHIP.assembleDO(target.getTargetEntityRelationships(),
                                source.getTargetEntityRelationships(),
                                state, context);
    }

    @Override
    protected void assembleObject(T target, DO source, Context context) {
        super.assembleObject(target, source, context);
        IDENT.assembleObject(target.getIdentities(), source.getIdentities(),
                             context);

        LOOKUPS.assembleObject(target.getClassifications(),
                               source.getClassifications(),
                               context);

        RELATIONSHIP.assembleObject(target.getSourceEntityRelationships(),
                                    source.getSourceEntityRelationships(),
                                    context);

        RELATIONSHIP.assembleObject(target.getTargetEntityRelationships(),
                                    source.getTargetEntityRelationships(),
                                    context);
    }

}
