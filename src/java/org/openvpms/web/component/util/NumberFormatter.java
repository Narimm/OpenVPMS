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

package org.openvpms.web.component.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.resource.util.Messages;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;


/**
 * Helper to format numbers.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class NumberFormatter {

    /**
     * Decimal edit pattern key.
     */
    private static final String DECIMAL_EDIT = "decimal.format.edit";

    /**
     * Decimal view pattern key.
     */
    private static final String DECIMAL_VIEW = "decimal.format.view";

    /**
     * Integer edit pattern key.
     */
    private static final String INTEGER_EDIT = "integer.format.edit";

    /**
     * Integer view pattern key.
     */
    private static final String INTEGER_VIEW = "integer.format.view";

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(NumberFormatter.class);


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
        return format(value, format);
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
                format = getFormat(DECIMAL_EDIT);
            } else {
                format = NumberFormat.getCurrencyInstance();
            }
        } else if (property.getType().isAssignableFrom(Float.class)
                || property.getType().isAssignableFrom(Double.class)
                || property.getType().isAssignableFrom(BigDecimal.class)) {
            format = (edit) ? getFormat(DECIMAL_EDIT) : getFormat(DECIMAL_VIEW);
        } else {
            format = (edit) ? getFormat(INTEGER_EDIT) : getFormat(INTEGER_VIEW);
        }
        return format;
    }

    /**
     * Format a number.
     *
     * @param value the number to format
     * @return the formatted number
     */
    public static String format(Number value) {
        NumberFormat format;
        if (value instanceof Long || value instanceof Integer ||
                value instanceof Short || value instanceof Byte) {
            format = getFormat(INTEGER_VIEW);
        } else {
            format = getFormat(DECIMAL_VIEW);
        }
        return format(value, format);
    }

    /**
     * Format a number.
     *
     * @param value  the number to format
     * @param format the formatter
     * @return the formatted number
     */
    public static String format(Number value, NumberFormat format) {
        String result;
        try {
            // @todo - potential loss of precision here as NumberFormat converts
            // BigDecimal to double before formatting
            result = format.format(value);
        } catch (IllegalArgumentException exception) {
            result = value.toString();
        }
        return result;
    }

    /**
     * Returns a number format for the specified key.
     *
     * @param key the key
     * @return the corresponding locale
     */
    private static NumberFormat getFormat(String key) {
        Locale locale = Messages.getLocale();
        String pattern = Messages.get(key);
        try {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
            return new DecimalFormat(pattern, symbols);
        } catch (Exception exception) {
            log.error("Failed to create format for key=" + key + ", locale="
                    + locale + ", pattern=" + pattern, exception);
            return NumberFormat.getInstance();
        }
    }

}
