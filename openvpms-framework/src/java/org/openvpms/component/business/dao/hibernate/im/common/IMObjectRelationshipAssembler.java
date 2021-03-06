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

package org.openvpms.component.business.dao.hibernate.im.common;

import org.hibernate.Hibernate;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.model.object.IMObject;
import org.openvpms.component.model.object.Reference;


/**
 * Assembles {@link IMObjectRelationship} from {@link IMObjectRelationshipDO}s and vice-versa.
 *
 * @author Tim Anderson
 */
public abstract class IMObjectRelationshipAssembler<T extends IMObjectRelationship, DO extends IMObjectRelationshipDO>
        extends IMObjectAssembler<T, DO> {

    /**
     * The relationship source/target data object interface type.
     */
    private final Class<? extends IMObjectDO> endType;

    /**
     * The relationship source/target data object implementation type.
     */
    private final Class<? extends IMObjectDOImpl> endTypeImpl;


    /**
     * Constructs an {@link IMObjectRelationshipAssembler}.
     *
     * @param type        the object type, or {@code null} if the implementation type has no corresponding interface
     * @param typeImpl    the relationship implementation type
     * @param typeDO      the relationship data object interface type
     * @param typeDOImpl  the relationship data object implementation type
     * @param endType     the relationship source/target data object interface type
     * @param endTypeImpl relationship source/target data object implementation type
     */
    public IMObjectRelationshipAssembler(Class<? extends IMObject> type, Class<T> typeImpl, Class<DO> typeDO,
                                         Class<? extends IMObjectDOImpl> typeDOImpl,
                                         Class<? extends IMObjectDO> endType,
                                         Class<? extends IMObjectDOImpl> endTypeImpl) {
        super(type, typeImpl, typeDO, typeDOImpl);
        this.endType = endType;
        this.endTypeImpl = endTypeImpl;
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
    protected void assembleDO(DO target, T source, DOState state, Context context) {
        super.assembleDO(target, source, state, context);
        assembleSource(target, source, state, context);
        assembleTarget(target, source, state, context);
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
        assembleSourceRef(target, source, context);
        assembleTargetRef(target, source, context);
    }

    /**
     * Assembles the source of the relationship.
     *
     * @param result  the relationship being assembled
     * @param source  the relationship to assemble from
     * @param state   the relationship state
     * @param context the assembly context
     */
    private void assembleSource(final DO result, final T source,
                                DOState state, Context context) {
        Reference sourceRef = source.getSource();
        if (sourceRef != null) {
            DOState sourceDO = get(sourceRef, endType, endTypeImpl, context);
            if (sourceDO != null) {
                result.setSource(sourceDO.getObject());
                state.addState(sourceDO);
            } else {
                new DeferredAssembler(state, sourceRef) {
                    protected void doAssemble(Context context) {
                        IMObjectDO source = load(sourceRef, endType, endTypeImpl, context);
                        result.setSource(source);
                    }
                };
            }
            if (sourceRef.isNew()) {
                new ReferenceUpdater(state, sourceRef) {
                    protected void doUpdate(Reference updated) {
                        source.setSource(updated);
                    }
                };
            }
        } else {
            result.setSource(null);
        }
    }

    /**
     * Assembles the target of the relationship.
     *
     * @param result  the relationship being assembled
     * @param source  the relationship to assemble from
     * @param state   the relationship state
     * @param context the assembly context
     */
    private void assembleTarget(final DO result, final T source, DOState state, Context context) {
        Reference targetRef = source.getTarget();
        if (targetRef != null) {
            DOState targetDO = get(targetRef, endType, endTypeImpl, context);
            if (targetDO != null) {
                result.setTarget(targetDO.getObject());
                state.addState(targetDO);
            } else {
                new DeferredAssembler(state, targetRef) {
                    public void doAssemble(Context context) {
                        IMObjectDO target = load(targetRef, endType, endTypeImpl, context);
                        result.setTarget(target);
                    }
                };
            }
            if (targetRef.isNew()) {
                new ReferenceUpdater(state, targetRef) {
                    protected void doUpdate(Reference updated) {
                        source.setTarget(updated);
                    }
                };
            }
        } else {
            result.setTarget(null);
        }
    }

    /**
     * Assembles the relationship source reference.
     *
     * @param result  the relationship to assemble
     * @param source  the source relationship
     * @param context the assembly context
     */
    private void assembleSourceRef(final T result, DO source, Context context) {
        IMObjectDO relSource = source.getSource();
        if (relSource != null) {
            if (Hibernate.isInitialized(relSource)) {
                result.setSource(context.getReference(relSource, endTypeImpl));
            } else {
                context.addDeferredReference(
                        new DeferredReference(relSource, endTypeImpl) {
                            public void update(IMObjectReference reference) {
                                result.setSource(reference);
                            }
                        });
            }
        } else {
            result.setSource(null);
        }
    }

    /**
     * Assembles the relationship target reference.
     *
     * @param result  the relationship to assemble
     * @param source  the source relationship
     * @param context the assembly context
     */
    private void assembleTargetRef(final T result, DO source, Context context) {
        IMObjectDO relTarget = source.getTarget();
        if (relTarget != null) {
            if (Hibernate.isInitialized(relTarget)) {
                result.setTarget(context.getReference(relTarget, endTypeImpl));
            } else {
                context.addDeferredReference(
                        new DeferredReference(relTarget, endTypeImpl) {
                            public void update(IMObjectReference reference) {
                                result.setTarget(reference);
                            }
                        });
            }
        } else {
            result.setTarget(null);
        }
    }

}
