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

package org.openvpms.component.business.dao.hibernate.im.act;

import org.openvpms.component.business.dao.hibernate.im.common.Assembler;
import org.openvpms.component.business.dao.hibernate.im.common.Context;
import org.openvpms.component.business.dao.hibernate.im.common.DOState;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectAssembler;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDOImpl;
import org.openvpms.component.business.dao.hibernate.im.common.SetAssembler;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.Participation;


/**
 * An {@link Assembler} responsible for assembling {@link ActDO} instances from
 * {@link Act}s and vice-versa.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractActAssembler<T extends Act, DO extends ActDO>
        extends IMObjectAssembler<T, DO> {

    /**
     * Assembles sets of participations.
     */
    private static final SetAssembler<Participation, ParticipationDO>
            PARTICIPATIONS = SetAssembler.create(Participation.class,
                                                 ParticipationDO.class);

    /**
     * Assembles sets of act relationships.
     */
    private static final SetAssembler<ActRelationship, ActRelationshipDO>
            RELATIONSHIPS = SetAssembler.create(ActRelationship.class,
                                                ActRelationshipDO.class);

    /**
     * Creates a new <tt>AbstractActAssembler</tt>.
     *
     * @param type   the object type
     * @param typeDO the data object interface type
     * @param impl   the data object implementation type
     */
    public AbstractActAssembler(Class<T> type, Class<DO> typeDO,
                                Class<? extends IMObjectDOImpl> impl) {
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
    protected void assembleDO(DO target, T source, DOState state,
                              Context context) {
        super.assembleDO(target, source, state, context);
        target.setTitle(source.getTitle());
        target.setActivityStartTime(source.getActivityStartTime());
        target.setActivityEndTime(source.getActivityEndTime());
        target.setReason(source.getReason());
        target.setStatus(source.getStatus());
        RELATIONSHIPS.assembleDO(target.getSourceActRelationships(),
                                 source.getSourceActRelationships(),
                                 state, context);
        RELATIONSHIPS.assembleDO(target.getTargetActRelationships(),
                                 source.getTargetActRelationships(),
                                 state, context);
        PARTICIPATIONS.assembleDO(target.getParticipations(),
                                  source.getParticipations(),
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
    protected void assembleObject(T target, DO source, Context context) {
        super.assembleObject(target, source, context);
        target.setTitle(source.getTitle());
        target.setActivityStartTime(source.getActivityStartTime());
        target.setActivityEndTime(source.getActivityEndTime());
        target.setReason(source.getReason());
        target.setStatus(source.getStatus());
        RELATIONSHIPS.assembleObject(target.getSourceActRelationships(),
                                     source.getSourceActRelationships(),
                                     context);
        RELATIONSHIPS.assembleObject(target.getTargetActRelationships(),
                                     source.getTargetActRelationships(),
                                     context);
        PARTICIPATIONS.assembleObject(target.getParticipations(),
                                      source.getParticipations(),
                                      context);
    }

}
