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
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.web.resource.util.Messages;

import java.math.BigDecimal;
import java.math.BigInteger;


/**
 * Handler for money properties.
 * todo - workaround for OBF-54
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-30 04:38:04Z $
 */
public class MoneyPropertyTransformer extends AbstractPropertyTransformer {

    /**
     * Constructs a new <tt>MoneyPropertyTransformer</tt>.
     *
     * @param property the property
     */
    public MoneyPropertyTransformer(Property property) {
        super(property);
    }

    /**
     * Transform an object to the required type, performing validation.
     *
     * @param object the object to convert
     * @return the transformed object, or <tt>object</tt> if no transformation
     *         is required
     * @throws PropertyException if the object is invalid
     */
    public Object apply(Object object) {
        BigDecimal result;
        try {
            if (object instanceof String) {
                result = new BigDecimal((String) object);
            } else if (object instanceof BigDecimal) {
                result = new BigDecimal(object.toString());
            } else if (object instanceof BigInteger) {
                result = new BigDecimal((BigInteger) object);
            } else if (object instanceof Short
                    || object instanceof Integer
                    || object instanceof Long) {
                result = new BigDecimal(((Number) object).longValue());
            } else if (object instanceof Float || object instanceof Double) {
                result = new BigDecimal(((Number) object).doubleValue());
            } else {
                throw getException(null);
            }
        } catch (Throwable exception) {
            throw getException(exception);
        }

        return new Money(MathRules.round(result));
    }

    /**
     * Helper to create a new property exception.
     *
     * @param cause the cause. May be <tt>null</tt>
     * @return a new property exception
     */
    private PropertyException getException(Throwable cause) {
        String message = Messages.get("property.error.invalidnumeric",
                                      getProperty().getDisplayName());
        return new PropertyException(getProperty(), message, cause);
    }

}
