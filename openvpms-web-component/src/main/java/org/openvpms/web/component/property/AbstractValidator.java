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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.property;

import java.util.Arrays;
import java.util.Collection;
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
     * Modifiable instances with their corresponding errors.
     */
    private Map<Modifiable, List<ValidatorError>> errors = new HashMap<Modifiable, List<ValidatorError>>();

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
        List<ValidatorError> errors = Arrays.asList(error);
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
}
