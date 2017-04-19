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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.dao.hibernate.im.act;

import org.openvpms.component.business.dao.hibernate.im.common.Assembler;
import org.openvpms.component.business.dao.hibernate.im.common.Context;
import org.openvpms.component.business.dao.hibernate.im.common.DOState;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectAssembler;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActIdentity;


/**
 * An {@link Assembler} responsible for assembling {@link ActIdentityDO} instances from {@link ActIdentity}s and
 * vice-versa.
 *
 * @author Tim Anderson
 */
public class ActIdentityAssembler extends IMObjectAssembler<ActIdentity, ActIdentityDO> {

    /**
     * Constructs an {@link ActIdentityAssembler}.
     */
    public ActIdentityAssembler() {
        super(ActIdentity.class, ActIdentityDO.class, ActIdentityDOImpl.class);
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
    protected void assembleDO(ActIdentityDO target, ActIdentity source, DOState state, Context context) {
        super.assembleDO(target, source, state, context);
        target.setIdentity(source.getIdentity());

        ActDO act = null;
        DOState actState = getDO(source.getAct(), context);
        if (actState != null) {
            act = (ActDO) actState.getObject();
            state.addState(actState);
        }
        target.setAct(act);
    }

    /**
     * Assembles an object from a data object.
     *
     * @param target  the object to assemble
     * @param source  the object to assemble from
     * @param context the assembly context
     */
    @Override
    protected void assembleObject(ActIdentity target, ActIdentityDO source, Context context) {
        super.assembleObject(target, source, context);
        target.setIdentity(source.getIdentity());
        target.setAct(getObject(source.getAct(), Act.class, context));
    }

    /**
     * Creates a new object.
     *
     * @param object the source data object
     * @return a new object corresponding to the supplied data object
     */
    protected ActIdentity create(ActIdentityDO object) {
        return new ActIdentity();
    }

    /**
     * Creates a new data object.
     *
     * @param object the source object
     * @return a new data object corresponding to the supplied object
     */
    protected ActIdentityDO create(ActIdentity object) {
        return new ActIdentityDOImpl();
    }
}
