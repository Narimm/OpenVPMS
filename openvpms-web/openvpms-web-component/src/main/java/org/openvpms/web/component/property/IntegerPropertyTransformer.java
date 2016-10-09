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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.property;

import org.openvpms.web.resource.i18n.Messages;

/**
 * Validator for integer nodes.
 *
 * @author Tim Anderson
 */
public class IntegerPropertyTransformer extends NumericPropertyTransformer {

    /**
     * The minimum allowed value, or {@code null} if there is no minimum.
     */
    private Integer min;

    /**
     * The maximum allowed value, or {@code null} if there is no maximum.
     */
    private Integer max;

    /**
     * Constructs an {@link IntegerPropertyTransformer|.
     *
     * @param property the property
     * @param property
     */
    public IntegerPropertyTransformer(Property property) {
        this(property, null, null);
    }

    /**
     * Constructs an {@link IntegerPropertyTransformer}
     *
     * @param property the property
     * @param min      the minimum allowed value
     * @param max      the maximum allowed value
     * @throws IllegalArgumentException if the property does not support integer values
     */
    public IntegerPropertyTransformer(Property property, int min, int max) {
        this(property, (Integer) min, (Integer) max);
    }

    /**
     * Constructs an {@link IntegerPropertyTransformer}
     *
     * @param property the property
     * @param min      the minimum allowed value, or {@code null} if there is no minimum
     * @param max      the maximum allowed value, or {@code null} if there is no minimum
     * @throws IllegalArgumentException if the property does not support integer values
     */
    public IntegerPropertyTransformer(Property property, Integer min, Integer max) {
        super(property);
        this.min = min;
        this.max = max;
        Class type = property.getType();
        if (!type.isAssignableFrom(Integer.class)) {
            throw new IllegalArgumentException("Property '" + property.getName() + "' does not support Integer values");
        }
    }

    /**
     * Converts an object to the required numeric type.
     *
     * @param object   the object to convert
     * @param property the property
     * @return the converted object
     * @throws PropertyException if the object cannot be converted
     */
    @Override
    protected Object convert(Object object, Property property) {
        if (object instanceof String) {
            try {
                return Integer.valueOf(object.toString());
            } catch (NumberFormatException exception) {
                String message = Messages.format("property.error.invalidint", property.getDisplayName());
                throw new PropertyException(property, message, exception);
            }
        } else if (object instanceof Number) {
            return ((Number) object).intValue();
        }
        return super.convert(object, property);
    }

    /**
     * Validates an object.
     *
     * @param object   the object to validate
     * @param property the property
     * @throws PropertyException if the object is invalid
     */
    @Override
    protected void validate(Object object, Property property) {
        super.validate(object, property);
        if (min != null && ((Integer) object).compareTo(min) < 0) {
            String message = Messages.format("property.error.min", property.getDisplayName(), min);
            throw new PropertyException(property, message);
        }

        if (max != null && ((Integer) object).compareTo(max) > 0) {
            String message = Messages.format("property.error.max", property.getDisplayName(), max);
            throw new PropertyException(property, message);
        }
    }
}
