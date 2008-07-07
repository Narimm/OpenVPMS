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

    public IMObjectRelationshipAssembler(Class<T> type, Class<DO> typeDO,
                                         Class<? extends IMObjectDO> endType) {
        super(type, typeDO);
        this.endType = endType;
    }

    @Override
    protected void assembleDO(DO result, T source, Context context
    ) {
        super.assembleDO(result, source, context);
        result.setSource(get(source.getSource(), endType, context));
        result.setTarget(get(source.getTarget(), endType, context));
    }

    @Override
    protected void assembleObject(T result, DO source, Context context) {
        super.assembleObject(result, source, context);
        result.setSource(source.getSource().getObjectReference());
        result.setTarget(source.getTarget().getObjectReference());
    }

}
