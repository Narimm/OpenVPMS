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

import org.openvpms.component.business.domain.im.common.PeriodRelationship;


/**
 * Assembles {@link PeriodRelationship}s from {@link PeriodRelationshipDO}s
 * and vice-versa.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class PeriodRelationshipAssembler<T extends PeriodRelationship,
        DO extends PeriodRelationshipDO>
        extends IMObjectRelationshipAssembler<T, DO> {

    /**
     * Creates a new <tt>PeriodRelationshipAssembler</tt>.
     *
     * @param type        the relationship type
     * @param typeDO      the relationship data object interface type
     * @param impl        the relationship data object implementation type
     * @param endType     the relationship source/target data object interface
     *                    type
     * @param endTypeImpl relationship source/target data object implementation
     *                    type
     */
    public PeriodRelationshipAssembler(
            Class<T> type, Class<DO> typeDO,
            Class<? extends IMObjectDOImpl> impl,
            Class<? extends IMObjectDO> endType,
            Class<? extends IMObjectDOImpl> endTypeImpl) {
        super(type, typeDO, impl, endType, endTypeImpl);
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
