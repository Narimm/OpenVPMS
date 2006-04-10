package org.openvpms.web.component.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import nextapp.echo2.app.ApplicationInstance;
import org.apache.commons.lang.StringUtils;

import org.openvpms.web.resource.util.Messages;

/**
 * Helper to format dates.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class DateFormatter {

    /**
     * Date edit pattern.
     */
    private static final String EDIT_PATTERN;

    /**
     * Date view pattern.
     */
    private static final String VIEW_PATTERN;

    /**
     * Format a date.
     *
     * @param date the date to format
     * @param edit if <code>true</code> format the number for editing
     * @return the formatted date
     */
    public static String format(Date date, boolean edit) {
        return getFormat(edit).format(date);
    }

    /**
     * Returns a date format.
     *
     * @param edit if <code>true</code> return a format for editingl otherwise
     *             return a format for viewing dates
     * @return a date format
     */
    public static DateFormat getFormat(boolean edit) {
        DateFormat format;
        Locale locale = ApplicationInstance.getActive().getLocale();
        String pattern = (edit) ? EDIT_PATTERN : VIEW_PATTERN;
        if (pattern == null) {
            if (edit) {
                // specify SHORT style when parsing, so that 2 digit years
                // are handled correctly
                format = DateFormat.getDateInstance(DateFormat.SHORT, locale);
            } else {
                format = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
            }
        } else {
            format = new SimpleDateFormat(pattern, locale);
        }
        return format;
    }

    static {
        String edit = Messages.get("date.format.edit", true);
        EDIT_PATTERN = (!StringUtils.isEmpty(edit)) ? edit : null;

        String view = Messages.get("date.format.view", true);
        VIEW_PATTERN = (!StringUtils.isEmpty(view)) ? view : null;
    }
}
