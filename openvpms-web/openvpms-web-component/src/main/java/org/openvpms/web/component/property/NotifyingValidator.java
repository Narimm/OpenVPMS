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

import java.util.List;

/**
 * A {@link Validator} that notifies {@link ErrorListener}s of any errors encountered whilst performing validation.
 *
 * @author Tim Anderson
 */
public class NotifyingValidator extends AbstractValidator {

    /**
     * The fallback listener, if an {@link Modifiable} has no error listener registered.
     */
    private final ErrorListener fallback;

    /**
     * Constructs a {@link NotifyingValidator}.
     *
     * @param fallback the fallback listener, if a {@link Modifiable} doesn't have a listener registered
     */
    public NotifyingValidator(ErrorListener fallback) {
        this.fallback = fallback;
    }

    /**
     * Validates an object.
     *
     * @param modifiable the object to validate
     * @return {@code true} if the object is valid; otherwise {@code false}
     */
    @Override
    public boolean validate(Modifiable modifiable) {
        ErrorListener listener = modifiable.getErrorListener();
        if (listener != null) {
            listener.clear();
        }
        return super.validate(modifiable);
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
        super.add(modifiable, errors);
        if (!errors.isEmpty()) {
            ErrorListener listener = modifiable.getErrorListener();
            if (listener == null) {
                listener = fallback;
            }
            for (ValidatorError error : errors) {
                listener.error(modifiable, error);
            }
        }
    }
}
