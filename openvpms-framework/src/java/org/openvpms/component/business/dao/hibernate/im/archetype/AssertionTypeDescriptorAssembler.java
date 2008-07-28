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

package org.openvpms.component.business.dao.hibernate.im.archetype;

import org.openvpms.component.business.dao.hibernate.im.common.Context;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectAssembler;
import org.openvpms.component.business.dao.hibernate.im.common.SetAssembler;
import org.openvpms.component.business.dao.hibernate.im.common.DOState;
import org.openvpms.component.business.domain.im.archetype.descriptor.ActionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AssertionTypeDescriptorAssembler
        extends IMObjectAssembler<AssertionTypeDescriptor,
        AssertionTypeDescriptorDO> {


    private static final
    SetAssembler<ActionTypeDescriptor, ActionTypeDescriptorDO>
            TYPES = SetAssembler.create(ActionTypeDescriptor.class,
                                        ActionTypeDescriptorDOImpl.class);

    public AssertionTypeDescriptorAssembler() {
        super(AssertionTypeDescriptor.class,
              AssertionTypeDescriptorDO.class,
              AssertionTypeDescriptorDOImpl.class);
    }

    @Override
    protected void assembleDO(AssertionTypeDescriptorDO target,
                                 AssertionTypeDescriptor source,
                                 DOState state, Context context) {
        super.assembleDO(target, source, state, context);
        target.setPropertyArchetype(source.getPropertyArchetype());
        TYPES.assembleDO(target.getActionTypes(),
                         source.getActionTypes(),
                         state, context);
    }

    @Override
    protected void assembleObject(AssertionTypeDescriptor target,
                                  AssertionTypeDescriptorDO source,
                                  Context context) {
        super.assembleObject(target, source, context);
        target.setPropertyArchetype(source.getPropertyArchetype());
        TYPES.assembleObject(target.getActionTypes(),
                             source.getActionTypes(),
                             context);
    }

    protected AssertionTypeDescriptor create(AssertionTypeDescriptorDO object) {
        return new AssertionTypeDescriptor();
    }

    protected AssertionTypeDescriptorDO create(AssertionTypeDescriptor object) {
        return new AssertionTypeDescriptorDOImpl();
    }
}
