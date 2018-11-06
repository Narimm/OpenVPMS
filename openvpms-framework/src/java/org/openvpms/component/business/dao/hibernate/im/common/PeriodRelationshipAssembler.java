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

import org.openvpms.component.business.domain.im.common.PeriodRelationship;
import org.openvpms.component.model.object.IMObject;


/**
 * Assembles {@link PeriodRelationship}s from {@link PeriodRelationshipDO}s and vice-versa.
 *
 * @author Tim Anderson
 */
public abstract class PeriodRelationshipAssembler<T extends PeriodRelationship, DO extends PeriodRelationshipDO>
        extends IMObjectRelationshipAssembler<T, DO> {

    /**
     * Constructs a {@link PeriodRelationshipAssembler}.
     *
     * @param type        the object type, or {@code null} if the implementation type has no corresponding interface
     * @param typeImpl    the relationship implementation type
     * @param typeDO      the relationship data object interface type
     * @param typeDOImpl  the relationship data object implementation type
     * @param endType     the relationship source/target data object interface type
     * @param endTypeImpl relationship source/target data object implementation type
     */
    public PeriodRelationshipAssembler(Class<? extends IMObject> type, Class<T> typeImpl, Class<DO> typeDO,
                                       Class<? extends IMObjectDOImpl> typeDOImpl, Class<? extends IMObjectDO> endType,
                                       Class<? extends IMObjectDOImpl> endTypeImpl) {
        super(type, typeImpl, typeDO, typeDOImpl, endType, endTypeImpl);
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
        target.setActiveStartTime(source.getActiveStartTime());
        target.setActiveEndTime(source.getActiveEndTime());
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
        target.setActiveStartTime(source.getActiveStartTime());
        target.setActiveEndTime(source.getActiveEndTime());
    }
}
