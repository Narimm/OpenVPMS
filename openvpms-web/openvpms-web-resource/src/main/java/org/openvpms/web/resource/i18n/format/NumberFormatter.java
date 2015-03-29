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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.resource.i18n.format;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.web.resource.i18n.Messages;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;


/**
 * Helper to format numbers for viewing and editing.
 *
 * @author Tim Anderson
 */
public class NumberFormatter {

    /**
     * Decimal edit pattern key.
     */
    public static final String DECIMAL_EDIT = "decimal.format.edit";

    /**
     * Decimal view pattern key.
     */
    public static final String DECIMAL_VIEW = "decimal.format.view";

    /**
     * Integer edit pattern key.
     */
    public static final String INTEGER_EDIT = "integer.format.edit";

    /**
     * Integer view pattern key.
     */
    public static final String INTEGER_VIEW = "integer.format.view";

    /**
     * The logger.
     */
    public static final Log log = LogFactory.getLog(NumberFormatter.class);

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
     * @param value  the number to format. May be {@code null}
     * @param format the formatter
     * @return the formatted number, or {@code null} if {@code value} is {@code null}
     */
    public static String format(Number value, NumberFormat format) {
        String result;
        if (value != null) {
            try {
                result = format.format(value);
            } catch (IllegalArgumentException exception) {
                result = value.toString();
            }
        } else {
            result = null;
        }
        return result;
    }

    /**
     * Formats a currency amount.
     * <p/>
     * When using {@link Messages}, this should be used to format amounts instead of using currency MessageFormats.
     * This is due to the fact that {@link Messages} uses the browser's locale to format messages, which may have a
     * different currency symbol to that of the practice.
     *
     * @param amount the amount. May be {@code null}
     * @return the formatted amount, or {@code null} if {@code value} is {@code null}
     */
    public static String formatCurrency(Number amount) {
        String result;
        if (amount != null) {
            try {
                result = getCurrencyFormat().format(amount);
            } catch (IllegalArgumentException exception) {
                result = amount.toString();
            }
        } else {
            result = null;
        }
        return result;
    }

    /**
     * Returns a number format for the specified key.
     *
     * @param key the key
     * @return the corresponding locale
     */
    public static NumberFormat getFormat(String key) {
        Locale locale = Messages.getLocale();
        String pattern = Messages.get(key);
        try {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
            return new DecimalFormat(pattern, symbols);
        } catch (Exception exception) {
            log.error("Failed to create format for key=" + key + ", locale=" + locale + ", pattern=" + pattern,
                      exception);
            return NumberFormat.getInstance();
        }
    }

    /**
     * Returns the currency format.
     *
     * @return the currency format
     */
    public static NumberFormat getCurrencyFormat() {
        // TODO - should use the configured currency's format, rather than the default Locale's.
        // Doesn't appear to be a simple way of going from Currency -> NumberFormat
        return NumberFormat.getCurrencyInstance();
    }
}