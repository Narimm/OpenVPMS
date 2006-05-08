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

import java.util.ArrayList;
import java.util.List;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.ValidationError;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.component.system.common.jxpath.OpenVPMSTypeConverter;


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
     * Convert the object to the required type.
     * <p/>
     * Notes:
     * <ul>
     *   <li>conversion from one numeric type to another may result
     *      in loss of precision, without error</li>
     *   <li>conversion from a string to an integer type will produce a
     *       ValidationException if the string contains a decimal point.</li>
     * </ul>
     * The inconsistency is tolerable in that all user input is via strings
     * and implici conversion is not desired. 
     *
     * @param object the object to convert. May be <code>null</code>
     * @return the converted object, or <code>object</code> if no conversion is
     *         required
     * @throws ValidationException if the object is invalid
     */
    protected Object convert(Object object) throws ValidationException {
        Object result = null;
        try {
            Class type = getDescriptor().getClazz();
            result = CONVERTER.convert(object, type);
        } catch (Throwable exception) {
            throwValidationException("Invalid number", exception);
        }

        return result;
    }

    /**
     * Helper to throw a validation exception.
     *
     * @param message the message
     * @param cause   the cause. May be <code>null</code>
     * @throws ValidationException
     */
    private void throwValidationException(String message, Throwable cause)
            throws ValidationException {
        ValidationError error = new ValidationError(
                getDescriptor().getName(), message);
        List<ValidationError> errors = new ArrayList<ValidationError>();
        errors.add(error);
        ValidationException.ErrorCode code
                = ValidationException.ErrorCode.FailedToValidObjectAgainstArchetype;
        throw new ValidationException(errors, code, cause);
    }

}
