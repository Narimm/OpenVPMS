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
 * Assembles {@link ActRelationship} from {@link ActRelationshipDO}s
 * and vice-versa.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ActRelationshipAssembler
        extends PeriodRelationshipAssembler<ActRelationship,
        ActRelationshipDO> {

    /**
     * Creates a new <tt>ActRelationshipAssembler</tt>.
     */
    public ActRelationshipAssembler() {
        super(ActRelationship.class, ActRelationshipDO.class,
              ActRelationshipDOImpl.class, ActDO.class, ActDOImpl.class);
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
    protected void assembleDO(ActRelationshipDO target, ActRelationship source,
                              DOState state, Context context) {
        super.assembleDO(target, source, state, context);
        target.setParentChildRelationship(source.isParentChildRelationship());
    }

    /**
     * Assembles an object from a data object.
     *
     * @param target  the object to assemble
     * @param source  the object to assemble from
     * @param context the assembly context
     */
    @Override
    protected void assembleObject(ActRelationship target,
                                  ActRelationshipDO source, Context context) {
        super.assembleObject(target, source, context);
        target.setParentChildRelationship(source.isParentChildRelationship());
    }

    /**
     * Creates a new object.
     *
     * @param object the source data object
     * @return a new object corresponding to the supplied data object
     */
    protected ActRelationship create(ActRelationshipDO object) {
        return new ActRelationship();
    }

    /**
     * Creates a new data object.
     *
     * @param object the source object
     * @return a new data object corresponding to the supplied object
     */
    protected ActRelationshipDO create(ActRelationship object) {
        return new ActRelationshipDOImpl();
    }
}
