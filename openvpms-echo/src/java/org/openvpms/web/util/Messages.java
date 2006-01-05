package org.openvpms.web.util;

import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.text.DateFormat;
import java.text.MessageFormat;

import nextapp.echo2.app.ApplicationInstance;


/**
 * A utility class that provides resources for obtaining localized messages.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public final class Messages {

    /**
     * Prevemt construction of utility class.
     */
    private Messages() {
    }

    /**
     * Resource bundle name.
     */
    private static final String BUNDLE_NAME
            = "org.openvpms.web.resource.localisation.Messages";

    /**
     * A map which contains <code>DateFormat</code> objects for various
     * locales.
     */
    private static final Map DATE_FORMAT_MEDIUM_MAP = new HashMap();

    /**
     * Formats a date with the specified locale.
     *
     * @param date the date to be formatted.
     * @return a localised String representation of the date
     */
    public static String formatDateTimeMedium(Date date) {
        Locale locale = ApplicationInstance.getActive().getLocale();
        DateFormat format;
        synchronized (DATE_FORMAT_MEDIUM_MAP) {
            format = (DateFormat) DATE_FORMAT_MEDIUM_MAP.get(locale);
            if (format == null) {
                format = DateFormat.getDateTimeInstance(
                        DateFormat.MEDIUM, DateFormat.MEDIUM, locale);
                DATE_FORMAT_MEDIUM_MAP.put(locale, format);
            }
        }
        return (date == null) ? null : format.format(date);
    }

    /**
     * Returns a localised, formatted message.
     *
     * @param key the key of the message to be returned
     * @param arguments an array of arguments to be inserted into the message
     */
    public static String getString(String key, Object ... arguments) {
        Locale locale = ApplicationInstance.getActive().getLocale();
        String pattern = getString(key);
        MessageFormat format = new MessageFormat(pattern, locale);
        return format.format(arguments);
    }

    /**
     * Returns localised text.
     *
     * @param key the key of the text to be returned
     * @return the appropriate localized text (if the key is not defined,
     *         the string "!key!" is returned)
     */
    public static String getString(String key) {
        String result;
        try {
            Locale locale = ApplicationInstance.getActive().getLocale();
            ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
            result = bundle.getString(key);
        } catch (MissingResourceException exception) {
            result = '!' + key + '!';
        }
        return result;
    }
}
