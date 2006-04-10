package org.openvpms.web.component.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.web.resource.util.Messages;


/**
 * Helper to format numbers.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class NumberFormatter {

    /**
     * Decimal edit format.
     */
    private static final DecimalFormat DECIMAL_EDIT
            = new DecimalFormat(Messages.get("decimal.format.edit"));

    /**
     * Decimal view format.
     */
    private static final DecimalFormat DECIMAL_VIEW
            = new DecimalFormat(Messages.get("decimal.format.view"));

    /**
     * Integer edit format.
     */
    private static final DecimalFormat INTEGER_EDIT
            = new DecimalFormat(Messages.get("integer.format.edit"));

    /**
     * Integer view format.
     */
    private static final DecimalFormat INTEGER_VIEW
            = new DecimalFormat(Messages.get("integer.format.view"));


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
            format = (edit) ? DECIMAL_EDIT : DECIMAL_VIEW;
        } else {
            format = (edit) ? INTEGER_EDIT : INTEGER_VIEW;
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
            format = INTEGER_VIEW;
        } else {
            format = DECIMAL_VIEW;
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
