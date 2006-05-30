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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.edit;

import org.openvpms.web.resource.util.Messages;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.ValidationError;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.component.system.common.jxpath.OpenVPMSTypeConverter;

import java.util.ArrayList;
import java.util.List;


/**
 * Validator for numeric nodes..
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class NumericPropertyHandler extends PropertyHandler {

    /**
     * The type converter.
     */
    private static final OpenVPMSTypeConverter CONVERTER
            = new OpenVPMSTypeConverter();


    /**
     * Construct a new <code>NumericPropertyHandler</code>.
     *
     * @param descriptor the node descriptor.
     */
    public NumericPropertyHandler(NodeDescriptor descriptor) {
        super(descriptor);
    }

    /**
     * Transform an object to the required type, performing validation.
     * <p/>
     * Notes:
     * <ul>
     * <li>conversion from one numeric type to another may result
     * in loss of precision, without error</li>
     * <li>conversion from a string to an integer type will produce a
     * ValidationException if the string contains a decimal point.</li>
     * </ul>
     * The inconsistency is tolerable in that all user input is via strings
     * and implicit conversion is not desired.
     *
     * @param object the object to convert
     * @return the transformed object, or <code>object</code> if no
     *         transformation is required
     * @throws ValidationException if the object is invalid
     */
    public Object apply(Object object) throws ValidationException {
        Object result;
        try {
            Class type = getDescriptor().getClazz();
            result = CONVERTER.convert(object, type);
        } catch (Throwable exception) {
            NodeDescriptor node = getDescriptor();
            String message = Messages.get("node.error.invalidnumeric",
                                          node.getDisplayName());
            ValidationError error = new ValidationError(node.getName(),
                                                        message);
            List<ValidationError> errors = new ArrayList<ValidationError>();
            errors.add(error);
            ValidationException.ErrorCode code
                    = ValidationException.ErrorCode.FailedToValidObjectAgainstArchetype;
            throw new ValidationException(errors, code, exception);
        }

        return result;
    }

}
