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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.presentation.tapestry.component;

import org.apache.tapestry.valid.BaseValidator;
import org.apache.tapestry.valid.IValidator;
import org.apache.tapestry.valid.NumberValidator;
import org.apache.tapestry.valid.StringValidator;
import org.openvpms.component.business.service.archetype.IPropertyDescriptor;

/**
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */

public abstract class PropertyEditor extends OvpmsComponent {
    public abstract IPropertyDescriptor getDescriptor();

    public abstract void setDescriptor(IPropertyDescriptor Descriptor);

    /**
     * @param descriptor
     * @return
     */
    public IValidator getValidator(IPropertyDescriptor descriptor) {
        BaseValidator validator = null;

        if (descriptor.isNumeric()) {
            validator = new NumberValidator();
            ((NumberValidator) validator).setValueTypeClass(descriptor
                    .getPropertyType());
        } else {
            validator = new StringValidator();
        }
        validator.setRequired(descriptor.isRequired());
        return validator;
    }
}
