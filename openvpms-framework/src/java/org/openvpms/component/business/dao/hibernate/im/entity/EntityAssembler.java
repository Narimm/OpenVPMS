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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.dao.hibernate.im.entity;

import org.openvpms.component.business.dao.hibernate.im.common.Assembler;
import org.openvpms.component.business.dao.hibernate.im.common.Context;
import org.openvpms.component.business.dao.hibernate.im.common.DOState;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectAssembler;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDOImpl;
import org.openvpms.component.business.dao.hibernate.im.common.SetAssembler;
import org.openvpms.component.business.dao.hibernate.im.lookup.LookupDO;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.EntityLink;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.model.object.IMObject;

import java.util.Set;


/**
 * An {@link Assembler} responsible for assembling {@link EntityDO} instances
 * from {@link Entity}s and vice-versa.
 *
 * @author Tim Anderson
 */
public abstract class EntityAssembler<T extends Entity, DO extends EntityDO> extends IMObjectAssembler<T, DO> {

    /**
     * Assembles sets of entity identities.
     */
    private static final SetAssembler<EntityIdentity, EntityIdentityDO> IDENT
            = SetAssembler.create(EntityIdentity.class, EntityIdentityDO.class);

    /**
     * Assembles sets of lookups.
     */
    private static final SetAssembler<Lookup, LookupDO> LOOKUPS
            = SetAssembler.create(Lookup.class, LookupDO.class, true);

    /**
     * Assembles sets of entity relationships.
     */
    private static final SetAssembler<EntityRelationship, EntityRelationshipDO> RELATIONSHIP
            = SetAssembler.create(EntityRelationship.class, EntityRelationshipDO.class);

    /**
     * Assembles sets of entity links.
     */
    private static final SetAssembler<EntityLink, EntityLinkDO> LINKS
            = SetAssembler.create(EntityLink.class, EntityLinkDO.class);


    /**
     * Constructs an {@link EntityAssembler}.
     *
     * @param type     the object type, or {@code null} if the implementation type has no corresponding interface
     * @param typeImpl the object type implementation
     * @param typeDO   the data object interface type
     * @param implDO   the data object implementation type
     */
    public EntityAssembler(Class<? extends IMObject> type, Class<T> typeImpl, Class<DO> typeDO,
                           Class<? extends IMObjectDOImpl> implDO) {
        super(type, typeImpl, typeDO, implDO);
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
    @SuppressWarnings("unchecked")
    protected void assembleDO(DO target, T source, DOState state, Context context) {
        super.assembleDO(target, source, state, context);

        IDENT.assembleDO(target.getIdentities(), (Set<EntityIdentity>) (Set) source.getIdentities(), state, context);

        LOOKUPS.assembleDO(target.getClassifications(), (Set<Lookup>) (Set) source.getClassifications(), state,
                           context);

        RELATIONSHIP.assembleDO(target.getSourceEntityRelationships(),
                                (Set<EntityRelationship>) (Set) source.getSourceEntityRelationships(), state, context);
        RELATIONSHIP.assembleDO(target.getTargetEntityRelationships(),
                                (Set<EntityRelationship>) (Set) source.getTargetEntityRelationships(), state, context);
        LINKS.assembleDO(target.getEntityLinks(), (Set<EntityLink>) (Set) source.getEntityLinks(), state, context);
    }

    /**
     * Assembles an object from a data object.
     *
     * @param target  the object to assemble
     * @param source  the object to assemble from
     * @param context the assembly context
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void assembleObject(T target, DO source, Context context) {
        super.assembleObject(target, source, context);
        IDENT.assembleObject((Set<EntityIdentity>) (Set) target.getIdentities(), source.getIdentities(), context);

        LOOKUPS.assembleObject((Set<Lookup>) (Set) target.getClassifications(), source.getClassifications(), context);

        RELATIONSHIP.assembleObject((Set<EntityRelationship>) (Set) target.getSourceEntityRelationships(),
                                    source.getSourceEntityRelationships(), context);

        RELATIONSHIP.assembleObject((Set<EntityRelationship>) (Set) target.getTargetEntityRelationships(),
                                    source.getTargetEntityRelationships(), context);

        LINKS.assembleObject((Set<EntityLink>) (Set) target.getEntityLinks(), source.getEntityLinks(), context);
    }

}
