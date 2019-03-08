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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract implementation of the {@link Validator} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractValidator implements Validator {

    /**
     * Determines if debug is enabled.
     */
    private final boolean debug = log.isDebugEnabled();

    /**
     * Modifiable instances with their corresponding errors.
     */
    private Map<Modifiable, List<ValidatorError>> errors = new HashMap<>();

    /**
     * The logger.
     */
    private static final Logger log = LoggerFactory.getLogger(AbstractValidator.class);

    /**
     * Default constructor.
     */
    protected AbstractValidator() {
        super();
    }

    /**
     * Validates an object.
     *
     * @param modifiable the object to validate
     * @return {@code true} if the object is valid; otherwise {@code false}
     */
    @Override
    public boolean validate(Modifiable modifiable) {
        return modifiable.validate(this);
    }

    /**
     * Adds a validation error for an object. This replaces any existing
     * errors for the object.
     *
     * @param modifiable the object
     * @param error      the validation error
     */
    @Override
    public void add(Modifiable modifiable, ValidatorError error) {
        List<ValidatorError> errors = Collections.singletonList(error);
        add(modifiable, errors);
    }

    /**
     * Adds validation errors for an object. This replaces any existing
     * errors for the object.
     *
     * @param modifiable the object
     * @param errors     the validation errors
     */
    @Override
    public void add(Modifiable modifiable, List<ValidatorError> errors) {
        if (!errors.isEmpty()) {
            if (debug && this.errors.isEmpty()) {
                // log the first error
                try {
                    log.debug(errors.get(0).toString());
                } catch (Throwable exception) {
                    log.debug("Failed to format validation error: " + exception.getMessage(), exception);
                }
            }
            this.errors.put(modifiable, errors);
        }
    }

    /**
     * Returns all invalid objects.
     *
     * @return all invalid objects
     */
    @Override
    public Collection<Modifiable> getInvalid() {
        return errors.keySet();
    }

    /**
     * Returns any errors for an object.
     *
     * @param modifiable the object
     * @return errors associated with {@code modifiable}, or {@code null} if there are no errors
     */
    @Override
    public List<ValidatorError> getErrors(Modifiable modifiable) {
        return errors.get(modifiable);
    }

    /**
     * Returns the first validation error.
     *
     * @return the first error, or {@code null} if none are found
     */
    @Override
    public ValidatorError getFirstError() {
        ValidatorError error = null;
        Collection<Modifiable> invalid = getInvalid();
        if (!invalid.isEmpty()) {
            Modifiable modifiable = invalid.iterator().next();
            List<ValidatorError> errors = getErrors(modifiable);
            if (!errors.isEmpty()) {
                error = errors.get(0);
            }
        }
        return error;
    }
}
