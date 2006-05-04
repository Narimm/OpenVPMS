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

import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.ValidationError;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.web.spring.ServiceHelper;


/**
 * PropertyHandler is responsible for processing user input prior to it being
 * set on {@link Property}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $Revision$ $Date$
 */
public abstract class PropertyHandler implements Validator {

    /**
     * The node descriptor.
     */
    private final NodeDescriptor _descriptor;


    /**
     * Construct a new <code>PropertyHandler</code>.
     *
     * @param descriptor the node descriptor.
     */
    public PropertyHandler(NodeDescriptor descriptor) {
        _descriptor = descriptor;
    }

    /**
     * Transform an object to the required type, performing validation.
     *
     * @param object the object to convert
     * @return the transformed object, or <code>object</code> if no
     *         transformation is required
     * @throws ValidationException if the object is invalid
     */
    public Object apply(Object object) throws ValidationException {
        Object value = convert(object);
        List<ValidationError> errors = validate(value);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors,
                                          ValidationException.ErrorCode.FailedToValidObjectAgainstArchetype);
        }
        return value;
    }

    /**
     * Perform validation, returning a list of errors if the object is invalid.
     *
     * @param value the value to validate
     * @return a list of error messages if the object is invalid; or an empty
     *         list if valid
     */
    public List<ValidationError> validate(Object value) {
        List<ValidationError> errors = new ArrayList<ValidationError>();

        if (value != null) {
            // only check the assertions for non-null values
            IArchetypeService service = ServiceHelper.getArchetypeService();

            for (AssertionDescriptor assertion :
                    _descriptor.getAssertionDescriptorsAsArray()) {
                AssertionTypeDescriptor assertionType =
                        service.getAssertionTypeDescriptor(assertion.getName());

                // @todo
                // no validation required where the type is not specified.
                // This is currently a work around since we need to deal
                // with assertions and some other type of declaration.
                if (assertionType.getActionType("assert") != null) {
                    checkAssertion(assertionType, value, assertion, errors);
                }
            }
        }
        return errors;
    }

    /**
     * Determines if the object is valid.
     *
     * @param value the value to validate
     * @return <code>true</code> if the object is valid; otherwise
     *         <code>false</code>
     */
    public boolean isValid(Object value) {
        return validate(value).isEmpty();
    }

    /**
     * Returns the node descriptor.
     *
     * @return the node descriptor
     */
    public NodeDescriptor getDescriptor() {
        return _descriptor;
    }

    /**
     * Extracts error mesages from a validation exception.
     */
    protected List<String> getErrors(ValidationException exception) {
        List<ValidationError> errors = exception.getErrors();
        List<String> result = new ArrayList<String>(errors.size());
        for (ValidationError error : errors) {
            result.add(error.getErrorMessage());
        }
        return result;
    }

    /**
     * Convert the object to the required type.
     *
     * @param object the object to convert. May be <code>null</code>
     * @return the converted object, or <code>object</code> if no conversion is
     *         required
     * @throws ValidationException if the object is invalid
     */
    protected abstract Object convert(Object object)
            throws ValidationException;

    /**
     * Check an assertion.
     *
     * @param type      the assertion type
     * @param value     the value to check
     * @param assertion the assertion
     * @param errors    the list of errors to populate
     */
    private void checkAssertion(AssertionTypeDescriptor type,
                                Object value, AssertionDescriptor assertion,
                                List<ValidationError> errors) {
        try {
            if (!type.validate(value, _descriptor, assertion)) {
                errors.add(createError(assertion.getErrorMessage()));
            }
        } catch (Exception exception) {
            errors.add(createError(assertion.getErrorMessage()));
        }
    }

    /**
     * Helper to create a validation error.
     *
     * @param message the message
     * @return a new validation error
     */
    private ValidationError createError(String message) {
        return new ValidationError(_descriptor.getName(), message);
    }
}
