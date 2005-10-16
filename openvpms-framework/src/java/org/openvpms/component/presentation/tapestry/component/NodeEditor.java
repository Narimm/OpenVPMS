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

import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry.form.validator.BaseValidator;
import org.apache.tapestry.form.validator.Max;
import org.apache.tapestry.form.validator.MaxLength;
import org.apache.tapestry.form.validator.Min;
import org.apache.tapestry.form.validator.Pattern;
import org.apache.tapestry.form.validator.Required;
import org.openvpms.component.business.service.archetype.descriptor.NodeDescriptor;

/**
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */

public abstract class NodeEditor extends OpenVpmsComponent {

    public abstract NodeDescriptor getDescriptor();

    public abstract void setDescriptor(NodeDescriptor Descriptor);

    /**
     * 
     * TODO Look at this when we get into tapestry
     * 
     * @param descriptor
     * @return IValidator
     * @throws Exception
     *             propagate exception
     */
    public List getValidators(NodeDescriptor descriptor) throws Exception {
        BaseValidator validator = null;
        
        List<BaseValidator> validators = new ArrayList<BaseValidator>();

        if (descriptor.isRequired()) {
            validator = new Required();
            validators.add(validator);
        }
        if (descriptor.isNumeric()) {
            validator = new Pattern();
            ((Pattern)validator).setPattern("#");
            validators.add(validator);
            if (descriptor.getMaxValue() != null) {
                validator = new Max(descriptor.getMaxValue().toString());
                validators.add(validator);
            }

            if (descriptor.getMinValue() != null) {
                validator = new Min(descriptor.getMinValue().toString());
                validators.add(validator);
            }
        } else if  (descriptor.isDate()){
        } else if (descriptor.isString()) {
            if (descriptor.getMaxLength() > 0) {
                validator = new MaxLength();
                ((MaxLength)validator).setMaxLength(descriptor.getMaxLength());
                validators.add(validator);
            }
            if (descriptor.getStringPattern() != null) {
                validator = new Pattern();
                ((Pattern)validator).setPattern(descriptor.getStringPattern());
                validators.add(validator);
            }
        }
        
        return validators;
    }
}
