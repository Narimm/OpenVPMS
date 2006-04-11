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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.ValidationError;
import org.openvpms.component.business.service.archetype.ValidationException;


/**
 * Validator for numeric nodes..
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class NumericPropertyHandler extends PropertyHandler {

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
     *
     * @param object the object to convert. May be <code>null</code>
     * @return the converted object, or <code>object</code> if no conversion is
     *         required
     * @throws ValidationException if the object is invalid
     */
    protected Object convert(Object object) throws ValidationException {
        Object result = null;
        if (object instanceof String) {
            String value = (String) object;
            if (!StringUtils.isEmpty(value)) {
                result = convert(value);
            }
        } else {
            // @todo - should convert numerics to target type, if they
            // don't match. Can leave for now as the inputs will always
            // be a string.
            result = object;
        }
        return result;
    }

    /**
     * Convert a string to the required type.
     *
     * @param value the value to convert.
     * @return the converted object
     * @throws ValidationException if the object is invalid
     */
    private Object convert(String value) {
        Object result = null;
        try {
            Class type = getType();
            Constructor constructor = type.getConstructor(String.class);
            result = constructor.newInstance(value);
        } catch (Throwable exception) {
            throwValidationException("Invalid number", exception);
        }
        return result;
    }

    /**
     * Returns the type of the property.
     *
     * @return the type of the property
     * @throws ClassNotFoundException if the class can't be found
     */
    private Class getType() throws ClassNotFoundException {
        String name = getDescriptor().getType();
        return Thread.currentThread().getContextClassLoader().loadClass(name);
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
