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

package org.openvpms.component.business.dao.hibernate.im.act;

import org.openvpms.component.business.dao.hibernate.im.common.Assembler;
import org.openvpms.component.business.dao.hibernate.im.common.Context;
import org.openvpms.component.business.dao.hibernate.im.common.DOState;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectAssembler;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDOImpl;
import org.openvpms.component.business.dao.hibernate.im.common.SetAssembler;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActIdentity;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.Participation;

import java.util.Set;

/**
 * An {@link Assembler} responsible for assembling {@link ActDO} instances from {@link Act}s and vice-versa.
 *
 * @author Tim Anderson
 */
public abstract class AbstractActAssembler<T extends Act, DO extends ActDO>
        extends IMObjectAssembler<T, DO> {

    /**
     * Assembles sets of identities.
     */
    private static final SetAssembler<ActIdentity, ActIdentityDO> IDENTITIES
            = SetAssembler.create(ActIdentity.class, ActIdentityDO.class);

    /**
     * Assembles sets of participations.
     */
    private static final SetAssembler<Participation, ParticipationDO>
            PARTICIPATIONS = SetAssembler.create(Participation.class, ParticipationDO.class);

    /**
     * Assembles sets of act relationships.
     */
    private static final SetAssembler<ActRelationship, ActRelationshipDO>
            RELATIONSHIPS = SetAssembler.create(ActRelationship.class, ActRelationshipDO.class);

    /**
     * Constructs an {@link AbstractActAssembler}.
     *
     * @param type   the object type
     * @param typeDO the data object interface type
     * @param impl   the data object implementation type
     */
    public AbstractActAssembler(Class<T> type, Class<DO> typeDO, Class<? extends IMObjectDOImpl> impl) {
        super(type, typeDO, impl);
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
        target.setTitle(source.getTitle());
        target.setActivityStartTime(source.getActivityStartTime());
        target.setActivityEndTime(source.getActivityEndTime());
        target.setReason(source.getReason());
        target.setStatus(source.getStatus());
        target.setStatus2(source.getStatus2());
        IDENTITIES.assembleDO(target.getIdentities(), (Set<ActIdentity>) (Set) source.getIdentities(), state, context);
        RELATIONSHIPS.assembleDO(target.getSourceActRelationships(),
                                 (Set<ActRelationship>) (Set) source.getSourceActRelationships(), state, context);
        RELATIONSHIPS.assembleDO(target.getTargetActRelationships(),
                                 (Set<ActRelationship>) (Set) source.getTargetActRelationships(), state, context);
        PARTICIPATIONS.assembleDO(target.getParticipations(), (Set<Participation>) (Set) source.getParticipations(),
                                  state, context);

        // duplicate the act's timestamps to improve performance of queries
        for (ParticipationDO participation : target.getParticipations()) {
            participation.setActivityStartTime(source.getActivityStartTime());
            participation.setActivityEndTime(source.getActivityEndTime());
        }
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
        target.setTitle(source.getTitle());
        target.setActivityStartTime(source.getActivityStartTime());
        target.setActivityEndTime(source.getActivityEndTime());
        target.setReason(source.getReason());
        target.setStatus(source.getStatus());
        target.setStatus2(source.getStatus2());
        IDENTITIES.assembleObject((Set<ActIdentity>) (Set) target.getIdentities(), source.getIdentities(), context);
        RELATIONSHIPS.assembleObject((Set<ActRelationship>) (Set) target.getSourceActRelationships(),
                                     source.getSourceActRelationships(), context);
        RELATIONSHIPS.assembleObject((Set<ActRelationship>) (Set) target.getTargetActRelationships(),
                                     source.getTargetActRelationships(), context);
        PARTICIPATIONS.assembleObject((Set<Participation>) (Set) target.getParticipations(), source.getParticipations(),
                                      context);
    }

}
