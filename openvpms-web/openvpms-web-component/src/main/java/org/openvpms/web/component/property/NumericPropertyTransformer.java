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

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.component.system.common.jxpath.OpenVPMSTypeConverter;
import org.openvpms.web.resource.i18n.Messages;

import java.math.BigDecimal;


/**
 * Validator for numeric nodes.
 *
 * @author Tim Anderson
 */
public class NumericPropertyTransformer extends AbstractPropertyTransformer {

    /**
     * Determines if the value must be positive.
     */
    private final boolean positive;

    /**
     * The no. of decimal places to round to.
     */
    private final int scale;

    /**
     * The type converter.
     */
    private static final OpenVPMSTypeConverter CONVERTER = new OpenVPMSTypeConverter();


    /**
     * Constructs a {@link NumericPropertyTransformer|.
     *
     * @param property the property
     */
    public NumericPropertyTransformer(Property property) {
        this(property, false);
    }

    /**
     * Constructs a {@link NumericPropertyTransformer}.
     *
     * @param property the property
     * @param positive if {@code true}, the value must be {@code >= 0}.
     */
    public NumericPropertyTransformer(Property property, boolean positive) {
        this(property, positive, 2);
    }

    /**
     * Constructs a {@link NumericPropertyTransformer}.
     *
     * @param property the property
     * @param positive if {@code true}, the value must be {@code >= 0}.
     * @param scale    the no. of decimal places to round to
     */
    public NumericPropertyTransformer(Property property, boolean positive, int scale) {
        super(property);
        this.positive = positive;
        this.scale = scale;
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
     * The inconsistency is tolerable in that all user input is via strings
     * and implicit conversion is not desired.
     * <li>BigDecimals are rounded to the application-wide default decimal
     * places using {@link MathRules#round}</li>
     * </ul>
     *
     * @param object the object to convert
     * @return the transformed object, or {@code object} if no transformation is required
     * @throws PropertyException if the object is invalid
     */
    public Object apply(Object object) {
        Object result;
        Property property = getProperty();
        boolean empty = (object == null || (object instanceof String && StringUtils.isEmpty(object.toString())));
        if (empty) {
            if (property.isRequired()) {
                String msg = Messages.format("property.error.required", property.getDisplayName());
                throw new PropertyException(property, msg);
            } else {
                result = null;
            }
        } else {
            result = convert(object, property);
            validate(result, property);
        }

        return result;
    }

    /**
     * Converts an object to the required numeric type.
     *
     * @param object   the object to convert
     * @param property the property
     * @return the converted object
     * @throws PropertyException if the object cannot be converted
     */
    protected Object convert(Object object, Property property) {
        Object result;
        try {
            Class type = property.getType();
            result = CONVERTER.convert(object, type);
            if (result instanceof BigDecimal) {
                result = MathRules.round((BigDecimal) result, scale);
            }
        } catch (Throwable exception) {
            String message = Messages.format("property.error.invalidnumeric", property.getDisplayName());
            throw new PropertyException(property, message, exception);
        }
        return result;
    }

    /**
     * Validates an object.
     *
     * @param object   the object to validate
     * @param property the property
     * @throws PropertyException if the object is invalid
     */
    protected void validate(Object object, Property property) {
        if (positive && object instanceof Number && ((Number) object).doubleValue() < 0) {
            String msg = Messages.format("property.error.positive", property.getDisplayName());
            throw new PropertyException(property, msg);
        }
    }

}
