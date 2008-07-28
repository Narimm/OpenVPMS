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

package org.openvpms.component.business.dao.hibernate.im.common;

import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class IMObjectRelationshipAssembler
        <T extends IMObjectRelationship, DO extends IMObjectRelationshipDO>
        extends IMObjectAssembler<T, DO> {

    private final Class<? extends IMObjectDO> endType;

    private final Class<? extends IMObjectDOImpl> endTypeImpl;

    public IMObjectRelationshipAssembler(
            Class<T> type, Class<DO> typeDO,
            Class<? extends IMObjectDOImpl> impl,
            Class<? extends IMObjectDO> endType,
            Class<? extends IMObjectDOImpl> endTypeImpl) {
        super(type, typeDO, impl);
        this.endType = endType;
        this.endTypeImpl = endTypeImpl;
    }

    @Override
    protected void assembleDO(DO target, T source, DOState state,
                              Context context) {
        super.assembleDO(target, source, state, context);
        assembleSource(target, source, state, context);
        assembleTarget(target, source, state, context);
    }

    @Override
    protected void assembleObject(T target, DO source, Context context) {
        super.assembleObject(target, source, context);
        target.setSource(source.getSource().getObjectReference());
        target.setTarget(source.getTarget().getObjectReference());
    }

    protected void assembleSource(final DO result, final T source,
                                  DOState state, Context context) {
        final IMObjectReference sourceRef = source.getSource();
        if (sourceRef != null) {
            DOState sourceDO = find(sourceRef, endType, endTypeImpl, context);
            if (sourceDO != null) {
                result.setSource(sourceDO.getObject());
                state.addState(sourceDO);
            } else {
                new DeferredAssembler(state, sourceRef) {
                    protected void doAssemble(Context context) {
                        IMObjectDO source = load(sourceRef, endType,
                                                 endTypeImpl, context);
                        result.setSource(source);
                    }
                };
            }
            if (sourceRef.isNew()) {
                new ReferenceUpdater(state, sourceRef) {
                    protected void doUpdate(IMObjectReference updated) {
                        source.setSource(updated);
                    }
                };
            }
        } else {
            result.setSource(null);
        }
    }

    private void assembleTarget(final DO result, final T source, DOState state,
                                Context context) {
        final IMObjectReference targetRef = source.getTarget();
        if (targetRef != null) {
            DOState targetDO = find(targetRef, endType, endTypeImpl, context);
            if (targetDO != null) {
                result.setTarget(targetDO.getObject());
                state.addState(targetDO);
            } else {
                new DeferredAssembler(state, targetRef) {
                    public void doAssemble(Context context) {
                        IMObjectDO target = load(targetRef, endType,
                                                 endTypeImpl, context);
                        result.setTarget(target);
                    }
                };
            }
            if (targetRef.isNew()) {
                new ReferenceUpdater(state, targetRef) {
                    protected void doUpdate(IMObjectReference updated) {
                        source.setTarget(updated);
                    }
                };
            }
        } else {
            result.setTarget(null);
        }
    }

}
