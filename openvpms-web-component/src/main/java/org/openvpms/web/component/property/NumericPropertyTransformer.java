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

package org.openvpms.web.component.property;

import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.component.system.common.jxpath.OpenVPMSTypeConverter;
import org.openvpms.web.resource.util.Messages;

import java.math.BigDecimal;


/**
 * Validator for numeric nodes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class NumericPropertyTransformer extends AbstractPropertyTransformer {

    /**
     * The type converter.
     */
    private static final OpenVPMSTypeConverter CONVERTER
        = new OpenVPMSTypeConverter();


    /**
     * Constructs a new <tt>NumericPropertyTransformer</tt>.
     *
     * @param property the property
     */
    public NumericPropertyTransformer(Property property) {
        super(property);
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
     * @return the transformed object, or <code>object</code> if no
     *         transformation is required
     * @throws PropertyException if the object is invalid
     */
    public Object apply(Object object) {
        Object result;
        Property property = getProperty();
        try {
            Class type = property.getType();
            result = CONVERTER.convert(object, type);
            if (result instanceof BigDecimal) {
                result = MathRules.round((BigDecimal) result);
            }
        } catch (Throwable exception) {
            String message = Messages.get("property.error.invalidnumeric",
                                          property.getDisplayName());
            throw new PropertyException(property, message, exception);
        }

        return result;
    }

}
