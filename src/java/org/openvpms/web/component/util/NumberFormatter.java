package org.openvpms.web.component.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import nextapp.echo2.app.ApplicationInstance;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;


/**
 * Helper to format numbers.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class NumberFormatter {

    /**
     * Format a number according to its node descriptor.
     *
     * @param value      the number to format
     * @param descriptor the node descriptor
     * @param edit       if <code>true</code> format the number for editing
     * @return the formatted number
     */
    public static String format(Number value, NodeDescriptor descriptor,
                                boolean edit) {
        NumberFormat format = getFormat(descriptor, edit);
        return format(value, format);
    }

    /**
     * Returns the format for a numeric property.
     *
     * @param descriptor the property descriptor
     * @param edit       if <code>true</code> format the number for editing
     * @return a format for the property
     */
    public static NumberFormat getFormat(NodeDescriptor descriptor,
                                         boolean edit) {
        NumberFormat format;
        if (descriptor.isMoney()
            || descriptor.getClazz().isAssignableFrom(Float.class)
            || descriptor.getClazz().isAssignableFrom(Double.class)
            || descriptor.getClazz().isAssignableFrom(BigDecimal.class)) {
            if (edit) {
                format = new DecimalFormat("###0.00");
            } else {
                format = new DecimalFormat("#,##0.00;(#,##0.00)");
            }
        } else {
            if (edit) {
                format = new DecimalFormat("###0");
            } else {
                format = new DecimalFormat("#,##0");
            }
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
        Locale locale = ApplicationInstance.getActive().getLocale();
        if (value instanceof Long || value instanceof Integer ||
            value instanceof Short || value instanceof Byte) {
            format = NumberFormat.getIntegerInstance(locale);
        } else {
            format = new DecimalFormat("#,##0.00;(#,##0.00)");
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

}
