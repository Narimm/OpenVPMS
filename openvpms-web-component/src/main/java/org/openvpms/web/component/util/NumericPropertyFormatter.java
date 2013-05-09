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
 */

package org.openvpms.web.component.util;

import org.openvpms.web.component.property.Property;
import org.openvpms.web.resource.i18n.format.NumberFormatter;

import java.math.BigDecimal;
import java.text.NumberFormat;


/**
 * Helper to format numeric properties.
 *
 * @author Tim Anderson
 */
public class NumericPropertyFormatter {


    /**
     * Format a number according to its node descriptor.
     *
     * @param value    the number to format
     * @param property the property
     * @param edit     if <tt>true</tt> format the number for editing
     * @return the formatted number
     */
    public static String format(Number value, Property property, boolean edit) {
        NumberFormat format = getFormat(property, edit);
        return NumberFormatter.format(value, format);
    }

    /**
     * Returns the format for a numeric property.
     *
     * @param property the property
     * @param edit     if <tt>true</tt> format the number for editing
     * @return a format for the property
     */
    public static NumberFormat getFormat(Property property, boolean edit) {
        NumberFormat format;
        if (property.isMoney()) {
            if (edit) {
                format = NumberFormatter.getFormat(NumberFormatter.DECIMAL_EDIT);
            } else {
                format = NumberFormatter.getCurrencyFormat();
            }
        } else if (property.getType().isAssignableFrom(Float.class)
                   || property.getType().isAssignableFrom(Double.class)
                   || property.getType().isAssignableFrom(BigDecimal.class)) {
            format = (edit) ? NumberFormatter.getFormat(NumberFormatter.DECIMAL_EDIT) : NumberFormatter.getFormat(NumberFormatter.DECIMAL_VIEW);
        } else {
            format = (edit) ? NumberFormatter.getFormat(NumberFormatter.INTEGER_EDIT) : NumberFormatter.getFormat(NumberFormatter.INTEGER_VIEW);
        }
        return format;
    }

}
