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
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AssertionDescriptorAssembler
        extends IMObjectAssembler<AssertionDescriptor,
        AssertionDescriptorDO> {

    public AssertionDescriptorAssembler() {
        super(AssertionDescriptor.class, AssertionDescriptorDO.class);
    }

    @Override
    protected void assembleDO(AssertionDescriptorDO result,
                              AssertionDescriptor source,
                              Context context) {
        super.assembleDO(result, source, context);
        result.setErrorMessage(source.getErrorMessage());
        result.setIndex(source.getIndex());
        result.setPropertyMap(source.getPropertyMap());
    }

    @Override
    protected void assembleObject(AssertionDescriptor result,
                                  AssertionDescriptorDO source,
                                  Context context) {
        super.assembleObject(result, source, context);
        result.setErrorMessage(source.getErrorMessage());
        result.setIndex(source.getIndex());
        result.setPropertyMap(source.getPropertyMap());
    }

    protected AssertionDescriptor create(AssertionDescriptorDO object) {
        return new AssertionDescriptor();
    }

    protected AssertionDescriptorDO create(AssertionDescriptor object) {
        return new AssertionDescriptorDO();
    }
}
