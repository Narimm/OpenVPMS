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
import org.apache.tapestry.valid.PatternValidator;
import org.apache.tapestry.valid.StringValidator;
import org.openvpms.component.business.service.archetype.INodeDescriptor;

/**
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */

public abstract class PropertyEditor extends OpenVpmsComponent {
    
    public abstract INodeDescriptor getDescriptor();

    public abstract void setDescriptor(INodeDescriptor Descriptor);

    /**
     * @param descriptor
     * @return
     */
    public IValidator getValidator(INodeDescriptor descriptor) {
        BaseValidator validator = null;

        if (descriptor.isNumeric()) {
            validator = new NumberValidator();
            ((NumberValidator) validator).setValueTypeClass(descriptor
                    .getType());
            if (descriptor.getMaximumValue() != null)
                ((NumberValidator) validator).setMaximum(descriptor.getMaximumValue());           
            if (descriptor.getMinimumValue() != null)
                ((NumberValidator) validator).setMinimum(descriptor.getMinimumValue());
        } else {
            if (descriptor.getStringPattern() == null)
                validator = new StringValidator();
            else {
                validator = new PatternValidator();
                ((PatternValidator)validator).setPatternString(descriptor.getStringPattern());
            }
        }
        validator.setRequired(descriptor.isRequired());
        return validator;
    }
}
