package org.openvpms.web.component.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import nextapp.echo2.app.ApplicationInstance;


/**
 * Helper to format numbers.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class NumberFormatter {

    /**
     * Format a number.
     *
     * @param value the number to format
     * @return the formatted number
     */
    public static String format(Number value) {
        String result;
        NumberFormat format;
        Locale locale = ApplicationInstance.getActive().getLocale();
        if (value instanceof Long || value instanceof Integer ||
            value instanceof Short || value instanceof Byte) {
            format = NumberFormat.getIntegerInstance(locale);
        } else {
            format = new DecimalFormat("#,##0.00;(#,##0.00)");
        }
        try {
            // @todo - potential loss of precision here as NumberFormat converts
            // BigDecimal to double before formatting
            result = format.format(value);
        } catch (IllegalArgumentException exception) {
            result = value.toString();
        }
        return result;
    }
}
