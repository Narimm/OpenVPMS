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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.property;

import org.openvpms.web.resource.i18n.Messages;

/**
 * Helper {@link Property} that marks the underlying property required.
 *
 * @author Tim Anderson
 */
public class RequiredProperty extends DelegatingProperty {

    /**
     * Constructs a {@link RequiredProperty}
     *
     * @param property the property to delegate to
     */
    public RequiredProperty(Property property) {
        super(property);
    }

    /**
     * Determines if the property is required.
     *
     * @return {@code true}
     */
    @Override
    public boolean isRequired() {
        return true;
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    public boolean validate(Validator validator) {
        boolean valid = super.validate(validator);
        if (valid && getValue() == null) {
            validator.add(this, new ValidatorError(this, Messages.format("property.error.required", getDisplayName())));
            valid = false;
        }
        return valid;
    }
}
