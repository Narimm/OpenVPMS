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

import org.hibernate.Hibernate;
import org.openvpms.component.business.dao.hibernate.im.common.Assembler;
import org.openvpms.component.business.dao.hibernate.im.common.Context;
import org.openvpms.component.business.dao.hibernate.im.common.DOState;
import org.openvpms.component.business.dao.hibernate.im.common.DeferredAssembler;
import org.openvpms.component.business.dao.hibernate.im.common.DeferredReference;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectAssembler;
import org.openvpms.component.business.dao.hibernate.im.common.ReferenceUpdater;
import org.openvpms.component.business.dao.hibernate.im.entity.EntityDO;
import org.openvpms.component.business.dao.hibernate.im.entity.EntityDOImpl;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;


/**
 * An {@link Assembler} responsible for assembling {@link ParticipationDO}
 * instances from {@link Participation}s and vice-versa.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ParticipationAssembler
        extends IMObjectAssembler<Participation, ParticipationDO> {

    /**
     * Creates a new <tt>ParticipationAssembler</tt>.
     */
    public ParticipationAssembler() {
        super(Participation.class, ParticipationDO.class,
              ParticipationDOImpl.class);
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
    protected void assembleDO(ParticipationDO target, Participation source,
                              DOState state, Context context) {
        super.assembleDO(target, source, state, context);
        assembleEntity(target, source, state, context);
        assembleAct(target, source, state, context);
    }

    /**
     * Assembles an object from a data object.
     *
     * @param target  the object to assemble
     * @param source  the object to assemble from
     * @param context the assembly context
     */
    @Override
    protected void assembleObject(Participation target, ParticipationDO source,
                                  Context context) {
        super.assembleObject(target, source, context);
        assembleEntityRef(target, source, context);
        assembleActRef(target, source, context);
    }

    /**
     * Creates a new object.
     *
     * @param object the source data object
     * @return a new object corresponding to the supplied data object
     */
    protected Participation create(ParticipationDO object) {
        return new Participation();
    }

    /**
     * Creates a new data object.
     *
     * @param object the source object
     * @return a new data object corresponding to the supplied object
     */
    protected ParticipationDO create(Participation object) {
        return new ParticipationDOImpl();
    }

    /**
     * Assembles the entity of a participation.
     *
     * @param target  the target participation
     * @param source  the source participation
     * @param state   the target state
     * @param context the assembly context
     */
    private void assembleEntity(final ParticipationDO target,
                                final Participation source,
                                DOState state, Context context) {
        final IMObjectReference entityRef = source.getEntity();
        if (entityRef != null) {
            DOState entityDO = get(entityRef, EntityDO.class,
                                   EntityDOImpl.class, context);
            if (entityDO != null) {
                target.setEntity((EntityDO) entityDO.getObject());
                state.addState(entityDO);
            } else {
                new DeferredAssembler(state, entityRef) {
                    public void doAssemble(Context context) {
                        target.setEntity(load(entityRef, EntityDO.class,
                                              EntityDOImpl.class, context));
                    }
                };
            }
            if (entityRef.isNew()) {
                new ReferenceUpdater(state, entityRef) {
                    protected void doUpdate(IMObjectReference updated) {
                        source.setEntity(updated);
                    }
                };
            }
        } else {
            target.setEntity(null);
        }
    }

    /**
     * Assembles the act of a participation.
     *
     * @param target  the target participation
     * @param source  the source participation
     * @param state   the target state
     * @param context the assembly context
     */
    private void assembleAct(final ParticipationDO target,
                             final Participation source,
                             DOState state, Context context) {
        final IMObjectReference actRef = source.getAct();
        if (actRef != null) {
            DOState actDO = get(actRef, ActDO.class, ActDOImpl.class, context);
            if (actDO != null) {
                target.setAct((ActDO) actDO.getObject());
                state.addState(actDO);
            } else {
                new DeferredAssembler(state, actRef) {
                    public void doAssemble(Context context) {
                        target.setAct(load(actRef, ActDO.class, ActDOImpl.class,
                                           context));
                    }
                };
            }
            if (actRef.isNew()) {
                new ReferenceUpdater(state, actRef) {
                    protected void doUpdate(IMObjectReference updated) {
                        source.setAct(updated);
                    }
                };
            }
        } else {
            target.setAct(null);
        }
    }

    /**
     * Assembles the participation entity reference.
     *
     * @param result  the participation to assemble
     * @param source  the source participation
     * @param context the assembly context
     */
    private void assembleEntityRef(final Participation result,
                                   ParticipationDO source, Context context) {
        EntityDO entity = source.getEntity();
        if (entity != null) {
            if (Hibernate.isInitialized(entity)) {
                result.setEntity(context.getReference(entity,
                                                      EntityDOImpl.class));
            } else {
                context.addDeferredReference(
                        new DeferredReference(entity, EntityDOImpl.class) {
                            public void update(IMObjectReference reference) {
                                result.setEntity(reference);
                            }
                        });
            }
        } else {
            result.setEntity(null);
        }
    }

    /**
     * Assembles the participation act reference.
     *
     * @param result  the participation to assemble
     * @param source  the source participation
     * @param context the assembly context
     */
    private void assembleActRef(final Participation result,
                                ParticipationDO source, Context context) {
        ActDO act = source.getAct();
        if (act != null) {
            if (Hibernate.isInitialized(act)) {
                result.setAct(context.getReference(act, ActDOImpl.class));
            } else {
                context.addDeferredReference(
                        new DeferredReference(act, ActDOImpl.class) {
                            public void update(IMObjectReference reference) {
                                result.setAct(reference);
                            }
                        });
            }
        } else {
            result.setAct(null);
        }
    }
}
