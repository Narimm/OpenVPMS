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

import org.openvpms.component.business.dao.hibernate.im.common.Context;
import org.openvpms.component.business.dao.hibernate.im.common.DOState;
import org.openvpms.component.business.dao.hibernate.im.common.PeriodRelationshipAssembler;
import org.openvpms.component.business.domain.im.act.ActRelationship;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ActRelationshipAssembler
        extends PeriodRelationshipAssembler<ActRelationship, 
        ActRelationshipDO> {

    public ActRelationshipAssembler() {
        super(ActRelationship.class, ActRelationshipDO.class,
              ActRelationshipDOImpl.class, ActDO.class, ActDOImpl.class);
    }

    @Override
    protected void assembleDO(ActRelationshipDO target,
                              ActRelationship source,
                              DOState state, Context context) {
        super.assembleDO(target, source, state, context);
        target.setParentChildRelationship(source.isParentChildRelationship());
    }

    @Override
    protected void assembleObject(ActRelationship target,
                                  ActRelationshipDO source,
                                  Context context) {
        super.assembleObject(target, source, context);
        target.setParentChildRelationship(source.isParentChildRelationship());
    }

    protected ActRelationship create(ActRelationshipDO object) {
        return new ActRelationship();
    }

    protected ActRelationshipDO create(ActRelationship object) {
        return new ActRelationshipDOImpl();
    }
}
